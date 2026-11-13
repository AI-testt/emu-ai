package ains;

import ains.config.ConfigData;
import ains.network.Network;
import ains.network.PolicyServer;
import ains.network.api.ServerNetwork;
import ains.network.sfs.Room;
import ains.network.sfs.User;
import ains.requests.IRequest;
import ains.requests.RequestWorker;
import ains.requests.threadfactory.RequestThreadFactory;
import ains.world.World;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ains.network.manager.RoomManager.roomManager;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Main instance;
    private World world;
    private static long startTime;
    private GlobalTrafficShapingHandler gtsh;
    private static ServerNetwork serverNetwork;

    private final ConcurrentHashMap<InetAddress, Integer> connected = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<InetAddress, Long> bannedIps = new ConcurrentHashMap<>();
    private final List<String> allowedRequestsForBannedUsers = Arrays.asList(
            "mv", "firstJoin", "afk", "isModerator", "retrieveInventory", "moveToCell", "retrieveUserData", "retrieveUserDatas", "emotea"
    );
    public final ExecutorService requestExecutor = Executors.newFixedThreadPool(
            ConfigData.REQUEST_THREADS,
            new RequestThreadFactory()
    );

    public static void main(String[] args) {
        Main current = core();
    }

    public Main() {
        world = World.world();
        log.info("Core initialized");
        world.db.getJdbc().run("UPDATE servers SET Online = 1 WHERE Name = ?", new Object[]{ConfigData.SERVER_NAME});

        try {
            gtsh = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(1), 1000L);
            log.debug("Setting up server");

            serverNetwork = new Network(ConfigData.SERVER_IP, ConfigData.SERVER_PORT);
            serverNetwork.createSocket();
            serverNetwork.bind();

            new PolicyServer(ConfigData.SERVER_IP).start();
        } catch (Exception e) {
            log.error("Error while setting up server", e);
        }

        startTime = System.currentTimeMillis();
    }

    public static Main core() {
        if (instance == null) {
            log.info("new instance of Main");
            instance = new Main();
        }

        return instance;
    }

    public void handleRequest(IRequest request, String cmd, String[] params, User user, int fromRoom) {
        if (user == null) {
            return;
        }

        if (!isRequestFiltered(user, cmd)) {
        }

//        log.info("Processing request from: " + user.properties.get("username") + " - " + cmd);

        int access = (Integer) user.properties.get("access");
        if (access <= 0 && !this.allowedRequestsForBannedUsers.contains(cmd)) {
            world.send(new String[]{"warning", "Your account is currently disabled. Actions in-game are limited."}, user);
            return;
        }

        try {
            if (fromRoom != 1 && fromRoom != 32123 && fromRoom > 0) {
                Room room = roomManager().getRoom(fromRoom);
                if (room == null) {
                    world.users.kick(user);
                    return;
                }
                requestExecutor.execute(new RequestWorker(
                        request,
                        params,
                        user,
                        world,
                        room
                ));
            } else {
                Room room = roomManager().getRoom(user.getRoom());
                requestExecutor.execute(new RequestWorker(
                        request,
                        params,
                        user,
                        world,
                        room
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRequestFiltered(User user, String request) {
        return true;
    }

    public ConcurrentHashMap<InetAddress, Integer> getConnectedList() {
        return this.connected;
    }

    public ConcurrentHashMap<InetAddress, Long> getBannedIps() {
        return this.bannedIps;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public GlobalTrafficShapingHandler getGtsh() {
        return gtsh;
    }

    public void setGtsh(GlobalTrafficShapingHandler gtsh) {
        this.gtsh = gtsh;
    }
}
