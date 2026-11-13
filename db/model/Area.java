/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

/**
 * @author Mystical
 */
public class Area {

    protected String name, file;
    protected int id, maxPlayers, reqLevel;
    private boolean upgrade, staff, PvP;
    public Set<MapNpc> npcs;
    public Set<MapMonster> monsters;
    public Set<Integer> items;
    public Map<Integer, Integer> requirements;

    public static final BeanCreator<Set<Integer>> beanItems = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();

            set.add(rs.getInt("ItemID"));

            while(rs.next())
                set.add(rs.getInt("ItemID"));

            return set;
        }
    };

    public static final ResultSetMapper<Integer, Integer> requiredItem = new ResultSetMapper<Integer, Integer>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("ReqItemID"), rs.getInt("Quantity"));
        }
    };

    public static final ResultSetMapper<String, Area> resultSetMapper = new ResultSetMapper<String, Area>() {

        @Override
        public AbstractMap.SimpleEntry<String, Area> mapRow(ResultSet rs) throws SQLException {
            Area area = new Area();

            area.id = rs.getInt("id");
            area.maxPlayers = rs.getInt("MaxPlayers");
            area.reqLevel = rs.getInt("ReqLevel");

            area.name = rs.getString("Name").toLowerCase();
            area.file = rs.getString("File");

            area.upgrade = rs.getBoolean("Upgrade");
            area.staff = rs.getBoolean("Staff");
            area.PvP = rs.getBoolean("PvP");

            return new AbstractMap.SimpleEntry<String, Area>(area.getName(), area);
        }
    };

    public Area() {
        this.npcs = Collections.EMPTY_SET;
        this.monsters = Collections.EMPTY_SET;
        this.items = Collections.EMPTY_SET;
        this.requirements = Collections.EMPTY_MAP;
    }

    public String getName() {
        return this.name;
    }

    public String getFile() {
        return this.file;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getReqLevel() {
        return this.reqLevel;
    }

    public boolean isUpgrade() {
        return this.upgrade;
    }

    public boolean isStaff() {
        return this.staff;
    }

    public int getId() {
        return this.id;
    }

    public boolean isPvP() {
        return this.PvP;
    }
}
