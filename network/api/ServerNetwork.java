package ains.network.api;

public abstract class ServerNetwork {

    private Integer port;
    private String ip;

    public ServerNetwork(String ip, Integer port) {
        this.port = port;
        this.ip = ip;
    }

    public abstract void createSocket();

    public abstract boolean bind();

    public Integer getPort() {
        return this.port;
    }

    public String getIp() {
        return this.ip;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
