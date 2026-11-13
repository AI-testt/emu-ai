package ains.network.netty;

import ains.network.api.PlayerNetwork;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;

public class NettyPlayerNetwork extends PlayerNetwork {

    private Channel chan;

    public NettyPlayerNetwork(Channel chan, int connectionId) {
        super(connectionId);
        this.chan = chan;
    }

    @Override
    public Channel getChannel() {
        return this.chan;
    }

    @Override
    public void close() {
        this.chan.close();
    }

    @Override
    public void flush() {
        this.chan.flush();
    }

    @Override
    public void send(String[] msg) {
        this.chan.writeAndFlush(Unpooled.copiedBuffer(msg.toString() + '\u0000', CharsetUtil.UTF_8));
    }

    @Override
    public void sendQueued(String[] msg) {
        this.chan.write(Unpooled.copiedBuffer(msg.toString() + '\u0000', CharsetUtil.UTF_8));
    }
}
