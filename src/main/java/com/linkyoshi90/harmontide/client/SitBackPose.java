package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * A taur sitting back onto the sleeper: her own sit clip folds the four legs, and this settles the back down onto him.
 * She faces the foot of the bed, so the back that lowers is the one toward his face.
 */
final class SitBackPose {
    private SitBackPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.sittingBack(mob)) return;
        // 0 at the top of the bounce, 1 when she has sat down.
        float settle = (1.0F - (float) Nsfw.bounce(mob.tickCount + partialTick)) * 0.5F;
        float drop = -8.5F - settle * 3.2F;
        float back = 2.2F + settle * 1.4F;
        CowgirlPose.setPos(proc, "body", 0, drop, back);
        CowgirlPose.setPos(proc, "barrel", 0, drop, back);
        CowgirlPose.setRot(proc, "body", -16.0F - settle * 8.0F, 0, 0);
    }
}
