package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

import com.google.common.collect.Multimap;
import jdbchelper.ResultSetMapper;

public class Skill {

    private String name, animation, description, icon, dsrc, reference, target, effects, type, strl;
    private double damage;
    private int id, mana, manaBack, range, hitTargets, cooldown;

    public Multimap<Integer, SkillAura> auras;

    public static final ResultSetMapper<Integer, Skill> resultSetMapper = new ResultSetMapper<Integer, Skill>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Skill> mapRow(ResultSet rs) throws SQLException {
            Skill skill = new Skill();

            skill.id = rs.getInt("id");

            skill.name = rs.getString("Name");
            skill.animation = rs.getString("Animation");
            skill.description = rs.getString("Description");
            skill.icon = rs.getString("Icon");
            skill.dsrc = rs.getString("Dsrc");
            skill.reference = rs.getString("Reference");
            skill.target = rs.getString("Target");
            skill.effects = rs.getString("Effects");
            skill.type = rs.getString("Type");
            skill.strl = rs.getString("Strl");

            skill.damage = rs.getDouble("Damage");

            skill.mana = rs.getInt("Mana");
            skill.manaBack = rs.getInt("ManaBack");
            skill.range = rs.getInt("Range");
            skill.hitTargets = rs.getInt("HitTargets");
            skill.cooldown = rs.getInt("Cooldown");

            return new AbstractMap.SimpleEntry<Integer, Skill>(skill.id, skill);
        }
    };

    public String getName() {
        return this.name;
    }

    public String getAnimation() {
        return this.animation;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getDsrc() {
        return this.dsrc;
    }

    public String getReference() {
        return this.reference;
    }

    public String getTarget() {
        return this.target;
    }

    public String getEffects() {
        return this.effects;
    }

    public String getType() {
        return this.type;
    }

    public String getStrl() {
        return this.strl;
    }

    public double getDamage() {
        return this.damage;
    }

    public int getMana() {
        return this.mana;
    }

    public int getManaBack() {
        return this.manaBack;
    }

    public int getRange() {
        return this.range;
    }

    public int getHitTargets() {
        return this.hitTargets;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getId() {
        return this.id;
    }

    public boolean hasAura() {
        return !this.auras.isEmpty();
    }

}
