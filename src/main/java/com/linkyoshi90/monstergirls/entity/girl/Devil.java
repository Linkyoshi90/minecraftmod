package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The infernal family (devil_field_guide.html): lesser devil, succubus, greater devil, jack o'lantern, jiang shi. Tricksters
 * with a sweet tooth. The lesser devil plays pranks (steals from your hotbar, swaps your slots, shoves you); the
 * succubus' gaze slows the one she is after and she slips into their bed whatever her friendship; the greater devil
 * flies and throws fire; the jack o'lantern hides as a pumpkin and creeps after you unseen; the jiang shi can only hop
 * and is calmed by a paper talisman.
 */
public class Devil extends AbstractMonsterGirl {
    private long nextPrank;
    private ItemStack loot = ItemStack.EMPTY;
    private int stillTicks;
    private long nextFire;

    public Devil(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.JACK_O_LANTERN_DEVIL) this.goalSelector.addGoal(1, new StalkGoal(this, 0.9, 1.8, true, t -> t instanceof Player));
        if (species == GirlSpecies.LESSER_DEVIL) {                           // a thief runs off with her prize
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.2, 1.5, p -> !this.loot.isEmpty()));
        }
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.SUGAR, Items.COOKIE, Items.CAKE, Items.PUMPKIN_PIE), false));
        if (species == GirlSpecies.SUCCUBUS_DEVIL) this.goalSelector.addGoal(5, new FollowEntityGoal(this, () -> this.level().isDay() ? null : this.level().getNearestPlayer(this, 24), 1.0, 8.0F, 3.0F));
        if (!this.canFly()) this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !Devil.this.isDisguised() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case LESSER_DEVIL -> girlAttributes(12, 0.32, 2);
            case SUCCUBUS_DEVIL -> girlAttributes(26, 0.3, 4);
            case GREATER_DEVIL -> girlAttributes(70, 0.28, 10);
            case JACK_O_LANTERN_DEVIL -> girlAttributes(24, 0.28, 4);
            default -> girlAttributes(30, 0.26, 5);
        };
    }

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.LESSER_DEVIL || this.species == GirlSpecies.SUCCUBUS_DEVIL || this.species == GirlSpecies.GREATER_DEVIL;
    }

    // --- hostility -----------------------------------------------------------------------------------------------

    /** The jack o'lantern and, at night, the jiang shi come for players. */
    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.JACK_O_LANTERN_DEVIL || this.species == GirlSpecies.JIANG_SHI_DEVIL;
    }

    @Override
    public boolean isHostileTo(Player player) {
        return super.isHostileTo(player) && (this.species != GirlSpecies.JIANG_SHI_DEVIL || !this.level().isDay());
    }

    /** A paper talisman for the jiang shi; pumpkin pie for the jack o'lantern. */
    @Override
    protected boolean calmsHer(ItemStack stack) {
        return this.species == GirlSpecies.JIANG_SHI_DEVIL ? stack.is(Items.PAPER) : stack.is(Items.PUMPKIN_PIE);
    }

    /** She slips into a bed whatever her friendship, and every night she is sure of it. */
    @Override
    public boolean trustsEnoughForBed(Player player) {
        return this.species == GirlSpecies.SUCCUBUS_DEVIL ? !player.getUUID().equals(this.getFather()) : super.trustsEnoughForBed(player);
    }

    @Override
    public float bedChanceFor(Player player) {
        return this.species == GirlSpecies.SUCCUBUS_DEVIL ? 0.5F + this.getAffection(player) / 200.0F : super.bedChanceFor(player);
    }

    // --- the jack o'lantern's disguise ---------------------------------------------------------------------------

    public boolean isDisguised() {
        return this.hasFlag(FLAG_HIDDEN);
    }

    public void setDisguised(boolean on) {
        this.setFlag(FLAG_HIDDEN, on);
        if (on) this.getNavigation().stop();
    }

    // --- pranks --------------------------------------------------------------------------------------------------

    public ItemStack getLoot() {
        return this.loot;
    }

    /** Steal something from the hotbar, swap two slots, or a shove: then off she flutters, giggling. */
    public void prank(Player p) {
        int kind = this.random.nextInt(3);
        var inv = p.getInventory();
        if (kind == 0 && this.loot.isEmpty()) {
            for (int tries = 0; tries < 9; tries++) {
                int slot = this.random.nextInt(9);
                if (!inv.getItem(slot).isEmpty()) {
                    this.loot = inv.removeItem(slot, 1);
                    p.displayClientMessage(Component.translatable("message.monstergirls.prank_steal", this.getDisplayName(), this.loot.getHoverName()), true);
                    break;
                }
            }
        } else if (kind == 1) {
            int a = this.random.nextInt(9), b = this.random.nextInt(9);
            ItemStack t = inv.getItem(a);
            inv.setItem(a, inv.getItem(b));
            inv.setItem(b, t);
            p.displayClientMessage(Component.translatable("message.monstergirls.prank_swap", this.getDisplayName()), true);
        } else {
            Vec3 push = p.position().subtract(this.position()).normalize();
            p.push(push.x * 0.8, 0.3, push.z * 0.8);
            p.hurtMarked = true;
        }
        this.startTimedAction(Action.LAUGH, 30);
        this.playSound(SoundEvents.VEX_AMBIENT, 1.0F, 1.6F);
    }

    /**
     * With nobody to prank, she lifts one thing from a chest or barrel within five blocks (never an unopened loot chest)
     * and keeps it until someone feeds her. Obeys mobGriefing.
     */
    public boolean raidChest(ServerLevel level) {
        if (!this.loot.isEmpty() || this.hasAnyAffection() && this.bondedPlayer(60, 64) != null
                || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this)) return false;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-5, -2, -5), this.blockPosition().offset(5, 2, 5))) {
            if (!(level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.BaseContainerBlockEntity box)) continue;
            if (box instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity r && r.getLootTable() != null) continue;
            int size = box.getContainerSize(), start = this.random.nextInt(size);
            for (int i = 0; i < size; i++) {
                int slot = (start + i) % size;
                if (box.getItem(slot).isEmpty()) continue;
                this.loot = box.removeItem(slot, 1);
                level.playSound(null, p, SoundEvents.CHEST_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.4F);
                this.startTimedAction(Action.LAUGH, 30);
                this.playSound(SoundEvents.VEX_AMBIENT, 1.0F, 1.6F);
                return true;
            }
        }
        return false;
    }

    /** Feeding or hitting the thief makes her give back what she took. */
    private void giveBack(Player p) {
        if (this.loot.isEmpty()) return;
        if (!p.getInventory().add(this.loot)) this.spawnAtLocation(this.loot);
        this.loot = ItemStack.EMPTY;
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SUGAR) || stack.is(Items.COOKIE) || stack.is(Items.CAKE) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.HONEY_BOTTLE);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        this.giveBack(player);
        return stack.is(Items.CAKE) ? 3 : 1;
    }

    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.isDisguised() && !this.level().isClientSide) this.setDisguised(false);
        return null;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case JIANG_SHI_DEVIL -> new ItemStack(r < 60 ? Items.PAPER : Items.GOLD_NUGGET, 3);
            case JACK_O_LANTERN_DEVIL -> new ItemStack(r < 50 ? Items.PUMPKIN_SEEDS : r < 80 ? Items.TORCH : Items.JACK_O_LANTERN, r < 80 ? 4 : 1);
            default -> new ItemStack(r < 40 ? Items.BLAZE_POWDER : r < 75 ? Items.MAGMA_CREAM : Items.QUARTZ, 2);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player p) this.giveBack(p);
        if (hurt) this.setDisguised(false);
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.GREATER_DEVIL) target.igniteForSeconds(4);
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        switch (this.species) {
            case LESSER_DEVIL -> {
                if (this.tickCount % 20 == 0 && server.getGameTime() >= this.nextPrank && !this.isBaby()) {
                    Player p = this.level().getNearestPlayer(this, 4.0);
                    if (p != null && !p.isCreative() && !p.isSpectator() && this.getAffection(p) < 60) {
                        this.nextPrank = server.getGameTime() + 1200 + this.random.nextInt(1200);
                        this.prank(p);
                    } else if (p == null && this.random.nextInt(10) == 0 && this.raidChest(server)) {
                        this.nextPrank = server.getGameTime() + 1200 + this.random.nextInt(1200);
                    }
                }
                Player friend = this.bondedPlayer(60, 16);                    // devoted: her pranks go to your enemies
                if (friend != null && this.tickCount % 40 == 0) {
                    for (Monster m : server.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(8))) {
                        m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                        m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    }
                }
            }
            case SUCCUBUS_DEVIL -> {
                if (this.tickCount % 40 == 0 && !server.isDay()) {
                    Player p = this.level().getNearestPlayer(this, 8.0);
                    if (p != null && !p.isCreative() && this.getAffection(p) < BED_AFFECTION) {
                        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));   // her gaze
                        this.getLookControl().setLookAt(p);
                        if (this.getAction() == Action.NONE) this.startTimedAction(Action.CHARM, 40);
                    }
                }
            }
            case GREATER_DEVIL -> {
                LivingEntity t = this.getTarget();
                if (t != null && server.getGameTime() >= this.nextFire && this.distanceToSqr(t) < 14 * 14 && this.hasLineOfSight(t)) {
                    this.nextFire = server.getGameTime() + 60;
                    Vec3 d = t.getEyePosition().subtract(this.getEyePosition()).normalize();
                    SmallFireball ball = new SmallFireball(server, this, d.scale(0.9));
                    ball.setPos(this.getX() + d.x, this.getEyeY(), this.getZ() + d.z);
                    server.addFreshEntity(ball);
                    this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.8F);
                }
            }
            case JACK_O_LANTERN_DEVIL -> {
                boolean still = this.getTarget() == null && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && !this.isBaby();
                this.stillTicks = still ? this.stillTicks + 1 : 0;
                if (this.stillTicks > 100 && !this.isDisguised() && this.bondedPlayer(10, 16) == null) this.setDisguised(true);
                if (this.isDisguised()) {
                    this.getNavigation().stop();
                    Player p = this.level().getNearestPlayer(this, 2.5);
                    if (p != null && !p.isSpectator()) {                     // boo!
                        this.setDisguised(false);
                        this.startTimedAction(Action.LAUGH, 30);
                        this.playSound(SoundEvents.WITCH_CELEBRATE, 1.0F, 1.4F);
                    }
                }
                if (!server.isDay()) this.glow(server, 10);
                else this.glow(server, 0);
            }
            case JIANG_SHI_DEVIL -> {                                         // she can only hop
                if (!this.getNavigation().isDone() && this.onGround() && !this.level().isDay()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0, 0.42, 0));
                    if (this.getAction() == Action.NONE) this.startTimedAction(Action.HOP, 16);
                }
                if (this.level().isDay() && this.getTarget() == null && !this.isCalm()) this.getNavigation().stop();   // asleep on her feet
            }
            default -> {
            }
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.LESSER_DEVIL ? SoundEvents.VEX_AMBIENT : this.isDisguised() ? null : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
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

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (!this.loot.isEmpty()) this.spawnAtLocation(this.loot);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.loot.isEmpty()) tag.put("Loot", this.loot.save(this.registryAccess()));
        tag.putLong("NextPrank", this.nextPrank);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loot = tag.contains("Loot") ? ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Loot")) : ItemStack.EMPTY;
        this.nextPrank = tag.getLong("NextPrank");
    }
}
