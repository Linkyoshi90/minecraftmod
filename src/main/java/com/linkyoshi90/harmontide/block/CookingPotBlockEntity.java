package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** How many foods are in the pot, how long they've cooked and how many meals wait. Butter or cheese in it flavours the meals (entity.Dairy). */
public class CookingPotBlockEntity extends BlockEntity {
    public static final int RECIPE = 3, COOK_TICKS = 300, MEALS = 2;
    private int ingredients, cooked, meals;
    /** The last dairy food put in, whose family and flavours the meals take. */
    private ItemStack flavour = ItemStack.EMPTY;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT.get(), pos, state);
    }

    public int ingredients() {
        return this.ingredients;
    }

    public int meals() {
        return this.meals;
    }

    /** One food into the pot. True if it took it. */
    public boolean add(ItemStack stack) {
        if (this.meals > 0 || this.ingredients >= RECIPE) return false;
        if (stack.is(ModItems.HOMEMADE_MEAL.get()) || (!stack.has(DataComponents.FOOD) && !stack.is(ModTags.MEAT))) return false;
        if (this.ingredients == 0) this.flavour = ItemStack.EMPTY;
        if (com.linkyoshi90.harmontide.entity.Dairy.familyOf(stack) != null) this.flavour = stack.copyWithCount(1);
        this.ingredients++;
        this.setChanged();
        return true;
    }

    /** A bowl takes out a meal; empty if the stack isn't a bowl or nothing is ready. */
    public ItemStack takeMeal(ItemStack stack) {
        if (!stack.is(Items.BOWL) || this.meals <= 0) return ItemStack.EMPTY;
        this.meals--;
        this.setChanged();
        return this.flavour.isEmpty() ? new ItemStack(ModItems.HOMEMADE_MEAL.get()) : com.linkyoshi90.harmontide.entity.Dairy.madeFrom(this.flavour, ModItems.HOMEMADE_MEAL.get(), 1);
    }

    public static boolean heated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.LAVA)
                || (below.is(BlockTags.CAMPFIRES) && CampfireBlock.isLitCampfire(below));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        if (pot.ingredients < RECIPE || !heated(level, pos)) return;
        if (++pot.cooked % 40 == 0 && level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 1, 0.1, 0.0, 0.1, 0.0);
            level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.5F, 0.8F);
        }
        if (pot.cooked < COOK_TICKS) return;
        pot.cooked = 0;
        pot.ingredients = 0;
        pot.meals = MEALS;
        pot.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Ingredients", this.ingredients);
        tag.putInt("Cooked", this.cooked);
        tag.putInt("Meals", this.meals);
        if (!this.flavour.isEmpty()) tag.put("Flavour", this.flavour.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ingredients = tag.getInt("Ingredients");
        this.cooked = tag.getInt("Cooked");
        this.meals = tag.getInt("Meals");
        this.flavour = tag.contains("Flavour") ? ItemStack.parseOptional(registries, tag.getCompound("Flavour")) : ItemStack.EMPTY;
    }
}
