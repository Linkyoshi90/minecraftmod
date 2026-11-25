package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * A biped kneeling astride the sleeper, folded out of the sit pose and bouncing with {@link Nsfw#bounce}.
 * Laid over idle, on the leg bones the rigs share. Missing bones are skipped, so a strange rig just stands.
 */
final class CowgirlPose {
    private CowgirlPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.kneeling(mob)) return;
        float down = (1.0F - (float) Nsfw.bounce(mob.tickCount + partialTick)) * 0.5F;
        float drop = -6.0F - down * 1.4F;
        setPos(proc, "body", 0, drop, 0);
        setRot(proc, "body", 8.0F + down * 6.0F, 0, 0);
        setPos(proc, "leg_r", 0, drop, 0);
        setPos(proc, "leg_l", 0, drop, 0);
        setRot(proc, "leg_r", -70.0F - down * 16.0F, 0, 26);
        setRot(proc, "leg_l", -70.0F - down * 16.0F, 0, -26);
        setRot(proc, "shin_r", 118.0F + down * 24.0F, 0, 0);
        setRot(proc, "shin_l", 118.0F + down * 24.0F, 0, 0);
        setRot(proc, "foot_r", -58, 0, 0);
        setRot(proc, "foot_l", -58, 0, 0);
        setRot(proc, "arm_r", -28, 12, 6);
        setRot(proc, "arm_l", -28, -12, -6);
        setRot(proc, "forearm_r", -36, 0, 0);
        setRot(proc, "forearm_l", -36, 0, 0);
        setRot(proc, "head", -18, 0, 0);
    }

    /** Keyframe degrees, converted the way GeckoLib converts them. Forward is negative x. */
    static void setRot(AnimationProcessor<?> proc, String name, float x, float y, float z) {
        GeoBone bone = proc.getBone(name);
        if (bone == null) return;
        var rest = bone.getInitialSnapshot();
        bone.setRotX(rest.getRotX() - x * Mth.DEG_TO_RAD);
        bone.setRotY(rest.getRotY() - y * Mth.DEG_TO_RAD);
        bone.setRotZ(rest.getRotZ() + z * Mth.DEG_TO_RAD);
    }

    /** A position in model pixels, as an animation keyframe writes it. */
    static void setPos(AnimationProcessor<?> proc, String name, float x, float y, float z) {
        GeoBone bone = proc.getBone(name);
        if (bone == null) return;
        bone.setPosX(x);
        bone.setPosY(y);
        bone.setPosZ(z);
    }
}
