package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.block.ParentageBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MonsterGirlsMod.MODID);

    /** Who fathered a harpy clutch or frog spawn. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParentageBlockEntity>> PARENTAGE = BLOCK_ENTITIES.register("parentage",
            () -> BlockEntityType.Builder.of(ParentageBlockEntity::new, ModBlocks.HARPY_EGG.get(), ModBlocks.ANURAN_SPAWN.get()).build(null));

    /** What a girl egg clutch hatches into. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GirlEggBlockEntity>> GIRL_EGG = BLOCK_ENTITIES.register("girl_egg",
            () -> BlockEntityType.Builder.of(GirlEggBlockEntity::new, ModBlocks.GIRL_EGG.get()).build(null));

    /** Whoever is in the jar. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.GirlJarBlockEntity>> GIRL_JAR =
            BLOCK_ENTITIES.register("girl_jar", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.GirlJarBlockEntity::new,
                    ModBlocks.GIRL_JAR.get()).build(null));
    /** The alraune asleep in her bloom. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.AlrauneBloomBlockEntity>> ALRAUNE_BLOOM =
            BLOCK_ENTITIES.register("alraune_bloom", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.AlrauneBloomBlockEntity::new,
                    ModBlocks.ALRAUNE_BLOOM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.HearthBlockEntity>> HEARTH =
            BLOCK_ENTITIES.register("hearth", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.HearthBlockEntity::new,
                    ModBlocks.HEARTH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity>> MILKING_MACHINE =
            BLOCK_ENTITIES.register("milking_machine", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity::new,
                    ModBlocks.MILKING_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.CookingPotBlockEntity>> COOKING_POT =
            BLOCK_ENTITIES.register("cooking_pot", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.CookingPotBlockEntity::new,
                    ModBlocks.COOKING_POT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.SoakingTubBlockEntity>> SOAKING_TUB =
            BLOCK_ENTITIES.register("soaking_tub", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.SoakingTubBlockEntity::new,
                    ModBlocks.SOAKING_TUB.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.SparringPostBlockEntity>> SPARRING_POST =
            BLOCK_ENTITIES.register("sparring_post", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.SparringPostBlockEntity::new,
                    ModBlocks.SPARRING_POST.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.monstergirls.block.FigurineBlockEntity>> FIGURINE =
            BLOCK_ENTITIES.register("figurine", () -> BlockEntityType.Builder.of(com.linkyoshi90.monstergirls.block.FigurineBlockEntity::new,
                    ModBlocks.FIGURINE.get()).build(null));

    private ModBlockEntities() {}
}
