package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.leviathan.DreamLeviathan;
import com.linkyoshi90.harmontide.leviathan.LeviathanNet;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import javax.annotation.Nullable;

/**
 * The Dream-Leviathan seen from far away: not an entity but a projection, like the sun and moon. The server tells every overworld
 * client where she is (leviathan/LeviathanNet.Sky, every half second); this draws her body there, scaled in toward the camera so she
 * stays inside the view distance while keeping her true size in the sky. Fog is lifted for her; she fades a little with distance.
 * As soon as her real body (leviathan/DreamLeviathan) is near enough to be drawn as an entity, this stops.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class DreamSky {
    @Nullable
    private static LeviathanNet.Sky last;
    private static long at;
    @Nullable
    private static Vec3 shown;
    private static float shownYaw;
    private static DreamLeviathanModel model;

    private DreamSky() {
    }

    public static void update(LeviathanNet.Sky sky) {
        Minecraft mc = Minecraft.getInstance();
        last = sky;
        at = mc.level == null ? 0 : mc.level.getGameTime();
        if (!sky.shown()) shown = null;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        last = null;
        shown = null;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        LeviathanNet.Sky s = last;
        if (mc.level == null || s == null || !s.shown() || mc.level.dimension() != Level.OVERWORLD) return;
        long now = mc.level.getGameTime();
        if (now - DreamLeviathan.clientSeen < 4) {                                        // her body is here: it draws her
            shown = null;
            return;
        }
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double dt = Math.min(30, now - at + pt);
        Vec3 want = new Vec3(s.x() + s.vx() * dt, s.y() + s.vy() * dt, s.z() + s.vz() * dt);
        shown = shown == null || shown.distanceTo(want) > 80 ? want : shown.lerp(want, 0.2);
        shownYaw = Mth.rotLerp(0.2F, shownYaw, s.yaw());
        Vec3 cam = event.getCamera().getPosition();
        Vec3 rel = shown.subtract(cam);
        double d = rel.length();
        if (d < 1) return;
        double far = mc.options.getEffectiveRenderDistance() * 16 * 0.85;
        double k = d > far ? far / d : 1.0;
        float alpha = (float) Mth.clamp(1.3 - d / 2600.0, 0.3, 1.0);
        if (model == null) model = new DreamLeviathanModel();
        PoseStack ps = new PoseStack();
        ps.translate(rel.x * k, rel.y * k, rel.z * k);
        ps.scale((float) k, (float) k, (float) k);
        float t = now + pt;
        if (s.mode() == DreamLeviathan.FLYING || s.mode() == DreamLeviathan.RISING) ps.translate(0, Mth.sin(t * 0.04F) * 0.6F, 0);
        DreamLeviathanModel.place(ps, shownYaw, s.pitch(), DreamLeviathan.SCALE, s.mode() == DreamLeviathan.DEAD, true);
        model.pose(s.mode(), t);
        float fogStart = RenderSystem.getShaderFogStart(), fogEnd = RenderSystem.getShaderFogEnd();
        FogShape fogShape = RenderSystem.getShaderFogShape();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        float glow = s.sick() >= 3 ? 0.45F : s.sick() > 0 ? 0.75F : 1.0F;
        model.render(ps, buffers, DreamLeviathanModel.skin(s.mode(), s.sick(), s.rot()), DreamLeviathanModel.glow(s.mode(), s.sick()),
                LightTexture.pack(0, 15), OverlayTexture.NO_OVERLAY, alpha, glow);
        buffers.endBatch();
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
        RenderSystem.setShaderFogShape(fogShape);
    }
}
