package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.Journal;
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

/** The Harmontide journal: opened, it is written afresh from what its reader has met and befriended (Journal). With a flower in the other hand, the flower is pressed between its pages instead. */
public class JournalItem extends Item {
    public JournalItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (other.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS)) {            // a flower in the other hand: pressed between the pages
            if (player instanceof ServerPlayer sp) Journal.press(sp, other);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (player instanceof ServerPlayer sp) {
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, Journal.book(sp));
            sp.containerMenu.broadcastChanges();            // the pages must reach her client's copy before the book opens
            sp.openItemGui(stack, hand);                    // vanilla's book screen reads them from the held stack
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.harmontide.harmontide_journal.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
