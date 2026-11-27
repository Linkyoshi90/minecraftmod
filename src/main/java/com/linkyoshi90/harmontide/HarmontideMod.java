package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.client.HarmontideClientConfig;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(HarmontideMod.MODID)
public final class HarmontideMod {
    public static final String MODID = "harmontide";

    public HarmontideMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, HarmontideClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, HarmontideConfig.SPEC);
        ModGirls.init();                                   // the later families' entity types and spawn eggs
        com.linkyoshi90.harmontide.leviathan.LeviathanRegistry.init();   // the Dream-Leviathan's blocks, items and creatures
        com.linkyoshi90.harmontide.registry.ModFluids.FLUID_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModFluids.FLUIDS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        com.linkyoshi90.harmontide.registry.ModVillagers.POI_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModVillagers.PROFESSIONS.register(modBus);
        com.linkyoshi90.harmontide.registry.ModFeatures.FEATURES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModStructures.STRUCTURE_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModStructures.PIECES.register(modBus);
        ModItems.TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        com.linkyoshi90.harmontide.registry.ModParticles.PARTICLE_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModAttachments.ATTACHMENT_TYPES.register(modBus);
        com.linkyoshi90.harmontide.registry.ModComponents.COMPONENTS.register(modBus);
        com.linkyoshi90.harmontide.registry.ModMenus.MENUS.register(modBus);
        com.linkyoshi90.harmontide.item.IceCreamRecipe.SERIALIZERS.register(modBus);
        aliasOldIds(com.linkyoshi90.harmontide.registry.ModFluids.FLUID_TYPES, com.linkyoshi90.harmontide.registry.ModFluids.FLUIDS,
                ModBlocks.BLOCKS, ModBlockEntities.BLOCK_ENTITIES, ModEntities.ENTITY_TYPES,
                com.linkyoshi90.harmontide.registry.ModArmorMaterials.ARMOR_MATERIALS, ModItems.ITEMS,
                com.linkyoshi90.harmontide.registry.ModVillagers.POI_TYPES, com.linkyoshi90.harmontide.registry.ModVillagers.PROFESSIONS,
                com.linkyoshi90.harmontide.registry.ModFeatures.FEATURES, com.linkyoshi90.harmontide.registry.ModStructures.STRUCTURE_TYPES,
                com.linkyoshi90.harmontide.registry.ModStructures.PIECES, ModItems.TABS, ModSounds.SOUND_EVENTS,
                com.linkyoshi90.harmontide.registry.ModParticles.PARTICLE_TYPES, com.linkyoshi90.harmontide.registry.ModAttachments.ATTACHMENT_TYPES,
                com.linkyoshi90.harmontide.registry.ModComponents.COMPONENTS, com.linkyoshi90.harmontide.registry.ModMenus.MENUS,
                com.linkyoshi90.harmontide.item.IceCreamRecipe.SERIALIZERS);

        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ModEntities::registerSpawnPlacements);
        modBus.addListener(ModGirls::registerAttributes);
        modBus.addListener(ModGirls::registerSpawnPlacements);
        modBus.addListener(ModItems::addToCreativeTabs);
        modBus.addListener(HarmontideMod::registerCapabilities);
        modBus.addListener(com.linkyoshi90.harmontide.network.ModPayloads::register);
        modBus.addListener(com.linkyoshi90.harmontide.leviathan.LeviathanNet::register);
        modBus.addListener(com.linkyoshi90.harmontide.leviathan.LeviathanRegistry::registerAttributes);
    }

    /** Every girl's ammo pouch as her item handler: where TaCZ looks for her ammunition when she reloads (GirlArms). */
    private static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, com.linkyoshi90.harmontide.registry.ModBlockEntities.MILKING_MACHINE.get(),
                (machine, side) -> machine.hopperHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ModBlockEntities.BUTTER_CHURN.get(), (churn, side) -> churn.hopperHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ModBlockEntities.CHEESE_SHELF.get(), (shelf, side) -> shelf.hopperHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ModBlockEntities.MILK_TANK.get(), (tank, side) -> tank.hopperHandler(side));
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ModBlockEntities.FRIDGE.get(), (fridge, side) -> fridge.hopperHandler());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ModBlockEntities.NESTING_BOX.get(), (box, side) -> box.hopperHandler());
        for (var holder : ModGirls.TYPES.values()) {
            event.registerEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ENTITY, holder.get(),
                    (girl, ctx) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(girl.ammoPouch()));
        }
    }

    /** The mod was called "monstergirls" before; old worlds still name its girls, blocks and items monstergirls:x. */
    public static final String OLD_MODID = "monstergirls";

    private static void aliasOldIds(net.neoforged.neoforge.registries.DeferredRegister<?>... registers) {
        for (var register : registers) {
            for (var holder : register.getEntries()) {
                register.addAlias(ResourceLocation.fromNamespaceAndPath(OLD_MODID, holder.getId().getPath()), holder.getId());
            }
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
