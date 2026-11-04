package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Hide and seek: a friend's crouching empty hand asks her to play. Her friend counts to ten (with their eyes shut: blindness)
 * while she runs off to a spot out of their sight, up to 20 blocks away, and stays there. Find her within three minutes (get
 * within three blocks where she can see you) and she is thrilled, now and then with a present; run out of time and she pops
 * out, glowing, to say she won. Daughters love it most.
 */
public final class HideAndSeek {
    public static final int COUNT = 200, LIMIT = 3600, FOUND = 3, REWARD = 5;
    public static final String KEY = "HideAndSeek", ADVANCEMENT = "hide_and_seek";

    private HideAndSeek() {
    }

    public static boolean playing(AbstractMonsterGirl girl) {
        return girl.getPersistentData().contains(KEY);
    }

    @Nullable
    public static BlockPos spot(AbstractMonsterGirl girl) {
        CompoundTag t = girl.getPersistentData().getCompound(KEY);
        return t.contains("At") ? BlockPos.of(t.getLong("At")) : null;
    }

    private static boolean hiddenFrom(ServerLevel level, Player player, Vec3 at) {
        return level.clip(new ClipContext(player.getEyePosition(), at.add(0, 1.0, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK;
    }

    /** She picks a spot and runs. Whether the game began. */
    public static boolean start(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        if (playing(girl) || girl.getAffection(player) < Orders.AFFECTION || girl.isPassenger() || girl.getTarget() != null
                || girl.getOrders().command(girl) == Orders.Command.SIT) return false;
        Vec3 best = null;
        double bestDist = 0;
        for (int i = 0; i < 30; i++) {
            Vec3 v = LandRandomPos.getPos(girl, 20, 7);
            if (v == null || v.distanceToSqr(player.position()) < 8 * 8) continue;
            if (hiddenFrom(level, player, v)) {
                best = v;
                break;
            }
            double d = v.distanceToSqr(player.position());
            if (d > bestDist) {                                                   // nowhere out of sight: as far as she can
                bestDist = d;
                best = v;
            }
        }
        if (best == null) return false;
        CompoundTag t = new CompoundTag();
        t.putUUID("By", player.getUUID());
        t.putLong("At", BlockPos.containing(best).asLong());
        t.putLong("Since", level.getGameTime());
        girl.getPersistentData().put(KEY, t);
        girl.getNavigation().moveTo(best.x, best.y, best.z, 1.4);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, COUNT, 0, false, false));
        level.playSound(null, girl.blockPosition(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, SoundSource.NEUTRAL, 1.0F, 1.3F);
        player.displayClientMessage(Component.translatable("message.monstergirls.hide.start", girl.getDisplayName()), false);
        return true;
    }

    /** Twice a second while she plays: she keeps to her spot, is found, or wins. */
    static void tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!playing(girl)) return;
        CompoundTag t = girl.getPersistentData().getCompound(KEY);
        Player player = t.hasUUID("By") ? level.getPlayerByUUID(t.getUUID("By")) : null;
        long since = level.getGameTime() - t.getLong("Since");
        BlockPos at = spot(girl);
        if (player == null || !player.isAlive() || at == null || girl.getTarget() != null) {
            girl.getPersistentData().remove(KEY);
            return;
        }
        if (since >= COUNT && found(level, girl, player)) return;
        if (since >= LIMIT) {
            girl.getPersistentData().remove(KEY);
            girl.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
            level.playSound(null, girl.blockPosition(), SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 1.0F, 1.2F);
            player.displayClientMessage(Component.translatable("message.monstergirls.hide.won", girl.getDisplayName()), false);
            return;
        }
        if (girl.distanceToSqr(Vec3.atBottomCenterOf(at)) > 1.5 * 1.5 && (girl.getNavigation().isDone() || since % 40 == 0)) {
            girl.getNavigation().moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, since < COUNT ? 1.4 : 1.0);
        }
    }

    /** Whether her friend has found her (and if so, the reward). */
    public static boolean found(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        if (!playing(girl) || player.distanceToSqr(girl) > FOUND * FOUND || !girl.hasLineOfSight(player)) return false;
        girl.getPersistentData().remove(KEY);
        girl.addAffection(player, girl.isBaby() ? REWARD * 2 : REWARD);
        girl.hearts(level, 5);
        level.sendParticles(ParticleTypes.NOTE, girl.getX(), girl.getY() + girl.getBbHeight() + 0.3, girl.getZ(), 5, 0.4, 0.2, 0.4, 1.0);
        level.playSound(null, girl.blockPosition(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 1.0F, 1.4F);
        player.displayClientMessage(Component.translatable("message.monstergirls.hide.found", girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
        if (girl.getRandom().nextInt(3) == 0) {
            ItemStack gift = girl.rollGift(player);
            if (!gift.isEmpty() && !player.addItem(gift)) player.drop(gift, false);
        }
        if (player instanceof ServerPlayer sp) {
            AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
        return true;
    }
}
