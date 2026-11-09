package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import java.util.Map;
import javax.annotation.Nullable;

/**
 * Upbringing (2026-11-09): toys for a little one, and what she'll be when she grows up. A wooden hoe makes a farmhand likelier,
 * a bowl a cook, a wooden sword a guard and a compass a scout: each toy adds {@link #WEIGHT} to that calling's odds in
 * {@link Roles#weights} (at most {@link #MOST} toys a calling), and she is {@link #CLOSER} closer for the present. One toy a day.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Upbringing {
    public static final int WEIGHT = 6, MOST = 3, CLOSER = 3;
    public static final Map<Item, Roles.Role> TOYS = Map.of(Items.WOODEN_HOE, Roles.Role.FARMHAND, Items.BOWL, Roles.Role.COOK,
            Items.WOODEN_SWORD, Roles.Role.GUARD, Items.COMPASS, Roles.Role.SCOUT);
    static final String KEY = "Upbringing", LAST = "UpbringingDay";

    public enum Answer { LOVES_IT, ENOUGH, TOMORROW }

    private Upbringing() {
    }

    /** How many toys of this calling she has had. */
    public static int toys(AbstractMonsterGirl girl, Roles.Role role) {
        return girl.getPersistentData().getCompound(KEY).getInt(role.name());
    }

    /** Extra weight for each calling from her toys, in Role order. */
    public static int[] extra(AbstractMonsterGirl girl) {
        int[] w = new int[Roles.Role.values().length];
        for (Roles.Role r : Roles.Role.values()) w[r.ordinal()] = toys(girl, r) * WEIGHT;
        return w;
    }

    /** A toy is handed over. Null if it isn't a toy or she is grown. */
    @Nullable
    public static Answer give(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack stack) {
        Roles.Role role = TOYS.get(stack.getItem());
        if (role == null || !girl.isBaby()) return null;
        CompoundTag pd = girl.getPersistentData();
        long today = level.getDayTime() / 24000L;
        if (pd.contains(LAST) && pd.getLong(LAST) == today) {
            player.displayClientMessage(Component.translatable("message.monstergirls.upbringing.tomorrow", girl.getDisplayName()), true);
            return Answer.TOMORROW;
        }
        if (toys(girl, role) >= MOST) {
            player.displayClientMessage(Component.translatable("message.monstergirls.upbringing.enough", girl.getDisplayName()), true);
            return Answer.ENOUGH;
        }
        CompoundTag toys = pd.getCompound(KEY);
        toys.putInt(role.name(), toys.getInt(role.name()) + 1);
        pd.put(KEY, toys);
        pd.putLong(LAST, today);
        stack.consume(1, player);
        girl.addAffection(player, CLOSER);
        girl.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.4F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.upbringing." + role.name().toLowerCase(java.util.Locale.ROOT), girl.getDisplayName()), false);
        return Answer.LOVES_IT;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || !girl.isBaby() || !TOYS.containsKey(event.getItemStack().getItem())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level) give(level, girl, event.getEntity(), event.getItemStack());
    }
}
