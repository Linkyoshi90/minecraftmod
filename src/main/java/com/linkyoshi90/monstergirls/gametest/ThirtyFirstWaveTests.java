package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The thirty-first wave (HARMONTIDE_CONCEPTS.md): every biome has creatures, and the world generation mods share them. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirtyFirstWaveTests {

    /** tools/gen_biome_coverage.py: at least 8 species from 3 families in every vanilla biome, the broad tags counted. */
    @GameTest(template = PLATFORM, batch = "everyBiomeHasEnoughCreatures")
    public static void everyBiomeHasEnoughCreatures(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        List<ResourceLocation> species = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (id.getNamespace().equals(MonsterGirlsMod.MODID)) species.add(id);
        }
        List<String> thin = new ArrayList<>();
        biomes.holders().forEach(biome -> {
            if (biome.is(Biomes.THE_VOID)) return;
            int count = 0;
            Set<String> families = new HashSet<>();
            for (ResourceLocation id : species) {
                if (!spawnsIn(biome, id)) continue;
                count++;
                families.add(id.getPath().substring(id.getPath().lastIndexOf('_') + 1));
            }
            if (count < 8 || families.size() < 3) thin.add(biome.key().location().getPath() + " (" + count + "/" + families.size() + ")");
        });
        check(thin.isEmpty(), "thin biomes: " + thin);
        helper.succeed();
    }

    private static boolean spawnsIn(Holder<Biome> biome, ResourceLocation id) {
        return biome.is(TagKey.create(Registries.BIOME, MonsterGirlsMod.id("spawns_" + id.getPath())));
    }
}
