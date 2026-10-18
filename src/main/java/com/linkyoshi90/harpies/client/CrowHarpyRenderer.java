package com.linkyoshi90.harpies.client;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrowHarpyRenderer extends GeoEntityRenderer<CrowHarpy> {
    /** The geo model is ~2 blocks tall; crows are medium-sized. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.85F;

    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();

    public CrowHarpyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(HarpiesMod.id("crow_harpy"), true) {
            @Override
            public ResourceLocation getTextureResource(CrowHarpy crow) {
                // "" -> crow_harpy.png, "_hooded" -> crow_harpy_hooded.png, ...
                return TEXTURES.computeIfAbsent(crow.getVariantSuffix(), suffix -> HarpiesMod.id("textures/entity/crow_harpy" + suffix + ".png"));
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.35F;
        this.addRenderLayer(new HeldItemLayer<>(this));
    }
}
