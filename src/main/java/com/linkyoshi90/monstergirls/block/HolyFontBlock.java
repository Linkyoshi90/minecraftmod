package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The holy font: a calcite basin of holy water. Within {@link #RADIUS} blocks of one the Demon Realm can't take hold: its grass
 * won't spread or take root, and no creature is possessed, passes it on or is turned there. A village's ward. It is a point of
 * interest (registry/ModVillagers#FONT), so the check is cheap.
 */
public class HolyFontBlock extends Block {
    public static final int RADIUS = 12;

    public HolyFontBlock(Properties properties) {
        super(properties);
    }

    /** A holy font within RADIUS of this spot. */
    public static boolean warded(ServerLevel level, BlockPos at) {
        return level.getPoiManager().findClosest(h -> h.is(ModVillagers.FONT_KEY), at, RADIUS, PoiManager.Occupancy.ANY).isPresent();
    }

    /** What the Order forgives for one gold ingot given at a font. */
    public static final int TITHE = 10;

    /**
     * A tithe: a gold ingot given at the font buys the Order's goodwill ({@link #TITHE} notoriety less). Refused (the ingot
     * kept) while the Order has nothing against the giver. True if it was taken.
     */
    public static boolean tithe(Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack) {
        if (!stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) return false;
        if (com.linkyoshi90.monstergirls.entity.order.Order.notoriety(player) <= 0) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.order.tithe.none"), true);
            return false;
        }
        com.linkyoshi90.monstergirls.entity.order.Order.addNotoriety(player, -TITHE);
        stack.consume(1, player);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.03);
            server.playSound(null, pos, net.minecraft.sounds.SoundEvents.BELL_RESONATE, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.5F);
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.order.tithe",
                com.linkyoshi90.monstergirls.entity.order.Order.notoriety(player)), true);
        com.linkyoshi90.monstergirls.entity.Dairy.award(player, "order_tithe");
        return true;
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (stack.is(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get())) {   // offered at the font by night: the sabbath raid (FallenChapels)
            if (level instanceof net.minecraft.server.level.ServerLevel server && com.linkyoshi90.monstergirls.entity.order.FallenChapels.begin(server, pos, player, true)) {
                stack.consume(1, player);
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!level.isClientSide) tithe(level, pos, player, stack);
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05, pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                    0.0, 0.02, 0.0);
        }
    }
}
