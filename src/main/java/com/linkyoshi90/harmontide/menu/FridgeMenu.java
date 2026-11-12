package com.linkyoshi90.harmontide.menu;

import com.linkyoshi90.harmontide.block.FridgeBlockEntity;
import com.linkyoshi90.harmontide.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A three-row chest whose own slots only take food: the fridge's (FridgeBlockEntity). Drawn by vanilla's chest screen. */
public class FridgeMenu extends ChestMenu {
    public FridgeMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(FridgeBlockEntity.SLOTS));
    }

    public FridgeMenu(int id, Inventory inventory, Container fridge) {
        super(ModMenus.FRIDGE.get(), id, inventory, fridge, 3);
        for (int i = 0; i < FridgeBlockEntity.SLOTS; i++) {
            Slot old = this.slots.get(i);
            Slot food = new Slot(fridge, old.getContainerSlot(), old.x, old.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FridgeBlockEntity.isFood(stack);
                }
            };
            food.index = i;
            this.slots.set(i, food);
        }
    }
}
