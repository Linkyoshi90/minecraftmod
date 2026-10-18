package com.linkyoshi90.monstergirls.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** What a fond harpy brings her favourite player (see {@code GiftGoal}). */
public final class HarpyGifts {
    private HarpyGifts() {}

    /** Crows: raw iron or gold she found glinting somewhere, and very rarely a diamond. */
    public static ItemStack crow(RandomSource random) {
        if (random.nextInt(20) == 0) return new ItemStack(Items.DIAMOND);
        return new ItemStack(random.nextBoolean() ? Items.RAW_IRON : Items.RAW_GOLD);
    }

    /** Riflebirds: a beakful of seeds, now and then a rare one. */
    public static ItemStack riflebird(RandomSource random) {
        if (random.nextInt(12) == 0) return new ItemStack(random.nextBoolean() ? Items.TORCHFLOWER_SEEDS : Items.PITCHER_POD);
        Item[] seeds = {Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        return new ItemStack(seeds[random.nextInt(seeds.length)], 1 + random.nextInt(3));
    }

    /** Owls: something from the night's hunt or forage. */
    public static ItemStack owl(RandomSource random) {
        Item[] food = {Items.RABBIT, Items.CHICKEN, Items.COD, Items.SWEET_BERRIES, Items.APPLE, Items.GLOW_BERRIES, Items.COOKED_RABBIT};
        return new ItemStack(food[random.nextInt(food.length)]);
    }

    /** Gulls and albatrosses: whatever the sea gave up today. */
    public static ItemStack sea(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll == 0) return new ItemStack(Items.HEART_OF_THE_SEA);
        if (roll < 4) return new ItemStack(Items.NAUTILUS_SHELL);
        if (roll < 8) return new ItemStack(Items.PRISMARINE_SHARD);
        if (roll < 12) return new ItemStack(Items.SEA_PICKLE);
        if (roll < 16) return new ItemStack(Items.INK_SAC);
        if (roll < 22) return new ItemStack(Items.TURTLE_SCUTE);
        if (roll < 34) return new ItemStack(Items.KELP, 1 + random.nextInt(3));
        if (roll < 46) return new ItemStack(Items.TROPICAL_FISH);
        if (roll < 70) return new ItemStack(Items.SALMON);
        return new ItemStack(Items.COD);
    }
}
