package com.linkyoshi90.harmontide.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only options (config/harmontide-client.toml). */
public final class HarmontideClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DETAILED_SPARROW_MODEL = BUILDER
            .comment("Render sparrow harpies with the detailed model (fuller hair, arm feathers, denser wings, talons)",
                    "instead of the plain one. Both share the same animations and colour variants.")
            .define("detailedSparrowModel", true);

    public static final ModConfigSpec.BooleanValue SOFT_SPARROW_FACE = BUILDER
            .comment("With the detailed sparrow model on: give her the softer face (rounded jaw and chin, domed hair,",
                    "rounder eyes, gentler shading). Off = the original square-jawed detailed head.")
            .define("softSparrowFace", true);

    public static final ModConfigSpec.IntValue CREATURE_VOLUME = BUILDER
            .comment("How loud Harmontide creatures are for you, in percent of the normal volume (0 = silent).",
                    "Covers their calls, voices, steps and attacks, also the vanilla sounds they borrow. Only on your side:",
                    "other players hear them at their own setting.")
            .defineInRange("creatureVolume", 100, 0, 100);

    public static final ModConfigSpec.IntValue CREATURE_CHATTER = BUILDER
            .comment("How often you hear Harmontide creatures chatter when idle, in percent (0 = never).",
                    "Only their idle calls (ambient, songs, caws, hoots, giggles...) are skipped; hurt, death, eating and",
                    "attacks always play. Only on your side.")
            .defineInRange("creatureChatter", 100, 0, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HarmontideClientConfig() {}

    /** Safe to call before the config has loaded (falls back to the default). */
    public static boolean detailedSparrow() {
        return SPEC.isLoaded() ? DETAILED_SPARROW_MODEL.get() : DETAILED_SPARROW_MODEL.getDefault();
    }

    /** The creature volume as a factor, 0 to 1 (safe before the config has loaded). */
    public static float creatureVolume() {
        int percent = SPEC.isLoaded() ? CREATURE_VOLUME.get() : CREATURE_VOLUME.getDefault();
        return com.linkyoshi90.harmontide.entity.CreatureVolume.factor(percent);
    }

    /** The share of idle chatter that plays, 0 to 1 (safe before the config has loaded). */
    public static float creatureChatter() {
        int percent = SPEC.isLoaded() ? CREATURE_CHATTER.get() : CREATURE_CHATTER.getDefault();
        return com.linkyoshi90.harmontide.entity.CreatureVolume.factor(percent);
    }

    /** Which adult sparrow model to draw: sparrow_harpy, sparrow_harpy_detailed or sparrow_harpy_soft. */
    public static String sparrowModel() {
        if (!detailedSparrow()) return "sparrow_harpy";
        boolean soft = SPEC.isLoaded() ? SOFT_SPARROW_FACE.get() : SOFT_SPARROW_FACE.getDefault();
        return soft ? "sparrow_harpy_soft" : "sparrow_harpy_detailed";
    }
}
