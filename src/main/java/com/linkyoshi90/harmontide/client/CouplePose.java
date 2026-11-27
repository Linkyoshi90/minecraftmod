package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * On him, without a kneel. A serpent or a mermaid lies along him and her tail pumps her hips.
 * A spider stands and bends one pair of legs after another. A slime bounces and squashes.
 * Missing bones are skipped.
 */
final class CouplePose {
    private CouplePose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        if (!Nsfw.coupling(mob) || !(mob instanceof AbstractMonsterGirl girl)) return;
        float tick = mob.tickCount + partialTick;
        switch (girl.getSpecies().family()) {
            case LAMIA, MERFOLK -> lay(proc, tick);
            case ARACHNE -> bend(proc, tick);
            case SLIME -> bounce(proc, tick);
            default -> { }
        }
    }

    /** On her back along him, belly to his. The tail base shoves the hips up and down. */
    private static void lay(AnimationProcessor<?> proc, float tick) {
        float pump = (float) Nsfw.bounce(tick);
        CowgirlPose.setRot(proc, "root", 90, 0, 0);
        CowgirlPose.setPos(proc, "root", 0, 3, 6);
        CowgirlPose.setPos(proc, "body", 0, pump * 2.4F, 0);
        CowgirlPose.setRot(proc, "head", -14, 0, 0);
        pumpChain(proc, "seg_", pump);
        pumpChain(proc, "fin_", pump);
        pumpChain(proc, "sp_", pump);
        CowgirlPose.setRot(proc, "tail", pump * 28, 0, 0);
        CowgirlPose.setRot(proc, "tail_2", 90 - pump * 18, 0, 0);
        CowgirlPose.setRot(proc, "tail_3", pump * 14, 0, 0);
    }

    /** First link pumps, the next folds the way a lie does, and the one after ripples. */
    private static void pumpChain(AnimationProcessor<?> proc, String prefix, float pump) {
        CowgirlPose.setRot(proc, prefix + "1", pump * 28, 0, 0);
        CowgirlPose.setRot(proc, prefix + "2", 90 - pump * 18, 0, 0);
        CowgirlPose.setRot(proc, prefix + "3", pump * 14, 0, 0);
    }

    /** Each pair of legs bends in turn, and her body dips with them. */
    private static void bend(AnimationProcessor<?> proc, float tick) {
        float dip = 0;
        for (int i = 1; i <= 4; i++) {
            float bent = (1.0F - (float) Math.sin(tick * 0.38 - i * 1.15)) * 0.5F;
            dip += bent;
            fold(proc, "leg" + i, bent);
            fold(proc, "bleg" + i, bent);
        }
        dip /= 4.0F;
        float drop = -dip * 5.0F;
        CowgirlPose.setPos(proc, "body", 0, drop, 0);
        CowgirlPose.setPos(proc, "thorax", 0, drop, 0);
        CowgirlPose.setPos(proc, "abdomen", 0, drop * 0.6F, 0);
    }

    private static void fold(AnimationProcessor<?> proc, String name, float bent) {
        float hip = -18.0F - bent * 48.0F;
        float shin = 40.0F + bent * 52.0F;
        CowgirlPose.setRot(proc, name + "_r", hip, 0, 0);
        CowgirlPose.setRot(proc, name + "_l", hip, 0, 0);
        CowgirlPose.setRot(proc, name + "b_r", shin, 0, 0);
        CowgirlPose.setRot(proc, name + "b_l", shin, 0, 0);
        CowgirlPose.setRot(proc, name + "c_r", -shin * 0.45F, 0, 0);
        CowgirlPose.setRot(proc, name + "c_l", -shin * 0.45F, 0, 0);
    }

    /** The harpy bounce, as a squash: lower and wider at the bottom, tall at the top. */
    private static void bounce(AnimationProcessor<?> proc, float tick) {
        float down = (1.0F - (float) Nsfw.bounce(tick)) * 0.5F;
        squish(proc, "body", down);
        squish(proc, "gown", down);
        squish(proc, "core", down);
    }

    private static void squish(AnimationProcessor<?> proc, String name, float down) {
        GeoBone bone = proc.getBone(name);
        if (bone == null) return;
        bone.setScaleX(1.0F + down * 0.18F);
        bone.setScaleY(1.0F - down * 0.22F);
        bone.setScaleZ(1.0F + down * 0.18F);
        bone.setPosY(-down * 3.5F);
    }
}
