package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Each family's milk and what a bucket of it does for whoever drinks it. Constructs and flora have none: they never
 * carry young the way the others do.
 */
public enum FamilyMilk {
    HARPY(MobEffects.SLOW_FALLING),
    ANURAN(MobEffects.JUMP, MobEffects.WATER_BREATHING),
    NEKOMATA(MobEffects.NIGHT_VISION),
    KITSUNE(MobEffects.LUCK),
    LAMIA(MobEffects.REGENERATION),
    ARACHNE(MobEffects.DAMAGE_RESISTANCE),
    CENTAUR(MobEffects.MOVEMENT_SPEED),
    MERFOLK(MobEffects.WATER_BREATHING),
    SLIME(MobEffects.HEALTH_BOOST, MobEffects.JUMP),
    JACKALOPE(MobEffects.JUMP),
    MINOTAUR(MobEffects.DAMAGE_BOOST),
    MOLLUSK(MobEffects.DOLPHINS_GRACE),
    ELF(MobEffects.HERO_OF_THE_VILLAGE),
    UNDEAD(MobEffects.INVISIBILITY),
    LIZARDFOLK(MobEffects.FIRE_RESISTANCE, MobEffects.MOVEMENT_SPEED),
    DRAGON(MobEffects.FIRE_RESISTANCE, MobEffects.DAMAGE_BOOST),
    DEVIL(MobEffects.ABSORPTION),
    BUG(MobEffects.DIG_SPEED),
    CANID(MobEffects.MOVEMENT_SPEED, MobEffects.NIGHT_VISION),
    RODENT(MobEffects.DIG_SPEED, MobEffects.LUCK),
    CRUSTACEAN(MobEffects.CONDUIT_POWER),
    MARSUPIAL(MobEffects.JUMP, MobEffects.DAMAGE_RESISTANCE),
    ARMOURED(MobEffects.DAMAGE_RESISTANCE, MobEffects.ABSORPTION),
    BAT(MobEffects.NIGHT_VISION, MobEffects.SLOW_FALLING),
    THEROPOD(MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED),
    RIDER(MobEffects.DAMAGE_BOOST, MobEffects.DAMAGE_RESISTANCE);

    private final List<Holder<MobEffect>> effects;

    @SafeVarargs
    FamilyMilk(Holder<MobEffect>... effects) {
        this.effects = List.of(effects);
    }

    public List<Holder<MobEffect>> effects() {
        return this.effects;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT) + "_milk";
    }

    /** Whose milk she gives, or null if her family has none. */
    @Nullable
    public static FamilyMilk of(LivingEntity creature) {
        if (creature instanceof AbstractHarpy) return HARPY;
        if (creature instanceof AbstractAnuran) return ANURAN;
        if (creature instanceof AbstractMonsterGirl girl) {
            GirlSpecies.Family family = girl.getSpecies().family();
            if (family == GirlSpecies.Family.CONSTRUCT || family == GirlSpecies.Family.FLORA) return null;
            return valueOf(family.name());
        }
        return null;
    }
}
