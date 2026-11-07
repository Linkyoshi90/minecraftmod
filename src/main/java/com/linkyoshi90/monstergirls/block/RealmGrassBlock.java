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
 */
public class RealmGrassBlock extends SnowyDirtBlock {
    public static final MapCodec<RealmGrassBlock> CODEC = simpleCodec(RealmGrassBlock::new);
    /** Block light (not daylight) at or above this keeps the realm away. */
    public static final int LIGHT = 8;
    /** One random tick in this many grows a tuft of crimson roots on bare realm grass. */
    public static final int ROOTS = 60;
    public static final DustParticleOptions HAZE = new DustParticleOptions(new Vector3f(0.95F, 0.45F, 0.78F), 1.0F);

    public RealmGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SnowyDirtBlock> codec() {
        return CODEC;
    }

    /** Lit by a lamp or torch: no realm here. */
    public static boolean lit(Level level, BlockPos ground) {
        return level.getBrightness(LightLayer.BLOCK, ground.above()) >= LIGHT;
    }

    private static boolean open(Level level, BlockPos ground) {
        BlockState above = level.getBlockState(ground.above());
        return !above.isSolidRender(level, ground.above()) && above.getFluidState().isEmpty();
    }

    /** Ground the realm can take: grass or dirt with open air above, out of torchlight. */
    public static boolean canTake(Level level, BlockPos ground) {
        BlockState s = level.getBlockState(ground);
        return (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT)) && open(level, ground) && !lit(level, ground);
    }

    /** Turns one block into realm grass. True if it did. */
    public static boolean corrupt(Level level, BlockPos ground) {
        if (!canTake(level, ground)) return false;
        level.setBlock(ground, ModBlocks.REALM_GRASS.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockState tuft = level.getBlockState(ground.above());
        if (tuft.is(Blocks.SHORT_GRASS) || tuft.is(Blocks.FERN)) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_ALL);  // the tufts turn too
        return true;
    }

    /** Back to plain grass (dirt if covered), and a heartfruit bush or weepcap on it withers. */
    public static void purify(Level level, BlockPos ground) {
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
        if (random.nextInt(3) == 0) spread(level, pos, random);
        if (random.nextInt(HeartfruitBushBlock.SPROUT) == 0) HeartfruitBushBlock.sprout(level, pos);
        if (random.nextInt(WeepcapBlock.SPROUT) == 0) WeepcapBlock.sprout(level, pos);
        if (random.nextInt(ROOTS) == 0 && level.isEmptyBlock(pos.above())) level.setBlock(pos.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_ALL);
        if (random.nextInt(3) == 0) RealmLeavesBlock.creep(level, pos, random);
    }

    /** One try at the ground nearby, as grass spreads. Where it went, or null. */
    @javax.annotation.Nullable
    public static BlockPos spread(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        return corrupt(level, to) ? to : null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(HAZE, pos.getX() + random.nextDouble(), pos.getY() + 1.1 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }
}
