package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Rider;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twenty-seventh Harmontide wave (HARMONTIDE_CONCEPTS.md): Bahamut, Cetus, the demon, the Fury, the Charro Negro, Koschei, La Mano Peluda, the
 * Gaitress, the bloodhound, the shedim, Anzu and the kitsune obaa.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentySeventhWaveTests {

    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
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

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "bahamutJudgesOnceADay")
    public static void bahamutJudgesOnceADay(GameTestHelper helper) {
        Dragon b = still(helper, GirlSpecies.BAHAMUT_DRAGON, new BlockPos(1, 2, 1));
        check(b.canFly(), "she flies");
        check(b.judgement(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, b, new BlockPos(5, 2, 5));
        check(b.judgement(helper.getLevel()) == 0, "nothing to strike");
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(b.judgement(helper.getLevel()) == 1 && z.getHealth() <= z.getMaxHealth() - 12.0F, "struck");
        check(b.judgement(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cetusSwallowsTheDrowned")
    public static void cetusSwallowsTheDrowned(GameTestHelper helper) {
        Merfolk c = still(helper, GirlSpecies.CETUS_MERMAID, new BlockPos(1, 2, 1));
        check(c.swallow(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, c, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(7, 2, 5));
        Drowned d = helper.spawn(EntityType.DROWNED, new BlockPos(6, 2, 5));
        d.setNoAi(true);
        check(c.swallow(helper.getLevel()) == 1 && !d.isAlive(), "whole");
        check(z.isAlive() && c.swallow(helper.getLevel()) == 0, "not a land monster");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "demonStrikesAPact")
    public static void demonStrikesAPact(GameTestHelper helper) {
        Devil d = still(helper, GirlSpecies.DEMON_DEVIL, new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        check(d.pact(helper.getLevel(), p) == -1, "no deals with strangers");
        d.addAffection(p, Orders.AFFECTION);
        p.setHealth(6.0F);
        check(d.pact(helper.getLevel(), p) == 0, "not when it would kill them");
        p.setHealth(20.0F);
        check(d.pact(helper.getLevel(), p) == 1 && p.getHealth() <= 14.0F && p.hasEffect(MobEffects.DAMAGE_BOOST), "three hearts for strength");
        check(d.pact(helper.getLevel(), p) == 0, "one at a time");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "furyHoundsTheGuilty")
    public static void furyHoundsTheGuilty(GameTestHelper helper) {
        Devil fury = still(helper, GirlSpecies.FURY_DEVIL, new BlockPos(1, 2, 1));
        check(fury.canFly(), "she flies");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        Wolf stray = helper.spawn(EntityType.WOLF, new BlockPos(6, 2, 5));
        Devil.witness(stray, helper.getLevel().damageSources().mobAttack(z));
        check(fury.quarry() == null, "a stray is nobody's pet");
        Villager v = helper.spawn(EntityType.VILLAGER, new BlockPos(6, 2, 6));
        Devil.witness(v, helper.getLevel().damageSources().mobAttack(z));
        check(fury.quarry() == z && fury.getTarget() == z && z.hasEffect(MobEffects.GLOWING), "marked");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "charroNegroLendsAndCollects")
    public static void charroNegroLendsAndCollects(GameTestHelper helper) {
        Rider c = still(helper, GirlSpecies.CHARRO_NEGRO_RIDER, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, c, new BlockPos(5, 2, 5));
        check(c.collect(helper.getLevel()) == -1, "nobody owes her");
        check(c.lend(p) && p.getInventory().countItem(Items.EMERALD) == Rider.LOAN, "eight emeralds");
        check(!c.lend(p), "one loan at a time");
        check(c.collect(helper.getLevel()) == 0, "not due yet");
        ItemStack purse = new ItemStack(Items.EMERALD, Rider.LOAN);
        check(!c.repay(p, purse) && c.hasLoan(), "eight will not do");
        c.setLoanDue(helper.getLevel().getGameTime() - 1);
        check(c.collect(helper.getLevel()) == 1 && c.getTarget() == p, "she comes to collect");
        purse = new ItemStack(Items.EMERALD, Rider.LOAN * 2);
        check(c.repay(p, purse) && purse.isEmpty() && !c.hasLoan(), "paid in full");
        c.mount(helper.getLevel());
        check(c.getVehicle() != null, "she rides");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "koscheiChainsThemDown")
    public static void koscheiChainsThemDown(GameTestHelper helper) {
        Undead k = still(helper, GirlSpecies.KOSCHEI_UNDEAD, new BlockPos(1, 2, 1));
        check(k.chain(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, k, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(k.chain(helper.getLevel()) == 1 && k.chainedCount() == 1, "chained");
        z.teleportTo(z.getX() + 4, z.getY(), z.getZ());
        k.holdChains(helper.getLevel());
        check(z.getDeltaMovement().x < 0, "hauled back");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "manoPeludaGoesForTheFace")
    public static void manoPeludaGoesForTheFace(GameTestHelper helper) {
        Undead m = still(helper, GirlSpecies.MANO_PELUDA_UNDEAD, new BlockPos(1, 2, 1));
        check(m.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WallClimberNavigation, "it runs up walls");
        check(m.lunge(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, m, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        z.setTarget(f);
        check(m.lunge(helper.getLevel()) == 1 && z.hasEffect(MobEffects.BLINDNESS) && z.getTarget() == null, "it forgets what it was doing");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "gaitressSetsThePace")
    public static void gaitressSetsThePace(GameTestHelper helper) {
        Undead g = still(helper, GirlSpecies.GAITRESS_UNDEAD, new BlockPos(1, 2, 1));
        check(g.pace(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, g, new BlockPos(2, 2, 2));
        check(g.pace(helper.getLevel()) == 1 && f.hasEffect(MobEffects.MOVEMENT_SPEED), "they keep up");
        Zombie z = zombie(helper, new BlockPos(2, 2, 1));
        g.doHurtTarget(z);
        check(g.getAction() == AbstractMonsterGirl.Action.KICK, "she kicks");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "bloodhoundNeverLosesAScent")
    public static void bloodhoundNeverLosesAScent(GameTestHelper helper) {
        Canid b = still(helper, GirlSpecies.BLOODHOUND_CANID, new BlockPos(1, 2, 1));
        check(b.scent(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, b, new BlockPos(5, 2, 5));
        check(b.scent(helper.getLevel()) == 0, "no scent");
        Zombie z = zombie(helper, new BlockPos(7, 2, 5));
        f.setLastHurtByMob(z);
        check(b.scent(helper.getLevel()) == 1 && b.trail() == z && z.hasEffect(MobEffects.GLOWING), "on its trail");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "shedimTakesHerFriendAway")
    public static void shedimTakesHerFriendAway(GameTestHelper helper) {
        Centaur s = still(helper, GirlSpecies.SHEDIM_CENTAUR, new BlockPos(1, 2, 1));
        check(!s.veil(false) && !s.hasEffect(MobEffects.INVISIBILITY), "seen by day");
        check(s.veil(true) && s.hasEffect(MobEffects.INVISIBILITY), "not by night");
        check(s.blink(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, s, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(3, 2, 5));
        check(s.blink(helper.getLevel()) == 0, "not while they are well");
        f.setHealth(4.0F);
        double before = f.distanceToSqr(z);
        check(s.blink(helper.getLevel()) == 1 && f.distanceToSqr(z) > before, "away from it");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "anzuStealsWhatShePleases")
    public static void anzuStealsWhatShePleases(GameTestHelper helper) {
        Centaur a = still(helper, GirlSpecies.ANZU_CENTAUR, new BlockPos(1, 2, 1));
        check(a.snatch(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, a, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(a.snatch(helper.getLevel()) == 0, "nothing worth having");
        z.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        z.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
        check(a.snatch(helper.getLevel()) == 1, "she takes it");
        check(z.getMainHandItem().isEmpty() && !z.hasEffect(MobEffects.DAMAGE_RESISTANCE) && f.hasEffect(MobEffects.DAMAGE_RESISTANCE), "weapon and blessing");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(z.blockPosition()).inflate(3)).isEmpty(), "the sword on the ground");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "kitsuneObaaHidesHerFriend")
    public static void kitsuneObaaHidesHerFriend(GameTestHelper helper) {
        Kitsune k = still(helper, GirlSpecies.OBAA_KITSUNE, new BlockPos(1, 2, 1));
        check(k.glamour(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, k, new BlockPos(5, 2, 5));
        Zombie a = zombie(helper, new BlockPos(6, 2, 5));
        a.setTarget(f);
        check(k.glamour(helper.getLevel()) == 0, "not for just one");
        Zombie b = zombie(helper, new BlockPos(4, 2, 5));
        b.setTarget(f);
        check(k.glamour(helper.getLevel()) == 2 && a.getTarget() == null && f.hasEffect(MobEffects.INVISIBILITY), "they lose interest");
        a.setTarget(f);
        b.setTarget(f);
        check(k.glamour(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }
}
