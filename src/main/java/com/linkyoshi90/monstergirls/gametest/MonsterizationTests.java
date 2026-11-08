package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.Monsterization;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Monsterization (entity/Monsterization): vanilla creatures in the Demon Realm become monster girls. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MonsterizationTests {
    private static void realmUnder(GameTestHelper helper, int x, int z) {
        RealmGrassBlock.corrupt(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, z)));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cowBecomesHolstaur")
    public static void cowBecomesHolstaur(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        realmUnder(helper, 7, 7);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(7, 2, 7));
        cow.setNoAi(true);
        cow.setCustomName(Component.literal("Bessie"));
        check(Monsterization.step(level, cow, false) == null && Monsterization.soaked(cow) == 0, "not in daylight");
        AbstractMonsterGirl girl = null;
        for (int i = 0; i < Monsterization.STEPS - 1; i++) girl = Monsterization.step(level, cow, true);
        check(girl == null && Monsterization.soaked(cow) == Monsterization.STEPS - 1 && cow.isAlive(), "still soaking it up");
        girl = Monsterization.step(level, cow, true);
        check(girl != null && girl.getSpecies() == GirlSpecies.HOLSTAUR_MINOTAUR && !cow.isAlive(), "a holstaur now");
        check(!((com.linkyoshi90.monstergirls.entity.girl.Minotaur) girl).isMale(), "the realm turns cows, never bulls");
        check(girl.hasCustomName() && "Bessie".equals(girl.getCustomName().getString()) && girl.isPersistenceRequired(), "her name kept, and she stays");
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(Monsterization.monsterized(girl) && girl.trustsEnoughForBed(stranger) && girl.bedChanceFor(stranger) >= Monsterization.EAGER, "eager for a stranger's bed");
        check(Monsterization.describe(girl) != null, "her bond says what happened to her");
        AbstractMonsterGirl born = spawn(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(11, 2, 3));
        check(!Monsterization.monsterized(born) && !born.trustsEnoughForBed(stranger) && born.bedChanceFor(stranger) == 0.0F, "one born a holstaur waits for friendship");
        girl.setFather(stranger.getUUID());
        check(!girl.trustsEnoughForBed(stranger), "never her father");
        HarpyTestUtil.remove(stranger);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "onlyGrownVanillaInTheRealm")
    public static void onlyGrownVanillaInTheRealm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Cow off = helper.spawn(EntityType.COW, new BlockPos(3, 2, 3));
        off.setNoAi(true);
        check(Monsterization.step(level, off, true) == null && Monsterization.soaked(off) == 0, "no realm under her");
        realmUnder(helper, 10, 10);
        Cow calf = helper.spawn(EntityType.COW, new BlockPos(10, 2, 10));
        calf.setNoAi(true);
        calf.setBaby(true);
        check(Monsterization.kindOf(calf) == null && Monsterization.step(level, calf, true) == null, "a calf stays a calf");
        check(Monsterization.kindOf(helper.spawn(EntityType.WITCH, new BlockPos(5, 2, 10))) == GirlSpecies.SUCCUBUS_DEVIL, "a witch becomes a succubus");
        check(Monsterization.kindOf(helper.spawn(EntityType.CREEPER, new BlockPos(2, 2, 12))) == null, "not everyone has a kind");
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(12, 2, 3));
        check(Monsterization.kindOf(girl) == null, "girls of the mod stay who they are");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tamedWolfStaysDevoted")
    public static void tamedWolfStaysDevoted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(7, 2, 7));
        wolf.setNoAi(true);
        wolf.tame(p);
        AbstractMonsterGirl girl = Monsterization.change(level, wolf, Monsterization.kindOf(wolf));
        check(girl != null && girl.getSpecies() == GirlSpecies.WOLF_CANID, "a wolf girl");
        check(girl.getAffection(p) == Monsterization.PET_AFFECTION, "still devoted to her owner");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    /** Ready to breed right now (its half-day wait over). */
    private static void ready(net.minecraft.world.entity.Mob mob) {
        mob.getPersistentData().putLong("RealmNextBreed", 0L);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "villagerPossessedBearsDemons")
    public static void villagerPossessedBearsDemons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        realmUnder(helper, 7, 7);
        var villager = helper.spawn(EntityType.VILLAGER, new BlockPos(7, 2, 7));
        villager.setNoAi(true);
        check(!Monsterization.soak(level, villager, false) && Monsterization.soaked(villager) == 0, "not in daylight");
        for (int i = 0; i < Monsterization.STEPS - 1; i++) check(!Monsterization.soak(level, villager, true), "still soaking it up");
        check(Monsterization.soak(level, villager, true) && Monsterization.possessed(villager), "possessed now");
        check(villager.isAlive() && villager.getType() == EntityType.VILLAGER && villager.isPersistenceRequired(), "she keeps her own body, and stays");
        check(Monsterization.breed(level, villager) == null, "no one of her kind near her yet");
        var partner = helper.spawn(EntityType.VILLAGER, new BlockPos(9, 2, 7));
        partner.setNoAi(true);
        ready(villager);
        var young = Monsterization.breed(level, villager);
        check(young instanceof AbstractMonsterGirl g && g.isBaby() && (g.getSpecies() == GirlSpecies.SUCCUBUS_DEVIL || g.getSpecies() == GirlSpecies.DEMON_DEVIL),
                "her young is a little succubus or demon");
        check(Monsterization.breed(level, villager) == null, "not again until half a day has passed");
        check(!Monsterization.possessed(partner) && Monsterization.offspringOf(partner) == null, "her partner is not possessed by it");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "possessedPigBearsPiglettes")
    public static void possessedPigBearsPiglettes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 4));
        var boar = helper.spawn(EntityType.PIG, new BlockPos(6, 2, 4));
        pig.setNoAi(true);
        boar.setNoAi(true);
        check(Monsterization.kindOf(pig) == null, "a pig is possessed, not changed");
        Monsterization.possess(level, pig);
        ready(pig);
        var young = Monsterization.breed(level, pig);
        check(young instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.PIGLETTE_ARMOURED && g.isBaby() && pig.isAlive(), "a piglette, and the pig is still a pig");
        for (int i = 0; i < Monsterization.CROWD; i++) spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(10 + i % 3, 2, 10 + i / 3));
        ready(pig);
        check(Monsterization.breed(level, pig) == null, "not while the girls already crowd the place");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "holyWaterFreesAPossessedCat")
    public static void holyWaterFreesAPossessedCat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var cat = helper.spawn(EntityType.CAT, new BlockPos(5, 2, 5));
        cat.setNoAi(true);
        Monsterization.possess(level, cat);
        GirlSpecies kind = Monsterization.offspringOf(cat);
        check(kind == GirlSpecies.BAKENEKO_NEKOMATA || kind == GirlSpecies.CAIT_SITH_NEKOMATA || kind == GirlSpecies.CHESHIRE_NEKOMATA, "her young would be nekomata");
        net.minecraft.world.item.ItemStack water = new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.HOLY_WATER.get());
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, water);
        water.getItem().interactLivingEntity(water, p, cat, net.minecraft.world.InteractionHand.MAIN_HAND);
        check(!Monsterization.possessed(cat) && Monsterization.offspringOf(cat) == null, "the realm is driven out of her");
        check(p.getMainHandItem().is(net.minecraft.world.item.Items.GLASS_BOTTLE), "the bottle is empty");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "golemsBecomeGolemGirls")
    public static void golemsBecomeGolemGirls(GameTestHelper helper) {
        check(Monsterization.kindOf(helper.spawn(EntityType.IRON_GOLEM, new BlockPos(4, 2, 4))) == GirlSpecies.GOLEM_CONSTRUCT, "an iron golem becomes a golem");
        check(Monsterization.kindOf(helper.spawn(EntityType.SNOW_GOLEM, new BlockPos(10, 2, 10))) == GirlSpecies.SNOWGIRL_CONSTRUCT, "a snow golem becomes a snowgirl");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "turnedGirlsAreEager")
    public static void turnedGirlsAreEager(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(7, 2, 7));
        cow.setNoAi(true);
        AbstractMonsterGirl girl = Monsterization.change(level, cow, GirlSpecies.HOLSTAUR_MINOTAUR);
        check(girl.bedChanceFor(p) == 1.0F, "she always comes to a sleeper's bed");
        check(com.linkyoshi90.monstergirls.entity.Missing.conceptionChance(girl, p) == 1.0F, "and it always takes");
        AbstractMonsterGirl born = spawn(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(11, 2, 11));
        for (int i = 0; i < 8; i++) {
            AbstractMonsterGirl calf = born.spawnYoung(level, born.blockPosition(), null);
            check(calf instanceof com.linkyoshi90.monstergirls.entity.girl.Minotaur m && !m.isMale(), "a holstaur's calves are all daughters");
        }
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "harpiesAndFrogsToo")
    public static void harpiesAndFrogsToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        realmUnder(helper, 7, 7);
        var parrot = helper.spawn(EntityType.PARROT, new BlockPos(7, 2, 7));
        parrot.setNoAi(true);
        check(Monsterization.otherOf(parrot) == com.linkyoshi90.monstergirls.registry.ModEntities.MACAW_HARPY.get(), "a parrot becomes a macaw harpy");
        net.minecraft.world.entity.Mob harpy = null;
        for (int i = 0; i < Monsterization.STEPS; i++) harpy = Monsterization.stepOther(level, parrot, true);
        check(harpy instanceof com.linkyoshi90.monstergirls.entity.MacawHarpy && Monsterization.monsterized(harpy) && !parrot.isAlive(), "she changed");
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 2, 12));
        var bonded = (com.linkyoshi90.monstergirls.entity.Bonded) harpy;
        check(bonded.trustsEnoughForBed(stranger) && bonded.bedChanceFor(stranger) == Monsterization.EAGER, "a harpy the realm changed is as eager as any");
        HarpyTestUtil.remove(stranger);
        var frog = helper.spawn(EntityType.FROG, new BlockPos(3, 2, 3));
        frog.setVariant(level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.FROG_VARIANT).getHolderOrThrow(net.minecraft.world.entity.animal.FrogVariant.WARM));
        check(Monsterization.otherOf(frog) == com.linkyoshi90.monstergirls.registry.ModEntities.TREE_FROG.get(), "a warm frog becomes a tree frog");
        frog.setVariant(level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.FROG_VARIANT).getHolderOrThrow(net.minecraft.world.entity.animal.FrogVariant.COLD));
        check(Monsterization.otherOf(frog) == com.linkyoshi90.monstergirls.registry.ModEntities.BULLFROG.get(), "a cold one a bullfrog");
        check(Monsterization.kindOf(helper.spawn(EntityType.TURTLE, new BlockPos(11, 2, 3))) == GirlSpecies.TURTLE_LIZARDFOLK, "a turtle a turtle girl");
        check(Monsterization.kindOf(helper.spawn(EntityType.PIGLIN, new BlockPos(11, 2, 11))) == GirlSpecies.PIGLETTE_ARMOURED, "a piglin a piglette");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "possessedHenHatchesHarpies")
    public static void possessedHenHatchesHarpies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var hen = helper.spawn(EntityType.CHICKEN, new BlockPos(4, 2, 4));
        var rooster = helper.spawn(EntityType.CHICKEN, new BlockPos(6, 2, 4));
        hen.setNoAi(true);
        rooster.setNoAi(true);
        Monsterization.possess(level, hen);
        ready(hen);
        var chick = Monsterization.breed(level, hen);
        check(chick instanceof com.linkyoshi90.monstergirls.entity.PoultryHarpy p && p.isBaby() && hen.isAlive(), "her chick is a little poultry harpy");
        var trader = helper.spawn(EntityType.WANDERING_TRADER, new BlockPos(10, 2, 10));
        trader.setNoAi(true);
        Monsterization.possess(level, trader);
        GirlSpecies kind = Monsterization.offspringOf(trader);
        check(kind == GirlSpecies.SUCCUBUS_DEVIL || kind == GirlSpecies.DEMON_DEVIL, "a wandering trader is possessed like a villager");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "possessedCatBringsHeartfruit")
    public static void possessedCatBringsHeartfruit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var cat = helper.spawn(EntityType.CAT, new BlockPos(6, 2, 6));
        cat.setNoAi(true);
        cat.tame(p);
        check(!Monsterization.giftOwner(level, cat, true), "an ordinary cat brings no heartfruit");
        Monsterization.possess(level, cat);
        check(!Monsterization.giftOwner(level, cat, false), "only in the morning");
        check(Monsterization.giftOwner(level, cat, true), "a possessed cat brings her owner a heartfruit");
        check(!Monsterization.giftOwner(level, cat, true), "once a day");
        check(!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(2),
                e -> e.getItem().is(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get())).isEmpty(), "it lies at their feet");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "possessedTradersDealInTheRealm")
    public static void possessedTradersDealInTheRealm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var trader = helper.spawn(EntityType.WANDERING_TRADER, new BlockPos(6, 2, 6));
        trader.setNoAi(true);
        int before = trader.getOffers().size();
        Monsterization.possess(level, trader);
        check(trader.getOffers().stream().anyMatch(o -> o.getResult().is(com.linkyoshi90.monstergirls.registry.ModItems.REALM_CENSER.get())), "she sells a realm censer");
        check(trader.getOffers().size() == before + 4, "four realm trades besides her own");
        Monsterization.exorcise(level, trader);
        check(trader.getOffers().size() == before, "freed, she deals only in her own wares again");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "heartfruitReadiesThePossessed")
    public static void heartfruitReadiesThePossessed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 4));
        var boar = helper.spawn(EntityType.PIG, new BlockPos(6, 2, 4));
        pig.setNoAi(true);
        boar.setNoAi(true);
        Monsterization.possess(level, pig);
        check(pig.goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof net.minecraft.world.entity.ai.goal.TemptGoal), "heartfruit tempts her");
        check(Monsterization.breed(level, pig) == null, "she must wait a while after the change");
        check(Monsterization.feed(level, pig), "a heartfruit makes her ready at once");
        check(Monsterization.breed(level, pig) != null, "and her young is born");
        check(!Monsterization.feed(level, boar), "an ordinary pig cares nothing for it");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "possessionSpreadsAtNight")
    public static void possessionSpreadsAtNight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var first = helper.spawn(EntityType.VILLAGER, new BlockPos(5, 2, 5));
        var near = helper.spawn(EntityType.VILLAGER, new BlockPos(7, 2, 5));
        var far = helper.spawn(EntityType.VILLAGER, new BlockPos(13, 2, 13));
        for (var v : List.of(first, near, far)) v.setNoAi(true);
        Monsterization.possess(level, first);
        check(Monsterization.spread(level, first, false) == null, "not by day");
        check(Monsterization.spread(level, first, true) == near && Monsterization.possessed(near), "at night it passes to the villager beside her");
        check(Monsterization.spread(level, first, true) == null && !Monsterization.possessed(far), "but not across the village at once");
        var pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 6));
        Monsterization.possess(level, pig);
        check(Monsterization.spread(level, pig, true) == null, "only villagers pass it on");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        try {
            var dispatcher = level.getServer().getCommands().getDispatcher();
            check(dispatcher.execute("monstergirls realm list 32", p.createCommandSourceStack()) == 3, "the list counts the three possessed");
            check(dispatcher.execute("monstergirls bonds 8", p.createCommandSourceStack()) >= 0, "and /monstergirls bonds still works beside it");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw new net.minecraft.gametest.framework.GameTestAssertException("the command failed: " + e.getMessage());
        }
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aTurningVillage")
    public static void aTurningVillage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(7, 2, 7));
        golem.setNoAi(true);
        check(!Monsterization.guardsTheTurned(golem), "a golem among ordinary villagers is just a golem");
        List<net.minecraft.world.entity.npc.Villager> folk = new java.util.ArrayList<>();
        for (int i = 0; i < Monsterization.TURNED_VILLAGE; i++) {
            var v = helper.spawn(EntityType.VILLAGER, new BlockPos(3 + i * 2, 2, 3));
            v.setNoAi(true);
            Monsterization.possess(level, v);
            folk.add(v);
        }
        check(Monsterization.guardsTheTurned(golem), "among three possessed she soaks up the realm");
        check(Monsterization.step(level, golem, true) != null || Monsterization.soaked(golem) == 1, "and begins to turn, no realm under her");
        var root = Monsterization.takeRoot(level, folk.get(0), true);
        check(root != null && level.getBlockState(root).is(com.linkyoshi90.monstergirls.registry.ModBlocks.REALM_GRASS.get()), "the realm takes root under a possessed villager");
        check(level.getBlockState(root).getValue(RealmGrassBlock.DEPTH) == Monsterization.ROOT_DEPTH, "near its edge, so it stays small");
        int freed = com.linkyoshi90.monstergirls.entity.ThrownHolyWater.splash(level, folk.get(1).blockPosition(), null);
        check(freed == Monsterization.TURNED_VILLAGE && folk.stream().noneMatch(Monsterization::possessed), "a splash of holy water frees them all");
        check(!level.getBlockState(root).is(com.linkyoshi90.monstergirls.registry.ModBlocks.REALM_GRASS.get()), "and turns the grass back");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "holyFontWards")
    public static void holyFontWards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.setBlock(helper.absolutePos(new BlockPos(2, 2, 2)), com.linkyoshi90.monstergirls.registry.ModBlocks.HOLY_FONT.get().defaultBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_ALL);
        helper.runAfterDelay(2, () -> {
            BlockPos ground = helper.absolutePos(new BlockPos(7, 1, 7));
            check(!RealmGrassBlock.corrupt(level, ground), "the realm can't take hold near a holy font");
            var villager = helper.spawn(EntityType.VILLAGER, new BlockPos(7, 2, 7));
            villager.setNoAi(true);
            check(!Monsterization.soak(level, villager, true) && Monsterization.soaked(villager) == 0, "nobody soaks it up there");
            level.removeBlock(helper.absolutePos(new BlockPos(2, 2, 2)), false);   // its ward must not reach the arenas beside this one
            helper.succeed();
        });
    }
}
