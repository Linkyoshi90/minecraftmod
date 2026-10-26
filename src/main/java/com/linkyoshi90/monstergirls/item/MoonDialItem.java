package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.BedSharing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A clock for the moon: its face shows tonight's phase (the "monstergirls:phase" item property, see MonsterGirlsClient),
 * and using it tells you how many nights are left until the full moon, the night the girls who love you all come at
 * once (see {@link BedSharing}).
 */
public class MoonDialItem extends Item {
    public MoonDialItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            int phase = level.getMoonPhase();              // the phase changes at dawn, so by day this is tonight's moon
            Component name = Component.translatable("moon_phase.monstergirls." + phase);
            player.displayClientMessage(phase == 0 ? Component.translatable("message.monstergirls.moon_dial.full")
                    : Component.translatable("message.monstergirls.moon_dial.until", name, 8 - phase), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
