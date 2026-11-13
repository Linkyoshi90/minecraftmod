package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.SeaWeddings;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Mermaid blood (entity/SeaWeddings): a merfolk friend's once-in-a-life gift. Drunk, a lasting extra heart. */
public class MermaidBloodItem extends Item {
    public MermaidBloodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (!level.isClientSide && drinker instanceof Player player && !SeaWeddings.drink(player)) return stack;   // only once: the vial is kept
        return drinker instanceof Player player ? ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)) : stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
