package com.linkyoshi90.monstergirls.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Throw it and a friend brings it back (FetchBallGoal). It only lands as an item; it can't hurt anything. */
public class ToyBallItem extends Item {
    public ToyBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemEntity ball = new ItemEntity(level, player.getX(), player.getEyeY() - 0.2, player.getZ(), stack.copyWithCount(1));
            ball.setDeltaMovement(player.getLookAngle().scale(1.1).add(0, 0.2, 0));
            ball.setThrower(player);
            ball.setPickUpDelay(40);
            level.addFreshEntity(ball);
            stack.consume(1, player);
            player.playSound(SoundEvents.SNOWBALL_THROW, 0.6F, 1.2F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
