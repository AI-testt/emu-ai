package ains.tasks;

import ains.Main;
import ains.network.sfs.User;
import ains.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KickUser implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(KickUser.class);

    private User user;
    private World world;

    public KickUser(User user, World world) {
        this.user = user;
        this.world = world;
    }

    @Override
    public void run() {
        try {
            this.world.send(new String[]{"logoutWarning", "", "60"}, user);
            Thread.sleep(1000L);
//            this.helper.disconnectUser(user);
            this.user.getNetwork().close();
            log.info("User [ {} ] has been kicked.", user.getName());
        } catch (InterruptedException ex) {
        }
    }
}
