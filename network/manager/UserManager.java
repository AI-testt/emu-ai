package ains.network.manager;

import ains.network.sfs.User;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private static final Logger log = LoggerFactory.getLogger(UserManager.class);

    private ConcurrentHashMap<Integer, User> usersConnected;
    private static UserManager _instance = new UserManager();
    public int highestCount = 0;

    public UserManager() {
        this.usersConnected = new ConcurrentHashMap<>();
    }

    /** @return true when the user was still registered (i.e. this call actually removed it) */
    public boolean removeUser(User u) {
        return this.usersConnected.remove(u.getUserId()) != null;
    }

    public void addUser(User u) {
        this.usersConnected.put(u.getUserId(), u);
        if (this.usersConnected.size() > this.highestCount) {
            this.highestCount = this.usersConnected.size();
        }
    }

    public User getUserById(Integer i) {
        synchronized (this.usersConnected) {
            for (Map.Entry<Integer, User> map : this.usersConnected.entrySet()) {
                if (map.getValue().getUserId() == i) {
                    return map.getValue();
                }
            }

            return null;
        }
    }

    public User getUserByName(String name) {
        User user = null;
        synchronized (this.usersConnected) {
            for (Map.Entry<Integer, User> map : this.usersConnected.entrySet()) {
                if (map.getValue().getName().equalsIgnoreCase(name)) {
                    user = map.getValue();
                }
            }

            return user;
        }
    }

    public LinkedList<Channel> getChannelList() {
        LinkedList<Channel> ll = new LinkedList<>();

        for (User user : this.usersConnected.values()) {
            if (user != null) {
                ll.add(user.getNetwork().getChannel());
            }
        }

        return ll;
    }

    public List<User> getUserList() {
        List<User> someList = new LinkedList<>();
        for (User user : this.usersConnected.values())
            someList.add(user);

        return someList;
    }

    public static UserManager userManager() {
        return _instance;
    }

    public int getUserCount() {
        return this.usersConnected.size();
    }

}
