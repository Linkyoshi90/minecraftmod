package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * A sheer bridal veil on her wedding day and anniversaries (FLAG_VEIL, entity/Marriage): a lace band over the crown of her head and a
 * long drape falling behind it. Plain boxes fitted to her head bone, as the towel is to her body.
 */
public class VeilLayer extends GeoRenderLayer<AbstractMonsterGirl> {
    private static final RenderType TYPE = RenderType.entityTranslucent(HarmontideMod.id("textures/entity/veil.png"));
    private static final float U = 1.0F / 16.0F, PAD = 0.35F, DRAPE = 13.0F, BEHIND = 1.6F;

    public VeilLayer(GirlRenderer renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack pose, AbstractMonsterGirl girl, GeoBone bone, RenderType renderType, MultiBufferSource buffers,
                              VertexConsumer buffer, float partialTick, int light, int overlay) {
        if (!bone.getName().equals("head") || !girl.hasFlag(AbstractMonsterGirl.FLAG_VEIL) || girl.isBaby()) return;
        float half = 0.0F, top = -Float.MAX_VALUE, front = Float.MAX_VALUE, back = -Float.MAX_VALUE;
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    half = Math.max(half, Math.abs(vertex.position().x() / U));
                    top = Math.max(top, vertex.position().y() / U);
                    front = Math.min(front, vertex.position().z() / U);
                    back = Math.max(back, vertex.position().z() / U);
                }
            }
        }
        if (half <= 0.0F) return;
        MilkingHarnessLayer.Boxes b = new MilkingHarnessLayer.Boxes(pose.last(), buffers.getBuffer(TYPE), light, overlay);
        float mid = (front + back) / 2.0F;
        b.box(-half - PAD, top - 0.2F, front + 0.6F, half + PAD, top + 0.6F, mid + 0.5F, 1);                // the lace band over her crown
        b.box(-half - PAD - 0.4F, top - DRAPE, back + BEHIND - 0.3F, half + PAD + 0.4F, top + 0.4F, back + BEHIND, 0); // the drape
        b.box(-half - PAD, top - 0.2F, mid, half + PAD, top + 0.5F, back + BEHIND, 0);                       // over the back of her head to it
    }
}
