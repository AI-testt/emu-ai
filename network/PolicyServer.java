package ains.network;

import ains.config.ConfigData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class PolicyServer {

    private static final Logger log = LoggerFactory.getLogger(PolicyServer.class);

    public static final int POLICY_PORT = 844;

    private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

    // Port 843 is machine-wide: with several emulators on one host only the first one binds it and answers for all
    // of them, so the policy must cover every game port (server.policy_ports, default "*"), not just its own.
    private static final String RESPONSE_POLICY = "<cross-domain-policy><allow-access-from domain='*' to-ports='"
            + ConfigData.POLICY_PORTS + "' /></cross-domain-policy>\0";

    private final String ip;

    public PolicyServer(String ip) {
        this.ip = ip;
    }

    public void start() {
        if (!ConfigData.POLICY_ENABLED) {
            log.info("Socket policy server disabled (server.policy_enabled=false)");
            return;
        }

        boolean epollAvailable = Epoll.isAvailable();
        EventLoopGroup group = epollAvailable ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(group, group)
                .channel(epollAvailable ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(new PolicyRequestDecoder());
                    }
                });

        bootstrap.bind(ip, POLICY_PORT).addListener(future -> {
            if (future.isSuccess()) {
                log.info("Socket policy server listening on {}:{}", ip, POLICY_PORT);
            } else {
                // Usually another emulator on this host already serves port 843 (its policy covers server.policy_ports).
                log.warn("Could not bind socket policy server on port {} ({}) - another emulator on this host probably "
                        + "serves it already; otherwise Flash clients fall back to the game port (~3s delay). "
                        + "Set server.policy_enabled=false to skip.", POLICY_PORT, future.cause().getMessage());
                group.shutdownGracefully();
            }
        });
    }

    private static class PolicyRequestDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            String data = in.toString(StandardCharsets.UTF_8);

            if (data.contains(POLICY_FILE_REQUEST)) {
                log.debug("Serving socket policy file to {}", ctx.channel().remoteAddress());
                ctx.writeAndFlush(Unpooled.copiedBuffer(RESPONSE_POLICY.getBytes(StandardCharsets.UTF_8)))
                        .addListener(ChannelFutureListener.CLOSE);
            } else if (in.readableBytes() > 128) {
                ctx.close();
            }
        }
    }
}
