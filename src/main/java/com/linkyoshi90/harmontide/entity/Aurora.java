package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Aurora nights (2026-11-08). One clear night in {@link #EVERY}, the sky over cold country (where it's cold enough to snow)
 * fills with curtains of green and violet light. Under it a frost-touched girl or two comes out near each player (a yuki-onna, a
 * snow elf, a frost slime, a snow leopard, an arctic fox, a polar bear), wild, and goes again at dawn unless she's made a
 * friend; and a few aurora shards fall to the snow, a gift every girl loves ({@link #SHARD_AFFECTION}, and she blushes).
 * Off with the option auroraNights.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Aurora {
    public static final int EVERY = 6, VISITORS = 2, SHARDS = 3, SHARD_AFFECTION = 6, HEIGHT = 40, SPREAD = 48;
    public static final List<GirlSpecies> FROST_TOUCHED = List.of(GirlSpecies.YUKI_ONNA_UNDEAD, GirlSpecies.SNOW_ELF, GirlSpecies.FROST_SLIME,
            GirlSpecies.SNOW_LEOPARD_NEKOMATA, GirlSpecies.ARCTIC_KITSUNE, GirlSpecies.POLAR_BEAR_CANID);
    private static final String SEEN = "AuroraNight", TOUCHED = "AuroraLeavesAtDawn";
    private static final DustParticleOptions[] LIGHT = {
            new DustParticleOptions(new Vector3f(0.30F, 1.00F, 0.55F), 4.0F), new DustParticleOptions(new Vector3f(0.20F, 0.85F, 0.75F), 4.0F),
            new DustParticleOptions(new Vector3f(0.65F, 0.40F, 1.00F), 4.0F)};

    private Aurora() {}

    public static long night(long dayTime) {
        return Math.floorDiv(dayTime - 13000L, 24000L);              // the night that began at this day's dusk
    }

    /** Whether tonight is an aurora night (one in EVERY, scattered by the night's number). */
    public static boolean auroraNight(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        if (t < 13000L || t >= 23000L) return false;
        long n = night(dayTime);
        return Math.floorMod(n * 0x9E3779B97F4A7C15L >>> 40, (long) EVERY) == 0;
    }

    /** Under the aurora: an aurora night, clear, in the overworld, somewhere cold enough to snow, under the open sky. */
    public static boolean under(ServerLevel level, BlockPos at) {
        return HarmontideConfig.auroraNights() && level.dimension() == Level.OVERWORLD && !level.isRaining() && auroraNight(level.getDayTime())
                && level.getBiome(at).value().coldEnoughToSnow(at) && level.canSeeSky(at.above());
    }

    /** Curtains of light high over a player (the particles travel the long way, so they're seen from far below). */
    public static void shimmer(ServerLevel level, ServerPlayer p) {
        long t = level.getGameTime();
        for (int band = 0; band < 3; band++) {
            double z = p.getZ() - 30 + band * 22;
            for (int i = 0; i < 6; i++) {
                double x = p.getX() - 40 + level.random.nextDouble() * 80;
                double y = p.getY() + HEIGHT + Math.sin(x * 0.08 + t * 0.02 + band) * 6 + level.random.nextDouble() * 10;
                level.sendParticles(p, LIGHT[(band + i) % LIGHT.length], true, x, y, z + Math.sin(x * 0.05 + t * 0.01) * 8, 1, 0.5, 2.0, 0.5, 0.0);
            }
        }
    }

    /** The night begins for this player: the frost-touched come out and shards fall. Who came. */
    public static List<Mob> begin(ServerLevel level, Player p) {
        p.displayClientMessage(Component.translatable("message.harmontide.aurora.begin").withStyle(ChatFormatting.AQUA), false);
        level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.5F, 0.6F);
        Dairy.award(p, "aurora");
        List<Mob> came = new ArrayList<>();
        for (int i = 0; i < VISITORS; i++) {
            BlockPos at = spot(level, p.blockPosition(), 16, 32);
            GirlSpecies kind = FROST_TOUCHED.get(level.random.nextInt(FROST_TOUCHED.size()));
            if (at == null || !(ModGirls.type(kind).create(level) instanceof AbstractMonsterGirl girl)) continue;
            girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
            girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            girl.setBaby(false);
            girl.setPersistenceRequired();
            girl.getPersistentData().putBoolean(TOUCHED, true);
            if (!level.addFreshEntity(girl)) continue;
            level.sendParticles(ParticleTypes.SNOWFLAKE, girl.getX(), girl.getY() + 1, girl.getZ(), 30, 0.5, 0.8, 0.5, 0.02);
            came.add(girl);
        }
        for (int i = 0; i < SHARDS; i++) {
            BlockPos at = spot(level, p.blockPosition(), 4, 20);
            if (at == null) continue;
            ItemEntity shard = new ItemEntity(level, at.getX() + 0.5, at.getY() + 6, at.getZ() + 0.5, new ItemStack(ModItems.AURORA_SHARD.get()));
            shard.setGlowingTag(true);
            level.addFreshEntity(shard);
        }
        return came;
    }

    private static BlockPos spot(ServerLevel level, BlockPos near, int min, int max) {
        for (int tries = 0; tries < 8; tries++) {
            double a = level.random.nextDouble() * Math.PI * 2, d = min + level.random.nextDouble() * (max - min);
            int x = near.getX() + (int) Math.round(Math.cos(a) * d), z = near.getZ() + (int) Math.round(Math.sin(a) * d);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos at = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level.getFluidState(at.below()).isEmpty()) return at;
        }
        return null;
    }

    /** At dawn a frost-touched girl goes back where she came from, unless she has a friend now. True if she left. */
    public static boolean dawn(ServerLevel level, Mob m) {
        long t = Math.floorMod(level.getDayTime(), 24000L);
        return dawn(level, m, t >= 13000L && t < 23000L);
    }

    public static boolean dawn(ServerLevel level, Mob m, boolean night) {
        if (!m.getPersistentData().getBoolean(TOUCHED)) return false;
        if (m instanceof Bonded b && b.hasAnyAffection()) {
            m.getPersistentData().remove(TOUCHED);
            return false;
        }
        if (night) return false;                                          // still night
        level.sendParticles(ParticleTypes.SNOWFLAKE, m.getX(), m.getY() + 1, m.getZ(), 30, 0.5, 0.8, 0.5, 0.05);
        m.discard();
        return true;
    }

    public static boolean frostTouched(Mob m) {
        return m.getPersistentData().getBoolean(TOUCHED);
    }

    /** An aurora shard given to a girl: she loves it. True if she took it. */
    public static boolean gift(ServerLevel level, Mob m, Player p, ItemStack shard) {
        if (!(m instanceof Bonded b) || m.isBaby()) return false;
        b.addAffection(p, SHARD_AFFECTION);
        b.blushFor(200);
        shard.consume(1, p);
        level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        level.sendParticles(LIGHT[level.random.nextInt(LIGHT.length)], m.getX(), m.getEyeY() + 0.5, m.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel level) || p.tickCount % 10 != 3) return;
        if (!under(level, p.blockPosition())) return;
        shimmer(level, p);
        long n = night(level.getDayTime());
        if (p.getPersistentData().getLong(SEEN) != n + 1) {                 // stored +1 so 0 means never
            p.getPersistentData().putLong(SEEN, n + 1);
            begin(level, p);
        }
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob m && m.tickCount % 200 == 87 && m.level() instanceof ServerLevel level) dawn(level, m);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.AURORA_SHARD.get()) || !(event.getTarget() instanceof Mob m) || !(event.getLevel() instanceof ServerLevel level)) return;
        if (gift(level, m, event.getEntity(), stack)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
