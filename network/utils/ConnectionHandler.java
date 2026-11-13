package ains.network.utils;

import ains.Main;
import ains.config.ConfigData;
import ains.network.Network;
import ains.network.api.ServerEvent;
import ains.network.netty.NettyPlayerNetwork;
import ains.network.sfs.Room;
import ains.network.sfs.User;
import ains.requests.IRequest;
import ains.requests.RequestFactory;
import ains.requests.RequestWorker;
import ains.world.World;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.n3.nanoxml.IXMLElement;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static ains.network.manager.RoomManager.roomManager;
import static ains.network.manager.UserManager.userManager;
import static ains.network.sfs.SFS.sfs;

public class ConnectionHandler extends SimpleChannelInboundHandler<ServerEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private Network network;
    private final String XML = "xml";
    private final String JSO = "jso";
    private final String STR = "str";
    private static final String PING = "ping";
    private static final String PONG = "{\"cmd\":\"pong\"}";

    public ConnectionHandler(Network network) {
        this.network = network;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerEvent serverEvent) throws Exception {
        try {
            User player = ctx.channel().attr(User.PLAYER_KEY).get();

            if (serverEvent == null)
                return;

            if (player == null)
                return;

            switch (serverEvent.getTypeFormat()) {
                case XML -> {
                    switch (serverEvent.getType()) {
                        case "sys":
                            IXMLElement bodyTag = serverEvent.getXmlMsg();
                            String eventName = bodyTag.getAttribute("action", "");
                            IRequest request = RequestFactory.EVENTS.create(eventName);
                            request.process(bodyTag, ctx.channel());
                            break;
                        default:
                            ctx.close();
                    }
                }

                case STR -> {
                    if (serverEvent.getType().equalsIgnoreCase("xt")) {
//                        log.info("xt: {}", serverEvent.getStrMsg());
                        String[] params = this.splitParameters(serverEvent.getStrMsg());
                        String[] xParams = new String[params.length - 3];
                        String xtCmd = params[1];
                        int fromRoom = Integer.parseInt(params[2]);
                        System.arraycopy(params, 3, xParams, 0, params.length - 3);

//                        log.info("{}, args: {}", xtCmd, Arrays.toString(xParams));

//                        if (this.isRequestFiltered(player, xtCmd)) {
//                            return;
//                        }

                        IRequest request = RequestFactory.REQUESTS.create(xtCmd);
                        Main.core().handleRequest(request, xtCmd, xParams, player, fromRoom);
                    }
                }

                case JSO -> {
                    JSONObject msg = serverEvent.getJsonMsg();

                    // client heartbeat: answered right here (also before login) so the client can tell the link is alive
                    if (PING.equals(msg.optString("cmd"))) {
                        sfs().sendResponse(PONG, ctx.channel());
                        return;
                    }
//                    log.info("cmd: {}", msg.getString("cmd"));
//                    log.info("msg: {}", msg.getJSONArray("args"));
//                    log.info("chan: {}", ctx.channel());

                    switch (serverEvent.getType()) {
                        case "event" -> {
                            IRequest request = RequestFactory.EVENTS.create(msg.getString("cmd"));
                            request.process(msg.getJSONArray("args"), ctx.channel());
                        }
                        case "request" -> {
//                            log.info("ini request");

                            String[] params = new String[msg.getJSONArray("args").size()];
                            for (int i = 0; i < msg.getJSONArray("args").size(); i++) {
                                params[i] = msg.getJSONArray("args").getString(i);
                            }

                            IRequest request = RequestFactory.REQUESTS.create(msg.getString("cmd"));
                            Main.core().handleRequest(request, msg.getString("cmd"), params, player, msg.getInt("room"));

//                            Main.core().handleRequest(
//                                    request,
//                                    msg.getString("cmd"),
//                                    msg.getJSONArray("args"),
//                                    player,
//                                    msg.getInt("room")
//                            );
                        }
//                        case "request" -> {
//                            String[] params = new String[msg.getJSONArray("args").size()];
//                            for (int i = 0; i < msg.getJSONArray("args").size(); i++) {
//                                params[i] = msg.getJSONArray("args").getString(i);
//                            }
//                            IRequest request = RequestFactory.REQUESTS.create(msg.getString("cmd"));
//                            Main.core().requestExecutor.execute(new RequestWorker(
//                                    request,
//                                    params,
//                                    player
//                            ));
//                        }
                        default -> {
                            log.warn("Unknown event type from {} - closing connection", ctx.channel().remoteAddress());
                            if (ctx.channel().isActive()) {
                                ctx.close();
                            }
                        }
                    }
                }

                default -> {
                    log.warn("Unknown event type from {} - closing connection", ctx.channel().remoteAddress());
                    if (ctx.channel().isActive()) {
                        ctx.close();
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Could not handle message: ", ex);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        network.getChannels().remove(ctx.channel());
        User user = ctx.channel().attr(User.PLAYER_KEY).get();
        if (user == null) {
            return;
        }

        // every step is guarded: if one throws, the user must still leave UserManager or it stays behind as a
        // ghost session that blocks the next login of that character
        try {
            Room room = roomManager().getRoom(user.getRoom());
            if (room != null) {
                JSONObject userGone = new JSONObject();
                userGone.put("cmd", "userGone");
                userGone.put("networkId", user.getUserId());
                sfs().sendResponseRemoveUser(userGone.toString(), user, room.getChannellList());

                World.world().rooms.exit(room, user);
                room.removeUser(user);
                if (room.getUserCount() <= 0) {
                    roomManager().removeRoom(room.getId());
                }
            }
        } catch (Exception ex) {
            log.error("[{}] Error removing user from room on disconnect", user.getUserId(), ex);
        }

        // false when a newer login of the same character already took this session over (Login cleaned it up then)
        boolean wasOnline = userManager().removeUser(user);

        // database work stays off the Netty event loop: a slow query here would freeze every player on this thread
        Main.core().requestExecutor.execute(() -> {
            if (wasOnline) {
                try {
                    World.world().users.lost(user);
                } catch (Exception ex) {
                    log.error("[{}] Error cleaning up user {} on disconnect", user.getUserId(), user.properties.get("username"), ex);
                }
            }
            try {
                World.world()
                        .db
                        .getJdbc()
                        .run("UPDATE servers SET Count = ? WHERE Name = ?", new Object[]{userManager().getUserCount(), ConfigData.SERVER_NAME});
            } catch (Exception ex) {
                log.error("Error updating server player count", ex);
            }
        });

        log.info("[{}] Disconnecting ip {} ", user.getNetwork().getConnectionId(), remoteIp(ctx));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idle && idle.state() == IdleState.READER_IDLE) {
            User user = ctx.channel().attr(User.PLAYER_KEY).get();
            log.info("[{}] No data from {} for {}s - closing dead connection",
                    user != null ? user.getNetwork().getConnectionId() : "?", remoteIp(ctx), ConfigData.SERVER_IDLE_TIMEOUT);
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        User user = new User(new NettyPlayerNetwork(ctx.channel(), ctx.channel().hashCode()));
        ctx.channel().attr(User.PLAYER_KEY).set(user);
        if (!network.getChannels().add(ctx.channel())) {
            ctx.disconnect();
        } else {
            log.info("[{}] Connection from {} on port {}", user.getNetwork().getConnectionId(), ctx.channel().remoteAddress().toString().replace("/", "").split(":")[0],
                    ((java.net.InetSocketAddress) ctx.channel().localAddress()).getPort());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            // "Connection reset" etc. - the client went away, nothing to debug
            log.info("Connection from {} lost: {}", remoteIp(ctx), cause.getMessage());
        } else if (cause instanceof TooLongFrameException) {
            log.warn("Oversized packet from {} - closing connection: {}", remoteIp(ctx), cause.getMessage());
        } else {
            log.error("Netty error occurred: ", cause);
        }
        // leave no half-working channel behind: the client sees the close and shows the disconnect screen
        ctx.close();
    }

    private static String remoteIp(ChannelHandlerContext ctx) {
        Object address = ctx.channel().remoteAddress();
        return address == null ? "unknown" : address.toString().replace("/", "").split(":")[0];
    }

    private boolean isRequestFiltered(User user, String request) {
        return true;
    }

    private String[] splitParameters(String str) {
        return str.split("\\%");
    }

}
