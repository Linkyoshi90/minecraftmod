package com.linkyoshi90.harpies.client;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AbstractHarpy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Renderer for harpies: one geo model named after the species, plus held items. A species with colour or species
 * variants passes a texture suffix function (e.g. "_snowy" -> textures/entity/owl_harpy_snowy.png).
 */
public class HarpyRenderer<T extends AbstractHarpy> extends GeoEntityRenderer<T> {
    public HarpyRenderer(EntityRendererProvider.Context context, String species, float scale, float shadowRadius) {
        this(context, species, scale, shadowRadius, harpy -> "");
    }

    public HarpyRenderer(EntityRendererProvider.Context context, String species, float scale, float shadowRadius,
                         Function<T, String> textureSuffix) {
        super(context, new DefaultedEntityGeoModel<>(HarpiesMod.id(species), true) {
            private final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

            @Override
            public ResourceLocation getTextureResource(T harpy) {
                return this.textures.computeIfAbsent(textureSuffix.apply(harpy),
                        suffix -> HarpiesMod.id("textures/entity/" + species + suffix + ".png"));
            }
        });
        this.withScale(scale);
        this.shadowRadius = shadowRadius;
        this.addRenderLayer(new HeldItemLayer<>(this));
    }
}
