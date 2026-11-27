package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A dream horn, made from a goat horn and dream scales: blown at night under the open sky in the overworld, it calls the
 * Dream-Leviathan back, sick again, to land near you (leviathan/LeviathanEvent#summon). It is not used up.
 */
public class DreamHornItem extends Item {
    public static final int COOLDOWN = 200;

    public DreamHornItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(6).value(), SoundSource.PLAYERS, 6.0F, 0.6F);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            String no = LeviathanEvent.get(server).summon(server, sp);
            sp.displayClientMessage(Component.translatable(no == null ? "message.harmontide.leviathan.horn_answered" : no)
                    .withStyle(no == null ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY), false);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tip.harmontide.dream_horn").withStyle(ChatFormatting.GRAY));
    }
}
