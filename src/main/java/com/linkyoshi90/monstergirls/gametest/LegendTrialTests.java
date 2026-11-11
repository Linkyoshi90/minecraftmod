package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.LegendTrials;
import com.linkyoshi90.monstergirls.entity.Rarity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.RelicItem;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Legendary trials (entity/LegendTrials) and the relics they give (item/RelicItem). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class LegendTrialTests {
    private static boolean has(ServerPlayer p, Item item) {
        return p.getInventory().items.stream().anyMatch(s -> s.is(item));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyLegendSetsATrial")
    public static void everyLegendSetsATrial(GameTestHelper helper) {
        for (String id : Rarity.LEGENDARY) check(LegendTrials.GROUPS.containsKey(id), "no trial for " + id);
        check(LegendTrials.GROUPS.size() == Rarity.LEGENDARY.size(), "and no trial for anyone else");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "anOfferingForTheGorgon")
    public static void anOfferingForTheGorgon(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.getInventory().clearContent();
        AbstractMonsterGirl medusa = spawn(helper, GirlSpecies.MEDUSA_LAMIA, new BlockPos(6, 2, 6));
        medusa.setNoAi(true);
        check(LegendTrials.begin(level, medusa, p) && !LegendTrials.begin(level, medusa, p), "she sets one trial");
        Item want = BuiltInRegistries.ITEM.get(ResourceLocation.parse(LegendTrials.trialFor(medusa, p).getString("Offering")));
        check(LegendTrials.Group.SERPENT.offerings.contains(want), "she asks for one of her offerings: " + want);
        Item wrong = want == Items.EMERALD ? Items.ENDER_PEARL : Items.EMERALD;
        check(!LegendTrials.offer(level, medusa, p, new ItemStack(wrong)), "not that");
        ItemStack gift = new ItemStack(want, 2);
        check(LegendTrials.offer(level, medusa, p, gift) && gift.getCount() == 1, "that: she takes one");
        check(has(p, ModItems.SERPENT_EYE.get()) && medusa.getAffection(p) >= LegendTrials.REWARD && LegendTrials.passed(medusa, p.getUUID()), "her relic, and her fondness");
        check(!LegendTrials.begin(level, medusa, p), "once passed, no trial again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "bestedTheHoundYields")
    public static void bestedTheHoundYields(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.getInventory().clearContent();
        AbstractMonsterGirl cerberus = spawn(helper, GirlSpecies.CERBERUS_CANID, new BlockPos(5, 2, 3));
        cerberus.setNoAi(true);
        check(LegendTrials.begin(level, cerberus, p), "a test of strength");
        cerberus.hurt(level.damageSources().playerAttack(p), cerberus.getMaxHealth() * 10);
        check(cerberus.isAlive() && cerberus.getHealth() > cerberus.getMaxHealth() / 2 && cerberus.isCalm(), "she yields instead of falling");
        check(has(p, ModItems.DRAGON_SCALE.get()) && !LegendTrials.underWay(cerberus), "and gives her relic");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aVigilWithTheMoonPrincess")
    public static void aVigilWithTheMoonPrincess(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.getInventory().clearContent();
        AbstractMonsterGirl kaguya = spawn(helper, GirlSpecies.KAGUYA_ELF, new BlockPos(6, 2, 6));
        kaguya.setNoAi(true);
        check(LegendTrials.begin(level, kaguya, p), "a vigil");
        for (int i = 0; i < 10; i++) LegendTrials.watch(level, kaguya, p, true);
        check(LegendTrials.watched(kaguya, p) == 10 * LegendTrials.VIGIL_STEP, "the night adds up");
        LegendTrials.watch(level, kaguya, p, false);
        check(LegendTrials.watched(kaguya, p) == 0 && !has(p, ModItems.MOON_MIRROR.get()), "dawn too soon: begin again");
        for (int i = 0; i * LegendTrials.VIGIL_STEP < LegendTrials.VIGIL; i++) LegendTrials.watch(level, kaguya, p, true);
        LegendTrials.watch(level, kaguya, p, false);
        check(has(p, ModItems.MOON_MIRROR.get()) && LegendTrials.passed(kaguya, p.getUUID()), "a whole night: the moon mirror");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theRelicsWork")
    public static void theRelicsWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 3));
        check(!RelicItem.carried(RelicItem.Relic.DRAGON_SCALE, level, p), "the scale does nothing until you burn");
        p.setRemainingFireTicks(100);
        check(RelicItem.carried(RelicItem.Relic.DRAGON_SCALE, level, p) && p.hasEffect(MobEffects.FIRE_RESISTANCE), "burning, the fire can't hurt you");
        p.clearFire();
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 8));
        zombie.setNoAi(true);
        p.setYRot(0);
        p.setXRot(0);
        check(RelicItem.gaze(level, p) == zombie && zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the serpent's eye turns what you look at");
        check(RelicItem.reveal(level, p) >= 1 && zombie.hasEffect(MobEffects.GLOWING), "the moon mirror shows everything near");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
