package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** A bath towel wrapped round her from chest to hips while she shares the soaking tub (FLAG_TOWEL). Plain boxes, fitted to her body bone. */
public class TowelLayer extends GeoRenderLayer<AbstractMonsterGirl> {
    private static final RenderType TYPE = RenderType.entityCutoutNoCull(MonsterGirlsMod.id("textures/entity/towel.png"));
    private static final float U = 1.0F / 16.0F, LOW = 0.5F, HIGH = 7.25F, PAD = 0.2F;

    public TowelLayer(GirlRenderer renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack pose, AbstractMonsterGirl girl, GeoBone bone, RenderType renderType, MultiBufferSource buffers,
                              VertexConsumer buffer, float partialTick, int light, int overlay) {
        if (!bone.getName().equals("body") || !girl.hasFlag(AbstractMonsterGirl.FLAG_TOWEL) || girl.isBaby()) return;
        float base = bone.getPivotY(), y0 = base + LOW, y1 = base + HIGH;
        float half = 0.0F, front = 0.0F, back = 0.0F;
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    float y = vertex.position().y() / U;
                    if (y < y0 || y > y1) continue;
                    half = Math.max(half, Math.abs(vertex.position().x() / U));
                    front = Math.min(front, vertex.position().z() / U);
                    back = Math.max(back, vertex.position().z() / U);
                }
            }
        }
        MilkingHarnessLayer.Boxes b = new MilkingHarnessLayer.Boxes(pose.last(), buffers.getBuffer(TYPE), light, overlay);
        b.box(-half - PAD, y0, front - PAD, half + PAD, y1, back + PAD, 0);
    }
}
