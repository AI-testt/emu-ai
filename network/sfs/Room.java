package ains.network.sfs;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ains.network.manager.UserManager.userManager;

public class Room {

    private static final AtomicInteger count = new AtomicInteger(3);
    private int id;
    private String name;
    private int maxUsers;
    private ConcurrentHashMap<Integer, User> roomUsers;
    public Map properties;
    public final Object roomSync = new Object();

    public Room(String name, int maxUsers) {
        this.id = count.getAndIncrement();
        this.name = name;
        this.maxUsers = maxUsers;
        this.roomUsers = new ConcurrentHashMap<>();
        this.properties = new HashMap();
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getUserCount() {
        synchronized (this.roomSync) {
            return this.roomUsers.size();
        }
    }

    public int getMaxUsers() {
        return this.maxUsers;
    }

    public void addUser(User u) {
        synchronized (this.roomSync) {
            if (!this.roomUsers.containsKey(u.getUserId())) {
                u.addRoom(this);
                this.roomUsers.put(u.getUserId(), u);
            }
        }
    }

    public boolean contains(User u) {
        synchronized (this.roomSync) {
            return this.roomUsers.containsKey(u.getUserId());
        }
    }

    public void removeUser(User u) {
        synchronized (this.roomSync) {
            if (this.roomUsers.containsKey(u.getUserId())) {
                u.removeRoom(this);
                this.roomUsers.remove(u.getUserId());
            }
        }
    }

    public User[] getAllUsers() {
        ArrayList<User> tempList = new ArrayList<>();
        synchronized (this.roomSync) {
            for (int id : this.roomUsers.keySet()) {
                User user = userManager().getUserById(id);
                if (user != null) {
                    tempList.add(user);
                }
            }
        }

        if (tempList.size() == 0) {
            return new User[0];
        }

        Object[] tempArr = tempList.toArray();
        User[] allUsers = new User[tempArr.length];
        System.arraycopy(tempArr, 0, allUsers, 0, allUsers.length);
        return allUsers;
    }

    public User[] getAllUsersButOne(Integer excludedUserId) {
        if (excludedUserId == null) {
            return this.getAllUsers();
        }

        User[] allUsers = null;
        boolean exist = false;
        synchronized (this.roomSync) {
            exist = this.roomUsers.containsKey(excludedUserId);
            if (!exist) {
                return this.getAllUsers();
            }

            allUsers = new User[this.roomUsers.size() - 1];
            int c = 0;

            for (Integer uid : this.roomUsers.keySet()) {
                User u = userManager().getUserById(uid);
                if (!uid.equals(excludedUserId)) {
                    allUsers[c++] = u;
                }
            }

            return allUsers;
        }
    }

    public LinkedList<Channel> getChannellList() {
        LinkedList<Channel> ll = new LinkedList<>();

        for (User user : this.getAllUsers()) {
            if (user != null) {
                ll.add(user.getNetwork().getChannel());
            }
        }

        return ll;
    }

}
