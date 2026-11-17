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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HarmontideClientConfig() {}

    /** Safe to call before the config has loaded (falls back to the default). */
    public static boolean detailedSparrow() {
        return SPEC.isLoaded() ? DETAILED_SPARROW_MODEL.get() : DETAILED_SPARROW_MODEL.getDefault();
    }

    /** Which adult sparrow model to draw: sparrow_harpy, sparrow_harpy_detailed or sparrow_harpy_soft. */
    public static String sparrowModel() {
        if (!detailedSparrow()) return "sparrow_harpy";
        boolean soft = SPEC.isLoaded() ? SOFT_SPARROW_FACE.get() : SOFT_SPARROW_FACE.getDefault();
        return soft ? "sparrow_harpy_soft" : "sparrow_harpy_detailed";
    }
}
