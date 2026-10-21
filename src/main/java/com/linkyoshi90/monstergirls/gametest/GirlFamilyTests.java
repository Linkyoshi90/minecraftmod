package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Gestation;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.SlimeGirl;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The families after the harpies and anurans (field guides III to IX). Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class GirlFamilyTests {

    @SuppressWarnings("unchecked")
    private static <T extends AbstractMonsterGirl> T spawn(GameTestHelper helper, GirlSpecies s, BlockPos pos) {
        return (T) helper.spawn(ModGirls.type(s), pos);
    }

    private static List<AbstractMonsterGirl> youngAround(GameTestHelper helper, BlockPos pos, double r, EntityType<?> type) {
        return helper.getLevel().getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(helper.absolutePos(pos)).inflate(r), g -> g.isBaby() && g.getType() == type);
    }

    private static void pool(GameTestHelper helper, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(cx + dx, 2, cz + dz), Blocks.WATER);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "everySpeciesSpawnsAndSaves")
    public static void everySpeciesSpawnsAndSaves(GameTestHelper helper) {
        UUID father = UUID.randomUUID();
        int i = 0;
        for (GirlSpecies s : GirlSpecies.values()) {
            AbstractMonsterGirl girl = spawn(helper, s, new BlockPos(1 + (i % 6) * 2, 2, 1 + (i / 6) * 2));
            i++;
            check(girl.getSpecies() == s, s + " has the wrong species");
            check(s.variants().size() == 5, s + " should have five colours");
            girl.setVariant(s.variants().get(3));
            check(girl.getVariantSuffix().equals("_" + s.variants().get(3)), s + " variant suffix: " + girl.getVariantSuffix());
            girl.setAffection(father, 42);
            girl.setFather(father);
            CompoundTag tag = new CompoundTag();
            girl.saveWithoutId(tag);
            AbstractMonsterGirl copy = ModGirls.type(s).create(helper.getLevel());
            copy.load(tag);
            check(copy.getVariantName().equals(s.variants().get(3)), s + " lost its variant");
            check(copy.getAffection(father) == 42 && father.equals(copy.getFather()), s + " lost its bonds");
            check(Math.abs(girl.getBbHeight() - s.height()) < 0.01, s + " height " + girl.getBbHeight());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "nekomataLitterIsDevoted")
    public static void nekomataLitterIsDevoted(GameTestHelper helper) {
        Nekomata cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        UUID father = UUID.randomUUID();
        cat.getGestation().conceive(father);
        cat.getGestation().setTicksLeft(Gestation.DURATION / 2);
        helper.runAfterDelay(20, () -> {
            check(Math.abs(cat.getBellySwell() - 0.5F) < 0.05F, "her belly should be half round: " + cat.getBellySwell());
            cat.getGestation().setTicksLeft(0);
            helper.succeedWhen(() -> {
                List<AbstractMonsterGirl> kittens = youngAround(helper, new BlockPos(7, 2, 7), 10, cat.getType());
                check(kittens.size() >= 2 && kittens.size() <= 3, "a litter of two or three: " + kittens.size());
                for (AbstractMonsterGirl k : kittens) {
                    check(k.getAffection(father) == Bonded.FULL_AFFECTION && father.equals(k.getFather()), "kittens are devoted to their father");
                    check(k.getBbHeight() < 1.0F, "kittens are small");
                }
                check(!cat.getGestation().isCarrying(), "she has given birth");
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "pythonLaysAClutchThatHatches")
    public static void pythonLaysAClutchThatHatches(GameTestHelper helper) {
        Lamia python = spawn(helper, GirlSpecies.PYTHON_LAMIA, new BlockPos(7, 2, 7));
        UUID father = UUID.randomUUID();
        python.getGestation().conceive(father);
        python.getGestation().setTicksLeft(0);
        helper.succeedWhen(() -> {
            BlockPos clutch = null;
            for (BlockPos p : BlockPos.betweenClosed(new BlockPos(2, 1, 2), new BlockPos(12, 4, 12))) {
                if (helper.getBlockState(p).is(ModBlocks.GIRL_EGG.get())) clutch = p.immutable();
            }
            check(clutch != null, "no clutch yet");
            check(helper.getBlockState(clutch).getValue(GirlEggBlock.KIND) == GirlEggBlock.Kind.LEATHERY, "leathery eggs");
            GirlEggBlockEntity be = (GirlEggBlockEntity) helper.getBlockEntity(clutch);
            check(GirlSpecies.PYTHON_LAMIA.id().equals(be.getSpecies()) && father.equals(be.getFather()), "the clutch remembers species and father");
            int count = be.getCount();
            GirlEggBlock.hatch(helper.getLevel(), helper.absolutePos(clutch));
            List<AbstractMonsterGirl> young = youngAround(helper, clutch, 4, python.getType());
            check(young.size() == count, "one snakelet per egg: " + young.size() + "/" + count);
            check(young.stream().allMatch(y -> y.getAffection(father) == Bonded.FULL_AFFECTION), "devoted to their father");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "spidersSacAndWolfSpiderBrood")
    public static void spidersSacAndWolfSpiderBrood(GameTestHelper helper) {
        Arachne weaver = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(3, 2, 3));
        Arachne wolf = spawn(helper, GirlSpecies.WOLF_SPIDER_ARACHNE, new BlockPos(11, 2, 11));
        weaver.getGestation().conceive(null);
        weaver.getGestation().setTicksLeft(0);
        wolf.getGestation().conceive(null);
        wolf.getGestation().setTicksLeft(0);
        helper.succeedWhen(() -> {
            boolean sac = false;
            for (BlockPos p : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(7, 4, 7))) {
                if (helper.getBlockState(p).is(ModBlocks.GIRL_EGG.get()) && helper.getBlockState(p).getValue(GirlEggBlock.KIND) == GirlEggBlock.Kind.SILK) sac = true;
            }
            check(sac, "the orb weaver should have spun an egg sac");
            check(wolf.hasFlag(AbstractMonsterGirl.FLAG_CARRYING), "the wolf spider carries her young");
            check(!youngAround(helper, new BlockPos(11, 2, 11), 6, wolf.getType()).isEmpty(), "and has spiderlings around her");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "koiLaysRoeOrcaHasACalf")
    public static void koiLaysRoeOrcaHasACalf(GameTestHelper helper) {
        pool(helper, 4, 4);
        pool(helper, 10, 10);
        Merfolk koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(4, 2, 4));
        Merfolk orca = spawn(helper, GirlSpecies.ORCA_MERMAID, new BlockPos(10, 2, 10));
        koi.getGestation().conceive(null);
        koi.getGestation().setTicksLeft(0);
        orca.getGestation().conceive(null);
        orca.getGestation().setTicksLeft(0);
        helper.succeedWhen(() -> {
            boolean roe = false;
            for (BlockPos p : BlockPos.betweenClosed(new BlockPos(3, 2, 3), new BlockPos(5, 2, 5))) {
                if (helper.getBlockState(p).is(ModBlocks.GIRL_EGG.get())) roe = helper.getBlockState(p).getValue(GirlEggBlock.WATERLOGGED);
            }
            check(roe, "koi roe should lie in the water");
            check(youngAround(helper, new BlockPos(10, 2, 10), 6, orca.getType()).size() == 1, "one orca calf");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stickSitsEveryFamily")
    public static void stickSitsEveryFamily(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        int i = 0;
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.TIGER_NEKOMATA, GirlSpecies.RED_KITSUNE, GirlSpecies.BOA_LAMIA, GirlSpecies.TARANTULA_ARACHNE,
                GirlSpecies.HORSE_CENTAUR, GirlSpecies.SEAL_MERMAID, GirlSpecies.HONEY_SLIME}) {
            AbstractMonsterGirl g = spawn(helper, s, new BlockPos(4 + (i % 4) * 2, 2, 4 + (i / 4) * 3));
            i++;
            g.interact(player, InteractionHand.MAIN_HAND);
            check(g.isOrderedToSit() && g.getAction() == AbstractMonsterGirl.Action.SITTING, s + " should sit: " + g.getAction());
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "kitsuneTailsFollowTheBond")
    public static void kitsuneTailsFollowTheBond(GameTestHelper helper) {
        Kitsune fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(7, 2, 7));
        UUID p = UUID.randomUUID();
        check(fox.visibleTails() == 1, "one tail when wild");
        fox.setAffection(p, Bonded.BED_AFFECTION);
        helper.runAfterDelay(25, () -> {
            check(fox.visibleTails() == 2, "two tails at high friendship: " + fox.visibleTails());
            fox.setAffection(p, 60);
            helper.runAfterDelay(25, () -> {
                check(fox.visibleTails() == 3, "three when devoted: " + fox.visibleTails());
                fox.setAffection(p, Bonded.FULL_AFFECTION);
                fox.setDevotedSince(helper.getLevel().getGameTime() - AbstractMonsterGirl.TRUE_FORM_TICKS - 1);
                helper.runAfterDelay(120, () -> {
                    check(fox.isTrueForm() && fox.visibleTails() == 9, "nine tails for a kyubi: " + fox.visibleTails());
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kitsuneDecoyAndStash")
    public static void kitsuneDecoyAndStash(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Kitsune fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(7, 2, 7));
        fox.onStashFound(player);
        check(fox.getAffection(player) == 3, "finding her stash is worth 3: " + fox.getAffection(player));
        fox.hurt(helper.getLevel().damageSources().playerAttack(player), fox.getMaxHealth() * 0.5F);
        long decoys = helper.getLevel().getEntitiesOfClass(Kitsune.class, new AABB(fox.blockPosition()).inflate(4), Kitsune::isDecoy).size();
        check(decoys == 2, "a red fox leaves two decoys: " + decoys);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "nekomataPatienceAndSwat")
    public static void nekomataPatienceAndSwat(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Nekomata cat = spawn(helper, GirlSpecies.LYNX_NEKOMATA, new BlockPos(8, 2, 8));
        cat.setFlag(AbstractMonsterGirl.FLAG_CALM, true);                 // a wild cat must be calmed with meat first
        helper.succeedWhen(() -> {
            check(cat.getAffection(player) >= 1, "standing still should win her over a little");
            cat.setAffection(player.getUUID(), 5);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int i = 0; i < 5; i++) cat.interact(player, InteractionHand.MAIN_HAND);
            check(cat.getAction() == AbstractMonsterGirl.Action.HISS, "too much petting: she swats");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "centaurGroomRideAndPack")
    public static void centaurGroomRideAndPack(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Centaur horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 5));
        Centaur zebra = spawn(helper, GirlSpecies.ZEBRA_CENTAUR, new BlockPos(9, 2, 5));
        Centaur alpaca = spawn(helper, GirlSpecies.ALPACA_CENTAUR, new BlockPos(5, 2, 10));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BRUSH));
        horse.interact(player, InteractionHand.MAIN_HAND);
        check(horse.getAffection(player) == 2, "grooming is worth 2: " + horse.getAffection(player));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        horse.interact(player, InteractionHand.MAIN_HAND);
        check(!horse.hasFlag(AbstractMonsterGirl.FLAG_SADDLED), "no saddle before she is devoted");
        horse.setAffection(player.getUUID(), 60);
        horse.interact(player, InteractionHand.MAIN_HAND);
        check(horse.hasFlag(AbstractMonsterGirl.FLAG_SADDLED), "saddled once devoted");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        horse.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == horse, "she lets her player ride");
        player.stopRiding();
        zebra.setAffection(player.getUUID(), 100);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        zebra.interact(player, InteractionHand.MAIN_HAND);
        check(zebra.hasFlag(AbstractMonsterGirl.FLAG_SADDLED), "every centaur takes a saddle now, the zebra too");
        alpaca.setAffection(player.getUUID(), 60);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
        alpaca.interact(player, InteractionHand.MAIN_HAND);
        check(alpaca.hasFlag(AbstractMonsterGirl.FLAG_CHEST) && alpaca.getPack().getContainerSize() == 27, "the alpaca carries a chest");
        alpaca.setFleeceTicks(24000);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        alpaca.interact(player, InteractionHand.MAIN_HAND);
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, alpaca.getBoundingBox().inflate(3), e -> e.getItem().is(Items.WHITE_WOOL)).isEmpty(), "shorn fleece");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "slimeDyesSplitsAndStores")
    public static void slimeDyesSplitsAndStores(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        SlimeGirl slime = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(7, 2, 7));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CYAN_DYE));
        slime.interact(player, InteractionHand.MAIN_HAND);
        check(slime.getVariantName().equals("teal"), "cyan dye turns her teal: " + slime.getVariantName());
        slime.setAffection(player.getUUID(), 40);
        slime.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(slime.getAffection(player) == 20, "a hit halves her trust: " + slime.getAffection(player));
        slime.hurt(helper.getLevel().damageSources().playerAttack(player), 999.0F);
        check(slime.isAlive(), "she splits instead of dying");
        long halves = helper.getLevel().getEntitiesOfClass(SlimeGirl.class, slime.getBoundingBox().inflate(4), SlimeGirl::isHalf).size();
        check(halves == 1, "one half splits off: " + halves);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "magmaSlimeCooksFrostSlimeFreezes")
    public static void magmaSlimeCooksFrostSlimeFreezes(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        SlimeGirl magma = spawn(helper, GirlSpecies.MAGMA_SLIME, new BlockPos(4, 2, 4));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF));
        magma.interact(player, InteractionHand.MAIN_HAND);
        check(player.getInventory().countItem(Items.COOKED_BEEF) == 1, "the magma slime cooks raw food");
        pool(helper, 10, 10);
        helper.setBlock(new BlockPos(10, 1, 10), Blocks.STONE);
        SlimeGirl frost = spawn(helper, GirlSpecies.FROST_SLIME, new BlockPos(10, 3, 10));
        HarpyTestUtil.remove(player);
        helper.succeedWhen(() -> {
            boolean ice = false;
            for (BlockPos p : BlockPos.betweenClosed(new BlockPos(9, 2, 9), new BlockPos(11, 2, 11))) ice |= helper.getBlockState(p).is(Blocks.FROSTED_ICE);
            check(ice, "water under the frost slime freezes");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "arachneSilkAndItchyHairs")
    public static void arachneSilkAndItchyHairs(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Arachne t = spawn(helper, GirlSpecies.TARANTULA_ARACHNE, new BlockPos(3, 2, 4));
        t.setSilkTicks(3000);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BRUSH));
        t.interact(player, InteractionHand.MAIN_HAND);
        check(t.getAffection(player) == 1 && !t.hasSilk(), "brushing gathers her silk");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, t.getBoundingBox().inflate(3), e -> e.getItem().is(Items.STRING)).isEmpty(), "string drops");
        t.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(player.hasEffect(MobEffects.CONFUSION), "the tarantula's hairs make her attacker queasy");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "merfolkDryOutOnLand")
    public static void merfolkDryOutOnLand(GameTestHelper helper) {
        Merfolk shark = spawn(helper, GirlSpecies.SHARK_MERMAID, new BlockPos(7, 2, 7));
        helper.runAfterDelay(40, () -> {
            check(shark.getDryTicks() >= 30, "on land she dries out: " + shark.getDryTicks());
            check(shark.getSpeed() < 0.15F, "and crawls slowly: " + shark.getSpeed());
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "trueNekomataHasASpareLife")
    public static void trueNekomataHasASpareLife(GameTestHelper helper) {
        Nekomata cat = spawn(helper, GirlSpecies.TIGER_NEKOMATA, new BlockPos(7, 2, 7));
        cat.setFlag(AbstractMonsterGirl.FLAG_TRUE_FORM, true);
        cat.hurt(helper.getLevel().damageSources().generic(), 999.0F);
        check(cat.isAlive() && cat.getHealth() >= cat.getMaxHealth() / 2 - 0.01F, "a true nekomata survives the first killing blow");
        cat.hurt(helper.getLevel().damageSources().generic(), 999.0F);
        check(!cat.isAlive(), "but not a second one the same day");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "girlsBringPresents")
    public static void girlsBringPresents(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Centaur reindeer = spawn(helper, GirlSpecies.REINDEER_CENTAUR, new BlockPos(8, 2, 8));
        reindeer.setAffection(player.getUUID(), Bonded.FULL_AFFECTION);
        reindeer.setNextGiftTime(0);
        helper.succeedWhen(() -> {
            check(!player.getInventory().isEmpty(), "she should have brought a present");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lamiaSwallowsEggsAndWarms")
    public static void lamiaSwallowsEggsAndWarms(GameTestHelper helper) {
        Lamia boa = spawn(helper, GirlSpecies.BOA_LAMIA, new BlockPos(7, 2, 7));
        boa.setWarmth(20);
        helper.spawnItem(Items.EGG, 7.5F, 2.5F, 7.5F);
        helper.runAfterDelay(60, () -> {
            check(boa.getBellySwell() > 0.5F, "she swallowed the egg whole: " + boa.getBellySwell());
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, boa.getBoundingBox().inflate(3)).isEmpty(), "the egg is gone");
            helper.succeed();
        });
    }
}
