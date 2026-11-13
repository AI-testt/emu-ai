/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.world.monsters;

import ains.db.model.Aura;
import ains.db.model.GlobalDrop;
import ains.db.model.Monster;
import ains.db.model.MonsterDrop;
import ains.world.avatars.State;
import ains.network.sfs.Room;
import ains.db.model.*;
import ains.tasks.MonsterRespawn;
import ains.tasks.RemoveAura;
import ains.world.users.Users;
import ains.world.World;
import ains.network.sfs.User;
import net.sf.json.JSONObject;
//import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ains.network.manager.UserManager.userManager;

/**
 *
 * @author Mystical
 */
public class MonsterState extends State {

    private Set<Integer> targets;
    private Room room;
    private final World world;
    private final MonsterAttack monster;

    public MonsterState(World world, MonsterAttack monster, int maxHealth, int maxMana) {
        super(maxHealth, maxMana);
        this.monster = monster;
        this.world = world;
        this.targets = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    }

    @Override
    public void die() {
        if (super.isDead()) return;

        super.die();
        super.clearAuras();

        /* Disable Respawn to WorldBoss and PvP */
        if (!world.areas.get(monster.getRoom().getName().split("-")[0]).isPvP())
            this.world.scheduleTask(new MonsterRespawn(this.world, monster), monster.getMonster().getRespawn(), TimeUnit.SECONDS);

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
            if (user == null) {
                continue;
            }
            Monster mon = this.monster.getMonster();
            for (MonsterDrop md : getDrops()) {
                this.world.users.dropItem(user, md.itemId, md.quantity);
            }
            for (GlobalDrop md : getGlobalDrops()) {
                this.world.users.dropItem(user, md.getItemID(), md.getQuantity());
            }
            this.world.users.giveRewards(user, mon.getExperience(), mon.getGold(), mon.getCoins(), mon.getClassPoint(), mon.getReputation(), -1, this.monster.getMapMonster().getMonMapId(), "m");

            this.getEnergy(user);
        }
    }

    private void getEnergy(User user) {
        if ((Integer) user.properties.get(Users.ENERGY) > 0 && (Boolean) user.properties.get(Users.BOT)) {
            int newEnergy = (Integer) user.properties.get(Users.ENERGY) - 1;

            JSONObject recover = new JSONObject();
            recover.put("cmd", "updateEnergy");
            recover.put("intEnergy", newEnergy);
            recover.put("isRecover", 0);
            recover.put("isCommand", 0);
            this.world.send(recover, user);

            user.properties.put(Users.ENERGY, newEnergy);
            this.world.db.getJdbc().run("UPDATE characters SET Energy = ? WHERE id = ?", newEnergy, user.properties.get(Users.DATABASE_ID));
        }
    }

    private Set<MonsterDrop> getDrops() {

        Set<MonsterDrop> drops = new HashSet<MonsterDrop>();
        for (MonsterDrop md : monster.getMonster().drops)
            if (Math.random() <= md.chance + world.DROP_RATE)
                drops.add(md);

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
        RemoveAura ra = new RemoveAura(this.world, aura, this.monster);

        ra.setRunning(this.world.scheduleTask(ra, aura.getDuration(), TimeUnit.SECONDS));
        addAura(ra);

        return ra;
    }

    public void setTargets(Set<Integer> targets) {
        this.targets = targets;
    }

}
