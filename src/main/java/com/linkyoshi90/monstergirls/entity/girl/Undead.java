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
    /** The lich's phylactery, once she has placed it. */
    @Nullable
    private BlockPos phylactery;
    private long nextCast;

    public Undead(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.HAND_UNDEAD) this.navigation = this.createNavigation(level);   // the first one was made before she had a species
        boolean creeper = species == GirlSpecies.LIVING_DOLL_UNDEAD || species == GirlSpecies.YUKI_ONNA_UNDEAD;
        if (creeper) this.goalSelector.addGoal(1, new StalkGoal(this, 1.0, 1.6, true, t -> t instanceof Player));
        if (species == GirlSpecies.LIVING_DOLL_UNDEAD) this.goalSelector.addGoal(2, new ShelfGoal());
        if (species == GirlSpecies.DULLAHAN_UNDEAD) {                       // the dullahan fears gold
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.3, p -> p instanceof Player pl && fearsGold(pl)));
        }
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
            case HAND_UNDEAD -> girlAttributes(12, 0.33, 2);
            case YUREI_UNDEAD -> girlAttributes(18, 0.3, 2);
            case LIVING_DOLL_UNDEAD -> girlAttributes(14, 0.3, 0);
            case WILL_O_WISP_UNDEAD -> girlAttributes(16, 0.3, 3);
            case YUKI_ONNA_UNDEAD -> girlAttributes(24, 0.28, 3);
            case PHANTOM_UNDEAD -> girlAttributes(20, 0.32, 4);
            case VAMPIRE_UNDEAD -> girlAttributes(28, 0.3, 3);
            case DULLAHAN_UNDEAD -> girlAttributes(30, 0.28, 5).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 4);
            case BANSHEE_UNDEAD -> girlAttributes(22, 0.28, 2);
            case FRANKENSTEIN_UNDEAD -> girlAttributes(50, 0.25, 10).add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.8);
            case LICH_UNDEAD -> girlAttributes(60, 0.25, 6).add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.5);
            case LLORONA_UNDEAD -> girlAttributes(26, 0.28, 2);
            case MUMMY_UNDEAD -> girlAttributes(28, 0.24, 4).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 3);
            default -> girlAttributes(24, 0.25, 4);
        };
    }

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.PHANTOM_UNDEAD || this.species == GirlSpecies.WILL_O_WISP_UNDEAD || this.species == GirlSpecies.YUREI_UNDEAD;
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
            case ZOMBIE_UNDEAD, LIVING_DOLL_UNDEAD, YUKI_ONNA_UNDEAD, PHANTOM_UNDEAD, LICH_UNDEAD -> true;
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

    /** No food calms a lich: only breaking her phylactery does. */
    @Override
    protected boolean calmsHer(ItemStack stack) {
        if (this.species == GirlSpecies.LICH_UNDEAD) return false;
        return this.species == GirlSpecies.ZOMBIE_UNDEAD ? stack.is(Items.ROTTEN_FLESH) : stack.is(Items.PUMPKIN_PIE);
    }

    // --- the mummy -------------------------------------------------------------------------------------------------

    private long rewrapAt = -1, spinEnds = -1, nextUnravel;

    /** How far her bandages have come off: 0 wrapped, 1 the outer layer gone, 2 down to her skimpy wraps. */
    public int unwrapped() {
        return this.hasFlag(FLAG_UNWRAP_B) ? 2 : this.hasFlag(FLAG_UNWRAP_A) ? 1 : 0;
    }

    public void setUnwrapped(int n) {
        this.setFlag(FLAG_UNWRAP_A, n >= 1);
        this.setFlag(FLAG_UNWRAP_B, n >= 2);
    }

    /** One more layer off (string and paper drop); at her skimpy wraps she hurries to wrap herself up again. */
    public void unravel(ServerLevel level, @Nullable Player puller) {
        int n = this.unwrapped();
        if (n >= 2 || this.spinEnds > level.getGameTime() || level.getGameTime() < this.nextUnravel) return;
        this.setUnwrapped(n + 1);
        this.spawnAtLocation(new ItemStack(this.random.nextBoolean() ? Items.STRING : Items.PAPER, 1 + this.random.nextInt(2)));
        this.playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.2F);
        if (n + 1 == 2) {
            this.rewrapAt = level.getGameTime() + 30;
            if (puller != null) puller.displayClientMessage(Component.translatable("message.monstergirls.mummy_flustered", this.getDisplayName()), true);
        }
    }

    private void tickWrapping(ServerLevel level) {
        long now = level.getGameTime();
        if (this.rewrapAt > 0 && now >= this.rewrapAt) {                   // flustered, she spins round and round, wrapping up again
            this.rewrapAt = -1;
            this.spinEnds = now + 40;
            this.startTimedAction(Action.SPIN, 40);
            this.getNavigation().stop();
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.4F);
        }
        if (this.spinEnds > 0 && now >= this.spinEnds) {
            this.spinEnds = -1;
            this.setUnwrapped(0);
            this.nextUnravel = now + 1200;
        }
    }

    /** A wild mummy near an unopened tomb chest keeps whoever she does not know from it (as Anubis does). */
    @Nullable
    public static Undead mummyGuardOf(Level level, BlockPos pos, Player player) {
        if (player.isCreative() || !(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity box)
                || box.getLootTable() == null) return null;
        for (Undead u : level.getEntitiesOfClass(Undead.class, new net.minecraft.world.phys.AABB(pos).inflate(8),
                u -> u.isAlive() && !u.isBaby() && u.species == GirlSpecies.MUMMY_UNDEAD && u.getAffection(player) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            return u;
        }
        return null;
    }

    public void warnOffTomb(Player player) {
        this.getLookControl().setLookAt(player);
        this.playSound(SoundEvents.HUSK_AMBIENT, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("message.monstergirls.mummy_guard", this.getDisplayName()), true);
    }

    // --- La Llorona ----------------------------------------------------------------------------------------------

    /** The night she last brought a lost one home: she does not weep for the rest of it. */
    private long comfortedNight = -1;
    @Nullable
    private net.minecraft.world.entity.AgeableMob lost;

    /** Which night it is (13000..23000 of a day and the dawn after count as one). */
    private static long nightOf(ServerLevel level) {
        return (level.getDayTime() + 11000L) / 24000L;
    }

    public static boolean isNight(ServerLevel level) {
        long t = level.getDayTime() % 24000L;
        return t >= 13000 && t < 23000;
    }

    public boolean isWeeping() {
        return this.level() instanceof ServerLevel server && isNight(server) && this.comfortedNight != nightOf(server);
    }

    /** A young one far from any grown one of its kind. */
    private static boolean isLost(ServerLevel level, net.minecraft.world.entity.AgeableMob m) {
        return m.isBaby() && m.isAlive() && level.getEntitiesOfClass(net.minecraft.world.entity.AgeableMob.class, m.getBoundingBox().inflate(12),
                a -> a.getType() == m.getType() && !a.isBaby()).isEmpty();
    }

    /** The nearest grown one of its kind, where she takes it. */
    @Nullable
    private static net.minecraft.world.entity.AgeableMob homeOf(ServerLevel level, net.minecraft.world.entity.AgeableMob m) {
        net.minecraft.world.entity.AgeableMob best = null;
        for (var a : level.getEntitiesOfClass(net.minecraft.world.entity.AgeableMob.class, m.getBoundingBox().inflate(48), a -> a.getType() == m.getType() && !a.isBaby())) {
            if (best == null || a.distanceToSqr(m) < best.distanceToSqr(m)) best = a;
        }
        return best;
    }

    /** At night she weeps and searches for lost young ones, and leads them home to their own kind. */
    public void searchTheRiver(ServerLevel level) {
        if (!isNight(level)) {
            this.lost = null;
            return;
        }
        if (this.isWeeping() && this.tickCount % 40 == 0) {
            level.sendParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getEyeY(), this.getZ(), 3, 0.15, 0.05, 0.15, 0.0);
            if (this.random.nextInt(3) == 0) this.playSound(SoundEvents.ALLAY_HURT, 0.6F, 0.5F);
        }
        if (this.lost == null || !this.lost.isAlive()) {
            this.lost = this.nearest(net.minecraft.world.entity.AgeableMob.class, 16, m -> isLost(level, m) && homeOf(level, m) != null);
            if (this.lost == null) return;
        }
        net.minecraft.world.entity.AgeableMob home = homeOf(level, this.lost);
        if (home == null) {
            this.lost = null;
            return;
        }
        if (this.lost.distanceToSqr(home) < 6 * 6) {                    // home: she can rest tonight
            this.bringHome(level, this.lost);
            this.lost = null;
            return;
        }
        if (this.distanceToSqr(this.lost) > 4 * 4) this.getNavigation().moveTo(this.lost, 1.0);
        else {
            this.getNavigation().moveTo(home, 0.8);
            this.lost.getNavigation().moveTo(this, 1.0);               // the little one follows her
        }
    }

    /** A lost one is home: she stops weeping for the night, and the players who walked with her are thanked. */
    public void bringHome(ServerLevel level, net.minecraft.world.entity.LivingEntity found) {
        this.comfortedNight = nightOf(level);
        this.hearts(level, 4);
        level.sendParticles(ParticleTypes.HEART, found.getX(), found.getY() + found.getBbHeight() + 0.2, found.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.0F, 0.9F);
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16), p -> !p.isSpectator())) {
            this.addAffection(p, 3);
            p.displayClientMessage(Component.translatable("message.monstergirls.llorona_home", this.getDisplayName()), true);
        }
    }

    // --- the lich ------------------------------------------------------------------------------------------------

    @Nullable
    public BlockPos getPhylactery() {
        return this.phylactery;
    }

    /** She hides her soul in a phylactery close by (only while she is wild). */
    private void placePhylactery(ServerLevel level) {
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 24; i++) {
            BlockPos p = o.offset(this.random.nextInt(9) - 4, this.random.nextInt(3) - 1, this.random.nextInt(9) - 4);
            if (!canHoldBlock(level, p)) continue;
            level.setBlock(p, com.linkyoshi90.monstergirls.registry.ModBlocks.PHYLACTERY.get().defaultBlockState(), 3);
            this.phylactery = p.immutable();
            this.setPersistenceRequired();                             // a mini-boss with a soul to guard stays put
            return;
        }
    }

    private boolean phylacteryStands() {
        return this.phylactery != null && this.level().getBlockState(this.phylactery).is(com.linkyoshi90.monstergirls.registry.ModBlocks.PHYLACTERY.get());
    }

    /** Her phylactery is gone: she is free, and peaceful from now on. */
    public void free(ServerLevel level) {
        this.phylactery = null;
        this.setFlag(FLAG_CALM, true);
        this.setTarget(null);
        this.setLastHurtByMob(null);
        level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.2, this.getZ(), 30, 0.5, 0.8, 0.5, 0.05);
        this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.5F, 0.8F);
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32))) {
            p.displayClientMessage(Component.translatable("message.monstergirls.lich_freed", this.getDisplayName()), true);
        }
    }

    /** A phylactery was removed: whichever lich it belonged to is freed. */
    public static void phylacteryBroken(ServerLevel level, BlockPos pos) {
        for (Undead lich : level.getEntitiesOfClass(Undead.class, new net.minecraft.world.phys.AABB(pos).inflate(64), u -> pos.equals(u.phylactery))) {
            lich.free(level);
        }
    }

    /** Slowness and weakness on her target, every four seconds she can see it. */
    private void cast(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target == null || level.getGameTime() < this.nextCast || this.distanceToSqr(target) > 16 * 16 || !this.hasLineOfSight(target)) return;
        this.nextCast = level.getGameTime() + 80;
        this.startTimedAction(Action.CAST, 20);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1, target.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.8F);
    }

    /** Freed and fond of you, she trades what she knows for books: experience for each one. */
    private boolean takeBook(ServerLevel level, Player player, ItemStack stack) {
        int xp = stack.is(Items.ENCHANTED_BOOK) ? 30 : stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK) ? 20 : stack.is(Items.BOOK) ? 10 : 0;
        if (xp == 0) return false;
        stack.consume(1, player);
        net.minecraft.world.entity.ExperienceOrb.award(level, player.position(), xp);
        this.addAffection(player, 2);
        this.startTimedAction(Action.CAST, 20);
        this.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        return true;
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
        if (this.species == GirlSpecies.MUMMY_UNDEAD && source.getEntity() != null && this.level() instanceof ServerLevel server && !this.isInvulnerableTo(source)) {
            this.unravel(server, null);                                      // a blow loosens her bandages
        }
        if (this.species == GirlSpecies.LICH_UNDEAD && amount >= this.getHealth() && this.level() instanceof ServerLevel server && this.phylacteryStands()
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            BlockPos p = this.phylactery;                                    // she cannot die: she reforms beside her phylactery
            server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1, this.getZ(), 20, 0.4, 0.8, 0.4, 0.05);
            this.teleportTo(p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5);
            this.setHealth(this.getMaxHealth() / 2);
            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.5F, 0.6F);
            return false;
        }
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
        if (this.species == GirlSpecies.MUMMY_UNDEAD && stack.isEmpty() && player.isShiftKeyDown() && !this.isBaby()
                && this.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {   // a gentle pull at a loose end
            if (this.level() instanceof ServerLevel server) this.unravel(server, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.LICH_UNDEAD && this.isCalm() && this.getAffection(player) >= 10 && this.level() instanceof ServerLevel server
                && this.takeBook(server, player, stack)) {
            return InteractionResult.SUCCESS;
        }
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
        if (this.species == GirlSpecies.MUMMY_UNDEAD && (stack.is(Items.PAPER) || stack.is(Items.GOLD_NUGGET))) return true;
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
            case LICH_UNDEAD -> new ItemStack(r < 50 ? Items.LAPIS_LAZULI : r < 85 ? Items.EXPERIENCE_BOTTLE : Items.ENCHANTED_BOOK, r < 50 ? 4 : 1);
            case LIVING_DOLL_UNDEAD -> new ItemStack(r < 50 ? Items.STRING : Items.PINK_WOOL);
            case LLORONA_UNDEAD -> new ItemStack(r < 50 ? Items.ORANGE_TULIP : r < 85 ? Items.CANDLE : Items.GHAST_TEAR, r < 50 ? 3 : 1);
            case MUMMY_UNDEAD -> new ItemStack(r < 40 ? Items.GOLD_NUGGET : r < 70 ? Items.PAPER : r < 95 ? Items.LAPIS_LAZULI : Items.GOLD_INGOT, r < 70 ? 4 : 2);
            default -> new ItemStack(r < 50 ? Items.BONE : r < 85 ? Items.SOUL_SAND : Items.ROTTEN_FLESH);
        };
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** The Hand runs up walls on its fingertips, like a spider. */
    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(net.minecraft.world.level.Level level) {
        return this.species == GirlSpecies.HAND_UNDEAD ? new net.minecraft.world.entity.ai.navigation.WallClimberNavigation(this, level) : super.createNavigation(level);
    }

    @Override
    public boolean onClimbable() {
        return this.species == GirlSpecies.HAND_UNDEAD && this.horizontalCollision || super.onClimbable();
    }

    /** The Hand holds the monsters within 6 of its friend by the ankle: nearly stopped for two seconds. Returns how many, -1 without a friend. */
    public int firmGrip(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        var near = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(6));
        for (var m : near) m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
        return near.size();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.HAND_UNDEAD && this.level() instanceof ServerLevel floor && !this.isBaby() && this.tickCount % 40 == 0) this.firmGrip(floor);
        if (this.species == GirlSpecies.JIANGSHI_UNDEAD && this.level() instanceof net.minecraft.server.level.ServerLevel grave && !this.isBaby() && this.tickCount % 40 == 0) this.stiffHop(grave);
        if (this.species == GirlSpecies.GASHADOKURO_UNDEAD && this.level() instanceof net.minecraft.server.level.ServerLevel grasp && !this.isBaby() && this.tickCount % 60 == 0) this.giantsGrasp(grasp);
        if (this.species == GirlSpecies.GHOUL_UNDEAD && this.level() instanceof net.minecraft.server.level.ServerLevel grave && !this.isBaby() && this.tickCount % 20 == 0) this.carrion(grave);
        if (this.species == GirlSpecies.KASURENIJIMI_UNDEAD && this.level() instanceof ServerLevel blur && !this.isBaby() && this.tickCount % 20 == 0) this.smudge(blur);
        if (this.species == GirlSpecies.DRAUGR_UNDEAD && this.level() instanceof ServerLevel barrow && !this.isBaby() && this.tickCount % 20 == 0) this.layDead(barrow);
        if (this.species == GirlSpecies.YUREI_UNDEAD && this.level() instanceof ServerLevel night && !this.isBaby() && this.tickCount % 20 == 0) this.haunt(night, !night.isDay());
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.MUMMY_UNDEAD) this.tickWrapping(server);
        if (this.species == GirlSpecies.WILL_O_WISP_UNDEAD) {
            this.glow(server, 11);
            if (this.tickCount % 4 == 0) server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.2, 0.2, 0.2, 0.01);
            this.guide(server);
        }
        if (this.species == GirlSpecies.BANSHEE_UNDEAD && this.tickCount % 20 == 0) this.keen(server);
        if (this.species == GirlSpecies.LICH_UNDEAD && !this.isBaby()) {
            if (this.phylactery == null && !this.isCalm() && !this.hasAnyAffection() && this.tickCount % 20 == 0) this.placePhylactery(server);
            else if (this.phylactery != null && this.tickCount % 20 == 0 && server.isLoaded(this.phylactery) && !this.phylacteryStands()) this.free(server);
            if (!this.isCalm()) this.cast(server);
        }
        if (this.species == GirlSpecies.LLORONA_UNDEAD && !this.isBaby() && this.tickCount % 10 == 0) this.searchTheRiver(server);
        if (this.wantsSteed) this.mountSteed(server);
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
            case MUMMY_UNDEAD -> SoundEvents.HUSK_AMBIENT;
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
        tag.putLong("ComfortedNight", this.comfortedNight);
        if (this.phylactery != null) tag.put("Phylactery", net.minecraft.nbt.NbtUtils.writeBlockPos(this.phylactery));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.strongUntil = tag.getLong("StrongUntil");
        this.comfortedNight = tag.contains("ComfortedNight") ? tag.getLong("ComfortedNight") : -1;
        this.phylactery = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "Phylactery").orElse(null);
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

    // --- the dullahan ------------------------------------------------------------------------------------------------

    /** Anything gold in either hand (everything a piglin loves). */
    public static boolean fearsGold(Player p) {
        return p.getMainHandItem().is(net.minecraft.tags.ItemTags.PIGLIN_LOVED) || p.getOffhandItem().is(net.minecraft.tags.ItemTags.PIGLIN_LOVED);
    }

    private boolean wantsSteed;

    /** Half of the dullahans that come out at night ride a skeleton horse (mounted on her first tick, once she is in the world). */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   net.minecraft.world.entity.MobSpawnType type, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        var data = super.finalizeSpawn(level, difficulty, type, group);
        if (this.species == GirlSpecies.DULLAHAN_UNDEAD && type == net.minecraft.world.entity.MobSpawnType.NATURAL && this.random.nextBoolean()) this.wantsSteed = true;
        return data;
    }

    public void setWantsSteed(boolean wants) {
        this.wantsSteed = wants;
    }

    private void mountSteed(ServerLevel level) {
        this.wantsSteed = false;
        if (this.isPassenger()) return;
        var horse = net.minecraft.world.entity.EntityType.SKELETON_HORSE.create(level);
        if (horse == null) return;
        horse.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(this.blockPosition()), net.minecraft.world.entity.MobSpawnType.JOCKEY, null);
        horse.setTamed(true);                                               // an untamed horse bucks its rider off (vanilla's skeleton riders do the same)
        level.addFreshEntity(horse);
        this.startRiding(horse, true);
    }

    // --- the yurei -----------------------------------------------------------------------------------------------

    /** At night the monsters within 8 blocks of her friend grow weak (Weakness I). How many, or -1 by day or without a friend. */
    public int haunt(ServerLevel level, boolean night) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (!night || friend == null) return -1;
        int n = 0;
        for (var m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, friend.getBoundingBox().inflate(8), m -> m instanceof net.minecraft.world.entity.monster.Enemy && m.isAlive())) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 60, 0, true, true));
            n++;
        }
        return n;
    }

    // --- the kasurenijimi and the draugr (nineteenth wave) ----------------------------------------------------------

    /** Her friend within 12 blocks, sneaking, blurs out of sight (Invisibility for two seconds). True if she hid them. */
    public boolean smudge(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 12);
        if (friend == null || !friend.isShiftKeyDown()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY, 40, 0, true, false));
        return true;
    }

    /** The undead within 16 blocks of her friend that are after them give up. How many, or -1 without a friend. */
    public int layDead(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.world.entity.Mob m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, friend.getBoundingBox().inflate(16),
                m -> m.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD) && m.getTarget() == friend)) {
            m.setTarget(null);
            n++;
        }
        return n;
    }

    // --- the ghoul (twentieth wave) ---------------------------------------------------------------------------------

    /** She eats one piece of rotten flesh lying within 8 blocks of her friend, and it heals her by two. 1 if she ate, 0 if there
     *  was none, -1 without a friend. */
    public int carrion(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, friend.getBoundingBox().inflate(8),
                e -> e.getItem().is(net.minecraft.world.item.Items.ROTTEN_FLESH))) {
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) item.discard();
            this.heal(2.0F);
            this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 1.0F, 0.7F);
            return 1;
        }
        return 0;
    }

    // --- the gashadokuro (twenty-first wave) ------------------------------------------------------------------------

    /** She seizes the nearest monster within 6 blocks of her friend and lifts it up: Levitation II for two seconds, then it falls.
     *  1 if she seized one, 0 if none was near, -1 without a friend. */
    public int giantsGrasp(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(6));
        if (m == null) return 0;
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION, 40, 1), this);
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.SKELETON_AMBIENT, 1.0F, 0.5F);
        return 1;
    }

    /**
     * The jiangshi's hop (twenty-fourth wave): the monsters within 3 of her, while her friend is near, are struck stiff: 2 damage and
     * Slowness VI for 2 seconds. The number struck, -1 without a friend.
     */
    public int stiffHop(net.minecraft.server.level.ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24) == null) return -1;
        int struck = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, this.getBoundingBox().inflate(3), m -> m.isAlive())) {
            m.hurt(level.damageSources().mobAttack(this), 2.0F);
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 5));
            struck++;
        }
        return struck;
    }

    /** The last day the nasnas took a blow. */
    private long lastBlow = -1;

    /** Would this nasnas take the killing blow for this player now? She is spent for the day if she does (GirlEvents asks). */
    public static boolean takesTheBlow(net.minecraft.world.entity.player.Player player, float amount) {
        if (amount < player.getHealth() + player.getAbsorptionAmount() || !(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return false;
        long day = level.getDayTime() / 24000L;
        for (Undead u : level.getEntitiesOfClass(Undead.class, player.getBoundingBox().inflate(16),
                u -> u.species == GirlSpecies.NASNAS_UNDEAD && !u.isBaby() && u.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            if (u.lastBlow == day) continue;
            u.lastBlow = day;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.4, 0.5, 0.4, 0.02);
            u.playSound(net.minecraft.sounds.SoundEvents.TOTEM_USE, 0.6F, 1.4F);
            return true;
        }
        return false;
    }
}
