package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Archery;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Archery matches (entity/Archery): her real arrows at a target block, the scoring, and the result. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ArcheryTests {
    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "archeryMatchAtTheTarget")
    public static void archeryMatchAtTheTarget(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos targetRel = new BlockPos(7, 3, 12);
        helper.setBlock(targetRel, Blocks.TARGET);
        BlockPos target = helper.absolutePos(targetRel);
        AbstractMonsterGirl elf = spawn(helper, GirlSpecies.WOOD_ELF, new BlockPos(7, 2, 5));
        elf.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 5));
        elf.setAffection(p.getUUID(), 30);
        check(target.equals(Archery.target(level, elf)), "she finds the target");
        BlockHitResult centre = new BlockHitResult(new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ()), Direction.NORTH, target, false);
        BlockHitResult rim = new BlockHitResult(new Vec3(target.getX() + 0.02, target.getY() + 0.5, target.getZ()), Direction.NORTH, target, false);
        check(Archery.score(centre) == 15 && Archery.score(rim) == 1, "dead centre 15, the rim 1: " + Archery.score(centre) + "/" + Archery.score(rim));
        check(Archery.hit(level, p, centre) == 0, "no match, no score");
        check(Archery.start(level, elf, p, target) && Archery.playing(elf), "the match is on");
        Archery.loosed(p);
        check(Archery.hit(level, p, centre) == 15, "their bullseye counts");
        Archery.loosed(p);
        Archery.loosed(p);
        Archery.loosed(p);
        check(Archery.hit(level, p, centre) == 0, "only three arrows count");
        helper.succeedWhen(() -> {
            check(!Archery.playing(elf), "she has shot her three and the match is over");
            check(elf.getAffection(p) >= 30 + Archery.AFFECTION, "closer either way: " + elf.getAffection(p));
            HarpyTestUtil.remove(p);
        });
    }
}
