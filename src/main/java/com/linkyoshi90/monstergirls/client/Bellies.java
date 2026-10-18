package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/** Rounds out an expecting monster girl's belly, from her synced gestation progress. */
final class Bellies {
    private Bellies() {}

    /**
     * Harpies: the "belly" bone (a copy of the midriff, added by tools/add_harpy_extras.py) only shows while she is
     * expecting, and grows forward (the bone pivots at its back face) as the day goes on.
     */
    static void harpy(GeoModel<?> model, AbstractHarpy harpy) {
        GeoBone belly = model.getAnimationProcessor().getBone("belly");
        if (belly == null) return;
        float swell = harpy.isBaby() ? 0.0F : harpy.getBellySwell();
        belly.setHidden(swell <= 0.0F);
        if (swell <= 0.0F) return;
        belly.setScaleX(1.05F + 0.3F * swell);
        belly.setScaleY(1.05F + 0.2F * swell);
        belly.setScaleZ(1.1F + 1.3F * swell);
    }

    /** Frogs and toads: their own belly bone (whose puff animations may already scale it) swells on top. */
    static void anuran(GeoModel<?> model, AbstractAnuran anuran) {
        float swell = anuran.isBaby() ? 0.0F : anuran.getBellySwell();
        if (swell <= 0.0F) return;
        GeoBone belly = model.getAnimationProcessor().getBone("belly");
        if (belly == null) return;
        belly.setScaleX(Math.max(belly.getScaleX(), 1.0F + 0.3F * swell));
        belly.setScaleY(Math.max(belly.getScaleY(), 1.0F + 0.15F * swell));
        belly.setScaleZ(Math.max(belly.getScaleZ(), 1.0F + 0.8F * swell));
    }
}
