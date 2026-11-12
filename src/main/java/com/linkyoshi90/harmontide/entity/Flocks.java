package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Passing flocks (2026-11-08): girls stay rare, but every {@link #EVERY} days, on the morning of a flock day, a flock of
 * wild harpies of one migratory kind comes down near each player who is out under the open sky: swans, ducks, pelicans or
 * flamingos by water, sparrows, finches or crows inland. They rest a day and fly on (gone in a puff of feathers), all but any
 * who has made a friend: she stays. Flocks alternate north and south, as if the seasons turned. Off with the option passingFlocks.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Flocks {
    public static final int EVERY = 8, ON = 3, MIN = 3, MAX = 5, NEAR = 20, FAR = 36, STAY = 20000, WATER_LOOK = 24;
    private static final String TAG = "FlockLeaves", DAY = "FlockDay";

    private Flocks() {}

    public static final List<Supplier<EntityType<? extends Mob>>> WATERFOWL = List.of(
            ModEntities.SWAN_HARPY::get, ModEntities.DUCK_HARPY::get, ModEntities.PELICAN_HARPY::get, ModEntities.FLAMINGO_HARPY::get);
    public static final List<Supplier<EntityType<? extends Mob>>> SONGBIRDS = List.of(
            ModEntities.SPARROW_HARPY::get, ModEntities.FINCH_HARPY::get, ModEntities.CROW_HARPY::get);

    public static boolean flockDay(long dayTime) {
        return Math.floorMod(dayTime / 24000L, (long) EVERY) == ON;
    }

    /** Which way this flock flies: by the count of flocks so far, north and south by turns. */
    public static boolean northward(long dayTime) {
        return Math.floorMod(dayTime / 24000L / EVERY, 2L) == 0;
    }

    /** Open water within WATER_LOOK blocks (a sparse look along the surface). */
    public static boolean nearWater(ServerLevel level, BlockPos pos) {
        for (int dx = -WATER_LOOK; dx <= WATER_LOOK; dx += 4) {
            for (int dz = -WATER_LOOK; dz <= WATER_LOOK; dz += 4) {
                int x = pos.getX() + dx, z = pos.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
                BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
                if (level.getFluidState(top).is(FluidTags.WATER)) return true;
            }
        }
        return false;
    }

    /** A flock comes down near this spot. Those who came. */
    public static List<Mob> land(ServerLevel level, BlockPos near, EntityType<? extends Mob> kind, int count, int min, int max) {
        List<Mob> out = new ArrayList<>();
        double angle = level.random.nextDouble() * Math.PI * 2, d = min + level.random.nextDouble() * Math.max(0, max - min);
        int cx = near.getX() + (int) Math.round(Math.cos(angle) * d), cz = near.getZ() + (int) Math.round(Math.sin(angle) * d);
        for (int i = 0; i < count; i++) {
            int x = cx + level.random.nextInt(7) - 3, z = cz + level.random.nextInt(7) - 3;
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos at = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (!level.getFluidState(at.below()).isEmpty() && !level.getBlockState(at.below()).isSolid()) continue;
            Mob bird = kind.create(level);
            if (bird == null) continue;
            bird.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            bird.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            if (bird instanceof AbstractHarpy harpy) harpy.randomizeVariant();
            if (bird.isBaby()) continue;
            bird.getPersistentData().putLong(TAG, level.getGameTime() + STAY);
            bird.setPersistenceRequired();
            if (!level.addFreshEntity(bird)) continue;
            level.sendParticles(ParticleTypes.CLOUD, bird.getX(), bird.getY() + 1.0, bird.getZ(), 6, 0.4, 0.3, 0.4, 0.02);
            out.add(bird);
        }
        return out;
    }

    /** One player's look at the sky on a flock morning. The flock, or empty. */
    public static List<Mob> tryFlock(ServerLevel level, Player player) {
        if (!HarmontideConfig.passingFlocks() || level.dimension() != Level.OVERWORLD || level.getDifficulty() == Difficulty.PEACEFUL
                || !flockDay(level.getDayTime()) || level.getDayTime() % 24000L > 3000L || !level.canSeeSky(player.blockPosition().above())
                || DemonRealm.realmAir(player)) return List.of();
        long day = level.getDayTime() / 24000L;
        if (player.getPersistentData().getLong(DAY) == day + 1) return List.of();   // one flock a flock day (stored +1 so 0 means never)
        player.getPersistentData().putLong(DAY, day + 1);
        var kinds = nearWater(level, player.blockPosition()) ? WATERFOWL : SONGBIRDS;
        EntityType<? extends Mob> kind = kinds.get(level.random.nextInt(kinds.size())).get();
        BlockPos perch = Colonies.perchNear(level, player.blockPosition());
        List<Mob> flock = perch != null ? land(level, perch, kind, MIN + level.random.nextInt(MAX - MIN + 1), 2, 6)
                : land(level, player.blockPosition(), kind, MIN + level.random.nextInt(MAX - MIN + 1), NEAR, FAR);
        if (!flock.isEmpty() && perch != null) {                           // the perch draws them in: they stay
            Colonies.settleAtPerch(flock, perch);
            level.playSound(null, perch, SoundEvents.PARROT_FLY, SoundSource.NEUTRAL, 2.0F, 0.8F);
            player.displayClientMessage(Component.translatable("message.harmontide.flock.perch", kind.getDescription()).withStyle(ChatFormatting.AQUA), false);
            Dairy.award(player, "harpy_perch");
        } else if (!flock.isEmpty()) {
            level.playSound(null, flock.get(0).blockPosition(), SoundEvents.PARROT_FLY, SoundSource.NEUTRAL, 2.0F, 0.8F);
            player.displayClientMessage(Component.translatable("message.harmontide.flock." + (northward(level.getDayTime()) ? "north" : "south"),
                    kind.getDescription()).withStyle(ChatFormatting.AQUA), false);
        }
        return flock;
    }

    /** A resting flock bird: once her day is over she flies on, unless she has made a friend (then she stays for good). True if she left. */
    public static boolean flyOn(ServerLevel level, Mob bird) {
        if (!bird.getPersistentData().contains(TAG)) return false;
        if (bird instanceof Bonded b && b.hasAnyAffection()) {
            bird.getPersistentData().remove(TAG);                      // she has a friend here now
            return false;
        }
        if (level.getGameTime() < bird.getPersistentData().getLong(TAG)) return false;
        level.sendParticles(ParticleTypes.CLOUD, bird.getX(), bird.getY() + 1.0, bird.getZ(), 12, 0.4, 0.5, 0.4, 0.05);
        level.playSound(null, bird.blockPosition(), SoundEvents.PARROT_FLY, SoundSource.NEUTRAL, 1.0F, 1.0F);
        bird.discard();
        return true;
    }

    /** She stays for good (a perch drew her in). */
    public static void stay(Mob bird) {
        bird.getPersistentData().remove(TAG);
    }

    /** For tests: her day is over now. */
    public static void dueNow(Mob bird) {
        bird.getPersistentData().putLong(TAG, 0L);
    }

    public static boolean resting(Mob bird) {
        return bird.getPersistentData().contains(TAG);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 200 == 77 && event.getEntity().level() instanceof ServerLevel level) tryFlock(level, event.getEntity());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 100 == 41 && event.getEntity() instanceof Mob bird && bird.level() instanceof ServerLevel level) flyOn(level, bird);
    }
}
