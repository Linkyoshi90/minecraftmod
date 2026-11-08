package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.Monsterization;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.ThrownHolyWater;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.entity.order.OrderSister;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The Order (entity/order): its sisters' faith and fall, who they hunt, purging the realm, patrols and crusades on a hearth. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class OrderTests {
    private static OrderSister sister(GameTestHelper helper, EntityType<OrderSister> type, BlockPos pos) {
        OrderSister s = helper.spawn(type, pos);
        s.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(s.blockPosition()), MobSpawnType.EVENT, null);
        return s;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "realmSilverHumblesASister")
    public static void realmSilverHumblesASister(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(4, 2, 4));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.REALM_SILVER_SWORD.get()));
        float health = knight.getHealth();
        p.attack(knight);
        check(knight.getHealth() == health && knight.faith() == OrderSister.Rank.KNIGHT.faith - 1, "the blade cuts her faith, not her flesh: " + knight.getHealth() + " / " + knight.faith());
        Order.setNotoriety(p, Order.MARKED);
        check(Order.hunts(knight, p), "standing, she fights a marked player");
        for (int i = 0; i < 10; i++) OrderSister.shake(level, knight, p);
        check(knight.kneeling() && knight.faith() == 0, "with no faith left she kneels");
        check(knight.getMainHandItem().isEmpty(), "and lays down her sword");
        check(!Order.hunts(knight, p), "and fights no more");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "nobodyStrikesAKneelingSister")
    public static void nobodyStrikesAKneelingSister(GameTestHelper helper) {
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(4, 2, 4));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 6));
        wolf.setTarget(knight);
        check(wolf.getTarget() == knight, "standing, she can be fought");
        knight.kneel(null);
        check(wolf.getTarget() == null, "once she kneels the fight is over");
        wolf.setTarget(knight);
        check(wolf.getTarget() == null, "and nobody picks her again");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "heartfruitMakesHerFall")
    public static void heartfruitMakesHerFall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OrderSister captain = sister(helper, ModEntities.ORDER_CAPTAIN.get(), new BlockPos(4, 2, 4));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HEARTFRUIT.get()));
        captain.interact(p, InteractionHand.MAIN_HAND);
        check(!captain.isRemoved(), "standing, she won't take it");
        captain.kneel(p);
        captain.interact(p, InteractionHand.MAIN_HAND);
        List<AbstractMonsterGirl> fallen = level.getEntitiesOfClass(AbstractMonsterGirl.class, captain.getBoundingBox().inflate(2));
        check(captain.isRemoved() && fallen.size() == 1, "she falls to the realm");
        AbstractMonsterGirl girl = fallen.get(0);
        check(girl.getSpecies() == GirlSpecies.DEMON_DEVIL, "a captain becomes a demon: " + girl.getSpecies());
        check(girl.getAffection(p) >= OrderSister.FALLEN_AFFECTION && Monsterization.monsterized(girl), "devoted to who fed her, and the realm's own");
        check(p.getMainHandItem().isEmpty(), "the heartfruit is eaten");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "anEmptyHandSparesHer")
    public static void anEmptyHandSparesHer(GameTestHelper helper) {
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(4, 2, 4));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        Order.setNotoriety(p, 50);
        knight.kneel(p);
        knight.interact(p, InteractionHand.MAIN_HAND);
        check(knight.leaving() && !knight.kneeling() && !knight.standing(), "spared, she goes home");
        check(Order.notoriety(p) == 50 - Order.MERCY, "the Order thinks better of mercy: " + Order.notoriety(p));
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sistersHuntTheRealmsOwn")
    public static void sistersHuntTheRealmsOwn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(2, 2, 2));
        AbstractMonsterGirl succubus = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 2, 6));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(8, 2, 8));
        Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(10, 2, 10));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 2, 12));
        Order.setNotoriety(p, 0);
        check(Order.hunts(knight, succubus), "a devil is hunted");
        check(!Order.hunts(knight, wolf), "an ordinary wild girl is left alone");
        check(!Order.hunts(knight, p), "an unknown player is left alone");
        Monsterization.possess(level, villager);
        check(!Order.hunts(knight, villager), "the possessed are freed, not fought");
        wolf.addAffection(p, Orders.AFFECTION);
        wolf.teleportTo(p.getX() + 1, p.getY(), p.getZ());
        Order.setNotoriety(p, Order.MARKED);
        check(Order.hunts(knight, p) && Order.hunts(knight, wolf), "a marked player and the friend at their side are fought");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aTouchFreesThePossessed")
    public static void aTouchFreesThePossessed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(4, 2, 4));
        Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(5, 2, 4));
        Monsterization.possess(level, villager);
        float health = villager.getHealth();
        check(knight.doHurtTarget(villager), "the blow lands");
        check(!Monsterization.possessed(villager) && villager.getHealth() == health, "it frees her, unhurt");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "inquisitrixBlessesTheGround")
    public static void inquisitrixBlessesTheGround(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(7, 1, 7)), aside = at.offset(3, 0, 0);
        RealmGrassBlock.corrupt(level, at);
        RealmGrassBlock.corrupt(level, aside);
        check(level.getBlockState(at).is(ModBlocks.REALM_GRASS.get()) && level.getBlockState(aside).is(ModBlocks.REALM_GRASS.get()), "realm ground to bless");
        OrderSister knight = sister(helper, ModEntities.ORDER_KNIGHT.get(), new BlockPos(7, 2, 7));
        check(knight.bless(level) >= 1 && level.getBlockState(at).is(Blocks.GRASS_BLOCK), "a knight blesses where she stands");
        check(level.getBlockState(aside).is(ModBlocks.REALM_GRASS.get()), "and only there");
        knight.discard();
        OrderSister inquisitrix = sister(helper, ModEntities.ORDER_INQUISITOR.get(), new BlockPos(7, 2, 7));
        check(inquisitrix.bless(level) >= 1 && level.getBlockState(aside).is(Blocks.GRASS_BLOCK), "an inquisitrix blesses all round her");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "flaskSearsGirlsNotPlayers")
    public static void flaskSearsGirlsNotPlayers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        OrderSister inquisitrix = sister(helper, ModEntities.ORDER_INQUISITOR.get(), new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(8, 2, 8));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 8));
        float gh = girl.getHealth(), ph = p.getHealth();
        int seared = ThrownHolyWater.sear(level, girl.blockPosition(), inquisitrix, inquisitrix);
        check(seared == 1 && girl.getHealth() < gh, "the girl is seared");
        check(p.getHealth() == ph, "the player only gets wet");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "realmTiesReachTheOrder")
    public static void realmTiesReachTheOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        check(Order.ties(level, p) == 0, "no ties yet");
        AbstractMonsterGirl succubus = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 2, 6));
        succubus.addAffection(p, Orders.AFFECTION);
        RealmGrassBlock.corrupt(level, p.blockPosition().below());
        check(Order.ties(level, p) == 2, "in the realm's air with a devil friend: " + Order.ties(level, p));
        Order.setNotoriety(p, 0);
        Order.addNotoriety(p, Order.MARKED);
        check(Order.notoriety(p) == Order.MARKED, "notoriety adds up");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "captainBearsTheBanner")
    public static void captainBearsTheBanner(GameTestHelper helper) {
        OrderSister captain = sister(helper, ModEntities.ORDER_CAPTAIN.get(), new BlockPos(4, 2, 4));
        ItemStack head = captain.getItemBySlot(EquipmentSlot.HEAD);
        check(head.is(Items.WHITE_BANNER) && head.has(DataComponents.BANNER_PATTERNS), "the Order's banner on her helm");
        check(captain.getMainHandItem().is(Items.IRON_SWORD) && captain.getOffhandItem().is(Items.SHIELD), "sword and shield");
        OrderSister inquisitrix = sister(helper, ModEntities.ORDER_INQUISITOR.get(), new BlockPos(6, 2, 6));
        check(inquisitrix.getMainHandItem().is(ModItems.SPLASH_HOLY_WATER.get()), "an inquisitrix carries her flasks");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aPatrolMarchesOnTheRealm")
    public static void aPatrolMarchesOnTheRealm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(7, 2, 7));
        List<OrderSister> band = Order.patrol(level, target, target, 2, 5);
        check(band.size() == 3, "two knights and an inquisitrix: " + band.size());
        check(band.stream().filter(s -> s.rank() == OrderSister.Rank.INQUISITOR).count() == 1, "one inquisitrix");
        check(band.stream().allMatch(s -> target.equals(s.march())), "all marching on the realm");
        check(Order.band(0).size() == 4 && Order.band(9).size() == 7 && Order.band(30).size() == 7, "a crusade grows with the household, up to seven");
        band.forEach(OrderSister::discard);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aCrusadeOnTheHearth")
    public static void aCrusadeOnTheHearth(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 2));
        girl.addAffection(p, Orders.AFFECTION);
        check(Settlement.settle(level, girl, hearth), "she lives here");
        helper.runAfterDelay(2, () -> {                                // the hearth counts itself in once loaded
            BlockPos abs = helper.absolutePos(hp);
            check(abs.equals(Order.ownHearth(level, p, Order.HEARTH_RANGE)), "the player's hearth is found");
            Order.setNotoriety(p, Order.RAID);
            List<OrderSister> band = Order.startRaid(level, abs, p, 3, 6);
            check(band.size() == 4, "a captain and her band: " + band.size());
            check(Order.notoriety(p) == 0, "they have come; notoriety starts again");
            check(band.stream().allMatch(s -> Order.hunts(s, p) && Order.hunts(s, girl)), "they fight the owner and the household");
            Order.Raid raid = Order.raids(level).all().stream().filter(r -> r.hearth().equals(abs)).findFirst().orElseThrow();
            int before = girl.getAffection(p);
            band.get(0).kneel(p);                                       // one kneels, the rest fall in battle
            for (int i = 1; i < band.size(); i++) band.get(i).discard();
            Order.tickRaids(level);
            check(Order.raids(level).all().stream().noneMatch(r -> r.id() == raid.id()), "the crusade is broken");
            check(girl.getAffection(p) == before + Order.VICTORY_AFFECTION, "the household is the fonder of its defender");
            band.get(0).discard();
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "dawnEndsACrusade")
    public static void dawnEndsACrusade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = helper.absolutePos(new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Order.Raid raid = new Order.Raid(-7, hp, p.getUUID(), 4, level.getGameTime());
        Order.end(level, raid, false);
        check(Order.notoriety(p) == Order.RAID / 2, "they withdraw, but the Order remembers: " + Order.notoriety(p));
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aChapelOfTheOrder")
    public static void aChapelOfTheOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos font = com.linkyoshi90.monstergirls.world.OrderChapelStructure.build(level, origin, level.random, true);
        check(level.getBlockState(font).is(ModBlocks.HOLY_FONT.get()), "a holy font at the altar");
        check(level.getBlockState(font.offset(2, 0, 0)).is(Blocks.CHEST), "the Order's stores beside it");
        check(level.getBlockEntity(font.above(2)) instanceof net.minecraft.world.level.block.entity.BannerBlockEntity b && !b.getPatterns().layers().isEmpty(),
                "the Order's banner behind it");
        check(level.getBlockState(origin.offset(0, 1, -4)).isAir() && level.getBlockState(origin.offset(0, 3, -5)).is(Blocks.BELL), "a door, and a bell over it");
        List<OrderSister> keepers = level.getEntitiesOfClass(OrderSister.class, new net.minecraft.world.phys.AABB(origin).inflate(8));
        check(keepers.size() == 1 && font.equals(keepers.get(0).chapel()) && keepers.get(0).hasRestriction(), "one sister keeps it, and stays near");
        keepers.forEach(OrderSister::discard);
        helper.succeed();
    }
}
