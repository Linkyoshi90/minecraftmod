package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;

/** The fertilised egg as an item; the species rides along as the block state it will be placed with. */
public class HarpyEggItem extends BlockItem {
    public HarpyEggItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack of(Block block, HarpyEggBlock.Species species) {
        ItemStack stack = new ItemStack(block);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HarpyEggBlock.SPECIES, species));
        return stack;
    }

    public static HarpyEggBlock.Species speciesOf(ItemStack stack) {
        BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
        return props == null ? HarpyEggBlock.Species.SPARROW : props.get(HarpyEggBlock.SPECIES) == null ? HarpyEggBlock.Species.SPARROW : props.get(HarpyEggBlock.SPECIES);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.monstergirls.harpy_egg.named", Component.translatable("entity.monstergirls." + speciesOf(stack).entityId()));
    }
}
