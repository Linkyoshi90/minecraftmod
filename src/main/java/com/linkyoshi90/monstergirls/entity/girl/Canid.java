package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The canid family, with the bears (canid_field_guide.html): wolf, polar bear, panda, Anubis, kobold, grizzly. Kin to the beasts of the
 * vanilla world. The wolf runs with a pack and its alpha, hunts sheep and rabbits while wild and leads her player's
 * tamed wolves into her fights. The polar bear hunts seal mermaids and penguin harpies, fishes at open water and guards
 * her cubs. The panda is born with a personality, like vanilla pandas. Anubis weighs the heart of every player who comes
 * near: those who killed villagers or iron golems are condemned and hunted, and only light hearts may open the tomb
 * chests she guards. The kobold sniffs out ore, warns of loose sand and gravel and smells mimics; the grizzly fishes
 * salmon, raids full beehives and hibernates while it snows.
 */
public class Canid extends AbstractMonsterGirl {
    /** The panda's nature, like vanilla pandas' genes ("brown" is a colour variant here). */
    public enum Personality { NORMAL, LAZY, WORRIED, PLAYFUL, AGGRESSIVE, WEAK }

    private boolean alpha;
    private Personality personality = Personality.NORMAL;
    private long nextHowl;
    private long nextFish;
    private long nextRaid;
    /** The kobold: the last ore she pointed out and the last loose ceiling she warned of, so she does not repeat herself. */
    @Nullable
    private BlockPos lastOre, lastCaveIn;
    /** Anubis: the players she has weighed, and those she found heavy. */
    private final Set<UUID> weighed = new HashSet<>();
    private final Set<UUID> condemned = new HashSet<>();

