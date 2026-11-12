package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CondoruneHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.PumpkingullHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Arachne;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Crustacean;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.Elf;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kitsune;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The sixteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): jorogumo, dunefennec, floodcrab, temple mantis, sword koi,
 * squidkin, reedbull, kikimora, cockatrice, valkyrie, pumpkingull, condorune.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SixteenthWaveTests {

    /** A friend for her: a survival player with enough friendship to count. */
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    private static Zombie stillZombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "jorogumoBindsAMonsterAtNight")
    public static void jorogumoBindsAMonsterAtNight(GameTestHelper helper) {
        Arachne joro = spawn(helper, GirlSpecies.JOROGUMO_ARACHNE, new BlockPos(3, 2, 3));
        joro.setNoAi(true);
        ServerPlayer p = friendOf(helper, joro, new BlockPos(5, 2, 5));
        Zombie z = stillZombie(helper, new BlockPos(7, 2, 5));
        check(joro.bindingWeb(helper.getLevel(), false) == null, "by day she leaves it be");
        check(joro.bindingWeb(helper.getLevel(), true) == z, "at night she binds the zombie");
        check(helper.getLevel().getBlockState(z.blockPosition()).is(Blocks.COBWEB), "in a cobweb at its feet");
        joro.meltSnares(helper.getLevel(), true);
        check(!helper.getLevel().getBlockState(z.blockPosition()).is(Blocks.COBWEB) && joro.snareCount() == 0, "and the web melts away");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dunefennecGuidesHerFriend")
    public static void dunefennecGuidesHerFriend(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.SAND);
        Kitsune fox = spawn(helper, GirlSpecies.DUNEFENNEC_KITSUNE, new BlockPos(4, 2, 5));
        fox.setNoAi(true);
        check(!fox.desertGuide(helper.getLevel()), "no friend, no help");
        ServerPlayer p = friendOf(helper, fox, new BlockPos(5, 2, 5));
        Husk husk = helper.spawn(EntityType.HUSK, new BlockPos(8, 2, 8));
        husk.setNoAi(true);
        husk.setTarget(p);
        check(fox.desertGuide(helper.getLevel()), "she helps");
        check(p.hasEffect(MobEffects.MOVEMENT_SPEED), "her friend runs fast on sand");
        check(husk.getTarget() == null, "and the husk loses interest");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "floodcrabMakesMud")
    public static void floodcrabMakesMud(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(6, 1, 5), Blocks.DIRT);
        Crustacean crab = spawn(helper, GirlSpecies.FLOODCRAB_CRUSTACEAN, new BlockPos(6, 2, 6));
        crab.setNoAi(true);
        check(crab.puddle(helper.getLevel()) == null, "only for a friend");
        ServerPlayer p = friendOf(helper, crab, new BlockPos(3, 2, 3));
        BlockPos at = crab.puddle(helper.getLevel());
        check(at != null && helper.getLevel().getBlockState(at).is(Blocks.MUD), "the dirt by the water is mud now: " + at);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "templeMantisCutsTripwires")
    public static void templeMantisCutsTripwires(GameTestHelper helper) {
        helper.setBlock(new BlockPos(6, 2, 5), Blocks.TRIPWIRE);
        Bug mantis = spawn(helper, GirlSpecies.TEMPLE_MANTIS_BUG, new BlockPos(3, 2, 6));
        mantis.setNoAi(true);
        ServerPlayer p = friendOf(helper, mantis, new BlockPos(4, 2, 5));
        check(mantis.cutTripwire(helper.getLevel(), false) == null, "a wire outside any structure is a player's: she leaves it");
        BlockPos at = mantis.cutTripwire(helper.getLevel(), true);
        check(at != null && !helper.getLevel().getBlockState(at).is(Blocks.TRIPWIRE), "the trap's wire is cut");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "swordKoiWinsTheTrident")
    public static void swordKoiWinsTheTrident(GameTestHelper helper) {
        Merfolk koi = spawn(helper, GirlSpecies.SWORD_KOI_MERMAID, new BlockPos(4, 2, 4));
        koi.setNoAi(true);
        Drowned d = helper.spawn(EntityType.DROWNED, new BlockPos(5, 2, 4));
        d.setNoAi(true);
        d.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        d.setHealth(1.0F);
        koi.doHurtTarget(d);
        check(!d.isAlive(), "the drowned falls");
        boolean trident = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, koi.getBoundingBox().inflate(6), e -> e.getItem().is(Items.TRIDENT)).isEmpty();
        check(trident, "and drops its trident");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "squidkinInksForHerFriend")
    public static void squidkinInksForHerFriend(GameTestHelper helper) {
        Mollusk squid = spawn(helper, GirlSpecies.SQUIDKIN_MOLLUSK, new BlockPos(3, 2, 3));
        squid.setNoAi(true);
        ServerPlayer p = friendOf(helper, squid, new BlockPos(5, 2, 5));
        Zombie z = stillZombie(helper, new BlockPos(7, 2, 5));
        p.hurtTime = 0;
        check(squid.inkCloud(helper.getLevel()) == -1, "unhurt, no ink");
        p.hurtTime = 10;
        check(squid.inkCloud(helper.getLevel()) >= 1, "hurt: ink");
        check(z.hasEffect(MobEffects.BLINDNESS) && p.hasEffect(MobEffects.INVISIBILITY), "the zombie is blind and her friend unseen");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "reedbullPlantsSugarCane")
    public static void reedbullPlantsSugarCane(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(6, 1, 5), Blocks.SAND);
        Minotaur cow = spawn(helper, GirlSpecies.REEDBULL_MINOTAUR, new BlockPos(3, 2, 7));
        cow.setNoAi(true);
        ServerPlayer p = friendOf(helper, cow, new BlockPos(4, 2, 4));
        BlockPos at = cow.plantReeds(helper.getLevel());
        check(at != null && helper.getLevel().getBlockState(at).is(Blocks.SUGAR_CANE), "sugar cane by the water: " + at);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kikimoraShutsTheDoors")
    public static void kikimoraShutsTheDoors(GameTestHelper helper) {
        helper.setBlock(new BlockPos(7, 2, 5), Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(BlockStateProperties.OPEN, true));
        helper.setBlock(new BlockPos(7, 2, 7), Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(BlockStateProperties.OPEN, true));
        Devil kiki = spawn(helper, GirlSpecies.KIKIMORA_DEVIL, new BlockPos(2, 2, 2));
        kiki.setNoAi(true);
        ServerPlayer p = friendOf(helper, kiki, new BlockPos(3, 2, 3));
        check(kiki.nightLatch(helper.getLevel(), false) == 0, "by day the doors stay as they are");
        check(kiki.nightLatch(helper.getLevel(), true) == 2, "at night she shuts both");
        check(!helper.getBlockState(new BlockPos(7, 2, 5)).getValue(BlockStateProperties.OPEN)
                && !helper.getBlockState(new BlockPos(7, 2, 7)).getValue(BlockStateProperties.OPEN), "trapdoor and gate are shut");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cockatriceGlares")
    public static void cockatriceGlares(GameTestHelper helper) {
        Dragon cock = spawn(helper, GirlSpecies.COCKATRICE_DRAGON, new BlockPos(3, 2, 3));
        cock.setNoAi(true);
        Zombie z = stillZombie(helper, new BlockPos(7, 2, 5));
        check(cock.glare(helper.getLevel()) == null, "only for a friend");
        ServerPlayer p = friendOf(helper, cock, new BlockPos(5, 2, 5));
        check(cock.glare(helper.getLevel()) == z, "she glares at the zombie");
        var slow = z.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        check(slow != null && slow.getAmplifier() == 3, "and it is nearly stopped");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "valkyrieKeepsAFallenFriendsThings")
    public static void valkyrieKeepsAFallenFriendsThings(GameTestHelper helper) {
        Elf val = spawn(helper, GirlSpecies.VALKYRIE_ELF, new BlockPos(3, 2, 3));
        val.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        List<ItemEntity> drops = new ArrayList<>(List.of(new ItemEntity(helper.getLevel(), p.getX(), p.getY(), p.getZ(), new ItemStack(Items.DIAMOND, 3))));
        check(!val.gatherFallen(p, drops) && drops.size() == 1, "a stranger's things are not hers to keep");
        val.addAffection(p, Orders.AFFECTION);
        check(val.gatherFallen(p, drops) && drops.isEmpty() && val.fallenCount() == 1, "she gathers her friend's things up");
        Vec3 far = helper.absoluteVec(new Vec3(3.5, 2, 13.5));
        p.teleportTo(far.x, far.y, far.z);
        check(val.returnFallen(helper.getLevel()) == 0, "she keeps them until her friend comes to her");
        Vec3 near = helper.absoluteVec(new Vec3(4.5, 2, 3.5));
        p.teleportTo(near.x, near.y, near.z);
        check(val.returnFallen(helper.getLevel()) == 1 && p.getInventory().countItem(Items.DIAMOND) == 3, "then gives them back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "pumpkingullCalmsEndermen")
    public static void pumpkingullCalmsEndermen(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        PumpkingullHarpy gull = helper.spawn(ModEntities.PUMPKINGULL_HARPY.get(), new BlockPos(5, 2, 5));
        gull.setNoAi(true);
        EnderMan e = helper.spawn(EntityType.ENDERMAN, new BlockPos(7, 2, 7));
        e.setNoAi(true);
        e.setTarget(friend);
        check(gull.calmEndermen(helper.getLevel()) == 0, "only for a friend");
        gull.setAffection(friend, Orders.AFFECTION);
        check(gull.calmEndermen(helper.getLevel()) == 1 && e.getTarget() == null, "the enderman looks away");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "condoruneCatchesAFall")
    public static void condoruneCatchesAFall(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        CondoruneHarpy condor = helper.spawn(ModEntities.CONDORUNE_HARPY.get(), new BlockPos(5, 2, 5));
        condor.setNoAi(true);
        condor.setAffection(friend, Orders.AFFECTION);
        friend.setOnGround(false);
        friend.fallDistance = 2.0F;
        check(condor.catchFall(helper.getLevel()) == null, "a short hop is not a fall");
        friend.fallDistance = 10.0F;
        check(condor.catchFall(helper.getLevel()) == friend, "she catches her friend");
        check(friend.hasEffect(MobEffects.SLOW_FALLING) && friend.fallDistance == 0.0F, "slow falling, the fall forgiven");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }
}
