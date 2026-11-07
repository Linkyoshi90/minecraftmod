package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Festivals;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * The wardrobe: clothes a friend is given go into her wardrobe, and she dresses for the moment by herself, over her own clothes
 * (client/OutfitLayer draws them fitted to her torso, so a lamia's tail or a centaur's flanks never get in the way):
 * a raincoat in the rain, a winter coat in the cold, a yukata on a festival day, a maid's apron at home by her hearth ({@link #HOME}).
 * The first that fits, in that order. Only grown girls, given by a friend. Her clothes are hers to keep.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Wardrobe {
    public static final double HOME = 24;
    public static final float COLD = 0.15F;
    public static final int EVERY = 39;
    private static final String OWNED = "Wardrobe";

    public enum Outfit {
        RAINCOAT(ModItems.RAINCOAT), WINTER_COAT(ModItems.WINTER_COAT), YUKATA(ModItems.YUKATA), MAID_APRON(ModItems.MAID_APRON);

        private final Supplier<? extends Item> item;

        Outfit(Supplier<? extends Item> item) {
            this.item = item;
        }

        public Item item() {
            return this.item.get();
        }

        public String key() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }

        @Nullable
        public static Outfit of(ItemStack stack) {
            for (Outfit o : values()) if (stack.is(o.item())) return o;
            return null;
        }
    }

    private Wardrobe() {
    }

    public static boolean owns(AbstractMonsterGirl girl, Outfit o) {
        return (girl.getPersistentData().getInt(OWNED) & (1 << o.ordinal())) != 0;
    }

    /** Into her wardrobe. False if she already has one. */
    public static boolean give(AbstractMonsterGirl girl, Outfit o) {
        if (owns(girl, o)) return false;
        girl.getPersistentData().putInt(OWNED, girl.getPersistentData().getInt(OWNED) | (1 << o.ordinal()));
        return true;
    }

    /** What to wear: the first outfit she owns whose moment it is, or null. */
    @Nullable
    public static Outfit choose(AbstractMonsterGirl girl, boolean rain, boolean cold, boolean festival, boolean home) {
        boolean[] when = {rain, cold, festival, home};
        for (Outfit o : Outfit.values()) if (when[o.ordinal()] && owns(girl, o)) return o;
        return null;
    }

    public static boolean atHome(ServerLevel level, AbstractMonsterGirl girl) {
        for (BlockPos h : HearthBlockEntity.loaded(level)) if (girl.distanceToSqr(h.getCenter()) <= HOME * HOME) return true;
        return false;
    }

    /** Dresses her for where she is now. What she put on, or null. */
    @Nullable
    public static Outfit dress(ServerLevel level, AbstractMonsterGirl girl) {
        if (girl.isBaby() || girl.getPersistentData().getInt(OWNED) == 0) {
            if (girl.getOutfit() != 0) girl.setOutfit(0);
            return null;
        }
        BlockPos at = girl.blockPosition();
        Outfit o = choose(girl, level.isRainingAt(at.above()) || level.isRainingAt(at), level.getBiome(at).value().getBaseTemperature() < COLD,
                Festivals.on(level.getDayTime()) != null, atHome(level, girl));
        int code = o == null ? 0 : o.ordinal() + 1;
        if (girl.getOutfit() != code) girl.setOutfit(code);
        return o;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % EVERY == 0 && event.getEntity() instanceof AbstractMonsterGirl girl && girl.level() instanceof ServerLevel level) dress(level, girl);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Outfit o = Outfit.of(event.getItemStack());
        if (o == null || !(event.getTarget() instanceof AbstractMonsterGirl girl) || girl.isBaby() || girl.getAffection(event.getEntity()) < HomeLife.FRIEND) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Component name = Component.translatable(o.item().getDescriptionId());
        if (!give(girl, o)) {
            event.getEntity().displayClientMessage(Component.translatable("message.monstergirls.wardrobe.has", girl.getDisplayName(), name), true);
            return;
        }
        event.getItemStack().consume(1, event.getEntity());
        girl.addAffection(event.getEntity(), 2);
        girl.blushFor(80);
        girl.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
        event.getEntity().displayClientMessage(Component.translatable("message.monstergirls.wardrobe.given", girl.getDisplayName(), name), true);
        dress(level, girl);
    }
}
