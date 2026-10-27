package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.particles.ParticleTypes;
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
 * and is calmed by a paper talisman. The oni walks the mountains at night with an iron club: a honey bottle calms her,
 * thrown beans (cocoa beans) drive her off, and once she is your friend she guards your door through the night.
 */
public class Devil extends AbstractMonsterGirl {
    private long nextPrank;
    private ItemStack loot = ItemStack.EMPTY;
    private int stillTicks;
    private long nextFire;
    @javax.annotation.Nullable
    private java.util.UUID beanThrower;
    private long fleeUntil;

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
        if (species == GirlSpecies.ONI_DEVIL) {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.2, 1.5,          // "Oni wa soto!"
                    p -> p.getUUID().equals(this.beanThrower) && this.level().getGameTime() < this.fleeUntil));
            this.goalSelector.addGoal(5, new GuardDoorGoal());
            this.targetSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> !(m instanceof net.minecraft.world.entity.monster.Creeper) && this.guardedDoor() != null && m.distanceToSqr(this) < 12 * 12));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case LESSER_DEVIL -> girlAttributes(12, 0.32, 2);
            case SUCCUBUS_DEVIL -> girlAttributes(26, 0.3, 4);
            case GREATER_DEVIL -> girlAttributes(70, 0.28, 10);
            case JACK_O_LANTERN_DEVIL -> girlAttributes(24, 0.28, 4);
            case ONI_DEVIL -> girlAttributes(50, 0.27, 9).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, 1.2);
            case GREMLIN_DEVIL -> girlAttributes(12, 0.3, 1);
            case KARAKASA_OBAKE_DEVIL -> girlAttributes(20, 0.3, 2);
            case CHOCHIN_OBAKE_DEVIL -> girlAttributes(18, 0.3, 2);
            case ITTAN_MOMEN_DEVIL -> girlAttributes(16, 0.3, 1);
            case ROKUROKUBI_DEVIL -> girlAttributes(18, 0.28, 2);
            case KIKIMORA_DEVIL -> girlAttributes(14, 0.3, 2);
            default -> girlAttributes(30, 0.26, 5);
        };
    }

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.LESSER_DEVIL || this.species == GirlSpecies.SUCCUBUS_DEVIL || this.species == GirlSpecies.GREATER_DEVIL
                || this.species == GirlSpecies.ITTAN_MOMEN_DEVIL;
    }

    // --- hostility -----------------------------------------------------------------------------------------------

    /** The jack o'lantern and, at night, the jiang shi and the oni come for players. */
    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.JACK_O_LANTERN_DEVIL || this.species == GirlSpecies.JIANG_SHI_DEVIL || this.species == GirlSpecies.ONI_DEVIL;
    }

    @Override
    public boolean isHostileTo(Player player) {
        boolean night = this.species != GirlSpecies.JIANG_SHI_DEVIL && this.species != GirlSpecies.ONI_DEVIL || !this.level().isDay();
        return super.isHostileTo(player) && night && !(this.species == GirlSpecies.ONI_DEVIL && this.level().getGameTime() < this.fleeUntil);
    }

    /** A paper talisman for the jiang shi; pumpkin pie for the jack o'lantern; a drink (a honey bottle) for the oni. */
    @Override
    protected boolean calmsHer(ItemStack stack) {
        return switch (this.species) {
            case JIANG_SHI_DEVIL -> stack.is(Items.PAPER);
            case ONI_DEVIL -> stack.is(Items.HONEY_BOTTLE);
            default -> stack.is(Items.PUMPKIN_PIE);
        };
    }

    // --- the rokurokubi -----------------------------------------------------------------------------------------------

    /** After dark her neck rises high ("watch") and the monsters within 24 blocks of her friend glow. How many she spotted. */
    public int nightWatch(ServerLevel level, boolean night) {
        if (!night || this.isBaby()) return 0;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (this.getAction() == Action.NONE || this.getAction() == Action.WATCH) this.startTimedAction(Action.WATCH, 40);
        if (friend == null) return 0;
        var seen = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(24), m -> m.isAlive());
        for (var m : seen) m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 60, 0, false, false));
        return seen.size();
    }

    // --- the gremlin and the karakasa obake ---------------------------------------------------------------------------

    private long calmUntil;
    private final java.util.List<net.minecraft.core.BlockPos> furnaces = new java.util.ArrayList<>();

    /** Befriended, she tinkers with the furnaces near her friend (they cook twice as fast); wild, she pranks at night. */
    private void tickGremlin(ServerLevel level) {
        if (this.isBaby()) return;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend != null) {
            if (this.tickCount % 40 == 0) this.findFurnaces(level);
            for (net.minecraft.core.BlockPos p : this.furnaces) {
                if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity f) {
                    net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(level, p, level.getBlockState(p), f);   // a second tick each tick
                }
            }
            return;
        }
        this.furnaces.clear();
        if (!level.isDay() && level.getGameTime() >= this.calmUntil && this.bestAffection() < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION
                && this.tickCount % 300 == 0) this.tinker(level);
    }

    public int findFurnaces(ServerLevel level) {
        this.furnaces.clear();
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-6, -2, -6), this.blockPosition().offset(6, 2, 6))) {
            if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity) this.furnaces.add(p.immutable());
        }
        return this.furnaces.size();
    }

    /** A prank on what people built: a lever flipped or a door swung, within 6 blocks. True if she did one. */
    public boolean tinker(ServerLevel level) {
        java.util.List<net.minecraft.core.BlockPos> found = new java.util.ArrayList<>();
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-6, -2, -6), this.blockPosition().offset(6, 2, 6))) {
            var b = level.getBlockState(p).getBlock();
            if (b instanceof net.minecraft.world.level.block.LeverBlock || b instanceof net.minecraft.world.level.block.DoorBlock) found.add(p.immutable());
        }
        if (found.isEmpty()) return false;
        net.minecraft.core.BlockPos p = found.get(this.random.nextInt(found.size()));
        var s = level.getBlockState(p);
        if (s.getBlock() instanceof net.minecraft.world.level.block.LeverBlock lever) lever.pull(s, level, p, null);
        else if (s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door) door.setOpen(this, level, s, p, !door.isOpen(s));
        this.startTimedAction(Action.LAUGH, 30);
        return true;
    }

    // --- the ittan-momen's flight: a friend rides her through the air, steering where they look -------------------------

    @javax.annotation.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.species == GirlSpecies.ITTAN_MOMEN_DEVIL && this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        return this.species == GirlSpecies.ITTAN_MOMEN_DEVIL ? new Vec3(0.0, dims.height() * 0.35, 0.6) : super.getPassengerAttachmentPoint(passenger, dims, scale);
    }

    @Override
    public void travel(Vec3 input) {
        if (this.species == GirlSpecies.ITTAN_MOMEN_DEVIL && this.isVehicle() && this.getControllingPassenger() instanceof Player p) {
            this.setYRot(p.getYRot());
            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
            this.setNoGravity(true);
            Vec3 v = this.getDeltaMovement().scale(0.85);
            if (p.zza > 0) v = v.add(p.getLookAngle().scale(0.11));             // she flies where her rider looks
            else if (p.zza < 0) v = v.scale(0.6);
            this.setDeltaMovement(v);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            return;
        }
        super.travel(input);
    }

    /** The chochin-obake's lantern lights where she is at night; befriended, she keeps beside her friend through the dark. */
    private void tickLantern(ServerLevel level) {
        boolean night = !level.isDay();
        this.glow(level, night ? 15 : 0);
        if (!night || this.isBaby() || this.tickCount % 20 != 0 || this.isOrderedToSit()) return;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend != null && this.distanceToSqr(friend) > 3 * 3) this.getNavigation().moveTo(friend, 1.2);
    }

    /** A projectile about to hit a player: a befriended karakasa obake within 4 blocks of them catches it on her umbrella. */
    public static boolean shelter(Player player, net.minecraft.world.entity.projectile.Projectile projectile) {
        if (!(player.level() instanceof ServerLevel level) || projectile.getOwner() == player) return false;
        for (Devil d : level.getEntitiesOfClass(Devil.class, player.getBoundingBox().inflate(4),
                d -> d.species == GirlSpecies.KARAKASA_OBAKE_DEVIL && !d.isBaby() && d.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            projectile.discard();
            d.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.3F);
            d.getLookControl().setLookAt(projectile);
            return true;
        }
        return false;
    }

    // --- the oni -------------------------------------------------------------------------------------------------

    /** Setsubun: beans thrown at an oni who is not your friend send her running for a minute. */
    public void pelt(ServerLevel level, Player player) {
        this.beanThrower = player.getUUID();
        this.fleeUntil = level.getGameTime() + 1200;
        if (this.getTarget() == player) this.setTarget(null);
        this.setLastHurtByMob(null);
        this.playSound(SoundEvents.RAVAGER_HURT, 1.0F, 1.4F);
        level.sendParticles(new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.COCOA_BEANS)),
                this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
        player.displayClientMessage(Component.translatable("message.monstergirls.oni_beans", this.getDisplayName()), true);
    }

    /** At night her friend's nearest door (within 12 blocks of them) is hers to guard. */
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos guardedDoor() {
        if (this.species != GirlSpecies.ONI_DEVIL || this.level().isDay() || this.isBaby() || this.isOrderedToSit()) return null;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return null;
        net.minecraft.core.BlockPos best = null;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-12, -3, -12), friend.blockPosition().offset(12, 3, 12))) {
            if (this.level().getBlockState(p).is(net.minecraft.tags.BlockTags.DOORS) && (best == null || p.distSqr(friend.blockPosition()) < best.distSqr(friend.blockPosition()))) {
                best = p.immutable();
            }
        }
        return best;
    }

    /** She goes to stand at the door, and stays there. */
    class GuardDoorGoal extends net.minecraft.world.entity.ai.goal.Goal {
        @javax.annotation.Nullable
        private net.minecraft.core.BlockPos door;

        GuardDoorGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (Devil.this.tickCount % 40 != 0 || Devil.this.getTarget() != null) return false;
            this.door = Devil.this.guardedDoor();
            return this.door != null && Devil.this.distanceToSqr(Vec3.atCenterOf(this.door)) > 3 * 3;
        }

        @Override
        public boolean canContinueToUse() {
            return this.door != null && Devil.this.getTarget() == null && !Devil.this.level().isDay() && Devil.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Devil.this.getNavigation().moveTo(this.door.getX() + 0.5, this.door.getY(), this.door.getZ() + 0.5, 1.0);
        }
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
        if (this.species == GirlSpecies.KARAKASA_OBAKE_DEVIL && (stack.is(Items.PAPER) || stack.is(Items.STRING))) return true;
        if (this.species == GirlSpecies.CHOCHIN_OBAKE_DEVIL && (stack.is(Items.PAPER) || stack.is(net.minecraft.tags.ItemTags.CANDLES))) return true;
        if (this.species == GirlSpecies.ITTAN_MOMEN_DEVIL && (stack.is(Items.STRING) || stack.is(net.minecraft.tags.ItemTags.WOOL))) return true;
        if (this.species == GirlSpecies.ROKUROKUBI_DEVIL && stack.is(Items.SWEET_BERRIES)) return true;
        return stack.is(Items.SUGAR) || stack.is(Items.COOKIE) || stack.is(Items.CAKE) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.HONEY_BOTTLE);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        this.giveBack(player);
        return stack.is(Items.CAKE) || this.species == GirlSpecies.ONI_DEVIL && stack.is(Items.HONEY_BOTTLE) ? 3 : 1;
    }

    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.isDisguised() && !this.level().isClientSide) this.setDisguised(false);
        if (this.species == GirlSpecies.ITTAN_MOMEN_DEVIL && stack.isEmpty() && !player.isShiftKeyDown() && !this.isBaby() && !this.isVehicle()
                && this.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {   // a friend climbs aboard
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
                this.getNavigation().stop();
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.GREMLIN_DEVIL && stack.is(Items.COOKIE)) this.calmUntil = this.level().getGameTime() + 24000;   // no pranks for a day
        if (this.species == GirlSpecies.ONI_DEVIL && stack.is(Items.COCOA_BEANS) && this.getAffection(player) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {
            if (this.level() instanceof ServerLevel server) {
                stack.consume(1, player);
                this.pelt(server, player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case JIANG_SHI_DEVIL -> new ItemStack(r < 60 ? Items.PAPER : Items.GOLD_NUGGET, 3);
            case JACK_O_LANTERN_DEVIL -> new ItemStack(r < 50 ? Items.PUMPKIN_SEEDS : r < 80 ? Items.TORCH : Items.JACK_O_LANTERN, r < 80 ? 4 : 1);
            case GREMLIN_DEVIL -> new ItemStack(r < 40 ? Items.REDSTONE : r < 75 ? Items.IRON_NUGGET : r < 95 ? Items.REPEATER : Items.COMPARATOR, r < 75 ? 4 : 1);
            case KARAKASA_OBAKE_DEVIL -> new ItemStack(r < 40 ? Items.PAPER : r < 75 ? Items.STRING : Items.BAMBOO, 3);
            case ROKUROKUBI_DEVIL -> new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 75 ? Items.HONEY_BOTTLE : r < 97 ? Items.GLOW_INK_SAC : Items.SPYGLASS,
                    r < 40 ? 3 : r < 75 ? 1 : r < 97 ? 2 : 1);
            case ITTAN_MOMEN_DEVIL -> new ItemStack(r < 45 ? Items.STRING : r < 80 ? Items.WHITE_WOOL : r < 97 ? Items.FEATHER : Items.PHANTOM_MEMBRANE, r < 97 ? 3 : 1);
            case CHOCHIN_OBAKE_DEVIL -> new ItemStack(r < 40 ? Items.PAPER : r < 75 ? Items.CANDLE : r < 95 ? Items.TORCH : Items.LANTERN, r < 95 ? 3 : 1);
            case ONI_DEVIL -> new ItemStack(r < 45 ? Items.IRON_INGOT : r < 80 ? Items.BAMBOO : Items.HONEY_BOTTLE, r < 45 ? 2 : r < 80 ? 8 : 1);
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
        if (hit && this.species == GirlSpecies.ONI_DEVIL) this.playSound(SoundEvents.ANVIL_LAND, 0.5F, 0.7F);   // the iron club
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.KIKIMORA_DEVIL && !this.isBaby() && this.tickCount % 40 == 0) this.nightLatch(server, !server.isDay());
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
            case GREMLIN_DEVIL -> this.tickGremlin(server);
            case CHOCHIN_OBAKE_DEVIL -> this.tickLantern(server);
            case ROKUROKUBI_DEVIL -> {
                if (this.tickCount % 20 == 0) this.nightWatch(server, !server.isDay());
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

    // --- the kikimora --------------------------------------------------------------------------------------------

    /** At night, befriended: the wooden doors, trapdoors and fence gates left open within 8 blocks of her friend (not the one they stand in) are shut. How many. */
    public int nightLatch(ServerLevel level, boolean night) {
        if (!night) return 0;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return 0;
        var OPEN = net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN;
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-8, -3, -8), friend.blockPosition().offset(8, 3, 8))) {
            var s = level.getBlockState(p);
            if (!s.hasProperty(OPEN) || !s.getValue(OPEN) || p.distSqr(friend.blockPosition()) <= 4) continue;
            if (s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door && s.is(net.minecraft.tags.BlockTags.WOODEN_DOORS)) {
                if (s.getValue(net.minecraft.world.level.block.DoorBlock.HALF) != net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) continue;
                door.setOpen(this, level, s, p, false);
                n++;
            } else if (s.is(net.minecraft.tags.BlockTags.WOODEN_TRAPDOORS) || s.is(net.minecraft.tags.BlockTags.FENCE_GATES)) {
                level.setBlock(p, s.setValue(OPEN, false), 10);
                level.gameEvent(this, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CLOSE, p);
                n++;
            }
        }
        if (n > 0) this.playSound(net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE, 0.8F, 1.2F);
        return n;
    }
}
