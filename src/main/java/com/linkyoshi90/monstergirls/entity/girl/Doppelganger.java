package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * The doppelganger (2026-11-11, after Monster Musume's shapeshifter and the changelings of fey lore): an elf who wears other
 * faces. Her own model, animations and size stay; only the look she shows is borrowed (client/GirlRenderer draws the borrowed
 * kind's model and texture over her own animations).
 * <ul>
 *   <li>Wild, every {@link #SHIFT_EVERY} ticks she takes the look of the nearest girl within {@link #SEE} she can copy, or her own.</li>
 *   <li>Befriended, she wears the look of the companion her friend is fondest of within {@link #FRIENDS} (one she can copy).</li>
 *   <li>Her friend crouching with an empty hand asks for her true face, or for the borrowed one back.</li>
 * </ul>
 * She copies only the kinds that share the biped girl's frame ({@link #SHAPES}; not the odd few rigs of the undead), never
 * another doppelganger.
 */
public final class Doppelganger {
    public static final int SHIFT_EVERY = 600, SEE = 16, FRIENDS = 32;
    public static final Set<GirlSpecies.Family> SHAPES = EnumSet.of(GirlSpecies.Family.ELF, GirlSpecies.Family.DEVIL, GirlSpecies.Family.UNDEAD,
            GirlSpecies.Family.KITSUNE, GirlSpecies.Family.NEKOMATA, GirlSpecies.Family.CANID, GirlSpecies.Family.JACKALOPE, GirlSpecies.Family.RODENT,
            GirlSpecies.Family.MINOTAUR, GirlSpecies.Family.LIZARDFOLK, GirlSpecies.Family.MARSUPIAL, GirlSpecies.Family.SLIME, GirlSpecies.Family.FLORA);
    private static final Set<GirlSpecies> ODD = EnumSet.of(GirlSpecies.HAND_UNDEAD, GirlSpecies.MANO_PELUDA_UNDEAD, GirlSpecies.GAITRESS_UNDEAD,
            GirlSpecies.SOUL_CLUSTER_UNDEAD, GirlSpecies.WILL_O_WISP_UNDEAD, GirlSpecies.LOST_SOUL_UNDEAD, GirlSpecies.SOUL_LEECH_UNDEAD,
            GirlSpecies.DOPPELGANGER_ELF);

    private Doppelganger() {}

    public static boolean is(AbstractMonsterGirl girl) {
        return girl.getSpecies() == GirlSpecies.DOPPELGANGER_ELF;
    }

    /** Whether she can wear this kind's look. */
    public static boolean copyable(GirlSpecies s) {
        return SHAPES.contains(s.family()) && !ODD.contains(s);
    }

    /** The look she should wear now: a kind to copy, or null for her own face. */
    @Nullable
    public static GirlSpecies choose(ServerLevel level, Elf doppel) {
        if (doppel.trueFace()) return null;
        Player friend = doppel.bondedPlayer(Orders.AFFECTION, FRIENDS);
        if (friend != null) {                                           // her friend's dearest companion about
            AbstractMonsterGirl best = null;
            for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, friend.getBoundingBox().inflate(FRIENDS),
                    g -> g != doppel && g.isAlive() && copyable(g.getSpecies()) && g.getAffection(friend) >= Orders.AFFECTION)) {
                if (best == null || g.getAffection(friend) > best.getAffection(friend)) best = g;
            }
            return best == null ? null : best.getSpecies();
        }
        AbstractMonsterGirl near = null;                                // wild: whoever is closest
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, doppel.getBoundingBox().inflate(SEE),
                g -> g != doppel && g.isAlive() && copyable(g.getSpecies()))) {
            if (near == null || g.distanceToSqr(doppel) < near.distanceToSqr(doppel)) near = g;
        }
        return near == null ? null : near.getSpecies();
    }

    /** She shifts to the look she should wear. Whether her look changed. */
    public static boolean shift(ServerLevel level, Elf doppel) {
        GirlSpecies want = choose(level, doppel);
        String id = want == null ? "" : want.id();
        if (id.equals(doppel.disguise())) return false;
        doppel.setDisguise(id);
        level.sendParticles(ParticleTypes.WITCH, doppel.getX(), doppel.getY() + doppel.getBbHeight() * 0.5, doppel.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        doppel.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.6F, 1.2F);
        return true;
    }

    /** Her friend asks for her true face, or for the borrowed one back. Whether she listened. */
    public static boolean askFace(ServerLevel level, Elf doppel, Player player) {
        if (doppel.getAffection(player) < Orders.AFFECTION) return false;
        doppel.setTrueFace(!doppel.trueFace());
        shift(level, doppel);
        player.displayClientMessage(Component.translatable(doppel.trueFace() ? "message.monstergirls.doppelganger.true_face" : "message.monstergirls.doppelganger.borrowed",
                doppel.getDisplayName()), true);
        return true;
    }
}
