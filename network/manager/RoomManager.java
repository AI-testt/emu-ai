package ains.network.manager;

import ains.network.sfs.Room;
import ains.network.sfs.User;
import ains.requests.RequestWorker;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ains.network.sfs.SFS.sfs;
import static ains.world.World.world;

public class RoomManager {

    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);

    private Map<Integer, Room> roomList = new ConcurrentHashMap<>();
    private static RoomManager _instance = new RoomManager();

    public void addRoom(Room rm) {
        if (!this.roomList.containsKey(rm.getId())) {
            this.roomList.put(rm.getId(), rm);
        }
    }

    public void removeRoom(int id) {
        if (this.roomList.containsKey(id)) {
            this.roomList.remove(id);
        }
    }

    public int getRoomCount() {
        return this.roomList.size();
    }

    public Room getRoom(int roomId) {
        return !this.roomList.containsKey(roomId) ? null : this.roomList.get(roomId);
    }

    public Room getRoomByName(String name) {
        Room found = null;

        for (Room room : this.roomList.values()) {
            if (room.getName().equals(name)) {
                found = room;
            }
        }

        return found;
    }

    public LinkedList<Room> getAllRooms() {
        LinkedList<Room> ll = new LinkedList<>();

        for (Room room : this.roomList.values()) {
            ll.add(room);
        }

        return ll;
    }

    public void joinRoom(User u, String name) {
        if (u.getRoom() != -1) {
            Room room = this.getRoom(u.getRoom());
            world().rooms.exit(room, u);
            room.removeUser(u);
            if (room.getUserCount() <= 0) {
                this.removeRoom(room.getId());
            }
        }

        Room room = this.getRoomByName(name);
        room.addUser(u);
        JSONObject userObj = world().users.getProperties(u, room);

        JSONObject uJoin = new JSONObject();
        uJoin.put("cmd", "uotls");
        uJoin.put("o", userObj);
        uJoin.put("unm", u.getName());
        world().sendToRoomButOne(uJoin, u, room);

        synchronized (room.roomSync) {
            // joinRoom
            JSONArray usersArr = new JSONArray();
            for (User user : room.getAllUsers()) {
                JSONObject rt = new JSONObject();
                rt.put("networkId", user.getUserId());
                rt.put("username", user.getName());
                usersArr.add(rt);
            }
            JSONObject joinRoom = new JSONObject();
            joinRoom.put("cmd", "joinRoom");
            joinRoom.put("users", usersArr);
            sfs().sendResponse(joinRoom.toString(), u.getNetwork().getChannel());

            // enterRoom
            JSONObject enterRoom = new JSONObject();
            enterRoom.put("cmd", "enterRoom");
            enterRoom.put("networkId", u.getUserId());
            enterRoom.put("username", u.getName());
            sfs().sendResponseRemoveUser(enterRoom.toString(), u, room.getChannellList());
        }

//        synchronized (room.roomSync) {
//            StringBuffer response = sfs().makeHeader("sys", room.getId(), "joinOK");
//            response.append("<pid id='").append(0).append("'/>");
//            response.append("<vars />");
//            response.append("<uLs r='").append(room.getId()).append("'>");
//
//            for (User user : room.getAllUsers()) {
//                response.append("<u i='").append(user.getUserId()).append("' ");
//                response.append("m='").append(user.isModerator() ? 1 : 0).append("'>");
//                response.append("<n><![CDATA[").append(user.getName()).append("]]></n>");
//                response.append("<vars></vars></u>");
//            }
//
//            response.append("</uLs></body></msg>");
//            sfs().sendResponse(response.toString(), u.getNetwork().getChannel());
//            String data = "<msg t='sys'><body action='uER' r='"
//                    + room.getId()
//                    + "'><u i ='"
//                    + u.getUserId()
//                    + "' m='0'><n><![CDATA["
//                    + u.getName()
//                    + "]]></n><vars></vars></u></body></msg>";
//            sfs().sendResponseRemoveUser(data, u, room.getChannellList());
//        }
    }

    public static RoomManager roomManager() {
        return _instance;
    }

}
