package com.linkyoshi90.monstergirls.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/** What the night leaves behind: anyone who shared a sleeping player's bed all the way to morning is now expecting. */
public final class BedSharing {
    private static final double RADIUS = 12.0;

    private BedSharing() {}

    /** Called when the night is skipped (everyone sleeping, morning comes). Returns how many became expecting. */
    public static int onNightSkipped(ServerLevel level) {
        int expecting = 0;
        for (ServerPlayer player : level.players()) {
            if (!player.isSleeping()) continue;
            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS), m -> m instanceof Bonded)) {
                Gestation gestation = ((Bonded) mob).getGestation();
                if (!player.getUUID().equals(gestation.getSharingWith())) continue;
                gestation.setSharingWith(null);
                if (mob instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl girl) girl.onNightShared(player);   // her bed perk
                if (!((Bonded) mob).canShareBed()) continue;
                gestation.conceive(player.getUUID());
                expecting++;
                level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
            }
        }
        return expecting;
    }
}
