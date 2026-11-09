package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Compliments (2026-11-09). Put on something new (a change in what is worn: armour pieces, their trims and dyes) and the first grown
 * friend within {@link #NEAR} who sees it says so: a line, {@link #CLOSER} closer, and a blush from one who would flirt. Each outfit is
 * complimented once; bare is not an outfit.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Compliments {
    public static final double NEAR = 6;
    public static final int CLOSER = 1, LINES = 5;
    static final String OUTFIT = "Outfit", PRAISED = "OutfitsPraised";

    public static final int KEEP = 16;

    private Compliments() {
    }

    static boolean praised(CompoundTag journal, int outfit) {
        for (int o : journal.getIntArray(PRAISED)) if (o == outfit) return true;
        return false;
    }

    /** What the player wears, as one number (0 for nothing at all). */
    public static int outfit(Player p) {
        int h = 0;
        boolean any = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack s = p.getItemBySlot(slot);
            if (s.isEmpty()) continue;
            any = true;
            h = h * 31 + BuiltInRegistries.ITEM.getKey(s.getItem()).hashCode();
            if (s.has(DataComponents.TRIM)) h = h * 31 + s.get(DataComponents.TRIM).hashCode();
            if (s.has(DataComponents.DYED_COLOR)) h = h * 31 + s.get(DataComponents.DYED_COLOR).rgb();
        }
        return any ? (h == 0 ? 1 : h) : 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 60 == 41 && event.getEntity().level() instanceof ServerLevel level) look(level, event.getEntity());
    }

    /** Every few seconds: a new outfit gets its compliment from the first friend near. Who said it, or null. */
    @Nullable
    public static Mob look(ServerLevel level, Player p) {
        if (!com.linkyoshi90.monstergirls.MonsterGirlsConfig.compliments()) return null;
        int now = outfit(p);
        CompoundTag journal = p.getData(ModAttachments.JOURNAL);
        if (now == 0 || praised(journal, now)) return null;
        if (journal.getInt(OUTFIT) != now) {                                  // just changed: remember it, praise comes when someone sees
            journal.putInt(OUTFIT, now);
            p.setData(ModAttachments.JOURNAL, journal);
        }
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(p.blockPosition()).inflate(NEAR),
                m -> m instanceof Bonded b && m.isAlive() && !m.isBaby() && b.getAffection(p) >= HomeLife.FRIEND && m.hasLineOfSight(p))) {
            int[] old = journal.getIntArray(PRAISED);                          // the last few outfits praised: swapping back is no new look
            int[] kept = new int[Math.min(old.length + 1, KEEP)];
            kept[0] = now;
            System.arraycopy(old, 0, kept, 1, kept.length - 1);
            journal.putIntArray(PRAISED, kept);
            p.setData(ModAttachments.JOURNAL, journal);
            Bonded b = (Bonded) m;
            b.addAffection(p, CLOSER);
            boolean sweet = Flirting.canFlirt(m, p);
            if (sweet) b.blushFor(80);
            m.getLookControl().setLookAt(p);
            level.sendParticles(sweet ? ParticleTypes.HEART : ParticleTypes.HAPPY_VILLAGER, m.getX(), m.getEyeY() + 0.3, m.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            p.displayClientMessage(Component.translatable("message.monstergirls.compliment." + (sweet ? "sweet." : "") + m.getRandom().nextInt(LINES), m.getDisplayName()), true);
            return m;
        }
        return null;
    }
}
