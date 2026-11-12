package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Wardrobe;
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
 * Clothes from her wardrobe (entity/Wardrobe), drawn over her own. The clothes themselves are painted onto her own model
 * (textures/entity/outfit/&lt;model&gt;_&lt;outfit&gt;.png, by tools/gen_outfit_overlays.py: coat or kimono on her torso and sleeves, hems by
 * her own knees and ankles) and drawn as her model again with that texture, the way dyed wool and the paints are drawn, so they follow
 * every pose. What sticks out (the hood, the knitted hat, the hair flower and the obi's bow, the frilled headband) are small boxes on her
 * head and back, from the 4x4 grid in textures/entity/outfits.png (one row per outfit).
 */
public class OutfitLayer extends GeoRenderLayer<AbstractMonsterGirl> {
    private static final RenderType TYPE = RenderType.entityCutoutNoCull(HarmontideMod.id("textures/entity/outfits.png"));
    private static final float U = 1.0F / 16.0F;
    private static final int GRID = 4, TRIM = 1, HEAD = 3;

    public OutfitLayer(GirlRenderer renderer) {
        super(renderer);
    }

    /** Which overlay textures exist (a model added later without its overlays just goes without). */
    private static final java.util.Map<net.minecraft.resources.ResourceLocation, Boolean> EXISTS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void render(PoseStack pose, AbstractMonsterGirl girl, software.bernie.geckolib.cache.object.BakedGeoModel model, RenderType renderType,
                       MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
        int code = girl.getOutfit();
        if (code <= 0 || code > Wardrobe.Outfit.values().length || girl.isBaby()) return;
        String outfit = Wardrobe.Outfit.values()[code - 1].name().toLowerCase(java.util.Locale.ROOT);
        String modelName = this.getGeoModel().getModelResource(girl).getPath().replace("geo/entity/", "").replace(".geo.json", "");
        net.minecraft.resources.ResourceLocation tex = HarmontideMod.id("textures/entity/outfit/" + modelName + "_" + outfit + ".png");
        if (!EXISTS.computeIfAbsent(tex, t -> net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(t).isPresent())) return;
        RenderType cloth = RenderType.entityCutoutNoCull(tex);
        this.getRenderer().reRender(model, pose, buffers, girl, cloth, buffers.getBuffer(cloth), partialTick, light, overlay, -1);
    }

    /** A bone's own extent, in model units: half width, lowest and highest point, front and back. */
    private record Extent(float half, float low, float high, float front, float back) {
        static Extent of(GeoBone bone) {
            float half = 0, low = Float.MAX_VALUE, high = -Float.MAX_VALUE, front = Float.MAX_VALUE, back = -Float.MAX_VALUE;
            for (GeoCube cube : bone.getCubes()) {
                for (GeoQuad quad : cube.quads()) {
                    if (quad == null) continue;
                    for (GeoVertex v : quad.vertices()) {
                        float x = v.position().x() / U, y = v.position().y() / U, z = v.position().z() / U;
                        half = Math.max(half, Math.abs(x));
                        low = Math.min(low, y);
                        high = Math.max(high, y);
                        front = Math.min(front, z);
                        back = Math.max(back, z);
                    }
                }
            }
            return half <= 0 ? null : new Extent(half, low, high, front, back);
        }
    }

    @Override
    public void renderForBone(PoseStack pose, AbstractMonsterGirl girl, GeoBone bone, RenderType renderType, MultiBufferSource buffers,
                              VertexConsumer buffer, float partialTick, int light, int overlay) {
        int code = girl.getOutfit();
        if (code <= 0 || code > Wardrobe.Outfit.values().length || girl.isBaby()) return;
        boolean body = bone.getName().equals("body"), head = bone.getName().equals("head");
        if (!body && !head) return;
        Extent e = Extent.of(bone);
        if (e == null) return;
        Wardrobe.Outfit outfit = Wardrobe.Outfit.values()[code - 1];
        int row = outfit.ordinal();
        Box b = new Box(pose.last(), buffers.getBuffer(TYPE), light, overlay);
        float h = e.half, lo = e.low, hi = e.high, f = e.front, k = e.back, mid = (f + k) / 2;
        switch (outfit) {
            case RAINCOAT -> {
                if (!body) {
                    b.box(-h - 0.5F, hi - 3.5F, mid - 0.5F, h + 0.5F, hi + 0.6F, k + 0.6F, row, HEAD);      // the hood, pushed back
                }
            }
            case WINTER_COAT -> {
                if (!body) {
                    b.box(-h - 0.4F, hi - 0.6F, f + 0.5F, h + 0.4F, hi + 1.4F, k + 0.4F, row, HEAD);        // a knitted hat
                }
            }
            case YUKATA -> {
                if (body) {
                    float obi = bone.getPivotY() + (hi - bone.getPivotY()) * 0.32F;                         // the painted obi's height
                    b.box(-1.2F, obi - 0.4F, k + 0.1F, 1.2F, obi + 1.6F, k + 1.0F, row, TRIM);             // the bow at her back
                } else {
                    b.box(h - 0.2F, hi - 1.0F, mid - 1.0F, h + 0.8F, hi, mid + 1.0F, row, HEAD);           // a hair flower
                }
            }
            case MAID_APRON -> {
                if (!body) {
                    b.box(-h - 0.2F, hi - 0.3F, f + 1.5F, h + 0.2F, hi + 0.9F, f + 2.5F, row, HEAD);       // frilled headband
                }
            }
        }
    }

    /** Boxes in model units, each face filled with one cell of the outfit grid. */
    private record Box(PoseStack.Pose pose, VertexConsumer v, int light, int overlay) {
        void box(float x0, float y0, float z0, float x1, float y1, float z1, int row, int col) {
            float u = col / (float) GRID, w = row / (float) GRID;
            this.quad(u, w, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            this.quad(u, w, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
            this.quad(u, w, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
            this.quad(u, w, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
            this.quad(u, w, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            this.quad(u, w, 0, -1, 0, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
        }

        private void quad(float u, float w, float nx, float ny, float nz, float... c) {
            float s = 1.0F / GRID;
            float[][] uv = {{u, w + s}, {u, w}, {u + s, w}, {u + s, w + s}};
            for (int i = 0; i < 4; i++) {
                this.v.addVertex(this.pose, c[i * 3] * U, c[i * 3 + 1] * U, c[i * 3 + 2] * U).setColor(-1).setUv(uv[i][0], uv[i][1])
                        .setOverlay(this.overlay).setLight(this.light).setNormal(this.pose, nx, ny, nz);
            }
        }
    }
}
