package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Bonded;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Cravings (2026-11-09). A girl who is expecting soon wants one food in particular, and tells the first friend who comes near
 * ({@link #TELL}): melon, pickled... well, glow berries. Bring it to her before her time and she is overjoyed ({@link #FED} closer),
 * and the craving answered brings twins: she bears twice over (a second little one, or a second clutch of eggs). The craving is
 * picked when she first feels it and forgotten at the birth. Girls only (harpies and frogs lay too soon to crave anything).
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Cravings {
    public static final double TELL = 6;
    public static final int FED = 5;
    static final String CRAVING = "Craving", TOLD = "CravingTold", ANSWERED = "CravingAnswered";
    public static final List<Item> FOODS = List.of(Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.HONEY_BOTTLE, Items.PUMPKIN_PIE,
            Items.COOKIE, Items.DRIED_KELP, Items.BAKED_POTATO, Items.GOLDEN_CARROT, Items.COOKED_SALMON, Items.RABBIT_STEW, Items.BEETROOT_SOUP,
            Items.MUSHROOM_STEW, Items.APPLE, Items.CHORUS_FRUIT, Items.COOKED_COD, Items.BREAD, Items.SUSPICIOUS_STEW);

    private Cravings() {
    }

    /** What she craves now, or null when she isn't expecting. Picks one the first time it is asked. */
    @Nullable
    public static Item craving(AbstractMonsterGirl girl) {
        if (!girl.getGestation().isCarrying()) return null;
        CompoundTag pd = girl.getPersistentData();
        if (!pd.contains(CRAVING)) pd.putString(CRAVING, BuiltInRegistries.ITEM.getKey(FOODS.get(girl.getRandom().nextInt(FOODS.size()))).toString());
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(pd.getString(CRAVING)));
    }

    public static boolean answered(AbstractMonsterGirl girl) {
        return girl.getPersistentData().getBoolean(ANSWERED);
    }

    /** Every few seconds from her tick: she tells the first friend near what she craves. Who she told, or null. */
    @Nullable
    public static Player tick(ServerLevel level, AbstractMonsterGirl girl) {
        Item want = craving(girl);
        CompoundTag pd = girl.getPersistentData();
        if (want == null || pd.getBoolean(TOLD) || answered(girl)) return null;
        Player friend = level.getNearestPlayer(girl.getX(), girl.getY(), girl.getZ(), TELL, p -> !p.isSpectator() && p instanceof Player pl && girl.getAffection(pl) >= Bonded.BED_AFFECTION);
        if (friend == null) return null;
        pd.putBoolean(TOLD, true);
        friend.displayClientMessage(Component.translatable("message.monstergirls.craving.tell." + girl.getRandom().nextInt(3), girl.getDisplayName(), new ItemStack(want).getHoverName()), false);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        return friend;
    }

    /** The craved food is offered. True if it was what she wanted. */
    public static boolean feed(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack stack) {
        Item want = craving(girl);
        if (want == null || answered(girl) || !stack.is(want)) return false;
        girl.getPersistentData().putBoolean(ANSWERED, true);
        stack.consume(1, player);
        girl.addAffection(player, FED);
        girl.blushFor(200);
        girl.heal(4.0F);
        girl.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.1F);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.craving.fed." + girl.getRandom().nextInt(3), girl.getDisplayName()), false);
        return true;
    }

    /** At the birth: whether she bears twice (the craving was answered). Forgets this time's craving either way. */
    public static boolean twins(AbstractMonsterGirl girl) {
        CompoundTag pd = girl.getPersistentData();
        boolean twins = pd.getBoolean(ANSWERED);
        pd.remove(CRAVING);
        pd.remove(TOLD);
        pd.remove(ANSWERED);
        return twins;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || event.getItemStack().isEmpty() || !girl.getGestation().isCarrying()) return;
        // the client can't know her craving: the server decides, and only then is the rest of the interaction skipped
        if (!(event.getLevel() instanceof ServerLevel level) || !feed(level, girl, event.getEntity(), event.getItemStack())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
