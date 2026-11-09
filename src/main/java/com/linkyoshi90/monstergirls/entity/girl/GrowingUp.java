package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

/**
 * Growing up (2026-11-09): the moments a parent remembers. A little one born to a player has three, each once, when she is far
 * enough along ({@link #AT}, quarters of {@link AbstractMonsterGirl#GROW_TICKS}) and her parent is within {@link #NEAR}: her first
 * word (their name), a flower she picked for them, and a drawing of the two of them (a paper to keep). Each makes her
 * {@link #CLOSER} closer, and none is missed: a moment waits until her parent is near.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = com.linkyoshi90.monstergirls.MonsterGirlsMod.MODID)
public final class GrowingUp {
    public static final float[] AT = {0.25F, 0.5F, 0.75F};
    public static final String[] MOMENTS = {"word", "flower", "drawing"};
    public static final double NEAR = 16;
    public static final int CLOSER = 2;
    static final String DONE = "GrowingUp";

    private GrowingUp() {
    }

    public static int done(net.minecraft.world.entity.Mob girl) {
        return girl.getPersistentData().getInt(DONE);
    }

    /** How far along to grown she is, 0 to 1. */
    public static float along(net.minecraft.world.entity.Mob girl) {
        return Math.min(1.0F, (float) ((com.linkyoshi90.monstergirls.entity.Bonded) girl).getGrowTicks() / AbstractMonsterGirl.GROW_TICKS);
    }

    /** Every few seconds for a little one: the next moment, if it's time and her parent is near. Which moment it was, or null. */
    @Nullable
    public static String tick(ServerLevel level, net.minecraft.world.entity.Mob girl) {
        if (!(girl instanceof com.linkyoshi90.monstergirls.entity.Bonded b)) return null;
        int n = done(girl);
        if (!girl.isBaby() || n >= AT.length || along(girl) < AT[n] || b.getFather() == null) return null;
        Player parent = level.getPlayerByUUID(b.getFather());
        if (parent == null || parent.isSpectator() || parent.distanceToSqr(girl) > NEAR * NEAR) return null;
        girl.getPersistentData().putInt(DONE, n + 1);
        String moment = MOMENTS[n];
        b.addAffection(parent, CLOSER);
        girl.getLookControl().setLookAt(parent);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        girl.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.6F);
        ItemStack gift = switch (moment) {
            case "flower" -> new ItemStack(girl.getRandom().nextBoolean() ? Items.DANDELION : Items.POPPY);
            case "drawing" -> drawing(girl, parent);
            default -> ItemStack.EMPTY;
        };
        if (!gift.isEmpty()) level.addFreshEntity(new ItemEntity(level, girl.getX(), girl.getY() + 0.5, girl.getZ(), gift));
        parent.displayClientMessage(Component.translatable("message.monstergirls.growing_up." + moment, girl.getDisplayName(), parent.getDisplayName()), false);
        return moment;
    }

    /** Harpy chicks and frog little ones have the same moments (girls tick theirs from AbstractMonsterGirl), and nap by a cradle too. */
    @net.neoforged.bus.api.SubscribeEvent
    public static void onTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.Mob m) || m instanceof AbstractMonsterGirl || !(m instanceof com.linkyoshi90.monstergirls.entity.Bonded)
                || !m.isBaby() || !(m.level() instanceof ServerLevel level)) return;
        if (m.tickCount % 60 == 37) tick(level, m);
        if (m.tickCount % Cradle.EVERY == 55 && !(m instanceof com.linkyoshi90.monstergirls.entity.AbstractAnuran)) Cradle.tick(level, m, level.isNight());   // tadpoles stay in the water
    }

    /** A paper with her name on it: "a drawing of the two of you". */
    static ItemStack drawing(net.minecraft.world.entity.Mob girl, Player parent) {
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.drawing", girl.getName()));
        paper.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(java.util.List.of(
                Component.translatable("item.monstergirls.drawing.lore", parent.getName()))));
        return paper;
    }
}
