/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Aura {
	
    private int id, duration, maxStack, dotInterval;
    private String name, category;
    private double damageIncrease, damageTakenDecrease, chance, dotDamage;
    public Set<Integer> effects;
    public boolean selfCast = false;
	
    public static final BeanCreator<Set<Integer>> beanEffects = new BeanCreator<Set<Integer>>() {
		
		@Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();
            set.add(rs.getInt("id"));

            while(rs.next())
                set.add(rs.getInt("id"));

            return set;
        }
    };

	public static final ResultSetMapper<Integer, Aura> resultSetMapper = new ResultSetMapper<Integer, Aura>() {
		
		@Override
        public AbstractMap.SimpleEntry<Integer, Aura> mapRow(ResultSet rs) throws SQLException {
            Aura aura = new Aura();
			
            aura.id = rs.getInt("id");
            aura.duration = rs.getInt("Duration");
            aura.dotInterval = rs.getInt("DoTInterval");
            aura.dotDamage = rs.getDouble("DoTDamage");
			
            aura.name = rs.getString("Name");
            aura.category = rs.getString("Category");
			
            aura.damageIncrease = rs.getDouble("DamageIncrease");
            aura.damageTakenDecrease = rs.getDouble("DamageTakenDecrease");
            aura.chance = rs.getDouble("Chance");
            aura.maxStack = Math.max(1, rs.getInt("MaxStack"));

			return new AbstractMap.SimpleEntry<Integer, Aura>(aura.id, aura);
        }
    };

    public JSONArray getAuraArray(boolean isNew, int stack) {
        JSONArray auras = new JSONArray();
        JSONObject auraInfo = new JSONObject();

        if (!this.getCategory().isEmpty() && !this.getCategory().equals("d")) {
            auraInfo.put("cat", this.getCategory());
            if (this.getCategory().equals("stun"))
                auraInfo.put("s", "s");
        }

        auraInfo.put("nam", this.getName());
        auraInfo.put("t", "s");
        auraInfo.put("dur", String.valueOf(this.getDuration()));
        auraInfo.put("isNew", isNew);

        // Aura yang bisa stack mengirim "val" berisi jumlah stack saat ini,
        // sama seperti protokol server AQW resmi (lihat Temporal Rift).
        // Client r5 (hudAuraSync) membaca jumlah stack dari "stk".
        if (this.maxStack > 1) {
            auraInfo.put("val", stack);
            auraInfo.put("stk", stack);
        }

        auras.add(auraInfo);

        return auras;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public double getDamageIncrease() {
        return this.damageIncrease;
    }

    public double getDamageTakenDecrease() {
        return this.damageTakenDecrease;
    }

    public double getChance() {
        return this.chance;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getMaxStack() {
        return this.maxStack;
    }

    public int getDoTInterval() {
        return this.dotInterval;
    }

    public double getDoTDamage() {
        return this.dotDamage;
    }
}
