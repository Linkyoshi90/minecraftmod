package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The spider family (arachne_field_guide.html): jumping spider, orb weaver, tarantula, trapdoor spider, wolf spider.
 * Silk is everything: she regrows silk you can brush off her, spins webs that fade by morning (the orb weaver's
 * phantom net), climbs walls, bites with venom, and extends the spider truce to her devoted player. Most lay a silk
 * egg sac; the wolf spider carries her young on her back instead.
 */
public class Arachne extends AbstractMonsterGirl {
    private static final int SILK_REGROW = 3000;

    private int silkTicks;
    private long nextMoult;
    private int carryTicks;
    private final List<BlockPos> webs = new ArrayList<>();
    private long websExpire;
    /** The orb weaver's hunting web (wild, hostile ones only): it stays until she is calmed. */
    private final List<BlockPos> trapWebs = new ArrayList<>();
    @Nullable
    private BlockPos trapCentre;
    /** The trapdoor spider's hole: she waits in it, unseen, and bursts out at whatever walks past. */
    @Nullable
    private BlockPos burrow;
    private boolean inBurrow;
    private int ambushCooldown;

    public Arachne(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.JUMPING_SPIDER_ARACHNE) {
            this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.6F));
            this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD, Items.GOLD_NUGGET), false));
        }
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Arachne.this.startTimedAction(Action.LEAP, 14);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.85) {
            @Override
            public boolean canUse() {
                return !Arachne.this.isHidden() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (species == GirlSpecies.ORB_WEAVER_ARACHNE) {
            // anything caught in her hunting web is dinner (a player she is calm with excepted)
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, false, false,
                    e -> this.isCaughtInTrap(e) && !(e instanceof Arachne) && !(e instanceof Player p && !this.isHostileTo(p))));
        }
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, true, r -> this.bestAffection() < 60));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, true, c -> this.bestAffection() < 10));
        if (species == GirlSpecies.TRAPDOOR_SPIDER_ARACHNE || species == GirlSpecies.WOLF_SPIDER_ARACHNE) {
            // the door guard and the hunter go after monsters near the player they love
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> m.distanceToSqr(this) < (species == GirlSpecies.TRAPDOOR_SPIDER_ARACHNE ? 4 * 4 : 16 * 16) && this.bondedPlayer(BED_AFFECTION, 24) != null));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case TARANTULA_ARACHNE -> girlAttributes(30, 0.24, 4);
            case JUMPING_SPIDER_ARACHNE -> girlAttributes(14, 0.3, 2);
            case TRAPDOOR_SPIDER_ARACHNE -> girlAttributes(24, 0.28, 7);
            case WOLF_SPIDER_ARACHNE -> girlAttributes(22, 0.34, 4);
            default -> girlAttributes(20, 0.28, 3);
        };
    }

    /** The ambush hunters: the trapdoor spider and the orb weaver attack on sight until someone calms them with meat. */
    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.TRAPDOOR_SPIDER_ARACHNE || this.species == GirlSpecies.ORB_WEAVER_ARACHNE;
    }

    @Override
    protected void calmDown(ServerLevel level, Player player) {
        super.calmDown(level, player);
        this.leaveBurrow();
        this.clearTrap(level);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    /** Every arachne but the trapdoor spider walks up walls. */
    @Override
    public boolean onClimbable() {
        return (this.species != GirlSpecies.TRAPDOOR_SPIDER_ARACHNE && this.horizontalCollision) || super.onClimbable();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.JUMPING_SPIDER_ARACHNE && super.causeFallDamage(distance, multiplier, source);   // a safety line
    }

    // --- silk, food, presents ------------------------------------------------------------------------------------

    public boolean hasSilk() {
        return this.silkTicks >= SILK_REGROW;
    }

    public void setSilkTicks(int t) {
        this.silkTicks = t;
    }

    /** A brush gently gathers her silk (+1). */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.is(Items.BRUSH)) return null;
        if (!this.hasSilk() || this.isBaby()) return InteractionResult.PASS;
        if (this.level() instanceof ServerLevel server) {
            this.silkTicks = 0;
            this.spawnAtLocation(new ItemStack(Items.STRING, 1 + this.random.nextInt(2)));
            if (this.random.nextInt(3) == 0) this.spawnAtLocation(new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.SPIDER_SILK.get()));
            this.addAffection(player, 1);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            this.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);
            this.hearts(server, 2);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CHICKEN) || stack.is(Items.RABBIT) || stack.is(Items.SPIDER_EYE) || stack.is(Items.STRING);
    }

    @Override
    protected int petValue(Player player) {
        return this.species == GirlSpecies.TARANTULA_ARACHNE ? 2 : 1;      // the tarantula loves being held
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.getAffection(player) >= 60 && r < 55) {                  // a silk-wrapped present: a cocoon to tear open
            ItemStack inside = new ItemStack(r < 3 ? Items.DIAMOND : r < 10 ? Items.BOOK : r < 30 ? Items.GOLD_INGOT : Items.IRON_INGOT);
            return com.linkyoshi90.monstergirls.item.CocoonItem.wrapping(inside);
        }
        return new ItemStack(r < 70 ? Items.STRING : r < 85 ? Items.COBWEB : Items.SPIDER_EYE, r < 70 ? 2 + this.random.nextInt(3) : 1);
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(2);                                 // of four to six, most go ballooning
    }

    /** A silk egg sac on the ground by a wall (the wolf spider carries her spiderlings on her back instead). */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        if (this.species == GirlSpecies.WOLF_SPIDER_ARACHNE) {
            super.giveBirth(level, father);
            this.carryTicks = 24000;
            this.setFlag(FLAG_CARRYING, true);
            return true;
        }
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-3, -1, -3), o.offset(3, 1, 3))) {
            if (!canHoldBlock(level, p)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.SILK), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father);
            for (int i = 0; i < 4; i++) {                                   // most of the brood balloons away on silk
                level.sendParticles(ParticleTypes.WHITE_ASH, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 6, 0.3, 1.5, 0.3, 0.02);
            }
            return true;
        }
        return super.giveBirth(level, father);
    }

    /** Eight eyes see in the dark: you wake beside her with night vision. */
    @Override
    public void onNightShared(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0));
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isHidden() ? null : SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.3F;
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public boolean doHurtTarget(Entity target) {
        this.setHidden(false);
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l) {
            l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            if (this.species == GirlSpecies.TRAPDOOR_SPIDER_ARACHNE) l.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        }
        return hit;
    }

    /** The tarantula flicks itchy hairs at whatever hurts her. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        this.leaveBurrow();
        this.setHidden(false);
        if (hurt && this.species == GirlSpecies.TARANTULA_ARACHNE && source.getEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 16) {
            attacker.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        }
        return hurt;
    }

    public boolean isHidden() {
        return this.hasFlag(FLAG_HIDDEN);
    }

    public void setHidden(boolean hidden) {
        if (hidden != this.isHidden()) {
            this.setFlag(FLAG_HIDDEN, hidden);
            this.setInvisible(hidden);
        }
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.silkTicks < SILK_REGROW) this.silkTicks++;
        long now = server.getGameTime();
        if (this.carryTicks > 0 && --this.carryTicks == 0) this.setFlag(FLAG_CARRYING, false);
        if (this.species == GirlSpecies.TARANTULA_ARACHNE) {
            if (this.nextMoult == 0) this.nextMoult = now + 3 * 24000 + this.random.nextInt(2 * 24000);
            if (now >= this.nextMoult) {
                this.nextMoult = now + 3 * 24000 + this.random.nextInt(2 * 24000);
                Player p = this.bondedPlayer(BED_AFFECTION, 16);
                ItemStack moult = new ItemStack(ModItems.EXOSKELETON.get());
                if (p == null || !p.getInventory().add(moult)) this.spawnAtLocation(moult);
            }
        }
        if (this.species == GirlSpecies.TRAPDOOR_SPIDER_ARACHNE) this.tickBurrow(server);
        if (this.species == GirlSpecies.ORB_WEAVER_ARACHNE && !this.isCalm() && !this.isBaby() && this.tickCount % 61 == 0) this.spinTrap(server);
        if (this.species == GirlSpecies.ORB_WEAVER_ARACHNE && this.tickCount % 200 == 0) this.phantomNet(server);
        if (!this.webs.isEmpty() && (now >= this.websExpire || server.isDay() && this.species == GirlSpecies.ORB_WEAVER_ARACHNE && this.tickCount % 200 == 0)) {
            this.clearWebs(server);
        }
    }

    // --- the trapdoor spider's burrow ----------------------------------------------------------------------------

    /** Soft ground she can dig her hole in. */
    private static boolean diggable(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.CLAY) || s.is(Blocks.SOUL_SOIL);
    }

    public boolean isInBurrow() {
        return this.inBurrow;
    }

    @Nullable
    public BlockPos getBurrow() {
        return this.burrow;
    }

    /**
     * Wild (or with no player of hers around) she digs a hole in soft ground and sinks into it, out of sight. Whatever
     * comes within three blocks (a player who has not calmed her, an animal; only monsters once she is calm) and she
     * bursts out and bites. Afterwards she goes back to her hole.
     */
    private void tickBurrow(ServerLevel level) {
        if (this.ambushCooldown > 0) this.ambushCooldown--;
        boolean lurk = !this.isBaby() && !this.isOrderedToSit() && this.getTarget() == null && this.ambushCooldown == 0
                && (!this.isCalm() || this.bondedPlayer(BED_AFFECTION, 16) == null);
        if (this.inBurrow) {
            LivingEntity prey = lurk ? this.ambushPrey() : null;
            if (!lurk || prey != null || this.burrow == null || !level.getBlockState(this.burrow).isAir()) {
                this.leaveBurrow();
                if (prey != null) {
                    this.setTarget(prey);
                    Vec3 d = prey.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(d.x * 0.6, 0.55, d.z * 0.6);
                    this.startTimedAction(Action.LEAP, 14);
                    this.playSound(SoundEvents.SPIDER_AMBIENT, 1.2F, 0.8F);
                }
            }
            return;
        }
        if (!lurk) return;
        if (this.burrow == null && this.onGround() && this.tickCount % 41 == 0) {
            BlockPos below = this.blockPosition().below();
            if (diggable(level.getBlockState(below)) && level.getBlockState(below.above()).isAir()) {
                level.destroyBlock(below, false, this);
                this.burrow = below.immutable();
                this.startTimedAction(Action.DIG, 30);
            }
        }
        if (this.burrow == null) return;
        Vec3 hole = Vec3.atBottomCenterOf(this.burrow);
        if (this.distanceToSqr(hole.x, this.getY(), hole.z) > 1.0) {
            if (this.tickCount % 20 == 0) this.getNavigation().moveTo(hole.x, hole.y + 1, hole.z, 0.9);
            if (this.distanceToSqr(hole) > 24 * 24) this.burrow = null;            // strayed too far: she digs a new one
            return;
        }
        if (level.getBlockState(this.burrow).isAir()) this.enterBurrow();
    }

    private void enterBurrow() {
        this.getNavigation().stop();
        this.inBurrow = true;
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.moveTo(this.burrow.getX() + 0.5, this.burrow.getY() - 0.4, this.burrow.getZ() + 0.5, this.getYRot(), 0);
        this.setHidden(true);
    }

    private void leaveBurrow() {
        if (!this.inBurrow) return;
        this.inBurrow = false;
        this.noPhysics = false;
        this.setNoGravity(false);
        if (this.burrow != null) this.moveTo(this.burrow.getX() + 0.5, this.burrow.getY() + 1.0, this.burrow.getZ() + 0.5, this.getYRot(), 0);
        this.setHidden(false);
        this.ambushCooldown = 200;
    }

    @Nullable
    private LivingEntity ambushPrey() {
        return this.nearest(LivingEntity.class, 3.0, e -> e.isAlive() && !(e instanceof Arachne) && (e instanceof Player p ? this.isHostileTo(p)
                : this.isCalm() ? e instanceof Monster : e instanceof Animal || e instanceof Monster));
    }

    @Override
    public boolean isPushable() {
        return !this.inBurrow && super.isPushable();
    }

    // --- the orb weaver's hunting web ---------------------------------------------------------------------------

    /** A wild orb weaver strings cobwebs across the paths around the spot she settled on, then waits for her catch. */
    private void spinTrap(ServerLevel level) {
        if (this.trapCentre == null) {
            if (!this.onGround() || this.getTarget() != null) return;
            this.trapCentre = this.blockPosition().immutable();
        }
        this.trapWebs.removeIf(w -> !level.getBlockState(w).is(Blocks.COBWEB));
        if (this.trapWebs.size() >= 8 || this.getTarget() != null) return;
        for (int i = 0; i < 12; i++) {
            BlockPos w = this.trapCentre.offset(this.random.nextInt(7) - 3, this.random.nextInt(2), this.random.nextInt(7) - 3);
            if (w.closerThan(this.trapCentre, 1.5) || !level.getBlockState(w).isAir()) continue;
            BlockState under = level.getBlockState(w.below());
            if (!under.isFaceSturdy(level, w.below(), Direction.UP) && !under.is(Blocks.COBWEB)) continue;
            level.setBlock(w, Blocks.COBWEB.defaultBlockState(), 3);
            this.trapWebs.add(w.immutable());
            this.startTimedAction(Action.SPIN, 40);
            return;                                                         // one strand at a time
        }
    }

    /** Whether something is stuck in one of her hunting webs right now. */
    public boolean isCaughtInTrap(LivingEntity e) {
        if (this.trapWebs.isEmpty()) return false;
        BlockPos p = e.blockPosition();
        return this.trapWebs.contains(p) || this.trapWebs.contains(p.above());
    }

    public List<BlockPos> getTrapWebs() {
        return this.trapWebs;
    }

    private void clearTrap(ServerLevel level) {
        for (BlockPos w : this.trapWebs) if (level.getBlockState(w).is(Blocks.COBWEB)) level.removeBlock(w, false);
        this.trapWebs.clear();
        this.trapCentre = null;
    }

    /**
     * At night a bonded orb weaver spins a web in the open air near her player's bed: phantoms, bats and vexes are
     * caught in it (vanilla cobweb). It fades at dawn.
     */
    private void phantomNet(ServerLevel level) {
        if (level.isDay() || !this.webs.isEmpty()) return;
        if (!(this.bondedPlayer(BED_AFFECTION, 32) instanceof ServerPlayer p)) return;
        BlockPos bed = p.getRespawnPosition() != null && p.getRespawnDimension() == level.dimension() ? p.getRespawnPosition() : p.blockPosition();
        if (bed.distSqr(this.blockPosition()) > 32 * 32) return;
        for (int i = 0; i < 40 && this.webs.size() < 6; i++) {
            BlockPos w = bed.offset(this.random.nextInt(9) - 4, 2 + this.random.nextInt(3), this.random.nextInt(9) - 4);
            if (level.getBlockState(w).isAir() && level.canSeeSky(w)) {
                level.setBlock(w, Blocks.COBWEB.defaultBlockState(), 3);
                this.webs.add(w.immutable());
            }
        }
        this.websExpire = level.getGameTime() + 12000;
        if (!this.webs.isEmpty()) this.startTimedAction(Action.SPIN, 60);
    }

    private void clearWebs(ServerLevel level) {
        for (BlockPos w : this.webs) if (level.getBlockState(w).is(Blocks.COBWEB)) level.removeBlock(w, false);
        this.webs.clear();
    }

    public List<BlockPos> getWebs() {
        return this.webs;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel server && reason.shouldDestroy()) {
            this.clearWebs(server);
            this.clearTrap(server);
        }
        super.remove(reason);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Silk", this.silkTicks);
        tag.putLong("NextMoult", this.nextMoult);
        tag.putInt("Carry", this.carryTicks);
        net.minecraft.nbt.ListTag w = new net.minecraft.nbt.ListTag();
        for (BlockPos p : this.webs) w.add(LongTag.valueOf(p.asLong()));
        tag.put("Webs", w);
        tag.putLong("WebsExpire", this.websExpire);
        net.minecraft.nbt.ListTag tw = new net.minecraft.nbt.ListTag();
        for (BlockPos p : this.trapWebs) tw.add(LongTag.valueOf(p.asLong()));
        tag.put("TrapWebs", tw);
        if (this.trapCentre != null) tag.putLong("TrapCentre", this.trapCentre.asLong());
        if (this.burrow != null) tag.putLong("Burrow", this.burrow.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.silkTicks = tag.getInt("Silk");
        this.nextMoult = tag.getLong("NextMoult");
        this.carryTicks = tag.getInt("Carry");
        this.webs.clear();
        for (Tag t : tag.getList("Webs", Tag.TAG_LONG)) this.webs.add(BlockPos.of(((LongTag) t).getAsLong()));
        this.websExpire = tag.getLong("WebsExpire");
        this.trapWebs.clear();
        for (Tag t : tag.getList("TrapWebs", Tag.TAG_LONG)) this.trapWebs.add(BlockPos.of(((LongTag) t).getAsLong()));
        this.trapCentre = tag.contains("TrapCentre") ? BlockPos.of(tag.getLong("TrapCentre")) : null;
        this.burrow = tag.contains("Burrow") ? BlockPos.of(tag.getLong("Burrow")) : null;
    }

    /** Devoted (60+): an arachne within 32 blocks extends the spider truce to her player. */
    public static boolean truceFor(Player player) {
        for (Arachne a : player.level().getEntitiesOfClass(Arachne.class, new AABB(player.blockPosition()).inflate(32))) {
            if (a.getAffection(player) >= 60) return true;
        }
        return false;
    }
}
