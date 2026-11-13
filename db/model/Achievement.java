/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class Achievement {

    private int id;
    private String name, description;

    public static final ResultSetMapper<Integer, Achievement> resultSetMapper = new ResultSetMapper<Integer, Achievement>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Achievement> mapRow(ResultSet rs) throws SQLException {
            Achievement achievement = new Achievement();

            achievement.id = rs.getInt("id");

            achievement.name = rs.getString("Name");
            achievement.description = rs.getString("Description");

            return new AbstractMap.SimpleEntry<Integer, Achievement>(achievement.id, achievement);
        }
    };

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getId() {
        return this.id;
    }
	
}



