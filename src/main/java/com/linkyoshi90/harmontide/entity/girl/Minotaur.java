package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * The cattle family (minotaur_field_guide.html): cattle, Minoan, bison, aurochs, water buffalo, sheep, highland, yak. The
 * sheep's wool comes off with shears ({@link #FLAG_SHORN}, back once she has grazed) and takes any dye; the highland and
 * the yak never freeze, and the yak carries a chest of cargo. The only family with men:
 * about one in three is a bull ({@link #FLAG_MALE}, drawn with the &lt;species&gt;_male model). A cow and a bull fed
 * wheat raise a calf with no player involved, so herds can be farmed. Cows give milk to a bucket. Hurt one and she
 * charges; the Minoan guards her ground, bison stampede, the aurochs cannot be pushed, the water buffalo wallows in water,
 * and a devoted minotaur ploughs your fields.
 */
public class Minotaur extends AbstractMonsterGirl {
    public static final int MILK_COOLDOWN = 6000;
    /** How long the machine's harness stays on her, and how long she blushes after a bucket. */
    public static final int MACHINE_MILK_TICKS = 100, BUCKET_BLUSH_TICKS = 60;
    /** Ticks between drips while her milk is ready. */
    public static final int DRIP_INTERVAL = 15;
    public static final int LOVE_TICKS = 600;
    public static final int GUARD_RADIUS = 10;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> DATA_WOOL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(Minotaur.class, net.minecraft.network.syncher.EntityDataSerializers.BYTE);
    private final net.minecraft.world.SimpleContainer pack = new net.minecraft.world.SimpleContainer(18);

    private long nextMilk;
    private boolean milkFull;
    private int harnessTicks;
    private int inLove;
    @Nullable
    private BlockPos post;
    private long warnedAt = -1000;
    private int stampede;
    @Nullable
    private Vec3 stampedeFrom;

    public Minotaur(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new StampedeGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5, true) {
            @Override
            public void tick() {
                super.tick();
                if (Minotaur.this.getAction() == Action.NONE) Minotaur.this.startTimedAction(Action.CHARGE, 10);
            }
        });
        this.goalSelector.addGoal(3, new CourtGoal());
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.WHEAT, Items.HAY_BLOCK), false));
        if (species == GirlSpecies.WATER_BUFFALO_MINOTAUR) this.goalSelector.addGoal(5, new WallowGoal());
        this.goalSelector.addGoal(6, new EatBlockGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (species == GirlSpecies.MINOAN_MINOTAUR) {                    // the labyrinth guard: strangers on her ground
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, p -> this.isTrespasser((Player) p)));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case DUSTBISON_MINOTAUR -> girlAttributes(26, 0.28, 3);
            case MINOAN_MINOTAUR -> girlAttributes(40, 0.27, 8).add(Attributes.ATTACK_KNOCKBACK, 1.5).add(Attributes.ARMOR, 4);
            case BISON_MINOTAUR -> girlAttributes(40, 0.28, 6).add(Attributes.ATTACK_KNOCKBACK, 1.5);
            case AUROCHS_MINOTAUR -> girlAttributes(48, 0.27, 9).add(Attributes.ATTACK_KNOCKBACK, 2.5).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
            case WATER_BUFFALO_MINOTAUR -> girlAttributes(36, 0.25, 6).add(Attributes.ATTACK_KNOCKBACK, 1.2);
            case SHEEP_MINOTAUR -> girlAttributes(24, 0.25, 2);
            case HIGHLAND_MINOTAUR -> girlAttributes(34, 0.25, 5).add(Attributes.ATTACK_KNOCKBACK, 1.0);
            case YAK_MINOTAUR -> girlAttributes(40, 0.24, 5).add(Attributes.ATTACK_KNOCKBACK, 1.0);
            case MOOSKUH_MINOTAUR -> girlAttributes(22, 0.22, 1);
            case BRONZEJAW_MINOTAUR -> girlAttributes(44, 0.24, 6).add(Attributes.ARMOR, 4).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
            case RHINO_MINOTAUR -> girlAttributes(48, 0.26, 9).add(Attributes.ATTACK_KNOCKBACK, 2.5).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
            case RHIWOOL_MINOTAUR -> girlAttributes(44, 0.26, 8).add(Attributes.ATTACK_KNOCKBACK, 2.5).add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
            default -> girlAttributes(30, 0.25, 4).add(Attributes.ATTACK_KNOCKBACK, 1.0);
        };
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOOL, (byte) 0);
    }

    // --- the sheep's wool, the cold, the yak's pack ----------------------------------------------------------------

    /** The dye on a sheep's wool, or null for her natural colour. */
    @Nullable
    public net.minecraft.world.item.DyeColor getWoolDye() {
        int w = this.entityData.get(DATA_WOOL);
        return w == 0 ? null : net.minecraft.world.item.DyeColor.byId(w - 1);
    }

    public void setWoolDye(@Nullable net.minecraft.world.item.DyeColor dye) {
        this.entityData.set(DATA_WOOL, (byte) (dye == null ? 0 : dye.getId() + 1));
    }

    /** The sheep and the rhiwool: wool to shear and to dye. */
    public boolean isWoolly() {
        return this.species == GirlSpecies.SHEEP_MINOTAUR || this.species == GirlSpecies.RHIWOOL_MINOTAUR;
    }

    public boolean isShorn() {
        return this.hasFlag(FLAG_SHORN);
    }

    /** What her fleece gives: the dye's wool, else her own colour's. */
    public net.minecraft.world.item.DyeColor woolColour() {
        if (this.getWoolDye() != null) return this.getWoolDye();
        return switch (this.getVariantName()) {
            case "black" -> net.minecraft.world.item.DyeColor.BLACK;
            case "brown" -> net.minecraft.world.item.DyeColor.BROWN;
            case "grey" -> net.minecraft.world.item.DyeColor.GRAY;
            case "red" -> net.minecraft.world.item.DyeColor.RED;
            default -> net.minecraft.world.item.DyeColor.WHITE;
        };
    }

    public static ItemStack wool(net.minecraft.world.item.DyeColor colour, int count) {
        return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace(colour.getName() + "_wool")), count);
    }

    public void shear(ServerLevel level) {
        this.setFlag(FLAG_SHORN, true);
        this.spawnAtLocation(wool(this.woolColour(), 1 + this.random.nextInt(3)), 1.0F);
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** Grazing grows a shorn sheep's wool back. */
    @Override
    public void ate() {
        super.ate();
        if (this.isShorn()) this.setFlag(FLAG_SHORN, false);
    }

    /** The highland (and the cattle's highland colouring) and the yak keep out any cold. */
    @Override
    public boolean canFreeze() {
        return this.species != GirlSpecies.HIGHLAND_MINOTAUR && this.species != GirlSpecies.YAK_MINOTAUR
                && !(this.species == GirlSpecies.CATTLE_MINOTAUR && "highland".equals(this.getVariantName())) && super.canFreeze();
    }

    public net.minecraft.world.SimpleContainer getPack() {
        return this.pack;
    }

    // --- bulls and cows ------------------------------------------------------------------------------------------

    public boolean isMale() {
        return this.hasFlag(FLAG_MALE);
    }

    public void setMale(boolean male) {
        this.setFlag(FLAG_MALE, male);
        this.refreshDimensions();
    }

    /** Bulls stand a tenth taller. */
    @Override
    public float sizeScale() {
        return super.sizeScale() * (this.isMale() ? 1.1F : 1.0F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (type != MobSpawnType.BREEDING && this.species != GirlSpecies.HOLSTAUR_MINOTAUR && this.random.nextInt(3) == 0) this.setMale(true);
        if (this.post == null) this.post = this.blockPosition();
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    /** Bulls share a bed only while gay sex is on, and that does not conceive. */
    @Override
    public boolean canShareBed() {
        if (this.isMale() && !com.linkyoshi90.harmontide.entity.Nsfw.gay()) return false;
        return super.canShareBed();
    }

    @Override
    public AbstractMonsterGirl spawnYoung(ServerLevel level, BlockPos pos, @Nullable UUID father) {
        AbstractMonsterGirl calf = super.spawnYoung(level, pos, father);
        if (calf instanceof Minotaur m) m.setMale(this.species != GirlSpecies.HOLSTAUR_MINOTAUR && this.random.nextBoolean());   // half the calves are bulls, none of a holstaur
        return calf;
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void setInLove(int ticks) {
        this.inLove = ticks;
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    /** Led on a lead and walking, she ploughs: the grass or dirt under her turns to farmland. True if she tilled. */
    public boolean ploughOnLead(ServerLevel level) {
        if (this.isBaby() || !(this.getLeashHolder() instanceof Player) || this.getDeltaMovement().horizontalDistanceSqr() < 0.0004) return false;
        BlockPos under = this.blockPosition().below();
        var s = level.getBlockState(under);
        if (!(s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || s.is(net.minecraft.world.level.block.Blocks.DIRT) || s.is(net.minecraft.world.level.block.Blocks.DIRT_PATH))
                || !level.getBlockState(under.above()).isAir() && !level.getBlockState(under.above()).canBeReplaced()) return false;
        if (!level.getBlockState(under.above()).isAir()) level.destroyBlock(under.above(), true, this);   // grass and flowers in the way
        level.setBlock(under, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 3);
        level.playSound(null, under, SoundEvents.HOE_TILL, this.getSoundSource(), 0.6F, 0.9F);
        return true;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.HAY_BLOCK) || this.species == GirlSpecies.MOOSKUH_MINOTAUR && stack.is(Items.GLOW_BERRIES);
    }

    /** The stone she rests on grows into moss. True if it did. */
    public boolean growMoss(ServerLevel level) {
        BlockPos under = this.blockPosition().below();
        var s = level.getBlockState(under);
        if (this.isBaby() || !this.onGround() || !(s.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || s.is(net.minecraft.world.level.block.Blocks.COBBLESTONE))) return false;
        level.setBlock(under, net.minecraft.world.level.block.Blocks.MOSS_BLOCK.defaultBlockState(), 3);
        level.levelEvent(2005, under.above(), 0);
        return true;
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.HAY_BLOCK) ? 3 : 1;
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        boolean client = this.level().isClientSide;
        InteractionResult home = com.linkyoshi90.harmontide.entity.Instincts.labyrinthSense(this, player, stack);   // shown a compass: the way home
        if (home != null) return home;
        if (this.isWoolly() && !this.isBaby()) {
            if (stack.is(Items.SHEARS) && !this.isShorn()) {
                if (this.level() instanceof ServerLevel server) {
                    this.shear(server);
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.getItem() instanceof net.minecraft.world.item.DyeItem dye && !this.isShorn() && this.woolColour() != dye.getDyeColor()) {
                if (!client) {
                    this.setWoolDye(dye.getDyeColor());
                    stack.consume(1, player);
                    this.playSound(SoundEvents.DYE_USE, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (this.species == GirlSpecies.YAK_MINOTAUR && !this.isBaby() && this.getAffection(player) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) {
            if (stack.is(Items.CHEST) && !this.hasFlag(FLAG_CHEST)) {                  // she takes a chest on her back
                if (!client) {
                    this.setFlag(FLAG_CHEST, true);
                    stack.consume(1, player);
                    this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.isEmpty() && player.isShiftKeyDown() && this.hasFlag(FLAG_CHEST)) {
                if (!client) player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new net.minecraft.world.inventory.ChestMenu(
                        net.minecraft.world.inventory.MenuType.GENERIC_9x2, id, inv, this.pack, 2), this.getDisplayName()));
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (stack.is(Items.WHEAT) && !this.isBaby() && !this.isInLove()) {             // wheat puts her in the mood for courting
            if (!client) {
                this.inLove = LOVE_TICKS;
                if (this.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0);
            }
            return null;                                                               // and it still counts as food
        }
        if (this.species == GirlSpecies.SHROOMOTAUR_MINOTAUR && stack.is(Items.BOWL) && !this.isMale() && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) this.fillBowl(server, player, stack);
            return InteractionResult.sidedSuccess(client);
        }
        boolean bottle = stack.is(Items.GLASS_BOTTLE) && this.species == GirlSpecies.HOLSTAUR_MINOTAUR;   // a holstaur's milk goes in a bottle too
        if ((stack.is(Items.BUCKET) || bottle) && !this.isMale() && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) {
                if (server.getGameTime() < this.nextMilk) {
                    player.displayClientMessage(Component.translatable("message.harmontide.no_milk", this.getDisplayName()), true);
                } else if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)
                        || !com.linkyoshi90.harmontide.entity.HandMilking.offer(server, this, sp, hand, bottle)) {
                    this.giveMilk(server, player, stack, bottle);             // option off, or she is not a grown partner
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(Items.BRUSH)) {
            if (this.level() instanceof ServerLevel server) {
                this.addAffection(player, 2);
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                this.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);
                this.hearts(server, 3);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    /** The milk is ready again: her cowbell rings once (a note over her head), so you know without asking. */
    private void ringBell(ServerLevel level) {
        this.nextMilk = 0;
        this.milkFull = true;
        this.playSound(SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.4, this.getZ(), 0, 0.3, 0.0, 0.0, 1.0);
    }

    public boolean isMilkReady() {
        return this.level().getGameTime() >= this.nextMilk;
    }

    public void setNextMilk(long time) {
        this.nextMilk = time;
        this.milkFull = false;
    }

    /** Her bell has rung and nobody has milked her since. A cow never milked has no bell to ring, so this stays false. */
    public boolean isMilkFull() {
        return this.milkFull;
    }

    /** Whether her milk drips from her chest just now. */
    public boolean isDripping() {
        return (this.milkFull && !this.isMale() && !this.isBaby() && com.linkyoshi90.harmontide.HarmontideConfig.milkDrips())
                || com.linkyoshi90.harmontide.entity.Lactation.isDripping(this);
    }

    private void blush(int ticks) {
        this.blushFor(ticks);
    }

    /** The machine has her: she stands in its harness for a few seconds, if those options are on. */
    public void milkedByMachine() {
        boolean harness = com.linkyoshi90.harmontide.HarmontideConfig.milkHarness();
        boolean embarrassed = com.linkyoshi90.harmontide.HarmontideConfig.milkEmbarrassed();
        if (harness) {
            this.harnessTicks = MACHINE_MILK_TICKS;
            this.setFlag(FLAG_HARNESSED, true);
        }
        if (harness || embarrassed) this.startTimedAction(Action.MILK, MACHINE_MILK_TICKS);
    }

    /** The timers are not saved, so a reload ends both of these. */
    private void tickMilking(ServerLevel level) {
        if (this.harnessTicks > 0) {
            this.harnessTicks--;
            this.getNavigation().stop();
        }
        if (this.harnessTicks == 0 && this.hasFlag(FLAG_HARNESSED)) this.setFlag(FLAG_HARNESSED, false);
        if (this.tickCount % DRIP_INTERVAL == 0 && this.isDripping()) com.linkyoshi90.harmontide.entity.Lactation.drip(level, this);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case HOLSTAUR_MINOTAUR -> r < 45 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.HOLSTAUR_MILK.get(), 2)
                    : r < 70 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.CHEESE.get(), 2)
                    : r < 85 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.HEARTFRUIT.get()) : new ItemStack(Items.WHEAT, 4);
            case CATTLE_MINOTAUR -> r < 35 ? new ItemStack(Items.MILK_BUCKET) : r < 60 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.CHEESE.get(), 2)
                    : new ItemStack(Items.WHEAT, 4);
            case WATER_BUFFALO_MINOTAUR -> r < 20 ? new ItemStack(Items.CAKE) : r < 60 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.CHEESE.get(), 2)
                    : new ItemStack(Items.MILK_BUCKET);
            case MINOAN_MINOTAUR -> new ItemStack(r < 10 ? Items.GOLD_INGOT : r < 50 ? Items.COPPER_INGOT : Items.BRICK, r < 50 ? 1 : 3);
            case SHEEP_MINOTAUR -> r < 60 ? wool(this.woolColour(), 2) : new ItemStack(r < 85 ? Items.STRING : Items.MUTTON, 2);
            case MOOSKUH_MINOTAUR -> r < 40 ? new ItemStack(Items.MOSS_BLOCK, 2) : r < 70 ? new ItemStack(Items.GLOW_BERRIES, 3) : r < 95 ? new ItemStack(Items.MILK_BUCKET)
                    : new ItemStack(Items.FLOWERING_AZALEA);
            case BRONZEJAW_MINOTAUR -> new ItemStack(r < 40 ? Items.WHEAT : r < 70 ? Items.LEATHER : r < 95 ? Items.COPPER_INGOT : Items.GOLD_INGOT, r < 95 ? 3 : 1);
            case RHIWOOL_MINOTAUR -> r < 50 ? wool(this.woolColour(), 3) : new ItemStack(r < 85 ? Items.LEATHER : Items.FLINT, 2);
            case YAK_MINOTAUR -> r < 40 ? new ItemStack(Items.MILK_BUCKET) : r < 70 ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.CHEESE.get(), 2)
                    : new ItemStack(Items.LEATHER, 2);
            default -> new ItemStack(r < 50 ? Items.LEATHER : Items.WHEAT, 2);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** The Minoan's ground: a stranger who comes within ten blocks of her post is warned with a bellow, then charged. */
    private boolean isTrespasser(Player p) {
        if (this.post == null || p.isCreative() || p.isSpectator() || this.getAffection(p) >= 10 || this.isBaby()) return false;
        if (p.distanceToSqr(Vec3.atCenterOf(this.post)) > GUARD_RADIUS * GUARD_RADIUS) return false;
        long now = this.level().getGameTime();
        if (now - this.warnedAt > 400) {
            this.warnedAt = now;
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.3F);
            p.displayClientMessage(Component.translatable("message.harmontide.trespass", this.getDisplayName()), true);
            return false;
        }
        return now - this.warnedAt > 60;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.BISON_MINOTAUR && source.getEntity() != null) {   // the herd stampedes
            for (Minotaur m : this.level().getEntitiesOfClass(Minotaur.class, this.getBoundingBox().inflate(16), m -> m.species == GirlSpecies.BISON_MINOTAUR && m != this)) {
                m.startStampede(source.getEntity().position());
            }
        }
        return hurt;
    }

    public void startStampede(Vec3 from) {
        this.stampede = 100;
        this.stampedeFrom = from;
    }

    public boolean isStampeding() {
        return this.stampede > 0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.playSound(SoundEvents.RAVAGER_ATTACK, 0.8F, 1.2F);
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** The pail (or a holstaur's three bottles). Same result by hand at once, or at the end of the close-up. */
    public void giveMilk(ServerLevel server, Player player, ItemStack stack, boolean bottle) {
        boolean friend = this.getAffection(player) >= BED_AFFECTION;
        this.setNextMilk(server.getGameTime() + (friend ? MILK_COOLDOWN / 2 : MILK_COOLDOWN));
        stack.consume(1, player);
        com.linkyoshi90.harmontide.entity.Lactation.relieve(server, this, player);
        ItemStack milk = bottle ? new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.HOLSTAUR_MILK.get(), 3)
                : com.linkyoshi90.harmontide.entity.Dairy.flavoured(this, new ItemStack(Items.MILK_BUCKET));
        if (!player.getInventory().add(milk)) player.drop(milk, false);
        com.linkyoshi90.harmontide.entity.Dairy.recordMilk(player, this);
        this.addAffection(player, 1);
        this.startTimedAction(Action.MILK, 40);
        if (com.linkyoshi90.harmontide.HarmontideConfig.milkEmbarrassed()) this.blush(BUCKET_BLUSH_TICKS);
        this.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
    }

    /** A bowl of mushroom stew, on the same clock as a pail of milk. Whether she had any. */
    public boolean fillBowl(ServerLevel level, Player player, ItemStack bowl) {
        if (level.getGameTime() < this.nextMilk) {
            player.displayClientMessage(Component.translatable("message.harmontide.no_milk", this.getDisplayName()), true);
            return false;
        }
        boolean friend = this.getAffection(player) >= BED_AFFECTION;
        this.setNextMilk(level.getGameTime() + (friend ? MILK_COOLDOWN / 2 : MILK_COOLDOWN));
        bowl.consume(1, player);
        ItemStack stew = new ItemStack(Items.MUSHROOM_STEW);
        if (!player.getInventory().add(stew)) player.drop(stew, false);
        this.addAffection(player, 1);
        return true;
    }

    /** The hippo's red sweat: wet, her friend near her doesn't burn. True while it holds. */
    public boolean redSweat() {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 8);
        if (friend == null || !this.isInWaterOrRain()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 60, 0, true, true));
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.HIPPO_MINOTAUR && !this.level().isClientSide && !this.isBaby() && this.tickCount % 40 == 0) this.redSweat();
        if (this.species == GirlSpecies.ORYX_MINOTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel dune && !this.isBaby() && this.tickCount % 20 == 0)
            this.desertStride(dune, dune.getBiome(this.blockPosition()).value().getBaseTemperature() >= 1.5F);
        if (this.species == GirlSpecies.BRASSHORN_MINOTAUR && this.level() instanceof net.minecraft.server.level.ServerLevel crag && !this.isBaby() && this.tickCount % 20 == 0) this.mountainFooting(crag);
        if (this.species == GirlSpecies.TUSKIN_MINOTAUR && this.level() instanceof ServerLevel snow && !this.isBaby() && this.tickCount % 20 == 0) this.snowplough(snow);
        if (this.species == GirlSpecies.SKJOLDRAM_MINOTAUR && this.level() instanceof ServerLevel ram && !this.isBaby() && this.tickCount % 10 == 0) this.ramCharge(ram);
        if (this.species == GirlSpecies.DUSTBISON_MINOTAUR && this.level() instanceof ServerLevel dust && !this.isBaby() && this.tickCount % 10 == 0) this.dustBath(dust);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.REEDBULL_MINOTAUR && !this.isBaby() && this.tickCount % 600 == 0) this.plantReeds(server);
        if (this.inLove > 0) this.inLove--;
        if (this.species == GirlSpecies.BRONZEJAW_MINOTAUR && this.tickCount % 4 == 0) this.ploughOnLead(server);
        if (this.species == GirlSpecies.MOOSKUH_MINOTAUR && this.tickCount % 400 == 0 && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) this.growMoss(server);
        if (this.stampede > 0) this.stampede--;
        if (this.nextMilk > 0 && server.getGameTime() >= this.nextMilk) this.ringBell(server);
        this.tickMilking(server);
        if (this.species == GirlSpecies.RHIWOOL_MINOTAUR || this.species == GirlSpecies.RHINO_MINOTAUR) this.tickCharge(server);
        if (this.tickCount % 40 == 0 && !this.isBaby()) {
            Player p = this.bondedPlayer(60, 16);
            if (p != null) {
                if (this.getTarget() != null || p.getLastHurtByMob() != null) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, true, false));
                this.plough(server);
            }
        }
    }

    // --- the rhiwool's charge --------------------------------------------------------------------------------------

    private int chargeTicks;
    private long nextCharge;
    @Nullable
    private Vec3 chargeDir;

    public boolean isCharging() {
        return this.chargeTicks > 0;
    }

    /** Head down and straight ahead at her target: whatever she meets on the way is sent flying. */
    public void startCharge(Vec3 towards) {
        Vec3 d = towards.subtract(this.position()).multiply(1, 0, 1);
        if (d.lengthSqr() < 1.0E-4) return;
        this.chargeDir = d.normalize();
        this.chargeTicks = 25;
        this.nextCharge = this.level().getGameTime() + 120;
        this.startTimedAction(Action.CHARGE, 25);
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.2F);
    }

    private void tickCharge(ServerLevel level) {
        LivingEntity t = this.getTarget();
        if (!this.isCharging() && t != null && level.getGameTime() >= this.nextCharge && this.distanceToSqr(t) > 4 * 4 && this.distanceToSqr(t) < 14 * 14) {
            this.startCharge(t.position());
        }
        if (!this.isCharging() || this.chargeDir == null) return;
        this.chargeTicks--;
        this.getNavigation().stop();
        this.setYRot((float) (Math.atan2(-this.chargeDir.x, this.chargeDir.z) * 180.0 / Math.PI));
        this.setDeltaMovement(this.chargeDir.x * 0.55, this.getDeltaMovement().y, this.chargeDir.z * 0.55);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.4), e -> e != this && e.isAlive()
                && !(e instanceof Player p && this.getAffection(p) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION))) {
            e.hurt(this.damageSources().mobAttack(this), 8.0F);
            e.knockback(2.5, -this.chargeDir.x, -this.chargeDir.z);
            e.setDeltaMovement(e.getDeltaMovement().add(0, 0.4, 0));
            this.chargeTicks = 0;
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 0.8F);
        }
        if (this.horizontalCollision) this.chargeTicks = 0;                            // into a wall: the charge ends
    }

    /** A devoted minotaur tills the grass and dirt she walks on into farmland, if there is farmland nearby already. */
    private void plough(ServerLevel level) {
        BlockPos under = this.blockPosition().below();
        var s = level.getBlockState(under);
        if (!(s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT)) || !level.getBlockState(under.above()).isAir()) return;
        for (BlockPos p : BlockPos.betweenClosed(under.offset(-4, -1, -4), under.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(Blocks.FARMLAND)) {
                level.setBlock(under, Blocks.FARMLAND.defaultBlockState(), 3);
                level.playSound(null, under, SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.0F);
                return;
            }
        }
    }

    /** A cow in love finds a bull in love (or the other way round) and they raise a calf. */
    class CourtGoal extends Goal {
        @Nullable
        private Minotaur partner;
        private int ticks;

        CourtGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Minotaur m = Minotaur.this;
            if (!m.isInLove() || m.isMale() || m.isBaby() || !m.getGestation().canConceive()) return false;
            this.partner = m.nearest(Minotaur.class, 8, o -> o.getType() == m.getType() && o.isMale() && o.isInLove() && !o.isBaby());
            return this.partner != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.partner != null && this.partner.isAlive() && this.ticks < 120;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void tick() {
            Minotaur m = Minotaur.this;
            this.ticks++;
            m.getLookControl().setLookAt(this.partner);
            this.partner.getLookControl().setLookAt(m);
            if (m.distanceToSqr(this.partner) > 2.5 * 2.5) {
                m.getNavigation().moveTo(this.partner, 1.0);
                return;
            }
            m.getGestation().conceive(this.partner.getUUID());
            m.inLove = 0;
            this.partner.inLove = 0;
            if (m.level() instanceof ServerLevel server) m.hearts(server, 6);
            this.ticks = 999;
        }
    }

    /** Bison run from whatever hurt one of their herd. */
    class StampedeGoal extends Goal {
        StampedeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Minotaur.this.isStampeding() && Minotaur.this.stampedeFrom != null;
        }

        @Override
        public void tick() {
            Minotaur m = Minotaur.this;
            if (m.tickCount % 10 != 0) return;
            Vec3 away = m.position().subtract(m.stampedeFrom).normalize().scale(12).add(m.position());
            m.getNavigation().moveTo(away.x, away.y, away.z, 1.7);
            m.setSprinting(true);
        }

        @Override
        public void stop() {
            Minotaur.this.setSprinting(false);
        }
    }

    /** By day the water buffalo goes and stands in water. */
    class WallowGoal extends Goal {
        @Nullable
        private BlockPos pool;

        WallowGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Minotaur m = Minotaur.this;
            if (!m.level().isDay() || m.isInWater() || m.getTarget() != null || m.tickCount % 41 != 0 || m.random.nextInt(3) != 0) return false;
            BlockPos o = m.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-12, -3, -12), o.offset(12, 3, 12))) {
                if (m.level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER) && m.level().getBlockState(p.above()).isAir()) {
                    this.pool = p.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.pool != null && !Minotaur.this.isInWater() && Minotaur.this.getTarget() == null && Minotaur.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Minotaur.this.getNavigation().moveTo(this.pool.getX() + 0.5, this.pool.getY(), this.pool.getZ() + 0.5, 1.0);
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.SHEEP_MINOTAUR ? SoundEvents.SHEEP_AMBIENT : SoundEvents.COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == GirlSpecies.SHEEP_MINOTAUR ? SoundEvents.SHEEP_HURT : SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.species == GirlSpecies.SHEEP_MINOTAUR ? SoundEvents.SHEEP_DEATH : SoundEvents.COW_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isMale() ? 0.75F : 1.0F);
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.COW_STEP, 0.2F, 1.0F);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.isBaby() && this.getFather() != null || this.hasFlag(FLAG_CHEST);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.hasFlag(FLAG_CHEST)) {
            this.spawnAtLocation(Items.CHEST);
            for (int i = 0; i < this.pack.getContainerSize(); i++) if (!this.pack.getItem(i).isEmpty()) this.spawnAtLocation(this.pack.getItem(i));
        }
        if (this.isWoolly() && !this.isShorn() && !this.isBaby()) this.spawnAtLocation(wool(this.woolColour(), 1));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextMilk", this.nextMilk);
        tag.putBoolean("MilkFull", this.milkFull);
        tag.putInt("InLove", this.inLove);
        if (this.post != null) tag.putLong("Post", this.post.asLong());
        if (this.getWoolDye() != null) tag.putString("WoolDye", this.getWoolDye().getName());
        if (this.hasFlag(FLAG_CHEST)) tag.put("Pack", this.pack.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextMilk = tag.getLong("NextMilk");
        this.milkFull = tag.getBoolean("MilkFull");
        this.inLove = tag.getInt("InLove");
        this.post = tag.contains("Post") ? BlockPos.of(tag.getLong("Post")) : null;
        this.setWoolDye(tag.contains("WoolDye") ? net.minecraft.world.item.DyeColor.byName(tag.getString("WoolDye"), null) : null);
        this.pack.fromTag(tag.getList("Pack", 10), this.registryAccess());
        this.refreshDimensions();
    }

    // --- the reedbull --------------------------------------------------------------------------------------------

    /** Befriended: sugar cane on a bare spot beside the water within 6 blocks of her friend. Where, or null. */
    @Nullable
    public BlockPos plantReeds(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) return null;
        var cane = net.minecraft.world.level.block.Blocks.SUGAR_CANE.defaultBlockState();
        java.util.List<BlockPos> spots = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(friend.blockPosition().offset(-6, -3, -6), friend.blockPosition().offset(6, 3, 6))) {
            if (level.isEmptyBlock(p) && !level.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.SUGAR_CANE) && cane.canSurvive(level, p)) spots.add(p.immutable());
        }
        if (spots.isEmpty()) return null;
        BlockPos at = spots.get(this.random.nextInt(spots.size()));
        level.setBlock(at, cane, 3);
        level.levelEvent(2005, at, 0);
        return at;
    }

    // --- the dustbison -------------------------------------------------------------------------------------------

    /** Her friend within 8 blocks, on fire, is rolled in the dust: the fire goes out, and five seconds of Fire Resistance. Whether she did. */
    public boolean dustBath(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 8);
        if (friend == null || !friend.isOnFire()) return false;
        friend.clearFire();
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 100, 0, false, true, true));
        level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,
                net.minecraft.world.level.block.Blocks.SAND.defaultBlockState()), friend.getX(), friend.getY() + 0.5, friend.getZ(), 30, 0.4, 0.4, 0.4, 0.1);
        return true;
    }

    // --- the skjoldram (eighteenth wave) ----------------------------------------------------------------------------

    /** Creepers within 6 blocks of her friend are butted away from them, their fuses stamped out. How many, or -1 without a friend. */
    public int ramCharge(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (var creeper : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class, friend.getBoundingBox().inflate(6))) {
            creeper.setSwellDir(-1);
            net.minecraft.world.phys.Vec3 away = creeper.position().subtract(friend.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0E-4) away = new net.minecraft.world.phys.Vec3(1, 0, 0);
            away = away.normalize();
            creeper.setDeltaMovement(away.x * 1.6, 0.45, away.z * 1.6);
            creeper.hurtMarked = true;
            creeper.hurt(level.damageSources().mobAttack(this), 2.0F);
            n++;
        }
        if (n > 0) this.playSound(net.minecraft.sounds.SoundEvents.GOAT_RAM_IMPACT, 1.2F, 0.9F);
        return n;
    }

    // --- the tuskin (nineteenth wave) -------------------------------------------------------------------------------

    /** The powder snow within 3 blocks of her friend packs into snow blocks, and the cold lets go of them. How many blocks she
     *  packed, or -1 without a friend. */
    public int snowplough(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        friend.setTicksFrozen(0);
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-3, -2, -3), friend.blockPosition().offset(3, 1, 3))) {
            if (!level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.POWDER_SNOW)) continue;
            level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.SNOW_BLOCK.defaultBlockState());
            n++;
        }
        if (n > 0) this.playSound(net.minecraft.sounds.SoundEvents.SNOW_PLACE, 1.0F, 0.8F);
        return n;
    }

    // --- the brasshorn ram (twentieth wave) -------------------------------------------------------------------------

    /** Her friend within 10 blocks jumps higher (Jump Boost I for three seconds). True if she steadied them. */
    public boolean mountainFooting(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 10);
        if (friend == null) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP, 60, 0, true, true));
        return true;
    }

    // --- the oryx (twenty-first wave) -------------------------------------------------------------------------------

    /** In the heat (deserts, badlands, savannas), her friend within 10 blocks doesn't tire: their exhaustion is wiped, so running
     *  and fighting cost no food. True if she spared them. */
    public boolean desertStride(net.minecraft.server.level.ServerLevel level, boolean hot) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 10);
        if (friend == null || !hot) return false;
        var food = friend.getFoodData();
        food.addExhaustion(-food.getExhaustionLevel());
        return true;
    }
}
