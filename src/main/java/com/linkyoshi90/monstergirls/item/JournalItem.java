package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.Journal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The Harmontide journal: opened, it is written afresh from what its reader has met and befriended (Journal). */
public class JournalItem extends Item {
    public JournalItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, Journal.book(sp));
            sp.openItemGui(stack, hand);                    // vanilla's book screen reads the pages we just wrote
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.monstergirls.harmontide_journal.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
