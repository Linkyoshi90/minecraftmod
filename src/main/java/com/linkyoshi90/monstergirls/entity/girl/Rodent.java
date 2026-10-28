package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The rodents (rodent_field_guide.html): capybara and Ratatoskr. The capybara is calm itself: no wolf, fox, cat or
 * ocelot hunts near her, idle girls sit down beside her, and she soaks in water. Ratatoskr gathers seeds and saplings
 * (and plants the ones she forgets), and carries what a friend hands her to the nearest other player she is fond of,
 * swapping it for something silly one time in eight.
 */
public class Rodent extends AbstractMonsterGirl {
    public static final double CALM_RADIUS = 8.0;
    public static final int PRANK_ODDS = 8;
    private static final List<net.minecraft.world.item.Item> PRANKS = List.of(Items.STICK, Items.WHEAT_SEEDS, Items.DEAD_BUSH, Items.POISONOUS_POTATO);

    /** Ratatoskr's cache of seeds and saplings. */
    private final SimpleContainer cache = new SimpleContainer(9);
    /** The parcel she is carrying, who sent it and who it is for. */
    private ItemStack parcel = ItemStack.EMPTY;
    @Nullable
    private UUID sender, recipient;

    public Rodent(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        if (species == GirlSpecies.CAPYBARA_RODENT) this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, species == GirlSpecies.RATATOSKR_RODENT ? 1.0 : 0.7));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (species == GirlSpecies.SQUIRRICERAT_RODENT || species == GirlSpecies.KAMAITACHI_RODENT) {   // they fight the monsters near their friend
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
            this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this,
                    net.minecraft.world.entity.monster.Monster.class, 10, true, false,
                    m -> !(m instanceof net.minecraft.world.entity.monster.Creeper) && this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16) != null));
        }
        if (species == GirlSpecies.NEEDLEHOG_RODENT) {                    // she hunts the silverfish in your walls
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
            this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this,
                    net.minecraft.world.entity.monster.Silverfish.class, true));
            this.setCanPickUpLoot(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.NEEDLEHOG_RODENT) return girlAttributes(16, 0.28, 2);
        if (s == GirlSpecies.COATIMANO_RODENT) return girlAttributes(14, 0.3, 1);
        if (s == GirlSpecies.MARMOTAUR_RODENT) return girlAttributes(28, 0.26, 5).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, 1.2);
        if (s == GirlSpecies.BAMBADGER_RODENT) return girlAttributes(16, 0.28, 2);
        if (s == GirlSpecies.SQUIRRICERAT_RODENT) return girlAttributes(24, 0.3, 5).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, 1.5);
        if (s == GirlSpecies.SILKCOON_RODENT) return girlAttributes(14, 0.3, 1);
        if (s == GirlSpecies.ROCKHYRAX_RODENT) return girlAttributes(14, 0.3, 1);
        if (s == GirlSpecies.KAMAITACHI_RODENT) return girlAttributes(18, 0.38, 4).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_KNOCKBACK, 1.2);
        return s == GirlSpecies.CAPYBARA_RODENT ? girlAttributes(26, 0.23, 2) : girlAttributes(14, 0.34, 1);
    }

    // --- the rockhyrax ------------------------------------------------------------------------------------------------

    /** The sentinel: a creeper within 12 blocks of her friend makes her whistle, and it glows. The creeper she spotted, or null. */
    @Nullable
    public net.minecraft.world.entity.monster.Creeper sentinel(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null || this.isBaby()) return null;
        var creepers = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class, friend.getBoundingBox().inflate(12),
                c -> c.isAlive() && !c.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
        if (creepers.isEmpty()) return null;
        for (var c : creepers) c.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200, 0, false, false));
        this.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_FLUTE.value(), 1.2F, 2.0F);
        this.getLookControl().setLookAt(creepers.get(0));
        return creepers.get(0);
    }

    // --- the silkcoon -------------------------------------------------------------------------------------------------

    /** Water within 3 blocks of her: where she can wash. */
    public boolean nearWater() {
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-3, -2, -3), this.blockPosition().offset(3, 1, 3))) {
            if (this.level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER)) return true;
        }
        return false;
    }

    /** The stack with its dye washed out (the same count), or null if there is no dye to wash out. */
    @Nullable
    public static ItemStack washed(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR)) {
            ItemStack c = stack.copy();
            c.remove(net.minecraft.core.component.DataComponents.DYED_COLOR);
            return c;
        }
        net.minecraft.world.item.Item plain = null;
        if (stack.is(net.minecraft.tags.ItemTags.WOOL) && !stack.is(Items.WHITE_WOOL)) plain = Items.WHITE_WOOL;
        else if (stack.is(net.minecraft.tags.ItemTags.WOOL_CARPETS) && !stack.is(Items.WHITE_CARPET)) plain = Items.WHITE_CARPET;
        else if (stack.is(net.minecraft.tags.ItemTags.BEDS) && !stack.is(Items.WHITE_BED)) plain = Items.WHITE_BED;
        else if (stack.is(net.minecraft.tags.ItemTags.TERRACOTTA) && !stack.is(Items.TERRACOTTA)) plain = Items.TERRACOTTA;
        else if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.StainedGlassBlock) plain = Items.GLASS;
        else if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof net.minecraft.world.level.block.StainedGlassPaneBlock) plain = Items.GLASS_PANE;
        return plain == null ? null : new ItemStack(plain, stack.getCount());
    }

    // --- the squirricerat and the kamaitachi ---------------------------------------------------------------------------

    /** A sapling buried in open grass within 6 blocks: one day it is a tree. True if she planted one. */
    public boolean buryAcorn(ServerLevel level) {
        var saplings = java.util.List.of(net.minecraft.world.level.block.Blocks.OAK_SAPLING, net.minecraft.world.level.block.Blocks.BIRCH_SAPLING,
                net.minecraft.world.level.block.Blocks.DARK_OAK_SAPLING);
        for (int i = 0; i < 12; i++) {
            BlockPos p = this.blockPosition().offset(this.random.nextInt(13) - 6, this.random.nextInt(5) - 2, this.random.nextInt(13) - 6);
            if (!level.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || !level.getBlockState(p).isAir() || !level.canSeeSky(p)) continue;
            level.setBlock(p, saplings.get(this.random.nextInt(saplings.size())).defaultBlockState(), 3);
            level.levelEvent(2001, p.below(), net.minecraft.world.level.block.Block.getId(level.getBlockState(p.below())));
            return true;
        }
        return false;
    }

    /** Her gusts cut the tall grass, natural leaves and cobwebs within 3 blocks of her friend (a few each time). Returns how many. */
    public int gust(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return 0;
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(friend.blockPosition().offset(-3, -1, -3), friend.blockPosition().offset(3, 3, 3))) {
            var s = level.getBlockState(p);
            boolean cut = s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS) || s.is(net.minecraft.world.level.block.Blocks.TALL_GRASS)
                    || s.is(net.minecraft.world.level.block.Blocks.FERN) || s.is(net.minecraft.world.level.block.Blocks.LARGE_FERN) || s.is(net.minecraft.world.level.block.Blocks.COBWEB)
                    || s.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock && !s.getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT);   // never leaves someone placed
            if (!cut) continue;
            level.destroyBlock(p, true, this);
            if (++n >= 6) break;
        }
        if (n > 0) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, friend.getX(), friend.getY() + 1, friend.getZ(), 3, 1.5, 0.5, 1.5, 0.0);
            this.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 0.5F, 1.4F);
        }
        return n;
    }

    // --- the marmotaur and the bambadger -------------------------------------------------------------------------------

    private long nextWhistle;

    /** Monsters within 16 blocks of her friend glow; when new ones come, she whistles a warning. Returns how many she saw. */
    public int keepWatch(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return 0;
        int n = 0;
        boolean fresh = false;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(16))) {
            fresh |= !m.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING);
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 60, 0, true, false));
            n++;
        }
        if (fresh && level.getGameTime() >= this.nextWhistle) {
            this.nextWhistle = level.getGameTime() + 200;
            this.playSound(SoundEvents.NOTE_BLOCK_FLUTE.value(), 2.0F, 2.0F);
            this.startTimedAction(Action.POINT, 20);
            friend.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.marmot_whistle", this.getDisplayName()), true);
        }
        return n;
    }

    /** Her bamboo flute: the bamboo and sugar cane within 6 blocks grow. Returns how many grew. */
    public int playFlute(ServerLevel level) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-6, -2, -6), this.blockPosition().offset(6, 3, 6))) {
            var s = level.getBlockState(p);
            if (s.is(net.minecraft.world.level.block.Blocks.BAMBOO) || s.is(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING)) {
                if (level.getBlockState(p.above()).is(net.minecraft.world.level.block.Blocks.BAMBOO)) continue;     // only the tip of each stalk
                if (net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, p.immutable())) n++;
            } else if (s.is(net.minecraft.world.level.block.Blocks.SUGAR_CANE) && level.getBlockState(p.above()).isAir()) {
                int height = 1;
                while (height < 3 && level.getBlockState(p.below(height)).is(net.minecraft.world.level.block.Blocks.SUGAR_CANE)) height++;
                if (height < 3) {
                    level.setBlock(p.above(), net.minecraft.world.level.block.Blocks.SUGAR_CANE.defaultBlockState(), 3);
                    n++;
                }
            }
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 6, 0.6, 0.3, 0.6, 1.0);
        this.playSound(SoundEvents.NOTE_BLOCK_FLUTE.value(), 1.0F, 1.0F + this.random.nextFloat() * 0.5F);
        return n;
    }

    // --- the coatimano ------------------------------------------------------------------------------------------------

    @Nullable
    private BlockPos dig;

    /** Suspicious sand or gravel within 12 blocks. */
    @Nullable
    public BlockPos findDig(ServerLevel level) {
        BlockPos o = this.blockPosition(), best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-12, -4, -12), o.offset(12, 4, 12))) {
            if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.BrushableBlockEntity && (best == null || p.distSqr(o) < best.distSqr(o))) {
                best = p.immutable();
            }
        }
        return best;
    }

    /** One stroke of her paws on the block; when it gives up its find, she carries it to her friend. True once it is dug out. */
    public boolean brushAt(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BrushableBlockEntity be)) return true;
        Player digger = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
        be.brush(level.getGameTime(), digger, net.minecraft.core.Direction.UP);
        this.playSound(SoundEvents.BRUSH_SAND, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BrushableBlockEntity) return false;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend != null) {
            for (net.minecraft.world.entity.item.ItemEntity found : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(1.5))) {
                ItemStack s = found.getItem().copy();
                found.discard();
                if (!friend.getInventory().add(s)) friend.drop(s, false);
            }
        }
        return true;
    }

    private void tickDig(ServerLevel level) {
        if (this.dig == null) {
            if (this.tickCount % 40 == 0 && this.random.nextInt(3) == 0) this.dig = this.findDig(level);
            return;
        }
        if (this.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(this.dig)) > 2.5 * 2.5) {
            if (this.tickCount % 20 == 0) this.getNavigation().moveTo(this.dig.getX() + 0.5, this.dig.getY() + 1, this.dig.getZ() + 0.5, 1.0);
        } else if (this.tickCount % 12 == 0) {
            this.getLookControl().setLookAt(net.minecraft.world.phys.Vec3.atCenterOf(this.dig));
            if (this.brushAt(level, this.dig)) this.dig = null;
        }
    }

    // --- the needlehog -----------------------------------------------------------------------------------------------

    private int curled;
    /** Paper and books she has picked up (her notes). */
    private final SimpleContainer notes = new SimpleContainer(9);

    public boolean isCurled() {
        return this.curled > 0;
    }

    public void curlUp() {
        this.curled = 60;
        this.getNavigation().stop();
        this.setAction(Action.CURL);
        this.playSound(SoundEvents.ARMADILLO_ROLL, 1.0F, 1.3F);
    }

    public SimpleContainer getNotes() {
        return this.notes;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.species != GirlSpecies.NEEDLEHOG_RODENT) return super.hurt(source, amount);
        if (this.isCurled() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {      // a ball of quills: the attacker gets pricked
            if (!this.level().isClientSide && source.getDirectEntity() instanceof net.minecraft.world.entity.LivingEntity l) l.hurt(this.damageSources().thorns(this), 2.0F);
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive()) this.curlUp();
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack s : this.notes.removeAllItems()) this.spawnAtLocation(s);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return this.species == GirlSpecies.NEEDLEHOG_RODENT && (stack.is(Items.PAPER) || stack.is(Items.BOOK) || stack.is(Items.WRITABLE_BOOK)
                || stack.is(Items.WRITTEN_BOOK) || stack.is(Items.MAP)) && this.notes.canAddItem(stack);
    }

    @Override
    protected void pickUpItem(net.minecraft.world.entity.item.ItemEntity item) {
        if (!this.wantsToPickUp(item.getItem())) return;
        this.onItemPickup(item);
        this.take(item, item.getItem().getCount());
        ItemStack left = this.notes.addItem(item.getItem().copy());
        if (left.isEmpty()) item.discard();
        else item.setItem(left);
    }

    // --- the capybara --------------------------------------------------------------------------------------------

    /** Predators near her forget their prey, idle girls come and sit with her, and her devoted friends rest easy. */
    public void spreadCalm(ServerLevel level) {
        for (Mob m : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(CALM_RADIUS),
                m -> m.getType().is(ModTags.CALMED_BY_CAPYBARA) && m.getTarget() != null)) {
            m.setTarget(null);
        }
        if (this.tickCount % 100 == 0) {
            for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, this.getBoundingBox().inflate(6),
                    g -> g != this && g.getSpecies() != GirlSpecies.CAPYBARA_RODENT && g.getAction() == Action.NONE && g.getTarget() == null
                            && !g.canFly() && g.getNavigation().isDone() && !g.isOrderedToSit())) {
                if (this.random.nextInt(3) != 0) continue;
                g.getLookControl().setLookAt(this);
                g.startTimedAction(Action.SITTING, 200);
            }
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6), p -> this.getAffection(p) >= 60)) {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, true, true));
            }
        }
    }

    // --- Ratatoskr -----------------------------------------------------------------------------------------------

    public static boolean isHoardable(ItemStack stack) {
        return stack.is(ItemTags.SAPLINGS) || stack.is(Tags.Items.SEEDS);
    }

    public SimpleContainer getCache() {
        return this.cache;
    }

    public ItemStack getParcel() {
        return this.parcel;
    }

    /** She gathers seeds and saplings lying nearby into her cache. */
    private void gather(ServerLevel level) {
        ItemEntity item = this.nearest(ItemEntity.class, 8, e -> isHoardable(e.getItem()) && this.cache.canAddItem(e.getItem()));
        if (item == null) return;
        if (this.distanceToSqr(item) > 1.5 * 1.5) {
            this.getNavigation().moveTo(item, 1.2);
            return;
        }
        ItemStack rest = this.cache.addItem(item.getItem().copy());
        if (rest.isEmpty()) item.discard();
        else item.setItem(rest);
        this.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.6F);
    }

    /** Now and then she forgets a sapling, which takes root where she buried it. */
    public boolean forgetASapling(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) return false;
        for (int i = 0; i < this.cache.getContainerSize(); i++) {
            ItemStack s = this.cache.getItem(i);
            if (s.is(ItemTags.SAPLINGS) && s.getItem() instanceof BlockItem b && b.getBlock().defaultBlockState().canSurvive(level, pos)) {
                level.setBlock(pos, b.getBlock().defaultBlockState(), 3);
                s.shrink(1);
                this.startTimedAction(Action.SITTING, 30);              // she crouches to bury it
                this.playSound(SoundEvents.GRASS_PLACE, 1.0F, 1.2F);
                return true;
            }
        }
        return false;
    }

    /** A friend hands her something: she runs it to the nearest other player she is fond of. */
    public boolean takeParcel(Player from, ItemStack stack) {
        Player to = this.recipientFor(from);
        if (to == null) {
            from.displayClientMessage(Component.translatable("message.monstergirls.ratatoskr_nobody", this.getDisplayName()), true);
            return false;
        }
        this.parcel = stack.split(stack.getCount());
        this.sender = from.getUUID();
        this.recipient = to.getUUID();
        this.startTimedAction(Action.SCAMPER, 8);
        from.displayClientMessage(Component.translatable("message.monstergirls.ratatoskr_takes", this.getDisplayName(), to.getDisplayName()), true);
        return true;
    }

    @Nullable
    private Player recipientFor(Player from) {
        Player best = null;
        for (Player p : this.level().players()) {
            if (p == from || p.isSpectator() || this.getAffection(p) < Orders.AFFECTION) continue;
            if (best == null || p.distanceToSqr(this) < best.distanceToSqr(this)) best = p;
        }
        return best;
    }

    /** She runs to the recipient; once there she hands it over (or, one time in eight, something else entirely). */
    public void deliver(ServerLevel level) {
        Player to = this.recipient == null ? null : level.getPlayerByUUID(this.recipient);
        if (to == null || to.isSpectator()) {                         // gone: she drops it where she stands
            this.spawnAtLocation(this.parcel);
            this.clearParcel();
            return;
        }
        if (this.distanceToSqr(to) > 2.5 * 2.5) {
            if (this.tickCount % 20 == 0) this.getNavigation().moveTo(to, 1.4);
            return;
        }
        Player from = this.sender == null ? null : level.getPlayerByUUID(this.sender);
        Component fromName = from != null ? from.getDisplayName() : Component.literal("?");
        boolean prank = this.random.nextInt(PRANK_ODDS) == 0;
        ItemStack given = prank ? new ItemStack(PRANKS.get(this.random.nextInt(PRANKS.size()))) : this.parcel;
        if (prank) this.cache.addItem(this.parcel);                    // the real thing goes into her cache: she gives it up as a present later
        if (!to.getInventory().add(given)) to.drop(given, false);
        this.getLookControl().setLookAt(to);
        this.startTimedAction(Action.SCAMPER, 8);
        this.playSound(SoundEvents.FOX_AMBIENT, 1.0F, 1.6F);
        to.displayClientMessage(Component.translatable(prank ? "message.monstergirls.ratatoskr_prank" : "message.monstergirls.ratatoskr_delivers",
                this.getDisplayName(), fromName), false);
        this.clearParcel();
    }

    private void clearParcel() {
        this.parcel = ItemStack.EMPTY;
        this.sender = null;
        this.recipient = null;
    }

    @Override
    @Nullable
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.SILKCOON_RODENT && !this.isBaby()) {
            ItemStack clean = washed(stack);
            if (clean != null) {
                if (!this.nearWater()) {
                    if (!this.level().isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.silkcoon_water", this.getDisplayName()), true);
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
                if (!this.level().isClientSide) {
                    player.setItemInHand(hand, clean);
                    this.playSound(SoundEvents.GENERIC_SPLASH, 0.8F, 1.3F);
                    this.startTimedAction(Action.SOAK, 30);
                    this.addAffection(player, 1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        if (this.species != GirlSpecies.RATATOSKR_RODENT || this.isBaby() || stack.isEmpty() || this.isFood(stack) || stack.is(ModTags.MEAT)
                || this.getAffection(player) < Orders.AFFECTION || !this.parcel.isEmpty()) return null;
        if (!this.level().isClientSide) this.takeParcel(player, stack);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.PINESKRELL_RODENT && this.level() instanceof ServerLevel ores && !this.isBaby() && this.tickCount % 100 == 0) this.oreSense(ores);
        if (this.species == GirlSpecies.COATIMANO_RODENT && this.level() instanceof ServerLevel diggingLevel && !this.isBaby()) this.tickDig(diggingLevel);
        if (this.species == GirlSpecies.SQUIRRICERAT_RODENT && this.level() instanceof ServerLevel acorn && !this.isBaby() && this.tickCount % 2400 == 0) this.buryAcorn(acorn);
        if (this.species == GirlSpecies.KAMAITACHI_RODENT && this.level() instanceof ServerLevel gale && !this.isBaby() && this.tickCount % 20 == 0) this.gust(gale);
        if (this.species == GirlSpecies.MARMOTAUR_RODENT && this.level() instanceof ServerLevel lookout && !this.isBaby() && this.tickCount % 20 == 0) this.keepWatch(lookout);
        if (this.species == GirlSpecies.ROCKHYRAX_RODENT && this.level() instanceof ServerLevel rocks && this.tickCount % 20 == 0) this.sentinel(rocks);
        if (this.species == GirlSpecies.BAMBADGER_RODENT && this.level() instanceof ServerLevel grove && !this.isBaby() && this.tickCount % 600 == 0
                && this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32) != null) this.playFlute(grove);
        if (this.species == GirlSpecies.NEEDLEHOG_RODENT && !this.level().isClientSide) {
            if (this.curled > 0 && --this.curled == 0 && this.getAction() == Action.CURL) this.setAction(Action.NONE);
            if (this.isCurled()) this.getNavigation().stop();
            else if (this.tickCount % 10 == 0 && !this.isBaby()) {          // a sprinting stranger rolls her up
                for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4),
                        p -> p.isSprinting() && !p.isSpectator() && !p.isCreative() && this.getAffection(p) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
                    this.curlUp();
                    break;
                }
            }
        }
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.CAPYBARA_RODENT) {
            if (this.tickCount % 20 == 0) this.spreadCalm(server);
            if (this.tickCount % 40 == 0 && this.isInWater() && this.getAction() == Action.NONE && this.getTarget() == null) this.startTimedAction(Action.SOAK, 200);
            if (this.getAction() == Action.SOAK && !this.isInWater()) this.setAction(Action.NONE);
        } else if (!this.isBaby()) {
            if (!this.parcel.isEmpty()) this.deliver(server);
            else if (this.tickCount % 20 == 0 && this.getTarget() == null) this.gather(server);
            if (this.tickCount % 1200 == 0 && this.random.nextInt(2) == 0) this.forgetASapling(server);
        }
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.NEEDLEHOG_RODENT) return stack.is(Items.SPIDER_EYE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES);
        if (this.species == GirlSpecies.COATIMANO_RODENT) return stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.EGG);
        if (this.species == GirlSpecies.MARMOTAUR_RODENT) return stack.is(Items.SHORT_GRASS) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.WHEAT);
        if (this.species == GirlSpecies.BAMBADGER_RODENT) return stack.is(Items.BAMBOO) || stack.is(Items.SWEET_BERRIES);
        if (this.species == GirlSpecies.SQUIRRICERAT_RODENT) return stack.is(Items.SWEET_BERRIES) || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.PUMPKIN_SEEDS);
        if (this.species == GirlSpecies.ROCKHYRAX_RODENT) return stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES);
        if (this.species == GirlSpecies.SILKCOON_RODENT) return stack.is(net.minecraft.tags.ItemTags.FISHES) || stack.is(Items.EGG) || stack.is(Items.SWEET_BERRIES);
        if (this.species == GirlSpecies.KAMAITACHI_RODENT) return stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN) || stack.is(Items.EGG);
        return this.species == GirlSpecies.CAPYBARA_RODENT ? stack.is(Items.MELON_SLICE) || stack.is(Items.SUGAR_CANE) || stack.is(Items.APPLE)
                : stack.is(Tags.Items.SEEDS) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) || stack.is(Tags.Items.SEEDS) ? 2 : super.foodValue(player, stack);
    }

    @Override
    public ItemStack rollGift(Player player) {
        if (this.species == GirlSpecies.NEEDLEHOG_RODENT) {                 // one of her notes, else something from the forest floor
            for (int i = 0; i < this.notes.getContainerSize(); i++) if (!this.notes.getItem(i).isEmpty()) return this.notes.removeItemNoUpdate(i);
            int r = this.random.nextInt(100);
            return new ItemStack(r < 40 ? Items.PAPER : r < 75 ? Items.SWEET_BERRIES : Items.SPIDER_EYE, r < 75 ? 2 : 1);
        }
        if (this.species == GirlSpecies.SQUIRRICERAT_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 30 ? Items.OAK_SAPLING : r < 55 ? Items.BIRCH_SAPLING : r < 75 ? Items.DARK_OAK_SAPLING : r < 95 ? Items.SWEET_BERRIES : Items.BONE_BLOCK,
                    r < 95 ? 2 : 1);
        }
        if (this.species == GirlSpecies.ROCKHYRAX_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 75 ? Items.FLINT : r < 95 ? Items.APPLE : Items.AMETHYST_SHARD, r < 95 ? 2 : 1);
        }
        if (this.species == GirlSpecies.SILKCOON_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 40 ? Items.STRING : r < 70 ? Items.WHITE_WOOL : r < 95 ? Items.CLAY_BALL : Items.NAME_TAG, r < 95 ? 3 : 1);
        }
        if (this.species == GirlSpecies.KAMAITACHI_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 40 ? Items.FEATHER : r < 75 ? Items.STRING : r < 95 ? Items.WHEAT_SEEDS : Items.WIND_CHARGE, r < 95 ? 3 : 2);
        }
        if (this.species == GirlSpecies.MARMOTAUR_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 40 ? Items.FLINT : r < 70 ? Items.HONEYCOMB : r < 95 ? Items.GOLD_NUGGET : Items.EMERALD, r < 95 ? 2 : 1);
        }
        if (this.species == GirlSpecies.BAMBADGER_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 45 ? Items.BAMBOO : r < 85 ? Items.SUGAR_CANE : Items.NOTE_BLOCK, r < 85 ? 4 : 1);
        }
        if (this.species == GirlSpecies.COATIMANO_RODENT) {
            int r = this.random.nextInt(100);
            return new ItemStack(r < 35 ? Items.BONE : r < 65 ? Items.FLINT : r < 90 ? Items.BRICK : Items.ARMS_UP_POTTERY_SHERD, r < 90 ? 2 : 1);
        }
        if (this.species == GirlSpecies.RATATOSKR_RODENT) {
            for (int i = 0; i < this.cache.getContainerSize(); i++) {
                if (!this.cache.getItem(i).isEmpty()) return this.cache.removeItemNoUpdate(i);
            }
            return new ItemStack(this.random.nextBoolean() ? Items.OAK_SAPLING : Items.SWEET_BERRIES, 2);
        }
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.SUGAR_CANE, 3) : r < 85 ? new ItemStack(Items.MELON_SLICE, 2) : new ItemStack(Items.LILY_PAD);
    }

    @Override
    protected int litterSize() {
        return this.species == GirlSpecies.RATATOSKR_RODENT ? 2 + this.random.nextInt(2) : 2 + this.random.nextInt(3);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level().isClientSide) return;
        this.spawnAtLocation(this.parcel);
        for (int i = 0; i < this.cache.getContainerSize(); i++) this.spawnAtLocation(this.cache.removeItemNoUpdate(i));
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.CAPYBARA_RODENT ? SoundEvents.SNIFFER_IDLE : SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == GirlSpecies.CAPYBARA_RODENT ? SoundEvents.SNIFFER_HURT : SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.species == GirlSpecies.CAPYBARA_RODENT ? SoundEvents.SNIFFER_DEATH : SoundEvents.RABBIT_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Cache", this.cache.createTag(this.registryAccess()));
        tag.put("Notes", this.notes.createTag(this.registryAccess()));
        if (!this.parcel.isEmpty()) {
            tag.put("Parcel", this.parcel.save(this.registryAccess()));
            if (this.sender != null) tag.putUUID("Sender", this.sender);
            if (this.recipient != null) tag.putUUID("Recipient", this.recipient);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.cache.fromTag(tag.getList("Cache", 10), this.registryAccess());
        this.notes.fromTag(tag.getList("Notes", 10), this.registryAccess());
        this.parcel = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Parcel"));
        this.sender = tag.hasUUID("Sender") ? tag.getUUID("Sender") : null;
        this.recipient = tag.hasUUID("Recipient") ? tag.getUUID("Recipient") : null;
    }

    // --- the pineskrell (eighteenth wave) ---------------------------------------------------------------------------

    /** The ores within 6 blocks of her friend sparkle (at most 16 of them). How many sparkled, or -1 without a friend. */
    public int oreSense(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -6, -6), friend.blockPosition().offset(6, 6, 6))) {
            if (!level.getBlockState(p).is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) continue;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 6, 0.4, 0.4, 0.4, 0.0);
            if (++n >= 16) break;
        }
        return n;
    }
}
