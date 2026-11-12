package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;

/**
 * Mating season, the restless days both Monmusu and the Encyclopedia like to tease about. Every branch of the tree of life (Clade) has
 * {@link #LENGTH} restless days in each {@link #CYCLE}-day year, each branch at its own time. A grown, full-size friend in season is
 * clingy: she comes to the side of a close friend ({@link Bonded#BED_AFFECTION}+) within {@link #CLING_RANGE}, blushes, flirts twice as often
 * (Flirting#chance) and is a quarter likelier to come to bed (Missing#bedBonus). Blushes, hearts and teasing only.
 * <p>
 * Cold calms her: a cold biome, or something icy fed to her (snowball, ice, packed or blue ice), which settles her for the rest of this season.
 * Behind the matingSeason option.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class MatingSeason {
    public static final int CYCLE = 40, LENGTH = 3, EVERY = 199;
    public static final float BED = 0.25F;
    public static final double CLING_RANGE = 16, BESIDE = 3;
    public static final float COLD = 0.15F;
    private static final String CALMED = "SeasonCalmed", NOTICED = "SeasonNoticed";

    private MatingSeason() {
    }

    public static long today(ServerLevel level) {
        return level.getDayTime() / 24000L;
    }

    /** The first day of a branch's season in each year. */
    public static int start(Clade clade) {
        return (clade.ordinal() * 7) % CYCLE;
    }

    public static boolean seasonOn(Clade clade, long day) {
        return Math.floorMod(day - start(clade), CYCLE) < LENGTH;
    }

    /** Which season (a count of years) a day belongs to, so a calming lasts just this one. */
    private static long year(Clade clade, long day) {
        return Math.floorDiv(day - start(clade), CYCLE);
    }

    /** In season today: grown, full size, her branch's days, not calmed, not in the cold. */
    public static boolean inSeason(Mob mob) {
        return mob.level() instanceof ServerLevel level && inSeason(mob, today(level));
    }

    public static boolean inSeason(Mob mob, long day) {
        if (!HarmontideConfig.matingSeason() || !Missing.canLong(mob)) return false;
        Clade clade = Clade.of(mob);
        if (clade == null || !seasonOn(clade, day)) return false;
        CompoundTag pd = mob.getPersistentData();
        if (pd.contains(CALMED) && pd.getLong(CALMED) == year(clade, day)) return false;
        return !cold(mob);
    }

    public static boolean cold(Mob mob) {
        return mob.level().getBiome(mob.blockPosition()).value().getBaseTemperature() < COLD;
    }

    /** What she adds to tonight's odds for a player. */
    public static float bedBonus(Mob mob, Player player) {
        return ((Bonded) mob).getAffection(player) >= Bonded.BED_AFFECTION && inSeason(mob) ? BED : 0.0F;
    }

    /** Something icy settles her for the rest of this season. True if she was in season. */
    public static boolean calm(ServerLevel level, Mob mob) {
        if (!inSeason(mob)) return false;
        Clade clade = Clade.of(mob);
        mob.getPersistentData().putLong(CALMED, year(clade, today(level)));
        level.sendParticles(ParticleTypes.SNOWFLAKE, mob.getX(), mob.getEyeY(), mob.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        mob.playSound(SoundEvents.GLASS_HIT, 0.6F, 1.6F);
        return true;
    }

    /** Out of season until her next one, whatever the day (tests that count exact odds). */
    public static void settle(Mob mob) {
        Clade clade = Clade.of(mob);
        if (clade != null && mob.level() instanceof ServerLevel level) mob.getPersistentData().putLong(CALMED, year(clade, today(level)));
    }

    public static boolean icy(ItemStack stack) {
        return stack.is(Items.SNOWBALL) || stack.is(Items.ICE) || stack.is(Items.PACKED_ICE) || stack.is(Items.BLUE_ICE);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!icy(event.getItemStack()) || !(event.getTarget() instanceof Mob mob) || !(mob instanceof Bonded b) || b.getAffection(event.getEntity()) < HomeLife.FRIEND
                || !(event.getLevel() instanceof ServerLevel level) || !inSeason(mob)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        calm(level, mob);
        event.getItemStack().consume(1, event.getEntity());
        event.getEntity().displayClientMessage(Component.translatable("message.harmontide.season.calmed", mob.getDisplayName()), true);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % EVERY != 0 || !(e instanceof Mob mob) || !(e instanceof Bonded) || !(e.level() instanceof ServerLevel level)) return;
        if (inSeason(mob)) restless(level, mob);
    }

    /** Once every EVERY ticks in season: she seeks out her closest friend nearby and blushes. Whom, or null. */
    @Nullable
    public static Player restless(ServerLevel level, Mob mob) {
        Bonded b = (Bonded) mob;
        Player best = null;
        for (Player p : level.players()) {
            if (p.isSpectator() || p.distanceToSqr(mob) > CLING_RANGE * CLING_RANGE || !Flirting.canFlirt(mob, p)) continue;
            if (best == null || b.getAffection(p) > b.getAffection(best)) best = p;
        }
        if (best == null) return null;
        if (!b.isOrderedToSit() && !mob.isSleeping() && mob.getTarget() == null && mob.distanceToSqr(best) > BESIDE * BESIDE) {
            mob.getNavigation().moveTo(best, 1.1);
        }
        b.blushFor(EVERY + 20);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 1, 0.25, 0.15, 0.25, 0.0);
        notice(level, mob, best);
        return best;
    }

    /** The first time a player is near her in a season, they hear of it. */
    private static void notice(ServerLevel level, Mob mob, Player p) {
        CompoundTag pd = mob.getPersistentData();
        Clade clade = Clade.of(mob);
        long y = year(clade, today(level));
        String key = NOTICED + p.getUUID();
        if (pd.contains(key) && pd.getLong(key) == y) return;
        pd.putLong(key, y);
        p.displayClientMessage(Component.translatable("message.harmontide.season.start." + mob.getRandom().nextInt(4), mob.getDisplayName()), false);
    }

    /** A line for /harmontide bonds, or null. */
    @Nullable
    public static Component describe(Mob mob) {
        return inSeason(mob) ? Component.translatable("bond.harmontide.in_season").withStyle(ChatFormatting.LIGHT_PURPLE) : null;
    }
}
