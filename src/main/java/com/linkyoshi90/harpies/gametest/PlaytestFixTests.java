package com.linkyoshi90.harpies.gametest;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import com.linkyoshi90.harpies.entity.OstrichHarpy;
import com.linkyoshi90.harpies.entity.OwlHarpy;
import com.linkyoshi90.harpies.entity.SeagullHarpy;
import com.linkyoshi90.harpies.entity.SparrowHarpy;
import com.linkyoshi90.harpies.registry.ModEntities;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.check;

/** Regression tests for issues found while playtesting. */
@GameTestHolder(HarpiesMod.MODID)
@PrefixGameTestTemplate(false)
public class PlaytestFixTests {

    /** Exactly what /summon does: load the entity from NBT, then run spawn setup (finalizeSpawn) on it. */
    private static <T extends AbstractHarpy> T summon(GameTestHelper helper, Class<T> type, String id, BlockPos rel, String nbt) {
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(rel));
        var source = helper.getLevel().getServer().createCommandSourceStack().withLevel(helper.getLevel());
        try {
            var holder = BuiltInRegistries.ENTITY_TYPE.getHolderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, HarpiesMod.id(id)));
            Entity entity = SummonCommand.createEntity(source, holder, pos, nbt.isEmpty() ? new CompoundTag() : TagParser.parseTag(nbt), true);
            check(type.isInstance(entity), "summoned the wrong thing: " + entity);
            return type.cast(entity);
        } catch (CommandSyntaxException e) {
            throw new GameTestAssertException("summon of " + id + " failed: " + e.getMessage());
        }
    }

    @GameTest(template = PLATFORM, batch = "summonedVariantsStick")
    public static void summonedVariantsStick(GameTestHelper helper) {
        // Run several times: a re-roll from the biome would only sometimes land on the same variant by chance.
        for (int i = 0; i < 5; i++) {
            check(summon(helper, OwlHarpy.class, "owl_harpy", new BlockPos(2, 2, 2 + i * 2), "{Variant:\"snowy\"}").getVariant() == OwlHarpy.Variant.SNOWY,
                    "summoned owl lost its variant");
            check(summon(helper, SeagullHarpy.class, "seagull_harpy", new BlockPos(5, 2, 2 + i * 2), "{Variant:\"kittiwake\"}").getVariant()
                    == SeagullHarpy.Variant.KITTIWAKE, "summoned gull lost its variant");
            check(summon(helper, SparrowHarpy.class, "sparrow_harpy", new BlockPos(8, 2, 2 + i * 2), "{Variant:\"papi\"}").getVariant()
                    == SparrowHarpy.Variant.PAPI, "summoned sparrow lost its variant");
            check(summon(helper, OstrichHarpy.class, "ostrich_harpy", new BlockPos(11, 2, 2 + i * 2), "{Variant:\"cassowary\"}").getVariant()
                    == OstrichHarpy.Variant.CASSOWARY, "summoned ostrich lost its variant");
        }
        // Without NBT the biome still decides (test level is a plains-like flat world: never a cassowary there).
        OstrichHarpy plain = summon(helper, OstrichHarpy.class, "ostrich_harpy", new BlockPos(13, 2, 13), "");
        check(plain.getVariant() != OstrichHarpy.Variant.CASSOWARY, "a plain summon should still pick by biome");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "ostrichFoodDoesNotLureHerFromHerPatch")
    public static void ostrichFoodDoesNotLureHerFromHerPatch(GameTestHelper helper) {
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(7, 2, 7));
        ostrich.setHomePos(helper.absolutePos(new BlockPos(0, 2, 0)));
        // Within tempting range of her, but well outside her patrol area (18 blocks from the nest).
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(13, 2, 13));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        helper.onEachTick(() -> check(ostrich.goalSelector.getAvailableGoals().stream()
                .noneMatch(g -> g.isRunning() && g.getGoal() instanceof TemptGoal), "food lured her out of her patrol area"));
        helper.runAtTickTime(300, () -> {
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "airborneMeansNoGroundUnderfoot")
    public static void airborneMeansNoGroundUnderfoot(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        crow.setNoAi(true);
        helper.runAtTickTime(40, () -> {
            check(!crow.isAirborne(), "standing on the floor counted as airborne");
            crow.setPos(crow.getX(), crow.getY() + 3.0, crow.getZ());
            check(crow.isAirborne(), "three blocks up should count as airborne");
            helper.succeed();
        });
    }
}
