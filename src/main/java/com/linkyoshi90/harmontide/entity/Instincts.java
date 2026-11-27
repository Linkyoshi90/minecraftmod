package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
 *   <li><b>Rooting.</b> A befriended boar girl (piglette, aegis boar, gribboar, daeodon) roots about in grass, dirt, moss
 *   and mud now and then and turns up something: mushrooms, potatoes, carrots, beetroot seeds, and now and then the seeds
 *   only a sniffer finds (torchflower seeds, pitcher pods).</li>
 *   <li><b>The anteater's nose.</b> A befriended anteater snaps up silverfish near her and shows where the stone around her is
 *   infested (a puff of dust over each block).</li>
 *   <li><b>Treetop browsing.</b> A befriended giraffe browses the leaves above her and now and then shakes down an apple
 *   (from oak and dark oak), a cocoa bean (jungle) or a stick.</li>
 *   <li><b>Belly slides.</b> A penguin on ice drops to her belly and slides, three times as fast.</li>
 *   <li><b>The way out.</b> Show a bat girl friend (10+) a torch underground and she tells you which way the open sky is.</li>
 *   <li><b>The lie of the land.</b> Show a kitsune friend (10+) a lantern and she tells you the way to the nearest village;
 *   show a dwarf one and she tells you the way to the nearest mineshaft.</li>
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

    // --- a bat knows the way out -----------------------------------------------------------------------------------------

    /** The nearest open air under the sky within reach of this spot (searched in rings), or null. */
    @Nullable
    public static BlockPos wayOut(ServerLevel level, BlockPos from) {
        if (level.canSeeSky(from)) return from;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int r = 4; r <= 48; r += 4) {
            for (int a = 0; a < 16; a++) {
                double ang = a * Math.PI / 8;
                int x = from.getX() + (int) Math.round(Math.cos(ang) * r), z = from.getZ() + (int) Math.round(Math.sin(ang) * r);
                int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
                BlockPos sky = new BlockPos(x, top, z);
                if (top > from.getY() + 48 || !level.getBlockState(sky).isAir()) continue;
                double d = sky.distSqr(from);
                if (d < bestDist) {
                    bestDist = d;
                    best = sky;
                }
            }
            if (best != null) return best;                                      // the nearest ring that has any
        }
        return null;
    }

    /** A torch shown to a bat friend: which way the sky is. */
    @Nullable
    public static InteractionResult batGuide(AbstractMonsterGirl g, Player player, ItemStack stack) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.instincts() || g.getSpecies().family() != GirlSpecies.Family.BAT || g.isBaby()
                || !stack.is(net.minecraft.world.item.Items.TORCH) || g.getAffection(player) < Orders.AFFECTION) return null;
        if (player instanceof ServerPlayer sp) {
            BlockPos out = wayOut(sp.serverLevel(), sp.blockPosition());
            if (out == null) {
                sp.displayClientMessage(Component.translatable("message.harmontide.bat_lost", g.getDisplayName()), true);
            } else if (out.equals(sp.blockPosition())) {
                sp.displayClientMessage(Component.translatable("message.harmontide.bat_sky", g.getDisplayName()), true);
            } else {
                Vec3 at = sp.position(), to = Vec3.atBottomCenterOf(out);
                int far = (int) Math.round(Math.sqrt(Math.pow(to.x - at.x, 2) + Math.pow(to.z - at.z, 2)));
                sp.displayClientMessage(Component.translatable("message.harmontide.bat_way_out", g.getDisplayName(), far,
                        Component.translatable("direction.harmontide." + bearing(at, to)), Math.max(0, out.getY() - sp.getBlockY())), false);
            }
            g.playSound(SoundEvents.BAT_AMBIENT, 0.8F, 1.6F);
        }
        return InteractionResult.sidedSuccess(g.level().isClientSide);
    }

    /** The guides, asked with what you show them (a bat a torch; a kitsune or dwarf a lantern). */
    @Nullable
    public static InteractionResult guide(AbstractMonsterGirl g, Player player, ItemStack stack) {
        InteractionResult r = batGuide(g, player, stack);
        return r != null ? r : landGuide(g, player, stack);
    }

    // --- the lie of the land -----------------------------------------------------------------------------------------------

    /** What she knows the way to: a kitsune, villages; a dwarf, mineshafts. Null for anyone else. */
    @Nullable
    public static net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure> knowsWayTo(GirlSpecies s) {
        if (s.family() == GirlSpecies.Family.KITSUNE) return net.minecraft.tags.StructureTags.VILLAGE;
        if (s == GirlSpecies.DWARF_ELF) return net.minecraft.tags.StructureTags.MINESHAFT;
        return null;
    }

    /** A lantern shown to a kitsune or dwarf friend: the way to the nearest village or mineshaft. */
    @Nullable
    public static InteractionResult landGuide(AbstractMonsterGirl g, Player player, ItemStack stack) {
        var where = knowsWayTo(g.getSpecies());
        if (where == null || !com.linkyoshi90.harmontide.HarmontideConfig.instincts() || g.isBaby() || g.getAffection(player) < Orders.AFFECTION
                || !(stack.is(net.minecraft.world.item.Items.LANTERN) || stack.is(net.minecraft.world.item.Items.SOUL_LANTERN))) return null;
        if (player instanceof ServerPlayer sp) {
            long now = sp.level().getGameTime();
            if (now < g.getPersistentData().getLong("GuidedAt")) {
                sp.displayClientMessage(Component.translatable("message.harmontide.guide_rest", g.getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }
            g.getPersistentData().putLong("GuidedAt", now + 200);                 // (a search the world has to work for)
            BlockPos found = sp.serverLevel().findNearestMapStructure(where, sp.blockPosition(), 64, false);
            String kind = where == net.minecraft.tags.StructureTags.VILLAGE ? "village" : "mineshaft";
            if (found == null) {
                sp.displayClientMessage(Component.translatable("message.harmontide.guide_none." + kind, g.getDisplayName()), true);
            } else {
                Vec3 at = sp.position(), to = Vec3.atBottomCenterOf(found);
                int far = (int) Math.round(Math.sqrt(Math.pow(to.x - at.x, 2) + Math.pow(to.z - at.z, 2)));
                sp.displayClientMessage(Component.translatable("message.harmontide.guide_way." + kind, g.getDisplayName(), far,
                        Component.translatable("direction.harmontide." + bearing(at, to))), false);
            }
        }
        return InteractionResult.sidedSuccess(g.level().isClientSide);
    }

    // --- boars root for truffles -------------------------------------------------------------------------------------------

    public static final java.util.Set<GirlSpecies> ROOTERS = java.util.Set.of(GirlSpecies.PIGLETTE_ARMOURED, GirlSpecies.AEGIS_BOAR_ARMOURED,
            GirlSpecies.GRIBBOAR_ARMOURED, GirlSpecies.DAEODON_ARMOURED);
    public static final int ROOT_EVERY = 3600;

    /** What a boar turns up. */
    public static ItemStack rootFind(net.minecraft.util.RandomSource r) {
        int n = r.nextInt(100);
        return n < 28 ? new ItemStack(net.minecraft.world.item.Items.BROWN_MUSHROOM) : n < 48 ? new ItemStack(net.minecraft.world.item.Items.POTATO)
                : n < 62 ? new ItemStack(net.minecraft.world.item.Items.CARROT) : n < 78 ? new ItemStack(net.minecraft.world.item.Items.BEETROOT_SEEDS, 2)
                : n < 86 ? new ItemStack(net.minecraft.world.item.Items.RED_MUSHROOM) : n < 94 ? new ItemStack(net.minecraft.world.item.Items.TORCHFLOWER_SEEDS)
                : new ItemStack(net.minecraft.world.item.Items.PITCHER_POD);
    }

    /** Soft ground to root in. */
    public static boolean rootable(net.minecraft.world.level.block.state.BlockState s) {
        return s.is(net.minecraft.tags.BlockTags.DIRT) || s.is(net.minecraft.world.level.block.Blocks.MUD) || s.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK);
    }

    /** A befriended boar, standing on soft ground, roots something up now and then. What she found, or empty. */
    public static ItemStack root(ServerLevel level, AbstractMonsterGirl boar, boolean force) {
        if (!ROOTERS.contains(boar.getSpecies()) || boar.isBaby() || !boar.hasAnyAffection() || !boar.onGround()) return ItemStack.EMPTY;
        if (!rootable(level.getBlockState(boar.getBlockPosBelowThatAffectsMyMovement()))) return ItemStack.EMPTY;
        long now = level.getGameTime();
        if (!force && now < boar.getPersistentData().getLong("RootedUntil")) return ItemStack.EMPTY;
        boar.getPersistentData().putLong("RootedUntil", now + ROOT_EVERY + boar.getRandom().nextInt(ROOT_EVERY));
        ItemStack find = rootFind(boar.getRandom());
        boar.spawnAtLocation(find.copy());
        level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,
                level.getBlockState(boar.getBlockPosBelowThatAffectsMyMovement())), boar.getX(), boar.getY() + 0.1, boar.getZ(), 12, 0.3, 0.05, 0.3, 0.1);
        boar.playSound(SoundEvents.SNIFFER_DIGGING, 1.0F, 1.2F);
        return find;
    }

    // --- the anteater's nose ------------------------------------------------------------------------------------------------

    /** Silverfish near a befriended anteater are eaten; infested stone near her puffs dust. How many silverfish she ate. */
    public static int anteater(ServerLevel level, AbstractMonsterGirl g) {
        if (g.getSpecies() != GirlSpecies.ANTEATER_ARMOURED || g.isBaby() || !g.hasAnyAffection()) return 0;
        int ate = 0;
        for (net.minecraft.world.entity.monster.Silverfish s : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Silverfish.class, g.getBoundingBox().inflate(10))) {
            s.hurt(g.damageSources().mobAttack(g), 40.0F);
            ate++;
        }
        if (ate > 0) g.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.4F);
        for (BlockPos p : BlockPos.betweenClosed(g.blockPosition().offset(-6, -4, -6), g.blockPosition().offset(6, 4, 6))) {
            if (level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.InfestedBlock) {
                level.sendParticles(ParticleTypes.WHITE_ASH, p.getX() + 0.5, p.getY() + 1.05, p.getZ() + 0.5, 6, 0.3, 0.05, 0.3, 0.0);
            }
        }
        return ate;
    }

    // --- the giraffe browses the treetops ---------------------------------------------------------------------------------

    public static final int BROWSE_EVERY = 3000;

    /** Leaves within her reach (over her head, up to two above), or null. */
    @Nullable
    public static BlockPos browseLeaves(ServerLevel level, AbstractMonsterGirl g) {
        BlockPos head = BlockPos.containing(g.getX(), g.getEyeY(), g.getZ());
        for (BlockPos p : BlockPos.betweenClosed(head.offset(-2, -1, -2), head.offset(2, 2, 2))) {
            if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.LEAVES)) return p.immutable();
        }
        return null;
    }

    /** A befriended giraffe browsing the leaves above her: now and then a find falls. What fell, or empty. */
    public static ItemStack browse(ServerLevel level, AbstractMonsterGirl g, boolean force) {
        if (g.getSpecies() != GirlSpecies.GIRAFFE_CENTAUR || g.isBaby() || !g.hasAnyAffection()) return ItemStack.EMPTY;
        long now = level.getGameTime();
        if (!force && now < g.getPersistentData().getLong("BrowsedUntil")) return ItemStack.EMPTY;
        BlockPos leaves = browseLeaves(level, g);
        if (leaves == null) return ItemStack.EMPTY;
        g.getPersistentData().putLong("BrowsedUntil", now + BROWSE_EVERY + g.getRandom().nextInt(BROWSE_EVERY));
        net.minecraft.world.level.block.state.BlockState s = level.getBlockState(leaves);
        ItemStack find = s.is(net.minecraft.world.level.block.Blocks.OAK_LEAVES) || s.is(net.minecraft.world.level.block.Blocks.DARK_OAK_LEAVES)
                ? new ItemStack(net.minecraft.world.item.Items.APPLE) : s.is(net.minecraft.world.level.block.Blocks.JUNGLE_LEAVES)
                ? new ItemStack(net.minecraft.world.item.Items.COCOA_BEANS) : new ItemStack(net.minecraft.world.item.Items.STICK);
        g.getLookControl().setLookAt(leaves.getX() + 0.5, leaves.getY() + 0.5, leaves.getZ() + 0.5);
        net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level, leaves.getX() + 0.5, leaves.getY(), leaves.getZ() + 0.5, find.copy());
        level.addFreshEntity(item);
        level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, s), leaves.getX() + 0.5, leaves.getY() + 0.5, leaves.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.05);
        g.playSound(SoundEvents.GRASS_BREAK, 0.8F, 1.2F);
        return find;
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
        Workshop.answerBell(g);                                                // on her way to a dinner bell
        if (g.getSpecies() == GirlSpecies.YUKI_ONNA_UNDEAD || Workshop.FIRE_WALKERS.contains(g.getSpecies())) Workshop.pathMaker(level, g);   // (the workshop's switch)
        else if (g.getSpecies().family() == GirlSpecies.Family.NEKOMATA && com.linkyoshi90.harmontide.HarmontideConfig.instincts()) zoomies(level, g, false);
        else if (ROOTERS.contains(g.getSpecies()) && g.tickCount % 100 == 41 && com.linkyoshi90.harmontide.HarmontideConfig.instincts()) root(level, g, false);
        else if (g.getSpecies() == GirlSpecies.ANTEATER_ARMOURED && g.tickCount % 40 == 17 && com.linkyoshi90.harmontide.HarmontideConfig.instincts()) anteater(level, g);
        else if (g.getSpecies() == GirlSpecies.GIRAFFE_CENTAUR && g.tickCount % 100 == 67 && com.linkyoshi90.harmontide.HarmontideConfig.instincts()) browse(level, g, false);
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
