package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The hoofed family (centaur_field_guide.html): horse, zebra, reindeer, mountain goat, alpaca, tapir, camel, dromedary,
 * kelpie. Herd and hoof:
 * they graze, follow their lead mare, kick back at whatever hurts them from behind, and are won over by grooming
 * (a brush). At high friendship she lets you ride on her back (a saddle makes her faster) and wears a chestplate you
 * give her (it shows over her torso and adds to her health); the devoted alpaca carries a chest. Any centaur who sees
 * a player sprinting by gallops alongside, grinning from ear to ear. Camels carry anyone, give milk and breed with
 * vanilla camels; the kelpie is ridden under a saddle, swims faster than a boat, and hunts strangers at night.
 */
public class Centaur extends AbstractMonsterGirl implements net.minecraft.world.entity.PlayerRideableJumping {
    public static final int RIDE_AFFECTION = BED_AFFECTION;
    public static final int ARMOUR_AFFECTION = 10;
    private static final net.minecraft.resources.ResourceLocation ARMOUR_HEALTH = com.linkyoshi90.monstergirls.MonsterGirlsMod.id("centaur_armour_health");
    private static final int GROOM_COOLDOWN = 3000;
    /** A full gallop lasts 15 seconds; at zero she slows to a walk until she has caught her breath (a third back). */
    static final float MAX_STAMINA = 300.0F;

    private long nextGroom;
    private long nextKick;
    private long nextAlarm;
    private int fleeceTicks;
    private float stamina = MAX_STAMINA;
    private boolean winded;
    private boolean leadMare;
    private final SimpleContainer pack = new SimpleContainer(27);
    private int camelLove;
    private long nextMilk;
    /** Expecting by a vanilla camel: half her young are camel calves. */
    private boolean camelSired;

