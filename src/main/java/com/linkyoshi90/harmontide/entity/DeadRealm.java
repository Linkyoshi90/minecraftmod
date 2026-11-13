package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GraveGrassBlock;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The realm of the dead (2026-11-13), Monster Girl Encyclopedia's undead realm and the Demon Realm's gloomy cousin. A wild, grown
 * lich or koschei ({@link #seeds}) turns the ground round her into grave grass (block/GraveGrassBlock), which spreads in the dark and
 * withers in the sun, under lamplight and under holy water; the Order's sisters cleanse it where they walk, as they do the Demon Realm.
 * <ul>
 * <li>Grave mist hangs over it (client/RealmAtmosphere: a grey fog and drifting mist).</li>
 * <li>Zombies, husks, drowned, skeletons and strays that linger on it in the dark turn into undead girls (Monsterization#step).</li>
 * <li>Undead friends standing on it are at their strongest: Regeneration and Resistance ({@link #STRENGTHEN}).</li>
 * <li>Deep in it, at night, now and then one of the dead rises: a wild ghoul, banshee, zombie or will-o'-wisp girl ({@link #rise}).</li>
 * </ul>
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class DeadRealm {
    public static final int SEED_RADIUS = 8, SEED_EVERY = 199, STRENGTHEN = 197, RISE = 500, RISE_CORE = 2, RISE_ALONE = 48, RISE_MAX = 3;
    public static final List<GirlSpecies> RISEN = List.of(GirlSpecies.GHOUL_UNDEAD, GirlSpecies.BANSHEE_UNDEAD, GirlSpecies.ZOMBIE_UNDEAD, GirlSpecies.WILL_O_WISP_UNDEAD);

    private DeadRealm() {}

    /** Who grows the realm of the dead: a lich or a koschei, grown and wild. */
    public static boolean seeds(Entity e) {
        return e instanceof AbstractMonsterGirl girl && (girl.getSpecies() == GirlSpecies.LICH_UNDEAD || girl.getSpecies() == GirlSpecies.KOSCHEI_UNDEAD)
                && !girl.isBaby() && !girl.hasAnyAffection();
    }

    /** Standing on grave grass (or in a dead bush on it). */
    public static boolean inDeadRealm(Entity e) {
        BlockPos feet = e.blockPosition();
        Level level = e.level();
        return GraveGrassBlock.grave(level.getBlockState(feet.below())) || GraveGrassBlock.grave(level.getBlockState(feet))
                || GraveGrassBlock.grave(level.getBlockState(feet.below(2))) && !level.getBlockState(feet.below()).isSolid();
    }

    /** Undead by nature: the vanilla undead the realm turns. */
    public static boolean turnsHere(Mob mob) {
        return mob.getType().is(EntityTypeTags.UNDEAD);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (!(e.level() instanceof ServerLevel level) || !(e instanceof Mob mob)) return;
        if (e.tickCount % SEED_EVERY == 0 && seeds(e) && HarmontideConfig.deadRealm()) seed(level, mob);
        if (e.tickCount % STRENGTHEN == 0 && e instanceof Bonded) strengthen(mob);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 == 11 && !event.getEntity().level().isClientSide && inDeadRealm(event.getEntity())) {
            Dairy.award(event.getEntity(), "dead_realm_entered");
        }
    }

    /** Her touch: a patch of ground near her becomes grave grass (a source, depth 0). Where, or null. */
    @Nullable
    public static BlockPos seed(ServerLevel level, Mob lich) {
        BlockPos at = lich.blockPosition();
        for (int tries = 0; tries < 6; tries++) {
            int dx = lich.getRandom().nextInt(SEED_RADIUS * 2 + 1) - SEED_RADIUS, dz = lich.getRandom().nextInt(SEED_RADIUS * 2 + 1) - SEED_RADIUS;
            for (int dy = 3; dy >= -4; dy--) {
                BlockPos ground = at.offset(dx, dy, dz);
                if (!GraveGrassBlock.corrupt(level, ground, 0)) continue;
                level.sendParticles(GraveGrassBlock.MIST, ground.getX() + 0.5, ground.getY() + 1.2, ground.getZ() + 0.5, 8, 0.4, 0.2, 0.4, 0.0);
                return ground;
            }
        }
        return null;
    }

    /** A befriended undead girl on grave grass is at her strongest. True if she was strengthened. */
    public static boolean strengthen(Mob mob) {
        if (!(mob instanceof AbstractMonsterGirl girl) || girl.getSpecies().family() != GirlSpecies.Family.UNDEAD || !girl.hasAnyAffection() || !inDeadRealm(mob)) return false;
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, STRENGTHEN + 40, 0, true, false, true));
        mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, STRENGTHEN + 40, 0, true, false, true));
        return true;
    }

    /**
     * Deep in the realm, at night, one of the dead rises out of the ground: a wild undead girl. Never more than {@link #RISE_MAX}
     * undead girls within {@link #RISE_ALONE} blocks. Who rose, or null.
     */
    @Nullable
    public static AbstractMonsterGirl rise(ServerLevel level, BlockPos ground, boolean night) {
        if (!night || !HarmontideConfig.deadRealm() || !level.isEmptyBlock(ground.above()) || !level.isEmptyBlock(ground.above(2))) return null;
        if (level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(ground).inflate(RISE_ALONE), g -> g.getSpecies().family() == GirlSpecies.Family.UNDEAD).size() >= RISE_MAX) return null;
        GirlSpecies kind = RISEN.get(level.random.nextInt(RISEN.size()));
        if (!(ModGirls.type(kind).create(level) instanceof AbstractMonsterGirl girl)) return null;
        girl.moveTo(ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), MobSpawnType.EVENT, null);
        girl.restrictTo(ground, 16);
        level.addFreshEntity(girl);
        level.sendParticles(GraveGrassBlock.MIST, girl.getX(), girl.getY() + 0.5, girl.getZ(), 40, 0.5, 0.6, 0.5, 0.0);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, girl.getX(), girl.getY() + 0.5, girl.getZ(), 12, 0.4, 0.4, 0.4, 0.02);
        level.playSound(null, ground.above(), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), SoundSource.HOSTILE, 1.0F, 0.7F);
        return girl;
    }

    /** Grave grass within this box of a spot, for the Order's sisters. */
    public static boolean near(ServerLevel level, BlockPos pos, int r) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -2, -r), pos.offset(r, 1, r))) if (GraveGrassBlock.grave(level.getBlockState(p))) return true;
        return false;
    }
}
