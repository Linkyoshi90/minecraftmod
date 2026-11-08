package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Harpy colonies (2026-11-08). The harpies of an aerie (world/HarpyAerieStructure) belong to it: each has her nest for a home
 * and the colony's mark; one is its matriarch, who keeps her name above her head. Befriend the matriarch ({@link HomeLife#FRIEND})
 * and the whole colony welcomes you: every one of them within {@link #REACH} blocks becomes your friend too.
 * <p>
 * A harpy perch (block/HarpyPerchBlock) does the same for a passing flock (entity/Flocks): a flock that comes down within
 * {@link #PERCH_REACH} blocks of one settles round it as a colony of its own, roosting there for good.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Colonies {
    public static final int REACH = 48, PERCH_REACH = 32, PERCH_LOOK_UP = 12;
    public static final String COLONY = "Colony", MATRIARCH = "ColonyMatriarch", WELCOMED = "ColonyWelcomed";

    private Colonies() {}

    /** One of a colony: grown, wild, persistent, nesting at {@code nest}. The matriarch is named so. */
    @Nullable
    public static <T extends AbstractHarpy> T settle(ServerLevelAccessor level, EntityType<T> kind, BlockPos nest, BlockPos colony, boolean matriarch) {
        T harpy = kind.create(level.getLevel());
        if (harpy == null) return null;
        harpy.moveTo(nest.getX() + 0.5, nest.getY(), nest.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
        harpy.finalizeSpawn(level, level.getCurrentDifficultyAt(nest), MobSpawnType.STRUCTURE, null);
        harpy.randomizeVariant();
        harpy.setBaby(false);
        harpy.setPersistenceRequired();
        harpy.setHomePos(nest);
        mark(harpy, colony, matriarch);
        level.addFreshEntityWithPassengers(harpy);
        return harpy;
    }

    public static void mark(Mob harpy, BlockPos colony, boolean matriarch) {
        mark(harpy, colony, matriarch, "entity.monstergirls.colony_matriarch");
    }

    /** One of a colony (any kind of girl: a grotto's court too); its leader named with {@code title} ("%s Matriarch", "%s Queen"). */
    public static void mark(Mob m, BlockPos colony, boolean leader, String title) {
        m.getPersistentData().putLong(COLONY, colony.asLong());
        if (leader) {
            m.getPersistentData().putBoolean(MATRIARCH, true);
            m.setCustomName(Component.translatable(title, m.getType().getDescription()));
        }
    }

    @Nullable
    public static BlockPos colonyOf(Mob m) {
        return m.getPersistentData().contains(COLONY) ? BlockPos.of(m.getPersistentData().getLong(COLONY)) : null;
    }

    public static boolean matriarch(Mob m) {
        return m.getPersistentData().getBoolean(MATRIARCH);
    }

    /** The rest of this matriarch's colony near her. */
    public static List<Mob> colony(ServerLevel level, Mob matriarch) {
        BlockPos id = colonyOf(matriarch);
        if (id == null) return List.of();
        return level.getEntitiesOfClass(Mob.class, matriarch.getBoundingBox().inflate(REACH), m -> m != matriarch && m instanceof Bonded && id.equals(colonyOf(m)));
    }

    /** The matriarch has made a friend: the colony welcomes them. How many took them in (0 if they were welcomed before). */
    public static int welcome(ServerLevel level, Mob matriarch, Player p) {
        ListTag done = matriarch.getPersistentData().getList(WELCOMED, 8);
        String id = p.getUUID().toString();
        for (int i = 0; i < done.size(); i++) if (done.getString(i).equals(id)) return 0;
        done.add(StringTag.valueOf(id));
        matriarch.getPersistentData().put(WELCOMED, done);
        int n = 0;
        for (Mob m : colony(level, matriarch)) {
            Bonded b = (Bonded) m;
            if (b.getAffection(p) < HomeLife.FRIEND) b.addAffection(p, HomeLife.FRIEND - Math.max(0, b.getAffection(p)));
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            n++;
        }
        boolean harpies = matriarch instanceof AbstractHarpy;
        p.displayClientMessage(Component.translatable(harpies ? "message.monstergirls.colony.welcome" : "message.monstergirls.grotto.welcome", matriarch.getDisplayName(), n)
                .withStyle(ChatFormatting.AQUA), false);
        Dairy.award(p, harpies ? "harpy_colony" : "merfolk_grotto");
        return n;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob m) || m.tickCount % 100 != 71 || !(m.level() instanceof ServerLevel level) || !matriarch(m) || !(m instanceof Bonded b)) return;
        for (Player p : level.players()) {
            if (b.getAffection(p) >= HomeLife.FRIEND && p.distanceToSqr(m) < REACH * REACH) welcome(level, m, p);
        }
    }

    // --- the perch ----------------------------------------------------------------------------------------------------

    /** The nearest harpy perch within reach of this spot, or null. */
    @Nullable
    public static BlockPos perchNear(ServerLevel level, BlockPos at) {
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-PERCH_REACH, -PERCH_LOOK_UP, -PERCH_REACH), at.offset(PERCH_REACH, PERCH_LOOK_UP, PERCH_REACH))) {
            if (!level.getBlockState(p).is(ModBlocks.HARPY_PERCH.get())) continue;
            double d = p.distSqr(at);
            if (d < bestD) {
                bestD = d;
                best = p.immutable();
            }
        }
        return best;
    }

    /** A flock settles at a perch: they roost there for good, a colony of their own; the first of them leads it. */
    public static void settleAtPerch(List<Mob> flock, BlockPos perch) {
        for (int i = 0; i < flock.size(); i++) {
            Mob m = flock.get(i);
            Flocks.stay(m);
            if (m instanceof AbstractHarpy h) h.setHomePos(perch.above());
            mark(m, perch, i == 0);
        }
    }
}
