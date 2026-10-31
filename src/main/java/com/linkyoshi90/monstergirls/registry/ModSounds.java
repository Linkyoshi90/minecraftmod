package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every harpy sound has its own event. For now assets/monstergirls/sounds.json points them at re-pitched vanilla
 * parrot and note-block sounds; drop real recordings into assets/monstergirls/sounds/ and change sounds.json to
 * swap them in without touching code.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MonsterGirlsMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_AMBIENT = register("entity.sparrow_harpy.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_HURT = register("entity.sparrow_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_DEATH = register("entity.sparrow_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_EAT = register("entity.sparrow_harpy.eat");
    /** One note of her song; the sing goal picks the pitch. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_SING = register("entity.sparrow_harpy.sing");

    /** The dolphin and orca mermaids' idle song (a real recording, sounds/merfolk/mermsing.ogg). */
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_SING = register("entity.mermaid.sing");

    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_CAW = register("entity.crow_harpy.caw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_GIGGLE = register("entity.crow_harpy.giggle");
    /** The cackle after picking on another mob. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_LAUGH = register("entity.crow_harpy.laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_HURT = register("entity.crow_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_DEATH = register("entity.crow_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CROW_EAT = register("entity.crow_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_AMBIENT = register("entity.ostrich_harpy.ambient");
    /** The warning display. */
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_HISS = register("entity.ostrich_harpy.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_KICK = register("entity.ostrich_harpy.kick");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_HURT = register("entity.ostrich_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_DEATH = register("entity.ostrich_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> OSTRICH_EAT = register("entity.ostrich_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> OWL_HOOT = register("entity.owl_harpy.hoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> OWL_HURT = register("entity.owl_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> OWL_DEATH = register("entity.owl_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> OWL_EAT = register("entity.owl_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> SEAGULL_AMBIENT = register("entity.seagull_harpy.ambient");
    /** One note of the siren song; the song goal picks the pitch. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAGULL_SING = register("entity.seagull_harpy.sing");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAGULL_HURT = register("entity.seagull_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAGULL_DEATH = register("entity.seagull_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEAGULL_EAT = register("entity.seagull_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> ALBATROSS_AMBIENT = register("entity.albatross_harpy.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALBATROSS_HURT = register("entity.albatross_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALBATROSS_DEATH = register("entity.albatross_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALBATROSS_EAT = register("entity.albatross_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_AMBIENT = register("entity.riflebird_harpy.ambient");
    /** Dance music: melody, bass and hi-hat notes (the dance goal picks pitches). */
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_SING = register("entity.riflebird_harpy.sing");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_BASS = register("entity.riflebird_harpy.bass");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_HAT = register("entity.riflebird_harpy.hat");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_LAY = register("entity.riflebird_harpy.lay");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_HURT = register("entity.riflebird_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_DEATH = register("entity.riflebird_harpy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLEBIRD_EAT = register("entity.riflebird_harpy.eat");

    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_AMBIENT = register("entity.phoenix_harpy.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_SHOOT = register("entity.phoenix_harpy.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_CRY = register("entity.phoenix_harpy.cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_REBIRTH = register("entity.phoenix_harpy.rebirth");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_HURT = register("entity.phoenix_harpy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHOENIX_DEATH = register("entity.phoenix_harpy.death");


    /** The sounds of one frog or toad species; every species has its own events (entity.&lt;species&gt;.ambient...). */
    public record AnuranSounds(DeferredHolder<SoundEvent, SoundEvent> ambient, DeferredHolder<SoundEvent, SoundEvent> hurt,
                               DeferredHolder<SoundEvent, SoundEvent> death, DeferredHolder<SoundEvent, SoundEvent> eat,
                               DeferredHolder<SoundEvent, SoundEvent> tongue, DeferredHolder<SoundEvent, SoundEvent> bellow) {}

    /** Every frog and toad species in the mod. */
    public static final java.util.List<String> ANURAN_SPECIES = java.util.List.of("bullywug", "bullfrog", "tree_frog", "poison_dart_frog",
            "glass_frog", "goliath_frog", "desert_rain_frog", "mucus_toad", "fire_bellied_toad", "cane_toad", "natterjack", "vodyanoy", "surinam_toad");
    private static final java.util.Map<String, AnuranSounds> ANURAN = new java.util.HashMap<>();

    static {
        for (String species : ANURAN_SPECIES) {
            ANURAN.put(species, new AnuranSounds(register("entity." + species + ".ambient"), register("entity." + species + ".hurt"),
                    register("entity." + species + ".death"), register("entity." + species + ".eat"),
                    register("entity." + species + ".tongue"), register("entity." + species + ".bellow")));
        }
    }

    public static AnuranSounds anuran(String species) {
        return ANURAN.get("mailtoad".equals(species) ? "cane_toad" : species);   // the mailtoad croaks like the cane toad she is
    }

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(MonsterGirlsMod.id(name)));
    }

    /** Small random pitch wobble so repeated sounds don't feel canned. */
    public static float wobble(Entity entity) {
        return 0.95F + entity.getRandom().nextFloat() * 0.1F;
    }
}
