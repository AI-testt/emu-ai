package ains.world.avatars;

import ains.db.model.Aura;
import ains.tasks.RemoveAura;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ains.world.users.Users;
import net.sf.json.JSONObject;

public abstract class State {

    private int maxHealth, maxMana, maxStamina;
    private volatile int health, mana, stamina, state;
    private final Set<RemoveAura> auras;

    public State(int maxHealth, int maxMana) {
        this.health = maxHealth;
        this.mana = maxMana;
        this.maxHealth = maxHealth;
        this.maxMana = maxMana;
        this.maxStamina = 100;
        this.stamina = maxStamina;
        this.state = Users.STATE_NORMAL;
        this.auras = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public int getHealth() {
        return health;
    }
    public int getStamina() {
        return stamina;
    }

    public boolean isNeutral() {
        return this.state == Users.STATE_NORMAL;
    }

    public boolean isDead() {
        return this.state == Users.STATE_DEAD;
    }

    public boolean onCombat() {
        return this.state == Users.STATE_COMBAT;
    }

    public void setHealth(int health) {
        if (health <= 0) die();
        else this.health = Math.min(health, maxHealth);
    }


    public int getMana() {
        return mana;
    }

    public void decreaseHealth(int health) {
        setHealth(getHealth() - health);
    }

    public void increaseHealth(int health) {
        setHealth(getHealth() + health);
    }

    public void decreaseStamina(int stamina) {
        setStamina(getStamina() - stamina);
    }

    public void increaseStamina(int stamina) {
        setStamina(getStamina() + stamina);
    }

    public void decreaseMana(int mana) {
        setMana(getMana() - mana);
    }

    public void increaseMana(int mana) {
        setMana(getMana() + mana);
    }

    public void decreaseHealthByPercent(double percent) {
        int amount = (int) (maxHealth * (percent / 100.0D));
        setHealth(getHealth() - amount);
    }

    public void increaseHealthByPercent(double percent) {
        int amount = (int) (maxHealth * ((percent * 100) / 100.0D));
        setHealth(getHealth() + amount);
    }

    public void increaseStaminaByPercent(double percent) {
        int amount = (int) (maxStamina * ((percent * 100) / 100.0D));
        setStamina(getStamina() + amount);
    }


    public void decreaseManaByPercent(double percent) {
        int amount = (int) (maxMana * ((percent * 100) / 100.0D));
        setMana(getMana() - amount);
    }

    public void increaseManaByPercent(double percent) {
        int amount = (int) (maxMana * ((percent * 100) / 100.0D));
        setMana(getMana() + amount);
    }

    public void die() {
        this.health = 0;
        this.mana = 0;
        this.state = Users.STATE_DEAD;
    }

    public void restore() {
        this.health = maxHealth;
        this.mana = maxMana;
        this.stamina = maxStamina;
        this.state = Users.STATE_NORMAL;
    }

    public void setStamina(int stamina) {
        if (stamina < 0) this.stamina = 0;
        if (stamina > maxStamina) this.stamina = maxStamina;
        else this.stamina = stamina;
    }

    public void setMana(int mana) {
        if (mana < 0) this.mana = 0;
        else if (mana > maxMana) this.mana = maxMana;
        else this.mana = mana;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if (state < Users.STATE_DEAD || state > Users.STATE_COMBAT) throw new IllegalArgumentException("invalid state value");
        this.state = state;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public JSONObject getData() {
        JSONObject data = new JSONObject();

        data.put("intState", this.state);
        data.put("intHP", this.health);
        data.put("intMP", this.mana);
        data.put("intSP", this.stamina);

        return data;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public boolean isDisabled() {
        for (RemoveAura ra : this.auras) {
            Aura aura = ra.getAura();
            if (aura.getCategory().equals("stun") || aura.getCategory().equals("freeze") || aura.getCategory().equals("stone") || aura.getCategory().equals("disabled"))
                return true;
        }
        return false;
    }

    public void clearAuras() {
        for (RemoveAura ra : this.auras) {
            ra.run();
            ra.cancel();
        }
        this.auras.clear();
    }

    public boolean hasAura(int auraId) {
        for (RemoveAura ra : this.auras) {
            Aura aura = ra.getAura();
            if (aura.getId() == auraId)
                return true;
        }
        return false;
    }

    public RemoveAura getAura(int auraId) {
        for (RemoveAura ra : this.auras) {
            Aura aura = ra.getAura();
            if (aura.getId() == auraId)
                return ra;
        }

        return null;
    }

    public Set<RemoveAura> getAuras() {
        return Collections.unmodifiableSet(auras);
    }

    public void removeAura(RemoveAura ra) {
        this.auras.remove(ra);
    }

    public void addAura(RemoveAura ra) {
        if (!this.auras.contains(ra))
            this.auras.add(ra);
    }

    public abstract RemoveAura applyAura(Aura aura);
}
