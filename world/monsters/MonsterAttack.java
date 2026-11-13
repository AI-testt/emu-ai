package ains.world.monsters;

import ains.db.model.*;
import ains.world.avatars.State;
import ains.world.avatars.Damage;
import ains.world.avatars.DamageType;
import ains.tasks.RemoveAura;
import ains.world.users.Users;
import ains.world.World;
import ains.world.users.Stats;
import ains.network.sfs.Room;
import ains.network.sfs.User;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import io.netty.channel.Channel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static ains.network.manager.UserManager.userManager;

/**
 *
 * @author Mystical
 */
public class MonsterAttack implements Runnable {

    public ScheduledFuture<?> attacking;
    private World world;
    private MonsterState state;
    private MapMonster mapMonster;
    private Monster monster;
    private MonsterSkills skills;
    private Room room;

    public MonsterAttack(MapMonster mapMon, World world, Room room) {
        this.world = world;
        this.mapMonster = mapMon;
        this.monster = world.monsters.get(mapMon.getMonsterId());
        this.room = room;
        this.state = new MonsterState(world, this, this.monster.getHealth(), this.monster.getMana());
        this.skills = new MonsterSkills();

        for (int skillId : this.monster.skills)
            this.skills.addSkill(world.skills.get(skillId));
    }

    public void cancel() {
        if (!this.state.hasTargets()) {
            this.restore();
            this.attacking.cancel(false);
        }
    }

    @Override
    public void run() {
        if (this.state.isDead()) {
            this.attacking.cancel(false);
            return;
        }

        int userId = this.state.getRandomTarget();
        User user = userManager().getUserById(userId);

        if (user == null || (this.room.getId() != user.getRoom()) || !this.mapMonster.getFrame().equals(user.properties.get(Users.FRAME))) {
            this.state.removeTarget(userId);
            cancel();
        } else
            attack(user);
    }

    private boolean isDead() {
        if (this.state.isDead()) {
            if (this.attacking != null) this.attacking.cancel(true);
            return true;
        }
        return false;
    }

    private void attack(User user) {
        if (isDead() || this.state.isDisabled()) return;

        Stats stats = (Stats) user.properties.get(Users.STATS);
        Set<RemoveAura> userAuras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
        State userState = (State) user.properties.get(Users.USER_STATE);

        DamageType type = Damage.evaluateDamageType(0.2, stats.get$tdo(), 0.2);
        int damage = Damage.getReducedDamage(Damage.getRandomDamage(type, this.monster.getMaxDmg(), this.monster.getMinDmg(), 0), userAuras);

        for (Object o : ((JSONObject) user.properties.get(Users.EQUIPMENT)).values()) {
            Item item = world.items.get(((JSONObject) o).getInt("ItemID"));
            if (item.getMeta() != null && item.getMeta().contains(":")) {
                for (String sMeta : item.getMeta().split(",")) {
                    String[] param = sMeta.split(":");
                    String metaType = param[0].toLowerCase();
                    double value = Double.parseDouble(param[1]);

                    if (metaType.equals("dmgtaken")) {
                        double reduction = 1 - value;
                        damage *= reduction > 0 ? reduction : 0;
                    }
                }
            }
        }

        damage = Damage.applyIncomingResist(damage, false, false, stats.get$cai(), stats.get$cpi(), stats.get$cmi(), stats.get$cdi(), stats.get$chi());

        this.state.increaseManaByPercent(0.02);
        userState.decreaseHealth(damage);

        if (userState.isDead()) {
            this.state.removeTarget(user.getUserId());
            cancel();
        }

        JSONObject p = new JSONObject().accumulate(user.getName(), userState.getData());
        JSONArray sara = new JSONArray().element(new JSONObject()
                .accumulate("actionResult", getAction(type, damage, user))
                .accumulate("iRes", 1));
        JSONObject m = new JSONObject().accumulate(String.valueOf(this.mapMonster.getMonMapId()), new JSONObject()
                .accumulate("intMP", this.state.getMana()));
        JSONObject ct = new JSONObject().accumulate("anims", new JSONArray()
                        .element(getAnim(user)))
                .accumulate("p", p)
                .accumulate("m", m)
                .accumulate("cmd", "ct");

        world.send(ct, getUserChannelButOne(room, user.getUserId()));
        ct.accumulate("sara", sara);
        world.send(ct, user);

        cast(this.skills.getSkill());
    }

