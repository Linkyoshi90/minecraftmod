package com.linkyoshi90.harmontide.gametest;

import com.google.gson.JsonPrimitive;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Clade;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.item.DairyItem;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.registry.ModComponents;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModItems;
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

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The taxonomy (TAXONOMY_PLAN.md, entity.Clade): every mob has a real clade apart from her form, milk follows the clade,
 * and milk from before the regrouping (old bucket ids, old family names on dairy) still loads.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TaxonomyTests {

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyMobHasAClade")
    public static void everyMobHasAClade(GameTestHelper helper) {
        for (GirlSpecies s : GirlSpecies.values()) check(s.clade() != null, s.id() + " has a clade");
        for (ResourceLocation key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (key.getNamespace().equals(HarmontideMod.MODID) && key.getPath().endsWith("_harpy")) {
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
            check(DairyItem.familyName(m).getContents() instanceof TranslatableContents t && t.getKey().equals("family.harmontide." + m.clade().key()),
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
            ResourceLocation id = HarmontideMod.id(old.getKey().toLowerCase(java.util.Locale.ROOT) + "_milk");
            check(BuiltInRegistries.ITEM.get(id) == ModItems.MILK.get(old.getValue()).get(), id + " turns into " + old.getValue().id());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aWholeBoughOfTheTree")
    public static void aWholeBoughOfTheTree(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        var data = p.getData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL);
        var branches = new net.minecraft.nbt.ListTag();
        branches.add(net.minecraft.nbt.StringTag.valueOf(Clade.RODENTIA.name()));       // the rodents done already
        data.put(com.linkyoshi90.harmontide.entity.Journal.BRANCHES, branches);
        p.setData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL, data);
        check(com.linkyoshi90.harmontide.entity.Journal.boughs(p).isEmpty(), "one branch of the gnawers is not the bough");
        for (String id : com.linkyoshi90.harmontide.entity.Journal.members(Clade.LAGOMORPHA)) {
            var e = BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(id)).create(helper.getLevel());
            com.linkyoshi90.harmontide.entity.Journal.record(p, e, false);
        }
        check(com.linkyoshi90.harmontide.entity.Journal.boughs(p).equals(java.util.List.of("gnawers")), "every hare met: the gnawers are whole");
        check(com.linkyoshi90.harmontide.entity.Journal.book(p).pages().get(1).raw().toString().contains("journal.harmontide.branches"), "the milestones page, over the cover, counts whole branches");
        var a = p.server.getAdvancements().get(HarmontideMod.id("bough_gnawers"));
        check(a != null && p.getAdvancements().getOrStartProgress(a).isDone(), "and the advancement");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "stillToMeetHintsTheLastKinds")
    public static void stillToMeetHintsTheLastKinds(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        var hares = com.linkyoshi90.harmontide.entity.Journal.members(Clade.LAGOMORPHA);
        String last = hares.stream().filter(id -> GirlSpecies.byId(id) != null).reduce((x, y) -> y).orElseThrow();
        java.util.function.Supplier<String> book = () -> com.linkyoshi90.harmontide.entity.Journal.book(p).pages().toString();
        var first = BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(hares.get(0).equals(last) ? hares.get(1) : hares.get(0))).create(helper.getLevel());
        com.linkyoshi90.harmontide.entity.Journal.record(p, first, false);
        check(hares.size() - 1 <= com.linkyoshi90.harmontide.entity.Journal.LAST_FEW || !book.get().contains(last + ".home"), "one hare met of many: no hints yet");
        for (String id : hares) {
            if (id.equals(last)) continue;
            com.linkyoshi90.harmontide.entity.Journal.record(p, BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(id)).create(helper.getLevel()), false);
        }
        check(book.get().contains("journal.harmontide.still.title") && book.get().contains("journal.harmontide." + last + ".home"),
                "one hare left: the journal says where she lives");
        check(!book.get().contains("entity.harmontide." + last + "'"), "but not who she is");
        com.linkyoshi90.harmontide.entity.Journal.record(p, BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(last)).create(helper.getLevel()), false);
        check(!book.get().contains("journal.harmontide.still.title"), "the branch whole: nothing still to meet");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "wordGetsAroundTheBranch")
    public static void wordGetsAroundTheBranch(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(6, 2, 3));
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(3, 2, 6));
        wolf.addAffection(p, com.linkyoshi90.harmontide.entity.Orders.AFFECTION);
        check(com.linkyoshi90.harmontide.entity.Journal.record(p, wolf, true), "a first wolf friend");
        check(fox.getAffection(p) == com.linkyoshi90.harmontide.entity.Journal.WORD, "the fox is a canid too: she has heard of you");
        check(hyena.getAffection(p) == 0, "the hyena only looks like one: a feliform hears nothing");
        AbstractMonsterGirl wolf2 = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 6));
        wolf2.addAffection(p, com.linkyoshi90.harmontide.entity.Orders.AFFECTION);
        com.linkyoshi90.harmontide.entity.Journal.record(p, wolf2, true);
        check(fox.getAffection(p) == com.linkyoshi90.harmontide.entity.Journal.WORD, "a second wolf friend is old news");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyMilkTasted")
    public static void everyMilkTasted(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        var bird = ModItems.MILK.get(FamilyMilk.AVES).get();
        var left = bird.finishUsingItem(new net.minecraft.world.item.ItemStack(bird), helper.getLevel(), p);
        check(left.is(net.minecraft.world.item.Items.BUCKET) && com.linkyoshi90.harmontide.entity.Journal.tasted(p) == 1, "a first taste, and the bucket back");
        check(!com.linkyoshi90.harmontide.entity.Journal.taste(p, FamilyMilk.AVES), "the same milk again is nothing new");
        var a = p.server.getAdvancements().get(HarmontideMod.id(com.linkyoshi90.harmontide.entity.Journal.CONNOISSEUR_ADVANCEMENT));
        check(a != null && !p.getAdvancements().getOrStartProgress(a).isDone(), "not a connoisseur yet");
        for (FamilyMilk m : FamilyMilk.values()) com.linkyoshi90.harmontide.entity.Journal.taste(p, m);
        check(com.linkyoshi90.harmontide.entity.Journal.tasted(p) == FamilyMilk.values().length && p.getAdvancements().getOrStartProgress(a).isDone(),
                "every milk: a connoisseur");
        check(com.linkyoshi90.harmontide.entity.Journal.book(p).pages().get(1).raw().toString().contains("journal.harmontide.tasted"), "the milestones page keeps count");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theTreeOfLifeFillsIn")
    public static void theTreeOfLifeFillsIn(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        java.util.List<String> branch = com.linkyoshi90.harmontide.entity.Journal.members(Clade.XENARTHRA);
        check(branch.size() == 4 && com.linkyoshi90.harmontide.entity.Journal.members(Clade.AVES).size() == 47, "every kind is on its branch, harpies too");
        for (String id : branch.subList(0, 3)) {
            var e = BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(id)).create(helper.getLevel());
            check(e != null && com.linkyoshi90.harmontide.entity.Journal.record(p, e, false), id + " is news");
        }
        check(com.linkyoshi90.harmontide.entity.Journal.metOn(p, Clade.XENARTHRA) == 3 && com.linkyoshi90.harmontide.entity.Journal.wholeBranches(p).isEmpty(), "three of four");
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(3, 2, 3));
        com.linkyoshi90.harmontide.entity.Journal.record(p, hyena, false);
        var last = BuiltInRegistries.ENTITY_TYPE.get(HarmontideMod.id(branch.get(3))).create(helper.getLevel());
        com.linkyoshi90.harmontide.entity.Journal.record(p, last, false);
        check(com.linkyoshi90.harmontide.entity.Journal.wholeBranches(p).contains("XENARTHRA"), "the fourth makes the branch whole");
        check(!com.linkyoshi90.harmontide.entity.Journal.branch(p, Clade.XENARTHRA), "only once");
        var charm = p.getInventory().items.stream().filter(s -> s.is(ModItems.BRANCH_CHARM.get())).findFirst().orElse(net.minecraft.world.item.ItemStack.EMPTY);
        check(charm.get(ModComponents.CLADE.get()) == Clade.XENARTHRA && charm.getCount() == 1, "and its branch charm, once");
        check(com.linkyoshi90.harmontide.item.BranchCharmItem.effect(charm) == net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE
                && com.linkyoshi90.harmontide.item.BranchCharmItem.effect(com.linkyoshi90.harmontide.item.BranchCharmItem.of(Clade.FLORA)) == null,
                "the charm lends the branch's milk's first effect; the plants' is a keepsake");
        var pages = com.linkyoshi90.harmontide.entity.Journal.book(p).pages();
        int tree = -1, hyenaPage = -1, firstArmadillo = -1;
        for (int i = 0; i < pages.size(); i++) {
            String raw = pages.get(i).raw().toString();
            if (raw.contains("journal.harmontide.tree.title")) tree = i;
            if (raw.contains("entity.harmontide.hyena_canid")) hyenaPage = i;
            if (firstArmadillo < 0 && raw.contains("entity.harmontide." + branch.get(0) + "'")) firstArmadillo = i;
        }
        check(tree >= 0 && pages.get(tree).raw().toString().contains("family.harmontide.xenarthra"), "the tree of life counts the branch");
        check(hyenaPage >= 0 && firstArmadillo > hyenaPage, "pages go branch by branch: the feliform before the xenarthrans she was met after");
        check(pages.get(hyenaPage).raw().toString().contains("clade.harmontide.feliformia"), "her page names her branch");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "kinKnowTheCharm")
    public static void kinKnowTheCharm(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(3, 2, 3));
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.TIGER_NEKOMATA, new BlockPos(5, 2, 5));
        check(!com.linkyoshi90.harmontide.entity.Journal.charmed(p, fox), "no charm, no welcome");
        p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, com.linkyoshi90.harmontide.item.BranchCharmItem.of(Clade.CANIDAE));
        check(com.linkyoshi90.harmontide.entity.Journal.charmed(p, fox) && fox.getAffection(p) == com.linkyoshi90.harmontide.entity.Journal.CHARM_WARMTH, "the fox knows the canid charm");
        check(!com.linkyoshi90.harmontide.entity.Journal.charmed(p, cat) && cat.getAffection(p) == 0, "a cat doesn't");
        check(!com.linkyoshi90.harmontide.entity.Journal.charmed(p, fox), "once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
