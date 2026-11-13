package ains.network;

import ains.network.api.ServerNetwork;
import ains.config.ConfigData;
import ains.network.utils.ChannelHandler;
import ains.network.utils.WebSocketChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Network extends ServerNetwork {

    private static final Logger log = LoggerFactory.getLogger(Network.class);

    private static final int BUFFER_SIZE = 5120;
    private static final boolean IS_EPOLL_AVAILABLE = Epoll.isAvailable();
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    private DefaultChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private ServerBootstrap bootstrap = new ServerBootstrap();
    private ServerBootstrap WebSocket = new ServerBootstrap();

    public Network(String ip, Integer port) {
        super(ip, port);
    }

    @Override
    public void createSocket() {
        EventLoopGroup bossGroup = IS_EPOLL_AVAILABLE ? new EpollEventLoopGroup(THREADS) : new NioEventLoopGroup(THREADS);
        EventLoopGroup workGroup = IS_EPOLL_AVAILABLE ? new EpollEventLoopGroup(THREADS) : new NioEventLoopGroup(THREADS);

        // Same game pipeline over WebSocket, for browser clients (Ruffle socketProxy). Disabled when server.ws_port <= 0.
        if (ConfigData.SERVER_WS_PORT > 0) {
            WebSocket
                    .group(bossGroup, workGroup)
                    .channel(IS_EPOLL_AVAILABLE ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new WebSocketChannelHandler(this))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        try {
            bootstrap
                    .group(bossGroup, workGroup)
                    .channel(IS_EPOLL_AVAILABLE ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelHandler(this))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_RCVBUF, 5120)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(5120))
                    .childOption(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
        } catch (Exception ex) {
            log.error("Error creating socket {}", ex.getMessage());
        }
    }

    @Override
    public boolean bind() {
        this.bootstrap.bind(new InetSocketAddress(this.getIp(), this.getPort())).addListener(objectFuture -> {
            if (objectFuture.isSuccess()) {
                log.info("Game socket listening on {}:{}", this.getIp(), this.getPort());
            } else {
                log.error("Failed to bind game port {}", this.getPort(), objectFuture.cause());
            }
        });

        if (ConfigData.SERVER_WS_PORT > 0) {
            this.WebSocket.bind(new InetSocketAddress(ConfigData.SERVER_WS_IP, ConfigData.SERVER_WS_PORT)).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("WebSocket listening on {}:{}", ConfigData.SERVER_WS_IP, ConfigData.SERVER_WS_PORT);
                } else {
                    log.error("Failed to bind WebSocket port {}", ConfigData.SERVER_WS_PORT, future.cause());
                }
            });
        }

        return false;
    }

    public DefaultChannelGroup getChannels() {
        return this.channels;
    }
}
