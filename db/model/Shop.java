package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Shop {
	
    private int id, reqItemId;
    private String name, field;
    private boolean house, upgrade, staff, limited;
    public Map<Integer, Integer> items;

    public static final BeanCreator<Set<Integer>> beanLocations = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet<Integer>();
            set.add(rs.getInt("MapID"));

            while(rs.next())
                set.add(rs.getInt("MapID"));

            return set;
        }
    };

    public static final ResultSetMapper<Integer, Integer> shopItemsMapper = new ResultSetMapper<Integer, Integer>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("id"), rs.getInt("ItemID"));
        }
    };

    public static final ResultSetMapper<Integer, Shop> resultSetMapper = new ResultSetMapper<Integer, Shop>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Shop> mapRow(ResultSet rs) throws SQLException {
            Shop shop = new Shop();
			
            shop.id = rs.getInt("id");

            shop.name = rs.getString("Name");
            shop.field = rs.getString("Field");
			
            shop.house = rs.getBoolean("House");
            shop.upgrade = rs.getBoolean("Upgrade");
            shop.staff = rs.getBoolean("Staff");
            shop.limited = rs.getBoolean("Limited");

            shop.reqItemId = rs.getInt("ReqItemID");

            return new AbstractMap.SimpleEntry<Integer, Shop>(shop.getId(), shop);
        }
    };

    public String getName() {
        return this.name;
    }

    public String getField() {
        return this.field;
    }

    public boolean isHouse() {
        return this.house;
    }

    public boolean isUpgrade() {
        return this.upgrade;
    }

    public boolean isStaff() {
        return this.staff;
    }

    public boolean isLimited() {
        return this.limited;
    }

    public int getId() {
        return this.id;
    }

    public int getReqItemId() {
        return this.reqItemId;
    }

}
