package ains.db.model;

public class SkillAura {

    public int auraId;
    public double chance;
    public boolean self;

    /** "apply" (default) menerapkan aura ke target/caster; "consume" membaca
     *  stack aura yang sudah ada di caster untuk menskalakan damage lalu
     *  menghapusnya (lihat Action.applyConsumeStacks). */
    public String mode = "apply";

    public SkillAura() {
        super();
    }
}
