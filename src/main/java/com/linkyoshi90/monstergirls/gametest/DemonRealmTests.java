package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HeartfruitBushBlock;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.DemonRealm;
import com.linkyoshi90.monstergirls.entity.Flirting;
import com.linkyoshi90.monstergirls.entity.Missing;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.HolyWaterItem;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The Demon Realm (entity/DemonRealm): realm grass, light and holy water, heartfruit, a Lilith's touch, and what it does to friends. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class DemonRealmTests {
    private static BlockPos ground(GameTestHelper helper, int x, int z) {
        return helper.absolutePos(new BlockPos(x, 1, z));
    }

    private static boolean realm(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.REALM_GRASS.get());
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "realmShunsTheLight")
    public static void realmShunsTheLight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos dark = ground(helper, 2, 2), covered = ground(helper, 4, 2), litGround = ground(helper, 11, 11), nearTorch = ground(helper, 11, 12);
        check(RealmGrassBlock.corrupt(level, dark) && realm(level, dark), "dark grass turns");
        check(RealmGrassBlock.corrupt(level, covered), "so does this one, for now");
        level.setBlockAndUpdate(covered.above(), Blocks.STONE.defaultBlockState());
        RealmGrassBlock.corrupt(level, nearTorch);
        level.setBlockAndUpdate(litGround.above(), Blocks.TORCH.defaultBlockState());
        helper.runAfterDelay(20, () -> {
            check(!RealmGrassBlock.corrupt(level, litGround) && level.getBlockState(litGround).is(Blocks.GRASS_BLOCK), "never into torchlight");
            BlockState s = level.getBlockState(nearTorch);
            s.randomTick(level, nearTorch, level.random);
            check(level.getBlockState(nearTorch).is(Blocks.GRASS_BLOCK), "lit realm grass turns back");
            s = level.getBlockState(covered);
            s.randomTick(level, covered, level.random);
            check(level.getBlockState(covered).is(Blocks.DIRT), "covered, it dies to dirt");
            check(realm(level, dark), "the dark one stays");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmSpreadsOverGrass")
    public static void realmSpreadsOverGrass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos from = ground(helper, 7, 7);
        RealmGrassBlock.corrupt(level, from);
        int spread = 0;
        for (int i = 0; i < 200; i++) if (RealmGrassBlock.spread(level, from, level.random) != null) spread++;
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(from.offset(-1, 0, -1), from.offset(1, 0, 1))) if (realm(level, p)) count++;
        check(spread > 0 && count > 1, "it took its neighbours: " + count);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "holyWaterCleanses")
    public static void holyWaterCleanses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = ground(helper, 7, 4), far = ground(helper, 7, 12);
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-1, 0, -1), centre.offset(1, 0, 1))) RealmGrassBlock.corrupt(level, p.immutable());
        RealmGrassBlock.corrupt(level, far);
        check(HeartfruitBushBlock.sprout(level, centre), "a bush");
        check(HolyWaterItem.cleanse(level, centre) == 9, "nine blocks cleansed");
        check(level.getBlockState(centre).is(Blocks.GRASS_BLOCK) && level.getBlockState(centre.above()).isAir(), "grass again, the bush gone");
        check(realm(level, far), "out of reach stays");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "heartfruitBush")
    public static void heartfruitBush(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos g = ground(helper, 4, 4), plain = ground(helper, 11, 11);
        RealmGrassBlock.corrupt(level, g);
        RealmGrassBlock.corrupt(level, g.east(2));
        check(HeartfruitBushBlock.sprout(level, g), "sprouts on realm grass");
        check(!HeartfruitBushBlock.sprout(level, g.east(2)), "not crowded");
        check(!ModBlocks.HEARTFRUIT_BUSH.get().defaultBlockState().canSurvive(level, plain.above()), "only on realm grass");
        BlockPos bush = g.above();
        check(HeartfruitBushBlock.pick(level, bush, level.getBlockState(bush)) == 0, "unripe");
        level.setBlockAndUpdate(bush, level.getBlockState(bush).setValue(HeartfruitBushBlock.AGE, 3));
        check(HeartfruitBushBlock.pick(level, bush, level.getBlockState(bush)) >= 2, "ripe: two or three");
        check(level.getBlockState(bush).getValue(HeartfruitBushBlock.AGE) == 1, "starts again");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lilithSeedsWhileWild")
    public static void lilithSeedsWhileWild(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl lilith = spawn(helper, GirlSpecies.LILITH_DEVIL, new BlockPos(7, 2, 7));
        lilith.setNoAi(true);
        check(DemonRealm.seeds(lilith), "a wild Lilith grows a realm");
        BlockPos at = null;
        for (int i = 0; i < 30 && at == null; i++) at = DemonRealm.seed(level, lilith);
        check(at != null && realm(level, at), "her touch turned the ground");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        lilith.addAffection(p, 10);
        check(!DemonRealm.seeds(lilith), "a befriended one keeps it in");
        check(!DemonRealm.seeds(spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(2, 2, 2))), "only a Lilith");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmStirsFriends")
    public static void realmStirsFriends(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(2, 2, 2)), away = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(11, 2, 11));
        girl.setNoAi(true);
        away.setNoAi(true);
        com.linkyoshi90.monstergirls.entity.MatingSeason.settle(girl);
        com.linkyoshi90.monstergirls.entity.MatingSeason.settle(away);
        RealmGrassBlock.corrupt(level, ground(helper, 2, 2));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        check(DemonRealm.inRealm(girl) && !DemonRealm.inRealm(away), "standing in it");
        check(!DemonRealm.breathe(level, girl), "a stranger feels nothing for them");
        girl.addAffection(p, 60);
        away.addAffection(p, 60);
        check(DemonRealm.breathe(level, girl) && Missing.longing(girl, p) == DemonRealm.LONGING, "a friend longs");
        check(!DemonRealm.breathe(level, away), "not outside it");
        check(Flirting.chance(girl, p) == 2 && Flirting.chance(away, p) == 4, "flirts more in the realm");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "heartfruitLovestruck")
    public static void heartfruitLovestruck(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(2, 2, 2)), shy = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(6, 2, 2));
        girl.setNoAi(true);
        shy.setNoAi(true);
        com.linkyoshi90.monstergirls.entity.MatingSeason.settle(girl);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 2)), other = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 4), "other");
        girl.addAffection(p, 60);
        girl.addAffection(other, 60);
        shy.addAffection(p, 5);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HEARTFRUIT.get(), 3));
        float before = Missing.bedBonus(girl, p);
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(DemonRealm.lovestruck(girl, p) && p.getMainHandItem().getCount() == 2, "she ate it and is lovestruck");
        check(Flirting.chance(girl, p) == 1 && Missing.bedBonus(girl, p) == before + DemonRealm.LOVESTRUCK_BED, "flirts at every pat, likelier to come to bed");
        check(!DemonRealm.lovestruck(girl, other) && Flirting.chance(girl, other) == 4, "only with who fed her");
        CommonHooks.onInteractEntity(p, shy, InteractionHand.MAIN_HAND);
        check(!DemonRealm.lovestruck(shy, p), "not someone who isn't close");
        HarpyTestUtil.remove(p);
        HarpyTestUtil.remove(other);
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmCreepsIntoTrees")
    public static void realmCreepsIntoTrees(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos g = ground(helper, 7, 7);
        RealmGrassBlock.corrupt(level, g);
        for (BlockPos p : BlockPos.betweenClosed(g.offset(-2, 4, -2), g.offset(2, 4, 2))) {
            level.setBlockAndUpdate(p.immutable(), Blocks.BIRCH_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        }
        BlockPos took = null;
        for (int i = 0; i < 40 && took == null; i++) took = com.linkyoshi90.monstergirls.block.RealmLeavesBlock.creep(level, g, level.random);
        check(took != null && level.getBlockState(took).is(ModBlocks.REALM_LEAVES.get()), "the realm reaches the leaves above it");
        BlockState s = level.getBlockState(took);
        check(s.getValue(com.linkyoshi90.monstergirls.block.RealmLeavesBlock.KIND) == com.linkyoshi90.monstergirls.block.RealmLeavesBlock.Kind.BIRCH
                && s.getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT), "remembering what they were");
        HolyWaterItem.cleanse(level, g);
        check(level.getBlockState(took).is(Blocks.BIRCH_LEAVES) && level.getBlockState(took).getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT),
                "holy water gives the tree back its own leaves");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "weepcapsGrowOnTheRealm")
    public static void weepcapsGrowOnTheRealm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos g = ground(helper, 4, 4);
        check(!com.linkyoshi90.monstergirls.block.WeepcapBlock.sprout(level, g), "not on plain grass");
        for (BlockPos p : BlockPos.betweenClosed(g.offset(-2, 0, -2), g.offset(2, 0, 2))) RealmGrassBlock.corrupt(level, p.immutable());
        int grown = 0;
        for (BlockPos p : BlockPos.betweenClosed(g.offset(-2, 0, -2), g.offset(2, 0, 2))) if (com.linkyoshi90.monstergirls.block.WeepcapBlock.sprout(level, p.immutable())) grown++;
        check(grown >= com.linkyoshi90.monstergirls.block.WeepcapBlock.CROWD && grown <= 12, "a scattering, not a carpet: " + grown);
        check(level.getBlockState(g.above()).getLightEmission(level, g.above()) > 0 || level.getBlockState(g.offset(-2, 1, -2)).is(ModBlocks.WEEPCAP.get()), "they glow");
        RealmGrassBlock.purify(level, g.offset(-2, 0, -2));
        check(!level.getBlockState(g.offset(-2, 1, -2)).is(ModBlocks.WEEPCAP.get()), "and wither with the realm");
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmRootsAndVines")
    public static void realmRootsAndVines(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos g = ground(helper, 3, 3);
        level.setBlockAndUpdate(g.above(), Blocks.SHORT_GRASS.defaultBlockState());
        check(RealmGrassBlock.corrupt(level, g) && level.getBlockState(g.above()).is(Blocks.CRIMSON_ROOTS), "the tufts turn crimson");
        RealmGrassBlock.purify(level, g);
        check(level.getBlockState(g).is(Blocks.GRASS_BLOCK) && level.getBlockState(g.above()).is(Blocks.SHORT_GRASS), "and back");
        BlockPos leaf = ground(helper, 10, 10).above(5);
        level.setBlockAndUpdate(leaf, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        check(com.linkyoshi90.monstergirls.block.RealmLeavesBlock.corrupt(level, leaf), "realm leaves");
        check(com.linkyoshi90.monstergirls.block.RealmLeavesBlock.hangVine(level, leaf) && level.getBlockState(leaf.below()).is(Blocks.WEEPING_VINES), "a weeping vine hangs");
        com.linkyoshi90.monstergirls.block.RealmLeavesBlock.purify(level, leaf);
        check(level.getBlockState(leaf).is(Blocks.OAK_LEAVES) && level.getBlockState(leaf.below()).isAir(), "the vine withers with the realm");
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmCreepsUpTrunks")
    public static void realmCreepsUpTrunks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos g = ground(helper, 7, 7);
        RealmGrassBlock.corrupt(level, g);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (dx != 0 || dz != 0) RealmGrassBlock.corrupt(level, g.offset(dx, 0, dz));
            level.setBlockAndUpdate(g.offset(dx, 1, dz), Blocks.SPRUCE_LOG.defaultBlockState());   // a wide stump: wherever the realm looks, a trunk
        }
        BlockPos took = com.linkyoshi90.monstergirls.block.RealmLeavesBlock.creep(level, g, level.random);
        check(took != null && level.getBlockState(took).is(ModBlocks.REALM_LOG.get()), "up the trunk");
        check(level.getBlockState(took).getValue(com.linkyoshi90.monstergirls.block.RealmLogBlock.KIND) == com.linkyoshi90.monstergirls.block.RealmLogBlock.Kind.SPRUCE, "a spruce at heart");
        HolyWaterItem.cleanse(level, g);
        check(level.getBlockState(took).is(Blocks.SPRUCE_LOG), "spruce again");
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmStopsAtItsEdge")
    public static void realmStopsAtItsEdge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos edge = ground(helper, 7, 7), source = ground(helper, 3, 3);
        RealmGrassBlock.corrupt(level, edge, RealmGrassBlock.MAX_DEPTH);
        for (int i = 0; i < 200; i++) check(RealmGrassBlock.spread(level, edge, level.random) == null, "the edge of the realm spreads no further");
        RealmGrassBlock.corrupt(level, source);
        BlockPos to = null;
        for (int i = 0; i < 200 && to == null; i++) to = RealmGrassBlock.spread(level, source, level.random);
        check(to != null && level.getBlockState(to).getValue(RealmGrassBlock.DEPTH) == 1, "a step from its source");
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wildRealmPatch")
    public static void wildRealmPatch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = ground(helper, 7, 7);
        BlockPos trunk = centre.offset(1, 1, 0);
        for (int y = 0; y < 4; y++) level.setBlockAndUpdate(trunk.above(y), Blocks.DARK_OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(trunk.above(4), Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        int taken = com.linkyoshi90.monstergirls.world.RealmPatchFeature.placePatch(level, centre, level.random, (x, z) -> centre.getY());
        check(taken >= 30, "a patch of realm: " + taken);
        check(level.getBlockState(centre).is(ModBlocks.REALM_GRASS.get()) && level.getBlockState(centre).getValue(RealmGrassBlock.DEPTH) == 0, "its heart is a source");
        check(level.getBlockState(trunk.above(2)).is(ModBlocks.REALM_LOG.get()) && level.getBlockState(trunk.above(4)).is(ModBlocks.REALM_LEAVES.get()), "the tree over it turned");
        helper.succeed();
    }
}
