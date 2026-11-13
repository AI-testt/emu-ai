
package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import jdbchelper.ResultSetMapper;

public class WorldBoss {

    public int monsterId;
    public int mapId;
    public int kills;
    public int deaths;

    public long spawnInterval;
    public long timeLimit;

    public String description;
    public String lastDefeat;
    public String image;

    public boolean isActive = false;
    public boolean isNotified = false;

    public static final ResultSetMapper<Integer, WorldBoss> resultSetMapper =
            new ResultSetMapper<Integer, WorldBoss>() {

                @Override
                public AbstractMap.SimpleEntry<Integer, WorldBoss> mapRow(ResultSet rs)
                        throws SQLException {

                    WorldBoss worldboss = new WorldBoss();

                    worldboss.monsterId = rs.getInt("MonsterID");
                    worldboss.mapId = rs.getInt("MapID");

                    worldboss.spawnInterval = rs.getLong("SpawnInterval");
                    worldboss.timeLimit = rs.getLong("TimeLimit");

                    worldboss.kills = rs.getInt("Kills");
                    worldboss.deaths = rs.getInt("Deaths");

                    worldboss.lastDefeat = rs.getString("LastDefeat");
                    worldboss.description = rs.getString("Description");
                    worldboss.image = rs.getString("Image");

                    return new AbstractMap.SimpleEntry<Integer, WorldBoss>(
                            worldboss.monsterId,
                            worldboss
                    );
                }
            };
}
