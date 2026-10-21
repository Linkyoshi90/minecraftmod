package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The first Harmontide species in existing families (HARMONTIDE_CONCEPTS.md): ocelot, tapir, alligator snapping turtle, banshee. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HarmontideTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tapirKeepsPhantomsAway")
    public static void tapirKeepsPhantomsAway(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(!Centaur.dreamWard(player), "no tapir, no ward");
        spawn(helper, GirlSpecies.TAPIR_CENTAUR, new BlockPos(10, 2, 10));
        check(Centaur.dreamWard(player), "a tapir nearby eats the bad dreams");
        PlayerSpawnPhantomsEvent event = NeoForge.EVENT_BUS.post(new PlayerSpawnPhantomsEvent(player, 2));
        check(event.getResult() == PlayerSpawnPhantomsEvent.Result.DENY, "so vanilla phantoms do not spawn: " + event.getResult());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "snappingTurtleLuresFish")
    public static void snappingTurtleLuresFish(GameTestHelper helper) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(7 + dx, 2, 7 + dz), Blocks.WATER);
        }
        Lizardfolk turtle = spawn(helper, GirlSpecies.SNAPPING_TURTLE_LIZARDFOLK, new BlockPos(7, 2, 7));
        turtle.setNoAi(true);                                  // sitting still on the riverbed
        helper.runAfterDelay(2, () -> {
            check(turtle.isInWater(), "she is in the water");
            for (int i = 0; i < 300; i++) turtle.lure(helper.getLevel());
            int fish = helper.getLevel().getEntitiesOfClass(ItemEntity.class, turtle.getBoundingBox().inflate(4), e -> e.getItem().is(ItemTags.FISHES)).size();
            check(fish == 3, "her lure catches fish, never more than three at a time: " + fish);
            Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
            turtle.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
            check(turtle.isRetracted(), "hit, she hides in her shell");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bansheeKeensForTheWounded")
    public static void bansheeKeensForTheWounded(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Undead banshee = spawn(helper, GirlSpecies.BANSHEE_UNDEAD, new BlockPos(11, 2, 11));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 3));
        banshee.keen(helper.getLevel());
        check(!z.hasEffect(MobEffects.GLOWING), "a healthy player gets no warning");
        player.setHealth(5.0F);
        banshee.keen(helper.getLevel());
        check(z.hasEffect(MobEffects.GLOWING), "three hearts or less: she keens and the danger glows");
        z.removeEffect(MobEffects.GLOWING);
        banshee.keen(helper.getLevel());
        check(!z.hasEffect(MobEffects.GLOWING), "once a minute per player, not every second");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ocelotBringsJunglePresents")
    public static void ocelotBringsJunglePresents(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Nekomata ocelot = spawn(helper, GirlSpecies.OCELOT_NEKOMATA, new BlockPos(7, 2, 7));
        for (int i = 0; i < 50; i++) {
            var gift = ocelot.rollGift(player);
            check(gift.is(Items.COCOA_BEANS) || gift.is(Items.FEATHER) || gift.is(Items.MELON_SLICE), "a jungle present: " + gift);
        }
        check(!ocelot.onClimbable(), "she only climbs when she runs into a trunk or a wall");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
