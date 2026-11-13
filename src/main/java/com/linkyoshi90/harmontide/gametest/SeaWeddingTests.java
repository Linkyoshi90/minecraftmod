package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.SeaWeddings;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Sea weddings and mermaid blood (entity/SeaWeddings). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SeaWeddingTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "aSeaWedding")
    public static void aSeaWedding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        for (int x = 2; x <= 5; x++) for (int z = 2; z <= 3; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
        AbstractMonsterGirl bride = still(helper, GirlSpecies.SEAL_MERMAID, new BlockPos(6, 2, 7));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        bride.setAffection(p.getUUID(), 100);
        BlockPos hearth = helper.absolutePos(hp);
        helper.runAfterDelay(3, () -> {
            check(SeaWeddings.byTheWater(level, hearth), "water by the hearth");
            check(SeaWeddings.seaBishop(level, bride, hearth) == null && !SeaWeddings.onWed(level, bride, p, hearth), "no sea bishop, no sea wedding");
            AbstractMonsterGirl bishop = still(helper, GirlSpecies.DUGONG_MERMAID, new BlockPos(9, 2, 9));
            check(SeaWeddings.seaBishop(level, bride, hearth) == bishop, "a sea girl among the guests officiates");
            check(Marriage.propose(level, bride, p) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, bride, p), "wed by the sea");
            check(SeaWeddings.blessed(p), "the husband is blessed");
            check(!SeaWeddings.breathe(p, false), "only in water");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "mermaidBloodOnce")
    public static void mermaidBloodOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = still(helper, GirlSpecies.KOI_MERMAID, new BlockPos(5, 2, 5));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        girl.setAffection(p.getUUID(), 50);
        check(SeaWeddings.onPet(level, girl, p, true) == null, "only a close friend");
        girl.setAffection(p.getUUID(), 100);
        check(SeaWeddings.onPet(level, girl, p, true) != null && p.getInventory().countItem(ModItems.MERMAID_BLOOD.get()) == 1, "her blood, given");
        check(SeaWeddings.onPet(level, girl, p, true) == null, "once in a life");
        double before = p.getAttributeValue(Attributes.MAX_HEALTH);
        check(SeaWeddings.drink(p) && p.getAttributeValue(Attributes.MAX_HEALTH) == before + SeaWeddings.BLOOD_HEALTH, "a lasting extra heart");
        check(!SeaWeddings.drink(p), "only once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
