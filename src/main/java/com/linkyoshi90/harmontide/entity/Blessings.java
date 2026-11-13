package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Wives bless the land (2026-11-13). In the old stories a monster girl's married life changes the place she lives in. Every
 * {@link #EVERY} ticks each loaded hearth whose owner is in the level looks for that owner's wives within {@link #WIVES} blocks;
 * each wife's clade gives the home one blessing, and wives of the same blessing stack up to {@link #CAP}:
 * <ul>
 * <li>{@link Blessing#GROWTH} (plants, fae, birds, small grazers, insects): crops round the hearth grow on.</li>
 * <li>{@link Blessing#HERD} (hoofed and horned kinds): young animals grow up and parents are ready to breed again sooner.</li>
 * <li>{@link Blessing#CATCH} (sea, river and shore kinds): Luck for the owner at home (better fishing).</li>
 * <li>{@link Blessing#WARD} (hunters, the made, riders, the dead): monsters near the home glow and are weakened.</li>
 * <li>{@link Blessing#HOARD} (dragons, serpents, spinners): Haste for the owner at home.</li>
 * <li>{@link Blessing#SOOTHE} (devils and slimes): a gentle Regeneration for the owner at home.</li>
 * </ul>
 * The owner hears about it the first time a blessing settles on their home. Off with the config option {@code wifeBlessings}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Blessings {
    public static final int EVERY = 200, CAP = 3, RANGE = 16, OWNER = 24, CROP_TRIES = 6, HERD_TICKS = 1200;
    public static final double WIVES = 32;
    private static final String TOLD = "BlessingsTold";

    private Blessings() {}

    public enum Blessing {
        GROWTH, HERD, CATCH, WARD, HOARD, SOOTHE;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Which blessing a wife of this clade brings home. */
    public static Blessing of(Clade clade) {
        return switch (clade) {
            case FLORA, FAE, AVES, LAGOMORPHA, RODENTIA, HEXAPODA, PRIMATES -> Blessing.GROWTH;
            case BOVIDAE, PERISSODACTYLA, CERVIDAE, CAMELIDAE, SUINA, PAENUNGULATA, XENARTHRA, MARSUPIALIA -> Blessing.HERD;
            case ACTINOPTERYGII, MOLLUSCA, MALACOSTRACA, AMPHIBIA, WHIPPOMORPHA, TESTUDINES -> Blessing.CATCH;
            case FELIFORMIA, CANIDAE, ARCTOIDEA, CHIROPTERA, CONSTRUCT, RIDER, UNDEAD -> Blessing.WARD;
            case DRAGON, ARCHOSAURIA, SQUAMATA, CHELICERATA -> Blessing.HOARD;
            case DEVIL, SLIME -> Blessing.SOOTHE;
        };
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % EVERY != 7 || !HarmontideConfig.wifeBlessings()) return;
        for (BlockPos hearth : Set.copyOf(HearthBlockEntity.loaded(level))) {
            if (level.getBlockEntity(hearth) instanceof HearthBlockEntity h && h.owner() != null) {
                Player owner = level.getPlayerByUUID(h.owner());
                if (owner != null) bless(level, hearth, owner);
            }
        }
    }

    /** The blessings on this hearth from this owner's wives near it, and how many wives bring each (capped). */
    public static Map<Blessing, Integer> blessingsAt(ServerLevel level, BlockPos hearth, Player owner) {
        Map<Blessing, Integer> out = new EnumMap<>(Blessing.class);
        if (Marriage.count(owner) == 0) return out;
        List<Mob> wives = level.getEntitiesOfClass(Mob.class, new AABB(hearth).inflate(WIVES), m -> m.isAlive() && Marriage.married(m, owner));
        for (Mob wife : wives) {
            Clade clade = Clade.of(wife);
            if (clade != null) out.merge(of(clade), 1, (a, b) -> Math.min(CAP, a + b));
        }
        return out;
    }

    /** One round of blessings on a hearth. Returns what was given. */
    public static Map<Blessing, Integer> bless(ServerLevel level, BlockPos hearth, Player owner) {
        Map<Blessing, Integer> blessings = blessingsAt(level, hearth, owner);
        if (blessings.isEmpty()) return blessings;
        boolean home = owner.level() == level && owner.distanceToSqr(hearth.getCenter()) <= OWNER * OWNER;
        for (Map.Entry<Blessing, Integer> e : blessings.entrySet()) {
            int n = e.getValue();
            switch (e.getKey()) {
                case GROWTH -> growCrops(level, hearth, CROP_TRIES * n);
                case HERD -> tendHerd(level, hearth, HERD_TICKS * n);
                case CATCH -> { if (home) owner.addEffect(new MobEffectInstance(MobEffects.LUCK, EVERY + 60, n - 1, true, false, true)); }
                case WARD -> ward(level, hearth, n);
                case HOARD -> { if (home) owner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, EVERY + 60, n - 1, true, false, true)); }
                case SOOTHE -> { if (home) owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50 * n, 0, true, false, true)); }
            }
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, hearth.getX() + 0.5, hearth.getY() + 1.2, hearth.getZ() + 0.5, 3, 0.4, 0.3, 0.4, 0.0);
        tell(owner, blessings);
        return blessings;
    }

    /** The owner hears once about each blessing their home has. */
    private static void tell(Player owner, Map<Blessing, Integer> blessings) {
        var journal = owner.getData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL);
        String told = journal.getString(TOLD);
        StringBuilder now = new StringBuilder(told);
        for (Blessing b : blessings.keySet()) {
            if (told.contains("|" + b.key() + "|")) continue;
            now.append('|').append(b.key()).append('|');
            owner.displayClientMessage(Component.translatable("message.harmontide.blessing." + b.key()), false);
        }
        if (now.length() != told.length()) {
            journal.putString(TOLD, now.toString());
            owner.setData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL, journal);
        }
    }

    /** Some crops round the hearth grow one stage. How many grew. */
    public static int growCrops(ServerLevel level, BlockPos hearth, int tries) {
        int grew = 0;
        var random = level.getRandom();
        for (int i = 0; i < tries; i++) {
            BlockPos at = hearth.offset(random.nextInt(RANGE * 2 + 1) - RANGE, random.nextInt(7) - 3, random.nextInt(RANGE * 2 + 1) - RANGE);
            if (grow(level, at)) grew++;
        }
        return grew;
    }

    /** One crop grows a stage, if it is one and not yet ripe. */
    public static boolean grow(ServerLevel level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
            level.setBlock(at, crop.getStateForAge(crop.getAge(state) + 1), 2);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 2, 0.25, 0.2, 0.25, 0.0);
            return true;
        }
        return false;
    }

    /** Young animals near the hearth grow up sooner, parents rest less. How many were touched. */
    public static int tendHerd(ServerLevel level, BlockPos hearth, int ticks) {
        int n = 0;
        for (Animal a : level.getEntitiesOfClass(Animal.class, new AABB(hearth).inflate(RANGE))) {
            int age = a.getAge();
            if (age < 0) a.setAge(Math.min(0, age + ticks));
            else if (age > 0) a.setAge(Math.max(0, age - ticks));
            else continue;
            n++;
        }
        return n;
    }

    /** Monsters near the home glow and are weakened. How many. */
    public static int ward(ServerLevel level, BlockPos hearth, int n) {
        int found = 0;
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(hearth).inflate(RANGE + 8), m -> m instanceof Enemy && m.isAlive())) {
            if (m instanceof com.linkyoshi90.harmontide.entity.Bonded) continue;
            m.addEffect(new MobEffectInstance(MobEffects.GLOWING, EVERY + 60, 0, true, false));
            m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EVERY + 60, n - 1, true, false));
            found++;
        }
        return found;
    }
}
