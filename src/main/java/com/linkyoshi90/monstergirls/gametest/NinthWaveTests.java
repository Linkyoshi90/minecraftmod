package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.BatGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.Flora;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Jackalope;
import com.linkyoshi90.monstergirls.entity.girl.Kinship;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.SlimeGirl;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The ninth Harmontide wave (HARMONTIDE_CONCEPTS.md): sheep, highland, yak, bat, dark elf, medusa, oni, mantis, ender slime, wolpertinger, venus flytrap, siren. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class NinthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sheepShearsDyesAndRegrows")
    public static void sheepShearsDyesAndRegrows(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Minotaur sheep = spawn(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(5, 2, 5));
        sheep.setNoAi(true);
        check(Kinship.kin(GirlSpecies.SHEEP_MINOTAUR).contains(EntityType.SHEEP), "vanilla sheep are her flock");
        interact(sheep, p, new ItemStack(Items.RED_DYE));
        check(sheep.getWoolDye() == DyeColor.RED, "the dye takes: " + sheep.getWoolDye());
        interact(sheep, p, new ItemStack(Items.SHEARS));
        check(sheep.isShorn(), "shears take her wool");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, sheep.getBoundingBox().inflate(3), e -> e.getItem().is(Items.RED_WOOL)).isEmpty(),
                "red wool for a red sheep");
        sheep.ate();
        check(!sheep.isShorn(), "grazing grows it back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "highlandAndYakKeepOutTheCold")
    public static void highlandAndYakKeepOutTheCold(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Minotaur highland = spawn(helper, GirlSpecies.HIGHLAND_MINOTAUR, new BlockPos(3, 2, 5));
        Minotaur yak = spawn(helper, GirlSpecies.YAK_MINOTAUR, new BlockPos(6, 2, 5));
        Minotaur cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(8, 2, 8));
        yak.setNoAi(true);
        check(!highland.canFreeze() && !yak.canFreeze() && cow.canFreeze(), "the shaggy ones never freeze");
        interact(yak, p, new ItemStack(Items.CHEST));
        check(!yak.hasFlag(AbstractMonsterGirl.FLAG_CHEST), "a stranger's chest she will not carry");
        yak.addAffection(p, Orders.AFFECTION);
        interact(yak, p, new ItemStack(Items.CHEST));
        check(yak.hasFlag(AbstractMonsterGirl.FLAG_CHEST) && yak.getPack().getContainerSize() == 18, "a friend's chest goes on her back: two rows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "batRoostsAndPings")
    public static void batRoostsAndPings(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 7, 5), Blocks.STONE);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BatGirl bat = spawn(helper, GirlSpecies.CAVE_BAT, new BlockPos(5, 2, 5));
        bat.setNoAi(true);
        BlockPos ceiling = bat.findRoost();
        check(ceiling != null && ceiling.equals(helper.absolutePos(new BlockPos(5, 7, 5))), "she finds the ceiling above her: " + ceiling);
        bat.hangFrom(ceiling);
        check(bat.isRoosting() && Math.abs(bat.getY() + bat.getBbHeight() - ceiling.getY()) < 0.01, "she hangs with her feet against it");
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 8));
        zombie.setNoAi(true);
        bat.addAffection(friend, Orders.AFFECTION);
        check(bat.ping(helper.getLevel(), friend) >= 1 && zombie.hasEffect(MobEffects.GLOWING), "her ping lights up the monster near her friend");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "darkElfIsSilentAndCalmsTheWarden")
    public static void darkElfIsSilentAndCalmsTheWarden(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        Elf elf = spawn(helper, GirlSpecies.DARK_ELF, new BlockPos(5, 2, 5));
        elf.setNoAi(true);
        check(elf.dampensVibrations(), "sculk never hears her");
        elf.addAffection(friend, Orders.AFFECTION);
        Warden warden = helper.spawn(EntityType.WARDEN, new BlockPos(7, 2, 5));
        warden.setNoAi(true);
        warden.increaseAngerAt(friend, 80, false);
        elf.calmWardens(helper.getLevel());
        check(warden.getAngerManagement().getActiveAnger(friend) == 0, "the Warden forgets its anger at her friend");
        check(Kinship.ignores(warden, friend) && !Kinship.ignores(warden, stranger), "it spares her friend, not a stranger");
        HarpyTestUtil.remove(friend);
        HarpyTestUtil.remove(stranger);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "medusaStonesStrangersNotFriends")
    public static void medusaStonesStrangersNotFriends(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 1));
        Lamia medusa = spawn(helper, GirlSpecies.MEDUSA_LAMIA, new BlockPos(5, 2, 6));
        medusa.setNoAi(true);
        p.lookAt(EntityAnchorArgument.Anchor.EYES, medusa.getEyePosition());
        check(medusa.meetsHerEyes(p), "he looks her in the eyes");
        medusa.petrify(helper.getLevel());
        check(p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && p.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() >= 5, "stone-slow");
        p.removeAllEffects();
        medusa.addAffection(p, Orders.AFFECTION);
        medusa.petrify(helper.getLevel());
        check(medusa.hasFlag(AbstractMonsterGirl.FLAG_BLINDFOLD) && !p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "befriended, she wears a blindfold");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "oniFleesBeansAndTakesADrink")
    public static void oniFleesBeansAndTakesADrink(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        ServerPlayer thrower = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ServerPlayer drinker = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        Devil oni = spawn(helper, GirlSpecies.ONI_DEVIL, new BlockPos(5, 2, 5));
        oni.setNoAi(true);
        helper.runAfterDelay(1, () -> {                                  // the sky darkens on the next tick
            check(oni.isHostileTo(thrower), "at night she comes for strangers");
            interact(oni, thrower, new ItemStack(Items.COCOA_BEANS));
            check(!oni.isHostileTo(thrower), "the beans drive her off");
            interact(oni, drinker, new ItemStack(Items.HONEY_BOTTLE));
            check(oni.isCalm(), "a drink calms her");
            HarpyTestUtil.remove(thrower);
            HarpyTestUtil.remove(drinker);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "mantisStrikesTheSilverfish")
    public static void mantisStrikesTheSilverfish(GameTestHelper helper) {
        Bug mantis = spawn(helper, GirlSpecies.MANTIS_BUG, new BlockPos(3, 2, 5));
        Silverfish fish = helper.spawn(EntityType.SILVERFISH, new BlockPos(7, 2, 5));
        fish.setNoAi(true);
        helper.succeedWhen(() -> check(!fish.isAlive(), "the mantis strikes: " + mantis.getAction()));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "orchidMantisHidesAmongFlowers")
    public static void orchidMantisHidesAmongFlowers(GameTestHelper helper) {
        helper.setBlock(new BlockPos(6, 1, 5), Blocks.GRASS_BLOCK);
        helper.setBlock(new BlockPos(6, 2, 5), Blocks.POPPY);
        Bug mantis = spawn(helper, GirlSpecies.MANTIS_BUG, new BlockPos(5, 2, 5));
        mantis.setVariant("orchid");
        mantis.setNoAi(true);
        helper.succeedWhen(() -> check(mantis.hasEffect(MobEffects.INVISIBILITY), "she vanishes among the flowers"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "enderSlimeBlinksAndCarries")
    public static void enderSlimeBlinksAndCarries(GameTestHelper helper) {
        SlimeGirl slime = spawn(helper, GirlSpecies.ENDER_SLIME, new BlockPos(5, 2, 5));
        slime.setNoAi(true);
        Vec3 before = slime.position();
        slime.hurt(helper.getLevel().damageSources().generic(), 1.0F);
        check(slime.position().distanceToSqr(before) > 0.5, "a blow and she is somewhere else");
        slime.setCarried(Blocks.DIRT.defaultBlockState());
        check(slime.getMainHandItem().is(Items.DIRT), "the block she carries shows in her hand");
        check(Kinship.kin(GirlSpecies.ENDER_SLIME).contains(EntityType.ENDERMITE), "endermites are her kin");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wolpertingerSwapsBoots")
    public static void wolpertingerSwapsBoots(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        Jackalope w = spawn(helper, GirlSpecies.WOLPERTINGER_JACKALOPE, new BlockPos(5, 2, 5));
        w.setNoAi(true);
        check(w.swapBoots(p), "she makes the swap");
        check(p.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS) && w.getBoots().is(Items.DIAMOND_BOOTS), "leather for diamond");
        interact(w, p, new ItemStack(Items.CARROT));
        check(p.getInventory().contains(s -> s.is(Items.DIAMOND_BOOTS)) && w.getBoots().isEmpty(), "fed, she gives them back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "wolpertingerGlidesDown")
    public static void wolpertingerGlidesDown(GameTestHelper helper) {
        Jackalope w = spawn(helper, GirlSpecies.WOLPERTINGER_JACKALOPE, new BlockPos(5, 14, 5));
        helper.succeedWhen(() -> check(w.onGround() && w.getHealth() == w.getMaxHealth(), "she glides down unhurt: " + w.getHealth()));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "flytrapSnapsStrangersNotFriends")
    public static void flytrapSnapsStrangersNotFriends(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Flora trap = spawn(helper, GirlSpecies.VENUS_FLYTRAP_FLORA, new BlockPos(5, 2, 5));
        trap.setNoAi(true);
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(6, 2, 5));
        pig.setNoAi(true);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 9));
        zombie.setNoAi(true);
        check(trap.snapsAt(pig) && trap.snapsAt(zombie) && trap.snapsAt(p), "wild, anything in reach");
        trap.addAffection(p, Orders.AFFECTION);
        check(!trap.snapsAt(pig) && trap.snapsAt(zombie) && !trap.snapsAt(p), "befriended, monsters only");
        trap.setAffection(p.getUUID(), 0);
        HarpyTestUtil.remove(p);
        helper.succeedWhen(() -> check(trap.getCaught() == pig, "her trap closes on the pig"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sirenLuresStrangers")
    public static void sirenLuresStrangers(GameTestHelper helper) {
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 1));
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 9));
        Merfolk siren = spawn(helper, GirlSpecies.SIREN_MERMAID, new BlockPos(5, 2, 5));
        siren.setNoAi(true);
        siren.addAffection(friend, Orders.AFFECTION);
        stranger.setDeltaMovement(Vec3.ZERO);
        friend.setDeltaMovement(Vec3.ZERO);
        siren.sirenSong(helper.getLevel());
        check(stranger.getDeltaMovement().z > 0.01, "the stranger is drawn toward her: " + stranger.getDeltaMovement());
        check(friend.getDeltaMovement().lengthSqr() < 1.0E-6, "a friend is not");
        HarpyTestUtil.remove(stranger);
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }
}
