package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Standing against him, out of bed. A biped hooks one leg up and rocks her hips in.
 * A taur stays on her feet and works her back. A serpent or a mermaid stays upright and coils.
 * A spider leans over him. A slime presses and gives. Missing bones are skipped.
 */
final class StandPose {
    private StandPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.standing(mob)) return;
        float pump = (float) Math.sin((mob.tickCount + partialTick) * 0.5);
        if (Nsfw.bipedal(mob)) legs(proc, pump);
        else if (Nsfw.quadruped(mob)) hind(proc, pump);
        else if (mob instanceof AbstractMonsterGirl girl) {
            GirlSpecies.Family family = girl.getSpecies().family();
            if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK) coil(proc, pump);
            else if (family == GirlSpecies.Family.ARACHNE) loom(proc, pump);
            else if (family == GirlSpecies.Family.SLIME) press(proc, pump);
            else legs(proc, pump);
        }
    }

    /** One knee up, the other planted, chest in, hips rocking toward him. */
    private static void legs(AnimationProcessor<?> proc, float pump) {
        CowgirlPose.setPos(proc, "body", 0, -1.2F, pump * -1.6F);
        CowgirlPose.setRot(proc, "body", 22.0F + pump * 7.0F, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -62.0F + pump * 8.0F, 14, 18);
        CowgirlPose.setRot(proc, "shin_r", 74.0F - pump * 6.0F, 0, 0);
        CowgirlPose.setRot(proc, "foot_r", -20, 0, 0);
        CowgirlPose.setRot(proc, "leg_l", 10.0F + pump * 4.0F, 0, -8);
        CowgirlPose.setRot(proc, "shin_l", 6, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", -46, 18, 10);
        CowgirlPose.setRot(proc, "arm_l", -46, -18, -10);
        CowgirlPose.setRot(proc, "forearm_r", -24, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -24, 0, 0);
        CowgirlPose.setRot(proc, "head", -12.0F - pump * 4.0F, 0, 0);
    }

    /** On her own four feet, back lowered a little, hips pushing. */
    private static void hind(AnimationProcessor<?> proc, float pump) {
        CowgirlPose.setPos(proc, "body", 0, -0.6F, pump * -1.1F);
        CowgirlPose.setPos(proc, "barrel", 0, -0.4F, pump * -0.8F);
        CowgirlPose.setRot(proc, "body", 24.0F + pump * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "barrel", 14.0F + pump * 5.0F, 0, 0);
        CowgirlPose.setRot(proc, "tail", -48.0F + pump * 12.0F, 0, 0);
    }

    /** Upright. The tail curves in around him and the hips rock. */
    private static void coil(AnimationProcessor<?> proc, float pump) {
        CowgirlPose.setRot(proc, "body", 14.0F + pump * 9.0F, 0, 0);
        CowgirlPose.setPos(proc, "body", 0, pump * -0.8F, pump * -1.4F);
        CowgirlPose.setRot(proc, "head", -10.0F - pump * 4.0F, pump * 8.0F, 0);
        CowgirlPose.setRot(proc, "tail", 22.0F + pump * 14.0F, 30, 0);
        CowgirlPose.setRot(proc, "tail_2", 38.0F - pump * 8.0F, 24, 0);
        CowgirlPose.setRot(proc, "tail_3", 16.0F + pump * 10.0F, 12, 0);
        curve(proc, "seg_", pump);
        curve(proc, "fin_", pump);
        curve(proc, "sp_", pump);
    }

    private static void curve(AnimationProcessor<?> proc, String prefix, float pump) {
        CowgirlPose.setRot(proc, prefix + "1", 18.0F + pump * 10.0F, 36, 0);
        CowgirlPose.setRot(proc, prefix + "2", 28.0F - pump * 6.0F, 22, 0);
        CowgirlPose.setRot(proc, prefix + "3", 12.0F + pump * 8.0F, 10, 0);
    }

    /** Leaning over him on her feet, front legs folding in turn. */
    private static void loom(AnimationProcessor<?> proc, float pump) {
        CowgirlPose.setPos(proc, "body", 0, -1.4F + pump * -0.6F, pump * -1.0F);
        CowgirlPose.setRot(proc, "body", 32.0F + pump * 6.0F, 0, 0);
        CowgirlPose.setPos(proc, "abdomen", 0, pump * 0.8F, 0);
        for (int i = 1; i <= 4; i++) {
            float bent = i <= 2 ? 0.75F + pump * 0.2F : 0.25F;
            CowgirlPose.setRot(proc, "leg" + i + "_r", -36.0F - bent * 30.0F, 0, 8);
            CowgirlPose.setRot(proc, "leg" + i + "_l", -36.0F - bent * 30.0F, 0, -8);
            CowgirlPose.setRot(proc, "leg" + i + "b_r", 48.0F + bent * 24.0F, 0, 0);
            CowgirlPose.setRot(proc, "leg" + i + "b_l", 48.0F + bent * 24.0F, 0, 0);
        }
    }

    /** A standing press: she leans in and gives, wider when she pushes. */
    private static void press(AnimationProcessor<?> proc, float pump) {
        float give = (1.0F - pump) * 0.5F;
        squish(proc, "body", give);
        squish(proc, "gown", give);
        squish(proc, "core", give);
        CowgirlPose.setRot(proc, "body", 16.0F + pump * 8.0F, 0, 0);
    }

    private static void squish(AnimationProcessor<?> proc, String name, float give) {
        GeoBone bone = proc.getBone(name);
        if (bone == null) return;
        bone.setScaleX(1.0F + give * 0.12F);
        bone.setScaleY(1.0F - give * 0.1F);
        bone.setScaleZ(1.0F + give * 0.12F);
        bone.setPosZ(-give * 1.6F);
    }
}
