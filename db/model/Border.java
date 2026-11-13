package ains.db.model;

import jdbchelper.ResultSetMapper;
import net.sf.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

public class Border {

    private int id;
    private String Name, File, Link;

    public static final ResultSetMapper<Integer, Border> resultSetMapper = new ResultSetMapper<Integer, Border>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Border> mapRow(ResultSet rs) throws SQLException {
            Border border = new Border();

            border.id = rs.getInt("id");
            border.Name = rs.getString("Name");
            border.File = rs.getString("File");
            border.Link = rs.getString("Link");

            return new AbstractMap.SimpleEntry<Integer, Border>(border.getId(), border);
        }
    };

    public JSONObject toJSON() {
        JSONObject border = new JSONObject();
        border.put("id", id);
        border.put("Name", Name);
        border.put("File", File);
        border.put("Link", Link);
        return border;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return Name;
    }

    public String getFile() {
        return File;
    }

    public String getLink() {
        return Link;
    }
}
