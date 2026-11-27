package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Over his face, while someone else rides. A biped straddles and grinds, looking down the bed.
 * A taur lowers her front. A serpent or a mermaid stays upright and curls over the pillow.
 * A spider stands wide. A slime settles and gives. Missing bones are skipped.
 */
final class FacePose {
    private FacePose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.facing(mob)) return;
        float grind = (float) Math.sin((mob.tickCount + partialTick) * 0.45);
        if (Nsfw.bipedal(mob)) straddle(proc, grind);
        else if (Nsfw.quadruped(mob)) front(proc, grind);
        else if (mob instanceof AbstractMonsterGirl girl) {
            GirlSpecies.Family family = girl.getSpecies().family();
            if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK) curl(proc, grind);
            else if (family == GirlSpecies.Family.ARACHNE) wide(proc, grind);
            else if (family == GirlSpecies.Family.SLIME) settle(proc, grind);
            else straddle(proc, grind);
        }
    }

    /** Hips down on his face, knees wide, eyes toward the one riding. */
    private static void straddle(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -9.5F + grind * -1.4F, 0.8F);
        CowgirlPose.setRot(proc, "body", 8.0F + grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -36, 26, 34);
        CowgirlPose.setRot(proc, "leg_l", -36, -26, -34);
        CowgirlPose.setRot(proc, "shin_r", 78.0F - grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 78.0F - grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "foot_r", -10, 0, 0);
        CowgirlPose.setRot(proc, "foot_l", -10, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", -70, 10, 16);
        CowgirlPose.setRot(proc, "arm_l", -70, -10, -16);
        CowgirlPose.setRot(proc, "forearm_r", -20, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -20, 0, 0);
        CowgirlPose.setRot(proc, "head", -14.0F - grind * 3.0F, 0, 0);
    }

    /** Too large to straddle: the front of her comes down over his face. */
    private static void front(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -2.2F, 0.6F);
        CowgirlPose.setRot(proc, "body", 36.0F + grind * 4.0F, 0, 0);
        CowgirlPose.setRot(proc, "barrel", 8, 0, 0);
        CowgirlPose.setRot(proc, "neck", 14, 0, 0);
        CowgirlPose.setRot(proc, "head", -22.0F - grind * 3.0F, 0, 0);
        CowgirlPose.setRot(proc, "tail", 12.0F + grind * 6.0F, 0, 0);
    }

    /** Upright. She curls over the pillow instead of lying along him. */
    private static void curl(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -2.0F + grind * -1.2F, 1.0F);
        CowgirlPose.setRot(proc, "body", 26.0F + grind * 7.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", -8.0F - grind * 3.0F, 0, 0);
        CowgirlPose.setRot(proc, "tail", 48.0F + grind * 6.0F, -22, 0);
        CowgirlPose.setRot(proc, "tail_2", 30.0F - grind * 5.0F, -14, 0);
        CowgirlPose.setRot(proc, "tail_3", 14.0F + grind * 4.0F, -8, 0);
        CowgirlPose.setRot(proc, "seg_1", 26.0F + grind * 5.0F, -20, 0);
        CowgirlPose.setRot(proc, "seg_2", 18.0F - grind * 4.0F, -12, 0);
        CowgirlPose.setRot(proc, "fin_1", 22.0F + grind * 5.0F, -16, 0);
        CowgirlPose.setRot(proc, "sp_1", 20.0F + grind * 4.0F, -14, 0);
    }

    /** Standing over his head, legs set wide and folding. */
    private static void wide(AnimationProcessor<?> proc, float grind) {
        CowgirlPose.setPos(proc, "body", 0, -3.2F + grind * -0.9F, 0.5F);
        CowgirlPose.setRot(proc, "body", 40.0F + grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", -18.0F - grind * 3.0F, 0, 0);
        for (int i = 1; i <= 4; i++) {
            float spread = i <= 2 ? 1.0F : 0.45F;
            CowgirlPose.setRot(proc, "leg" + i + "_r", -20.0F - spread * 18.0F, 0, 22);
            CowgirlPose.setRot(proc, "leg" + i + "_l", -20.0F - spread * 18.0F, 0, -22);
            CowgirlPose.setRot(proc, "leg" + i + "b_r", 55.0F + grind * 6.0F, 0, 0);
            CowgirlPose.setRot(proc, "leg" + i + "b_l", 55.0F + grind * 6.0F, 0, 0);
        }
    }

    /** She spreads over his face and gives. */
    private static void settle(AnimationProcessor<?> proc, float grind) {
        float give = (1.0F - grind) * 0.5F;
        squish(proc, "body", give);
        squish(proc, "gown", give);
        squish(proc, "core", give);
        CowgirlPose.setPos(proc, "body", 0, -5.0F + grind * -1.1F, 0.3F);
        CowgirlPose.setRot(proc, "body", 6.0F + grind * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", -12.0F - grind * 4.0F, 0, 0);
    }

    private static void squish(AnimationProcessor<?> proc, String name, float give) {
        GeoBone bone = proc.getBone(name);
        if (bone == null) return;
        bone.setScaleX(1.0F + give * 0.08F);
        bone.setScaleY(1.0F - give * 0.16F);
        bone.setScaleZ(1.0F + give * 0.08F);
    }
}
