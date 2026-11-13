package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Herds;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Herds and schools (entity/Herds). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class HerdTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aHerdKeepsWatch")
    public static void aHerdKeepsWatch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl a = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(3, 2, 3));
        AbstractMonsterGirl b = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(5, 2, 3));
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 12));
        check(Herds.herds(a) && !Herds.herds(wolf), "grazers herd, hunters don't");
        check(Herds.herd(a).size() == 2, "two of a kind make a herd");
        check(Herds.threat(a, wolf), "a wild hunter of another kind is a threat");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 3));
        check(!Herds.threat(a, p), "a stranger walking at a distance isn't");
        p.setSprinting(true);
        check(Herds.threat(a, p), "running up, they are");
        p.setShiftKeyDown(true);
        p.setSprinting(false);
        check(!Herds.threat(a, p), "sneaking, you get close");
        p.setShiftKeyDown(false);
        a.addAffection(p, 20);
        check(!Herds.threat(a, p), "never a friend");
        check(!Herds.herds(a) && Herds.herd(b).size() == 1, "a befriended girl leaves the wild herd");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theAlarmMovesTheHerd")
    public static void theAlarmMovesTheHerd(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl a = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(3, 2, 3));
        AbstractMonsterGirl b = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(4, 2, 5));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 12));
        z.setNoAi(true);
        check(Herds.alarm(level, a, z.position()) == 2 && Herds.alarmed(a) && Herds.alarmed(b), "the alarm reaches the whole herd");
        check(!Herds.standing(b) == !Herds.stands(a, 2), "all answer the same way");
        Vec3 to = Herds.answer(b);
        if (!Herds.standing(b)) check(to.z < b.getZ(), "they flee away from it: " + to);
        for (int i = 0; i < 3; i++) still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(6 + i, 2, 3));
        check(Herds.stands(a, Herds.herd(a).size()), "five of them stand their ground");
        AbstractMonsterGirl stray = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(14, 2, 14));
        Vec3 back = Herds.gather(stray);
        check(back != null && back.distanceTo(stray.position()) > 1, "a stray goes back to the herd");
        helper.succeed();
    }
}
