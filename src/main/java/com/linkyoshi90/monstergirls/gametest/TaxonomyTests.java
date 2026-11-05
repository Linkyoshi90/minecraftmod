package com.linkyoshi90.monstergirls.gametest;

import com.google.gson.JsonPrimitive;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.DairyItem;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.linkyoshi90.monstergirls.registry.ModComponents;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The taxonomy (TAXONOMY_PLAN.md, entity.Clade): every mob has a real clade apart from her form, milk follows the clade,
 * and milk from before the regrouping (old bucket ids, old family names on dairy) still loads.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TaxonomyTests {

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyMobHasAClade")
    public static void everyMobHasAClade(GameTestHelper helper) {
        for (GirlSpecies s : GirlSpecies.values()) check(s.clade() != null, s.id() + " has a clade");
        for (ResourceLocation key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (key.getNamespace().equals(MonsterGirlsMod.MODID) && key.getPath().endsWith("_harpy")) {
                check(Clade.of(key.getPath()) == Clade.AVES, key + " is a bird");
            }
        }
        check(Clade.of("bullywug") == Clade.AMPHIBIA && Clade.of("vodyanoy") == Clade.AMPHIBIA, "anurans are amphibians");
        check(Clade.of("cow") == null, "vanilla ids are not ours");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "cladeIsNotForm")
    public static void cladeIsNotForm(GameTestHelper helper) {
        check(GirlSpecies.HYENA_CANID.family() == GirlSpecies.Family.CANID && GirlSpecies.HYENA_CANID.clade() == Clade.FELIFORMIA,
                "the hyena keeps the canid body but is a feliform");
        check(GirlSpecies.RED_KITSUNE.clade() == GirlSpecies.WOLF_CANID.clade(), "foxes and wolves are both canids");
        check(GirlSpecies.ORCA_MERMAID.clade() == GirlSpecies.HIPPO_MINOTAUR.clade(), "whales and hippos are whippomorphs");
        check(GirlSpecies.SALAMANDER_LIZARDFOLK.clade() == Clade.AMPHIBIA, "salamanders are amphibians");
        check(Clade.guest("starfish_crustacean") && !Clade.guest("crab_crustacean"), "the starfish is a guest, the crab is not");
        check(Clade.FAE.mythic() && !Clade.FELIFORMIA.mythic(), "the fae sit outside the tree");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "milkFollowsTheClade")
    public static void milkFollowsTheClade(GameTestHelper helper) {
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(1, 2, 1));
        check(FamilyMilk.of(hyena) == FamilyMilk.FELIFORMIA, "a hyena gives feliform milk");
        AbstractMonsterGirl rose = spawn(helper, GirlSpecies.ROSE_FLORA, new BlockPos(3, 2, 1));
        check(FamilyMilk.of(rose) == null, "plants give none");
        LivingEntity sparrow = ModEntities.SPARROW_HARPY.get().create(helper.getLevel());
        LivingEntity frog = ModEntities.BULLYWUG.get().create(helper.getLevel());
        check(sparrow != null && FamilyMilk.of(sparrow) == FamilyMilk.AVES, "harpies give bird milk");
        check(frog != null && FamilyMilk.of(frog) == FamilyMilk.AMPHIBIA, "anurans give amphibian milk");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyMilkHasABucket")
    public static void everyMilkHasABucket(GameTestHelper helper) {
        for (FamilyMilk m : FamilyMilk.values()) {
            var item = ModItems.MILK.get(m).get();
            check(BuiltInRegistries.ITEM.getKey(item).getPath().equals(m.id()), m + " bucket is " + m.id());
            check(item.getDefaultInstance().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "buckets/milk"))), m.id() + " counts as milk for recipes");
            check(m.clade().name().equals(m.name()) && !m.effects().isEmpty(), m + " has its clade and effects");
            check(DairyItem.familyName(m).getContents() instanceof TranslatableContents t && t.getKey().equals("family.monstergirls." + m.clade().key()),
                    m + " names its dairy after the clade");
        }
        check(FamilyMilk.of(Clade.FLORA) == null && FamilyMilk.of(Clade.CONSTRUCT) == null, "plants and constructs give none");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "oldMilkStillLoads")
    public static void oldMilkStillLoads(GameTestHelper helper) {
        var codec = ModComponents.MILK_FAMILY.get().codecOrThrow();
        check(codec.parse(JsonOps.INSTANCE, new JsonPrimitive("NEKOMATA")).getOrThrow() == FamilyMilk.FELIFORMIA, "an old nekomata cheese is feliform");
        check(codec.parse(JsonOps.INSTANCE, new JsonPrimitive("HARPY")).getOrThrow() == FamilyMilk.AVES, "an old harpy butter is bird");
        check(codec.parse(JsonOps.INSTANCE, new JsonPrimitive("UNDEAD")).getOrThrow() == FamilyMilk.UNDEAD, "unchanged names still read");
        for (var old : FamilyMilk.OLD.entrySet()) {
            ResourceLocation id = MonsterGirlsMod.id(old.getKey().toLowerCase(java.util.Locale.ROOT) + "_milk");
            check(BuiltInRegistries.ITEM.get(id) == ModItems.MILK.get(old.getValue()).get(), id + " turns into " + old.getValue().id());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "wordGetsAroundTheBranch")
    public static void wordGetsAroundTheBranch(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(6, 2, 3));
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(3, 2, 6));
        wolf.addAffection(p, com.linkyoshi90.monstergirls.entity.Orders.AFFECTION);
        check(com.linkyoshi90.monstergirls.entity.Journal.record(p, wolf, true), "a first wolf friend");
        check(fox.getAffection(p) == com.linkyoshi90.monstergirls.entity.Journal.WORD, "the fox is a canid too: she has heard of you");
        check(hyena.getAffection(p) == 0, "the hyena only looks like one: a feliform hears nothing");
        AbstractMonsterGirl wolf2 = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 6));
        wolf2.addAffection(p, com.linkyoshi90.monstergirls.entity.Orders.AFFECTION);
        com.linkyoshi90.monstergirls.entity.Journal.record(p, wolf2, true);
        check(fox.getAffection(p) == com.linkyoshi90.monstergirls.entity.Journal.WORD, "a second wolf friend is old news");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyMilkTasted")
    public static void everyMilkTasted(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        var bird = ModItems.MILK.get(FamilyMilk.AVES).get();
        var left = bird.finishUsingItem(new net.minecraft.world.item.ItemStack(bird), helper.getLevel(), p);
        check(left.is(net.minecraft.world.item.Items.BUCKET) && com.linkyoshi90.monstergirls.entity.Journal.tasted(p) == 1, "a first taste, and the bucket back");
        check(!com.linkyoshi90.monstergirls.entity.Journal.taste(p, FamilyMilk.AVES), "the same milk again is nothing new");
        var a = p.server.getAdvancements().get(MonsterGirlsMod.id(com.linkyoshi90.monstergirls.entity.Journal.CONNOISSEUR_ADVANCEMENT));
        check(a != null && !p.getAdvancements().getOrStartProgress(a).isDone(), "not a connoisseur yet");
        for (FamilyMilk m : FamilyMilk.values()) com.linkyoshi90.monstergirls.entity.Journal.taste(p, m);
        check(com.linkyoshi90.monstergirls.entity.Journal.tasted(p) == FamilyMilk.values().length && p.getAdvancements().getOrStartProgress(a).isDone(),
                "every milk: a connoisseur");
        check(com.linkyoshi90.monstergirls.entity.Journal.book(p).pages().get(0).raw().toString().contains("journal.monstergirls.tasted"), "the cover keeps count");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theTreeOfLifeFillsIn")
    public static void theTreeOfLifeFillsIn(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        java.util.List<String> branch = com.linkyoshi90.monstergirls.entity.Journal.members(Clade.XENARTHRA);
        check(branch.size() == 4 && com.linkyoshi90.monstergirls.entity.Journal.members(Clade.AVES).size() == 47, "every kind is on its branch, harpies too");
        for (String id : branch.subList(0, 3)) {
            var e = BuiltInRegistries.ENTITY_TYPE.get(MonsterGirlsMod.id(id)).create(helper.getLevel());
            check(e != null && com.linkyoshi90.monstergirls.entity.Journal.record(p, e, false), id + " is news");
        }
        check(com.linkyoshi90.monstergirls.entity.Journal.metOn(p, Clade.XENARTHRA) == 3 && com.linkyoshi90.monstergirls.entity.Journal.wholeBranches(p).isEmpty(), "three of four");
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(3, 2, 3));
        com.linkyoshi90.monstergirls.entity.Journal.record(p, hyena, false);
        var last = BuiltInRegistries.ENTITY_TYPE.get(MonsterGirlsMod.id(branch.get(3))).create(helper.getLevel());
        com.linkyoshi90.monstergirls.entity.Journal.record(p, last, false);
        check(com.linkyoshi90.monstergirls.entity.Journal.wholeBranches(p).contains("XENARTHRA"), "the fourth makes the branch whole");
        check(!com.linkyoshi90.monstergirls.entity.Journal.branch(p, Clade.XENARTHRA), "only once");
        var pages = com.linkyoshi90.monstergirls.entity.Journal.book(p).pages();
        int tree = -1, hyenaPage = -1, firstArmadillo = -1;
        for (int i = 0; i < pages.size(); i++) {
            String raw = pages.get(i).raw().toString();
            if (raw.contains("journal.monstergirls.tree.title")) tree = i;
            if (raw.contains("entity.monstergirls.hyena_canid")) hyenaPage = i;
            if (firstArmadillo < 0 && raw.contains("entity.monstergirls." + branch.get(0) + "'")) firstArmadillo = i;
        }
        check(tree >= 0 && pages.get(tree).raw().toString().contains("family.monstergirls.xenarthra"), "the tree of life counts the branch");
        check(hyenaPage >= 0 && firstArmadillo > hyenaPage, "pages go branch by branch: the feliform before the xenarthrans she was met after");
        check(pages.get(hyenaPage).raw().toString().contains("clade.monstergirls.feliformia"), "her page names her branch");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
