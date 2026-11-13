package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Set;

/**
 * Decomposers (2026-11-13): slimes, the fungus kinds and the dung beetle break down what is left lying about and give it back to the soil.
 * Every {@link #PULSE} ticks a decomposer, wild or befriended:
 * <ul>
 * <li>picks a carcass near her ({@link #REACH}) to bones faster, and breaks bones that are left into bone meal on the ground;</li>
 * <li>takes in the scraps near her ({@link #scrap}: rotten flesh, bones, spider eyes, spoiled or dropped food, leaves, saplings, seeds, flowers);</li>
 * <li>with {@link #PER_ENRICH} scraps' worth of humus in her, enriches the ground: a composter near her ({@link #COMPOSTER}) fills a layer
 * (so a decomposer friend by your composter makes you bone meal), else the soil round her improves ({@link #enrich}: coarse dirt
 * loosens to dirt, bare dirt greens over, a crop or sapling gets a dose of bone meal).</li>
 * </ul>
 * Part of the food web (config {@code foodWeb}).
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Decomposers {
    public static final int PULSE = 100, PER_ENRICH = 2, BITES = 2, ROT_FASTER = 2400;
    public static final double REACH = 4, COMPOSTER = 6;
    public static final Set<GirlSpecies> FUNGI = Set.of(GirlSpecies.MATANGO_FLORA, GirlSpecies.SHIITAKE_FLORA, GirlSpecies.CENSHROOM_CENTAUR,
            GirlSpecies.SHROOMOTAUR_MINOTAUR, GirlSpecies.CORDYCEPS_UNDEAD, GirlSpecies.DATE_BEETLE_BUG);
    private static final String HUMUS = "Humus";

    private Decomposers() {}

    public static boolean decomposer(AbstractMonsterGirl g) {
        return HarmontideConfig.foodWeb() && !g.isBaby() && (g.getSpecies().family() == GirlSpecies.Family.SLIME || FUNGI.contains(g.getSpecies()));
    }

    /** Something that rots down: meat gone off, bones, any food, leaves, saplings, seeds, flowers. */
    public static boolean scrap(ItemStack s) {
        return s.is(Items.ROTTEN_FLESH) || s.is(Items.BONE) || s.is(Items.SPIDER_EYE) || s.is(Items.POISONOUS_POTATO) || s.has(DataComponents.FOOD)
                || s.is(ItemTags.LEAVES) || s.is(ItemTags.SAPLINGS) || s.is(ItemTags.FLOWERS) || s.is(Items.WHEAT_SEEDS) || s.is(Items.DEAD_BUSH);
    }

    public static int humus(AbstractMonsterGirl g) {
        return g.getPersistentData().getInt(HUMUS);
    }

    private static void addHumus(AbstractMonsterGirl g, int n) {
        CompoundTag pd = g.getPersistentData();
        pd.putInt(HUMUS, Math.max(0, pd.getInt(HUMUS) + n));
    }

    /** One round of her work. How much humus she took in. */
    public static int pulse(ServerLevel level, AbstractMonsterGirl g) {
        if (!decomposer(g)) return 0;
        int taken = 0;
        for (Carcass c : level.getEntitiesOfClass(Carcass.class, g.getBoundingBox().inflate(REACH))) {
            if (c.stage() == Carcass.Stage.BONES) {                          // the last of it, to bone meal
                level.addFreshEntity(new ItemEntity(level, c.getX(), c.getY() + 0.2, c.getZ(), new ItemStack(Items.BONE_MEAL, 2)));
                level.sendParticles(ParticleTypes.MYCELIUM, c.getX(), c.getY() + 0.2, c.getZ(), 12, 0.4, 0.1, 0.4, 0.0);
                c.discard();
                taken++;
            } else {
                taken += c.bite(BITES);
                c.setAge(c.age() + ROT_FASTER);                              // it goes off faster with her at it
            }
        }
        List<ItemEntity> scraps = level.getEntitiesOfClass(ItemEntity.class, g.getBoundingBox().inflate(REACH), i -> scrap(i.getItem()));
        for (ItemEntity i : scraps) {
            taken += i.getItem().getCount();
            level.sendParticles(ParticleTypes.MYCELIUM, i.getX(), i.getY() + 0.1, i.getZ(), 6, 0.2, 0.1, 0.2, 0.0);
            i.discard();
        }
        if (taken > 0) {
            addHumus(g, taken);
            if (!g.hasAnyAffection()) FoodWeb.fed(level, g);
        }
        while (humus(g) >= PER_ENRICH) {
            addHumus(g, -PER_ENRICH);
            if (!compost(level, g)) enrich(level, g.blockPosition(), level.getRandom());
        }
        return taken;
    }

    /** A composter near her fills a layer. True if there was one with room. */
    public static boolean compost(ServerLevel level, AbstractMonsterGirl g) {
        for (BlockPos p : BlockPos.betweenClosed(g.blockPosition().offset(-6, -2, -6), g.blockPosition().offset(6, 2, 6))) {
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.COMPOSTER) && s.getValue(ComposterBlock.LEVEL) < 7) {
                level.setBlockAndUpdate(p, s.setValue(ComposterBlock.LEVEL, s.getValue(ComposterBlock.LEVEL) + 1));
                if (s.getValue(ComposterBlock.LEVEL) + 1 == 7) level.scheduleTick(p.immutable(), s.getBlock(), 20);   // ripens into bone meal as usual
                level.playSound(null, p, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    /** The ground round a spot improves a little. What changed, or null. */
    @javax.annotation.Nullable
    public static BlockPos enrich(ServerLevel level, BlockPos around, RandomSource r) {
        java.util.List<BlockPos> can = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(around.offset(-4, -2, -4), around.offset(4, 2, 4))) {
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.COARSE_DIRT) || s.is(Blocks.DIRT) && level.getBlockState(p.above()).isAir() && level.getMaxLocalRawBrightness(p.above()) >= 9
                    || s.is(net.minecraft.tags.BlockTags.CROPS) || s.is(net.minecraft.tags.BlockTags.SAPLINGS)) can.add(p.immutable());
        }
        java.util.Collections.shuffle(can, new java.util.Random(r.nextLong()));
        for (BlockPos p : can) {
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.COARSE_DIRT)) level.setBlockAndUpdate(p, Blocks.DIRT.defaultBlockState());
            else if (s.is(Blocks.DIRT)) level.setBlockAndUpdate(p, Blocks.GRASS_BLOCK.defaultBlockState());
            else if (BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, p)) level.levelEvent(1505, p, 0);   // the bone meal sparkle
            else continue;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 4, 0.3, 0.1, 0.3, 0.0);
            return p;
        }
        return null;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % PULSE == 73 && g.level() instanceof ServerLevel level) pulse(level, g);
    }
}
