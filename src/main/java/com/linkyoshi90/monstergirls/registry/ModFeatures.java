package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** World generation features of our own (configured and placed in data/monstergirls/worldgen). */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MonsterGirlsMod.MODID);

    /** A wild patch of the Demon Realm (world/RealmPatchFeature). */
    public static final DeferredHolder<Feature<?>, com.linkyoshi90.monstergirls.world.RealmPatchFeature> REALM_PATCH =
            FEATURES.register("realm_patch", com.linkyoshi90.monstergirls.world.RealmPatchFeature::new);

    private ModFeatures() {
    }
}
