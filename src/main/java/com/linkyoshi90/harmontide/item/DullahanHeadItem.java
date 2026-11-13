package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.DullahanHeads;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A dullahan's head (entity/DullahanHeads). Use it on her to hand it back; held too long, it finds its own way home. */
public class DullahanHeadItem extends Item {
    public DullahanHeadItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (!level.isClientSide && level.getGameTime() % 20 == 0 && DullahanHeads.stale(stack, level.getGameTime())) stack.setCount(0);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, net.minecraft.world.entity.item.ItemEntity entity) {
        if (!entity.level().isClientSide && entity.tickCount % 20 == 0 && DullahanHeads.stale(stack, entity.level().getGameTime())) entity.discard();
        return false;
    }
}
