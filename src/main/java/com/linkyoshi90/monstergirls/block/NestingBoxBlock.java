package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nullable;

/**
 * A nesting box: a low wooden box of hay. An egg a creature lays (chickens, hens, harpies: anything with the c:eggs tag that drops
 * beside a mob, not from a player) within {@link #REACH} blocks of one goes into it instead of rolling about on the ground. An empty
 * hand takes the eggs out; hoppers can too.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public class NestingBoxBlock extends BaseEntityBlock {
    public static final MapCodec<NestingBoxBlock> CODEC = simpleCodec(NestingBoxBlock::new);
    public static final BooleanProperty EGGS = BooleanProperty.create("eggs");
    public static final int REACH = 4;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 7, 15);

    public NestingBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EGGS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EGGS);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NestingBoxBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof NestingBoxBlockEntity box)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        java.util.List<ItemStack> eggs = box.takeAll();
        if (eggs.isEmpty()) return InteractionResult.PASS;
        for (ItemStack s : eggs) if (!player.getInventory().add(s)) player.drop(s, false);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NestingBoxBlockEntity box) Containers.dropContents(level, pos, box.eggs());
        super.onRemove(state, level, pos, newState, moved);
    }

    /** Whether this item, just dropped here, is an egg a creature laid (not one a player threw or dropped). */
    public static boolean laid(ItemEntity item) {
        if (item.getOwner() != null || !item.getItem().is(net.neoforged.neoforge.common.Tags.Items.EGGS)) return false;
        return !item.level().getEntitiesOfClass(Mob.class, item.getBoundingBox().inflate(1.5), Mob::isAlive).isEmpty();
    }

    /** The nearest nesting box with room within reach, or null. */
    @Nullable
    public static NestingBoxBlockEntity nearest(Level level, BlockPos at, ItemStack egg) {
        NestingBoxBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-REACH, -2, -REACH), at.offset(REACH, 2, REACH))) {
            if (!(level.getBlockEntity(p) instanceof NestingBoxBlockEntity box) || !box.eggs().canAddItem(egg)) continue;
            double d = p.distSqr(at);
            if (d < bestDist) {
                bestDist = d;
                best = box;
            }
        }
        return best;
    }

    /** A laid egg near a nesting box goes into it. */
    @SubscribeEvent
    public static void onEggLaid(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ItemEntity item) || event.loadedFromDisk() || !laid(item)) return;
        NestingBoxBlockEntity box = nearest(level, item.blockPosition(), item.getItem());
        if (box == null) return;
        ItemStack left = box.lay(item.getItem().copy());
        BlockPos b = box.getBlockPos();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, b.getX() + 0.5, b.getY() + 0.6, b.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0);
        if (left.isEmpty()) {
            event.setCanceled(true);
            item.discard();                                             // never joins the world: nothing left to pick up
        }
        else item.setItem(left);
    }
}
