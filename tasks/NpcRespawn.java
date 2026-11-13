package ains.tasks;

import ains.world.World;
import ains.world.npcs.NpcAttack;

public class NpcRespawn implements Runnable {

    private NpcAttack ai;
    private World world;

    public NpcRespawn(World world, NpcAttack ai) {
        this.ai = ai;
        this.world = world;
    }

    @Override
    public void run() {
        this.ai.restore();
        this.world.send(new String[]{"respawnMon", Integer.toString(ai.getMapNpc().getNpcMapId())}, this.ai.getRoom().getChannellList());
    }
}
