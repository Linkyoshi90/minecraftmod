package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Treats;
import com.linkyoshi90.monstergirls.entity.girl.Jackalope;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The rabbit family (jackalope_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class JackalopeTests {

    @SuppressWarnings("unchecked")
    static <T extends AbstractMonsterGirl> T spawn(GameTestHelper helper, GirlSpecies s, BlockPos pos) {
        return (T) helper.spawn(ModGirls.type(s), pos);
    }

    static void interact(AbstractMonsterGirl girl, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        girl.interact(player, InteractionHand.MAIN_HAND);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "jackalopeBringsLuckWhileFollowing")
    public static void jackalopeBringsLuckWhileFollowing(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Jackalope bunny = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(7, 2, 7));
        bunny.setAffection(player.getUUID(), Jackalope.FOLLOW_AFFECTION);
        helper.succeedWhen(() -> {
            check(player.hasEffect(MobEffects.LUCK), "a jackalope at high friendship brings luck");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "inabaWarmsUpTwiceAsFastOnceCaught")
    public static void inabaWarmsUpTwiceAsFastOnceCaught(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Jackalope inaba = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(7, 2, 7));
        check(!inaba.isCaught(), "not caught yet");
        player.setShiftKeyDown(true);
        interact(inaba, player, ItemStack.EMPTY);
        check(inaba.isCaught(), "a hand on her: caught");
        int before = inaba.getAffection(player);
        interact(inaba, player, new ItemStack(Items.CARROT));
        int worth = Treats.favourite(inaba) == Items.CARROT ? 2 * Treats.FAVOURITE_TIMES : 2;
        check(inaba.getAffection(player) - before == worth, "a carrot is worth double now: " + (inaba.getAffection(player) - before));
        check(!Jackalope.spawnRule(GirlSpecies.INABA_JACKALOPE, helper.getLevel(), helper.absolutePos(new BlockPos(7, 2, 7)), helper.getLevel().random)
                || helper.getLevel().isNight(), "Inaba never spawns by day");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "easterBunnyHidesPaintedEggs")
    public static void easterBunnyHidesPaintedEggs(GameTestHelper helper) {
        for (int x = 0; x <= 14; x++) for (int z = 0; z <= 14; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
        Jackalope bunny = spawn(helper, GirlSpecies.EASTER_JACKALOPE, new BlockPos(7, 2, 7));
        bunny.setNextEgg(0);
        helper.succeedWhen(() -> {
            check(!bunny.getHiddenEggs().isEmpty(), "she should hide an egg");
            check(helper.getLevel().getBlockState(bunny.getHiddenEggs().get(0)).is(ModBlocks.PAINTED_EGG.get()), "a painted egg block");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "easterBunnyLovesHerEggsBack")
    public static void easterBunnyLovesHerEggsBack(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Jackalope bunny = spawn(helper, GirlSpecies.EASTER_JACKALOPE, new BlockPos(7, 2, 7));
        interact(bunny, player, new ItemStack(ModBlocks.PAINTED_EGG.get()));
        check(bunny.getAffection(player) == 2, "a found egg brought back is worth 2: " + bunny.getAffection(player));
        check(player.getInventory().items.stream().anyMatch(s -> !s.isEmpty()), "and she gives a treat");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "alMirajClanDefendsItsOwn")
    public static void alMirajClanDefendsItsOwn(GameTestHelper helper) {
        Jackalope a = spawn(helper, GirlSpecies.AL_MIRAJ_JACKALOPE, new BlockPos(5, 2, 5));
        Jackalope b = spawn(helper, GirlSpecies.AL_MIRAJ_JACKALOPE, new BlockPos(9, 2, 9));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        a.hurt(helper.getLevel().damageSources().mobAttack(zombie), 1.0F);
        check(b.getTarget() == zombie, "the rest of the clan goes for whoever hurt one of them");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "volcanoRabbitRunsOutOfBreath")
    public static void volcanoRabbitRunsOutOfBreath(GameTestHelper helper) {
        Jackalope bunny = spawn(helper, GirlSpecies.VOLCANO_JACKALOPE, new BlockPos(3, 2, 3));
        check(bunny.fireImmune(), "lava cannot hurt her");
        bunny.setStamina(3);
        helper.onEachTick(() -> {
            if (!bunny.isPanting()) bunny.setDeltaMovement(0.3, bunny.getDeltaMovement().y, 0.0);   // sprinting away
        });
        helper.succeedWhen(() -> check(bunny.isPanting(), "she runs out of breath and pants"));
    }
}
