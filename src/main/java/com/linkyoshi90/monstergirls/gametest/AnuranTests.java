package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.MucusToad;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The Bullywug and the Mucus Toad. Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class AnuranTests {

    /** A 3x3 pool one block deep, centred on (x, z): the platform floor is at y=1, so water goes in at y=2. */
    private static void pool(GameTestHelper helper, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(cx + dx, 2, cz + dz), Blocks.WATER);
        }
    }

    private static String readResource(String path) throws Exception {
        try (InputStream in = MonsterGirlsMod.class.getResourceAsStream(path)) {
            check(in != null, "missing resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GameTest(template = PLATFORM, batch = "anuranAssetsExist")
    public static void anuranAssetsExist(GameTestHelper helper) throws Exception {
        String[][] actions = {
                {"bullywug", "idle", "walk", "hop", "swim", "croak", "tongue", "sit", "bellow"},
                {"mucus_toad", "idle", "walk", "hop", "swim", "croak", "tongue", "sit", "puff", "dig"}};
        for (String[] species : actions) {
            String name = species[0];
            check(MonsterGirlsMod.class.getResource("/assets/monstergirls/geo/entity/" + name + ".geo.json") != null, "missing geo for " + name);
            check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/" + name + ".png") != null, "missing texture for " + name);
            String animations = readResource("/assets/monstergirls/animations/entity/" + name + ".animation.json");
            for (int i = 1; i < species.length; i++) {
                check(animations.contains("animation." + name + "." + species[i]), name + " lacks animation " + species[i]);
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "anuranSpawnRules")
    public static void anuranSpawnRules(GameTestHelper helper) {
        helper.setBlock(new BlockPos(10, 1, 10), Blocks.STONE);
        pool(helper, 3, 3);
        helper.runAfterDelay(5, () -> {
            var level = helper.getLevel();
            BlockPos nearPond = helper.absolutePos(new BlockPos(5, 2, 3));
            BlockPos farFromWater = helper.absolutePos(new BlockPos(13, 2, 13));
            check(AbstractAnuran.checkWaterSideSpawnRules(ModEntities.BULLYWUG.get(), level, MobSpawnType.SPAWNER, nearPond, level.random),
                    "bullywug should spawn on grass beside a pond");
            check(!AbstractAnuran.checkWaterSideSpawnRules(ModEntities.BULLYWUG.get(), level, MobSpawnType.SPAWNER, farFromWater, level.random),
                    "bullywug should not spawn far from water");
            check(AbstractAnuran.checkSpawnRules(ModEntities.MUCUS_TOAD.get(), level, MobSpawnType.SPAWNER, farFromWater, level.random),
                    "toad should spawn on grass, water or not");
            check(!AbstractAnuran.checkSpawnRules(ModEntities.MUCUS_TOAD.get(), level, MobSpawnType.SPAWNER,
                    helper.absolutePos(new BlockPos(10, 2, 10)), level.random), "toad should not spawn on stone");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "drynessIsSavedAndHurts")
    public static void drynessIsSavedAndHurts(GameTestHelper helper) {
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(4, 2, 4));
        frog.setDryTicks(1234);
        CompoundTag tag = new CompoundTag();
        frog.saveWithoutId(tag);
        Bullywug copy = ModEntities.BULLYWUG.get().create(helper.getLevel());
        copy.load(tag);
        check(copy.getDryTicks() == 1234, "dry ticks lost: " + copy.getDryTicks());
        check(!frog.isDriedOut(), "a fresh frog is not dried out");
        frog.setDryTicks(10_000);
        check(frog.isDriedOut() && frog.isDry(), "a long-dry frog should be dried out");
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(8, 2, 8));
        toad.setDryTicks(13_000);
        check(!toad.isDriedOut() && toad.isDry(), "a toad dries out much more slowly than a frog: half way, not yet hurting");
        helper.runAfterDelay(60, () -> {
            check(frog.getHealth() < frog.getMaxHealth(), "dried-out frog should have lost health");
            check(toad.getHealth() == toad.getMaxHealth(), "toad should be unharmed");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "waterRestoresMoisture")
    public static void waterRestoresMoisture(GameTestHelper helper) {
        pool(helper, 7, 7);
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 2, 7));
        frog.setDryTicks(3000);
        helper.succeedWhen(() -> check(frog.getDryTicks() == 0, "still dry: " + frog.getDryTicks()));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "dryFrogFindsWater")
    public static void dryFrogFindsWater(GameTestHelper helper) {
        pool(helper, 11, 11);
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        frog.setDryTicks(2500);
        helper.succeedWhen(() -> check(frog.isInWater() && frog.getDryTicks() == 0, "hasn't reached the pond: " + helper.relativeVec(frog.position())));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "loneBullywugBacksDownIntoWater")
    public static void loneBullywugBacksDownIntoWater(GameTestHelper helper) {
        pool(helper, 11, 11);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(5, 2, 5));
        frog.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(frog.getFleeTicks() > 0, "a lone frog hit once should flee");
        check(frog.getTarget() == null, "a lone frog should not fight back");
        helper.succeedWhen(() -> {
            check(frog.isInWater(), "hasn't leapt into the pond: " + helper.relativeVec(frog.position()));
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bullywugPackGangsUp")
    public static void bullywugPackGangsUp(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug first = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 6));
        Bullywug second = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 2, 6));
        Bullywug third = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 7));
        first.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        for (Bullywug b : new Bullywug[]{first, second, third}) {
            check(b.getTarget() == player, "every pack member should turn on the attacker");
            check(b.getFleeTicks() == 0, "a pack does not flee");
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "toadMucusMakesAttackersSick")
    public static void toadMucusMakesAttackersSick(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(6, 2, 6));
        check(!player.hasEffect(MobEffects.CONFUSION), "starts healthy");
        toad.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(player.hasEffect(MobEffects.CONFUSION), "attacker should be nauseous");
        check(toad.getAction() == AbstractAnuran.Action.PUFFING, "toad should puff up: " + toad.getAction());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "feedingHealsAFrog")
    public static void feedingHealsAFrog(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 2));
        frog.setHealth(4.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD, 2));
        frog.interact(player, InteractionHand.MAIN_HAND);
        check(frog.getHealth() > 4.0F, "fish should heal her");
        check(player.getMainHandItem().getCount() == 1, "one fish should be used up");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD, 2));
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(3, 2, 3));
        toad.setHealth(4.0F);
        toad.interact(player, InteractionHand.MAIN_HAND);
        check(toad.getHealth() > 4.0F, "fish is meat, and every monster girl takes meat");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "bullywugSnatchesFishWithHerTongue")
    public static void bullywugSnatchesFishWithHerTongue(GameTestHelper helper) {
        helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(4, 2, 4));
        ItemEntity fish = helper.spawnItem(Items.COD, new BlockPos(9, 2, 4));
        helper.succeedWhen(() -> check(!fish.isAlive(), "the fish is still lying there"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "toadEatsPests")
    public static void toadEatsPests(GameTestHelper helper) {
        for (int x = 1; x <= 9; x++) for (int z = 1; z <= 7; z++) helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);   // grass: by day (the clock is shared) she'd dig in instead
        helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(4, 2, 4));
        Silverfish pest = helper.spawn(EntityType.SILVERFISH, new BlockPos(8, 2, 4));
        pest.setNoAi(true);
        helper.succeedWhen(() -> check(!pest.isAlive(), "the silverfish is still alive"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "toadLeavesBigSlimesAlone")
    public static void toadLeavesBigSlimesAlone(GameTestHelper helper) {
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(4, 2, 4));
        Slime tiny = helper.spawn(EntityType.SLIME, new BlockPos(8, 2, 4));
        tiny.setSize(1, true);
        Slime big = helper.spawn(EntityType.SLIME, new BlockPos(8, 2, 8));
        big.setSize(2, true);
        check(toad.isPest(tiny), "tiny slime is a pest");
        check(!toad.isPest(big), "bigger slimes are not toad food");
        tiny.discard();
        big.discard();
        helper.succeed();
    }

    @GameTest(template = PLATFORM, skyAccess = true, timeoutTicks = 2400, batch = "toadDigsInByDay")
    public static void toadDigsInByDay(GameTestHelper helper) {
        helper.setDayTime(1000);
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> check(toad.getAction() == AbstractAnuran.Action.DIGGING, "not digging yet: " + toad.getAction()));
    }

    @GameTest(template = PLATFORM, skyAccess = true, timeoutTicks = 60, batch = "toadComesOutAtDusk")
    public static void toadComesOutAtDusk(GameTestHelper helper) {
        helper.setDayTime(1000);
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(7, 2, 7));
        helper.runAfterDelay(5, () -> {
            check(toad.shouldBeBuried(), "buried by day");
            helper.setDayTime(14000);
            helper.runAfterDelay(5, () -> {
                check(!toad.shouldBeBuried(), "she should come out at dusk");
                helper.setDayTime(1000);   // back to morning: later tests share this world's clock
                helper.succeed();
            });
        });
    }
}
