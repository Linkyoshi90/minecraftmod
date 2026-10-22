package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import com.linkyoshi90.monstergirls.entity.ai.BellowGoal;
import com.linkyoshi90.monstergirls.entity.ai.FleeIntoWaterGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekWaterGoal;
import com.linkyoshi90.monstergirls.entity.ai.TongueSnatchGoal;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The frog family's archetype (field guide: "swamp and riverbank, boisterous"): loud, boastful and pushy, yet she
 * backs down the moment someone pushes back. Alone, a hurt bullywug bolts for water; three or more of them gang up
 * on whoever hurt one of them. She snatches food and shiny things off the ground with her tongue, sits on her
 * lily pad, bellows at rival bullywugs, and dries out quickly away from water. Now and then she sheds a slime ball
 * (only the bullywug herself, not the other frogs of her family).
 */
public class Bullywug extends AbstractAnuran {
    /** Dry skin starts to hurt after about three minutes without water or rain. */
    private static final int DRY_LIMIT_TICKS = 3600;
    /** This many bullywugs (counting the victim) nearby turn a bluff into a brawl. */
    public static final int GANG_SIZE = 3;
    private static final double GANG_RADIUS = 16.0;
    private static final int FLEE_TICKS = 200;
    private static final int RETALIATE_TICKS = 400;

    /** A slime ball every five to ten minutes, like a chicken's egg. */
    public static final int SLIME_INTERVAL = 6000;

    private int fleeTicks;
    private int slimeTime = SLIME_INTERVAL + 1;
    private int retaliateTicks;
    @Nullable
    private Vec3 fleeFrom;

    public Bullywug(EntityType<? extends Bullywug> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(14.0, 0.25, 2.0, 1.2);
    }

    /** What every frog needs: health, speed, a bite for the species that fight, and the family swimming skill. */
    public static AttributeSupplier.Builder frogAttributes(double health, double speed, double damage, double knockback) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ATTACK_KNOCKBACK, knockback)
                .add(Attributes.JUMP_STRENGTH, 0.6)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FleeIntoWaterGoal(this));  // no FloatGoal: she breathes water and dives
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(3, new SeekWaterGoal(this));
        this.goalSelector.addGoal(4, new TongueSnatchGoal(this));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.1, stack -> stack.is(ModTags.BULLYWUG_FOOD), false));
        this.addSpeciesGoals();
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    /** The goals that make a species what she is (priorities 5 and 7 are free). The bullywug bellows and sits on her bank. */
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(5, new BellowGoal(this));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 400, 200, 500, b -> waterNearby(b.level(), b.blockPosition(), 4)));
    }

    // --- pushing back ---------------------------------------------------------------------------------------------

    public int getFleeTicks() {
        return this.fleeTicks;
    }

    public void setFleeTicks(int ticks) {
        this.fleeTicks = ticks;
    }

    @Nullable
    public Vec3 getFleeFrom() {
        return this.fleeFrom;
    }

    /** How many of her own kind (herself included) are close enough to join a fight. */
    public List<Bullywug> nearbyPack() {
        return this.level().getEntitiesOfClass(Bullywug.class, this.getBoundingBox().inflate(GANG_RADIUS),
                b -> b.isAlive() && !b.isBaby() && b.getType() == this.getType());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof Bullywug)) {
            if (this.isBaby()) this.startFleeing(attacker);
            else this.reactToAttacker(attacker, source);
        }
        return hurt;
    }

    /** The bully's answer to being hit: alone she backs down into the water, in a pack she gangs up. Species override. */
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        List<Bullywug> pack = this.nearbyPack();
        if (pack.size() >= GANG_SIZE) {
            for (Bullywug b : pack) b.retaliateAgainst(attacker);
        } else {
            this.startFleeing(attacker);
        }
    }

    protected void startFleeing(LivingEntity attacker) {
        this.fleeTicks = FLEE_TICKS;
        this.fleeFrom = attacker.position();
        this.setTarget(null);
    }

    protected void retaliateAgainst(LivingEntity attacker) {
        this.fleeTicks = 0;
        this.retaliateTicks = RETALIATE_TICKS;
        this.setTarget(attacker);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.fleeTicks > 0) this.fleeTicks--;
            if (this.retaliateTicks > 0 && --this.retaliateTicks == 0) this.setTarget(null);
            if (this.dropsSlimeBalls() && !this.isBaby() && --this.slimeTime <= 0) {
                this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(Items.SLIME_BALL);
                this.slimeTime = SLIME_INTERVAL + this.random.nextInt(SLIME_INTERVAL);
            }
        }
    }

    /** Only the bullywug herself; the rest of the frog family (which extend her) keep their skin to themselves. */
    protected boolean dropsSlimeBalls() {
        return this.getClass() == Bullywug.class;
    }

    public void setSlimeTime(int ticks) {
        this.slimeTime = ticks;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Undressed", this.isUndressed());
        tag.putInt("SlimeTime", this.slimeTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setUndressed(tag.getBoolean("Undressed"));
        if (tag.contains("SlimeTime")) this.slimeTime = tag.getInt("SlimeTime");
    }

    @Override
    protected String movingAnimation() {
        return this.isAggressive() ? "hop" : "walk";
    }

    // --- tongue ---------------------------------------------------------------------------------------------------

    /** Food to gulp or shiny things to show off. */
    public boolean wantsToSnatch(ItemStack stack) {
        return this.isFood(stack) || stack.is(ModTags.BULLYWUG_SHINIES);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.BULLYWUG_FOOD);
    }

    // --- species data ---------------------------------------------------------------------------------------------

    @Override
    protected String species() {
        return "bullywug";
    }

    @Override
    protected int dryLimitTicks() {
        return DRY_LIMIT_TICKS;
    }

    // --- her outfit: the lily-pad skirt and the cattails come off (reed shorts and wrap stay) ----------------------------

    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_UNDRESSED =
            net.minecraft.network.syncher.SynchedEntityData.defineId(Bullywug.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_UNDRESSED, false);
    }

    /** Whether she has taken off her lily-pad skirt and cattails (the renderer hides those bones). */
    public boolean isUndressed() {
        return this.entityData.get(DATA_UNDRESSED);
    }

    public void setUndressed(boolean undressed) {
        this.entityData.set(DATA_UNDRESSED, undressed);
    }

    /**
     * At high friendship, shears: she takes off her skirt and cattails and hands them to you (a lily pad and three
     * cattails, as sugar cane). A lily pad: she makes herself a new skirt and picks fresh cattails.
     */
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND && !this.isTadpole() && !this.isBaby()) {
            if (stack.is(Items.SHEARS) && !this.isUndressed() && this.getAffection(player) >= BED_AFFECTION) {
                if (!this.level().isClientSide) {
                    this.setUndressed(true);
                    this.spawnAtLocation(new ItemStack(Items.LILY_PAD));
                    this.spawnAtLocation(new ItemStack(Items.SUGAR_CANE, 3));
                    this.playSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (stack.is(Items.LILY_PAD) && this.isUndressed()) {
                if (!this.level().isClientSide) {
                    this.setUndressed(false);
                    stack.consume(1, player);
                    this.playSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }
}
