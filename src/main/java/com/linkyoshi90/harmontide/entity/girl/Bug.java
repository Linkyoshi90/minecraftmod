package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.block.GirlEggBlock;
import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The insect family (bug_field_guide.html): mosquito, moth and her caterpillar, fairy and the Fairy Queen, scorpion, bee
 * and the Queen Bee, papillon and her caterpillar, hornet. Instinct first: the mosquito drinks blood (from you until calmed with
 * honey, from your enemies after), the moth flies to the brightest light at night, caterpillars eat and in five days turn
 * into moths and papillons, fairies heal and build a fountain of flowers and water from which a Fairy Queen may rise, the
 * scorpion stings and carries her young, bees pollinate, grow crops and fill hives (twice as fast near their queen), the
 * papillon plants flowers, and the hornet stings anyone who comes close and hunts bees until she is befriended. The
 * mantis holds her prayer pose until a silverfish, endermite or spider comes near, then strikes in a flash; the orchid
 * mantis cannot be seen among flowers.
 */
public class Bug extends AbstractMonsterGirl {
    /** Five days as a caterpillar. */
    public static final int METAMORPHOSIS = GROW_TICKS;
    public static final int QUEEN_HIVE_BONUS = 5;

    private int age;
    private boolean nectar;
    private int workTicks;
    @Nullable
    private BlockPos fountain;
    private int fountainBuilt;
    @Nullable
    private BlockPos light;
    private long nextHeal;
    private int carryTicks;
    private long nextLunge;

