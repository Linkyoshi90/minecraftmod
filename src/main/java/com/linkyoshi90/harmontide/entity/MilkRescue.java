package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.block.MilkingMachineBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.item.FamilyMilkItem;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * A friend with milk to spare (Lactation.isLactating) sees a friend poisoned or withering, down to DANGER health, somewhere sheltered
 * (a roof overhead): she hurries over and offers a cup of her milk. The player agrees by right-clicking her with an empty hand within
 * OFFER_TICKS; it clears the poison and the wither, gives her family's effects and her diet's, and a moment of Regeneration.
 */
public final class MilkRescue {
    public static final float DANGER = 6.0F;
    public static final int RANGE = 16, OFFER_TICKS = 600, AFFECTION = 3;
    private static final String FOR = "RescueFor", UNTIL = "RescueUntil";

    private MilkRescue() {}

    public static boolean inDanger(Player player) {
        return !player.isCreative() && !player.isSpectator() && player.isAlive() && player.getHealth() <= DANGER
                && (player.hasEffect(MobEffects.POISON) || player.hasEffect(MobEffects.WITHER));
    }

    public static boolean sheltered(ServerLevel level, Player player) {
        return level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, player.getBlockX(), player.getBlockZ()) > player.getBlockY() + 1;   // a roof (or leaves) overhead
    }

    /** Twice a second: whether to offer, and coming over while the offer stands. */
    public static void tick(ServerLevel level, LivingEntity creature) {
        if (creature.tickCount % 10 != 0 || !(creature instanceof Mob mob) || !(creature instanceof Bonded bonded) || creature.isBaby()) return;
        CompoundTag data = creature.getPersistentData();
        long now = level.getGameTime();
        if (data.hasUUID(FOR)) {
            Player p = level.getPlayerByUUID(data.getUUID(FOR));
            if (p == null || now > data.getLong(UNTIL) || !inDanger(p) || !Lactation.isLactating(creature)) {
                clear(data);
                return;
            }
            if (mob.distanceToSqr(p) > 2.0 * 2.0) mob.getNavigation().moveTo(p, 1.3);
            else mob.getLookControl().setLookAt(p);
            return;
        }
        if (creature.tickCount % 40 != 0 || !Lactation.isLactating(creature)) return;
        for (Player p : level.getEntitiesOfClass(Player.class, creature.getBoundingBox().inflate(RANGE),
                p -> bonded.getAffection(p) >= HomeLife.FRIEND && inDanger(p) && sheltered(level, p))) {
            offer(level, mob, p);
            return;
        }
    }

    /** She offers it: the player has OFFER_TICKS to accept. */
    public static void offer(ServerLevel level, Mob mob, Player player) {
        CompoundTag data = mob.getPersistentData();
        data.putUUID(FOR, player.getUUID());
        data.putLong(UNTIL, level.getGameTime() + OFFER_TICKS);
        player.displayClientMessage(Component.translatable("message.harmontide.rescue_offer", mob.getDisplayName()), false);
        mob.getNavigation().moveTo(player, 1.3);
    }

    public static boolean offered(LivingEntity creature, Player player) {
        CompoundTag data = creature.getPersistentData();
        return data.hasUUID(FOR) && data.getUUID(FOR).equals(player.getUUID()) && creature.level().getGameTime() <= data.getLong(UNTIL);
    }

    /** The player took her up on it (an empty hand on her). Whether there was an offer to take. */
    public static boolean accept(ServerLevel level, LivingEntity creature, Player player) {
        if (!offered(creature, player)) return false;
        clear(creature.getPersistentData());
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WITHER);
        FamilyMilk family = FamilyMilk.of(creature);
        if (family != null) for (Holder<MobEffect> e : family.effects()) player.addEffect(new MobEffectInstance(e, FamilyMilkItem.EFFECT_TICKS));
        for (Holder<MobEffect> e : Dairy.diet(creature)) player.addEffect(new MobEffectInstance(e, FamilyMilkItem.EFFECT_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        Lactation.relieve(level, creature, null);
        MilkingMachineBlockEntity.markMilked(creature, level.getGameTime() + Minotaur.MILK_COOLDOWN);
        if (creature instanceof Bonded b) b.addAffection(player, AFFECTION);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HEART, creature.getX(), creature.getEyeY() + 0.3, creature.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.rescue_drink", creature.getDisplayName()), false);
        return true;
    }

    private static void clear(CompoundTag data) {
        data.remove(FOR);
        data.remove(UNTIL);
    }
}
