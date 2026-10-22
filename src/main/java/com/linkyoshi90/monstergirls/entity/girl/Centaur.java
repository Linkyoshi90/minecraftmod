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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The hoofed family (centaur_field_guide.html): horse, zebra, reindeer, mountain goat, alpaca. Herd and hoof:
 * they graze, follow their lead mare, kick back at whatever hurts them from behind, and are won over by grooming
 * (a brush). At high friendship she lets you ride on her back (a saddle makes her faster) and wears a chestplate you
 * give her (it shows over her torso and adds to her health); the devoted alpaca carries a chest. Any centaur who sees
 * a player sprinting by gallops alongside, grinning from ear to ear.
 */
public class Centaur extends AbstractMonsterGirl {
    public static final int RIDE_AFFECTION = BED_AFFECTION;
    public static final int ARMOUR_AFFECTION = 10;
    private static final net.minecraft.resources.ResourceLocation ARMOUR_HEALTH = com.linkyoshi90.monstergirls.MonsterGirlsMod.id("centaur_armour_health");
    private static final int GROOM_COOLDOWN = 3000;
    /** A full gallop lasts 15 seconds; at zero she slows to a walk until she has caught her breath (a third back). */
    static final float MAX_STAMINA = 300.0F;

    private long nextGroom;
    private long nextKick;
    private long nextAlarm;
    private int fleeceTicks;
    private float stamina = MAX_STAMINA;
    private boolean winded;
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
        this.goalSelector.addGoal(3, new RunAlongGoal());
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
            case TAPIR_CENTAUR -> girlAttributes(32, 0.24, 3);
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
        return stack.is(Items.APPLE) || stack.is(Items.CARROT) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.SUGAR) || stack.is(Items.WHEAT) || stack.is(Items.HAY_BLOCK)
                || (this.species == GirlSpecies.TAPIR_CENTAUR && stack.is(Items.MELON_SLICE));
    }

    /** The baku: no phantom (vanilla or girl) comes for a player within 32 blocks of a tapir. */
    public static boolean dreamWard(Player player) {
        return !player.level().getEntitiesOfClass(Centaur.class, player.getBoundingBox().inflate(32),
                c -> c.isAlive() && c.species == GirlSpecies.TAPIR_CENTAUR).isEmpty();
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.GOLDEN_CARROT) || stack.is(Items.HAY_BLOCK) ? 3 : stack.is(Items.APPLE) ? 2 : 1;
    }

    private static boolean isChestplate(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem a && a.getType() == ArmorItem.Type.CHESTPLATE;
    }

    /** Her chestplate adds twice its armour points to her health (leather +6 ... diamond and netherite +16). */
    public void refreshArmourHealth() {
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        health.removeModifier(ARMOUR_HEALTH);
        if (this.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem a) {
            health.addTransientModifier(new AttributeModifier(ARMOUR_HEALTH, a.getDefense() * 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
        if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
    }

    public void wearArmour(ItemStack chestplate) {
        this.setItemSlot(EquipmentSlot.CHEST, chestplate);
        this.setDropChance(EquipmentSlot.CHEST, 2.0F);
        this.refreshArmourHealth();
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
        if (isChestplate(stack)) {                                     // torso armour
            if (this.getAffection(player) < ARMOUR_AFFECTION) return InteractionResult.PASS;
            if (!client) {
                ItemStack old = this.getItemBySlot(EquipmentSlot.CHEST);
                this.wearArmour(stack.split(1));
                if (!old.isEmpty() && !player.getInventory().add(old)) this.spawnAtLocation(old);
                this.heal(4.0F);
                this.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.0F);
                if (this.level() instanceof ServerLevel server) this.hearts(server, 2);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(Items.SADDLE) && !this.hasFlag(FLAG_SADDLED)) {
            if (this.getAffection(player) < RIDE_AFFECTION || !this.mayCarry(player)) {
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
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            ItemStack worn = this.getItemBySlot(EquipmentSlot.CHEST);
            if (!worn.isEmpty() && this.getAffection(player) >= ARMOUR_AFFECTION) {   // help her out of her armour
                if (!client) {
                    this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    this.refreshArmourHealth();
                    if (!player.getInventory().add(worn)) this.spawnAtLocation(worn);
                    this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            return this.getAffection(player) >= RIDE_AFFECTION ? this.pet(player) : null;   // riders pat her while crouching
        }
        if (stack.isEmpty() && !this.isVehicle() && this.getAffection(player) >= RIDE_AFFECTION) {   // high friendship: up on her back
            if (!this.mayCarry(player)) {
                if (!client) player.displayClientMessage(Component.translatable("message.monstergirls.ride_refused", this.getDisplayName()), true);
                return InteractionResult.sidedSuccess(client);
            }
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
            case TAPIR_CENTAUR -> new ItemStack(r < 50 ? Items.MELON_SLICE : r < 85 ? Items.SWEET_BERRIES : Items.GLOW_BERRIES, 3);
            default -> new ItemStack(r < 50 ? Items.APPLE : r < 85 ? Items.WHEAT : Items.GOLDEN_CARROT, r < 85 ? 2 : 1);
        };
    }

    // --- riding --------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player p && (this.hasFlag(FLAG_SADDLED) || this.getAffection(p) >= RIDE_AFFECTION) ? p : super.getControllingPassenger();
    }

    /** The zebra is famously never tamed: only her father may ride her. */
    public boolean mayCarry(Player player) {
        return this.species != GirlSpecies.ZEBRA_CENTAUR || player.getUUID().equals(this.getFather());
    }

    public float getStamina() {
        return this.stamina;
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        boolean galloping = player.isSprinting() && travel.lengthSqr() > 0 && !this.winded;
        this.stamina = Math.max(0.0F, Math.min(MAX_STAMINA, this.stamina + (galloping ? -1.0F : 0.5F)));
        if (this.stamina <= 0.0F && !this.winded) {
            this.winded = true;
            if (!this.level().isClientSide) player.displayClientMessage(Component.translatable("message.monstergirls.winded", this.getDisplayName()), true);
        } else if (this.winded && this.stamina >= MAX_STAMINA / 3) {
            this.winded = false;
        }
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
        float base = (this.species == GirlSpecies.HORSE_CENTAUR ? 0.32F : 0.26F) * (this.hasFlag(FLAG_SADDLED) ? 1.15F : 1.0F);
        return player.isSprinting() && !this.winded ? base * 1.45F : this.winded ? base * 0.7F : base;
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
        return this.hasFlag(FLAG_SADDLED) || this.hasFlag(FLAG_CHEST) || !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
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
        this.refreshArmourHealth();
        if (tag.contains("Health")) this.setHealth(tag.getFloat("Health"));
    }

    // --- running together ----------------------------------------------------------------------------------------

    /** The player she is galloping beside, or null. */
    @Nullable
    private Player runningWith;

    public boolean isRunningAlong() {
        return this.runningWith != null;
    }

    /**
     * Wild or tame, a centaur who sees a player sprint past cannot help herself: she gallops up beside them and keeps pace,
     * grinning, until they slow down.
     */
    class RunAlongGoal extends Goal {
        private int slowTicks;

        RunAlongGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean sprinting(Player p) {
            return p.isSprinting() && !p.isPassenger() && !p.isSpectator() && !p.isFallFlying();
        }

        @Override
        public boolean canUse() {
            Centaur c = Centaur.this;
            if (c.isBaby() || c.isOrderedToSit() || c.getTarget() != null || c.isVehicle() || c.tickCount % 7 != 0) return false;
            Player p = c.level().getNearestPlayer(c, 16.0);
            if (p == null || !this.sprinting(p) || c.isHostileTo(p)) return false;
            c.runningWith = p;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            Player p = Centaur.this.runningWith;
            return p != null && p.isAlive() && Centaur.this.getTarget() == null && !Centaur.this.isOrderedToSit()
                    && this.slowTicks < 40 && p.distanceToSqr(Centaur.this) < 40 * 40;
        }

        @Override
        public void start() {
            this.slowTicks = 0;
            Centaur.this.setFlag(FLAG_SMILE, true);
        }

        @Override
        public void stop() {
            Centaur.this.runningWith = null;
            Centaur.this.setSprinting(false);
            Centaur.this.setFlag(FLAG_SMILE, false);
            Centaur.this.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Centaur c = Centaur.this;
            Player p = c.runningWith;
            if (p == null) return;
            this.slowTicks = this.sprinting(p) ? 0 : this.slowTicks + 1;
            Vec3 fwd = Vec3.directionFromRotation(0, p.getYRot());
            Vec3 side = new Vec3(-fwd.z, 0, fwd.x);
            if (side.dot(c.position().subtract(p.position())) < 0) side = side.scale(-1);        // stay on the side she came from
            Vec3 spot = p.position().add(side.scale(2.2)).add(fwd.scale(0.5));
            c.setSprinting(true);
            if (c.tickCount % 3 == 0) c.getNavigation().moveTo(spot.x, spot.y, spot.z, c.distanceToSqr(spot) > 16 ? 1.9 : 1.5);
            c.getLookControl().setLookAt(p, 30.0F, 30.0F);
        }
    }
}
