package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.ArgentowlisHarpy;
import com.linkyoshi90.monstergirls.entity.IbiskingHarpy;
import com.linkyoshi90.monstergirls.entity.LandharpyHarpy;
import com.linkyoshi90.monstergirls.entity.MotmoraHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.PoultryHarpy;
import com.linkyoshi90.monstergirls.entity.ReliquaryDoveHarpy;
import com.linkyoshi90.monstergirls.entity.SkycrowHarpy;
import com.linkyoshi90.monstergirls.entity.SpawnRarity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.GirlArms;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twenty-eighth Harmontide wave (HARMONTIDE_CONCEPTS.md), the last of the reference art: the Gandharva, the Apsara, the pixi, the
 * rlff, the Ark Seraphim and seven birds; rarer spawns, the bed kept from childlike characters, Manako's sniper rifle.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyEighthWaveTests {
    private static ServerPlayer friendOf(GameTestHelper helper, com.linkyoshi90.monstergirls.entity.Bonded girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    private static <T extends net.minecraft.world.entity.Mob> T bird(GameTestHelper helper, EntityType<T> type) {
        T b = helper.spawn(type, new BlockPos(1, 2, 1));
        b.setNoAi(true);
        return b;
    }

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wildGirlsKeepTheirDistance")
    public static void wildGirlsKeepTheirDistance(GameTestHelper helper) {
        AbstractMonsterGirl a = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        AbstractMonsterGirl herdmate = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 1));
        AbstractMonsterGirl other = still(helper, GirlSpecies.CARACAL_NEKOMATA, new BlockPos(5, 2, 1));
        check(!SpawnRarity.spacedOut(helper.getLevel(), other), "no second wild girl beside the first");
        other.discard();
        check(SpawnRarity.spacedOut(helper.getLevel(), herdmate), "her own group is fine");
        AbstractMonsterGirl next = still(helper, GirlSpecies.CARACAL_NEKOMATA, new BlockPos(5, 2, 3));
        ServerPlayer f = friendOf(helper, a, new BlockPos(6, 2, 6));
        herdmate.addAffection(f, Orders.AFFECTION);
        check(SpawnRarity.spacedOut(helper.getLevel(), next), "friends don't count as wild");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theHyraxCanSpawn")
    public static void theHyraxCanSpawn(GameTestHelper helper) {
        TagKey<Biome> tag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MonsterGirlsMod.MODID, "spawns_rockhyrax_rodent"));
        check(helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getTag(tag).isPresent(), "her spawn list loads");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theLittleOnesStayOutOfBed")
    public static void theLittleOnesStayOutOfBed(GameTestHelper helper) {
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.FAIRY_BUG, GirlSpecies.PIXI_BUG, GirlSpecies.BROWNIE_ELF, GirlSpecies.FIREFLY_BUG, GirlSpecies.GREMLIN_DEVIL}) {
            AbstractMonsterGirl g = still(helper, s, new BlockPos(1, 2, 1));
            check(!g.canShareBed(), s + " looks like a child");
            g.discard();
        }
        check(((AbstractMonsterGirl) still(helper, GirlSpecies.FAIRY_QUEEN_BUG, new BlockPos(1, 2, 1))).canShareBed(), "the Fairy Queen is grown");
        check(!((AbstractMonsterGirl) still(helper, GirlSpecies.GANDHARVA_ELF, new BlockPos(3, 2, 1))).canShareBed(), "he is a man");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "manakoCarriesARifle")
    public static void manakoCarriesARifle(GameTestHelper helper) {
        AbstractMonsterGirl plain = still(helper, GirlSpecies.CYCLOPS_DEVIL, new BlockPos(1, 2, 1));
        AbstractMonsterGirl manako = still(helper, GirlSpecies.CYCLOPS_DEVIL, new BlockPos(3, 2, 1));
        plain.setVariant(GirlSpecies.CYCLOPS_DEVIL.variants().get(0));
        manako.setVariant("tactical_eye");
        check(!GirlArms.isFighter(plain) && GirlArms.isFighter(manako) && GirlArms.isMarksman(manako), "only her");
        ServerPlayer f = friendOf(helper, manako, new BlockPos(5, 2, 5));
        plain.addAffection(f, Orders.AFFECTION);
        ItemStack bow = new ItemStack(Items.BOW);
        check(GirlArms.interact(plain, f, bow) == null, "a plain cyclops doesn't take it");
        check(GirlArms.interact(manako, f, bow) != null && manako.getMainHandItem().is(Items.BOW), "she does");
        check(!GirlArms.isSniping(manako), "a bow is not a sniper rifle");
        check(GirlArms.fireRange(true) > GirlArms.fireRange(false) && GirlArms.holdRange(true) > GirlArms.holdRange(false)
                && GirlArms.aimTicks(true) > GirlArms.aimTicks(false), "a sniper shoots from farther, after settling");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gandharvaPlaysAlong")
    public static void gandharvaPlaysAlong(GameTestHelper helper) {
        Elf g = still(helper, GirlSpecies.GANDHARVA_ELF, new BlockPos(1, 2, 1));
        check(g.canFly(), "he flies");
        ServerPlayer f = friendOf(helper, g, new BlockPos(3, 2, 3));
        g.hearNote(helper.getLevel(), 12);
        check(g.isPlaying() && g.getAction() == AbstractMonsterGirl.Action.PLAY && g.hearsMusic(helper.getLevel()), "he joins in");
        helper.runAfterDelay(30, () -> {
            check(f.hasEffect(MobEffects.REGENERATION), "his friend heals to the music");
            HarpyTestUtil.remove(f);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "apsaraDancesToAnyMusic")
    public static void apsaraDancesToAnyMusic(GameTestHelper helper) {
        Elf a = still(helper, GirlSpecies.APSARA_ELF, new BlockPos(1, 2, 1));
        ServerPlayer f = friendOf(helper, a, new BlockPos(3, 2, 3));
        check(a.dance(helper.getLevel()) == 0, "no music, no dance");
        a.hearNote(helper.getLevel(), 12);
        check(a.dance(helper.getLevel()) == 1 && f.hasEffect(MobEffects.MOVEMENT_SPEED) && !f.hasEffect(MobEffects.REGENERATION), "a note block will do");
        Elf g = still(helper, GirlSpecies.GANDHARVA_ELF, new BlockPos(5, 2, 1));
        g.playLute();
        check(a.dance(helper.getLevel()) == 2 && f.hasEffect(MobEffects.REGENERATION), "with him, more");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pixiShrinksHerFriend")
    public static void pixiShrinksHerFriend(GameTestHelper helper) {
        Bug p = still(helper, GirlSpecies.PIXI_BUG, new BlockPos(1, 2, 1));
        ServerPlayer f = friendOf(helper, p, new BlockPos(3, 2, 3));
        double size = f.getAttributeValue(Attributes.SCALE);
        check(p.shrink(f) && f.getAttributeValue(Attributes.SCALE) == size * 0.5, "half size");
        check(!p.shrink(f), "not again so soon");
        Bug.growBack(f);
        check(f.getAttributeValue(Attributes.SCALE) == size * 0.5, "not yet");
        f.getPersistentData().putLong("PixiShrunkUntil", 0);
        Bug.growBack(f);
        check(f.getAttributeValue(Attributes.SCALE) == size, "back to size");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "rlffsWaterHeals")
    public static void rlffsWaterHeals(GameTestHelper helper) {
        Bug r = still(helper, GirlSpecies.RLFF_BUG, new BlockPos(1, 2, 1));
        ServerPlayer f = friendOf(helper, r, new BlockPos(3, 2, 3));
        check(!r.healingWater(), "only in water");
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.WATER);
        helper.runAfterDelay(5, () -> {
            check(r.healingWater() && f.hasEffect(MobEffects.REGENERATION), "the water heals");
            HarpyTestUtil.remove(f);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "arkSeraphimAnswersOnceADay")
    public static void arkSeraphimAnswersOnceADay(GameTestHelper helper) {
        Centaur a = still(helper, GirlSpecies.ARK_SERAPHIM_CENTAUR, new BlockPos(1, 2, 1));
        check(a.blindingLight(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, a, new BlockPos(5, 2, 5));
        check(a.blindingLight(helper.getLevel()) == -1, "not while they are well");
        f.setHealth(4.0F);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(a.blindingLight(helper.getLevel()) == 1 && z.getHealth() <= z.getMaxHealth() - 14.0F && f.hasEffect(MobEffects.ABSORPTION), "the light burns and shields");
        check(a.blindingLight(helper.getLevel()) == -1, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "skycrowCallsTheLightning")
    public static void skycrowCallsTheLightning(GameTestHelper helper) {
        SkycrowHarpy s = bird(helper, ModEntities.SKYCROW_HARPY.get());
        check(s.strike(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, s, new BlockPos(5, 2, 5));
        check(s.strike(helper.getLevel()) == 0, "nothing to strike");
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(s.strike(helper.getLevel()) >= 6 && z.getHealth() < z.getMaxHealth(), "struck");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "reliquaryDoveKeepsItSafe")
    public static void reliquaryDoveKeepsItSafe(GameTestHelper helper) {
        ReliquaryDoveHarpy d = bird(helper, ModEntities.RELIQUARY_DOVE_HARPY.get());
        ServerPlayer f = friendOf(helper, d, new BlockPos(3, 2, 3));
        f.setShiftKeyDown(true);
        f.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 5));
        d.interact(f, InteractionHand.MAIN_HAND);
        check(d.keepsake().is(Items.DIAMOND) && d.keepsake().getCount() == 5 && f.getMainHandItem().isEmpty(), "she has it");
        d.interact(f, InteractionHand.MAIN_HAND);
        check(d.keepsake().isEmpty() && f.getMainHandItem().is(Items.DIAMOND), "and gives it back");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "poultryKeepsTheFarm")
    public static void poultryKeepsTheFarm(GameTestHelper helper) {
        PoultryHarpy p = bird(helper, ModEntities.POULTRY_HARPY.get());
        check(p.tend(helper.getLevel()) == -1 && p.layEgg(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, p, new BlockPos(5, 2, 5));
        check(p.tend(helper.getLevel()) == 0, "nothing planted");
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.WHEAT);
        check(p.tend(helper.getLevel()) == 1, "she tends it");
        check(((CropBlock) Blocks.WHEAT).getAge(helper.getBlockState(new BlockPos(2, 2, 1))) > 0, "and it grows");
        check(p.layEgg(helper.getLevel()) == 1, "an egg");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "landharpyKicks")
    public static void landharpyKicks(GameTestHelper helper) {
        LandharpyHarpy l = bird(helper, ModEntities.LANDHARPY_HARPY.get());
        check(l.stomp(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, l, new BlockPos(5, 2, 5));
        check(l.stomp(helper.getLevel()) == 0, "nothing near");
        Zombie z = zombie(helper, new BlockPos(2, 2, 1));
        check(l.stomp(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth() && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "kicked and staggered");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "argentowlisWatchesTheDark")
    public static void argentowlisWatchesTheDark(GameTestHelper helper) {
        ArgentowlisHarpy a = bird(helper, ModEntities.ARGENTOWLIS_HARPY.get());
        check(a.watch(helper.getLevel(), true) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, a, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(2, 2, 1));
        z.setInvisible(true);
        check(a.watch(helper.getLevel(), false) == 0, "not by day");
        check(a.watch(helper.getLevel(), true) == 1 && z.hasEffect(MobEffects.GLOWING), "she marks it");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "ibiskingReadsForYou")
    public static void ibiskingReadsForYou(GameTestHelper helper) {
        IbiskingHarpy i = bird(helper, ModEntities.IBISKING_HARPY.get());
        ServerPlayer f = friendOf(helper, i, new BlockPos(3, 2, 3));
        f.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PAPER, 2));
        i.interact(f, InteractionHand.MAIN_HAND);
        check(f.getMainHandItem().getCount() == 1, "she takes one sheet");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "motmoraDazesThem")
    public static void motmoraDazesThem(GameTestHelper helper) {
        MotmoraHarpy m = bird(helper, ModEntities.MOTMORA_HARPY.get());
        check(m.display(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, m, new BlockPos(5, 2, 5));
        Zombie a = zombie(helper, new BlockPos(6, 2, 5));
        check(m.display(helper.getLevel()) == 0, "one is not worth it");
        Zombie b = zombie(helper, new BlockPos(4, 2, 5));
        check(m.display(helper.getLevel()) == 2 && a.hasEffect(MobEffects.WEAKNESS) && b.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "dazed");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }
}
