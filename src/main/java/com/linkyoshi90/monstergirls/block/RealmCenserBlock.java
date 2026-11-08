package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The realm censer: a lantern of dark plum iron. Fed a heartfruit it burns with the realm's violet flame for a day
 * ({@link #BURN} quarters of {@link #QUARTER} ticks), and while it burns the girls within {@link #RADIUS} blocks feel as if they
 * stood in the Demon Realm (entity/DemonRealm#realmAir): they flirt more, miss you more and join the sabbath. It never turns
 * the ground and never changes anyone. Lit censers are a point of interest (registry/ModVillagers#CENSER), so they are found fast.
 */
public class RealmCenserBlock extends LanternBlock {
    public static final MapCodec<RealmCenserBlock> CODEC = simpleCodec(RealmCenserBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty BURN = IntegerProperty.create("burn", 0, 3);
    public static final int QUARTER = 6000, RADIUS = 8;

    public RealmCenserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(BURN, 0));
    }

    @Override
    public MapCodec<LanternBlock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, BURN);
    }

    /** A heartfruit lights it (or tops it up) for a whole day. True if it took the fruit. */
    public static boolean light(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof RealmCenserBlock) || (state.getValue(LIT) && state.getValue(BURN) == 3)) return false;
        level.setBlock(pos, state.setValue(LIT, true).setValue(BURN, 3), Block.UPDATE_ALL);
        level.scheduleTick(pos, state.getBlock(), QUARTER);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 1.6F);
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.HEARTFRUIT.get())) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!level.isClientSide && light(level, pos, state)) {
            stack.consume(1, player);
            com.linkyoshi90.monstergirls.entity.Dairy.award(player, "realm_censer");
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A quarter of the day burns away; the last one puts it out. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (state.getValue(BURN) > 0) {
            level.setBlock(pos, state.setValue(BURN, state.getValue(BURN) - 1), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, QUARTER);
        } else {
            level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.8F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (random.nextInt(3) == 0) {
            level.addParticle(RealmGrassBlock.HAZE, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4, pos.getY() + 0.7, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.02, 0.0);
        }
        if (random.nextInt(8) == 0) level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
    }
}
