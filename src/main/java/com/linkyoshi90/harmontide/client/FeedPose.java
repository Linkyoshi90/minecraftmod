package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Breastfeed;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * A feed at her chest, laid over idle. A tall girl stands and holds him up. A girl his own size bends so her chest
 * comes down over his head. A smaller one has already climbed, and her arms close around his shoulders.
 * Missing bones are skipped. The baby at her own breast stays on the nurse pose.
 */
final class FeedPose {
    private FeedPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!feeding(mob)) return;
        float breath = (Mth.sin((mob.tickCount + partialTick) * 0.2F) + 1.0F) * 0.5F;
        switch (Breastfeed.kind(mob)) {
            case TOWER -> tower(proc);
            case BEND -> bend(proc, breath);
            case SMALL -> small(proc, breath);
        }
    }

    private static boolean feeding(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) return girl.getAction() == AbstractMonsterGirl.Action.FEED;
        if (mob instanceof AbstractHarpy harpy) return harpy.getAction() == AbstractHarpy.Action.FEED;
        if (mob instanceof AbstractAnuran anuran) return anuran.getAction() == AbstractAnuran.Action.FEED;
        return false;
    }

    /** Upright, chest offered, arms gathered around the person she lifted. */
    private static void tower(AnimationProcessor<?> proc) {
        CowgirlPose.setRot(proc, "body", 10, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", -78, -16, 14);
        CowgirlPose.setRot(proc, "arm_l", -78, 16, -14);
        CowgirlPose.setRot(proc, "forearm_r", -58, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -58, 0, 0);
        CowgirlPose.setRot(proc, "head", 24, 0, 0);
    }

    /** Folded forward, chest lowered over his head, arms around it. */
    private static void bend(AnimationProcessor<?> proc, float breath) {
        CowgirlPose.setRot(proc, "body", -56.0F - breath * 6.0F, 0, 0);
        CowgirlPose.setPos(proc, "body", 0, -2.5F, 1.5F);
        CowgirlPose.setRot(proc, "head", 34, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", -42, -20, 18);
        CowgirlPose.setRot(proc, "arm_l", -42, 20, -18);
        CowgirlPose.setRot(proc, "forearm_r", -72, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -72, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -16, 0, 0);
        CowgirlPose.setRot(proc, "leg_l", -16, 0, 0);
        CowgirlPose.setRot(proc, "shin_r", 20, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 20, 0, 0);
    }

    /** Clung to his shoulders. Her feet are already up at his chest. */
    private static void small(AnimationProcessor<?> proc, float breath) {
        CowgirlPose.setRot(proc, "arm_r", -90, -8, 22.0F + breath * 4.0F);
        CowgirlPose.setRot(proc, "arm_l", -90, 8, -(22.0F + breath * 4.0F));
        CowgirlPose.setRot(proc, "forearm_r", -78, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -78, 0, 0);
        CowgirlPose.setRot(proc, "head", 10, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -28, 0, 6);
        CowgirlPose.setRot(proc, "leg_l", -28, 0, -6);
        CowgirlPose.setRot(proc, "shin_r", 34, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 34, 0, 0);
    }
}
