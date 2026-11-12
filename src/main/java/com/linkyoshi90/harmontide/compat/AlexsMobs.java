package com.linkyoshi90.harmontide.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Optional Alex's Mobs support. Nothing here links against the mod: its mobs are looked up by id at runtime, so without
 * it every lookup comes back empty and the girls behave as before. With it, its animals count as kin of their girls
 * (Kinship), and a few hunts and calming effects reach them too.
 */
public final class AlexsMobs {
    public static final String MODID = "alexsmobs";

    /** Girl (species id or entity id path) -> the Alex's Mobs animals that count as her kin. */
    private static final Map<String, List<String>> KIN = Map.ofEntries(
            Map.entry("tiger_nekomata", List.of("tiger")),
            Map.entry("snow_leopard_nekomata", List.of("snow_leopard")),
            Map.entry("grizzly_canid", List.of("grizzly_bear")),
            Map.entry("seal_mermaid", List.of("seal")),
            Map.entry("orca_mermaid", List.of("orca")),
            Map.entry("shark_mermaid", List.of("hammerhead_shark", "frilled_shark")),
            Map.entry("croc_lizardfolk", List.of("crocodile", "caiman")),
            Map.entry("snapping_turtle_lizardfolk", List.of("alligator_snapping_turtle")),
            Map.entry("red_eared_slider_lizardfolk", List.of("terrapin")),
            Map.entry("rattlesnake_lamia", List.of("rattlesnake")),
            Map.entry("python_lamia", List.of("anaconda")),
            Map.entry("boa_lamia", List.of("anaconda")),
            Map.entry("kraken_mollusk", List.of("giant_squid")),
            Map.entry("octopus_mollusk", List.of("mimic_octopus")),
            Map.entry("snail_mollusk", List.of("banana_slug")),
            Map.entry("mimic_slime", List.of("mimicube")),
            Map.entry("bison_minotaur", List.of("bison")),
            Map.entry("reindeer_centaur", List.of("moose")),
            Map.entry("mosquito_bug", List.of("crimson_mosquito")),
            Map.entry("gryphon_dragon", List.of("bald_eagle")),
            Map.entry("crow_harpy", List.of("crow")),
            Map.entry("seagull_harpy", List.of("seagull")),
            Map.entry("toucan_harpy", List.of("toucan")),
            Map.entry("ostrich_harpy", List.of("emu")),
            Map.entry("desert_rain_frog", List.of("rain_frog")),
            Map.entry("kangaroo_marsupial", List.of("kangaroo")),
            Map.entry("jellyfish_mollusk", List.of("comb_jelly")));

    private AlexsMobs() {}

    /** The Alex's Mobs kin of a girl (by her species id or entity id path); empty without the mod. */
    public static Set<EntityType<?>> kinOf(String girl) {
        Set<EntityType<?>> out = new HashSet<>();
        for (String path : KIN.getOrDefault(girl, List.of())) {
            BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(MODID, path)).ifPresent(out::add);
        }
        return out;
    }

    /** Alex's Mobs fish our fish hunters (the penguin, the matamata) take too. */
    public static boolean isFish(Entity e) {
        return is(e, "catfish");
    }

    /** What a caught Alex's Mobs fish gives: its own raw fish if the mod has one, else a cod. */
    public static net.minecraft.world.item.ItemStack catchOf(Entity e) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(MODID, "raw_catfish"))
                .map(net.minecraft.world.item.ItemStack::new).orElseGet(() -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COD));
    }

    /** Whether this is the Alex's Mobs animal with that id path. */
    public static boolean is(Entity e, String path) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        return MODID.equals(key.getNamespace()) && path.equals(key.getPath());
    }
}
