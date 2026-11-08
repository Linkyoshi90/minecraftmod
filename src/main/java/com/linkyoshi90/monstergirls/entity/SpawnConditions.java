package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Kinds that only come out at the right moment: under a full moon, at night, in a storm (any rain or thunder), in the rain, or only in a thunderstorm. It binds
 * in a dimension with a sky and weather of its own (the overworld); a kind that also lives in the Nether or the End comes there as
 * always. Natural spawns only (SpawnRarity.allows); the journal says when to look. spawnConditions turns it off.
 */
public final class SpawnConditions {
    public enum When {
        FULL_MOON, NIGHT, STORM, RAIN, THUNDER;

        public String key() {
            return "journal.monstergirls.when." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Map<String, When> WHEN = new HashMap<>();

    static {
        put(When.FULL_MOON, "moon_rabbit_jackalope", "amarok_canid", "moonseal_mermaid");
        put(When.NIGHT, "vampire_undead", "banshee_undead", "yurei_undead", "mothman_bug", "firefly_bug", "will_o_wisp_undead", "nue_nekomata",
                "rusalka_mermaid", "lorelei_mermaid");
        put(When.STORM, "anzu_centaur", "ziz_harpy", "leviathan_mermaid", "sylph_elf");
        put(When.THUNDER, "raiju_canid");
        put(When.RAIN, "kappa_lizardfolk", "boghare_jackalope", "llorona_undead", "ame_onna_undead");
    }

    private SpawnConditions() {}

    private static void put(When when, String... ids) {
        for (String id : ids) WHEN.put(id, when);
    }

    public static Map<String, When> all() {
        return java.util.Collections.unmodifiableMap(WHEN);
    }

    @Nullable
    public static When of(String id) {
        return WHEN.get(id);
    }

    @Nullable
    public static When of(Entity e) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        return MonsterGirlsMod.MODID.equals(key.getNamespace()) ? of(key.getPath()) : null;
    }

    /** Whether now is her moment, here. */
    public static boolean met(LevelAccessor level, Entity e) {
        When when = of(e);
        if (when == null || !MonsterGirlsConfig.spawnConditions() || !level.dimensionType().natural()) return true;
        return met(when, level.dayTime(), level.getMoonPhase(), level.getLevelData().isRaining(), level.getLevelData().isThundering());
    }

    public static boolean met(When when, long dayTime, int moonPhase, boolean raining, boolean thundering) {
        long t = Math.floorMod(dayTime, 24000L);
        boolean night = t >= 13000 && t < 23000;
        return switch (when) {
            case FULL_MOON -> night && moonPhase == 0;
            case NIGHT -> night;
            case STORM -> raining || thundering;
            case RAIN -> raining;
            case THUNDER -> thundering;
        };
    }
}
