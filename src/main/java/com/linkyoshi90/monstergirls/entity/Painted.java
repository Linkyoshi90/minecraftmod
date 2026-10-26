package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A creature that can wear a Harmontide paint (girls, harpies, anurans; see AbstractMonsterGirl.PAINTS). Copper paint
 * weathers like copper blocks (copper, exposed, weathered, oxidized) unless waxed with honeycomb; an axe scrapes off the
 * wax, then a stage. The seasonal paints are only rolled in their month.
 */
public interface Painted {
    /** About one stage every 40 minutes, checked once a minute. */
    int WEATHER_ODDS = 40;

    int getPaint();

    void setPaint(int paint);

    boolean isWaxed();

    void setWaxed(boolean waxed);

    @Nullable
    default String getPaintName() {
        int p = this.getPaint();
        return p == 0 ? null : AbstractMonsterGirl.PAINTS.get(p - 1);
    }

    /** A paint for a new spawn: never an aged copper, pumpkin only in October, candy cane only in December. */
    static int roll(RandomSource random) {
        int month = java.time.LocalDate.now().getMonthValue();
        List<Integer> ok = new ArrayList<>();
        for (int i = 0; i < AbstractMonsterGirl.PAINTS.size(); i++) {
            String p = AbstractMonsterGirl.PAINTS.get(i);
            if (p.startsWith("copper_") || "pumpkin".equals(p) && month != 10 || "candy_cane".equals(p) && month != 12 || "valentine".equals(p) && month != 2 || "easter".equals(p) && month != 4 || "fireworks".equals(p) && month != 1) continue;
            ok.add(i + 1);
        }
        return ok.get(random.nextInt(ok.size()));
    }

    private static boolean isCopper(@Nullable String paint) {
        return paint != null && paint.startsWith("copper");
    }

    /** Once a minute: unwaxed copper may weather one stage. */
    static void tickCopper(Mob mob, Painted painted) {
        String p = painted.getPaintName();
        if (mob.tickCount % 1200 != 0 || !isCopper(p) || painted.isWaxed() || "copper_oxidized".equals(p) || mob.getRandom().nextInt(WEATHER_ODDS) != 0) return;
        painted.setPaint(painted.getPaint() + 1);
    }

    /** Honeycomb waxes copper paint; an axe takes the wax off, or else scrapes back a stage. Null when it is not that. */
    @Nullable
    static InteractionResult interact(Mob mob, Painted painted, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String p = painted.getPaintName();
        if (!isCopper(p) || !(stack.is(Items.HONEYCOMB) || stack.getItem() instanceof AxeItem)) return null;
        if (stack.is(Items.HONEYCOMB) && painted.isWaxed()) return null;
        if (stack.getItem() instanceof AxeItem && !painted.isWaxed() && "copper".equals(p)) return null;
        if (mob.level() instanceof ServerLevel level) {
            if (stack.is(Items.HONEYCOMB)) {
                painted.setWaxed(true);
                stack.consume(1, player);
                level.levelEvent(3003, mob.blockPosition(), 0);                     // the wax-on sparkle
                mob.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1.0F, 1.0F);
            } else {
                if (painted.isWaxed()) {
                    painted.setWaxed(false);
                    level.sendParticles(ParticleTypes.WAX_OFF, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
                    mob.playSound(SoundEvents.AXE_WAX_OFF, 1.0F, 1.0F);
                } else {
                    painted.setPaint(painted.getPaint() - 1);
                    level.sendParticles(ParticleTypes.SCRAPE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
                    mob.playSound(SoundEvents.AXE_SCRAPE, 1.0F, 1.0F);
                }
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
        return InteractionResult.sidedSuccess(mob.level().isClientSide);
    }
}