    private LinkedList<Channel> getUserChannelButOne(Room room, int userId) {
        LinkedList<Channel> list = new LinkedList<Channel>();

        User[] usersInRoom = room.getAllUsersButOne(userId);

        for (User user : usersInRoom) {
            list.add(user.getNetwork().getChannel());
        }

        return list;
    }

    private void cast(Skill skill) {
        if (skill == null) return;
        if (skill.getMana() > this.state.getMana()) return;

        String targets = this.state.getRandomTargets(skill.getHitTargets());

        JSONObject p = new JSONObject();
        JSONArray a = new JSONArray();
        JSONArray auras = new JSONArray();

        for (String tgt : targets.split(",")) {
            int userId = Integer.parseInt(tgt.split(":")[1]);
            User user = userManager().getUserById(userId);

            if (user == null || (this.room.getId() != user.getRoom())) {
                this.state.removeTarget(userId);
                continue;
            }

            Stats stats = (Stats) user.properties.get(Users.STATS);
            Set<RemoveAura> userAuras = (Set<RemoveAura>) user.properties.get(Users.AURAS);
            State userState = (State) user.properties.get(Users.USER_STATE);

            DamageType type = Damage.evaluateDamageType(0.2, stats.get$tdo(), 0.2);
            int damage = (int) (Damage.evaluateAuras(Damage.evaluateAuras(Damage.getRandomDamage(type, this.monster.getMaxDmg(), this.monster.getMinDmg(), 0), userAuras), state.getAuras()) * skill.getDamage());
damage = Damage.applyIncomingResist(damage, false, false, stats.get$cai(), stats.get$cpi(), stats.get$cmi(), stats.get$cdi(), stats.get$chi());

            this.state.increaseManaByPercent(0.02);
            userState.decreaseHealth(damage);

            if (userState.isDead()) {
                this.state.removeTarget(user.getUserId());
                cancel();
            }

            if (skill.hasAura()) {
                Iterator i$ = skill.auras.entries().iterator();
                while (i$.hasNext()) {
                    Map.Entry<Integer, SkillAura> entry = (Map.Entry) i$.next();
                    Aura aura = world.auras.get(entry.getValue().auraId);
                    if (aura == null || Math.random() > aura.getChance()) {
                        continue;
                    }
                    // Aura player disimpan di Users.AURAS (bukan UserState) supaya ikut
                    // dihapus RemoveAura.run() dan efek stat-nya diterapkan.
                    boolean auraExists = world.users.hasAura(user, aura.getId());
                    RemoveAura ra = auraExists ? world.users.getAura(user, aura.getId()) : world.users.applyAura(user, aura);
                    int stack = auraExists ? ra.restack() : 1;
                    long newDelay = auraExists ? ra.extendDelay() : aura.getDuration();
                    world.users.applyAuraEffects(user, aura, stack);
                    JSONObject aInfo = new JSONObject();
                    aInfo.put("cInf", "m:" + this.mapMonster.getMonMapId());
                    aInfo.put("cmd", "aura+");
                    JSONArray auraArray = aura.getAuraArray(!auraExists, stack);
                    if (auraExists)
                        auraArray.getJSONObject(0).put("dur", String.valueOf(newDelay));
                    aInfo.put("auras", auraArray);
                    aInfo.put("tInf", "p:" + user.getUserId());
                    auras.add(aInfo);
//                    auras.add(userState.applyAura(world.auras.get(entry.getValue().auraId)));
                }
            }

//            if (world.skillsAura.containsKey(skill.getId())) {
//                Iterator iterator = world.skillsAura.entries().iterator();
//                while (iterator.hasNext()) {
//                    Map.Entry entry = (Map.Entry) iterator.next();
//                    Aura aura = world.auras.get(entry.getValue());
//                    if (aura == null) continue;
//
//                    boolean auraExists = userState.hasAura(aura.getId());
//                    userState.applyAura(aura);
//
//                    JSONObject aInfo = new JSONObject();
//                    aInfo.put("cInf", "m:" + this.mapMonster.getMonMapId());
//                    aInfo.put("cmd", "aura+");
//                    aInfo.put("auras", aura.getAuraArray(!auraExists));
//                    aInfo.put("tInf", "p:" + user.getUserId());
//                    auras.add(aInfo);
//                }
//            }

//            if (skill.hasAuraId()) auras.add(userState.applyAura(world.auras.get(skill.getAuraId())));

            a.add(Damage.getDamageResult("m:" + this.mapMonster.getMonMapId(), tgt, damage, type));
            p.put(user.getName(), userState.getData());

            JSONArray sara = new JSONArray().element(new JSONObject()
                    .accumulate("actionResult", getAction(type, damage, user))
                    .accumulate("iRes", 1));

            JSONObject ct = new JSONObject().accumulate("p", p)
                    .accumulate("m", new JSONObject().accumulate(String.valueOf(this.mapMonster.getMonMapId()), new JSONObject()
                            .accumulate("intMP", this.state.getMana())))
                    .accumulate("sarsa", new JSONArray().element(new JSONObject()
                            .accumulate("cInf", "m:" + this.mapMonster.getMonMapId())
                            .accumulate("a", a)
                            .accumulate("iRes", 1)))
                    .accumulate("cmd", "ct")
                    .accumulate("a", auras)
                    .accumulate("anims", new JSONArray().element(getAnim(this.mapMonster.getFrame(), targets, skill)))
                    .accumulate("sara", sara);
            world.send(ct, room.getChannellList());
        }

        this.state.decreaseMana(skill.getMana());
    }

