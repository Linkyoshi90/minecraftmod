package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Dullahan head pops (2026-11-13). A dullahan's head sits on her neck by habit more than anything, and when she is startled (a blow,
 * a tease that flusters her, a trip) it pops off and rolls away ({@link #pop}). Headless she stumbles slowly after it, and picks it up
 * again when she reaches it. Catch it first and hand it back (use it on her) and she is very grateful ({@link #THANKS}); keep it, and
 * after {@link #RECALL} ticks it finds its own way home (the one you hold crumbles to nothing). Startled once per {@link #CALM} ticks
 * at most; a blow startles her one time in {@link #HIT_ODDS}. Off with the config option {@code dullahanHeads}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class DullahanHeads {
    public static final int RECALL = 6000, CALM = 1200, HIT_ODDS = 3, THANKS = 5, CHASE = 24;
    public static final String OWNER = "HeadOf", POPPED = "HeadPopped";
    private static final String ITEM = "HeadItem", NEXT = "HeadCalmUntil";

    private DullahanHeads() {}

    public static boolean dullahan(AbstractMonsterGirl girl) {
        return girl.getSpecies() == GirlSpecies.DULLAHAN_UNDEAD && !girl.isBaby();
    }

    public static boolean headless(AbstractMonsterGirl girl) {
        return girl.hasFlag(AbstractMonsterGirl.FLAG_HEADLESS);
    }

    /** Her head pops off and rolls away. The head on the ground, or null if it didn't (not a dullahan, already off, too soon). */
    @Nullable
    public static ItemEntity pop(ServerLevel level, AbstractMonsterGirl girl) {
        if (!HarmontideConfig.dullahanHeads() || !dullahan(girl) || headless(girl) || level.getGameTime() < girl.getPersistentData().getLong(NEXT)) return null;
        ItemStack head = new ItemStack(ModItems.DULLAHAN_HEAD.get());
        CompoundTag tag = new CompoundTag();
        tag.putUUID(OWNER, girl.getUUID());
        tag.putLong(POPPED, level.getGameTime());
        head.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        head.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.dullahan_head.of", girl.getName()).withStyle(s -> s.withItalic(false)));
        ItemEntity e = new ItemEntity(level, girl.getX(), girl.getEyeY(), girl.getZ(), head);
        double a = girl.getRandom().nextDouble() * Math.PI * 2;
        e.setDeltaMovement(Math.cos(a) * 0.35, 0.35, Math.sin(a) * 0.35);
        e.setPickUpDelay(20);
        level.addFreshEntity(e);
        CompoundTag pd = girl.getPersistentData();
        pd.putUUID(ITEM, e.getUUID());
        pd.putLong(POPPED, level.getGameTime());
        pd.putLong(NEXT, level.getGameTime() + CALM);
        girl.setFlag(AbstractMonsterGirl.FLAG_HEADLESS, true);
        girl.playSound(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 1.0F, 1.6F);
        level.sendParticles(ParticleTypes.POOF, girl.getX(), girl.getEyeY(), girl.getZ(), 6, 0.2, 0.2, 0.2, 0.02);
        return e;
    }

    /** Her head is back where it belongs. */
    public static void restore(ServerLevel level, AbstractMonsterGirl girl) {
        girl.setFlag(AbstractMonsterGirl.FLAG_HEADLESS, false);
        girl.getPersistentData().remove(ITEM);
        girl.getPersistentData().remove(POPPED);
        girl.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        girl.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F);
    }

    /** Whose head this is. */
    @Nullable
    public static UUID owner(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
    }

    /** A head held too long has gone home on its own. */
    public static boolean stale(ItemStack stack, long now) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null || now - data.copyTag().getLong(POPPED) >= RECALL;
    }

    /** The player hands her head back. True if it was hers. */
    public static boolean handBack(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack head) {
        if (!headless(girl) || !girl.getUUID().equals(owner(head))) return false;
        head.shrink(1);
        restore(level, girl);
        girl.addAffection(player, THANKS);
        girl.blushFor(200);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.4, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.dullahan.thanks", girl.getDisplayName()), true);
        return true;
    }

    /** A startle: a tease, a trip. */
    public static void startle(ServerLevel level, net.minecraft.world.entity.Mob mob) {
        if (mob instanceof AbstractMonsterGirl g && pop(level, g) != null) {
            Player near = level.getNearestPlayer(g, 12);
            if (near != null) near.displayClientMessage(Component.translatable("message.harmontide.dullahan.pop", g.getDisplayName()), true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.level() instanceof ServerLevel level && dullahan(g) && g.isAlive()
                && g.getRandom().nextInt(HIT_ODDS) == 0) startle(level, g);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl g) || g.tickCount % 10 != 3 || !(g.level() instanceof ServerLevel level) || !headless(g)) return;
        chase(level, g);
    }

    /** Headless: after her head, slowly. True once she has it back. */
    public static boolean chase(ServerLevel level, AbstractMonsterGirl g) {
        CompoundTag pd = g.getPersistentData();
        if (level.getGameTime() - pd.getLong(POPPED) >= RECALL || !pd.hasUUID(ITEM)) {   // it found its own way home (or this is an old save)
            restore(level, g);
            return true;
        }
        g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false));
        if (level.getEntity(pd.getUUID(ITEM)) instanceof ItemEntity head && head.isAlive() && head.distanceToSqr(g) <= CHASE * CHASE) {
            if (head.distanceToSqr(g) <= 2.25) {
                head.discard();
                restore(level, g);
                return true;
            }
            g.getNavigation().moveTo(head.getX(), head.getY(), head.getZ(), 0.9);
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractMonsterGirl g) || event.getHand() != InteractionHand.MAIN_HAND || !event.getItemStack().is(ModItems.DULLAHAN_HEAD.get())) return;
        if (g.level() instanceof ServerLevel level) handBack(level, g, event.getEntity(), event.getItemStack());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(g.level().isClientSide));
    }
}
