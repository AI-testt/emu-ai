//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.db.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;

import com.google.common.collect.Multimap;
import jdbchelper.ResultSetMapper;

public class Quest {
	
    private int id, factionId, warId, reqReputation, reqClassId, reqClassPoints, experience, gold, coins, reputation, classPoints, level, slot, value, index, achievementId;
    private String name, description, endText, rewardType, field;
    private boolean upgrade, once, isWarMega;
	public Map<Integer, Integer> requirementitems;
    public Multimap<Integer, QuestReward> rewards;
	public Map<Integer, Integer> reqd;
    public Multimap<Integer, QuestRequirement> requirements;

    public static final ResultSetMapper<Integer, Integer> requiredItem = new ResultSetMapper<Integer, Integer>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Integer> mapRow(ResultSet rs) throws SQLException {
            return new AbstractMap.SimpleEntry<Integer, Integer>(rs.getInt("ItemID"), rs.getInt("Quantity"));
        }
    };
	
    public static final ResultSetMapper<Integer, Quest> resultSetMapper = new ResultSetMapper<Integer, Quest>() {

        @Override
        public AbstractMap.SimpleEntry<Integer, Quest> mapRow(ResultSet rs) throws SQLException {
            Quest quest = new Quest();
			
            quest.id = rs.getInt("id");
			quest.warId = rs.getInt("WarID");
            quest.factionId = rs.getInt("FactionID");
            quest.reqReputation = rs.getInt("ReqReputation");
            quest.reqClassId = rs.getInt("ReqClassID");
            quest.reqClassPoints = rs.getInt("ReqClassPoints");
            quest.experience = rs.getInt("Experience");
            quest.gold = rs.getInt("Gold");
            quest.coins = rs.getInt("Coins");
            quest.reputation = rs.getInt("Reputation");
            quest.classPoints = rs.getInt("ClassPoints");
            quest.level = rs.getInt("Level");
            quest.slot = rs.getInt("Slot");
            quest.value = rs.getInt("Value");
            quest.index = rs.getInt("Index");
			quest.achievementId = rs.getInt("AchievementID");

            quest.name = rs.getString("Name");
            quest.description = rs.getString("Description");
            quest.endText = rs.getString("EndText");
            quest.rewardType = rs.getString("RewardType");
            quest.field = rs.getString("Field");
			
            quest.once = rs.getBoolean("Once");
            quest.upgrade = rs.getBoolean("Upgrade");
			quest.isWarMega = rs.getBoolean("WarMega");
			
            return new AbstractMap.SimpleEntry<Integer, Quest>(quest.getId(), quest);
        }
    };

    public int getId() {
        return this.id;
    }

    public int getFactionId() {
        return this.factionId;
    }
	
    public int getWarId() {
        return this.warId;
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

    public int getExperience() {
        return this.experience;
    }

    public int getGold() {
        return this.gold;
    }

    public int getCoins() {
        return this.coins;
    }

    public int getReputation() {
        return this.reputation;
    }

    public int getClassPoints() {
        return this.classPoints;
    }

    public int getLevel() {
        return this.level;
    }
    public int getSlot() {
        return this.slot;
    }

    public int getValue() {
        return this.value;
    }

    public int getIndex() {
        return this.index;
    }

    public int getAchievementID() {
        return this.achievementId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getEndText() {
        return this.endText;
    }

    public String getRewardType() {
        return this.rewardType;
    }

    public String getField() {
        return this.field;
    }

    public boolean isUpgrade() {
        return this.upgrade;
    }

    public boolean isOnce() {
        return this.once;
    }
	
    public boolean isWarMega() {
        return this.isWarMega;
    }
}
