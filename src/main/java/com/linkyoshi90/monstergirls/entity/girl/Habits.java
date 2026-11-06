package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Habits of a branch of the tree of life (entity.Clade), part of the comfort setting:
 * <ul>
 * <li>Canids howl at the full moon, once a night, and every canid in earshot answers; a friend out under the moon with one
 * of them is a little closer for it.</li>
 * <li>Primates groom: a friend beside you picks out one harmful effect (poison, hunger, slowness and the like), every two minutes.</li>
 * <li>Elephants and their kin (on land) spray a burning friend out with their trunks, every half minute.</li>
 * <li>Crabs, lobsters and shrimp comb the beach: a friend standing on sand by the water gets what she digs up at their feet, every
 * couple of minutes (kelp, clay, bones and flint, now and then a shard, a sea pickle or ink, rarely gold or a nautilus shell).</li>
 * <li>Fairies, pixies and the leshy leave a fairy ring: on a night out on the grass with a friend near, a ring of mushrooms comes up
 * around her, every few days. Anyone standing in a ring at night (theirs, or one a player planted) is lucky till morning.</li>
 * </ul>
 */
public final class Habits {
    public static final String HOWLED = "HowledNight", GROOM_AT = "GroomAt";
    public static final double EARSHOT = 48.0, MOON_FRIEND = 16.0, GROOM_REACH = 3.0;
    public static final int GROOM_GAP = 2400;

    private Habits() {
    }

    // --- the full-moon howl -----------------------------------------------------------------------------------------

