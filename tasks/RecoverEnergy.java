package ains.tasks;

import ains.network.sfs.User;
import ains.world.World;
import ains.world.users.Users;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ains.network.manager.UserManager.userManager;

public class RecoverEnergy implements Runnable {

    private final Logger log = LoggerFactory.getLogger(RecoverEnergy.class);

    private World world;

    public RecoverEnergy(World world) {
        this.world = world;
        this.log.info("Energy Recovery Event initialized.");
    }

    @Override
    public void run() {
//        this.log.info("Start Energy Recovery.");
        this.world.db.getJdbc().run("UPDATE characters SET Energy = (Energy + 1) WHERE Energy < EnergyMax");

        for (Channel chan : userManager().getChannelList()) {
            User tgt = (User) chan.attr(User.PLAYER_KEY).get();
            if (tgt != null && (Integer) tgt.properties.get(Users.ENERGY) < (Integer) tgt.properties.get(Users.ENERGY_MAX)) {
                int Amount = (Integer) tgt.properties.get(Users.ENERGY) + 1;
                tgt.properties.put(Users.ENERGY, Amount);

                JSONObject recover = new JSONObject();
                recover.put("cmd", "updateEnergy");
                recover.put("intEnergy", Amount);
                recover.put("isRecover", 1);
                recover.put("isCommand", 0);
                this.world.send(recover, tgt);
            }
        }
    }
}
