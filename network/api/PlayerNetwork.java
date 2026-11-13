package ains.network.api;

import io.netty.channel.Channel;

public abstract class PlayerNetwork {

    private int connectionId;

    public PlayerNetwork(int connectionId) {
        this.connectionId = connectionId;
    }

    public abstract void close();

    public int getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public abstract void flush();

    public abstract void send(String[] var1);

    public abstract void sendQueued(String[] var1);

    public abstract Channel getChannel();
}
