package ains.tasks;

import ains.Main;
import ains.db.model.Area;
import ains.world.users.Users;
import ains.world.World;
import ains.network.sfs.Room;
import ains.network.sfs.User;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarzoneQueue implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(WarzoneQueue.class);
    public ConcurrentHashMap<String, LinkedBlockingQueue<User>> warzoneQueues = new ConcurrentHashMap<String, LinkedBlockingQueue<User>>();
    private World world;

    public WarzoneQueue(World world) {
        this.world = world;
		
        log.info("WarzoneQueue intialized.");
    }

    @Override
    public void run() {
        try {
            for (Map.Entry<String, LinkedBlockingQueue<User>> e : warzoneQueues.entrySet()) {
                LinkedBlockingQueue<User> pq = e.getValue();
                String warzone = e.getKey();
                Area area = world.areas.get(warzone);

                if (pq.size() == area.getMaxPlayers()) {
                    JSONObject PVPI = new JSONObject();
                    PVPI.put("cmd", "PVPI");
                    PVPI.put("warzone", warzone);

					final Room warzoneRoom = this.world.rooms.createRoom(warzone + "-" + (new Random()).nextInt(9999));

                    for (int i = 0; i < area.getMaxPlayers(); i++) {
                        final User user = pq.take();
                        user.properties.put(Users.ROOM_QUEUED, warzoneRoom);

                        if (i % 2 == 0)
                            user.properties.put(Users.PVP_TEAM, 0);
                        else
                            user.properties.put(Users.PVP_TEAM, 1);

                        this.world.send(new String[]{"server", "A new Warzone battle has started!"}, user);
                        this.world.send(PVPI, user);
                        this.world.scheduleTask(new Runnable() {
                            public void run() {
                                world.rooms.joinRoom(warzoneRoom, user, "Enter" + user.properties.get(Users.PVP_TEAM), "Spawn");
                            }
                        }, 5 + i, TimeUnit.SECONDS);
                    }
                }
			}
        } catch (InterruptedException ex) {
            log.error("WarzoneQueue interrupted, reinitializing..");
            log.error("Error message: {}", ex.getMessage());
            warzoneQueues = new ConcurrentHashMap<String, LinkedBlockingQueue<User>>();
        }
    }

    public LinkedBlockingQueue<User> getWarzoneQueue(String warzone) {
        if (warzoneQueues.containsKey(warzone))
            return warzoneQueues.get(warzone);
        else {
            warzoneQueues.putIfAbsent(warzone, new LinkedBlockingQueue<User>());
            return warzoneQueues.get(warzone);
        }
    }

    public void removeUserFromQueues(User user) {
        Area queuedRoom = world.areas.get(user.properties.get(Users.PVP_QUEUED));
        if (queuedRoom == null) return;
        LinkedBlockingQueue<User> pq = world.warzoneQueue.getWarzoneQueue(queuedRoom.getName());
        if (pq != null && pq.contains(user)) {
            pq.remove(user);
            JSONObject PVPCTR = new JSONObject();
            PVPCTR.put("cmd", "PVPQueueCntr");
            PVPCTR.put("queues", pq.size());
            PVPCTR.put("slots", queuedRoom.getMaxPlayers());

            for (User client : pq) {
                world.send(PVPCTR, client);
            }
        }
    }

    public void queueUser(String warzone, User user) {
        Area queuedRoom = world.areas.get(warzone);
        if (queuedRoom == null) return;

        LinkedBlockingQueue<User> pq = getWarzoneQueue(warzone);
        if (pq != null && !pq.contains(user)) {
            removeUserFromQueues(user);
            user.properties.put(Users.PVP_QUEUED, warzone);
            pq.offer(user);

            JSONObject PVPCTR = new JSONObject();
            PVPCTR.put("cmd", "PVPQueueCntr");
            PVPCTR.put("queues",  pq.size());
            PVPCTR.put("slots", queuedRoom.getMaxPlayers());

            for (User client : pq) {
                world.send(PVPCTR, client);
            }
        }
    }
}