package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * The cassowrath (HARMONTIDE_CONCEPTS.md, eleventh wave; model tools/gen_cassowrath_harpy.py, the ostrich's rig): a
 * Harmontide cassowary. Everything the ostrich harpy does (her nest and territory, the warning display, the kicks,
 * loyalty, riding), only harder: more health and a kick that hurts more. Her colours use the ostrich's variant names
 * (see the model script); she lays green cassowary eggs. Befriended, a stick's patrol order has her guard a spot.
 */
public class CassowrathHarpy extends OstrichHarpy {
    public CassowrathHarpy(EntityType<? extends CassowrathHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return OstrichHarpy.createAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 9.0).add(Attributes.ATTACK_KNOCKBACK, 1.6);
    }

    public static boolean checkCassowrathHarpySpawnRules(EntityType<CassowrathHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                         RandomSource random) {
        var below = level.getBlockState(pos.below());
        return (below.is(BlockTags.DIRT) || below.is(BlockTags.LEAVES)) && level.getRawBrightness(pos, 0) > 8;
    }

    /** Any of her colours, not the ostrich's by biome. */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);
        if (spawnType != MobSpawnType.BREEDING && com.linkyoshi90.harmontide.HarmontideConfig.keepsBiomeVariant(this.random)) this.randomizeVariant();
        return data;
    }

    public String getVariantSuffix() {
        return this.getVariant() == Variant.OSTRICH ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    protected String species() {
        return "cassowrath_harpy";
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CASSOWARY_EGG.get());
    }
}
