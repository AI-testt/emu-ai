
package ains.tasks;

import ains.db.model.WorldBoss;
import ains.network.sfs.Room;
import ains.network.sfs.User;
import ains.network.manager.UserManager;
import ains.network.manager.RoomManager;
import ains.world.rooms.Rooms;
import ains.world.World;
import ains.world.monsters.MonsterState;

import io.netty.channel.Channel;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class WBSpawn implements Runnable {

    private final Logger log = LoggerFactory.getLogger(WBSpawn.class);

    private World world;
    public HashMap<Integer, Long> deathTimes = new HashMap<Integer, Long>();
    public HashMap<Integer, Long> spawnTimes = new HashMap<Integer, Long>();

    public WBSpawn(World world) {
        this.world = world;
        for (Map.Entry<Integer, WorldBoss> entry : world.worldbosses.entrySet()) {
            String mapName = world.db.getJdbc().queryForString("SELECT Name FROM maps WHERE id = ?", new Object[]{((WorldBoss) entry.getValue()).mapId});
            world.worldBossMaps.put((entry.getValue()).mapId, mapName);
            deathTimes.put(entry.getValue().monsterId, System.currentTimeMillis());
            spawnTimes.put(entry.getValue().monsterId, System.currentTimeMillis());
        }
        world.db.getJdbc().run("UPDATE monsters_bosses SET DeathTime = NOW()");
        log.info("WBSpawn intialized.");
    }

    @Override
    public void run() {
        try {
            for (Map.Entry<Integer, WorldBoss> entry : world.worldbosses.entrySet()) {

                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, yyyy (hh:mm:ss a)");

                world.now = Calendar.getInstance();
                world.now.add(Calendar.MILLISECOND, -(int) (System.currentTimeMillis() - deathTimes.get(entry.getValue().monsterId)));
                world.now.add(Calendar.MILLISECOND, (int) (entry.getValue().spawnInterval));

                if (TimeUnit.MILLISECONDS.toMinutes(((deathTimes.get(entry.getValue().monsterId) + entry.getValue().spawnInterval) - System.currentTimeMillis())) == 4 && !entry.getValue().isNotified) {
                    entry.getValue().isNotified = true;
                }

                if (System.currentTimeMillis() - deathTimes.get(entry.getValue().monsterId) > entry.getValue().spawnInterval && !entry.getValue().isActive) {
                    entry.getValue().isNotified = false;
                    entry.getValue().isActive = true;
                    entry.getValue().roomNumber = 1;

                    String room = world.worldBossMaps.get(entry.getValue().mapId).toLowerCase() + "-" + entry.getValue().roomNumber;
                    Room bossRoom = RoomManager.roomManager().getRoomByName(room);

                    spawnTimes.put(entry.getValue().monsterId, System.currentTimeMillis());

                    if (bossRoom == null) {
                        bossRoom = world.rooms.createRoom(room);
                    }

                    JSONObject umsg = new JSONObject();
                    umsg.put("cmd", "WorldBossInvite"); //WorldBossInvite
                    umsg.put("room", room);
                    umsg.put("monName", world.monsters.get(entry.getValue().monsterId).getName());
                    umsg.put("monLevel", world.monsters.get(entry.getValue().monsterId).getLevel());
                    umsg.put("monFile", world.monsters.get(entry.getValue().monsterId).getFile());
                    umsg.put("monLink", world.monsters.get(entry.getValue().monsterId).getLinkage());
                    umsg.put("spawnTime", spawnTimes.get(entry.getValue().monsterId));
                    umsg.put("timeLimit", entry.getValue().timeLimit * 1000);
                    umsg.put("timestamp", System.currentTimeMillis());

//                    LinkedList listOfChannels = world.zone.getAllUsersInZone();
//                    for (Object temp : listOfChannels) {
//                        User tgt = ExtensionHelper.instance().getUserByChannel((SocketChannel) temp);
//                        if (tgt == null) continue;
//                        world.send(umsg, tgt);
//                    }

                    for (Channel chan : UserManager.userManager().getChannelList()) {
                        User tgt = (User) chan.attr(User.PLAYER_KEY).get();
                        if (tgt == null) continue;
                        world.send(umsg, tgt);
                    }

                    Map<Integer, MonsterState> monsters = (ConcurrentHashMap) bossRoom.properties.get(Rooms.MONSTERS);
                    for (MonsterState actMon : monsters.values()) {
                        if (world.worldbosses.containsKey(actMon.getMonster().getId())) {
                            this.world.scheduleTask(new MonsterRespawn(this.world, actMon), 1, TimeUnit.SECONDS);
                            break;
                        }
                    }

                    world.db.getJdbc().run("UPDATE monsters_bosses SET SpawnTime = NOW() WHERE MonsterID = ? AND MapID = ?", entry.getValue().monsterId, entry.getValue().mapId);

//                    if (!(ConfigData.DISCORD_BOT_TOKEN).isEmpty() && world.discord.api.getChannelById(ConfigData.DISCORD_CHANNEL_WORLDBOSS).isPresent()) {
//                        TextChannel chn = (TextChannel) world.discord.api.getChannelById(ConfigData.DISCORD_CHANNEL_WORLDBOSS).get();
//                        EmbedBuilder embed = new EmbedBuilder();
//                        embed.setTitle("**" + world.monsters.get(entry.getValue().monsterId).getName() + "** just spawned.");
//                        embed.setDescription("The world boss **" + world.monsters.get(entry.getValue().monsterId).getName() + "** just spawned at **" + bossRoom.getName() + "**. Here is an accurate list of all the world bosses with their names aligned together with their current time remaining in game.");
//                        embed.setColor(Color.GREEN);
//                        for (Map.Entry<Integer, WorldBoss> list : world.worldbosses.entrySet()) {
//                            long remaining = ((deathTimes.get(list.getValue().monsterId) + list.getValue().spawnInterval) - System.currentTimeMillis());
//
//                            String remainingFormat;
//                            if (remaining <= 0)
//                                remainingFormat = "Spawned";
//                            else
//                                remainingFormat = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(remaining), TimeUnit.MILLISECONDS.toMinutes(remaining) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(remaining)), TimeUnit.MILLISECONDS.toSeconds(remaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remaining)));
//
//                            embed.addField(world.monsters.get(list.getValue().monsterId).getName(), remainingFormat, true);
//                        }
//                        embed.setUrl(ConfigData.SERVER_URL);
//                        chn.sendMessage(embed);
//                    }
                }
                if (System.currentTimeMillis() - spawnTimes.get(entry.getValue().monsterId) > (entry.getValue().timeLimit * 1000) && entry.getValue().isActive) {
                    entry.getValue().isActive = false;
                    deathTimes.put(entry.getValue().monsterId, System.currentTimeMillis());

                    Room bossRoom = RoomManager.roomManager().getRoomByName(world.worldBossMaps.get(entry.getValue().mapId).toLowerCase() + "-" + entry.getValue().roomNumber);

                    if (bossRoom != null && bossRoom.getUserCount() > 0) {
                        Set<User> users = new HashSet();
                        for (User user : bossRoom.getAllUsers())
                            users.add(user);
                        this.world.scheduleTask(new WarpUser(this.world, users), 2L, TimeUnit.SECONDS);
                    }

                    Map<Integer, MonsterState> monsters = (ConcurrentHashMap) bossRoom.properties.get(Rooms.MONSTERS);

                    for (MonsterState actMon : monsters.values()) {
                        if (world.worldbosses.containsKey(actMon.getMonster().getId())) {
                            if (actMon.attacking != null) actMon.attacking.cancel(true);
                            for (RemoveAura ra : actMon.state.getAuras()) {
                                ra.run();
                                ra.cancel();
                            }
                            world.db.jdbc.run("UPDATE monsters_bosses SET DeathTime = NOW() WHERE MonsterID = ? AND MapID = ?", entry.getValue().monsterId, entry.getValue().mapId);
                            actMon.state.die();
                            world.worldbosses.get(actMon.getMonster().getId()).isActive = false;
                            deathTimes.put(entry.getValue().monsterId, System.currentTimeMillis());
                        }
                    }
                    world.send(new String[]{"warning", "Time limit has exceeded. You may try again next time."}, bossRoom.getChannellList());
                }
            }
        } catch (Exception e) {
            log.warn("Error message: " + e.getMessage());
        }
    }
}