package ains.db.model;

import jdbchelper.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Date;

public class ShopSeasonal {

    private int ShopID;
    private Date EndDate;

    public static final ResultSetMapper<Integer, ShopSeasonal> resultSetMapper = new ResultSetMapper<Integer, ShopSeasonal>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, ShopSeasonal> mapRow(ResultSet rs) throws SQLException {
            ShopSeasonal shopSeasonal = new ShopSeasonal();

            shopSeasonal.ShopID = rs.getInt("ShopID");
            shopSeasonal.EndDate = rs.getDate("EndDate");

            return new AbstractMap.SimpleEntry<Integer, ShopSeasonal>(shopSeasonal.ShopID, shopSeasonal);
        }
    };

    public int getShopID() {
        return ShopID;
    }

    public Date getEndDate() {
        return EndDate;
    }
}
