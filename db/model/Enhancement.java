//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

public class Enhancement {
	
    private String name;
    private int id, patternId, rarity, DPS, level;

	public static final ResultSetMapper<Integer, Enhancement> resultSetMapper = new ResultSetMapper<Integer, Enhancement>() {
		
		@Override
        public AbstractMap.SimpleEntry<Integer, Enhancement> mapRow(ResultSet rs) throws SQLException {
            Enhancement enhancement = new Enhancement();
			
            enhancement.id = rs.getInt("id");
            enhancement.patternId = rs.getInt("PatternID");
            enhancement.rarity = rs.getInt("Rarity");
            enhancement.DPS = rs.getInt("DPS");
            enhancement.level = rs.getInt("Level");
			
			enhancement.name = rs.getString("Name");
			
            return new AbstractMap.SimpleEntry<Integer, Enhancement>(enhancement.id, enhancement);
        }
    };

    public String getName() {
        return this.name;
    }

    public int getPatternId() {
        return this.patternId;
    }

    public int getRarity() {
        return this.rarity;
    }

    public int getDPS() {
        return this.DPS;
    }

    public int getLevel() {
        return this.level;
    }

    public int getId() {
        return this.id;
    }
}
