package ains.network.sfs;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;

public class SFS {

    private final Logger log = LoggerFactory.getLogger(SFS.class);
    private static final SFS _instance = new SFS();

    public void sendResponse(String params, Channel chan) {
        write(params, chan);
    }

    public void sendResponse(String[] params, int fromRoom, Channel channel) {
        StringBuffer response = new StringBuffer("%").append("xt").append("%");
        response.append(params[0]).append("%");
        response.append(fromRoom).append("%");

        for (int i = 1; i < params.length; i++) {
            response.append(params[i]).append("%");
        }

        write(response.toString(), channel);
    }

    public void sendResponse(JSONObject jso, int fromRoom, Channel channel) {
        try {
            write(new JSONObject()
                    .element("t", "xt")
                    .element("b", new JSONObject()
                            .element("r", fromRoom)
                            .element("o", jso)
                    ).toString(), channel);
        } catch (Exception jsoEx) {
            this.log.error("Error creating JSON response: ", jsoEx);
        }
    }

    public void sendResponse(String[] params, int fromRoom, LinkedList<Channel> channels) {
        StringBuffer response = new StringBuffer("%").append("xt").append("%");
        response.append(params[0]).append("%");
        response.append(fromRoom).append("%");

        for (int i = 1; i < params.length; i++) {
            response.append(params[i]).append("%");
        }

        for (Channel channel : channels) {
            write(response.toString(), channel);
        }
    }

    public void sendResponse(JSONObject jso, int fromRoom, LinkedList<Channel> channels) {
        try {
            for (Channel channel : channels) {
//                write(new JSONObject()
//                        .element("t", "xt")
//                        .element("b", new JSONObject()
//                                .element("r", fromRoom)
//                                .element("o", jso)
//                        ).toString(), channel);

                write(jso.toString(), channel);
            }
        } catch (Exception jsoEx) {
            this.log.error("Error creating JSON response: ", jsoEx);
        }
    }

    public void sendResponseRemoveUser(String params, User user, LinkedList<Channel> channels) {
        for (Channel chan : channels) {
            if (!user.getNetwork().getChannel().equals(chan)) {
                write(params, chan);
            }
        }
    }

    public void sendResponseRemoveUser(String[] params, int fromRoom, User user, LinkedList<Channel> channels) {
        for (Channel channel : channels) {
            if (!user.getNetwork().getChannel().equals(channel)) {
                StringBuffer response = new StringBuffer("%").append("xt").append("%");
                response.append(params[0]).append("%");
                response.append(fromRoom).append("%");

                for (int i = 1; i < params.length; i++) {
                    response.append(params[i]).append("%");
                }

                write(response.toString(), channel);
            }
        }
    }

    private void write(String content, Channel channel) {
        log.debug(">>> '{}'", content);

        channel.writeAndFlush(Unpooled.copiedBuffer(content + '\0', CharsetUtil.UTF_8), channel.voidPromise());
    }

    public StringBuffer makeHeader(String type, int room, String action) {
        StringBuffer sb = new StringBuffer();
        sb.append("<msg t='").append(type);
        sb.append("'><body action='").append(action);
        sb.append("' r='").append(room).append("'>");
        return sb;
    }

    public static SFS sfs() {
        return _instance;
    }
}
