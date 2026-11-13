package ains.tasks;

import ains.Main;
import ains.world.World;
import ains.network.sfs.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static ains.network.manager.UserManager.userManager;

public class ServerMessages implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerMessages.class);

    private World world;
    private Random rand;

    public ServerMessages(World world) {
        this.world = world;
        this.rand = new Random(System.currentTimeMillis());
        log.info("Server messages event initialized.");
    }

//    private User getRandomUser() {
//        List characters = this.world.zone.getUserList();
//        User user = (User)users.get(this.rand.nextInt(users.size()));
//        return !user.isAdmin() && !user.isModerator() ? user : this.getRandomUser();
//    }

    private User getRandomUser() {
        List<User> users = (List<User>) userManager().getUserList();
        User user = users.get(this.rand.nextInt(users.size()));

        if (user.isAdmin() || user.isModerator())
            return getRandomUser();
        else
            return user;
    }

    public void run() {
        String Message = world.db.getJdbc().queryForString("SELECT message from settings_messages order by rand() limit 1");
        this.world.sendToUsers(new String[]{"moderator", Message});
    }
}
