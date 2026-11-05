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
}
