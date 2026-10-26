package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import com.linkyoshi90.monstergirls.entity.TenguHarpy;
import com.linkyoshi90.monstergirls.entity.ToucanHarpy;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The fourth Harmontide wave (HARMONTIDE_CONCEPTS.md): camel, dromedary, kelpie, wadjet, hornet, dugong, and the injured girl. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class FourthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "injuredGirlIsMendedByABalm")
    public static void injuredGirlIsMendedByABalm(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bug hornet = spawn(helper, GirlSpecies.HORNET_BUG, new BlockPos(4, 2, 4));
        Centaur horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(9, 2, 9));
        hornet.setInjured(true);
        horse.setInjured(true);
        check(hornet.getNavigation() instanceof GroundPathNavigation, "a broken wing: she walks");
        check(!hornet.isHostileTo(player), "an injured hornet stings nobody, even close up");
        hornet.setTarget(player);
        check(hornet.getTarget() == null, "and cannot fight");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HEALING_BALM.get(), 2));
        player.interactOn(hornet, InteractionHand.MAIN_HAND);
        check(!hornet.isInjured() && hornet.getAffection(player) == AbstractMonsterGirl.FULL_AFFECTION, "a balm mends her: she is yours for life");
        check(hornet.getNavigation() instanceof FlyingPathNavigation, "and she flies again");
        player.interactOn(horse, InteractionHand.MAIN_HAND);
        check(!horse.isInjured() && horse.getAffection(player) == 40 && player.getMainHandItem().isEmpty(), "any injured girl lets you, and remembers it");
        int injured = 0;
        for (int i = 0; i < 200; i++) {                                // hornets are often found injured
            AbstractMonsterGirl h = ModGirls.type(GirlSpecies.HORNET_BUG).create(helper.getLevel());
            h.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.ZERO), MobSpawnType.NATURAL, null);
            if (h.isInjured()) injured++;
        }
        check(injured > 15 && injured < 80, "about one hornet in five is injured: " + injured);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "camelsCarryAnyoneAndGiveMilk")
    public static void camelsCarryAnyoneAndGiveMilk(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 3));
        Centaur camel = spawn(helper, GirlSpecies.DROMEDARY_CENTAUR, new BlockPos(5, 2, 5));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(camel, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == camel && camel.getControllingPassenger() == player, "a stranger may ride her");
        player.stopRiding();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 2));
        player.interactOn(camel, InteractionHand.MAIN_HAND);
        check(player.getInventory().countItem(Items.MILK_BUCKET) == 1, "a bucket milks her");
        player.interactOn(camel, InteractionHand.MAIN_HAND);
        check(player.getInventory().countItem(Items.MILK_BUCKET) == 1, "but not twice in a row");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "camelBreedsWithAVanillaCamel")
    public static void camelBreedsWithAVanillaCamel(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Centaur girl = spawn(helper, GirlSpecies.CAMEL_CENTAUR, new BlockPos(5, 2, 5));
        Camel camel = helper.spawn(EntityType.CAMEL, new BlockPos(8, 2, 5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CACTUS, 2));
        player.interactOn(girl, InteractionHand.MAIN_HAND);
        check(girl.getCamelLove() > 0, "cactus puts her in the mood");
        camel.setInLove(player);
        helper.succeedWhen(() -> {
            check(girl.getGestation().isCarrying(), "she courts the camel in love and conceives");
            check(!camel.isInLove(), "and his love is spent");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kelpieHuntsByNightAndCarriesUnderSaddle")
    public static void kelpieHuntsByNightAndCarriesUnderSaddle(GameTestHelper helper) {
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 2));
        Centaur kelpie = spawn(helper, GirlSpecies.KELPIE_CENTAUR, new BlockPos(6, 2, 6));
        kelpie.setAffection(friend.getUUID(), Orders.AFFECTION);
        long time = helper.getLevel().getDayTime();
        helper.getLevel().setDayTime(1000);
        check(!kelpie.isHostileTo(stranger), "by day she is calm");
        helper.getLevel().setDayTime(18000);
        boolean peaceful = helper.getLevel().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL;
        check(kelpie.isHostileTo(stranger) != peaceful && !kelpie.isHostileTo(friend), "at night she hunts strangers (unless peaceful), not friends");
        helper.getLevel().setDayTime(time);
        friend.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        friend.interactOn(kelpie, InteractionHand.MAIN_HAND);
        check(friend.getVehicle() == null, "no riding her bareback");
        friend.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        friend.interactOn(kelpie, InteractionHand.MAIN_HAND);
        check(kelpie.hasFlag(AbstractMonsterGirl.FLAG_SADDLED), "a friend may saddle her");
        friend.interactOn(kelpie, InteractionHand.MAIN_HAND);
        check(friend.getVehicle() == kelpie, "saddled, she carries you");
        HarpyTestUtil.remove(stranger);
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "wadjetBlessesAndHuntsMonsters")
    public static void wadjetBlessesAndHuntsMonsters(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Lamia wadjet = spawn(helper, GirlSpecies.WADJET_LAMIA, new BlockPos(5, 2, 5));
        AbstractMonsterGirl follower = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(3, 2, 3));
        follower.setAffection(player.getUUID(), Orders.AFFECTION);
        wadjet.bless(helper.getLevel());
        check(player.hasEffect(MobEffects.REGENERATION) && player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 0, "a passer-by is blessed");
        check(follower.hasEffect(MobEffects.REGENERATION), "and so are the girls with her");
        double base = wadjet.getAttributeValue(Attributes.ATTACK_DAMAGE);
        wadjet.setCustomName(Component.literal("Nekhbet"));
        player.removeAllEffects();
        wadjet.bless(helper.getLevel());
        check(player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 1, "Nekhbet's blessing is stronger");
        check(wadjet.getAttributeValue(Attributes.ATTACK_DAMAGE) == base + 4.0, "and her bite harder: " + wadjet.getAttributeValue(Attributes.ATTACK_DAMAGE));
        HarpyTestUtil.remove(player);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 9));
        helper.succeedWhen(() -> check(wadjet.getTarget() == zombie, "she goes for any monster in sight"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "hornetIsTerritorialAndHuntsBees")
    public static void hornetIsTerritorialAndHuntsBees(GameTestHelper helper) {
        ServerPlayer near = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        ServerPlayer far = HarpyTestUtil.survivalPlayer(helper, new BlockPos(14, 2, 14));
        Bug hornet = spawn(helper, GirlSpecies.HORNET_BUG, new BlockPos(5, 2, 5));
        hornet.setNoAi(true);
        boolean peaceful = helper.getLevel().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL;
        check(hornet.isHostileTo(near) != peaceful && !hornet.isHostileTo(far), "she stings players who come close (unless peaceful), not those who keep away");
        hornet.setAffection(near.getUUID(), Orders.AFFECTION);
        check(!hornet.isHostileTo(near), "never a friend");
        for (int i = 0; i < 40; i++) {
            ItemStack g = hornet.rollGift(near);
            check(g.is(Items.PAPER) || g.is(Items.CLAY_BALL), "her presents are paper and clay: " + g);
        }
        HarpyTestUtil.remove(near);
        HarpyTestUtil.remove(far);
        Bug wild = spawn(helper, GirlSpecies.HORNET_BUG, new BlockPos(10, 3, 5));
        Bug bee = spawn(helper, GirlSpecies.BEE_BUG, new BlockPos(12, 3, 5));
        bee.setNoAi(true);
        helper.succeedWhen(() -> check(wild.getTarget() == bee, "a wild hornet hunts bee girls"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dugongIsWonWithSeagrass")
    public static void dugongIsWonWithSeagrass(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Merfolk dugong = spawn(helper, GirlSpecies.DUGONG_MERMAID, new BlockPos(5, 2, 5));
        check(!dugong.isFood(new ItemStack(Items.COD)), "a grazer, no fish");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SEAGRASS));
        player.interactOn(dugong, InteractionHand.MAIN_HAND);
        check(dugong.getAffection(player) == 3, "seagrass wins her over: " + dugong.getAffection(player));
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    // --- swimming, riding (playtest fixes, 2026-10-24) -------------------------------------------------------------

    /** A glass-walled pool over the whole platform, water from y=1 up to y=depth. */
    static void pool(GameTestHelper helper, int depth) {
        for (int x = 0; x <= 14; x++) for (int z = 0; z <= 14; z++) for (int y = 1; y <= depth; y++) {
            boolean wall = x == 0 || z == 0 || x == 14 || z == 14;
            helper.setBlock(new BlockPos(x, y, z), wall ? net.minecraft.world.level.block.Blocks.GLASS : net.minecraft.world.level.block.Blocks.WATER);
        }
    }

    /** Across the pool and up from the bottom to near the surface (a target mostly overhead is the hard case). */
    private static void swimsStraightThere(GameTestHelper helper, GirlSpecies s) {
        pool(helper, 7);
        AbstractMonsterGirl girl = spawn(helper, s, new BlockPos(4, 1, 4));
        Vec3 goal = helper.absoluteVec(new Vec3(6.5, 5.0, 5.5));
        helper.succeedWhen(() -> {
            if (girl.tickCount % 20 == 0) girl.getNavigation().moveTo(goal.x, goal.y, goal.z, 1.0);
            check(girl.position().distanceToSqr(goal) < 2.0 * 2.0 && girl.getNavigation().isDone(),
                    s + " gets there and stops instead of circling: " + girl.position().distanceTo(goal));
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "orcaSwimsStraightThere")
    public static void orcaSwimsStraightThere(GameTestHelper helper) {
        swimsStraightThere(helper, GirlSpecies.ORCA_MERMAID);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "sealSwimsStraightThere")
    public static void sealSwimsStraightThere(GameTestHelper helper) {
        swimsStraightThere(helper, GirlSpecies.SEAL_MERMAID);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "seaSnakeSwimsStraightThere")
    public static void seaSnakeSwimsStraightThere(GameTestHelper helper) {
        swimsStraightThere(helper, GirlSpecies.SEA_SNAKE_LAMIA);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "ikaDivesInsteadOfBobbing")
    public static void ikaDivesInsteadOfBobbing(GameTestHelper helper) {
        pool(helper, 4);
        AbstractMonsterGirl ika = spawn(helper, GirlSpecies.IKA_MOLLUSK, new BlockPos(7, 1, 7));
        int[] under = {0};
        helper.onEachTick(() -> {
            if (ika.tickCount > 40 && ika.isUnderWater()) under[0]++;
        });
        helper.runAfterDelay(140, () -> {
            check(under[0] > 50, "Ika stays down in the water instead of bobbing at the surface: " + under[0] + "/100 ticks under");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "merfolkComeUpForAir")
    public static void merfolkComeUpForAir(GameTestHelper helper) {
        pool(helper, 4);
        Merfolk orca = spawn(helper, GirlSpecies.ORCA_MERMAID, new BlockPos(7, 1, 7));
        check(orca.getType().is(net.minecraft.tags.EntityTypeTags.CAN_BREATHE_UNDER_WATER), "she breathes water");
        orca.setNextBreath(0);
        helper.succeedWhen(() -> check(orca.isInWater() && !orca.isEyeInFluid(net.minecraft.tags.FluidTags.WATER), "she puts her head out for a breath"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wadjetCarriesNobody")
    public static void wadjetCarriesNobody(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 1, 4));
        Lamia wadjet = spawn(helper, GirlSpecies.WADJET_LAMIA, new BlockPos(5, 1, 5));
        Lamia python = spawn(helper, GirlSpecies.PYTHON_LAMIA, new BlockPos(9, 1, 9));
        wadjet.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        python.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(wadjet, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == null, "the wadjet walks on legs: no coils to ride");
        player.interactOn(python, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == python, "a serpent lamia still carries you");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    /** The client knows nothing of her friendship, so who steers must not depend on it (it did: lamias and unsaddled centaurs ignored their riders). */
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ridersSteerWithoutKnowingFriendship")
    public static void ridersSteerWithoutKnowingFriendship(GameTestHelper helper) {
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.PYTHON_LAMIA, GirlSpecies.HORSE_CENTAUR}) {
            ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 1, 4));
            AbstractMonsterGirl mount = spawn(helper, s, new BlockPos(5, 1, 5));
            player.startRiding(mount, true);                       // as the client sees it: on her back, friendship unknown (0)
            check(mount.getControllingPassenger() == player, s + ": her rider steers");
            player.stopRiding();
            HarpyTestUtil.remove(player);
        }
        helper.succeed();
    }

    // --- the harpies ---------------------------------------------------------------------------------------------

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tenguGustsTrespassersAway")
    public static void tenguGustsTrespassersAway(GameTestHelper helper) {
        TenguHarpy tengu = helper.spawn(ModEntities.TENGU_HARPY.get(), new BlockPos(7, 2, 7));
        tengu.setNoAi(true);
        Creeper creeper = helper.spawn(EntityType.CREEPER, new BlockPos(9, 2, 7));
        creeper.setNoAi(true);
        check(!tengu.trespasses(creeper), "free, she leaves monsters be: she is neutral");
        tengu.getOrders().set(Orders.Command.PATROL, null, tengu.blockPosition());
        check(tengu.trespasses(creeper), "on patrol, a creeper on her ground is a trespasser");
        tengu.gust(creeper);
        Vec3 push = creeper.getDeltaMovement();
        check(push.x > 1.0 && push.y > 0.3, "her fan throws it out of her territory: " + push);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "toucansHoldAClackOff")
    public static void toucansHoldAClackOff(GameTestHelper helper) {
        ToucanHarpy a = helper.spawn(ModEntities.TOUCAN_HARPY.get(), new BlockPos(5, 2, 5));
        ToucanHarpy b = helper.spawn(ModEntities.TOUCAN_HARPY.get(), new BlockPos(7, 2, 5));
        check(a.isEdible(new ItemStack(Items.MELON_SLICE)) && !a.isEdible(new ItemStack(Items.COD)), "a fruit eater");
        a.startClackOff(b);
        check(a.getClackPartner() == b && b.getClackPartner() == a, "two toucans face off");
        helper.succeedWhen(() -> check(a.getAction() == AbstractHarpy.Action.CLACKING || b.getAction() == AbstractHarpy.Action.CLACKING,
                "and clack at each other"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "penguinSharesHerCatch")
    public static void penguinSharesHerCatch(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        PenguinHarpy penguin = helper.spawn(ModEntities.PENGUIN_HARPY.get(), new BlockPos(8, 2, 8));
        penguin.setAffection(player.getUUID(), PenguinHarpy.FRIEND);
        Cod cod = helper.spawn(EntityType.COD, new BlockPos(9, 2, 8));
        penguin.killedEntity(helper.getLevel(), cod);
        int shared = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(1.5), e -> e.getItem().is(Items.COD)).size();
        check(shared == 1, "a befriended penguin brings her player a fish: " + shared);
        int wanderers = 0;
        for (int i = 0; i < 240; i++) {
            PenguinHarpy p = ModEntities.PENGUIN_HARPY.get().create(helper.getLevel());
            p.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(BlockPos.ZERO), MobSpawnType.NATURAL, null);
            if (p.isWanderer()) wanderers++;
        }
        check(wanderers > 5 && wanderers < 45, "now and then one walks off alone: " + wanderers);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "orcaHuntsPenguinsWhoHuntKoi")
    public static void orcaHuntsPenguinsWhoHuntKoi(GameTestHelper helper) {
        PenguinHarpy penguin = helper.spawn(ModEntities.PENGUIN_HARPY.get(), new BlockPos(7, 2, 7));
        Merfolk koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(9, 2, 7));
        Merfolk orca = spawn(helper, GirlSpecies.ORCA_MERMAID, new BlockPos(4, 2, 7));
        koi.setNoAi(true);
        boolean[] seen = new boolean[2];                           // once bitten, the penguin turns on the orca: count each hunt when it happens
        helper.onEachTick(() -> {
            seen[0] |= penguin.getTarget() == koi;
            seen[1] |= orca.getTarget() == penguin;
        });
        helper.succeedWhen(() -> {
            check(seen[0], "a wild penguin goes for the koi");
            check(seen[1], "and the orca goes for the penguin");
        });
    }
}
