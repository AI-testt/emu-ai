package ains.network.utils;

import ains.Main;
import ains.config.ConfigData;
import ains.network.Network;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

@Sharable
public class CustomFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {

    public static Logger log = LoggerFactory.getLogger(CustomFilter.class);

    private final Map<InetAddress, Integer> connected = Main.core().getConnectedList();
    private final Map<InetAddress, Long> bannedIps = Main.core().getBannedIps();
    private Network network;

    public CustomFilter(Network network) {
        this.network = network;
    }

    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        InetAddress remoteIp = remoteAddress.getAddress();
        if (this.bannedIps.containsKey(remoteIp)) {
            if (this.bannedIps.get(remoteIp) + 1800000L > System.currentTimeMillis()) {
                return false;
            }

            this.bannedIps.remove(remoteIp);
        }

        // connection.max_connection = concurrent connections allowed per IP; 0 or less disables the limit
        int limit = ConfigData.MAX_CONNECTION;
        int current = this.connected.merge(remoteIp, 1, Integer::sum);
        if (limit > 0 && current > limit) {
            // reject only the extra connection; banning the IP would also drop every other player behind the same NAT
            this.connected.computeIfPresent(remoteIp, (ip, n) -> n > 1 ? n - 1 : null);
            log.warn("Connection limit ({}) reached for {} - rejecting new connection", limit, remoteIp.getHostAddress());
            return false;
        }

        ctx.channel().closeFuture().addListener((ChannelFutureListener) future ->
                this.connected.computeIfPresent(remoteIp, (ip, n) -> n > 1 ? n - 1 : null));
        return true;
    }
}
