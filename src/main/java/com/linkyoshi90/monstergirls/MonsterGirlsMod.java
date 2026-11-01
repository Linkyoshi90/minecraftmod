package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.client.MonsterGirlsClientConfig;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(MonsterGirlsMod.MODID)
public final class MonsterGirlsMod {
    public static final String MODID = "monstergirls";

    public MonsterGirlsMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, MonsterGirlsClientConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, MonsterGirlsConfig.SPEC);
        ModGirls.init();                                   // the later families' entity types and spawn eggs
        com.linkyoshi90.monstergirls.registry.ModFluids.FLUID_TYPES.register(modBus);
        com.linkyoshi90.monstergirls.registry.ModFluids.FLUIDS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        com.linkyoshi90.monstergirls.registry.ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.TABS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        com.linkyoshi90.monstergirls.registry.ModParticles.PARTICLE_TYPES.register(modBus);
        com.linkyoshi90.monstergirls.registry.ModAttachments.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ModEntities::registerSpawnPlacements);
        modBus.addListener(ModGirls::registerAttributes);
        modBus.addListener(ModGirls::registerSpawnPlacements);
        modBus.addListener(ModItems::addToCreativeTabs);
        modBus.addListener(MonsterGirlsMod::registerCapabilities);
        modBus.addListener(com.linkyoshi90.monstergirls.network.ModPayloads::register);
    }

    /** Every girl's ammo pouch as her item handler: where TaCZ looks for her ammunition when she reloads (GirlArms). */
    private static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, com.linkyoshi90.monstergirls.registry.ModBlockEntities.MILKING_MACHINE.get(),
                (machine, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(machine.store()));
        for (var holder : ModGirls.TYPES.values()) {
            event.registerEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ENTITY, holder.get(),
                    (girl, ctx) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(girl.ammoPouch()));
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
