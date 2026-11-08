package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.world.RealmPatchStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Structures of our own (data/monstergirls/worldgen/structure and structure_set). */
public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MonsterGirlsMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MonsterGirlsMod.MODID);

    /** A wild patch of the Demon Realm (world/RealmPatchStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<RealmPatchStructure>> REALM_PATCH =
            STRUCTURE_TYPES.register("realm_patch", () -> () -> RealmPatchStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> REALM_PATCH_PIECE =
            PIECES.register("realm_patch", () -> (StructurePieceType.ContextlessType) (tag) -> new RealmPatchStructure.Piece(null, tag));
    public static final ResourceKey<Structure> REALM_PATCH_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("realm_patch"));

    /** A wayside chapel of the Order (world/OrderChapelStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.OrderChapelStructure>> ORDER_CHAPEL =
            STRUCTURE_TYPES.register("order_chapel", () -> () -> com.linkyoshi90.monstergirls.world.OrderChapelStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ORDER_CHAPEL_PIECE =
            PIECES.register("order_chapel", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.OrderChapelStructure.Piece(null, tag));
    public static final ResourceKey<Structure> ORDER_CHAPEL_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("order_chapel"));

    /** A Lilith's manor (world/RealmManorStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.RealmManorStructure>> REALM_MANOR =
            STRUCTURE_TYPES.register("realm_manor", () -> () -> com.linkyoshi90.monstergirls.world.RealmManorStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> REALM_MANOR_PIECE =
            PIECES.register("realm_manor", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.RealmManorStructure.Piece(null, tag));
    public static final ResourceKey<Structure> REALM_MANOR_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("realm_manor"));
    /** Small one-chunk structures (world/OneChunkStructure): the oasis, the sky island, the silk den, sharing one piece type. */
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ONE_CHUNK_PIECE =
            PIECES.register("one_chunk", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.OneChunkStructure.Piece(null, tag));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.OasisStructure>> OASIS =
            STRUCTURE_TYPES.register("oasis", () -> () -> com.linkyoshi90.monstergirls.world.OasisStructure.CODEC);
    public static final ResourceKey<Structure> OASIS_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("oasis"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.SkyIslandStructure>> SKY_ISLAND =
            STRUCTURE_TYPES.register("sky_island", () -> () -> com.linkyoshi90.monstergirls.world.SkyIslandStructure.CODEC);
    public static final ResourceKey<Structure> SKY_ISLAND_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("sky_island"));
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.SilkDenStructure>> SILK_DEN =
            STRUCTURE_TYPES.register("silk_den", () -> () -> com.linkyoshi90.monstergirls.world.SilkDenStructure.CODEC);
    public static final ResourceKey<Structure> SILK_DEN_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("silk_den"));
    /** A Minoan labyrinth (world/LabyrinthStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.LabyrinthStructure>> LABYRINTH =
            STRUCTURE_TYPES.register("labyrinth", () -> () -> com.linkyoshi90.monstergirls.world.LabyrinthStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> LABYRINTH_PIECE =
            PIECES.register("labyrinth", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.LabyrinthStructure.Piece(null, tag));
    public static final ResourceKey<Structure> LABYRINTH_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("labyrinth"));
    /** A kitsune shrine (world/KitsuneShrineStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.KitsuneShrineStructure>> KITSUNE_SHRINE =
            STRUCTURE_TYPES.register("kitsune_shrine", () -> () -> com.linkyoshi90.monstergirls.world.KitsuneShrineStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> KITSUNE_SHRINE_PIECE =
            PIECES.register("kitsune_shrine", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.KitsuneShrineStructure.Piece(null, tag));
    public static final ResourceKey<Structure> KITSUNE_SHRINE_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("kitsune_shrine"));
    /** A wayside inn (world/WaysideInnStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.WaysideInnStructure>> WAYSIDE_INN =
            STRUCTURE_TYPES.register("wayside_inn", () -> () -> com.linkyoshi90.monstergirls.world.WaysideInnStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> WAYSIDE_INN_PIECE =
            PIECES.register("wayside_inn", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.WaysideInnStructure.Piece(null, tag));
    public static final ResourceKey<Structure> WAYSIDE_INN_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("wayside_inn"));
    /** A merfolk grotto (world/MerfolkGrottoStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.MerfolkGrottoStructure>> MERFOLK_GROTTO =
            STRUCTURE_TYPES.register("merfolk_grotto", () -> () -> com.linkyoshi90.monstergirls.world.MerfolkGrottoStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> MERFOLK_GROTTO_PIECE =
            PIECES.register("merfolk_grotto", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.MerfolkGrottoStructure.Piece(null, tag));
    public static final ResourceKey<Structure> MERFOLK_GROTTO_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("merfolk_grotto"));
    /** A harpy aerie (world/HarpyAerieStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.HarpyAerieStructure>> HARPY_AERIE =
            STRUCTURE_TYPES.register("harpy_aerie", () -> () -> com.linkyoshi90.monstergirls.world.HarpyAerieStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> HARPY_AERIE_PIECE =
            PIECES.register("harpy_aerie", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.HarpyAerieStructure.Piece(null, tag));
    public static final ResourceKey<Structure> HARPY_AERIE_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("harpy_aerie"));
    /** The Demon's spire (world/DemonSpireStructure). */
    public static final DeferredHolder<StructureType<?>, StructureType<com.linkyoshi90.monstergirls.world.DemonSpireStructure>> DEMON_SPIRE =
            STRUCTURE_TYPES.register("demon_spire", () -> () -> com.linkyoshi90.monstergirls.world.DemonSpireStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> DEMON_SPIRE_PIECE =
            PIECES.register("demon_spire", () -> (StructurePieceType.ContextlessType) (tag) -> new com.linkyoshi90.monstergirls.world.DemonSpireStructure.Piece(null, tag));
    public static final ResourceKey<Structure> DEMON_SPIRE_KEY = ResourceKey.create(Registries.STRUCTURE, MonsterGirlsMod.id("demon_spire"));

    private ModStructures() {
    }
}
