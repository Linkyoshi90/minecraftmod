package com.linkyoshi90.harpies.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only options (config/harpies-client.toml). */
public final class HarpiesClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DETAILED_SPARROW_MODEL = BUILDER
            .comment("Render sparrow harpies with the detailed model (fuller hair, arm feathers, denser wings, talons)",
                    "instead of the plain one. Both share the same animations and colour variants.")
            .define("detailedSparrowModel", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HarpiesClientConfig() {}

    /** Safe to call before the config has loaded (falls back to the default). */
    public static boolean detailedSparrow() {
        return SPEC.isLoaded() ? DETAILED_SPARROW_MODEL.get() : DETAILED_SPARROW_MODEL.getDefault();
    }
}
