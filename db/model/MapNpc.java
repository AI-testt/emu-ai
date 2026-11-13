package ains.db.model;

import jdbchelper.BeanCreator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapNpc {

    private int id, npcId, mapId;
    private String frame;
    private String X;
    private String Y;
    private String turn;
    private String state;
    private String animation;

    private Boolean aggresive;
    private Boolean staff;

    public JSONArray dialogues;
    public JSONArray actions;

    public Map<Integer, Integer> items;

    public static final BeanCreator<Set<MapNpc>> setCreator = rs -> {
        Set<MapNpc> npcs = new HashSet<>();

        do {
            npcs.add(mapNpc(rs));
        } while (rs.next());

        return npcs;
    };

    public static final BeanCreator<JSONArray> beanDialogues = rs -> {
        JSONArray dialogues = new JSONArray();
        do {
            dialogues.add(rs.getString("Dialog"));
        } while (rs.next());

        return dialogues;
    };

    public static final BeanCreator<JSONArray> beanActions = rs -> {
        JSONArray actions = new JSONArray();

        do {
            JSONObject action = new JSONObject();
            action.element("Name", rs.getString("Name"));
            action.element("NameColor", rs.getString("NameColor").replace("#", ""));
            action.element("Icon", rs.getString("Icon"));
            action.element("Action", rs.getString("Action"));
            action.element("Value", rs.getString("Value"));
            action.element("isStaffOnly", rs.getBoolean("Staff"));

            actions.element(action);
        } while (rs.next());

        return actions;
    };

    private static MapNpc mapNpc(ResultSet rs) throws SQLException {
        MapNpc mapNpc = new MapNpc();
        mapNpc.frame = rs.getString("Frame");
        mapNpc.X = rs.getString("X");
        mapNpc.Y = rs.getString("Y");
        mapNpc.turn = rs.getString("Turn");
        mapNpc.state = rs.getString("State");
        mapNpc.animation = rs.getString("Animation");
        mapNpc.id = rs.getInt("id");
        mapNpc.mapId = rs.getInt("MapID");
        mapNpc.npcId = rs.getInt("NpcID");
        mapNpc.aggresive = rs.getBoolean("Aggresive");
        mapNpc.staff = rs.getBoolean("Staff");

        return mapNpc;
    }

    public int getId() {
        return id;
    }

    public int getMapId() {
        return mapId;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getNpcMapId() {
        return id;
    }

    public Boolean getAggresive() {
        return aggresive;
    }

    public Boolean getStaff() {
        return staff;
    }

    public String getFrame() {
        return frame;
    }

    public String getX() {
        return X;
    }

    public String getY() {
        return Y;
    }

    public String getTurn() {
        return turn;
    }

    public String getState() {
        return state;
    }

    public String getAnimation() {
        return animation;
    }

    public Boolean isAggresive() {
        return aggresive;
    }

    public Boolean isStaff() {
        return staff;
    }
}
