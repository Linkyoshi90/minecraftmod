package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The golem family (construct_field_guide.html): the clay golem. Tougher than an iron golem (120 health), she fights
 * monsters near villagers and, once devoted, near her friend; iron golems are her kin. She is made, not born (no young,
 * no bed), clay patches her up, and when she falls she breaks into pieces.
 */
public class Construct extends AbstractMonsterGirl {
    public static final double HEALTH = 120.0;

    public Construct(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, this::isThreat));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.TIN_SQUIRREL_CONSTRUCT) return girlAttributes(16, 0.32, 1).add(Attributes.ARMOR, 4);
        if (s == GirlSpecies.TALOSLING_CONSTRUCT) return girlAttributes(40, 0.3, 6).add(Attributes.ARMOR, 6);
        if (s == GirlSpecies.GARGOYLE_CONSTRUCT) return girlAttributes(40, 0.27, 7).add(Attributes.ARMOR, 6).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
        return girlAttributes(HEALTH, 0.22, 12).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.ARMOR, 4);
    }

    // --- the tin squirrel ----------------------------------------------------------------------------------------------

    private final net.minecraft.world.SimpleContainer hoard = new net.minecraft.world.SimpleContainer(9);

    public boolean isTin() {
        return this.species == GirlSpecies.TIN_SQUIRREL_CONSTRUCT;
    }

    public net.minecraft.world.SimpleContainer getHoard() {
        return this.hoard;
    }

    /** A worn iron or gold tool or armour piece, broken down into nuggets (more the less worn it is). Null if she won't take it. */
    @Nullable
    public ItemStack scrap(ItemStack worn) {
        if (!worn.isDamageableItem() || !worn.isDamaged()) return null;
        net.minecraft.world.item.Item nugget = worn.getItem().isValidRepairItem(worn, new ItemStack(Items.IRON_INGOT)) ? Items.IRON_NUGGET
                : worn.getItem().isValidRepairItem(worn, new ItemStack(Items.GOLD_INGOT)) ? Items.GOLD_NUGGET : null;
        if (nugget == null) return null;
        int n = 1 + Math.round(8.0F * (1.0F - (float) worn.getDamageValue() / worn.getMaxDamage()));
        return new ItemStack(nugget, n);
    }

    /** Nuggets lying near her go into her hoard. */
    private void tickHoard(net.minecraft.server.level.ServerLevel level) {
        for (net.minecraft.world.entity.item.ItemEntity e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, this.getBoundingBox().inflate(1.5),
                e -> e.isAlive() && (e.getItem().is(Items.IRON_NUGGET) || e.getItem().is(Items.GOLD_NUGGET)))) {
            ItemStack left = this.hoard.addItem(e.getItem().copy());
            if (left.getCount() != e.getItem().getCount()) this.playSound(SoundEvents.ITEM_PICKUP, 0.6F, 1.6F);
            if (left.isEmpty()) e.discard();
            else e.setItem(left);
        }
        if (this.tickCount % 40 == 0 && this.getNavigation().isDone()) {
            net.minecraft.world.entity.item.ItemEntity near = this.nearest(net.minecraft.world.entity.item.ItemEntity.class, 8,
                    e -> e.getItem().is(Items.IRON_NUGGET) || e.getItem().is(Items.GOLD_NUGGET));
            if (near != null) this.getNavigation().moveTo(near, 1.1);
        }
    }

    // --- the talosling --------------------------------------------------------------------------------------------------

    /** Ticks a redstone dust winds her for, and the most her spring holds. */
    public static final int WIND_PER_DUST = 6000, MAX_WIND = 24000;
    private int wound = MAX_WIND;

    public boolean isTalos() {
        return this.species == GirlSpecies.TALOSLING_CONSTRUCT;
    }

    public int getWound() {
        return this.wound;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.isTalos()) tag.putInt("Wound", this.wound);
        if (this.isTin()) tag.put("Hoard", this.hoard.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Wound")) this.wound = tag.getInt("Wound");
        if (tag.contains("Hoard")) this.hoard.fromTag(tag.getList("Hoard", net.minecraft.nbt.Tag.TAG_COMPOUND), this.registryAccess());
    }

    /** Redstone dust winds her spring. */
    public boolean windUp(Player player, ItemStack dust) {
        if (!this.isTalos() || !dust.is(Items.REDSTONE) || this.wound >= MAX_WIND) return false;
        dust.consume(1, player);
        this.wound = Math.min(MAX_WIND, this.wound + WIND_PER_DUST);
        if (this.getAction() == Action.RUNDOWN) this.setAction(Action.NONE);
        this.playSound(SoundEvents.LEVER_CLICK, 1.0F, 1.4F);
        return true;
    }

    @Nullable
    @Override
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        net.minecraft.world.InteractionResult toss = com.linkyoshi90.harmontide.entity.Traversal.toss(this, player, stack);   // up onto the ledge with you
        if (toss != null) return toss;
        if (this.isTin() && !this.isBaby()) {
            ItemStack nuggets = this.scrap(stack);
            if (nuggets != null) {
                if (!this.level().isClientSide) {
                    stack.shrink(1);
                    if (!player.getInventory().add(nuggets)) player.drop(nuggets, false);
                    this.playSound(SoundEvents.SMITHING_TABLE_USE, 0.8F, 1.4F);
                    this.addAffection(player, 1);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        if (this.isTalos() && stack.is(Items.REDSTONE)) {
            if (!this.level().isClientSide && this.windUp(player, stack)) this.addAffection(player, 1);
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    private void tickTalos() {
        if (this.wound > 0) {
            this.wound--;
            if (this.wound == 0) {
                this.setAction(Action.RUNDOWN);
                this.setTarget(null);
                this.playSound(SoundEvents.LEVER_CLICK, 0.6F, 0.6F);
            }
        } else {
            this.getNavigation().stop();
            if (this.getAction() != Action.RUNDOWN) this.setAction(Action.RUNDOWN);
        }
    }

    // --- the gargoyle ---------------------------------------------------------------------------------------------------

    public boolean isGargoyle() {
        return this.species == GirlSpecies.GARGOYLE_CONSTRUCT;
    }

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.GARGOYLE_CONSTRUCT;
    }

    /** By day, with nothing to fight, the gargoyle is stone. */
    public boolean isStone() {
        return this.isGargoyle() && this.getAction() == Action.PERCH;
    }

    private void tickGargoyle() {
        boolean day = this.level().isDay() && this.getTarget() == null;
        if (day && !this.isStone() && (this.getAction() == Action.NONE || this.getAction() == Action.LYING)) this.setAction(Action.PERCH);
        if (!day && this.isStone()) this.setAction(Action.NONE);
        if (this.isStone()) {
            this.getNavigation().stop();
            this.setNoGravity(false);
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isStone() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {   // nothing hurts stone
            if (!this.level().isClientSide) this.playSound(SoundEvents.STONE_HIT, 1.0F, 0.8F);
            return false;
        }
        return super.hurt(source, amount);
    }

    /** The scarecrow: monsters within 8 of her lose their targets and are slowed. How many, or -1 without a friend. */
    public int onWatch(net.minecraft.server.level.ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 32) == null) return -1;
        int scared = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, this.getBoundingBox().inflate(8), net.minecraft.world.entity.monster.Monster::isAlive)) {
            m.setTarget(null);
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            scared++;
        }
        return scared;
    }

    /** The snowgirl pelts the monster nearest her friend: 1, or 5 to a blaze. 1 on a hit, 0 with none, -1 without a friend. */
    public int snowball(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(10), net.minecraft.world.entity.monster.Monster::isAlive)) {
            m.hurt(this.damageSources().mobAttack(this), m instanceof net.minecraft.world.entity.monster.Blaze ? 5 : 1);
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SNOWBALL, m.getX(), m.getY() + 1, m.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
            return 1;
        }
        return 0;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof net.minecraft.server.level.ServerLevel yard && !this.isBaby()) {
            if (this.species == GirlSpecies.SCARECROW_CONSTRUCT && this.tickCount % 41 == 0) this.onWatch(yard);
            if (this.species == GirlSpecies.SNOWGIRL_CONSTRUCT && this.tickCount % 41 == 0) this.snowball(yard);
        }
        if (this.species == GirlSpecies.AUTOMATON_CONSTRUCT && this.level() instanceof net.minecraft.server.level.ServerLevel works && !this.isBaby() && this.tickCount % 40 == 0)
            this.lamplighter(works, p -> works.getMaxLocalRawBrightness(p) < 8);
        if (this.species == GirlSpecies.TIN_JACK_CONSTRUCT && this.level() instanceof ServerLevel field && !this.isBaby() && this.tickCount % 20 == 0) this.scarePhantoms(field);
        if (this.isGargoyle() && !this.level().isClientSide) this.tickGargoyle();
        if (this.isTalos() && !this.level().isClientSide) this.tickTalos();
        if (this.isTin() && this.level() instanceof net.minecraft.server.level.ServerLevel tinLevel && this.tickCount % 5 == 0) this.tickHoard(tinLevel);
    }

    /** A monster near a villager (or, once she is devoted, near her friend). Creepers are left alone, as iron golems do. */
    private boolean isThreat(LivingEntity m) {
        if (m instanceof Creeper) return false;
        if (this.isTalos()) {                                           // wound up, the talosling guards her friend
            Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 32);
            return this.wound > 0 && friend != null && m.distanceToSqr(friend) < 16 * 16;
        }
        if (this.isGargoyle()) {                                        // the gargoyle keeps watch over her friend, at night
            Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 32);
            return !this.level().isDay() && friend != null && m.distanceToSqr(friend) < 16 * 16;
        }
        if (!this.level().getEntitiesOfClass(AbstractVillager.class, m.getBoundingBox().inflate(16)).isEmpty()) return true;
        Player p = this.bondedPlayer(60, 24);
        return p != null && m.distanceToSqr(p) < 12 * 12;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.startTimedAction(Action.SLAM, 14);
        boolean hit = super.doHurtTarget(target);
        if (hit) target.setDeltaMovement(target.getDeltaMovement().add(0, 0.4, 0));   // a blow that lifts, like an iron golem's
        return hit;
    }

    // --- made, not born --------------------------------------------------------------------------------------------

    @Override
    public boolean canShareBed() {
        return false;
    }

    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        return true;                                                    // nothing to bear (the gestation just clears)
    }

    // --- breaking apart ----------------------------------------------------------------------------------------------

    @Override
    public boolean hasDeathAnimation() {
        return true;
    }

    /** Clay crumbles off her while she breaks apart. */
    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.level() instanceof ServerLevel server && this.deathTime % 2 == 0) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, (this.isGargoyle() ? Blocks.STONE : this.isTalos() ? Blocks.COPPER_BLOCK : Blocks.CLAY).defaultBlockState()),
                    this.getX(), this.getY() + this.getBbHeight() * 0.5,
                    this.getZ(), 12, 0.4, this.getBbHeight() * 0.3, 0.4, 0.1);
            if (this.deathTime == 2) this.playSound(SoundEvents.DECORATED_POT_SHATTER, 1.2F, 0.7F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(this.isGargoyle() ? Items.COBBLESTONE : this.isTalos() ? Items.COPPER_INGOT : Items.CLAY_BALL, 4 + this.random.nextInt(5)));
    }

    // --- food, presents ----------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.isTalos()) return stack.is(Items.COPPER_INGOT) || stack.is(Items.IRON_NUGGET);
        if (this.isTin()) return stack.is(Items.IRON_NUGGET) || stack.is(Items.GOLD_NUGGET);
        if (this.isGargoyle()) return stack.is(Items.COBBLESTONE) || stack.is(Items.STONE) || stack.is(Items.STONE_BRICKS);
        return stack.is(Items.CLAY_BALL) || stack.is(Items.CLAY);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.CLAY) || stack.is(Items.STONE_BRICKS) ? 3 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.isTin()) {                                             // something from her hoard, else odds and ends
            for (int i = 0; i < this.hoard.getContainerSize(); i++) if (!this.hoard.getItem(i).isEmpty()) return this.hoard.removeItem(i, 4);
            return new ItemStack(r < 50 ? Items.IRON_NUGGET : r < 85 ? Items.COPPER_INGOT : Items.GOLD_NUGGET, r < 50 ? 4 : 2);
        }
        if (this.isTalos()) return new ItemStack(r < 40 ? Items.COPPER_INGOT : r < 75 ? Items.REDSTONE : r < 95 ? Items.IRON_NUGGET : Items.CLOCK,
                r < 40 ? 2 : r < 75 ? 4 : r < 95 ? 6 : 1);
        if (this.isGargoyle()) return r < 45 ? new ItemStack(Items.COBBLESTONE, 4) : r < 80 ? new ItemStack(Items.FLINT, 2) : r < 95 ? new ItemStack(Items.AMETHYST_SHARD)
                : new ItemStack(Items.CHISELED_STONE_BRICKS);
        return r < 45 ? new ItemStack(Items.CLAY_BALL, 4) : r < 75 ? new ItemStack(Items.BRICK, 3) : r < 95 ? new ItemStack(Items.FLOWER_POT)
                : new ItemStack(Items.POPPY);
    }

    // --- sounds ----------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isGargoyle() ? SoundEvents.STONE_HIT : SoundEvents.MUD_BRICKS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DECORATED_POT_BREAK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.MUD_PLACE;
    }

    // --- Tin Jack (nineteenth wave) ---------------------------------------------------------------------------------

    /** Phantoms within 24 blocks of her friend lose heart: Weakness II, no target, and they wheel up and away. How many, or -1
     *  without a friend. */
    public int scarePhantoms(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (var phantom : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Phantom.class, friend.getBoundingBox().inflate(24))) {
            phantom.setTarget(null);
            phantom.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 1));
            net.minecraft.world.phys.Vec3 away = phantom.position().subtract(friend.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0E-4) away = new net.minecraft.world.phys.Vec3(1, 0, 0);
            away = away.normalize();
            phantom.setDeltaMovement(away.x * 0.8, 0.6, away.z * 0.8);
            n++;
        }
        return n;
    }

    /**
     * The automaton's lamplighter (twenty-third wave): a torch from her friend's own pack set on a dark spot near them (within
     * 5, on solid ground). 1 when she set one, 0 when there was no dark spot or no torch, -1 without a friend.
     */
    public int lamplighter(net.minecraft.server.level.ServerLevel level, java.util.function.Predicate<net.minecraft.core.BlockPos> dark) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int slot = friend.getInventory().findSlotMatchingItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TORCH));
        if (slot < 0) return 0;
        net.minecraft.core.BlockPos at = friend.blockPosition();
        for (int i = 0; i < 40; i++) {
            net.minecraft.core.BlockPos p = at.offset(this.random.nextInt(11) - 5, this.random.nextInt(3) - 1, this.random.nextInt(11) - 5);
            if (!level.getBlockState(p).isAir() || !net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState().canSurvive(level, p) || !dark.test(p)) continue;
            level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.TORCH.defaultBlockState());
            if (!friend.getAbilities().instabuild) friend.getInventory().getItem(slot).shrink(1);
            this.getLookControl().setLookAt(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            this.playSound(net.minecraft.sounds.SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
            return 1;
        }
        return 0;
    }
}
