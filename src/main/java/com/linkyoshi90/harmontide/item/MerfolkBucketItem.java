package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.List;

/** A young merfolk in a bucket of water (any species: the species travels in the bucket's data, see Merfolk#saveToBucketTag). */
public class MerfolkBucketItem extends MobBucketItem {
    public MerfolkBucketItem(Properties properties) {
        super(EntityType.AXOLOTL, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, properties);   // the type is never used: checkExtraContent spawns her
    }

    @Nullable
    private static GirlSpecies speciesOf(ItemStack stack) {
        return GirlSpecies.byId(stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag().getString("Species"));
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack stack, BlockPos pos) {
        GirlSpecies species = speciesOf(stack);
        if (!(level instanceof ServerLevel server) || species == null) return;
        if (!(ModGirls.type(species).create(server) instanceof Merfolk girl)) return;
        girl.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        girl.loadFromBucketTag(stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag());
        girl.setFromBucket(true);
        server.addFreshEntity(girl);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GirlSpecies species = speciesOf(stack);
        if (species != null) tooltip.add(Component.translatable("entity.harmontide." + species.id()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
