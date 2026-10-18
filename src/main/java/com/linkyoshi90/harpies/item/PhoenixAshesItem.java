package com.linkyoshi90.harpies.item;

import com.linkyoshi90.harpies.entity.PhoenixHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a slain phoenix leaves behind. Drop the ashes in or near a fire (within two blocks of fire, soul fire, a lit
 * campfire, lava or magma) and after ten seconds of warming they burst into flame and a phoenix chick hatches from them.
 * The ashes themselves don't burn.
 */
public class PhoenixAshesItem extends Item {
    public static final int WARMING_TICKS = 200;
    private static final String WARMTH = "harpies_phoenix_warmth";

    public PhoenixAshesItem(Properties properties) {
        super(properties.fireResistant());
    }

    public static boolean isFireSource(BlockState state) {
        return state.is(BlockTags.FIRE) || state.getFluidState().is(FluidTags.LAVA) || state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
                || (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT));
    }

    private static boolean nearFire(Level level, BlockPos pos) {
        // Two blocks of reach: a dropped item bounces a little, and "by the fire" shouldn't need pixel precision.
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (isFireSource(level.getBlockState(p))) return true;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        var data = entity.getPersistentData();
        if (!nearFire(level, entity.blockPosition())) {
            data.putInt(WARMTH, 0);
            return false;
        }
        int warmth = data.getInt(WARMTH) + 1;
        data.putInt(WARMTH, warmth);
        if (warmth % 10 == 0) level.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY() + 0.2, entity.getZ(), 3, 0.1, 0.1, 0.1, 0.01);
        if (warmth >= WARMING_TICKS) {
            PhoenixHarpy.hatchFromAshes(level, entity.position());
            stack.shrink(1);
            if (stack.isEmpty()) entity.discard();
            else data.putInt(WARMTH, 0);
            return true;
        }
        return false;
    }
}
