package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.RealmGrassBlock;
import com.linkyoshi90.harmontide.entity.RealmTouched;
import com.linkyoshi90.harmontide.entity.ThrownHolyWater;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.order.Chaplain;
import com.linkyoshi90.harmontide.entity.order.Order;
import com.linkyoshi90.harmontide.entity.order.OrderSister;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Realm-touched players (entity/RealmTouched): how the touch grows and washes out, and what each stage does. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RealmTouchedTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theRealmTouchesThoseInIt")
    public static void theRealmTouchesThoseInIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        RealmTouched.setTouch(p, 0);
        RealmTouched.tick(level, p, true);
        check(RealmTouched.touch(p) == 0, "outside the realm nothing happens");
        RealmGrassBlock.corrupt(level, p.blockPosition().below());
        RealmTouched.tick(level, p, true);
        check(RealmTouched.touch(p) == RealmTouched.AIR, "a minute in its air touches them: " + RealmTouched.touch(p));
        ThrownHolyWater.splash(level, p.blockPosition(), null);
        check(RealmTouched.touch(p) == 0, "holy water washes it out");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "kindredAreLetBe")
    public static void kindredAreLetBe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        AbstractMonsterGirl succubus = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 2, 6));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(8, 2, 8));
        RealmTouched.setTouch(p, RealmTouched.TOUCHED);
        check(!RealmTouched.spares(succubus, p), "only touched: she may still fight them");
        int ties = Order.ties(level, p);
        RealmTouched.setTouch(p, RealmTouched.KINDRED);
        check(RealmTouched.spares(succubus, p) && !RealmTouched.spares(wolf, p), "kindred: the realm's own let them be, others don't care");
        succubus.setTarget(p);
        check(succubus.getTarget() == null, "she won't pick them");
        check(RealmTouched.bedBonus(succubus, p) == RealmTouched.KIN_BED && RealmTouched.bedBonus(wolf, p) == 0.0F, "a little likelier to share their bed");
        check(Order.ties(level, p) == ties + 1, "and the Order counts it");
        succubus.setLastHurtByMob(p);
        check(!RealmTouched.spares(succubus, p), "struck first, she fights back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theRealmFeedsItsOwn")
    public static void theRealmFeedsItsOwn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        RealmGrassBlock.corrupt(level, p.blockPosition().below());
        p.getFoodData().setFoodLevel(10);
        RealmTouched.setTouch(p, RealmTouched.KINDRED);
        RealmTouched.tick(level, p, false);
        check(p.getFoodData().getFoodLevel() == 10, "kindred aren't fed yet");
        RealmTouched.setTouch(p, RealmTouched.REALM_BORN);
        RealmTouched.tick(level, p, false);
        check(p.getFoodData().getFoodLevel() == 11, "realm-born: its air feeds them");
        check(level.isDay() || p.hasEffect(MobEffects.NIGHT_VISION) && p.hasEffect(MobEffects.DAMAGE_BOOST), "and at night they see and are strong");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aRealmWifeTouchesThem")
    public static void aRealmWifeTouchesThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        AbstractMonsterGirl wife = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 2, 6));
        wife.setNoAi(true);
        wife.setAffection(p.getUUID(), 100);
        Marriage.wed(level, wife, p, helper.absolutePos(new BlockPos(3, 2, 3)));
        RealmTouched.setTouch(p, 0);
        check(RealmTouched.realmSpouses(level, p) == 1, "a wife of the realm's own at their side");
        RealmTouched.tick(level, p, true);
        check(RealmTouched.touch(p) == RealmTouched.SPOUSE, "she touches them, even outside the realm: " + RealmTouched.touch(p));
        wife.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "kindredQuickRealmBornHardy")
    public static void kindredQuickRealmBornHardy(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        RealmGrassBlock.corrupt(level, p.blockPosition().below());
        float base = p.getMaxHealth();
        RealmTouched.setTouch(p, RealmTouched.KINDRED);
        RealmTouched.tick(level, p, false);
        check(p.hasEffect(MobEffects.MOVEMENT_SPEED) && p.getMaxHealth() == base, "kindred: quick in the realm, no more hearts yet");
        RealmTouched.setTouch(p, RealmTouched.REALM_BORN);
        RealmTouched.tick(level, p, false);
        check(p.getMaxHealth() == base + RealmTouched.HEALTH, "realm-born: hardier: " + p.getMaxHealth());
        p.setHealth(p.getMaxHealth());
        RealmTouched.setTouch(p, 0);
        RealmTouched.tick(level, p, false);
        check(p.getMaxHealth() == base && p.getHealth() <= base, "washed out, it goes again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theOrderSeesTheKindred")
    public static void theOrderSeesTheKindred(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        OrderSister knight = helper.spawn(ModEntities.ORDER_KNIGHT.get(), new BlockPos(6, 2, 4));
        knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), MobSpawnType.EVENT, null);
        knight.setNoAi(true);
        Order.setNotoriety(p, 0);
        RealmTouched.setTouch(p, RealmTouched.TOUCHED);
        check(RealmTouched.sprinkle(level, p) == null, "only touched: she doesn't see it");
        RealmTouched.setTouch(p, RealmTouched.KINDRED + 2);
        check(RealmTouched.sprinkle(level, p) == knight && RealmTouched.touch(p) == RealmTouched.KINDRED + 2 - RealmTouched.SPRINKLE, "kindred: she sprinkles holy water on them");
        check(RealmTouched.sprinkle(level, p) == null, "not again so soon");
        BlockPos hearth = helper.absolutePos(new BlockPos(3, 2, 3));
        knight.becomeChaplain("Agnes", hearth, level.getGameTime() + 24000);
        AbstractMonsterGirl bride = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(6, 2, 6));
        bride.setNoAi(true);
        RealmTouched.setTouch(p, RealmTouched.KINDRED);
        check(!Chaplain.bless(level, bride, p, hearth), "a chaplain won't bless the kindred");
        RealmTouched.setTouch(p, 0);
        check(Chaplain.bless(level, bride, p, hearth), "washed clean, she will");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
