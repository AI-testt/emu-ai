/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.tasks;

import ains.world.World;
import ains.network.sfs.User;
import java.util.Set;

/**
 *
 * @author Mystical
 */
public class WarpUser implements Runnable {

    private Set<User> users;
    private World world;

    public WarpUser(World world, Set<User> users) {
        this.users = users;
        this.world = world;
    }

    @Override
    public void run() {
        for (User user : users)
            this.world.rooms.basicRoomJoin(user, "faroff");
    }
}
