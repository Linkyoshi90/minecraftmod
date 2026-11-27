package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Dream-Leviathan herself while someone is close enough to see her as more than a shape in the sky: a whale of night sky the size
 * of a ship. She has no will of her own here: leviathan/LeviathanEvent moves her (flying, landing, lying sick, dying and rotting, or
 * rising healed) and spawns or removes this body as players come near or leave. She is never saved with the chunk; the event is.
 * <p>
 * While she lies sick on the ground, you climb in by crouching at her open mouth (or, with the nsfw setting on, at her rear): see
 * {@link LeviathanEvent#entrance}. Lying dead, she gives up a dream scale to whoever pulls at her.
 */
public class DreamLeviathan extends Entity {
    public static final int FLYING = 0, LANDED = 1, DEAD = 2, RISING = 3;
    /** How big she is drawn: model units to blocks (client/DreamLeviathanModel is 142 units nose to fluke, so about 40 blocks). */
    public static final float SCALE = 4.5F;
    /** How far her mouth is in front of her middle, and her rear behind it, in blocks. */
    public static final double MOUTH = 14.0, REAR = 9.0;

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(DreamLeviathan.class, EntityDataSerializers.INT);
    /** How sick she looks: 0 healthy .. 3 at her worst. */
    private static final EntityDataAccessor<Integer> SICK = SynchedEntityData.defineId(DreamLeviathan.class, EntityDataSerializers.INT);
    /** Dead: how far gone (0 fresh, 1 rotting, 2 bones). */
    private static final EntityDataAccessor<Integer> ROT = SynchedEntityData.defineId(DreamLeviathan.class, EntityDataSerializers.INT);

    /** The client's game time when it last had her body to draw (client/DreamSky draws her as a far shape only without it). */
    public static long clientSeen = -100;

    public DreamLeviathan(EntityType<? extends DreamLeviathan> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        b.define(MODE, FLYING);
        b.define(SICK, 0);
        b.define(ROT, 0);
    }

    public int mode() {
        return this.entityData.get(MODE);
    }

    public int sick() {
        return this.entityData.get(SICK);
    }

    public int rot() {
        return this.entityData.get(ROT);
    }

    public void setLook(int mode, int sick, int rot) {
        if (this.mode() != mode) this.entityData.set(MODE, mode);
        if (this.sick() != sick) this.entityData.set(SICK, sick);
        if (this.rot() != rot) this.entityData.set(ROT, rot);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            clientSeen = this.level().getGameTime();
            this.clientEffects();
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (!LeviathanEvent.get(level).owns(this)) {                                  // a body the event no longer knows
            this.discard();
            return;
        }
        if (this.tickCount % 200 == 37) {
            boolean call = this.mode() == FLYING || this.mode() == RISING;
            if (call || this.mode() == LANDED) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.NEUTRAL,
                        call ? 9.0F : 5.0F, call ? 0.45F : 0.32F);
            }
        }
    }

    /** Glitter trailing from her fins in flight, spores and coughs while sick, flies over her body once she is dead. */
    private void clientEffects() {
        var r = this.random;
        Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot());
        Vec3 side = new Vec3(-fwd.z, 0, fwd.x);
        if (this.mode() == FLYING || this.mode() == RISING) {
            for (int s = -1; s <= 1; s += 2) {
                Vec3 fin = this.position().add(side.scale(s * 9.0)).add(fwd.scale(4.0)).add(0, 1.5, 0);
                if (r.nextInt(2) == 0) this.level().addParticle(ParticleTypes.END_ROD, fin.x + r.nextGaussian(), fin.y + r.nextGaussian() * 0.5,
                        fin.z + r.nextGaussian(), fwd.x * -0.05, -0.02, fwd.z * -0.05);
            }
            if (this.sick() > 0 && r.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX() + r.nextGaussian() * 6, this.getY() + r.nextGaussian() * 2,
                        this.getZ() + r.nextGaussian() * 6, 0, -0.05, 0);
            }
        } else if (this.mode() == LANDED) {
            if (r.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX() + r.nextGaussian() * 8, this.getY() + 3 + r.nextGaussian() * 2,
                        this.getZ() + r.nextGaussian() * 8, 0, -0.05, 0);
            }
            if (this.tickCount % 90 == 0) {                                          // a weak breath from her blowhole
                Vec3 hole = this.position().add(fwd.scale(5)).add(0, 7.5, 0);
                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(ParticleTypes.CLOUD, hole.x, hole.y, hole.z, r.nextGaussian() * 0.05, 0.25 + r.nextFloat() * 0.2, r.nextGaussian() * 0.05);
                }
            }
        } else if (this.mode() == DEAD && this.rot() < 2 && r.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.ASH, this.getX() + r.nextGaussian() * 9, this.getY() + 2 + r.nextFloat() * 5,
                    this.getZ() + r.nextGaussian() * 9, 0, 0.02, 0);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer sp && this.level() instanceof ServerLevel level) {
            LeviathanEvent event = LeviathanEvent.get(level);
            if (this.mode() == DEAD) return event.harvest(level, sp) ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            if (this.mode() == LANDED) {
                if (!event.tryEnter(level, sp)) {
                    sp.displayClientMessage(net.minecraft.network.chat.Component.translatable(LeviathanEvent.nsfwEntry()
                            ? "message.harmontide.leviathan.go_to_rear" : "message.harmontide.leviathan.go_to_mouth"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return this.mode() == LANDED || this.mode() == DEAD ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
    }

    @Override
    public boolean isPickable() {
        return this.mode() == LANDED || this.mode() == DEAD;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /** Where her mouth (or rear) opens, at her side of the ground, for the given yaw and middle. */
    public static Vec3 opening(Vec3 middle, float yaw, boolean rear) {
        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        return middle.add(fwd.scale(rear ? -REAR : MOUTH));
    }

    public float bodyYaw(float partialTick) {
        return Mth.rotLerp(partialTick, this.yRotO, this.getYRot());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
