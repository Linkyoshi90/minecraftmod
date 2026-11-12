package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The small, wary mountain-pond toad with the fire-red belly (the lantern glows in the dark; the renderer adds the
 * glow mask). Threatened, she performs the Unken reflex, flashing her red belly and soles. She trills at dusk and
 * lives in the Nether too, where she is fireproof and never dries out.
 */
public class FireBelliedToad extends MucusToad {
    public FireBelliedToad(EntityType<? extends FireBelliedToad> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(12.0, 0.2);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(4, new AnuranRestGoal<>(this, Action.TRILLING, 300, 100, 240, t -> t.level().isNight()));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, t -> true));
    }

    /** Nether moisture drains very fast, so she is exempt there (open design question in the field guide). */
    @Override
    public boolean isWet() {
        return super.isWet() || this.level().dimension() == Level.NETHER;
    }

    @Override
    protected Action defenceAction() {
        return Action.UNKEN;
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return this.level().isNight() ? Action.TRILLING : Action.CROAKING;
    }

    @Override
    protected String species() {
        return "fire_bellied_toad";
    }
}
