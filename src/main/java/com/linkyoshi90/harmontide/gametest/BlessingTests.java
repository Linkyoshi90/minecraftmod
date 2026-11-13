package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.Blessings;
import com.linkyoshi90.harmontide.entity.Clade;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Wives bless the land (entity/Blessings). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class BlessingTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyCladeBlesses")
    public static void everyCladeBlesses(GameTestHelper helper) {
        for (Clade c : Clade.values()) check(Blessings.of(c) != null, "a blessing for " + c);
        check(Blessings.of(Clade.FLORA) == Blessings.Blessing.GROWTH && Blessings.of(Clade.BOVIDAE) == Blessings.Blessing.HERD
                && Blessings.of(Clade.ACTINOPTERYGII) == Blessings.Blessing.CATCH && Blessings.of(Clade.CANIDAE) == Blessings.Blessing.WARD
                && Blessings.of(Clade.DRAGON) == Blessings.Blessing.HOARD && Blessings.of(Clade.DEVIL) == Blessings.Blessing.SOOTHE, "the old stories");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "wivesBlessTheHome")
    public static void wivesBlessTheHome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(null);
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 7));
        AbstractMonsterGirl alraune = still(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(9, 2, 7));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(p.getUUID());
        BlockPos hearth = helper.absolutePos(hp);
        check(Blessings.blessingsAt(level, hearth, p).isEmpty(), "no wives, no blessing");
        wolf.setAffection(p.getUUID(), 100);
        alraune.setAffection(p.getUUID(), 100);
        helper.runAfterDelay(3, () -> {
            check(Marriage.propose(level, wolf, p) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, wolf, p), "the wolf weds");
            check(Marriage.propose(level, alraune, p) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, alraune, p), "the alraune weds");
            Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 12));
            zombie.setNoAi(true);
            helper.setBlock(new BlockPos(3, 1, 3), Blocks.FARMLAND);
            helper.setBlock(new BlockPos(3, 2, 3), Blocks.WHEAT);
            Map<Blessings.Blessing, Integer> given = Blessings.bless(level, hearth, p);
            check(given.get(Blessings.Blessing.WARD) == 1 && given.get(Blessings.Blessing.GROWTH) == 1 && given.size() == 2, "a wolf wards, an alraune grows: " + given);
            check(zombie.hasEffect(MobEffects.GLOWING) && zombie.hasEffect(MobEffects.WEAKNESS), "the monster is marked and weakened");
            BlockPos wheat = helper.absolutePos(new BlockPos(3, 2, 3));
            int before = ((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(wheat));   // the alraune may have helped it along already
            check(Blessings.grow(level, wheat) && ((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(wheat)) == before + 1, "the wheat grows a stage");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "herdsAndHomeComforts")
    public static void herdsAndHomeComforts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Cow calf = helper.spawn(EntityType.COW, new BlockPos(3, 2, 3));
        calf.setAge(-24000);
        Cow mother = helper.spawn(EntityType.COW, new BlockPos(5, 2, 3));
        mother.setAge(6000);
        BlockPos hearth = helper.absolutePos(new BlockPos(7, 2, 7));
        check(Blessings.tendHerd(level, hearth, Blessings.HERD_TICKS) == 2, "both animals tended");
        check(calf.getAge() == -24000 + Blessings.HERD_TICKS && mother.getAge() == 6000 - Blessings.HERD_TICKS, "the calf grows, the mother rests less");
        helper.succeed();
    }
}
