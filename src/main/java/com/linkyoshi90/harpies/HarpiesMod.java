package com.linkyoshi90.harpies;

import com.linkyoshi90.harpies.client.HarpiesClientConfig;
import com.linkyoshi90.harpies.registry.ModBlocks;
import com.linkyoshi90.harpies.registry.ModEntities;
import com.linkyoshi90.harpies.registry.ModItems;
import com.linkyoshi90.harpies.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(HarpiesMod.MODID)
public final class HarpiesMod {
    public static final String MODID = "harpies";

    public HarpiesMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, HarpiesClientConfig.SPEC);
        ModBlocks.BLOCKS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);

        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ModEntities::registerSpawnPlacements);
        modBus.addListener(ModItems::addToCreativeTabs);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
