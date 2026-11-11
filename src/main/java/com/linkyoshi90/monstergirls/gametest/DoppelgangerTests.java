package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Doppelganger;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The doppelganger (entity/girl/Doppelganger): whose look she borrows, her true face, and that she keeps her look. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class DoppelgangerTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sheBorrowsTheNearestFace")
    public static void sheBorrowsTheNearestFace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Elf doppel = spawn(helper, GirlSpecies.DOPPELGANGER_ELF, new BlockPos(4, 2, 4));
        doppel.setNoAi(true);
        check(Doppelganger.copyable(GirlSpecies.RED_KITSUNE) && !Doppelganger.copyable(GirlSpecies.KOI_MERMAID)
                && !Doppelganger.copyable(GirlSpecies.DOPPELGANGER_ELF) && !Doppelganger.copyable(GirlSpecies.HAND_UNDEAD), "biped girls only, never another doppelganger");
        check(!Doppelganger.shift(level, doppel) && doppel.disguise().isEmpty(), "nobody near: her own face");
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(6, 2, 4));
        fox.setNoAi(true);
        AbstractMonsterGirl koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(5, 2, 4));
        koi.setNoAi(true);
        check(Doppelganger.shift(level, doppel) && doppel.disguise().equals("red_kitsune"), "the nearest she can copy: the fox, not the mermaid: " + doppel.disguise());
        CompoundTag saved = new CompoundTag();
        doppel.saveWithoutId(saved);
        Elf again = spawn(helper, GirlSpecies.DOPPELGANGER_ELF, new BlockPos(8, 2, 8));
        again.load(saved);
        check(again.disguise().equals("red_kitsune"), "she keeps her borrowed look");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sheWearsYourDearest")
    public static void sheWearsYourDearest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Elf doppel = spawn(helper, GirlSpecies.DOPPELGANGER_ELF, new BlockPos(4, 2, 4));
        doppel.setNoAi(true);
        doppel.setAffection(p.getUUID(), 20);
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 4));
        cat.setNoAi(true);
        cat.setAffection(p.getUUID(), 40);
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(10, 2, 10));
        wolf.setNoAi(true);
        wolf.setAffection(p.getUUID(), 90);
        Doppelganger.shift(level, doppel);
        check(doppel.disguise().equals("wolf_canid"), "your dearest companion's look, not the nearest: " + doppel.disguise());
        check(Doppelganger.askFace(level, doppel, p) && doppel.trueFace() && doppel.disguise().isEmpty(), "the mirror: her true face");
        Doppelganger.shift(level, doppel);
        check(doppel.disguise().isEmpty(), "and she keeps it");
        check(Doppelganger.askFace(level, doppel, p) && doppel.disguise().equals("wolf_canid"), "again: she borrows it back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
