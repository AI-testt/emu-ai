//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import jdbchelper.BeanCreator;

public class MapMonster {
	
    private int monMapId, monsterId, mapId;
    private String frame;
	private Boolean aggresive;
	
    public static final BeanCreator<Set<MapMonster>> setCreator = new BeanCreator<Set<MapMonster>>() {

        @Override
        public Set<MapMonster> createBean(ResultSet rs) throws SQLException {
            Set<MapMonster> monsters = new HashSet<MapMonster>();

            MapMonster mapMonster = new MapMonster();
			
            mapMonster.frame = rs.getString("Frame");
            mapMonster.monsterId = rs.getInt("MonsterID");
			mapMonster.mapId = rs.getInt("MapID");
            mapMonster.monMapId = rs.getInt("MonMapID");
			mapMonster.aggresive = rs.getBoolean("Aggresive");
			
            monsters.add(mapMonster);

            while(rs.next()) {
                mapMonster = new MapMonster();
				
                mapMonster.frame = rs.getString("Frame");
                mapMonster.monsterId = rs.getInt("MonsterID");
				mapMonster.mapId = rs.getInt("MapID");
                mapMonster.monMapId = rs.getInt("MonMapID");
				mapMonster.aggresive = rs.getBoolean("Aggresive");
				
                monsters.add(mapMonster);
            }

            return monsters;
        }
    };

    public int getMonMapId() {
        return this.monMapId;
    }

    public int getMonsterId() {
        return this.monsterId;
    }
	
    public int getMapId() { return mapId; }

    public String getFrame() {
        return this.frame;
    }
	
	public boolean isAggresive() {
		return this.aggresive;
	}
}
