package ains.world;

import ains.db.model.Item;
import ains.db.model.Quest;
import ains.db.model.QuestReward;
import ains.utils.RouletteWheel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quest reward type "roulette": exactly one item is drawn from the quest's roulette pool
 * with Roulette Wheel Selection. An entry's weight is its rarity tier weight multiplied by
 * the quests_rewards.Rate column, so Rate 1.00 keeps the tier weight and Rate 0 removes it.
 */
public final class QuestRoulette {

    private static final Logger log = LoggerFactory.getLogger(QuestRoulette.class);

    public static final String TYPE = "roulette";

    private QuestRoulette() {
    }

    public static boolean isRoulette(QuestReward reward) {
        return TYPE.equalsIgnoreCase(reward.type);
    }

    public static List<QuestReward> pool(Quest quest) {
        List<QuestReward> pool = new ArrayList<>();
        for (QuestReward reward : quest.rewards.values()) {
            if (isRoulette(reward)) pool.add(reward);
        }
        return pool;
    }

    public static RewardRarity rarityOf(Item item, World world) {
        String name = world.rarities == null ? null : world.rarities.get(item.getRarity());
        return RewardRarity.fromName(name);
    }

    public static double weight(QuestReward reward, World world) {
        Item item = world.items.get(reward.itemId);
        if (item == null) return 0;
        return rarityOf(item, world).weight(world.coreValues) * reward.rate;
    }

    /**
     * Chance of this reward being drawn from the given pool, as a percentage (0-100).
     */
    public static double chancePercent(QuestReward reward, List<QuestReward> pool, World world) {
        double total = RouletteWheel.totalWeight(pool, r -> weight(r, world));
        if (total <= 0) return 0;
        return Math.max(weight(reward, world), 0) / total * 100;
    }

    public static RouletteWheel.Spin<QuestReward> spin(List<QuestReward> pool, World world, Random rng) {
        return RouletteWheel.spin(pool, r -> weight(r, world), rng);
    }

    /**
     * Writes one spin to quests_rewards_logs. A failed insert is logged and swallowed so the
     * player still receives the reward.
     *
     * @param charId characters.id, or null for simulation runs
     * @param seed   RNG seed for simulation runs, or null for live spins
     * @param source "game" or "simulation"
     */
    public static void log(World world, Integer charId, int questId, RouletteWheel.Spin<QuestReward> spin, Long seed, String source) {
        QuestReward reward = spin.entry();
        Item item = world.items.get(reward.itemId);
        String rarity = item == null ? RewardRarity.COMMON.getLabel() : rarityOf(item, world).getLabel();

        try {
            world.db.getJdbc().run(
                    "INSERT INTO quests_rewards_logs (CharID, QuestID, QuestRewardID, ItemID, Quantity, Rarity, Weight, TotalWeight, RandomValue, Seed, Source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    charId, questId, reward.id, reward.itemId, reward.quantity, rarity,
                    weight(reward, world), spin.totalWeight(), spin.random(), seed, source);
        } catch (Exception e) {
            log.error("Failed to write quests_rewards_logs for quest {}: {}", questId, e.getMessage());
        }
    }
}
