/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.tasks;

//import it.gotoandplay.smartfoxserver.SmartFoxServer;
import ains.world.monsters.MonsterAttack;
import ains.world.monsters.MonsterState;
import ains.world.avatars.State;
import ains.world.avatars.Damage;
import ains.world.avatars.DamageType;
import ains.db.model.Item;
import ains.world.npcs.NpcAttack;
import ains.world.npcs.NpcState;
import ains.world.users.Users;
import ains.world.users.Stats;
import ains.world.World;
import ains.network.sfs.Room;
import ains.network.sfs.User;
//import it.gotoandplay.smartfoxserver.extensions.ExtensionHelper;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static ains.network.manager.RoomManager.roomManager;
import static ains.network.manager.UserManager.userManager;

/**
 *
 * @author Mystical
 */
public class DamageOverTime implements Runnable, CancellableTask {

    private static final Random rand;

    private World world;
    private ScheduledFuture<?> running;
    private User user;
    private NpcAttack aiNpc;
    private MonsterAttack aiMon;
    private String fromTarget;
    private int damage;
    private boolean isMagical;

    static {
        rand = new Random();
    }

    public DamageOverTime(World world, User user, int damage, String fromTarget) {
        this.world = world;
        this.user = user;
        this.damage = damage;
        this.fromTarget = fromTarget;
        this.isMagical = false;

        rand.setSeed(damage);
    }

    public DamageOverTime(World world, User user, int damage, String fromTarget, boolean isMagical) {
        this.world = world;
        this.user = user;
        this.damage = damage;
        this.fromTarget = fromTarget;
        this.isMagical = isMagical;

        rand.setSeed(damage);
    }

    public DamageOverTime(World world, NpcAttack ai, int damage, String fromTarget) {
        this.world = world;
        this.aiNpc = ai;
        this.damage = damage;
        this.fromTarget = fromTarget;
        this.isMagical = false;

        rand.setSeed(damage);
    }

    public DamageOverTime(World world, MonsterAttack ai, int damage, String fromTarget) {
        this.world = world;
        this.aiMon = ai;
        this.damage = damage;
        this.fromTarget = fromTarget;
        this.isMagical = false;

        rand.setSeed(damage);
    }

    @Override
    public void run() {
        if (this.damage == 0)
            throw new RuntimeException("damage is 0, pointless to continue.");

        int dotDamage = rand.nextInt(Math.abs(this.damage));
        dotDamage = (this.damage < 0) ? (dotDamage * -1) : dotDamage;

        if (this.user != null) processUser(dotDamage);
        if (this.aiNpc != null) processNpc(dotDamage);
        if (this.aiMon != null) processMonster(dotDamage);
    }

    public int getDamage() {
        return (damage);
    }

    public void processNpc(int dotDamage) {
        JSONObject ct = new JSONObject();

        ct.put("cmd", "ct");
        ct.put("sara", new JSONArray().element(new JSONObject()
                .accumulate("actionResult", Damage.getDamageResult(fromTarget, "n:" + aiNpc.getMapNpc().getNpcMapId(), dotDamage, DamageType.DAMAGE_OVER_TIME))
                .accumulate("iRes", 1)));

        JSONObject p = new JSONObject();

        NpcState state = this.aiNpc.getState();

        state.decreaseHealth(dotDamage);

        if (state.isDead() && state.getHealth() <= 0) {
            cancel();

            Set<Integer> npcTargets = state.getTargets();

            for (int userId : npcTargets) {
//                User userTgt = ExtensionHelper.instance().getUserById(userId);
                User userTgt = userManager().getUserById(userId);

                //Check if user exists
                if (userTgt != null) {
                    world.users.regen(user);

                    JSONObject userData = new JSONObject();
                    userData.put("intState", 1);
                    p.put(userTgt.getName(), userData);
                }
            }
        }
        ct.put("n", new JSONObject().accumulate(String.valueOf(aiNpc.getMapNpc().getNpcId()), state.getData()
                .accumulate("targets", state.getTargets())));

        if (!p.isEmpty())
            ct.put("p", p);

        world.send(ct, this.aiNpc.getRoom().getChannellList());
    }

