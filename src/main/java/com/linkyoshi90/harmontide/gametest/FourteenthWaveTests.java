package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.Armoured;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kitsune;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The fourteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): gribboar, silkcoon, bronzejaw ox, cocoskink, prairhorn,
 * halberelk, vulphea, cait sith, cu sith, yeti, undine, ittan-momen.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FourteenthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gribboarRootsForTruffles")
    public static void gribboarRootsForTruffles(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.PODZOL);
        Armoured boar = spawn(helper, GirlSpecies.GRIBBOAR_ARMOURED, new BlockPos(5, 2, 5));
        boar.setNoAi(true);
        ItemStack find = boar.root(helper.getLevel());
        check(!find.isEmpty(), "she digs something up");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, boar.getBoundingBox().inflate(2)).isEmpty(), "and leaves it lying there");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "silkcoonWashesOutDye")
    public static void silkcoonWashesOutDye(GameTestHelper helper) {
        check(Rodent.washed(new ItemStack(Items.RED_WOOL, 5)).is(Items.WHITE_WOOL), "red wool comes out white");
        check(Rodent.washed(new ItemStack(Items.WHITE_WOOL)) == null, "white wool has nothing to wash");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Rodent coon = spawn(helper, GirlSpecies.SILKCOON_RODENT, new BlockPos(4, 2, 3));
        coon.setNoAi(true);
        interact(coon, p, new ItemStack(Items.BLUE_WOOL, 3));
        check(p.getMainHandItem().is(Items.BLUE_WOOL), "no water, no washing");
        helper.setBlock(new BlockPos(5, 2, 3), Blocks.WATER);
        interact(coon, p, new ItemStack(Items.BLUE_WOOL, 3));
        check(p.getMainHandItem().is(Items.WHITE_WOOL) && p.getMainHandItem().getCount() == 3, "by the water she washes it white");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bronzejawPloughsOnALead")
    public static void bronzejawPloughsOnALead(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.GRASS_BLOCK);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Minotaur ox = spawn(helper, GirlSpecies.BRONZEJAW_MINOTAUR, new BlockPos(5, 2, 5));
        ox.setNoAi(true);
        ox.setDeltaMovement(0.1, 0, 0);
        check(!ox.ploughOnLead(helper.getLevel()), "loose, she does not plough");
        ox.setLeashedTo(p, true);
        ox.setDeltaMovement(0.1, 0, 0);
        check(ox.ploughOnLead(helper.getLevel()) && helper.getBlockState(new BlockPos(5, 1, 5)).is(Blocks.FARMLAND), "led, she ploughs the grass");
        ox.dropLeash(true, false);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cocoskinkGrowsCocoa")
    public static void cocoskinkGrowsCocoa(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.JUNGLE_LOG);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Lizardfolk skink = spawn(helper, GirlSpecies.COCOSKINK_LIZARDFOLK, new BlockPos(7, 2, 7));
        skink.setNoAi(true);
        skink.addAffection(friend, Orders.AFFECTION);
        check(skink.growCocoa(helper.getLevel()), "she grows a cocoa pod");
        boolean pod = false;
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) pod |= helper.getBlockState(new BlockPos(5, 2, 5).relative(d)).is(Blocks.COCOA);
        check(pod, "on the jungle log");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "prairhornIsTheFastest")
    public static void prairhornIsTheFastest(GameTestHelper helper) {
        Centaur pr = spawn(helper, GirlSpecies.PRAIRHORN_CENTAUR, new BlockPos(3, 2, 3));
        Centaur horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(7, 2, 7));
        check(pr.getAttributeValue(Attributes.MOVEMENT_SPEED) > horse.getAttributeValue(Attributes.MOVEMENT_SPEED), "faster than a horse");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "halberelkFellsTrees")
    public static void halberelkFellsTrees(GameTestHelper helper) {
        for (int y = 2; y <= 6; y++) helper.setBlock(new BlockPos(3, y, 3), Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(3, 7, 3), Blocks.OAK_LEAVES);
        for (int y = 2; y <= 5; y++) helper.setBlock(new BlockPos(8, y, 8), Blocks.SPRUCE_LOG);        // a post, not a tree: no leaves
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 3));
        Centaur elk = spawn(helper, GirlSpecies.HALBERELK_CENTAUR, new BlockPos(6, 2, 6));
        elk.setNoAi(true);
        elk.addAffection(friend, Orders.AFFECTION);
        BlockPos cut = helper.absolutePos(new BlockPos(3, 2, 3));
        check(Centaur.fell(helper.getLevel(), friend, cut, helper.getLevel().getBlockState(cut)) == 4, "she fells the rest of the tree");
        check(helper.getBlockState(new BlockPos(3, 6, 3)).isAir(), "right to the top");
        BlockPos post = helper.absolutePos(new BlockPos(8, 2, 8));
        check(Centaur.fell(helper.getLevel(), friend, post, helper.getLevel().getBlockState(post)) == 0, "but leaves a post with no leaves alone");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "vulpheaRisesFromHerAshes")
    public static void vulpheaRisesFromHerAshes(GameTestHelper helper) {
        Kitsune fox = spawn(helper, GirlSpecies.VULPHEA_KITSUNE, new BlockPos(5, 2, 5));
        fox.setNoAi(true);
        check(fox.fireImmune(), "she cannot burn");
        fox.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        check(fox.isAlive() && fox.isAshes() && fox.isInvisible(), "a killing blow leaves only ashes");
        CompoundTag tag = new CompoundTag();
        fox.addAdditionalSaveData(tag);
        tag.putInt("Ashes", 1);
        fox.readAdditionalSaveData(tag);
        helper.runAfterDelay(3, () -> {
            check(!fox.isAshes() && !fox.isInvisible() && fox.getHealth() == fox.getMaxHealth(), "and she rises again, whole");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "caitSithKeepsPhantomsAway")
    public static void caitSithKeepsPhantomsAway(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Nekomata cat = spawn(helper, GirlSpecies.CAIT_SITH_NEKOMATA, new BlockPos(5, 2, 5));
        cat.setNoAi(true);
        cat.addAffection(friend, Orders.AFFECTION);
        Phantom ph = helper.spawn(EntityType.PHANTOM, new BlockPos(3, 6, 3));
        ph.setNoAi(true);
        ph.setTarget(friend);
        check(cat.warnOffPhantoms(helper.getLevel()) == 1 && ph.getTarget() == null, "the phantom loses interest in her friend");
        friend.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
        cat.interact(friend, InteractionHand.MAIN_HAND);
        check(friend.getMainHandItem().is(Items.BUCKET) && friend.hasEffect(MobEffects.LUCK), "a saucer of milk brings Luck");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cuSithBarksThrice")
    public static void cuSithBarksThrice(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Canid hound = spawn(helper, GirlSpecies.CU_SITH_CANID, new BlockPos(5, 2, 5));
        hound.setNoAi(true);
        hound.addAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 7));
        check(!hound.bark(helper.getLevel()) && !hound.bark(helper.getLevel()), "two barks: a warning");
        check(hound.bark(helper.getLevel()) && z.hasEffect(MobEffects.WEAKNESS), "the third sends the monster fleeing");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "yetiThrowsSnowballs")
    public static void yetiThrowsSnowballs(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Canid yeti = spawn(helper, GirlSpecies.YETI_CANID, new BlockPos(5, 2, 5));
        yeti.setNoAi(true);
        yeti.addAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 5));
        z.setNoAi(true);
        check(!yeti.canFreeze(), "she never freezes");
        check(yeti.throwSnowball(helper.getLevel()), "she throws a snowball at the zombie");
        check(!helper.getLevel().getEntitiesOfClass(Snowball.class, yeti.getBoundingBox().inflate(4)).isEmpty(), "there it goes");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "undineWatersTheFarm")
    public static void undineWatersTheFarm(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(6, 2, 3), Blocks.CAULDRON);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 6));
        SlimeGirl undine = spawn(helper, GirlSpecies.UNDINE_SLIME, new BlockPos(5, 2, 5));
        undine.setNoAi(true);
        undine.addAffection(friend, Orders.AFFECTION);
        check(undine.water(helper.getLevel()) >= 2, "she waters and fills");
        check(helper.getBlockState(new BlockPos(3, 1, 3)).getValue(FarmBlock.MOISTURE) == 7, "the farmland is wet");
        check(helper.getBlockState(new BlockPos(6, 2, 3)).is(Blocks.WATER_CAULDRON)
                && helper.getBlockState(new BlockPos(6, 2, 3)).getValue(LayeredCauldronBlock.LEVEL) == 3, "and the cauldron full");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ittanMomenCarriesHerFriend")
    public static void ittanMomenCarriesHerFriend(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Devil cloth = spawn(helper, GirlSpecies.ITTAN_MOMEN_DEVIL, new BlockPos(4, 2, 4));
        cloth.setNoAi(true);
        check(cloth.canFly(), "she flies");
        interact(cloth, friend, ItemStack.EMPTY);
        check(friend.getVehicle() == null, "strangers do not ride her");
        cloth.addAffection(friend, Orders.AFFECTION);
        interact(cloth, friend, ItemStack.EMPTY);
        check(friend.getVehicle() == cloth && cloth.getControllingPassenger() == friend, "her friend rides and steers her");
        friend.stopRiding();
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }
}
