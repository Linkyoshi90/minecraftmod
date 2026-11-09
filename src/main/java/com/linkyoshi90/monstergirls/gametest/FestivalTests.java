package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Festivals;
import com.linkyoshi90.monstergirls.entity.girl.Festivals.Festival;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Festivals (entity.girl.Festivals): the calendar, the feast, the dance, the presents. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class FestivalTests {
    private static final long DAY = 24000L;

    private static boolean criterion(ServerPlayer p, String c) {
        var a = p.server.getAdvancements().get(MonsterGirlsMod.id(Festivals.EVERY));
        return a != null && p.getAdvancements().getOrStartProgress(a).getCriterion(c).isDone();
    }

    /** A hearth with its owner and one friend living there. */
    private record Home(BlockPos hearth, ServerPlayer owner, AbstractMonsterGirl girl) {
    }

    private static Home home(GameTestHelper helper) {
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 2));
        girl.addAffection(p, Orders.AFFECTION);
        check(Settlement.settle(helper.getLevel(), girl, hearth), "she lives at the hearth");
        return new Home(helper.absolutePos(hp), p, girl);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theFestivalCalendar")
    public static void theFestivalCalendar(GameTestHelper helper) {
        check(Festivals.on(0) == null && Festivals.on(7 * DAY + 100) == Festival.DANCE && Festivals.on((24 + 15) * DAY) == Festival.HARVEST
                && Festivals.on(23 * DAY + 23999) == Festival.GIFTS && Festivals.on(19 * DAY) == Festival.HEARTS, "four days in every 24");
        check(Festivals.gathering(7 * DAY + 12000) && !Festivals.gathering(7 * DAY + 6000) && !Festivals.gathering(8 * DAY + 12000), "they gather at dusk");
        check(Festivals.daysUntil(0) == 7 && Festivals.next(0) == Festival.DANCE && Festivals.next(16 * DAY) == Festival.HEARTS && Festivals.next(20 * DAY) == Festival.GIFTS
                && Festivals.daysUntil(23 * DAY) == 0 && Festivals.daysUntil(23 * DAY + DAY) == 7, "the journal can say when the next one is");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theHarvestFeast")
    public static void theHarvestFeast(GameTestHelper helper) {
        Home h = home(helper);
        var level = helper.getLevel();
        check(Festivals.wants(level, h.hearth(), Festival.HARVEST) != null && Festivals.celebrate(level, h.owner(), h.hearth(), Festival.HARVEST) == 0,
                "no feast without food");
        BlockPos fp = new BlockPos(6, 2, 1);
        helper.setBlock(fp, ModBlocks.FRIDGE.get());
        var fridge = (FridgeBlockEntity) helper.getBlockEntity(fp);
        fridge.store().setItem(0, new ItemStack(Items.BREAD, 3));
        int before = h.girl().getAffection(h.owner());
        check(Festivals.celebrate(level, h.owner(), h.hearth(), Festival.HARVEST) == 1, "she sits down to the feast");
        check(fridge.store().getItem(0).getCount() == 2 && h.girl().getAffection(h.owner()) == before + Festivals.BONUS, "she eats, and is the fonder for it");
        check(h.owner().hasEffect(MobEffects.REGENERATION) && criterion(h.owner(), "harvest"), "and her friend feasts too");
        HarpyTestUtil.remove(h.owner());
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theMidsummerDance")
    public static void theMidsummerDance(GameTestHelper helper) {
        Home h = home(helper);
        var level = helper.getLevel();
        check(Festivals.celebrate(level, h.owner(), h.hearth(), Festival.DANCE) == 0, "no dance without music");
        BlockPos jp = new BlockPos(1, 2, 5);
        helper.setBlock(jp, Blocks.JUKEBOX);
        ((JukeboxBlockEntity) helper.getBlockEntity(jp)).setTheItem(new ItemStack(Items.MUSIC_DISC_CAT));
        check(Festivals.wants(level, h.hearth(), Festival.DANCE) == null && Festivals.celebrate(level, h.owner(), h.hearth(), Festival.DANCE) == 1, "they dance");
        check(criterion(h.owner(), "dance"), "and it counts");
        HarpyTestUtil.remove(h.owner());
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theGiftExchange")
    public static void theGiftExchange(GameTestHelper helper) {
        Home h = home(helper);
        var level = helper.getLevel();
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 4));
        check(Festivals.guests(level, h.hearth()).size() == 1, "only those who live there are guests");
        int before = h.girl().getAffection(h.owner());
        check(Festivals.celebrate(level, h.owner(), h.hearth(), Festival.GIFTS) == 1 && h.girl().getAffection(h.owner()) == before + Festivals.BONUS
                && stranger.getAffection(h.owner()) == 0, "she brings a present");
        check(criterion(h.owner(), "gifts"), "and it counts");
        HarpyTestUtil.remove(h.owner());
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theDayOfHearts")
    public static void theDayOfHearts(GameTestHelper helper) {
        Home h = home(helper);
        var level = helper.getLevel();
        check(Festivals.wants(level, h.hearth(), Festival.HEARTS) != null && Festivals.celebrate(level, h.owner(), h.hearth(), Festival.HEARTS) == 0, "no cake, no party");
        BlockPos cp = new BlockPos(6, 2, 1);
        helper.setBlock(cp, Blocks.CAKE);
        int before = h.girl().getAffection(h.owner());
        check(Festivals.celebrate(level, h.owner(), h.hearth(), Festival.HEARTS) == 1 && h.girl().getAffection(h.owner()) == before + Festivals.BONUS, "a slice each");
        check(helper.getBlockState(cp).getValue(net.minecraft.world.level.block.CakeBlock.BITES) == 1, "one bite gone");
        check(h.owner().getInventory().countItem(Items.COOKIE) == 1 && criterion(h.owner(), "hearts"), "a sweetheart cookie, and it counts");
        HarpyTestUtil.remove(h.owner());
        helper.succeed();
    }
}
