package ains.world.npcs;

import ains.db.model.Skill;

import java.util.HashMap;
import java.util.Map;

public class NpcSkills {

    private Map<Skill, Long> skills;

    public NpcSkills() {
        skills = new HashMap<Skill, Long>();
    }

    public void addSkill(Skill skill) {
        if (skill == null)
            return;

        skills.put(skill, System.currentTimeMillis());
    }

    public Skill getSkill() {
        long now = System.currentTimeMillis();

        for (Map.Entry<Skill, Long> entry : this.skills.entrySet()) {
            Skill skill = entry.getKey();

            if (skill == null)
                continue;

            if (now >= entry.getValue()) {
                entry.setValue(now + skill.getCooldown());
                return skill;
            }
        }

        return null;
    }
}