    public Bug(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.HONEY_BOTTLE, Items.SUGAR, Items.HONEYCOMB, Items.POPPY, Items.DANDELION), false));
        if (!this.canFly()) this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (species == GirlSpecies.MOSQUITO_BUG) {                          // once calm, she drinks from your enemies
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> { Player p = this.bondedPlayer(10, 16); return p != null && m.distanceToSqr(p) < 10 * 10; }));
        }
        if (species == GirlSpecies.MANTIS_BUG) {                            // the ambush: the arthropods that wander into reach
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                    e -> (e instanceof net.minecraft.world.entity.monster.Silverfish || e instanceof net.minecraft.world.entity.monster.Endermite
                            || e instanceof net.minecraft.world.entity.monster.Spider) && e.distanceToSqr(this) < 10 * 10));
        }
        if (species == GirlSpecies.HORNET_BUG) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, false,      // on patrol she tears into monsters
                    m -> this.getOrders().command(this) == com.linkyoshi90.harmontide.entity.Orders.Command.PATROL));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, this::isHornetPrey));
        }
    }

    // --- the hornet ----------------------------------------------------------------------------------------------

    /** Bees and bee girls, until someone has befriended her. */
    private boolean isHornetPrey(LivingEntity e) {
        if (this.bestAffection() >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) return false;
        return e instanceof net.minecraft.world.entity.animal.Bee
                || e instanceof Bug b && (b.species == GirlSpecies.BEE_BUG || b.species == GirlSpecies.QUEEN_BEE_BUG);
    }

    /** She stings any player who comes within 5 blocks, unless she is fond of them. */
    @Override
    public boolean isHostileTo(Player player) {
        if (this.species != GirlSpecies.HORNET_BUG) return super.isHostileTo(player);
        return this.distanceToSqr(player) < 5 * 5 && this.getAffection(player) < com.linkyoshi90.harmontide.entity.Orders.AFFECTION && this.couldAttack(player);
    }

    /** Hornets are often found with a broken wing, and the one who mends it has her for life. */
    @Override
    protected int injuredRarity() {
        return this.species == GirlSpecies.HORNET_BUG ? 5 : super.injuredRarity();
    }

    @Override
    protected int mendAffection() {
        return this.species == GirlSpecies.HORNET_BUG ? FULL_AFFECTION : super.mendAffection();
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case DATE_BEETLE_BUG -> girlAttributes(12, 0.28, 1);
            case ANT_BUG -> girlAttributes(16, 0.3, 3);
            case QUEEN_ANT_BUG -> girlAttributes(30, 0.26, 4);
            case SPIDER_WASP_BUG -> girlAttributes(22, 0.32, 5);
            case MOSQUITO_BUG -> girlAttributes(14, 0.3, 3);
            case FAIRY_BUG -> girlAttributes(8, 0.3, 1);
            case FIREFLY_BUG -> girlAttributes(8, 0.3, 1);
            case FAIRY_QUEEN_BUG -> girlAttributes(60, 0.3, 4);
            case SCORPION_BUG -> girlAttributes(30, 0.28, 5);
            case QUEEN_BEE_BUG -> girlAttributes(44, 0.28, 5);
            case HORNET_BUG -> girlAttributes(22, 0.32, 5);
            case MANTIS_BUG -> girlAttributes(20, 0.32, 6);
            case SCARABRONZE_BUG -> girlAttributes(30, 0.26, 4).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 10);
            case MOTH_LARVA_BUG, PAPILLON_LARVA_BUG -> girlAttributes(10, 0.14, 0);
            case TEMPLE_MANTIS_BUG -> girlAttributes(22, 0.3, 5);
            default -> girlAttributes(16, 0.3, 2);
        };
    }

    public boolean isLarva() {
        return this.species == GirlSpecies.MOTH_LARVA_BUG || this.species == GirlSpecies.PAPILLON_LARVA_BUG;
    }

    @Override
    public boolean canFly() {
        return this.species != GirlSpecies.SCORPION_BUG && this.species != GirlSpecies.MOTH_LARVA_BUG && this.species != GirlSpecies.PAPILLON_LARVA_BUG
                && this.species != GirlSpecies.MANTIS_BUG && this.species != GirlSpecies.ANT_BUG;
    }

    /** A caterpillar is already small: she hatches full size and grows by turning into her adult form instead. */
    @Override
    public void setBaby(boolean baby) {
        super.setBaby(baby && !this.isLarva());
    }

    @Override
    protected boolean hasSwimAnimation() {
        return false;
    }

    // --- hostility -----------------------------------------------------------------------------------------------

    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.MOSQUITO_BUG;
    }

    @Override
    protected boolean calmsHer(ItemStack stack) {
        return stack.is(Items.HONEY_BOTTLE);
    }

    /** The mantis: still in prayer while nothing is near; a lunge at her prey; the orchid hides among flowers. */
    private void ambush(ServerLevel level) {
        LivingEntity t = this.getTarget();
        if (t == null && this.getAction() == Action.NONE && this.tickCount % 40 == 0 && this.getNavigation().isDone() && this.random.nextInt(3) == 0) {
            this.startTimedAction(Action.PRAY, 200);
        }
        if (this.getAction() == Action.PRAY) {
            if (t != null) this.setAction(Action.NONE);
            else this.getNavigation().stop();
        }
        if (t != null && this.onGround() && level.getGameTime() >= this.nextLunge && this.distanceToSqr(t) < 4 * 4) {   // faster than the eye
            this.nextLunge = level.getGameTime() + 30;
            Vec3 d = t.position().subtract(this.position()).normalize();
            this.setDeltaMovement(d.x * 0.8, 0.25, d.z * 0.8);
            this.startTimedAction(Action.STRIKE, 8);
        }
        if ("orchid".equals(this.getVariantName()) && this.tickCount % 20 == 0) {
            BlockPos o = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-1, -1, -1), o.offset(1, 0, 1))) {
                if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.FLOWERS)) {
                    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
                    break;
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.MANTIS_BUG) this.startTimedAction(Action.STRIKE, 8);
        if (!hit || !(target instanceof LivingEntity l)) return hit;
        if (this.species == GirlSpecies.MOSQUITO_BUG) {                      // she drinks, and something nasty follows
            List<Holder<MobEffect>> nasty = List.of(MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.POISON);
            l.addEffect(new MobEffectInstance(nasty.get(this.random.nextInt(nasty.size())), 120, 0));
            this.heal(2.0F);
            this.startTimedAction(Action.DRINK, 20);
        }
        if (this.species == GirlSpecies.SCORPION_BUG || this.species == GirlSpecies.BEE_BUG || this.species == GirlSpecies.QUEEN_BEE_BUG
                || this.species == GirlSpecies.HORNET_BUG) {
            l.addEffect(new MobEffectInstance(MobEffects.POISON, 100, this.species == GirlSpecies.SCORPION_BUG ? 1 : 0));
            this.startTimedAction(this.species == GirlSpecies.SCORPION_BUG ? Action.STING : Action.POUNCE, 16);
        }
        return hit;
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    private long lastSunrise = -1;

    /** At sunrise (the first half minute of the day) her friend gets Haste for the morning, once a day. True when she gave it. */
    public boolean sunrise(ServerLevel level) {
        long day = level.getDayTime() / 24000, time = level.getDayTime() % 24000;
        if (time >= 600 || day == this.lastSunrise) return false;
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 32);
        if (friend == null) return false;
        this.lastSunrise = day;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 6000, 0, true, true));
        return true;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.HONEY_BOTTLE) || stack.is(Items.SUGAR) || stack.is(Items.HONEYCOMB) || stack.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS)
                || (this.isLarva() && (stack.is(net.minecraft.tags.ItemTags.LEAVES) || stack.is(Items.SHORT_GRASS)))
                || (this.species == GirlSpecies.MANTIS_BUG && stack.is(Items.SPIDER_EYE))
                || (this.species == GirlSpecies.HORNET_BUG && (stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)
                        || stack.is(Items.MELON_SLICE)));                   // an omnivore: fruit, and meat like everyone
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.HONEY_BOTTLE) || stack.is(Items.SPIDER_EYE) ? 2 : 1;
    }

    // --- the Pixi and the Rlff --------------------------------------------------------------------------------------

    private static final net.minecraft.resources.ResourceLocation SHRINK =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.linkyoshi90.harmontide.HarmontideMod.MODID, "pixi_shrink");
    private static final String SHRUNK_UNTIL = "PixiShrunkUntil";
    private static final int SHRINK_TICKS = 600;
    private long nextShrink;

    /** Half a minute at half size, small enough for a gap one block high. Whether she did it (she needs a minute between). */
    public boolean shrink(Player player) {
        long now = this.level().getGameTime();
        if (now < this.nextShrink) return false;
        var scale = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (scale == null) return false;
        if (!scale.hasModifier(SHRINK)) {
            scale.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(SHRINK, -0.5,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        player.getPersistentData().putLong(SHRUNK_UNTIL, now + SHRINK_TICKS);
        this.nextShrink = now + 1200;
        this.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.6F);
        return true;
    }

    /** Back to size once the time is up. */
    public static void growBack(Player player) {
        var scale = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (scale != null && scale.hasModifier(SHRINK) && player.level().getGameTime() >= player.getPersistentData().getLong(SHRUNK_UNTIL)) scale.removeModifier(SHRINK);
    }

    /** Her friend, standing in water near her, heals. Whether they did. */
    public boolean healingWater() {
        Player friend = this.bondedPlayer(10, 12);
        if (friend == null || !friend.level().getFluidState(friend.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60));
        return true;
    }

    /** A glass bottle catches a firefly or a fairy in a jar (why?). */
    @Override
    @javax.annotation.Nullable
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.PIXI_BUG && hand == net.minecraft.world.InteractionHand.MAIN_HAND && player.isShiftKeyDown() && player.getMainHandItem().isEmpty()
                && this.getAffection(player) >= 10) {
            boolean client = this.level().isClientSide;
            if (!client) this.shrink(player);
            return net.minecraft.world.InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.SCARABRONZE_BUG && stack.is(Items.ROTTEN_FLESH) && !this.isBaby()) {   // she rolls it into bone meal
            if (this.level() instanceof ServerLevel server) {
                stack.consume(1, player);
                ItemStack meal = new ItemStack(Items.BONE_MEAL, 2);
                if (!player.getInventory().add(meal)) player.drop(meal, false);
                this.playSound(SoundEvents.ROOTED_DIRT_PLACE, 1.0F, 1.2F);
                this.addAffection(player, 1);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if ((this.species != GirlSpecies.FIREFLY_BUG && this.species != GirlSpecies.FAIRY_BUG) || !stack.is(Items.GLASS_BOTTLE)) return null;
        if (this.level() instanceof ServerLevel server) {
            stack.consume(1, player);
            ItemStack jar = jar(this);
            if (!player.getInventory().add(jar)) player.drop(jar, false);
            this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.4F);
            this.clearLight();
            this.discard();
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                var why = server.getServer().getAdvancements().get(com.linkyoshi90.harmontide.HarmontideMod.id("why"));
                if (why != null) sp.getAdvancements().award(why, "jarred");
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** The jar she goes into: her species and everything she is, kept for when she is let out. */
    public static ItemStack jar(AbstractMonsterGirl girl) {
        net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
        girl.saveWithoutId(saved);
        saved.remove("UUID");
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putString("Species", girl.getSpecies().id());
        data.put("Girl", saved);
        ItemStack jar = new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.GIRL_JAR.get());
        net.minecraft.world.item.BlockItem.setBlockEntityData(jar, com.linkyoshi90.harmontide.registry.ModBlockEntities.GIRL_JAR.get(), data);
        boolean fairy = girl.getSpecies() == GirlSpecies.FAIRY_BUG;
        jar.set(net.minecraft.core.component.DataComponents.BLOCK_STATE, net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                .with(com.linkyoshi90.harmontide.block.GirlJarBlock.KIND, fairy ? com.linkyoshi90.harmontide.block.GirlJarBlock.Kind.FAIRY
                        : com.linkyoshi90.harmontide.block.GirlJarBlock.Kind.FIREFLY));
        jar.set(net.minecraft.core.component.DataComponents.ITEM_NAME, net.minecraft.network.chat.Component.translatable(
                fairy ? "item.harmontide.fairy_jar" : "item.harmontide.firefly_jar"));
        return jar;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case BEE_BUG, QUEEN_BEE_BUG -> new ItemStack(r < 60 ? Items.HONEYCOMB : Items.HONEY_BOTTLE);
            case FAIRY_BUG, FAIRY_QUEEN_BUG -> new ItemStack(r < 50 ? Items.GLOW_BERRIES : r < 85 ? Items.GLOWSTONE_DUST : Items.GOLDEN_APPLE);
            case FIREFLY_BUG -> new ItemStack(r < 50 ? Items.GLOWSTONE_DUST : r < 85 ? Items.GLOW_BERRIES : Items.GLOW_INK_SAC, r < 85 ? 2 : 1);
            case MOTH_BUG -> new ItemStack(r < 70 ? Items.STRING : Items.WHITE_WOOL);
            case SCARABRONZE_BUG -> new ItemStack(r < 45 ? Items.BONE_MEAL : r < 80 ? Items.GOLD_NUGGET : r < 97 ? Items.LAPIS_LAZULI : Items.GOLD_INGOT, r < 97 ? 3 : 1);
            case SCORPION_BUG -> new ItemStack(r < 60 ? Items.SPIDER_EYE : Items.GOLD_NUGGET, r < 60 ? 1 : 3);
            case HORNET_BUG -> new ItemStack(r < 55 ? Items.PAPER : Items.CLAY_BALL, r < 55 ? 2 : 3);
            case MANTIS_BUG -> new ItemStack(r < 50 ? Items.STRING : r < 85 ? Items.SPIDER_EYE : Items.ENDER_PEARL, r < 50 ? 3 : 1);
            default -> new ItemStack(r < 50 ? Items.POPPY : r < 80 ? Items.DANDELION : Items.CORNFLOWER, 2);
        };
    }

    // --- young ---------------------------------------------------------------------------------------------------

    /** Moths and papillons lay eggs that hatch into caterpillars; the scorpion carries her young on her back. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        String young = this.species == GirlSpecies.MOTH_BUG ? "moth_larva_bug" : this.species == GirlSpecies.PAPILLON_BUG ? "papillon_larva_bug" : null;
        if (young != null) {
            BlockPos o = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -2, -4), o.offset(4, 2, 4))) {
                if (!canHoldBlock(level, p)) continue;
                level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.SILK), 3);
                if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(young, 2, father).withPaint(this.getPaint());
                return true;
            }
        }
        boolean born = super.giveBirth(level, father);
        if (born && this.species == GirlSpecies.SCORPION_BUG) {
            this.carryTicks = 24000;
            this.setFlag(FLAG_CARRYING, true);
        }
        return born;
    }

    /** The caterpillar's change: the adult form takes her place, with all her bonds. */
    public AbstractMonsterGirl metamorphose(ServerLevel level) {
        GirlSpecies adult = this.species == GirlSpecies.MOTH_LARVA_BUG ? GirlSpecies.MOTH_BUG : GirlSpecies.PAPILLON_BUG;
        AbstractMonsterGirl moth = ModGirls.type(adult).create(level);
        if (moth == null) return null;
        moth.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        CompoundTag tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        tag.remove("Variant");
        moth.readAdditionalSaveData(tag);
        moth.setBaby(false);
        moth.randomizeVariant();
        moth.setPersistenceRequired();
        level.addFreshEntity(moth);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
        this.discard();
        return moth;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** The ant carries the loose items within 8 of her friend straight to them. How many, or -1 without a friend. */
    public int haul(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int carried = 0;
        for (net.minecraft.world.entity.item.ItemEntity item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, friend.getBoundingBox().inflate(8), i -> i.isAlive() && !i.hasPickUpDelay())) {
            if (carried >= 3) break;
            item.teleportTo(friend.getX(), friend.getY() + 0.5, friend.getZ());
            item.setNoPickUpDelay();
            carried++;
        }
        return carried;
    }

    /** The queen raises an ant, who knows her friend, with fewer than four near her. True if she did. */
    public boolean brood(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 32);
        if (friend == null) return false;
        var kind = com.linkyoshi90.harmontide.registry.ModGirls.type(GirlSpecies.ANT_BUG);
        if (level.getEntities(kind, this.getBoundingBox().inflate(16), net.minecraft.world.entity.Entity::isAlive).size() >= 4) return false;
        AbstractMonsterGirl ant = kind.create(level);
        if (ant == null) return false;
        ant.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0);
        ant.setPersistenceRequired();
        ant.addAffection(friend, com.linkyoshi90.harmontide.entity.Orders.AFFECTION);
        level.addFreshEntity(ant);
        return true;
    }

    /** The spider-wasp stings the spiders and spider-girls within 10 of her friend who are hostile: they can't move for 6 seconds. How many, or -1 without a friend. */
    public int paralyse(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int stung = 0;
        for (net.minecraft.world.entity.monster.Spider s : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Spider.class, friend.getBoundingBox().inflate(10), net.minecraft.world.entity.monster.Spider::isAlive)) {
            s.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 120, 9));
            s.hurt(this.damageSources().mobAttack(this), 3);
            stung++;
        }
        return stung;
    }

    /** The dragonfly hits the phantoms and vexes within 16 of her friend for 8. How many, or -1 without a friend. */
    public int hawk(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int hit = 0;
        for (net.minecraft.world.entity.Mob m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, friend.getBoundingBox().inflate(16), x -> x instanceof net.minecraft.world.entity.monster.Phantom || x instanceof net.minecraft.world.entity.monster.Vex)) {
            m.hurt(this.damageSources().mobAttack(this), 8);
            hit++;
        }
        return hit;
    }

    /** The ladybug: Luck for her friend near her, and a stage on for the sweet berry bushes within 4. The bushes she ripened, or -1 without a friend. */
    public int luckySpots(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 8);
        if (friend == null) return -1;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 400, 0, true, true));
        int grown = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-4, -1, -4), this.blockPosition().offset(4, 2, 4))) {
            var state = level.getBlockState(p);
            if (state.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH) && state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) < 3) {
                level.setBlockAndUpdate(p, state.setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) + 1));
                grown++;
            }
        }
        return grown;
    }

    /** The cicada's song: Speed and Haste for her friend near her. True if she sang. */
    public boolean chorus() {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 600, 0, true, true));
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 600, 0, true, true));
        this.playSound(net.minecraft.sounds.SoundEvents.BEE_LOOP, 1.0F, 1.6F);
        return true;
    }

    /** The antlion's sand: monsters standing on sand within 8 of her friend sink and slow. How many, or -1 without a friend. */
    public int pit(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int sunk = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8), x -> x.isAlive() && x.getBlockStateOn().is(net.minecraft.tags.BlockTags.SAND))) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
            m.hurt(this.damageSources().mobAttack(this), 2);
            sunk++;
        }
        return sunk;
    }

    /** The tardigrade, nearly dead, curls into a tun once a day: all but untouchable, and she mends. True if she did. */
    public boolean tun(long day) {
        if (this.getHealth() > 4 || this.getPersistentData().getLong("TunDay") == day + 1) return false;
        this.getPersistentData().putLong("TunDay", day + 1);
        this.setHealth(this.getMaxHealth());
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 100, 4));
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.TARDIGRADE_BUG && !this.level().isClientSide && !this.isBaby()) this.tun(this.level().getDayTime() / 24000L);
        if (this.species == GirlSpecies.ANTLION_BUG && this.level() instanceof net.minecraft.server.level.ServerLevel dunes && !this.isBaby() && this.tickCount % 61 == 0) this.pit(dunes);
        if (this.species == GirlSpecies.CICADA_BUG && !this.level().isClientSide && !this.isBaby() && this.tickCount % 3001 == 0) this.chorus();
        if (this.level() instanceof net.minecraft.server.level.ServerLevel meadow && !this.isBaby()) {
            if (this.species == GirlSpecies.DRAGONFLY_BUG && this.tickCount % 41 == 0) this.hawk(meadow);
            if (this.species == GirlSpecies.LADYBUG_BUG && this.tickCount % 401 == 0) this.luckySpots(meadow);
        }
        if (this.level() instanceof net.minecraft.server.level.ServerLevel colony && !this.isBaby()) {
            if (this.species == GirlSpecies.ANT_BUG && this.tickCount % 21 == 0) this.haul(colony);
            if (this.species == GirlSpecies.QUEEN_ANT_BUG && this.tickCount % 6001 == 0) this.brood(colony);
            if (this.species == GirlSpecies.SPIDER_WASP_BUG && this.tickCount % 41 == 0) this.paralyse(colony);
        }
        if (this.species == GirlSpecies.GIRTABLILU_BUG && this.level() instanceof net.minecraft.server.level.ServerLevel gate && !this.isBaby() && this.tickCount % 20 == 0 && !gate.isDay()) this.gateGuard(gate);
        if (this.species == GirlSpecies.MOTHMAN_BUG && this.level() instanceof net.minecraft.server.level.ServerLevel omen && !this.isBaby() && this.tickCount % 20 == 0) this.omen(omen, !omen.isDay());
        if (this.species == GirlSpecies.DATE_BEETLE_BUG && this.level() instanceof ServerLevel oasis && !this.isBaby() && this.tickCount % 200 == 0) this.shareDate(oasis);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.TEMPLE_MANTIS_BUG && !this.isBaby() && this.tickCount % 20 == 0) this.cutTripwire(server, false);
        if (this.carryTicks > 0 && --this.carryTicks == 0) this.setFlag(FLAG_CARRYING, false);
        if (this.species == GirlSpecies.FIREFLY_BUG) this.glow(server, server.isDay() ? 0 : 7);   // her lantern lights the night
        if (this.species == GirlSpecies.MANTIS_BUG && !this.isBaby()) this.ambush(server);
        if (this.species == GirlSpecies.SCARABRONZE_BUG && !this.isBaby() && this.tickCount % 20 == 0) this.sunrise(server);
        if (this.isLarva()) {
            if (++this.age >= METAMORPHOSIS) {
                this.metamorphose(server);
                return;
            }
            if (this.tickCount % 81 == 0) this.forage(server);
            return;
        }
        switch (this.species) {
            case MOTH_BUG -> {
                this.seekLight(server);
                if (this.tickCount % 20 == 0) this.eatWool(server);
            }
            case FAIRY_BUG -> this.fairy(server);
            case RLFF_BUG -> {
                if (this.tickCount % 20 == 0) this.healingWater();
            }
            case FAIRY_QUEEN_BUG -> this.bless(server);
            case BEE_BUG, QUEEN_BEE_BUG -> {
                this.tickBee(server);
                if (this.tickCount % 100 == 0) this.pollinate(server);
            }
            case PAPILLON_BUG -> this.tickPapillon(server);
            default -> {
            }
        }
        if (this.species == GirlSpecies.MOTH_BUG && this.tickCount % 40 == 0) {
            Player p = this.bondedPlayer(60, 12);
            if (p != null) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        }
    }

    /** Like real clothes moths, she nibbles wool (and carpet) lying about, and it heals her. Never wool that is built into something. */
    private void eatWool(ServerLevel level) {
        ItemEntity wool = this.nearest(ItemEntity.class, 6.0, e -> e.isAlive() && (e.getItem().is(net.minecraft.tags.ItemTags.WOOL) || e.getItem().is(net.minecraft.tags.ItemTags.WOOL_CARPETS)));
        if (wool == null) return;
        if (this.distanceToSqr(wool) > 9.0) {                              // a moth flutters: close enough to reach is close enough
            this.getNavigation().moveTo(wool, 1.0);
            return;
        }
        wool.getItem().shrink(1);
        if (wool.getItem().isEmpty()) wool.discard();
        this.heal(2.0F);
        this.playSound(SoundEvents.WOOL_BREAK, 0.8F, 1.3F);
    }

    private boolean floraNectar;

    public boolean carriesFloraNectar() {
        return this.floraNectar;
    }

    /**
     * A bee girl next to a flora girl with nectar to spare gathers some. Carrying it past a vanilla flower, she may
     * pollinate it (one chance in twenty a visit): the flower becomes a young alraune. Not where there are already
     * four flora girls within 16 blocks, and only where mobGriefing allows.
     */
    public void pollinate(ServerLevel level) {
        if (this.isBaby()) return;
        if (!this.floraNectar) {
            Flora flora = this.nearest(Flora.class, 3.0, f -> f.hasNectar());
            if (flora != null) {
                flora.giveNectar();
                this.floraNectar = true;
                level.sendParticles(ParticleTypes.FALLING_NECTAR, this.getX(), this.getY() + 0.5, this.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
            }
            return;
        }
        if (this.random.nextInt(20) != 0 || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this)) return;
        if (level.getEntitiesOfClass(Flora.class, this.getBoundingBox().inflate(16)).size() >= 4) return;
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-2, -2, -2), this.blockPosition().offset(2, 1, 2))) {
            if (!level.getBlockState(p).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) continue;
            BlockPos at = p.immutable();
            if (!(com.linkyoshi90.harmontide.registry.ModGirls.type(GirlSpecies.ALRAUNE_FLORA).create(level) instanceof Flora bud)) return;
            level.removeBlock(at, false);
            bud.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, this.random.nextFloat() * 360.0F, 0.0F);
            bud.finalizeSpawn(level, level.getCurrentDifficultyAt(at), net.minecraft.world.entity.MobSpawnType.BREEDING, null);
            bud.setBaby(true);
            level.addFreshEntity(bud);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.0);
            this.floraNectar = false;
            return;
        }
    }

    /** Caterpillars eat grass and leaves, and any food lying about. */
    private void forage(ServerLevel level) {
        ItemEntity food = this.nearest(ItemEntity.class, 6.0, e -> e.isAlive() && this.isFood(e.getItem()));
        if (food != null) {
            if (this.distanceToSqr(food) < 2.0) {
                food.getItem().shrink(1);
                this.heal(2.0F);
                this.startTimedAction(Action.EAT, 40);
            } else {
                this.getNavigation().moveTo(food, 1.0);
            }
            return;
        }
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-1, -1, -1), o.offset(1, 1, 1))) {
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.SHORT_GRASS) || s.is(BlockTags.LEAVES)) {
                level.destroyBlock(p, false, this);
                this.heal(2.0F);
                this.startTimedAction(Action.EAT, 40);
                return;
            }
        }
    }

    /** At night the moth flies to the brightest light within sixteen blocks and circles it. */
    private void seekLight(ServerLevel level) {
        if (level.isDay()) {
            this.light = null;
            return;
        }
        if (this.tickCount % 61 == 0) {
            BlockPos o = this.blockPosition();
            int best = 9;
            BlockPos found = null;
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-16, -6, -16), o.offset(16, 6, 16))) {
                int e = level.getBlockState(p).getLightEmission(level, p);
                if (e > best) {
                    best = e;
                    found = p.immutable();
                }
            }
            this.light = found;
        }
        if (this.light != null && this.getTarget() == null && this.tickCount % 10 == 0) {
            double a = this.tickCount * 0.15;
            Vec3 c = Vec3.atCenterOf(this.light).add(Math.cos(a) * 1.8, 0.8, Math.sin(a) * 1.8);
            this.getNavigation().moveTo(c.x, c.y, c.z, 1.0);
        }
    }

    @Nullable
    public BlockPos getLight() {
        return this.light;
    }

    // --- fairies -------------------------------------------------------------------------------------------------

    private static final Block[] FOUNTAIN_FLOWERS = {Blocks.POPPY, Blocks.ALLIUM, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET,
            Blocks.PINK_TULIP, Blocks.LILY_OF_THE_VALLEY, Blocks.BLUE_ORCHID};

    /** Whether her fountain is finished: water in the middle, a flower on all eight sides. */
    public boolean isFountainFinished() {
        if (this.fountain == null) return false;
        Level level = this.level();
        if (!level.getFluidState(this.fountain).is(net.minecraft.tags.FluidTags.WATER)) return false;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if ((dx != 0 || dz != 0) && !level.getBlockState(this.fountain.offset(dx, 1, dz)).is(BlockTags.SMALL_FLOWERS)) return false;
        }
        return true;
    }

    @Nullable
    public BlockPos getFountain() {
        return this.fountain;
    }

    public void setFountain(@Nullable BlockPos pos) {
        this.fountain = pos;
        this.fountainBuilt = 0;
    }

    /** Heals the hurt; builds her fountain step by step; its finished water heals; a queen may rise from it at night. */
    private void fairy(ServerLevel level) {
        if (this.tickCount % 40 == 0) {
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8), p -> p.getHealth() < p.getMaxHealth() && !this.isHostileTo(p))) {
                if (level.getGameTime() < this.nextHeal) break;
                this.nextHeal = level.getGameTime() + 200;
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                this.startTimedAction(Action.HEAL, 30);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 8, 0.4, 0.6, 0.4, 0);
            }
        }
        if (this.isBaby() || this.tickCount % 100 != 0) return;
        if (this.fountain == null) {
            BlockPos below = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition()).below();
            if (level.getBlockState(below).is(Blocks.GRASS_BLOCK)) this.fountain = below.immutable();
            return;
        }
        if (this.isFountainFinished()) {
            for (Player p : level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(this.fountain).inflate(2))) {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));      // the healing water
            }
            if (!level.isDay() && this.random.nextInt(30) == 0
                    && level.getEntities(ModGirls.type(GirlSpecies.FAIRY_QUEEN_BUG), new net.minecraft.world.phys.AABB(this.fountain).inflate(48), e -> true).isEmpty()) {
                this.summonQueen(level);
            }
            return;
        }
        if (this.distanceToSqr(Vec3.atCenterOf(this.fountain)) > 6 * 6) {
            this.getNavigation().moveTo(this.fountain.getX() + 0.5, this.fountain.getY() + 2, this.fountain.getZ() + 0.5, 1.0);
            return;
        }
        // the next step: the pool first, then the flowers one by one
        if (!level.getFluidState(this.fountain).is(net.minecraft.tags.FluidTags.WATER)) {
            level.setBlock(this.fountain, Blocks.WATER.defaultBlockState(), 3);
        } else {
            for (int i = 0; i < 8; i++) {
                int dx = new int[]{-1, 0, 1, 1, 1, 0, -1, -1}[i], dz = new int[]{-1, -1, -1, 0, 1, 1, 1, 0}[i];
                BlockPos f = this.fountain.offset(dx, 1, dz);
                if (level.getBlockState(f).is(BlockTags.SMALL_FLOWERS)) continue;
                if (level.getBlockState(f).canBeReplaced() && level.getBlockState(f.below()).is(BlockTags.DIRT)) {
                    level.setBlock(f, FOUNTAIN_FLOWERS[i].defaultBlockState(), 3);
                    this.fountainBuilt++;
                }
                break;
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, this.fountain.getX() + 0.5, this.fountain.getY() + 1.2, this.fountain.getZ() + 0.5, 6, 0.6, 0.3, 0.6, 0.01);
    }

    public AbstractMonsterGirl summonQueen(ServerLevel level) {
        AbstractMonsterGirl queen = ModGirls.type(GirlSpecies.FAIRY_QUEEN_BUG).create(level);
        if (queen == null || this.fountain == null) return null;
        queen.moveTo(this.fountain.getX() + 0.5, this.fountain.getY() + 1.2, this.fountain.getZ() + 0.5, 0, 0);
        queen.finalizeSpawn(level, level.getCurrentDifficultyAt(this.fountain), MobSpawnType.EVENT, null);
        level.addFreshEntity(queen);
        level.sendParticles(ParticleTypes.END_ROD, queen.getX(), queen.getY() + 1, queen.getZ(), 40, 0.6, 1.0, 0.6, 0.05);
        level.playSound(null, this.fountain, SoundEvents.AMETHYST_BLOCK_RESONATE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.5F, 1.2F);
        return queen;
    }

    /** The Fairy Queen blesses those she favours (fond, 10+). */
    private void bless(ServerLevel level) {
        if (this.tickCount % 200 != 0) return;
        List<Holder<MobEffect>> gifts = List.of(MobEffects.REGENERATION, MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.LUCK, MobEffects.DAMAGE_RESISTANCE);
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10), p -> this.getAffection(p) >= 10)) {
            p.addEffect(new MobEffectInstance(gifts.get(this.random.nextInt(gifts.size())), 1200, 0));
            this.startTimedAction(Action.HEAL, 30);
        }
    }

    // --- bees and papillons --------------------------------------------------------------------------------------

    public boolean hasNectar() {
        return this.nectar;
    }

    /** Flowers, then crops on the way, then the hive: one honey level per trip (twice as quick near a queen). */
    private void tickBee(ServerLevel level) {
        if (this.getTarget() != null || this.tickCount % 20 != 0) return;
        boolean queenNear = this.species == GirlSpecies.QUEEN_BEE_BUG
                || this.nearest(Bug.class, 24, b -> b.species == GirlSpecies.QUEEN_BEE_BUG) != null;
        BlockPos o = this.blockPosition();
        if (!this.nectar) {
            BlockPos flower = null;
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-12, -4, -12), o.offset(12, 4, 12))) {
                if (level.getBlockState(p).is(BlockTags.FLOWERS)) {
                    flower = p.immutable();
                    break;
                }
            }
            if (flower == null) return;
            if (this.distanceToSqr(Vec3.atCenterOf(flower)) > 2.5 * 2.5) {
                this.getNavigation().moveTo(flower.getX() + 0.5, flower.getY() + 1, flower.getZ() + 0.5, 1.0);
                return;
            }
            this.workTicks += queenNear ? 40 : 20;
            if (this.getAction() == Action.NONE) this.startTimedAction(Action.POLLINATE, 40);
            if (this.workTicks >= 200) {
                this.workTicks = 0;
                this.nectar = true;
                level.sendParticles(ParticleTypes.FALLING_NECTAR, this.getX(), this.getY() + 0.5, this.getZ(), 6, 0.3, 0.3, 0.3, 0);
            }
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-2, -3, -2), o.offset(2, 0, 2))) {    // crops grow under her
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s) && this.random.nextInt(4) == 0) {
                level.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                level.levelEvent(2005, p, 0);
                break;
            }
        }
        BlockPos hive = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-24, -6, -24), o.offset(24, 6, 24))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof BeehiveBlock && s.getValue(BeehiveBlock.HONEY_LEVEL) < BeehiveBlock.MAX_HONEY_LEVELS) {
                hive = p.immutable();
                break;
            }
        }
        if (hive == null) return;
        if (this.distanceToSqr(Vec3.atCenterOf(hive)) > 2.5 * 2.5) {
            this.getNavigation().moveTo(hive.getX() + 0.5, hive.getY() + 1, hive.getZ() + 0.5, 1.0);
            return;
        }
        BlockState s = level.getBlockState(hive);
        level.setBlock(hive, s.setValue(BeehiveBlock.HONEY_LEVEL, s.getValue(BeehiveBlock.HONEY_LEVEL) + 1), 3);
        this.nectar = false;
        level.playSound(null, hive, SoundEvents.BEEHIVE_WORK, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    /** She rests among flowers and now and then plants a new one on bare grass beside her. */
    private void tickPapillon(ServerLevel level) {
        if (this.tickCount % 600 != 0 || this.random.nextInt(3) != 0) return;
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos p = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    o.offset(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3));
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(p, FOUNTAIN_FLOWERS[this.random.nextInt(FOUNTAIN_FLOWERS.length)].defaultBlockState(), 3);
                this.startTimedAction(Action.POLLINATE, 40);
                return;
            }
        }
    }

    /** A player who places a beehive near a queen bee is her friend for life (+5 each). */
    public static void onHivePlaced(Player player, BlockPos pos) {
        for (Bug q : player.level().getEntitiesOfClass(Bug.class, new net.minecraft.world.phys.AABB(pos).inflate(16), b -> b.species == GirlSpecies.QUEEN_BEE_BUG)) {
            q.addAffection(player, QUEEN_HIVE_BONUS);
            if (player.level() instanceof ServerLevel s) q.hearts(s, 5);
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case BEE_BUG, QUEEN_BEE_BUG -> SoundEvents.BEE_LOOP;
            case MOSQUITO_BUG, HORNET_BUG -> SoundEvents.BEE_LOOP_AGGRESSIVE;
            case FAIRY_BUG, FAIRY_QUEEN_BUG -> SoundEvents.AMETHYST_BLOCK_CHIME;
            default -> null;
        };
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.HONEY_DRINK;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", this.age);
        tag.putBoolean("Nectar", this.nectar);
        if (this.fountain != null) tag.putLong("Fountain", this.fountain.asLong());
        tag.putInt("Carry", this.carryTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.age = tag.getInt("Age");
        this.nectar = tag.getBoolean("Nectar");
        this.fountain = tag.contains("Fountain") ? BlockPos.of(tag.getLong("Fountain")) : null;
        this.carryTicks = tag.getInt("Carry");
    }

    @Override
    public boolean isHealer() {
        return this.species == GirlSpecies.FAIRY_BUG || this.species == GirlSpecies.FAIRY_QUEEN_BUG;
    }

    // --- the temple mantis ---------------------------------------------------------------------------------------

    /** A tripwire within 8 blocks of her friend, inside a structure (or `anywhere`), cut before anyone trips it. Where, or null. */
    @Nullable
    public BlockPos cutTripwire(ServerLevel level, boolean anywhere) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return null;
        for (BlockPos p : BlockPos.betweenClosed(friend.blockPosition().offset(-8, -4, -8), friend.blockPosition().offset(8, 4, 8))) {
            var s = level.getBlockState(p);
            if (!s.is(Blocks.TRIPWIRE)) continue;
            if (!anywhere && !level.structureManager().getStructureWithPieceAt(p, h -> true).isValid()) continue;   // a player's own wires stay
            BlockPos at = p.immutable();
            level.setBlock(at, s.setValue(net.minecraft.world.level.block.TripWireBlock.DISARMED, true), 4);   // cut like shears: nothing fires
            level.destroyBlock(at, true, this);
            this.getLookControl().setLookAt(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
            this.startTimedAction(Action.STRIKE, 12);
            this.playSound(net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, 1.0F, 1.2F);
            return at;
        }
        return null;
    }

    // --- the date palm beetle ------------------------------------------------------------------------------------

    /** Her friend within 6 blocks, hungry (14 or fewer hunger points), gets a date: three hunger points. Whether she shared. */
    public boolean shareDate(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 6);
        if (friend == null || friend.getFoodData().getFoodLevel() > 14) return false;
        friend.getFoodData().eat(3, 0.6F);
        level.playSound(null, friend.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.2F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 1, 0, 0, 0, 0);
        return true;
    }

    // --- the Mothman (twentieth wave) -------------------------------------------------------------------------------

    /** At night the monsters within 16 blocks of her friend glow, seen through walls, for two seconds. How many, 0 by day, -1
     *  without a friend. */
    public int omen(net.minecraft.server.level.ServerLevel level, boolean night) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (!night) return 0;
        int n = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(16))) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 40, 0, true, false));
            n++;
        }
        return n;
    }

    /**
     * The girtablilu's watch (twenty-fourth wave): at night (her caller checks), the nearest monster within 6 of her friend is stung:
     * 2 damage, Poison and Slowness II for 5 seconds. 1 when she stung one, 0 when there was none, -1 without a friend.
     */
    public int gateGuard(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().ignoreLineOfSight(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(6));
        if (m == null) return 0;
        m.hurt(level.damageSources().mobAttack(this), 2.0F);
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 0));
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1));   // the undead shrug off the poison, not the numbness
        this.getLookControl().setLookAt(m);
        return 1;
    }
}
