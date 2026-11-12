package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Keepsakes;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Flora;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Marsupial;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import com.linkyoshi90.harmontide.item.UmbrellaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The thirty-ninth wave (HARMONTIDE_CONCEPTS.md): eight creatures, the friendship bracelet, love letters and the umbrella. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirtyNinthWaveTests {
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at, int affection) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, affection);
        return p;
    }

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "opossumPlaysPossum")
    public static void opossumPlaysPossum(GameTestHelper helper) {
        Marsupial o = still(helper, GirlSpecies.OPOSSUM_MARSUPIAL, new BlockPos(1, 2, 1));
        check(o.playPossum(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, o, new BlockPos(3, 2, 3), 30);
        check(o.playPossum(helper.getLevel()) == 0, "not hurt");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        z.setTarget(p);
        p.setHealth(4);
        check(o.playPossum(helper.getLevel()) == 1 && p.hasEffect(MobEffects.INVISIBILITY) && z.getTarget() == null, "hidden");
        check(o.playPossum(helper.getLevel()) == 0, "once a minute");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "rodentsOfTheThirtyNinthWave")
    public static void rodentsOfTheThirtyNinthWave(GameTestHelper helper) {
        Rodent mole = still(helper, GirlSpecies.MOLE_RODENT, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, mole, new BlockPos(3, 2, 3), 30);
        check(mole.tunneller() == !helper.getLevel().canSeeSky(p.blockPosition()), "Haste only out of the sky's sight");
        Rodent panda = still(helper, GirlSpecies.RED_PANDA_RODENT, new BlockPos(2, 2, 1));
        panda.addAffection(p, 30);
        p.setTicksFrozen(100);
        check(panda.snug() && p.getTicksFrozen() == 0, "warmed");
        Rodent meerkat = still(helper, GirlSpecies.MEERKAT_RODENT, new BlockPos(1, 2, 2));
        meerkat.addAffection(p, 30);
        Canid wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 2));
        wolf.addAffection(p, 30);
        check(meerkat.lookout(helper.getLevel()) == 0, "nobody hit them");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        p.setLastHurtByMob(z);
        check(meerkat.lookout(helper.getLevel()) >= 2 && wolf.getTarget() == z, "friends called to the fight");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wave39Fighters")
    public static void wave39Fighters(GameTestHelper helper) {
        Canid w = still(helper, GirlSpecies.WOLVERINE_CANID, new BlockPos(1, 2, 1));
        check(!w.berserk(), "unhurt");
        w.setHealth(w.getMaxHealth() / 2 - 1);
        check(w.berserk() && w.hasEffect(MobEffects.DAMAGE_BOOST), "berserk");
        Flora r = still(helper, GirlSpecies.RAFFLESIA_FLORA, new BlockPos(2, 2, 2));
        check(r.stench(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, r, new BlockPos(6, 2, 6), 30);
        Zombie z = zombie(helper, new BlockPos(3, 2, 2));
        check(r.stench(helper.getLevel()) >= 1 && z.hasEffect(MobEffects.WEAKNESS), "driven back");
        Undead c = still(helper, GirlSpecies.CANDLE_SPIRIT_UNDEAD, new BlockPos(1, 2, 5));
        c.addAffection(p, 30);
        check(c.vigil(helper.getLevel(), false) == 0, "only at night");
        check(c.vigil(helper.getLevel(), true) >= 1 && z.getRemainingFireTicks() > 0, "the undead burn");
        Mollusk cuttle = still(helper, GirlSpecies.CUTTLEFISH_MOLLUSK, new BlockPos(5, 2, 1));
        cuttle.addAffection(p, 30);
        check(cuttle.dazzle(helper.getLevel()) == 0, "only with her friend swimming");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "keepsakes")
    public static void keepsakes(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, girl, new BlockPos(3, 2, 3), 10);
        check(!Keepsakes.giveBracelet(helper.getLevel(), girl, p), "not close enough");
        ItemStack reply = Keepsakes.readLetter(helper.getLevel(), girl, p);
        check(!reply.isEmpty() && Keepsakes.isReply(reply) && reply.has(DataComponents.CUSTOM_NAME), "a friend writes back");
        check(Keepsakes.readLetter(helper.getLevel(), girl, p).isEmpty(), "once a day");
        girl.addAffection(p, 20);
        int before = girl.getAffection(p);
        check(Keepsakes.giveBracelet(helper.getLevel(), girl, p) && Keepsakes.wearsBracelet(girl, p) && girl.getAffection(p) > before, "she wears it");
        check(!Keepsakes.giveBracelet(helper.getLevel(), girl, p), "one is enough");
        check(helper.getLevel().isRainingAt(p.blockPosition().above()) || UmbrellaItem.share(helper.getLevel(), p) == null, "no rain, nothing to share");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "milkDripsFromHerChestLyingDown")
    public static void milkDripsFromHerChestLyingDown(GameTestHelper helper) {
        AbstractMonsterGirl cow = still(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(2, 2, 2));
        double standing = com.linkyoshi90.harmontide.entity.Lactation.chest(cow)[0];
        cow.setAction(AbstractMonsterGirl.Action.LYING);
        double[] lying = com.linkyoshi90.harmontide.entity.Lactation.chest(cow);
        check(lying[0] < standing / 2 && lying[1] < 0, "low, and towards her head behind her");
        cow.setAction(AbstractMonsterGirl.Action.SITTING);
        check(com.linkyoshi90.harmontide.entity.Lactation.chest(cow)[0] < standing, "lower sitting");
        helper.succeed();
    }
}
