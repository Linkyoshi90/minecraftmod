package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.compat.AlexsMobs;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.FinchHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kinship;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The turtles of 2026-10-25 (slider, Galapagos, box, softshell, matamata) and kinship for every girl with a vanilla counterpart. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TurtleAndKinTests {

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "sliderSlidesIntoTheWater")
    public static void sliderSlidesIntoTheWater(GameTestHelper helper) {
        helper.setBlock(new BlockPos(9, 2, 9), Blocks.WATER);
        Lizardfolk slider = spawn(helper, GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK, new BlockPos(4, 2, 4));
        check(slider.slideIntoWater(helper.getLevel()), "she finds the water");
        helper.succeedWhen(() -> check(slider.isInWater(), "and slides into it"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "boxTurtleShutsTightAndStaysHome")
    public static void boxTurtleShutsTightAndStaysHome(GameTestHelper helper) {
        Lizardfolk box = spawn(helper, GirlSpecies.BOX_TURTLE_LIZARDFOLK, new BlockPos(5, 2, 5));
        box.shutShell();
        float health = box.getHealth();
        check(!box.hurt(helper.getLevel().damageSources().generic(), 8.0F) && box.getHealth() == health, "shut tight, nothing gets in");
        helper.succeedWhen(() -> check(box.getHome() != null && box.hasRestriction(), "she keeps to her home"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "galapagosCarriesAFriend")
    public static void galapagosCarriesAFriend(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 5));
        Lizardfolk tortoise = spawn(helper, GirlSpecies.GALAPAGOS_TORTOISE_LIZARDFOLK, new BlockPos(5, 2, 5));
        check(!tortoise.hurt(helper.getLevel().damageSources().cactus(), 2.0F), "cactus does not prick her");
        tortoise.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == null, "a stranger cannot climb on");
        tortoise.setAffection(player.getUUID(), Orders.AFFECTION);
        tortoise.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == tortoise, "a friend rides on her shell");
        player.stopRiding();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "softshellBuriesHerself")
    public static void softshellBuriesHerself(GameTestHelper helper) {
        for (int x = 4; x <= 6; x++) {
            for (int z = 4; z <= 6; z++) {
                for (int y = 2; y <= 3; y++) helper.setBlock(new BlockPos(x, y, z), x == 5 && z == 5 ? Blocks.WATER : Blocks.GLASS);
            }
        }
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.MUD);              // (sand would fall through the platform)
        Lizardfolk softshell = spawn(helper, GirlSpecies.SOFTSHELL_TURTLE_LIZARDFOLK, new BlockPos(5, 2, 5));
        softshell.setNoAi(true);
        helper.succeedWhen(() -> check(softshell.isInvisible(), "still on the muddy bottom, she is buried out of sight"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "matamataGulpsFish")
    public static void matamataGulpsFish(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Lizardfolk matamata = spawn(helper, GirlSpecies.MATAMATA_LIZARDFOLK, new BlockPos(5, 2, 5));
        matamata.setNoAi(true);
        matamata.setAffection(player.getUUID(), Orders.AFFECTION);
        Cod cod = helper.spawn(EntityType.COD, new BlockPos(7, 2, 5));
        check(matamata.gulp(helper.getLevel()) && !cod.isAlive(), "the fish is sucked in");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, matamata.getBoundingBox().inflate(2), e -> e.getItem().is(Items.COD)).isEmpty(),
                "and her friend gets it");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "everyVanillaCounterpartIsKin")
    public static void everyVanillaCounterpartIsKin(GameTestHelper helper) {
        check(Kinship.kin(GirlSpecies.VAMPIRE_UNDEAD).contains(EntityType.BAT), "bats are the vampire's kin");
        check(Kinship.kin(GirlSpecies.YUKI_ONNA_UNDEAD).contains(EntityType.SNOW_GOLEM), "snow golems the yuki-onna's");
        check(Kinship.kin(GirlSpecies.GOLEM_CONSTRUCT).contains(EntityType.IRON_GOLEM), "iron golems the golem's");
        check(Kinship.kin(GirlSpecies.FAIRY_BUG).contains(EntityType.ALLAY), "allays the fairy's");
        check(Kinship.kin(GirlSpecies.LESSER_DEVIL).contains(EntityType.VEX), "vexes the lesser devil's");
        check(Kinship.kin(GirlSpecies.MATAMATA_LIZARDFOLK).contains(EntityType.TURTLE), "turtles every turtle's");
        var sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(5, 2, 5));
        check(Kinship.kinOf(sparrow).contains(EntityType.PARROT), "parrots a harpy's");
        check(Kinship.kinOf(helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(8, 2, 5))).contains(EntityType.CHICKEN), "chickens the ostrich's");
        check(Kinship.kinOf(helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(2, 2, 8))).contains(EntityType.FROG), "frogs an anuran's");
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Parrot parrot = helper.spawn(EntityType.PARROT, new BlockPos(6, 2, 6));
        check(Kinship.ignores(parrot, player), "a parrot near a friendly harpy does not mind you");
        check(AlexsMobs.kinOf("tiger_nekomata").isEmpty(), "without Alex's Mobs there is nothing extra");
        check(Kinship.kin(GirlSpecies.PEGASUS_CENTAUR).contains(EntityType.HORSE) && !Kinship.kin(GirlSpecies.KELPIE_CENTAUR).contains(EntityType.HORSE),
                "horses are the pegasus's kin, not the kelpie's");
        check(!Kinship.kin(GirlSpecies.ANUBIS_CANID).contains(EntityType.WOLF), "wolves belong to the wolf girl, not Anubis");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "finchesSingTheDawnChorus")
    public static void finchesSingTheDawnChorus(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        FinchHarpy a = helper.spawn(ModEntities.FINCH_HARPY.get(), new BlockPos(5, 2, 5));
        FinchHarpy b = helper.spawn(ModEntities.FINCH_HARPY.get(), new BlockPos(8, 2, 8));
        a.setAffection(player.getUUID(), Orders.AFFECTION);
        check(Kinship.kinOf(a).contains(EntityType.PARROT), "parrots are a finch's kin");
        check(a.isEdible(new ItemStack(Items.WHEAT_SEEDS)), "she eats seeds");
        a.startChorus(helper.getLevel());
        check(b.getAction() == AbstractHarpy.Action.SINGING, "every finch around joins the chorus");
        check(player.hasEffect(MobEffects.LUCK), "and her friend starts the day lucky");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
