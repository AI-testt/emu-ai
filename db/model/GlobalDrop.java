package ains.db.model;

import jdbchelper.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

public class GlobalDrop {

    private int id;
    private int ItemID;
    private double Chance;
    private int Quantity;

    public static final ResultSetMapper<Integer, GlobalDrop> resultSetMapper = new ResultSetMapper<Integer, GlobalDrop>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, GlobalDrop> mapRow(ResultSet rs) throws SQLException {
            GlobalDrop gd = new GlobalDrop();

            gd.id = rs.getInt("id");
            gd.ItemID = rs.getInt("ItemID");
            gd.Chance = rs.getDouble("Chance");
            gd.Quantity = rs.getInt("Quantity");

            return new AbstractMap.SimpleEntry<Integer, GlobalDrop>(gd.getId(), gd);
        }
    };

    public int getId() {
        return id;
    }
    public int getItemID() {
        return ItemID;
    }

    public double getChance() {
        return Chance;
    }


    public int getQuantity() {
        return Quantity;
    }
}
