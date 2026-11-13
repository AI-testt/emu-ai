package ains.network.utils;

import ains.config.ConfigData;
import ains.network.api.ServerEvent;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GameDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final Logger log = LoggerFactory.getLogger(GameDecoder.class);

    private final static char MESSAGE_XML = '<';
    private final static char MESSAGE_JSON = '{';
    private final static char MESSAGE_SFS = '%';

    private final static String MESSAGE_SFS_POLICY = "policy";

    private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

    private final static String RESPONSE_POLICY = "<cross-domain-policy><allow-access-from domain='*' to-ports='" + ConfigData.SERVER_PORT + "' /></cross-domain-policy>\0";

    private static final String XML_VER_CHK = "verChk";
    private static final String XML_LOGIN = "login";
    private static final String XML_ENTITY = "<!ENTITY";
    private static final String XML_HTTP = "http";
    private static final String XML_SYSTEM = "SYSTEM";

    private static final String JSON_BODY = "body";
    private static final String JSON_TYPE = "type";

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        byteBuf.markReaderIndex();

        if (byteBuf.readableBytes() < 6) {
            log.warn("Received packet with less than 6 bytes from {}", ctx.channel().remoteAddress());
            if (ctx.channel().isActive()) {
                ctx.close();
            }
            return;
        }

        String rawMsg = byteBuf.toString(Charsets.UTF_8);

//        log.info("BEFORE: {}", rawMsg);

        if (rawMsg.startsWith(POLICY_FILE_REQUEST)) {
            log.info("Socket policy file request from {} - sending cross-domain-policy", ctx.channel().remoteAddress());
            ChannelFuture future = ctx.writeAndFlush(Unpooled.copiedBuffer(RESPONSE_POLICY.getBytes()));
            future.addListener(ChannelFutureListener.CLOSE);
            return;
        }

//        String msg = byteBuf.toString(Charsets.UTF_8);
        String msg = dae(rawMsg);
//        String msg = byteBuf.toString(Charsets.UTF_8);
//        log.info("AFTER: {}", msg);

        log.debug("<<< {}", msg);

        switch (msg.charAt(0)) {
            case MESSAGE_XML -> {
                handleXML(ctx, list, msg);
            }
            case MESSAGE_SFS -> {
                handleSFS(ctx, list, msg);
            }
            case MESSAGE_JSON -> {
                handleJSO(ctx, list, msg);
            }
            default -> {
                log.warn("Unknown packet type from {} - closing connection", ctx.channel().remoteAddress());
                if (ctx.channel().isActive()) {
                    ctx.close();
                }
            }
        }
    }

    private void handleXML(ChannelHandlerContext ctx, List<Object> list, String msg) {
        log.debug("XML: {}", msg);

        if (msg.contains(MESSAGE_SFS_POLICY)) {
            ChannelFuture future = ctx.writeAndFlush(Unpooled.copiedBuffer(RESPONSE_POLICY.getBytes()));
            future.addListener(ChannelFutureListener.CLOSE);
            return;
        }

        try {
            if (msg.contains(XML_VER_CHK) || msg.contains(XML_LOGIN)) {
                if (msg.contains(XML_ENTITY) || msg.contains(XML_HTTP) || msg.contains(XML_SYSTEM)) {
                    log.warn("Blocked suspicious XML from {}", ctx.channel().remoteAddress());
                    return;
                }
                XMLParser.xmlParser().setReader(StdXMLReader.stringReader(msg));
                IXMLElement header = (IXMLElement) XMLParser.xmlParser().parse();
                list.add(new ServerEvent(header.getAttribute("t", ""), header.getFirstChildNamed("body"), ctx.channel()));
                return;
            }
            log.warn("User sent crafted packet (XML not allowed): {}", ctx.channel().remoteAddress());
            if (ctx.channel().isActive()) {
                ctx.close();
            }
        } catch (XMLException e) {
            log.warn("Malformed XML from {} - kicking client", ctx.channel().remoteAddress());
            if (ctx.channel().isActive()) {
                ctx.close();
            }
        }
    }

    private void handleSFS(ChannelHandlerContext ctx, List<Object> list, String msg) {
        log.debug("SFS: {}", msg);
        int msgBody = msg.indexOf(MESSAGE_SFS, 1);
        if (msgBody == -1) {
            log.warn("Bad SFS string format from {}", ctx.channel().remoteAddress());
            if (ctx.channel().isActive()) {
                ctx.close();
            }
            return;
        }
        list.add(new ServerEvent(msg.substring(1, msgBody), msg.substring(msgBody + 1), ctx.channel()));
    }

    private void handleJSO(ChannelHandlerContext ctx, List<Object> list, String msg) {
//        log.info("JSON: {}", msg);

        try {
            JSONObject jso = JSONObject.fromObject(msg);
            list.add(new ServerEvent(jso.getString(JSON_TYPE), jso.getJSONObject(JSON_BODY), ctx.channel()));
        } catch (Exception e) {
            log.warn("Malformed JSON from {} - {}", ctx.channel().remoteAddress(), e.getMessage());
            if (ctx.channel().isActive()) {
                ctx.close();
            }
        }
    }

    protected String dae(String encoded) {
        int K = 666;

        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < encoded.length(); i++) {
            char encodedChar = encoded.charAt(i);
            char decodedChar = (char) (encodedChar - K);
            decoded.append(decodedChar);
        }

        return decoded.toString();
    }
}
