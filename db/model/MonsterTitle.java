//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

public class MonsterTitle {

    private int id;
    private String name, color;

    public static final ResultSetMapper<Integer, MonsterTitle> resultSetMapper = new ResultSetMapper<Integer, MonsterTitle>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, MonsterTitle> mapRow(ResultSet rs) throws SQLException {
            MonsterTitle montitle = new MonsterTitle();

            montitle.id = rs.getInt("id");
            montitle.name = rs.getString("Name");
            montitle.color = rs.getString("Color");

            return new AbstractMap.SimpleEntry<Integer, MonsterTitle>(montitle.id, montitle);
        }
    };

    public String getColor() { 
	    return this.color; 
	}

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }
}



