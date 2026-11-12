package com.linkyoshi90.harmontide.entity;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The state of a monster girl who spent the night beside a player she trusts: how long until she is ready to lay
 * (one in-game day), a cool-down afterwards, and whose bed she is sharing tonight. Harpies lay a fertilised egg;
 * frogs and toads lay spawn in still water.
 */
public final class Gestation {
    public static final int DURATION = 24000;
    public static final int COOLDOWN = 24000;

    private int ticksLeft = -1;
    private int cooldown;
    @Nullable
    private UUID sharingWith;
    /** The player whose bed she shared: her young are born devoted to that player. */
    @Nullable
    private UUID father;

    public boolean isCarrying() {
        return this.ticksLeft >= 0;
    }

    /** Carrying, and the day is up. */
    public boolean isReady() {
        return this.ticksLeft == 0;
    }

    public boolean canConceive() {
        return this.ticksLeft < 0 && this.cooldown <= 0;
    }

    public void conceive() {
        this.conceive(null);
    }

    public void conceive(@Nullable UUID father) {
        this.ticksLeft = DURATION;
        this.father = father;
    }

    @Nullable
    public UUID getFather() {
        return this.father;
    }

    /** How far along she is, 0 (not expecting) to 1 (ready to lay). Drives how round her belly is. */
    public float progress() {
        return this.ticksLeft < 0 ? 0.0F : 1.0F - (float) this.ticksLeft / DURATION;
    }

    /** After laying: empty again, and not again for a day. */
    public void clear() {
        this.ticksLeft = -1;
        this.father = null;
        this.cooldown = COOLDOWN;
    }

    /** Ready again sooner (a girl changed by the realm): half the cool-down left. */
    public void halveCooldown() {
        this.cooldown /= 2;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public int getTicksLeft() {
        return this.ticksLeft;
    }

    public void setTicksLeft(int ticks) {
        this.ticksLeft = ticks;
    }

    public void tick() {
        if (this.ticksLeft > 0) this.ticksLeft--;
        if (this.cooldown > 0) this.cooldown--;
    }

    @Nullable
    public UUID getSharingWith() {
        return this.sharingWith;
    }

    public void setSharingWith(@Nullable UUID player) {
        this.sharingWith = player;
    }

    public void save(CompoundTag tag) {
        CompoundTag t = new CompoundTag();
        t.putInt("Ticks", this.ticksLeft);
        t.putInt("Cooldown", this.cooldown);
        if (this.father != null) t.putUUID("Father", this.father);
        tag.put("Gestation", t);
    }

    public void load(CompoundTag tag) {
        CompoundTag t = tag.getCompound("Gestation");
        this.ticksLeft = t.contains("Ticks") ? t.getInt("Ticks") : -1;
        this.cooldown = t.getInt("Cooldown");
        this.father = t.hasUUID("Father") ? t.getUUID("Father") : null;
        this.sharingWith = null;
    }
}
