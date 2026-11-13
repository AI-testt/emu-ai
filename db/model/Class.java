package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

/**
 *
 * @author Mystical
 */
public class Class {

    private int id;
    private String Name;
    private String category, description, manaRegenerationMethods, statsDescription;

    public Multimap<Integer, ClassSkill> skills;

    public static final BeanCreator<Set<Integer>> beanSkills = new BeanCreator<Set<Integer>>() {

        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();

            set.add(rs.getInt("SkillID"));

            while (rs.next())
                set.add(rs.getInt("SkillID"));

            return set;
        }
    };

    public static final BeanCreator<Class> beanCreator = rs -> {
        Class oClass = new Class();
        oClass.id = rs.getInt("id");
        oClass.Name = rs.getString("Name");
        oClass.category = rs.getString("Category");
        oClass.description = rs.getString("Description");
        oClass.manaRegenerationMethods = rs.getString("ManaRegenerationMethods");
        oClass.statsDescription = rs.getString("StatsDescription");

        return oClass;
    };

    public static final ResultSetMapper<Integer, Class> resultSetMapper = rs -> {
        Class oClass = new Class();
        oClass.id = rs.getInt("id");
        oClass.Name = rs.getString("Name");
        oClass.category = rs.getString("Category");
        oClass.description = rs.getString("Description");
        oClass.manaRegenerationMethods = rs.getString("ManaRegenerationMethods");
        oClass.statsDescription = rs.getString("StatsDescription");

        return new AbstractMap.SimpleEntry<>(oClass.id, oClass);
    };

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.Name;
    }

    public String getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public String getManaRegenerationMethods() {
        return this.manaRegenerationMethods;
    }

    public String getStatsDescription() {
        return this.statsDescription;
    }

}
