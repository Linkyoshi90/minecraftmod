package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Ice cream, in any order on the grid: a bucket of milk (any family's or a cow's), sugar, a snowball and a bowl. It keeps the milk's
 * family and flavours (entity.Dairy); the bucket comes back.
 */
public class IceCreamRecipe extends CustomRecipe {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, HarmontideMod.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<IceCreamRecipe>> SERIALIZER =
            SERIALIZERS.register("ice_cream", () -> new SimpleCraftingRecipeSerializer<>(IceCreamRecipe::new));

    public IceCreamRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** The milk on the grid if it holds exactly the four, else empty. */
    private static ItemStack milkOf(CraftingInput input) {
        ItemStack milk = ItemStack.EMPTY;
        int sugar = 0, snow = 0, bowl = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (Dairy.isMilk(s) && milk.isEmpty()) milk = s;
            else if (s.is(Items.SUGAR)) sugar++;
            else if (s.is(Items.SNOWBALL)) snow++;
            else if (s.is(Items.BOWL)) bowl++;
            else return ItemStack.EMPTY;
        }
        return sugar == 1 && snow == 1 && bowl == 1 ? milk : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !milkOf(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack milk = milkOf(input);
        return milk.isEmpty() ? ItemStack.EMPTY : Dairy.madeFrom(milk, ModItems.ICE_CREAM.get(), 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }
}
