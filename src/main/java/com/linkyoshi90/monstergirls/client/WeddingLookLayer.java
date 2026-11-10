package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.WeddingVeil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * The wedding look of the harpies and the frogs (2026-11-10), worn when the girls wear their veil (WeddingVeil): a harpy has a crown
 * of white feathers standing round the top of her head with a short sheer veil behind; a frog or toad a lily pad for a hat, a pink
 * blossom on it, and the sheer veil falling from under it. Boxes fitted to the head bone, as the girls' veil is; the texture is a
 * 2x2 grid (textures/entity/wedding_looks.png): feather, sheer lace, lily pad, blossom.
 */
public class WeddingLookLayer<T extends LivingEntity & GeoAnimatable & WeddingVeil> extends GeoRenderLayer<T> {
    private static final RenderType TYPE = RenderType.entityTranslucent(MonsterGirlsMod.id("textures/entity/wedding_looks.png"));
    private static final float U = 1.0F / 16.0F;
    private static final int FEATHER = 0, LACE = 1, PAD = 2, BLOSSOM = 3;
    private final boolean frog;

    public WeddingLookLayer(GeoRenderer<T> renderer, boolean frog) {
        super(renderer);
        this.frog = frog;
    }

    @Override
    public void renderForBone(PoseStack pose, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource buffers,
                              VertexConsumer buffer, float partialTick, int light, int overlay) {
        if (!bone.getName().equals("head") || !animatable.isVeiled() || animatable.isBaby()) return;
        float half = 0.0F, top = -Float.MAX_VALUE, front = Float.MAX_VALUE, back = -Float.MAX_VALUE, cx = 0.0F;
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    minX = Math.min(minX, vertex.position().x() / U);
                    maxX = Math.max(maxX, vertex.position().x() / U);
                    top = Math.max(top, vertex.position().y() / U);
                    front = Math.min(front, vertex.position().z() / U);
                    back = Math.max(back, vertex.position().z() / U);
                }
            }
        }
        if (maxX <= minX) return;
        half = (maxX - minX) / 2.0F;
        cx = (maxX + minX) / 2.0F;
        MilkingHarnessLayer.Boxes b = new MilkingHarnessLayer.Boxes(pose.last(), buffers.getBuffer(TYPE), light, overlay);
        float depth = back - front, drape = Math.max(5.0F, Math.min(9.0F, depth * 1.4F));
        if (this.frog) {
            float y = top - 0.1F;                                                // the lily pad, a little wider than her head
            b.box(cx - half - 0.8F, y, front - 0.4F, cx + half + 0.8F, y + 0.5F, back + 0.6F, PAD);
            float fx = cx - half * 0.45F, fz = front + depth * 0.35F;            // the blossom on it
            b.box(fx - 1.0F, y + 0.5F, fz - 1.0F, fx + 1.0F, y + 1.9F, fz + 1.0F, BLOSSOM);
            b.box(cx - half - 0.5F, y - drape, back + 0.6F, cx + half + 0.5F, y + 0.3F, back + 0.9F, LACE);   // the veil from under it
        } else {
            int n = 7;                                                           // the crown: feathers along the front and sides
            for (int i = 0; i < n; i++) {
                float t = i / (float) (n - 1), x = cx - half + t * 2.0F * half;
                float edge = Math.abs(t - 0.5F) * 2.0F;                          // the outer ones stand further back
                float z = front + 0.5F + edge * depth * 0.45F, tall = 3.2F - edge * 1.2F;
                b.box(x - 0.35F, top - 0.3F, z - 0.25F, x + 0.35F, top + tall, z + 0.25F, FEATHER);
            }
            b.box(cx - half - 0.2F, top - 0.4F, front + 0.2F, cx + half + 0.2F, top + 0.4F, front + 1.0F, FEATHER);   // the band they're set in
            b.box(cx - half, top - drape, back + 0.4F, cx + half, top + 0.4F, back + 0.7F, LACE);                    // a short veil behind
        }
    }
}
