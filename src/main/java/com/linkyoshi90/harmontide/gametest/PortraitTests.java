package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Portrait paintings (tools/gen_portraits.py): data-driven painting variants, placeable like vanilla ones. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class PortraitTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "portraitsHangLikePaintings")
    public static void portraitsHangLikePaintings(GameTestHelper helper) {
        var reg = helper.getLevel().registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        var crow = reg.getHolder(net.minecraft.resources.ResourceKey.create(Registries.PAINTING_VARIANT, HarmontideMod.id("portrait_crow_harpy")));
        check(crow.isPresent(), "the crow's portrait is a painting");
        PaintingVariant v = crow.get().value();
        check(v.width() == 2 && v.height() == 2, "two blocks square");
        check(crow.get().is(PaintingVariantTags.PLACEABLE), "and a painting hung on a wall can come up as her");
        long ours = reg.keySet().stream().filter(k -> k.getNamespace().equals(HarmontideMod.MODID)).count();
        check(ours >= 20, "a gallery of them: " + ours);
        var crowGirl = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.CROW_HARPY.get(), new net.minecraft.core.BlockPos(4, 2, 4));
        var made = com.linkyoshi90.harmontide.entity.Hobbies.portrait(helper.getLevel(), crowGirl, helper.getLevel().random);
        var data = made.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
        check(made.is(net.minecraft.world.item.Items.PAINTING) && data != null && data.copyTag().getString("variant").equals("harmontide:portrait_crow_harpy"),
                "a painter paints her own kind");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "studyingThroughASpyglass")
    public static void studyingThroughASpyglass(GameTestHelper helper) {
        var girl = JackalopeTests.spawn(helper, com.linkyoshi90.harmontide.entity.girl.GirlSpecies.SERVAL_NEKOMATA, new net.minecraft.core.BlockPos(14, 2, 7));
        girl.setNoAi(true);
        var p = HarpyTestUtil.survivalPlayer(helper, new net.minecraft.core.BlockPos(1, 2, 7));
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, girl.position().add(0, girl.getBbHeight() / 2, 0));
        int before = com.linkyoshi90.harmontide.entity.Journal.sightings(p, "serval_nekomata");
        check(com.linkyoshi90.harmontide.entity.Journal.spyglass(p) == girl, "she is in the crosshair");
        check(com.linkyoshi90.harmontide.entity.Journal.sightings(p, "serval_nekomata") == before + 2, "studied twice over");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aMusicianPlaysTheNoteBlock")
    public static void aMusicianPlaysTheNoteBlock(GameTestHelper helper) {
        var girl = JackalopeTests.spawn(helper, com.linkyoshi90.harmontide.entity.girl.GirlSpecies.SERVAL_NEKOMATA, new net.minecraft.core.BlockPos(7, 2, 7));
        girl.setNoAi(true);
        check(com.linkyoshi90.harmontide.entity.Hobbies.playNoteBlock(helper.getLevel(), girl) == null, "nothing to play");
        helper.setBlock(new net.minecraft.core.BlockPos(9, 2, 7), net.minecraft.world.level.block.Blocks.NOTE_BLOCK);
        check(helper.absolutePos(new net.minecraft.core.BlockPos(9, 2, 7)).equals(com.linkyoshi90.harmontide.entity.Hobbies.playNoteBlock(helper.getLevel(), girl)), "she plays it");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aComplimentForANewLook")
    public static void aComplimentForANewLook(GameTestHelper helper) {
        var girl = JackalopeTests.spawn(helper, com.linkyoshi90.harmontide.entity.girl.GirlSpecies.SERVAL_NEKOMATA, new net.minecraft.core.BlockPos(7, 2, 7));
        girl.setNoAi(true);
        var p = HarpyTestUtil.survivalPlayer(helper, new net.minecraft.core.BlockPos(8, 2, 7));
        girl.setAffection(p.getUUID(), 30);
        check(com.linkyoshi90.harmontide.entity.Compliments.look(helper.getLevel(), p) == null, "nothing on, nothing to praise");
        p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET));
        check(com.linkyoshi90.harmontide.entity.Compliments.look(helper.getLevel(), p) == girl && girl.getAffection(p) == 31, "a new look, praised");
        check(com.linkyoshi90.harmontide.entity.Compliments.look(helper.getLevel(), p) == null, "once an outfit");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aFriendNotices")
    public static void aFriendNotices(GameTestHelper helper) {
        var girl = JackalopeTests.spawn(helper, com.linkyoshi90.harmontide.entity.girl.GirlSpecies.SERVAL_NEKOMATA, new net.minecraft.core.BlockPos(7, 2, 7));
        girl.setNoAi(true);
        var p = HarpyTestUtil.survivalPlayer(helper, new net.minecraft.core.BlockPos(8, 2, 7));
        girl.setAffection(p.getUUID(), 30);
        var pick = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pick);
        check(com.linkyoshi90.harmontide.entity.Notices.wornTool(helper.getLevel(), p) == null, "a new pick is fine");
        pick.setDamageValue(pick.getMaxDamage() - 3);
        check(com.linkyoshi90.harmontide.entity.Notices.wornTool(helper.getLevel(), p) == girl && com.linkyoshi90.harmontide.entity.Notices.wornTool(helper.getLevel(), p) == null,
                "nearly worn through: she says so, once");
        check(com.linkyoshi90.harmontide.entity.Notices.tired(helper.getLevel(), p, 100) == null, "rested");
        check(com.linkyoshi90.harmontide.entity.Notices.tired(helper.getLevel(), p, 80000) == girl
                && com.linkyoshi90.harmontide.entity.Notices.tired(helper.getLevel(), p, 80000) == null, "go to bed (once a night)");
        var creeper = helper.spawn(net.minecraft.world.entity.EntityType.CREEPER, new net.minecraft.core.BlockPos(11, 2, 7));
        creeper.setNoAi(true);
        check(com.linkyoshi90.harmontide.entity.Notices.creeper(helper.getLevel(), creeper, p) == girl
                && com.linkyoshi90.harmontide.entity.Notices.creeper(helper.getLevel(), creeper, p) == null, "creeper! (once)");
        creeper.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
