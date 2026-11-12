package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GirlEggBlock;
import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.entity.JukeboxSensing;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import com.linkyoshi90.harmontide.entity.ai.AmphibiousMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The serpent family (lamia_field_guide.html): python, king cobra, sea snake, rattlesnake, boa. Cold blood, warm
 * heart: her warmth (sun, campfires, lava) decides how quick she is, and in the cold she brumates. She holds enemies
 * in her coils, tastes the air for hidden mobs, can freeze a monster with her gaze, swallows eggs whole, sheds her
 * skin every few days, and bonds with whoever shares a fire with her. Python and cobra lay eggs; the others give
 * birth to live young. The naga guards treasure; the wadjet walks on two legs, blesses everyone who passes and hunts
 * every monster in sight. Meet a stranger medusa's eyes and you are slowed nearly to a stop, and so are the monsters she
 * looks at; once she has a friend she wears a blindfold ({@link #FLAG_BLINDFOLD}) and her gaze is harmless.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = com.linkyoshi90.harmontide.HarmontideMod.MODID)
public class Lamia extends AbstractMonsterGirl {
    private static final ResourceLocation COLD = HarmontideMod.id("lamia_cold");
    public static final int WARM = 60, COOL = 30, COLDEST = 12;

    private int warmth = 70;
    private int digest;
    private long nextShed;
    private long nextGaze;
    private long nextSpit;
    private long nextHeatSight;
    private int warning;
    @Nullable
    private UUID warned;
    @Nullable
    private BlockPos clutch;
    private final Map<UUID, Long> fireBond = new HashMap<>();
    @Nullable
    private BlockPos treasure;
    private long nextTreasureSearch;

    public Lamia(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.SEA_SNAKE_LAMIA || species == GirlSpecies.SEA_SERPENT_LAMIA || species == GirlSpecies.BOA_LAMIA) {
            this.navigation = this.createNavigation(level);       // the first one was made before species was set
            this.navigation.setCanFloat(true);
        }
        if (species == GirlSpecies.SEA_SNAKE_LAMIA || species == GirlSpecies.SEA_SERPENT_LAMIA) {
            this.setPathfindingMalus(PathType.WATER, 0.0F);
            this.moveControl = new AmphibiousMoveControl(this);
            this.goalSelector.removeAllGoals(g -> g instanceof FloatGoal);
            this.goalSelector.addGoal(7, new RandomSwimmingGoal(this, 1.0, 20));
        }
        this.goalSelector.addGoal(1, new BrumateGoal());
        if (species == GirlSpecies.PYTHON_LAMIA) this.goalSelector.addGoal(2, new BroodGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Lamia.this.startTimedAction(Action.COIL, 30);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(6, new BaskGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, true, r -> this.bestAffection() < 60));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, true, c -> this.bestAffection() < 10));
        if (species == GirlSpecies.RATTLESNAKE_LAMIA) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, p -> p.getUUID().equals(this.warned) && this.warning >= 3));
        }
        if (species == GirlSpecies.WADJET_LAMIA) {                      // merciless to monsters, any she sees
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, false));
        }
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case ANACONDA_LAMIA -> girlAttributes(60, 0.2, 7);
            case SEA_SERPENT_LAMIA -> girlAttributes(50, 0.26, 7);
            case PYTHON_LAMIA -> girlAttributes(32, 0.22, 5);
            case COBRA_LAMIA -> girlAttributes(24, 0.26, 4);
            case NAGA_LAMIA -> girlAttributes(32, 0.26, 5);
            case WADJET_LAMIA -> girlAttributes(30, 0.3, 6);
            case MEDUSA_LAMIA -> girlAttributes(30, 0.26, 4);
            case TSUCHINOKO_LAMIA -> girlAttributes(18, 0.24, 2);
            case CARAVIPER_LAMIA -> girlAttributes(24, 0.28, 4);
            case BOA_LAMIA -> girlAttributes(28, 0.24, 4);
            default -> girlAttributes(22, 0.26, 3);
        };
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (this.species == GirlSpecies.SEA_SNAKE_LAMIA || this.species == GirlSpecies.SEA_SERPENT_LAMIA) return new com.linkyoshi90.harmontide.entity.ai.SwimmerNavigation(this, level);
        if (this.species == GirlSpecies.BOA_LAMIA) return new WallClimberNavigation(this, level);
        return super.createNavigation(level);
    }

    @Override
    public boolean onClimbable() {
        return ((this.species == GirlSpecies.BOA_LAMIA || this.species == GirlSpecies.CENTIPEDE_LAMIA) && this.horizontalCollision) || super.onClimbable();
    }

    @Override
    public void travel(Vec3 input) {
        if ((this.species == GirlSpecies.SEA_SNAKE_LAMIA || this.species == GirlSpecies.SEA_SERPENT_LAMIA) && this.isControlledByLocalInstance() && this.isInWater()) {
            double y = this.getY();
            this.moveRelative(this.getSpeed() * 1.4F, input);       // a paddle tail: fast in the water
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            Vec3 v = this.getDeltaMovement().scale(0.9);
            if (this.horizontalCollision && this.isFree(v.x, v.y + 0.6 - this.getY() + y, v.z)) v = new Vec3(v.x, 0.3, v.z); // up onto the bank
            this.setDeltaMovement(v);
            return;
        }
        super.travel(input);
    }

    // --- warmth --------------------------------------------------------------------------------------------------

    public int getWarmth() {
        return this.warmth;
    }

    public void setWarmth(int w) {
        this.warmth = Math.max(0, Math.min(100, w));
    }

    public boolean isBrumating() {
        return this.warmth <= COLDEST && this.isColdPlace();
    }

    private boolean isColdPlace() {
        BlockPos p = this.blockPosition();
        return this.level().getBiome(p).value().coldEnoughToSnow(p) || this.level().isRaining() && !this.level().isDay();
    }

    private boolean nearHeat() {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-3, -1, -3), o.offset(3, 2, 3))) {
            BlockState s = this.level().getBlockState(p);
            if (s.is(Blocks.LAVA) || s.is(Blocks.MAGMA_BLOCK) || s.is(BlockTags.FIRE) || (s.getBlock() instanceof CampfireBlock && s.getValue(CampfireBlock.LIT))) return true;
        }
        return false;
    }

    private boolean nearLitCampfire(BlockPos o) {
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -1, -4), o.offset(4, 1, 4))) {
            BlockState s = this.level().getBlockState(p);
            if (s.getBlock() instanceof CampfireBlock && s.getValue(CampfireBlock.LIT)) return true;
        }
        return false;
    }

    private void tickWarmth(ServerLevel level) {
        if (this.tickCount % 20 != 0) return;
        BlockPos p = this.blockPosition();
        boolean sun = level.isDay() && !level.isRaining() && level.canSeeSky(p);
        if (this.nearHeat()) this.warmth += 3;
        else if (sun) this.warmth += 1;
        else if (this.tickCount % 40 == 0) this.warmth -= this.isColdPlace() ? 2 : 1;
        this.setWarmth(this.warmth);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            boolean cold = this.warmth < COOL;
            if (cold && !speed.hasModifier(COLD)) speed.addTransientModifier(new AttributeModifier(COLD, -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            if (!cold && speed.hasModifier(COLD)) speed.removeModifier(COLD);
        }
    }

    // --- food, presents, young -----------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.TSUCHINOKO_LAMIA && stack.is(Items.HONEY_BOTTLE)) return true;   // she loves a drink
        return stack.is(Items.EGG) || stack.is(Items.RABBIT) || stack.is(Items.CHICKEN) || stack.is(ItemTags.FISHES) || stack.is(Items.TURTLE_EGG);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.HONEY_BOTTLE) && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        int v = stack.is(Items.EGG) || stack.is(Items.RABBIT) || stack.is(Items.HONEY_BOTTLE) ? 2 : super.foodValue(player, stack);
        return this.species == GirlSpecies.BOA_LAMIA ? v * 2 : v;       // the boa bonds fastest
    }

    @Override
    protected int petValue(Player player) {
        return this.species == GirlSpecies.BOA_LAMIA ? 2 : 1;
    }

    /** She swallows eggs she finds on the ground whole, and lies digesting for a while (a round middle). */
    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return this.digest <= 0 && (stack.is(Items.EGG) || stack.is(Items.TURTLE_EGG) || stack.is(com.linkyoshi90.harmontide.registry.ModTags.LAID_EGGS));
    }

    @Override
    protected void pickUpItem(ItemEntity item) {
        if (!this.wantsToPickUp(item.getItem())) return;
        this.onItemPickup(item);
        this.take(item, 1);
        item.getItem().shrink(1);
        if (item.getItem().isEmpty()) item.discard();
        this.digest = 2400;
        this.heal(4.0F);
        this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 0.7F);
    }

    @Override
    protected float bellyProgress() {
        return Math.max(super.bellyProgress(), this.digest > 0 ? 0.6F : 0.0F);
    }

    // --- the tsuchinoko ----------------------------------------------------------------------------------------------

    /** Whether the ground two blocks ahead of where she is going lies lower than she stands: a slope to roll down. */
    public boolean downhillAhead(ServerLevel level) {
        net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
        if (v.horizontalDistanceSqr() < 0.0025) return false;
        BlockPos a = BlockPos.containing(this.position().add(v.multiply(1, 0, 1).normalize().scale(2.0)));
        BlockPos under = a.below();                                     // nothing to stand on ahead at her own level: it drops away
        return level.getBlockState(a).getCollisionShape(level, a).isEmpty() && level.getBlockState(under).getCollisionShape(level, under).isEmpty();
    }

    /** Downhill she curls up and rolls, faster than a player can run. */
    private void tickRoll(ServerLevel level) {
        boolean roll = !this.isBaby() && (this.onGround() || this.getDeltaMovement().y < 0) && this.downhillAhead(level);
        if (roll) {
            if (this.getAction() != Action.ROLL) this.setAction(Action.ROLL);
            net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
            double h = Math.sqrt(v.horizontalDistanceSqr());
            if (h < 0.42) this.setDeltaMovement(v.x * 1.3, v.y, v.z * 1.3);
        } else if (this.getAction() == Action.ROLL) {
            this.setAction(Action.NONE);
        }
    }

    /** The spitting cobra: venom in the eyes of a monster near her friend, blinding and poisoning it. The monster, or null. */
    @Nullable
    public net.minecraft.world.entity.monster.Monster spitVenom(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null || this.isBaby()) return null;
        net.minecraft.world.entity.monster.Monster m = this.nearest(net.minecraft.world.entity.monster.Monster.class, 10, x -> x.distanceToSqr(friend) < 10 * 10 && this.hasLineOfSight(x) && !x.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS));
        if (m == null) return null;
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0), this);
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 0), this);
        net.minecraft.world.phys.Vec3 from = this.getEyePosition(), to = m.getEyePosition();
        for (int i = 1; i <= 6; i++) {
            net.minecraft.world.phys.Vec3 at = from.lerp(to, i / 6.0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPIT, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        }
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.LLAMA_SPIT, 1.0F, 1.6F);
        return m;
    }

    @Override
    public ItemStack rollGift(Player player) {
        if (this.level().getGameTime() >= this.nextShed - 2400) return new ItemStack(ModItems.SHED_SKIN.get());
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case CARAVIPER_LAMIA -> new ItemStack(r < 40 ? Items.EGG : r < 75 ? Items.RABBIT_HIDE : r < 97 ? Items.GOLD_NUGGET : Items.EMERALD, r < 97 ? 3 : 1);
            case TSUCHINOKO_LAMIA -> new ItemStack(r < 50 ? Items.GOLD_NUGGET : r < 80 ? Items.GOLDEN_CARROT : r < 97 ? Items.GOLD_INGOT : Items.GOLDEN_APPLE,
                    r < 50 ? 5 : 1);
            case NAGA_LAMIA -> new ItemStack(r < 50 ? Items.GOLD_NUGGET : r < 90 ? Items.EMERALD : r < 98 ? Items.GOLD_INGOT : Items.DIAMOND, r < 50 ? 4 : 1);
            case WADJET_LAMIA -> r < 70 ? new ItemStack(Items.EMERALD, 1 + r % 2) : new ItemStack(r < 90 ? Items.GOLD_NUGGET : Items.LAPIS_LAZULI, 3);
            case SEA_SNAKE_LAMIA -> new ItemStack(r < 50 ? Items.COD : r < 80 ? Items.PRISMARINE_SHARD : Items.NAUTILUS_SHELL);
            case RATTLESNAKE_LAMIA -> new ItemStack(r < 50 ? Items.RABBIT_HIDE : r < 85 ? Items.CACTUS : Items.GOLD_NUGGET);
            default -> new ItemStack(r < 40 ? Items.EGG : r < 70 ? Items.RABBIT : r < 90 ? Items.FEATHER : Items.EMERALD);
        };
    }

    private boolean laysEggs() {
        return this.species == GirlSpecies.PYTHON_LAMIA || this.species == GirlSpecies.COBRA_LAMIA;
    }

    @Override
    protected int litterSize() {
        return this.laysEggs() ? 3 + this.random.nextInt(3) : 2 + this.random.nextInt(3);
    }

    /** Egg-layers lay a leathery clutch in warm ground (the cobra in a heap of leaves); the rest give birth here. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        if (!this.laysEggs()) return super.giveBirth(level, father);
        BlockPos o = this.blockPosition();
        BlockPos spot = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -2, -4), o.offset(4, 2, 4))) {
            if (canHoldBlock(level, p)) {
                spot = p.immutable();
                if (level.getBlockState(p.below()).is(BlockTags.SAND) || level.getBlockState(p.below()).is(BlockTags.DIRT)) break;
            }
        }
        if (spot == null) return super.giveBirth(level, father);
        level.setBlock(spot, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.LEATHERY), 3);
        if (level.getBlockEntity(spot) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father).withPaint(this.getPaint());
        if (this.species == GirlSpecies.COBRA_LAMIA) {                // the only snake that builds a nest: leaves heaped around it
            for (BlockPos p : new BlockPos[]{spot.north(), spot.south(), spot.east(), spot.west()}) {
                if (canHoldBlock(level, p)) level.setBlock(p, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), 3);
            }
        }
        this.clutch = spot;
        return true;
    }

    /** A python coiled around her clutch (it hatches twice as fast). */
    public boolean isBrooding() {
        return this.clutch != null && this.distanceToSqr(Vec3.atCenterOf(this.clutch)) < 6.0 && this.getAction() == Action.COIL;
    }

    @Override
    public void onNightShared(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
        player.setTicksFrozen(0);
        if (this.species == GirlSpecies.PYTHON_LAMIA) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.RATTLESNAKE_LAMIA && this.warning > 0 ? SoundEvents.ARMOR_EQUIP_CHAIN.value() : SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HISS;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** Her coils hold what she catches (it can barely move); the rattlesnake and cobra poison it too. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l) {
            l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3));
            if (this.species == GirlSpecies.RATTLESNAKE_LAMIA || this.species == GirlSpecies.COBRA_LAMIA || this.species == GirlSpecies.WADJET_LAMIA) {
                l.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            }
        }
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    private long fullUntil;

    /** The anaconda swallows a small monster (6 health or less, or small enough) near her friend whole, then wants a long rest. 1 on a gulp, 0 with none, -1 without a friend, -2 while full. */
    public int gulp(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        if (level.getGameTime() < this.fullUntil) return -2;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, this.getBoundingBox().inflate(4), net.minecraft.world.entity.monster.Monster::isAlive)) {
            if (m.getBbHeight() > 1.0F && m.getHealth() > 6) continue;
            m.hurt(this.damageSources().mobAttack(this), 1000);
            this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 1.0F, 0.6F);
            this.fullUntil = level.getGameTime() + 2400;
            this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 1200, 1));
            return 1;
        }
        return 0;
    }

    /** Music near the anaconda gets into everyone: her friend moves faster and hits harder for as long as it plays. True if it did. */
    public boolean groove(boolean music) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null || !music) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, 0, true, true));
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, 0, true, true));
        return true;
    }

    /** The sea serpent: in the rain, out under the sky, lightning finds a monster near her friend. 1 on a strike, 0 if not now, -1 without a friend. */
    public int stormCall(net.minecraft.server.level.ServerLevel level, boolean raining) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (!raining) return 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(16), net.minecraft.world.entity.monster.Monster::isAlive)) {
            net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) return 0;
            bolt.moveTo(m.position());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
            m.hurt(this.damageSources().lightningBolt(), 10);
            return 1;
        }
        return 0;
    }

    /** Her bite on the monster nearest her friend: Weakness, Slowness and Poison. 1 on a bite, 0 with none, -1 without a friend. */
    public int forcipules(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8), net.minecraft.world.entity.monster.Monster::isAlive)) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 1));
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 0));
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            return 1;
        }
        return 0;
    }

    /** Echidna hushes the creepers within 10 of her friend: they stop swelling and lose their target. How many, or -1 without a friend. */
    public int hush(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int hushed = 0;
        for (net.minecraft.world.entity.monster.Creeper c : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class, friend.getBoundingBox().inflate(10), net.minecraft.world.entity.monster.Creeper::isAlive)) {
            c.setSwellDir(-1);
            c.setTarget(null);
            hushed++;
        }
        return hushed;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.ECHIDNA_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel den && !this.isBaby() && this.tickCount % 5 == 0) this.hush(den);
        if (this.species == GirlSpecies.CENTIPEDE_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel burrow && !this.isBaby() && this.tickCount % 61 == 0) this.forcipules(burrow);
        if (this.species == GirlSpecies.ANACONDA_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel coils && !this.isBaby()) {
            if (this.tickCount % 41 == 0) this.gulp(coils);
            if (this.tickCount % 40 == 0) this.groove(com.linkyoshi90.harmontide.entity.JukeboxSensing.findPlaying(coils, this.blockPosition(), 16) != null);
        }
        if (this.species == GirlSpecies.SEA_SERPENT_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel storm && !this.isBaby() && this.tickCount % 201 == 0) this.stormCall(storm, storm.isRaining());
        if (this.species == GirlSpecies.TITANOBOA_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel marsh && !this.isBaby() && this.tickCount % 100 == 0) this.constrict(marsh);
        if (this.species == GirlSpecies.SERPOPARD_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel fang && !this.isBaby() && this.tickCount % 60 == 0) this.venomBite(fang);
        if (this.species == GirlSpecies.SANDWORM_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel dune && !this.isBaby() && this.tickCount % 200 == 0) this.swallow(dune);
        if (this.species == GirlSpecies.XIANGLIU_LAMIA && this.level() instanceof net.minecraft.server.level.ServerLevel marsh && !this.isBaby() && this.tickCount % 60 == 0) this.nineHeads(marsh);
        if (this.species == GirlSpecies.TURQUOISE_SERPENT_LAMIA && this.level() instanceof ServerLevel ward && !this.isBaby() && this.tickCount % 20 == 0) this.scaleWard(ward);
        if (this.species == GirlSpecies.HOOP_SNAKE_LAMIA && this.level() instanceof ServerLevel hills && !this.isBaby() && this.tickCount % 100 == 0) this.sting(hills);
        if (!(this.level() instanceof ServerLevel server)) return;
        this.tickWarmth(server);
        if (this.digest > 0) this.digest--;
        long now = server.getGameTime();
        if (this.nextShed == 0) this.nextShed = now + 3 * 24000 + this.random.nextInt(2 * 24000);
        if (now >= this.nextShed) {                                    // she sheds; a favourite gets it straight away
            this.nextShed = now + 3 * 24000 + this.random.nextInt(2 * 24000);
            Player p = this.bondedPlayer(BED_AFFECTION, 16);
            ItemStack skin = new ItemStack(ModItems.SHED_SKIN.get());
            if (p == null || !p.getInventory().add(skin)) this.spawnAtLocation(skin);
        }
        if (this.tickCount % 41 == 0) this.senses(server);
        if (this.tickCount % 20 == 0) this.campfireBond(server);
        switch (this.species) {
            case COBRA_LAMIA -> this.hoodDisplay(server);
            case CARAVIPER_LAMIA -> {
                this.hoodDisplay(server);
                if (this.tickCount % 40 == 0) this.spitVenom(server);
            }
            case RATTLESNAKE_LAMIA -> this.rattle(server);
            case SEA_SNAKE_LAMIA -> this.reefGuide(server);
            case WADJET_LAMIA -> {
                if (this.tickCount % 40 == 0) this.bless(server);
            }
            case MEDUSA_LAMIA -> {
                if (this.tickCount % 10 == 0) this.petrify(server);
            }
            case TSUCHINOKO_LAMIA -> this.tickRoll(server);
            default -> {
            }
        }
    }

    // --- the medusa ----------------------------------------------------------------------------------------------

    private long nextStoneMessage;

    /** Whether this player is looking her in the eyes (the enderman's test) with nothing in between. */
    public boolean meetsHerEyes(Player p) {
        Vec3 view = p.getViewVector(1.0F).normalize();
        Vec3 toEyes = new Vec3(this.getX() - p.getX(), this.getEyeY() - p.getEyeY(), this.getZ() - p.getZ());
        double d = toEyes.length();
        return view.dot(toEyes.normalize()) > 1.0 - 0.025 / d && p.hasLineOfSight(this);
    }

    /** Her gaze: strangers who meet her eyes and the monsters she sees all but stop (Slowness VI). Blindfolded, she is harmless. */
    public void petrify(ServerLevel level) {
        this.setFlag(FLAG_BLINDFOLD, this.bestAffection() >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION);
        if (this.hasFlag(FLAG_BLINDFOLD) || this.isBaby()) return;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16), p -> !p.isSpectator() && !p.isCreative()
                && this.getAffection(p) < com.linkyoshi90.harmontide.entity.Orders.AFFECTION && this.meetsHerEyes(p))) {
            stone(level, p);
            if (level.getGameTime() >= this.nextStoneMessage) {
                this.nextStoneMessage = level.getGameTime() + 200;
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.petrified", this.getDisplayName()), true);
            }
        }
        for (Monster m : level.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(10), m -> m.isAlive() && this.hasLineOfSight(m))) {
            stone(level, m);
        }
    }

    private static void stone(ServerLevel level, LivingEntity e) {
        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 5));
        level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(), 4, 0.3, 0.4, 0.3, 0.0);
    }

    // --- the wadjet ----------------------------------------------------------------------------------------------

    private static final ResourceLocation NEKHBET = HarmontideMod.id("nekhbet");

    /** Named Nekhbet, after her sister the vulture goddess: a stronger blessing and a harder bite. */
    public boolean isNekhbet() {
        return this.hasCustomName() && "Nekhbet".equalsIgnoreCase(this.getCustomName().getString());
    }

    /**
     * Every player within 8 blocks, and the girls (any family) fond of them close by, get Regeneration and Resistance
     * (level II from Nekhbet). Not from an injured or young one.
     */
    public void bless(ServerLevel level) {
        boolean nekhbet = this.isNekhbet();
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && nekhbet != damage.hasModifier(NEKHBET)) {
            if (nekhbet) damage.addTransientModifier(new AttributeModifier(NEKHBET, 4.0, AttributeModifier.Operation.ADD_VALUE));
            else damage.removeModifier(NEKHBET);
        }
        if (this.isBaby() || this.isInjured()) return;
        int amp = nekhbet ? 1 : 0;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8), p -> !p.isSpectator())) {
            blessOne(p, amp);
            for (LivingEntity girl : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8),
                    e -> e != this && e instanceof com.linkyoshi90.harmontide.entity.Bonded b && b.getAffection(p) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION)) {
                blessOne(girl, amp);
            }
        }
    }

    private static void blessOne(LivingEntity e, int amp) {
        e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, amp, true, true));
        e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, amp, true, true));
    }

    /** Tongue sense (invisible mobs glow for her favourite), heat sight at night (rattlesnake), and the hypnotic gaze. */
    private void senses(ServerLevel level) {
        Player p = this.bondedPlayer(BED_AFFECTION, 16);
        if (p == null) return;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16), e -> e.isInvisible() && e != p && e != this)) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
        }
        if (this.species == GirlSpecies.RATTLESNAKE_LAMIA && !level.isDay() && level.getGameTime() >= this.nextHeatSight) {
            this.nextHeatSight = level.getGameTime() + 60;
            for (Mob m : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(20), m -> m != this && !(m instanceof AbstractMonsterGirl))) {
                m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            }
        }
        if (level.getGameTime() >= this.nextGaze) {
            Monster threat = this.nearest(Monster.class, 8, m -> m.getTarget() == p);
            if (threat != null) {
                this.nextGaze = level.getGameTime() + 200;
                this.getLookControl().setLookAt(threat);
                threat.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
                level.sendParticles(ParticleTypes.ENCHANT, threat.getX(), threat.getEyeY(), threat.getZ(), 15, 0.3, 0.3, 0.3, 0.5);
            }
        }
    }

    /** Sharing a lit campfire at night with her builds trust, up to fond. */
    private void campfireBond(ServerLevel level) {
        if (level.isDay() || !this.nearLitCampfire(this.blockPosition())) return;
        long now = level.getGameTime();
        for (Player p : level.players()) {
            if (p.distanceToSqr(this) > 5 * 5 || this.getAffection(p) >= 10 || now < this.fireBond.getOrDefault(p.getUUID(), 0L)) continue;
            this.fireBond.put(p.getUUID(), now + 1200);
            this.addAffection(p, 1);
            this.hearts(level, 1);
        }
    }

    /** The king cobra rears and spreads her hood at strangers who come close; too close, and she spits. Music calms her. */
    private void hoodDisplay(ServerLevel level) {
        if (this.tickCount % 10 != 0 || this.isOrderedToSit()) return;
        if (JukeboxSensing.findPlaying(level, this.blockPosition(), 16) != null) {
            if (this.getAction() == Action.REAR) this.setAction(Action.NONE);
            if (this.bestAffection() >= BED_AFFECTION && this.getAction() == Action.NONE) this.startTimedAction(Action.BASK, 40);
            return;
        }
        Player stranger = this.nearest(Player.class, 5, p -> !p.isSpectator() && !p.isCreative() && this.getAffection(p) < 10);
        if (stranger == null) {
            if (this.getAction() == Action.REAR) this.setAction(Action.NONE);
            return;
        }
        this.getLookControl().setLookAt(stranger);
        if (this.getAction() != Action.REAR) {
            this.setAction(Action.REAR);
            this.playSound(SoundEvents.CAT_HISS, 1.0F, 0.6F);
        } else if (stranger.distanceToSqr(this) < 3.5 * 3.5 && level.getGameTime() >= this.nextSpit) {
            this.nextSpit = level.getGameTime() + 100;
            this.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 1.2F);
            stranger.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            stranger.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
            level.sendParticles(ParticleTypes.SPIT, stranger.getX(), stranger.getEyeY(), stranger.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
        }
    }

    /** Fair warning: a rattle, a louder rattle and coiling, and only then a strike. Her favourites never hear it. */
    private void rattle(ServerLevel level) {
        if (this.tickCount % 20 != 0 || this.isOrderedToSit()) return;
        Player stranger = this.nearest(Player.class, 6, p -> !p.isSpectator() && !p.isCreative() && this.getAffection(p) < 10);
        if (stranger == null) {
            this.warning = 0;
            this.warned = null;
            if (this.getAction() == Action.COIL || this.getAction() == Action.HISS) this.setAction(Action.NONE);
            return;
        }
        if (!stranger.getUUID().equals(this.warned)) {
            this.warned = stranger.getUUID();
            this.warning = 0;
        }
        double d = Math.sqrt(stranger.distanceToSqr(this));
        int stage = d < 3 ? 3 : d < 4.5 ? 2 : 1;
        if (stage > this.warning && this.tickCount % 40 == 0 || this.warning == 0) this.warning = Math.min(stage, this.warning + 1);
        this.getLookControl().setLookAt(stranger);
        this.setAction(this.warning >= 2 ? Action.COIL : Action.HISS);
        this.playSound(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 0.6F + 0.3F * this.warning, 1.6F + 0.2F * this.warning);
    }

    public int getWarning() {
        return this.warning;
    }

    /** The sea snake leads a swimming favourite towards the nearest shipwreck or ocean ruin, and shares her breath. */
    private void reefGuide(ServerLevel level) {
        if (this.tickCount % 40 != 0) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 16);
        if (p == null || !(p.isInWater() || p.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat)) return;
        if (p.isInWater() && p.distanceToSqr(this) < 8 * 8) p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false));
        if (level.getGameTime() >= this.nextTreasureSearch) {
            this.nextTreasureSearch = level.getGameTime() + 600;
            this.treasure = level.findNearestMapStructure(StructureTags.DOLPHIN_LOCATED, this.blockPosition(), 50, false);
        }
        if (this.treasure != null && this.getNavigation().isDone() && this.isInWater()) {
            Vec3 dir = Vec3.atCenterOf(this.treasure).subtract(this.position()).normalize().scale(14);
            Vec3 t = this.position().add(dir);
            this.getNavigation().moveTo(t.x, this.getY(), t.z, 1.2);
            level.sendParticles(ParticleTypes.DOLPHIN, this.getX(), this.getY() + 0.5, this.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        }
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Warmth", this.warmth);
        tag.putInt("Digest", this.digest);
        tag.putLong("NextShed", this.nextShed);
        if (this.clutch != null) tag.putLong("Clutch", this.clutch.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Warmth")) this.warmth = tag.getInt("Warmth");
        this.digest = tag.getInt("Digest");
        this.nextShed = tag.getLong("NextShed");
        this.clutch = tag.contains("Clutch") ? BlockPos.of(tag.getLong("Clutch")) : null;
    }

    // --- goals ---------------------------------------------------------------------------------------------------

    /** Too cold: she curls up and sleeps (brumation) until she warms up. */
    class BrumateGoal extends Goal {
        BrumateGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return Lamia.this.isBrumating() && Lamia.this.getTarget() == null;
        }

        @Override
        public void start() {
            Lamia.this.getNavigation().stop();
            Lamia.this.setAction(Action.SITTING);
        }

        @Override
        public void stop() {
            if (Lamia.this.getAction() == Action.SITTING && !Lamia.this.isOrderedToSit()) Lamia.this.setAction(Action.NONE);
        }
    }

    /** By day, when she is cool, she finds a sunny spot (or a fire) and basks. */
    class BaskGoal extends Goal {
        private int ticks;
        private Vec3 spot;

        BaskGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (Lamia.this.tickCount % 37 != 0 || Lamia.this.warmth >= WARM || Lamia.this.getTarget() != null) return false;
            if (!Lamia.this.level().isDay() || Lamia.this.level().isRaining()) return false;
            BlockPos o = Lamia.this.blockPosition();
            for (int i = 0; i < 10; i++) {
                BlockPos p = o.offset(Lamia.this.random.nextInt(13) - 6, 0, Lamia.this.random.nextInt(13) - 6);
                p = Lamia.this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p);
                if (Lamia.this.level().canSeeSky(p)) {
                    this.spot = Vec3.atBottomCenterOf(p);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < 600 && Lamia.this.warmth < 95 && Lamia.this.getTarget() == null && Lamia.this.level().isDay();
        }

        @Override
        public void tick() {
            this.ticks++;
            if (Lamia.this.distanceToSqr(this.spot) > 2.0 && this.ticks < 200) {
                if (this.ticks % 20 == 1) Lamia.this.getNavigation().moveTo(this.spot.x, this.spot.y, this.spot.z, 0.9);
                return;
            }
            Lamia.this.getNavigation().stop();
            if (Lamia.this.getAction() == Action.NONE) Lamia.this.setAction(Action.BASK);
        }

        @Override
        public void stop() {
            if (Lamia.this.getAction() == Action.BASK) Lamia.this.setAction(Action.NONE);
        }
    }

    /** The python coils around her clutch until it hatches, shivering to keep it warm. */
    class BroodGoal extends Goal {
        BroodGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (Lamia.this.clutch == null) return false;
            if (!Lamia.this.level().getBlockState(Lamia.this.clutch).is(ModBlocks.GIRL_EGG.get())) {
                Lamia.this.clutch = null;
                return false;
            }
            return Lamia.this.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            Vec3 c = Vec3.atBottomCenterOf(Lamia.this.clutch);
            if (Lamia.this.distanceToSqr(c) > 3.0) {
                if (Lamia.this.tickCount % 20 == 0) Lamia.this.getNavigation().moveTo(c.x, c.y, c.z, 1.0);
                return;
            }
            Lamia.this.getNavigation().stop();
            Lamia.this.setAction(Action.COIL);
        }

        @Override
        public void stop() {
            if (Lamia.this.getAction() == Action.COIL) Lamia.this.setAction(Action.NONE);
        }
    }

    /** Devoted (60+): a lamia next to her player takes half of the damage they would take on her coils. */
    @Nullable
    public static Lamia guardianOf(Player player) {
        for (Lamia l : player.level().getEntitiesOfClass(Lamia.class, new AABB(player.blockPosition()).inflate(3))) {
            if (l.getAffection(player) >= 60 && l.isAlive() && !l.isBaby()) return l;
        }
        return null;
    }

    // --- riding in her coils -------------------------------------------------------------------------------------

    /** At high friendship an empty hand climbs into her coils (crouch to pat her instead). The wadjet walks on legs: no coils to ride. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (!stack.isEmpty() || player.isShiftKeyDown() || this.isBaby() || this.isVehicle() || this.getAffection(player) < BED_AFFECTION
                || this.species == GirlSpecies.WADJET_LAMIA) return null;
        if (!this.level().isClientSide) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setOrderedToSit(false);
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * Whoever she let into her coils steers. No friendship check here: the client decides who steers, and friendship is
     * not synced to it (the check is made when you climb in).
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travel) {
        float forward = player.zza <= 0.0F ? player.zza * 0.25F : player.zza;
        return new Vec3(player.xxa * 0.5F, 0.0, forward);
    }

    /** A sidewinding glide across sand, a slow slither everywhere else. */
    @Override
    protected float getRiddenSpeed(Player player) {
        return this.onSand() ? 0.36F : 0.12F;
    }

    public boolean onSand() {
        return this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.SAND);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        return new Vec3(0.0, dims.height() * 0.3, seatBack() - 0.9);      // in her coils, 0.9 behind her waist: the model takes up 0.4 of it
    }

    /** Only part of the way: further and her torso would leave her hitbox (0.8 wide). */
    @Override
    public double seatBack() {
        return 0.4;
    }

    // --- the naga guards treasure -------------------------------------------------------------------------------------

    /**
     * Whether a naga keeps this player from a treasure chest here: an unopened structure chest (it still has its loot
     * table) within 10 blocks of a naga who is not fond of the player. Player chests are never hers to guard.
     */
    @Nullable
    public static Lamia guardOf(Level level, BlockPos pos, Player player) {
        if (player.isCreative() || !(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity box)
                || box.getLootTable() == null) return null;
        for (Lamia l : level.getEntitiesOfClass(Lamia.class, new net.minecraft.world.phys.AABB(pos).inflate(10),
                l -> l.isAlive() && !l.isBaby() && l.species == GirlSpecies.NAGA_LAMIA && l.getAffection(player) < 10)) {
            return l;
        }
        return null;
    }

    /** She spreads her hood and warns the thief off. */
    public void warnOff(Player player) {
        this.startTimedAction(Action.HISS, 30);
        this.getLookControl().setLookAt(player);
        this.playSound(SoundEvents.CAT_HISS, 1.0F, 0.6F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.harmontide.naga_guard", this.getDisplayName()), true);
    }

    // --- the urchskin (eighteenth wave) -----------------------------------------------------------------------------

    /** Her spines prick a monster that hurt her friend: three points of thorns damage. True if it was pricked. */
    public boolean prick(net.minecraft.world.entity.LivingEntity attacker) {
        if (!(this.level() instanceof ServerLevel level) || attacker == this || !attacker.isAlive()) return false;
        attacker.hurt(level.damageSources().thorns(this), 3.0F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, attacker.getX(), attacker.getY(0.6), attacker.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
        return true;
    }

    /** A monster hurt a player: a befriended urchskin near them pricks it. */
    @net.neoforged.bus.api.SubscribeEvent
    public static void onFriendHurt(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player) || player.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof net.minecraft.world.entity.monster.Monster attacker)) return;
        Lamia urchskin = urchskinOf(player);
        if (urchskin != null) urchskin.prick(attacker);
    }

    /** A befriended urchskin within 8 blocks of the player, if any. */
    @Nullable
    public static Lamia urchskinOf(net.minecraft.world.entity.player.Player player) {
        for (Lamia l : player.level().getEntitiesOfClass(Lamia.class, player.getBoundingBox().inflate(8),
                l -> l.species == GirlSpecies.URCHSKIN_LAMIA && !l.isBaby() && l.getAffection(player) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION)) {
            return l;
        }
        return null;
    }

    // --- the turquoise serpent and the hoop snake (nineteenth wave) -------------------------------------------------

    /** Her friend within 10 blocks, below half health, gets Resistance I. True if she warded them. */
    public boolean scaleWard(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 10);
        if (friend == null || friend.getHealth() >= friend.getMaxHealth() / 2) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 60, 0, true, true));
        return true;
    }

    /** The hoop snake stings the nearest monster within 8 blocks of her friend: Poison I for five seconds. 1 if she stung one,
     *  0 if none was near, -1 without a friend. */
    public int sting(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(8));
        if (m == null) return 0;
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 0), this);
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.BEE_STING, 1.0F, 0.8F);
        return 1;
    }

    // --- the serpopard (twenty-first wave) --------------------------------------------------------------------------

    /** She bites the nearest monster within 6 blocks of her friend: Poison II for four seconds (the undead, who can't be poisoned,
     *  take three points of venom instead). 1 if she bit, 0 if none was near,
     *  -1 without a friend. */
    public int venomBite(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(6));
        if (m == null) return 0;
        if (m.isInvertedHealAndHarm()) m.hurt(level.damageSources().indirectMagic(this, this), 3.0F);   // the undead shrug off poison: the venom burns instead
        else m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 80, 1), this);
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.CAT_HISS, 1.0F, 0.8F);
        return 1;
    }

    // --- the twenty-second wave ---------------------------------------------------------------------------------------

    /** The sandworm swallows the nearest monster within 6 blocks of her friend and spits it out 16 blocks away from them, on the
     *  ground there. 1 if she did, 0 if none was near, -1 without a friend. */
    public int swallow(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), friend, friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(6));
        if (m == null) return 0;
        net.minecraft.world.phys.Vec3 away = new net.minecraft.world.phys.Vec3(m.getX() - friend.getX(), 0, m.getZ() - friend.getZ());
        if (away.lengthSqr() < 1.0E-4) away = new net.minecraft.world.phys.Vec3(1, 0, 0);
        away = away.normalize().scale(16);
        int x = net.minecraft.util.Mth.floor(friend.getX() + away.x), z = net.minecraft.util.Mth.floor(friend.getZ() + away.z);
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight()) y = (int) m.getY();
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, m.getX(), m.getY(0.5), m.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        m.teleportTo(x + 0.5, y, z + 0.5);
        m.setTarget(null);
        this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 1.0F, 0.6F);
        return 1;
    }

    /** Xiangliu's heads bite up to three monsters within 8 blocks of her friend at once: four points each. How many she bit, -1
     *  without a friend. */
    public int nineHeads(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        java.util.List<net.minecraft.world.entity.monster.Monster> near = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8), net.minecraft.world.entity.LivingEntity::isAlive);
        near.sort(java.util.Comparator.comparingDouble(friend::distanceToSqr));
        int n = 0;
        for (net.minecraft.world.entity.monster.Monster m : near) {
            if (n == 3) break;
            m.hurt(level.damageSources().mobAttack(this), 4.0F);
            n++;
        }
        return n;
    }

    /**
     * The titanoboa's constriction (twenty-third wave): she coils round the biggest monster within 8 of her friend and holds it fast
     * for five seconds (it cannot move: Slowness VII) while she squeezes (Wither II). 1 when she coiled round one, 0 with none near,
     * -1 without a friend.
     */
    public int constrict(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster biggest = null;
        double size = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8), net.minecraft.world.entity.LivingEntity::isAlive)) {
            net.minecraft.world.phys.AABB b = m.getBoundingBox();
            double v = b.getXsize() * b.getYsize() * b.getZsize();
            if (v > size) {
                size = v;
                biggest = m;
            }
        }
        if (biggest == null) return 0;
        biggest.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 6), this);
        biggest.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 100, 1), this);
        biggest.setDeltaMovement(0, 0, 0);
        this.getLookControl().setLookAt(biggest);
        this.playSound(net.minecraft.sounds.SoundEvents.SLIME_SQUISH, 1.0F, 0.5F);
        return 1;
    }
}
