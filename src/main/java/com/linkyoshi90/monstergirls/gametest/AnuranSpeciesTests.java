package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.Bullfrog;
import com.linkyoshi90.monstergirls.entity.CaneToad;
import com.linkyoshi90.monstergirls.entity.DesertRainFrog;
import com.linkyoshi90.monstergirls.entity.FireBelliedToad;
import com.linkyoshi90.monstergirls.entity.GlassFrog;
import com.linkyoshi90.monstergirls.entity.GoliathFrog;
import com.linkyoshi90.monstergirls.entity.Natterjack;
import com.linkyoshi90.monstergirls.entity.PoisonDartFrog;
import com.linkyoshi90.monstergirls.entity.SurinamToad;
import com.linkyoshi90.monstergirls.entity.TreeFrog;
import com.linkyoshi90.monstergirls.entity.ai.ChorusGoal;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;

/** The ten frog and toad species beyond the two family archetypes. Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class AnuranSpeciesTests {
    /** Every animation a species plays beyond idle/walk/hop/swim/croak/tongue/sit (the file must contain them). */
    private static final Map<String, List<String>> ANIMATIONS = Map.of(
            "bullywug", List.of("bellow"),
            "bullfrog", List.of("bellow", "pound", "shove"),
            "tree_frog", List.of("glide", "sleep", "climb"),
            "poison_dart_frog", List.of("warn", "climb"),
            "glass_frog", List.of("peep", "guard"),
            "goliath_frog", List.of("stomp", "lift"),
            "desert_rain_frog", List.of("scream", "burrow", "sip"),
            "mucus_toad", List.of("puff", "dig"),
            "fire_bellied_toad", List.of("unken", "trill"),
            "cane_toad", List.of("gulp", "squirt"));
    private static final Map<String, List<String>> ANIMATIONS_2 = Map.of(
            "natterjack", List.of("run", "chorus", "burrow"),
            "surinam_toad", List.of("sweep", "brood", "birth", "idle_empty"));

    private static String readResource(String path) throws Exception {
        try (InputStream in = MonsterGirlsMod.class.getResourceAsStream(path)) {
            check(in != null, "missing resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void pool(GameTestHelper helper, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(cx + dx, 2, cz + dz), Blocks.WATER);
        }
    }

    @GameTest(template = PLATFORM, batch = "everySpeciesHasItsOwnEggAndAssets")
    public static void everySpeciesHasItsOwnEggAndAssets(GameTestHelper helper) throws Exception {
        java.util.Set<Item> eggs = new java.util.HashSet<>();
        for (String species : ModSounds.ANURAN_SPECIES) {
            var id = MonsterGirlsMod.id(species);
            check(BuiltInRegistries.ENTITY_TYPE.containsKey(id), "no entity " + species);
            Item egg = BuiltInRegistries.ITEM.get(MonsterGirlsMod.id(species + "_spawn_egg"));
            check(egg instanceof SpawnEggItem, "no spawn egg for " + species);
            check(((SpawnEggItem) egg).getType(new ItemStack(egg)) == BuiltInRegistries.ENTITY_TYPE.get(id), species + " egg spawns the wrong species");
            check(eggs.add(egg), species + " shares its egg with another species");
            check(ModSounds.anuran(species) != null && ModSounds.anuran(species).ambient().isBound(), "no sounds for " + species);
            check(MonsterGirlsMod.class.getResource("/assets/monstergirls/geo/entity/" + species + ".geo.json") != null, "missing geo for " + species);
            check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/" + species + ".png") != null, "missing texture for " + species);
            check(MonsterGirlsMod.class.getResource("/data/monstergirls/neoforge/biome_modifier/" + species + "_spawns.json") != null, "no spawns for " + species);
            check(MonsterGirlsMod.class.getResource("/data/monstergirls/tags/block/" + species + "_spawnable_on.json") != null, "no spawn ground for " + species);
            String animations = readResource("/assets/monstergirls/animations/entity/" + species + ".animation.json");
            for (String base : List.of("idle", "walk", "hop", "swim", "croak", "tongue", "sit")) {
                check(animations.contains("animation." + species + "." + base), species + " lacks animation " + base);
            }
            for (String extra : ANIMATIONS.getOrDefault(species, ANIMATIONS_2.getOrDefault(species, List.of()))) {
                check(animations.contains("animation." + species + "." + extra), species + " lacks animation " + extra);
            }
        }
        check(eggs.size() == 12, "expected twelve different spawn eggs");
        check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/fire_bellied_toad_glowmask.png") != null, "missing glow mask");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bullfrogNeverBacksDown")
    public static void bullfrogNeverBacksDown(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullfrog boss = helper.spawn(ModEntities.BULLFROG.get(), new BlockPos(6, 2, 6));
        boss.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(boss.getTarget() == player, "the bullfrog should turn on whoever hit her");
        check(boss.getFleeTicks() == 0, "the bullfrog never flees");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "poisonDartFrogPoisonsAttackers")
    public static void poisonDartFrogPoisonsAttackers(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        PoisonDartFrog frog = helper.spawn(ModEntities.POISON_DART_FROG.get(), new BlockPos(6, 2, 6));
        frog.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        var poison = player.getEffect(MobEffects.POISON);
        check(poison != null && poison.getAmplifier() == 1, "attacker should have Poison II: " + poison);
        check(frog.getFleeTicks() == 0 && frog.getTarget() == null, "she neither flees nor fights");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "glassFrogHidesFromMobs")
    public static void glassFrogHidesFromMobs(GameTestHelper helper) {
        GlassFrog glass = helper.spawn(ModEntities.GLASS_FROG.get(), new BlockPos(2, 2, 2));
        Silverfish near = helper.spawn(EntityType.SILVERFISH, new BlockPos(3, 2, 2));
        Silverfish far = helper.spawn(EntityType.SILVERFISH, new BlockPos(13, 2, 13));
        near.setNoAi(true);
        far.setNoAi(true);
        check(glass.getVisibilityPercent(near) > 0.5, "close mobs should see her: " + glass.getVisibilityPercent(near));
        check(glass.getVisibilityPercent(far) < 0.2, "distant mobs should barely notice her: " + glass.getVisibilityPercent(far));
        near.discard();
        far.discard();
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "goliathStompKnocksMobsBack")
    public static void goliathStompKnocksMobsBack(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 7));
        GoliathFrog goliath = helper.spawn(ModEntities.GOLIATH_FROG.get(), new BlockPos(7, 2, 7));
        Cow bystander = helper.spawn(EntityType.COW, new BlockPos(9, 2, 7));
        Vec3 before = bystander.position();
        goliath.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.succeedWhen(() -> {
            check(bystander.position().distanceTo(before) > 0.5, "the cow was not knocked back: " + bystander.position().distanceTo(before));
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "treeFrogGlidesDown")
    public static void treeFrogGlidesDown(GameTestHelper helper) {
        TreeFrog frog = helper.spawn(ModEntities.TREE_FROG.get(), new BlockPos(7, 6, 7)); // a four-block drop would cost a normal frog half a heart
        helper.succeedWhen(() -> {
            check(frog.onGround(), "still falling");
            check(frog.getHealth() == frog.getMaxHealth(), "a gliding frog takes no fall damage: " + frog.getHealth());
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "rainFrogSipsDew")
    public static void rainFrogSipsDew(GameTestHelper helper) {
        DesertRainFrog frog = helper.spawn(ModEntities.DESERT_RAIN_FROG.get(), new BlockPos(7, 2, 7));
        frog.setDryTicks(5000);
        check(frog.isDry() && !frog.isDriedOut(), "a thirsty rain frog is dry, not yet hurting: limit is longer than a bullywug's");
        frog.setAction(AbstractAnuran.Action.SIPPING);
        helper.runAfterDelay(10, () -> {
            check(frog.getDryTicks() < 4800, "sipping should quench her: " + frog.getDryTicks());
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fireBelliedToadFlashesHerBelly")
    public static void fireBelliedToadFlashesHerBelly(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        FireBelliedToad toad = helper.spawn(ModEntities.FIRE_BELLIED_TOAD.get(), new BlockPos(6, 2, 6));
        check(toad.fireImmune(), "she lives in the Nether");
        toad.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(toad.getAction() == AbstractAnuran.Action.UNKEN, "she should show her red belly: " + toad.getAction());
        check(player.hasEffect(MobEffects.CONFUSION), "toad mucus still applies");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "caneToadVenom")
    public static void caneToadVenom(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        CaneToad toad = helper.spawn(ModEntities.CANE_TOAD.get(), new BlockPos(6, 2, 6));
        toad.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(player.hasEffect(MobEffects.POISON), "venom should poison");
        check(player.hasEffect(MobEffects.CONFUSION), "venom should cause nausea");
        check(toad.getAction() == AbstractAnuran.Action.SQUIRTING, "she should squirt: " + toad.getAction());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "caneToadSwallowsItemsAndSmallMobs")
    public static void caneToadSwallowsItemsAndSmallMobs(GameTestHelper helper) {
        CaneToad toad = helper.spawn(ModEntities.CANE_TOAD.get(), new BlockPos(4, 2, 4));
        Silverfish pest = helper.spawn(EntityType.SILVERFISH, new BlockPos(8, 2, 4));
        pest.setNoAi(true);
        ItemEntity apple = helper.spawnItem(Items.APPLE, new BlockPos(4, 2, 9));
        check(!toad.isPest(helper.spawn(EntityType.COW, new BlockPos(12, 2, 12))), "cows are too big to swallow");
        helper.succeedWhen(() -> {
            check(pest.isRemoved() && pest.getRemovalReason() == net.minecraft.world.entity.Entity.RemovalReason.DISCARDED,
                    "the silverfish should be swallowed whole (gone, no death)");
            check(apple.isRemoved(), "the apple should be swallowed");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "natterjackChorusDrawsOthers")
    public static void natterjackChorusDrawsOthers(GameTestHelper helper) {
        Natterjack caller = helper.spawn(ModEntities.NATTERJACK.get(), new BlockPos(2, 2, 2));
        Natterjack listener = helper.spawn(ModEntities.NATTERJACK.get(), new BlockPos(12, 2, 12));
        new ChorusGoal(caller).callOthers();
        check(listener.getCallPos() != null, "the listener should have heard the chorus");
        helper.succeedWhen(() -> check(listener.position().distanceTo(caller.position()) < 5.0,
                "hasn't come to the chorus: " + listener.position().distanceTo(caller.position())));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "surinamToadBearsYoungAsBabies")
    public static void surinamToadBearsYoungAsBabies(GameTestHelper helper) {
        pool(helper, 7, 7);
        SurinamToad mother = helper.spawn(ModEntities.SURINAM_TOAD.get(), new BlockPos(7, 2, 7));
        mother.setYoung(3);
        mother.setDevelopTicks(SurinamToad.DEVELOP_TICKS - 1);
        helper.succeedWhen(() -> {
            var babies = helper.getLevel().getEntitiesOfClass(SurinamToad.class, mother.getBoundingBox().inflate(8.0), SurinamToad::isBaby);
            check(babies.size() == 3, "expected three babies, got " + babies.size());
            check(mother.getYoung() == 0, "the pits should be empty");
            check(babies.get(0).getBbHeight() < mother.getBbHeight() * 0.6F, "babies are small");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "surinamBabiesGrowUpAndSurviveSaving")
    public static void surinamBabiesGrowUpAndSurviveSaving(GameTestHelper helper) {
        pool(helper, 7, 7);
        SurinamToad toad = helper.spawn(ModEntities.SURINAM_TOAD.get(), new BlockPos(7, 2, 7));
        toad.setBaby(true);
        toad.setYoung(2);
        check(toad.getYoung() == 0, "a baby carries no young");
        toad.setGrowTicks(5);
        CompoundTag tag = new CompoundTag();
        toad.saveWithoutId(tag);
        SurinamToad copy = ModEntities.SURINAM_TOAD.get().create(helper.getLevel());
        copy.load(tag);
        check(copy.isBaby(), "baby flag lost");
        toad.setGrowTicks(AbstractAnuran.GROW_TICKS - 1);
        helper.runAfterDelay(10, () -> {
            check(!toad.isBaby(), "she should have grown up");
            toad.setYoung(2);
            CompoundTag adult = new CompoundTag();
            toad.saveWithoutId(adult);
            SurinamToad copy2 = ModEntities.SURINAM_TOAD.get().create(helper.getLevel());
            copy2.load(adult);
            check(copy2.getYoung() == 2 && !copy2.isBaby(), "young lost: " + copy2.getYoung());
            helper.succeed();
        });
    }
}
