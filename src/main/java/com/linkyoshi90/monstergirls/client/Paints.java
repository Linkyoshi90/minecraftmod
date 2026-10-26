package com.linkyoshi90.monstergirls.client;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A texture in a Harmontide paint: textures/entity/&lt;name&gt;_paint_&lt;paint&gt;.png (tools/gen_paints.py), for harpies and anurans. */
final class Paints {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    private Paints() {}

    /** Jello, ghost and stained glass are see-through: drawn translucent. */
    static boolean translucent(@Nullable String paint) {
        return "jello".equals(paint) || "ghost".equals(paint) || "stained_glass".equals(paint) || "gummy".equals(paint);
    }

    static ResourceLocation of(ResourceLocation texture, @Nullable String paint) {
        if (paint == null) return texture;
        return CACHE.computeIfAbsent(texture.getPath() + "#" + paint, k -> texture.withPath(texture.getPath().replace(".png", "_paint_" + paint + ".png")));
    }
}
