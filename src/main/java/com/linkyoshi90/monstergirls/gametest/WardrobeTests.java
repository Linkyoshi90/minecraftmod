package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Wardrobe;
import com.linkyoshi90.monstergirls.entity.Wardrobe.Outfit;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The wardrobe (entity/Wardrobe): giving clothes, and what she chooses to wear when. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WardrobeTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "givingClothes")
    public static void givingClothes(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.COBRA_LAMIA, new BlockPos(3, 2, 3));
        girl.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 3));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.YUKATA.get(), 1));
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(!Wardrobe.owns(girl, Outfit.YUKATA) && !p.getMainHandItem().isEmpty(), "not from a stranger");
        girl.addAffection(p, 20);
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(Wardrobe.owns(girl, Outfit.YUKATA) && p.getMainHandItem().isEmpty() && girl.getAffection(p) == 22, "into her wardrobe, and she is pleased");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.YUKATA.get(), 1));
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(!p.getMainHandItem().isEmpty(), "one of each is enough");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "dressingForTheMoment")
    public static void dressingForTheMoment(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(3, 2, 3));
        girl.setNoAi(true);
        check(Wardrobe.choose(girl, true, true, true, true) == null, "nothing to wear yet");
        Wardrobe.give(girl, Outfit.MAID_APRON);
        Wardrobe.give(girl, Outfit.WINTER_COAT);
        check(Wardrobe.choose(girl, false, false, false, false) == null, "no reason to dress up");
        check(Wardrobe.choose(girl, false, false, false, true) == Outfit.MAID_APRON, "the apron at home");
        check(Wardrobe.choose(girl, true, true, false, true) == Outfit.WINTER_COAT, "the coat in the cold (no raincoat for the rain)");
        Wardrobe.give(girl, Outfit.RAINCOAT);
        check(Wardrobe.choose(girl, true, true, false, true) == Outfit.RAINCOAT, "the rain comes first");
        helper.setBlock(new BlockPos(8, 2, 8), ModBlocks.HEARTH.get());
        helper.runAfterDelay(3, () -> {
            Outfit worn = Wardrobe.dress(helper.getLevel(), girl);
            check(worn != null && girl.getOutfit() == worn.ordinal() + 1, "she dresses, and it shows: " + worn);
            girl.setBaby(true);
            check(Wardrobe.dress(helper.getLevel(), girl) == null && girl.getOutfit() == 0, "not on the young");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyGirlHasHerClothes")
    public static void everyGirlHasHerClothes(GameTestHelper helper) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (com.linkyoshi90.monstergirls.entity.girl.GirlSpecies s : com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.values()) {
            for (Wardrobe.Outfit o : Wardrobe.Outfit.values()) {
                String path = "/assets/monstergirls/textures/entity/outfit/" + s.id() + "_" + o.name().toLowerCase(java.util.Locale.ROOT) + ".png";
                if (WardrobeTests.class.getResource(path) == null) missing.add(path);
            }
        }
        check(missing.isEmpty(), "outfits painted for every girl (tools/gen_outfit_overlays.py): missing " + missing.size() + " " + missing.stream().limit(5).toList());
        helper.succeed();
    }
}
