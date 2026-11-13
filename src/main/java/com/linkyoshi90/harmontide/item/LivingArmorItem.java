package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.CursedGear;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Living armour (entity/CursedGear): an iron-grade breastplate with a spirit in it that grows fond of its wearer. */
public class LivingArmorItem extends ArmorItem {
    public LivingArmorItem(Properties properties) {
        super(com.linkyoshi90.harmontide.registry.ModArmorMaterials.CURSED, Type.CHESTPLATE, properties.durability(Type.CHESTPLATE.getDurability(15)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level instanceof ServerLevel server && holder instanceof Player p) CursedGear.carried(server, p, stack, selected);
        else if (level.isClientSide && holder instanceof Player p) CursedGear.aura(level, p, stack, selected);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;                                                        // a dark shimmer: something lives in it
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.harmontide.cursed.fondness", CursedGear.fondness(stack), CursedGear.FOND).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
