//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jdbchelper.BeanCreator;
import jdbchelper.ResultSetMapper;
import net.sf.json.JSONObject;


public class Item {

    public static final String EQUIPMENT_CLASS = "ar";
    public static final String EQUIPMENT_ARMOR = "co";
    public static final String EQUIPMENT_PET = "pe";
    public static final String EQUIPMENT_HELM = "he";
    public static final String EQUIPMENT_CAPE = "ba";
    public static final String EQUIPMENT_WEAPON = "Weapon";
    public static final String EQUIPMENT_AMULET = "am";
    public static final String EQUIPMENT_HOUSE = "ho";
    public static final String EQUIPMENT_HOUSE_ITEM = "hi";
    private int ClassID;
    private String name, description, type, element, file, link, icon, equipment, reqQuests, meta;
    private int id, level, DPS, range, cost, quantity, rarity, stack, enhId, factionId, reqReputation, reqClassId, reqClassPoints, questStringIndex, questStringValue;
    private boolean coins, upgrade, staff, temporary, sell, trade, market;
    public Class classObj;

    public TypeRarity objRarity;
    public Set<Integer> skills;
    public Map<Integer, Integer> requirements;

    public static final BeanCreator<Set<Integer>> beanSkills = rs -> {
        Set<Integer> set = new HashSet<>();
        do {
            set.add(rs.getInt("id"));
        } while (rs.next());

        return set;
    };

