package ains.db.model;

import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Npc {

    private int id, health, mana, level, gold, coins, experience, reputation, DPS, minDmg, maxDmg, speed, respawn;
    private String name;
    private String nameColor;
    private String Subtitle;
    private String SubtitleColor;
    private int hair;
    private String gender;
    private boolean immune;
    private double DR;
    private String ColorHair;
    private String ColorSkin;
    private String ColorEye;
    private String ColorBase;
    private String ColorTrim;
    private String ColorAccessory;

    public Set<Integer> items;
    public Set<NpcDrop> drops;
    public Set<Integer> skills;

    public static final BeanCreator<Set<Integer>> beanItems = rs -> {
        Set<Integer> items = new HashSet<>();
        do {
            items.add(rs.getInt("ItemID"));
        } while (rs.next());

        return items;
    };

    public static final BeanCreator<Set<Integer>> beanSkills = rs -> {
        Set<Integer> skills = new HashSet<>();
        do {
            skills.add(rs.getInt("SkillID"));
        } while (rs.next());
        return skills;
    };

    public static final BeanCreator<Set<NpcDrop>> beanDrops = rs -> {
        Set<NpcDrop> drops = new HashSet<>();
        do {
            NpcDrop nd = new NpcDrop();
            nd.itemId = rs.getInt("ItemID");
            nd.quantity = rs.getInt("Quantity");
            nd.chance = rs.getDouble("Chance");
            drops.add(nd);
        } while (rs.next());
        return drops;
    };

    public static final ResultSetMapper<Integer, Npc> resultSetMapper = rs -> {
        Npc npc = new Npc();

        npc.id = rs.getInt("id");
        npc.name = rs.getString("Name");
        npc.nameColor = rs.getString("NameColor");
        npc.Subtitle = rs.getString("Subtitle");
        npc.SubtitleColor = rs.getString("SubtitleColor");
        npc.hair = rs.getInt("HairID");
        npc.gender = rs.getString("Gender");
        npc.health = rs.getInt("Health");
        npc.mana = rs.getInt("Mana");
        npc.level = rs.getInt("Level");
        npc.gold = rs.getInt("Gold");
        npc.coins = rs.getInt("Coin");
        npc.experience = rs.getInt("Experience");
        npc.reputation = rs.getInt("Reputation");
        npc.DPS = rs.getInt("DPS");
        npc.minDmg = (int) Math.floor((npc.DPS - (npc.DPS * 0.1)));
        npc.maxDmg = (int) Math.ceil((npc.DPS + (npc.DPS * 0.1)));
        npc.DR = rs.getDouble("DamageReduction");
        npc.speed = rs.getInt("Speed");
        npc.respawn = rs.getInt("Respawn");
        npc.immune = rs.getBoolean("Immune");
        npc.ColorAccessory = rs.getString("ColorAccessory");
        npc.ColorBase = rs.getString("ColorBase");
        npc.ColorTrim = rs.getString("ColorTrim");
        npc.ColorSkin = rs.getString("ColorSkin");
        npc.ColorEye = rs.getString("ColorEye");
        npc.ColorHair = rs.getString("ColorHair");

        return new AbstractMap.SimpleEntry<>(npc.id, npc);
    };

    public int getId() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public int getMana() {
        return mana;
    }

    public int getLevel() {
        return level;
    }

    public int getGold() {
        return gold;
    }

    public int getCoins() {
        return coins;
    }

    public int getExperience() {
        return experience;
    }

    public int getReputation() {
        return reputation;
    }

    public int getDPS() {
        return DPS;
    }

    public int getMinDmg() {
        return minDmg;
    }

    public int getMaxDmg() {
        return maxDmg;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRespawn() {
        return respawn;
    }

    public String getName() {
        return name;
    }

    public String getNameColor() {
        return nameColor.replace("#", "");
    }

    public String getSubtitle() {
        return Subtitle;
    }

    public String getSubtitleColor() {
        return SubtitleColor.replace("#", "");
    }

    public int getHair() {
        return hair;
    }

    public String getGender() {
        return gender;
    }

    public boolean isImmune() {
        return immune;
    }

    public double getDamageReduction() {
        return DR;
    }

    public String getColorHair() {
        return ColorHair.replace("#", "");
    }

    public String getColorSkin() {
        return ColorSkin.replace("#", "");
    }

    public String getColorEye() {
        return ColorEye.replace("#", "");
    }

    public String getColorBase() {
        return ColorBase.replace("#", "");
    }

    public String getColorTrim() {
        return ColorTrim.replace("#", "");
    }

    public String getColorAccessory() {
        return ColorAccessory.replace("#", "");
    }

    public Set<NpcDrop> getDrops() {
        return drops;
    }

    public Set<Integer> getSkills() {
        return skills;
    }
}
