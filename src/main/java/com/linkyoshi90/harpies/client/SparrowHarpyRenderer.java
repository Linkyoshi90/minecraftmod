package com.linkyoshi90.harpies.client;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.SparrowHarpy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.EnumMap;
import java.util.Map;

public class SparrowHarpyRenderer extends GeoEntityRenderer<SparrowHarpy> {
    /** The geo model is 2 blocks tall; sparrows are the small ones. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.75F;

    private static final ResourceLocation DETAILED_MODEL = HarpiesMod.id("geo/entity/sparrow_harpy_detailed.geo.json");
    private static final Map<SparrowHarpy.Variant, ResourceLocation> TEXTURES = new EnumMap<>(SparrowHarpy.Variant.class);
    private static final Map<SparrowHarpy.Variant, ResourceLocation> DETAILED_TEXTURES = new EnumMap<>(SparrowHarpy.Variant.class);

    static {
        for (SparrowHarpy.Variant variant : SparrowHarpy.Variant.values()) {
            String suffix = variant == SparrowHarpy.Variant.HOUSE ? "" : "_" + variant.getSerializedName();
            TEXTURES.put(variant, HarpiesMod.id("textures/entity/sparrow_harpy" + suffix + ".png"));
            DETAILED_TEXTURES.put(variant, HarpiesMod.id("textures/entity/sparrow_harpy_detailed" + suffix + ".png"));
        }
    }

    public SparrowHarpyRenderer(EntityRendererProvider.Context context) {
        // Resolves geo/entity/sparrow_harpy.geo.json and animations/entity/...; true = head follows look direction.
        // The detailed model (tools/gen_sparrow_detailed_harpy.py) has the same bones and reuses these animations.
        super(context, new DefaultedEntityGeoModel<>(HarpiesMod.id("sparrow_harpy"), true) {
            @Override
            public ResourceLocation getModelResource(SparrowHarpy harpy) {
                return HarpiesClientConfig.detailedSparrow() ? DETAILED_MODEL : super.getModelResource(harpy);
            }

            @Override
            public ResourceLocation getTextureResource(SparrowHarpy harpy) {
                return (HarpiesClientConfig.detailedSparrow() ? DETAILED_TEXTURES : TEXTURES).get(harpy.getVariant());
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new HeldItemLayer<>(this));
    }
}
