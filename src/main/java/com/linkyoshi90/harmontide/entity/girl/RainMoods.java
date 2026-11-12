package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.entity.Clade;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Rain moods, by branch of the tree of life (entity.Clade): cats hate getting wet, so a feliform out in the rain with nowhere
 * she has to be looks for a roof nearby and waits it out there. Amphibians love it: out in the rain they brighten up, and
 * a friend nearby who stands in it with them is a little closer for it (once a shower). Part of the comfort setting.
 */
public final class RainMoods {
    /** How far a cat looks for shelter, and how often an amphibian's good mood rubs off on a friend (once per rain, by game time). */
    public static final int SHELTER_RANGE = 10;
    public static final String SHARED_RAIN = "SharedRainAt";
    public static final long RAIN_GAP = 6000;

    private RainMoods() {
    }

    public static boolean hatesRain(AbstractMonsterGirl girl) {
        return girl.getSpecies().clade() == Clade.FELIFORMIA && !girl.needsWater();
    }

    public static boolean lovesRain(AbstractMonsterGirl girl) {
        return girl.getSpecies().clade() == Clade.AMPHIBIA;
    }

    /** Rain falling on her right now. */
    public static boolean wet(AbstractMonsterGirl girl) {
        return girl.level().isRainingAt(girl.blockPosition()) || girl.level().isRainingAt(BlockPos.containing(girl.getX(), girl.getEyeY(), girl.getZ()));
    }

    /** Free to go where she likes: wild, or a friend left to roam; not sitting, fighting, riding or sleeping. */
    static boolean free(AbstractMonsterGirl girl) {
        Orders.Command c = girl.getOrders().command(girl);
        return (c == null || c == Orders.Command.FREE_ROAM) && girl.getTarget() == null && !girl.isPassenger() && !girl.isSleeping()
                && !girl.isInWater() && !girl.isOrderedToSit();
    }

    /** Every five seconds: an amphibian out in the rain brightens up, and shares it with a friend standing in the rain nearby. The friend, or null. */
    @Nullable
    public static Player tick(ServerLevel level, AbstractMonsterGirl girl) {
        return lovesRain(girl) && wet(girl) ? splash(level, girl, false) : null;
    }

    /** An amphibian in the rain (the friend counts as out in it when rainOnFriend, else when rain falls where they stand). The friend she shared it with, or null. */
    @Nullable
    public static Player splash(ServerLevel level, AbstractMonsterGirl girl, boolean rainOnFriend) {
        if (!HarmontideConfig.comfort() || !lovesRain(girl)) return null;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 2, 0.3, 0.2, 0.3, 0);
        if (girl.getRandom().nextInt(3) == 0) level.playSound(null, girl, SoundEvents.FROG_AMBIENT, SoundSource.NEUTRAL, 0.8F, 1.2F);
        if (girl.isBaby() || level.getGameTime() < girl.getPersistentData().getLong(SHARED_RAIN)) return null;
        Player friend = girl.bondedPlayer(Orders.AFFECTION, 8.0);
        if (friend == null || !rainOnFriend && !level.isRainingAt(friend.blockPosition())) return null;
        girl.addAffection(friend, 2);
        girl.getPersistentData().putLong(SHARED_RAIN, level.getGameTime() + RAIN_GAP);
        if (friend instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.rain_lover", girl.getDisplayName())
                    .withStyle(net.minecraft.ChatFormatting.AQUA), true);
        }
        return friend;
    }

    /** A dry spot under a roof near her that she can stand on, or null (one of a dozen tries). */
    @Nullable
    public static BlockPos shelter(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos at = girl.blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos p = at.offset(girl.getRandom().nextInt(SHELTER_RANGE * 2 + 1) - SHELTER_RANGE, girl.getRandom().nextInt(5) - 2,
                    girl.getRandom().nextInt(SHELTER_RANGE * 2 + 1) - SHELTER_RANGE);
            if (!level.canSeeSky(p) && !level.isRainingAt(p) && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                    && level.getBlockState(p.below()).isSolidRender(level, p.below()) && level.getFluidState(p).isEmpty()) return p;
        }
        return null;
    }

    /** A cat in the rain heads for a roof. */
    public static class ShelterGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable
        private BlockPos target;
        private int cooldown, ticks;

        public ShelterGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0) return false;
            this.cooldown = 40;
            if (!HarmontideConfig.comfort() || !(this.girl.level() instanceof ServerLevel level) || !hatesRain(this.girl) || !wet(this.girl) || !free(this.girl)) return false;
            this.target = shelter(level, this.girl);
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.ticks < 200 && free(this.girl) && !this.girl.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.ticks = 0;
            if (this.target != null) this.girl.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.2);
        }

        @Override
        public void tick() {
            this.ticks++;
        }

        @Override
        public void stop() {
            this.target = null;
            this.girl.getNavigation().stop();
        }

        @Nullable
        public BlockPos target() {
            return this.target;
        }

        /** Where she is headed, for the tests. */
        public Vec3 heading() {
            return this.target == null ? this.girl.position() : Vec3.atBottomCenterOf(this.target);
        }
    }
}
