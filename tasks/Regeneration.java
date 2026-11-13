//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.tasks;

import ains.world.avatars.State;
import ains.world.users.Users;
import ains.world.World;
import ains.network.sfs.User;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONObject;

import static ains.network.manager.RoomManager.roomManager;

public class Regeneration implements Runnable, CancellableTask {

    private User user;
    private World world;
    private ScheduledFuture<?> running;

    public Regeneration(User user, World world) {
        this.user = user;
        this.world = world;
    }

    @Override
    public void run() {
        if (this.running == null && this.user == null)
            throw new RuntimeException("regen handle is null, unable to continue.");

		State state = (State) user.properties.get(Users.USER_STATE);

        if (state.isNeutral() && (state.getHealth() < state.getMaxHealth() || state.getMana() < state.getMaxMana() || state.getStamina() < state.getMaxStamina())) {

            state.increaseHealthByPercent(0.125);
            state.increaseManaByPercent(0.1);
            state.increaseStaminaByPercent(0.25);

            JSONObject ct = new JSONObject();
            JSONObject p = new JSONObject();

            p.put(this.user.getName(), state.getData());

            ct.put("cmd", "ct");
            ct.put("p", p);

//            this.world.send(ct, this.world.zone.getRoom(this.user.getRoom()).getChannellList());
            this.world.send(ct, roomManager().getRoom(this.user.getRoom()).getChannellList());
        } else
            this.running.cancel(false);
    }

    public boolean isRegenerating() {
        return (this.running != null && !this.running.isDone());
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        if (this.running == null || this.running.isDone())
            this.running = running;
        else
            running.cancel(true);
    }

    @Override
    public void cancel() {
        if (this.running != null)
            this.running.cancel(false);
    }
}