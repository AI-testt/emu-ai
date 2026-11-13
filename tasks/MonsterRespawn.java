/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.tasks;

import ains.world.monsters.MonsterAttack;
import ains.world.World;

/**
 *
 * @author Mystical
 */
public class MonsterRespawn implements Runnable {

    private MonsterAttack ai;
    private World world;

    public MonsterRespawn(World world, MonsterAttack ai) {
        this.ai = ai;
        this.world = world;
    }

    @Override
    public void run() {
        this.ai.restore();
        this.world.send(new String[]{"respawnMon", Integer.toString(ai.getMapMonster().getMonMapId())}, this.ai.getRoom().getChannellList());
    }

}
