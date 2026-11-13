/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package ains.world.avatars;

import ains.db.model.Aura;
import ains.tasks.RemoveAura;

import java.util.Random;
import java.util.Set;
import net.sf.json.JSONObject;

/**
 *
 * @author Mystical
 */
public class Damage {

    private static Random rand = new Random();

    private Damage() {
        throw new UnsupportedOperationException("not allowed to have an instance of this class");
    }

    public static JSONObject getDamageResult(String fromTgt, String tgt, int damage, DamageType type) {
        JSONObject damageResult = new JSONObject();
        damageResult.put("hp", damage);
        damageResult.put("cInf", fromTgt);
        damageResult.put("tInf", tgt);

        if (type.equals(DamageType.DAMAGE_OVER_TIME))
            damageResult.put("typ", type.toString());
        else
            damageResult.put("type", type.toString());

        return damageResult;
    }

    public static JSONObject getDamageResult(String tgt, int damage, DamageType type) {
        JSONObject damageResult = new JSONObject();
        damageResult.put("hp", damage);
        damageResult.put("tInf", tgt);

        if (type.equals(DamageType.DAMAGE_OVER_TIME))
            damageResult.put("typ", type.toString());
        else
            damageResult.put("type", type.toString());

        return damageResult;
    }

    public static int getRandomDamage(DamageType type, int maxDmg, int minDmg, int weaponDmg) {
        return getRandomDamage(type, maxDmg, minDmg, weaponDmg, 1.5);
    }

    public static int getRandomDamage(DamageType type, int maxDmg, int minDmg, int weaponDmg, double critMultiplier) {
        int damage = (rand.nextInt(maxDmg - minDmg) + minDmg);
        if (weaponDmg != 0)
            damage +=  (weaponDmg / 2);

        if (type.equals(DamageType.CRITICAL)) damage = (int) (damage * critMultiplier);
        if (type.equals(DamageType.DODGE) || type.equals(DamageType.MISS) || type.equals(DamageType.NONE)) damage = 0;

        return damage;
    }

    public static DamageType evaluateDamageType(double hitChance, double dodgeChance, double critChance) {
        if (Math.random() < critChance) return DamageType.CRITICAL;
        if (Math.random() < dodgeChance) return DamageType.DODGE;
        if (Math.random() > (hitChance / (hitChance + dodgeChance))) return DamageType.MISS;
        return DamageType.HIT;
    }

    public static int evaluateDamageFromStats(int Dmg, int Strength, int Endurance, int Dexterity, int Intellect, int Wisdom, int Luck, String Type) {
        if (Type.equals("m"))
            if(Dmg > 0)
                return Dmg += (Intellect * 2.3);
            else
                return Dmg -= (Intellect * 2.3);
        else
            if(Dmg > 0)
                return Dmg += (Strength * 2);
            else
                return Dmg -= (Strength * 2);
    }

    public static int evaluateAuras(int damage, Set<RemoveAura> auras) {
        if(damage > 0)
            return getReducedDamage(getIncreasedDamage(damage, auras), auras);
        else
            return damage;
    }

    public static int getIncreasedDamage(int damage, Set<RemoveAura> auras) {
        for (RemoveAura ra : auras) {
            Aura aura = ra.getAura();
            if (!aura.getCategory().equals("d")) {
                damage = (int) ((double) damage * (1.0D + aura.getDamageIncrease() * ra.getStack()));
                damage = Math.max(damage, 0);
            }
        }

        return damage;
    }

    public static int getReducedDamage(int damage, Set<RemoveAura> auras) {
        for (RemoveAura ra : auras) {
            Aura aura = ra.getAura();
            if (!aura.getCategory().equals("d")) {
                damage = (int) ((double) damage * (1.0D - aura.getDamageTakenDecrease() * ra.getStack()));
                damage = Math.max(damage, 0);
            }
        }
        return damage;
    }

    /**
     * Applies outgoing category boosts to a raw attack result.
     * Positive damage (damage dealt): boosted by $cao (general) times the type-specific
     * multiplier ($cpo physical / $cmo magical / $cdo damage-over-time).
     * Negative damage (healing cast): boosted by $cho (healing boost out).
     */
    public static int applyOutgoingBoost(int damage, boolean isMagical, boolean isDot, double generalBoost, double physicalBoost, double magicalBoost, double dotBoost, double healingBoost) {
        if (damage > 0) {
            double boost = generalBoost * (isDot ? dotBoost : (isMagical ? magicalBoost : physicalBoost));
            if (boost > 0)
                damage = (int) (damage * boost);
        } else if (damage < 0 && healingBoost > 0) {
            damage = (int) (damage * healingBoost);
        }
        return damage;
    }

    /**
     * Applies incoming category resistance to a raw attack result landing on a target.
     * Positive damage (damage taken): resisted by $cai (general) times the type-specific
     * multiplier ($cpi physical / $cmi magical / $cdi damage-over-time).
     * Negative damage (healing received): boosted by $chi (healing intake).
     */
    public static int applyIncomingResist(int damage, boolean isMagical, boolean isDot, double generalResist, double physicalResist, double magicalResist, double dotResist, double healingIntake) {
        if (damage > 0) {
            double resist = generalResist * (isDot ? dotResist : (isMagical ? magicalResist : physicalResist));
            if (resist > 0)
                damage = (int) (damage * resist);
            damage = Math.max(damage, 0);
        } else if (damage < 0 && healingIntake > 0) {
            damage = (int) (damage * healingIntake);
        }
        return damage;
    }
}
