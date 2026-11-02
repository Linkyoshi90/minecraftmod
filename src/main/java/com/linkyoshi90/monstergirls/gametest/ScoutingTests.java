package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.Scouting;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The scout pack. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ScoutingTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "friendshipBuysMoreLoot")
    public static void friendshipBuysMoreLoot(GameTestHelper helper) {
        check(Scouting.rolls(100, 0) > Scouting.rolls(10, 0), "friendship");
        check(Scouting.rolls(50, 1) > Scouting.rolls(50, 0) && Scouting.rolls(50, 2) > Scouting.rolls(50, 1), "missing you");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "findsComeFromTheBiome")
    public static void findsComeFromTheBiome(GameTestHelper helper) {
        RandomSource r = helper.getLevel().getRandom();
        List<ItemStack> sea = Scouting.finds(Biomes.DEEP_OCEAN, 40, 100, r);
        check(sea.stream().anyMatch(s -> s.is(Items.KELP) || s.is(Items.PRISMARINE_SHARD)) && sea.stream().noneMatch(s -> s.is(Items.SAND) || s.is(Items.CACTUS)), "sea things");
        List<ItemStack> sand = Scouting.finds(Biomes.DESERT, 40, 100, r);
        check(sand.stream().anyMatch(s -> s.is(Items.CACTUS) || s.is(Items.SAND)) && sand.stream().noneMatch(s -> s.is(Items.KELP)), "desert things");
        check(Scouting.finds(Biomes.CHERRY_GROVE, 40, 100, r).stream().noneMatch(s -> s.is(Items.SNOWBALL)), "a cherry grove is not a snowy one");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "scoutGoesToHerOwnBiome")
    public static void scoutGoesToHerOwnBiome(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.BELUGA_MERMAID, new BlockPos(2, 2, 2));
        String biome = Scouting.homeBiome(helper.getLevel(), girl).location().getPath();
        check(biome.contains("ocean") || biome.contains("river") || biome.contains("frozen") || biome.contains("glacier") || biome.contains("ice") || biome.contains("arctic") || biome.contains("sea"), "a cold sea: " + biome);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "packGoesOutAndComesBackFull")
    public static void packGoesOutAndComesBackFull(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 2));
        girl.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        ItemStack pack = new ItemStack(ModItems.SCOUT_PACK.get());
        check(!Scouting.send(girl, p, pack), "strangers don't go");
        girl.addAffection(p, Orders.AFFECTION);
        check(Scouting.send(girl, p, pack) && girl.isRemoved() && pack.isEmpty() && Scouting.away(p) == 1, "off she goes");
        check(Scouting.returnDue(p, false).isEmpty(), "not back yet");
        List<Mob> back = Scouting.returnDue(p, true);
        check(back.size() == 1 && Scouting.away(p) == 0, "back");
        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, back.get(0).getBoundingBox().inflate(3), i -> i.getItem().is(ModItems.SCOUT_PACK.get()));
        check(dropped.size() == 1 && Scouting.hasFinds(dropped.get(0).getItem()), "a full pack");
        ItemStack full = dropped.get(0).getItem();
        check(!Scouting.send(back.get(0), p, full), "a full pack has to be emptied first");
        check(ModItems.SCOUT_PACK.get().use(helper.getLevel(), withItem(p, full), net.minecraft.world.InteractionHand.MAIN_HAND).getObject().get(DataComponents.CONTAINER) == null, "unpacked");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    private static ServerPlayer withItem(ServerPlayer p, ItemStack stack) {
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        return p;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "harpiesScoutToo")
    public static void harpiesScoutToo(GameTestHelper helper) {
        var owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(2, 2, 2));
        owl.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        owl.addAffection(p, Orders.AFFECTION);
        check(Scouting.send(owl, p, new ItemStack(ModItems.SCOUT_PACK.get())), "she goes");
        check(Scouting.returnDue(p, true).size() == 1, "she comes back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
