package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * The Harmontide hearth: the heart of a settlement. The befriended girls of whoever placed it who roam free nearby
 * settle here, come home at night and, after a day, each build a little hut close by. Once three or more live here,
 * every morning each puts a present into the hearth's store: right-click to take what the market brought.
 */
public class HearthBlock extends BaseEntityBlock {
    public static final MapCodec<HearthBlock> CODEC = simpleCodec(HearthBlock::new);

    public HearthBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HearthBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.HEARTH.get(), HearthBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player p && level.getBlockEntity(pos) instanceof HearthBlockEntity h) h.setOwner(p.getUUID());
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (stack.is(net.minecraft.world.item.Items.PAINTING) && !stack.has(net.minecraft.core.component.DataComponents.ENTITY_DATA)
                && level instanceof net.minecraft.server.level.ServerLevel server && com.linkyoshi90.harmontide.entity.Marriage.familyPortrait(server, pos, player, stack)) {
            return net.minecraft.world.ItemInteractionResult.SUCCESS;              // a portrait of the family gathered here
        }
        // a name tag names the place (its owner only)
        if (!stack.is(net.minecraft.world.item.Items.NAME_TAG) || !stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)
                || !(level.getBlockEntity(pos) instanceof HearthBlockEntity h) || !player.getUUID().equals(h.owner())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            h.setPlaceName(stack.getHoverName().getString());
            stack.consume(1, player);
            player.displayClientMessage(Component.translatable("message.harmontide.hamlet.renamed", h.placeName()), true);
        }
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HearthBlockEntity h)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer sp) {
            Component place = com.linkyoshi90.harmontide.entity.girl.Hamlet.describe(h);
            sp.displayClientMessage(place != null ? place : Component.translatable("message.harmontide.hearth", h.residentCount()), true);
            sp.openMenu(new SimpleMenuProvider((id, inv, pl) -> ChestMenu.threeRows(id, inv, h.store()), Component.translatable("block.harmontide.hearth")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HearthBlockEntity h) Containers.dropContents(level, pos, h.store());
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.04, 0.0);
        }
        if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.0, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.01, 0);
    }
}
