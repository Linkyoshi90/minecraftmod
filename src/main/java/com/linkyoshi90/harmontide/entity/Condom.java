package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * A grown daughter will have sex with the player who fathered her only while he is carrying one of these.
 * It is used up when the night would have conceived, and that night does not conceive: the line she gives
 * is that monster girls have a higher chance of inherited birth defects. Anyone younger stays out, condom or not.
 * Anyone who is not his daughter is unchanged, and does not need one.
 */
public final class Condom {
    private static final String SAID = "CondomSaid";

    private Condom() {}

    /** His own daughter, and grown by the same rule as the bed, even on a day she is already expecting. */
    public static boolean grownOffspring(Mob mob, Player player) {
        if (!(mob instanceof Bonded bonded) || mob.isBaby()) return false;
        UUID father = bonded.getFather();
        if (father == null || !father.equals(player.getUUID())) return false;
        return bonded.getGestation().ignoringTerm(bonded::canShareBed);
    }

    public static boolean carrying(Player player) {
        return player.getInventory().countItem(ModItems.CONDOM.get()) > 0;
    }

    /** Sex may go ahead. A daughter needs one in the inventory. Everyone else is as before. */
    public static boolean allows(Mob mob, Player player) {
        return !grownOffspring(mob, player) || carrying(player);
    }

    /**
     * Still refused because of who her father is. A grown daughter is refused only while he has no condom.
     * A baby, a son, or anyone of childlike height stays refused.
     */
    public static boolean barred(Mob mob, Player player) {
        if (!(mob instanceof Bonded bonded)) return false;
        UUID father = bonded.getFather();
        if (father == null || !father.equals(player.getUUID())) return false;
        if (grownOffspring(mob, player)) return !carrying(player);
        return true;
    }

    /** Uses one, if he has one. Creative keeps the stack. */
    public static boolean take(Player player) {
        if (!carrying(player)) return false;
        if (player.getAbilities().instabuild) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(ModItems.CONDOM.get())) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    /** She would have come to bed, and she says why she will not. Quiet for a minute afterwards. */
    public static void mention(ServerLevel level, Mob mob, Player player) {
        if (!grownOffspring(mob, player) || carrying(player) || !(mob instanceof Bonded bonded)) return;
        boolean succubus = mob instanceof com.linkyoshi90.harmontide.entity.girl.Devil devil
                && devil.getSpecies() == com.linkyoshi90.harmontide.entity.girl.GirlSpecies.SUCCUBUS_DEVIL;
        boolean would = com.linkyoshi90.harmontide.entity.Monsterization.monsterized(mob)
                || succubus || bonded.getAffection(player) >= Bonded.BED_AFFECTION;
        if (!would) return;
        long now = level.getGameTime();
        if (now < mob.getPersistentData().getLong(SAID)) return;
        mob.getPersistentData().putLong(SAID, now + 1200);
        player.displayClientMessage(Component.translatable("message.harmontide.no_condom", mob.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }
}
