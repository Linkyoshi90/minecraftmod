package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Small instincts that make a kind herself.
 *
 * <ul>
 *   <li><b>The labyrinth sense.</b> A minotaur never loses her way: show a friend (10+) a compass and she tells you which way
 *   home lies (your bed, or the world's spawn) and how far.</li>
 *   <li><b>Zoomies.</b> At dusk a cat girl sometimes tears about for a few seconds for no reason at all, once a day at most.</li>
 *   <li><b>Night eyes.</b> An owl harpy at her friend's side in the dark lends them her eyes (Night Vision).</li>
 *   <li><b>Belly slides.</b> A penguin on ice drops to her belly and slides, three times as fast.</li>
 * </ul>
 */
public final class Instincts {
    public static final int ZOOMIES = 120;
    public static final double OWL_EYES = 12;
    /** The owls (they lend their night eyes). */
    public static final java.util.Set<String> OWLS = java.util.Set.of("owl_harpy", "stigowl_harpy", "argentowlis_harpy", "lechuza_harpy");
    public static final ResourceLocation SLIDE = ResourceLocation.fromNamespaceAndPath("harmontide", "belly_slide");
    private static final String[] POINTS = {"south", "south_west", "west", "north_west", "north", "north_east", "east", "south_east"};

    private Instincts() {}

    // --- the labyrinth sense -------------------------------------------------------------------------------------------

    /** Where home is for this player: their bed in this world, else the world's spawn. */
    public static BlockPos home(ServerPlayer p) {
        BlockPos bed = p.getRespawnPosition();
        if (bed != null && p.getRespawnDimension() == p.level().dimension()) return bed;
        return p.serverLevel().getSharedSpawnPos();
    }

    /** One of eight compass points (the lang key's last part) from one spot toward another. */
    public static String bearing(Vec3 from, Vec3 to) {
        double angle = Mth.atan2(-(to.x - from.x), to.z - from.z) * Mth.RAD_TO_DEG;   // Minecraft yaw: 0 south, 90 west
        int i = Math.floorMod(Math.round((float) angle / 45.0F), 8);
        return POINTS[i];
    }

    /** A compass shown to a minotaur friend: she points the way home (the compass stays in hand). */
    @Nullable
    public static InteractionResult labyrinthSense(AbstractMonsterGirl g, Player player, ItemStack stack) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.instincts() || g.getSpecies().family() != GirlSpecies.Family.MINOTAUR || g.isBaby() || !stack.is(net.minecraft.world.item.Items.COMPASS)
                || g.getAffection(player) < Orders.AFFECTION) return null;
        if (player instanceof ServerPlayer sp) {
            BlockPos home = home(sp);
            Vec3 at = sp.position(), to = Vec3.atBottomCenterOf(home);
            int far = (int) Math.round(Math.sqrt(Math.pow(to.x - at.x, 2) + Math.pow(to.z - at.z, 2)));
            int dy = home.getY() - sp.getBlockY();
            Component way = Component.translatable("direction.harmontide." + bearing(at, to));
            String key = far < 4 ? "message.harmontide.labyrinth_here" : dy < -8 ? "message.harmontide.labyrinth_below"
                    : dy > 8 ? "message.harmontide.labyrinth_above" : "message.harmontide.labyrinth";
            sp.displayClientMessage(Component.translatable(key, g.getDisplayName(), far, way, Math.abs(dy)), false);
            g.getLookControl().setLookAt(to.x, sp.getEyeY(), to.z);
            g.playSound(SoundEvents.COW_AMBIENT, 0.6F, 1.3F);
        }
        return InteractionResult.sidedSuccess(g.level().isClientSide);
    }

    // --- zoomies -------------------------------------------------------------------------------------------------------

    public static boolean dusk(ServerLevel level) {
        long t = level.getDayTime() % 24000L;
        return t >= 11800 && t <= 13200;
    }

    /** A cat girl's mad few seconds. Whether she is in one now (and running about). */
    public static boolean zoomies(ServerLevel level, AbstractMonsterGirl cat, boolean force) {
        long now = level.getGameTime();
        long until = cat.getPersistentData().getLong("ZoomiesUntil");
        if (now < until) {
            if (cat.getNavigation().isDone() || now % 15 == 0) {
                Vec3 to = cat.position().add(cat.getRandom().nextInt(17) - 8, 0, cat.getRandom().nextInt(17) - 8);
                cat.getNavigation().moveTo(to.x, to.y, to.z, 1.8);
            }
            return true;
        }
        if (cat.getSpecies().family() != GirlSpecies.Family.NEKOMATA || cat.isBaby() || cat.isOrderedToSit() || cat.getTarget() != null || cat.isPassenger()) return false;
        long day = level.getDayTime() / 24000L;
        if (!force && (now % 20 != 0 || !dusk(level) || cat.getPersistentData().getLong("ZoomiesDay") == day + 1 || cat.getRandom().nextInt(200) != 0)) return false;
        cat.getPersistentData().putLong("ZoomiesDay", day + 1);
        cat.getPersistentData().putLong("ZoomiesUntil", now + ZOOMIES);
        cat.playSound(SoundEvents.CAT_AMBIENT, 1.0F, 1.5F);
        return true;
    }

    /** Every few ticks for every girl (server): the instincts that run on their own. */
    public static void tick(ServerLevel level, AbstractMonsterGirl g) {
        if (g.getSpecies() == GirlSpecies.YUKI_ONNA_UNDEAD || Workshop.FIRE_WALKERS.contains(g.getSpecies())) Workshop.pathMaker(level, g);   // (the workshop's switch)
        else if (g.getSpecies().family() == GirlSpecies.Family.NEKOMATA && com.linkyoshi90.harmontide.HarmontideConfig.instincts()) zoomies(level, g, false);
    }

    // --- owls and penguins ---------------------------------------------------------------------------------------------

    /** An owl's eyes for her friend near her at night. Whether she lent them. */
    public static boolean owlEyes(ServerLevel level, AbstractHarpy owl) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.instincts()) return false;
        if (level.isDay() && level.getMaxLocalRawBrightness(owl.blockPosition()) > 7 || owl.isBaby()) return false;
        boolean lent = false;
        for (Player p : level.getEntitiesOfClass(Player.class, owl.getBoundingBox().inflate(OWL_EYES), p -> owl.getAffection(p) >= Orders.AFFECTION)) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
            lent = true;
        }
        return lent;
    }

    /** Standing on ice. */
    public static boolean onIce(Mob mob) {
        return mob.onGround() && mob.level().getBlockState(mob.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.ICE);
    }

    /** On ice a penguin slides on her belly (three times as fast); off it she waddles again. Whether she is sliding. */
    public static boolean bellySlide(Mob penguin) {
        AttributeInstance speed = penguin.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean ice = com.linkyoshi90.harmontide.HarmontideConfig.instincts() && onIce(penguin);
        if (speed != null) {
            if (ice && !speed.hasModifier(SLIDE)) speed.addTransientModifier(new AttributeModifier(SLIDE, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            else if (!ice && speed.hasModifier(SLIDE)) speed.removeModifier(SLIDE);
        }
        return ice;
    }
}
