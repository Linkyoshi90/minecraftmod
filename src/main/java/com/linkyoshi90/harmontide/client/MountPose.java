package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * One behind the other. The one behind wears the strap and thrusts. The one in front is bent and taken.
 * A taur uses the barrel and the hindquarters. A bull uses two legs. Missing bones are skipped.
 */
final class MountPose {
    private MountPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        float thrust = (float) Math.sin((mob.tickCount + partialTick) * 0.55);
        if (Nsfw.mounting(mob)) behind(proc, mob, thrust);
        else if (Nsfw.presented(mob)) front(proc, mob, thrust);
    }

    private static void behind(AnimationProcessor<?> proc, Mob mob, float thrust) {
        if (Nsfw.quadruped(mob)) {
            CowgirlPose.setPos(proc, "body", 0, -0.4F, thrust * -1.4F);
            CowgirlPose.setPos(proc, "barrel", 0, -0.3F, thrust * -1.6F);
            CowgirlPose.setRot(proc, "body", 16.0F + thrust * 6.0F, 0, 0);
            CowgirlPose.setRot(proc, "barrel", 22.0F + thrust * 8.0F, 0, 0);
            CowgirlPose.setRot(proc, "tail", -36.0F + thrust * 10.0F, 0, 0);
            return;
        }
        CowgirlPose.setPos(proc, "body", 0, -0.8F, thrust * -1.8F);
        CowgirlPose.setRot(proc, "body", 32.0F + thrust * 6.0F, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", 14.0F + thrust * 4.0F, 4, 6);
        CowgirlPose.setRot(proc, "leg_l", 14.0F + thrust * 4.0F, -4, -6);
        CowgirlPose.setRot(proc, "shin_r", 18, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 18, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", 48, 10, 14);
        CowgirlPose.setRot(proc, "arm_l", 48, -10, -14);
        CowgirlPose.setRot(proc, "head", -14.0F - thrust * 3.0F, 0, 0);
    }

    private static void front(AnimationProcessor<?> proc, Mob mob, float thrust) {
        if (Nsfw.quadruped(mob)) {
            CowgirlPose.setRot(proc, "body", 38.0F + thrust * 3.0F, 0, 0);
            CowgirlPose.setRot(proc, "barrel", -6.0F + thrust * 2.0F, 0, 0);
            CowgirlPose.setPos(proc, "barrel", 0, -0.8F, 0.4F);
            CowgirlPose.setRot(proc, "tail", 46.0F - thrust * 8.0F, 0, 0);
            CowgirlPose.setRot(proc, "head", 12, 0, 0);
            return;
        }
        CowgirlPose.setPos(proc, "body", 0, -4.5F, 1.2F);
        CowgirlPose.setRot(proc, "body", 62.0F + thrust * 4.0F, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", 8, 3, 4);
        CowgirlPose.setRot(proc, "leg_l", 8, -3, -4);
        CowgirlPose.setRot(proc, "shin_r", 16, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 16, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", 70, 6, 4);
        CowgirlPose.setRot(proc, "arm_l", 70, -6, -4);
        CowgirlPose.setRot(proc, "head", 18.0F - thrust * 2.0F, 0, 0);
    }
}
