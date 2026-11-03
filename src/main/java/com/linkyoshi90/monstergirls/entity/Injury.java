package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * The injured encounter for harpies and frogs (girls have their own, AbstractMonsterGirl.setInjured): one wild one in 50 is found hurt, at
 * 30% health, half her speed on the ground and in the air, unwilling to fight. A healing balm mends her (GearEvents.onBalm): 40 friendship.
 * The state lives in getPersistentData, and the slowness is put back on when she loads.
 */
public final class Injury {
    public static final int RARITY = 50, MEND = 40;
    private static final String KEY = "Injured";
    private static final ResourceLocation SLOW = MonsterGirlsMod.id("injured");

    private Injury() {}

    public static boolean isInjured(Mob mob) {
        return mob.getPersistentData().getBoolean(KEY);
    }

    /** A wild spawn: one in RARITY grown ones is found hurt. */
    public static void roll(Mob mob, MobSpawnType spawnType) {
        if ((spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) && !mob.isBaby() && mob.getRandom().nextInt(RARITY) == 0) {
            set(mob, true);
            mob.setHealth(mob.getMaxHealth() * 0.3F);
        }
    }

    public static void set(Mob mob, boolean injured) {
        mob.getPersistentData().putBoolean(KEY, injured);
        reapply(mob);
        if (injured) mob.setTarget(null);
    }

    /** The slowness, which isn't saved with her. */
    public static void reapply(Mob mob) {
        boolean injured = isInjured(mob);
        for (var attribute : java.util.List.of(Attributes.MOVEMENT_SPEED, Attributes.FLYING_SPEED)) {
            AttributeInstance a = mob.getAttribute(attribute);
            if (a == null) continue;
            a.removeModifier(SLOW);
            if (injured) a.addTransientModifier(new AttributeModifier(SLOW, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Every few seconds a hurt one shows it. */
    public static void tick(ServerLevel level, Mob mob) {
        if (mob.tickCount % 60 == 0 && isInjured(mob)) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
        }
    }

    /** The balm on her wound: whole again, and she remembers who helped. */
    public static void mend(ServerLevel level, Mob mob, Player player) {
        set(mob, false);
        if (mob instanceof Bonded b) b.addAffection(player, MEND);
        mob.setLastHurtByMob(null);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 12, 0.5, 0.4, 0.5, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.mended", mob.getDisplayName()), true);
    }
}