    public Canid(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Canid.this.startTimedAction(Action.POUNCE, 12);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, switch (species) {
            case WOLF_CANID -> Ingredient.of(Items.BONE);
            case PANDA_CANID -> Ingredient.of(Items.BAMBOO, Items.CAKE);
            case POLAR_BEAR_CANID -> Ingredient.of(ItemTags.FISHES);
            case KOBOLD_CANID -> Ingredient.of(Items.BONE, Items.COOKED_BEEF, Items.COOKED_PORKCHOP);
            case GRIZZLY_CANID -> Ingredient.of(Items.SALMON, Items.COOKED_SALMON, Items.HONEY_BOTTLE, Items.HONEYCOMB);
            case BLACK_BEAR_CANID -> Ingredient.of(Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GLOW_BERRIES);
            default -> Ingredient.of(Items.BREAD, Items.HONEY_BOTTLE);
        }, false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::packLeader, 1.1, 10.0F, 4.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        switch (species) {
            case WOLF_CANID -> {                                         // a wild pack hunts sheep and rabbits
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Sheep.class, 40, true, false, s -> this.isWild()));
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, 40, true, false, r -> this.isWild()));
            }
            case POLAR_BEAR_CANID -> this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 20, true, false, this::isPolarPrey));
            case BLACK_BEAR_CANID -> this.goalSelector.addGoal(1, new com.linkyoshi90.monstergirls.entity.ai.ClimbGoal(this, 120, this::isScared,
                    s -> s.is(BlockTags.LOGS), 200));                      // up a tree now and then, and straight up one when scared
            case PANDA_CANID -> {
                this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.3,
                        e -> this.personality == Personality.WORRIED && e instanceof Player p && this.getAffection(p) < Orders.AFFECTION && !p.isSpectator()));
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true, m -> {   // the aggressive ones guard their friends
                    Player p = this.bondedPlayer(Orders.AFFECTION, 16);
                    return this.personality == Personality.AGGRESSIVE && p != null && m.distanceToSqr(p) < 8 * 8;
                }));
            }
            default -> {
            }
        }
    }

    // --- the cu sith and the yeti ------------------------------------------------------------------------------------

    private int barks;

    /** Monsters near her friend: she barks; at the third bark they flee. True on the third bark. */
    public boolean bark(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        java.util.List<net.minecraft.world.entity.monster.Monster> near = friend == null ? java.util.List.of()
                : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(10));
        if (near.isEmpty()) {
            this.barks = 0;
            return false;
        }
        this.playSound(SoundEvents.WOLF_GROWL, 1.6F, 0.6F);
        if (++this.barks < 3) return false;
        this.barks = 0;
        for (net.minecraft.world.entity.monster.Monster m : near) {
            m.setTarget(null);
            net.minecraft.world.phys.Vec3 away = m.position().add(m.position().subtract(friend.position()).normalize().scale(14));
            m.getNavigation().moveTo(away.x, away.y, away.z, 1.5);
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 0));
        }
        this.startTimedAction(Action.HOWL, 30);
        return true;
    }

    /** A snowball at the nearest monster within 12 blocks of her friend. True if she threw one. */
    public boolean throwSnowball(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return false;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class,
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(16), this, this.getX(), this.getY(), this.getZ(),
                friend.getBoundingBox().inflate(12));
        if (m == null || !this.hasLineOfSight(m)) return false;
        net.minecraft.world.entity.projectile.Snowball ball = new net.minecraft.world.entity.projectile.Snowball(level, this);
        double dx = m.getX() - this.getX(), dy = m.getEyeY() - 0.5 - ball.getY(), dz = m.getZ() - this.getZ();
        ball.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15, dz, 1.6F, 2.0F);
        level.addFreshEntity(ball);
        this.getLookControl().setLookAt(m);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.8F);
        return true;
    }

    /** Now and then, befriended and on snow, she builds a snow golem (no more than two near her). True if she built one. */
    public boolean buildSnowGolem(ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32) == null) return false;
        var under = level.getBlockState(this.blockPosition().below());
        if (!under.is(net.minecraft.tags.BlockTags.SNOW) && !level.getBlockState(this.blockPosition()).is(net.minecraft.world.level.block.Blocks.SNOW)) return false;
        if (level.getEntitiesOfClass(net.minecraft.world.entity.animal.SnowGolem.class, this.getBoundingBox().inflate(16)).size() >= 2) return false;
        net.minecraft.world.entity.animal.SnowGolem golem = EntityType.SNOW_GOLEM.create(level);
        if (golem == null) return false;
        golem.moveTo(this.getX() + 1.5, this.getY(), this.getZ(), this.getYRot(), 0.0F);
        level.addFreshEntity(golem);
        this.playSound(SoundEvents.SNOW_PLACE, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean canFreeze() {
        return this.species != GirlSpecies.YETI_CANID && super.canFreeze();
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case AMAROK_CANID -> girlAttributes(40, 0.33, 8);
            case WOLF_CANID -> girlAttributes(24, 0.32, 5);
            case POLAR_BEAR_CANID -> girlAttributes(40, 0.27, 7);
            case PANDA_CANID -> girlAttributes(30, 0.22, 4);
            case KOBOLD_CANID -> girlAttributes(18, 0.3, 3);
            case GRIZZLY_CANID -> girlAttributes(44, 0.27, 8);
            case BLACK_BEAR_CANID -> girlAttributes(34, 0.29, 6);
            case MELONCOLLIE_CANID -> girlAttributes(20, 0.32, 3);
            case CU_SITH_CANID -> girlAttributes(36, 0.33, 6);
            case YETI_CANID -> girlAttributes(50, 0.27, 8).add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.6);
            default -> girlAttributes(40, 0.3, 8);                       // Anubis
        };
    }

    /** Wild: nobody has befriended her yet. */
    public boolean isWild() {
        return this.bestAffection() < Orders.AFFECTION;
    }

    // --- the pack ------------------------------------------------------------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (!(group instanceof PackData)) {
            this.alpha = true;                                          // the first of a pack leads it
            group = new PackData();
        }
        if (this.species == GirlSpecies.PANDA_CANID) this.setPersonality(Personality.values()[this.random.nextInt(Personality.values().length)]);
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    private static class PackData implements SpawnGroupData {
    }

    public boolean isAlpha() {
        return this.alpha;
    }

    @Nullable
    private LivingEntity packLeader() {
        if (this.isBaby()) return this.mother();
        if (this.species != GirlSpecies.WOLF_CANID || this.alpha) return null;
        return this.nearest(Canid.class, 32, c -> c.species == GirlSpecies.WOLF_CANID && c.isAlpha() && !c.isBaby());
    }

    // --- the polar bear ------------------------------------------------------------------------------------------

    /** Seal mermaids and penguin harpies that nobody has befriended, while she is wild herself. */
    public boolean isPolarPrey(LivingEntity e) {
        if (!this.isWild()) return false;
        if (e instanceof PenguinHarpy p) return !p.isBefriended();
        if (com.linkyoshi90.monstergirls.compat.AlexsMobs.is(e, "seal")) return true;
        return e instanceof Merfolk m && m.getSpecies() == GirlSpecies.SEAL_MERMAID && !m.hasAnyAffection();
    }

    /** A wild bear goes for any stranger who comes near her cubs. */
    private boolean guardsCubFrom(Player player) {
        if ((this.species != GirlSpecies.POLAR_BEAR_CANID && this.species != GirlSpecies.GRIZZLY_CANID) || this.getAffection(player) >= Orders.AFFECTION) return false;
        return this.nearest(Canid.class, 12, c -> c.isBaby() && c.getType() == this.getType() && c.distanceToSqr(player) < 8 * 8) != null;
    }

    /** At open water she crouches, waits, and scoops out a fish: for her friend if she has one, else for herself. */
    private void fish(ServerLevel level) {
        if (this.isBaby() || this.getTarget() != null || this.isInWater() || level.getGameTime() < this.nextFish) return;
        BlockPos o = this.blockPosition();
        boolean water = false;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-2, -1, -2), o.offset(2, 0, 2))) {
            if (level.getFluidState(p).is(FluidTags.WATER) && level.getFluidState(p).isSource()) {
                water = true;
                break;
            }
        }
        if (!water) return;
        this.nextFish = level.getGameTime() + 1200 + this.random.nextInt(1200);
        this.startTimedAction(Action.FISH, 60);
        this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.6F, 1.0F);
        boolean salmon = this.species == GirlSpecies.GRIZZLY_CANID || this.random.nextBoolean();   // the grizzly catches salmon
        if (this.bondedPlayer(Orders.AFFECTION, 24) != null) this.spawnAtLocation(new ItemStack(salmon ? Items.SALMON : Items.COD));
        else this.heal(4.0F);
    }

    // --- the kobold ----------------------------------------------------------------------------------------------

    /** The nearest diamond, gold or emerald ore within reach of her nose, or null. */
    @Nullable
    public BlockPos sniffOre(ServerLevel level) {
        BlockPos o = this.blockPosition(), best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-10, -8, -10), o.offset(10, 8, 10))) {
            BlockState s = level.getBlockState(p);
            if ((s.is(BlockTags.DIAMOND_ORES) || s.is(BlockTags.GOLD_ORES) || s.is(BlockTags.EMERALD_ORES))
                    && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    /** She points her friend at the nearest valuable ore she smells (once per ore). */
    public void pointOutOre(ServerLevel level, Player friend) {
        BlockPos ore = this.sniffOre(level);
        if (ore == null || ore.equals(this.lastOre)) return;
        this.lastOre = ore;
        this.getNavigation().stop();
        this.getLookControl().setLookAt(ore.getX() + 0.5, ore.getY() + 0.5, ore.getZ() + 0.5);
        this.startTimedAction(Action.POINT, 30);
        this.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.4F);
        friend.displayClientMessage(Component.translatable("message.monstergirls.kobold_ore", this.getDisplayName(),
                level.getBlockState(ore).getBlock().getName(), (int) Math.sqrt(ore.distSqr(this.blockPosition()))), true);
    }

    /** The block her friend is looking at holds up sand or gravel: she yelps a warning. */
    public void warnOfCaveIn(ServerLevel level, Player friend) {
        if (!(friend.pick(5.0, 1.0F, false) instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos above = hit.getBlockPos().above();
        if (!(level.getBlockState(above).getBlock() instanceof FallingBlock) || above.equals(this.lastCaveIn)) return;
        this.lastCaveIn = above;
        this.startTimedAction(Action.POINT, 20);
        this.playSound(SoundEvents.WOLF_WHINE, 1.2F, 1.5F);
        friend.displayClientMessage(Component.translatable("message.monstergirls.kobold_cave_in", this.getDisplayName()), true);
    }

    /** A mimic near her: the chest rattles as she growls at it. */
    public void smellMimics(ServerLevel level) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-6, -3, -6), o.offset(6, 3, 6))) {
            if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity && SlimeGirl.isMimicChest(level, p)) {
                this.getLookControl().setLookAt(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                this.playSound(SoundEvents.WOLF_GROWL, 0.8F, 1.4F);
                level.playSound(null, p, SoundEvents.CHEST_LOCKED, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 0.6F);
                level.sendParticles(ParticleTypes.SMOKE, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.01);
                return;
            }
        }
    }

    // --- the black bear -------------------------------------------------------------------------------------------

    private long scaredUntil;

    public boolean isScared() {
        return this.scaredUntil > this.level().getGameTime();
    }

    /** Something startled her: she drops what she holds and runs from it for a few seconds. */
    public void scare(ServerLevel level, net.minecraft.world.phys.Vec3 from) {
        this.scaredUntil = level.getGameTime() + 100;
        this.setTarget(null);
        if (!this.getMainHandItem().isEmpty()) {
            this.spawnAtLocation(this.getMainHandItem());
            this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        net.minecraft.world.phys.Vec3 away = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPosAway(this, 16, 7, from);
        if (away != null) this.getNavigation().moveTo(away.x, away.y, away.z, 1.6);
        this.playSound(SoundEvents.POLAR_BEAR_WARNING, 0.8F, 1.6F);
    }

    /** A loud noise near black bears (an explosion, a bell, a goat horn) sends them running; once befriended, only explosions do. */
    public static void startle(ServerLevel level, net.minecraft.world.phys.Vec3 at, boolean explosion) {
        for (Canid c : level.getEntitiesOfClass(Canid.class, new net.minecraft.world.phys.AABB(at, at).inflate(16),
                c -> c.species == GirlSpecies.BLACK_BEAR_CANID && c.isAlive() && (explosion || c.isWild()))) {
            c.scare(level, at);
        }
    }

    /** A bear climbing down the hard way is not hurt by it. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.BLACK_BEAR_CANID && super.causeFallDamage(distance, multiplier, source);
    }

    /** She climbs tree trunks (and any wall she runs into). */
    @Override
    public boolean onClimbable() {
        return this.species == GirlSpecies.BLACK_BEAR_CANID && this.horizontalCollision || super.onClimbable();
    }

    /** A ripe berry bush near her: she goes to it and eats it back down to bare. */
    public void raidBerries(ServerLevel level) {
        if (this.getTarget() != null || this.isBaby()) return;
        BlockPos o = this.blockPosition(), bush = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, -2, -8), o.offset(8, 2, 8))) {
            BlockState s = level.getBlockState(p);
            if (s.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH) && s.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) >= 2) {
                bush = p.immutable();
                break;
            }
        }
        if (bush == null) return;
        if (this.distanceToSqr(bush.getCenter()) > 2.5 * 2.5) {
            this.getNavigation().moveTo(bush.getX() + 0.5, bush.getY(), bush.getZ() + 0.5, 1.0);
            return;
        }
        BlockState s = level.getBlockState(bush);
        level.setBlock(bush, s.setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 1), 2);
        this.startTimedAction(Action.FISH, 30);
        this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
        this.heal(3.0F);
        if (this.bondedPlayer(Orders.AFFECTION, 16) != null) this.spawnAtLocation(new ItemStack(Items.SWEET_BERRIES, 2));
    }

    // --- the grizzly ---------------------------------------------------------------------------------------------

    /** She hibernates while it snows where she stands, unless something wakes her. */
    public boolean isHibernating() {
        return this.species == GirlSpecies.GRIZZLY_CANID && !this.isBaby() && this.getTarget() == null
                && this.level().getGameTime() >= this.getWokenUntil() && this.level().isRaining()
                && this.level().getBiome(this.blockPosition()).value().getPrecipitationAt(this.blockPosition()) == Biome.Precipitation.SNOW;
    }

    /** A full beehive near her: she goes to it, scoops out the honey and gets stung for it. */
    public void raidHive(ServerLevel level) {
        if (this.getTarget() != null || level.getGameTime() < this.nextRaid) return;
        BlockPos o = this.blockPosition(), hive = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, -3, -8), o.offset(8, 4, 8))) {
            BlockState s = level.getBlockState(p);
            if (s.is(BlockTags.BEEHIVES) && s.hasProperty(BeehiveBlock.HONEY_LEVEL) && s.getValue(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.MAX_HONEY_LEVELS) {
                hive = p.immutable();
                break;
            }
        }
        if (hive == null) return;
        if (this.distanceToSqr(hive.getX() + 0.5, hive.getY(), hive.getZ() + 0.5) > 2.5 * 2.5) {
            this.getNavigation().moveTo(hive.getX() + 0.5, hive.getY(), hive.getZ() + 0.5, 1.0);
            return;
        }
        this.nextRaid = level.getGameTime() + 2400;
        BlockState s = level.getBlockState(hive);
        level.setBlock(hive, s.setValue(BeehiveBlock.HONEY_LEVEL, 0), 3);
        if (level.getBlockEntity(hive) instanceof BeehiveBlockEntity box) box.emptyAllLivingFromHive(null, s, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        for (Bee bee : level.getEntitiesOfClass(Bee.class, this.getBoundingBox().inflate(10))) {
            bee.setPersistentAngerTarget(this.getUUID());
            bee.startPersistentAngerTimer();
            bee.setTarget(this);
        }
        this.startTimedAction(Action.FISH, 40);              // a paw into the hive
        this.playSound(SoundEvents.BEEHIVE_SHEAR, 1.0F, 0.8F);
        if (this.bondedPlayer(Orders.AFFECTION, 24) != null) this.spawnAtLocation(new ItemStack(Items.HONEYCOMB, 3));
        else this.heal(6.0F);
    }

    // --- the panda -----------------------------------------------------------------------------------------------

    public Personality getPersonality() {
        return this.personality;
    }

    public void setPersonality(Personality personality) {
        this.personality = personality;
        var health = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (health != null && this.species == GirlSpecies.PANDA_CANID) health.setBaseValue(personality == Personality.WEAK ? 20.0 : 30.0);   // a weak panda is frail
    }

    /** She sneezes now and then (a weak panda often), and a slimeball comes out. */
    public void sneeze(ServerLevel level) {
        this.startTimedAction(Action.SNEEZE, 20);
        this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);
        this.spawnAtLocation(new ItemStack(Items.SLIME_BALL));
        level.sendParticles(ParticleTypes.SNEEZE, this.getX(), this.getEyeY(), this.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
    }

    @Override
    public float getSpeed() {
        return this.personality == Personality.LAZY ? super.getSpeed() * 0.6F : super.getSpeed();
    }

    private void tickPanda(ServerLevel level) {
        int sneezeOdds = this.personality == Personality.WEAK ? 1200 : 6000;
        if (this.random.nextInt(sneezeOdds) == 0) this.sneeze(level);
        if (this.tickCount % 40 != 0 || this.getTarget() != null || this.getAction() != Action.NONE || this.isOrderedToSit()) return;
        if (this.personality == Personality.LAZY && this.getNavigation().isDone() && this.random.nextInt(4) == 0) this.startTimedAction(Action.LYING, 300);
        if (this.personality == Personality.PLAYFUL && this.random.nextInt(8) == 0) this.startTimedAction(Action.ROLL, 20);
    }

    // --- Anubis --------------------------------------------------------------------------------------------------

    /** How heavy a player's heart is: the villagers and iron golems they have killed. */
    public static int heartWeight(ServerPlayer player) {
        return player.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER))
                + player.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.IRON_GOLEM));
    }

    /** She weighs a player she has not judged yet: a heavy heart is condemned (and hunted), a light one may pass. */
    public void weigh(ServerPlayer player) {
        if (!this.weighed.add(player.getUUID())) return;
        boolean heavy = heartWeight(player) > 0;
        if (heavy) this.condemned.add(player.getUUID());
        this.startTimedAction(Action.JUDGE, 40);
        this.getLookControl().setLookAt(player);
        this.playSound(heavy ? SoundEvents.WOLF_GROWL : SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, heavy ? 0.7F : 1.2F);
        player.displayClientMessage(Component.translatable(heavy ? "message.monstergirls.heart_heavy" : "message.monstergirls.heart_light",
                this.getDisplayName()), false);
    }

    public boolean hasWeighed(Player player) {
        return this.weighed.contains(player.getUUID());
    }

    public boolean isCondemned(Player player) {
        return this.condemned.contains(player.getUUID());
    }

    /**
     * Whether an Anubis keeps this player from a tomb chest here: an unopened structure chest within 12 blocks of an
     * Anubis who has not weighed the player light. Player chests are never hers to guard.
     */
    @Nullable
    public static Canid tombGuardOf(Level level, BlockPos pos, Player player) {
        if (player.isCreative() || !(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity box)
                || box.getLootTable() == null) return null;
        for (Canid c : level.getEntitiesOfClass(Canid.class, new net.minecraft.world.phys.AABB(pos).inflate(12),
                c -> c.isAlive() && !c.isBaby() && c.species == GirlSpecies.ANUBIS_CANID && (!c.hasWeighed(player) || c.isCondemned(player)))) {
            return c;
        }
        return null;
    }

    public void warnOff(Player player) {
        this.startTimedAction(Action.JUDGE, 30);
        this.getLookControl().setLookAt(player);
        this.playSound(SoundEvents.WOLF_GROWL, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.monstergirls.anubis_guard", this.getDisplayName()), true);
    }

    // --- hostility -----------------------------------------------------------------------------------------------

    /** Anubis hunts the condemned; a polar bear goes for strangers near her cubs. */
    @Override
    public boolean isHostileTo(Player player) {
        if (this.species == GirlSpecies.ANUBIS_CANID) return this.isCondemned(player) && this.couldAttack(player);
        if (this.guardsCubFrom(player)) return this.couldAttack(player);
        if (this.isHibernating()) return false;
        return super.isHostileTo(player);
    }

    /** A hurt cub calls her mother. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.BLACK_BEAR_CANID && this.level() instanceof ServerLevel server && this.isWild() && !this.isBaby()) {
            this.scare(server, source.getSourcePosition() != null ? source.getSourcePosition() : this.position());   // she runs rather than fights
        }
        if (hurt && this.isBaby() && source.getEntity() instanceof LivingEntity attacker && this.mother() instanceof Canid mom) mom.setTarget(attacker);
        return hurt;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.OBSIDIAN_HOUND_CANID && this.level() instanceof ServerLevel badlands && !this.isBaby() && this.tickCount % 20 == 0) this.coolLava(badlands);
        if (this.species == GirlSpecies.TARNHELM_CANID && this.level() instanceof ServerLevel watch && !this.isBaby() && this.tickCount % 100 == 0) this.sentinel(watch);
        if (this.species == GirlSpecies.AMAROK_CANID && this.level() instanceof ServerLevel pack && !this.isBaby() && this.tickCount % 20 == 0) this.packMother(pack);
        if (this.species == GirlSpecies.CU_SITH_CANID && this.level() instanceof ServerLevel moor && !this.isBaby() && this.tickCount % 40 == 0) this.bark(moor);
        if (this.species == GirlSpecies.YETI_CANID && this.level() instanceof ServerLevel peak && !this.isBaby()) {
            if (this.tickCount % 30 == 0) this.throwSnowball(peak);
            if (this.tickCount % 6000 == 0) this.buildSnowGolem(peak);
        }
        if (this.species == GirlSpecies.MELONCOLLIE_CANID && this.level() instanceof ServerLevel farm && !this.isBaby()) {
            if (this.tickCount % 40 == 0) this.herd(farm);
            if (this.tickCount % 2400 == 0) this.plantMelon(farm);
        }
        if (!(this.level() instanceof ServerLevel server) || this.isBaby()) return;
        switch (this.species) {
            case WOLF_CANID -> {
                long time = server.getDayTime() % 24000L;
                if (time > 13000 && time < 23000 && server.getGameTime() >= this.nextHowl && this.getTarget() == null) {   // she howls at night
                    this.nextHowl = server.getGameTime() + 1200 + this.random.nextInt(2400);
                    this.startTimedAction(Action.HOWL, 40);
                    this.playSound(SoundEvents.WOLF_HOWL, 2.0F, 1.1F);
                }
                if (this.tickCount % 20 == 0) this.leadTheWolves(server);
            }
            case POLAR_BEAR_CANID -> {
                if (this.tickCount % 100 == 0) this.fish(server);
            }
            case GRIZZLY_CANID -> {
                if (this.isHibernating()) {                               // curled up asleep until the snow stops
                    this.getNavigation().stop();
                    if (this.getAction() != Action.LYING) this.startTimedAction(Action.LYING, 200);
                    if (this.tickCount % 100 == 0) server.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getEyeY(), this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
                } else {
                    if (this.tickCount % 100 == 0) this.fish(server);
                    if (this.tickCount % 40 == 0) this.raidHive(server);
                }
            }
            case BLACK_BEAR_CANID -> {
                if (this.scaredUntil > server.getGameTime()) {
                    this.setTarget(null);
                } else {
                    if (this.tickCount % 10 == 0) {
                        Player sprinter = this.nearest(Player.class, 6, p -> p.isSprinting() && !p.isSpectator() && !p.isCreative()
                                && this.getAffection(p) < Orders.AFFECTION);
                        if (sprinter != null) this.scare(server, sprinter.position());
                    }
                    if (this.tickCount % 40 == 0) this.raidBerries(server);
                }
            }
            case KOBOLD_CANID -> {
                Player friend = this.bondedPlayer(Orders.AFFECTION, 16);
                if (friend != null && this.tickCount % 10 == 0) this.warnOfCaveIn(server, friend);
                if (friend != null && this.tickCount % 200 == 0 && this.getTarget() == null) this.pointOutOre(server, friend);
                if (this.tickCount % 100 == 0) this.smellMimics(server);
            }
            case PANDA_CANID -> this.tickPanda(server);
            case ANUBIS_CANID -> {
                if (this.tickCount % 20 == 0) {
                    for (ServerPlayer p : server.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(10),
                            p -> !p.isSpectator() && !p.isCreative())) this.weigh(p);
                }
            }
            default -> {
            }
        }
    }

    /** Her player's tamed wolves join her fights. */
    public void leadTheWolves(ServerLevel level) {
        LivingEntity target = this.getTarget();
        Player p = this.bondedPlayer(Orders.AFFECTION, 32);
        if (target == null || p == null || target == p) return;
        for (Wolf w : level.getEntitiesOfClass(Wolf.class, this.getBoundingBox().inflate(16),
                w -> w.isTame() && w.isOwnedBy(p) && !w.isOrderedToSit() && w.getTarget() != target)) {
            w.setTarget(target);
        }
    }

    // --- food, presents, young -----------------------------------------------------------------------------------

    // --- the meloncollie --------------------------------------------------------------------------------------------

    /** The fence gate nearest her within 24 blocks: where the strays go. */
    @Nullable
    public BlockPos pen(ServerLevel level) {
        BlockPos o = this.blockPosition(), best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-24, -3, -24), o.offset(24, 3, 24))) {
            if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.FENCE_GATES) && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    /** Befriended, she picks the nearest stray farm animal (more than 6 blocks from the gate) and drives it to the gate. */
    @Nullable
    public net.minecraft.world.entity.animal.Animal herd(ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32) == null) return null;
        BlockPos gate = this.pen(level);
        if (gate == null) return null;
        Vec3 g = Vec3.atBottomCenterOf(gate);
        net.minecraft.world.entity.animal.Animal stray = this.nearest(net.minecraft.world.entity.animal.Animal.class, 16, a -> !a.isLeashed()
                && (a instanceof net.minecraft.world.entity.animal.Cow || a instanceof net.minecraft.world.entity.animal.Sheep
                || a instanceof net.minecraft.world.entity.animal.Pig || a instanceof net.minecraft.world.entity.animal.Chicken) && a.position().distanceToSqr(g) > 6 * 6);
        if (stray == null) return null;
        Vec3 behind = stray.position().add(stray.position().subtract(g).multiply(1, 0, 1).normalize().scale(2.0));
        this.getNavigation().moveTo(behind.x, behind.y, behind.z, 1.3);
        stray.getNavigation().moveTo(g.x, g.y, g.z, 1.2);
        if (this.random.nextInt(3) == 0) this.playSound(SoundEvents.WOLF_AMBIENT, 1.0F, 1.3F);
        return stray;
    }

    /** Now and then she plants a melon seed in empty farmland near her. */
    public boolean plantMelon(ServerLevel level) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -1, -4), o.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.FARMLAND) && level.getBlockState(p.above()).isAir()) {
                level.setBlock(p.above(), net.minecraft.world.level.block.Blocks.MELON_STEM.defaultBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return switch (this.species) {
            case WOLF_CANID -> stack.is(Items.BONE);
            case POLAR_BEAR_CANID -> stack.is(ItemTags.FISHES);
            case PANDA_CANID -> stack.is(Items.BAMBOO) || stack.is(Items.CAKE);
            case KOBOLD_CANID -> stack.is(Items.BONE);
            case GRIZZLY_CANID -> stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON) || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.HONEYCOMB);
            case BLACK_BEAR_CANID -> stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.HONEY_BOTTLE);
            case MELONCOLLIE_CANID -> stack.is(Items.MELON_SLICE) || stack.is(Items.BONE);
            case CU_SITH_CANID -> stack.is(Items.BEEF) || stack.is(Items.COOKED_BEEF) || stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON);
            case YETI_CANID -> stack.is(Items.SWEET_BERRIES) || stack.is(ItemTags.FISHES);
            default -> stack.is(Items.BREAD) || stack.is(Items.HONEY_BOTTLE);
        };
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.BONE) || stack.is(Items.CAKE) || (this.species == GirlSpecies.GRIZZLY_CANID && stack.is(Items.HONEY_BOTTLE))) return 3;
        if (stack.is(Items.BAMBOO) || stack.is(ItemTags.FISHES) || stack.is(Items.HONEY_BOTTLE)) return 2;
        return super.foodValue(player, stack);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case WOLF_CANID -> r < 40 ? new ItemStack(Items.BONE, 2) : new ItemStack(r < 70 ? Items.RABBIT_HIDE : r < 95 ? Items.LEATHER : Items.NAME_TAG);
            case POLAR_BEAR_CANID -> r < 85 ? new ItemStack(r < 50 ? Items.COD : Items.SALMON, 2) : new ItemStack(Items.PACKED_ICE, 4);
            case PANDA_CANID -> r < 60 ? new ItemStack(Items.BAMBOO, 4) : new ItemStack(r < 90 ? Items.SLIME_BALL : Items.CAKE);
            case KOBOLD_CANID -> r < 50 ? new ItemStack(Items.RAW_IRON, 2) : r < 80 ? new ItemStack(Items.RAW_COPPER, 3) : new ItemStack(r < 95 ? Items.RAW_GOLD : Items.DIAMOND);
            case GRIZZLY_CANID -> r < 50 ? new ItemStack(Items.SALMON, 2) : r < 85 ? new ItemStack(Items.HONEYCOMB, 2) : new ItemStack(Items.SWEET_BERRIES, 6);
            case BLACK_BEAR_CANID -> r < 60 ? new ItemStack(Items.SWEET_BERRIES, 6) : r < 90 ? new ItemStack(Items.GLOW_BERRIES, 3) : new ItemStack(Items.HONEYCOMB);
            case CU_SITH_CANID -> new ItemStack(r < 45 ? Items.BONE : r < 80 ? Items.MOSS_BLOCK : r < 97 ? Items.FERN : Items.EMERALD, r < 97 ? 2 : 1);
            case YETI_CANID -> new ItemStack(r < 45 ? Items.SNOWBALL : r < 80 ? Items.ICE : r < 97 ? Items.PACKED_ICE : Items.BLUE_ICE, r < 45 ? 8 : 2);
            case MELONCOLLIE_CANID -> r < 50 ? new ItemStack(Items.MELON_SLICE, 4) : r < 90 ? new ItemStack(Items.MELON_SEEDS, 3) : new ItemStack(Items.GLISTERING_MELON_SLICE);
            default -> r < 45 ? new ItemStack(Items.GOLD_NUGGET, 3) : r < 80 ? new ItemStack(Items.LAPIS_LAZULI, 2) : new ItemStack(r < 95 ? Items.BONE : Items.GOLD_INGOT);
        };
    }

    @Override
    protected int litterSize() {
        return switch (this.species) {
            case WOLF_CANID -> 2 + this.random.nextInt(3);
            case POLAR_BEAR_CANID, GRIZZLY_CANID, BLACK_BEAR_CANID -> 1 + this.random.nextInt(2);
            case KOBOLD_CANID -> 2 + this.random.nextInt(2);
            default -> 1;
        };
    }

    @Override
    public AbstractMonsterGirl spawnYoung(ServerLevel level, BlockPos pos, @Nullable UUID father) {
        AbstractMonsterGirl young = super.spawnYoung(level, pos, father);
        if (young instanceof Canid c && this.species == GirlSpecies.PANDA_CANID) {
            c.setPersonality(this.random.nextBoolean() ? this.personality : Personality.values()[this.random.nextInt(Personality.values().length)]);
        }
        return young;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case POLAR_BEAR_CANID, GRIZZLY_CANID, BLACK_BEAR_CANID -> this.isHibernating() ? SoundEvents.FOX_SLEEP : SoundEvents.POLAR_BEAR_AMBIENT;
            case PANDA_CANID -> SoundEvents.PANDA_AMBIENT;
            default -> SoundEvents.WOLF_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (this.species) {
            case POLAR_BEAR_CANID, GRIZZLY_CANID, BLACK_BEAR_CANID -> SoundEvents.POLAR_BEAR_HURT;
            case PANDA_CANID -> SoundEvents.PANDA_HURT;
            default -> SoundEvents.WOLF_HURT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (this.species) {
            case POLAR_BEAR_CANID, GRIZZLY_CANID, BLACK_BEAR_CANID -> SoundEvents.POLAR_BEAR_DEATH;
            case PANDA_CANID -> SoundEvents.PANDA_DEATH;
            default -> SoundEvents.WOLF_DEATH;
        };
    }

    @Override
    public SoundEvent getEatSound() {
        return this.species == GirlSpecies.PANDA_CANID ? SoundEvents.PANDA_EAT : SoundEvents.GENERIC_EAT;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Alpha", this.alpha);
        tag.putString("Personality", this.personality.name());
        ListTag w = new ListTag();
        for (UUID u : this.weighed) w.add(NbtUtils.createUUID(u));
        tag.put("Weighed", w);
        ListTag c = new ListTag();
        for (UUID u : this.condemned) c.add(NbtUtils.createUUID(u));
        tag.put("Condemned", c);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.alpha = tag.getBoolean("Alpha");
        try {
            this.setPersonality(Personality.valueOf(tag.getString("Personality")));
        } catch (IllegalArgumentException e) {
            this.setPersonality(Personality.NORMAL);
        }
        this.weighed.clear();
        for (var t : tag.getList("Weighed", 11)) this.weighed.add(NbtUtils.loadUUID(t));
        this.condemned.clear();
        for (var t : tag.getList("Condemned", 11)) this.condemned.add(NbtUtils.loadUUID(t));
    }

    // --- the amarok ----------------------------------------------------------------------------------------------

    /** Her friend's tamed wolves within 12 blocks of her fight harder (Strength I). How many, or -1 without a friend. */
    public int packMother(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return -1;
        int n = 0;
        for (var wolf : level.getEntitiesOfClass(net.minecraft.world.entity.animal.Wolf.class, this.getBoundingBox().inflate(12), w -> w.isTame() && w.isOwnedBy(friend))) {
            wolf.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, 0, true, true));
            n++;
        }
        return n;
    }

    /** The tarnhelm: raiders and illagers within 48 blocks of her friend glow for eleven seconds. How many, or -1 without a friend. */
    public int sentinel(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return -1;
        int n = 0;
        for (var raider : level.getEntitiesOfClass(net.minecraft.world.entity.raid.Raider.class, friend.getBoundingBox().inflate(48))) {
            raider.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 220, 0, true, false));
            n++;
        }
        return n;
    }

    // --- the obsidian hound (nineteenth wave) -----------------------------------------------------------------------

    /** Still lava within 2 blocks of her friend's feet (and the layer below) cools to obsidian. How many blocks, or -1 without a
     *  friend. */
    public int coolLava(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-2, -1, -2), friend.blockPosition().offset(2, 0, 2))) {
            var fluid = level.getFluidState(p);
            if (!fluid.is(net.minecraft.tags.FluidTags.LAVA) || !fluid.isSource()) continue;
            level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState());
            level.levelEvent(1501, p, 0);                // the hiss and smoke of lava going out
            n++;
        }
        return n;
    }
}
