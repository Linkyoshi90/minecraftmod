package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.CursedGear;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A cursed blade (entity/CursedGear): an iron-grade sword with a spirit in it that grows fond of its bearer. */
public class CursedBladeItem extends SwordItem {
    public CursedBladeItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level instanceof ServerLevel server && holder instanceof Player p) CursedGear.carried(server, p, stack, selected);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.harmontide.cursed.fondness", CursedGear.fondness(stack), CursedGear.FOND).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
