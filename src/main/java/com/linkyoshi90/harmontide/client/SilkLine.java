package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Arachne;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** The thread an arachne swings on (entity/girl/Arachne): a pale line from her shoulders up to the block it hangs from. */
public final class SilkLine {
    private SilkLine() {}

    public static void render(Arachne girl, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        BlockPos anchor = girl.getSwingAnchor().orElse(null);
        if (anchor == null) return;
        Vec3 me = girl.getPosition(partialTick);
        Vec3 hook = Vec3.atBottomCenterOf(anchor).subtract(me);
        float top = girl.getBbHeight() * 0.85F;
        Vec3 n = hook.subtract(0, top, 0).normalize();
        VertexConsumer line = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose last = pose.last();
        line.addVertex(last, 0.0F, top, 0.0F).setColor(238, 238, 248, 230).setNormal(last, (float) n.x, (float) n.y, (float) n.z);
        line.addVertex(last, (float) hook.x, (float) hook.y, (float) hook.z).setColor(238, 238, 248, 230).setNormal(last, (float) n.x, (float) n.y, (float) n.z);
    }

    /** A small friend on the player's own shoulder is not drawn in first person (she would sit in front of the camera). */
    public static boolean hiddenOnShoulder(AbstractMonsterGirl girl) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && girl.getVehicle() == mc.player && mc.options.getCameraType().isFirstPerson();
    }
}
