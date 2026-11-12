package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import com.linkyoshi90.harmontide.entity.ai.AnswerChorusGoal;
import com.linkyoshi90.harmontide.entity.ai.ChorusGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The fast toad: quicker than the others, active at night as well as by day, and she scampers rather than waddles.
 * Her loud rolling chorus draws every other natterjack in earshot; in the heat of the day she burrows into sand.
 */
public class Natterjack extends MucusToad {
    /** How far a chorus carries. */
    public static final double CHORUS_RANGE = 32.0;
    private static final int CALL_MEMORY_TICKS = 400;

    @Nullable
    private BlockPos callPos;
    private int callTicks;

    public Natterjack(EntityType<? extends Natterjack> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(12.0, 0.28);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(3, new AnswerChorusGoal(this));
        this.goalSelector.addGoal(4, new ChorusGoal(this));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.BURROWING, 500, 300, 800,
                t -> t.level().isDay() && !t.level().isRaining() && t.level().getBlockState(t.blockPosition().below()).is(BlockTags.SAND)));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, t -> true));
    }

    /** Another natterjack's chorus reached her. */
    public void hearChorus(BlockPos source) {
        this.callPos = source.immutable();
        this.callTicks = CALL_MEMORY_TICKS;
    }

    @Nullable
    public BlockPos getCallPos() {
        return this.callPos;
    }

    public void forgetCall() {
        this.callPos = null;
        this.callTicks = 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.callTicks > 0 && --this.callTicks == 0) this.callPos = null;
    }

    @Override
    protected String movingAnimation() {
        return "run";
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return this.getAction() == Action.BURROWING ? null : Action.CROAKING;
    }

    @Override
    protected String species() {
        return "natterjack";
    }
}
