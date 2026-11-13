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
public class Cell {
	
    private String frame, pad;
	
    public static final ResultSetMapper<Integer, Cell> resultSetMapper = new ResultSetMapper<Integer, Cell>() {
		
		@Override
        public AbstractMap.SimpleEntry<Integer, Cell> mapRow(ResultSet rs) throws SQLException {
			return new AbstractMap.SimpleEntry<Integer, Cell>(rs.getInt("id"), new Cell(rs.getString("Frame"), rs.getString("Pad")));
        }
    };

    public Cell(String frame, String pad) {
        this.frame = frame;
        this.pad = pad;
    }

    public String getFrame() {
        return this.frame;
    }

    public String getPad() {
        return this.pad;
    }
}
