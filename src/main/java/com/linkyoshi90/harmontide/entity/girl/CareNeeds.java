package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.HotSprings;
import com.linkyoshi90.harmontide.entity.SeagullHarpy;
import com.linkyoshi90.harmontide.entity.SparrowHarpy;
import com.linkyoshi90.harmontide.entity.Wardrobe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;

/**
 * Care needs (2026-11-11, after Monster Musume's homestay troubles): what some kinds need from the home they share with you.
 * (Lamias already shed their skin, merfolk already dry out ashore, sparrows and gulls already moult into their roost hopper.)
 * <ul>
 *   <li>A pool for the merfolk: a merfolk resident whose hearth has at least {@link #POOL} blocks of still water within
 *   {@link #POOL_RANGE} treats the pool as her place at home and waits in it. Without one she tells the hearth's owner each day
 *   that she needs one, and is a little put out ({@link #NO_POOL} friendship).</li>
 *   <li>Warmth for the lamias: in a cold biome a lamia slows to a crawl (Slowness) unless she is warmed: her winter coat on,
 *   a fire, campfire, lava or magma within {@link #WARM_RANGE}, or a hot spring.</li>
 *   <li>Moulting for the harpies: every few days a grown harpy with a friend ({@link #MOULT_FRIEND}) moults, and her favourite
 *   gets {@link #MOULT_MIN} to {@link #MOULT_MAX} feathers.</li>
 * </ul>
 * Config "careNeeds".
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class CareNeeds {
    public static final int POOL = 6, POOL_RANGE = 8, POOL_RECHECK = 200, NO_POOL = -2, WARM_RANGE = 5, MOULT_FRIEND = 10,
            MOULT_MIN = 3, MOULT_MAX = 6, MOULT_RANGE = 32;
    public static final long MOULT_EVERY = 4 * 24000L, MOULT_SPREAD = 3 * 24000L;
    private static final String POOL_AT = "PoolAt", POOL_CHECKED = "PoolChecked", POOL_NAGGED = "PoolNaggedDay", NEXT_MOULT = "NextMoult";

    private CareNeeds() {}

    // --- a pool for the merfolk ---------------------------------------------------------------------------------------

    /** Water enough by this hearth for a merfolk to live at: the still water nearest it, or null. */
    @Nullable
    public static BlockPos findPool(ServerLevel level, BlockPos hearth) {
        int count = 0;
        BlockPos best = null;
        for (BlockPos p : BlockPos.betweenClosed(hearth.offset(-POOL_RANGE, -3, -POOL_RANGE), hearth.offset(POOL_RANGE, 1, POOL_RANGE))) {
            if (!level.getFluidState(p).is(FluidTags.WATER) || !level.getFluidState(p).isSource()) continue;
            count++;
            if (best == null || p.distSqr(hearth) < best.distSqr(hearth)) best = p.immutable();
        }
        return count >= POOL ? best : null;
    }

    /** Her pool at home (looked up again every POOL_RECHECK ticks), or null. Only for merfolk with a home hearth. */
    @Nullable
    public static BlockPos pool(AbstractMonsterGirl girl) {
        if (!(girl instanceof Merfolk) || !(girl.level() instanceof ServerLevel level) || !HarmontideConfig.careNeeds()) return null;
        BlockPos hearth = girl.homeHearth();
        if (hearth == null) return null;
        CompoundTag data = girl.getPersistentData();
        if (level.getGameTime() >= data.getLong(POOL_CHECKED)) {
            data.putLong(POOL_CHECKED, level.getGameTime() + POOL_RECHECK);
            BlockPos found = findPool(level, hearth);
            if (found == null) data.remove(POOL_AT);
            else data.put(POOL_AT, NbtUtils.writeBlockPos(found));
        }
        return NbtUtils.readBlockPos(data, POOL_AT).orElse(null);
    }

    /** Once a day a merfolk resident without a pool tells her host. Whether she did. */
    public static boolean nag(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos hearth = girl.homeHearth();
        if (!(girl instanceof Merfolk) || girl.isBaby() || hearth == null || pool(girl) != null) return false;
        long day = level.getDayTime() / 24000L;
        if (girl.getPersistentData().contains(POOL_NAGGED) && girl.getPersistentData().getLong(POOL_NAGGED) >= day) return false;
        if (!(level.getBlockEntity(hearth) instanceof HearthBlockEntity h) || h.owner() == null) return false;
        Player owner = level.getPlayerByUUID(h.owner());
        if (owner == null || owner.distanceToSqr(girl) > 48 * 48) return false;
        girl.getPersistentData().putLong(POOL_NAGGED, day);
        girl.addAffection(owner, NO_POOL);
        owner.displayClientMessage(Component.translatable("message.harmontide.care.pool", girl.getDisplayName(), POOL), false);
        return true;
    }

    // --- warmth for the lamias ----------------------------------------------------------------------------------------

    public static boolean cold(ServerLevel level, AbstractMonsterGirl girl) {
        return level.getBiome(girl.blockPosition()).value().getBaseTemperature() < Wardrobe.COLD;
    }

    public static boolean warmed(ServerLevel level, AbstractMonsterGirl girl) {
        if (girl.getOutfit() == Wardrobe.Outfit.WINTER_COAT.ordinal() + 1 || HotSprings.inSpring(girl) || girl.isOnFire()) return true;
        for (BlockPos p : BlockPos.betweenClosed(girl.blockPosition().offset(-WARM_RANGE, -2, -WARM_RANGE), girl.blockPosition().offset(WARM_RANGE, 2, WARM_RANGE))) {
            BlockState s = level.getBlockState(p);
            if (s.is(BlockTags.FIRE) || s.is(Blocks.MAGMA_BLOCK) || s.getFluidState().is(FluidTags.LAVA)
                    || s.is(BlockTags.CAMPFIRES) && s.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)) return true;
        }
        return false;
    }

    /** Every two seconds: a lamia in the cold, unwarmed, slows to a crawl. Whether she is chilled. */
    public static boolean chill(ServerLevel level, AbstractMonsterGirl girl) {
        return chill(level, girl, cold(level, girl));
    }

    /** The same, with the cold given (tests). */
    public static boolean chill(ServerLevel level, AbstractMonsterGirl girl, boolean cold) {
        if (girl.getSpecies().family() != GirlSpecies.Family.LAMIA || !cold || warmed(level, girl)) return false;
        girl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false));
        if (girl.getRandom().nextInt(3) == 0) level.sendParticles(ParticleTypes.SNOWFLAKE, girl.getX(), girl.getY() + girl.getBbHeight() * 0.8, girl.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    // --- moulting harpies ---------------------------------------------------------------------------------------------

    /** Her favourite within MOULT_RANGE (at least MOULT_FRIEND), or null. */
    @Nullable
    private static Player favourite(ServerLevel level, AbstractHarpy harpy) {
        Player best = null;
        for (Player p : level.players()) {
            int a = harpy.getAffection(p);
            if (a < MOULT_FRIEND || p.isSpectator() || p.distanceToSqr(harpy) > MOULT_RANGE * MOULT_RANGE) continue;
            if (best == null || a > harpy.getAffection(best)) best = p;
        }
        return best;
    }

    /** When it's time, a harpy moults for her favourite. How many feathers (0: not now). */
    public static int moult(ServerLevel level, AbstractHarpy harpy, boolean force) {
        if (harpy.isBaby() || harpy instanceof SparrowHarpy || harpy instanceof SeagullHarpy) return 0;   // those two shed into their roost
        CompoundTag data = harpy.getPersistentData();
        long now = level.getGameTime();
        if (!data.contains(NEXT_MOULT)) data.putLong(NEXT_MOULT, now + MOULT_EVERY + harpy.getRandom().nextInt((int) MOULT_SPREAD));
        if (!force && now < data.getLong(NEXT_MOULT)) return 0;
        Player friend = favourite(level, harpy);
        if (friend == null) return 0;
        data.putLong(NEXT_MOULT, now + MOULT_EVERY + harpy.getRandom().nextInt((int) MOULT_SPREAD));
        int n = MOULT_MIN + harpy.getRandom().nextInt(MOULT_MAX - MOULT_MIN + 1);
        ItemStack feathers = new ItemStack(Items.FEATHER, n);
        if (!friend.getInventory().add(feathers)) harpy.spawnAtLocation(feathers);
        level.sendParticles(ParticleTypes.CLOUD, harpy.getX(), harpy.getY() + harpy.getBbHeight() * 0.6, harpy.getZ(), 8, 0.4, 0.3, 0.4, 0.01);
        harpy.playSound(SoundEvents.CHICKEN_EGG, 0.6F, 1.6F);
        friend.displayClientMessage(Component.translatable("message.harmontide.care.moult", harpy.getDisplayName(), n), true);
        return n;
    }

    // --- ticking ------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 7 || !(event.getEntity().level() instanceof ServerLevel level) || !HarmontideConfig.careNeeds()) return;
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;   // the tests call these themselves
        if (event.getEntity() instanceof AbstractMonsterGirl girl) {
            chill(level, girl);
            if (girl.tickCount % 400 == 7) nag(level, girl);
        } else if (event.getEntity() instanceof AbstractHarpy harpy && harpy.tickCount % 200 == 7) {
            moult(level, harpy, false);
        }
    }
}
