//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

import jdbchelper.ResultSetMapper;

public class Hair {
	
    private int id;
    private String name, file, gender;

	public static final ResultSetMapper<Integer, Hair> resultSetMapper = new ResultSetMapper<Integer, Hair>() {
		
		@Override
		public AbstractMap.SimpleEntry<Integer, Hair> mapRow(ResultSet rs) throws SQLException {
            Hair hair = new Hair();
			
            hair.id = rs.getInt("id");
            hair.name = rs.getString("Name");
            hair.file = rs.getString("File");
            hair.gender = rs.getString("Gender");

			return new AbstractMap.SimpleEntry<Integer, Hair>(hair.id, hair);
        }
    };

    public String getName() {
        return this.name;
    }

    public String getFile() {
        return this.file;
    }

    public String getGender() {
        return this.gender;
    }

    public int getId() {
        return this.id;
    }
}
