package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * A length of a rearing serpent's body (tools/serpent_rig.py: the Macawtl, the Turquoise Serpent, Quetzalcoatl): its own hitbox, laid out behind
 * her along the ground as the model is. Hits and clicks on it go to her, as on the Ender Dragon's parts.
 */
public class SerpentSegment extends PartEntity<AbstractMonsterGirl> {
    private static final SerpentSegment[] NONE = {};
    /** How far behind her each segment sits, and how wide it is, in blocks (her body tapers to the tail). */
    private static final float[] BACK = {0.7F, 1.45F, 2.2F, 2.9F};
    private static final float[] WIDTH = {0.85F, 0.8F, 0.65F, 0.45F};

    private final EntityDimensions size;
    private final float back;

    private SerpentSegment(AbstractMonsterGirl serpent, int index) {
        super(serpent);
        this.back = BACK[index];
        this.size = EntityDimensions.scalable(WIDTH[index], WIDTH[index] * 0.8F);
        this.refreshDimensions();
    }

    static SerpentSegment[] of(AbstractMonsterGirl serpent, GirlSpecies species) {
        if (species != GirlSpecies.CENTIPEDE_LAMIA && species != GirlSpecies.TURQUOISE_SERPENT_LAMIA && species != GirlSpecies.MACAWTL_DRAGON && species != GirlSpecies.AMPHIPTERE_DRAGON && species != GirlSpecies.QUETZALCOATL_DRAGON) return NONE;
        SerpentSegment[] parts = new SerpentSegment[BACK.length];
        for (int i = 0; i < parts.length; i++) parts[i] = new SerpentSegment(serpent, i);
        return parts;
    }

    /** Every tick, on both sides: the body stretched out straight behind her, as the model lies. */
    static void follow(AbstractMonsterGirl serpent, SerpentSegment[] parts) {
        double yaw = Math.toRadians(serpent.yBodyRot), scale = serpent.getScale();
        for (SerpentSegment part : parts) {
            part.xo = part.getX();
            part.yo = part.getY();
            part.zo = part.getZ();
            part.setPos(serpent.getX() + Math.sin(yaw) * part.back * scale, serpent.getY(), serpent.getZ() - Math.cos(yaw) * part.back * scale);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.getParent().hurt(source, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return this.getParent().interact(player, hand);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