    private JSONObject getAction(DamageType type, int damage, User user) {
        JSONObject action = new JSONObject();
        action.put("hp", damage);
        action.put("cInf", "m:" + this.mapMonster.getMonMapId());
        action.put("tInf", "p:" + Integer.toString(user.getUserId()));
        action.put("type", type.toString());
        return action;
    }

    private JSONObject getAnim(String frame, String targets, Skill skill) {
        JSONObject anim = new JSONObject();
        anim.put("strFrame", frame);
        anim.put("cInf", "m:" + this.mapMonster.getMonMapId());
        anim.put("fx", skill.getEffects());
        anim.put("tInf", targets);
        anim.put("animStr", skill.getAnimation());

        if (world.adaIsi(skill.getStrl()))
            anim.put("strl", skill.getStrl());

        return anim;
    }

    private JSONObject getAnim(User user) {
        JSONObject anim = new JSONObject();
        anim.put("strFrame", user.properties.get(Users.FRAME));
        anim.put("cInf", "m:" + this.mapMonster.getMonMapId());
        anim.put("fx", "m");
        anim.put("tInf", "p:" + user.getUserId());
        anim.put("animStr", "Attack1,Attack2");
        return anim;
    }

    public void restore() {
        this.state.restore();

        JSONObject mtls = new JSONObject();
        mtls.put("cmd", "mtls");
        mtls.put("id", this.mapMonster.getMonMapId());
        mtls.put("o", this.state.getData());

        this.world.send(mtls, room.getChannellList());
    }

    public Room getRoom() {
        return room;
    }

    public synchronized void setAttacking(ScheduledFuture<?> attacking) {
        if (this.attacking == null || this.attacking.isDone()) {
            this.state.setState(2);
            this.attacking = attacking;
        } else
            attacking.cancel(true);
    }

    public Monster getMonster() {
        return monster;
    }

    public MapMonster getMapMonster() {
        return mapMonster;
    }

    public MonsterState getState() {
        return state;
    }
}