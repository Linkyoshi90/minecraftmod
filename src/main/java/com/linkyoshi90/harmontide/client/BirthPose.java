package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Labor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * Labor, laid over idle. A live birth sits and leans back with her knees apart. A spider crouches.
 * An egg-layer lies on her stomach and props herself on her arms. Missing bones are skipped.
 * These angles are the birth, not the night in bed.
 */
final class BirthPose {
    private BirthPose() {}

    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        float tick = mob.tickCount + partialTick;
        if (mob instanceof AbstractMonsterGirl girl && girl.getAction() == AbstractMonsterGirl.Action.LABOR) {
            switch (Labor.kind(girl)) {
                case SIT -> sit(proc, tick);
                case SPIDER -> spider(proc, tick);
                case EGG -> prone(proc, tick);
                case TAIL -> tail(proc, tick);
                case TAUR -> taur(proc, tick);
                case SLIME -> slime(proc, tick);
            }
            return;
        }
        if (mob instanceof AbstractHarpy harpy && harpy.getAction() == AbstractHarpy.Action.LAYING) prone(proc, tick);
        else if (mob instanceof AbstractAnuran anuran && anuran.getAction() == AbstractAnuran.Action.LAYING) prone(proc, tick);
    }

    /** Sitting, leaned back, knees open. The push deepens the lean. */
    private static void sit(AnimationProcessor<?> proc, float tick) {
        float push = (Mth.sin(tick * 0.32F) + 1.0F) * 0.5F;
        CowgirlPose.setPos(proc, "body", 0, -2.0F - push * 1.2F, 0);
        CowgirlPose.setRot(proc, "body", 20.0F + push * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "leg_r", -46.0F - push * 6.0F, 0, 32.0F + push * 8.0F);
        CowgirlPose.setRot(proc, "leg_l", -46.0F - push * 6.0F, 0, -(32.0F + push * 8.0F));
        CowgirlPose.setRot(proc, "shin_r", 52.0F + push * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "shin_l", 52.0F + push * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "foot_r", -18, 0, 8);
        CowgirlPose.setRot(proc, "foot_l", -18, 0, -8);
        CowgirlPose.setRot(proc, "arm_r", 36, 14, 8);
        CowgirlPose.setRot(proc, "arm_l", 36, -14, -8);
        CowgirlPose.setRot(proc, "forearm_r", -22, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -22, 0, 0);
        CowgirlPose.setRot(proc, "head", -8.0F - push * 4.0F, 0, 0);
    }

    /** All the legs fold together and she leans over the floor. */
    private static void spider(AnimationProcessor<?> proc, float tick) {
        float push = (Mth.sin(tick * 0.22F) + 1.0F) * 0.5F;
        float drop = -(4.5F + push * 5.0F);
        CowgirlPose.setPos(proc, "body", 0, drop, 1.5F);
        CowgirlPose.setPos(proc, "thorax", 0, drop * 0.65F, 0);
        CowgirlPose.setPos(proc, "abdomen", 0, drop * 0.35F, 0);
        CowgirlPose.setRot(proc, "body", 14.0F + push * 10.0F, 0, 0);
        for (int i = 1; i <= 4; i++) fold(proc, "leg" + i, push);
        for (int i = 1; i <= 4; i++) fold(proc, "bleg" + i, push);
    }

    private static void fold(AnimationProcessor<?> proc, String name, float push) {
        float hip = -34.0F - push * 26.0F;
        float shin = 48.0F + push * 30.0F;
        CowgirlPose.setRot(proc, name + "_r", hip, 0, 0);
        CowgirlPose.setRot(proc, name + "_l", hip, 0, 0);
        CowgirlPose.setRot(proc, name + "b_r", shin, 0, 0);
        CowgirlPose.setRot(proc, name + "b_l", shin, 0, 0);
        CowgirlPose.setRot(proc, name + "c_r", -shin * 0.4F, 0, 0);
        CowgirlPose.setRot(proc, name + "c_l", -shin * 0.4F, 0, 0);
    }

    /** Face-down over the nest, chest propped on her forearms, a push per egg. */
    private static void prone(AnimationProcessor<?> proc, float tick) {
        float push = Math.max(0.0F, Mth.sin(tick * 0.42F));
        CowgirlPose.setRot(proc, "root", -74, 0, 0);
        CowgirlPose.setPos(proc, "root", 0, 2.2F, -3.5F);
        CowgirlPose.setRot(proc, "body", -8.0F - push * 7.0F, 0, 0);
        CowgirlPose.setPos(proc, "body", 0, -push * 1.3F, 0.6F);
        CowgirlPose.setRot(proc, "arm_r", -64, 12, 18);
        CowgirlPose.setRot(proc, "arm_l", -64, -12, -18);
        CowgirlPose.setRot(proc, "forearm_r", -50.0F - push * 12.0F, 0, 0);
        CowgirlPose.setRot(proc, "forearm_l", -50.0F - push * 12.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", 16.0F - push * 6.0F, 0, 0);
        CowgirlPose.setRot(proc, "seg_1", 14.0F + push * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "fin_1", 14.0F + push * 8.0F, 0, 0);
        CowgirlPose.setRot(proc, "sp_1", 14.0F + push * 8.0F, 0, 0);
    }

    /** No legs to spread: she leans back along her tail. */
    private static void tail(AnimationProcessor<?> proc, float tick) {
        float push = (Mth.sin(tick * 0.28F) + 1.0F) * 0.5F;
        CowgirlPose.setRot(proc, "root", 24.0F + push * 10.0F, 0, 0);
        CowgirlPose.setPos(proc, "root", 0, -1.2F, 1.6F);
        CowgirlPose.setRot(proc, "body", 8, 0, 0);
        CowgirlPose.setRot(proc, "head", -14.0F - push * 4.0F, 0, 0);
        CowgirlPose.setRot(proc, "arm_r", 22, 8, 6);
        CowgirlPose.setRot(proc, "arm_l", 22, -8, -6);
        CowgirlPose.setRot(proc, "seg_1", -18, 0, 0);
        CowgirlPose.setRot(proc, "seg_2", 16, 0, 0);
        CowgirlPose.setRot(proc, "fin_1", -18, 0, 0);
        CowgirlPose.setRot(proc, "fin_2", 16, 0, 0);
        CowgirlPose.setRot(proc, "tail", -18, 0, 0);
        CowgirlPose.setRot(proc, "tail_2", 16, 0, 0);
    }

    /** A taur settles her barrel and leans her torso back. The horse legs stay on the idle. */
    private static void taur(AnimationProcessor<?> proc, float tick) {
        float push = (Mth.sin(tick * 0.26F) + 1.0F) * 0.5F;
        float drop = -5.2F - push * 2.4F;
        float back = 0.8F + push * 0.7F;
        CowgirlPose.setPos(proc, "body", 0, drop, back);
        CowgirlPose.setPos(proc, "barrel", 0, drop, back);
        CowgirlPose.setRot(proc, "body", 18.0F + push * 7.0F, 0, 0);
        CowgirlPose.setRot(proc, "head", -10, 0, 0);
    }

    /** No legs. She flattens low and bears down. */
    private static void slime(AnimationProcessor<?> proc, float tick) {
        float push = (Mth.sin(tick * 0.3F) + 1.0F) * 0.5F;
        squish(proc, "body", push);
        squish(proc, "gown", push);
        squish(proc, "core", push);
    }

    private static void squish(AnimationProcessor<?> proc, String name, float push) {
        var bone = proc.getBone(name);
        if (bone == null) return;
        bone.setScaleX(1.06F + push * 0.1F);
        bone.setScaleZ(1.06F + push * 0.1F);
        bone.setScaleY(0.84F - push * 0.08F);
        bone.setPosY(-2.4F - push * 1.6F);
    }
}
