package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Four Riders (rider_field_guide.html): War, Famine, Pestilence, Death. Rare and solitary, each with her own look (tools/gen_rider.py).
 * Befriended, War gives her friend Strength when they are outnumbered, Famine keeps them fed in a fight and weakens the monsters, Pestilence
 * lays a teal cloud on the monsters, Death finishes off the ones that are nearly dead. Pestilence rides a real black horse: she mounts one
 * of her own when she first ticks, and walks if it dies.
 */
public class Rider extends AbstractMonsterGirl {
    /** From her feet down to the saddle: her hips (12 model pixels, she is drawn at 0.84) stand a little above it, as a player's do. */
    public static final double SEAT = 0.48;
    /** Whether Pestilence has had her horse yet: it does not come back once it is lost. */
    private boolean hadSteed;
    /** The Charro Negro's terms: what she lends (twice this comes back) and for how long. */
    public static final int LOAN = 8;
    public static final int LOAN_TICKS = 3 * 24000;

    public Rider(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.BREAD, Items.COOKED_BEEF, Items.BONE, Items.GLOW_BERRIES), false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.CHARRO_NEGRO_RIDER) return girlAttributes(40, 0.3, 8);
        return switch (s) {
            case WAR_RIDER -> girlAttributes(44, 0.3, 9).add(Attributes.ARMOR, 8);
            case FAMINE_RIDER -> girlAttributes(30, 0.26, 4);
            case PESTILENCE_RIDER -> girlAttributes(26, 0.28, 3);
            default -> girlAttributes(30, 0.3, 8);
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.MEAT) || stack.is(Items.BREAD) || stack.is(Items.BONE) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case WAR_RIDER -> new ItemStack(r < 60 ? Items.IRON_INGOT : Items.ARROW, r < 60 ? 2 : 8);
            case FAMINE_RIDER -> new ItemStack(r < 70 ? Items.BREAD : Items.GOLDEN_CARROT, 3);
            case PESTILENCE_RIDER -> new ItemStack(r < 70 ? Items.FERMENTED_SPIDER_EYE : Items.GLOW_BERRIES, 2);
            default -> new ItemStack(r < 70 ? Items.BONE : Items.WITHER_SKELETON_SKULL, r < 70 ? 4 : 1);
        };
    }

    @Override
    protected boolean hasRideAnimation() {
        return true;
    }

    /** Her hips, not her feet, go on the saddle (with the legs folded, her feet are nowhere near it). */
    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        return new Vec3(0.0, SEAT, 0.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.isBaby()) return;
        if ((this.species == GirlSpecies.PESTILENCE_RIDER || this.species == GirlSpecies.CHARRO_NEGRO_RIDER) && !this.hadSteed) this.mount(level);
        if (this.species == GirlSpecies.CHARRO_NEGRO_RIDER && this.tickCount % 40 == 0) this.collect(level);
        if (this.species == GirlSpecies.WAR_RIDER && this.tickCount % 20 == 0) this.battleCry(level);
        if (this.species == GirlSpecies.FAMINE_RIDER && this.tickCount % 40 == 0) this.hollowHunger(level);
        if (this.species == GirlSpecies.PESTILENCE_RIDER && this.tickCount % 60 == 0) this.tealCloud(level);
        if (this.species == GirlSpecies.DEATH_RIDER && this.tickCount % 10 == 0) this.lastCall(level);
    }

    @Override
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.CHARRO_NEGRO_RIDER && !this.isBaby()) {
            if (stack.is(Items.EMERALD) && this.repay(player, stack)) return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
            if (stack.isEmpty() && player.isShiftKeyDown() && this.getAffection(player) >= Orders.AFFECTION) {
                if (!this.level().isClientSide) this.lend(player);
                return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.familyInteract(player, hand, stack);
    }

    /** The Charro Negro's loan: 8 emeralds now, 16 back within three days. Only one debtor at a time. */
    public boolean lend(Player player) {
        var data = this.getPersistentData();
        if (data.hasUUID("LoanTo")) return false;
        data.putUUID("LoanTo", player.getUUID());
        data.putLong("LoanDue", this.level().getGameTime() + LOAN_TICKS);
        player.getInventory().placeItemBackInInventory(new ItemStack(Items.EMERALD, LOAN));
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.charro.lent", this.getDisplayName()), true);
        this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 0.6F);
        return true;
    }

    /** Paying her back, from the stack in hand. False if they owe her nothing or are short. */
    public boolean repay(Player player, ItemStack stack) {
        var data = this.getPersistentData();
        if (!data.hasUUID("LoanTo") || !data.getUUID("LoanTo").equals(player.getUUID()) || stack.getCount() < LOAN * 2) return false;
        stack.shrink(LOAN * 2);
        this.forgive();
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.charro.paid", this.getDisplayName()), true);
        return true;
    }

    private void forgive() {
        this.getPersistentData().remove("LoanTo");
        this.getPersistentData().remove("LoanDue");
    }

    /** Whether anybody owes her. */
    public boolean hasLoan() {
        return this.getPersistentData().hasUUID("LoanTo");
    }

    public void setLoanDue(long tick) {
        this.getPersistentData().putLong("LoanDue", tick);
    }

    /** Past the day, she rides to the debtor, wherever they are in this world. 1 when she is coming, 0 not due or they are away, -1 with no loan out. */
    public int collect(ServerLevel level) {
        var data = this.getPersistentData();
        if (!data.hasUUID("LoanTo")) return -1;
        if (level.getGameTime() < data.getLong("LoanDue")) return 0;
        Player debtor = level.getPlayerByUUID(data.getUUID("LoanTo"));
        if (debtor == null || !debtor.isAlive() || debtor.isSpectator()) return 0;
        if (this.distanceToSqr(debtor) > 48 * 48) {
            net.minecraft.world.entity.LivingEntity who = this.getVehicle() instanceof net.minecraft.world.entity.LivingEntity horse ? horse : this;
            if (who.randomTeleport(debtor.getX() + 12, debtor.getY(), debtor.getZ(), false)) this.playSound(SoundEvents.HORSE_GALLOP, 1.0F, 0.6F);
        }
        if (this.getTarget() != debtor) debtor.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.charro.due", this.getDisplayName()), true);
        this.settleScoreWith(debtor);
        return 1;
    }

    /** A debt dies with the debtor. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof Player p && !p.isAlive() && this.hasLoan() && this.getPersistentData().getUUID("LoanTo").equals(p.getUUID())) this.forgive();
        return hit;
    }

    /** Pestilence's horse, a real Minecraft horse in black, under her. */
    public Horse mount(ServerLevel level) {
        this.hadSteed = true;
        Horse horse = EntityType.HORSE.create(level);
        if (horse == null) return null;
        horse.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        horse.setVariant(Variant.BLACK);
        horse.setAge(0);
        horse.setPersistenceRequired();
        level.addFreshEntity(horse);
        this.startRiding(horse, true);
        return horse;
    }

    /** War: with three or more monsters within 8 of her friend, Strength II for 5 seconds. 1 when she called, 0 when not outnumbered, -1 without a friend. */
    public int battleCry(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (this.monsters(level, friend, 8).size() < 3) return 0;
        friend.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
        this.playSound(SoundEvents.RAID_HORN.value(), 0.5F, 1.4F);
        return 1;
    }

    /** Famine: with a monster within 8 of her friend, a little food for her friend and Weakness on the monsters. The number weakened, -1 without a friend. */
    public int hollowHunger(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(Orders.AFFECTION, 24);
        if (friend == null) return -1;
        List<Monster> near = this.monsters(level, friend, 8);
        if (near.isEmpty()) return 0;
        friend.getFoodData().eat(1, 0.3F);
        for (Monster m : near) m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
        return near.size();
    }

    /** Pestilence: a teal cloud on the monsters within 10 of her friend: 2 damage and Slowness II for 3 seconds (the undead do not escape it). The number hit, -1 without a friend. */
    public int tealCloud(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(Orders.AFFECTION, 32);
        if (friend == null) return -1;
        List<Monster> near = this.monsters(level, friend, 10);
        for (Monster m : near) {
            m.hurt(level.damageSources().magic(), 2.0F);
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, m.getX(), m.getEyeY(), m.getZ(), 10, 0.4, 0.4, 0.4, 0.01);
        }
        return near.size();
    }

    /** Death: the monsters within 8 of her friend at a quarter of their health or less fall. Never a boss. The number that fell, -1 without a friend. */
    public int lastCall(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int fell = 0;
        for (Monster m : this.monsters(level, friend, 8)) {
            if (m.getHealth() > m.getMaxHealth() * 0.25F || m.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)) continue;
            m.hurt(level.damageSources().magic(), m.getHealth() + 1.0F);
            level.sendParticles(ParticleTypes.SOUL, m.getX(), m.getEyeY(), m.getZ(), 8, 0.2, 0.3, 0.2, 0.02);
            fell++;
        }
        return fell;
    }

    private List<Monster> monsters(ServerLevel level, net.minecraft.world.entity.player.Player friend, double range) {
        return level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(range), m -> m.isAlive());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HadSteed", this.hadSteed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.hadSteed = tag.getBoolean("HadSteed");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }
}
