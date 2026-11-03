package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The plant family (flora_field_guide.html): alraune, matango, ent, cactus, mandragora. Rooted: standing still on soil she
 * heals and the crops around her grow faster (much faster once she is devoted). They want bone meal and water. The
 * alraune dusts attackers with pollen, the matango sinks into mycelium and comes up elsewhere (her spores sicken
 * attackers), the ent plants saplings and hits very hard, the cactus pricks whatever hits her, and the mandragora hides
 * in the ground until someone pulls her out, when she screams. The leshy (male, like the vodyanoy, so he never shares
 * a bed) keeps count of the trees each player plants and fells near him: planters get saplings and apples, clear-cutters
 * get lost in his fog. He bellows at any vodyanoy he meets. The venus flytrap snaps her trap shut on whatever steps into
 * reach (once befriended, only monsters), holding it fast and biting.
 */
public class Flora extends AbstractMonsterGirl {
    /** Nectar can be gathered once every half day. */
    public static final int NECTAR_COOLDOWN = 12000;
    private long nextNectar;

    /** Whether a bee could gather nectar from her right now (a grown flower that has nectar to spare). */
    public boolean hasNectar() {
        return !this.isBaby() && this.level().getGameTime() >= this.nextNectar;
    }

    /** Herself, curled up as a bloom to plant elsewhere (AlrauneBloomBlock lets her out again). */
    public ItemStack asBloom() {
        CompoundTag girl = new CompoundTag();
        this.saveWithoutId(girl);
        girl.remove("UUID");                                                // a fresh one when she unfolds (a copied bloom never clashes)
        CompoundTag data = new CompoundTag();
        data.put("Girl", girl);
        ItemStack bloom = new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.ALRAUNE_BLOOM.get());
        net.minecraft.world.item.BlockItem.setBlockEntityData(bloom, com.linkyoshi90.monstergirls.registry.ModBlockEntities.ALRAUNE_BLOOM.get(), data);
        return bloom;
    }

    /** A bee girl took her nectar. */
    public void giveNectar() {
        this.nextNectar = this.level().getGameTime() + NECTAR_COOLDOWN;
    }

    private int stillTicks;
    /** The leshy's tally per player: + for every sapling planted near him, - for every log cut. */
    private final java.util.Map<UUID, Integer> treeTally = new java.util.HashMap<>();
    private long nextBellow;
    public static final int PLANTER_REWARD = 6, CLEAR_CUT = -12;
    private int sunk;
    private long nextPlant;
    @Nullable
    private LivingEntity caught;
    private int holdTicks;
    private long nextSnap;

    public Flora(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.BONE_MEAL), false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return !Flora.this.isHidden() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case DRYAD_FLORA -> girlAttributes(24, 0.25, 3);
            case ALRAUNE_FLORA -> girlAttributes(30, 0.1, 3);
            case MATANGO_FLORA -> girlAttributes(20, 0.2, 2);
            case ENT_FLORA -> girlAttributes(80, 0.2, 14).add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.ATTACK_KNOCKBACK, 2.0);
            case CACTUS_FLORA -> girlAttributes(28, 0.18, 4).add(Attributes.ARMOR, 4);
            case LESHY_FLORA -> girlAttributes(60, 0.24, 6).add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
            case VENUS_FLYTRAP_FLORA -> girlAttributes(32, 0.1, 4).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
            case KESARAN_PASARAN_FLORA -> girlAttributes(8, 0.2, 1);
            default -> girlAttributes(10, 0.24, 1);
        };
    }

    public boolean isHidden() {
        return this.hasFlag(FLAG_HIDDEN);
    }

    public void setHidden(boolean on) {
        this.setFlag(FLAG_HIDDEN, on);
        if (on) this.getNavigation().stop();
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BONE_MEAL) || stack.is(Items.POTION)
                || this.species == GirlSpecies.VENUS_FLYTRAP_FLORA && (stack.is(Items.SPIDER_EYE) || stack.is(Items.ROTTEN_FLESH))
                || this.species == GirlSpecies.KESARAN_PASARAN_FLORA && stack.is(Items.SUGAR);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.POTION) && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        return stack.is(Items.BONE_MEAL) || this.species == GirlSpecies.CACTUS_FLORA ? 2 : 1;
    }

    /** Pull a buried mandragora out of the ground: she screams. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.KESARAN_PASARAN_FLORA && stack.is(Items.SUGAR) && !this.isBaby()) {   // sugar: she splits in two
            if (this.level() instanceof ServerLevel server) {
                stack.consume(1, player);
                this.addAffection(player, 1);
                this.hearts(server, 2);
                this.split(server);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.ALRAUNE_FLORA && stack.is(net.minecraft.tags.ItemTags.SHOVELS) && player.isShiftKeyDown() && !this.isBaby()) {
            if (this.getAffection(player) < BED_AFFECTION) return InteractionResult.PASS;   // only for someone she trusts
            if (!this.level().isClientSide) {
                ItemStack bloom = this.asBloom();
                if (!player.getInventory().add(bloom)) player.drop(bloom, false);
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                this.playSound(net.minecraft.sounds.SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 1.0F);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(Items.GLASS_BOTTLE) && !this.isBaby()) {                // nectar from her blossoms
            if (this.getAffection(player) < BED_AFFECTION || this.level().getGameTime() < this.nextNectar) return InteractionResult.PASS;
            if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
                this.nextNectar = server.getGameTime() + NECTAR_COOLDOWN;
                stack.consume(1, player);
                ItemStack nectar = new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.NECTAR_BOTTLE.get());
                if (!player.getInventory().add(nectar)) player.drop(nectar, false);
                this.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_FILL, 1.0F, 1.2F);
                this.hearts(server, 2);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.MANDRAGORA_FLORA && this.isHidden()) {
            if (this.level() instanceof ServerLevel server) {
                this.setHidden(false);
                this.scream(server, player);
                this.addAffection(player, 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    /** Everyone around but the one who pulled her up is stunned. */
    public void scream(ServerLevel level, @Nullable Player puller) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8), e -> e != this && e != puller)) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        }
        this.startTimedAction(Action.SCREAM, 24);
        this.playSound(SoundEvents.GHAST_SCREAM, 1.2F, 1.8F);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case ENT_FLORA -> new ItemStack(r < 50 ? Items.OAK_SAPLING : r < 75 ? Items.APPLE : Items.BIRCH_SAPLING, 2);
            case LESHY_FLORA -> new ItemStack(r < 40 ? Items.SPRUCE_SAPLING : r < 70 ? Items.SWEET_BERRIES : Items.MOSS_BLOCK, 2);
            case MATANGO_FLORA -> new ItemStack(r < 50 ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM, 2);
            case CACTUS_FLORA -> new ItemStack(r < 60 ? Items.CACTUS : Items.GREEN_DYE, 2);
            case MANDRAGORA_FLORA -> new ItemStack(r < 50 ? Items.POTATO : Items.CARROT, 3);
            case VENUS_FLYTRAP_FLORA -> new ItemStack(r < 50 ? Items.BONE_MEAL : r < 80 ? Items.SLIME_BALL : Items.SPIDER_EYE, r < 50 ? 3 : 1);
            case KESARAN_PASARAN_FLORA -> new ItemStack(r < 45 ? Items.DANDELION : r < 75 ? Items.SUGAR : r < 95 ? Items.WHITE_DYE : Items.RABBIT_FOOT, r < 95 ? 2 : 1);
            default -> new ItemStack(r < 40 ? Items.WHEAT_SEEDS : r < 80 ? Items.PINK_TULIP : Items.HONEY_BOTTLE, r < 40 ? 4 : 1);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) amount *= 2.0F;          // plants burn
        boolean hurt = super.hurt(source, amount);
        if (!hurt || !(this.level() instanceof ServerLevel server)) return hurt;
        if (source.getEntity() instanceof LivingEntity a && a.distanceToSqr(this) < 4 * 4 && !source.is(DamageTypes.THORNS)) {
            switch (this.species) {
                case ALRAUNE_FLORA -> {
                    a.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                    a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    server.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 1.5, this.getZ(), 30, 1, 0.6, 1, 0.01);
                    this.startTimedAction(Action.PUFF, 20);
                }
                case MATANGO_FLORA -> {
                    a.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                    a.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                    server.sendParticles(ParticleTypes.MYCELIUM, this.getX(), this.getY() + 1, this.getZ(), 40, 1, 0.6, 1, 0.01);
                    this.sinkAway(server);
                }
                case CACTUS_FLORA -> a.hurt(this.damageSources().thorns(this), 2.0F);
                case MANDRAGORA_FLORA -> this.scream(server, null);
                default -> {
                }
            }
        }
        return hurt;
    }

    /** The matango sinks into the mycelium and comes up somewhere else nearby. */
    public void sinkAway(ServerLevel level) {
        this.setHidden(true);
        this.sunk = 100;
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 20; i++) {
            BlockPos p = o.offset(this.random.nextInt(17) - 8, this.random.nextInt(5) - 2, this.random.nextInt(17) - 8);
            BlockState s = level.getBlockState(p.below());
            if ((s.is(Blocks.MYCELIUM) || s.is(Blocks.PODZOL) || s.is(BlockTags.DIRT)) && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                this.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                return;
            }
        }
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** On soil and standing still. */
    public boolean isRooted() {
        BlockState under = this.level().getBlockState(this.blockPosition().below());
        return this.stillTicks > 40 && (under.is(BlockTags.DIRT) || under.is(Blocks.FARMLAND) || under.is(BlockTags.SAND) || under.is(Blocks.MYCELIUM));
    }

    // --- Shiitake --------------------------------------------------------------------------------------------------

    private static final String REGROW = "ShiitakeRegrow";

    public boolean hasOffshoots() {
        return !this.hasFlag(FLAG_PRUNED);
    }

    /** Her friend near her is about to be hit: the offshoots take it. Whether they did. */
    public boolean takeBlow(ServerLevel level) {
        if (!this.hasOffshoots()) return false;
        this.setFlag(FLAG_PRUNED, true);
        this.getPersistentData().putLong(REGROW, level.getGameTime() + com.linkyoshi90.monstergirls.entity.Missing.DAY);
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 1.2, this.getZ(), 20, 0.5, 0.5, 0.5, 0.0);
        return true;
    }

    private void regrow(ServerLevel level) {
        if (this.hasFlag(FLAG_PRUNED) && level.getGameTime() >= this.getPersistentData().getLong(REGROW)) this.setFlag(FLAG_PRUNED, false);
    }

    /** The rose's thorns: whatever just hurt her friend near her takes 3 and is slowed. 1 on a prick, 0 with nobody to prick, -1 without a friend. */
    public int thorns() {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 10);
        if (friend == null) return -1;
        net.minecraft.world.entity.LivingEntity by = friend.getLastHurtByMob();
        if (by == null || !by.isAlive() || friend.tickCount - friend.getLastHurtByMobTimestamp() > 20) return 0;
        by.hurt(this.damageSources().thorns(this), 3);
        by.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        return 1;
    }

    private long lastBloom = -1;

    /** The lotus opens at dawn: once a day her friend near her loses every harmful effect and is fed. True if she bloomed. */
    public boolean dawnBloom(long day) {
        if (day == this.lastBloom) return false;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return false;
        this.lastBloom = day;
        for (net.minecraft.world.effect.MobEffectInstance e : java.util.List.copyOf(friend.getActiveEffects())) {
            if (e.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) friend.removeEffect(e.getEffect());
        }
        friend.getFoodData().eat(8, 0.6F);
        return true;
    }

    /** The sunflower: by day her friend near her gets a little food. True if she fed them. */
    public boolean solar(boolean day) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 10);
        if (friend == null || !day || friend.getFoodData().getFoodLevel() >= 20) return false;
        friend.getFoodData().eat(1, 0.2F);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.SUNFLOWER_FLORA && !this.level().isClientSide && !this.isBaby() && this.tickCount % 201 == 0) this.solar(this.level().isDay());
        if (this.species == GirlSpecies.LOTUS_FLORA && !this.level().isClientSide && !this.isBaby() && this.tickCount % 41 == 0 && this.level().getDayTime() % 24000L < 1000L) this.dawnBloom(this.level().getDayTime() / 24000L);
        if (this.species == GirlSpecies.ROSE_FLORA && !this.level().isClientSide && !this.isBaby() && this.tickCount % 11 == 0) this.thorns();
        if (this.species == GirlSpecies.DRYAD_FLORA && this.level() instanceof ServerLevel grove && !this.isBaby() && this.tickCount % 600 == 0) this.nurture(grove);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.SHIITAKE_FLORA && this.tickCount % 100 == 0) this.regrow(server);
        boolean still = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && this.getTarget() == null;
        this.stillTicks = still ? this.stillTicks + 1 : 0;
        if (this.sunk > 0 && --this.sunk == 0) this.setHidden(false);
        if (this.species == GirlSpecies.MANDRAGORA_FLORA && !this.isBaby() && this.stillTicks > 200 && !this.isHidden() && this.bondedPlayer(10, 16) == null) {
            this.setHidden(true);                                            // she burrows back in, leaves showing
        }
        if (this.isHidden()) this.getNavigation().stop();
        if (this.tickCount % 40 == 0 && this.isRooted()) {
            if (this.getHealth() < this.getMaxHealth()) this.heal(1.0F);
            int tries = this.bondedPlayer(60, 32) != null ? 6 : 2;           // devoted: the farm fairly leaps up
            for (int i = 0; i < tries; i++) {
                BlockPos p = this.blockPosition().offset(this.random.nextInt(13) - 6, this.random.nextInt(3) - 1, this.random.nextInt(13) - 6);
                BlockState s = server.getBlockState(p);
                if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s)) {
                    server.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                    server.levelEvent(2005, p, 0);
                }
            }
        }
        if (server.getGameTime() >= this.nextPlant && this.tickCount % 41 == 0) {
            if (this.species == GirlSpecies.ENT_FLORA) this.plantSapling(server);
            if (this.species == GirlSpecies.ALRAUNE_FLORA) this.plant(server, Blocks.PINK_TULIP);
        }
        if (this.species == GirlSpecies.LESHY_FLORA && this.tickCount % 40 == 0) this.bellowAtRival(server);
        if (this.species == GirlSpecies.VENUS_FLYTRAP_FLORA && !this.isBaby()) this.tickTrap(server);
        if (this.isPuff()) this.tickDrift(server);
    }

    // --- the kesaran pasaran -------------------------------------------------------------------------------------

    public static final int MAX_PUFFS = 3;

    public boolean isPuff() {
        return this.species == GirlSpecies.KESARAN_PASARAN_FLORA;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !this.isPuff() && super.causeFallDamage(distance, multiplier, source);
    }

    /** Which way the wind blows: the same for every puff in the world, turning slowly over the day. */
    public static net.minecraft.world.phys.Vec3 wind(long time) {
        double a = time / 2400.0 * Math.PI * 2 + Math.sin(time / 700.0);
        return new net.minecraft.world.phys.Vec3(Math.cos(a), 0, Math.sin(a));
    }

    /** Off the ground, carried by the wind 2 to 5 blocks up; befriended, she drifts after her friend instead. */
    private void tickDrift(ServerLevel level) {
        this.setNoGravity(true);
        this.getNavigation().stop();
        net.minecraft.world.phys.Vec3 want = wind(level.getGameTime()).scale(0.045);
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        int ground = 0;
        while (ground < 8 && level.getBlockState(this.blockPosition().below(ground + 1)).getCollisionShape(level, this.blockPosition().below(ground + 1)).isEmpty()) {
            ground++;
        }
        double up = ground < 2 ? 0.04 : ground > 5 ? -0.025 : 0.012 * Math.sin(this.tickCount / 20.0);
        if (friend != null && this.distanceToSqr(friend) > 3 * 3) {
            net.minecraft.world.phys.Vec3 to = friend.position().add(0, 1.5, 0).subtract(this.position());
            want = to.normalize().scale(0.09);
            up = Math.max(up, want.y);
        }
        if (this.isOrderedToSit()) want = net.minecraft.world.phys.Vec3.ZERO;
        if (this.horizontalCollision) up = 0.06;                         // up and over whatever she bumps into
        net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x + (want.x - v.x) * 0.1, v.y + (up - v.y) * 0.2, v.z + (want.z - v.z) * 0.1);
        if (v.horizontalDistanceSqr() > 1.0E-4) this.setYRot(this.yBodyRot = (float) (Math.atan2(-v.x, v.z) * 180.0 / Math.PI));
        if (friend != null && this.tickCount % 40 == 0 && this.distanceToSqr(friend) < 8 * 8) {
            friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 100, 0, true, false));
        }
    }

    /** A second puff of her drifts off (no more than three of them within 16 blocks). True if she split. */
    public boolean split(ServerLevel level) {
        if (level.getEntitiesOfClass(Flora.class, this.getBoundingBox().inflate(16), Flora::isPuff).size() >= MAX_PUFFS) return false;
        if (!(this.getType().create(level) instanceof Flora twin)) return false;
        twin.moveTo(this.getX() + 0.4, this.getY() + 0.2, this.getZ(), this.getYRot(), 0.0F);
        twin.setVariant(this.getVariantName());
        level.addFreshEntity(twin);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, this.getX(), this.getY() + 1, this.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        this.playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.6F);
        return true;
    }

    // --- the venus flytrap ----------------------------------------------------------------------------------------

    /** What her trap closes on: monsters always; while she is wild, any animal or stranger too. Never girls, villagers or pets. */
    public boolean snapsAt(LivingEntity e) {
        if (!e.isAlive() || e == this || e instanceof com.linkyoshi90.monstergirls.entity.Bonded || e instanceof net.minecraft.world.entity.npc.AbstractVillager
                || e instanceof net.minecraft.world.entity.animal.IronGolem || e instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame()) return false;
        if (e instanceof net.minecraft.world.entity.monster.Enemy) return true;
        boolean wild = this.bestAffection() < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
        if (e instanceof Player p) return wild && !p.isCreative() && !p.isSpectator() && this.getAffection(p) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
        return wild && e instanceof net.minecraft.world.entity.animal.Animal;
    }

    @Nullable
    public LivingEntity getCaught() {
        return this.caught;
    }

    /** Snap: she holds what she caught for three seconds, biting every second, then lets it go and opens again. */
    private void tickTrap(ServerLevel level) {
        if (this.caught != null) {
            if (!this.caught.isAlive() || --this.holdTicks <= 0 || this.caught.distanceToSqr(this) > 4 * 4) {
                this.caught = null;
                this.nextSnap = level.getGameTime() + 60;
                return;
            }
            this.caught.setDeltaMovement(0, Math.min(0, this.caught.getDeltaMovement().y), 0);
            this.caught.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 9, true, false));
            if (this.holdTicks % 20 == 0) {
                this.caught.hurt(this.damageSources().mobAttack(this), 3.0F);
                this.playSound(net.minecraft.sounds.SoundEvents.FOX_BITE, 0.8F, 0.7F);
            }
            return;
        }
        if (this.tickCount % 5 != 0 || level.getGameTime() < this.nextSnap || this.isHidden()) return;
        LivingEntity victim = this.nearest(LivingEntity.class, 1.6, this::snapsAt);
        if (victim == null) return;
        this.caught = victim;
        this.holdTicks = 60;
        this.getLookControl().setLookAt(victim);
        this.startTimedAction(Action.SNAP, 12);
        this.playSound(net.minecraft.sounds.SoundEvents.EVOKER_FANGS_ATTACK, 1.0F, 1.3F);
    }

    private void plantSapling(ServerLevel level) {
        Block sapling = switch (this.getVariantName()) {
            case "birch" -> Blocks.BIRCH_SAPLING;
            case "spruce" -> Blocks.SPRUCE_SAPLING;
            case "cherry" -> Blocks.CHERRY_SAPLING;
            case "autumn" -> Blocks.DARK_OAK_SAPLING;
            default -> Blocks.OAK_SAPLING;
        };
        if (this.plant(level, sapling)) this.startTimedAction(Action.PLANT, 30);
    }

    private boolean plant(ServerLevel level, Block block) {
        this.nextPlant = level.getGameTime() + 2400 + this.random.nextInt(2400);
        for (int i = 0; i < 12; i++) {
            BlockPos p = this.blockPosition().offset(this.random.nextInt(9) - 4, 0, this.random.nextInt(9) - 4);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(p, block.defaultBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    // --- the leshy ----------------------------------------------------------------------------------------------

    public int getTreeTally(Player player) {
        return this.treeTally.getOrDefault(player.getUUID(), 0);
    }

    /** A sapling planted near him: the planter gets saplings and apples now and then. */
    public void onSaplingPlanted(ServerLevel level, Player player) {
        int t = this.treeTally.merge(player.getUUID(), 1, Integer::sum);
        this.getLookControl().setLookAt(player);
        this.hearts(level, 1);
        if (t >= PLANTER_REWARD) {
            this.treeTally.put(player.getUUID(), 0);
            this.addAffection(player, 2);
            this.startTimedAction(Action.PLANT, 30);
            this.spawnAtLocation(new ItemStack(Items.SPRUCE_SAPLING, 2));
            this.spawnAtLocation(new ItemStack(Items.APPLE, 2));
            player.displayClientMessage(Component.translatable("message.monstergirls.leshy_thanks", this.getDisplayName()), true);
        }
    }

    /** A log cut near him: a clear-cutter gets lost in his fog. */
    public void onLogCut(ServerLevel level, Player player) {
        int t = this.treeTally.merge(player.getUUID(), -1, Integer::sum);
        this.getLookControl().setLookAt(player);
        if (t > CLEAR_CUT) return;
        this.treeTally.put(player.getUUID(), CLEAR_CUT / 2);           // a second warning comes sooner
        this.addAffection(player, -5);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 30, 2, 1, 2, 0.01);
        this.startTimedAction(Action.SCREAM, 24);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.6F);
        player.displayClientMessage(Component.translatable("message.monstergirls.leshy_fog", this.getDisplayName()), true);
    }

    /** The leshy nearest a block that just changed, or null. */
    @Nullable
    public static Flora leshyNear(Level level, BlockPos pos) {
        Flora best = null;
        for (Flora f : level.getEntitiesOfClass(Flora.class, new net.minecraft.world.phys.AABB(pos).inflate(24),
                f -> f.isAlive() && f.species == GirlSpecies.LESHY_FLORA)) {
            if (best == null || f.distanceToSqr(pos.getCenter()) < best.distanceToSqr(pos.getCenter())) best = f;
        }
        return best;
    }

    /** Two lords of the wild who cannot stand each other: he bellows, the vodyanoy croaks back. */
    private void bellowAtRival(ServerLevel level) {
        if (level.getGameTime() < this.nextBellow) return;
        com.linkyoshi90.monstergirls.entity.Vodyanoy v = this.nearest(com.linkyoshi90.monstergirls.entity.Vodyanoy.class, 12, x -> x.isAlive());
        if (v == null) return;
        this.nextBellow = level.getGameTime() + 600 + this.random.nextInt(600);
        this.getLookControl().setLookAt(v);
        v.getLookControl().setLookAt(this);
        this.startTimedAction(Action.SCREAM, 24);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.2F, 0.7F);
        v.playSound(SoundEvents.FROG_AMBIENT, 2.0F, 0.5F);
    }

    @Override
    public boolean canShareBed() {
        return this.species != GirlSpecies.LESHY_FLORA && this.species != GirlSpecies.SHIITAKE_FLORA && super.canShareBed();
    }

    public void setNextPlant(long t) {
        this.nextPlant = t;
    }

    // --- young ---------------------------------------------------------------------------------------------------

    /** A seed in the soil (a clutch block) that sprouts into a seedling. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-3, -1, -3), o.offset(3, 1, 3))) {
            if (!canHoldBlock(level, p) || !level.getBlockState(p.below()).is(BlockTags.DIRT)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.LEATHERY), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), 1, father).withPaint(this.getPaint());
            return true;
        }
        return super.giveBirth(level, father);
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isHidden() ? null : SoundEvents.AZALEA_LEAVES_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AZALEA_LEAVES_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AZALEA_BREAK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.BONE_MEAL_USE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextPlant", this.nextPlant);
        tag.putBoolean("Buried", this.isHidden());
        CompoundTag tally = new CompoundTag();
        this.treeTally.forEach((u, t) -> tally.putInt(u.toString(), t));
        tag.put("TreeTally", tally);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextPlant = tag.getLong("NextPlant");
        this.setFlag(FLAG_HIDDEN, tag.getBoolean("Buried"));
        this.treeTally.clear();
        CompoundTag tally = tag.getCompound("TreeTally");
        for (String k : tally.getAllKeys()) this.treeTally.put(UUID.fromString(k), tally.getInt(k));
    }

    @Override
    public boolean isHealer() {
        return true;
    }

    // --- the dryad -----------------------------------------------------------------------------------------------

    /** The saplings within 8 blocks of her friend grow as if given bone meal. How many, or -1 without a friend. */
    public int nurture(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-8, -3, -8), friend.blockPosition().offset(8, 3, 8))) {
            var st = level.getBlockState(p);
            if (st.is(net.minecraft.tags.BlockTags.SAPLINGS) && st.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock b && b.isValidBonemealTarget(level, p, st)) {
                net.minecraft.core.BlockPos at = p.immutable();
                b.performBonemeal(level, this.random, at, st);
                level.levelEvent(1505, at, 15);                                    // the bone meal sparkles
                n++;
            }
        }
        return n;
    }
}
