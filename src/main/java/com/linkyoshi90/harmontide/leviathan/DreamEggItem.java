package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** The egg the Dream-Leviathan gives with her blessing: set it down and her calf (leviathan/DreamCalf) hatches, yours. */
public class DreamEggItem extends Item {
    public DreamEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level) || ctx.getPlayer() == null) return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
        BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
        DreamCalf calf = LeviathanRegistry.DREAM_CALF.get().create(level);
        if (calf == null) return InteractionResult.FAIL;
        calf.moveTo(at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5, ctx.getPlayer().getYRot() + 180, 0);
        calf.setOwner(ctx.getPlayer().getUUID());
        calf.setGrowth(0);
        level.addFreshEntity(calf);
        level.sendParticles(ParticleTypes.END_ROD, calf.getX(), calf.getY() + 0.3, calf.getZ(), 30, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, at, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.NEUTRAL, 1.0F, 1.2F);
        ItemStack stack = ctx.getItemInHand();
        stack.consume(1, ctx.getPlayer());
        ctx.getPlayer().displayClientMessage(Component.translatable("message.harmontide.dream_calf.hatched").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tip.harmontide.dream_leviathan_egg").withStyle(ChatFormatting.GRAY));
    }
}
