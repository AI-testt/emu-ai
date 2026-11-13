package ains.db.model;

import jdbchelper.BeanCreator;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TypeRarity {

    private int id;
    private String Name;
    private String Color;

    public static final BeanCreator<TypeRarity> beanCreator = rs -> {
        TypeRarity typeRarity = new TypeRarity();
        typeRarity.id = rs.getInt("id");
        typeRarity.Name = rs.getString("Name");
        typeRarity.Color = rs.getString("Color");
        return typeRarity;
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return Name;
    }

    public String getColor() {
        return Color;
    }
}
