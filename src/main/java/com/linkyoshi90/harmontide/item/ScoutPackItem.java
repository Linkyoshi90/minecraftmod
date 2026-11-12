package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.Scouting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

/** Hand it to a friend and she goes scouting (Scouting). It comes back full; right-click unpacks it into your inventory, and it can go out again. */
public class ScoutPackItem extends Item {
    public ScoutPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pack = player.getItemInHand(hand);
        if (!Scouting.hasFinds(pack)) return InteractionResultHolder.pass(pack);
        if (!level.isClientSide) {
            pack.get(DataComponents.CONTAINER).nonEmptyStream().forEach(s -> player.getInventory().placeItemBackInInventory(s.copy()));
            pack.remove(DataComponents.CONTAINER);
            player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(pack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            tooltip.add(Component.translatable("item.harmontide.scout_pack.empty"));
            return;
        }
        contents.nonEmptyStream().limit(6).forEach(s -> tooltip.add(Component.translatable("item.harmontide.scout_pack.line", s.getCount(), s.getHoverName())));
    }
}
