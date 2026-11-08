package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * A mailbox (MailboxBlockEntity): a harpy or centaur friend carries what you put in it. A feather on this mailbox and then on
 * another links them both ways; a feather on the same one twice unlinks it (then the mail comes to you).
 */
public class MailboxBlock extends SimpleTickerBlock {
    public static final MapCodec<MailboxBlock> CODEC = simpleCodec(MailboxBlock::new);

    public MailboxBlock(Properties properties) {
        super(properties, Block.box(4, 0, 4, 12, 16, 12));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MailboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.MAILBOX.get(), MailboxBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player p && level.getBlockEntity(pos) instanceof MailboxBlockEntity box) box.setOwner(p.getUUID());
    }

    public static final String LINK = "MailboxLink";

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(net.minecraft.world.item.Items.FEATHER)) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!level.isClientSide) link(level, pos, player);
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A feather: the first mailbox remembered, the second linked to it both ways; the same one twice unlinks it. */
    public static void link(Level level, BlockPos pos, Player player) {
        var data = player.getPersistentData();
        if (!data.contains(LINK)) {
            data.putLong(LINK, pos.asLong());
            player.displayClientMessage(Component.translatable("message.monstergirls.mailbox.link_first"), true);
            return;
        }
        BlockPos first = BlockPos.of(data.getLong(LINK));
        data.remove(LINK);
        if (!(level.getBlockEntity(pos) instanceof MailboxBlockEntity here)) return;
        if (first.equals(pos)) {
            here.setTarget(null);
            player.displayClientMessage(Component.translatable("message.monstergirls.mailbox.unlinked"), true);
            return;
        }
        if (!(level.getBlockEntity(first) instanceof MailboxBlockEntity there)) return;
        here.setTarget(first);
        there.setTarget(pos);
        player.displayClientMessage(Component.translatable("message.monstergirls.mailbox.linked", (int) Math.sqrt(first.distSqr(pos))), true);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MailboxBlockEntity box) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new HopperMenu(id, inv, box.store()), Component.translatable("block.monstergirls.mailbox")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof MailboxBlockEntity box) Containers.dropContents(level, pos, box.store());
        super.onRemove(state, level, pos, next, moved);
    }
}
