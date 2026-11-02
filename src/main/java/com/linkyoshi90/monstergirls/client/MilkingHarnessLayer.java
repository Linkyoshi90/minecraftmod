package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * The milking machine's harness on a cow: straps over her clothes, two pumps on her chest, a hose hanging from each.
 * Drawn as plain boxes on her body bone with its own texture, so none of her textures change; it fits itself to the
 * outermost cubes around her chest, whatever she wears.
 */
public class MilkingHarnessLayer extends GeoRenderLayer<AbstractMonsterGirl> {
    private static final RenderType TYPE = RenderType.entityCutoutNoCull(MonsterGirlsMod.id("textures/entity/milking_harness.png"));
    private static final int LEATHER = 0, BRASS = 1, COPPER = 2, HOSE = 3;
    /** Model units per block. */
    private static final float U = 1.0F / 16.0F;
    /** The height of her chest, in model units. */
    private static final float CHEST_LOW = 16.0F, CHEST_HIGH = 19.0F;
    private static final float STRAP = 0.25F;

    /** Half her width, the front and the back of her chest: model units. Cows of a species share a model. */
    private final Map<GirlSpecies, float[]> fits = new EnumMap<>(GirlSpecies.class);

    public MilkingHarnessLayer(GirlRenderer renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack pose, AbstractMonsterGirl girl, GeoBone bone, RenderType renderType, MultiBufferSource buffers,
                              VertexConsumer buffer, float partialTick, int light, int overlay) {
        if (!bone.getName().equals("body") || !girl.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED) || girl.isBaby()) return;
        float[] fit = this.fits.computeIfAbsent(girl.getSpecies(), s -> fit(bone));
        float half = fit[0] + 0.15F, front = fit[1] - 0.15F, back = fit[2] + 0.15F;
        VertexConsumer v = buffers.getBuffer(TYPE);
        Boxes b = new Boxes(pose.last(), v, light, overlay);

        // the band around her, under the pumps
        float y0 = CHEST_LOW, y1 = y0 + 0.6F;
        b.box(-half - STRAP, y0, front - STRAP, half + STRAP, y1, front, LEATHER);
        b.box(-half - STRAP, y0, back, half + STRAP, y1, back + STRAP, LEATHER);
        b.box(-half - STRAP, y0, front, -half, y1, back, LEATHER);
        b.box(half, y0, front, half + STRAP, y1, back, LEATHER);

        float top = CHEST_HIGH + 1.0F;   // over the shoulders
        for (int side = -1; side <= 1; side += 2) {
            float x = side * 1.25F;
            float sx0 = side * 2.1F - 0.3F, sx1 = side * 2.1F + 0.3F;
            b.box(sx0, y1, front - STRAP, sx1, top, front, LEATHER);
            b.box(sx0, y1, back, sx1, top, back + STRAP, LEATHER);
            b.box(sx0, top, front - STRAP, sx1, top + STRAP, back + STRAP, LEATHER);
            b.box(sx0, y0, front - STRAP - 0.1F, sx1, y1, front - STRAP, BRASS);          // a buckle

            // the pump: a brass ring, a copper body, a hose down from its end
            float py = 17.75F;
            float stroke = 0.3F + 0.3F * net.minecraft.util.Mth.sin((girl.tickCount + partialTick) * 0.7F + side);   // the two work in turn
            b.box(x - 1.0F, py - 1.0F, front - STRAP - 0.3F, x + 1.0F, py + 1.0F, front - STRAP, BRASS);
            b.box(x - 0.75F, py - 0.75F, front - STRAP - 1.1F - stroke, x + 0.75F, py + 0.75F, front - STRAP - 0.3F, COPPER);
            b.box(x - 0.2F, py - 0.2F, front - STRAP - 1.4F - stroke, x + 0.2F, py + 0.2F, front - STRAP - 1.1F - stroke, BRASS);
            b.box(x - 0.15F, 11.0F, front - STRAP - 1.4F - stroke, x + 0.15F, py - 0.2F, front - STRAP - 1.1F - stroke, HOSE);
        }
    }

    private static float[] fit(GeoBone body) {
        float half = 0.0F, front = 0.0F, back = 0.0F;
        for (GeoCube cube : body.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    float y = vertex.position().y() / U;
                    if (y < CHEST_LOW || y > CHEST_HIGH) continue;
                    half = Math.max(half, Math.abs(vertex.position().x() / U));
                    front = Math.min(front, vertex.position().z() / U);
                    back = Math.max(back, vertex.position().z() / U);
                }
            }
        }
        return new float[]{half, front, back};
    }

    /** Boxes in model units, each face filled with one quarter of the harness texture. */
    record Boxes(PoseStack.Pose pose, VertexConsumer v, int light, int overlay) {
        void box(float x0, float y0, float z0, float x1, float y1, float z1, int cell) {
            float u0 = (cell % 2) * 0.5F, v0 = (cell / 2) * 0.5F;
            this.quad(cell, u0, v0, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            this.quad(cell, u0, v0, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
            this.quad(cell, u0, v0, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
            this.quad(cell, u0, v0, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            this.quad(cell, u0, v0, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            this.quad(cell, u0, v0, 0, -1, 0, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
        }

        private void quad(int cell, float u, float w, float nx, float ny, float nz, float... c) {
            float[][] uv = {{u, w + 0.5F}, {u, w}, {u + 0.5F, w}, {u + 0.5F, w + 0.5F}};
            for (int i = 0; i < 4; i++) {
                this.v.addVertex(this.pose, c[i * 3] * U, c[i * 3 + 1] * U, c[i * 3 + 2] * U).setColor(-1).setUv(uv[i][0], uv[i][1])
                        .setOverlay(this.overlay).setLight(this.light).setNormal(this.pose, nx, ny, nz);
            }
        }
    }
}
