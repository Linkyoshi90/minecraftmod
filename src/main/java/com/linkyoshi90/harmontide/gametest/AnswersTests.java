package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.Flora;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kinship;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import com.linkyoshi90.harmontide.item.CocoonItem;
import com.linkyoshi90.harmontide.item.NectarItem;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The answers to the field guides' open questions (TODO.md section 0f). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class AnswersTests {

    private static long count(GameTestHelper helper, AbstractMonsterGirl near, net.minecraft.world.item.Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, near.getBoundingBox().inflate(4), e -> e.getItem().is(item)).size();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kittensOnALeadZebraForHerFather")
    public static void kittensOnALeadZebraForHerFather(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Nekomata kitten = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        kitten.setBaby(true);
        check(kitten.canBeLeashed(), "a kitten can be put on a lead");
        Centaur zebra = spawn(helper, GirlSpecies.ZEBRA_CENTAUR, new BlockPos(9, 2, 9));
        zebra.setAffection(player.getUUID(), Orders.AFFECTION * 10);
        check(!zebra.mayCarry(player), "a zebra carries no one but her father");
        zebra.setFather(player.getUUID());
        check(zebra.mayCarry(player), "her father may ride");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "youngMerfolkFitInABucket")
    public static void youngMerfolkFitInABucket(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Merfolk calf = spawn(helper, GirlSpecies.ORCA_MERMAID, new BlockPos(5, 2, 5));
        calf.setBaby(true);
        calf.setAffection(player.getUUID(), 42);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        calf.interact(player, InteractionHand.MAIN_HAND);
        ItemStack bucket = player.getMainHandItem();
        check(bucket.is(ModItems.MERFOLK_BUCKET.get()) && calf.isRemoved(), "she goes into the bucket: " + bucket);
        ModItems.MERFOLK_BUCKET.get().checkExtraContent(player, helper.getLevel(), bucket, helper.absolutePos(new BlockPos(9, 2, 9)));
        var out = helper.getLevel().getEntitiesOfClass(Merfolk.class, new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(9, 2, 9))).inflate(2));
        check(out.size() == 1 && out.get(0).getSpecies() == GirlSpecies.ORCA_MERMAID && out.get(0).isBaby(), "and comes out the same young orca");
        check(out.get(0).getAffection(player) == 42, "remembering you");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "merfolkShareBreathAndTrade")
    public static void merfolkShareBreathAndTrade(GameTestHelper helper) {
        for (int x = 3; x <= 9; x++) for (int z = 3; z <= 9; z++) for (int y = 2; y <= 4; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6));
        Merfolk friend = spawn(helper, GirlSpecies.SEAL_MERMAID, new BlockPos(7, 2, 7));
        friend.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        Merfolk merchant = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(4, 2, 4));
        merchant.setMerchant(true);
        helper.runAfterDelay(2, () -> {
            player.setAirSupply(20);
            friend.breathe(helper.getLevel());
            check(player.getAirSupply() == player.getMaxAirSupply(), "she gives you her breath: " + player.getAirSupply());
            check(friend.needsAir(), "and has to surface herself");
            check(!merchant.isHostileTo(player), "a merchant is neutral");
            var offers = merchant.getOffers();
            check(offers.size() >= 3, "she trades sea loot: " + offers.size());
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cocoonOpensNectarWaters")
    public static void cocoonOpensNectarWaters(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        player.setItemInHand(InteractionHand.MAIN_HAND, CocoonItem.wrapping(new ItemStack(Items.DIAMOND)));
        ModItems.SILK_COCOON.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(player.getInventory().contains(new ItemStack(Items.DIAMOND)), "tear it open: the present is yours");
        BlockPos farm = new BlockPos(8, 1, 8);
        helper.setBlock(farm, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
        helper.setBlock(farm.above(), Blocks.WHEAT);
        int touched = NectarItem.pour(helper.getLevel(), helper.absolutePos(farm));
        BlockState soil = helper.getBlockState(farm);
        check(touched > 0 && soil.getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE, "nectar soaks the farmland");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "devilRaidsChestsSlimeSpillsMothEatsWool")
    public static void devilRaidsChestsSlimeSpillsMothEatsWool(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(3, 2, 3);
        helper.setBlock(chestPos, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(chestPos)).setItem(0, new ItemStack(Items.GOLD_INGOT, 5));
        Devil imp = spawn(helper, GirlSpecies.LESSER_DEVIL, new BlockPos(5, 2, 5));
        check(imp.raidChest(helper.getLevel()) && imp.getLoot().is(Items.GOLD_INGOT), "she lifts one thing from the chest");
        SlimeGirl slime = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(10, 2, 4));
        slime.getBelly().setItem(0, new ItemStack(Items.EMERALD, 3));
        slime.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        check(slime.isAlive() && slime.getBelly().isEmpty() && count(helper, slime, Items.EMERALD) > 0, "splitting knocks her belly out");
        Bug moth = spawn(helper, GirlSpecies.MOTH_BUG, new BlockPos(10, 2, 10));
        helper.spawnItem(Items.WHITE_WOOL, new BlockPos(10, 2, 10));
        helper.succeedWhen(() -> check(count(helper, moth, Items.WHITE_WOOL) == 0, "the moth eats the wool"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wyrmFireLightsTheGround")
    public static void wyrmFireLightsTheGround(GameTestHelper helper) {
        Dragon wyrm = spawn(helper, GirlSpecies.WYRM_DRAGON, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 9));
        wyrm.breatheFire(helper.getLevel(), z);
        boolean fire = false;
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(7, 2, 7), new BlockPos(11, 3, 11))) fire |= helper.getBlockState(p).is(Blocks.FIRE);
        check(fire, "fire on the ground around her target");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "beesPollinateFlowersIntoAlraunes")
    public static void beesPollinateFlowersIntoAlraunes(GameTestHelper helper) {
        Flora alraune = spawn(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(3, 2, 3));
        Bug bee = spawn(helper, GirlSpecies.BEE_BUG, new BlockPos(4, 2, 3));
        bee.pollinate(helper.getLevel());
        check(bee.carriesFloraNectar() && !alraune.hasNectar(), "the bee takes the alraune's nectar");
        helper.setBlock(new BlockPos(11, 2, 11), Blocks.POPPY);
        bee.teleportTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(11.5, 2, 10.5)).x, helper.absoluteVec(new net.minecraft.world.phys.Vec3(11.5, 2, 10.5)).y,
                helper.absoluteVec(new net.minecraft.world.phys.Vec3(11.5, 2, 10.5)).z);
        for (int i = 0; i < 400 && bee.carriesFloraNectar(); i++) bee.pollinate(helper.getLevel());
        check(!helper.getBlockState(new BlockPos(11, 2, 11)).is(Blocks.POPPY), "the flower became an alraune");
        check(helper.getLevel().getEntitiesOfClass(Flora.class, bee.getBoundingBox().inflate(4), Flora::isBaby).size() == 1, "a young one");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "befriendedZombieTakesOrders")
    public static void befriendedZombieTakesOrders(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Undead girl = spawn(helper, GirlSpecies.ZOMBIE_UNDEAD, new BlockPos(7, 2, 7));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 7));
        Kinship.befriend(helper.getLevel(), zombie, player, girl);
        check(Kinship.orders(zombie).rawCommand() == Orders.Command.FOLLOW, "a new friend follows you");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        player.interactOn(zombie, InteractionHand.MAIN_HAND);
        check(Kinship.orders(zombie).rawCommand() == Orders.Command.FREE_ROAM, "a stick: roam freely");
        player.interactOn(zombie, InteractionHand.MAIN_HAND);
        check(Kinship.orders(zombie).rawCommand() == Orders.Command.SIT, "again: sit");
        player.setShiftKeyDown(true);
        player.interactOn(zombie, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        check(Kinship.orders(zombie).stance() == Orders.Stance.HOSTILE, "sneaking: a stance");
        check(zombie.getPersistentData().contains("harmontide:kin_orders"), "and the orders are saved with it");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "dragonKoiFloatsOverLand")
    public static void dragonKoiFloatsOverLand(GameTestHelper helper) {
        Merfolk koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(7, 2, 7));
        koi.setFlag(AbstractMonsterGirl.FLAG_TRUE_FORM, true);
        double ground = helper.absoluteVec(new net.minecraft.world.phys.Vec3(0, 2, 0)).y;
        helper.succeedWhen(() -> check(koi.isLevitating() && koi.isNoGravity() && koi.getY() > ground + 0.6,
                "the dragon koi floats above the ground: " + (koi.getY() - ground)));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bullywugOutfitComesOff")
    public static void bullywugOutfitComesOff(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        var wug = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.BULLYWUG.get(), new BlockPos(5, 2, 5));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        wug.interact(player, InteractionHand.MAIN_HAND);
        check(!wug.isUndressed(), "not for a stranger");
        wug.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        wug.interact(player, InteractionHand.MAIN_HAND);
        check(wug.isUndressed(), "at high friendship she takes off the skirt and cattails");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LILY_PAD));
        wug.interact(player, InteractionHand.MAIN_HAND);
        check(!wug.isUndressed() && player.getMainHandItem().isEmpty(), "a lily pad: a new skirt");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "alrauneRootsAsABloom")
    public static void alrauneRootsAsABloom(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Flora alraune = spawn(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(4, 2, 4));
        alraune.setAffection(player.getUUID(), 33);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SHOVEL));
        player.setShiftKeyDown(true);
        alraune.interact(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        int slot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (player.getInventory().getItem(i).is(ModItems.ALRAUNE_BLOOM.get())) slot = i;
        check(alraune.isRemoved() && slot >= 0, "dug up, she is a bloom in your bag");
        ItemStack bloom = player.getInventory().getItem(slot);
        var data = bloom.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        check(data != null && data.copyTag().contains("Girl"), "carrying all of her");
        BlockPos at = new BlockPos(10, 2, 10);
        helper.setBlock(at, com.linkyoshi90.harmontide.registry.ModBlocks.ALRAUNE_BLOOM.get());
        ((com.linkyoshi90.harmontide.block.AlrauneBloomBlockEntity) helper.getBlockEntity(at)).setGirl(data.copyTag().getCompound("Girl"));
        helper.destroyBlock(at);
        var out = helper.getLevel().getEntitiesOfClass(Flora.class, new net.minecraft.world.phys.AABB(helper.absolutePos(at)).inflate(2));
        check(out.size() == 1 && out.get(0).getAffection(player) == 33, "planted and picked, she steps out remembering you");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lamiaCarriesHerFriend")
    public static void lamiaCarriesHerFriend(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        var lamia = (com.linkyoshi90.harmontide.entity.girl.Lamia) spawn(helper, GirlSpecies.PYTHON_LAMIA, new BlockPos(5, 2, 5));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        lamia.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() != lamia, "not for a stranger");
        lamia.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        lamia.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == lamia && lamia.getControllingPassenger() == player, "at high friendship she carries you in her coils");
        check(!lamia.onSand(), "grass is not sand (a slow ride)");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    /** The crash report of 2026-10-22: a nest goal ticked after losing its spot. */
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nestGoalSurvivesALostSpot")
    public static void nestGoalSurvivesALostSpot(GameTestHelper helper) {
        var crow = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.CROW_HARPY.get(), new BlockPos(5, 2, 5));
        new com.linkyoshi90.harmontide.entity.ai.BuildNestGoal(crow).tick();          // no spot yet: must not throw
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "dollSitsOnTheBookshelf")
    public static void dollSitsOnTheBookshelf(GameTestHelper helper) {
        BlockPos shelf = new BlockPos(9, 2, 9);
        helper.setBlock(shelf, Blocks.BOOKSHELF);
        Undead doll = spawn(helper, GirlSpecies.LIVING_DOLL_UNDEAD, new BlockPos(5, 2, 5));
        helper.succeedWhen(() -> {
            check(doll.blockPosition().equals(helper.absolutePos(shelf.above())), "the doll perches on top of the bookshelf: " + helper.relativePos(doll.blockPosition()));
            check(doll.getAction() == AbstractMonsterGirl.Action.SITTING, "and sits there");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nectarFluidWatersAndFeedsAField")
    public static void nectarFluidWatersAndFeedsAField(GameTestHelper helper) {
        BlockPos soil = new BlockPos(5, 1, 5), crop = soil.above(), pool = new BlockPos(7, 1, 5);
        helper.setBlock(soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
        helper.setBlock(crop, Blocks.WHEAT);
        helper.setBlock(pool, com.linkyoshi90.harmontide.registry.ModBlocks.NECTAR.get());
        var level = helper.getLevel();
        level.getBlockState(helper.absolutePos(soil)).randomTick(level, helper.absolutePos(soil), level.random);
        check(helper.getBlockState(soil).getValue(FarmBlock.MOISTURE) == FarmBlock.MAX_MOISTURE, "nectar keeps farmland wet, like water");
        check(com.linkyoshi90.harmontide.registry.ModFluids.nectarNear(level, helper.absolutePos(crop)), "the crop is in its reach");
        boolean grew = false;
        for (int i = 0; i < 40 && !grew; i++) {
            var e = net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.block.CropGrowEvent.Pre(level, helper.absolutePos(crop),
                    helper.getBlockState(crop)));
            grew = e.getResult() == net.neoforged.neoforge.event.level.block.CropGrowEvent.Pre.Result.GROW;
        }
        check(grew, "and its crops get growth spurts");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "slimesMergeAndSeparate")
    public static void slimesMergeAndSeparate(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        SlimeGirl honey = spawn(helper, GirlSpecies.HONEY_SLIME, new BlockPos(6, 2, 6));
        SlimeGirl frost = spawn(helper, GirlSpecies.FROST_SLIME, new BlockPos(7, 2, 6));
        honey.setAffection(player.getUUID(), 40);
        frost.setAffection(player.getUUID(), 30);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SLIME_BALL));
        honey.interact(player, InteractionHand.MAIN_HAND);
        var mixed = helper.getLevel().getEntitiesOfClass(SlimeGirl.class, honey.getBoundingBox().inflate(4), SlimeGirl::isMixed);
        check(honey.isRemoved() && frost.isRemoved() && mixed.size() == 1, "they flow together into one");
        SlimeGirl m = mixed.get(0);
        check("honey_frost".equals(m.getVariantName()) && m.partCount() == 2 && m.getAffection(player) == 40, "marbled honey and frost, carrying both");
        check(m.isFood(new ItemStack(Items.HONEY_BOTTLE)) && m.isFood(new ItemStack(Items.SNOWBALL)), "she likes what both of them liked");
        m.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        var back = helper.getLevel().getEntitiesOfClass(SlimeGirl.class, m.getBoundingBox().inflate(4), g -> !g.isMixed() && g.isAlive());
        check(m.isRemoved() && back.size() == 2, "a killing blow separates them again: " + back.size());
        check(back.stream().anyMatch(g -> g.getSpecies() == GirlSpecies.HONEY_SLIME && g.getAffection(player) == 40)
                && back.stream().anyMatch(g -> g.getSpecies() == GirlSpecies.FROST_SLIME && g.getAffection(player) == 30), "each remembering you as she did");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "slimeColoursMixLikeLight")
    public static void slimeColoursMixLikeLight(GameTestHelper helper) {
        int magenta = SlimeGirl.blend(0xFF0000, 0x0000FF);
        int r = magenta >> 16 & 255, g = magenta >> 8 & 255, b = magenta & 255;
        check(r > 200 && b > 150 && g < 80, "red and blue make a vivid magenta, not a muddy purple: " + Integer.toHexString(magenta));
        int yellow = SlimeGirl.blend(0xFF0000, 0x00FF00);
        check((yellow >> 16 & 255) > 200 && (yellow >> 8 & 255) > 200 && (yellow & 255) < 80, "red and green make yellow: " + Integer.toHexString(yellow));
        int c = 0xFF0000;
        for (int k = 0; k < 6; k++) c = SlimeGirl.blend(c, 0x0000FF);
        int lo = Math.min(c >> 16 & 255, Math.min(c >> 8 & 255, c & 255));
        check(lo < 90, "merging again and again never washes out to white: " + Integer.toHexString(c));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        SlimeGirl slime = spawn(helper, GirlSpecies.FROST_SLIME, new BlockPos(6, 2, 6));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RED_DYE));
        slime.interact(player, InteractionHand.MAIN_HAND);
        check(slime.hasCustomColour(), "a dye gives her a colour of her own");
        player.setItemInHand(InteractionHand.MAIN_HAND, net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
        slime.interact(player, InteractionHand.MAIN_HAND);
        check(!slime.hasCustomColour(), "a water bottle washes it out");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ordersOutrankFollowing")
    public static void ordersOutrankFollowing(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        var owl = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.OWL_HARPY.get(), new BlockPos(13, 2, 13));
        owl.setAffection(player.getUUID(), 50);
        var follow = new com.linkyoshi90.harmontide.entity.ai.FollowLeaderGoal(owl, () -> player, 1.2, 10.0F, 3.0F);
        check(follow.canUse(), "free, she follows her favourite");
        owl.getOrders().set(Orders.Command.PATROL, player, owl.blockPosition());
        check(!follow.canUse() && Orders.holdsPlace(owl), "told to patrol, she stays");
        owl.getOrders().set(Orders.Command.FREE_ROAM, player, null);
        owl.setOrderedToSit(true);
        check(!follow.canUse(), "told to sit, she stays");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "balmsAndHealers")
    public static void balmsAndHealers(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 5));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.ZEBRA_CENTAUR, new BlockPos(9, 2, 5));
        friend.setAffection(player.getUUID(), Orders.AFFECTION);
        friend.setHealth(5.0F);
        stranger.setHealth(5.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.HEALING_BALM.get(), 2));
        player.interactOn(stranger, InteractionHand.MAIN_HAND);
        check(stranger.getHealth() == 5.0F && player.getMainHandItem().getCount() == 2, "a wild girl will not let you");
        player.interactOn(friend, InteractionHand.MAIN_HAND);
        check(friend.getHealth() >= 15.0F && player.getMainHandItem().getCount() == 1, "a balm heals your friend: " + friend.getHealth());
        Flora healer = spawn(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(6, 2, 7));
        healer.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        friend.setHealth(5.0F);
        check(healer.isHealer(), "an alraune is a healer");
        healer.tendCompanions(helper.getLevel());
        check(friend.getHealth() >= 9.0F, "and tends your hurt companions: " + friend.getHealth());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "farmhandHarvestsReplantsStores")
    public static void farmhandHarvestsReplantsStores(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos soil = new BlockPos(9, 1, 7), crop = soil.above(), chest = new BlockPos(5, 2, 9);
        helper.setBlock(soil, Blocks.FARMLAND);
        helper.setBlock(crop, ((net.minecraft.world.level.block.CropBlock) Blocks.WHEAT).getStateForAge(7));
        helper.setBlock(chest, Blocks.CHEST);
        AbstractMonsterGirl hand = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(6, 2, 6));
        hand.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        hand.getOrders().set(Orders.Command.PATROL, player, hand.blockPosition());
        helper.succeedWhen(() -> {
            BlockState s = helper.getBlockState(crop);
            check(s.is(Blocks.WHEAT) && ((net.minecraft.world.level.block.CropBlock) Blocks.WHEAT).getAge(s) == 0, "she harvests and replants the wheat");
            var box = (ChestBlockEntity) helper.getBlockEntity(chest);
            boolean stored = false;
            for (int i = 0; i < box.getContainerSize(); i++) stored |= box.getItem(i).is(Items.WHEAT);
            check(stored, "and puts the harvest in the chest");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cowbellRingsWhenMilkIsReady")
    public static void cowbellRingsWhenMilkIsReady(GameTestHelper helper) {
        var cow = (com.linkyoshi90.harmontide.entity.girl.Minotaur) spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(6, 2, 6));
        cow.setNextMilk(helper.getLevel().getGameTime() + 5);
        check(!cow.isMilkReady(), "just milked");
        helper.runAfterDelay(15, () -> {
            check(cow.isMilkReady(), "ready again (her bell has rung)");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "harpiesFarmTooAndTheCharm")
    public static void harpiesFarmTooAndTheCharm(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos soil = new BlockPos(9, 1, 7), crop = soil.above();
        helper.setBlock(soil, Blocks.FARMLAND);
        helper.setBlock(crop, ((net.minecraft.world.level.block.CropBlock) Blocks.CARROTS).getStateForAge(7));
        var owl = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.OWL_HARPY.get(), new BlockPos(6, 2, 6));
        owl.setAffection(player.getUUID(), Orders.AFFECTION);                     // fond is enough, the same as for patrolling
        owl.getOrders().set(Orders.Command.PATROL, player, owl.blockPosition());
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(12, 2, 12));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FRIENDSHIP_CHARM.get()));
        player.interactOn(girl, InteractionHand.MAIN_HAND);
        check(girl.getAffection(player) == AbstractMonsterGirl.FULL_AFFECTION, "the charm fills her friendship");
        helper.succeedWhen(() -> {
            BlockState s = helper.getBlockState(crop);
            check(s.is(Blocks.CARROTS) && ((net.minecraft.world.level.block.CropBlock) Blocks.CARROTS).getAge(s) == 0, "a patrolling harpy harvests and replants too");
            HarpyTestUtil.remove(player);
        });
    }
}
