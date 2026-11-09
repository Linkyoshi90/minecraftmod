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
        helper.succeed();
    }
}
