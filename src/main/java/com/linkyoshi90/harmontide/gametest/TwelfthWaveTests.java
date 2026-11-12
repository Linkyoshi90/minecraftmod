package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.DodoboldHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.Flora;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Jackalope;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The twelfth Harmontide wave (HARMONTIDE_CONCEPTS.md): cheshire cat, kesaran pasaran, tsuchinoko, platypus, moonseal,
 * marmotaur, bambadger, dodobold, boghare, gremlin, karakasa obake, alebrije.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TwelfthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cheshireBlinksToHerFriend")
    public static void cheshireBlinksToHerFriend(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Nekomata cat = spawn(helper, GirlSpecies.CHESHIRE_NEKOMATA, new BlockPos(5, 2, 5));
        cat.setNoAi(true);
        cat.setNoGravity(true);
        cat.addAffection(friend, Orders.AFFECTION);
        check(!cat.blinkToFriend(helper.getLevel()), "close by, she stays where she is");
        Vec3 far = helper.absoluteVec(new Vec3(25, 4, 2));
        cat.teleportTo(far.x, far.y, far.z);
        check(cat.blinkToFriend(helper.getLevel()), "left behind, she blinks back");
        check(cat.distanceToSqr(friend) < 4 * 4, "to her friend's side: " + Math.sqrt(cat.distanceToSqr(friend)));
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "kesaranDriftsAndSplits")
    public static void kesaranDriftsAndSplits(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Flora puff = spawn(helper, GirlSpecies.KESARAN_PASARAN_FLORA, new BlockPos(5, 2, 5));
        double start = puff.getY();
        interact(puff, p, new ItemStack(Items.SUGAR, 4));
        java.util.function.IntSupplier puffs = () -> helper.getLevel().getEntitiesOfClass(Flora.class, puff.getBoundingBox().inflate(16), Flora::isPuff).size();
        check(puffs.getAsInt() == 2, "sugar: she splits in two");
        check(puff.split(helper.getLevel()) && puffs.getAsInt() == Flora.MAX_PUFFS, "and once more");
        check(!puff.split(helper.getLevel()), "but no more than three of her");
        HarpyTestUtil.remove(p);
        helper.succeedWhen(() -> check(puff.isNoGravity() && puff.getY() > start + 1.0, "she floats up off the ground: " + (puff.getY() - start)));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tsuchinokoRollsDownhill")
    public static void tsuchinokoRollsDownhill(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 2, 5), Blocks.STONE);
        helper.setBlock(new BlockPos(3, 3, 5), Blocks.STONE);
        Lamia hill = spawn(helper, GirlSpecies.TSUCHINOKO_LAMIA, new BlockPos(3, 4, 5));
        Lamia flat = spawn(helper, GirlSpecies.TSUCHINOKO_LAMIA, new BlockPos(5, 2, 2));
        hill.setNoAi(true);
        flat.setNoAi(true);
        hill.setDeltaMovement(0.2, 0, 0);
        flat.setDeltaMovement(0.2, 0, 0);
        check(hill.downhillAhead(helper.getLevel()), "the ground falls away ahead of her on the hill");
        check(!flat.downhillAhead(helper.getLevel()), "but not on the flat");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "platypusSensesAndStings")
    public static void platypusSensesAndStings(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Merfolk pl = spawn(helper, GirlSpecies.PLATYPUS_MERMAID, new BlockPos(5, 2, 5));
        pl.setNoAi(true);
        pl.addAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 8));
        z.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(6, 2, 5));
        cow.setNoAi(true);
        pl.hurt(helper.getLevel().damageSources().mobAttack(cow), 1.0F);
        check(cow.hasEffect(MobEffects.POISON), "her spur poisons whoever hits her");
        helper.succeedWhen(() -> {                                       // she has to be in the water first
            check(pl.electrosense(helper.getLevel()) >= 1 && z.hasEffect(MobEffects.GLOWING), "the monster near her friend glows");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "moonsealSingsAtTheFullMoon")
    public static void moonsealSingsAtTheFullMoon(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);                             // midnight of day 0: a full moon
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Merfolk seal = spawn(helper, GirlSpecies.MOONSEAL_MERMAID, new BlockPos(5, 2, 5));
        seal.setNoAi(true);
        seal.addAffection(friend, Orders.AFFECTION);
        helper.runAfterDelay(1, () -> {                                  // the sky darkens on the next tick
            check(seal.moonSong(helper.getLevel()), "she sings at the full moon");
            check(friend.hasEffect(MobEffects.REGENERATION), "and her friend heals");
            HarpyTestUtil.remove(friend);
            helper.setDayTime(1000);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "marmotaurWhistles")
    public static void marmotaurWhistles(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Rodent m = spawn(helper, GirlSpecies.MARMOTAUR_RODENT, new BlockPos(4, 2, 4));
        m.setNoAi(true);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        z.setNoAi(true);
        check(m.keepWatch(helper.getLevel()) == 0, "she keeps watch only for a friend");
        m.addAffection(friend, Orders.AFFECTION);
        check(m.keepWatch(helper.getLevel()) == 1 && z.hasEffect(MobEffects.GLOWING), "the monster coming glows");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bambadgerFluteGrowsBamboo")
    public static void bambadgerFluteGrowsBamboo(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.BAMBOO);
        helper.setBlock(new BlockPos(6, 1, 3), Blocks.SAND);
        helper.setBlock(new BlockPos(7, 1, 3), Blocks.WATER);
        helper.setBlock(new BlockPos(6, 2, 3), Blocks.SUGAR_CANE);
        Rodent b = spawn(helper, GirlSpecies.BAMBADGER_RODENT, new BlockPos(5, 2, 6));
        b.setNoAi(true);
        check(b.playFlute(helper.getLevel()) >= 2, "her tune grows them");
        check(helper.getBlockState(new BlockPos(3, 3, 3)).is(Blocks.BAMBOO), "the bamboo shoots up");
        check(helper.getBlockState(new BlockPos(6, 3, 3)).is(Blocks.SUGAR_CANE), "and the sugar cane");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dodoboldCarriesAPack")
    public static void dodoboldCarriesAPack(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        DodoboldHarpy dodo = helper.spawn(ModEntities.DODOBOLD_HARPY.get(), new BlockPos(4, 2, 3));
        dodo.setNoAi(true);
        dodo.setAffection(p, Orders.AFFECTION);
        p.setShiftKeyDown(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        dodo.interact(p, InteractionHand.MAIN_HAND);
        p.setShiftKeyDown(false);
        check(p.containerMenu instanceof ChestMenu, "a crouching hand opens her saddlebags");
        dodo.getPack().addItem(new ItemStack(Items.DIRT, 5));
        CompoundTag tag = new CompoundTag();
        dodo.addAdditionalSaveData(tag);
        dodo.getPack().clearContent();
        dodo.readAdditionalSaveData(tag);
        check(dodo.getPack().countItem(Items.DIRT) == 5, "and what is in them is saved");
        p.closeContainer();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "boghareLendsHerLeap")
    public static void boghareLendsHerLeap(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Jackalope hare = spawn(helper, GirlSpecies.BOGHARE_JACKALOPE, new BlockPos(5, 2, 5));
        hare.setNoAi(true);
        hare.addAffection(friend, Orders.AFFECTION);
        helper.succeedWhen(() -> {
            check(friend.hasEffect(MobEffects.JUMP), "her friend leaps like her");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gremlinTinkers")
    public static void gremlinTinkers(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, Blocks.LEVER);
        helper.setBlock(new BlockPos(7, 2, 3), Blocks.FURNACE);
        Devil g = spawn(helper, GirlSpecies.GREMLIN_DEVIL, new BlockPos(5, 2, 5));
        g.setNoAi(true);
        check(g.tinker(helper.getLevel()), "she pranks the lever");
        check(helper.getBlockState(at).getValue(LeverBlock.POWERED), "and flips it");
        check(g.findFurnaces(helper.getLevel()) == 1, "she finds the furnace to tinker with");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "karakasaCatchesArrows")
    public static void karakasaCatchesArrows(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Devil k = spawn(helper, GirlSpecies.KARAKASA_OBAKE_DEVIL, new BlockPos(4, 2, 3));
        k.setNoAi(true);
        Arrow a = EntityType.ARROW.create(helper.getLevel());
        Vec3 at = helper.absoluteVec(new Vec3(6, 3, 3));
        a.setPos(at.x, at.y, at.z);
        helper.getLevel().addFreshEntity(a);
        check(!Devil.shelter(friend, a), "strangers get no shelter");
        k.addAffection(friend, Orders.AFFECTION);
        check(Devil.shelter(friend, a) && a.isRemoved(), "her umbrella catches the arrow aimed at her friend");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "alebrijePaintsSheep")
    public static void alebrijePaintsSheep(GameTestHelper helper) {
        Dragon a = spawn(helper, GirlSpecies.ALEBRIJE_DRAGON, new BlockPos(5, 2, 5));
        a.setNoAi(true);
        Sheep s = helper.spawn(EntityType.SHEEP, new BlockPos(7, 2, 5));
        s.setNoAi(true);
        s.setColor(DyeColor.WHITE);
        check(a.canFly(), "she flies");
        check(a.paintSheep(helper.getLevel()) && s.getColor() != DyeColor.WHITE, "she dyes the sheep: " + s.getColor());
        helper.succeed();
    }
}
