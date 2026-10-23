package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The first Harmontide species in existing families (HARMONTIDE_CONCEPTS.md): ocelot, tapir, alligator snapping turtle, banshee. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HarmontideTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tapirKeepsPhantomsAway")
    public static void tapirKeepsPhantomsAway(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(!Centaur.dreamWard(player), "no tapir, no ward");
        spawn(helper, GirlSpecies.TAPIR_CENTAUR, new BlockPos(10, 2, 10));
        check(Centaur.dreamWard(player), "a tapir nearby eats the bad dreams");
        PlayerSpawnPhantomsEvent event = NeoForge.EVENT_BUS.post(new PlayerSpawnPhantomsEvent(player, 2));
        check(event.getResult() == PlayerSpawnPhantomsEvent.Result.DENY, "so vanilla phantoms do not spawn: " + event.getResult());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "snappingTurtleLuresFish")
    public static void snappingTurtleLuresFish(GameTestHelper helper) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.WATER);
        }
        Lizardfolk turtle = spawn(helper, GirlSpecies.SNAPPING_TURTLE_LIZARDFOLK, new BlockPos(7, 2, 7));
        turtle.setNoAi(true);                                  // sitting still on the riverbed
        helper.runAfterDelay(2, () -> {
            check(turtle.isInWater(), "she is in the water");
            for (int i = 0; i < 300; i++) turtle.lure(helper.getLevel());
            int fish = helper.getLevel().getEntitiesOfClass(ItemEntity.class, turtle.getBoundingBox().inflate(4), e -> e.getItem().is(ItemTags.FISHES)).size();
            check(fish == 3, "her lure catches fish, never more than three at a time: " + fish);
            Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
            turtle.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
            check(turtle.isRetracted(), "hit, she hides in her shell");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bansheeKeensForTheWounded")
    public static void bansheeKeensForTheWounded(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Undead banshee = spawn(helper, GirlSpecies.BANSHEE_UNDEAD, new BlockPos(11, 2, 11));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 3));
        banshee.keen(helper.getLevel());
        check(!z.hasEffect(MobEffects.GLOWING), "a healthy player gets no warning");
        player.setHealth(5.0F);
        banshee.keen(helper.getLevel());
        check(z.hasEffect(MobEffects.GLOWING), "three hearts or less: she keens and the danger glows");
        z.removeEffect(MobEffects.GLOWING);
        banshee.keen(helper.getLevel());
        check(!z.hasEffect(MobEffects.GLOWING), "once a minute per player, not every second");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ocelotBringsJunglePresents")
    public static void ocelotBringsJunglePresents(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Nekomata ocelot = spawn(helper, GirlSpecies.OCELOT_NEKOMATA, new BlockPos(7, 2, 7));
        for (int i = 0; i < 50; i++) {
            var gift = ocelot.rollGift(player);
            check(gift.is(Items.COCOA_BEANS) || gift.is(Items.FEATHER) || gift.is(Items.MELON_SLICE), "a jungle present: " + gift);
        }
        check(!ocelot.onClimbable(), "she only climbs when she runs into a trunk or a wall");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kappaBowsBackAndSpillsHerDish")
    public static void kappaBowsBackAndSpillsHerDish(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Lizardfolk kappa = spawn(helper, GirlSpecies.KAPPA_LIZARDFOLK, new BlockPos(5, 2, 5));
        kappa.setTarget(player);
        check(!kappa.isDishDry(), "her dish starts full");
        kappa.bowBack(helper.getLevel(), player);
        check(kappa.isDishDry() && kappa.getTarget() == null && kappa.getAffection(player) == 1, "a bow: she bows back, spills her dish and stops");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nagaGuardsTreasureNotYourChests")
    public static void nagaGuardsTreasureNotYourChests(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        com.linkyoshi90.monstergirls.entity.girl.Lamia naga = spawn(helper, GirlSpecies.NAGA_LAMIA, new BlockPos(8, 2, 8));
        BlockPos loot = new BlockPos(5, 2, 5), own = new BlockPos(5, 2, 9);
        helper.setBlock(loot, net.minecraft.world.level.block.Blocks.CHEST);
        helper.setBlock(own, net.minecraft.world.level.block.Blocks.CHEST);
        ((net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(loot))
                .setLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.JUNGLE_TEMPLE, 1L);
        var level = helper.getLevel();
        check(com.linkyoshi90.monstergirls.entity.girl.Lamia.guardOf(level, helper.absolutePos(loot), player) == naga, "she guards the temple chest from a stranger");
        check(com.linkyoshi90.monstergirls.entity.girl.Lamia.guardOf(level, helper.absolutePos(own), player) == null, "never a chest the player placed");
        naga.setAffection(player.getUUID(), 10);
        check(com.linkyoshi90.monstergirls.entity.girl.Lamia.guardOf(level, helper.absolutePos(loot), player) == null, "a friend may take the treasure");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dullahanRidesAndFearsGold")
    public static void dullahanRidesAndFearsGold(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(!Undead.fearsGold(player), "an empty hand is nothing to fear");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT));
        check(Undead.fearsGold(player), "gold in your hand: she backs away");
        Undead dullahan = spawn(helper, GirlSpecies.DULLAHAN_UNDEAD, new BlockPos(9, 2, 9));
        dullahan.setWantsSteed(true);
        helper.runAfterDelay(3, () -> {
            check(dullahan.getVehicle() instanceof net.minecraft.world.entity.animal.horse.SkeletonHorse, "she rides a skeleton horse");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "girlsSleepTheirOffHours")
    public static void girlsSleepTheirOffHours(GameTestHelper helper) {
        var level = helper.getLevel();
        long was = level.getDayTime();
        level.setDayTime(18000);                                             // midnight
        var centaur = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(4, 2, 4));
        var fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(10, 2, 10));
        check(fox instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl k && k.isNocturnal(), "a fox is up at night");
        helper.runAfterDelay(80, () -> {
            check(centaur.getAction() == com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.Action.LYING, "a day girl sleeps at night: " + centaur.getAction());
            check(((com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl) fox).getAction() != com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.Action.LYING, "a fox does not");
            centaur.hurt(level.damageSources().generic(), 1.0F);
            helper.runAfterDelay(5, () -> {
                check(centaur.getAction() != com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.Action.LYING, "a hit wakes her");
                level.setDayTime(6000);                                      // noon: now the fox sleeps
                helper.runAfterDelay(80, () -> {
                    check(((com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl) fox).getAction() == com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.Action.LYING,
                            "the fox sleeps through the day");
                    level.setDayTime(was);
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "paintsAreInherited")
    public static void paintsAreInherited(GameTestHelper helper) {
        var level = helper.getLevel();
        var mother = (com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl) spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 5));
        mother.setPaint(com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.PAINTS.indexOf("purple") + 1);
        var daughter = mother.spawnYoung(level, helper.absolutePos(new BlockPos(7, 2, 7)), null);
        check(daughter != null && "purple".equals(daughter.getPaintName()), "a painted mother's daughter has her paint");
        var tag = new net.minecraft.nbt.CompoundTag();
        mother.saveWithoutId(tag);
        var copy = (com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl) com.linkyoshi90.monstergirls.registry.ModGirls.type(GirlSpecies.HORSE_CENTAUR).create(level);
        copy.load(tag);
        check("purple".equals(copy.getPaintName()), "the paint is saved");
        BlockPos egg = new BlockPos(10, 2, 10);
        helper.setBlock(egg, com.linkyoshi90.monstergirls.registry.ModBlocks.GIRL_EGG.get());
        ((com.linkyoshi90.monstergirls.block.GirlEggBlockEntity) helper.getBlockEntity(egg)).setup(GirlSpecies.PYTHON_LAMIA.id(), 1, null).withPaint(3);
        com.linkyoshi90.monstergirls.block.GirlEggBlock.hatch(level, helper.absolutePos(egg));
        var hatched = level.getEntitiesOfClass(com.linkyoshi90.monstergirls.entity.girl.Lamia.class, new net.minecraft.world.phys.AABB(helper.absolutePos(egg)).inflate(2));
        check(hatched.size() == 1 && hatched.get(0).getPaint() == 3, "a painted clutch hatches painted young");
        helper.succeed();
    }
}
