package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kinship;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Kinship: girls with vanilla counterparts calm their kin and introduce them to the player (entity/girl/Kinship.java). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class KinshipTests {

    /** A real right-click, so the interaction event fires as it does in game. */
    private static void feed(ServerPlayer player, Entity target, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.interactOn(target, InteractionHand.MAIN_HAND);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ocelotNekomataIntroducesOcelots")
    public static void ocelotNekomataIntroducesOcelots(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6));
        Nekomata girl = spawn(helper, GirlSpecies.OCELOT_NEKOMATA, new BlockPos(7, 2, 7));
        Ocelot ocelot = helper.spawn(EntityType.OCELOT, new BlockPos(8, 2, 7));
        check(!Kinship.ignores(ocelot, player), "a wild cat sees you as prey, and so does her kin");
        feed(player, girl, new ItemStack(Items.CHICKEN));     // the first bite calms her
        check(girl.isCalm() && Kinship.ignores(ocelot, player), "calmed, her ocelot stops minding you");
        feed(player, girl, new ItemStack(Items.CHICKEN));
        feed(player, ocelot, new ItemStack(Items.COD));
        check(player.getUUID().equals(Kinship.friendOf(ocelot)), "fed together, she introduces you to the ocelot");
        check(ocelot.isPersistenceRequired(), "and it stays");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "zombieGirlIntroducesZombies")
    public static void zombieGirlIntroducesZombies(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Undead girl = spawn(helper, GirlSpecies.ZOMBIE_UNDEAD, new BlockPos(7, 2, 7));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 7));
        feed(player, girl, new ItemStack(Items.ROTTEN_FLESH)); // calms her
        zombie.setTarget(player);
        check(zombie.getTarget() != player, "her zombie will not go for a player she has made peace with");
        feed(player, girl, new ItemStack(Items.ROTTEN_FLESH));
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH, 2);
        feed(player, zombie, flesh);
        check(flesh.getCount() == 1, "the zombie eats it");
        check(player.getUUID().equals(Kinship.friendOf(zombie)) && zombie.isPersistenceRequired(), "and is yours now");
        girl.discard();
        zombie.setTarget(player);
        check(zombie.getTarget() == null, "even without her around it never targets a player");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "noKinNoIntroduction")
    public static void noKinNoIntroduction(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        check(Kinship.kin(GirlSpecies.PYTHON_LAMIA).isEmpty() && Kinship.kin(GirlSpecies.CATTLE_MINOTAUR).contains(EntityType.COW),
                "lamias have no vanilla kin, minotaurs have cows");
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 7));
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH, 2);
        feed(player, zombie, flesh);
        check(flesh.getCount() == 2 && Kinship.friendOf(zombie) == null, "without a zombie girl nearby, a zombie is just a zombie");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "kinWithoutPathsStillFollow")
    public static void kinWithoutPathsStillFollow(GameTestHelper helper) {
        var girl = spawn(helper, GirlSpecies.HONEY_SLIME, new BlockPos(2, 2, 2));
        var slime = helper.spawn(EntityType.SLIME, new BlockPos(12, 2, 12));
        slime.setSize(1, true);
        check(Kinship.unpathedLeader(slime) == girl, "a slime's girl leads it");
        check(Kinship.follow(slime, girl), "it turns and hops after her");
        var phantom = helper.spawn(EntityType.PHANTOM, new BlockPos(10, 6, 10));
        check(Kinship.follow(phantom, girl) && phantom.anchorPoint.equals(girl.blockPosition().above(12)), "a phantom circles over her");
        slime.discard();
        phantom.discard();
        helper.succeed();
    }
}
