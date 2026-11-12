package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.SpiritPacts;
import com.linkyoshi90.harmontide.entity.SpiritPacts.Element;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Spirit pacts (entity/SpiritPacts): binding a spirit, and her element at work. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SpiritPactTests {
    private static AbstractMonsterGirl spirit(GameTestHelper helper, GirlSpecies s, BlockPos at, ServerPlayer p, int affection) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        g.setAffection(p.getUUID(), affection);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aSalamandersPact")
    public static void aSalamandersPact(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl sal = spirit(helper, GirlSpecies.SALAMANDER_LIZARDFOLK, new BlockPos(5, 2, 5), p, 30);
        check(SpiritPacts.element(GirlSpecies.SALAMANDER_LIZARDFOLK) == Element.FIRE && SpiritPacts.element(GirlSpecies.WOLF_CANID) == null, "the four spirits only");
        check(!SpiritPacts.bind(level, sal, p) && !SpiritPacts.has(p, Element.FIRE), "fond isn't enough");
        sal.setAffection(p.getUUID(), SpiritPacts.DEVOTED);
        check(SpiritPacts.bind(level, sal, p) && SpiritPacts.has(p, Element.FIRE) && p.getUUID().equals(SpiritPacts.boundTo(sal)), "devoted, she binds herself");
        AbstractMonsterGirl other = spirit(helper, GirlSpecies.SALAMANDER_LIZARDFOLK, new BlockPos(7, 2, 5), p, 100);
        check(!SpiritPacts.bind(level, other, p), "one spirit of fire at a time");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 6));
        z.setNoAi(true);
        z.hurt(level.damageSources().playerAttack(p), 1.0F);
        check(z.isOnFire(), "the bearer's blows burn");
        check(SpiritPacts.release(level, sal, p) && !SpiritPacts.has(p, Element.FIRE) && SpiritPacts.boundTo(sal) == null, "released");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theElementsAtWork")
    public static void theElementsAtWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos feet = new BlockPos(5, 2, 5);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, feet);
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.UNDINE_SLIME, GirlSpecies.SYLPH_ELF, GirlSpecies.GNOME_ELF}) {
            check(SpiritPacts.bind(level, spirit(helper, s, new BlockPos(9, 2, 9), p, 100), p), "bound: " + s);
        }
        Set<Element> acted = SpiritPacts.tick(level, p);
        check(acted.contains(Element.WIND) && p.hasEffect(MobEffects.JUMP), "wind: light on your feet");
        check(!acted.contains(Element.WATER), "dry: the water does nothing");
        check(acted.contains(Element.EARTH) == SpiritPacts.deep(level, p), "earth only deep down");
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) if (dx != 0 || dz != 0) helper.setBlock(feet.offset(dx, 0, dz), Blocks.GLASS);
        helper.setBlock(feet, Blocks.WATER);
        check(SpiritPacts.tick(level, p).contains(Element.WATER) && p.hasEffect(MobEffects.REGENERATION), "in water, it heals");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
