package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The dragon family (dragon_field_guide.html): long, Jormungandr, wyrm, gryphon, Kannae, wyvern, Yamata no Orochi. Proud
 * and strong, neutral unless struck. Every dragon hoards: she gathers gold and gems lying about (or given to her) into a
 * chest near her lair. The long dragon swims through the sky and brings rain for her favourite, Jormungandr coils and
 * shrugs off arrows, the wyrm breathes fire, Kannae calls lightning, the wyvern stings, Orochi's seven heads bite everything
 * around her (and honey, her sake, makes her drowsy). The gryphon carries a friend through the air; the wyrm only at full
 * devotion.
 */
public class Dragon extends AbstractMonsterGirl {
    public static final int GRYPHON_RIDE = BED_AFFECTION;
    private static final int BREATH_COOLDOWN = 80;

    private final SimpleContainer hoard = new SimpleContainer(9);
    @Nullable
    private BlockPos lair;
    private long nextBreath;
    private long nextRain;
    private int drowsy;

    public Dragon(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true) {
            @Override
            public boolean canUse() {
                return Dragon.this.drowsy == 0 && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.COOKED_BEEF, Items.GOLDEN_APPLE, Items.GOLD_INGOT), false));
        if (species == GirlSpecies.KANNAE_DRAGON) this.goalSelector.addGoal(5, new FollowEntityGoal(this, () -> this.bondedPlayer(10, 24), 1.2, 6.0F, 2.0F));
        if (!this.canFly()) this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case LONG_DRAGON -> girlAttributes(60, 0.25, 6);
            case JORMUNGANDR_DRAGON -> girlAttributes(80, 0.22, 10).add(Attributes.ARMOR, 8);
            case WYRM_DRAGON -> girlAttributes(70, 0.28, 10).add(Attributes.ARMOR, 6);
            case GRYPHON_DRAGON -> girlAttributes(50, 0.32, 7);
            case KANNAE_DRAGON -> girlAttributes(40, 0.3, 8);
            case WYVERN_DRAGON -> girlAttributes(44, 0.3, 6);
            case ALEBRIJE_DRAGON -> girlAttributes(30, 0.3, 4);
            case COCKATRICE_DRAGON -> girlAttributes(30, 0.28, 5).add(Attributes.ARMOR, 2);
            default -> girlAttributes(70, 0.26, 9).add(Attributes.ARMOR, 4);
        };
    }

    @Override
    public boolean canFly() {
        return switch (this.species) {
            case LONG_DRAGON, WYRM_DRAGON, GRYPHON_DRAGON, WYVERN_DRAGON, ALEBRIJE_DRAGON, COCKATRICE_DRAGON, MACAWTL_DRAGON, QUETZALCOATL_DRAGON -> true;
            default -> false;
        };
    }

    // --- hoard ---------------------------------------------------------------------------------------------------

    /** Gold, gems and other shiny things a dragon cannot leave lying about. */
    public static boolean shiny(ItemStack s) {
        return s.is(Items.GOLD_INGOT) || s.is(Items.GOLD_NUGGET) || s.is(Items.GOLD_BLOCK) || s.is(Items.DIAMOND) || s.is(Items.EMERALD)
                || s.is(Items.AMETHYST_SHARD) || s.is(Items.RAW_GOLD) || s.is(Items.LAPIS_LAZULI) || s.is(Items.GOLDEN_APPLE);
    }

    public SimpleContainer getHoard() {
        return this.hoard;
    }

    @Nullable
    public BlockPos getLair() {
        return this.lair;
    }

    /** Into the chest by her lair if there is one, otherwise she keeps it on her (dropped when she dies). */
    public void stash(ItemStack stack) {
        if (this.lair != null && this.level() instanceof ServerLevel level) {
            for (BlockPos p : BlockPos.betweenClosed(this.lair.offset(-8, -3, -8), this.lair.offset(8, 3, 8))) {
                if (level.getBlockEntity(p) instanceof ChestBlockEntity chest) {
                    stack = addTo(chest, stack);
                    if (stack.isEmpty()) return;
                }
            }
        }
        ItemStack rest = this.hoard.addItem(stack);
        if (!rest.isEmpty()) this.spawnAtLocation(rest);
    }

    private static ItemStack addTo(Container c, ItemStack stack) {
        for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack in = c.getItem(i);
            if (in.isEmpty()) {
                c.setItem(i, stack.copy());
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(in, stack) && in.getCount() < in.getMaxStackSize()) {
                int n = Math.min(stack.getCount(), in.getMaxStackSize() - in.getCount());
                in.grow(n);
                stack.shrink(n);
            }
        }
        c.setChanged();
        return stack;
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.ALEBRIJE_DRAGON && (stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES)
                || stack.getItem() instanceof net.minecraft.world.item.DyeItem)) return true;
        return stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.COOKED_MUTTON) || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.COOKED_RABBIT) || stack.is(Items.GOLDEN_APPLE);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE) ? 4 : stack.is(Items.COOKED_BEEF) ? 2 : 1;
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        boolean client = this.level().isClientSide;
        if (shiny(stack) && !stack.is(Items.GOLDEN_APPLE)) {                    // a gift for the hoard
            if (!client) {
                this.addAffection(player, 2);
                this.stash(stack.split(1));
                if (this.level() instanceof ServerLevel s) this.hearts(s, 3);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.OROCHI_DRAGON && stack.is(Items.HONEY_BOTTLE)) {   // her sake
            if (!client) {
                stack.consume(1, player);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                this.drowsy = 1200;
                this.setTarget(null);
                this.addAffection(player, 3);
                this.startTimedAction(Action.LYING, 200);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.isEmpty() && !this.isBaby()) {
            if (this.species == GirlSpecies.LONG_DRAGON && player.isShiftKeyDown() && this.getAffection(player) >= 60
                    && this.level() instanceof ServerLevel server && server.getGameTime() >= this.nextRain) {
                this.nextRain = server.getGameTime() + 24000;                   // the rain bringer
                server.setWeatherParameters(0, 6000, true, false);
                this.startTimedAction(Action.CALL, 30);
                return InteractionResult.SUCCESS;
            }
            int need = this.species == GirlSpecies.GRYPHON_DRAGON ? GRYPHON_RIDE : this.species == GirlSpecies.WYRM_DRAGON ? FULL_AFFECTION : Integer.MAX_VALUE;
            if (!player.isShiftKeyDown() && this.getAffection(player) >= need && !this.isVehicle()) {
                if (!client) {
                    this.setOrderedToSit(false);
                    this.getNavigation().stop();
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (player.isShiftKeyDown()) return this.pet(player);
        }
        return null;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.species == GirlSpecies.ALEBRIJE_DRAGON) {                 // her presents are dyes, every colour
            return new ItemStack(net.minecraft.world.item.DyeItem.byColor(net.minecraft.world.item.DyeColor.byId(r % 16)), 3);
        }
        if (r < 5) return new ItemStack(Items.DIAMOND);
        if (r < 30) return new ItemStack(Items.GOLD_INGOT, 2);
        return switch (this.species) {
            case WYRM_DRAGON -> new ItemStack(Items.BLAZE_POWDER, 2);
            case KANNAE_DRAGON -> new ItemStack(Items.CAKE);
            case OROCHI_DRAGON -> new ItemStack(Items.HONEY_BOTTLE);
            default -> new ItemStack(Items.COOKED_BEEF, 2);
        };
    }

    // --- riding: the gryphon and the wyrm carry a friend through the air ----------------------------------------

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        return new Vec3(0.0, dims.height() * (this.species == GirlSpecies.GRYPHON_DRAGON ? 0.55 : 0.75), 0.4);
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player p && this.canFly()) {
            this.setYRot(p.getYRot());
            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
            this.setNoGravity(true);
            Vec3 v = this.getDeltaMovement().scale(0.85);
            if (p.zza > 0) v = v.add(p.getLookAngle().scale(0.12));             // fly where she is looking
            else if (p.zza < 0) v = v.scale(0.6);
            this.setDeltaMovement(v);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
        super.travel(input);
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.species == GirlSpecies.JORMUNGANDR_DRAGON && source.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.3F;   // arrows barely scratch her
        if (this.drowsy > 0) this.drowsy = 0;
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (!hit || !(target instanceof LivingEntity l)) return hit;
        switch (this.species) {
            case JORMUNGANDR_DRAGON -> l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
            case WYVERN_DRAGON -> {
                l.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                this.startTimedAction(Action.STING, 16);
            }
            case OROCHI_DRAGON -> {                                            // every head bites
                for (LivingEntity other : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0),
                        e -> e != this && e != target && (e instanceof Enemy || e == this.getLastHurtByMob()))) {
                    other.hurt(this.damageSources().mobAttack(this), 5.0F);
                }
            }
            default -> {
            }
        }
        return hit;
    }

    /** The wyrm's fire breath: a spray of small fireballs at her target. */
    public void breatheFire(ServerLevel level, LivingEntity target) {
        Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
        for (int i = 0; i < 3; i++) {
            Vec3 d = dir.add(this.random.nextGaussian() * 0.08, this.random.nextGaussian() * 0.05, this.random.nextGaussian() * 0.08);
            SmallFireball ball = new SmallFireball(level, this, d.scale(0.8));
            ball.setPos(this.getX() + dir.x, this.getEyeY() - 0.2, this.getZ() + dir.z);
            level.addFreshEntity(ball);
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX() + dir.x * 1.5, this.getEyeY(), this.getZ() + dir.z * 1.5, 20, 0.3, 0.2, 0.3, 0.05);
        this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.7F);
        if (net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this)) {          // fire on the ground around the target
            net.minecraft.core.BlockPos at = target.blockPosition();
            for (int i = 0; i < 3; i++) {
                net.minecraft.core.BlockPos p = at.offset(this.random.nextInt(3) - 1, 0, this.random.nextInt(3) - 1);
                for (int dy = 1; dy >= -2; dy--) {
                    net.minecraft.core.BlockPos q = p.above(dy);
                    if (level.getBlockState(q).isAir() && net.minecraft.world.level.block.BaseFireBlock.canBePlacedAt(level, q, net.minecraft.core.Direction.UP)) {
                        level.setBlockAndUpdate(q, net.minecraft.world.level.block.BaseFireBlock.getState(level, q));
                        break;
                    }
                }
            }
        }
        this.startTimedAction(Action.BREATHE, 30);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** The alebrije dyes a sheep near her in a bright colour (not the one it has). True if she did. */
    public boolean paintSheep(ServerLevel level) {
        java.util.List<net.minecraft.world.entity.animal.Sheep> sheep = level.getEntitiesOfClass(net.minecraft.world.entity.animal.Sheep.class,
                this.getBoundingBox().inflate(8), s -> !s.isSheared() && !s.isBaby());
        if (sheep.isEmpty()) return false;
        net.minecraft.world.entity.animal.Sheep s = sheep.get(this.random.nextInt(sheep.size()));
        net.minecraft.world.item.DyeColor c;
        do c = net.minecraft.world.item.DyeColor.byId(this.random.nextInt(16)); while (c == s.getColor());
        s.setColor(c);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, s.getX(), s.getY() + 1, s.getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        this.playSound(SoundEvents.DYE_USE, 1.0F, 1.0F);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.QUETZALCOATL_DRAGON && this.level() instanceof net.minecraft.server.level.ServerLevel dawn && !this.isBaby() && this.tickCount % 100 == 0
                && dawn.getDayTime() % 24000L < 1000L) this.morningStar(dawn);
        if (this.species == GirlSpecies.NENEKEA_DRAGON && this.level() instanceof net.minecraft.server.level.ServerLevel tide && !this.isBaby() && this.tickCount % 20 == 0) this.tidalBalm(tide);
        if (this.species == GirlSpecies.MACAWTL_DRAGON && this.level() instanceof net.minecraft.server.level.ServerLevel sky && !this.isBaby() && this.tickCount % 20 == 0) this.tailwind(sky);
        if (this.species == GirlSpecies.MOO_DRAGON && this.level() instanceof net.minecraft.server.level.ServerLevel frill && !this.isBaby() && this.tickCount % 20 == 0) this.herdStand(frill);
        if (this.species == GirlSpecies.HAIRYPTERIX_DRAGON && this.level() instanceof net.minecraft.server.level.ServerLevel wind && !this.isBaby() && this.tickCount % 10 == 0) this.updraft(wind);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.COCKATRICE_DRAGON && !this.isBaby() && this.tickCount % 60 == 0) this.glare(server);
        if (this.lair == null && this.onGround()) this.lair = this.blockPosition();
        if (this.species == GirlSpecies.ALEBRIJE_DRAGON && !this.isBaby() && this.tickCount % 1200 == 0
                && this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32) != null) this.paintSheep(server);
        if (!this.isVehicle() && this.isNoGravity() && !this.canFly()) this.setNoGravity(false);
        if (this.drowsy > 0) {
            this.drowsy--;
            this.setTarget(null);
            if (this.tickCount % 20 == 0) server.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 1, 0.3, 0.2, 0.3, 0);
        }
        LivingEntity t = this.getTarget();
        if (t != null && t.isAlive() && server.getGameTime() >= this.nextBreath) {
            double d = this.distanceToSqr(t);
            if (this.species == GirlSpecies.WYRM_DRAGON && d < 10 * 10 && d > 2 * 2 && this.hasLineOfSight(t)) {
                this.nextBreath = server.getGameTime() + BREATH_COOLDOWN;
                this.breatheFire(server, t);
            } else if (this.species == GirlSpecies.KANNAE_DRAGON && d < 16 * 16 && !(t instanceof Player)) {
                this.nextBreath = server.getGameTime() + 400;                  // lightning, never at a player
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
                if (bolt != null) {
                    bolt.moveTo(t.getX(), t.getY(), t.getZ());
                    bolt.setVisualOnly(false);
                    server.addFreshEntity(bolt);
                    this.startTimedAction(Action.CALL, 30);
                }
            }
        }
        if (this.tickCount % 41 == 0) this.gatherHoard(server);
        if (this.tickCount % 40 == 0) {
            Player p = this.bondedPlayer(60, 12);
            if (p != null) {
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false));
                if (this.getTarget() != null) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, true, false));
            }
        }
    }

    /** A dragon cannot leave gold lying about: she picks it up and puts it in her hoard. */
    private void gatherHoard(ServerLevel level) {
        ItemEntity item = this.nearest(ItemEntity.class, 8.0, e -> e.isAlive() && shiny(e.getItem()));
        if (item == null) return;
        if (this.distanceToSqr(item) > 2.5 * 2.5) {
            this.getNavigation().moveTo(item, 1.1);
            return;
        }
        this.stash(item.getItem().copy());
        item.discard();
        this.playSound(SoundEvents.ITEM_PICKUP, 0.6F, 0.8F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < this.hoard.getContainerSize(); i++) if (!this.hoard.getItem(i).isEmpty()) this.spawnAtLocation(this.hoard.getItem(i));
    }

    @Override
    protected int litterSize() {
        return 1;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.KANNAE_DRAGON ? null : SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.6F;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Hoard", this.hoard.createTag(this.registryAccess()));
        if (this.lair != null) tag.putLong("Lair", this.lair.asLong());
        tag.putLong("NextRain", this.nextRain);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hoard.fromTag(tag.getList("Hoard", 10), this.registryAccess());
        this.lair = tag.contains("Lair") ? BlockPos.of(tag.getLong("Lair")) : null;
        this.nextRain = tag.getLong("NextRain");
    }

    // --- the cockatrice -------------------------------------------------------------------------------------------

    /** Befriended: her glare stops a monster within 12 blocks of her and her friend almost dead (Slowness IV, 5 s). The monster, or null. */
    @Nullable
    public net.minecraft.world.entity.monster.Monster glare(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return null;
        var m = this.nearest(net.minecraft.world.entity.monster.Monster.class, 12,
                x -> x.distanceToSqr(friend) < 12 * 12 && this.hasLineOfSight(x) && !x.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
        if (m == null) return null;
        m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3), this);
        this.getLookControl().setLookAt(m);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH, m.getX(), m.getY() + 1, m.getZ(), 30, 0.4, 0.6, 0.4, 0.0);
        this.playSound(net.minecraft.sounds.SoundEvents.CHICKEN_AMBIENT, 1.2F, 0.6F);
        return m;
    }

    // --- the moo and the hairypterix (twentieth wave) ---------------------------------------------------------------

    /** With two or more monsters within 8 blocks of her friend, she and her friend stand together: Strength I for her friend for
     *  three seconds. True if they made a stand. */
    public boolean herdStand(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null) return false;
        if (level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8)).size() < 2) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, 0, true, true));
        return true;
    }

    /** Her friend within 12 blocks, falling more than three blocks, drifts down gently (Slow Falling for two seconds). True if
     *  she caught them on the wind. */
    public boolean updraft(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 12);
        if (friend == null || friend.fallDistance <= 3.0F) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 40, 0, true, true));
        return true;
    }

    // --- the nenekea (twenty-first wave) ----------------------------------------------------------------------------

    /** Her hurt friend within 8 blocks, standing in water, heals: Regeneration I for three seconds. True if she soothed them. */
    public boolean tidalBalm(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null || !friend.isInWater() || friend.getHealth() >= friend.getMaxHealth()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60, 0, true, true));
        return true;
    }

    // --- the twenty-second wave ---------------------------------------------------------------------------------------

    /** Her friend within 10 blocks of her, out under the open sky, runs faster (Speed I for three seconds). 1 if they got it, 0 if they
     *  are under a roof or too far, -1 without a friend. */
    public int tailwind(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        if (friend.distanceToSqr(this) > 10 * 10 || !level.canSeeSky(friend.blockPosition().above())) return 0;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, 0, true, true), this);
        return 1;
    }

    /** The last day Quetzalcoatl ripened the crops. */
    private long lastDawn = -1;

    /**
     * Quetzalcoatl's morning star (twenty-fourth wave): once a day, the crops within 6 of her friend grow. The number of crops, -1
     * without a friend (0 also when she has already done it today).
     */
    public int morningStar(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return -1;
        long day = level.getDayTime() / 24000L;
        if (day == this.lastDawn) return 0;
        this.lastDawn = day;
        int grown = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -2, -6), friend.blockPosition().offset(6, 2, 6))) {
            var state = level.getBlockState(p);
            if (state.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(state)) {
                crop.growCrops(level, p.immutable(), state);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
                grown++;
            }
        }
        return grown;
    }
}
