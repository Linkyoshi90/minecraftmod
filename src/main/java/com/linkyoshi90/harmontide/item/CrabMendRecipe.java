package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * A crab armour piece mends at the smithing table (entity/girl/Jobs): no template, any worn tool, weapon or armour in the middle
 * slot, a piece of shed crab shell beside it. Each piece takes away {@link #MENDS} of the item's wear.
 */
public class CrabMendRecipe implements SmithingRecipe {
    /** How much of an item's full durability one piece gives back. */
    public static final float MENDS = 0.5F;
    /** The only one: it has no settings, and StreamCodec.unit only sends the very instance it was built with. */
    public static final CrabMendRecipe INSTANCE = new CrabMendRecipe();

    private CrabMendRecipe() {
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return false;                                                       // nothing goes in the template slot
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.isDamageableItem();
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return stack.is(ModItems.CRAB_ARMOUR_PIECE.get());
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().isEmpty() && input.base().isDamageableItem() && input.base().isDamaged() && this.isAdditionIngredient(input.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack mended = input.base().copyWithCount(1);
        mended.setDamageValue(Math.max(0, mended.getDamageValue() - Math.round(mended.getMaxDamage() * MENDS)));
        return mended;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IceCreamRecipe.CRAB_MEND.get();
    }

    /** No settings: the recipe file only names the type. */
    public static final class Serializer implements RecipeSerializer<CrabMendRecipe> {
        private static final MapCodec<CrabMendRecipe> CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf, CrabMendRecipe> STREAM = StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<CrabMendRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrabMendRecipe> streamCodec() {
            return STREAM;
        }
    }
}
