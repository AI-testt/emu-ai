package ains.world.npcs;

import ains.db.model.*;
import ains.network.sfs.Room;
import ains.network.sfs.User;
import ains.tasks.MonsterRespawn;
import ains.tasks.NpcRespawn;
import ains.tasks.RemoveAura;
import ains.world.World;
import ains.world.avatars.State;
import ains.world.monsters.MonsterAttack;
import ains.world.users.Users;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ains.network.manager.UserManager.userManager;

public class NpcState extends State {

    private Set<Integer> targets;
    private Room room;
    private final World world;
    private final NpcAttack npc;

    public NpcState(World world, NpcAttack npc, int maxHealth, int maxMana) {
        super(maxHealth, maxMana);
        this.npc = npc;
        this.world = world;
        this.targets = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    }

    @Override
    public void die() {
        if (super.isDead()) return;

        super.die();
        super.clearAuras();

        /* Disable Respawn to WorldBoss and PvP */
        if (!world.areas.get(npc.getRoom().getName().split("-")[0]).isPvP())
            this.world.scheduleTask(new NpcRespawn(this.world, npc), npc.getNpc().getRespawn(), TimeUnit.SECONDS);

        setUserState();
        giveRewards();
        this.targets.clear();
    }

    private User generateLastHit() {
        for (int userId : this.targets) {
            User user = userManager().getUserById(userId);
            if (user != null) {
                return user;
            }
            break;
        }
        return null;
    }

    private void setUserState() {
        for (int userId : this.targets) {
            User user = userManager().getUserById(userId);
            if (user != null) {
                State state = (State) user.properties.get(Users.USER_STATE);
                if (!state.isDead()) {
                    state.setState(Users.STATE_NORMAL);
                    world.users.regen(user);
                }
            }
        }
    }

    private void giveRewards() {
        for (int userId : this.targets) {
            User user = userManager().getUserById(userId);

            if (user == null) continue;

            Npc mon = this.npc.getNpc();

            for (NpcDrop nd : getDrops())
                this.world.users.dropItem(user, nd.itemId, nd.quantity);

            for (GlobalDrop gd : getGlobalDrops())
                this.world.users.dropItem(user, gd.getItemID(), gd.getQuantity());

            this.world.users.giveRewards(user, mon.getExperience(), mon.getGold(), mon.getCoins(), mon.getReputation(), 0, -1, this.npc.getMapNpc().getNpcMapId(), "n");
        }
    }

    private Set<NpcDrop> getDrops() {

        Set<NpcDrop> drops = new HashSet<NpcDrop>();
        for (NpcDrop nd : npc.getNpc().drops)
            if (Math.random() <= nd.chance + world.DROP_RATE)
                drops.add(nd);

        return drops;
    }

    private Set<GlobalDrop> getGlobalDrops() {

        Set<GlobalDrop> drops = new HashSet<GlobalDrop>();
        for (GlobalDrop md : world.globalDrops.values())
            if (Math.random() <= md.getChance() + world.DROP_RATE)
                drops.add(md);

        return drops;
    }

    @Override
    public void restore() {
        super.restore();
        this.targets.clear();
    }

    public int getRandomTarget() {
        Integer[] setArray = this.targets.toArray(new Integer[this.targets.size()]);
        return setArray[World.RANDOM.nextInt(setArray.length)];
    }

    public String getRandomTargets(int maxTargets) {
        StringBuilder sb = new StringBuilder();
        maxTargets = Math.max(maxTargets, 1);
        while (maxTargets > 0) {
            int userId = getRandomTarget();
            if (!sb.toString().contains(String.valueOf(userId))) {
                sb.append(",");
                sb.append("p:");
                sb.append(userId);
            }
            maxTargets--;
        }
        sb.deleteCharAt(0);
        return sb.toString();
    }

    public boolean hasTargets() {
        return !this.targets.isEmpty();
    }

    public Set<Integer> getTargets() {
        return Collections.unmodifiableSet(this.targets);
    }

    public void clearTarget() {
        if (!this.targets.isEmpty()) this.getTargets().clear();
    }

    public void addTarget(int userId) {
        if (!isDead() && !targets.contains(userId))
            targets.add(userId);
    }

    public void removeTarget(int userId) {
        targets.remove(userId);
    }

    @Override
    public RemoveAura applyAura(Aura aura) {
        RemoveAura ra = new RemoveAura(this.world, aura, this.npc);

        ra.setRunning(this.world.scheduleTask(ra, aura.getDuration(), TimeUnit.SECONDS));
        addAura(ra);

        return ra;
    }

    public void setTargets(Set<Integer> targets) {
        this.targets = targets;
    }

}
