package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

/** An arachne's silk-wrapped present: use it to tear it open (you keep a little of the silk). */
public class CocoonItem extends Item {
    public CocoonItem(Properties properties) {
        super(properties);
    }

    /** A cocoon with this present wrapped inside. */
    public static ItemStack wrapping(ItemStack inside) {
        ItemStack cocoon = new ItemStack(ModItems.SILK_COCOON.get());
        cocoon.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(inside)));
        return cocoon;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack cocoon = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemContainerContents contents = cocoon.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for (ItemStack s : contents.nonEmptyItemsCopy()) {
                if (!player.getInventory().add(s)) player.drop(s, false);
            }
            ItemStack silk = new ItemStack(Items.STRING, 2);
            if (!player.getInventory().add(silk)) player.drop(silk, false);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 1.0F, 1.3F);
            cocoon.consume(1, player);
        }
        return InteractionResultHolder.sidedSuccess(cocoon, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.monstergirls.silk_cocoon.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
