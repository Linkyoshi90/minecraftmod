package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.HarpyCommands;
import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Fainting;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.Travelling;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Group orders by whistle, travelling together, fainting, /monstergirls friends, her orders in Jade. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class QualityOfLifeTests {

    private static void sneakWhistle(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.WHISTLE.get()));
        player.setShiftKeyDown(true);
        player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sneakWhistleOrdersEveryone")
    public static void sneakWhistleOrdersEveryone(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-whistler");
        AbstractMonsterGirl a = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        AbstractMonsterGirl b = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 4));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(12, 2, 12));
        a.setAffection(player.getUUID(), Orders.AFFECTION);
        b.setAffection(player.getUUID(), Orders.AFFECTION);
        sneakWhistle(player);
        check(a.isOrderedToSit() && b.isOrderedToSit(), "both friends stay put");
        check(!stranger.isOrderedToSit(), "a stranger takes no orders");
        sneakWhistle(player);
        check(Orders.current(a) == Orders.Command.FOLLOW && Orders.current(b) == Orders.Command.FOLLOW, "then both follow: " + Orders.current(a) + " " + Orders.current(b));
        check(player.getUUID().equals(a.getOrders().commanderId()), "following this player");
        b.setOrderedToSit(true);                                     // one sits, one follows: the next whistle makes everyone stay
        sneakWhistle(player);
        check(a.isOrderedToSit() && b.isOrderedToSit(), "a mixed group is told to stay first");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "travelPartyIsHerFollowers")
    public static void travelPartyIsHerFollowers(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-traveller");
        ServerPlayer other = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3), "qol-other");
        AbstractMonsterGirl follower = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        AbstractMonsterGirl sitter = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(8, 2, 4));
        AbstractMonsterGirl theirs = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(4, 2, 8));
        for (AbstractMonsterGirl g : List.of(follower, sitter, theirs)) {
            g.setAffection(player.getUUID(), Orders.AFFECTION);
            g.setAffection(other.getUUID(), Orders.AFFECTION);
        }
        follower.getOrders().set(Orders.Command.FOLLOW, player, null);
        theirs.getOrders().set(Orders.Command.FOLLOW, other, null);
        sitter.getOrders().set(Orders.Command.SIT, player, null);
        sitter.setOrderedToSit(true);
        List<Mob> party = Travelling.party(helper.getLevel(), player);
        check(party.size() == 1 && party.get(0) == follower, "only the one following this player comes along: " + party);
        HarpyTestUtil.remove(player);
        HarpyTestUtil.remove(other);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "friendFaintsInsteadOfDying")
    public static void friendFaintsInsteadOfDying(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-fainting");
        MonsterGirlsConfig.FRIENDS_FAINT.set(true);
        try {
            Rodent friend = spawn(helper, GirlSpecies.CAPYBARA_RODENT, new BlockPos(6, 2, 6));
            AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10));
            friend.setAffection(player.getUUID(), Orders.AFFECTION);
            friend.setOnGround(true);
            friend.hurt(friend.damageSources().generic(), 10000);
            check(friend.isAlive() && friend.getHealth() > 0, "she faints instead of dying");
            check(friend.isOrderedToSit(), "and sits until she is helped up");
            check(Fainting.recovering(friend), "she is recovering");
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, friend.getBoundingBox().inflate(4)).isEmpty(), "a fainted rodent keeps her things");
            stranger.hurt(stranger.damageSources().generic(), 10000);
            check(!stranger.isAlive(), "a wild one with no friends still dies");
            friend.removeAllEffects();
            friend.invulnerableTime = 0;                              // past the hurt cooldown of the first blow
            friend.hurt(friend.damageSources().generic(), 10000);
            check(!friend.isAlive(), "she can't faint twice in a minute");
        } finally {
            MonsterGirlsConfig.FRIENDS_FAINT.set(false);
            HarpyTestUtil.remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "friendsCommandListsEveryFriend")
    public static void friendsCommandListsEveryFriend(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-lister");
        AbstractMonsterGirl a = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        AbstractMonsterGirl b = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10));
        spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(12, 2, 4));     // a stranger: not listed
        a.setAffection(player.getUUID(), Orders.AFFECTION);
        b.setAffection(player.getUUID(), Orders.AFFECTION);
        b.setOrderedToSit(true);
        List<net.minecraft.network.chat.Component> lines = HarpyCommands.friendLines(player);
        check(lines.size() == 2, "both friends and nobody else: " + lines.size());
        check(lines.stream().anyMatch(l -> l.getString().contains("Sitting") || l.getString().contains("order.sit")), "her orders are shown: " + lines);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ownShotsSpareFriends")
    public static void ownShotsSpareFriends(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-archer");
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(helper.getLevel(), player,
                new ItemStack(net.minecraft.world.item.Items.ARROW), null);
        check(net.neoforged.neoforge.event.EventHooks.onProjectileImpact(arrow, new net.minecraft.world.phys.EntityHitResult(friend)), "her arrow flies past a friend");
        check(!net.neoforged.neoforge.event.EventHooks.onProjectileImpact(arrow, new net.minecraft.world.phys.EntityHitResult(stranger)), "but hits a stranger");
        float health = friend.getHealth();
        friend.hurt(friend.damageSources().explosion(null, player), 10);
        check(friend.getHealth() == health, "her blast spares a friend");
        stranger.hurt(stranger.damageSources().explosion(null, player), 10);
        check(stranger.getHealth() < stranger.getMaxHealth(), "but not a stranger");
        arrow.discard();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "compassLightsUpFriends")
    public static void compassLightsUpFriends(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-seeker");
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(10, 2, 10));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FRIEND_COMPASS.get()));
        player.setShiftKeyDown(true);
        player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        check(friend.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "a friend glows");
        check(!stranger.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "a stranger does not");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "followerWaitsWhileAway")
    public static void followerWaitsWhileAway(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-leaver");
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        friend.getOrders().set(Orders.Command.FOLLOW, player, null);
        check(friend.getOrders().centre(friend).distanceTo(player.position()) < 0.01, "she keeps near her player");
        HarpyTestUtil.remove(player);
        net.minecraft.world.phys.Vec3 wait = friend.getOrders().centre(friend);
        check(wait != null && wait.distanceTo(friend.position()) < 1.5, "with her player gone she waits where she is: " + wait);
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "followerTakesTheBoatSeat")
    public static void followerTakesTheBoatSeat(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5), "qol-boater");
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.BAKENEKO_NEKOMATA, new BlockPos(7, 2, 7));
        AbstractMonsterGirl broad = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(3, 2, 3));
        for (AbstractMonsterGirl g : List.of(friend, broad)) {
            g.setAffection(player.getUUID(), Orders.AFFECTION);
            g.getOrders().set(Orders.Command.FOLLOW, player, null);
        }
        net.minecraft.world.phys.Vec3 at = helper.absoluteVec(new net.minecraft.world.phys.Vec3(5.5, 2, 5.5));
        net.minecraft.world.entity.vehicle.Boat boat = new net.minecraft.world.entity.vehicle.Boat(helper.getLevel(), at.x, at.y, at.z);
        helper.getLevel().addFreshEntity(boat);
        player.startRiding(boat, true);
        com.linkyoshi90.monstergirls.entity.BoatBuddies.tick(player);
        check(friend.getVehicle() == boat, "she takes the free seat");
        check(broad.getVehicle() == null, "a centaur is too broad for a boat");
        player.stopRiding();
        com.linkyoshi90.monstergirls.entity.BoatBuddies.tick(player);
        check(friend.getVehicle() == null, "and gets out when her player does");
        boat.discard();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "badlyHurtFriendIsHeard")
    public static void badlyHurtFriendIsHeard(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-listener");
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        float max = friend.getMaxHealth();
        friend.setHealth(max * 0.2F);
        check(com.linkyoshi90.monstergirls.entity.HurtAlert.warn(friend, max * 0.5F).contains(player), "her friend hears she is badly hurt");
        check(com.linkyoshi90.monstergirls.entity.HurtAlert.warn(friend, max * 0.25F).isEmpty(), "only once, as she crosses the line");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "milestonesAreAnnounced")
    public static void milestonesAreAnnounced(GameTestHelper helper) {
        check(com.linkyoshi90.monstergirls.entity.Milestones.crossed(9, 10) == 0, "friend at 10");
        check(com.linkyoshi90.monstergirls.entity.Milestones.crossed(20, 30) == 1, "close at 25");
        check(com.linkyoshi90.monstergirls.entity.Milestones.crossed(0, 100) == 3, "a big jump names the highest");
        check(com.linkyoshi90.monstergirls.entity.Milestones.crossed(30, 40) == -1, "nothing between milestones");
        check(com.linkyoshi90.monstergirls.entity.Milestones.crossed(30, 5) == -1, "losses say nothing");
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2), "qol-befriender");
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(6, 2, 6));
        girl.addAffection(player, Orders.AFFECTION);                       // must not throw on the way through every family's addAffection
        check(girl.getAffection(player) == Orders.AFFECTION, "affection still adds up");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
