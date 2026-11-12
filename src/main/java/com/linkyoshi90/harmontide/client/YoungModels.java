package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.resources.ResourceLocation;

/** Where the young-stage models live: hatchlings, tadpoles and froglets each have their own geo, animations and texture. */
final class YoungModels {
    private YoungModels() {}

    static ResourceLocation geo(String name) {
        return HarmontideMod.id("geo/entity/" + name + ".geo.json");
    }

    static ResourceLocation anim(String name) {
        return HarmontideMod.id("animations/entity/" + name + ".animation.json");
    }

    static ResourceLocation tex(String name) {
        return HarmontideMod.id("textures/entity/" + name + ".png");
    }
}
