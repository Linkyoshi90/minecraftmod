package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import com.linkyoshi90.monstergirls.entity.ai.EatPestGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekWaterGoal;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Slime;

import javax.annotation.Nullable;

/**
 * The toad family's archetype (field guide: "damp woodland and caves, solitary, unbothered"): placid, slow and
 * stubborn. Her slippery skin shrugs off knockback and a bitter secretion gives anything that hits her nausea.
 * She eats silverfish, endermites and tiny slimes (a friend to farms), digs in under soft ground by day and comes
 * out at dusk and in rain, and dries out slowly.
 */
public class MucusToad extends AbstractAnuran {
    /** Toads dry out slowly: about twenty minutes without water or rain. */
    private static final int DRY_LIMIT_TICKS = 24000;
    /** How long the bitter secretion upsets a stomach. */
    public static final int NAUSEA_TICKS = 200;

    public MucusToad(EntityType<? extends MucusToad> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(16.0, 0.18);
    }

    /** What every toad needs: health, speed, slippery skin and a modest swimming skill. */
    public static AttributeSupplier.Builder toadAttributes(double health, double speed) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5) // slippery
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.6)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected float waterPathMalus() {
        return 4.0F; // she can swim but would rather not
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this) {          // she can't breathe water: up for air only when she needs it
            @Override
            public boolean canUse() {
                return MucusToad.this.getAirSupply() < MucusToad.this.getMaxAirSupply() && super.canUse();
            }
        });
        this.goalSelector.addGoal(1, new SeekWaterGoal(this));
        this.goalSelector.addGoal(2, new EatPestGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, stack -> stack.is(ModTags.MUCUS_TOAD_FOOD), false));
        this.addSpeciesGoals();
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** The goals that make a species what she is (priorities 4 and 5 are free). The mucus toad digs in by day and sits. */
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(4, new AnuranRestGoal<>(this, Action.DIGGING, 100, 1200, 2400, MucusToad::shouldBeBuried));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, t -> true));
    }

    /** Daytime and dry, on ground soft enough to dig into. Dusk and rain bring her out. */
    public boolean shouldBeBuried() {
        Level level = this.level();
        return level.isDay() && !level.isRaining() && !this.isDry()
                && level.getBlockState(this.blockPosition().below()).is(ModTags.MUCUS_TOAD_DIGGABLE);
    }

    public boolean isPest(LivingEntity entity) {
        if (!entity.getType().is(ModTags.MUCUS_TOAD_PREY)) return false;
        return !(entity instanceof Slime slime) || slime.getSize() <= 1;
    }

    /** What her tongue hunt looks like: a flick, or a gulp for the cane toad. */
    public Action huntAction() {
        return Action.TONGUE;
    }

    /** What happens to a pest her tongue catches. */
    public void swallow(LivingEntity prey) {
        prey.hurt(this.damageSources().mobAttack(this), 8.0F);
    }

    // --- mucus ----------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.setAction(Action.NONE);
            this.startTimedAction(this.defenceAction(), 40);
            if (source.getDirectEntity() instanceof LivingEntity attacker) this.secrete(attacker);
        }
        return hurt;
    }

    protected Action defenceAction() {
        return Action.PUFFING;
    }

    /** Her mucus on whatever hit her: nausea. Venomous cousins add more. */
    protected void secrete(LivingEntity attacker) {
        attacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS), this);
    }

    // --- species data ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.MUCUS_TOAD_FOOD);
    }

    @Override
    protected String species() {
        return "mucus_toad";
    }

    @Override
    protected int dryLimitTicks() {
        return DRY_LIMIT_TICKS;
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return this.getAction() == Action.DIGGING ? null : Action.CROAKING; // asleep under the soil
    }
}
