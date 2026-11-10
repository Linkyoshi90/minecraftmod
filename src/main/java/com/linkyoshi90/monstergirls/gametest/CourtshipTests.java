package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Courtship;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Old customs (entity/Courtship): a fair duel, a spinner's snare, a house spirit keeping the hearth. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class CourtshipTests {
    private static boolean criterion(ServerPlayer p, String c) {
        var a = p.server.getAdvancements().get(MonsterGirlsMod.id(Courtship.ADVANCEMENT));
        return a != null && p.getAdvancements().getOrStartProgress(a).getCriterion(c).isDone();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "beatenFairlySheYields")
    public static void beatenFairlySheYields(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl oni = spawn(helper, GirlSpecies.ONI_DEVIL, new BlockPos(4, 2, 3));
        oni.setNoAi(true);
        check(Courtship.of(GirlSpecies.ONI_DEVIL) == Courtship.Custom.DUEL && Courtship.of(GirlSpecies.SUCCUBUS_DEVIL) == null, "the oni duels, the succubus doesn't");
        float max = oni.getMaxHealth();
        oni.hurt(level.damageSources().playerAttack(p), 1.0F);
        check(oni.getHealth() < max && !Courtship.courted(oni, p.getUUID()), "a light blow just hurts");
        oni.setTarget(p);
        oni.invulnerableTime = 0;
        oni.hurt(level.damageSources().playerAttack(p), max * 10);
        check(oni.isAlive() && oni.getHealth() > max / 2 - 1, "the blow that would fell her makes her yield");
        check(Courtship.courted(oni, p.getUUID()) && oni.getAffection(p) >= Courtship.REWARD && oni.isCalm() && oni.getTarget() == null, "she respects the winner");
        check(criterion(p, "duel"), "an old custom kept");
        int a = oni.getAffection(p);
        oni.invulnerableTime = 0;
        oni.hurt(level.damageSources().playerAttack(p), 1.0F);
        check(oni.getAffection(p) < a, "only once: after that a blow is just a blow");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "arrowsDontWinADuel")
    public static void arrowsDontWinADuel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 3));
        wolf.setNoAi(true);
        Arrow arrow = new Arrow(EntityType.ARROW, level);
        arrow.setOwner(p);
        wolf.hurt(level.damageSources().arrow(arrow, p), wolf.getMaxHealth() * 0.75F);
        check(wolf.getHealth() < wolf.getMaxHealth() / 2 && !Courtship.courted(wolf, p.getUUID()), "shot from afar: no duel, no respect");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "caughtInHerWeb")
    public static void caughtInHerWeb(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl spider = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(9, 2, 9));
        check(Courtship.snare(level, spider, p) && level.getBlockState(p.blockPosition()).is(Blocks.COBWEB), "a web at your feet");
        helper.onEachTick(() -> {
            if (Courtship.snaring(spider)) Courtship.updateSnare(level, spider);
        });
        helper.succeedWhen(() -> {
            check(Courtship.courted(spider, p.getUUID()) && spider.getAffection(p) == Courtship.REWARD, "she claims her catch");
            check(!level.getBlockState(p.blockPosition()).is(Blocks.COBWEB) && criterion(p, "snare"), "and cuts you loose");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "struckSheGivesUp")
    public static void struckSheGivesUp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl tiger = spawn(helper, GirlSpecies.TIGER_NEKOMATA, new BlockPos(8, 2, 3));
        tiger.setNoAi(true);
        check(Courtship.snare(level, tiger, p) && !level.getBlockState(p.blockPosition()).is(Blocks.COBWEB), "the tigress spins no web");
        tiger.hurt(level.damageSources().playerAttack(p), 1.0F);
        Courtship.updateSnare(level, tiger);
        check(!Courtship.snaring(tiger) && !Courtship.courted(tiger, p.getUUID()), "struck, she gives up");
        check(!Courtship.trySnare(level, tiger), "and not again today");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theKikimoraKeepsYourHouse")
    public static void theKikimoraKeepsYourHouse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(5, 2, 5), chest = new BlockPos(7, 2, 5), web = new BlockPos(5, 3, 8);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        helper.setBlock(chest, Blocks.CHEST);
        helper.setBlock(web, Blocks.COBWEB);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(p.getUUID());
        AbstractMonsterGirl kiki = spawn(helper, GirlSpecies.KIKIMORA_DEVIL, new BlockPos(6, 2, 6));
        kiki.setNoAi(true);
        ItemEntity mess = new ItemEntity(level, kiki.getX() + 1, kiki.getY(), kiki.getZ(), new ItemStack(Items.BREAD, 3));
        mess.setPickUpDelay(0);
        level.addFreshEntity(mess);
        helper.runAfterDelay(2, () -> {                                  // the hearth is in loaded() from the tick after it's placed
            check(Courtship.serve(level, kiki), "she comes to keep the hearth");
            check(!mess.isAlive() && !((ChestBlockEntity) helper.getBlockEntity(chest)).isEmpty(), "the bread is put away");
            check(helper.getBlockState(web).isAir(), "the cobwebs come down");
            check(Courtship.visits(kiki, p) == 1 && kiki.getAffection(p) == Courtship.SERVICE_STEP, "a little fond of the house");
            check(!Courtship.serve(level, kiki), "one visit at a time");
            BlockPos home = helper.absolutePos(hp);
            for (int i = 1; i < Courtship.VISITS; i++) Courtship.keep(level, kiki, home, p);
            check(Courtship.courted(kiki, p.getUUID()) && criterion(p, "service"), "she has made up her mind to stay");
            check(kiki.getAffection(p) == Courtship.SERVICE_STEP * (Courtship.VISITS - 1) + Courtship.REWARD, "fond of its master");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
