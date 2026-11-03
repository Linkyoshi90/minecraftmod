package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Aiming a weapon (GirlArms): laid over whatever animation plays, on the arm bones every fighter's rig shares (arm_*,
 * forearm_*, hand_*). The angles are written as keyframe degrees, as in our animation files (forward is negative x;
 * positive y swings either arm toward her right) and converted the way GeckoLib converts keyframes (x and y negated).
 * Both arms follow her head's pitch.
 *
 * <ul>
 *   <li>Bow (in her right hand): the bow arm straight out, the left drawing the string back across her chest; it
 *   snaps forward when she looses and draws again over the next second.</li>
 *   <li>Crossbow and gun: both arms forward, the left hand under the stock; each shot kicks the arms up a moment.</li>
 * </ul>
 */
final class AimPose {
    private AimPose() {
    }

    static void apply(AnimationProcessor<AbstractMonsterGirl> proc, AbstractMonsterGirl girl, float headPitch, float partialTick) {
        int aim = girl.getAim();
        if (aim == AbstractMonsterGirl.AIM_NONE) return;
        float since = girl.tickCount + partialTick - girl.lastShotTick();
        float kick = aim != AbstractMonsterGirl.AIM_BOW && since >= 0 && since < 6 ? 14.0F * (1.0F - since / 6.0F) : 0.0F;
        float pitch = Mth.clamp(headPitch, -60.0F, 60.0F);
        switch (aim) {
            case AbstractMonsterGirl.AIM_BOW -> {
                float draw = since < 0 ? 1.0F : Mth.clamp((since - 2.0F) / 16.0F, 0.0F, 1.0F);   // released, then drawn again
                set(proc, "arm_r", -90 + pitch, -5, 0);
                set(proc, "arm_l", -90 + pitch, 20 + 30 * draw, 0);
                set(proc, "forearm_l", 0, 100 * draw, 0);
            }
            case AbstractMonsterGirl.AIM_CROSSBOW -> {
                set(proc, "arm_r", -85 + pitch - kick, -15, 0);
                set(proc, "arm_l", -85 + pitch - kick, 35, 0);
                set(proc, "forearm_l", -15, 0, 0);
            }
            default -> {
                set(proc, "arm_r", -90 + pitch - kick, -8, 0);
                set(proc, "arm_l", -85 + pitch - kick, 40, 0);
                set(proc, "forearm_l", -10, 0, 0);
            }
        }
        set(proc, "forearm_r", 0, 0, 0);                                        // the weapon arm straight
        set(proc, "hand_r", 0, 0, 0);
        set(proc, "hand_l", 0, 0, 0);
    }

    /** A bone's rotation in keyframe degrees, over its rest pose. */
    static void set(AnimationProcessor<AbstractMonsterGirl> proc, String name, float x, float y, float z) {
        GeoBone b = proc.getBone(name);
        if (b == null) return;
        var rest = b.getInitialSnapshot();
        b.setRotX(rest.getRotX() - x * Mth.DEG_TO_RAD);
        b.setRotY(rest.getRotY() - y * Mth.DEG_TO_RAD);
        b.setRotZ(rest.getRotZ() + z * Mth.DEG_TO_RAD);
    }
}
