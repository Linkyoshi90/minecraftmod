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
        ModGirls.init();                                   // the later families' entity types and spawn eggs
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);

        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ModEntities::registerSpawnPlacements);
        modBus.addListener(ModGirls::registerAttributes);
        modBus.addListener(ModGirls::registerSpawnPlacements);
        modBus.addListener(ModItems::addToCreativeTabs);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
