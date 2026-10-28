package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Expeditions;
import com.linkyoshi90.monstergirls.entity.Journal;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlArms;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Life with the girls beyond befriending: weapons for the fighters, the journal, settlements and expeditions. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HarmontideLifeTests {

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fighterTakesABowAndShoots")
    public static void fighterTakesABowAndShoots(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.KAMAITACHI_RODENT, new BlockPos(3, 2, 3));
        AbstractMonsterGirl other = still(helper, GirlSpecies.FENNEC_KITSUNE, new BlockPos(3, 2, 6));
        ServerPlayer p = friendOf(helper, girl, new BlockPos(5, 2, 5));
        other.addAffection(p, Orders.AFFECTION);
        ItemStack bow = new ItemStack(Items.BOW);
        p.setItemInHand(InteractionHand.MAIN_HAND, bow);
        check(GirlArms.interact(other, p, bow) == null, "a girl who doesn't fight for her friend won't take it");
        check(GirlArms.interact(girl, p, bow) != null && girl.getMainHandItem().is(Items.BOW) && bow.isEmpty(), "the kamaitachi takes the bow");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 3));
        z.setNoAi(true);
        girl.setTarget(z);
        check(new GirlArms.RangedGoal(girl).canUse(), "armed, with a target, she shoots");
        check(GirlArms.aimOf(girl.getMainHandItem()) == AbstractMonsterGirl.AIM_BOW && GirlArms.aimOf(new ItemStack(Items.CROSSBOW)) == AbstractMonsterGirl.AIM_CROSSBOW,
                "a bow is drawn, a crossbow levelled");
        girl.setAim(GirlArms.aimOf(girl.getMainHandItem()));
        check(girl.getAim() == AbstractMonsterGirl.AIM_BOW, "she raises her bow");
        GirlArms.shootArrow(girl, z, girl.getMainHandItem());
        check(girl.lastShotTick() == girl.tickCount, "the shot is announced (the client's release and recoil)");
        List<Arrow> arrows = helper.getLevel().getEntitiesOfClass(Arrow.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16));
        check(!arrows.isEmpty() && arrows.get(0).getOwner() == girl, "an arrow of hers is flying");
        p.setShiftKeyDown(true);
        check(GirlArms.interact(girl, p, ItemStack.EMPTY) != null && girl.getMainHandItem().isEmpty() && p.getInventory().contains(new ItemStack(Items.BOW)),
                "sneaking with an empty hand, her friend takes the bow back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fighterFetchesAThrownWeapon")
    public static void fighterFetchesAThrownWeapon(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.KAMAITACHI_RODENT, new BlockPos(3, 2, 3));
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7), "stranger");
        ServerPlayer p = friendOf(helper, girl, new BlockPos(5, 2, 5));
        net.minecraft.world.entity.item.ItemEntity fromStranger = stranger.drop(new ItemStack(Items.BOW), true, true);
        net.minecraft.world.entity.item.ItemEntity fromFriend = p.drop(new ItemStack(Items.CROSSBOW), true, true);
        check(fromStranger != null && !GirlArms.wantsThrown(girl, fromStranger), "a stranger's bow is not for her");
        check(fromFriend != null && GirlArms.wantsThrown(girl, fromFriend), "her friend's crossbow is");
        check(GirlArms.pickUp(girl, fromFriend) && girl.getMainHandItem().is(Items.CROSSBOW) && fromFriend.isRemoved(), "she picks it up and holds it");
        HarpyTestUtil.remove(stranger);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    /** A trapdoor spider saved out of her burrow but still weightless (as old saves are) comes back with her gravity. */
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "trapdoorSpiderKeepsHerWeight")
    public static void trapdoorSpiderKeepsHerWeight(GameTestHelper helper) {
        AbstractMonsterGirl spider = still(helper, GirlSpecies.TRAPDOOR_SPIDER_ARACHNE, new BlockPos(3, 2, 3));
        spider.setNoGravity(true);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        spider.saveWithoutId(tag);
        spider.load(tag);
        check(!spider.isNoGravity(), "reloaded outside her burrow, she falls like anyone");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "journalRecordsMeetingsAndFriends")
    public static void journalRecordsMeetingsAndFriends(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.YUREI_UNDEAD, new BlockPos(3, 2, 3));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        check(Journal.book(p).pages().size() == 1 + Journal.GUIDE.size(), "a fresh journal: the contents and the guide");
        check(Journal.record(p, girl, false) && !Journal.record(p, girl, false), "met once is news, twice is not");
        check(Journal.met(p).contains("yurei_undead") && Journal.friends(p).isEmpty(), "met, not yet befriended");
        check(Journal.record(p, girl, true) && Journal.friends(p).contains("yurei_undead"), "befriended");
        check(Journal.book(p).pages().size() == 2 + Journal.GUIDE.size(), "and she has her page");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "girlsSettleAndTheMarketOpens")
    public static void girlsSettleAndTheMarketOpens(GameTestHelper helper) {
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl[] girls = new AbstractMonsterGirl[3];
        for (int i = 0; i < 3; i++) {
            girls[i] = still(helper, GirlSpecies.SPHINX_CENTAUR, new BlockPos(3 + i * 2, 2, 3));
            if (i < 2) girls[i].addAffection(p, Orders.AFFECTION);
        }
        check(Settlement.settle(helper.getLevel(), girls[0], hearth) && helper.absolutePos(hp).equals(girls[0].homeHearth()), "a friend settles");
        check(!Settlement.settle(helper.getLevel(), girls[2], hearth), "a stranger doesn't");
        Settlement.settle(helper.getLevel(), girls[1], hearth);
        check(hearth.market(helper.getLevel()) == 0, "two residents: no market yet");
        girls[2].addAffection(p, Orders.AFFECTION);
        Settlement.settle(helper.getLevel(), girls[2], hearth);
        check(hearth.residentCount() == 3 && hearth.market(helper.getLevel()) == 3 && !hearth.store().isEmpty(), "three: each brings a present to the market");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "settledGirlBuildsHerHut")
    public static void settledGirlBuildsHerHut(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.FENNEC_KITSUNE, new BlockPos(1, 2, 1));
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        List<Settlement.Piece> plan = Settlement.hut(GirlSpecies.FENNEC_KITSUNE, origin, Direction.NORTH);
        check(plan.size() == 46 + 25 + 3, "walls with a doorway, a roof, a bed and a lantern");
        check(plan.stream().anyMatch(pc -> pc.state().getBlock() instanceof BedBlock), "with a bed");
        girl.planHutAt(origin, Direction.NORTH);
        for (int i = 0; i < 3; i++) Settlement.buildStep(helper.getLevel(), girl);
        check(girl.hutStep() == 3 && helper.getLevel().getBlockState(plan.get(0).pos()).is(plan.get(0).state().getBlock()), "she builds it a block at a time");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "expeditionGoesAndComesBack")
    public static void expeditionGoesAndComesBack(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.SPHINX_CENTAUR, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, girl, new BlockPos(5, 2, 5));
        UUID id = girl.getUUID();
        ItemStack map = new ItemStack(Items.MAP);
        check(Expeditions.send(girl, p, map) && girl.isRemoved() && map.isEmpty() && Expeditions.away(p) == 1, "with the map, she sets off");
        check(Expeditions.returnDue(p, false).isEmpty(), "not back before her day is up");
        List<AbstractMonsterGirl> back = Expeditions.returnDue(p, true);
        check(back.size() == 1 && back.get(0).getUUID().equals(id) && back.get(0).getAffection(p) >= Orders.AFFECTION && Expeditions.away(p) == 0,
                "she comes back, herself, and still your friend");
        check(!Expeditions.finds(back.get(0), p).isEmpty(), "with finds");
        AbstractMonsterGirl friend = Expeditions.companion(helper.getLevel(), p, back.get(0));
        check(friend != null && friend.getSpecies().family() == GirlSpecies.Family.CENTAUR && friend.getAffection(p) >= Orders.AFFECTION,
                "and sometimes a friend of her family, who likes you already");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
