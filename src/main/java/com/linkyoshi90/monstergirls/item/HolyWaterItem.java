package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Holy water: poured on the ground it turns Demon Realm grass within {@link #RADIUS} back into grass, and the bottle is left. */
public class HolyWaterItem extends Item {
    public static final int RADIUS = 5, CANOPY = 20;

    public HolyWaterItem(Properties properties) {
        super(properties);
    }

    /** Cleanses the realm around a spot. How many blocks it turned back. */
    public static int cleanse(Level level, BlockPos centre) {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-RADIUS, -2, -RADIUS), centre.offset(RADIUS, 2, RADIUS))) {
            if (p.distSqr(centre) > (RADIUS + 0.5) * (RADIUS + 0.5) || !level.getBlockState(p).is(ModBlocks.REALM_GRASS.get())) continue;
            RealmGrassBlock.purify(level, p.immutable());
            count++;
        }
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-RADIUS, 0, -RADIUS), centre.offset(RADIUS, CANOPY, RADIUS))) {   // and the trees above
            if (level.getBlockState(p).is(ModBlocks.REALM_LEAVES.get())) com.linkyoshi90.monstergirls.block.RealmLeavesBlock.purify(level, p.immutable());
            else if (level.getBlockState(p).is(ModBlocks.REALM_LOG.get())) com.linkyoshi90.monstergirls.block.RealmLogBlock.purify(level, p.immutable());
        }
        return count;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level instanceof ServerLevel server) {
            cleanse(level, pos);
            server.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 40, RADIUS / 2.0, 0.2, RADIUS / 2.0, 0.1);
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 12, RADIUS / 2.0, 0.3, RADIUS / 2.0, 0.02);
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
            Player player = context.getPlayer();
            if (player != null) {
                ItemStack left = ItemUtils.createFilledResult(context.getItemInHand(), player, new ItemStack(Items.GLASS_BOTTLE));
                player.setItemInHand(context.getHand(), left);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
