package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Roles;
import com.linkyoshi90.harmontide.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Scout maps (2026-11-08): a girl back from an expedition sometimes brings a map she drew on the way, marked with a red cross
 * over a place nobody has explored yet: an Order chapel, a Lilith's manor, the Demon's spire, a harpy aerie or a merfolk grotto.
 * The sea's girls chart grottoes and the realm's own its manors and spires; anyone else whatever she came across. A scout brings
 * one {@link #SCOUT_ODDS}, anyone else {@link #ODDS}.
 */
public final class ScoutMaps {
    public static final int ODDS = 4, SCOUT_ODDS = 2, SEARCH = 50;
    public static final List<ResourceKey<Structure>> ALL = List.of(ModStructures.ORDER_CHAPEL_KEY, ModStructures.REALM_MANOR_KEY, ModStructures.DEMON_SPIRE_KEY,
            ModStructures.HARPY_AERIE_KEY, ModStructures.MERFOLK_GROTTO_KEY);

    private ScoutMaps() {}

    /** The places she'd chart, by where her kind feels at home. */
    public static List<ResourceKey<Structure>> places(AbstractMonsterGirl girl) {
        GirlSpecies.Family f = girl.getSpecies().family();
        if (f == GirlSpecies.Family.MERFOLK || f == GirlSpecies.Family.MOLLUSK || f == GirlSpecies.Family.CRUSTACEAN) return List.of(ModStructures.MERFOLK_GROTTO_KEY);
        if (f == GirlSpecies.Family.DEVIL || Monsterization.monsterized(girl)) return List.of(ModStructures.REALM_MANOR_KEY, ModStructures.DEMON_SPIRE_KEY);
        return ALL;
    }

    public static boolean brings(AbstractMonsterGirl girl) {
        return girl.getRandom().nextInt(Roles.is(girl, Roles.Role.SCOUT) ? SCOUT_ODDS : ODDS) == 0;
    }

    /** A map to the nearest unexplored place of one of these kinds from {@code from}, or empty if there's none in reach. */
    public static ItemStack chart(ServerLevel level, BlockPos from, List<ResourceKey<Structure>> kinds, int pick) {
        for (int i = 0; i < kinds.size(); i++) {
            ResourceKey<Structure> key = kinds.get((pick + i) % kinds.size());
            var holder = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(key);
            if (holder.isEmpty()) continue;
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder.get()), from, SEARCH, true);
            if (found == null) continue;
            BlockPos at = found.getFirst();
            ItemStack map = MapItem.create(level, at.getX(), at.getZ(), (byte) 2, true, true);
            MapItem.renderBiomePreviewMap(level, map);
            MapItemSavedData.addTargetDecoration(map, at, "+", MapDecorationTypes.RED_X);
            map.set(DataComponents.ITEM_NAME, Component.translatable("item.harmontide.scout_map." + key.location().getPath()));
            return map;
        }
        return ItemStack.EMPTY;
    }

    /** Back from her expedition: maybe a map. */
    @Nullable
    public static ItemStack onReturn(ServerLevel level, AbstractMonsterGirl girl) {
        if (!brings(girl)) return null;
        List<ResourceKey<Structure>> kinds = places(girl);
        ItemStack map = chart(level, girl.blockPosition(), kinds, girl.getRandom().nextInt(kinds.size()));
        return map.isEmpty() ? null : map;
    }
}