    public void processMonster(int dotDamage) {
        JSONObject ct = new JSONObject();

        ct.put("cmd", "ct");
        ct.put("sara", new JSONArray().element(new JSONObject()
                .accumulate("actionResult", Damage.getDamageResult(fromTarget, "m:" + aiMon.getMapMonster().getMonMapId(), dotDamage, DamageType.DAMAGE_OVER_TIME))
                .accumulate("iRes", 1)));

        JSONObject p = new JSONObject();
        
        MonsterState state = this.aiMon.getState();

        state.decreaseHealth(dotDamage);

        if (state.isDead() && state.getHealth() <= 0) {
            cancel();

            Set<Integer> monTargets = state.getTargets();

            for (int userId : monTargets) {
//                User userTgt = ExtensionHelper.instance().getUserById(userId);
                User userTgt = userManager().getUserById(userId);

                //Check if user exists
                if (userTgt != null) {
                    world.users.regen(user);

                    JSONObject userData = new JSONObject();
                    userData.put("intState", 1);
                    p.put(userTgt.getName(), userData);
                }
            }
        }
        ct.put("m", new JSONObject().accumulate(String.valueOf(aiMon.getMapMonster().getMonsterId()), state.getData()
                .accumulate("targets", state.getTargets())));

        if (!p.isEmpty())
            ct.put("p", p);

        world.send(ct, this.aiMon.getRoom().getChannellList());
    }

    public void processUser(int dotDamage) {
        JSONObject ct = new JSONObject();

        ct.put("cmd", "ct");
        ct.put("sara", new JSONArray().element(new JSONObject()
                .accumulate("actionResult", Damage.getDamageResult(fromTarget, "p:" + user.getUserId(), dotDamage, DamageType.DAMAGE_OVER_TIME))
                .accumulate("iRes", 1)));

        for (Object o : ((JSONObject) user.properties.get(Users.EQUIPMENT)).values()) {
            Item item = world.items.get(((JSONObject) o).getInt("ItemID"));
            if(item.getMeta() != null && item.getMeta().contains(":")) {
                for (String sMeta : item.getMeta().split(",")) {
                    String[] param = sMeta.split(":");
                    String metaType = param[0].toLowerCase();
                    double value = Double.parseDouble(param[1]);

                    if(metaType.equals("dmgtaken")) {
                        double reduction = 1.0 - value;
                        dotDamage *= reduction > 0 ? reduction : 0;
                    }
                }
            }
        }

        Stats targetStats = (Stats) user.properties.get(Users.STATS);
        dotDamage = Damage.applyIncomingResist(dotDamage, this.isMagical, true, targetStats.get$cai(), targetStats.get$cpi(), targetStats.get$cmi(), targetStats.get$cdi(), targetStats.get$chi());

//        Room room = world.zone.getRoom(user.getRoom());
        Room room = roomManager().getRoom(this.user.getRoom());

        JSONObject p = new JSONObject();
        State userState = (State) user.properties.get(Users.USER_STATE);

        userState.decreaseHealth(dotDamage);

        if (userState.isDead()) {
            cancel();

            if (world.areas.get(room.getName().split("-")[0]).isPvP()) {
                int teamId = (Integer) user.properties.get(Users.PVP_TEAM) == 0 ? 1 : 0;
                if (room.getName().split("-")[0].equals("arena"))
                    world.rooms.addPvPScore(room, 1000, teamId);
                else
                    world.rooms.addPvPScore(room, (Integer) user.properties.get(Users.LEVEL), teamId);
                ct.put("pvp", world.rooms.getPvPResult(room));
            }
        }

        p.put(this.user.getName(), userState.getData());
        ct.put("p", p);

        world.sendToRoom(ct, user, room);
    }

    @Override
    public void cancel() {
        this.running.cancel(false);
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        this.running = running;
    }
}
