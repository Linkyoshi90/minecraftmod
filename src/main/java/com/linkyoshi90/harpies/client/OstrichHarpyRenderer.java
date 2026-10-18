package com.linkyoshi90.harpies.client;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.OstrichHarpy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.EnumMap;
import java.util.Map;

public class OstrichHarpyRenderer extends GeoEntityRenderer<OstrichHarpy> {
    /** The geo model is ~2.45 blocks tall. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.95F;

    private static final Map<OstrichHarpy.Variant, ResourceLocation> TEXTURES = new EnumMap<>(OstrichHarpy.Variant.class);

    static {
        for (OstrichHarpy.Variant variant : OstrichHarpy.Variant.values()) {
            String suffix = variant == OstrichHarpy.Variant.OSTRICH ? "" : "_" + variant.getSerializedName();
            TEXTURES.put(variant, HarpiesMod.id("textures/entity/ostrich_harpy" + suffix + ".png"));
        }
    }

    public OstrichHarpyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(HarpiesMod.id("ostrich_harpy"), true) {
            @Override
            public ResourceLocation getTextureResource(OstrichHarpy ostrich) {
                return TEXTURES.get(ostrich.getVariant());
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.5F;
    }
}
