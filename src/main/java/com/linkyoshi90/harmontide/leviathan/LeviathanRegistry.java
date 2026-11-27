package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

/**
 * Everything the Dream-Leviathan event adds (leviathan/LeviathanEvent): the sky whale and her calf, the parasites inside her, the
 * blocks of her insides, the horn that calls her and the blessing she gives. Registered on the mod's shared registers; {@link #init}
 * only makes sure this class is loaded before they fire.
 */
public final class LeviathanRegistry {
    public static final ResourceKey<Level> INTERIOR = ResourceKey.create(Registries.DIMENSION, HarmontideMod.id("leviathan_interior"));
    public static final ResourceKey<net.minecraft.world.level.biome.Biome> INTERIOR_BIOME = ResourceKey.create(Registries.BIOME, HarmontideMod.id("leviathan_interior"));
    public static final ResourceKey<DamageType> STOMACH_ACID = ResourceKey.create(Registries.DAMAGE_TYPE, HarmontideMod.id("stomach_acid"));

    // --- blocks of her insides (none breakable in survival) ----------------------------------------------------------------

    private static BlockBehaviour.Properties flesh() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(-1.0F, 3600000.0F).sound(SoundType.SLIME_BLOCK)
                .noLootTable().isValidSpawn((s, l, p, t) -> false).pushReaction(PushReaction.BLOCK);
    }

    public static final DeferredBlock<net.minecraft.world.level.block.Block> FLESH = ModBlocks.BLOCKS.registerSimpleBlock("leviathan_flesh", flesh());
    public static final DeferredBlock<net.minecraft.world.level.block.Block> DENSE_FLESH = ModBlocks.BLOCKS.registerSimpleBlock("leviathan_dense_flesh",
            flesh().mapColor(MapColor.CRIMSON_HYPHAE));
    public static final DeferredBlock<net.minecraft.world.level.block.Block> VEIN = ModBlocks.BLOCKS.registerSimpleBlock("leviathan_vein",
            flesh().mapColor(MapColor.COLOR_PURPLE).lightLevel(s -> 7));
    public static final DeferredBlock<net.minecraft.world.level.block.Block> BALEEN = ModBlocks.BLOCKS.registerSimpleBlock("leviathan_baleen",
            flesh().mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.BONE_BLOCK).noOcclusion());
    public static final DeferredBlock<LeviathanBlocks.Membrane> MEMBRANE = ModBlocks.BLOCKS.registerBlock("leviathan_membrane",
            LeviathanBlocks.Membrane::new, flesh().mapColor(MapColor.COLOR_PINK).noOcclusion().lightLevel(s -> 3));
    public static final DeferredBlock<LeviathanBlocks.NerveNode> NERVE_NODE = ModBlocks.BLOCKS.registerBlock("leviathan_nerve_node",
            LeviathanBlocks.NerveNode::new, flesh().mapColor(MapColor.COLOR_YELLOW).lightLevel(s -> s.getValue(LeviathanBlocks.LIT) ? 14 : 4));
    public static final DeferredBlock<LeviathanBlocks.Acid> ACID = ModBlocks.BLOCKS.registerBlock("leviathan_acid",
            LeviathanBlocks.Acid::new, flesh().mapColor(MapColor.COLOR_LIGHT_GREEN).noOcclusion().lightLevel(s -> 9).sound(SoundType.HONEY_BLOCK));
    public static final DeferredBlock<LeviathanBlocks.Cilia> CILIA = ModBlocks.BLOCKS.registerBlock("leviathan_cilia",
            LeviathanBlocks.Cilia::new, flesh().mapColor(MapColor.COLOR_PINK).noOcclusion());
    public static final DeferredBlock<LeviathanBlocks.PulseWall> PULSE_WALL = ModBlocks.BLOCKS.registerBlock("leviathan_pulse_wall",
            LeviathanBlocks.PulseWall::new, flesh().noOcclusion());
    public static final DeferredBlock<LeviathanBlocks.Valve> VALVE = ModBlocks.BLOCKS.registerBlock("leviathan_valve",
            LeviathanBlocks.Valve::new, flesh().mapColor(MapColor.COLOR_MAGENTA).lightLevel(s -> 6));
    public static final DeferredBlock<LeviathanBlocks.Cyst> CYST = ModBlocks.BLOCKS.registerBlock("leviathan_cyst",
            LeviathanBlocks.Cyst::new, flesh().mapColor(MapColor.COLOR_GREEN).lightLevel(s -> s.getValue(LeviathanBlocks.CURED) ? 2 : 8));
    public static final DeferredBlock<LeviathanBlocks.Gland> GLAND = ModBlocks.BLOCKS.registerBlock("leviathan_gland",
            LeviathanBlocks.Gland::new, flesh().mapColor(MapColor.COLOR_LIGHT_BLUE).lightLevel(s -> 10));
    public static final DeferredBlock<LeviathanBlocks.Heart> HEART = ModBlocks.BLOCKS.registerBlock("leviathan_heart",
            LeviathanBlocks.Heart::new, flesh().mapColor(MapColor.COLOR_MAGENTA).lightLevel(s -> 13));
    public static final DeferredBlock<LeviathanBlocks.Exit> EXIT = ModBlocks.BLOCKS.registerBlock("leviathan_exit",
            LeviathanBlocks.Exit::new, flesh().mapColor(MapColor.COLOR_PINK).lightLevel(s -> 8));

    // --- entities ----------------------------------------------------------------------------------------------------------

    public static final DeferredHolder<EntityType<?>, EntityType<DreamLeviathan>> DREAM_LEVIATHAN = ModEntities.ENTITY_TYPES.register("dream_leviathan",
            () -> EntityType.Builder.<DreamLeviathan>of(DreamLeviathan::new, MobCategory.MISC).sized(12.0F, 8.0F).fireImmune()
                    .clientTrackingRange(16).updateInterval(1).build("dream_leviathan"));
    public static final DeferredHolder<EntityType<?>, EntityType<DreamCalf>> DREAM_CALF = ModEntities.ENTITY_TYPES.register("dream_calf",
            () -> EntityType.Builder.<DreamCalf>of(DreamCalf::new, MobCategory.CREATURE).sized(1.4F, 1.1F).fireImmune()
                    .clientTrackingRange(10).build("dream_calf"));
    public static final DeferredHolder<EntityType<?>, EntityType<Parasites.InfestedZombie>> INFESTED_ZOMBIE = ModEntities.ENTITY_TYPES.register("infested_zombie",
            () -> EntityType.Builder.<Parasites.InfestedZombie>of(Parasites.InfestedZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F)
                    .eyeHeight(1.74F).clientTrackingRange(8).build("infested_zombie"));
    public static final DeferredHolder<EntityType<?>, EntityType<Parasites.InfestedSkeleton>> INFESTED_SKELETON = ModEntities.ENTITY_TYPES.register("infested_skeleton",
            () -> EntityType.Builder.<Parasites.InfestedSkeleton>of(Parasites.InfestedSkeleton::new, MobCategory.MONSTER).sized(0.6F, 1.99F)
                    .eyeHeight(1.74F).clientTrackingRange(8).build("infested_skeleton"));
    public static final DeferredHolder<EntityType<?>, EntityType<Parasites.InfestedCreeper>> INFESTED_CREEPER = ModEntities.ENTITY_TYPES.register("infested_creeper",
            () -> EntityType.Builder.<Parasites.InfestedCreeper>of(Parasites.InfestedCreeper::new, MobCategory.MONSTER).sized(0.6F, 1.7F)
                    .clientTrackingRange(8).build("infested_creeper"));

    // --- items -------------------------------------------------------------------------------------------------------------

    public static final DeferredItem<DreamHornItem> DREAM_HORN = ModItems.ITEMS.register("dream_horn",
            () -> new DreamHornItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> DREAM_SCALE = ModItems.ITEMS.registerSimpleItem("dream_scale", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ANTIBODY = ModItems.ITEMS.registerSimpleItem("antibody", new Item.Properties().stacksTo(1));
    public static final DeferredItem<DreamEggItem> DREAM_EGG = ModItems.ITEMS.register("dream_leviathan_egg",
            () -> new DreamEggItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<DeferredSpawnEggItem> INFESTED_ZOMBIE_SPAWN_EGG = ModItems.ITEMS.register("infested_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(INFESTED_ZOMBIE, 0x4A5A3A, 0x8A2E5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> INFESTED_SKELETON_SPAWN_EGG = ModItems.ITEMS.register("infested_skeleton_spawn_egg",
            () -> new DeferredSpawnEggItem(INFESTED_SKELETON, 0xBDB59A, 0x8A2E5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> INFESTED_CREEPER_SPAWN_EGG = ModItems.ITEMS.register("infested_creeper_spawn_egg",
            () -> new DeferredSpawnEggItem(INFESTED_CREEPER, 0x3F7A3A, 0x8A2E5A, new Item.Properties()));

    // --- the blessing --------------------------------------------------------------------------------------------------------

    /** The Dream-Leviathan's blessing on a player (leviathan/DreamHeart): whether given, the second heart bar's fill, whether the egg was given. */
    public static final Supplier<AttachmentType<CompoundTag>> BLESSING = ModAttachments.ATTACHMENT_TYPES.register("dream_blessing",
            () -> AttachmentType.builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());

    private LeviathanRegistry() {
    }

    public static void init() {
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(DREAM_CALF.get(), DreamCalf.createAttributes().build());
        event.put(INFESTED_ZOMBIE.get(), Parasites.InfestedZombie.createAttributes().build());
        event.put(INFESTED_SKELETON.get(), Parasites.InfestedSkeleton.createAttributes().build());
        event.put(INFESTED_CREEPER.get(), Parasites.InfestedCreeper.createAttributes().build());
    }
}
