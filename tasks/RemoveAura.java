/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.tasks;

import ains.world.monsters.MonsterAttack;
import ains.db.model.Aura;
import ains.world.npcs.NpcAttack;
import ains.world.users.Users;
import ains.world.World;
import ains.network.sfs.User;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ains.network.manager.RoomManager.roomManager;

/**
 *
 * @author Mystical
 */
public class RemoveAura implements Runnable, CancellableTask {

    private final Logger log = LoggerFactory.getLogger(RemoveAura.class);

    private World world;
    private Aura aura;
    private DamageOverTime dot;
    private ScheduledFuture<?> running;
    private User user;
    private NpcAttack aiNpc;
    private MonsterAttack aiMon;
    private int stack = 1;

    public RemoveAura(World world, Aura aura, NpcAttack ai) {
        this.world = world;
        this.aura = aura;
        this.aiNpc = ai;
    }

    public RemoveAura(World world, Aura aura, MonsterAttack ai) {
        this.world = world;
        this.aura = aura;
        this.aiMon = ai;
    }

    public RemoveAura(World world, Aura aura, User user) {
        this.world = world;
        this.aura = aura;
        this.user = user;
    }

    @Override
    public void run() {
//        System.out.println("RemoveAura > " + aura.getName());
        log.info("Removing aura: {}", aura.getName());

        JSONObject ct = new JSONObject();
        JSONArray a = new JSONArray();
        JSONObject o = new JSONObject();
        JSONObject auraInfo = new JSONObject();

        if (!this.aura.getCategory().isEmpty() && !this.aura.getCategory().equals("d")) {
            auraInfo.put("cat", this.aura.getCategory());
            if (this.aura.getCategory().equals("stun"))
                auraInfo.put("s", "s");
        }

        auraInfo.put("nam", aura.getName());

        o.put("cmd", "aura-");
        o.put("aura", auraInfo);

        if (this.user != null)
            o.put("tInf", "p:" + this.user.getUserId());
        else if (this.aiNpc != null)
            o.put("tInf", "n:" + this.aiNpc.getMapNpc().getNpcMapId());
        else if (this.aiMon != null)
            o.put("tInf", "m:" + this.aiMon.getMapMonster().getMonMapId());

        a.add(o);

        ct.put("cmd", "ct");
        ct.put("a", a);

        //Process depending on type
        if (this.user != null) {
            Set<RemoveAura> auras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
            auras.remove(this);

            this.world.users.removeAuraEffects(this.user, this.aura);

//            this.world.send(ct, this.world.zone.getRoom(this.user.getRoom()).getChannellList());
            this.world.send(ct, roomManager().getRoom(this.user.getRoom()).getChannellList());

        } else if (this.aiNpc != null) {
            this.aiNpc.getState().removeAura(this);
            this.world.send(ct, this.aiNpc.getRoom().getChannellList());
        } else if (this.aiMon != null) {
            this.aiMon.getState().removeAura(this);
            this.world.send(ct, this.aiMon.getRoom().getChannellList());
        }

        if (this.dot != null)
            this.dot.cancel();
    }

    @Override
    public void cancel() {
        if (this.dot != null)
            this.dot.cancel();

        if (this.running != null)
            this.running.cancel(false);
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        this.running = running;
    }

    public Aura getAura() {
        return aura;
    }

    public void setDot(DamageOverTime dot) {
        this.dot = dot;
    }

    public DamageOverTime getDot() {
        return this.dot;
    }

    @Override
    public int hashCode() {
        return this.aura.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final RemoveAura other = (RemoveAura) obj;
        return this.aura.getId() == other.aura.getId();
    }

    public int getStack() {
        return this.stack;
    }

    /**
     * Menambah stack aura ini sampai batas Aura.getMaxStack(). Dipanggil
     * setiap kali skill/efek yang sama diterapkan ulang ke target yang
     * sudah memegang aura ini (lihat Users.applyAura / State.applyAura
     * caller di Action.java, NpcAttack, MonsterAttack).
     */
    public int restack() {
        if (this.stack < this.aura.getMaxStack())
            this.stack++;
        return this.stack;
    }

    public long extendDelay() {
        long remaining = running.getDelay(TimeUnit.SECONDS);

        if (aura == null) return remaining;

        // Refresh me-reset ke durasi penuh (seperti AQW resmi), bukan sisa + durasi
        // yang membuat aura terus memanjang kalau skill di-spam.
        long newDelay = aura.getDuration();

        running.cancel(false);

        running = this.world.scheduleTask(this, newDelay, TimeUnit.SECONDS);

        return newDelay;
    }

}
