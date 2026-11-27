package com.linkyoshi90.harmontide.registry;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.block.ParentageBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HarmontideMod.MODID);

    /** Who fathered a harpy clutch or frog spawn. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.RabbitBurrowBlockEntity>> RABBIT_BURROW = BLOCK_ENTITIES.register("rabbit_burrow",
            () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.RabbitBurrowBlockEntity::new, ModBlocks.RABBIT_BURROW.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.FootprintBlockEntity>> FOOTPRINT = BLOCK_ENTITIES.register("footprint",
            () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.FootprintBlockEntity::new, ModBlocks.FOOTPRINT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParentageBlockEntity>> PARENTAGE = BLOCK_ENTITIES.register("parentage",
            () -> BlockEntityType.Builder.of(ParentageBlockEntity::new, ModBlocks.HARPY_EGG.get(), ModBlocks.ANURAN_SPAWN.get()).build(null));

    /** What a girl egg clutch hatches into. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GirlEggBlockEntity>> GIRL_EGG = BLOCK_ENTITIES.register("girl_egg",
            () -> BlockEntityType.Builder.of(GirlEggBlockEntity::new, ModBlocks.GIRL_EGG.get()).build(null));

    /** Whoever is in the jar. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.GirlJarBlockEntity>> GIRL_JAR =
            BLOCK_ENTITIES.register("girl_jar", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.GirlJarBlockEntity::new,
                    ModBlocks.GIRL_JAR.get()).build(null));
    /** The alraune asleep in her bloom. */
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.AlrauneBloomBlockEntity>> ALRAUNE_BLOOM =
            BLOCK_ENTITIES.register("alraune_bloom", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.AlrauneBloomBlockEntity::new,
                    ModBlocks.ALRAUNE_BLOOM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.HearthBlockEntity>> HEARTH =
            BLOCK_ENTITIES.register("hearth", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.HearthBlockEntity::new,
                    ModBlocks.HEARTH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.MilkingMachineBlockEntity>> MILKING_MACHINE =
            BLOCK_ENTITIES.register("milking_machine", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.MilkingMachineBlockEntity::new,
                    ModBlocks.MILKING_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.CookingPotBlockEntity>> COOKING_POT =
            BLOCK_ENTITIES.register("cooking_pot", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.CookingPotBlockEntity::new,
                    ModBlocks.COOKING_POT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.SoakingTubBlockEntity>> SOAKING_TUB =
            BLOCK_ENTITIES.register("soaking_tub", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.SoakingTubBlockEntity::new,
                    ModBlocks.SOAKING_TUB.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.SparringPostBlockEntity>> SPARRING_POST =
            BLOCK_ENTITIES.register("sparring_post", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.SparringPostBlockEntity::new,
                    ModBlocks.SPARRING_POST.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.FigurineBlockEntity>> FIGURINE =
            BLOCK_ENTITIES.register("figurine", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.FigurineBlockEntity::new,
                    ModBlocks.FIGURINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.MailboxBlockEntity>> MAILBOX =
            BLOCK_ENTITIES.register("mailbox", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.MailboxBlockEntity::new,
                    ModBlocks.MAILBOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.MarketStallBlockEntity>> MARKET_STALL =
            BLOCK_ENTITIES.register("market_stall", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.MarketStallBlockEntity::new,
                    ModBlocks.MARKET_STALL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.GiftBoxBlockEntity>> GIFT_BOX =
            BLOCK_ENTITIES.register("gift_box", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.GiftBoxBlockEntity::new,
                    ModBlocks.GIFT_BOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.ButterChurnBlockEntity>> BUTTER_CHURN =
            BLOCK_ENTITIES.register("butter_churn", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.ButterChurnBlockEntity::new,
                    ModBlocks.BUTTER_CHURN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.CheeseShelfBlockEntity>> CHEESE_SHELF =
            BLOCK_ENTITIES.register("cheese_shelf", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.CheeseShelfBlockEntity::new,
                    ModBlocks.CHEESE_SHELF.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.MilkTankBlockEntity>> MILK_TANK =
            BLOCK_ENTITIES.register("milk_tank", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.MilkTankBlockEntity::new,
                    ModBlocks.MILK_TANK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.FridgeBlockEntity>> FRIDGE =
            BLOCK_ENTITIES.register("fridge", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.FridgeBlockEntity::new,
                    ModBlocks.FRIDGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.linkyoshi90.harmontide.block.NestingBoxBlockEntity>> NESTING_BOX =
            BLOCK_ENTITIES.register("nesting_box", () -> BlockEntityType.Builder.of(com.linkyoshi90.harmontide.block.NestingBoxBlockEntity::new,
                    ModBlocks.NESTING_BOX.get()).build(null));

    private ModBlockEntities() {}
}
