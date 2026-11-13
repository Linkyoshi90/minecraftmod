package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.registry.DietTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Pollination and seed spreading (2026-11-13).
 * <p>
 * <b>Pollinators</b> (bees, moths, butterflies, fairies, pixies, nectar bats: registry/DietTable#POLLINATORS) go from flower to flower
 * ({@link VisitGoal}), wild or befriended. Each visit dusts her with pollen; the next flower she visits {@link #APART} or more
 * blocks from the last is pollinated ({@link #pollinate}): another flower of its kind springs up beside it, and the crops, stems and
 * berry bushes within {@link #GARDEN} grow on a stage.
 * <p>
 * <b>Seed carriers</b> (fruit, nut and seed eaters: DietTable#SEEDERS), wild, carry a seed away from their meals ({@link #swallow}) and,
 * at least {@link #CARRY} ticks and {@link #FAR} blocks later, leave it on open grass ({@link #sow}): a sapling suited to the land
 * there (a berry bush in the taiga), never in a village or near a hearth. So the woods creep out into the meadows the herds roam.
 * Part of the food web (config {@code foodWeb}).
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Pollination {
    public static final int APART = 4, GARDEN = 6, GROWS = 4, VISIT_EVERY = 300, CARRY = 1200, HOME = 48;
    public static final double FAR = 16;
    private static final String POLLEN_X = "PollenX", POLLEN_Z = "PollenZ", POLLEN_FLOWER = "PollenFlower", SEED = "Seed", SEED_X = "SeedX", SEED_Z = "SeedZ", SEED_TIME = "SeedTime";

    private Pollination() {}

    public static boolean pollinator(AbstractMonsterGirl g) {
        return HarmontideConfig.foodWeb() && !g.isBaby() && DietTable.POLLINATORS.contains(g.getSpecies().id()) && !g.isPassenger() && !g.isOrderedToSit()
                && Orders.current(g) != Orders.Command.FOLLOW;
    }

    public static boolean seeder(AbstractMonsterGirl g) {
        return DietTable.SEEDERS.contains(g.getSpecies().id());
    }

    // --- pollen --------------------------------------------------------------------------------------------------------

    public static boolean flower(BlockState s) {
        return s.is(BlockTags.FLOWERS) || s.is(BlockTags.SMALL_FLOWERS);
    }

    @Nullable
    public static BlockPos findFlower(ServerLevel level, AbstractMonsterGirl g) {
        var r = g.getRandom();
        BlockPos at = g.blockPosition();
        for (int i = 0; i < 32; i++) {
            BlockPos p = at.offset(r.nextInt(25) - 12, r.nextInt(7) - 3, r.nextInt(25) - 12);
            if (flower(level.getBlockState(p))) return p;
        }
        return null;
    }

    /** She visits a flower: pollinating it if she brings pollen from another, then dusting herself with this one's. True if it was pollinated. */
    public static boolean visit(ServerLevel level, AbstractMonsterGirl g, BlockPos flower) {
        BlockState s = level.getBlockState(flower);
        if (!flower(s)) return false;
        CompoundTag pd = g.getPersistentData();
        boolean pollinated = false;
        if (pd.contains(POLLEN_X) && Math.abs(pd.getInt(POLLEN_X) - flower.getX()) + Math.abs(pd.getInt(POLLEN_Z) - flower.getZ()) >= APART) {
            pollinate(level, flower, s);
            pollinated = true;
        }
        pd.putInt(POLLEN_X, flower.getX());
        pd.putInt(POLLEN_Z, flower.getZ());
        pd.putString(POLLEN_FLOWER, BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString());
        g.startTimedAction(AbstractMonsterGirl.Action.POLLINATE, 40);
        level.sendParticles(ParticleTypes.FALLING_NECTAR, flower.getX() + 0.5, flower.getY() + 0.8, flower.getZ() + 0.5, 5, 0.3, 0.2, 0.3, 0.0);
        return pollinated;
    }

    /** Pollinated: a flower of its kind springs up beside it (small flowers only), and the crops near it grow a stage. How much grew. */
    public static int pollinate(ServerLevel level, BlockPos flower, BlockState s) {
        int n = 0;
        var r = level.getRandom();
        if (s.is(BlockTags.SMALL_FLOWERS)) {
            java.util.List<BlockPos> spots = new java.util.ArrayList<>();
            for (BlockPos q : BlockPos.betweenClosed(flower.offset(-2, -1, -2), flower.offset(2, 1, 2))) spots.add(q.immutable());
            java.util.Collections.shuffle(spots, new java.util.Random(r.nextLong()));
            for (BlockPos p : spots) {
                if (level.isEmptyBlock(p) && level.getBlockState(p.below()).is(BlockTags.DIRT) && s.canSurvive(level, p)) {
                    level.setBlockAndUpdate(p, s.getBlock().defaultBlockState());
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.5, 4, 0.2, 0.2, 0.2, 0.0);
                    n++;
                    break;
                }
            }
        }
        int grown = 0;
        for (BlockPos p : BlockPos.betweenClosed(flower.offset(-GARDEN, -2, -GARDEN), flower.offset(GARDEN, 2, GARDEN))) {
            if (grown >= GROWS) break;
            if (growOn(level, p.immutable())) grown++;
        }
        return n + grown;
    }

    /** A crop, stem or berry bush grows a stage. True if it did. */
    public static boolean growOn(ServerLevel level, BlockPos p) {
        BlockState s = level.getBlockState(p);
        if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s)) {
            level.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
        } else if (s.getBlock() instanceof StemBlock && s.getValue(StemBlock.AGE) < StemBlock.MAX_AGE) {
            level.setBlock(p, s.setValue(StemBlock.AGE, s.getValue(StemBlock.AGE) + 1), 2);
        } else if (s.getBlock() instanceof SweetBerryBushBlock && s.getValue(SweetBerryBushBlock.AGE) < SweetBerryBushBlock.MAX_AGE) {
            level.setBlock(p, s.setValue(SweetBerryBushBlock.AGE, s.getValue(SweetBerryBushBlock.AGE) + 1), 2);
        } else {
            return false;
        }
        level.sendParticles(ParticleTypes.WAX_ON, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.25, 0.2, 0.25, 0.0);
        return true;
    }

    public static final class VisitGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable private BlockPos to;
        private int cooldown, ticks;

        public VisitGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level) || !pollinator(this.girl) || this.girl.restingHours()) return false;
            this.cooldown = VISIT_EVERY / 2 + this.girl.getRandom().nextInt(VISIT_EVERY);
            this.to = findFlower(level, this.girl);
            return this.to != null;
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 1.0);
        }

        @Override
        public boolean canContinueToUse() {
            return this.to != null && this.ticks < 300;
        }

        @Override
        public void tick() {
            if (++this.ticks % 10 != 0 || !(this.girl.level() instanceof ServerLevel level)) return;
            if (this.girl.blockPosition().closerThan(this.to, 2.5)) {
                visit(level, this.girl, this.to);
                this.to = null;
            } else if (this.girl.getNavigation().isDone()) {
                this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 1.0);
            }
        }
    }

    // --- seeds ---------------------------------------------------------------------------------------------------------

    /** What would grow from a seed left here: a sapling for the land, a berry bush in the taiga. */
    public static Block seedFor(Holder<Biome> biome, net.minecraft.util.RandomSource r) {
        if (biome.is(BiomeTags.IS_TAIGA)) return r.nextInt(3) == 0 ? Blocks.SWEET_BERRY_BUSH : Blocks.SPRUCE_SAPLING;
        if (biome.is(BiomeTags.IS_JUNGLE)) return Blocks.JUNGLE_SAPLING;
        if (biome.is(BiomeTags.IS_SAVANNA)) return Blocks.ACACIA_SAPLING;
        if (biome.is(Biomes.CHERRY_GROVE)) return Blocks.CHERRY_SAPLING;
        if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) return Blocks.BIRCH_SAPLING;
        if (biome.is(Biomes.MANGROVE_SWAMP)) return Blocks.MANGROVE_PROPAGULE;
        if (biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.GROVE)) return Blocks.SPRUCE_SAPLING;
        return r.nextInt(4) == 0 ? Blocks.BIRCH_SAPLING : Blocks.OAK_SAPLING;
    }

    /** After a meal, a seed carrier takes a seed of the land she ate in away with her. */
    public static void swallow(ServerLevel level, AbstractMonsterGirl g) {
        if (!seeder(g) || g.hasAnyAffection() || g.getPersistentData().contains(SEED)) return;
        Block seed = seedFor(level.getBiome(g.blockPosition()), g.getRandom());
        CompoundTag pd = g.getPersistentData();
        pd.putString(SEED, BuiltInRegistries.BLOCK.getKey(seed).toString());
        pd.putInt(SEED_X, g.getBlockX());
        pd.putInt(SEED_Z, g.getBlockZ());
        pd.putLong(SEED_TIME, level.getGameTime());
    }

    public static boolean carrying(AbstractMonsterGirl g) {
        return g.getPersistentData().contains(SEED);
    }

    /** Far enough and long enough after, on open grass and away from people's homes, the seed is left to grow. Where, or null. */
    @Nullable
    public static BlockPos sow(ServerLevel level, AbstractMonsterGirl g, long now) {
        CompoundTag pd = g.getPersistentData();
        if (!pd.contains(SEED) || now - pd.getLong(SEED_TIME) < CARRY) return null;
        if (Math.hypot(g.getX() - pd.getInt(SEED_X), g.getZ() - pd.getInt(SEED_Z)) < FAR) return null;
        BlockPos at = g.blockPosition();
        if (!level.isEmptyBlock(at) || !level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK) || level.isVillage(at)) return null;
        for (BlockPos h : HearthBlockEntity.loaded(level)) if (h.closerThan(at, HOME)) return null;
        Block seed = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(pd.getString(SEED)));
        pd.remove(SEED);
        BlockState s = seed.defaultBlockState();
        if (!s.canSurvive(level, at)) return null;
        level.setBlockAndUpdate(at, s);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.3, at.getZ() + 0.5, 5, 0.2, 0.1, 0.2, 0.0);
        return at;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % 200 == 61 && g.level() instanceof ServerLevel level && carrying(g)) sow(level, g, level.getGameTime());
    }
}
