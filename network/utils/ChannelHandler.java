package ains.network.utils;

import ains.config.ConfigData;
import ains.network.Network;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static ains.Main.core;

public class ChannelHandler extends ChannelInitializer<SocketChannel> {

    private final Network network;

    public ChannelHandler(Network network) {
        this.network = network;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("ipFilter", new CustomFilter(this.network));
        // a silently dropped connection (Wi-Fi switch, NAT timeout, sleep) never fires a close; the client pings
        // every 20s, so a channel that stays quiet this long is dead and gets closed by ConnectionHandler
        if (ConfigData.SERVER_IDLE_TIMEOUT > 0) {
            pipeline.addLast("idle", new IdleStateHandler(ConfigData.SERVER_IDLE_TIMEOUT, 0, 0, TimeUnit.SECONDS));
        }
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(ConfigData.MAX_FRAME_LENGTH, Delimiters.nulDelimiter()));
        pipeline.addLast("traffic", core().getGtsh());
        pipeline.addLast("gameDecoder", new GameDecoder());
        pipeline.addLast("handler", new ConnectionHandler(this.network));
    }

}
