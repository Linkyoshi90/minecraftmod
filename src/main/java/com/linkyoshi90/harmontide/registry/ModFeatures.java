package com.linkyoshi90.harmontide.registry;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** World generation features of our own (configured and placed in data/harmontide/worldgen). */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, HarmontideMod.MODID);

    /** A hot spring (world/HotSpringFeature). */
    public static final DeferredHolder<Feature<?>, com.linkyoshi90.harmontide.world.HotSpringFeature> HOT_SPRING =
            FEATURES.register("hot_spring", com.linkyoshi90.harmontide.world.HotSpringFeature::new);
    /** A wild patch of the Demon Realm (world/RealmPatchFeature). */
    public static final DeferredHolder<Feature<?>, com.linkyoshi90.harmontide.world.RealmPatchFeature> REALM_PATCH =
            FEATURES.register("realm_patch", com.linkyoshi90.harmontide.world.RealmPatchFeature::new);

    private ModFeatures() {
    }
}
