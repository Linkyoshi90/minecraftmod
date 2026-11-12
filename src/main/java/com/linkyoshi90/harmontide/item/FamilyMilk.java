package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Clade;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Each clade's milk (entity.Clade, TAXONOMY_PLAN.md) and what a bucket of it does for whoever drinks it. Every clade gives
 * milk except the plants and fungi and the constructs: they never carry young the way the others do. The constant names
 * match the clades, so name().toLowerCase() is the clade's lang key.
 */
public enum FamilyMilk {
    FELIFORMIA("feliform", MobEffects.NIGHT_VISION),
    CANIDAE("canid", MobEffects.MOVEMENT_SPEED, MobEffects.LUCK),
    ARCTOIDEA("arctoid", MobEffects.DAMAGE_RESISTANCE, MobEffects.WATER_BREATHING),
    RODENTIA("rodent", MobEffects.DIG_SPEED, MobEffects.LUCK),
    LAGOMORPHA("lagomorph", MobEffects.JUMP),
    PRIMATES("primate", MobEffects.JUMP, MobEffects.DAMAGE_BOOST),
    CHIROPTERA("bat", MobEffects.NIGHT_VISION, MobEffects.SLOW_FALLING),
    MARSUPIALIA("marsupial", MobEffects.JUMP, MobEffects.DAMAGE_RESISTANCE),
    XENARTHRA("xenarthran", MobEffects.DAMAGE_RESISTANCE, MobEffects.ABSORPTION),
    PAENUNGULATA("paenungulate", MobEffects.HEALTH_BOOST),
    PERISSODACTYLA("perissodactyl", MobEffects.MOVEMENT_SPEED),
    BOVIDAE("bovid", MobEffects.DAMAGE_BOOST),
    CERVIDAE("cervid", MobEffects.MOVEMENT_SPEED, MobEffects.JUMP),
    CAMELIDAE("camelid", MobEffects.FIRE_RESISTANCE),
    SUINA("suine", MobEffects.DIG_SPEED, MobEffects.ABSORPTION),
    WHIPPOMORPHA("whippomorph", MobEffects.DOLPHINS_GRACE, MobEffects.WATER_BREATHING),
    AVES("bird", MobEffects.SLOW_FALLING),
    ARCHOSAURIA("archosaur", MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED),
    TESTUDINES("testudine", MobEffects.DAMAGE_RESISTANCE, MobEffects.WATER_BREATHING),
    SQUAMATA("squamate", MobEffects.REGENERATION),
    AMPHIBIA("amphibian", MobEffects.JUMP, MobEffects.WATER_BREATHING),
    ACTINOPTERYGII("fish", MobEffects.WATER_BREATHING),
    CHELICERATA("chelicerate", MobEffects.DAMAGE_RESISTANCE),
    HEXAPODA("insect", MobEffects.DIG_SPEED),
    MALACOSTRACA("malacostracan", MobEffects.CONDUIT_POWER),
    MOLLUSCA("mollusk", MobEffects.DOLPHINS_GRACE),
    FAE("fae", MobEffects.HERO_OF_THE_VILLAGE),
    DRAGON("dragon", MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_BOOST),
    UNDEAD("undead", MobEffects.INVISIBILITY),
    DEVIL("devil", MobEffects.ABSORPTION),
    SLIME("slime", MobEffects.HEALTH_BOOST, MobEffects.JUMP),
    RIDER("rider", MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE);

    /**
     * The milks before the taxonomy (2026-11-05), each to the clade that kept most of that family. Old saves name them on
     * dairy stacks (the milk_family component) and as bucket item ids (ModItems registers aliases).
     */
    public static final Map<String, FamilyMilk> OLD = Map.ofEntries(
            Map.entry("NEKOMATA", FELIFORMIA), Map.entry("KITSUNE", CANIDAE), Map.entry("CANID", CANIDAE), Map.entry("LAMIA", SQUAMATA),
            Map.entry("ARACHNE", CHELICERATA), Map.entry("CENTAUR", PERISSODACTYLA), Map.entry("MERFOLK", ACTINOPTERYGII),
            Map.entry("JACKALOPE", LAGOMORPHA), Map.entry("MINOTAUR", BOVIDAE), Map.entry("MOLLUSK", MOLLUSCA), Map.entry("ELF", FAE),
            Map.entry("LIZARDFOLK", TESTUDINES), Map.entry("BUG", HEXAPODA), Map.entry("RODENT", RODENTIA), Map.entry("CRUSTACEAN", MALACOSTRACA),
            Map.entry("MARSUPIAL", MARSUPIALIA), Map.entry("ARMOURED", XENARTHRA), Map.entry("BAT", CHIROPTERA), Map.entry("THEROPOD", ARCHOSAURIA),
            Map.entry("HARPY", AVES), Map.entry("ANURAN", AMPHIBIA));

    private final String prefix;
    private final List<Holder<MobEffect>> effects;

    @SafeVarargs
    FamilyMilk(String prefix, Holder<MobEffect>... effects) {
        this.prefix = prefix;
        this.effects = List.of(effects);
    }

    public List<Holder<MobEffect>> effects() {
        return this.effects;
    }

    /** The bucket's item id, e.g. "feliform_milk". */
    public String id() {
        return this.prefix + "_milk";
    }

    public Clade clade() {
        return Clade.valueOf(this.name());
    }

    /** By name, old family names included (an old cheese says "NEKOMATA"). */
    public static FamilyMilk byName(String name) {
        FamilyMilk old = OLD.get(name.toUpperCase(Locale.ROOT));
        return old != null ? old : valueOf(name.toUpperCase(Locale.ROOT));
    }

    /** The clade's milk, or null for the plants and fungi and the constructs. */
    @Nullable
    public static FamilyMilk of(@Nullable Clade clade) {
        if (clade == null || clade == Clade.FLORA || clade == Clade.CONSTRUCT) return null;
        return valueOf(clade.name());
    }

    /** Whose milk she gives, or null if her clade has none. */
    @Nullable
    public static FamilyMilk of(LivingEntity creature) {
        Clade clade = Clade.of(creature);
        if (clade == null && creature instanceof AbstractHarpy) clade = Clade.AVES;
        if (clade == null && creature instanceof AbstractAnuran) clade = Clade.AMPHIBIA;
        return of(clade);
    }
}
