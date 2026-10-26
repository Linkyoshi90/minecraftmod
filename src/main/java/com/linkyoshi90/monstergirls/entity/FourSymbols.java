package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * The Four Symbols advancement (data/monstergirls/advancement/four_symbols.json): befriend (affection 10+) the guardian
 * of each quarter of the sky. East: the long dragon (the Azure Dragon). West: a white tiger nekomata. South: Zhuque,
 * the Vermilion Bird. North: Xuanwu, the Black Tortoise.
 */
public final class FourSymbols {
    public static final net.minecraft.resources.ResourceLocation ID = MonsterGirlsMod.id("four_symbols");

    private FourSymbols() {}

    /** The quarter she guards (the advancement's criterion), or null. */
    @Nullable
    public static String quarterOf(Entity e) {
        if (e instanceof ZhuqueHarpy) return "south";
        if (!(e instanceof AbstractMonsterGirl g)) return null;
        GirlSpecies s = g.getSpecies();
        if (s == GirlSpecies.LONG_DRAGON) return "east";
        if (s == GirlSpecies.TIGER_NEKOMATA && "white".equals(g.getVariantName())) return "west";
        if (s == GirlSpecies.BLACK_TORTOISE_LIZARDFOLK) return "north";
        return null;
    }

    /** Called whenever a player's affection with her changes. */
    public static void onAffection(Entity girl, @Nullable Player player, int affection) {
        if (affection < Orders.AFFECTION || !(player instanceof ServerPlayer sp)) return;
        String quarter = quarterOf(girl);
        if (quarter == null) return;
        AdvancementHolder a = sp.server.getAdvancements().get(ID);
        if (a != null) sp.getAdvancements().award(a, quarter);
    }
}
