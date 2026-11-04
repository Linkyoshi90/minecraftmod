package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Missing;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The tar-shroom: a black, sticky mushroom of swamps and dark forests that grows where a mushroom would (or on mud in any light).
 * Its sweet, heavy spores make friends near it long for the players they are close to: now and then each grown friend within
 * RANGE misses them five percent more, even with the player standing right there (Missing#addLonging). It spreads slowly, as
 * a mushroom does.
 */
public class TarShroomBlock extends BushBlock {
    public static final MapCodec<TarShroomBlock> CODEC = simpleCodec(TarShroomBlock::new);
    public static final double RANGE = 8;
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);

    public TarShroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender(level, pos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.is(BlockTags.MUSHROOM_GROW_BLOCK) || ground.is(Blocks.MUD) || ground.is(Blocks.MUDDY_MANGROVE_ROOTS)) return true;
        return level.getRawBrightness(pos, 0) < 13 && this.mayPlaceOn(ground, level, below);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()) breathe(level, pos);
        if (random.nextInt(25) == 0) spread(state, level, pos, random);
    }

    /** As a mushroom spreads: to a free spot nearby where it survives, unless five already grow within four blocks. Where, or null. */
    @javax.annotation.Nullable
    public static BlockPos spread(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int crowd = 5;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(state.getBlock()) && --crowd <= 0) return null;
        }
        BlockPos to = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        for (int i = 0; i < 4; i++) {
            if (level.isEmptyBlock(to) && state.canSurvive(level, to)) pos = to;
            to = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }
        if (!level.isEmptyBlock(to) || !state.canSurvive(level, to)) return null;
        level.setBlock(to, state, Block.UPDATE_CLIENTS);
        return to;
    }

    /** One puff of spores: every grown friend in range longs a little more for each player she is close to. How many longed. */
    public static int breathe(ServerLevel level, BlockPos pos) {
        int longed = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(RANGE), m -> m instanceof Bonded && Missing.canLong(m))) {
            boolean any = false;
            for (Player player : level.players()) any |= Missing.addLonging(mob, player, Missing.SHROOM);
            if (any) {
                longed++;
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mob.getX(), mob.getEyeY(), mob.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            }
        }
        return longed;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.5, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.01, 0.0);
        }
    }
}
