package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * Hips to hips with another girl. A biped hooks a thigh across and grinds. A serpent or a mermaid stays upright and
 * coils in. A spider leans onto her. A slime presses and gives. Missing bones are skipped.
 */
final class TribePose {
    private TribePose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.tribing(mob)) return;
        float grind = (float) Math.sin((mob.tickCount + partialTick) * 0.42);
        if (Nsfw.bipedal(mob)) scissor(proc, grind);
        else if (mob instanceof AbstractMonsterGirl girl) {
            GirlSpecies.Family family = girl.getSpecies().family();
            if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK) coil(proc, grind);
            else if (family == GirlSpecies.Family.ARACHNE) lean(proc, grind);
            else if (family == GirlSpecies.Family.SLIME) press(proc, grind);
            else scissor(proc, grind);
        }
    }

    /** One thigh hooked across hers, chest in, hips rolling. */
    private static void scissor(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", grind * 0.6F, -2.4F, grind * -1.1F);
        CowgirlPose.setRot(proc, "body", 18.0F + grind * 5.0F, 0, 11);
        CowgirlPose.setRot(proc, "leg_r", -88.0F + grind * 7.0F, 18, 22);
        CowgirlPose.setRot(proc, "shin_r", 64.0F - grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "foot_r", -16, 0, 0);
        CowgirlPose.setRot(proc, "leg_l", 24.0F + grind * 3.0F, -8, -14);
        CowgirlPose.setRot(proc, "shin_l", 12, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", -58, 14, 8);
        CowgirlPose.setRot(proc, "arm_l", -22, -20, -12);
        CowgirlPose.setRot(proc, "forearm_r", -30, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -18, 0, 0);
        CowgirlPose.setRot(proc, "head", -6.0F + grind * 3.0F, grind * 6.0F, 0);
    }

    /** Upright. The tail curves in around her and the hips rock. No face-down lay. */
    private static void coil(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setRot(proc, "body", 20.0F + grind * 7.0F, grind * 4.0F, 0);
        CowgirlPose.setPos(proc, "body", 0, grind * -0.6F, grind * -1.2F);
        CowgirlPose.setRot(proc, "head", -8.0F, grind * 10.0F, 0);
        CowgirlPose.setRot(proc, "tail", 34.0F + grind * 12.0F, 18, 0);
        CowgirlPose.setRot(proc, "tail_2", 26.0F - grind * 6.0F, 14, 0);
        CowgirlPose.setRot(proc, "tail_3", 14.0F + grind * 8.0F, 8, 0);
        CowgirlPose.setRot(proc, "seg_1", 16.0F + grind * 8.0F, 20, 0);
        CowgirlPose.setRot(proc, "seg_2", 22.0F - grind * 5.0F, 12, 0);
        CowgirlPose.setRot(proc, "seg_3", 10.0F + grind * 6.0F, 6, 0);
    }

    /** Over her on her feet, front legs folding. */
    private static void lean(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -2.2F + grind * -0.5F, grind * -0.8F);
        CowgirlPose.setRot(proc, "body", 44.0F + grind * 5.0F, 0, 0);
        CowgirlPose.setPos(proc, "abdomen", 0, grind * 0.6F, 0.4F);
        CowgirlPose.setRot(proc, "leg_r", -18, 8, 16);
        CowgirlPose.setRot(proc, "leg_l", 12, -6, -10);
        CowgirlPose.setRot(proc, "head", 8.0F - grind * 3.0F, 0, 0);
    }

    /** Pressed flat to her, the body giving. */
    private static void press(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -3.2F + grind * -1.6F, grind * -0.7F);
        CowgirlPose.setRot(proc, "body", 12.0F + grind * 4.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", -4, grind * 5.0F, 0);
    }
}
