package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The ironbeak (HARMONTIDE_CONCEPTS.md, twenty-third wave; model tools/gen_ironbeak_harpy.py, the crow's rig in a blue courier
 * bird's colours): silver talons, a blue scarf, a satchel of letters. Befriended, once a day she brings her friend (within 16
 * of her) an explorer's map to a structure they have not found yet: a woodland mansion, an ocean monument, buried treasure
 * or a trial chamber, a different one each day.
 */
public class IronbeakHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;
    /** The kinds of map she carries, one a day in turn. */
    private static final List<TagKey<Structure>> ROUTES = List.of(StructureTags.ON_WOODLAND_EXPLORER_MAPS, StructureTags.ON_OCEAN_EXPLORER_MAPS,
            StructureTags.ON_TREASURE_MAPS, StructureTags.ON_TRIAL_CHAMBERS_MAPS);

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(IronbeakHarpy.class, EntityDataSerializers.BYTE);
    /** The day she last brought a map (-1: never). */
    private long lastMapDay = -1;

    /** Her colours (tools/gen_ironbeak_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), SLATE("slate"), CARDINAL("cardinal"), JADE("jade"), GOLD("gold");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return BLUE;
        }
    }

    public IronbeakHarpy(EntityType<? extends IronbeakHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkIronbeakHarpySpawnRules(EntityType<IronbeakHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                       RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    public Variant getVariant() {
        return Variant.values()[this.entityData.get(DATA_VARIANT)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, (byte) variant.ordinal());
    }

    public String getVariantSuffix() {
        return this.getVariant().ordinal() == 0 ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    public void plainVariant() {
        this.setVariant(Variant.values()[0]);
    }

    @Override
    public void randomizeVariant() {
        int r = this.random.nextInt(100);
        this.setVariant(r < 60 ? Variant.BLUE : r < 75 ? Variant.SLATE : r < 87 ? Variant.CARDINAL : r < 95 ? Variant.JADE : Variant.GOLD);
    }

    // --- the post --------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 100 == 0) this.dailyPost(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** Once a day, the map of the day's route for her friend, if one of those structures is out there unfound. */
    public void dailyPost(ServerLevel level) {
        long day = level.getDayTime() / 24000L;
        Player friend = this.friend();
        if (friend == null || day == this.lastMapDay) return;
        this.lastMapDay = day;
        BlockPos target = level.findNearestMapStructure(ROUTES.get((int) (day % ROUTES.size())), friend.blockPosition(), 50, true);
        if (target != null) this.deliverMap(level, friend, target);
    }

    /** An explorer's map centred on `target`, its red cross on it, into her friend's hands (or at their feet). */
    public ItemStack deliverMap(ServerLevel level, Player friend, BlockPos target) {
        ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable("item.harmontide.ironbeak_map"));
        ItemStack given = map.copy();
        if (!friend.getInventory().add(map)) friend.drop(map, false);
        this.getLookControl().setLookAt(friend);
        this.startTimedAction(Action.CAWING, 20);
        this.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.2F);
        return given;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.PAPER) || stack.is(Items.MAP) || stack.is(Items.WRITABLE_BOOK);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.harmontide.ironbeak." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 45 ? new ItemStack(Items.FEATHER, 3) : r < 75 ? new ItemStack(Items.PAPER, 4) : r < 95 ? new ItemStack(Items.COMPASS)
                : new ItemStack(Items.MAP);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "ironbeak_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING, SINGING -> "caw";
            case CACHING -> "cache";
            case KNOCKING -> "knock";
            case SITTING -> "sit";
            case LYING, SLEEPING -> "lie";
            default -> null;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.putLong("LastMapDay", this.lastMapDay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.lastMapDay = tag.contains("LastMapDay") ? tag.getLong("LastMapDay") : -1;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.PARROT_EAT;
    }
}
