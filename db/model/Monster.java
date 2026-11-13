//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Monster {

    private int id, health, mana, level, gold, coins, experience, reputation, DPS, minDmg, maxDmg, speed, respawn;
    private int ClassPoint;
    private String name, file, linkage;
    private boolean immune;
    private boolean worldboss;
    private double DR;

    public Set<MonsterDrop> drops;
    public Set<Integer> skills;

    public static final BeanCreator<Set<Integer>> beanSkills = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> skills = new HashSet<Integer>();

            skills.add(rs.getInt("SkillID"));

            while (rs.next()) skills.add(rs.getInt("SkillID"));

            return skills;
        }

    };

    public static final BeanCreator<Set<MonsterDrop>> beanDrops = new BeanCreator<Set<MonsterDrop>>() {

        @Override
        public Set<MonsterDrop> createBean(ResultSet rs) throws SQLException {
            Set<MonsterDrop> drops = new HashSet<MonsterDrop>();

            MonsterDrop md = new MonsterDrop();

            md.itemId = rs.getInt("ItemID");
            md.quantity = rs.getInt("Quantity");
            md.chance = rs.getDouble("Chance");
            drops.add(md);

            while (rs.next()) {
                MonsterDrop md2 = new MonsterDrop();
                md2.itemId = rs.getInt("ItemID");
                md2.quantity = rs.getInt("Quantity");
                md2.chance = rs.getDouble("Chance");
                drops.add(md2);
            }

            return drops;
        }
    };

    public static final ResultSetMapper<Integer, Monster> resultSetMapper = new ResultSetMapper<Integer, Monster>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Monster> mapRow(ResultSet rs) throws SQLException {
            Monster monster = new Monster();

            monster.id = rs.getInt("id");
            monster.name = rs.getString("Name");
            monster.file = rs.getString("File");
            monster.linkage = rs.getString("Linkage");
            monster.health = rs.getInt("Health");
            monster.mana = rs.getInt("Mana");
            monster.level = rs.getInt("Level");
            monster.gold = rs.getInt("Gold");
            monster.coins = rs.getInt("Coin");
            monster.experience = rs.getInt("Experience");
            monster.ClassPoint = rs.getInt("ClassPoint");
            monster.reputation = rs.getInt("Reputation");
            monster.DPS = rs.getInt("DPS");
            monster.minDmg = (int) Math.floor((monster.DPS - (monster.DPS * 0.1)));
            monster.maxDmg = (int) Math.ceil((monster.DPS + (monster.DPS * 0.1)));
            monster.DR = rs.getDouble("DamageReduction");
            monster.speed = rs.getInt("Speed");
            monster.respawn = rs.getInt("Respawn");
            monster.immune = rs.getBoolean("Immune");
            monster.worldboss = rs.getBoolean("WorldBoss");

            return new AbstractMap.SimpleEntry<Integer, Monster>(monster.id, monster);
        }
    };

    public String getName() {
        return this.name;
    }

    public String getFile() {
        return this.file;
    }

    public String getLinkage() {
        return this.linkage;
    }

    public int getHealth() {
        return this.health;
    }

    public int getMana() {
        return this.mana;
    }

    public int getLevel() {
        return this.level;
    }

    public int getGold() {
        return this.gold;
    }

    public int getCoins() {
        return this.coins;
    }

    public int getExperience() {
        return this.experience;
    }

    public int getClassPoint() {
        return ClassPoint;
    }

    public int getReputation() {
        return this.reputation;
    }

    public int getDPS() {
        return this.DPS;
    }

    public int getId() {
        return this.id;
    }

    public int getMinDmg() {
        return minDmg;
    }

    public int getMaxDmg() {
        return maxDmg;
    }

    public int getSpeed() {
        return this.speed;
    }

    public int getRespawn() {
        return this.respawn;
    }

    public double getDamageReduction() {
        return DR;
    }

    public boolean isImmune() {
        return this.immune;
    }

    public boolean isWorldBoss() { return this.worldboss; }

}
