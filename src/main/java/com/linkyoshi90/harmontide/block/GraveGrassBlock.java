package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.entity.DeadRealm;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.joml.Vector3f;

/**
 * Grave grass (entity/DeadRealm, 2026-11-13): the ashen, grey-green turf of the realm of the dead, MGE's undead realm, that grows
 * round a wild lich or koschei. Like the Demon Realm's grass it knows its distance from its source ({@link #DEPTH}) and stops
 * {@link #MAX_DEPTH} past it, and lamplight ({@link #LIGHT} or more) turns it back. Unlike it, the dead keep to the night:
 * under the open sky by day it withers back one patch in {@link #SUN} per tick, and it spreads only in the dark (night, or under a roof
 * or a canopy). Grave mist rises off it; dead bushes sprout on it.
 */
public class GraveGrassBlock extends SnowyDirtBlock {
    public static final MapCodec<GraveGrassBlock> CODEC = simpleCodec(GraveGrassBlock::new);
    public static final int LIGHT = 8, MAX_DEPTH = 8, SUN = 3, BUSH = 70;
    public static final IntegerProperty DEPTH = IntegerProperty.create("depth", 0, MAX_DEPTH);
    /** Grave mist: a cold grey-green. */
    public static final DustParticleOptions MIST = new DustParticleOptions(new Vector3f(0.62F, 0.70F, 0.64F), 1.4F);

    public GraveGrassBlock(Properties properties) {
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

    public static boolean grave(BlockState s) {
        return s.getBlock() instanceof GraveGrassBlock;
    }

    public static boolean lit(LevelAccessor level, BlockPos ground) {
        return level.getBrightness(LightLayer.BLOCK, ground.above()) >= LIGHT;
    }

    private static boolean open(LevelAccessor level, BlockPos ground) {
        BlockState above = level.getBlockState(ground.above());
        return !above.isSolidRender(level, ground.above()) && above.getFluidState().isEmpty();
    }

    /** In the sun: daytime, with the sky open above. */
    public static boolean sunlit(Level level, BlockPos ground) {
        return level.isDay() && level.canSeeSky(ground.above());
    }

    /** Ground the dead take: grass, dirt, podzol, coarse dirt, mud. */
    public static boolean takeable(BlockState s) {
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.MUD);
    }

    public static boolean canTake(LevelAccessor level, BlockPos ground) {
        return takeable(level.getBlockState(ground)) && open(level, ground) && !lit(level, ground)
                && !(level instanceof ServerLevel server && HolyFontBlock.warded(server, ground));
    }

    public static boolean corrupt(LevelAccessor level, BlockPos ground, int depth) {
        if (!canTake(level, ground)) return false;
        level.setBlock(ground, ModBlocks.GRAVE_GRASS.get().defaultBlockState().setValue(DEPTH, Math.min(MAX_DEPTH, depth)), Block.UPDATE_ALL);
        BlockState tuft = level.getBlockState(ground.above());
        if (tuft.is(Blocks.SHORT_GRASS) || tuft.is(Blocks.FERN)) level.setBlock(ground.above(), Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    /** Back to grass (dirt if covered); a dead bush on it goes back to a grass tuft. */
    public static void purify(LevelAccessor level, BlockPos ground) {
        if (!grave(level.getBlockState(ground))) return;
        if (level.getBlockState(ground.above()).is(Blocks.DEAD_BUSH)) level.setBlock(ground.above(), Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(ground, (open(level, ground) ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!open(level, pos)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (lit(level, pos) || sunlit(level, pos) && state.getValue(DEPTH) > 0 && random.nextInt(SUN) == 0) {   // its heart outlasts the day
            purify(level, pos);
            return;
        }
        if (!HarmontideConfig.deadRealm() || sunlit(level, pos)) return;
        spread(level, pos, random);
        if (random.nextInt(BUSH) == 0 && level.isEmptyBlock(pos.above())) level.setBlock(pos.above(), Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_ALL);
        if (state.getValue(DEPTH) <= DeadRealm.RISE_CORE && random.nextInt(DeadRealm.RISE) == 0) DeadRealm.rise(level, pos, !level.isDay());
    }

    /** One try at the ground nearby, a step further from the source. Where it went, or null. */
    @javax.annotation.Nullable
    public static BlockPos spread(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState here = level.getBlockState(pos);
        int depth = here.hasProperty(DEPTH) ? here.getValue(DEPTH) : MAX_DEPTH;
        if (depth >= MAX_DEPTH) return null;
        BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        return corrupt(level, to, depth + 1) ? to : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(level.isDay() ? 14 : 5) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(MIST, pos.getX() + random.nextDouble(), pos.getY() + 1.05 + random.nextDouble() * 0.3, pos.getZ() + random.nextDouble(), 0.0, 0.005, 0.0);
        }
    }
}
