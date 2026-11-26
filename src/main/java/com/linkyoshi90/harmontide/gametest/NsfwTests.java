package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.BedSharing;
import com.linkyoshi90.harmontide.entity.DuckHarpy;
import com.linkyoshi90.harmontide.entity.HandMilking;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.MorningOral;
import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.WildEncounter;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Private-server adult content (entity/Nsfw): off unless asked, and never for the young. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class NsfwTests {
    @SuppressWarnings("unchecked")
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies species, BlockPos at) {
        AbstractMonsterGirl girl = (AbstractMonsterGirl) helper.spawn(ModGirls.type(species), at);
        girl.setNoAi(true);
        return girl;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "nsfwStaysOffForTheYoung")
    public static void nsfwStaysOffForTheYoung(GameTestHelper helper) {
        check(!HarmontideConfig.NSFW.getDefault(), "off unless a private world asks");
        check(Math.abs(Nsfw.along(0)) < 1.0E-6 && Math.abs(Nsfw.along(3)) > 0.05, "the rock actually moves");
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl grown = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(3, 2, 3));
        AbstractMonsterGirl young = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 3));
        AbstractMonsterGirl fairy = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(7, 2, 3));
        AbstractMonsterGirl man = still(helper, GirlSpecies.GANDHARVA_ELF, new BlockPos(9, 2, 3));
        young.setBaby(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 5));
        grown.setAffection(player.getUUID(), 40);
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(false);
            check(!Nsfw.on() && Nsfw.callKey().equals("message.harmontide.call_friends"), "the cuddle call");
            check(Nsfw.line(grown, player, Nsfw.Kind.FLIRT) == null, "a pat stays a pat");
            check(!Nsfw.rock(level, grown, player, grown.position(), 0.0F), "she lies still");
            HarmontideConfig.NSFW.set(true);
            check(Nsfw.callKey().equals("message.harmontide.call_friends_nsfw"), "the blunt call");
            check(!Nsfw.adult(young, player) && !Nsfw.rock(level, young, player, young.position(), 0.0F), "a baby stays out");
            check(!Nsfw.adult(fairy, player) && Nsfw.line(fairy, player, Nsfw.Kind.BED) == null, "childlike height stays out");
            check(!Nsfw.adult(man, player), "he is a man");
            grown.setFather(player.getUUID());
            check(Nsfw.adult(grown, player) && !grown.trustsEnoughForBed(player)
                    && !Nsfw.rock(level, grown, player, grown.position(), 0.0F), "no sex without a condom");
            player.getInventory().add(new ItemStack(ModItems.CONDOM.get()));
            check(grown.trustsEnoughForBed(player) && Nsfw.rock(level, grown, player, grown.position(), 0.0F), "a condom, and she will");
            grown.setFather(null);
            player.getInventory().clearContent();
            check(Nsfw.adult(grown, player), "a grown partner");
            String flirt = Nsfw.line(grown, player, Nsfw.Kind.FLIRT);
            check(flirt != null && flirt.startsWith("message.harmontide.nsfw_flirt."), "she says so");
            grown.tickCount = 3;
            Vec3 spot = grown.position();
            check(Nsfw.bipedal(grown), "a serval has the legs to kneel");
            check(Nsfw.rock(level, grown, player, spot, 0.0F), "she kneels");
            check(Math.abs(grown.getX() - spot.x) < 1.0E-4 && Math.abs(grown.getZ() - spot.z) < 1.0E-4, "she stays astride");
            check(Math.abs(grown.getY() - spot.y) > 0.02 && grown.getAction() == AbstractMonsterGirl.Action.KNEEL, "she bounces");
            AbstractMonsterGirl horse = still(helper, GirlSpecies.CAMEL_CENTAUR, new BlockPos(1, 2, 7));
            horse.tickCount = 3;
            Vec3 hoof = horse.position();
            check(!Nsfw.bipedal(horse) && Nsfw.quadruped(horse) && Nsfw.rock(level, horse, player, hoof, 0.0F), "a camel sits back");
            check(Math.abs(horse.getX() - hoof.x) < 1.0E-4 && Math.abs(horse.getZ() - hoof.z) < 1.0E-4, "she stays over him");
            check(Math.abs(horse.getY() - hoof.y) > 0.02 && horse.getAction() == AbstractMonsterGirl.Action.SITBACK, "her back lowers and rises");
            AbstractMonsterGirl snake = still(helper, GirlSpecies.BOA_LAMIA, new BlockPos(11, 2, 7));
            snake.tickCount = 3;
            Vec3 coil = snake.position();
            check(!Nsfw.quadruped(snake) && Nsfw.rock(level, snake, player, coil, 0.0F), "a serpent lies on him");
            check(Math.abs(snake.getX() - coil.x) < 1.0E-4 && Math.abs(snake.getZ() - coil.z) < 1.0E-4, "she stays along him");
            check(Math.abs(snake.getY() - coil.y) > 0.02 && snake.getAction() == AbstractMonsterGirl.Action.COUPLE, "her hips rise");
            AbstractMonsterGirl spider = still(helper, GirlSpecies.BLACK_WIDOW_ARACHNE, new BlockPos(4, 2, 12));
            spider.tickCount = 3;
            Vec3 web = spider.position();
            check(Nsfw.rock(level, spider, player, web, 0.0F), "a spider stands over him");
            check(Math.abs(spider.getZ() - web.z) < 1.0E-4 && spider.getAction() == AbstractMonsterGirl.Action.COUPLE, "her legs do the bending");
            AbstractMonsterGirl mer = still(helper, GirlSpecies.LORELEI_MERMAID, new BlockPos(8, 2, 12));
            mer.tickCount = 3;
            Vec3 tide = mer.position();
            check(Nsfw.rock(level, mer, player, tide, 0.0F) && mer.getAction() == AbstractMonsterGirl.Action.COUPLE, "a mermaid lies on him");
            check(Math.abs(mer.getY() - tide.y) > 0.02, "her tail pumps");
            AbstractMonsterGirl blob = still(helper, GirlSpecies.HONEY_SLIME, new BlockPos(12, 2, 12));
            blob.tickCount = 3;
            Vec3 puddle = blob.position();
            check(Nsfw.rock(level, blob, player, puddle, 0.0F) && blob.getAction() == AbstractMonsterGirl.Action.COUPLE, "a slime bounces");
            check(Math.abs(blob.getY() - puddle.y) > 0.05 && Math.abs(blob.getZ() - puddle.z) < 1.0E-4, "up and down, like a harpy");
            check(Nsfw.say(level, grown, player, Nsfw.Kind.MORNING), "the morning after");
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "nsfwStaysOffForTheYoung")
    public static void nsfwWifeSpeaksPlainly(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(4, 2, 4), ModBlocks.HEARTH.get());
        AbstractMonsterGirl wife = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 5));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        wife.setAffection(player.getUUID(), 100);
        boolean was = HarmontideConfig.NSFW.get();
        boolean flirting = HarmontideConfig.FLIRTING.get();
        helper.runAfterDelay(3, () -> {
            try {
                HarmontideConfig.FLIRTING.set(true);
                HarmontideConfig.NSFW.set(true);
                check(Marriage.propose(level, wife, player) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, wife, player), "wed");
                String key = Nsfw.line(wife, player, Nsfw.Kind.WIFE);
                check(key != null && key.startsWith("message.harmontide.nsfw_wife."), "a wife's line");
                check(Nsfw.line(wife, player, Nsfw.Kind.TUB) != null && Nsfw.line(wife, player, Nsfw.Kind.TUB).startsWith("message.harmontide.nsfw_tub."), "and in the tub");
            } finally {
                HarmontideConfig.NSFW.set(was);
                HarmontideConfig.FLIRTING.set(flirting);
            }
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    private static void interact(AbstractMonsterGirl girl, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        girl.interact(player, InteractionHand.MAIN_HAND);
    }

    private static Minotaur cow(GameTestHelper helper, GirlSpecies species, BlockPos at, boolean male) {
        Minotaur mino = (Minotaur) still(helper, species, at);
        mino.setMale(male);
        return mino;
    }

    /** Off, a cow's bucket fills at once. On, any grown girl opens the close-up, and the pail fills only after the pulls. */
    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "nsfwStaysOffForTheYoung")
    public static void nsfwBucketMilkingIsAMinigame(GameTestHelper helper) {
        boolean was = HarmontideConfig.NSFW.get();
        ServerPlayer player = null;
        boolean handedOff = false;
        try {
            ServerLevel level = helper.getLevel();
            player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
            Minotaur cow = cow(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(4, 2, 2), false);
            Minotaur bull = cow(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(8, 2, 2), true);
            Minotaur calf = cow(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(6, 2, 4), false);
            calf.setBaby(true);
            Minotaur daughter = cow(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(10, 2, 2), false);
            daughter.setFather(player.getUUID());
            Minotaur holstaur = cow(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(4, 2, 5), false);
            Centaur camel = (Centaur) still(helper, GirlSpecies.CAMEL_CENTAUR, new BlockPos(4, 2, 8));
            AbstractMonsterGirl fairy = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(8, 2, 6));
            ServerPlayer who = player;
            HarmontideConfig.NSFW.set(false);
            check(!HandMilking.offer(level, cow, who, InteractionHand.MAIN_HAND, false) && !HandMilking.isOpen(who), "off, no close-up");
            interact(cow, who, new ItemStack(Items.BUCKET));
            check(who.getInventory().countItem(Items.MILK_BUCKET) == 1 && !HandMilking.isOpen(who), "off, the pail fills at once");
            HarmontideConfig.NSFW.set(true);
            AbstractMonsterGirl dry = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(8, 2, 8));
            check(HandMilking.interested(dry, who), "a grown girl, milk or not");
            who.getInventory().selected = 1;
            interact(dry, who, new ItemStack(Items.BUCKET));
            check(HandMilking.isOpen(who) && who.getInventory().countItem(Items.MILK_BUCKET) == 1, "she undresses, the pail waits");
            check(dry.hasFlag(AbstractMonsterGirl.FLAG_BARE), "her top is off");
            HandMilking.close(who);
            check(!HandMilking.isOpen(who) && !dry.hasFlag(AbstractMonsterGirl.FLAG_BARE) && who.getMainHandItem().is(Items.BUCKET), "closing dresses her");
            check(!HandMilking.offer(level, fairy, who, InteractionHand.MAIN_HAND, false) && !HandMilking.isOpen(who), "a little one is not undressed");
            check(!Nsfw.adult(bull, who) && !HandMilking.offer(level, bull, who, InteractionHand.MAIN_HAND, false), "a bull is not undressed");
            who.getInventory().selected = 7;
            interact(bull, who, new ItemStack(Items.BUCKET));
            check(who.getInventory().countItem(Items.MILK_BUCKET) == 1 && !HandMilking.isOpen(who), "bulls give no milk");
            check(!HandMilking.offer(level, calf, who, InteractionHand.MAIN_HAND, false), "a calf is not undressed");
            who.getInventory().selected = 6;
            interact(calf, who, new ItemStack(Items.BUCKET));
            check(who.getInventory().countItem(Items.MILK_BUCKET) == 1 && !HandMilking.isOpen(who), "a calf gives no milk");
            check(Nsfw.adult(daughter, who) && !HandMilking.interested(daughter, who), "grown, and milking stays the old pail");
            who.getInventory().selected = 5;
            interact(daughter, who, new ItemStack(Items.BUCKET));
            check(who.getInventory().countItem(Items.MILK_BUCKET) == 2 && !HandMilking.isOpen(who), "her father gets the old pail, not the close-up");
            who.getInventory().selected = 4;
            interact(holstaur, who, new ItemStack(Items.GLASS_BOTTLE));
            check(HandMilking.isOpen(who) && who.getMainHandItem().is(Items.GLASS_BOTTLE), "a holstaur keeps the bottle");
            check(who.getInventory().countItem(ModItems.HOLSTAUR_MILK.get()) == 0, "the bottle is empty until she is");
            HandMilking.close(who);
            check(!HandMilking.isOpen(who) && who.getInventory().countItem(ModItems.HOLSTAUR_MILK.get()) == 0, "closing early gives nothing");
            check(Nsfw.adult(camel, who), "a grown camel");
            who.getInventory().selected = 3;
            interact(camel, who, new ItemStack(Items.BUCKET));
            check(HandMilking.isOpen(who) && who.getInventory().countItem(Items.MILK_BUCKET) == 2, "a camel waits to be emptied");
            HandMilking.close(who);
            check(!HandMilking.isOpen(who) && who.getMainHandItem().is(Items.BUCKET), "closing keeps the bucket");
            Centaur giraffe = (Centaur) still(helper, GirlSpecies.GIRAFFE_CENTAUR, new BlockPos(1, 2, 10));
            giraffe.getGestation().conceive();
            check(!giraffe.canShareBed() && HandMilking.interested(giraffe, who), "expecting, and the bucket still opens");
            who.getInventory().selected = 5;
            interact(giraffe, who, new ItemStack(Items.BUCKET));
            check(HandMilking.isOpen(who) && giraffe.hasFlag(AbstractMonsterGirl.FLAG_BARE)
                    && giraffe.getAction() != AbstractMonsterGirl.Action.MILK, "a giraffe undresses, and does not play a clip she lacks");
            HandMilking.close(who);
            Centaur deer = (Centaur) still(helper, GirlSpecies.REINDEER_CENTAUR, new BlockPos(12, 2, 10));
            interact(deer, who, new ItemStack(Items.BUCKET));
            check(HandMilking.isOpen(who) && deer.hasFlag(AbstractMonsterGirl.FLAG_BARE), "a cervid opens the close-up");
            HandMilking.close(who);
            DuckHarpy duck = helper.spawn(ModEntities.DUCK_HARPY.get(), new BlockPos(12, 2, 12));
            duck.setNoAi(true);
            duck.getGestation().conceive();
            check(!duck.canShareBed() && HandMilking.interested(duck, who), "a duck laying an egg still opens it");
            who.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            duck.interact(who, InteractionHand.MAIN_HAND);
            check(HandMilking.isOpen(who) && duck.isBare(), "a duck undresses");
            HandMilking.close(who);
            cow.setNextMilk(0);
            who.getInventory().selected = 8;
            interact(cow, who, new ItemStack(Items.BUCKET));
            check(HandMilking.isOpen(who) && who.getMainHandItem().is(Items.BUCKET), "she slips her top down, bucket still in hand");
            check(who.getInventory().countItem(Items.MILK_BUCKET) == 2, "nothing in the pail yet");
            handedOff = true;
            pull(helper, who, cow, 1, 2, was);
        } finally {
            if (!handedOff) {
                HarmontideConfig.NSFW.set(was);
                if (player != null) HarpyTestUtil.remove(player);
            }
        }
    }

    private static void pull(GameTestHelper helper, ServerPlayer player, Minotaur cow, int done, int milkBefore, boolean was) {
        helper.runAfterDelay(4, () -> {
            boolean more = false;
            try {
                if (done == 1) {
                    check(HandMilking.stroke(player), "the first pull");
                    check(!HandMilking.stroke(player), "a second pull in the same moment does nothing");
                    check(HandMilking.strokes(player) == 1, "one stroke counted");
                } else {
                    check(HandMilking.stroke(player), "pull " + done);
                }
                if (done < HandMilking.STROKES) {
                    more = true;
                    pull(helper, player, cow, done + 1, milkBefore, was);
                    return;
                }
                check(!HandMilking.isOpen(player), "the session ends when she is empty");
                check(player.getInventory().countItem(Items.MILK_BUCKET) == milkBefore + 1, "one pail, once she is empty");
                check(!player.getMainHandItem().is(Items.BUCKET), "the empty bucket was used");
                player.getInventory().selected = 7;
                interact(cow, player, new ItemStack(Items.BUCKET));
                check(player.getInventory().countItem(Items.MILK_BUCKET) == milkBefore + 1 && !HandMilking.isOpen(player), "not again right away");
                helper.succeed();
            } finally {
                if (!more) {
                    HarmontideConfig.NSFW.set(was);
                    HarpyTestUtil.remove(player);
                }
            }
        });
    }

    /** A grown daughter will, with a condom, and the night does not take. A little one still will not. */
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aGrownDaughterUsesACondom")
    public static void aGrownDaughterUsesACondom(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl daughter = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        AbstractMonsterGirl fairy = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(8, 2, 4));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 4));
        daughter.setFather(player.getUUID());
        daughter.setAffection(player.getUUID(), 100);
        fairy.setFather(player.getUUID());
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(true);
            check(!daughter.trustsEnoughForBed(player) && !Nsfw.rock(level, daughter, player, daughter.position(), 0.0F), "no condom, no sex");
            player.getInventory().add(new ItemStack(ModItems.CONDOM.get()));
            check(daughter.trustsEnoughForBed(player), "the condom changes her mind");
            player.getInventory().add(new ItemStack(ModItems.CONDOM.get()));
            check(!Nsfw.adult(fairy, player) && !fairy.trustsEnoughForBed(player), "a little one stays out, condom or not");
            daughter.getGestation().setSharingWith(player.getUUID());
            player.startSleeping(helper.absolutePos(new BlockPos(6, 2, 4)));
            BedSharing.onNightSkipped(level);
            check(!daughter.getGestation().isCarrying(), "the condom held");
            check(player.getInventory().countItem(ModItems.CONDOM.get()) == 1, "one was used, one is left");
            check(MorningOral.pending(daughter), "she stays for the morning");
            for (int i = 0; i < 5; i++) MorningOral.step(level);
            check(daughter.getAction() == AbstractMonsterGirl.Action.ORAL, "she kneels to it");
            for (int i = 0; i < MorningOral.TICKS; i++) MorningOral.step(level);
            check(!MorningOral.pending(daughter) && daughter.getAction() != AbstractMonsterGirl.Action.ORAL, "the morning ends");
            player.stopSleeping();
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    /** A wild grown girl catches him, grows fond, and leaves him restored. The young stay out. A daughter needs a condom and does not conceive. */
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aWildGirlCatchesHim")
    public static void aWildGirlCatchesHim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl daughter = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        AbstractMonsterGirl baby = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 4));
        AbstractMonsterGirl fairy = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(9, 2, 4));
        AbstractMonsterGirl man = still(helper, GirlSpecies.GANDHARVA_ELF, new BlockPos(2, 2, 4));
        AbstractMonsterGirl stranger = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 6));
        baby.setBaby(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 5));
        daughter.setFather(player.getUUID());
        boolean was = HarmontideConfig.NSFW.get();
        int odds = HarmontideConfig.CONCEPTION_CHANCE.get();
        try {
            HarmontideConfig.NSFW.set(false);
            check(!WildEncounter.start(level, daughter, player), "off, and she walks on");
            HarmontideConfig.NSFW.set(true);
            check(!WildEncounter.start(level, baby, player), "a baby stays out");
            check(!WildEncounter.start(level, fairy, player), "childlike height stays out");
            check(!WildEncounter.start(level, man, player), "he is a man");
            check(!WildEncounter.start(level, daughter, player), "no condom, no catching her father");
            player.getInventory().add(new ItemStack(ModItems.CONDOM.get()));
            check(WildEncounter.start(level, daughter, player), "a condom, and she will");
            for (int i = 0; i < WildEncounter.TACKLE + WildEncounter.SCENE; i++) WildEncounter.step(level);
            check(!WildEncounter.active(daughter) && daughter.getAction() != AbstractMonsterGirl.Action.KNEEL, "she lets go");
            check(!player.isSwimming() && player.getPose() != Pose.SWIMMING, "he is back on his feet");
            check(!daughter.getGestation().isCarrying(), "the condom held");
            check(player.getInventory().countItem(ModItems.CONDOM.get()) == 0, "the condom was used");
            check(daughter.getAffection(player) == WildEncounter.AFFECTION, "she is fond of him");
            HarmontideConfig.CONCEPTION_CHANCE.set(100);
            player.getFoodData().setFoodLevel(8);
            check(WildEncounter.start(level, stranger, player), "a stranger comes for him");
            WildEncounter.step(level);
            check(WildEncounter.active(stranger) && stranger.getAction() == AbstractMonsterGirl.Action.POUNCE
                    && !player.isSwimming(), "she is still tackling");
            for (int i = 1; i < WildEncounter.TACKLE; i++) WildEncounter.step(level);
            check(player.isSwimming() && player.getPose() == Pose.SWIMMING
                    && stranger.getAction() != AbstractMonsterGirl.Action.KNEEL, "he is down, and the pose has not started");
            WildEncounter.step(level);
            check(stranger.getAction() == AbstractMonsterGirl.Action.KNEEL && player.isSwimming(), "then she is on him");
            for (int i = 1; i < WildEncounter.SCENE; i++) WildEncounter.step(level);
            check(!WildEncounter.active(stranger) && stranger.getAction() != AbstractMonsterGirl.Action.KNEEL, "then she lets go");
            check(!player.isSwimming(), "and he gets up");
            check(stranger.getGestation().isCarrying(), "she may conceive");
            check(stranger.getAffection(player) == WildEncounter.AFFECTION, "and she is fond of him");
            check(player.hasEffect(MobEffects.REGENERATION) && player.hasEffect(MobEffects.ABSORPTION), "he is restored");
            check(player.getFoodData().getFoodLevel() >= 12, "and he is less hungry");
        } finally {
            HarmontideConfig.CONCEPTION_CHANCE.set(odds);
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
