package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekWaterGoal;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

import javax.annotation.Nullable;

/**
 * The swamp midwife: fully aquatic, flat and leaf-brown, with no tongue. She carries up to three young in the pits
 * on her back; they develop for five in-game days and then climb out as tiny, fully formed Surinam toads (no
 * tadpole stage), who take another five days to grow up. Eggs are only pressed into her back when another adult
 * Surinam toad is nearby.
 */
public class SurinamToad extends MucusToad {
    private static final EntityDataAccessor<Integer> DATA_YOUNG =
            SynchedEntityData.defineId(SurinamToad.class, EntityDataSerializers.INT);

    public static final int MAX_YOUNG = 3;
    /** Five in-game days. */
    public static final int DEVELOP_TICKS = 5 * 24000;
    private static final int BIRTH_TICKS = 80;
    private static final int BIRTH_INTERVAL = 20;
    private static final double MATE_RANGE = 8.0;

    private int developTicks;
    private int birthTicks;

    public SurinamToad(EntityType<? extends SurinamToad> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WALKABLE, 8.0F); // she would rather not leave the water
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(14.0, 0.14).add(net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0);
    }

    /** She spawns in the water itself. */
    public static boolean checkSurinamSpawnRules(EntityType<SurinamToad> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                 BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected float waterPathMalus() {
        return 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SeekWaterGoal(this)); // stranded on land: crawl back
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.0, stack -> stack.is(com.linkyoshi90.monstergirls.registry.ModTags.MUCUS_TOAD_FOOD), false));
        this.goalSelector.addGoal(3, new AnuranRestGoal<>(this, Action.SWEEPING, 250, 60, 120, t -> true, true));
        this.goalSelector.addGoal(3, new AnuranRestGoal<>(this, Action.BROODING, 300, 60, 100, t -> t.getYoung() > 0, true));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.8, 40));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // --- young ----------------------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_YOUNG, 0);
    }

    public int getYoung() {
        return this.entityData.get(DATA_YOUNG);
    }

    public void setYoung(int young) {
        // a baby has no pits yet
        this.entityData.set(DATA_YOUNG, this.isBaby() ? 0 : Math.max(0, Math.min(MAX_YOUNG, young)));
        if (young > 0) this.developTicks = 0;
    }


    public void setDevelopTicks(int ticks) {
        this.developTicks = ticks;
    }


    private boolean mateNearby() {
        return !this.level().getEntitiesOfClass(SurinamToad.class, this.getBoundingBox().inflate(MATE_RANGE),
                o -> o != this && o.isAlive() && !o.isBaby()).isEmpty();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isBaby()) return; // growing up is handled by AbstractAnuran
        if (this.birthTicks > 0) {
            this.birthTicks--;
            if (this.birthTicks % BIRTH_INTERVAL == 0 && this.getYoung() > 0) this.bearYoung(server);
            if (this.birthTicks == 0) {
                this.setYoung(0);
                this.setAction(Action.NONE);
            }
        } else if (this.getYoung() > 0) {
            if (++this.developTicks >= DEVELOP_TICKS) {
                this.birthTicks = BIRTH_TICKS;
                this.setAction(Action.BIRTH);
            }
        } else if (this.isInWater() && this.tickCount % 200 == 0 && this.random.nextInt(4) == 0 && this.mateNearby()) {
            this.setYoung(1 + this.random.nextInt(MAX_YOUNG));
        }
    }

    private void bearYoung(ServerLevel level) {
        SurinamToad baby = ModEntities.SURINAM_TOAD.get().create(level);
        if (baby == null) return;
        baby.moveTo(this.getX() + (this.random.nextDouble() - 0.5), this.getY(), this.getZ() + (this.random.nextDouble() - 0.5), this.random.nextFloat() * 360.0F, 0.0F);
        baby.setBaby(true);
        level.addFreshEntity(baby);
        this.entityData.set(DATA_YOUNG, this.getYoung() - 1);
    }

    // --- species data ---------------------------------------------------------------------------------------------

    /** No tongue (the shared animation only opens her mouth), so she sweeps instead of lashing out. */
    @Override
    protected String idleAnimation() {
        return this.getYoung() > 0 ? "idle" : "idle_empty";
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return this.getAction() == Action.BIRTH ? null : Action.CROAKING;
    }

    @Override
    public boolean hasYoungModels() {
        return false;
    }

    @Override
    protected boolean isAquaticOnly() {
        return true;
    }

    @Override
    protected String species() {
        return "surinam_toad";
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Young", this.getYoung());
        tag.putInt("DevelopTicks", this.developTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_YOUNG, tag.getInt("Young"));
        this.developTicks = tag.getInt("DevelopTicks");
    }
}
