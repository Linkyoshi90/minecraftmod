package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HeartfruitBushBlock;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * The Demon Realm, after Monster Girl Encyclopedia: a wild Lilith turns the ground around her into purple realm grass (RealmGrassBlock),
 * which then spreads by itself in the dark and sprouts heartfruit bushes. Light ({@link RealmGrassBlock#LIGHT}) drives it back, and so does holy water.
 * A befriended Lilith keeps it in check: only wild ones grow it.
 * <p>
 * The realm stirs the heart, and only that: grown, full-size friends standing in it blush and long for their close friends
 * ({@link #LONGING} percent every half minute, Missing#addLonging), and they flirt at every other pat instead of every fourth (Flirting#chance).
 * A heartfruit fed to a friend who would flirt leaves her lovestruck with that player for {@link #LOVESTRUCK} ticks:
 * she blushes, flirts at every pat, and is a quarter likelier to come to bed (Missing#bedBonus). Words, blushes and hearts only.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class DemonRealm {
    public static final int SEED_RADIUS = 8, SEED_EVERY = 199, BREATHE_EVERY = 599, LONGING = 3, LOVESTRUCK = 3600;
    public static final float LOVESTRUCK_BED = 0.25F;
    private static final String BY = "LovestruckBy", UNTIL = "LovestruckUntil";

    private DemonRealm() {
    }

    /** Who grows a realm: a Lilith, grown and wild. */
    public static boolean seeds(Entity e) {
        return e instanceof AbstractMonsterGirl girl && girl.getSpecies() == GirlSpecies.LILITH_DEVIL && !girl.isBaby() && !girl.hasAnyAffection();
    }

    /** Standing on realm grass (or in a plant on it). */
    public static boolean inRealm(Entity e) {
        BlockPos feet = e.blockPosition();
        Level level = e.level();
        return level.getBlockState(feet.below()).is(ModBlocks.REALM_GRASS.get()) || level.getBlockState(feet).is(ModBlocks.REALM_GRASS.get())
                || (level.getBlockState(feet).is(ModBlocks.HEARTFRUIT_BUSH.get()) && level.getBlockState(feet.below()).is(ModBlocks.REALM_GRASS.get()));
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (!(e.level() instanceof ServerLevel level) || !(e instanceof Mob mob) || !(e instanceof Bonded)) return;
        if (e.tickCount % SEED_EVERY == 0 && seeds(e) && MonsterGirlsConfig.demonRealm()) seed(level, mob);
        if (e.tickCount % BREATHE_EVERY == 0) breathe(level, mob);
        if (e.tickCount % 23 == 0 && mob.getPersistentData().contains(UNTIL) && level.getGameTime() < mob.getPersistentData().getLong(UNTIL)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.5, mob.getZ(), 1, 0.2, 0.1, 0.2, 0.0);  // lovestruck
        }
    }

    /** A Lilith's touch: one patch of ground near her becomes realm grass, now and then with a bush on it. Where, or null. */
    @javax.annotation.Nullable
    public static BlockPos seed(ServerLevel level, Mob lilith) {
        BlockPos at = lilith.blockPosition();
        for (int tries = 0; tries < 6; tries++) {
            int dx = lilith.getRandom().nextInt(SEED_RADIUS * 2 + 1) - SEED_RADIUS, dz = lilith.getRandom().nextInt(SEED_RADIUS * 2 + 1) - SEED_RADIUS;
            for (int dy = 3; dy >= -4; dy--) {
                BlockPos ground = at.offset(dx, dy, dz);
                if (!RealmGrassBlock.corrupt(level, ground)) continue;
                level.sendParticles(RealmGrassBlock.HAZE, ground.getX() + 0.5, ground.getY() + 1.2, ground.getZ() + 0.5, 6, 0.4, 0.2, 0.4, 0.0);
                if (lilith.getRandom().nextInt(6) == 0) HeartfruitBushBlock.sprout(level, ground);
                else if (lilith.getRandom().nextInt(8) == 0) com.linkyoshi90.monstergirls.block.WeepcapBlock.sprout(level, ground);
                com.linkyoshi90.monstergirls.block.RealmLeavesBlock.creep(level, ground, lilith.getRandom());
                return ground;
            }
        }
        return null;
    }

    /** Half a minute in the realm: a grown friend blushes and longs a little more for her close friends. True if she did. */
    public static boolean breathe(ServerLevel level, Mob mob) {
        if (!inRealm(mob) || !Missing.canLong(mob)) return false;
        boolean any = false;
        for (Player player : level.players()) any |= Missing.addLonging(mob, player, LONGING);
        if (!any) return false;
        ((Bonded) mob).blushFor(200);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getEyeY(), mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    // --- heartfruit ----------------------------------------------------------------------------------------------

    public static boolean lovestruck(Mob mob, Player player) {
        CompoundTag pd = mob.getPersistentData();
        return pd.contains(UNTIL) && player.getUUID().toString().equals(pd.getString(BY)) && mob.level().getGameTime() < pd.getLong(UNTIL);
    }

    public static float bedBonus(Mob mob, Player player) {
        return lovestruck(mob, player) ? LOVESTRUCK_BED : 0.0F;
    }

    /** She eats the heartfruit: lovestruck with this player for a while. False if she isn't one who would flirt with them. */
    public static boolean feed(ServerLevel level, Mob mob, Player player) {
        if (!Flirting.canFlirt(mob, player)) return false;
        CompoundTag pd = mob.getPersistentData();
        pd.putString(BY, player.getUUID().toString());
        pd.putLong(UNTIL, level.getGameTime() + LOVESTRUCK);
        ((Bonded) mob).blushFor(LOVESTRUCK);
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.1F);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 7, 0.4, 0.3, 0.4, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.lovestruck." + mob.getRandom().nextInt(4), mob.getDisplayName()), true);
        return true;
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItems.HEARTFRUIT.get()) || !(event.getTarget() instanceof Mob mob) || !(mob instanceof Bonded)
                || !Flirting.canFlirt(mob, event.getEntity())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level && feed(level, mob, event.getEntity())) event.getItemStack().consume(1, event.getEntity());
    }
}
