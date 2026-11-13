package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A carcass (2026-11-13): what is left where a wild animal died to anything but a player (a hunter girl, a wolf, a zombie, a fall).
 * It is drawn as the animal itself, fallen on its side and bloodied (client/CarcassRenderer), with the signs of the struggle scattered
 * round it, and it draws the scavengers and hungry hunters of the food web (entity/FoodWeb), each meal taking meat off the bones.
 * <p>
 * Each animal carries its own amount of meat ({@link #KINDS}: a cow most, a chicken or rabbit least; a young one half). Use a sword or
 * an axe on it to butcher it: a random share of what is left ({@link #butcher}), the hide, wool or feathers that are left, and a bone.
 * It goes off with time: fresh for a day, then {@link Stage#ROTTING} (flies, and some of the meat comes away rotten), then only
 * {@link Stage#BONES}, and after {@link #GONE} ticks nothing at all. Off with the config option {@code foodWeb}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public class Carcass extends Entity {
    public static final int FRESH = 24000, ROTTEN = 48000, GONE = 72000;
    public static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.55F, 0.05F, 0.05F), 0.9F);
    public static final DustParticleOptions FLY = new DustParticleOptions(new Vector3f(0.08F, 0.08F, 0.1F), 0.35F);
    private static final EntityDataAccessor<String> DATA_KIND = SynchedEntityData.defineId(Carcass.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_MEAT = SynchedEntityData.defineId(Carcass.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FULL = SynchedEntityData.defineId(Carcass.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_STAGE = SynchedEntityData.defineId(Carcass.class, EntityDataSerializers.BYTE);
    private long born;

    /** What an animal leaves: its meat, how much of it at most, and its hide (with how many at most). */
    public record Kind(Item meat, int meat_max, Item hide, int hide_max) {}

    public static final Map<EntityType<?>, Kind> KINDS = Map.of(
            EntityType.COW, new Kind(Items.BEEF, 8, Items.LEATHER, 2),
            EntityType.MOOSHROOM, new Kind(Items.BEEF, 8, Items.LEATHER, 2),
            EntityType.PIG, new Kind(Items.PORKCHOP, 6, Items.AIR, 0),
            EntityType.SHEEP, new Kind(Items.MUTTON, 4, Items.WHITE_WOOL, 1),
            EntityType.GOAT, new Kind(Items.MUTTON, 4, Items.LEATHER, 1),
            EntityType.CHICKEN, new Kind(Items.CHICKEN, 2, Items.FEATHER, 3),
            EntityType.RABBIT, new Kind(Items.RABBIT, 2, Items.RABBIT_HIDE, 1));

    public enum Stage { FRESH, ROTTING, BONES }

    public Carcass(EntityType<? extends Carcass> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_KIND, "minecraft:cow");
        builder.define(DATA_MEAT, 0);
        builder.define(DATA_FULL, 1);
        builder.define(DATA_STAGE, (byte) 0);
    }

    // --- what it is ------------------------------------------------------------------------------------------------------

    public EntityType<?> animal() {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(this.entityData.get(DATA_KIND)));
    }

    @Nullable
    public Kind kind() {
        return KINDS.get(this.animal());
    }

    public int meat() {
        return this.entityData.get(DATA_MEAT);
    }

    /** How much there was to begin with. */
    public int full() {
        return Math.max(1, this.entityData.get(DATA_FULL));
    }

    public void setMeat(int meat) {
        this.entityData.set(DATA_MEAT, Math.max(0, meat));
        if (meat <= 0) this.setStage(Stage.BONES);
    }

    public Stage stage() {
        return Stage.values()[this.entityData.get(DATA_STAGE)];
    }

    private void setStage(Stage s) {
        this.entityData.set(DATA_STAGE, (byte) s.ordinal());
    }

    public long age() {
        return this.level().getGameTime() - this.born;
    }

    /** Ages it (tests, commands). */
    public void setAge(long ticks) {
        this.born = this.level().getGameTime() - ticks;
    }

    // --- coming to be ----------------------------------------------------------------------------------------------------

    /** The carcass of an animal that just died here, or null if it leaves none. */
    @Nullable
    public static Carcass of(ServerLevel level, LivingEntity dead) {
        Kind k = KINDS.get(dead.getType());
        if (k == null) return null;
        Carcass c = ModEntities.CARCASS.get().create(level);
        if (c == null) return null;
        c.moveTo(dead.getX(), dead.getY(), dead.getZ(), dead.getYRot(), 0.0F);
        c.entityData.set(DATA_KIND, BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType()).toString());
        int meat = dead.isBaby() ? Math.max(1, k.meat_max() / 2) : k.meat_max();
        c.entityData.set(DATA_FULL, meat);
        c.setMeat(meat);
        c.born = level.getGameTime();
        level.addFreshEntity(c);
        level.sendParticles(BLOOD, c.getX(), c.getY() + 0.3, c.getZ(), 24, 0.6, 0.15, 0.6, 0.0);   // the struggle
        RandomSource r = level.getRandom();
        if (k.hide() == Items.FEATHER || k.hide() == Items.WHITE_WOOL && r.nextBoolean()) {             // feathers or a tuft of wool torn loose
            ItemEntity torn = new ItemEntity(level, c.getX() + r.nextGaussian() * 0.8, c.getY() + 0.2, c.getZ() + r.nextGaussian() * 0.8, new ItemStack(k.hide()));
            level.addFreshEntity(torn);
        }
        FoodWeb.died(level, c.blockPosition());
        return c;
    }

    /** A wild animal killed by anything but a player leaves a carcass instead of its drops. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || !HarmontideConfig.foodWeb() || !KINDS.containsKey(dead.getType()) || dead.isOnFire()) return;
        DamageSource src = event.getSource();
        if (src.getEntity() instanceof Player || event.isRecentlyHit() || dead.hasCustomName()) return;   // players get their loot as ever
        if (of(level, dead) != null) event.setCanceled(true);
    }

    // --- eating and butchering ------------------------------------------------------------------------------------------

    /** Something eats from it: so much meat comes off. How much it got. */
    public int bite(int amount) {
        int got = Math.min(amount, this.meat());
        this.setMeat(this.meat() - got);
        if (got > 0 && this.level() instanceof ServerLevel level) {
            level.sendParticles(BLOOD, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.3, 0.1, 0.3, 0.0);
            this.playSound(SoundEvents.GENERIC_EAT, 0.8F, 0.8F);
        }
        return got;
    }

    /**
     * Butchered: a random share (at least half) of the meat that is left, some of it rotten once it is going off, the hide or wool
     * still on it (none once only bones are left), and a bone. The carcass is gone. What it gave.
     */
    public List<ItemStack> butcher(RandomSource r) {
        Kind k = this.kind();
        List<ItemStack> out = new java.util.ArrayList<>();
        if (k != null) {
            int left = this.meat();
            int meat = left <= 0 ? 0 : (left + 1) / 2 + r.nextInt(left - (left + 1) / 2 + 1);
            int rotten = this.stage() == Stage.ROTTING ? (meat + 1) / 2 : 0;
            if (meat - rotten > 0) out.add(new ItemStack(k.meat(), meat - rotten));
            if (rotten > 0) out.add(new ItemStack(Items.ROTTEN_FLESH, rotten));
            if (k.hide() != Items.AIR && this.stage() != Stage.BONES) {
                int hide = Math.max(0, Math.round(k.hide_max() * (float) left / this.full()));
                if (hide > 0) out.add(new ItemStack(k.hide(), hide));
            }
        }
        out.add(new ItemStack(Items.BONE, 1 + r.nextInt(2)));
        return out;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (!tool.is(ItemTags.SWORDS) && !tool.is(ItemTags.AXES)) {
            if (!this.level().isClientSide) player.displayClientMessage(Component.translatable("message.harmontide.carcass.blade"), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.level() instanceof ServerLevel level) {
            for (ItemStack s : this.butcher(level.getRandom())) {
                if (!player.getInventory().add(s)) player.drop(s, false);
            }
            tool.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
            level.sendParticles(BLOOD, this.getX(), this.getY() + 0.3, this.getZ(), 12, 0.4, 0.1, 0.4, 0.0);
            this.playSound(SoundEvents.SLIME_BLOCK_BREAK, 1.0F, 0.8F);
            this.discard();
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    // --- going off -----------------------------------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (!this.onGround()) this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.98, 0.5));
        Level level = this.level();
        if (level.isClientSide) {
            RandomSource r = this.random;
            if (this.stage() == Stage.ROTTING && r.nextInt(3) == 0) {   // flies
                level.addParticle(FLY, this.getX() + r.nextGaussian() * 0.4, this.getY() + 0.3 + r.nextDouble() * 0.6, this.getZ() + r.nextGaussian() * 0.4,
                        r.nextGaussian() * 0.05, 0.02, r.nextGaussian() * 0.05);
            } else if (this.stage() == Stage.FRESH && r.nextInt(40) == 0) {
                level.addParticle(BLOOD, this.getX() + r.nextGaussian() * 0.3, this.getY() + 0.25, this.getZ() + r.nextGaussian() * 0.3, 0, 0, 0);
            }
            return;
        }
        if (this.tickCount % 100 == 0) this.goOff();
    }

    /** It goes off with time: rotting after a day, bones after two (or once picked clean), gone after three. */
    public void goOff() {
        long age = this.age();
        if (age >= GONE) this.discard();
        else if ((age >= ROTTEN || this.meat() <= 0) && this.stage() != Stage.BONES) this.setStage(Stage.BONES);
        else if (age >= FRESH && this.stage() == Stage.FRESH) this.setStage(Stage.ROTTING);
    }

    public Vec3 centre() {
        return this.position();
    }

    // --- saving ----------------------------------------------------------------------------------------------------------

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_KIND, tag.getString("Kind"));
        this.entityData.set(DATA_MEAT, tag.getInt("Meat"));
        this.entityData.set(DATA_FULL, Math.max(1, tag.getInt("Full")));
        this.entityData.set(DATA_STAGE, tag.getByte("Stage"));
        this.born = tag.getLong("Born");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("Kind", this.entityData.get(DATA_KIND));
        tag.putInt("Meat", this.meat());
        tag.putInt("Full", this.full());
        tag.putByte("Stage", this.entityData.get(DATA_STAGE));
        tag.putLong("Born", this.born);
    }
}