    public static final ResultSetMapper<Integer, Integer> requirementMapper = new ResultSetMapper<Integer, Integer>() {
        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("ReqItemID"), rs.getInt("Quantity"));
        }
    };

    public static final ResultSetMapper<Integer, Item> resultSetMapper = new ResultSetMapper<Integer, Item>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Item> mapRow(ResultSet rs) throws SQLException {
            Item item = new Item();

            item.id = rs.getInt("id");
            item.ClassID = rs.getInt("ClassID");
            item.name = rs.getString("Name");
            item.description = rs.getString("Description");
            item.type = rs.getString("Type");
            item.element = rs.getString("Element");
            item.file = rs.getString("File");
            item.link = rs.getString("Link");
            item.icon = rs.getString("Icon");
            item.equipment = rs.getString("Equipment");
            item.reqQuests = rs.getString("ReqQuests");
            item.meta = rs.getString("Meta");

            item.level = rs.getInt("Level");
            item.DPS = rs.getInt("DPS");
            item.range = rs.getInt("Range");
            item.rarity = rs.getInt("Rarity");
            item.cost = rs.getInt("Cost");
            item.quantity = rs.getInt("Quantity");
            item.stack = rs.getInt("Stack");
            item.enhId = rs.getInt("EnhID");
            item.factionId = rs.getInt("FactionID");
            item.reqReputation = rs.getInt("ReqReputation");
            item.reqClassId = rs.getInt("ReqClassID");
            item.reqClassPoints = rs.getInt("ReqClassPoints");
            item.questStringIndex = rs.getInt("QuestStringIndex");
            item.questStringValue = rs.getInt("QuestStringValue");

            item.coins = rs.getBoolean("Coins");
            item.upgrade = rs.getBoolean("Upgrade");
            item.staff = rs.getBoolean("Staff");
            item.temporary = rs.getBoolean("Temporary");
            item.sell = rs.getBoolean("Sell");
            item.trade = rs.getBoolean("Trade");
            item.market = rs.getBoolean("Market");

            return new AbstractMap.SimpleEntry<Integer, Item>(item.getId(), item);
        }
    };

    public static JSONObject getItemJSON(Item itemObj) {
        return Item.getItemJSON(itemObj, null);
    }

    public static JSONObject getItemJSON(Item itemObj, Enhancement enhancement) {
        if (itemObj == null) throw new NullPointerException("itemObj is null");

        JSONObject item = new JSONObject();

        item.put("ItemID", itemObj.getId());
        item.put("bCoins", itemObj.isCoins() ? 1 : 0);
        item.put("bHouse", itemObj.isHouse() ? 1 : 0);
        item.put("bPTR", 0);
        item.put("bStaff", itemObj.isStaff() ? 1 : 0);
        item.put("bTrade", itemObj.isTradeable() ? 1 : 0);
        item.put("bSell", itemObj.isSellable() ? 1 : 0);
        item.put("bMarket", itemObj.isMarketable() ? 1 : 0);
        item.put("bTemp", itemObj.isTemporary() ? 1 : 0);
        item.put("bUpg", itemObj.isUpgrade() ? 1 : 0);
        item.put("iCost", itemObj.getCost());
        item.put("iDPS", itemObj.getDPS());
        item.put("iLvl", itemObj.getLevel());
        item.put("iQSindex", itemObj.getQuestStringIndex());
        item.put("iQSvalue", itemObj.getQuestStringValue());
        item.put("iRng", itemObj.getRange());
        item.put("iRty", itemObj.getRarity());
        item.put("iStk", itemObj.getStack());
        item.put("sDesc", itemObj.getDescription());
        item.put("sES", itemObj.getEquipment());
        item.put("sElmt", itemObj.getElement());
        item.put("sFile", itemObj.getFile());
        item.put("sIcon", itemObj.getIcon());
        item.put("sLink", itemObj.getLink());
        item.put("sMeta", itemObj.getMeta());
        item.put("sName", itemObj.getName());
        item.put("sReqQuests", itemObj.getReqQuests());
        item.put("sType", itemObj.getType());

        if (enhancement != null) {
            if (itemObj.getType().equals("Enhancement")) {
                item.put("PatternID", enhancement.getPatternId());
                item.put("iDPS", enhancement.getDPS());
                item.put("iLvl", enhancement.getLevel());
                item.put("iRty", enhancement.getRarity());
                item.put("EnhID", 0);
                item.remove("sFile");
            } else {
                item.put("EnhID", enhancement.getId());
                item.put("EnhLvl", enhancement.getLevel());
                item.put("EnhPatternID", enhancement.getPatternId());
                item.put("EnhRty", enhancement.getRarity());
                item.put("iRng", itemObj.getRange());
                item.put("EnhRng", itemObj.getRange());
                item.put("InvEnhPatternID", enhancement.getPatternId());
                item.put("EnhDPS", enhancement.getDPS());
            }
        }
        return item;
    }

    public int getId() {
        return this.id;
    }

    public int getClassId() {
        return this.ClassID;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getType() {
        return this.type;
    }

    public String getElement() {
        return this.element;
    }

    public String getFile() {
        return this.file;
    }

    public String getLink() {
        return this.link;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getEquipment() {
        return this.equipment;
    }

    public int getLevel() {
        return this.level;
    }

    public int getDPS() {
        return this.DPS;
    }

    public int getRange() {
        return this.range;
    }

    public int getRarity() {
        return this.rarity;
    }

    public int getCost() {
        return this.cost;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public int getStack() {
        return this.stack;
    }

    public int getEnhId() {
        return this.enhId;
    }

    public int getFactionId() {
        return this.factionId;
    }

    public int getReqReputation() {
        return this.reqReputation;
    }

    public int getReqClassId() {
        return this.reqClassId;
    }

    public int getReqClassPoints() {
        return this.reqClassPoints;
    }

    public int getQuestStringIndex() {
        return this.questStringIndex;
    }

    public int getQuestStringValue() {
        return this.questStringValue;
    }

    public boolean isCoins() {
        return this.coins;
    }

    public boolean isGold() {
        return !coins;
    }

    public boolean isSellable() {
        return this.sell;
    }

    public boolean isTradeable() {
        return this.trade;
    }

    public boolean isMarketable() {
        return this.market;
    }

    public boolean isUpgrade() {
        return this.upgrade;
    }

    public boolean isHouse() {
        return this.getEquipment().equals(Item.EQUIPMENT_HOUSE) || this.getEquipment().equals(Item.EQUIPMENT_HOUSE_ITEM);
    }

    public boolean isStaff() {
        return this.staff;
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public String getReqQuests() {
        return this.reqQuests;
    }

    public String getMeta() {
        return this.meta;
    }

    public TypeRarity getObjRarity() {
        return objRarity;
    }
}
