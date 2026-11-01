package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.PaintedEggBlock;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The rabbit family (jackalope_field_guide.html): Inaba, Jack, Easter Bunny, Al-mi'raj, volcano rabbit. Skittish
 * (they bolt from sprinters and predators), big jumpers, won with carrots. At high friendship she follows her favourite
 * player and brings luck with her. Inaba comes down only under a full moon and warms up twice as fast once caught; Jack
 * charges with her antlers; the Easter Bunny hides painted eggs; the Al-mi'raj clan defends its own; the volcano
 * rabbit lives by lava, is very shy and runs out of breath. The wolpertinger jumps twice, glides down from any height on
 * her little wings, and now and then swaps a player's boots for leather ones (feed her and she gives them back).
 */
public class Jackalope extends AbstractMonsterGirl {
    public static final int FOLLOW_AFFECTION = BED_AFFECTION;
    /** How long the volcano rabbit can run before she has to stop and pant. */
    public static final int STAMINA = 100;
    private static final int MAX_EGGS = 6;

    private int stamina = STAMINA;
    private boolean caught;
    private long nextEgg;
    private long nextShed;
    private final List<BlockPos> eggs = new ArrayList<>();
    private boolean jumped;
    private boolean doubleJumped;
    private double lastDy;
    /** The wolpertinger's prize: someone's boots, and whose. */
    private ItemStack boots = ItemStack.EMPTY;
    @Nullable
    private java.util.UUID bootsOwner;
    private long nextBootPrank;

