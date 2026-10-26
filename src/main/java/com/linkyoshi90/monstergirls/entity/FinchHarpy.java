package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.Tags;

/**
 * A little seed-eating songbird who lives in flocks on meadows and flower fields (model by Kimi, tools/gen_finch_harpy.py).
 * At dawn one finch starts the chorus and every finch within earshot joins in; friends who hear it start the day lucky.
 */
public class FinchHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    public static final double CHORUS_RANGE = 16.0;
    private long nextChorus;

    public FinchHarpy(EntityType<? extends FinchHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkFinchHarpySpawnRules(EntityType<FinchHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                    RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the dawn chorus -------------------------------------------------------------------------------------------

    /** From the last hour of night to the first of the morning. */
    public static boolean isDawn(Level level) {
        long t = level.getDayTime() % 24000L;
        return t >= 23000 || t < 1500;
    }

    /** She starts the chorus: every finch around joins in, and friends who hear it get Luck for a minute. */
    public void startChorus(ServerLevel level) {
        for (FinchHarpy f : level.getEntitiesOfClass(FinchHarpy.class, this.getBoundingBox().inflate(CHORUS_RANGE), f -> !f.isBaby())) {
            f.nextChorus = level.getGameTime() + 2400;
            f.startTimedAction(Action.SINGING, 60);
            f.playSound(SoundEvents.PARROT_AMBIENT, 1.0F, 1.6F + f.random.nextFloat() * 0.3F);
            level.sendParticles(ParticleTypes.NOTE, f.getX(), f.getY() + f.getBbHeight() + 0.3, f.getZ(), 0, f.random.nextDouble(), 0, 0, 1);
        }
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(CHORUS_RANGE), p -> this.getAffection(p) >= FRIEND)) {
            p.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 0));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel server && !this.isBaby() && this.tickCount % 20 == 0 && isDawn(server)
                && server.getGameTime() >= this.nextChorus && this.random.nextInt(10) == 0) {
            this.startChorus(server);
        }
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.is(Tags.Items.SEEDS) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.finch." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.WHEAT_SEEDS, 4) : r < 65 ? new ItemStack(Items.FEATHER, 2) : r < 90 ? new ItemStack(Items.DANDELION, 2)
                : new ItemStack(Items.SUNFLOWER);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.FINCH_EGG.get());
    }

    @Override
    protected String species() {
        return "finch_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case SINGING -> "sing";
            case BATHING -> "bathe";
            default -> null;
        };
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.PARROT_EAT;
    }
}
