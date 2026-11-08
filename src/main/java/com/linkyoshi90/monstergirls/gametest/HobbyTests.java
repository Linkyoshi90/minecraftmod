package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Hobbies;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Hobbies (entity/Hobbies). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HobbyTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "herHobbyAndHour")
    public static void herHobbyAndHour(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 1, 2));
        check(Hobbies.hobby(girl) == Hobbies.hobby(girl), "always the same hobby");
        int start = Hobbies.hourStart(girl);
        check(start >= 1000 && start <= 9000, "her hour is in the daytime");
        check(Hobbies.herHour(girl, start + 10) && Hobbies.herHour(girl, 24000L * 5 + start + 10), "it is her hour, every day");
        check(!Hobbies.herHour(girl, start - 1) && !Hobbies.herHour(girl, start + Hobbies.HOUR), "and only then");
        Hobbies.setHobby(girl, Hobbies.Hobby.KNITTING);
        check(Hobbies.hobby(girl) == Hobbies.Hobby.KNITTING, "a hobby can be set");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "sheBakesForAFriend")
    public static void sheBakesForAFriend(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 1, 2));
        Hobbies.setHobby(girl, Hobbies.Hobby.BAKING);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 1, 12));
        girl.addAffection(p, HomeLife.FRIEND);
        for (int i = 0; i < Hobbies.WORK / 20; i++) Hobbies.practise(level, girl, 20);
        check(Hobbies.doneToday(level, girl), "a day's baking done");
        var drops = level.getEntitiesOfClass(ItemEntity.class, girl.getBoundingBox().inflate(3));
        check(drops.stream().anyMatch(d -> d.getItem().is(net.minecraft.world.item.Items.BREAD) || d.getItem().is(net.minecraft.world.item.Items.COOKIE)
                || d.getItem().is(net.minecraft.world.item.Items.PUMPKIN_PIE) || d.getItem().is(net.minecraft.world.item.Items.CAKE)), "something baked at her feet");
        int work = Hobbies.work(girl);
        Hobbies.practise(level, girl, 20);
        check(Hobbies.work(girl) == work, "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "keepingHerCompany")
    public static void keepingHerCompany(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 1, 2));
        Hobbies.setHobby(girl, Hobbies.Hobby.COLLECTING);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 1, 2));
        girl.addAffection(p, HomeLife.FRIEND);
        int before = girl.getAffection(p);
        for (int i = 0; i < Hobbies.COMPANY / 20; i++) Hobbies.practise(level, girl, 20);
        check(Hobbies.sharedToday(level, girl) && girl.getAffection(p) == before + Hobbies.AFFECTION, "she shows them how she does it");
        check(p.getInventory().items.stream().anyMatch(s -> !s.isEmpty()), "and gives them one of her finds");
        check(!Hobbies.share(level, girl, p), "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aGardenerTends")
    public static void aGardenerTends(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 1, 4));
        BlockPos soil = helper.absolutePos(new BlockPos(5, 0, 4));
        level.setBlockAndUpdate(soil, Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(soil.above(), Blocks.WHEAT.defaultBlockState());
        check(Hobbies.tend(level, girl), "she tends the wheat");
        check(((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(soil.above())) == 1, "and it grows a stage");
        Hobbies.setHobby(girl, Hobbies.Hobby.FISHING);
        check(Hobbies.make(level, girl, level.random).isEmpty(), "no water, no fish");
        Hobbies.setHobby(girl, Hobbies.Hobby.KNITTING);
        ItemStack knit = Hobbies.make(level, girl, level.random);
        check(!knit.isEmpty(), "a knitter always makes something");
        helper.succeed();
    }
}
