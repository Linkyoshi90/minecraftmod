package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.SpawnConditions;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.WeatherSpirits;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Weather spirits (entity/girl/WeatherSpirits): the raiju, the ame-onna and the sylph. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WeatherSpiritTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theyComeWithTheirWeather")
    public static void theyComeWithTheirWeather(GameTestHelper helper) {
        check(SpawnConditions.of("raiju_canid") == SpawnConditions.When.THUNDER, "the raiju waits for thunder");
        check(SpawnConditions.of("ame_onna_undead") == SpawnConditions.When.RAIN && SpawnConditions.of("sylph_elf") == SpawnConditions.When.STORM, "rain and storm");
        check(SpawnConditions.met(SpawnConditions.When.THUNDER, 6000, 0, true, true), "in a thunderstorm");
        check(!SpawnConditions.met(SpawnConditions.When.THUNDER, 6000, 0, true, false), "not in plain rain");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theRaijuCallsLightning")
    public static void theRaijuCallsLightning(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        AbstractMonsterGirl raiju = spawn(helper, GirlSpecies.RAIJU_CANID, new BlockPos(5, 2, 5));
        raiju.addAffection(p, HomeLife.FRIEND);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        z.setNoAi(true);
        float was = z.getHealth();
        check(WeatherSpirits.thunderCall(level, raiju, false) == null, "no storm, no lightning");
        Mob hit = WeatherSpirits.thunderCall(level, raiju, true);
        check(hit == z && z.getHealth() < was, "a bolt on the monster near her friend");
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        EntityStruckByLightningEvent struck = new EntityStruckByLightningEvent(raiju, bolt);
        WeatherSpirits.onStruck(struck);
        check(struck.isCanceled(), "lightning never touches her");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theAmeOnnasRain")
    public static void theAmeOnnasRain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        AbstractMonsterGirl ame = spawn(helper, GirlSpecies.AME_ONNA_UNDEAD, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(6, 1, 6), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(6, 2, 6), Blocks.WHEAT);
        BlockPos wheat = helper.absolutePos(new BlockPos(6, 2, 6));
        check(WeatherSpirits.rainGrowth(level, ame, true) == 0, "a stranger's crops are her own business");
        ame.addAffection(p, HomeLife.FRIEND);
        check(WeatherSpirits.rainGrowth(level, ame, false) == 0, "not without rain");
        check(WeatherSpirits.rainGrowth(level, ame, true) >= 1 && ((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(wheat)) == 1, "the wheat grows in her rain");
        if (!level.getLevelData().isRaining()) {
            boolean showered = WeatherSpirits.shower(level, ame, p), rained = level.getLevelData().isRaining();
            level.setWeatherParameters(0, 0, false, false);                   // put the sky back at once, for the other tests
            check(showered && rained, "a water bottle brings a shower");
            check(!WeatherSpirits.shower(level, ame, p), "once a day");
            level.setWeatherParameters(0, 0, false, false);
        }
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theSylphsTailwind")
    public static void theSylphsTailwind(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        AbstractMonsterGirl sylph = spawn(helper, GirlSpecies.SYLPH_ELF, new BlockPos(5, 2, 5));
        check(!WeatherSpirits.tailwind(level, sylph), "not for a stranger");
        sylph.addAffection(p, HomeLife.FRIEND);
        p.fallDistance = 6;
        boolean sky = level.canSeeSky(p.blockPosition().above());
        check(WeatherSpirits.tailwind(level, sylph) == sky, "under the open sky");
        if (sky) check(p.hasEffect(MobEffects.MOVEMENT_SPEED) && p.hasEffect(MobEffects.SLOW_FALLING), "a tailwind and a soft landing");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "wildSpiritsGoWithTheWeather")
    public static void wildSpiritsGoWithTheWeather(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        AbstractMonsterGirl wild = spawn(helper, GirlSpecies.RAIJU_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.AME_ONNA_UNDEAD, new BlockPos(5, 2, 3));
        AbstractMonsterGirl egg = spawn(helper, GirlSpecies.SYLPH_ELF, new BlockPos(7, 2, 3));
        WeatherSpirits.weatherBorn(wild);
        WeatherSpirits.weatherBorn(friend);
        friend.addAffection(p, HomeLife.FRIEND);
        check(!WeatherSpirits.leaves(level, wild, true, true), "she stays while it thunders");
        check(WeatherSpirits.leaves(level, wild, true, false) && wild.isRemoved(), "the raiju goes when the thunder stops");
        check(!WeatherSpirits.leaves(level, friend, false, false) && !WeatherSpirits.isWeatherBorn(friend), "a friend stays");
        check(!WeatherSpirits.leaves(level, egg, false, false), "one not born of the weather stays");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
