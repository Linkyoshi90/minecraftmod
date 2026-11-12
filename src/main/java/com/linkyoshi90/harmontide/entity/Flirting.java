package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * A little flirting from close friends: now and then a pat gets a teasing line and a blush of hearts. Words only, and nothing past a tease.
 * Girls, harpies and frogs alike. Only for grown, full-size ones (anyone under CHILDLIKE_HEIGHT stays out, as from the bed), never one and her own father, and it has
 * an off switch (the flirting option).
 */
public final class Flirting {
    public static final int LINES = 8;
    public static final int TUB_LINES = 3, WIFE_LINES = 6;
    private static final int CHANCE = 4;

    private Flirting() {}

    public static boolean canFlirt(AbstractMonsterGirl girl, Player player) {
        return HarmontideConfig.flirting() && !girl.isBaby() && girl.getSpecies().height() >= Bonded.CHILDLIKE_HEIGHT
                && girl.getAffection(player) >= Bonded.BED_AFFECTION && !player.getUUID().equals(girl.getFather())
                && girl.getSpecies().family() != com.linkyoshi90.harmontide.entity.girl.GirlSpecies.Family.CONSTRUCT;
    }

    /** A harpy or a frog: grown, full size (her own size, not a species table's), and never with her father. */
    public static boolean canFlirt(net.minecraft.world.entity.Mob mob, Player player) {
        if (mob instanceof AbstractMonsterGirl girl) return canFlirt(girl, player);
        return HarmontideConfig.flirting() && mob instanceof Bonded b && !mob.isBaby() && mob.getBbHeight() >= Bonded.CHILDLIKE_HEIGHT
                && b.getAffection(player) >= Bonded.BED_AFFECTION && !player.getUUID().equals(b.getFather());
    }

    /** After a pat. True if she flirted. */
    public static boolean onPet(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        return onPet(level, (net.minecraft.world.entity.Mob) girl, player);
    }

    public static boolean onPet(ServerLevel level, net.minecraft.world.entity.Mob mob, Player player) {
        Marriage.warmth(level, mob, player);                                    // the rest of the family is glad of it
        if (!canFlirt(mob, player) || mob.getRandom().nextInt(chance(mob, player)) != 0) return false;
        if (Marriage.married(mob, player)) tease(level, mob, player, "flirt_wife", WIFE_LINES);   // a wife has her own way of teasing
        else tease(level, mob, player, "flirt", LINES);
        return true;
    }

    /** One pat in this many gets a tease: every pat when she is lovestruck with this player, every other one in the Demon Realm, twice as often in her season. */
    public static int chance(net.minecraft.world.entity.Mob mob, Player player) {
        if (DemonRealm.lovestruck(mob, player) || Marriage.honeymoon(mob) && Marriage.married(mob, player)) return 1;
        int chance = DemonRealm.realmAir(mob) ? 2 : CHANCE;
        return MatingSeason.inSeason(mob) ? Math.max(1, chance / 2) : chance;
    }

    /** Sharing the tub, a line of her own. */
    public static void inTub(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        if (canFlirt(girl, player)) tease(level, girl, player, "flirt_tub", TUB_LINES);
    }

    private static void tease(ServerLevel level, net.minecraft.world.entity.Mob girl, Player player, String key, int lines) {
        player.displayClientMessage(Component.translatable("message.harmontide." + key + "." + girl.getRandom().nextInt(lines), girl.getDisplayName()), true);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.25, 0.15, 0.25, 0.0);
    }
}
