package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The elven family (elf_field_guide.html): night elf, high elf, wood elf, blood elf, desert elf. The one family with a
 * society: every elf trades with friends through the vanilla trading screen (fond, 10+), what she sells depends on her
 * people. Night elves walk only by night; high elves raise a pavilion where their group settles and drive strangers
 * off it; wood and desert elves live in villages and shoot what threatens them; blood elves are fireproof, set their
 * enemies alight and trade for gold. The dark elf of the deep dark makes no vibrations (sculk never hears her), trades
 * echo shards and sculk, and the Wardens are her kin: one near her lets her friends be.
 */
public class Elf extends AbstractMonsterGirl implements Merchant, RangedAttackMob {
    public static final int TRADE_AFFECTION = 10;
    public static final int SETTLEMENT_RADIUS = 8;
    private static final int BUILD_INTERVAL = 60;

    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;
    @Nullable
    private BlockPos settlement;
    private boolean leader;
    private int built;
    private long warnedAt = -1000;

    public Elf(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (this.isArcher()) {
            this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 30, 15.0F) {
                @Override
                public void start() {
                    super.start();
                    Elf.this.startTimedAction(Action.SHOOT, 20);
                }
            });
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(species == GirlSpecies.DESERT_ELF ? Items.CROSSBOW : Items.BOW));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        }
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.HONEY_BOTTLE, Items.GOLDEN_APPLE), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::leaderOrHome, 1.0, 10.0F, 5.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !Elf.this.isResting() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (this.isArcher() || species == GirlSpecies.BLOOD_ELF) {
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> !(m instanceof net.minecraft.world.entity.monster.Creeper) && m.distanceToSqr(this) < 16 * 16));
        }
        if (species == GirlSpecies.HIGH_ELF) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, p -> this.isTrespasser((Player) p)));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case HIGH_ELF -> girlAttributes(26, 0.28, 5);
            case BLOOD_ELF -> girlAttributes(28, 0.3, 6);
            case WOOD_ELF, DESERT_ELF -> girlAttributes(24, 0.3, 3);
            case DARK_ELF -> girlAttributes(26, 0.3, 5);
            case BROWNIE_ELF -> girlAttributes(8, 0.3, 1);
            case VALKYRIE_ELF -> girlAttributes(30, 0.3, 6).add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 6);
            default -> girlAttributes(22, 0.3, 4);
        };
    }

    private boolean isArcher() {
        return this.species == GirlSpecies.WOOD_ELF || this.species == GirlSpecies.DESERT_ELF;
    }

    /** Night elves rest in the shade by day. */
    public boolean isResting() {
        return this.species == GirlSpecies.NIGHT_ELF && this.level().isDay() && this.getTarget() == null;
    }

    // --- group and settlement ------------------------------------------------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (!(group instanceof GroupData g)) {
            this.leader = true;
            group = new GroupData(this.blockPosition());
        } else if (this.species == GirlSpecies.HIGH_ELF) {
            this.settlement = g.origin();
        }
        if (this.leader && this.species == GirlSpecies.HIGH_ELF) this.settlement = this.blockPosition();
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    private record GroupData(BlockPos origin) implements SpawnGroupData {
    }

    @Nullable
    private LivingEntity leaderOrHome() {
        if (this.isBaby()) return this.mother();
        if (this.leader || this.isResting()) return null;
        return this.nearest(Elf.class, 24, e -> e.getType() == this.getType() && e.leader && !e.isBaby());
    }

    public void setSettlement(@Nullable BlockPos pos) {
        this.settlement = pos;
        this.leader = pos != null;
    }

    @Nullable
    public BlockPos getSettlement() {
        return this.settlement;
    }

    public int getBuilt() {
        return this.built;
    }

    /** The elven pavilion: a quartz floor, four birch pillars, a slab roof, a lantern. Positions relative to the origin. */
    public static List<Placement> pavilion() {
        List<Placement> out = new ArrayList<>();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) out.add(new Placement(x, -1, z, Blocks.QUARTZ_BRICKS.defaultBlockState()));
        BlockState log = Blocks.STRIPPED_BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);
        for (int y = 0; y < 3; y++) for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) out.add(new Placement(c[0], y, c[1], log));
        BlockState slab = Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            if (Math.abs(x) <= 1 && Math.abs(z) <= 1) continue;
            out.add(new Placement(x, 3, z, slab));
        }
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) out.add(new Placement(x, 3, z, Blocks.BIRCH_PLANKS.defaultBlockState()));
        out.add(new Placement(0, 4, 0, Blocks.BIRCH_SLAB.defaultBlockState()));
        out.add(new Placement(0, 2, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true)));
        for (int[] c : new int[][]{{-1, -2}, {1, -2}, {-1, 2}, {1, 2}}) out.add(new Placement(c[0], 0, c[1], Blocks.POTTED_ALLIUM.defaultBlockState()));
        return out;
    }

    public record Placement(int x, int y, int z, BlockState state) {
    }

    /** The group's builder places the next block of the pavilion now and then. */
    private void build(ServerLevel level) {
        List<Placement> plan = pavilion();
        if (this.settlement == null || this.built >= plan.size()) return;
        Placement p = plan.get(this.built);
        BlockPos at = this.settlement.offset(p.x(), p.y(), p.z());
        if (this.distanceToSqr(Vec3.atCenterOf(at)) > 6 * 6) {
            this.getNavigation().moveTo(at.getX() + 0.5, this.settlement.getY(), at.getZ() + 0.5, 0.9);
            return;
        }
        BlockState cur = level.getBlockState(at);
        if (p.y() < 0 || cur.canBeReplaced()) {
            level.setBlock(at, p.state(), 3);
            level.playSound(null, at, p.state().getSoundType().getPlaceSound(), net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.0F);
            this.swing(InteractionHand.MAIN_HAND);
        }
        this.built++;
    }

    /** Within the settlement, strangers are warned and then driven off. */
    private boolean isTrespasser(Player p) {
        if (this.settlement == null || p.isCreative() || p.isSpectator() || this.getAffection(p) >= TRADE_AFFECTION || this.isBaby()) return false;
        if (p.distanceToSqr(Vec3.atCenterOf(this.settlement)) > SETTLEMENT_RADIUS * SETTLEMENT_RADIUS) return false;
        long now = this.level().getGameTime();
        if (now - this.warnedAt > 400) {
            this.warnedAt = now;
            p.displayClientMessage(Component.translatable("message.monstergirls.elf_warning", this.getDisplayName()), true);
            return false;
        }
        return now - this.warnedAt > 80;
    }

    // --- trading -------------------------------------------------------------------------------------------------

    private static MerchantOffer offer(net.minecraft.world.level.ItemLike cost, int count, ItemStack result, int uses) {
        return new MerchantOffer(new ItemCost(cost, count), result, uses, 2, 0.05F);
    }

    /** What her people sell (the blood elves want gold, everyone else emeralds). */
    public MerchantOffers makeOffers() {
        MerchantOffers o = new MerchantOffers();
        var E = Items.EMERALD;
        switch (this.species) {
            case NIGHT_ELF -> {
                o.add(offer(E, 2, new ItemStack(Items.GLOWSTONE_DUST, 6), 12));
                o.add(offer(E, 6, new ItemStack(Items.ENDER_PEARL), 6));
                o.add(offer(E, 3, new ItemStack(Items.AMETHYST_SHARD, 4), 12));
                o.add(offer(E, 4, new ItemStack(Items.PHANTOM_MEMBRANE, 2), 8));
                o.add(offer(Items.GLOW_BERRIES, 12, new ItemStack(E), 16));
            }
            case HIGH_ELF -> {
                o.add(offer(E, 8, new ItemStack(Items.BOOK, 3), 8));
                o.add(offer(E, 12, new ItemStack(Items.EXPERIENCE_BOTTLE, 4), 6));
                o.add(offer(E, 16, new ItemStack(Items.GOLDEN_APPLE), 4));
                o.add(offer(E, 4, new ItemStack(Items.QUARTZ_BLOCK, 8), 12));
                o.add(offer(Items.HONEY_BOTTLE, 3, new ItemStack(E), 16));
            }
            case WOOD_ELF -> {
                o.add(offer(E, 1, new ItemStack(Items.ARROW, 16), 16));
                o.add(offer(E, 2, new ItemStack(Items.BOW), 8));
                o.add(offer(E, 1, new ItemStack(Items.OAK_SAPLING, 4), 16));
                o.add(offer(E, 2, new ItemStack(Items.HONEY_BOTTLE, 2), 12));
                o.add(offer(Items.SWEET_BERRIES, 16, new ItemStack(E), 16));
            }
            case DARK_ELF -> {
                o.add(offer(E, 6, new ItemStack(Items.ECHO_SHARD), 6));
                o.add(offer(E, 2, new ItemStack(Items.SCULK, 8), 16));
                o.add(offer(E, 4, new ItemStack(Items.SCULK_SENSOR), 8));
                o.add(offer(E, 10, new ItemStack(Items.SCULK_CATALYST), 4));
                o.add(offer(Items.CANDLE, 6, new ItemStack(E), 16));
            }
            case BLOOD_ELF -> {
                var G = Items.GOLD_INGOT;
                o.add(offer(G, 2, new ItemStack(Items.QUARTZ, 12), 16));
                o.add(offer(G, 4, new ItemStack(Items.BLAZE_ROD, 2), 8));
                o.add(offer(G, 3, new ItemStack(Items.MAGMA_CREAM, 4), 12));
                o.add(offer(G, 6, new ItemStack(Items.FIRE_CHARGE, 4), 8));
                o.add(offer(G, 8, new ItemStack(Items.NETHERITE_SCRAP), 2));
            }
            default -> {
                o.add(offer(E, 1, new ItemStack(Items.LEATHER, 4), 16));
                o.add(offer(E, 3, new ItemStack(Items.CROSSBOW), 8));
                o.add(offer(E, 1, new ItemStack(Items.ARROW, 12), 16));
                o.add(offer(E, 2, new ItemStack(Items.CACTUS, 8), 12));
                o.add(offer(Items.LEATHER, 6, new ItemStack(E), 16));
            }
        }
        return o;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) this.offers = this.makeOffers();
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (this.tradingPlayer != null) this.addAffection(this.tradingPlayer, 1);
        this.startTimedAction(Action.TRADE, 30);
        this.playSound(SoundEvents.VILLAGER_YES, 0.8F, 1.4F);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    /** An empty hand opens her trades once she is fond of you (crouch to pat her instead). */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.isEmpty() || this.isBaby() || this.species == GirlSpecies.BROWNIE_ELF) return null;   // the brownie does not trade
        if (player.isShiftKeyDown()) return this.pet(player);
        if (this.getAffection(player) < TRADE_AFFECTION || this.getTradingPlayer() != null || this.getTarget() == player) return null;
        if (!this.level().isClientSide) {
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.BLOOD_ELF) return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.APPLE);
        if (this.species == GirlSpecies.BROWNIE_ELF) return stack.is(Items.MILK_BUCKET) || stack.is(Items.BREAD);
        return stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.HONEY_BOTTLE);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.MILK_BUCKET) && !player.getInventory().add(new ItemStack(Items.BUCKET))) player.drop(new ItemStack(Items.BUCKET), false);
        return stack.is(Items.HONEY_BOTTLE) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.MILK_BUCKET) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        if (this.species == GirlSpecies.BROWNIE_ELF) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 35 ? Items.GOLD_NUGGET : r < 65 ? Items.SWEET_BERRIES : r < 90 ? Items.HONEYCOMB : Items.EMERALD, r < 90 ? 2 : 1);
        }
        MerchantOffers o = this.makeOffers();
        return o.get(this.random.nextInt(o.size())).getResult().copy();
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack weapon = this.getMainHandItem();
        Arrow arrow = new Arrow(this.level(), this, new ItemStack(Items.ARROW), weapon.isEmpty() ? null : weapon);
        arrow.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
        double dx = target.getX() - this.getX(), dz = target.getZ() - this.getZ();
        double dy = target.getY(0.33) - arrow.getY();
        double d = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + d * 0.2, dz, 1.6F, 6.0F);
        this.playSound(this.species == GirlSpecies.DESERT_ELF ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.SKELETON_SHOOT, 1.0F, 1.2F);
        this.level().addFreshEntity(arrow);
        this.startTimedAction(Action.SHOOT, 20);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.BLOOD_ELF) target.igniteForSeconds(4);
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.MAD_HATTER_ELF && this.level() instanceof net.minecraft.server.level.ServerLevel party && !this.isBaby() && this.tickCount % 100 == 0) this.teaTime(party);
        if (this.species == GirlSpecies.ANGEL_ELF && this.level() instanceof net.minecraft.server.level.ServerLevel grace && !this.isBaby() && this.tickCount % 40 == 0) this.mercy(grace);
        if (this.species == GirlSpecies.DOMOVOI_ELF && this.level() instanceof ServerLevel house && !this.isBaby()) this.keepHearth(house, this.tickCount % 40 == 0);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.GANDHARVA_ELF && !this.isBaby()) this.tickLute(server);
        if (this.species == GirlSpecies.KNOCKER_ELF && !this.isBaby() && this.tickCount % 200 == 0) this.knock(server);
        if (this.species == GirlSpecies.KAGUYA_ELF && !this.isBaby() && this.tickCount % 40 == 0) this.moonsWeight();
        if (this.species == GirlSpecies.OPHANIM_ELF && !this.isBaby() && this.tickCount % 60 == 0) this.allSeeing(server);
        if (this.species == GirlSpecies.NORN_ELF && !this.isBaby() && this.tickCount % 20 == 0) this.foretell(server);
        if (this.species == GirlSpecies.APSARA_ELF && !this.isBaby() && this.tickCount % 20 == 0) this.dance(server);
        if (this.species == GirlSpecies.VALKYRIE_ELF && this.tickCount % 20 == 0) this.returnFallen(server);
        if (this.tradingPlayer != null && (!this.tradingPlayer.isAlive() || this.tradingPlayer.distanceToSqr(this) > 16 * 16)) this.tradingPlayer = null;
        if (this.species == GirlSpecies.NIGHT_ELF && this.tickCount % 20 == 0) {
            if (this.isResting() && this.getAction() == Action.NONE) {
                this.getNavigation().stop();
                this.setAction(Action.SITTING);
            } else if (!this.isResting() && this.getAction() == Action.SITTING && !this.isOrderedToSit()) {
                this.setAction(Action.NONE);
            }
        }
        if (this.species == GirlSpecies.HIGH_ELF && this.leader && this.settlement != null && this.getTarget() == null && this.tickCount % BUILD_INTERVAL == 0) {
            this.build(server);
        }
        if (this.species == GirlSpecies.DARK_ELF && this.tickCount % 20 == 0) this.calmWardens(server);
        if (this.species == GirlSpecies.BROWNIE_ELF && this.tickCount % 40 == 0 && !this.isBaby()) {
            Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 48);
            if (friend != null && (!server.isDay() || friend.isSleeping())) {   // sleeping skips the night: she works while her friend sleeps
                if (friend.isSleeping() && this.distanceToSqr(friend) > 4 * 4) this.getNavigation().moveTo(friend, 1.1);
                this.tidy(server, friend);
            }
        }
    }

    /**
     * The brownie's night's work: the chests within 8 blocks of her and of her friend (by their bed, most nights) get
     * their stacks merged, and loose items lying around go into them. Returns how many stacks she merged or put away.
     */
    public int tidy(ServerLevel level, @Nullable Player friend) {
        java.util.Set<net.minecraft.world.Container> found = new java.util.LinkedHashSet<>();
        for (BlockPos o : friend == null ? java.util.List.of(this.blockPosition()) : java.util.List.of(this.blockPosition(), friend.blockPosition())) {
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, -3, -8), o.offset(8, 3, 8))) {
                if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) found.add(chest);
            }
        }
        java.util.List<net.minecraft.world.Container> chests = new java.util.ArrayList<>(found);
        int n = 0;
        for (net.minecraft.world.Container c : chests) {                 // merge part stacks of the same thing
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack a = c.getItem(i);
                if (a.isEmpty() || a.getCount() >= a.getMaxStackSize()) continue;
                for (int j = i + 1; j < c.getContainerSize() && a.getCount() < a.getMaxStackSize(); j++) {
                    ItemStack b = c.getItem(j);
                    if (b.isEmpty() || !ItemStack.isSameItemSameComponents(a, b)) continue;
                    int move = Math.min(b.getCount(), a.getMaxStackSize() - a.getCount());
                    a.grow(move);
                    b.shrink(move);
                    if (b.isEmpty()) c.setItem(j, ItemStack.EMPTY);
                    n++;
                }
            }
            c.setChanged();
        }
        if (!chests.isEmpty()) {                                        // and put the loose things away
            net.minecraft.world.phys.AABB area = friend == null ? this.getBoundingBox().inflate(8) : this.getBoundingBox().inflate(8).minmax(friend.getBoundingBox().inflate(8));
            for (net.minecraft.world.entity.item.ItemEntity item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    area, e -> e.isAlive() && !e.hasPickUpDelay())) {
                for (net.minecraft.world.Container c : chests) {
                    if (net.minecraft.world.level.block.entity.HopperBlockEntity.addItem(c, item)) {
                        n++;
                        break;
                    }
                }
            }
        }
        if (n > 0) this.playSound(SoundEvents.BUNDLE_INSERT, 0.6F, 1.4F);
        return n;
    }

    /** The dark elf walks without a sound: no sculk sensor or shrieker ever hears her. */
    @Override
    public boolean dampensVibrations() {
        return this.species == GirlSpecies.DARK_ELF || super.dampensVibrations();
    }

    /** Every Warden within 16 blocks of a dark elf forgets its anger at her friends (Kinship keeps it from targeting them). */
    public void calmWardens(ServerLevel level) {
        for (var warden : level.getEntitiesOfClass(net.minecraft.world.entity.monster.warden.Warden.class, this.getBoundingBox().inflate(16))) {
            for (Player p : level.getEntitiesOfClass(Player.class, warden.getBoundingBox().inflate(32), p -> this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
                warden.clearAnger(p);
                if (warden.getTarget() == p) warden.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
            }
        }
    }

    public static boolean spawnRule(GirlSpecies s, ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        if (s == GirlSpecies.WOOD_ELF || s == GirlSpecies.DESERT_ELF) {   // only in and around villages
            return level.getLevel().structureManager().getStructureWithPieceAt(pos, StructureTags.VILLAGE).isValid()
                    || level.getLevel().structureManager().getStructureWithPieceAt(pos.offset(24, 0, 0), StructureTags.VILLAGE).isValid()
                    || level.getLevel().structureManager().getStructureWithPieceAt(pos.offset(-24, 0, 0), StructureTags.VILLAGE).isValid();
        }
        if (s == GirlSpecies.NIGHT_ELF) return level.getLevel().isNight();
        if (s == GirlSpecies.HIGH_ELF) {                                       // a clearing: flat ground under open sky
            return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) <= pos.getY() + 1;
        }
        return true;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
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
    public SoundEvent getChirpSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.settlement != null && this.built > 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.fallenOwner != null && !this.fallen.isEmpty()) {
            net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
            for (ItemStack st : this.fallen) items.add(st.save(this.registryAccess()));
            tag.put("Fallen", items);
            tag.putUUID("FallenOwner", this.fallenOwner);
        }
        tag.putBoolean("Leader", this.leader);
        if (this.settlement != null) tag.putLong("Settlement", this.settlement.asLong());
        tag.putInt("Built", this.built);
        if (this.offers != null) tag.put("Offers", MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), this.offers).getOrThrow());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fallen.clear();
        this.fallenOwner = tag.hasUUID("FallenOwner") ? tag.getUUID("FallenOwner") : null;
        for (net.minecraft.nbt.Tag t : tag.getList("Fallen", net.minecraft.nbt.Tag.TAG_COMPOUND)) ItemStack.parse(this.registryAccess(), t).ifPresent(this.fallen::add);
        this.leader = tag.getBoolean("Leader");
        this.settlement = tag.contains("Settlement") ? BlockPos.of(tag.getLong("Settlement")) : null;
        this.built = tag.getInt("Built");
        if (tag.contains("Offers")) {
            MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get("Offers"))
                    .resultOrPartial(e -> {}).ifPresent(o -> this.offers = o);
        }
    }

    // --- the thirtieth wave ---------------------------------------------------------------------------------------

    /**
     * She raps on the rock: 2 with loose gravel or sand over her friend's head (a warning), 1 with ore within 8 (and tells them
     * which way), 0 with nothing to say, -1 without a friend.
     */
    public int knock(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        BlockPos at = friend.blockPosition();
        for (int y = 2; y <= 6; y++) {
            if (level.getBlockState(at.above(y)).getBlock() instanceof net.minecraft.world.level.block.FallingBlock && level.getBlockState(at.above(y - 1)).isAir()) {
                this.rap(3);
                friend.displayClientMessage(Component.translatable("message.monstergirls.knocker.loose", this.getDisplayName()), true);
                return 2;
            }
        }
        for (BlockPos p : BlockPos.withinManhattan(at, 8, 8, 8)) {
            if (level.getBlockState(p).is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
                this.rap(1);
                String way = net.minecraft.core.Direction.getNearest((float) (p.getX() - at.getX()), (float) (p.getY() - at.getY()), (float) (p.getZ() - at.getZ())).getName();
                friend.displayClientMessage(Component.translatable("message.monstergirls.knocker.ore", this.getDisplayName(), way), true);
                return 1;
            }
        }
        return 0;
    }

    private void rap(int times) {
        for (int i = 0; i < times; i++) this.playSound(SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1.5F, 0.8F + 0.1F * i);
    }

    /** Her friend near her falls softly and jumps high. Whether she has one near. */
    public boolean moonsWeight() {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 12);
        if (friend == null) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP, 60, 2));
        if (friend.fallDistance > 2.0F) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 60));
        return true;
    }

    /** Every monster within 32 of her friend shows through the walls. How many, -1 without a friend. */
    public int allSeeing(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int seen = 0;
        for (Monster m : level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(32), Monster::isAlive)) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 100));
            seen++;
        }
        return seen;
    }

    /** A monster within 16 has her friend as its target: she says so, and steadies them. Whether one did. */
    public boolean foretell(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null || level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(16), m -> m.isAlive() && m.getTarget() == friend).isEmpty()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40));
        friend.displayClientMessage(Component.translatable("message.monstergirls.norn.warning", this.getDisplayName()), true);
        return true;
    }

    // --- the Gandharva and the Apsara -------------------------------------------------------------------------------

    /** His tune, in the notes of a note block (0-24): written for him, round and round a five-note scale. */
    private static final int[] TUNE = {12, 14, 16, 19, 21, 19, 16, 14, 12, 9, 12, 14, 16, 14, 12, 7};
    private static final int NOTE_TICKS = 8;
    private static final int TUNE_TICKS = TUNE.length * NOTE_TICKS * 2;
    private int lutePlaying;
    private int tunePos;
    private long nextTune;
    private long lastHeardNote = -1000;

    public boolean isPlaying() {
        return this.lutePlaying > 0;
    }

    /** He takes the lute in hand and plays the tune through twice. */
    public void playLute() {
        this.lutePlaying = TUNE_TICKS;
        this.tunePos = 0;
        this.startTimedAction(Action.PLAY, TUNE_TICKS);
    }

    /** One plucked note; the same pitches a note block makes. */
    private void pluck(ServerLevel level, int note) {
        float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
        this.playSound(SoundEvents.NOTE_BLOCK_GUITAR.value(), 1.2F, pitch);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE, this.getX(), this.getEyeY() + 0.4, this.getZ(), 0, note / 24.0, 0, 0, 1.0);
    }

    /** A note block rang near. The Gandharva answers it a fifth up; either of them takes it for music. */
    public void hearNote(ServerLevel level, int note) {
        this.lastHeardNote = level.getGameTime();
        if (this.species == GirlSpecies.GANDHARVA_ELF) {
            this.pluck(level, Math.min(24, note + 7));
            if (!this.isPlaying()) this.playLute();
        }
    }

    /** A jukebox within 16 is playing, or a note block rang in the last few seconds. */
    public boolean hearsMusic(ServerLevel level) {
        return level.getGameTime() - this.lastHeardNote < 100 || com.linkyoshi90.monstergirls.entity.JukeboxSensing.findPlaying(level, this.blockPosition(), 16) != null;
    }

    private void tickLute(ServerLevel level) {
        if (this.isPlaying()) {
            if (this.lutePlaying-- % NOTE_TICKS == 0) this.pluck(level, TUNE[this.tunePos++ % TUNE.length]);
            if (this.tickCount % 20 == 0 && this.hearsMusic(level)) {
                Player friend = this.bondedPlayer(10, 12);
                if (friend != null) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60));
            }
        } else if (this.tickCount % 20 == 0 && (this.hearsMusic(level) || level.getGameTime() >= this.nextTune)) {
            this.nextTune = level.getGameTime() + 1200 + this.random.nextInt(1200);
            this.playLute();
        }
    }

    /** She dances to any music. 2 with a Gandharva playing, 1 to other music, 0 with none; her friend gains from it. */
    public int dance(ServerLevel level) {
        boolean lute = !level.getEntitiesOfClass(Elf.class, this.getBoundingBox().inflate(16), e -> e.species == GirlSpecies.GANDHARVA_ELF && e.isPlaying()).isEmpty();
        if (!lute && !this.hearsMusic(level)) return 0;
        this.startTimedAction(Action.DANCE, 40);
        Player friend = this.bondedPlayer(10, 12);
        if (friend != null) {
            friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60));
            if (lute) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60));
        }
        return lute ? 2 : 1;
    }

    @Override
    public boolean canShareBed() {
        return this.species != GirlSpecies.GANDHARVA_ELF && super.canShareBed();
    }

    // --- the valkyrie --------------------------------------------------------------------------------------------

    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.VALKYRIE_ELF || this.species == GirlSpecies.ANGEL_ELF || this.species == GirlSpecies.GANDHARVA_ELF || this.species == GirlSpecies.OPHANIM_ELF || super.canFly();
    }

    /** What she gathered up where her friend fell, and whose it is. */
    private final List<ItemStack> fallen = new ArrayList<>();
    @Nullable
    private java.util.UUID fallenOwner;

    /** Her friend died within 32 blocks of her: she takes up everything they dropped (the item entities are discarded and `drops` emptied). Whether she did. */
    public boolean gatherFallen(net.minecraft.server.level.ServerPlayer player, java.util.Collection<net.minecraft.world.entity.item.ItemEntity> drops) {
        if (this.species != GirlSpecies.VALKYRIE_ELF || this.isBaby() || drops.isEmpty() || this.getAffection(player) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION
                || player.distanceToSqr(this) > 32 * 32 || this.fallenOwner != null && !this.fallenOwner.equals(player.getUUID())) return false;
        for (var e : drops) {
            this.fallen.add(e.getItem().copy());
            e.discard();
        }
        drops.clear();
        this.fallenOwner = player.getUUID();
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.8, 0.5, 0.05);
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.valkyrie.gathered", this.getDisplayName()));
        return true;
    }

    /** Her friend back within 6 blocks of her, alive: everything she kept goes back to them. How many stacks. */
    public int returnFallen(ServerLevel level) {
        if (this.fallenOwner == null) return 0;
        Player p = level.getPlayerByUUID(this.fallenOwner);
        if (p == null || !p.isAlive() || p.distanceToSqr(this) > 6 * 6) return 0;
        int n = this.fallen.size();
        for (ItemStack st : this.fallen) if (!p.getInventory().add(st)) p.drop(st, false);
        this.fallen.clear();
        this.fallenOwner = null;
        p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.valkyrie.returned", this.getDisplayName()), true);
        this.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 1.0F, 0.8F);
        return n;
    }

    public int fallenCount() {
        return this.fallen.size();
    }

    /** She never wanders off with someone's things. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.fallen.isEmpty() && super.removeWhenFarAway(distance);
    }

    /** If she falls herself, what she kept falls with her. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack st : this.fallen) this.spawnAtLocation(st);
        this.fallen.clear();
    }

    // --- the domovoi (eighteenth wave) ------------------------------------------------------------------------------

    /** The furnaces (and smokers and blast furnaces) within 6 blocks of her friend, found every two seconds. */
    private final java.util.List<net.minecraft.core.BlockPos> hearths = new java.util.ArrayList<>();

    /** Each tick the furnaces near her friend get a second tick: they smelt (and burn) twice as fast. `look`: find them afresh
     *  first. How many she tended. */
    public int keepHearth(ServerLevel level, boolean look) {
        if (look) {
            this.hearths.clear();
            net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
            if (friend != null) {
                for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -3, -6), friend.blockPosition().offset(6, 3, 6))) {
                    if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity) this.hearths.add(p.immutable());
                }
            }
        }
        int n = 0;
        for (net.minecraft.core.BlockPos p : this.hearths) {
            if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace) {
                net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(level, p, level.getBlockState(p), furnace);
                n++;
            }
        }
        return n;
    }

    /** The last day the Hatter poured. */
    private long lastTea = -1;

    /** The Mad Hatter's tea (twenty-fifth wave): once a day, a good effect for her friend, chosen at random, for two minutes. 1 when she poured, 0 when she already has, -1 without a friend. */
    public int teaTime(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        long day = level.getDayTime() / 24000L;
        if (day == this.lastTea) return 0;
        this.lastTea = day;
        var cups = java.util.List.of(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, net.minecraft.world.effect.MobEffects.DIG_SPEED, net.minecraft.world.effect.MobEffects.JUMP, net.minecraft.world.effect.MobEffects.REGENERATION, net.minecraft.world.effect.MobEffects.NIGHT_VISION, net.minecraft.world.effect.MobEffects.WATER_BREATHING, net.minecraft.world.effect.MobEffects.LUCK, net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE);
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(cups.get(this.random.nextInt(cups.size())), 2400, 0));
        this.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY, 1.0F, 1.2F);
        return 1;
    }

    /** The Angel's mercy (twenty-fifth wave): her friend under a third of their health gets Regeneration II for 5 seconds. 1 when she healed, 0 when they are well, -1 without a friend. */
    public int mercy(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (friend.getHealth() >= friend.getMaxHealth() / 3.0F) return 0;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, friend.getX(), friend.getY() + 1, friend.getZ(), 8, 0.4, 0.5, 0.4, 0.02);
        return 1;
    }
}
