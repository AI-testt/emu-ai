/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */

package ains.world.monsters;

import ains.db.model.Skill;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mystical
 */
public class MonsterSkills {

    private Map<Skill, Long> skills;
    
    public MonsterSkills() {
        skills = new HashMap<Skill, Long>();
    }
    
    public void addSkill(Skill skill) {
        if (skill == null)
            return;

        skills.put(skill, System.currentTimeMillis());
    }

    public Skill getSkill() {
        long now = System.currentTimeMillis();

        for(Map.Entry<Skill, Long> entry : this.skills.entrySet()) {
            Skill skill = entry.getKey();

            if(skill == null)
                continue;

            if(now >= entry.getValue()) {
                entry.setValue(now + skill.getCooldown());
                return skill;
            }
        }

        return null;
    }
}