    public Jackalope(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        boolean fighter = species == GirlSpecies.JACK_JACKALOPE || species == GirlSpecies.AL_MIRAJ_JACKALOPE;
        if (!fighter) this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        // prey animals: sprinting strangers (any stranger, for the volcano rabbit) and predators send them running
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.2, 1.7, e -> e instanceof Player p && this.isAfraidOf(p)));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Wolf.class, 10.0F, 1.2, 1.7, w -> !((Wolf) w).isTame()));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Fox.class, 8.0F, 1.2, 1.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Ocelot.class, 8.0F, 1.2, 1.6));
        if (fighter) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true) {
                @Override
                protected void checkAndPerformAttack(LivingEntity target) {
                    if (this.canPerformAttack(target)) Jackalope.this.startTimedAction(Action.POUNCE, 16);
                    super.checkAndPerformAttack(target);
                }
            });
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::companion, 1.2, 7.0F, 3.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.9));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case INABA_JACKALOPE -> girlAttributes(16, 0.42, 2).add(Attributes.JUMP_STRENGTH, 0.7);
            case JACK_JACKALOPE -> girlAttributes(22, 0.33, 5).add(Attributes.JUMP_STRENGTH, 0.66).add(Attributes.ATTACK_KNOCKBACK, 1.5);
            case AL_MIRAJ_JACKALOPE -> girlAttributes(20, 0.34, 6).add(Attributes.JUMP_STRENGTH, 0.62);
            case VOLCANO_JACKALOPE -> girlAttributes(10, 0.36, 1).add(Attributes.JUMP_STRENGTH, 0.55);
            case WOLPERTINGER_JACKALOPE -> girlAttributes(16, 0.34, 1).add(Attributes.JUMP_STRENGTH, 0.7);
            case CLOCKHARE_JACKALOPE -> girlAttributes(16, 0.33, 1).add(Attributes.JUMP_STRENGTH, 0.62);
            case BOGHARE_JACKALOPE -> girlAttributes(16, 0.34, 1).add(Attributes.JUMP_STRENGTH, 0.9);
            default -> girlAttributes(16, 0.32, 1).add(Attributes.JUMP_STRENGTH, 0.62);
        };
    }

    /** A sprinting stranger frightens any jackalope; the volcano rabbit runs from anyone who is not crouching. */
    private boolean isAfraidOf(Player p) {
        if (p.isSpectator() || p.isCreative() || this.getAffection(p) >= 10 || this.isPanting()) return false;
        if (this.species == GirlSpecies.JACK_JACKALOPE || this.species == GirlSpecies.AL_MIRAJ_JACKALOPE) return false;
        return this.species == GirlSpecies.VOLCANO_JACKALOPE ? !p.isShiftKeyDown() : p.isSprinting();
    }

    /** At high friendship she tags along with her favourite player. */
    @Nullable
    private LivingEntity companion() {
        if (this.isBaby() || this.isOrderedToSit()) return null;
        return this.bondedPlayer(FOLLOW_AFFECTION, 32);
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CARROT) || stack.is(Items.GOLDEN_CARROT) || this.species == GirlSpecies.BOGHARE_JACKALOPE && stack.is(Items.LILY_PAD);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (!this.boots.isEmpty() && player.getUUID().equals(this.bootsOwner)) this.returnBoots(player);
        return stack.is(Items.GOLDEN_CARROT) ? 3 : 1;
    }

    // --- the wolpertinger -----------------------------------------------------------------------------------------

    public ItemStack getBoots() {
        return this.boots;
    }

    /** Her prank: your boots for a pair of leather ones, once a day at most, and only from someone close enough. */
    public boolean swapBoots(Player p) {
        ItemStack worn = p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        if (!this.boots.isEmpty() || worn.isEmpty() || worn.is(Items.LEATHER_BOOTS)) return false;
        this.boots = worn.copy();
        this.bootsOwner = p.getUUID();
        p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        this.startTimedAction(Action.LAUGH, 30);
        this.playSound(SoundEvents.RABBIT_JUMP, 1.0F, 1.6F);
        p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.boots_swapped", this.getDisplayName()), true);
        return true;
    }

    private void returnBoots(Player p) {
        if (!p.getInventory().add(this.boots)) p.drop(this.boots, false);
        this.boots = ItemStack.EMPTY;
        this.bootsOwner = null;
    }

    /** A second jump at the top of the first; a glide down from any height. */
    private void tickWings(ServerLevel level) {
        double dy = this.getDeltaMovement().y;
        if (this.onGround()) {
            this.jumped = false;
            this.doubleJumped = false;
        } else {
            if (dy > 0.2 && this.lastDy <= 0.0) this.jumped = true;
            if (this.jumped && !this.doubleJumped && this.lastDy > 0.0 && dy <= 0.0 && !this.getNavigation().isDone()) {   // still going somewhere
                this.doubleJumped = true;
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.45, 0));
                level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 3, 0.2, 0.0, 0.2, 0.0);
            }
            if (dy < -0.15 && !this.isInWater()) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.15, this.getDeltaMovement().z);
                this.resetFallDistance();
            }
        }
        this.lastDy = this.getDeltaMovement().y;
        if (this.tickCount % 40 == 0 && level.getGameTime() >= this.nextBootPrank && !this.isBaby() && this.random.nextInt(6) == 0) {
            Player p = this.level().getNearestPlayer(this, 2.5);
            if (p != null && !p.isCreative() && !p.isSpectator() && this.swapBoots(p)) this.nextBootPrank = level.getGameTime() + 24000;
        }
    }

    // --- the clockhare ------------------------------------------------------------------------------------------------

    /** Ticks without sleep after which phantoms come (three nights). */
    public static final int PHANTOM_TICKS = 72000;
    private int clockPhase = -1;
    private long phantomWarned = -1;

    /** Her watch: a chime at dawn and at dusk, a warning when her friend has not slept for three nights, Haste beside her. */
    private void tickClock(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        int phase = level.getDayTime() % 24000L < 12000L ? 0 : 1;
        if (this.clockPhase >= 0 && phase != this.clockPhase) this.chime(level, friend, phase == 0 ? "dawn" : "dusk");
        this.clockPhase = phase;
        if (friend == null) return;
        if (friend.distanceToSqr(this) < 8 * 8) friend.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, false));
        long night = level.getDayTime() / 24000L;
        if (phase == 1 && this.phantomWarned != night && phantomsDue(friend)) {
            this.phantomWarned = night;
            this.chime(level, friend, "phantoms");
        }
    }

    public static boolean phantomsDue(Player p) {
        return p instanceof net.minecraft.server.level.ServerPlayer sp
                && sp.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST)) >= PHANTOM_TICKS;
    }

    /** "dawn", "dusk" or "phantoms": the watch chimes, and her friend is told. */
    public void chime(ServerLevel level, @Nullable Player friend, String which) {
        this.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, which.equals("phantoms") ? 0.6F : 1.4F);
        level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 1, 0.2, 0.1, 0.2, 0.6);
        if (friend != null) friend.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.clock_" + which, this.getDisplayName()), true);
    }

    @Override
    protected boolean isGliding() {
        return this.species == GirlSpecies.WOLPERTINGER_JACKALOPE && this.getDeltaMovement().y < -0.1;
    }

    /** Petting only counts if you crouch down to her level. */
    @Override
    protected int petValue(Player player) {
        return player.isShiftKeyDown() || this.getAffection(player) >= 10 ? 1 : 0;
    }

    /** Once caught, the Inaba's affection grows twice as fast. */
    @Override
    public void addAffection(Player player, int amount) {
        super.addAffection(player, amount > 0 && this.caught ? amount * 2 : amount);
    }

    public boolean isCaught() {
        return this.caught;
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.MOON_RABBIT_JACKALOPE && stack.is(net.minecraft.world.item.Items.CHORUS_FRUIT) && !this.isBaby()) {
            if (this.level() instanceof ServerLevel) this.pound(player, stack);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.INABA_JACKALOPE && !this.caught) {         // any hand on her at all: caught
            if (this.level() instanceof ServerLevel server) {
                this.caught = true;
                this.setPersistenceRequired();
                server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.caught", this.getDisplayName()), true);
            }
            return null;                                                            // and the click still feeds or pets her
        }
        if (this.species == GirlSpecies.EASTER_JACKALOPE && stack.is(ModBlocks.PAINTED_EGG.get().asItem()) && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) {                     // a found egg brought back: she is delighted
                stack.consume(1, player);
                this.addAffection(player, 2);
                ItemStack treat = PaintedEggBlock.treat(this.random);
                if (!player.getInventory().add(treat)) this.spawnAtLocation(treat);
                this.startTimedAction(Action.LEAP, 16);
                this.hearts(server, 4);
                this.nextEgg = 0;                                                    // and runs off to hide it again
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.getAffection(player) >= 60 && r < 10) return new ItemStack(Items.RABBIT_FOOT);
        if (this.species == GirlSpecies.INABA_JACKALOPE && this.getAffection(player) >= FULL_AFFECTION && r < 50) {
            return new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.RICE_CAKE.get(), 2);   // pounded under the moon
        }
        return switch (this.species) {
            case INABA_JACKALOPE -> new ItemStack(r < 50 ? Items.SUGAR : r < 80 ? Items.GLOWSTONE_DUST : Items.CAKE);
            case EASTER_JACKALOPE -> new ItemStack(ModBlocks.PAINTED_EGG.get());
            case AL_MIRAJ_JACKALOPE -> new ItemStack(r < 60 ? Items.CACTUS : Items.GOLD_NUGGET, r < 60 ? 1 : 3);
            case VOLCANO_JACKALOPE -> new ItemStack(r < 50 ? Items.MAGMA_CREAM : Items.COAL);
            case BOGHARE_JACKALOPE -> new ItemStack(r < 45 ? Items.LILY_PAD : r < 80 ? Items.SLIME_BALL : Items.CARROT, 2);
            case WOLPERTINGER_JACKALOPE -> new ItemStack(r < 50 ? Items.FEATHER : r < 80 ? Items.SWEET_BERRIES : Items.RABBIT_HIDE, 2);
            case CLOCKHARE_JACKALOPE -> new ItemStack(r < 40 ? Items.GOLD_NUGGET : r < 75 ? Items.CARROT : r < 95 ? Items.COPPER_INGOT : Items.CLOCK, r < 75 ? 3 : 1);
            default -> new ItemStack(r < 50 ? Items.CARROT : Items.DANDELION, 2);
        };
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    protected int litterSize() {
        return 3 + this.random.nextInt(2);                                   // rabbits are famous for it
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** Short drops (up to six blocks) do not hurt a jackalope; none hurt the gliding wolpertinger. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.WOLPERTINGER_JACKALOPE && super.causeFallDamage(distance - 3.0F, multiplier, source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (!this.boots.isEmpty()) this.spawnAtLocation(this.boots);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.AL_MIRAJ_JACKALOPE && source.getEntity() instanceof LivingEntity attacker) this.rallyClan(attacker);
        return hurt;
    }

    /** Hurt one Al-mi'raj and the whole clan comes for you. */
    private void rallyClan(LivingEntity attacker) {
        if (attacker instanceof Jackalope) return;
        for (Jackalope j : this.level().getEntitiesOfClass(Jackalope.class, this.getBoundingBox().inflate(24),
                j -> j.species == GirlSpecies.AL_MIRAJ_JACKALOPE && !j.isBaby() && j != attacker)) {
            j.setTarget(attacker);
        }
    }

    /** Jack's antlers throw what she hits. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.JACK_JACKALOPE && target instanceof LivingEntity l) {
            Vec3 push = l.position().subtract(this.position()).normalize();
            l.push(push.x * 1.2, 0.4, push.z * 1.2);
            l.hurtMarked = true;
        }
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    public boolean isPanting() {
        return this.getAction() == Action.PANT;
    }

    public int getStamina() {
        return this.stamina;
    }

    /** Chorus fruit into a potion of healing. */
    public void pound(Player player, ItemStack fruit) {
        fruit.consume(1, player);
        ItemStack elixir = net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.HEALING);
        if (!player.getInventory().add(elixir)) player.drop(elixir, false);
        this.playSound(net.minecraft.sounds.SoundEvents.GRINDSTONE_USE, 0.6F, 1.4F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.BOGHARE_JACKALOPE && this.tickCount % 40 == 0 && !this.isBaby()) {   // her friend leaps like her
            Player leaper = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
            if (leaper != null) leaper.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 1, true, false));
        }
        // luck travels with her while she is following her favourite player
        if (this.tickCount % 40 == 0 && !this.isBaby()) {
            Player p = this.bondedPlayer(FOLLOW_AFFECTION, 8);
            if (p != null) p.addEffect(new MobEffectInstance(MobEffects.LUCK, 100, this.getAffection(p) >= 60 ? 1 : 0, true, false));
        }
        if (this.species == GirlSpecies.VOLCANO_JACKALOPE) this.tickStamina();
        if (this.species == GirlSpecies.WOLPERTINGER_JACKALOPE) this.tickWings(server);
        if (this.species == GirlSpecies.CLOCKHARE_JACKALOPE && !this.isBaby() && this.tickCount % 20 == 0) this.tickClock(server);
        if (this.species == GirlSpecies.INABA_JACKALOPE && !server.isDay() && this.tickCount % 20 == 0) {
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.2, this.getZ(), 1, 0.3, 0.4, 0.3, 0.0);
        }
        if (this.species == GirlSpecies.EASTER_JACKALOPE && !this.isBaby()) this.tickEggs(server);
        if (this.species == GirlSpecies.JACK_JACKALOPE && !this.isBaby()) {
            if (this.nextShed == 0) this.nextShed = server.getGameTime() + 3 * 24000 + this.random.nextInt(2 * 24000);
            if (server.getGameTime() >= this.nextShed) {                     // she sheds her antlers (they grow back)
                this.nextShed = server.getGameTime() + 3 * 24000 + this.random.nextInt(2 * 24000);
                this.spawnAtLocation(new ItemStack(Items.BONE, 2));
            }
        }
        if (this.species == GirlSpecies.AL_MIRAJ_JACKALOPE && this.tickCount % 20 == 0) {   // the clan defends her player too
            Player p = this.bondedPlayer(60, 24);
            if (p != null && p.getLastHurtByMob() != null && p.getLastHurtByMob().isAlive() && p.getLastHurtByMob() != this) this.setTarget(p.getLastHurtByMob());
        }
    }

    /** Running drains her breath; at zero she stops and pants for a few seconds, then recovers. */
    private void tickStamina() {
        boolean running = this.getDeltaMovement().horizontalDistanceSqr() > 0.004;
        if (this.isPanting()) {
            this.getNavigation().stop();
            if (++this.stamina >= STAMINA) this.setAction(Action.NONE);
            else this.stamina++;
            return;
        }
        if (running) {
            if (--this.stamina <= 0) {
                this.stamina = 0;
                this.getNavigation().stop();
                this.setAction(Action.PANT);
            }
        } else if (this.stamina < STAMINA) {
            this.stamina++;
        }
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }

    /** Every few minutes she hops off and hides a painted egg in the grass somewhere nearby. */
    private void tickEggs(ServerLevel level) {
        if (this.tickCount % 20 != 0) return;
        this.eggs.removeIf(p -> !level.getBlockState(p).is(ModBlocks.PAINTED_EGG.get()));
        if (level.getGameTime() < this.nextEgg || this.eggs.size() >= MAX_EGGS) return;
        this.nextEgg = level.getGameTime() + 200;                               // nowhere to hide one here: try again soon
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 24; i++) {
            BlockPos p = o.offset(this.random.nextInt(17) - 8, this.random.nextInt(5) - 2, this.random.nextInt(17) - 8);
            if (!level.getBlockState(p).canBeReplaced() || level.getBlockState(p).liquid()) continue;
            if (!level.getBlockState(p.below()).is(BlockTags.DIRT) && !level.getBlockState(p.below()).is(BlockTags.SAND)) continue;
            level.setBlock(p, ModBlocks.PAINTED_EGG.get().defaultBlockState().setValue(PaintedEggBlock.COLOR, this.random.nextInt(PaintedEggBlock.COLORS)), 3);
            this.eggs.add(p.immutable());
            this.nextEgg = level.getGameTime() + 2400 + this.random.nextInt(2400);
            this.getNavigation().moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 1.1);
            level.playSound(null, p, SoundEvents.GRASS_PLACE, net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.4F);
            return;
        }
    }

    public List<BlockPos> getHiddenEggs() {
        return this.eggs;
    }

    public void setNextEgg(long time) {
        this.nextEgg = time;
    }

    // --- spawning ------------------------------------------------------------------------------------------------

    /**
     * Inaba only at night under a full moon; the volcano rabbit only within a few blocks of lava; the others like any
     * creature.
     */
    public static boolean spawnRule(GirlSpecies s, ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (s == GirlSpecies.INABA_JACKALOPE) {
            return level.getLevel().isNight() && level.getLevel().getMoonPhase() == 0;
        }
        if (s == GirlSpecies.VOLCANO_JACKALOPE) {
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -3, -6), pos.offset(6, 2, 6))) {
                if (level.getFluidState(p).is(net.minecraft.tags.FluidTags.LAVA)) return true;
            }
            return false;
        }
        return true;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.RABBIT_JUMP, 0.15F, 1.2F);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.caught;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Caught", this.caught);
        tag.putLong("NextEgg", this.nextEgg);
        tag.putLong("NextShed", this.nextShed);
        if (!this.boots.isEmpty()) {
            tag.put("Boots", this.boots.save(this.registryAccess()));
            if (this.bootsOwner != null) tag.putUUID("BootsOwner", this.bootsOwner);
        }
        tag.putLong("NextBootPrank", this.nextBootPrank);
        net.minecraft.nbt.ListTag l = new net.minecraft.nbt.ListTag();
        for (BlockPos p : this.eggs) l.add(LongTag.valueOf(p.asLong()));
        tag.put("Eggs", l);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.caught = tag.getBoolean("Caught");
        this.nextEgg = tag.getLong("NextEgg");
        this.nextShed = tag.getLong("NextShed");
        this.boots = tag.contains("Boots") ? ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Boots")) : ItemStack.EMPTY;
        this.bootsOwner = tag.hasUUID("BootsOwner") ? tag.getUUID("BootsOwner") : null;
        this.nextBootPrank = tag.getLong("NextBootPrank");
        this.eggs.clear();
        for (Tag t : tag.getList("Eggs", Tag.TAG_LONG)) this.eggs.add(BlockPos.of(((LongTag) t).getAsLong()));
    }
}