    public Centaur(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5) {
            @Override
            public boolean canUse() {
                return Centaur.this.getHealth() < Centaur.this.getMaxHealth() * 0.5F && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Centaur.this.startTimedAction(species == GirlSpecies.GOAT_CENTAUR ? Action.REAR : Action.KICK, 16);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(3, new RunAlongGoal());
        if (species == GirlSpecies.UNICORN_CENTAUR) {                    // she keeps away from impure hearts
            this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, Player.class, 10.0F, 1.1, 1.5, e -> e instanceof Player p && impure(p)));
        }
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.APPLE, Items.GOLDEN_CARROT, Items.CARROT, Items.SUGAR, Items.WHEAT), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::leader, 1.0, 10.0F, 5.0F));
        this.goalSelector.addGoal(6, new EatBlockGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (species == GirlSpecies.GOAT_CENTAUR || species == GirlSpecies.ZEBRA_CENTAUR) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> { Player p = this.bondedPlayer(BED_AFFECTION, 16); return p != null && m.distanceToSqr(p) < 6 * 6; }));
        }
    }

    // --- the halberelk ---------------------------------------------------------------------------------------------

    /**
     * Her friend has cut a log: a befriended halberelk within 8 blocks fells the rest of the tree. Only a tree (natural
     * leaves touch it), never a log cabin, and no more than 96 logs. Returns how many logs she felled.
     */
    public static int fell(ServerLevel level, Player player, net.minecraft.core.BlockPos cut, net.minecraft.world.level.block.state.BlockState log) {
        if (player.isShiftKeyDown() || !log.is(net.minecraft.tags.BlockTags.LOGS)) return 0;
        Centaur elk = null;
        for (Centaur c : level.getEntitiesOfClass(Centaur.class, new net.minecraft.world.phys.AABB(cut).inflate(8),
                c -> c.species == GirlSpecies.HALBERELK_CENTAUR && !c.isBaby() && c.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            elk = c;
        }
        if (elk == null) return 0;
        java.util.Set<net.minecraft.core.BlockPos> tree = new java.util.LinkedHashSet<>();
        java.util.ArrayDeque<net.minecraft.core.BlockPos> open = new java.util.ArrayDeque<>(java.util.List.of(cut));
        boolean natural = false;
        while (!open.isEmpty() && tree.size() < 96) {
            net.minecraft.core.BlockPos p = open.poll();
            if (!tree.add(p)) continue;
            for (net.minecraft.core.BlockPos q : net.minecraft.core.BlockPos.betweenClosed(p.offset(-1, 0, -1), p.offset(1, 1, 1))) {
                var s = level.getBlockState(q);
                if (s.is(log.getBlock()) && !tree.contains(q) && q.getY() >= cut.getY()) open.add(q.immutable());
                if (s.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock && !s.getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT)) natural = true;
            }
        }
        if (!natural) return 0;
        int n = 0;
        for (net.minecraft.core.BlockPos p : tree) {
            if (p.equals(cut)) continue;
            if (level.destroyBlock(p, true, player)) n++;
        }
        elk.getLookControl().setLookAt(cut.getX() + 0.5, cut.getY() + 0.5, cut.getZ() + 0.5);
        elk.startTimedAction(Action.REAR, 20);
        elk.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 0.8F, 0.9F);
        return n;
    }

    // --- the momoshika ----------------------------------------------------------------------------------------------

    /** By day her friend within 8 blocks of her regenerates. True if she healed them. */
    public boolean blossom(ServerLevel level) {
        if (!level.isDay()) return false;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null) return false;
        friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.5, 0.3, 0.5, 0.0);
        return true;
    }

    // --- the sphinx -------------------------------------------------------------------------------------------------

    /** Her riddles, each answered with an item (the lang key is riddle.monstergirls.N). */
    public static final java.util.List<net.minecraft.world.item.Item> RIDDLE_ANSWERS = java.util.List.of(Items.CLOCK, Items.COMPASS, Items.SPONGE,
            Items.NOTE_BLOCK, Items.GLASS_BOTTLE, Items.TORCH, Items.BELL, Items.BOOK, Items.WATER_BUCKET);
    private final java.util.Map<java.util.UUID, Integer> riddles = new java.util.HashMap<>();

    /** The riddle she has put to this player, or -1. */
    public int riddleFor(Player player) {
        return this.riddles.getOrDefault(player.getUUID(), -1);
    }

    public void askRiddle(Player player, int which) {
        this.riddles.put(player.getUUID(), which);
        this.getLookControl().setLookAt(player);
        player.displayClientMessage(Component.translatable("message.monstergirls.sphinx_asks", this.getDisplayName(),
                Component.translatable("riddle.monstergirls." + which)), false);
    }

    /** The answer, held out: right earns a reward, wrong earns a pounce. Returns whether it was right. */
    public boolean answer(ServerLevel level, Player player, ItemStack held) {
        int which = this.riddleFor(player);
        this.riddles.remove(player.getUUID());
        if (which >= 0 && held.is(RIDDLE_ANSWERS.get(which))) {
            ItemStack reward = this.random.nextInt(3) == 0
                    ? net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(this.random, new ItemStack(Items.BOOK), 20, level.registryAccess(), java.util.Optional.empty())
                    : new ItemStack(this.random.nextBoolean() ? Items.EMERALD : Items.GOLD_INGOT, 3);
            if (!player.getInventory().add(reward)) player.drop(reward, false);
            this.addAffection(player, 5);
            this.hearts(level, 4);
            player.displayClientMessage(Component.translatable("message.monstergirls.sphinx_right", this.getDisplayName()), false);
            return true;
        }
        this.startTimedAction(Action.REAR, 20);                      // the pounce
        player.hurt(this.damageSources().mobAttack(this), 4.0F);
        Vec3 away = player.position().subtract(this.position()).normalize();
        player.knockback(1.2, -away.x, -away.z);
        this.addAffection(player, -2);
        player.displayClientMessage(Component.translatable("message.monstergirls.sphinx_wrong", this.getDisplayName()), false);
        return false;
    }

    // --- the tusktaur and the aurorantler ------------------------------------------------------------------------

    /** The shaggy ones of the snow never freeze. */
    @Override
    public boolean canFreeze() {
        return this.species != GirlSpecies.TUSKTAUR_CENTAUR && this.species != GirlSpecies.AURORANTLER_CENTAUR && super.canFreeze();
    }

    /** The tusk sweep: her blow knocks back everything in front of her, not just what she aimed at (never her friends). */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.TUSKTAUR_CENTAUR) this.sweep(target);
        return hit;
    }

    public int sweep(net.minecraft.world.entity.Entity aimed) {
        net.minecraft.world.phys.Vec3 look = this.getLookAngle().multiply(1, 0, 1).normalize();
        int n = 0;
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0), e -> e != this && e != aimed && e.isAlive()
                && !(e instanceof Player p && this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) && !(e instanceof com.linkyoshi90.monstergirls.entity.Bonded))) {
            net.minecraft.world.phys.Vec3 to = e.position().subtract(this.position()).multiply(1, 0, 1);
            if (to.lengthSqr() < 1.0E-4 || to.normalize().dot(look) < 0.3) continue;      // only what is in front of her
            e.hurt(this.damageSources().mobAttack(this), 4.0F);
            e.knockback(1.5, -to.x, -to.z);
            n++;
        }
        this.playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ATTACK, 1.0F, 0.7F);
        return n;
    }

    /** At night the aurorantler's antlers light the snow, and her friend sees in the dark beside her. */
    public void aurora(ServerLevel level) {
        boolean night = !level.isDay();
        this.glow(level, night ? 10 : 0);
        if (night && this.tickCount % 40 == 0) {
            Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
            if (friend != null) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 300, 0, true, false));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case STAGEL_CENTAUR -> girlAttributes(20, 0.32, 2).add(Attributes.JUMP_STRENGTH, 0.8);
            case LAMASSU_CENTAUR -> girlAttributes(50, 0.3, 8);
            case HORSE_CENTAUR -> girlAttributes(30, 0.3, 4);
            case ZEBRA_CENTAUR -> girlAttributes(30, 0.3, 6);
            case GOAT_CENTAUR -> girlAttributes(26, 0.28, 5).add(Attributes.JUMP_STRENGTH, 0.75);
            case ALPACA_CENTAUR -> girlAttributes(24, 0.25, 2);
            case TAPIR_CENTAUR -> girlAttributes(32, 0.24, 3);
            case CAMEL_CENTAUR -> girlAttributes(36, 0.26, 3);
            case DROMEDARY_CENTAUR -> girlAttributes(32, 0.28, 3);
            case KELPIE_CENTAUR -> girlAttributes(30, 0.32, 6);
            case UNICORN_CENTAUR -> girlAttributes(30, 0.32, 3);
            case TUSKTAUR_CENTAUR -> girlAttributes(60, 0.24, 9).add(Attributes.ATTACK_KNOCKBACK, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
            case AURORANTLER_CENTAUR -> girlAttributes(26, 0.3, 3);
            case SPHINX_CENTAUR -> girlAttributes(50, 0.3, 8);
            case MOMOSHIKA_CENTAUR -> girlAttributes(26, 0.3, 3);
            case PRAIRHORN_CENTAUR -> girlAttributes(26, 0.42, 3).add(Attributes.JUMP_STRENGTH, 0.8);
            case HALBERELK_CENTAUR -> girlAttributes(40, 0.28, 6).add(Attributes.ATTACK_KNOCKBACK, 1.0);
            case BELLMARE_CENTAUR -> girlAttributes(28, 0.28, 2);
            case VICUNERO_CENTAUR -> girlAttributes(24, 0.32, 2).add(Attributes.JUMP_STRENGTH, 0.75);
            case CARIBOU_CENTAUR -> girlAttributes(30, 0.3, 3);
            default -> girlAttributes(28, 0.28, 3);
        };
    }

    // --- the unicorn ----------------------------------------------------------------------------------------------

    /** A heart weighed down by villager or iron golem kills (Anubis's measure): the unicorn will have nothing to do with it. */
    public static boolean impure(Player player) {
        return player instanceof net.minecraft.server.level.ServerPlayer sp && Canid.heartWeight(sp) > 0;
    }

    @Override
    public void addAffection(Player player, int amount) {
        if (this.species == GirlSpecies.UNICORN_CENTAUR && amount > 0 && impure(player)) return;   // nothing she will take from those hands
        super.addAffection(player, amount);
    }

    /** Her horn: poison and wither leave her friends, and zombie villagers near her are cured much faster. */
    public void tendWithHorn(ServerLevel level) {
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8), p -> this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON) || p.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
                p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
                p.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            }
        }
        for (net.minecraft.world.entity.monster.ZombieVillager zv : level.getEntitiesOfClass(net.minecraft.world.entity.monster.ZombieVillager.class,
                this.getBoundingBox().inflate(8), net.minecraft.world.entity.monster.ZombieVillager::isConverting)) {
            hastenCure(zv, 200);
        }
    }

    // ponytail: reflection on the cure's countdown (Mojang name, which NeoForge keeps at runtime); swap for an access transformer if it ever breaks
    private static final java.lang.reflect.Field CURE_TIME = cureField();

    @Nullable
    private static java.lang.reflect.Field cureField() {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.monster.ZombieVillager.class.getDeclaredField("villagerConversionTime");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** Takes this many ticks off a zombie villager's cure. */
    public static void hastenCure(net.minecraft.world.entity.monster.ZombieVillager zv, int ticks) {
        if (CURE_TIME == null) return;
        try {
            CURE_TIME.setInt(zv, Math.max(1, CURE_TIME.getInt(zv) - ticks));
        } catch (IllegalAccessException ignored) {
        }
    }

    public boolean isCamel() {
        return this.species == GirlSpecies.CAMEL_CENTAUR || this.species == GirlSpecies.DROMEDARY_CENTAUR;
    }

    /** Camels carry anyone; the kelpie only under a saddle and the pegasus under her golden bridle (put on by a friend); the others at high friendship. */
    private boolean letsRide(Player player) {
        if (this.isCamel()) return true;
        if (this.needsTack()) return this.hasFlag(FLAG_SADDLED);
        return this.getAffection(player) >= RIDE_AFFECTION;
    }

    /** The kelpie and the pegasus are ridden only with tack on (a saddle; for the pegasus, Bellerophon's golden bridle). */
    private boolean needsTack() {
        return this.species == GirlSpecies.KELPIE_CENTAUR || this.species == GirlSpecies.PEGASUS_CENTAUR;
    }

    private int saddleAffection() {
        return this.isCamel() ? 0 : this.needsTack() ? com.linkyoshi90.monstergirls.entity.Orders.AFFECTION : RIDE_AFFECTION;
    }

    /** What she wears to be ridden: a saddle, or gold horse armour as the pegasus' golden bridle. */
    private net.minecraft.world.item.Item tack() {
        return this.species == GirlSpecies.PEGASUS_CENTAUR ? Items.GOLDEN_HORSE_ARMOR : Items.SADDLE;
    }

    // --- the pegasus: a leap and a long glide ----------------------------------------------------------------------

    private float jumpPending;

    @Override
    public void onPlayerJump(int power) {
        if (this.canJump()) this.jumpPending = power >= 90 ? 1.0F : 0.4F + 0.6F * power / 90.0F;
    }

    @Override
    public boolean canJump() {
        return this.species == GirlSpecies.PEGASUS_CENTAUR && this.hasFlag(FLAG_SADDLED);
    }

    @Override
    public void handleStartJump(int power) {
        this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 0.6F, 1.6F);
    }

    @Override
    public void handleStopJump() {
    }

    // --- the kelpie by night -------------------------------------------------------------------------------------

    /** By day she is calm; at night she attacks any player she is not fond of. */
    @Override
    public boolean isHostileTo(Player player) {
        if (this.species != GirlSpecies.KELPIE_CENTAUR) return super.isHostileTo(player);
        long time = this.level().getDayTime() % 24000L;                 // the clock, not the sky: night is 13000 to 23000
        return time >= 13000L && time < 23000L && this.getAffection(player) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION && this.couldAttack(player);
    }


    // --- herd ----------------------------------------------------------------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (!(group instanceof HerdData)) {
            this.leadMare = true;                         // the first of her group leads it
            group = new HerdData();
        }
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    private static class HerdData implements SpawnGroupData {
    }

    public boolean isLeadMare() {
        return this.leadMare;
    }

    public void setLeadMare(boolean lead) {
        this.leadMare = lead;
    }

    @Nullable
    private LivingEntity leader() {
        if (this.leadMare || this.isVehicle()) return null;
        if (this.isBaby()) return this.mother();
        return this.nearest(Centaur.class, 32, c -> c.getType() == this.getType() && c.isLeadMare() && !c.isBaby());
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.CARROT) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.SUGAR) || stack.is(Items.WHEAT) || stack.is(Items.HAY_BLOCK)
                || (this.species == GirlSpecies.TAPIR_CENTAUR && stack.is(Items.MELON_SLICE))
                || (this.isCamel() && stack.is(Items.CACTUS))
                || (this.species == GirlSpecies.KELPIE_CENTAUR && (stack.is(Items.KELP) || stack.is(Items.DRIED_KELP)))
                || (this.species == GirlSpecies.PEGASUS_CENTAUR && stack.is(Items.GOLDEN_APPLE))
                || (this.species == GirlSpecies.AURORANTLER_CENTAUR && (stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)))
                || (this.species == GirlSpecies.MOMOSHIKA_CENTAUR && (stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES)))
                || (this.species == GirlSpecies.HALBERELK_CENTAUR && (stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES)));
    }

    // --- the bellmare and the vicunero -----------------------------------------------------------------------------------

    /** The bellwether: the farm animals within 12 blocks of her walk over to her while her friend is near. How many came. */
    public int leadHerd(ServerLevel level) {
        if (this.isBaby() || this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24) == null) return 0;
        int n = 0;
        for (var a : level.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class, this.getBoundingBox().inflate(12),
                a -> (a instanceof net.minecraft.world.entity.animal.Cow || a instanceof net.minecraft.world.entity.animal.Sheep
                        || a instanceof net.minecraft.world.entity.animal.goat.Goat || a instanceof net.minecraft.world.entity.animal.Pig)
                        && !a.isLeashed() && a.distanceToSqr(this) > 16)) {
            a.getNavigation().moveTo(this, 1.1);
            n++;
        }
        if (n > 0 && this.random.nextInt(3) == 0) this.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(), 0.6F, 1.6F);
        return n;
    }

    /** The vicuna spits at a monster near her friend, like a llama. The spit, or null if there was nothing to spit at. */
    @Nullable
    public net.minecraft.world.entity.projectile.LlamaSpit spitAtMonster(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null || this.isBaby()) return null;
        net.minecraft.world.entity.monster.Monster m = this.nearest(net.minecraft.world.entity.monster.Monster.class, 12, x -> x.distanceToSqr(friend) < 10 * 10 && this.hasLineOfSight(x));
        if (m == null) return null;
        var spit = net.minecraft.world.entity.EntityType.LLAMA_SPIT.create(level);
        if (spit == null) return null;
        spit.setOwner(this);
        spit.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        double dx = m.getX() - this.getX(), dz = m.getZ() - this.getZ(), dy = m.getY(0.33) - spit.getY();
        spit.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.5F, 10.0F);
        level.addFreshEntity(spit);
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.LLAMA_SPIT, 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        return spit;
    }

    /** The baku: no phantom (vanilla or girl) comes for a player within 32 blocks of a tapir. */
    public static boolean dreamWard(Player player) {
        return !player.level().getEntitiesOfClass(Centaur.class, player.getBoundingBox().inflate(32),
                c -> c.isAlive() && c.species == GirlSpecies.TAPIR_CENTAUR).isEmpty();
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.GOLDEN_APPLE)) return 10;                       // the pegasus' great weakness
        return stack.is(Items.GOLDEN_CARROT) || stack.is(Items.HAY_BLOCK) ? 3 : stack.is(Items.APPLE) ? 2 : 1;
    }

    private static boolean isChestplate(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem a && a.getType() == ArmorItem.Type.CHESTPLATE;
    }

    /** Her chestplate adds twice its armour points to her health (leather +6 ... diamond and netherite +16). */
    public void refreshArmourHealth() {
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        health.removeModifier(ARMOUR_HEALTH);
        if (this.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem a) {
            health.addTransientModifier(new AttributeModifier(ARMOUR_HEALTH, a.getDefense() * 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
        if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
    }

    public void wearArmour(ItemStack chestplate) {
        this.setItemSlot(EquipmentSlot.CHEST, chestplate);
        this.setDropChance(EquipmentSlot.CHEST, 2.0F);
        this.refreshArmourHealth();
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.SPHINX_CENTAUR && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) {
                int which = this.riddleFor(player);
                if (which < 0) this.askRiddle(player, this.random.nextInt(RIDDLE_ANSWERS.size()));
                else if (stack.isEmpty()) this.askRiddle(player, which);           // asked again
                else this.answer(server, player, stack);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        boolean client = this.level().isClientSide;
        if (stack.is(Items.BRUSH)) {                                   // grooming: the herd's way of saying trust
            if (this.level() instanceof ServerLevel server && server.getGameTime() >= this.nextGroom) {
                this.nextGroom = server.getGameTime() + GROOM_COOLDOWN;
                this.addAffection(player, 2);
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                this.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);
                this.hearts(server, 4);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.isBaby()) return null;
        if (this.isCamel() && stack.is(Items.CACTUS) && this.camelLove <= 0) {   // cactus: in the mood, and a vanilla camel in love will do
            if (this.level() instanceof ServerLevel server) {
                this.camelLove = 600;
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0);
            }
            return null;                                               // and it still counts as food
        }
        if (this.isCamel() && stack.is(Items.BUCKET)) {               // camel milk
            if (this.level() instanceof ServerLevel server) {
                if (server.getGameTime() < this.nextMilk) {
                    player.displayClientMessage(Component.translatable("message.monstergirls.no_milk", this.getDisplayName()), true);
                } else {
                    this.nextMilk = server.getGameTime() + Minotaur.MILK_COOLDOWN;
                    stack.consume(1, player);
                    ItemStack milk = new ItemStack(Items.MILK_BUCKET);
                    if (!player.getInventory().add(milk)) player.drop(milk, false);
                    this.addAffection(player, 1);
                    this.playSound(SoundEvents.COW_MILK, 1.0F, 0.9F);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (isChestplate(stack)) {                                     // torso armour
            if (this.getAffection(player) < ARMOUR_AFFECTION) return InteractionResult.PASS;
            if (!client) {
                ItemStack old = this.getItemBySlot(EquipmentSlot.CHEST);
                this.wearArmour(stack.split(1));
                if (!old.isEmpty() && !player.getInventory().add(old)) this.spawnAtLocation(old);
                this.heal(4.0F);
                this.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.0F);
                if (this.level() instanceof ServerLevel server) this.hearts(server, 2);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(this.tack()) && !this.hasFlag(FLAG_SADDLED)) {
            if (this.getAffection(player) < this.saddleAffection() || !this.mayCarry(player)) {
                if (!client) player.displayClientMessage(Component.translatable("message.monstergirls.ride_refused", this.getDisplayName()), true);
                return InteractionResult.sidedSuccess(client);
            }
            if (!client) {
                this.setFlag(FLAG_SADDLED, true);
                stack.consume(1, player);
                this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.ALPACA_CENTAUR) {
            if (stack.is(Items.CHEST) && !this.hasFlag(FLAG_CHEST) && this.getAffection(player) >= RIDE_AFFECTION) {
                if (!client) {
                    this.setFlag(FLAG_CHEST, true);
                    stack.consume(1, player);
                    this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.is(Items.SHEARS) && this.fleeceTicks >= 24000) {   // her fleece, without hurting her bond
                if (!client) {
                    this.fleeceTicks = 0;
                    this.spawnAtLocation(new ItemStack(this.woolColour(), 1 + this.random.nextInt(2)));
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.isEmpty() && player.isShiftKeyDown() && this.hasFlag(FLAG_CHEST) && this.getAffection(player) >= RIDE_AFFECTION) {
                if (!client) player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, this.pack), this.getDisplayName()));
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            ItemStack worn = this.getItemBySlot(EquipmentSlot.CHEST);
            if (!worn.isEmpty() && this.getAffection(player) >= ARMOUR_AFFECTION) {   // help her out of her armour
                if (!client) {
                    this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    this.refreshArmourHealth();
                    if (!player.getInventory().add(worn)) this.spawnAtLocation(worn);
                    this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            return this.letsRide(player) ? this.pet(player) : null;      // riders pat her while crouching
        }
        if (stack.isEmpty() && !this.isVehicle() && this.letsRide(player)) {   // up on her back
            if (!this.mayCarry(player)) {
                if (!client) player.displayClientMessage(Component.translatable("message.monstergirls.ride_refused", this.getDisplayName()), true);
                return InteractionResult.sidedSuccess(client);
            }
            if (!client) {
                this.getNavigation().stop();
                this.setTarget(null);
                this.setOrderedToSit(false);
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    private net.minecraft.world.item.Item woolColour() {
        return switch (this.getVariantName()) {
            case "fawn", "brown" -> Items.BROWN_WOOL;
            case "black" -> Items.BLACK_WOOL;
            case "grey" -> Items.LIGHT_GRAY_WOOL;
            default -> Items.WHITE_WOOL;
        };
    }

    public SimpleContainer getPack() {
        return this.pack;
    }

    public void setFleeceTicks(int t) {
        this.fleeceTicks = t;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case REINDEER_CENTAUR -> new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 75 ? Items.MOSS_BLOCK : Items.GLOW_LICHEN, r < 40 ? 4 : 1);
            case TUSKTAUR_CENTAUR -> new ItemStack(r < 45 ? Items.BROWN_WOOL : r < 80 ? Items.BONE : Items.PACKED_ICE, r < 80 ? 2 : 4);
            case SPHINX_CENTAUR -> new ItemStack(r < 40 ? Items.GOLD_NUGGET : r < 75 ? Items.LAPIS_LAZULI : r < 95 ? Items.SANDSTONE : Items.GOLD_INGOT,
                    r < 40 ? 4 : r < 75 ? 3 : r < 95 ? 4 : 1);
            case PRAIRHORN_CENTAUR -> new ItemStack(r < 50 ? Items.WHEAT : r < 85 ? Items.LEATHER : Items.RABBIT_FOOT, r < 85 ? 3 : 1);
            case BELLMARE_CENTAUR -> new ItemStack(r < 40 ? Items.WHEAT : r < 75 ? Items.CARROT : r < 95 ? Items.LEAD : Items.BELL, r < 75 ? 3 : 1);
            case VICUNERO_CENTAUR -> new ItemStack(r < 45 ? Items.WHITE_WOOL : r < 80 ? Items.WHEAT : r < 97 ? Items.HAY_BLOCK : Items.LIGHT_BLUE_CARPET, r < 80 ? 2 : 1);
            case HALBERELK_CENTAUR -> new ItemStack(r < 40 ? Items.SPRUCE_LOG : r < 75 ? Items.STICK : r < 95 ? Items.SPRUCE_SAPLING : Items.IRON_AXE, r < 75 ? 4 : r < 95 ? 2 : 1);
            case MOMOSHIKA_CENTAUR -> new ItemStack(r < 45 ? Items.PINK_PETALS : r < 80 ? Items.APPLE : r < 95 ? Items.CHERRY_SAPLING : Items.GOLDEN_APPLE,
                    r < 80 ? 3 : 1);
            case AURORANTLER_CENTAUR -> new ItemStack(r < 45 ? Items.GLOW_BERRIES : r < 80 ? Items.GLOW_LICHEN : Items.AMETHYST_SHARD, r < 45 ? 3 : 2);
            case GOAT_CENTAUR -> new ItemStack(r < 60 ? Items.MILK_BUCKET : Items.EMERALD);
            case ALPACA_CENTAUR -> new ItemStack(this.woolColour(), 2);
            case TAPIR_CENTAUR -> new ItemStack(r < 50 ? Items.MELON_SLICE : r < 85 ? Items.SWEET_BERRIES : Items.GLOW_BERRIES, 3);
            case CAMEL_CENTAUR -> r < 40 ? new ItemStack(Items.BROWN_WOOL, 2) : new ItemStack(r < 75 ? Items.MILK_BUCKET : Items.CACTUS, r < 75 ? 1 : 2);
            case DROMEDARY_CENTAUR -> r < 45 ? new ItemStack(Items.MILK_BUCKET) : new ItemStack(r < 80 ? Items.CACTUS : Items.GOLD_NUGGET, r < 80 ? 2 : 3);
            case KELPIE_CENTAUR -> r < 40 ? new ItemStack(Items.KELP, 4) : r < 70 ? new ItemStack(Items.SALMON, 2) : new ItemStack(r < 95 ? Items.LILY_PAD : Items.NAUTILUS_SHELL);
            case UNICORN_CENTAUR -> new ItemStack(r < 40 ? Items.GLOW_BERRIES : r < 75 ? Items.AMETHYST_SHARD : r < 95 ? Items.GOLDEN_CARROT : Items.TOTEM_OF_UNDYING,
                    r < 75 ? 2 : 1);
            default -> new ItemStack(r < 50 ? Items.APPLE : r < 85 ? Items.WHEAT : Items.GOLDEN_CARROT, r < 85 ? 2 : 1);
        };
    }

    // --- riding --------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        // no friendship check: the client decides who steers and does not know her friendship (it is checked when you get on)
        return this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    /** The zebra is famously never tamed: only her father may ride her. */
    public boolean mayCarry(Player player) {
        return this.species != GirlSpecies.SPHINX_CENTAUR && (this.species != GirlSpecies.ZEBRA_CENTAUR || player.getUUID().equals(this.getFather()));
    }

    public float getStamina() {
        return this.stamina;
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        boolean galloping = player.isSprinting() && travel.lengthSqr() > 0 && !this.winded;
        this.stamina = Math.max(0.0F, Math.min(MAX_STAMINA, this.stamina + (galloping ? -1.0F : 0.5F)));
        if (this.stamina <= 0.0F && !this.winded) {
            this.winded = true;
            if (!this.level().isClientSide) player.displayClientMessage(Component.translatable("message.monstergirls.winded", this.getDisplayName()), true);
        } else if (this.winded && this.stamina >= MAX_STAMINA / 3) {
            this.winded = false;
        }
        Vec2 rot = new Vec2(player.getXRot() * 0.5F, player.getYRot());
        this.setRot(rot.y, rot.x);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        if (this.species == GirlSpecies.PEGASUS_CENTAUR) {
            if (this.jumpPending > 0 && this.onGround()) {              // the leap: up and forward on her wings
                Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot()).scale(1.2 * this.jumpPending);
                this.setDeltaMovement(fwd.x, 0.5 + 0.9 * this.jumpPending, fwd.z);
                this.hasImpulse = true;
            } else if (!this.onGround() && this.getDeltaMovement().y < -0.12) {   // then a slow glide down
                this.setDeltaMovement(this.getDeltaMovement().x, -0.12, this.getDeltaMovement().z);
            }
            this.jumpPending = 0;
        }
        if (this.species == GirlSpecies.KELPIE_CENTAUR && this.isInWater()) {   // the water horse: faster than a boat, her rider kept dry
            float speed = player.zza > 0 ? (player.isSprinting() ? 0.5F : 0.42F) : 0.0F;
            Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot()).scale(speed);
            double up = this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 1.0 ? 0.08 : this.horizontalCollision ? 0.4 : 0.0;
            this.setDeltaMovement(fwd.x, Math.max(this.getDeltaMovement().y, up), fwd.z);
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travel) {
        float forward = player.zza <= 0.0F ? player.zza * 0.25F : player.zza;
        return new Vec3(player.xxa * 0.5F, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float base = (this.species == GirlSpecies.HORSE_CENTAUR || this.species == GirlSpecies.KELPIE_CENTAUR || this.species == GirlSpecies.PEGASUS_CENTAUR ? 0.32F : 0.26F) * (this.hasFlag(FLAG_SADDLED) ? 1.15F : 1.0F);
        return player.isSprinting() && !this.winded ? base * 1.45F : this.winded ? base * 0.7F : base;
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        return new Vec3(0.0, dims.height() * 0.55, 0.45);                // on her back, behind her waist
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.GOAT_CENTAUR && this.species != GirlSpecies.PEGASUS_CENTAUR && super.causeFallDamage(distance, multiplier, source);
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** A double-hoof kick at whatever hits her from behind; the zebra's herd scatters its attackers (dazzle). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt || !(this.level() instanceof ServerLevel server) || !(source.getEntity() instanceof LivingEntity attacker)) return hurt;
        Vec3 toAttacker = attacker.position().subtract(this.position()).normalize();
        Vec3 facing = Vec3.directionFromRotation(0, this.getYRot());
        if (toAttacker.dot(facing) < -0.3 && server.getGameTime() >= this.nextKick && attacker.distanceToSqr(this) < 9) {
            this.nextKick = server.getGameTime() + 40;
            this.startTimedAction(Action.KICK, 16);
            attacker.hurt(this.damageSources().mobAttack(this), this.species == GirlSpecies.ZEBRA_CENTAUR ? 7.0F : 5.0F);
            attacker.knockback(1.5, this.getX() - attacker.getX(), this.getZ() - attacker.getZ());
        }
        if (this.species == GirlSpecies.ALPACA_CENTAUR && attacker instanceof Player p && this.getAffection(p) < BED_AFFECTION) {
            this.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 1.0F);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SPIT, p.getX(), p.getEyeY(), p.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            p.hurt(this.damageSources().mobAttack(this), 1.0F);
        }
        if (this.species == GirlSpecies.ZEBRA_CENTAUR && attacker instanceof Mob mob && this.random.nextBoolean()) mob.setTarget(null);
        return hurt;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** The caribou with a player on her back, on snow or ice, runs faster for two seconds. 1 when she does, 0 off the snow, -1 with nobody on her back. */
    public int snowStride(net.minecraft.server.level.ServerLevel level) {
        if (!(this.getFirstPassenger() instanceof net.minecraft.world.entity.player.Player)) return -1;
        var on = level.getBlockState(this.blockPosition().below());
        var in = level.getBlockState(this.blockPosition());
        if (!on.is(net.minecraft.tags.BlockTags.SNOW) && !on.is(net.minecraft.tags.BlockTags.ICE) && !in.is(net.minecraft.tags.BlockTags.SNOW)) return 0;
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
        return 1;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.CARIBOU_CENTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel tundra && !this.isBaby() && this.tickCount % 20 == 0) this.snowStride(tundra);
        if (this.species == GirlSpecies.QILIN_CENTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel meadow && !this.isBaby() && this.tickCount % 60 == 0) this.gentleTread(meadow);
        if (this.species == GirlSpecies.URCUCHILLAY_CENTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel herd && !this.isBaby() && this.tickCount % 200 == 0) this.herdKeeper(herd);
        if (this.species == GirlSpecies.CEIBLER_CENTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel trail && !this.isBaby() && this.tickCount % 20 == 0) this.trailGuide(trail);
        if (this.species == GirlSpecies.PALMYR_CENTAUR && this.level() instanceof ServerLevel meadow && !this.isBaby() && this.tickCount % 20 == 0) this.fleetFoot(meadow);
        if ((this.species == GirlSpecies.STAGEL_CENTAUR || this.species == GirlSpecies.LAMASSU_CENTAUR) && this.level() instanceof ServerLevel ward && !this.isBaby() && this.tickCount % 20 == 0) this.blessFriend(ward);
        if (this.species == GirlSpecies.AURORANTLER_CENTAUR && this.level() instanceof ServerLevel aurora && !this.isBaby()) this.aurora(aurora);
        if (this.species == GirlSpecies.MOMOSHIKA_CENTAUR && this.level() instanceof ServerLevel grove && !this.isBaby() && this.tickCount % 40 == 0) this.blossom(grove);
        if (this.species == GirlSpecies.BELLMARE_CENTAUR && this.level() instanceof ServerLevel herd && this.tickCount % 40 == 0) this.leadHerd(herd);
        if (this.species == GirlSpecies.VICUNERO_CENTAUR && this.level() instanceof ServerLevel hills && this.tickCount % 30 == 0) this.spitAtMonster(hills);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.ALPACA_CENTAUR && this.fleeceTicks < 24000) this.fleeceTicks++;
        if (this.camelLove > 0 && --this.camelLove % 10 == 0) this.courtCamel(server);
        if (this.species == GirlSpecies.KELPIE_CENTAUR && this.getTarget() instanceof Player p && !this.isHostileTo(p) && this.getLastHurtByMob() != p) {
            this.setTarget(null);                                     // morning: the hunt is over
        }
        if (this.species == GirlSpecies.ZEBRA_CENTAUR && server.getGameTime() >= this.nextAlarm && this.tickCount % 20 == 0
                && this.bondedPlayer(BED_AFFECTION, 32) != null) {
            Monster m = this.nearest(Monster.class, 16, x -> true);
            if (m != null) {                                         // the alarm bark
                this.nextAlarm = server.getGameTime() + 200;
                this.playSound(SoundEvents.DONKEY_ANGRY, 2.0F, 1.4F);
                m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                this.startTimedAction(Action.REAR, 30);
            }
        }
    }

    /** A camel girl in love walks up to a vanilla camel in love (fed cactus) within 8 blocks, and they raise a calf. */
    private void courtCamel(ServerLevel level) {
        if (!this.getGestation().canConceive()) return;
        net.minecraft.world.entity.animal.camel.Camel camel = this.nearest(net.minecraft.world.entity.animal.camel.Camel.class, 8,
                c -> c.isInLove() && !c.isBaby());
        if (camel == null) return;
        if (this.distanceToSqr(camel) > 3.0 * 3.0) {
            this.getNavigation().moveTo(camel, 1.0);
            return;
        }
        this.getGestation().conceive(null);
        this.camelSired = true;
        this.camelLove = 0;
        camel.resetLove();
        this.hearts(level, 6);
    }

    public int getCamelLove() {
        return this.camelLove;
    }

    /** Sired by a vanilla camel: a camel calf or a foal of her own, even odds. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable java.util.UUID father) {
        if (this.camelSired && this.random.nextBoolean()) {
            net.minecraft.world.entity.animal.camel.Camel calf = EntityType.CAMEL.create(level);
            if (calf != null) {
                calf.setAge(-24000);
                calf.moveTo(this.getX(), this.getY(), this.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(calf);
                this.camelSired = false;
                return true;
            }
        }
        this.camelSired = false;
        return super.giveBirth(level, father);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.hasFlag(FLAG_SADDLED)) this.spawnAtLocation(this.tack());
        if (this.hasFlag(FLAG_CHEST)) {
            this.spawnAtLocation(Items.CHEST);
            for (int i = 0; i < this.pack.getContainerSize(); i++) if (!this.pack.getItem(i).isEmpty()) this.spawnAtLocation(this.pack.getItem(i));
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case ZEBRA_CENTAUR -> SoundEvents.DONKEY_AMBIENT;
            case GOAT_CENTAUR -> SoundEvents.GOAT_AMBIENT;
            case ALPACA_CENTAUR -> SoundEvents.LLAMA_AMBIENT;
            case CAMEL_CENTAUR, DROMEDARY_CENTAUR -> SoundEvents.CAMEL_AMBIENT;
            default -> SoundEvents.HORSE_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isCamel()) return SoundEvents.CAMEL_HURT;
        return this.species == GirlSpecies.GOAT_CENTAUR ? SoundEvents.GOAT_HURT : this.species == GirlSpecies.ALPACA_CENTAUR ? SoundEvents.LLAMA_HURT : SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.HORSE_STEP, 0.25F, 1.1F);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.hasFlag(FLAG_SADDLED) || this.hasFlag(FLAG_CHEST) || !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("LeadMare", this.leadMare);
        tag.putInt("Fleece", this.fleeceTicks);
        tag.put("Pack", this.pack.createTag(this.registryAccess()));
        tag.putLong("NextMilk", this.nextMilk);
        tag.putBoolean("CamelSired", this.camelSired);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.leadMare = tag.getBoolean("LeadMare");
        this.fleeceTicks = tag.getInt("Fleece");
        this.pack.fromTag(tag.getList("Pack", 10), this.registryAccess());
        this.nextMilk = tag.getLong("NextMilk");
        this.camelSired = tag.getBoolean("CamelSired");
        this.refreshArmourHealth();
        if (tag.contains("Health")) this.setHealth(tag.getFloat("Health"));
    }

    // --- running together ----------------------------------------------------------------------------------------

    /** The player she is galloping beside, or null. */
    @Nullable
    private Player runningWith;

    public boolean isRunningAlong() {
        return this.runningWith != null;
    }

    /**
     * Wild or tame, a centaur who sees a player sprint past cannot help herself: she gallops up beside them and keeps pace,
     * grinning, until they slow down.
     */
    class RunAlongGoal extends Goal {
        private int slowTicks;

        RunAlongGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean sprinting(Player p) {
            return p.isSprinting() && !p.isPassenger() && !p.isSpectator() && !p.isFallFlying();
        }

        @Override
        public boolean canUse() {
            Centaur c = Centaur.this;
            if (c.isBaby() || c.isOrderedToSit() || c.getTarget() != null || c.isVehicle() || c.tickCount % 7 != 0) return false;
            Player p = c.level().getNearestPlayer(c, 16.0);
            if (p == null || !this.sprinting(p) || c.isHostileTo(p)) return false;
            c.runningWith = p;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            Player p = Centaur.this.runningWith;
            return p != null && p.isAlive() && Centaur.this.getTarget() == null && !Centaur.this.isOrderedToSit()
                    && this.slowTicks < 40 && p.distanceToSqr(Centaur.this) < 40 * 40;
        }

        @Override
        public void start() {
            this.slowTicks = 0;
            Centaur.this.setFlag(FLAG_SMILE, true);
        }

        @Override
        public void stop() {
            Centaur.this.runningWith = null;
            Centaur.this.setSprinting(false);
            Centaur.this.setFlag(FLAG_SMILE, false);
            Centaur.this.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Centaur c = Centaur.this;
            Player p = c.runningWith;
            if (p == null) return;
            this.slowTicks = this.sprinting(p) ? 0 : this.slowTicks + 1;
            Vec3 fwd = Vec3.directionFromRotation(0, p.getYRot());
            Vec3 side = new Vec3(-fwd.z, 0, fwd.x);
            if (side.dot(c.position().subtract(p.position())) < 0) side = side.scale(-1);        // stay on the side she came from
            Vec3 spot = p.position().add(side.scale(2.2)).add(fwd.scale(0.5));
            c.setSprinting(true);
            if (c.tickCount % 3 == 0) c.getNavigation().moveTo(spot.x, spot.y, spot.z, c.distanceToSqr(spot) > 16 ? 1.9 : 1.5);
            c.getLookControl().setLookAt(p, 30.0F, 30.0F);
        }
    }

    // --- the stagel and the lamassu ------------------------------------------------------------------------------

    /** Her friend within 8 blocks: the stagel gives Jump Boost II, the lamassu Resistance I. Whether she did. */
    public boolean blessFriend(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null) return false;
        if (this.species == GirlSpecies.STAGEL_CENTAUR) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP, 60, 1, true, true, true));
        else if (this.species == GirlSpecies.LAMASSU_CENTAUR) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 60, 0, true, true, true));
        else return false;
        return true;
    }

    // --- the palmyr (eighteenth wave) -------------------------------------------------------------------------------

    /** Her friend within 12 blocks runs faster while sprinting (Speed I). True if she sped them up. */
    public boolean fleetFoot(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 12);
        if (friend == null || !friend.isSprinting()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, true, true));
        return true;
    }

    // --- the ceibler (twentieth wave) -------------------------------------------------------------------------------

    /** Her friend within 10 blocks, standing on grass, dirt or a path, runs faster (Speed I for three seconds). True if so. */
    public boolean trailGuide(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 10);
        if (friend == null) return false;
        var ground = level.getBlockState(friend.blockPosition().below());
        if (!ground.is(net.minecraft.tags.BlockTags.DIRT) && !ground.is(net.minecraft.world.level.block.Blocks.DIRT_PATH)) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, 0, true, true));
        return true;
    }

    // --- Urcuchillay (twenty-first wave) ----------------------------------------------------------------------------

    /** The young animals within 12 blocks of her friend grow up a minute sooner (every ten seconds: six times as fast). How many,
     *  or -1 without a friend. */
    public int herdKeeper(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (var young : level.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class, friend.getBoundingBox().inflate(12),
                net.minecraft.world.entity.animal.Animal::isBaby)) {
            young.ageUp(60, true);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, young.getX(), young.getY(0.8), young.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
            n++;
        }
        return n;
    }

    /** The qilin's blessing (twenty-fourth wave): the hurt animals within 8 of her friend heal 2 health. The number healed, -1 without a friend. */
    public int gentleTread(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int healed = 0;
        for (net.minecraft.world.entity.animal.Animal a : level.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class, friend.getBoundingBox().inflate(8),
                a -> a.isAlive() && a.getHealth() < a.getMaxHealth())) {
            a.heal(2.0F);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, a.getX(), a.getEyeY(), a.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
            healed++;
        }
        return healed;
    }
}
