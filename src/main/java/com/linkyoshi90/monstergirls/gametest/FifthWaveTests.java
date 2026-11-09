package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import com.linkyoshi90.monstergirls.entity.Vodyanoy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.SlimeGirl;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The fifth Harmontide wave (HARMONTIDE_CONCEPTS.md): the canids, pegasus, lich, mimic, vodyanoy, and frog kin. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class FifthWaveTests {

    private static boolean peaceful(GameTestHelper helper) {
        return helper.getLevel().getDifficulty() == Difficulty.PEACEFUL;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "wildWolfHuntsSheep")
    public static void wildWolfHuntsSheep(GameTestHelper helper) {
        Canid wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 4));
        Sheep sheep = helper.spawn(EntityType.SHEEP, new BlockPos(9, 2, 9));
        sheep.setNoAi(true);
        helper.succeedWhen(() -> check(wolf.getTarget() == sheep, "a wild wolf hunts sheep"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wolfLeadsHerPlayersWolves")
    public static void wolfLeadsHerPlayersWolves(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Canid girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        girl.setAffection(player.getUUID(), Orders.AFFECTION);
        Wolf dog = helper.spawn(EntityType.WOLF, new BlockPos(6, 2, 6));
        dog.tame(player);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        zombie.setNoAi(true);
        girl.setTarget(zombie);
        girl.leadTheWolves(helper.getLevel());
        check(dog.getTarget() == zombie, "her player's tamed wolves join her fight");
        check(!girl.isWild(), "befriended, she no longer hunts the flocks");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "polarBearHuntsSealsAndPenguinsAndGuardsCubs")
    public static void polarBearHuntsSealsAndPenguinsAndGuardsCubs(GameTestHelper helper) {
        Canid bear = spawn(helper, GirlSpecies.POLAR_BEAR_CANID, new BlockPos(4, 2, 4));
        bear.setNoAi(true);
        Merfolk seal = spawn(helper, GirlSpecies.SEAL_MERMAID, new BlockPos(8, 2, 8));
        PenguinHarpy penguin = helper.spawn(ModEntities.PENGUIN_HARPY.get(), new BlockPos(10, 2, 4));
        check(bear.isPolarPrey(seal) && bear.isPolarPrey(penguin), "a wild polar bear hunts seals and penguins");
        Canid cub = spawn(helper, GirlSpecies.POLAR_BEAR_CANID, new BlockPos(6, 2, 4));
        cub.setBaby(true);
        ServerPlayer near = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        check(bear.isHostileTo(near) != peaceful(helper), "and goes for a stranger near her cub (unless peaceful)");
        bear.setAffection(near.getUUID(), Orders.AFFECTION);
        check(!bear.isHostileTo(near) && !bear.isPolarPrey(seal), "never a friend; and befriended, she stops hunting");
        HarpyTestUtil.remove(near);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "pandaPersonalities")
    public static void pandaPersonalities(GameTestHelper helper) {
        Canid panda = spawn(helper, GirlSpecies.PANDA_CANID, new BlockPos(5, 2, 5));
        panda.setPersonality(Canid.Personality.WEAK);
        check(panda.getAttributeValue(Attributes.MAX_HEALTH) == 20.0, "a weak panda is frail");
        panda.sneeze(helper.getLevel());
        int balls = helper.getLevel().getEntitiesOfClass(ItemEntity.class, panda.getBoundingBox().inflate(2), e -> e.getItem().is(Items.SLIME_BALL)).size();
        check(balls == 1, "a sneeze brings up a slimeball");
        Canid wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        check(wolf.getAttributeValue(Attributes.MAX_HEALTH) == 24.0, "personalities are only for pandas: " + wolf.getAttributeValue(Attributes.MAX_HEALTH));
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "anubisWeighsHeartsAndGuardsTombs")
    public static void anubisWeighsHeartsAndGuardsTombs(GameTestHelper helper) {
        ServerPlayer guilty = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ServerPlayer innocent = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 2));
        guilty.awardStat(Stats.ENTITY_KILLED.get(EntityType.VILLAGER));
        Canid anubis = spawn(helper, GirlSpecies.ANUBIS_CANID, new BlockPos(6, 2, 6));
        BlockPos tomb = new BlockPos(8, 2, 8);
        helper.setBlock(tomb, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(tomb)).setLootTable(BuiltInLootTables.DESERT_PYRAMID, 1L);
        BlockPos abs = helper.absolutePos(tomb);
        check(Canid.tombGuardOf(helper.getLevel(), abs, innocent) == anubis, "before she weighs you, the tomb is closed");
        anubis.weigh(guilty);
        anubis.weigh(innocent);
        check(anubis.isCondemned(guilty) && anubis.isHostileTo(guilty) != peaceful(helper), "a heart heavy with blood is condemned and hunted");
        check(!anubis.isCondemned(innocent) && !anubis.isHostileTo(innocent), "a light heart passes");
        check(Canid.tombGuardOf(helper.getLevel(), abs, innocent) == null, "and may open the tomb");
        check(Canid.tombGuardOf(helper.getLevel(), abs, guilty) == anubis, "the condemned may not");
        check(!anubis.reweigh(guilty) && anubis.isCondemned(guilty), "a feather alone won't lighten it");
        while (com.linkyoshi90.monstergirls.entity.girl.Atonement.netWeight(guilty) > 0) com.linkyoshi90.monstergirls.entity.girl.Atonement.deed(guilty, "golem");
        check(anubis.reweigh(guilty) && !anubis.isCondemned(guilty) && !anubis.isHostileTo(guilty), "good deeds balance it: atoned");
        HarpyTestUtil.remove(guilty);
        HarpyTestUtil.remove(innocent);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "pegasusWearsAGoldenBridle")
    public static void pegasusWearsAGoldenBridle(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Centaur pegasus = spawn(helper, GirlSpecies.PEGASUS_CENTAUR, new BlockPos(6, 2, 6));
        pegasus.setAffection(player.getUUID(), Orders.AFFECTION);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        player.interactOn(pegasus, InteractionHand.MAIN_HAND);
        check(!pegasus.hasFlag(AbstractMonsterGirl.FLAG_SADDLED), "no plain saddle for her");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_HORSE_ARMOR));
        player.interactOn(pegasus, InteractionHand.MAIN_HAND);
        check(pegasus.hasFlag(AbstractMonsterGirl.FLAG_SADDLED) && pegasus.canJump(), "the golden bridle: she can be ridden, and leaps");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(pegasus, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == pegasus, "up on her back");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "lichCannotDieUntilHerPhylacteryBreaks")
    public static void lichCannotDieUntilHerPhylacteryBreaks(GameTestHelper helper) {
        Undead lich = spawn(helper, GirlSpecies.LICH_UNDEAD, new BlockPos(7, 2, 7));
        lich.setNoAi(true);
        helper.runAfterDelay(30, () -> {
            lich.aiStep();                                               // no AI: tick her placing the phylactery by hand
            BlockPos p = lich.getPhylactery();
            check(p != null && helper.getLevel().getBlockState(p).is(ModBlocks.PHYLACTERY.get()), "she hides her soul in a phylactery");
            lich.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
            check(lich.isAlive() && lich.getHealth() > 0, "while it stands she cannot die");
            helper.getLevel().destroyBlock(p, false);
            check(lich.isCalm() && lich.getPhylactery() == null, "break it and she is free, and peaceful");
            ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
            lich.setAffection(player.getUUID(), Orders.AFFECTION);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOOK));
            player.interactOn(lich, InteractionHand.MAIN_HAND);
            int orbs = helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(3)).size();
            check(orbs > 0 && player.getMainHandItem().isEmpty(), "a book for experience");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mimicOnlyLivesInLootChests")
    public static void mimicOnlyLivesInLootChests(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BlockPos loot = new BlockPos(6, 2, 6);
        BlockPos own = new BlockPos(9, 2, 9);
        helper.setBlock(loot, Blocks.CHEST);
        helper.setBlock(own, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(loot)).setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, 7L);
        check(SlimeGirl.springMimic(helper.getLevel(), helper.absolutePos(own), player) == null, "never a chest anyone placed");
        SlimeGirl mimic = SlimeGirl.springMimic(helper.getLevel(), helper.absolutePos(loot), player);
        check(mimic != null && helper.getBlockState(loot).isAir(), "an unopened loot chest was a mimic");
        check(!mimic.getHoard().isEmpty(), "she keeps the loot");
        check(mimic.getAction() == AbstractMonsterGirl.Action.BITE, "and bites once");      // (a player who just joined cannot be hurt yet)
        mimic.setAffection(player.getUUID(), Orders.AFFECTION);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
        check(mimic.isFood(player.getMainHandItem()), "gold wins her over");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "vodyanoyKeepsACleanPool")
    public static void vodyanoyKeepsACleanPool(GameTestHelper helper) {
        for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);   // his pool, in the floor
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Vodyanoy vod = helper.spawn(ModEntities.VODYANOY.get(), new BlockPos(7, 2, 7));
        vod.setNoAi(true);
        vod.setAffection(friend.getUUID(), Orders.AFFECTION);
        check(!vod.canShareBed(), "he is a man: no bed, no young");
        vod.bringFish(helper.getLevel());
        int fish = helper.getLevel().getEntitiesOfClass(ItemEntity.class, friend.getBoundingBox().inflate(1.5),
                e -> e.getItem().is(Items.COD) || e.getItem().is(Items.SALMON)).size();
        check(fish == 1, "a clean pool: fish for his friend");
        ItemEntity litter = new ItemEntity(helper.getLevel(), vod.getX() + 2, vod.getY() - 0.7, vod.getZ(), new ItemStack(Items.DIRT));
        helper.getLevel().addFreshEntity(litter);
        helper.succeedWhen(() -> {
            vod.aiStep();
            check(vod.isGrumpy(), "litter in his pool makes him grumpy");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "anuranBreedsWithAFrog")
    public static void anuranBreedsWithAFrog(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Bullywug wug = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(5, 2, 5));
        Frog frog = helper.spawn(EntityType.FROG, new BlockPos(7, 2, 5));
        frog.setNoAi(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SLIME_BALL, 2));
        player.interactOn(wug, InteractionHand.MAIN_HAND);
        check(wug.getFrogLove() > 0, "a slimeball puts her in the mood");
        frog.setInLove(player);
        helper.succeedWhen(() -> {
            check(AbstractAnuran.FROG_FATHER.equals(wug.getGestation().getFather()), "she courts the frog and is expecting");
            check(!frog.isInLove(), "and his love is spent");
            HarpyTestUtil.remove(player);
        });
    }
}
