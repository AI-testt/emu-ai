package ains.network.sfs;

import ains.network.api.PlayerNetwork;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class User {

    public static final AttributeKey<User> PLAYER_KEY = AttributeKey.valueOf("User");
    private PlayerNetwork network;
    public Map properties;
    private static final AtomicInteger count = new AtomicInteger(0);
    private int uid;
    private String name;
    private LinkedList roomsConnected;
    private boolean moderator;
    private boolean admin;
    private boolean apiOk = true;
    public boolean isBeingKicked;
    public int floodCounter;
    public int repeatedMsgCounter;
    public int floodWarningsCounter;
    public String lastMessage;

    public User(PlayerNetwork network) {
        this.uid = count.getAndIncrement();
        this.network = network;
        this.properties = new HashMap();
        this.roomsConnected = new LinkedList();
        this.moderator = false;
        this.admin = false;
        this.isBeingKicked = false;
        this.floodCounter = 0;
        this.repeatedMsgCounter = 0;
        this.floodWarningsCounter = 0;
    }

    public boolean isApiOk() {
        return this.apiOk;
    }

    public void setApiOk(boolean status) {
        this.apiOk = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name.toLowerCase();
    }

    public int getUserId() {
        return this.uid;
    }

    public PlayerNetwork getNetwork() {
        return this.network;
    }

    public void addRoom(Room r) {
        synchronized (this.roomsConnected) {
            this.roomsConnected.add(r);
        }
    }

    public void removeRoom(Room r) {
        synchronized (this.roomsConnected) {
            this.roomsConnected.remove(r);
        }
    }

    public boolean isModerator() {
        return this.moderator;
    }

    public void setModerator(boolean status) {
        this.moderator = status;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public void setAdmin(boolean status) {
        this.admin = status;
    }

    public int getRoom() {
        int res = -1;

        try {
            Room r = (Room) this.roomsConnected.get(0);
            res = r.getId();
        } catch (IndexOutOfBoundsException var3) {
        }

        return res;
    }

}
