package com.linkyoshi90.harmontide.registry;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.world.RealmPatchStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Structures of our own (data/harmontide/worldgen/structure and structure_set). */
public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, HarmontideMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, HarmontideMod.MODID);

    /** A wild patch of the Demon Realm (world/RealmPatchStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<RealmPatchStructure>> REALM_PATCH =
            STRUCTURE_TYPES.register("realm_patch", () -> () -> RealmPatchStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> REALM_PATCH_PIECE =
            PIECES.register("realm_patch", () -> (StructurePieceType.ContextlessType) (tag) -> new RealmPatchStructure.Piece(null, tag));
    public static final ResourceKey<Structure> REALM_PATCH_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("realm_patch"));

    /** A wayside chapel of the Order (world/OrderChapelStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.OrderChapelStructure>> ORDER_CHAPEL =
            STRUCTURE_TYPES.register("order_chapel", () -> () -> com.linkyoshi90.harmontide.world.OrderChapelStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ORDER_CHAPEL_PIECE =
            PIECES.register("order_chapel", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.OrderChapelStructure.Piece(null, tag));
    public static final ResourceKey<Structure> ORDER_CHAPEL_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("order_chapel"));

    /** A Lilith's manor (world/RealmManorStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.RealmManorStructure>> REALM_MANOR =
            STRUCTURE_TYPES.register("realm_manor", () -> () -> com.linkyoshi90.harmontide.world.RealmManorStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> REALM_MANOR_PIECE =
            PIECES.register("realm_manor", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.RealmManorStructure.Piece(null, tag));
    public static final ResourceKey<Structure> REALM_MANOR_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("realm_manor"));
    /** Small one-chunk structures (world/OneChunkStructure): the oasis, the sky island, the silk den, sharing one piece type. */
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ONE_CHUNK_PIECE =
            PIECES.register("one_chunk", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.OneChunkStructure.Piece(null, tag));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.OasisStructure>> OASIS =
            STRUCTURE_TYPES.register("oasis", () -> () -> com.linkyoshi90.harmontide.world.OasisStructure.CODEC);
    public static final ResourceKey<Structure> OASIS_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("oasis"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.SkyIslandStructure>> SKY_ISLAND =
            STRUCTURE_TYPES.register("sky_island", () -> () -> com.linkyoshi90.harmontide.world.SkyIslandStructure.CODEC);
    public static final ResourceKey<Structure> SKY_ISLAND_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("sky_island"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.SilkDenStructure>> SILK_DEN =
            STRUCTURE_TYPES.register("silk_den", () -> () -> com.linkyoshi90.harmontide.world.SilkDenStructure.CODEC);
    public static final ResourceKey<Structure> SILK_DEN_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("silk_den"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.DragonLairStructure>> DRAGON_LAIR =
            STRUCTURE_TYPES.register("dragon_lair", () -> () -> com.linkyoshi90.harmontide.world.DragonLairStructure.CODEC);
    public static final ResourceKey<Structure> DRAGON_LAIR_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("dragon_lair"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.CentaurCampStructure>> CENTAUR_CAMP =
            STRUCTURE_TYPES.register("centaur_camp", () -> () -> com.linkyoshi90.harmontide.world.CentaurCampStructure.CODEC);
    public static final ResourceKey<Structure> CENTAUR_CAMP_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("centaur_camp"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.GraveyardStructure>> GRAVEYARD =
            STRUCTURE_TYPES.register("graveyard", () -> () -> com.linkyoshi90.harmontide.world.GraveyardStructure.CODEC);
    public static final ResourceKey<Structure> GRAVEYARD_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("graveyard"));
    /** A Minoan labyrinth (world/LabyrinthStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.LabyrinthStructure>> LABYRINTH =
            STRUCTURE_TYPES.register("labyrinth", () -> () -> com.linkyoshi90.harmontide.world.LabyrinthStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> LABYRINTH_PIECE =
            PIECES.register("labyrinth", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.LabyrinthStructure.Piece(null, tag));
    public static final ResourceKey<Structure> LABYRINTH_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("labyrinth"));
    /** A kitsune shrine (world/KitsuneShrineStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.KitsuneShrineStructure>> KITSUNE_SHRINE =
            STRUCTURE_TYPES.register("kitsune_shrine", () -> () -> com.linkyoshi90.harmontide.world.KitsuneShrineStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> KITSUNE_SHRINE_PIECE =
            PIECES.register("kitsune_shrine", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.KitsuneShrineStructure.Piece(null, tag));
    public static final ResourceKey<Structure> KITSUNE_SHRINE_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("kitsune_shrine"));
    /** A wayside inn (world/WaysideInnStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.WaysideInnStructure>> WAYSIDE_INN =
            STRUCTURE_TYPES.register("wayside_inn", () -> () -> com.linkyoshi90.harmontide.world.WaysideInnStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> WAYSIDE_INN_PIECE =
            PIECES.register("wayside_inn", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.WaysideInnStructure.Piece(null, tag));
    public static final ResourceKey<Structure> WAYSIDE_INN_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("wayside_inn"));
    /** A merfolk grotto (world/MerfolkGrottoStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.MerfolkGrottoStructure>> MERFOLK_GROTTO =
            STRUCTURE_TYPES.register("merfolk_grotto", () -> () -> com.linkyoshi90.harmontide.world.MerfolkGrottoStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> MERFOLK_GROTTO_PIECE =
            PIECES.register("merfolk_grotto", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.MerfolkGrottoStructure.Piece(null, tag));
    public static final ResourceKey<Structure> MERFOLK_GROTTO_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("merfolk_grotto"));
    /** A harpy aerie (world/HarpyAerieStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.HarpyAerieStructure>> HARPY_AERIE =
            STRUCTURE_TYPES.register("harpy_aerie", () -> () -> com.linkyoshi90.harmontide.world.HarpyAerieStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> HARPY_AERIE_PIECE =
            PIECES.register("harpy_aerie", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.HarpyAerieStructure.Piece(null, tag));
    public static final ResourceKey<Structure> HARPY_AERIE_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("harpy_aerie"));
    /** The Demon's spire (world/DemonSpireStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.harmontide.world.DemonSpireStructure>> DEMON_SPIRE =
            STRUCTURE_TYPES.register("demon_spire", () -> () -> com.linkyoshi90.harmontide.world.DemonSpireStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> DEMON_SPIRE_PIECE =
            PIECES.register("demon_spire", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.harmontide.world.DemonSpireStructure.Piece(null, tag));
    public static final ResourceKey<Structure> DEMON_SPIRE_KEY = ResourceKey.create(Registries.STRUCTURE, HarmontideMod.id("demon_spire"));

    private ModStructures() {
    }
}
