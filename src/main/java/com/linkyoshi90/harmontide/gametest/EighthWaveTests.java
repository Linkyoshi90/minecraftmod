package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GirlJarBlock;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.FlamingoHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Marsupial;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The eighth Harmontide wave (HARMONTIDE_CONCEPTS.md): unicorn, black bear, koala, gecko, chupacabra, firefly, mummy, flamingo. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class EighthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "unicornKnowsAPureHeartAndHeals")
    public static void unicornKnowsAPureHeartAndHeals(GameTestHelper helper) {
        ServerPlayer pure = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ServerPlayer killer = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        killer.awardStat(Stats.ENTITY_KILLED.get(EntityType.VILLAGER));
        Centaur unicorn = spawn(helper, GirlSpecies.UNICORN_CENTAUR, new BlockPos(5, 2, 5));
        unicorn.setNoAi(true);
        unicorn.addAffection(killer, 10);
        check(unicorn.getAffection(killer) == 0, "she will take nothing from a heavy heart");
        unicorn.addAffection(pure, Orders.AFFECTION);
        check(unicorn.getAffection(pure) == Orders.AFFECTION, "a pure heart she befriends");
        pure.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        unicorn.tendWithHorn(helper.getLevel());
        check(!pure.hasEffect(MobEffects.POISON), "her horn draws out the poison");
        HarpyTestUtil.remove(pure);
        HarpyTestUtil.remove(killer);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "blackBearRaidsBerriesAndScares")
    public static void blackBearRaidsBerriesAndScares(GameTestHelper helper) {
        BlockPos bush = new BlockPos(6, 2, 5);
        helper.setBlock(new BlockPos(6, 1, 5), Blocks.GRASS_BLOCK);
        helper.setBlock(bush, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3));
        Canid bear = spawn(helper, GirlSpecies.BLACK_BEAR_CANID, new BlockPos(5, 2, 5));
        bear.setNoAi(true);
        bear.raidBerries(helper.getLevel());
        check(helper.getBlockState(bush).getValue(SweetBerryBushBlock.AGE) == 1, "she eats the bush bare");
        Canid.startle(helper.getLevel(), bear.position(), true);
        check(bear.isScared(), "an explosion sends her running");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "scaredBlackBearClimbsATree")
    public static void scaredBlackBearClimbsATree(GameTestHelper helper) {
        for (int y = 2; y <= 8; y++) helper.setBlock(new BlockPos(7, y, 5), Blocks.OAK_LOG);
        Canid bear = spawn(helper, GirlSpecies.BLACK_BEAR_CANID, new BlockPos(5, 2, 5));
        Canid.startle(helper.getLevel(), helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 5)), true);
        helper.succeedWhen(() -> check(helper.relativeVec(bear.position()).y >= 4.0, "scared, she goes up the tree: " + helper.relativeVec(bear.position())));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "koalaSleepsOnATrunk")
    public static void koalaSleepsOnATrunk(GameTestHelper helper) {
        for (int y = 2; y <= 6; y++) helper.setBlock(new BlockPos(7, y, 5), Blocks.OAK_LOG);
        Marsupial koala = spawn(helper, GirlSpecies.KOALA_MARSUPIAL, new BlockPos(5, 2, 5));
        koala.goToSleep(helper.getLevel());
        check(koala.isClinging() && koala.isNoGravity() && koala.getAction() == AbstractMonsterGirl.Action.CLING, "asleep, clinging to the trunk");
        koala.wakeUp();
        check(!koala.isClinging() && !koala.isNoGravity(), "awake, she lets go");
        check(koala.isFood(new ItemStack(Items.OAK_LEAVES)) && !koala.isFood(new ItemStack(Items.APPLE)), "leaves, and nothing else");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "geckoShedsHerTail")
    public static void geckoShedsHerTail(GameTestHelper helper) {
        Lizardfolk gecko = spawn(helper, GirlSpecies.GECKO_LIZARDFOLK, new BlockPos(5, 2, 5));
        gecko.hurt(helper.getLevel().damageSources().generic(), 1.0F);
        check(gecko.hasFlag(AbstractMonsterGirl.FLAG_TAILLESS), "hit, she sheds her tail");
        check(gecko.isFood(new ItemStack(Items.MELON_SLICE)), "she likes fruit");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "chupacabraHuntsOnlyWildGoats")
    public static void chupacabraHuntsOnlyWildGoats(GameTestHelper helper) {
        Goat wild = helper.spawn(EntityType.GOAT, new BlockPos(2, 2, 2));
        Goat named = helper.spawn(EntityType.GOAT, new BlockPos(9, 2, 2));
        named.setCustomName(Component.literal("Bessie"));
        Goat penned = helper.spawn(EntityType.GOAT, new BlockPos(9, 2, 9));
        helper.setBlock(new BlockPos(10, 2, 9), Blocks.OAK_FENCE);
        check(Lizardfolk.wildGoat(wild), "a wild goat is fair game");
        check(!Lizardfolk.wildGoat(named) && !Lizardfolk.wildGoat(penned), "never a named or penned goat");
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Lizardfolk chupa = spawn(helper, GirlSpecies.CHUPACABRA_LIZARDFOLK, new BlockPos(5, 2, 5));
        chupa.setNoAi(true);
        chupa.setAffection(player.getUUID(), Orders.AFFECTION);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        chupa.interact(player, InteractionHand.MAIN_HAND);
        check(!chupa.isOrderedToSit() && chupa.getOrders().rawCommand() == Orders.Command.FREE_ROAM, "a stick means nothing to her");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fireflyInAJar")
    public static void fireflyInAJar(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Bug firefly = spawn(helper, GirlSpecies.FIREFLY_BUG, new BlockPos(5, 2, 5));
        firefly.setNoAi(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        firefly.interact(player, InteractionHand.MAIN_HAND);
        check(firefly.isRemoved() && player.getInventory().items.stream().anyMatch(st -> st.is(ModItems.GIRL_JAR.get())), "a glass bottle catches her in a jar");
        var why = helper.getLevel().getServer().getAdvancements().get(HarmontideMod.id("why"));
        check(why != null && player.getAdvancements().getOrStartProgress(why).isDone(), "why?");
        int before = helper.getLevel().getEntitiesOfClass(Bug.class, new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(8, 2, 8))).inflate(3)).size();
        GirlJarBlock.release(helper.getLevel(), helper.absolutePos(new BlockPos(8, 2, 8)), GirlSpecies.FAIRY_BUG, null);
        check(helper.getLevel().getEntitiesOfClass(Bug.class, new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(8, 2, 8))).inflate(3)).size() == before + 1,
                "opening a jar lets her out");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "mummyUnravelsAndRewraps")
    public static void mummyUnravelsAndRewraps(GameTestHelper helper) {
        Undead mummy = spawn(helper, GirlSpecies.MUMMY_UNDEAD, new BlockPos(5, 2, 5));
        mummy.setNoAi(true);
        mummy.unravel(helper.getLevel(), null);
        mummy.unravel(helper.getLevel(), null);
        check(mummy.unwrapped() == 2, "down to her skimpy wraps");
        mummy.unravel(helper.getLevel(), null);
        check(mummy.unwrapped() == 2, "and never further");
        helper.succeedWhen(() -> check(mummy.unwrapped() == 0, "she spins herself back up in her bandages"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "flamingoFadesAndMarches")
    public static void flamingoFadesAndMarches(GameTestHelper helper) {
        FlamingoHarpy a = helper.spawn(ModEntities.FLAMINGO_HARPY.get(), new BlockPos(4, 2, 4));
        FlamingoHarpy b = helper.spawn(ModEntities.FLAMINGO_HARPY.get(), new BlockPos(6, 2, 4));
        FlamingoHarpy c = helper.spawn(ModEntities.FLAMINGO_HARPY.get(), new BlockPos(5, 2, 6));
        a.fade();
        a.fade();
        check(a.getVariant() == FlamingoHarpy.Variant.WHITE, "without fish she fades to white");
        a.eatFish();
        check(a.getVariant() == FlamingoHarpy.Variant.PALE, "fish bring her pink back a shade");
        check(a.startMarch(helper.getLevel()) && b.getAction() == AbstractHarpy.Action.DISPLAYING && c.getAction() == AbstractHarpy.Action.DISPLAYING,
                "the whole flock marches");
        helper.succeed();
    }
}
