package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The hoofed family (centaur_field_guide.html): horse, zebra, reindeer, mountain goat, alpaca. Herd and hoof:
 * they graze, follow their lead mare, kick back at whatever hurts them from behind, and are won over by grooming
 * (a brush). Devoted, she lets you ride her (the zebra never does) or, for the alpaca, carries a chest.
 */
public class Centaur extends AbstractMonsterGirl {
    public static final int RIDE_AFFECTION = 60;
    private static final int GROOM_COOLDOWN = 3000;

    private long nextGroom;
    private long nextKick;
    private long nextAlarm;
    private int fleeceTicks;
    private boolean leadMare;
    private final SimpleContainer pack = new SimpleContainer(27);

    public Centaur(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5) {
            @Override
            public boolean canUse() {
                return Centaur.this.getHealth() < Centaur.this.getMaxHealth() * 0.5F && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Centaur.this.startTimedAction(species == GirlSpecies.GOAT_CENTAUR ? Action.REAR : Action.KICK, 16);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.APPLE, Items.GOLDEN_CARROT, Items.CARROT, Items.SUGAR, Items.WHEAT), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::leader, 1.0, 10.0F, 5.0F));
        this.goalSelector.addGoal(6, new EatBlockGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (species == GirlSpecies.GOAT_CENTAUR || species == GirlSpecies.ZEBRA_CENTAUR) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> { Player p = this.bondedPlayer(BED_AFFECTION, 16); return p != null && m.distanceToSqr(p) < 6 * 6; }));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case HORSE_CENTAUR -> girlAttributes(30, 0.3, 4);
            case ZEBRA_CENTAUR -> girlAttributes(30, 0.3, 6);
            case GOAT_CENTAUR -> girlAttributes(26, 0.28, 5).add(Attributes.JUMP_STRENGTH, 0.75);
            case ALPACA_CENTAUR -> girlAttributes(24, 0.25, 2);
            default -> girlAttributes(28, 0.28, 3);
        };
    }

    // --- herd ----------------------------------------------------------------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (!(group instanceof HerdData)) {
            this.leadMare = true;                         // the first of her group leads it
            group = new HerdData();
        }
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    private static class HerdData implements SpawnGroupData {
    }

    public boolean isLeadMare() {
        return this.leadMare;
    }

    public void setLeadMare(boolean lead) {
        this.leadMare = lead;
    }

    @Nullable
    private LivingEntity leader() {
        if (this.leadMare || this.isVehicle()) return null;
        if (this.isBaby()) return this.mother();
        return this.nearest(Centaur.class, 32, c -> c.getType() == this.getType() && c.isLeadMare() && !c.isBaby());
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.CARROT) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.SUGAR) || stack.is(Items.WHEAT) || stack.is(Items.HAY_BLOCK);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.GOLDEN_CARROT) || stack.is(Items.HAY_BLOCK) ? 3 : stack.is(Items.APPLE) ? 2 : 1;
    }

    private boolean rideable() {
        return this.species != GirlSpecies.ZEBRA_CENTAUR && this.species != GirlSpecies.ALPACA_CENTAUR;
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        boolean client = this.level().isClientSide;
        if (stack.is(Items.BRUSH)) {                                   // grooming: the herd's way of saying trust
            if (this.level() instanceof ServerLevel server && server.getGameTime() >= this.nextGroom) {
                this.nextGroom = server.getGameTime() + GROOM_COOLDOWN;
                this.addAffection(player, 2);
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                this.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);
                this.hearts(server, 4);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.isBaby()) return null;
        if (stack.is(Items.SADDLE) && this.rideable() && !this.hasFlag(FLAG_SADDLED)) {
            if (this.getAffection(player) < RIDE_AFFECTION) {
                if (!client) player.displayClientMessage(Component.translatable("message.monstergirls.ride_refused", this.getDisplayName()), true);
                return InteractionResult.sidedSuccess(client);
            }
            if (!client) {
                this.setFlag(FLAG_SADDLED, true);
                stack.consume(1, player);
                this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.ALPACA_CENTAUR) {
            if (stack.is(Items.CHEST) && !this.hasFlag(FLAG_CHEST) && this.getAffection(player) >= RIDE_AFFECTION) {
                if (!client) {
                    this.setFlag(FLAG_CHEST, true);
                    stack.consume(1, player);
                    this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.is(Items.SHEARS) && this.fleeceTicks >= 24000) {   // her fleece, without hurting her bond
                if (!client) {
                    this.fleeceTicks = 0;
                    this.spawnAtLocation(new ItemStack(this.woolColour(), 1 + this.random.nextInt(2)));
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.isEmpty() && player.isShiftKeyDown() && this.hasFlag(FLAG_CHEST) && this.getAffection(player) >= RIDE_AFFECTION) {
                if (!client) player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, this.pack), this.getDisplayName()));
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (stack.isEmpty() && !player.isShiftKeyDown() && this.hasFlag(FLAG_SADDLED) && !this.isVehicle() && this.getAffection(player) >= RIDE_AFFECTION) {
            if (!client) {
                this.getNavigation().stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    private net.minecraft.world.item.Item woolColour() {
        return switch (this.getVariantName()) {
            case "fawn", "brown" -> Items.BROWN_WOOL;
            case "black" -> Items.BLACK_WOOL;
            case "grey" -> Items.LIGHT_GRAY_WOOL;
            default -> Items.WHITE_WOOL;
        };
    }

    public SimpleContainer getPack() {
        return this.pack;
    }

    public void setFleeceTicks(int t) {
        this.fleeceTicks = t;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case REINDEER_CENTAUR -> new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 75 ? Items.MOSS_BLOCK : Items.GLOW_LICHEN, r < 40 ? 4 : 1);
            case GOAT_CENTAUR -> new ItemStack(r < 60 ? Items.MILK_BUCKET : Items.EMERALD);
            case ALPACA_CENTAUR -> new ItemStack(this.woolColour(), 2);
            default -> new ItemStack(r < 50 ? Items.APPLE : r < 85 ? Items.WHEAT : Items.GOLDEN_CARROT, r < 85 ? 2 : 1);
        };
    }

    // --- riding --------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.hasFlag(FLAG_SADDLED) && this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        Vec2 rot = new Vec2(player.getXRot() * 0.5F, player.getYRot());
        this.setRot(rot.y, rot.x);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travel) {
        float forward = player.zza <= 0.0F ? player.zza * 0.25F : player.zza;
        return new Vec3(player.xxa * 0.5F, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float base = this.species == GirlSpecies.HORSE_CENTAUR ? 0.32F : 0.26F;
        return player.isSprinting() ? base * 1.45F : base;
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        return new Vec3(0.0, dims.height() * 0.55, 0.45);                // on her back, behind her waist
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.GOAT_CENTAUR && super.causeFallDamage(distance, multiplier, source);
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** A double-hoof kick at whatever hits her from behind; the zebra's herd scatters its attackers (dazzle). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt || !(this.level() instanceof ServerLevel server) || !(source.getEntity() instanceof LivingEntity attacker)) return hurt;
        Vec3 toAttacker = attacker.position().subtract(this.position()).normalize();
        Vec3 facing = Vec3.directionFromRotation(0, this.getYRot());
        if (toAttacker.dot(facing) < -0.3 && server.getGameTime() >= this.nextKick && attacker.distanceToSqr(this) < 9) {
            this.nextKick = server.getGameTime() + 40;
            this.startTimedAction(Action.KICK, 16);
            attacker.hurt(this.damageSources().mobAttack(this), this.species == GirlSpecies.ZEBRA_CENTAUR ? 7.0F : 5.0F);
            attacker.knockback(1.5, this.getX() - attacker.getX(), this.getZ() - attacker.getZ());
        }
        if (this.species == GirlSpecies.ALPACA_CENTAUR && attacker instanceof Player p && this.getAffection(p) < BED_AFFECTION) {
            this.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 1.0F);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SPIT, p.getX(), p.getEyeY(), p.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            p.hurt(this.damageSources().mobAttack(this), 1.0F);
        }
        if (this.species == GirlSpecies.ZEBRA_CENTAUR && attacker instanceof Mob mob && this.random.nextBoolean()) mob.setTarget(null);
        return hurt;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.ALPACA_CENTAUR && this.fleeceTicks < 24000) this.fleeceTicks++;
        if (this.species == GirlSpecies.ZEBRA_CENTAUR && server.getGameTime() >= this.nextAlarm && this.tickCount % 20 == 0
                && this.bondedPlayer(BED_AFFECTION, 32) != null) {
            Monster m = this.nearest(Monster.class, 16, x -> true);
            if (m != null) {                                         // the alarm bark
                this.nextAlarm = server.getGameTime() + 200;
                this.playSound(SoundEvents.DONKEY_ANGRY, 2.0F, 1.4F);
                m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                this.startTimedAction(Action.REAR, 30);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.hasFlag(FLAG_SADDLED)) this.spawnAtLocation(Items.SADDLE);
        if (this.hasFlag(FLAG_CHEST)) {
            this.spawnAtLocation(Items.CHEST);
            for (int i = 0; i < this.pack.getContainerSize(); i++) if (!this.pack.getItem(i).isEmpty()) this.spawnAtLocation(this.pack.getItem(i));
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case ZEBRA_CENTAUR -> SoundEvents.DONKEY_AMBIENT;
            case GOAT_CENTAUR -> SoundEvents.GOAT_AMBIENT;
            case ALPACA_CENTAUR -> SoundEvents.LLAMA_AMBIENT;
            default -> SoundEvents.HORSE_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == GirlSpecies.GOAT_CENTAUR ? SoundEvents.GOAT_HURT : this.species == GirlSpecies.ALPACA_CENTAUR ? SoundEvents.LLAMA_HURT : SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.HORSE_STEP, 0.25F, 1.1F);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.hasFlag(FLAG_SADDLED) || this.hasFlag(FLAG_CHEST);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("LeadMare", this.leadMare);
        tag.putInt("Fleece", this.fleeceTicks);
        tag.put("Pack", this.pack.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.leadMare = tag.getBoolean("LeadMare");
        this.fleeceTicks = tag.getInt("Fleece");
        this.pack.fromTag(tag.getList("Pack", 10), this.registryAccess());
    }
}
