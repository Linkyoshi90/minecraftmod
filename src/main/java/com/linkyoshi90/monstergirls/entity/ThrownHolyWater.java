package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.item.HolyWaterItem;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * A thrown splash holy water: where it breaks, the realm's grass, leaves and logs within the holy water's reach turn back
 * (HolyWaterItem#cleanse) and every possessed creature within {@link #FREE} blocks is freed (Monsterization#exorcise).
 * For a village that has begun to turn.
 */
public class ThrownHolyWater extends ThrowableItemProjectile {
    public static final double FREE = 4.0;

    public ThrownHolyWater(EntityType<? extends ThrownHolyWater> type, Level level) {
        super(type, level);
    }

    public ThrownHolyWater(Level level, LivingEntity thrower) {
        super(ModEntities.SPLASH_HOLY_WATER.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SPLASH_HOLY_WATER.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            BlockPos at = BlockPos.containing(result.getLocation());
            splash(level, at, this.getOwner() instanceof Player p ? p : null);
            if (this.getOwner() instanceof com.linkyoshi90.monstergirls.entity.order.OrderSister sister) sear(level, at, this, sister);
            this.discard();
        }
    }

    /** How far an inquisitrix's flask sears, and how much. */
    public static final double SEAR = 2.5;
    public static final float SEAR_DAMAGE = 4.0F;

    /**
     * An inquisitrix's flask burns the monster girls it breaks among (harpies and frog girls too): the realm's own, and any
     * girl in the fight. Players and everyone else only get wet. How many it seared.
     */
    public static int sear(ServerLevel level, BlockPos at, net.minecraft.world.entity.Entity flask, LivingEntity thrower) {
        int n = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(at).inflate(SEAR), m -> m instanceof Bonded && m.isAlive())) {
            if (mob.hurt(level.damageSources().indirectMagic(flask, thrower), SEAR_DAMAGE)) n++;
            level.sendParticles(ParticleTypes.WHITE_SMOKE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        }
        return n;
    }

    /** Where it broke: the realm turned back, the possessed freed. How many creatures were freed. */
    public static int splash(ServerLevel level, BlockPos at, Player thrower) {
        HolyWaterItem.cleanse(level, at);
        int freed = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(at).inflate(FREE))) {
            if (Monsterization.exorcise(level, mob)) freed++;
        }
        if (freed > 0 && thrower != null) {
            Dairy.award(thrower, "realm_exorcist");
            com.linkyoshi90.monstergirls.entity.order.Order.addNotoriety(thrower, -com.linkyoshi90.monstergirls.entity.order.Order.EXORCISM);
        }
        level.sendParticles(ParticleTypes.SPLASH, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 60, 1.5, 0.3, 1.5, 0.2);
        level.sendParticles(ParticleTypes.END_ROD, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 16, 1.5, 0.5, 1.5, 0.02);
        level.playSound(null, at, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, 1.1F);
        level.playSound(null, at, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.4F);
        return freed;
    }
}
