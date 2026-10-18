package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.GoliathFrog;
import com.linkyoshi90.monstergirls.entity.SurinamToad;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Hatchlings, tadpoles and froglets, and the nest the eggs sit in. Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class YoungStageTests {
    private static final List<String> HARPIES = List.of("sparrow_harpy", "crow_harpy", "ostrich_harpy", "owl_harpy", "seagull_harpy",
            "albatross_harpy", "riflebird_harpy", "phoenix_harpy");

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

    private static void checkModel(String name, String species, List<String> animations) throws Exception {
        check(MonsterGirlsMod.class.getResource("/assets/monstergirls/geo/entity/" + name + ".geo.json") != null, "missing geo for " + name);
        check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/" + name + ".png") != null, "missing texture for " + name);
        String file = readResource("/assets/monstergirls/animations/entity/" + name + ".animation.json");
        for (String a : animations) check(file.contains("animation." + name + "." + a), name + " lacks animation " + a);
    }

    @GameTest(template = PLATFORM, batch = "everyYoungStageHasAModel")
    public static void everyYoungStageHasAModel(GameTestHelper helper) throws Exception {
        for (String harpy : HARPIES) checkModel(harpy + "_hatchling", harpy, List.of("idle", "walk", "fly"));
        for (String species : ModSounds.ANURAN_SPECIES) {
            if (species.equals("surinam_toad")) continue;   // her young are miniature adults
            checkModel(species + "_tadpole", species, List.of("idle", "swim"));
            checkModel(species + "_froglet", species, List.of("idle", "walk", "hop", "swim"));
        }
        check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/phoenix_harpy_hatchling_glowmask.png") != null, "phoenix hatchling glow");
        check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/fire_bellied_toad_froglet_glowmask.png") != null, "fire-bellied froglet glow");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "everyEggStageHasANestModel")
    public static void everyEggStageHasANestModel(GameTestHelper helper) throws Exception {
        for (String harpy : HARPIES) {
            for (int stage = 0; stage < 4; stage++) {
                check(MonsterGirlsMod.class.getResource("/assets/monstergirls/models/block/harpy_egg_" + harpy + "_" + stage + ".json") != null, "model " + harpy + stage);
                check(MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/block/harpy_egg_" + harpy + "_" + stage + ".png") != null, "texture " + harpy + stage);
            }
        }
        String states = readResource("/assets/monstergirls/blockstates/harpy_egg.json");
        for (HarpyEggBlock.Species s : HarpyEggBlock.Species.values()) check(states.contains("species=" + s.getSerializedName()), "blockstate lacks " + s);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "tadpolesStayInTheWater")
    public static void tadpolesStayInTheWater(GameTestHelper helper) {
        pool(helper, 7, 7);
        Bullywug tadpole = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 2, 7));
        tadpole.setTadpole();
        check(tadpole.isTadpole() && tadpole.isBaby(), "should be a tadpole");
        check(tadpole.modelName().equals("bullywug_tadpole"), "model: " + tadpole.modelName());
        check(tadpole.getBbHeight() < 0.5F, "tadpoles are tiny: " + tadpole.getBbHeight());
        helper.runAfterDelay(120, () -> {
            check(tadpole.isInWater(), "should still be swimming: " + helper.relativeVec(tadpole.position()));
            check(tadpole.getAirSupply() == tadpole.getMaxAirSupply(), "tadpoles have gills");
            check(tadpole.getHealth() == tadpole.getMaxHealth(), "unharmed");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "strandedTadpoleFindsThePond")
    public static void strandedTadpoleFindsThePond(GameTestHelper helper) {
        pool(helper, 11, 11);
        GoliathFrog tadpole = helper.spawn(ModEntities.GOLIATH_FROG.get(), new BlockPos(4, 2, 4));
        tadpole.setTadpole();
        helper.succeedWhen(() -> check(tadpole.isInWater(), "hasn't reached the pond: " + helper.relativeVec(tadpole.position())));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tadpolesGrowLegsThenGrowUp")
    public static void tadpolesGrowLegsThenGrowUp(GameTestHelper helper) {
        pool(helper, 7, 7);
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 2, 7));
        frog.setTadpole();
        frog.setGrowTicks(AbstractAnuran.TADPOLE_TICKS - 1);
        helper.runAfterDelay(5, () -> {
            check(frog.getStage() == AbstractAnuran.FROGLET && frog.modelName().equals("bullywug_froglet"), "should be a froglet now: " + frog.modelName());
            frog.setGrowTicks(AbstractAnuran.GROW_TICKS - 1);
            CompoundTag tag = new CompoundTag();
            frog.saveWithoutId(tag);
            Bullywug copy = ModEntities.BULLYWUG.get().create(helper.getLevel());
            copy.load(tag);
            check(copy.getStage() == AbstractAnuran.FROGLET, "stage lost when saving: " + copy.getStage());
            helper.runAfterDelay(5, () -> {
                check(!frog.isBaby() && frog.modelName().equals("bullywug"), "should be grown up: " + frog.modelName());
                check(frog.getBbHeight() > 1.5F, "adult size: " + frog.getBbHeight());
                helper.succeed();
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "froletsFleeInsteadOfFighting")
    public static void frogletsFleeInsteadOfFighting(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug a = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 6));
        Bullywug b = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 2, 6));
        Bullywug c = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 7));
        a.setBaby(true);
        a.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(a.getTarget() == null && a.getFleeTicks() > 0, "a froglet runs away");
        check(b.getTarget() == null && c.getTarget() == null, "froglets do not make a pack");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "surinamYoungKeepTheAdultModel")
    public static void surinamYoungKeepTheAdultModel(GameTestHelper helper) {
        pool(helper, 7, 7);
        SurinamToad toad = helper.spawn(ModEntities.SURINAM_TOAD.get(), new BlockPos(7, 2, 7));
        toad.setTadpole();   // no tadpole stage for her: a toadling straight away
        check(toad.isBaby() && !toad.isTadpole() && toad.modelName().equals("surinam_toad"), "toadling: " + toad.modelName());
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hatchlingHarpiesUseTheirOwnModel")
    public static void hatchlingHarpiesUseTheirOwnModel(GameTestHelper helper) {
        for (String id : HARPIES) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(MonsterGirlsMod.id(id));
            AbstractHarpy harpy = (AbstractHarpy) type.create(helper.getLevel());
            check(harpy.modelName().equals(id), "adult model " + harpy.modelName());
            harpy.setBaby(true);
            check(harpy.modelName().equals(id + "_hatchling"), "hatchling model " + harpy.modelName());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "crackedEggHatchesAtTheNest")
    public static void crackedEggHatchesAtTheNest(GameTestHelper helper) {
        BlockPos pos = new BlockPos(7, 2, 7);
        BlockState egg = ModBlocks.HARPY_EGG.get().defaultBlockState().setValue(HarpyEggBlock.SPECIES, HarpyEggBlock.Species.PHOENIX)
                .setValue(HarpyEggBlock.HATCH, 3);
        helper.setBlock(pos, egg);
        check(helper.getBlockState(pos).getValue(HarpyEggBlock.HATCH) == 3, "last stage");
        HarpyEggBlock.hatch(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos));
        var chicks = helper.getLevel().getEntitiesOfClass(AbstractHarpy.class, new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(3.0));
        check(chicks.size() == 1 && chicks.get(0).isBaby() && chicks.get(0).modelName().equals("phoenix_harpy_hatchling"), "a phoenix hatchling should have hatched");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spawnHatchesIntoTadpoles")
    public static void spawnHatchesIntoTadpoles(GameTestHelper helper) {
        pool(helper, 7, 7);
        BlockPos spawn = new BlockPos(7, 3, 7);
        helper.setBlock(spawn, ModBlocks.ANURAN_SPAWN.get().defaultBlockState().setValue(AnuranSpawnBlock.SPECIES, AnuranSpawnBlock.Species.TREE_FROG));
        AnuranSpawnBlock.hatch(helper.getLevel(), helper.absolutePos(spawn), helper.getBlockState(spawn));
        var young = helper.getLevel().getEntitiesOfClass(AbstractAnuran.class, new net.minecraft.world.phys.AABB(helper.absolutePos(spawn)).inflate(4.0));
        check(!young.isEmpty() && young.stream().allMatch(AbstractAnuran::isTadpole), "the spawn hatches into tadpoles");
        helper.succeed();
    }
}
