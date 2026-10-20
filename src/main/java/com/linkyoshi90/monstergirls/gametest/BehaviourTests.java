package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/**
 * The 2026-10-20 behaviour changes: hostile hunters calmed with meat, the trapdoor spider's burrow and the orb weaver's
 * hunting web, centaurs carrying riders at high friendship, wearing chestplates and running along with sprinters, cats
 * stalking, born split tails, merfolk true to their kind and finding seashells, slimes bouncing, harpies laying eggs
 * and gulls roosting in hoppers.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class BehaviourTests {

    @SuppressWarnings("unchecked")
    private static <T extends AbstractMonsterGirl> T spawn(GameTestHelper helper, GirlSpecies s, BlockPos pos) {
        return (T) helper.spawn(ModGirls.type(s), pos);
    }

    /** Spawns a mob at an absolute block position (relativePos round trips are unreliable here). */
    private static <T extends net.minecraft.world.entity.Mob> T spawnAt(GameTestHelper helper, EntityType<T> type, BlockPos abs) {
        T mob = type.create(helper.getLevel());
        mob.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0.0F, 0.0F);
        mob.setPersistenceRequired();
        helper.getLevel().addFreshEntity(mob);
        return mob;
    }

    private static void interact(AbstractMonsterGirl girl, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        girl.interact(player, InteractionHand.MAIN_HAND);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hostileGirlsAreCalmedWithMeat")
    public static void hostileGirlsAreCalmedWithMeat(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        boolean peaceful = helper.getLevel().getDifficulty() == Difficulty.PEACEFUL;
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.TRAPDOOR_SPIDER_ARACHNE, GirlSpecies.ORB_WEAVER_ARACHNE, GirlSpecies.TIGER_NEKOMATA, GirlSpecies.SHARK_MERMAID}) {
            AbstractMonsterGirl girl = spawn(helper, s, new BlockPos(8, 2, 8));
            if (s != GirlSpecies.SHARK_MERMAID) check(girl.isHostileTo(player) != peaceful, s + " should be hostile to strangers (unless peaceful)");
            interact(girl, player, new ItemStack(Items.BEEF));
            check(girl.isCalm() && !girl.isHostileTo(player), s + " should be calm after meat");
            check(girl.getAffection(player) == 0, s + ": the first bite only calms her");
            interact(girl, player, new ItemStack(Items.BEEF));
            check(girl.getAffection(player) > 0, s + ": more meat builds friendship");
            CompoundTag tag = new CompoundTag();
            girl.saveWithoutId(tag);
            AbstractMonsterGirl copy = ModGirls.type(s).create(helper.getLevel());
            copy.load(tag);
            check(copy.isCalm(), s + " forgot she was calmed");
            girl.discard();
        }
        AbstractMonsterGirl friendly = spawn(helper, GirlSpecies.JUMPING_SPIDER_ARACHNE, new BlockPos(5, 2, 5));
        check(!friendly.isHostileTo(player), "the jumping spider was never hostile");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "trapdoorSpiderAmbushesFromHerBurrow")
    public static void trapdoorSpiderAmbushesFromHerBurrow(GameTestHelper helper) {
        for (int x = 5; x <= 9; x++) for (int z = 5; z <= 9; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
        Arachne spider = spawn(helper, GirlSpecies.TRAPDOOR_SPIDER_ARACHNE, new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> {
            check(spider.isInBurrow() && spider.isInvisible(), "she should dig in and hide");
            BlockPos hole = spider.getBurrow();
            check(hole != null && helper.getLevel().getBlockState(hole).isAir(), "the hole is dug out");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "trapdoorSpiderBurstsOutAtPrey")
    public static void trapdoorSpiderBurstsOutAtPrey(GameTestHelper helper) {
        for (int x = 5; x <= 9; x++) for (int z = 5; z <= 9; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
        Arachne spider = spawn(helper, GirlSpecies.TRAPDOOR_SPIDER_ARACHNE, new BlockPos(7, 2, 7));
        Chicken[] prey = new Chicken[1];
        helper.onEachTick(() -> {
            if (prey[0] == null && spider.isInBurrow()) {
                prey[0] = spawnAt(helper, EntityType.CHICKEN, spider.getBurrow().above().east());
                prey[0].setNoAi(true);
            }
        });
        helper.succeedWhen(() -> {
            check(prey[0] != null, "she should hide first");
            check(!spider.isInBurrow() && (spider.getTarget() == prey[0] || !prey[0].isAlive()), "she should burst out at the chicken");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "orbWeaverSpinsAHuntingWeb")
    public static void orbWeaverSpinsAHuntingWeb(GameTestHelper helper) {
        Arachne spider = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(7, 2, 7));
        Rabbit[] caught = new Rabbit[1];
        helper.onEachTick(() -> {
            if (caught[0] == null && spider.getTrapWebs().size() >= 3) {
                caught[0] = spawnAt(helper, EntityType.RABBIT, spider.getTrapWebs().get(0));
                caught[0].setNoAi(true);
            }
        });
        helper.succeedWhen(() -> {
            check(spider.getTrapWebs().size() >= 3, "she should string at least three webs: " + spider.getTrapWebs().size());
            for (BlockPos w : spider.getTrapWebs()) check(helper.getLevel().getBlockState(w).is(Blocks.COBWEB), "a web is missing");
            check(caught[0] != null && (spider.getTarget() == caught[0] || !caught[0].isAlive()), "she should go for what her web caught");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "orbWeaverClearsHerWebWhenCalmed")
    public static void orbWeaverClearsHerWebWhenCalmed(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Arachne spider = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(7, 2, 7));
        BlockPos web = helper.absolutePos(new BlockPos(9, 2, 7));
        helper.setBlock(new BlockPos(9, 2, 7), Blocks.COBWEB);
        spider.getTrapWebs().add(web);
        interact(spider, player, new ItemStack(Items.CHICKEN));
        check(spider.getTrapWebs().isEmpty() && !helper.getLevel().getBlockState(web).is(Blocks.COBWEB), "a calm orb weaver takes her hunting web down");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "centaurCarriesAFriendBareback")
    public static void centaurCarriesAFriendBareback(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Centaur zebra = spawn(helper, GirlSpecies.ZEBRA_CENTAUR, new BlockPos(7, 2, 7));
        zebra.setAffection(player.getUUID(), Centaur.RIDE_AFFECTION - 1);
        interact(zebra, player, ItemStack.EMPTY);
        check(player.getVehicle() != zebra, "not yet: she needs high friendship");
        zebra.setAffection(player.getUUID(), Centaur.RIDE_AFFECTION);
        interact(zebra, player, ItemStack.EMPTY);
        check(player.getVehicle() == zebra, "at high friendship she carries you, no saddle needed (even the zebra)");
        check(zebra.getControllingPassenger() == player, "and you steer");
        player.stopRiding();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "centaurWearsAChestplate")
    public static void centaurWearsAChestplate(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Centaur horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(7, 2, 7));
        float base = horse.getMaxHealth();
        horse.setAffection(player.getUUID(), Centaur.ARMOUR_AFFECTION);
        interact(horse, player, new ItemStack(Items.IRON_CHESTPLATE));
        check(horse.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE), "she should wear the chestplate");
        check(Math.abs(horse.getMaxHealth() - (base + 12)) < 0.01, "iron adds 12 health: " + horse.getMaxHealth());
        horse.setHealth(horse.getMaxHealth());
        CompoundTag tag = new CompoundTag();
        horse.saveWithoutId(tag);
        Centaur copy = (Centaur) ModGirls.type(GirlSpecies.HORSE_CENTAUR).create(helper.getLevel());
        copy.load(tag);
        check(Math.abs(copy.getMaxHealth() - (base + 12)) < 0.01 && Math.abs(copy.getHealth() - (base + 12)) < 0.01, "kept after loading: " + copy.getHealth());
        helper.runAfterDelay(5, () -> {
        check(horse.getArmorValue() >= 6, "and its armour: " + horse.getArmorValue());
        player.setShiftKeyDown(true);
        interact(horse, player, ItemStack.EMPTY);
        check(horse.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && Math.abs(horse.getMaxHealth() - base) < 0.01, "crouch-click takes it off again");
        check(player.getInventory().countItem(Items.IRON_CHESTPLATE) == 1, "and gives it back");
        HarpyTestUtil.remove(player);
        helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "centaurRunsAlongWithASprinter")
    public static void centaurRunsAlongWithASprinter(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 7));
        Centaur horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(11, 2, 7));
        helper.onEachTick(() -> player.setSprinting(true));
        helper.succeedWhen(() -> {
            check(horse.isRunningAlong() && horse.hasFlag(AbstractMonsterGirl.FLAG_SMILE), "she should join the run, grinning");
            check(horse.distanceToSqr(player) < 4 * 4, "beside the player: " + Math.sqrt(horse.distanceToSqr(player)));
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "nekomataStalksHerPrey")
    public static void nekomataStalksHerPrey(GameTestHelper helper) {
        Nekomata cat = spawn(helper, GirlSpecies.LYNX_NEKOMATA, new BlockPos(2, 2, 2));
        Rabbit rabbit = helper.spawn(EntityType.RABBIT, new BlockPos(12, 2, 12));
        rabbit.setNoAi(true);
        boolean[] stalked = {false};
        helper.onEachTick(() -> {
            if (cat.getTarget() == null && rabbit.isAlive()) cat.setTarget(rabbit);
            if (cat.getAction() == AbstractMonsterGirl.Action.STALK) stalked[0] = true;
        });
        helper.succeedWhen(() -> check(stalked[0] && (!rabbit.isAlive() || cat.distanceToSqr(rabbit) < 9), "she should creep up on the rabbit"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "splitTailsAreBornAndKept")
    public static void splitTailsAreBornAndKept(GameTestHelper helper) {
        Kitsune fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 5));
        fox.setTails(3);
        Nekomata cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(9, 2, 9));
        cat.setTails(2);
        helper.runAfterDelay(30, () -> {
            check(fox.visibleTails() == 3, "an old fox spirit shows her tails without any bond: " + fox.visibleTails());
            CompoundTag tag = new CompoundTag();
            cat.saveWithoutId(tag);
            AbstractMonsterGirl copy = ModGirls.type(GirlSpecies.SERVAL_NEKOMATA).create(helper.getLevel());
            copy.load(tag);
            check(copy.getTails() == 2, "a born nekomata keeps her split tail");
            int split = 0;
            for (int i = 0; i < 400; i++) {
                Kitsune k = (Kitsune) ModGirls.type(GirlSpecies.RED_KITSUNE).create(helper.getLevel());
                k.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(helper.absolutePos(BlockPos.ZERO)),
                        net.minecraft.world.entity.MobSpawnType.NATURAL, null);
                if (k.getTails() > 1) split++;
            }
            check(split > 40 && split < 200, "about one wild kitsune in four has split tails: " + split + "/400");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 900, batch = "slimeBouncesOnASlimeBlock")
    public static void slimeBouncesOnASlimeBlock(GameTestHelper helper) {
        helper.setBlock(new BlockPos(9, 1, 9), Blocks.SLIME_BLOCK);
        AbstractMonsterGirl slime = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(5, 2, 5));
        helper.succeedWhen(() -> check(slime.getAction() == AbstractMonsterGirl.Action.BOUNCE, "she should be bouncing on it"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1000, batch = "mermaidFindsSeashells")
    public static void mermaidFindsSeashells(GameTestHelper helper) {
        for (int x = 4; x <= 10; x++) for (int z = 4; z <= 10; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.SAND);
            helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
            helper.setBlock(new BlockPos(x, 3, z), Blocks.WATER);
        }
        Merfolk orca = spawn(helper, GirlSpecies.ORCA_MERMAID, new BlockPos(7, 2, 7));
        orca.setShellChance(1);
        helper.succeedWhen(() -> {
            check(orca.hasShells(), "she should find a pair of seashells");
            CompoundTag tag = new CompoundTag();
            orca.saveWithoutId(tag);
            AbstractMonsterGirl copy = ModGirls.type(GirlSpecies.ORCA_MERMAID).create(helper.getLevel());
            copy.load(tag);
            check(copy.hasFlag(AbstractMonsterGirl.FLAG_SHELLS), "and keep wearing them");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "sharkGoesForAnythingInTheWater")
    public static void sharkGoesForAnythingInTheWater(GameTestHelper helper) {
        for (int x = 2; x <= 12; x++) for (int z = 2; z <= 12; z++) {
            helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
            helper.setBlock(new BlockPos(x, 3, z), Blocks.WATER);
        }
        Merfolk shark = spawn(helper, GirlSpecies.SHARK_MERMAID, new BlockPos(4, 2, 4));
        Squid squid = helper.spawn(EntityType.SQUID, new BlockPos(9, 2, 9));
        helper.succeedWhen(() -> check(shark.getTarget() == squid || !squid.isAlive(), "the shark should go after the squid"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "harpiesLayUnfertilisedEggs")
    public static void harpiesLayUnfertilisedEggs(GameTestHelper helper) {
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(7, 2, 7));
        owl.setEggTime(1);
        helper.succeedWhen(() -> check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(8),
                e -> e.getItem().is(ModItems.OWL_EGG.get())).isEmpty(), "the owl should lay an egg"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "gullRoostsInAHopperAndShedsFlint")
    public static void gullRoostsInAHopperAndShedsFlint(GameTestHelper helper) {
        BlockPos hopper = new BlockPos(7, 2, 7);
        helper.setBlock(hopper, Blocks.HOPPER);
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(4, 2, 4));
        helper.onEachTick(() -> {
            if (gull.isRoosting()) gull.setRoostDropTime(1);
        });
        helper.succeedWhen(() -> {
            check(helper.absolutePos(hopper).equals(gull.getRoost()), "she should claim the hopper");
            check(helper.getBlockEntity(hopper) instanceof HopperBlockEntity h && (h.countItem(Items.FLINT) + h.countItem(Items.FEATHER)) > 0,
                    "flint or a feather should be in the hopper");
        });
    }
}
