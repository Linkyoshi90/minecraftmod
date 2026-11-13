package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.registry.HabitatTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Microhabitats (2026-11-13). Each kind cares about the little things about where she is ({@link Need}, one or two each, from her biomes
 * and concept: registry/HabitatTable): shade or sun, water near or dry ground, heights, caves, cold or heat.
 * <ul>
 * <li>Every {@link #CHECK} ticks she feels how well the spot suits her ({@link #comfort}, the share of her needs met). Content, she
 * sparkles now and then and heals slowly; ill at ease (under half), she sighs (a puff of cloud).</li>
 * <li>A wild one ill at ease goes looking for a better spot near by ({@link SeekGoal}: the best of a dozen tries within {@link #SEEK}).</li>
 * <li>Natural spawns where nothing about the spot suits her are thinned: one in {@link #THIN} still comes.</li>
 * </ul>
 * Water kinds (merfolk, mollusks, crustaceans) live in the water and have none. Config {@code microhabitats}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Microhabitats {
    public static final int CHECK = 200, SEEK = 16, THIN = 3, WATER = 6, HIGH_Y = 100, DEEP_Y = 50;
    public static final float CONTENT = 0.99F, UNEASY = 0.5F, COLD_T = 0.3F, HOT_T = 1.0F;

    private Microhabitats() {}

    public enum Need {
        SHADE, SUN, WET, DRY, HIGH, DEEP, COLD, HOT;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static List<Need> needs(AbstractMonsterGirl g) {
        List<String> n = HabitatTable.NEEDS.get(g.getSpecies().id());
        return n == null ? List.of() : n.stream().map(Need::valueOf).toList();
    }

    /** Whether this spot meets one need. */
    public static boolean meets(LevelAccessor level, BlockPos pos, Need n) {
        boolean sky = level.canSeeSky(pos.above());
        return switch (n) {
            case SHADE -> !sky || level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos.above()) < 14;
            case SUN -> sky;
            case WET -> water(level, pos, WATER) || level instanceof Level l && l.isRainingAt(pos.above());
            case DRY -> !water(level, pos, 3) && !(level instanceof Level l && l.isRainingAt(pos.above()));
            case HIGH -> pos.getY() >= HIGH_Y;
            case DEEP -> !sky && pos.getY() < DEEP_Y;
            case COLD -> level.getBiome(pos).value().getBaseTemperature() < COLD_T || pos.getY() >= HIGH_Y + 30;
            case HOT -> level.getBiome(pos).value().getBaseTemperature() > HOT_T;
        };
    }

    private static boolean water(LevelAccessor level, BlockPos pos, int r) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -2, -r), pos.offset(r, 1, r))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** How well a spot suits her: the share of her needs it meets (1 for a kind with none). */
    public static float comfort(LevelAccessor level, BlockPos pos, List<Need> needs) {
        if (needs.isEmpty()) return 1.0F;
        int met = 0;
        for (Need n : needs) if (meets(level, pos, n)) met++;
        return met / (float) needs.size();
    }

    public static float comfort(AbstractMonsterGirl g) {
        return comfort(g.level(), g.blockPosition(), needs(g));
    }

    /** Every so often she feels her spot: content, she heals slowly and sparkles; ill at ease, she sighs. Her comfort. */
    public static float feel(ServerLevel level, AbstractMonsterGirl g) {
        float c = comfort(g);
        if (c >= CONTENT) {
            g.addEffect(new MobEffectInstance(MobEffects.REGENERATION, CHECK / 2, 0, true, false, false));
            if (g.getRandom().nextInt(3) == 0) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, g.getX(), g.getEyeY() + 0.3, g.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
        } else if (c < UNEASY && g.getRandom().nextInt(2) == 0) {
            level.sendParticles(ParticleTypes.CLOUD, g.getX(), g.getEyeY(), g.getZ(), 2, 0.15, 0.05, 0.15, 0.01);
        }
        return c;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % CHECK == 89 && g.level() instanceof ServerLevel level
                && HarmontideConfig.microhabitats() && !g.isBaby()) feel(level, g);
    }

    /** The best spot near her, or null if none is better than where she is. */
    @Nullable
    public static BlockPos better(ServerLevel level, AbstractMonsterGirl g) {
        List<Need> needs = needs(g);
        float here = comfort(level, g.blockPosition(), needs), best = here;
        BlockPos at = null;
        var r = g.getRandom();
        for (int i = 0; i < 12; i++) {
            BlockPos p = g.blockPosition().offset(r.nextInt(SEEK * 2 + 1) - SEEK, r.nextInt(9) - 4, r.nextInt(SEEK * 2 + 1) - SEEK);
            if (!level.isLoaded(p) || !level.isEmptyBlock(p) || !level.isEmptyBlock(p.above()) || !level.getBlockState(p.below()).isSolid()) continue;
            float c = comfort(level, p, needs);
            if (c > best) {
                best = c;
                at = p;
            }
        }
        return at;
    }

    /** A wild girl ill at ease goes to a spot that suits her better. */
    public static final class SeekGoal extends Goal {
        private final AbstractMonsterGirl girl;
        private int cooldown;

        public SeekGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.cooldown = 400 + girl.getRandom().nextInt(400);               // she gives a new spot a while first
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level) || !HarmontideConfig.microhabitats()) return false;
            this.cooldown = 300 + this.girl.getRandom().nextInt(300);
            if (this.girl.isBaby() || this.girl.hasAnyAffection() || this.girl.hasRestriction() || comfort(this.girl) >= UNEASY) return false;
            BlockPos to = better(level, this.girl);
            return to != null && this.girl.getNavigation().moveTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5, 0.9);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.girl.getNavigation().isDone();
        }
    }

    /** Natural spawns where nothing about the spot suits her are thinned out. */
    @SubscribeEvent
    public static void onSpawn(MobSpawnEvent.PositionCheck event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl g) || !HarmontideConfig.microhabitats()) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        List<Need> needs = needs(g);
        if (!needs.isEmpty() && comfort(event.getLevel(), BlockPos.containing(event.getX(), event.getY(), event.getZ()), needs) == 0.0F
                && g.getRandom().nextInt(THIN) != 0) event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
    }
}
