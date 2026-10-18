package com.linkyoshi90.harpies.gametest;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.PhoenixHarpy;
import com.linkyoshi90.harpies.entity.RiflebirdHarpy;
import com.linkyoshi90.harpies.registry.ModEntities;
import com.linkyoshi90.harpies.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(HarpiesMod.MODID)
@PrefixGameTestTemplate(false)
public class RiflebirdAndPhoenixTests {

    // --- riflebird --------------------------------------------------------------------------------------------

    @GameTest(template = PLATFORM, timeoutTicks = 3000, batch = "riflebirdDancesForAFan")
    public static void riflebirdDancesForAFan(GameTestHelper helper) {
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(4, 2, 4));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        bird.interact(player, InteractionHand.MAIN_HAND);
        helper.succeedWhen(() -> {
            check(bird.getAction() == AbstractHarpy.Action.DANCING, "hasn't danced for her fan yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "riflebirdLaysAnEggForAGem")
    public static void riflebirdLaysAnEggForAGem(GameTestHelper helper) {
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(7, 2, 7));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        bird.interact(player, InteractionHand.MAIN_HAND);
        check(bird.getAffection(player) >= RiflebirdHarpy.FOND, "a diamond should make her fond of you");
        helper.succeedWhen(() -> {
            AABB around = new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(10);
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream()
                    .anyMatch(i -> i.getItem().is(ModItems.RIFLEBIRD_EGG.get())), "no egg yet");
            HarpyTestUtil.remove(player);
        });
    }

    // --- phoenix ----------------------------------------------------------------------------------------------

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "phoenixWildOnesAttack")
    public static void phoenixWildOnesAttack(GameTestHelper helper) {
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        helper.succeedWhen(() -> {
            check(player.getHealth() < player.getMaxHealth() || player.isOnFire(), "a wild phoenix should attack");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "phoenixAshesHatchByAFireAndImprint")
    public static void phoenixAshesHatchByAFireAndImprint(GameTestHelper helper) {
        helper.setBlock(new BlockPos(7, 2, 7), Blocks.CAMPFIRE);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(11, 2, 7));
        // Dropped gently in the middle of the block beside the campfire (an item spawned on the campfire's corner gets
        // pushed out of its hitbox in a random direction, sometimes out of range).
        ItemEntity ashes = helper.spawnItem(ModItems.PHOENIX_ASHES.get(), 8.5F, 2.0F, 7.5F);
        ashes.setDeltaMovement(Vec3.ZERO);
        helper.succeedWhen(() -> {
            AABB area = new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(10);
            var chicks = helper.getLevel().getEntitiesOfClass(PhoenixHarpy.class, area);
            check(!chicks.isEmpty(), "the ashes haven't hatched");
            check(chicks.get(0).isHatchling(), "should hatch as a chick");
            check(chicks.get(0).isImprintedOn(player), "should imprint on the player she saw first");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "phoenixTearsHealHerFriend")
    public static void phoenixTearsHealHerFriend(GameTestHelper helper) {
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(7, 2, 7));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        phoenix.imprintOn(player);
        player.setHealth(6.0F);
        helper.succeedWhen(() -> {
            check(player.hasEffect(MobEffects.REGENERATION), "no healing tears");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "phoenixDeflectsGhastFireballs")
    public static void phoenixDeflectsGhastFireballs(GameTestHelper helper) {
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(9, 2, 9));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(11, 2, 11));
        phoenix.imprintOn(player);
        LargeFireball fireball = EntityType.FIREBALL.create(helper.getLevel());
        Vec3 start = helper.absoluteVec(new Vec3(2.5, 4.0, 2.5));
        fireball.moveTo(start.x, start.y, start.z, 0, 0);
        fireball.setDeltaMovement(player.position().add(0, 1, 0).subtract(start).normalize().scale(0.3));
        helper.getLevel().addFreshEntity(fireball);
        helper.succeedWhen(() -> {
            check(fireball.getOwner() == phoenix, "the fireball wasn't deflected");
            HarpyTestUtil.remove(player);
        });
    }
}