    public static boolean fullMoonNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return level.getMoonPhase() == 0 && time > 13000 && time < 23000 && !level.isRaining();
    }

    private static long night(ServerLevel level) {
        return (level.getDayTime() + 12000L) / 24000L;                 // one number for the whole night, either side of midnight
    }

    /** Every ten seconds for a grown canid: on a full-moon night under open sky she may lead a howl. How many joined in (her included), 0 if she didn't. */
    public static int tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.CANIDAE || !fullMoonNight(level)
                || !level.canSeeSky(girl.blockPosition().above()) || girl.getTarget() != null || girl.getRandom().nextInt(4) != 0) return 0;
        return howl(level, girl);
    }

    /** She howls at the moon, and every canid in earshot who hasn't tonight answers. How many howled. */
    public static int howl(ServerLevel level, AbstractMonsterGirl lead) {
        long night = night(level);
        if (lead.getPersistentData().getLong(HOWLED) == night + 1) return 0;
        int n = 0;
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, lead.getBoundingBox().inflate(EARSHOT),
                g -> g.getSpecies().clade() == Clade.CANIDAE && !g.isBaby() && g.getPersistentData().getLong(HOWLED) != night + 1)) {
            g.getPersistentData().putLong(HOWLED, night + 1);
            if (g.getSpecies().family() == GirlSpecies.Family.CANID) g.startTimedAction(AbstractMonsterGirl.Action.HOWL, 40);
            g.playSound(SoundEvents.WOLF_HOWL, 2.0F, 0.9F + g.getRandom().nextFloat() * 0.3F);
            n++;
            Player friend = g.bondedPlayer(Orders.AFFECTION, MOON_FRIEND);
            if (friend != null && level.canSeeSky(friend.blockPosition().above())) {
                g.addAffection(friend, 2);
                if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.moon_howl", g.getDisplayName())
                        .withStyle(net.minecraft.ChatFormatting.DARK_AQUA), true);
            }
        }
        return n;
    }

    // --- grooming ---------------------------------------------------------------------------------------------------

    /** Every five seconds for a primate: a friend right beside her with something harmful on them gets one effect picked out. The effect, or null. */
    @Nullable
    public static Holder<MobEffect> groom(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.PRIMATES
                || level.getGameTime() < girl.getPersistentData().getLong(GROOM_AT)) return null;
        Player friend = girl.bondedPlayer(Orders.AFFECTION, GROOM_REACH);
        if (friend == null) return null;
        for (MobEffectInstance e : friend.getActiveEffects()) {
            if (e.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
            Holder<MobEffect> effect = e.getEffect();
            friend.removeEffect(effect);
            girl.getPersistentData().putLong(GROOM_AT, level.getGameTime() + GROOM_GAP);
            girl.getLookControl().setLookAt(friend);
            girl.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            girl.addAffection(friend, 1);
            level.sendParticles(ParticleTypes.HEART, friend.getX(), friend.getEyeY() + 0.4, friend.getZ(), 1, 0.2, 0.1, 0.2, 0);
            if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.groomed", girl.getDisplayName(),
                    effect.value().getDisplayName()).withStyle(net.minecraft.ChatFormatting.GREEN), true);
            return effect;
        }
        return null;
    }

    // --- the trunk's water ------------------------------------------------------------------------------------------

    public static final String DOUSE_AT = "DouseAt";
    public static final double DOUSE_REACH = 6.0;
    public static final int DOUSE_GAP = 600;

    /** Every second for a grown elephant, hyrax or tusked kin who walks on land: a friend within reach who is on fire is sprayed out (every 30 s). True if she did. */
    public static boolean douse(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.PAENUNGULATA
                || girl.getSpecies().family() == GirlSpecies.Family.MERFOLK || level.getGameTime() < girl.getPersistentData().getLong(DOUSE_AT)) return false;
        Player friend = girl.bondedPlayer(Orders.AFFECTION, DOUSE_REACH);
        if (friend == null || !friend.isOnFire()) return false;
        friend.clearFire();
        girl.getPersistentData().putLong(DOUSE_AT, level.getGameTime() + DOUSE_GAP);
        girl.getLookControl().setLookAt(friend);
        girl.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        girl.playSound(SoundEvents.GENERIC_SPLASH, 1.0F, 1.0F);
        girl.addAffection(friend, 1);
        level.sendParticles(ParticleTypes.SPLASH, friend.getX(), friend.getY() + 1.0, friend.getZ(), 30, 0.4, 0.6, 0.4, 0.1);
        level.sendParticles(ParticleTypes.HEART, friend.getX(), friend.getEyeY() + 0.4, friend.getZ(), 1, 0.2, 0.1, 0.2, 0);
        if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.doused", girl.getDisplayName())
                .withStyle(net.minecraft.ChatFormatting.AQUA), true);
        return true;
    }

    // --- beachcombing -----------------------------------------------------------------------------------------------

    public static final String COMB_AT = "CombAt";
    public static final double COMB_REACH = 10.0;
    public static final int COMB_GAP = 2400, SHORE = 6;

    /** What turns up in the sand, out of 40. */
    private static net.minecraft.world.item.Item find(int roll) {
        if (roll == 0) return net.minecraft.world.item.Items.NAUTILUS_SHELL;
        if (roll < 4) return net.minecraft.world.item.Items.GOLD_NUGGET;
        if (roll < 8) return net.minecraft.world.item.Items.PRISMARINE_SHARD;
        if (roll < 12) return net.minecraft.world.item.Items.SEA_PICKLE;
        if (roll < 16) return net.minecraft.world.item.Items.INK_SAC;
        if (roll < 22) return net.minecraft.world.item.Items.CLAY_BALL;
        if (roll < 28) return net.minecraft.world.item.Items.BONE;
        if (roll < 34) return net.minecraft.world.item.Items.KELP;
        return net.minecraft.world.item.Items.FLINT;
    }

    /** Every five seconds for a grown crab, lobster or shrimp out of the water: a friend within reach on sand by the water gets
     *  what she digs up at their feet (every two minutes). The find, or null. */
    @Nullable
    public static net.minecraft.world.item.ItemStack beachcomb(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.MALACOSTRACA || girl.isInWater()
                || girl.getSpecies().family() == GirlSpecies.Family.MERFOLK || level.getGameTime() < girl.getPersistentData().getLong(COMB_AT)) return null;
        Player friend = girl.bondedPlayer(Orders.AFFECTION, COMB_REACH);
        if (friend == null || friend.isInWater()) return null;
        net.minecraft.core.BlockPos under = friend.blockPosition().below();
        if (!level.getBlockState(under).is(net.minecraft.tags.BlockTags.SAND)) return null;
        if (net.minecraft.core.BlockPos.findClosestMatch(under, SHORE, 2, p -> level.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)).isEmpty()) return null;
        girl.getPersistentData().putLong(COMB_AT, level.getGameTime() + COMB_GAP);
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(find(girl.getRandom().nextInt(40)));
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(level, friend.getX(), friend.getY() + 0.3, friend.getZ(), stack.copy());
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
        level.levelEvent(2001, under, net.minecraft.world.level.block.Block.getId(level.getBlockState(under)));
        girl.getLookControl().setLookAt(friend);
        girl.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.beachcombed", girl.getDisplayName(),
                stack.getHoverName()).withStyle(net.minecraft.ChatFormatting.YELLOW), true);
        return stack;
    }

    // --- fairy rings ------------------------------------------------------------------------------------------------

    public static final String RING_AT = "FairyRingAt", RING_NIGHT = "FairyRingNight";
    public static final int RING_GAP = 72000, RING_MIN = 6;
    public static final double RING_FRIEND = 16.0;
    /** The ring: eight spots two blocks out around its middle. */
    public static final java.util.List<net.minecraft.core.BlockPos> RING = java.util.List.of(
            new net.minecraft.core.BlockPos(2, 0, 1), new net.minecraft.core.BlockPos(2, 0, -1), new net.minecraft.core.BlockPos(-2, 0, 1), new net.minecraft.core.BlockPos(-2, 0, -1),
            new net.minecraft.core.BlockPos(1, 0, 2), new net.minecraft.core.BlockPos(-1, 0, 2), new net.minecraft.core.BlockPos(1, 0, -2), new net.minecraft.core.BlockPos(-1, 0, -2));

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time > 13000 && time < 23000;
    }

    /** Every ten seconds for a grown fae at night on the grass with a friend near: now and then (every few days at most) a ring of
     *  mushrooms comes up around her. How many mushrooms. Mob griefing off: none. */
    public static int fairyRing(ServerLevel level, AbstractMonsterGirl girl, boolean force) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.FAE || !isNight(level)
                || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)
                || level.getGameTime() < girl.getPersistentData().getLong(RING_AT) || (!force && girl.getRandom().nextInt(30) != 0)) return 0;
        if (girl.bondedPlayer(Orders.AFFECTION, RING_FRIEND) == null) return 0;
        net.minecraft.core.BlockPos mid = girl.blockPosition();
        if (!level.getBlockState(mid.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) return 0;
        int n = 0;
        for (net.minecraft.core.BlockPos off : RING) {
            net.minecraft.core.BlockPos at = mid.offset(off);
            if (!level.isEmptyBlock(at) || !level.getBlockState(at.below()).is(net.minecraft.tags.BlockTags.DIRT)) continue;
            level.setBlock(at, (girl.getRandom().nextInt(3) == 0 ? net.minecraft.world.level.block.Blocks.RED_MUSHROOM
                    : net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM).defaultBlockState(), 2);
            level.sendParticles(ParticleTypes.ENCHANT, at.getX() + 0.5, at.getY() + 0.3, at.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.3);
            n++;
        }
        if (n > 0) girl.getPersistentData().putLong(RING_AT, level.getGameTime() + RING_GAP);
        return n;
    }

    /** Whether a ring of mushrooms (at least six of the eight spots) stands around this spot. */
    public static boolean inRing(ServerLevel level, net.minecraft.core.BlockPos mid) {
        int n = 0;
        for (net.minecraft.core.BlockPos off : RING) if (level.getBlockState(mid.offset(off)).getBlock() instanceof net.minecraft.world.level.block.MushroomBlock) n++;
        return n >= RING_MIN;
    }

    /** Every two seconds for a player: standing in a fairy ring at night, they are lucky till morning (once a night). Whether they were just blessed. */
    public static boolean ringLuck(ServerLevel level, ServerPlayer p) {
        if (!MonsterGirlsConfig.comfort() || !isNight(level) || !inRing(level, p.blockPosition())) return false;
        long tonight = (level.getDayTime() + 12000L) / 24000L + 1;
        if (p.getPersistentData().getLong(RING_NIGHT) == tonight) return false;
        p.getPersistentData().putLong(RING_NIGHT, tonight);
        long left = 23000 - level.getDayTime() % 24000L;
        p.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, (int) Math.max(1200, left), 0));
        level.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.0, p.getZ(), 30, 0.6, 0.6, 0.6, 0.5);
        p.displayClientMessage(Component.translatable("message.monstergirls.fairy_ring").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }
}
