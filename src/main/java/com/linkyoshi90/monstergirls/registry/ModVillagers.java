package com.linkyoshi90.monstergirls.registry;

import com.google.common.collect.ImmutableSet;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The exchange liaison (entity/Liaison): a villager who works at the exchange desk. */
public final class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MonsterGirlsMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MonsterGirlsMod.MODID);

    public static final ResourceKey<PoiType> DESK_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, MonsterGirlsMod.id("exchange_desk"));

    /** A lit realm censer (block/RealmCenserBlock): only its lit states, so the game's index answers "is one burning near her?". */
    public static final ResourceKey<PoiType> CENSER_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, MonsterGirlsMod.id("lit_realm_censer"));
    public static final DeferredHolder<PoiType, PoiType> CENSER = POI_TYPES.register("lit_realm_censer",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.REALM_CENSER.get().getStateDefinition().getPossibleStates().stream()
                    .filter(s -> s.getValue(com.linkyoshi90.monstergirls.block.RealmCenserBlock.LIT)).toList()), 0, 1));
    /** A holy font (block/HolyFontBlock): the realm's ward. */
    public static final ResourceKey<PoiType> FONT_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, MonsterGirlsMod.id("holy_font"));
    public static final DeferredHolder<PoiType, PoiType> FONT = POI_TYPES.register("holy_font",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.HOLY_FONT.get().getStateDefinition().getPossibleStates()), 0, 1));
    public static final DeferredHolder<PoiType, PoiType> DESK = POI_TYPES.register("exchange_desk",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.EXCHANGE_DESK.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> LIAISON = PROFESSIONS.register("liaison",
            () -> new VillagerProfession("liaison", holder -> holder.is(DESK_KEY), holder -> holder.is(DESK_KEY),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));

    private ModVillagers() {
    }
}
