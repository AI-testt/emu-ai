package ains.network.utils;

import ains.config.ConfigData;
import ains.network.Network;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static ains.Main.core;

/**
 * WebSocket listener for browser clients running the SWF in Ruffle. Ruffle cannot open raw TCP sockets, so its
 * {@code socketProxy} option tunnels the flash.net.Socket byte stream through a WebSocket instead. Each frame's
 * payload is unwrapped into a plain ByteBuf and fed into the exact same framer/decoder/handler chain as the TCP
 * listener ({@link ChannelHandler}); outgoing ByteBufs are wrapped back into binary frames.
 */
public class WebSocketChannelHandler extends ChannelInitializer<SocketChannel> {

    private static final int MAX_FRAME_SIZE = 65536;

    private final Network network;

    public WebSocketChannelHandler(Network network) {
        this.network = network;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebSocketChannelHandler.class);

    /** Set once the WebSocket handshake completed (read by HandshakeProbe when the channel closes). */
    private static final io.netty.util.AttributeKey<Boolean> HANDSHAKE_DONE = io.netty.util.AttributeKey.valueOf("wsHandshakeDone");

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // Behind a TLS reverse proxy (needed for wss:// from https pages) every connection comes from the proxy's
        // address, so the per-IP limit/ban filter is applied after the handshake using X-Real-IP instead.
        boolean viaProxy = isTrustedProxy(socketChannel.remoteAddress().getAddress());
        if (!viaProxy) {
            pipeline.addLast("ipFilter", new CustomFilter(this.network));
        }
        // see ChannelHandler: closes connections that went silent without a close (client pings every 20s)
        if (ConfigData.SERVER_IDLE_TIMEOUT > 0) {
            pipeline.addLast("idle", new IdleStateHandler(ConfigData.SERVER_IDLE_TIMEOUT, 0, 0, TimeUnit.SECONDS));
        }
        pipeline.addLast("wsProbe", new HandshakeProbe());
        pipeline.addLast("http", new HttpServerCodec());
        pipeline.addLast("httpAggregator", new HttpObjectAggregator(MAX_FRAME_SIZE));
        // any path (e.g. /ws/5582 behind nginx); handles the handshake, ping/pong and close frames
        pipeline.addLast("ws", new WebSocketServerProtocolHandler("/", null, true, MAX_FRAME_SIZE, false, true));
        pipeline.addLast("wsSession", new HandshakeListener(this.network, viaProxy));
        pipeline.addLast("wsDecoder", new FrameToByteBufDecoder());
        pipeline.addLast("wsEncoder", new ByteBufToFrameEncoder());
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(ConfigData.MAX_FRAME_LENGTH, Delimiters.nulDelimiter()));
        pipeline.addLast("traffic", core().getGtsh());
        pipeline.addLast("gameDecoder", new GameDecoder());
        pipeline.addLast("handler", new ConnectionHandler(this.network));
    }

    /**
     * Logs what actually arrives on the WebSocket port before the HTTP codec sees it, so failed handshakes are not
     * silent: TLS on the plain port (page opened over https -> wss://), non-HTTP clients (e.g. a Flash client pointed
     * at the ws port), the handshake result and why the connection ended.
     */
    private static final class HandshakeProbe extends io.netty.channel.ChannelDuplexHandler {

        private boolean firstRead = true;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (firstRead && msg instanceof ByteBuf buf && buf.isReadable()) {
                firstRead = false;
                short first = buf.getUnsignedByte(buf.readerIndex());
                if (first == 0x16) {
                    log.warn("[ws] TLS handshake received from {} on plain ws port - the page is https so Ruffle uses "
                            + "wss://; put a TLS reverse proxy in front of this port (or open the page over http)",
                            ctx.channel().remoteAddress());
                } else {
                    String head = buf.toString(buf.readerIndex(), Math.min(buf.readableBytes(), 120),
                            java.nio.charset.StandardCharsets.UTF_8);
                    String firstLine = head.split("\r?\n", 2)[0];
                    if (firstLine.startsWith("GET ")) {
                        log.info("[ws] HTTP request from {}: {}", ctx.channel().remoteAddress(), firstLine);
                    } else {
                        log.warn("[ws] Non-HTTP data from {} on ws port (Flash client pointed at ws port?): {}",
                                ctx.channel().remoteAddress(), firstLine);
                    }
                }
            }
            super.channelRead(ctx, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.warn("[ws] Error on {}: {}", ctx.channel().remoteAddress(), cause.toString());
            super.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (!Boolean.TRUE.equals(ctx.channel().attr(HANDSHAKE_DONE).get())) {
                log.warn("[ws] {} closed before WebSocket handshake completed{}", ctx.channel().remoteAddress(),
                        firstRead ? " (no bytes received - client gave up / blocked by browser)" : "");
            }
            super.channelInactive(ctx);
        }
    }

    /**
     * Sits after the protocol handler (HandshakeComplete is only fired towards the tail). Logs the handshake and, for
     * connections coming through the trusted reverse proxy, applies the per-IP limit/ban filter to the real client
     * address from X-Real-IP / X-Forwarded-For.
     */
    private static final class HandshakeListener extends io.netty.channel.ChannelInboundHandlerAdapter {

        private final Network network;
        private final boolean viaProxy;

        HandshakeListener(Network network, boolean viaProxy) {
            this.network = network;
            this.viaProxy = viaProxy;
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete hc) {
                ctx.channel().attr(HANDSHAKE_DONE).set(true);
                String clientIp = null;
                if (viaProxy) {
                    clientIp = realIp(hc.requestHeaders());
                    if (clientIp == null) {
                        log.warn("[ws] Proxied connection from {} without X-Real-IP/X-Forwarded-For - closing",
                                ctx.channel().remoteAddress());
                        ctx.close();
                        return;
                    }
                    java.net.InetAddress address;
                    try {
                        address = com.google.common.net.InetAddresses.forString(clientIp);
                    } catch (IllegalArgumentException e) {
                        log.warn("[ws] Invalid client IP '{}' from proxy - closing", clientIp);
                        ctx.close();
                        return;
                    }
                    if (!new CustomFilter(this.network).accept(ctx, new java.net.InetSocketAddress(address, 0))) {
                        ctx.close();
                        return;
                    }
                }
                log.info("[ws] Handshake complete from {}{} (uri={}, origin={})", ctx.channel().remoteAddress(),
                        clientIp != null ? " for client " + clientIp : "", hc.requestUri(),
                        hc.requestHeaders().get("Origin"));
            }
            super.userEventTriggered(ctx, evt);
        }

        private static String realIp(io.netty.handler.codec.http.HttpHeaders headers) {
            // Behind Cloudflare the proxy's $remote_addr is a Cloudflare edge IP; the player is in CF-Connecting-IP
            // (nginx forwards client headers as-is), so no nginx realip setup is needed.
            String ip = headers.get("CF-Connecting-IP");
            if (ip == null || ip.isBlank()) {
                ip = headers.get("X-Real-IP");
            }
            if (ip == null || ip.isBlank()) {
                String forwarded = headers.get("X-Forwarded-For");
                if (forwarded != null && !forwarded.isBlank()) {
                    ip = forwarded.split(",")[0];
                }
            }
            return (ip == null || ip.isBlank()) ? null : ip.trim();
        }
    }

    private static boolean isTrustedProxy(java.net.InetAddress address) {
        String host = address.getHostAddress();
        for (String trusted : ains.config.ConfigData.WS_TRUSTED_PROXIES) {
            if (trusted.equals(host) || (trusted.equals("loopback") && address.isLoopbackAddress())) {
                return true;
            }
        }
        return false;
    }

    private static final class FrameToByteBufDecoder extends MessageToMessageDecoder<WebSocketFrame> {
        @Override
        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) {
            if (frame instanceof BinaryWebSocketFrame || frame instanceof TextWebSocketFrame
                    || frame instanceof ContinuationWebSocketFrame) {
                out.add(frame.content().retain());
            }
        }
    }

    private static final class ByteBufToFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
            out.add(new BinaryWebSocketFrame(msg.retain()));
        }
    }
}
