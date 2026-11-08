package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * Demon Realm grass (entity/DemonRealm): the purple, faintly sweet-smelling turf that grows around a Lilith. It spreads over grass
 * and dirt in the dark, as mycelium does, but light drives it back: where a torch's light ({@link #LIGHT} or more) reaches the air
 * above it, it turns back into grass, and it never spreads into such a spot. Now and then a heartfruit bush sprouts on it.
 * <p>
 * Each patch knows how far it is from where the realm was seeded ({@link #DEPTH}): it spreads no further than {@link #MAX_DEPTH} blocks
 * past its source, so a realm grows to its size round a Lilith (or a wild patch) and stops there, instead of swallowing the world.
 */
public class RealmGrassBlock extends SnowyDirtBlock implements net.minecraft.world.level.block.BonemealableBlock {
    public static final MapCodec<RealmGrassBlock> CODEC = simpleCodec(RealmGrassBlock::new);
    /** Block light (not daylight) at or above this keeps the realm away. */
    public static final int LIGHT = 8;
    /** One random tick in this many grows a tuft of crimson roots on bare realm grass. */
    public static final int ROOTS = 60, MAX_DEPTH = 8;
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty DEPTH =
            net.minecraft.world.level.block.state.properties.IntegerProperty.create("depth", 0, MAX_DEPTH);
    public static final DustParticleOptions HAZE = new DustParticleOptions(new Vector3f(0.95F, 0.45F, 0.78F), 1.0F);

    public RealmGrassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(DEPTH, 0));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DEPTH);
    }

    @Override
    protected MapCodec<? extends SnowyDirtBlock> codec() {
        return CODEC;
    }

    /** Lit by a lamp or torch: no realm here. */
    public static boolean lit(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        return level.getBrightness(LightLayer.BLOCK, ground.above()) >= LIGHT;
    }

    private static boolean open(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        BlockState above = level.getBlockState(ground.above());
        return !above.isSolidRender(level, ground.above()) && above.getFluidState().isEmpty();
    }

    /** Ground the realm can take: grass or dirt with open air above, out of torchlight. */
    public static boolean canTake(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        BlockState s = level.getBlockState(ground);
        return (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT)) && open(level, ground) && !lit(level, ground)
                && !(level instanceof net.minecraft.server.level.ServerLevel server && HolyFontBlock.warded(server, ground));   // a holy font keeps it out
    }

    /** Turns one block into realm grass, a source of its own (depth 0). True if it did. */
    public static boolean corrupt(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        return corrupt(level, ground, 0);
    }

    /** Turns one block into realm grass this far from its source. True if it did. */
    public static boolean corrupt(net.minecraft.world.level.LevelAccessor level, BlockPos ground, int depth) {
        if (!canTake(level, ground)) return false;
        level.setBlock(ground, ModBlocks.REALM_GRASS.get().defaultBlockState().setValue(DEPTH, Math.min(MAX_DEPTH, depth)), Block.UPDATE_ALL);
        BlockState tuft = level.getBlockState(ground.above());
        if (tuft.is(Blocks.SHORT_GRASS) || tuft.is(Blocks.FERN)) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_ALL);  // the tufts turn too
        return true;
    }

    /** Back to plain grass (dirt if covered), and a heartfruit bush or weepcap on it withers. */
    public static void purify(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        if (!level.getBlockState(ground).is(ModBlocks.REALM_GRASS.get())) return;
        BlockState plant = level.getBlockState(ground.above());
        if (plant.is(ModBlocks.HEARTFRUIT_BUSH.get()) || plant.is(ModBlocks.WEEPCAP.get())) level.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (plant.is(Blocks.CRIMSON_ROOTS)) level.setBlock(ground.above(), Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(ground, (open(level, ground) ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!open(level, pos)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (lit(level, pos)) {
            purify(level, pos);
            return;
        }
        if (!MonsterGirlsConfig.demonRealm()) return;
        boolean moon = com.linkyoshi90.monstergirls.entity.BedSharing.fullMoon(level) || com.linkyoshi90.monstergirls.entity.RoseMoon.tonight(level);
        if (random.nextInt(moon ? 1 : 3) == 0) spread(level, pos, random);   // a full or a rose moon feeds it
        if (random.nextInt(HeartfruitBushBlock.SPROUT) == 0) HeartfruitBushBlock.sprout(level, pos);
        if (random.nextInt(WeepcapBlock.SPROUT) == 0) WeepcapBlock.sprout(level, pos);
        if (random.nextInt(ROOTS) == 0 && level.isEmptyBlock(pos.above())) level.setBlock(pos.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_ALL);
        if (random.nextInt(3) == 0) RealmLeavesBlock.creep(level, pos, random);
        if (state.getValue(DEPTH) <= com.linkyoshi90.monstergirls.entity.DemonRealm.SUMMON_CORE && random.nextInt(com.linkyoshi90.monstergirls.entity.DemonRealm.SUMMON / com.linkyoshi90.monstergirls.entity.RoseMoon.stir(level)) == 0) {
            com.linkyoshi90.monstergirls.entity.DemonRealm.summon(level, pos, level.isNight());
        }
    }

    /** One try at the ground nearby, as grass spreads, a step further from the source; none at the realm's edge. Where it went, or null. */
    @javax.annotation.Nullable
    public static BlockPos spread(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState here = level.getBlockState(pos);
        int depth = here.hasProperty(DEPTH) ? here.getValue(DEPTH) : MAX_DEPTH;
        if (depth >= MAX_DEPTH) return null;
        BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        return corrupt(level, to, depth + 1) ? to : null;
    }

    // --- bone meal: the realm's own plants, as grass grows its own ------------------------------------------------------

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        bloom(level, pos, random);
    }

    /** Crimson roots on the bare realm grass round a spot, a weepcap or heartfruit bush now and then. How many plants grew. */
    public static int bloom(net.minecraft.world.level.LevelAccessor level, BlockPos pos, RandomSource random) {
        int grown = 0;
        for (int i = 0; i < 24; i++) {
            BlockPos ground = pos.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            if (!level.getBlockState(ground).is(ModBlocks.REALM_GRASS.get()) || !level.isEmptyBlock(ground.above())) continue;
            int roll = random.nextInt(12);
            if (roll == 0 && WeepcapBlock.sprout(level, ground)) grown++;
            else if (roll == 1 && HeartfruitBushBlock.sprout(level, ground)) grown++;
            else if (roll > 1) {
                level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_ALL);
                grown++;
            }
        }
        return grown;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(HAZE, pos.getX() + random.nextDouble(), pos.getY() + 1.1 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }
}
