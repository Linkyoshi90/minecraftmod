package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.SilkBridgeBlock;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.DuckHarpy;
import com.linkyoshi90.harmontide.entity.HummingbirdHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.RocHarpy;
import com.linkyoshi90.harmontide.entity.SparrowHarpy;
import com.linkyoshi90.harmontide.entity.Traversal;
import com.linkyoshi90.harmontide.entity.girl.Arachne;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Getting about together (entity/Traversal): shoulders, silk, a lift and a catch, tows, coils and bounces. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TraversalTests {

    private static Arachne spiderling(GameTestHelper helper, ServerPlayer p, BlockPos at) {
        Arachne s = spawn(helper, GirlSpecies.JUMPING_SPIDER_ARACHNE, at);
        s.setBaby(true);
        s.setAffection(p.getUUID(), Orders.AFFECTION);
        return s;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "spiderlingHopsOntoTheShoulderSheFollows")
    public static void spiderlingHopsOntoTheShoulderSheFollows(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Arachne s = spiderling(helper, p, new BlockPos(7, 2, 5));
        s.getOrders().set(Orders.Command.FOLLOW, p, null);
        helper.succeedWhen(() -> {
            check(s.getVehicle() == p, "up onto the shoulder of the player she follows");
            Vec3 off = s.position().subtract(p.position());
            check(off.y > 1.0 && off.y < 1.6, "at shoulder height, not on the head: " + off.y);
            double side = Math.sqrt(off.x * off.x + off.z * off.z);
            check(side > 0.25 && side < 0.55, "out on the shoulder: " + side);
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "longCrouchSetsTheSpiderlingDown")
    public static void longCrouchSetsTheSpiderlingDown(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Arachne s = spiderling(helper, p, new BlockPos(6, 2, 5));
        p.setShiftKeyDown(true);
        interact(s, p, ItemStack.EMPTY);                                     // a crouching hand: up she climbs
        check(s.getVehicle() == p, "a crouching empty hand lifts her up");
        helper.succeedWhen(() -> {
            check(!s.isPassenger(), "two seconds of crouching set her down");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spiderlingIsASafetyLine")
    public static void spiderlingIsASafetyLine(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        ServerPlayer control = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9), "control");
        Arachne s = spiderling(helper, p, new BlockPos(6, 2, 5));
        check(Traversal.perchOn(s, p), "perched");
        check(Traversal.spiderling(p) == s, "she is the spiderling aboard");
        check(!p.causeFallDamage(20.0F, 1.0F, helper.getLevel().damageSources().fall()), "no fall hurts with her line");
        check(control.causeFallDamage(20.0F, 1.0F, helper.getLevel().damageSources().fall()), "a fall hurts without one");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.COBWEB);
        p.stuckSpeedMultiplier = new Vec3(0.25, 0.05, 0.25);
        check(Traversal.partWebs(p), "the web lets go of them");
        check(p.stuckSpeedMultiplier.lengthSqr() < 1.0E-7, "not slowed any more");
        HarpyTestUtil.remove(p);
        HarpyTestUtil.remove(control);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "arachneSpinsASilkBridge")
    public static void arachneSpinsASilkBridge(GameTestHelper helper) {
        for (int x = 6; x <= 9; x++) for (int z = 3; z <= 9; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.AIR);   // a trench across the floor
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 6));
        p.setYRot(-90.0F);                                                  // facing east, over the trench
        Arachne a = spawn(helper, GirlSpecies.JUMPING_SPIDER_ARACHNE, new BlockPos(3, 2, 4));
        a.setAffection(p.getUUID(), Orders.AFFECTION);
        p.setShiftKeyDown(true);
        interact(a, p, new ItemStack(Items.STRING, 2));
        check(p.getMainHandItem().getCount() == 1, "she took a string");
        for (int x = 6; x <= 9; x++) {
            for (int z = 5; z <= 7; z++) check(helper.getBlockState(new BlockPos(x, 1, z)).is(ModBlocks.SILK_BRIDGE.get()), "silk at " + x + "," + z);
        }
        check(!helper.getBlockState(new BlockPos(10, 1, 6)).is(ModBlocks.SILK_BRIDGE.get()), "it stops at the far side");
        BlockPos abs = helper.absolutePos(new BlockPos(7, 1, 6));
        check(helper.getLevel().getBlockTicks().hasScheduledTick(abs, ModBlocks.SILK_BRIDGE.get()), "it will dry and blow away");
        check(a.spinBridge(helper.getLevel(), helper.absolutePos(new BlockPos(4, 2, 6)), Direction.NORTH) == 0, "no gap to the north: no bridge");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 160, batch = "arachneSwingsOnASilkLine")
    public static void arachneSwingsOnASilkLine(GameTestHelper helper) {
        helper.setBlock(new BlockPos(7, 7, 7), Blocks.STONE);
        Arachne a = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(3, 2, 7));
        a.setNoAi(false);
        Vec3 to = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(11, 2, 7)));
        BlockPos anchor = a.findAnchor(to);
        check(anchor != null && anchor.equals(helper.absolutePos(new BlockPos(7, 7, 7))), "the stone above the way is her anchor: " + anchor);
        double startX = a.getX();
        check(a.startSwing(to, anchor), "off she swings");
        check(a.getSwingAnchor().isPresent(), "the line shows");
        helper.succeedWhen(() -> {
            check(!a.isSwinging() && a.getSwingAnchor().isEmpty(), "the swing is over");
            check(a.getX() - startX > 3.0, "and it carried her a good way: " + (a.getX() - startX));
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "harpyLiftsABondedFriend")
    public static void harpyLiftsABondedFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        RocHarpy roc = helper.spawn(ModEntities.ROC_HARPY.get(), new BlockPos(6, 2, 5));
        check(roc.lift() == AbstractHarpy.Lift.STRONG, "a roc flies with someone");
        roc.setAffection(p.getUUID(), 24);
        p.setShiftKeyDown(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        roc.interact(p, InteractionHand.MAIN_HAND);
        check(!p.isPassenger(), "not before she trusts them a lot");
        roc.setAffection(p.getUUID(), 25);
        roc.interact(p, InteractionHand.MAIN_HAND);
        p.setShiftKeyDown(false);
        check(p.getVehicle() == roc && roc.carryMode() == AbstractHarpy.CARRY_FLIGHT, "up into the air, hanging from her");
        check(roc.liftLeft() == Traversal.STRONG_TICKS, "half a minute of flight");
        check(roc.getControllingPassenger() == p, "they steer");
        helper.succeedWhen(() -> {                                          // nobody flies her here: their feet touch the floor and she lets go
            check(!p.isPassenger() && roc.carryMode() == AbstractHarpy.CARRY_NONE, "set down");
            check(p.hasEffect(MobEffects.SLOW_FALLING), "gently");
            check(!roc.canCarry(), "then she rests her wings");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "smallHarpiesGlideOrCannotCarry")
    public static void smallHarpiesGlideOrCannotCarry(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(3, 2, 3));
        HummingbirdHarpy hummingbird = helper.spawn(ModEntities.HUMMINGBIRD_HARPY.get(), new BlockPos(9, 2, 9));
        check(sparrow.lift() == AbstractHarpy.Lift.GLIDE, "a sparrow can only glide someone down");
        check(hummingbird.lift() == AbstractHarpy.Lift.NONE, "a hummingbird is far too small");
        check(Traversal.liftTicks(AbstractHarpy.Lift.STRONG, 100) > Traversal.liftTicks(AbstractHarpy.Lift.STRONG, 25), "a devoted one flies longer");
        Vec3 glide = Traversal.flightStroke(new Vec3(0, 0.3, 0), HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5)), AbstractHarpy.Lift.GLIDE, 10);
        check(glide.y < 0, "past the take-off a glide only sinks");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "harpyCatchesAFallingFriend")
    public static void harpyCatchesAFallingFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 3, 5));
        com.linkyoshi90.harmontide.entity.SwanHarpy swan = helper.spawn(ModEntities.SWAN_HARPY.get(), new BlockPos(9, 2, 9));
        swan.setAffection(p.getUUID(), 25);
        p.fallDistance = 2.0F;
        check(Traversal.catchFalling(p) == null, "a little hop needs no catch");
        p.fallDistance = 9.0F;
        check(Traversal.catchFalling(p) == swan, "she swoops on a long fall");
        check(p.getVehicle() == swan && p.fallDistance == 0.0F, "caught, the fall forgotten");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    private static void pool(GameTestHelper helper) {
        for (int x = 2; x <= 10; x++) for (int z = 2; z <= 10; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "duckTowsASwimmer")
    public static void duckTowsASwimmer(GameTestHelper helper) {
        pool(helper);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 1, 5));
        DuckHarpy duck = helper.spawn(ModEntities.DUCK_HARPY.get(), new BlockPos(6, 1, 5));
        duck.setAffection(p.getUUID(), Orders.AFFECTION);
        helper.runAfterDelay(5, () -> {
            p.baseTick();                                                   // (a test player is not ticked: let it notice the water)
            check(duck.isInWater() && p.isInWater(), "both in the water");
            p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            duck.interact(p, InteractionHand.MAIN_HAND);
            check(p.getVehicle() == duck && duck.carryMode() == AbstractHarpy.CARRY_TOW, "they take hold and she tows");
            check(duck.getControllingPassenger() == p, "the swimmer steers");
            duck.teleportTo(helper.absolutePos(new BlockPos(12, 2, 12)).getX() + 0.5, helper.absolutePos(new BlockPos(12, 2, 12)).getY(), helper.absolutePos(new BlockPos(12, 2, 12)).getZ() + 0.5);
        });
        helper.runAfterDelay(30, () -> helper.succeedWhen(() -> {
            p.baseTick();                                                   // (it notices it is out of the water)
            check(!p.isPassenger() && duck.carryMode() == AbstractHarpy.CARRY_NONE, "ashore, she lets go");
            HarpyTestUtil.remove(p);
        }));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "duckFloatsAndPaddles")
    public static void duckFloatsAndPaddles(GameTestHelper helper) {
        pool(helper);
        DuckHarpy duck = helper.spawn(ModEntities.DUCK_HARPY.get(), new BlockPos(6, 1, 6));
        helper.succeedWhen(() -> {                                          // (she may fly off later: she is free to)
            check(duck.isInWater() && duck.tickCount > 20, "afloat a moment after landing on the pond");
            check(duck.getY() > helper.absolutePos(new BlockPos(6, 1, 6)).getY() + 0.3, "riding the surface, not lying on the bottom: " + duck.getY());
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "mermaidTowsADiver")
    public static void mermaidTowsADiver(GameTestHelper helper) {
        pool(helper);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 1, 5));
        Merfolk m = spawn(helper, GirlSpecies.DOLPHIN_MERMAID, new BlockPos(6, 1, 5));
        m.setAffection(p.getUUID(), Orders.AFFECTION);
        helper.runAfterDelay(5, () -> {
            p.baseTick();
            p.setAirSupply(10);
            interact(m, p, ItemStack.EMPTY);
            check(p.getVehicle() == m && m.towed() == p, "hand in hand");
            check(m.getControllingPassenger() == p, "the diver leads");
        });
        helper.runAfterDelay(10, () -> helper.succeedWhen(() -> {
            check(p.getAirSupply() == p.getMaxAirSupply(), "her breath is theirs");
            check(p.hasEffect(MobEffects.WATER_BREATHING), "while she holds on");
            HarpyTestUtil.remove(p);
        }));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lamiaCoilsAHurtFriend")
    public static void lamiaCoilsAHurtFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Lamia l = spawn(helper, GirlSpecies.PYTHON_LAMIA, new BlockPos(8, 2, 5));
        l.setAffection(p.getUUID(), 25);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 5));
        z.setTarget(p);
        check(Traversal.coil(p, z) == null, "a healthy friend needs no shield");
        p.setHealth(5.0F);
        check(Traversal.coil(p, z) == l, "badly hurt: she lunges");
        check(p.getVehicle() == l, "wrapped in her coils");
        check(p.hasEffect(MobEffects.DAMAGE_RESISTANCE) && p.hasEffect(MobEffects.REGENERATION), "shielded and mending");
        check(z.getTarget() == l, "the attacker turns on her");
        p.stopRiding();
        check(Traversal.coil(p, z) == null, "once a minute");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "slimeGirlBouncesAFall")
    public static void slimeGirlBouncesAFall(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        ServerPlayer crouched = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 6), "crouched");
        SlimeGirl s = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(5, 2, 5));
        s.setNoAi(true);
        p.setDeltaMovement(0, -1.0, 0);
        check(!p.causeFallDamage(9.0F, 1.0F, helper.getLevel().damageSources().fall()), "no harm landing on her");
        check(p.getDeltaMovement().y > 0.8, "and up they go: " + p.getDeltaMovement().y);
        crouched.setShiftKeyDown(true);
        crouched.setDeltaMovement(0, -1.0, 0);
        check(!crouched.causeFallDamage(9.0F, 1.0F, helper.getLevel().damageSources().fall()), "crouching: still no harm");
        check(crouched.getDeltaMovement().y < 0, "but they sink in");
        check(Traversal.bounceSpeed(100) <= 1.4, "never sky-high");
        HarpyTestUtil.remove(p);
        HarpyTestUtil.remove(crouched);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "silkBridgeIsThinAndWalkable")
    public static void silkBridgeIsThinAndWalkable(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 3, 5), ModBlocks.SILK_BRIDGE.get());
        var state = helper.getBlockState(new BlockPos(5, 3, 5));
        var shape = state.getCollisionShape(helper.getLevel(), helper.absolutePos(new BlockPos(5, 3, 5)));
        check(!shape.isEmpty() && shape.max(Direction.Axis.Y) <= 0.07, "a thin sheet you can stand on");
        check(SilkBridgeBlock.LASTS >= 1200, "it lasts minutes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "centaurPullsAMinecart")
    public static void centaurPullsAMinecart(GameTestHelper helper) {
        for (int x = 1; x <= 13; x++) helper.setBlock(new BlockPos(x, 2, 7), Blocks.RAIL);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 9));
        p.setYRot(-90.0F);                                                  // facing east, along the track
        var cart = helper.spawn(EntityType.MINECART, new BlockPos(3, 2, 7));
        check(p.startRiding(cart, true), "in the cart");
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 9));
        horse.setAffection(p.getUUID(), Orders.AFFECTION);
        check(!Traversal.pullCart(p), "not while she roams free");
        horse.getOrders().set(Orders.Command.FOLLOW, p, null);
        check(Traversal.pullCart(p), "following: she takes the front");
        check(cart.getDeltaMovement().horizontalDistance() > 0.35, "and the cart rolls");
        double start = cart.getX();
        helper.succeedWhen(() -> {
            check(cart.getX() - start > 3.0, "pulled along the track: " + (cart.getX() - start));
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "capybaraFerriesASwimmer")
    public static void capybaraFerriesASwimmer(GameTestHelper helper) {
        pool(helper);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 1, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl capy = spawn(helper, GirlSpecies.CAPYBARA_RODENT, new BlockPos(6, 1, 5));
        capy.setAffection(p.getUUID(), Orders.AFFECTION);
        check(capy.ferries(), "a capybara carries swimmers");
        helper.runAfterDelay(5, () -> {
            p.baseTick();
            interact(capy, p, ItemStack.EMPTY);
            check(p.getVehicle() == capy && capy.ferried() == p, "up onto her back");
            check(!capy.isInWater() || capy.getControllingPassenger() == p, "the swimmer steers in the water");
            capy.teleportTo(helper.absolutePos(new BlockPos(12, 2, 12)).getX() + 0.5, helper.absolutePos(new BlockPos(12, 2, 12)).getY(), helper.absolutePos(new BlockPos(12, 2, 12)).getZ() + 0.5);
        });
        helper.runAfterDelay(10, () -> helper.succeedWhen(() -> {
            check(!p.isPassenger(), "ashore, they slide off");
            HarpyTestUtil.remove(p);
        }));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "fairyRidesTheShoulderAndMends")
    public static void fairyRidesTheShoulderAndMends(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl fairy = spawn(helper, GirlSpecies.FAIRY_BUG, new BlockPos(7, 2, 5));
        fairy.setAffection(p.getUUID(), Orders.AFFECTION);
        check(Traversal.perches(fairy), "a fairy fits on a shoulder");
        check(!Traversal.perches(spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10))), "a centaur does not");
        fairy.getOrders().set(Orders.Command.FOLLOW, p, null);
        helper.succeedWhen(() -> {
            check(fairy.getVehicle() == p, "up she hops");
            check(p.hasEffect(MobEffects.REGENERATION), "and mends her friend a little");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "followersLendTheirGifts")
    public static void followersLendTheirGifts(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl bunny = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(7, 2, 7));
        for (var g : java.util.List.of(cat, bunny)) {
            g.setAffection(p.getUUID(), Orders.AFFECTION);
            g.getOrders().set(Orders.Command.FOLLOW, p, null);
        }
        check(Traversal.followers(p).size() == 2, "both follow");
        Traversal.company(p);
        check(p.hasEffect(MobEffects.JUMP), "a spring in their step");
        check(p.getPersistentData().getLong(Traversal.CAT_GRACE) > helper.getLevel().getGameTime(), "and a cat's landing");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hatchlingRidesLikeAParrot")
    public static void hatchlingRidesLikeAParrot(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        SparrowHarpy chick = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(6, 2, 5));
        chick.setBaby(true);
        chick.setAffection(p.getUUID(), Orders.AFFECTION);
        p.setShiftKeyDown(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        chick.interact(p, InteractionHand.MAIN_HAND);
        check(chick.getVehicle() == p, "up onto the shoulder");
        helper.succeedWhen(() -> {
            check(!chick.isPassenger(), "a long crouch sets her down");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "golemTossesAFriend")
    public static void golemTossesAFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl golem = spawn(helper, GirlSpecies.GOLEM_CONSTRUCT, new BlockPos(6, 2, 5));
        golem.setAffection(p.getUUID(), Orders.AFFECTION);
        p.setShiftKeyDown(true);
        interact(golem, p, ItemStack.EMPTY);
        check(p.getDeltaMovement().y > 1.0, "up they go");
        check(!p.causeFallDamage(10.0F, 1.0F, helper.getLevel().damageSources().fall()), "and land unhurt");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "octopusDivesWithASwimmer")
    public static void octopusDivesWithASwimmer(GameTestHelper helper) {
        pool(helper);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 1, 5));
        com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl octo = spawn(helper, GirlSpecies.OCTOPUS_MOLLUSK, new BlockPos(6, 1, 5));
        octo.setAffection(p.getUUID(), Orders.AFFECTION);
        check(octo.ferries() && Traversal.DIVERS.contains(octo.getSpecies()), "an octopus dives with a swimmer");
        check(p.startRiding(octo, true), "holding on");
        p.setAirSupply(3);
        helper.succeedWhen(() -> {
            check(p.getAirSupply() == p.getMaxAirSupply(), "her air is theirs");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tarantulaIsAWallClimbingMount")
    public static void tarantulaIsAWallClimbingMount(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Arachne t = spawn(helper, GirlSpecies.TARANTULA_ARACHNE, new BlockPos(6, 2, 5));
        t.setAffection(p.getUUID(), 25);
        check(t.isMount(), "a tarantula carries a rider");
        interact(t, p, new ItemStack(Items.SADDLE));
        interact(t, p, ItemStack.EMPTY);
        check(p.getVehicle() == t && t.getControllingPassenger() == p, "saddled, she carries them and they steer");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
