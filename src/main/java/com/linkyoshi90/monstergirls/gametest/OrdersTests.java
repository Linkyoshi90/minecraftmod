package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.OstrichHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.PhoenixHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Stick orders and stances (entity/Orders.java). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class OrdersTests {

    private static void stick(ServerPlayer player, AbstractMonsterGirl girl, boolean sneak) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        player.setShiftKeyDown(sneak);
        girl.interact(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stickCyclesOrders")
    public static void stickCyclesOrders(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10));
        stick(player, stranger, false);
        check(stranger.isOrderedToSit(), "anyone can make her sit");
        stick(player, stranger, false);
        check(stranger.getOrders().command(stranger) == Orders.Command.FREE_ROAM, "and a stranger's next click lets her go: " + stranger.getOrders().command(stranger));
        stick(player, stranger, true);
        check(stranger.getOrders().stance() == Orders.Stance.NATURAL, "a stranger cannot set her stance");

        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 5));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        Orders.Command[] expected = {Orders.Command.SIT, Orders.Command.PATROL, Orders.Command.FOLLOW, Orders.Command.FREE_ROAM};
        for (Orders.Command c : expected) {
            stick(player, friend, false);
            check(friend.getOrders().command(friend) == c, "expected " + c + ", got " + friend.getOrders().command(friend));
            if (c == Orders.Command.PATROL) check(friend.blockPosition().equals(friend.getOrders().anchor()), "she patrols where she was told");
        }
        Orders.Stance[] stances = {Orders.Stance.HOSTILE, Orders.Stance.NEUTRAL, Orders.Stance.SELF_DEFENCE, Orders.Stance.PEACEFUL, Orders.Stance.HOSTILE};
        for (Orders.Stance s : stances) {
            stick(player, friend, true);
            check(friend.getOrders().stance() == s, "expected " + s + ", got " + friend.getOrders().stance());
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "patrolPullsHerBack")
    public static void patrolPullsHerBack(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(2, 2, 2));
        girl.getOrders().set(Orders.Command.PATROL, player, girl.blockPosition());
        Vec3 far = helper.absoluteVec(new Vec3(13.5, 2, 13.5));
        girl.teleportTo(far.x, far.y, far.z);
        Vec3 anchor = Vec3.atBottomCenterOf(girl.getOrders().anchor());
        helper.succeedWhen(() -> {
            check(girl.distanceToSqr(anchor) < Orders.PATROL_RADIUS * Orders.PATROL_RADIUS, "she heads back to her patrol spot: " + Math.sqrt(girl.distanceToSqr(anchor)));
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stancesDecideWhomSheFights")
    public static void stancesDecideWhomSheFights(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(7, 2, 7));
        girl.setAffection(player.getUUID(), Orders.AFFECTION);
        Zombie biter = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 7));
        Zombie stranger = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 12));
        Zombie mugger = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 2));
        girl.setLastHurtByMob(biter);
        player.setLastHurtByMob(mugger);
        Orders o = girl.getOrders();
        o.setStance(Orders.Stance.PEACEFUL, player);
        check(!o.allows(girl, biter) && !o.allows(girl, mugger), "peaceful: she fights no one");
        o.setStance(Orders.Stance.SELF_DEFENCE, player);
        check(o.allows(girl, biter) && !o.allows(girl, mugger) && !o.allows(girl, stranger), "self-defence: only who hit her");
        o.setStance(Orders.Stance.NEUTRAL, player);
        check(o.allows(girl, biter) && o.allows(girl, mugger) && !o.allows(girl, stranger), "neutral: who hit her or you");
        o.setStance(Orders.Stance.HOSTILE, player);
        check(o.allows(girl, stranger), "hostile: any hostile mob");
        check(!o.allows(girl, player), "never her own player");
        o.setStance(Orders.Stance.PEACEFUL, player);
        girl.setTarget(biter);
        check(girl.getTarget() == null, "and the stance has the last word on her target");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "companionsNeverFightEachOther")
    public static void companionsNeverFightEachOther(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(5, 2, 5));
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(8, 2, 8));
        phoenix.setAffection(player.getUUID(), Orders.AFFECTION);
        ostrich.setAffection(player.getUUID(), Orders.AFFECTION);
        float hp = ostrich.getHealth();
        ostrich.hurt(helper.getLevel().damageSources().mobAttack(phoenix), 4.0F);
        check(ostrich.getHealth() == hp, "the phoenix's fire never hurts the ostrich");
        ostrich.setTarget(phoenix);
        check(ostrich.getTarget() == null, "and the ostrich never turns on her");
        helper.runAfterDelay(70, () -> {                      // past the player's spawn protection
            float php = player.getHealth();
            player.hurt(helper.getLevel().damageSources().mobAttack(ostrich), 1.0F);
            check(player.getHealth() < php, "a deliberate (clumsy) kick at her player still lands");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }
}
