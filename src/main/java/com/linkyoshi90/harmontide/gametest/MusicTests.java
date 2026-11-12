package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.AlbatrossHarpy;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import com.linkyoshi90.harmontide.entity.JukeboxSensing;
import com.linkyoshi90.harmontide.entity.OstrichHarpy;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import com.linkyoshi90.harmontide.entity.PhoenixHarpy;
import com.linkyoshi90.harmontide.entity.RiflebirdHarpy;
import com.linkyoshi90.harmontide.entity.SeagullHarpy;
import com.linkyoshi90.harmontide.entity.SparrowHarpy;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSongs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Jukebox reactions, one per species (and the despawn fix). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MusicTests {
    private static final BlockPos JUKEBOX = new BlockPos(7, 2, 7);

    /** Puts a jukebox down and starts the record playing. */
    private static JukeboxBlockEntity playRecord(GameTestHelper helper, BlockPos rel, Item disc) {
        helper.setBlock(rel, Blocks.JUKEBOX);
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) helper.getBlockEntity(rel);
        jukebox.setTheItem(new ItemStack(disc));
        check(jukebox.getSongPlayer().isPlaying(), "the record should be playing");
        return jukebox;
    }

    private static boolean near(GameTestHelper helper, AbstractHarpy harpy, BlockPos rel, double radius) {
        return harpy.distanceToSqr(Vec3.atBottomCenterOf(helper.absolutePos(rel))) < radius * radius;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "sparrowSingsAlong")
    public static void sparrowSingsAlong(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(1, 2, 1));
        helper.succeedWhen(() -> check(sparrow.getAction() == AbstractHarpy.Action.SINGING && near(helper, sparrow, JUKEBOX, 4.5),
                "isn't singing along by the jukebox"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "seagullFlockJoinsIn")
    public static void seagullFlockJoinsIn(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        SeagullHarpy a = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(1, 2, 1));
        SeagullHarpy b = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(13, 2, 13));
        helper.succeedWhen(() -> {
            check(a.getAction() == AbstractHarpy.Action.SINGING && near(helper, a, JUKEBOX, 5), "first gull isn't singing by the jukebox");
            check(b.getAction() == AbstractHarpy.Action.SINGING && near(helper, b, JUKEBOX, 5), "second gull isn't singing by the jukebox");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "riflebirdDancesToTheRecord")
    public static void riflebirdDancesToTheRecord(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 11));
        helper.succeedWhen(() -> {
            check(bird.getAction() == AbstractHarpy.Action.DANCING && near(helper, bird, JUKEBOX, 4), "isn't dancing by the jukebox");
            check(bird.getAffection(player) >= 1, "dancing for an audience should win her audience over a little");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "owlListensAtNight")
    public static void owlListensAtNight(GameTestHelper helper) {
        helper.setNight();
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(1, 2, 1));
        // Listening quietly within a few blocks for a while (not just passing through).
        int[] close = {0};
        helper.onEachTick(() -> close[0] = near(helper, owl, JUKEBOX, 6) && owl.getAction() == AbstractHarpy.Action.NONE ? close[0] + 1 : 0);
        helper.succeedWhen(() -> check(close[0] >= 100, "isn't sitting and listening by the jukebox"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "owlWokenByDaytimeMusic")
    public static void owlWokenByDaytimeMusic(GameTestHelper helper) {
        helper.setDayTime(1000);
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(3, 2, 3));
        boolean[] started = {false};
        helper.onEachTick(() -> {
            if (!started[0] && owl.isSleeping()) {
                started[0] = true;
                playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
            }
        });
        helper.succeedWhen(() -> check(started[0] && owl.getWokenByMusicCount() >= 1, "music by day should wake her"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "albatrossCourtshipDance")
    public static void albatrossCourtshipDance(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        AlbatrossHarpy albatross = helper.spawn(ModEntities.ALBATROSS_HARPY.get(), new BlockPos(2, 2, 2));
        helper.succeedWhen(() -> check(albatross.getAction() == AbstractHarpy.Action.DANCING, "nobody around: she should dance"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "albatrossTooShyToDanceForYou")
    public static void albatrossTooShyToDanceForYou(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        AlbatrossHarpy albatross = helper.spawn(ModEntities.ALBATROSS_HARPY.get(), new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        helper.onEachTick(() -> check(albatross.getAction() != AbstractHarpy.Action.DANCING, "danced with someone watching"));
        helper.runAtTickTime(300, () -> {
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "wildOstrichKicksTheJukebox")
    public static void wildOstrichKicksTheJukebox(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(3, 2, 3));
        ostrich.setHomePos(helper.absolutePos(new BlockPos(4, 2, 4)));
        helper.succeedWhen(() -> {
            check(!JukeboxSensing.isPlaying(helper.getLevel(), helper.absolutePos(JUKEBOX)), "the music is still playing");
            AABB around = new AABB(helper.absolutePos(JUKEBOX)).inflate(4);
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream().anyMatch(i -> i.getItem().is(Items.MUSIC_DISC_CAT)),
                    "the disc should have been kicked out");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "loyalOstrichDancesToYourMusic")
    public static void loyalOstrichDancesToYourMusic(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        ostrich.addAffection(player, OstrichHarpy.LOYAL);
        check(ostrich.getLoyalPlayer() == player, "should be loyal now");
        helper.succeedWhen(() -> {
            check(JukeboxSensing.isPlaying(helper.getLevel(), helper.absolutePos(JUKEBOX)), "she must not kick her companion's jukebox");
            check(ostrich.getAction() == AbstractHarpy.Action.DISPLAYING && near(helper, ostrich, JUKEBOX, 4.5), "isn't dancing to the music");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "musicSoothesAWildPhoenix")
    public static void musicSoothesAWildPhoenix(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        helper.onEachTick(() -> check(phoenix.getTarget() == null, "a soothed phoenix shouldn't pick a fight"));
        helper.runAtTickTime(300, () -> {
            check(player.getHealth() == player.getMaxHealth() && !player.isOnFire(), "the player got hurt despite the music");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "phoenixFireDancesToPigstep")
    public static void phoenixFireDancesToPigstep(GameTestHelper helper) {
        JukeboxBlockEntity jukebox = playRecord(helper, JUKEBOX, Items.MUSIC_DISC_PIGSTEP);
        check(JukeboxSongs.PIGSTEP.equals(JukeboxSensing.songAt(helper.getLevel(), jukebox.getBlockPos())), "should be Pigstep");
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(3, 2, 3));
        helper.succeedWhen(() -> check(phoenix.getAction() == AbstractHarpy.Action.STRIKING && near(helper, phoenix, JUKEBOX, 5.5),
                "isn't fire-dancing to Pigstep"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "crowLearnsARecordAndSingsIt")
    public static void crowLearnsARecordAndSingsIt(GameTestHelper helper) {
        JukeboxBlockEntity jukebox = playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        // Someone standing by the jukebox, so she doesn't just steal the record.
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        helper.succeedWhen(() -> {
            check(crow.getKnownSongs().contains(JukeboxSongs.CAT.location()), "hasn't learned the song");
            jukebox.setTheItem(ItemStack.EMPTY);
            check(crow.mimicSong() && crow.isMimickingSong(), "should be able to sing it back");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1500, batch = "crowStealsTheRecord")
    public static void crowStealsTheRecord(GameTestHelper helper) {
        playRecord(helper, JUKEBOX, Items.MUSIC_DISC_CAT);
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        helper.succeedWhen(() -> {
            check(!JukeboxSensing.isPlaying(helper.getLevel(), helper.absolutePos(JUKEBOX)), "the record is still playing");
            check(crow.getMainHandItem().is(Tags.Items.MUSIC_DISCS) || crow.countCaches() > 0, "she should have the disc (or have buried it)");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wildHarpiesDespawnBondedOnesStay")
    public static void wildHarpiesDespawnBondedOnesStay(GameTestHelper helper) {
        SparrowHarpy sparrow = ModEntities.SPARROW_HARPY.get().spawn(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 3)), MobSpawnType.NATURAL);
        check(sparrow != null && sparrow.removeWhenFarAway(200.0), "a wild sparrow should be able to despawn");
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        sparrow.addAffection(player, 1);
        check(!sparrow.removeWhenFarAway(200.0), "one that likes someone should stay");
        RiflebirdHarpy bird = ModEntities.RIFLEBIRD_HARPY.get().spawn(helper.getLevel(), helper.absolutePos(new BlockPos(10, 2, 3)), MobSpawnType.NATURAL);
        helper.setBlock(new BlockPos(11, 2, 3), ModBlocks.RIFLEBIRD_NEST.get());
        bird.setHomePos(helper.absolutePos(new BlockPos(11, 2, 3)));
        check(!bird.removeWhenFarAway(200.0), "a riflebird living in a nest should stay");
        HarpyTestUtil.remove(player);
        sparrow.discard();
        bird.discard();
        helper.succeed();
    }
}
