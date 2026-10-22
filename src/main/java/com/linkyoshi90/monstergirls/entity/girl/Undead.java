package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The restless family (undead_field_guide.html): living doll, will-o'-wisp, yuki-onna, phantom, vampire, zombie,
 * Frankenstein's monster. Most of them fix on a person: the doll and the yuki-onna creep after you while you are not
 * looking, the phantom swoops on the sleepless, the vampire stalks you by night and drinks a little blood (never
 * killing). The zombie bites until fed rotten flesh; the others are won with pumpkin pie. The wisp floats and burns,
 * the monster is too shy to be near anyone who is not crouching.
 */
public class Undead extends AbstractMonsterGirl {
    /** Three nights without sleep (the vanilla phantom rule). */
    public static final int INSOMNIA = 72000;
    public static final float BLOOD = 4.0F;

    private long nextDrinkOffer;
    private long strongUntil;
    private long nextGuide;
    @Nullable
    private BlockPos treasure;
    private final java.util.Map<java.util.UUID, Long> keened = new java.util.HashMap<>();

    public Undead(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        boolean creeper = species == GirlSpecies.LIVING_DOLL_UNDEAD || species == GirlSpecies.YUKI_ONNA_UNDEAD;
        if (creeper) this.goalSelector.addGoal(1, new StalkGoal(this, 1.0, 1.6, true, t -> t instanceof Player));
        if (species == GirlSpecies.LIVING_DOLL_UNDEAD) this.goalSelector.addGoal(2, new ShelfGoal());
        if (species == GirlSpecies.VAMPIRE_UNDEAD) this.goalSelector.addGoal(1, new StalkGoal(this, 0.8, 2.5, false, t -> !this.level().isDay()));
        if (species == GirlSpecies.FRANKENSTEIN_UNDEAD) {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.0, 1.4,
                    e -> e instanceof Player p && !p.isShiftKeyDown() && this.getAffection(p) < BED_AFFECTION && !p.isSpectator()));
        }
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, species == GirlSpecies.ZOMBIE_UNDEAD
                ? Ingredient.of(Items.ROTTEN_FLESH, Items.PUMPKIN_PIE) : Ingredient.of(Items.PUMPKIN_PIE), false));
        if (!this.canFly()) this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (species == GirlSpecies.PHANTOM_UNDEAD) {                         // befriended: no vanilla phantom comes near
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Phantom.class, true, p -> this.isCalm()));
        }
        if (species == GirlSpecies.FRANKENSTEIN_UNDEAD) {                    // a terrifying bodyguard, once devoted
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> { Player p = this.bondedPlayer(60, 16); return p != null && m.distanceToSqr(p) < 10 * 10; }));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case LIVING_DOLL_UNDEAD -> girlAttributes(14, 0.3, 0);
            case WILL_O_WISP_UNDEAD -> girlAttributes(16, 0.3, 3);
            case YUKI_ONNA_UNDEAD -> girlAttributes(24, 0.28, 3);
            case PHANTOM_UNDEAD -> girlAttributes(20, 0.32, 4);
            case VAMPIRE_UNDEAD -> girlAttributes(28, 0.3, 3);
            case BANSHEE_UNDEAD -> girlAttributes(22, 0.28, 2);
            case FRANKENSTEIN_UNDEAD -> girlAttributes(50, 0.25, 10).add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.8);
            default -> girlAttributes(24, 0.25, 4);
        };
    }

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.PHANTOM_UNDEAD || this.species == GirlSpecies.WILL_O_WISP_UNDEAD;
    }

    @Override
    protected boolean hasFlyAnimation() {
        return this.species == GirlSpecies.PHANTOM_UNDEAD;
    }

    // --- hostility -----------------------------------------------------------------------------------------------

    /** The zombie bites, the doll and the yuki-onna stalk, the phantom swoops on the sleepless. */
    @Override
    protected boolean wildHostile() {
        return switch (this.species) {
            case ZOMBIE_UNDEAD, LIVING_DOLL_UNDEAD, YUKI_ONNA_UNDEAD, PHANTOM_UNDEAD -> true;
            default -> false;
        };
    }

    @Override
    public boolean isHostileTo(Player player) {
        if (!super.isHostileTo(player)) return false;
        if (this.species == GirlSpecies.PHANTOM_UNDEAD) return sleepless(player);
        return true;
    }

    /** Whether the player has gone three nights without sleep. */
    public static boolean sleepless(Player player) {
        return player instanceof ServerPlayer sp && sp.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= INSOMNIA;
    }

    @Override
    protected boolean calmsHer(ItemStack stack) {
        return this.species == GirlSpecies.ZOMBIE_UNDEAD ? stack.is(Items.ROTTEN_FLESH) : stack.is(Items.PUMPKIN_PIE);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.species == GirlSpecies.LIVING_DOLL_UNDEAD) {                 // she only tugs at your sleeve
            Vec3 push = target.position().subtract(this.position()).normalize();
            target.push(push.x * 0.3, 0.1, push.z * 0.3);
            target.hurtMarked = true;
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.8F, 1.6F);
            this.swing(InteractionHand.MAIN_HAND);
            return true;
        }
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l) {
            switch (this.species) {
                case YUKI_ONNA_UNDEAD -> {                                    // a freezing touch, like powder snow
                    l.setTicksFrozen(Math.min(l.getTicksRequiredToFreeze() + 120, l.getTicksFrozen() + 140));
                    l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                }
                case WILL_O_WISP_UNDEAD -> l.igniteForSeconds(4);
                case ZOMBIE_UNDEAD -> l.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
                default -> {
                }
            }
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.WILL_O_WISP_UNDEAD && source.getEntity() instanceof LivingEntity a && a.distanceToSqr(this) < 9) {
            a.igniteForSeconds(3);                                           // touch the flame, catch fire
        }
        return hurt;
    }

    /** Lightning heals Frankenstein's monster and leaves her stronger for a day. */
    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        if (this.species != GirlSpecies.FRANKENSTEIN_UNDEAD) {
            super.thunderHit(level, bolt);
            return;
        }
        this.setHealth(this.getMaxHealth());
        this.strongUntil = level.getGameTime() + 24000;
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 24000, 1));
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.5, this.getZ(), 40, 0.5, 1, 0.5, 0.2);
    }

    // --- the vampire's drink -------------------------------------------------------------------------------------

    /** A little blood, never the last of it: the drink stops at one heart. */
    public static void drinkFrom(Player player, Undead vampire) {
        float take = Math.min(BLOOD, Math.max(0.0F, player.getHealth() - 2.0F));
        if (take > 0) player.setHealth(player.getHealth() - take);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2400, 0));
        vampire.heal(BLOOD);
        vampire.addAffection(player, 3);
        vampire.startTimedAction(Action.DRINK, 40);
        vampire.playSound(SoundEvents.HONEY_DRINK, 1.0F, 0.8F);
    }

    /** A polite vampire asks first: say yes with an empty hand. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.VAMPIRE_UNDEAD && stack.isEmpty() && !player.isShiftKeyDown() && !this.isBaby()
                && this.level().getGameTime() < this.nextDrinkOffer + 600 && this.nextDrinkOffer > 0) {
            if (!this.level().isClientSide) {
                drinkFrom(player, this);
                this.nextDrinkOffer = 0;
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.PUMPKIN_PIE) || (this.species == GirlSpecies.ZOMBIE_UNDEAD && stack.is(Items.ROTTEN_FLESH));
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.PUMPKIN_PIE) ? 2 : 1;
    }

    @Override
    protected int petValue(Player player) {
        return this.species == GirlSpecies.FRANKENSTEIN_UNDEAD && !player.isShiftKeyDown() && this.getAffection(player) < BED_AFFECTION ? 0 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.getAffection(player) >= 60 && r < 3) return new ItemStack(Items.ECHO_SHARD);
        return switch (this.species) {
            case WILL_O_WISP_UNDEAD -> new ItemStack(r < 60 ? Items.SOUL_TORCH : Items.SOUL_LANTERN);
            case YUKI_ONNA_UNDEAD -> new ItemStack(r < 60 ? Items.SNOWBALL : Items.BLUE_ICE, r < 60 ? 8 : 1);
            case VAMPIRE_UNDEAD -> new ItemStack(r < 50 ? Items.GOLD_NUGGET : Items.ROSE_BUSH, r < 50 ? 4 : 1);
            case PHANTOM_UNDEAD -> new ItemStack(Items.PHANTOM_MEMBRANE);
            case BANSHEE_UNDEAD -> new ItemStack(r < 75 ? Items.IRON_NUGGET : Items.GHAST_TEAR, r < 75 ? 3 : 1);
            case LIVING_DOLL_UNDEAD -> new ItemStack(r < 50 ? Items.STRING : Items.PINK_WOOL);
            default -> new ItemStack(r < 50 ? Items.BONE : r < 85 ? Items.SOUL_SAND : Items.ROTTEN_FLESH);
        };
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.WILL_O_WISP_UNDEAD) {
            this.glow(server, 11);
            if (this.tickCount % 4 == 0) server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.2, 0.2, 0.2, 0.01);
            this.guide(server);
        }
        if (this.species == GirlSpecies.BANSHEE_UNDEAD && this.tickCount % 20 == 0) this.keen(server);
        if (this.tickCount % 40 != 0) return;
        boolean sun = server.isDay() && server.canSeeSky(this.blockPosition().above());
        if (sun && (this.species == GirlSpecies.VAMPIRE_UNDEAD || this.species == GirlSpecies.ZOMBIE_UNDEAD)) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false));   // sluggish in the sun, never burnt
        }
        if (this.species == GirlSpecies.YUKI_ONNA_UNDEAD && !this.hasAnyAffection()
                && !server.getBiome(this.blockPosition()).value().coldEnoughToSnow(this.blockPosition())) {
            server.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1, this.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
            this.discard();                                                   // she melts away anywhere warm
            return;
        }
        if (this.species == GirlSpecies.VAMPIRE_UNDEAD && !server.isDay() && this.nextDrinkOffer == 0 && affectionOf(this.nearestPlayer()) >= 10) {
            Player p = this.nearestPlayer();
            if (p != null && p.distanceToSqr(this) < 4 * 4) {               // a polite one asks first
                this.nextDrinkOffer = server.getGameTime();
                p.displayClientMessage(Component.translatable("message.monstergirls.vampire_asks", this.getDisplayName()), true);
            }
        }
        if (this.species == GirlSpecies.FRANKENSTEIN_UNDEAD && this.tickCount % 600 == 0) {   // patience: crouch near her and wait
            Player p = this.level().getNearestPlayer(this, 5);
            if (p != null && p.isShiftKeyDown()) {
                this.addAffection(p, 1);
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 1, 0.2, 0.2, 0.2, 0);
            }
        }
        Player friend = this.bondedPlayer(60, 16);                           // devoted: she haunts your enemies
        if (friend != null) {
            for (Monster m : server.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(8))) {
                m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
        }
    }

    /**
     * The banshee's keening: a player within 24 blocks at three hearts or less makes her wail, and every hostile mob
     * within 16 blocks of that player glows for ten seconds (like a bell showing raiders). Once a minute per player.
     */
    public void keen(ServerLevel level) {
        long now = level.getGameTime();
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24),
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator() && p.getHealth() <= 6.0F)) {
            if (now < this.keened.getOrDefault(p.getUUID(), 0L)) continue;
            this.keened.put(p.getUUID(), now + 1200);
            this.playSound(SoundEvents.GHAST_SCREAM, 1.0F, 1.4F);
            for (Monster m : level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(16))) {
                m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            }
            p.displayClientMessage(Component.translatable("message.monstergirls.keening", this.getDisplayName()), true);
        }
    }

    @Nullable
    private Player nearestPlayer() {
        return this.level().getNearestPlayer(this, 16);
    }

    private int affectionOf(@Nullable Player p) {
        return p == null ? 0 : this.getAffection(p);
    }

    /** At night the wisp drifts ahead of the player she likes, towards buried treasure. */
    private void guide(ServerLevel level) {
        if (level.isDay() || this.tickCount % 20 != 0) return;
        Player p = this.bondedPlayer(10, 24);
        if (p == null || this.getTarget() != null) return;
        if (level.getGameTime() >= this.nextGuide) {
            this.nextGuide = level.getGameTime() + 1200;
            this.treasure = level.findNearestMapStructure(StructureTags.ON_TREASURE_MAPS, this.blockPosition(), 50, false);
        }
        if (this.treasure != null && this.getNavigation().isDone() && p.distanceToSqr(this) < 10 * 10) {
            Vec3 t = this.position().add(Vec3.atCenterOf(this.treasure).subtract(this.position()).normalize().scale(10));
            this.getNavigation().moveTo(t.x, this.getY(), t.z, 0.9);
        }
    }

    public static boolean spawnRule(GirlSpecies s, ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (s == GirlSpecies.YUKI_ONNA_UNDEAD) return level.getBiome(pos).value().coldEnoughToSnow(pos);
        return true;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case ZOMBIE_UNDEAD -> SoundEvents.ZOMBIE_AMBIENT;
            case PHANTOM_UNDEAD -> SoundEvents.PHANTOM_AMBIENT;
            case WILL_O_WISP_UNDEAD -> SoundEvents.SOUL_ESCAPE.value();
            case LIVING_DOLL_UNDEAD -> null;
            default -> SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == GirlSpecies.ZOMBIE_UNDEAD ? SoundEvents.ZOMBIE_HURT : SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.species == GirlSpecies.ZOMBIE_UNDEAD ? SoundEvents.ZOMBIE_DEATH : SoundEvents.SOUL_ESCAPE.value();
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("StrongUntil", this.strongUntil);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.strongUntil = tag.getLong("StrongUntil");
    }

    // --- the doll's perch ---------------------------------------------------------------------------------------------

    /** A bookshelf (or anything that powers an enchanting table, so modded shelves count). */
    public static boolean isShelf(Level level, BlockPos pos) {
        var s = level.getBlockState(pos);
        return s.is(net.minecraft.world.level.block.Blocks.BOOKSHELF) || s.is(net.minecraft.world.level.block.Blocks.CHISELED_BOOKSHELF)
                || s.getEnchantPowerBonus(level, pos) > 0;
    }

    /**
     * With nobody to creep after, the living doll looks for a bookshelf with room on top, hops up and sits there
     * like a doll on display, watching the room, for a minute or two. Then she gets down for a while.
     */
    class ShelfGoal extends net.minecraft.world.entity.ai.goal.Goal {
        @Nullable
        private BlockPos shelf;
        private int cooldown;
        private int ticks;
        private int sitFor;

        ShelfGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        private boolean onTop() {
            return this.shelf != null && Undead.this.blockPosition().equals(this.shelf.above());
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || Undead.this.getTarget() != null || Undead.this.isOrderedToSit()) return false;
            this.cooldown = 40;
            BlockPos o = Undead.this.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-10, -3, -10), o.offset(10, 4, 10))) {
                if (!isShelf(Undead.this.level(), p) || !Undead.this.level().getBlockState(p.above()).isAir()) continue;
                double d = p.distSqr(o);
                if (d < bestDist) {
                    bestDist = d;
                    best = p.immutable();
                }
            }
            this.shelf = best;
            return best != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.shelf != null && Undead.this.getTarget() == null && !Undead.this.isOrderedToSit() && isShelf(Undead.this.level(), this.shelf)
                    && Undead.this.level().getBlockState(this.shelf.above()).isAir() && (this.onTop() ? this.ticks < this.sitFor : this.ticks < 400);
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.sitFor = 1200 + Undead.this.random.nextInt(1200);
        }

        @Override
        public void tick() {
            this.ticks++;
            if (this.onTop()) {
                Undead.this.getNavigation().stop();
                Undead.this.setDeltaMovement(Undead.this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                if (Undead.this.getAction() != Action.SITTING) {
                    Undead.this.setAction(Action.SITTING);
                    this.ticks = 0;                                      // the sit begins now
                }
                Player p = Undead.this.level().getNearestPlayer(Undead.this, 12);
                if (p != null) Undead.this.getLookControl().setLookAt(p);
                return;
            }
            Vec3 top = Vec3.atBottomCenterOf(this.shelf.above());
            double dx = top.x - Undead.this.getX(), dz = top.z - Undead.this.getZ();
            if (dx * dx + dz * dz < 2.6 && top.y - Undead.this.getY() < 3.5) {   // close enough: a hop up onto the shelf
                Undead.this.moveTo(top.x, top.y, top.z, Undead.this.getYRot(), 0.0F);
                Undead.this.playSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.6F, 1.6F);
                return;
            }
            if (this.ticks % 10 == 1) Undead.this.getNavigation().moveTo(this.shelf.getX() + 0.5, this.shelf.getY(), this.shelf.getZ() + 0.5, 1.0);
        }

        @Override
        public void stop() {
            if (Undead.this.getAction() == Action.SITTING && !Undead.this.isOrderedToSit()) Undead.this.setAction(Action.NONE);
            if (this.onTop()) this.cooldown = 1200;                         // she has sat long enough for now
            this.shelf = null;
        }
    }
}
