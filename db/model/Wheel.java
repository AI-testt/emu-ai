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
 * @author rin
 */
public class Wheel {

    public int itemId, quantity;
    public double chance;
    public boolean globalMsg;

    public static final ResultSetMapper<Integer, Wheel> resultSetMapper = new ResultSetMapper<Integer, Wheel>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Wheel> mapRow(ResultSet rs) throws SQLException {
            Wheel wheel = new Wheel();

            wheel.itemId = rs.getInt("ItemID");
            wheel.chance = rs.getDouble("Chance");
            wheel.quantity = rs.getInt("Quantity");

            return new AbstractMap.SimpleEntry<Integer, Wheel>(wheel.itemId, wheel);
        }
    };

}
