package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * The cattle milk clip, applied as a pose: a small lean, head turned, forearms down over the pail.
 * Cattle play that clip themselves. Anyone else has no such clip, so this stands in while her top is off.
 */
final class MilkPose {
    private MilkPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob) {
        if (!leaning(mob)) return;
        CowgirlPose.setRot(proc, "body", -4, 0, 0);
        CowgirlPose.setRot(proc, "head", 10, 20, 0);
        CowgirlPose.setRot(proc, "arm_r", -20, 0, 14);
        CowgirlPose.setRot(proc, "arm_l", -20, 0, -14);
        CowgirlPose.setRot(proc, "forearm_r", -60, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -60, 0, 0);
    }

    private static boolean leaning(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) {
            return girl.hasFlag(AbstractMonsterGirl.FLAG_BARE) && girl.getAction() != AbstractMonsterGirl.Action.MILK;
        }
        if (mob instanceof AbstractHarpy harpy) return harpy.isBare();
        if (mob instanceof AbstractAnuran anuran) return anuran.isBare();
        return false;
    }
}
