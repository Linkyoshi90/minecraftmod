package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
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

    private ModBlockEntities() {}
}
