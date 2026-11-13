package ains.db.model;

import jdbchelper.ResultSetMapper;

import java.util.AbstractMap;
import java.util.Map;

public class SettingMenu {

    private int id;
    private Boolean Special;
    private String Name;
    private String NameColor;
    private String Subtitle;
    private String SubtitleColor;
    private String Action;
    private String Value;

    public static final ResultSetMapper<Integer, SettingMenu> resultSetMapper = rs -> {
        SettingMenu menu = new SettingMenu();
        menu.id = rs.getInt("id");
        menu.Special = rs.getBoolean("Special");
        menu.Name = rs.getString("Name");
        menu.NameColor = rs.getString("NameColor");
        menu.Subtitle = rs.getString("Subtitle");
        menu.SubtitleColor = rs.getString("SubtitleColor");
        menu.Action = rs.getString("Action");
        menu.Value = rs.getString("Value");

        return new AbstractMap.SimpleEntry<>(Integer.valueOf(menu.id), menu);
    };

    public int getId() {
        return id;
    }

    public Boolean getSpecial() {
        return Special;
    }

    public String getName() {
        return Name;
    }

    public String getNameColor() {
        return NameColor;
    }

    public String getSubtitle() {
        return Subtitle;
    }

    public String getSubtitleColor() {
        return SubtitleColor;
    }

    public String getAction() {
        return Action;
    }

    public String getValue() {
        return Value;
    }
}
