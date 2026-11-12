package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HeartfruitBushBlock;
import com.linkyoshi90.harmontide.block.RealmGrassBlock;
import com.linkyoshi90.harmontide.block.WeepcapBlock;
import com.linkyoshi90.harmontide.entity.DemonRealm;
import com.linkyoshi90.harmontide.item.HolyWaterItem;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Realm variants (block/RealmGrassBlock#variantFor, block/RealmSandBlock): the realm takes on its land. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RealmVariantTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theRealmTakesOnItsLand")
    public static void theRealmTakesOnItsLand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(4, 1, 4));
        check(RealmGrassBlock.variantFor(level, at, Blocks.SAND.defaultBlockState(), 0).is(ModBlocks.REALM_SAND.get()), "sand becomes realm sand");
        check(RealmGrassBlock.variantFor(level, at, Blocks.RED_SAND.defaultBlockState(), 0).is(ModBlocks.REALM_SAND.get()), "red sand too");
        check(RealmGrassBlock.variantFor(level, at, Blocks.MUD.defaultBlockState(), 0).is(ModBlocks.REALM_BOG.get()), "mud becomes the bog realm");
        boolean cold = level.getBiome(at).value().coldEnoughToSnow(at);
        check(RealmGrassBlock.variantFor(level, at, Blocks.GRASS_BLOCK.defaultBlockState(), 3).is(cold ? ModBlocks.REALM_FROST.get() : ModBlocks.REALM_GRASS.get()),
                "grass becomes the realm's grass here (or its frost, in the cold)");
        check(RealmGrassBlock.variantFor(level, at, Blocks.SAND.defaultBlockState(), 5).getValue(RealmGrassBlock.DEPTH) == 5, "and keeps its distance from the source");
        for (var b : new net.minecraft.world.level.block.Block[]{ModBlocks.REALM_GRASS.get(), ModBlocks.REALM_FROST.get(), ModBlocks.REALM_BOG.get(), ModBlocks.REALM_SAND.get()}) {
            check(RealmGrassBlock.realm(b.defaultBlockState()), "all of it is the realm: " + b);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "realmSandGrowsHeartfruitAndWashesBack")
    public static void realmSandGrowsHeartfruitAndWashesBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos rel = new BlockPos(4, 1, 4);
        helper.setBlock(rel.below(), Blocks.STONE);
        helper.setBlock(rel, Blocks.SAND);
        BlockPos ground = helper.absolutePos(rel);
        check(RealmGrassBlock.corrupt(level, ground), "the realm takes the sand");
        check(level.getBlockState(ground).is(ModBlocks.REALM_SAND.get()), "as realm sand");
        check(!WeepcapBlock.grows(level.getBlockState(ground)) && HeartfruitBushBlock.grows(level.getBlockState(ground)), "heartfruit, no weepcaps");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, rel.above());
        p.teleportTo(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5);
        check(DemonRealm.inRealm(p), "standing on it is standing in the realm");
        check(HolyWaterItem.cleanse(level, ground) >= 1 && level.getBlockState(ground).is(Blocks.SAND), "holy water makes it sand again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theBogGrowsWeepcaps")
    public static void theBogGrowsWeepcaps(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos rel = new BlockPos(6, 1, 6);
        helper.setBlock(rel, Blocks.MUD);
        BlockPos ground = helper.absolutePos(rel);
        check(RealmGrassBlock.corrupt(level, ground) && level.getBlockState(ground).is(ModBlocks.REALM_BOG.get()), "mud turns to the bog realm");
        check(!HeartfruitBushBlock.sprout(level, ground), "no heartfruit in the bog");
        check(WeepcapBlock.sprout(level, ground) && level.getBlockState(ground.above()).is(ModBlocks.WEEPCAP.get()), "but weepcaps");
        level.setBlock(ground.above(), Blocks.AIR.defaultBlockState(), 3);
        RealmGrassBlock.purify(level, ground);
        check(!RealmGrassBlock.realm(level.getBlockState(ground)), "purified");
        helper.succeed();
    }
}
