package com.linkyoshi90.monstergirls.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A texture in a Harmontide paint: textures/entity/&lt;name&gt;_paint_&lt;paint&gt;.png (tools/gen_paints.py), for harpies and anurans.
 * The textures are in the optional paints jar. Without it she keeps her paint (saved, passed to her young) and is drawn unpainted.
 */
final class Paints {
    private static final boolean INSTALLED = ModList.get().isLoaded("monstergirls_paints");
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private Paints() {}

    /** The paint to draw: hers, or null when the paints jar isn't installed. */
    @Nullable
    static String shown(@Nullable String paint) {
        return INSTALLED ? paint : null;
    }

    /** Jello, ghost and stained glass are see-through: drawn translucent. */
    static boolean translucent(@Nullable String paint) {
        paint = shown(paint);
        return "jello".equals(paint) || "ghost".equals(paint) || "stained_glass".equals(paint) || "gummy".equals(paint) || "hologram".equals(paint);
    }

    static ResourceLocation of(ResourceLocation texture, @Nullable String paint) {
        if (shown(paint) == null) return texture;
        return CACHE.computeIfAbsent(texture.getPath() + "#" + paint, k -> texture.withPath(texture.getPath().replace(".png", "_paint_" + paint + ".png")));
    }
}
