package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Portrait paintings (tools/gen_portraits.py): data-driven painting variants, placeable like vanilla ones. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class PortraitTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "portraitsHangLikePaintings")
    public static void portraitsHangLikePaintings(GameTestHelper helper) {
        var reg = helper.getLevel().registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        var crow = reg.getHolder(net.minecraft.resources.ResourceKey.create(Registries.PAINTING_VARIANT, MonsterGirlsMod.id("portrait_crow_harpy")));
        check(crow.isPresent(), "the crow's portrait is a painting");
        PaintingVariant v = crow.get().value();
        check(v.width() == 2 && v.height() == 2, "two blocks square");
        check(crow.get().is(PaintingVariantTags.PLACEABLE), "and a painting hung on a wall can come up as her");
        long ours = reg.keySet().stream().filter(k -> k.getNamespace().equals(MonsterGirlsMod.MODID)).count();
        check(ours >= 20, "a gallery of them: " + ours);
        var crowGirl = helper.spawn(com.linkyoshi90.monstergirls.registry.ModEntities.CROW_HARPY.get(), new net.minecraft.core.BlockPos(4, 2, 4));
        var made = com.linkyoshi90.monstergirls.entity.Hobbies.portrait(helper.getLevel(), crowGirl, helper.getLevel().random);
        var data = made.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
        check(made.is(net.minecraft.world.item.Items.PAINTING) && data != null && data.copyTag().getString("variant").equals("monstergirls:portrait_crow_harpy"),
                "a painter paints her own kind");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "studyingThroughASpyglass")
    public static void studyingThroughASpyglass(GameTestHelper helper) {
        var girl = JackalopeTests.spawn(helper, com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.SERVAL_NEKOMATA, new net.minecraft.core.BlockPos(13, 2, 7));
        girl.setNoAi(true);
        var p = HarpyTestUtil.survivalPlayer(helper, new net.minecraft.core.BlockPos(1, 2, 7));
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, girl.position().add(0, girl.getBbHeight() / 2, 0));
        int before = com.linkyoshi90.monstergirls.entity.Journal.sightings(p, "serval_nekomata");
        check(com.linkyoshi90.monstergirls.entity.Journal.spyglass(p) == girl, "she is in the crosshair");
        check(com.linkyoshi90.monstergirls.entity.Journal.sightings(p, "serval_nekomata") == before + 2, "studied twice over");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
