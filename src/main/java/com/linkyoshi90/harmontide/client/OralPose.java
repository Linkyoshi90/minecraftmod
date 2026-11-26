package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.MorningOral;
import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * The morning after, laid over idle. A biped kneels and lowers her head. A taur bows hers down.
 * A serpent or a mermaid stays low and reaches. A spider bends. A slime settles. Missing bones are skipped.
 */
final class OralPose {
    private OralPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!MorningOral.posing(mob)) return;
        float bob = Mth.sin((mob.tickCount + partialTick) * 0.45F);
        if (Nsfw.bipedal(mob)) kneel(proc, bob);
        else if (Nsfw.quadruped(mob)) bow(proc, bob);
        else if (mob instanceof AbstractMonsterGirl girl) family(proc, girl.getSpecies().family(), bob);
        else kneel(proc, bob);
    }

    /** On her knees, head down at his lap, hands on his thighs. */
    private static void kneel(AnimationProcessor<?> proc, float bob) {
        CowgirlPose.setPos(proc, "body", 0, -8.0F, 1.2F);
        CowgirlPose.setRot(proc, "body", -28, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -82, 0, 24);
        CowgirlPose.setRot(proc, "leg_l", -82, 0, -24);
        CowgirlPose.setRot(proc, "shin_r", 118, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 118, 0, 0);
        CowgirlPose.setRot(proc, "foot_r", -40, 0, 0);
        CowgirlPose.setRot(proc, "foot_l", -40, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", 36, 14, 10);
        CowgirlPose.setRot(proc, "arm_l", 36, -14, -10);
        CowgirlPose.setRot(proc, "forearm_r", -48, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -48, 0, 0);
        CowgirlPose.setRot(proc, "head", -62.0F + bob * 8.0F, 0, 0);
    }

    /** Too large to kneel: the front of her comes down. */
    private static void bow(AnimationProcessor<?> proc, float bob) {
        CowgirlPose.setRot(proc, "body", -34, 0, 0);
        CowgirlPose.setRot(proc, "neck", -22, 0, 0);
        CowgirlPose.setRot(proc, "head", -68.0F + bob * 6.0F, 0, 0);
    }

    private static void family(AnimationProcessor<?> proc, GirlSpecies.Family family, float bob) {
        switch (family) {
            case LAMIA, MERFOLK -> {
                CowgirlPose.setPos(proc, "root", 0, -2.0F, 0);
                CowgirlPose.setRot(proc, "body", -32, 0, 0);
                CowgirlPose.setRot(proc, "head", -58.0F + bob * 8.0F, 0, 0);
                CowgirlPose.setRot(proc, "seg_1", 14, 0, 0);
                CowgirlPose.setRot(proc, "tail", 10, 0, 0);
                CowgirlPose.setRot(proc, "fin_1", 14, 0, 0);
            }
            case ARACHNE -> {
                CowgirlPose.setPos(proc, "body", 0, -4.0F, 0.8F);
                CowgirlPose.setRot(proc, "body", -22, 0, 0);
                CowgirlPose.setRot(proc, "thorax", 12, 0, 0);
                CowgirlPose.setRot(proc, "head", -52.0F + bob * 6.0F, 0, 0);
            }
            case SLIME -> {
                CowgirlPose.setPos(proc, "body", 0, -6.0F, 0);
                CowgirlPose.setRot(proc, "body", -18, 0, 0);
                CowgirlPose.setRot(proc, "head", -44.0F + bob * 10.0F, 0, 0);
            }
            default -> kneel(proc, bob);
        }
    }
}
