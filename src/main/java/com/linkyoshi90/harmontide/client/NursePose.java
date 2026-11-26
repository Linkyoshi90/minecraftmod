package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * Nursing (AbstractMonsterGirl.Action.NURSE), laid over the sit animation on the shared arm bones as AimPose is: the mother's arms
 * folded in front of her, cradling the baby held at her chest; the baby's arms reach round her mother. The baby stays clothed.
 * With her top up the arms open so the baby is held against the breast. Clothed, the cradle is unchanged.
 */
final class NursePose {
    private NursePose() {
    }

    static void apply(AnimationProcessor<AbstractMonsterGirl> proc, AbstractMonsterGirl girl) {
        if (girl.getAction() != AbstractMonsterGirl.Action.NURSE) return;
        if (girl.isBaby()) {
            AimPose.set(proc, "arm_r", -110, -25, 0);
            AimPose.set(proc, "arm_l", -110, 25, 0);
            AimPose.set(proc, "forearm_r", -20, 0, 0);
            AimPose.set(proc, "forearm_l", -20, 0, 0);
            return;
        }
        if (girl.hasFlag(AbstractMonsterGirl.FLAG_BARE)) {
            AimPose.set(proc, "arm_r", -68, -10, 14);               // open, so the baby rests on the breast
            AimPose.set(proc, "arm_l", -76, 16, -12);
            AimPose.set(proc, "forearm_r", -34, -16, 0);
            AimPose.set(proc, "forearm_l", -34, 20, 0);
            AimPose.set(proc, "head", 30, 0, 0);                    // still looking down at her
            return;
        }
        AimPose.set(proc, "arm_r", -40, -25, 0);                    // one arm under the baby, the other round her back
        AimPose.set(proc, "arm_l", -55, 30, 0);
        AimPose.set(proc, "forearm_r", 0, -75, 0);
        AimPose.set(proc, "forearm_l", 0, 80, 0);
        AimPose.set(proc, "head", 25, 0, 0);                        // looking down at her
    }
}
