package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;

/**
 * The dragon egg (2026-11-09). The egg the Ender Dragon leaves is not dead stone: crouch and warm it with a blaze rod while a dragon
 * girl who counts you a friend is within {@link #NEAR}, and it hatches into a little ender dragon girl, devoted to you from the first
 * breath. Without a dragon friend there it only grows warm (the rod is kept). The egg is used up: there is one in a world.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class DragonEgg {
    public static final double NEAR = 8;

    private DragonEgg() {
    }

    /** A dragon friend of theirs near the egg, or null. */
    @Nullable
    public static AbstractMonsterGirl midwife(ServerLevel level, BlockPos egg, Player player) {
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(egg).inflate(NEAR),
                g -> g.isAlive() && !g.isBaby() && g.getSpecies().family() == GirlSpecies.Family.DRAGON && g.getAffection(player) >= HomeLife.FRIEND)) {
            return g;
        }
        return null;
    }

    /** Warming the egg. The hatchling, or null if it only grew warm. */
    @Nullable
    public static AbstractMonsterGirl warm(ServerLevel level, BlockPos egg, Player player) {
        if (!level.getBlockState(egg).is(Blocks.DRAGON_EGG)) return null;
        level.sendParticles(ParticleTypes.FLAME, egg.getX() + 0.5, egg.getY() + 0.6, egg.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.01);
        AbstractMonsterGirl dragon = midwife(level, egg, player);
        if (dragon == null) {
            player.displayClientMessage(Component.translatable("message.harmontide.dragon_egg.warm"), true);
            return null;
        }
        level.removeBlock(egg, false);
        AbstractMonsterGirl young = AbstractMonsterGirl.youngOf(level, ModGirls.type(GirlSpecies.ENDER_DRAGON), egg, player.getUUID());
        level.playSound(null, egg, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 0.6F, 1.8F);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, egg.getX() + 0.5, egg.getY() + 0.5, egg.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.02);
        level.sendParticles(ParticleTypes.HEART, dragon.getX(), dragon.getEyeY() + 0.3, dragon.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.dragon_egg.hatched", dragon.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        com.linkyoshi90.harmontide.entity.Dairy.award(player, "dragon_egg");
        return young;
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        Player p = event.getEntity();
        if (!p.isShiftKeyDown() || !event.getItemStack().is(Items.BLAZE_ROD) || !event.getLevel().getBlockState(event.getPos()).is(Blocks.DRAGON_EGG)) return;
        event.setCanceled(true);                                              // and the egg doesn't teleport away
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level) warm(level, event.getPos(), p);
    }
}
