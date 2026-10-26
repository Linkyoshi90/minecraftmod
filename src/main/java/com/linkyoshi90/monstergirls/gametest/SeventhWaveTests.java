package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.Painted;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Armoured;
import com.linkyoshi90.monstergirls.entity.girl.Construct;
import com.linkyoshi90.monstergirls.entity.girl.Crustacean;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kinship;
import com.linkyoshi90.monstergirls.entity.girl.Marsupial;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Mollusk;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The seventh Harmontide wave (HARMONTIDE_CONCEPTS.md): crabs, kangaroo, armadillo, golem, jellyfish, ahuizotl, and the new paints. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SeventhWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "crabsPinchAndHermitsSwapShells")
    public static void crabsPinchAndHermitsSwapShells(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Crustacean crab = spawn(helper, GirlSpecies.CRAB_CRUSTACEAN, new BlockPos(5, 2, 5));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        zombie.setNoAi(true);
        crab.doHurtTarget(zombie);
        check(crab.getAction() == AbstractMonsterGirl.Action.PINCH && zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "her pinch slows what she grabs");
        Crustacean hermit = spawn(helper, GirlSpecies.HERMIT_CRAB_CRUSTACEAN, new BlockPos(8, 2, 8));
        hermit.setVariant("whelk");
        ItemStack nautilus = new ItemStack(Items.NAUTILUS_SHELL);
        hermit.moveInto(player, nautilus, "nautilus");
        check("nautilus".equals(hermit.getVariantName()) && nautilus.isEmpty(), "she moves into the nautilus shell");
        hermit.moveInto(player, new ItemStack(Items.SHULKER_SHELL), "shulker");
        check("shulker".equals(hermit.getVariantName()) && player.getInventory().contains(new ItemStack(Items.NAUTILUS_SHELL)), "and hands back the old one");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "kangarooCarriesHerJoeyAndKicks")
    public static void kangarooCarriesHerJoeyAndKicks(GameTestHelper helper) {
        Marsupial mom = spawn(helper, GirlSpecies.KANGAROO_MARSUPIAL, new BlockPos(5, 2, 5));
        mom.setNoAi(true);
        AbstractMonsterGirl joey = mom.spawnYoung(helper.getLevel(), mom.blockPosition(), null);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        zombie.setNoAi(true);
        mom.doHurtTarget(zombie);
        check(mom.getAction() == AbstractMonsterGirl.Action.KICK, "she kicks");
        helper.succeedWhen(() -> check(joey != null && joey.getVehicle() == mom, "her joey rides in her pouch"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "armadilloRollsUp")
    public static void armadilloRollsUp(GameTestHelper helper) {
        Armoured dillo = spawn(helper, GirlSpecies.ARMADILLO_ARMOURED, new BlockPos(5, 2, 5));
        dillo.curlUp();
        float health = dillo.getHealth();
        check(dillo.isCurled() && !dillo.hurt(helper.getLevel().damageSources().generic(), 6.0F) && dillo.getHealth() == health, "rolled up, nothing gets through");
        check(Kinship.kin(GirlSpecies.ARMADILLO_ARMOURED).contains(EntityType.ARMADILLO), "vanilla armadillos are her kin");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "golemOutlastsAnIronGolemAndBreaks")
    public static void golemOutlastsAnIronGolemAndBreaks(GameTestHelper helper) {
        Construct golem = spawn(helper, GirlSpecies.GOLEM_CONSTRUCT, new BlockPos(5, 2, 5));
        golem.setNoAi(true);
        check(golem.getMaxHealth() > EntityType.IRON_GOLEM.create(helper.getLevel()).getMaxHealth(), "more health than an iron golem");
        check(Kinship.kin(GirlSpecies.GOLEM_CONSTRUCT).contains(EntityType.IRON_GOLEM) && !Kinship.kin(GirlSpecies.FRANKENSTEIN_UNDEAD).contains(EntityType.IRON_GOLEM),
                "iron golems are her kin now");
        check(!golem.canShareBed() && golem.hasDeathAnimation(), "made, not born; she breaks apart when she falls");
        golem.kill();
        helper.succeedWhen(() -> check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, golem.getBoundingBox().inflate(3), e -> e.getItem().is(Items.CLAY_BALL)).isEmpty(),
                "and leaves clay behind"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "jellyfishStingsStrangers")
    public static void jellyfishStingsStrangers(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Mollusk jelly = spawn(helper, GirlSpecies.JELLYFISH_MOLLUSK, new BlockPos(5, 2, 5));
        jelly.setNoAi(true);
        jelly.setAffection(friend.getUUID(), Orders.AFFECTION);
        var pig = helper.spawn(EntityType.PIG, new BlockPos(5, 2, 5));          // (not a zombie: the undead shrug off poison)
        pig.setNoAi(true);
        check(jelly.sting(helper.getLevel()) == 1 && pig.hasEffect(MobEffects.POISON), "she stings what bumps into her");
        check(!friend.hasEffect(MobEffects.POISON), "but never a friend");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ahuizotlStealsTheCatch")
    public static void ahuizotlStealsTheCatch(GameTestHelper helper) {
        ServerPlayer fisher = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Merfolk thief = spawn(helper, GirlSpecies.AHUIZOTL_MERMAID, new BlockPos(7, 2, 7));
        thief.setNoAi(true);
        check(Merfolk.thiefNear(fisher, fisher) == thief, "a wild ahuizotl near the hook steals the catch");
        fisher.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.LANTERN));
        check(Merfolk.thiefNear(fisher, fisher) == null, "not with a lantern in hand");
        fisher.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        thief.setAffection(fisher.getUUID(), Orders.AFFECTION);
        check(Merfolk.thiefNear(fisher, fisher) == null, "and never from a friend");
        HarpyTestUtil.remove(fisher);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "copperPaintWeathersAndWaxes")
    public static void copperPaintWeathersAndWaxes(GameTestHelper helper) {
        RandomSource r = RandomSource.create(7);
        for (int i = 0; i < 400; i++) {
            String p = AbstractMonsterGirl.PAINTS.get(Painted.roll(r) - 1);
            check(!p.startsWith("copper_"), "a new spawn is never painted an aged copper");
        }
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        girl.setPaint(AbstractMonsterGirl.PAINTS.indexOf("copper_exposed") + 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB));
        Painted.interact(girl, girl, player, InteractionHand.MAIN_HAND);
        check(girl.isWaxed(), "honeycomb waxes her copper");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
        Painted.interact(girl, girl, player, InteractionHand.MAIN_HAND);
        check(!girl.isWaxed() && "copper_exposed".equals(girl.getPaintName()), "an axe takes the wax off first");
        Painted.interact(girl, girl, player, InteractionHand.MAIN_HAND);
        check("copper".equals(girl.getPaintName()), "then scrapes back a stage");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
