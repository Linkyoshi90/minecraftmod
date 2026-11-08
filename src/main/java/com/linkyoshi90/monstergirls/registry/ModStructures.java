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

    private ModStructures() {
    }
}
