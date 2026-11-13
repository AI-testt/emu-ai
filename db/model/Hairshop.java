//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.AbstractMap;

import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;

public class Hairshop {

    private int id;
    public Set<Integer> male;
    public Set<Integer> female;

    public static final BeanCreator<Set<Integer>> beanHairshopItems = new BeanCreator<Set<Integer>>() {
        @Override
        public Set<Integer> createBean(ResultSet rs) throws SQLException {
            Set<Integer> set = new HashSet();
            set.add(rs.getInt("HairID"));
            while (rs.next()) {
                set.add(rs.getInt("HairID"));
            }
            return set;
        }
    };

    public static final ResultSetMapper<Integer, Hairshop> resultSetMapper = new ResultSetMapper<Integer, Hairshop>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Hairshop> mapRow(ResultSet rs) throws SQLException {
            Hairshop hairshop = new Hairshop();

            hairshop.id = rs.getInt("id");

            return new AbstractMap.SimpleEntry<Integer, Hairshop>(hairshop.getId(), hairshop);
        }
    };

    public Set<Integer> getShopItems(String gender) {
        if (gender.equals("M"))
            return Collections.unmodifiableSet(this.male);
        else
            return Collections.unmodifiableSet(this.female);
    }

    public int getId() {
        return this.id;
    }
}
