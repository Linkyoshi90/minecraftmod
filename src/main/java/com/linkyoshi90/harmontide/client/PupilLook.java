package com.linkyoshi90.harmontide.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * Eyes that follow you: tools/beautify_model.py gives every face its pupils and highlights as small pieces of their own
 * (bones pupil_r, pupil_l) in front of the eyes, and the renderers slide them towards the player here.
 */
public final class PupilLook {
    /** how far the pupils may slide, in model pixels */
    private static final float MAX_X = 0.25F, MAX_Y = 0.2F;

    private PupilLook() {}

    /** Slides the pupil bones (pupil_r, pupil_l) towards the player, past what the head turn already covers. */
    public static void lookAt(GeoModel<?> model, LivingEntity girl) {
        GeoBone right = model.getAnimationProcessor().getBone("pupil_r");
        GeoBone left = model.getAnimationProcessor().getBone("pupil_l");
        if (right == null && left == null) return;
        float x = 0, y = 0;
        Player player = Minecraft.getInstance().player;
        if (player != null && player != girl && !girl.isSleeping() && player.distanceToSqr(girl) < 16 * 16) {
            Vec3 to = player.getEyePosition().subtract(girl.getEyePosition());
            double yaw = Math.toRadians(girl.getYHeadRot());
            double side = -to.x * Math.cos(yaw) - to.z * Math.sin(yaw);       // towards her right
            double ahead = -to.x * Math.sin(yaw) + to.z * Math.cos(yaw);
            double flat = Math.sqrt(to.x * to.x + to.z * to.z);
            if (ahead > 0) {
                double turn = Math.toDegrees(Math.atan2(side, ahead));
                double lift = Math.toDegrees(Math.atan2(to.y, flat)) + girl.getXRot();
                x = (float) Mth.clamp(turn / 35.0, -1.0, 1.0);
                y = (float) Mth.clamp(lift / 30.0, -1.0, 1.0);
            }
        }
        // her right is model -x (arm_r sits at -x)
        for (GeoBone bone : new GeoBone[]{right, left}) {
            if (bone == null) continue;
            bone.setPosX(-x * MAX_X);
            bone.setPosY(y * MAX_Y);
        }
    }
}
