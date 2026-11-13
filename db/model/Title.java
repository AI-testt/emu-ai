package ains.db.model;

import jdbchelper.ResultSetMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;

public class Title {

    private int id, Strength, Intellect, Endurance, Dexterity, Wisdom, Luck;
    private int Rarity;
    private String Name, Color, Description;

    public static final ResultSetMapper<Integer, Title> resultSetMapper = new ResultSetMapper<Integer, Title>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Title> mapRow(ResultSet rs) throws SQLException {
            Title title = new Title();

            title.id = rs.getInt("id");

            title.Name = rs.getString("Name");
            title.Color = rs.getString("Color");
            title.Description = rs.getString("Description");
            title.Rarity = rs.getInt("Rarity");

            title.Strength = rs.getInt("Strength");
            title.Intellect = rs.getInt("Intellect");
            title.Endurance = rs.getInt("Endurance");
            title.Dexterity = rs.getInt("Dexterity");
            title.Wisdom = rs.getInt("Wisdom");
            title.Luck = rs.getInt("Luck");

            return new AbstractMap.SimpleEntry<Integer, Title>(title.getId(), title);
        }
    };

    public int getId() {
        return id;
    }

    public int getStrength() {
        return Strength;
    }

    public int getIntellect() {
        return Intellect;
    }

    public int getEndurance() {
        return Endurance;
    }

    public int getDexterity() {
        return Dexterity;
    }

    public int getWisdom() {
        return Wisdom;
    }

    public int getLuck() {
        return Luck;
    }

    public String getName() {
        return Name;
    }

    public String getColor() {
        return Color;
    }

    public String getDescription() {
        return Description;
    }

    public int getRarity() {
        return Rarity;
    }
}