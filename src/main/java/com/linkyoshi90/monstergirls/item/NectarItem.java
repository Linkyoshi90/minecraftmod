package com.linkyoshi90.monstergirls.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Nectar from a flora girl. Poured on a field, it soaks the farmland (as if water were next to it) and gives every
 * crop within two blocks a growth spurt.
 */
public class NectarItem extends Item {
    public static final int RADIUS = 2;

    public NectarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (pour(level, context.getClickedPos()) == 0) return InteractionResult.PASS;
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        level.playSound(null, context.getClickedPos(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
        if (player != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        }
        return InteractionResult.CONSUME;
    }

    /** Waters and feeds the field around a spot; returns how many blocks it touched. */
    public static int pour(ServerLevel level, BlockPos centre) {
        int touched = 0;
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-RADIUS, -1, -RADIUS), centre.offset(RADIUS, 1, RADIUS))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof FarmBlock && s.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                level.setBlock(p, s.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                touched++;
            } else if ((s.getBlock() instanceof CropBlock || s.getBlock() instanceof StemBlock) && s.getBlock() instanceof BonemealableBlock grow
                    && grow.isValidBonemealTarget(level, p, s)) {
                grow.performBonemeal(level, level.random, p, s);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.0);
                touched++;
            }
        }
        return touched;
    }
}
