package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The birth itself, once her day is up. Live young: she sits (on a bed, if one is close) and leans back.
 * A spider crouches on the floor. An egg-layer builds a nest, lies on her stomach, and pushes each egg out.
 * What she bears is still {@link AbstractMonsterGirl#giveBirth}. This is not the night in bed.
 */
public final class Labor {
    public enum Kind { SIT, SPIDER, EGG, TAIL, TAUR, SLIME }

    /** How long a live birth or a spider crouch is held before the young are out. */
    public static final int POSE_TICKS = 32;
    /** Twigs and leaves before the first egg. */
    public static final int BUILD_TICKS = 28;
    /** On her stomach before the first egg, and between eggs. */
    public static final int FIRST_PUSH = 20;
    public static final int PUSH_EVERY = 12;
    private static final int SEEK_TICKS = 80;
    private static final int BED_RADIUS = 10;
    private static final int RETRY_TICKS = 40;

    private enum Phase { SEEK, BUILD, POSE, PUSH }

    private static final WeakHashMap<AbstractMonsterGirl, Session> SESSIONS = new WeakHashMap<>();
    private static final WeakHashMap<AbstractMonsterGirl, Integer> RETRY = new WeakHashMap<>();

    private Labor() {}

    public static boolean active(AbstractMonsterGirl girl) {
        return SESSIONS.containsKey(girl);
    }

    /** Which labor her body can do. Spiders crouch. Egg-layers lie prone. The rest sit, or the closest their body allows. */
    public static Kind kind(AbstractMonsterGirl girl) {
        GirlSpecies.Family family = girl.getSpecies().family();
        if (family == GirlSpecies.Family.ARACHNE) return Kind.SPIDER;
        if (family == GirlSpecies.Family.CENTAUR) return Kind.TAUR;
        if (family == GirlSpecies.Family.SLIME) return Kind.SLIME;
        if (laysEggs(girl)) return Kind.EGG;
        if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK) return Kind.TAIL;
        return Kind.SIT;
    }

    /** Called from her tick once she is ready, and every tick until the labor is over. */
    public static void tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (girl.isBaby() || girl.hasFlag(AbstractMonsterGirl.FLAG_DECOY)) {
            if (girl.getGestation().isReady()) instant(level, girl);
            return;
        }
        Integer wait = RETRY.get(girl);
        if (wait != null) {
            if (wait <= 1) RETRY.remove(girl);
            else RETRY.put(girl, wait - 1);
            if (!SESSIONS.containsKey(girl)) return;
        }
        Session session = SESSIONS.get(girl);
        if (session == null) {
            if (!girl.getGestation().isReady()) return;
            if (girl.getSpecies().family() == GirlSpecies.Family.CONSTRUCT) {
                instant(level, girl);
                return;
            }
            session = begin(level, girl);
        }
        step(level, girl, session);
    }

    /** Holds her on the way to a bed, and still while she is in the pose. The goal calls this. */
    public static void steer(AbstractMonsterGirl girl) {
        Session session = SESSIONS.get(girl);
        if (session == null) return;
        if (session.phase == Phase.SEEK && session.bed != null && girl.level().getBlockState(session.bed).getBlock() instanceof BedBlock) {
            Vec3 sit = sit(girl.level(), session.bed);
            if (girl.distanceToSqr(sit.x, sit.y, sit.z) > 2.25) {
                if (girl.getNavigation().isDone()) girl.getNavigation().moveTo(sit.x, sit.y, sit.z, 1.05);
                girl.getLookControl().setLookAt(sit.x, sit.y, sit.z);
                return;
            }
        }
        girl.getNavigation().stop();
    }

    private static void instant(ServerLevel level, AbstractMonsterGirl girl) {
        if (!girl.getGestation().isReady()) return;
        UUID father = girl.getGestation().getFather();
        if (!girl.giveBirth(level, father)) return;
        boolean twins = Cravings.twins(girl);
        if (twins) girl.giveBirth(level, father);
        done(level, girl, father, twins);
    }

    private static Session begin(ServerLevel level, AbstractMonsterGirl girl) {
        Session session = new Session();
        session.father = girl.getGestation().getFather();
        Kind kind = kind(girl);
        if (kind == Kind.EGG) {
            session.phase = waterEggs(girl) ? Phase.PUSH : Phase.BUILD;
        } else if (kind == Kind.SPIDER) {
            session.phase = Phase.POSE;
        } else {
            session.bed = findBed(level, girl.blockPosition());
            session.phase = session.bed == null ? Phase.POSE : Phase.SEEK;
        }
        SESSIONS.put(girl, session);
        return session;
    }

    private static void step(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        switch (session.phase) {
            case SEEK -> seek(girl, session);
            case BUILD -> build(level, girl, session);
            case POSE -> pose(level, girl, session);
            case PUSH -> push(level, girl, session);
        }
    }

    private static void seek(AbstractMonsterGirl girl, Session session) {
        session.age++;
        boolean gone = session.bed == null || !(girl.level().getBlockState(session.bed).getBlock() instanceof BedBlock);
        if (gone || session.age > SEEK_TICKS) {
            session.bed = null;
            session.phase = Phase.POSE;
            session.age = 0;
            return;
        }
        if (arrived(girl, session.bed)) {
            session.phase = Phase.POSE;
            session.age = 0;
        }
    }

    private static void build(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        if (session.age == 0) nest(level, girl);
        session.age++;
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        if (session.age % 6 == 0) {
            ItemStack twig = new ItemStack(girl.getRandom().nextBoolean() ? Items.STICK : Items.OAK_LEAVES);
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, twig), girl.getX(), girl.getY() + 0.15, girl.getZ(), 3, 0.35, 0.05, 0.35, 0.02);
            level.playSound(null, girl.blockPosition(), SoundEvents.GRASS_PLACE, SoundSource.NEUTRAL, 0.45F, 1.15F);
        }
        if (session.age >= BUILD_TICKS) {
            session.phase = Phase.PUSH;
            session.age = 0;
        }
    }

    private static void pose(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        session.age++;
        girl.setAction(AbstractMonsterGirl.Action.LABOR);
        pin(girl, session.bed);
        if (session.age < POSE_TICKS) return;
        UUID father = session.father;
        if (!girl.giveBirth(level, father)) {
            fail(girl);
            return;
        }
        boolean twins = Cravings.twins(girl);
        if (twins) girl.giveBirth(level, father);
        SESSIONS.remove(girl);
        done(level, girl, father, twins);
    }

    private static void push(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        session.age++;
        girl.setAction(AbstractMonsterGirl.Action.LABOR);
        pin(girl, null);
        if (session.clutch == null) {
            if (session.age < FIRST_PUSH) return;
            dropOne(level, girl, session);
            return;
        }
        if (session.age < PUSH_EVERY) return;
        session.age = 0;
        reveal(level, girl, session);
    }

    /** The first egg of this clutch. {@code giveBirth} lays the whole count; it is shown as one until she pushes the rest. */
    private static void dropOne(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        Set<BlockPos> before = eggsNear(level, girl.blockPosition());
        if (!girl.giveBirth(level, session.father)) {
            fail(girl);
            return;
        }
        if (!session.twinsDecided) {
            session.twinsDecided = true;
            session.twins = Cravings.twins(girl);
        }
        BlockPos neu = newest(level, girl.blockPosition(), before);
        if (neu == null || !(level.getBlockEntity(neu) instanceof GirlEggBlockEntity be)) {
            if (session.twins && !session.second) {
                session.second = true;
                girl.giveBirth(level, session.father);
            }
            SESSIONS.remove(girl);
            done(level, girl, session.father, session.twins);
            return;
        }
        session.target = Math.max(1, be.getCount());
        be.setCount(1);
        session.laid = 1;
        session.clutch = neu;
        session.age = 0;
        pop(level, girl, neu);
        if (session.laid >= session.target) nextClutch(level, girl, session);
    }

    private static void reveal(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        if (!(level.getBlockEntity(session.clutch) instanceof GirlEggBlockEntity be)) {
            SESSIONS.remove(girl);
            done(level, girl, session.father, session.twins);
            return;
        }
        if (session.laid < session.target) {
            session.laid++;
            be.setCount(session.laid);
            pop(level, girl, session.clutch);
        }
        if (session.laid >= session.target) nextClutch(level, girl, session);
    }

    /** A craving answered is a second clutch, pushed out the same way. */
    private static void nextClutch(ServerLevel level, AbstractMonsterGirl girl, Session session) {
        if (session.twins && !session.second) {
            session.second = true;
            session.clutch = null;
            session.laid = 0;
            session.target = 0;
            session.age = FIRST_PUSH - 1;   // the next tick bears the twin clutch down
            return;
        }
        SESSIONS.remove(girl);
        done(level, girl, session.father, session.twins);
    }

    private static void fail(AbstractMonsterGirl girl) {
        SESSIONS.remove(girl);
        if (girl.getAction() == AbstractMonsterGirl.Action.LABOR) girl.setAction(AbstractMonsterGirl.Action.NONE);
        RETRY.put(girl, RETRY_TICKS);
    }

    private static void done(ServerLevel level, AbstractMonsterGirl girl, @Nullable UUID father, boolean twins) {
        com.linkyoshi90.harmontide.entity.Marriage.announceBirth(level, girl, father, twins);
        girl.getGestation().clear();
        if (com.linkyoshi90.harmontide.entity.Monsterization.monsterized(girl)) girl.getGestation().halveCooldown();
        level.playSound(null, girl.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 0.8F, 1.3F);
        if (girl.getAction() == AbstractMonsterGirl.Action.LABOR) girl.setAction(AbstractMonsterGirl.Action.NONE);
    }

    private static void pop(ServerLevel level, AbstractMonsterGirl girl, BlockPos clutch) {
        Vec3 look = girl.getLookAngle();
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
                girl.getX() + look.x * 0.45, girl.getY() + 0.3, girl.getZ() + look.z * 0.45, 3, 0.08, 0.04, 0.08, 0.02);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
                clutch.getX() + 0.5, clutch.getY() + 0.25, clutch.getZ() + 0.5, 2, 0.12, 0.02, 0.12, 0.01);
        level.playSound(null, clutch, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.65F, 1.25F);
    }

    /** A low ring of leaves around her feet. The egg itself is still her family's to place. */
    private static void nest(ServerLevel level, AbstractMonsterGirl girl) {
        if (waterEggs(girl)) return;
        BlockPos o = girl.blockPosition();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (BlockPos side : new BlockPos[]{o.north(), o.south(), o.east(), o.west()}) {
            if (AbstractMonsterGirl.canHoldBlock(level, side)) level.setBlock(side, leaves, 3);
        }
    }

    private static void pin(AbstractMonsterGirl girl, @Nullable BlockPos bed) {
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        if (bed == null || !(girl.level().getBlockState(bed).getBlock() instanceof BedBlock)) return;
        Vec3 sit = sit(girl.level(), bed);
        float yaw = yaw(girl.level(), bed);
        girl.moveTo(sit.x, sit.y, sit.z, yaw, 0.0F);
        girl.setYBodyRot(yaw);
        girl.setYHeadRot(yaw);
    }

    private static boolean arrived(AbstractMonsterGirl girl, BlockPos bed) {
        Vec3 sit = sit(girl.level(), bed);
        return girl.distanceToSqr(sit.x, sit.y, sit.z) <= 2.25;
    }

    /** On the mattress, facing the foot, so a lean back puts her head toward the pillow. */
    private static Vec3 sit(Level level, BlockPos head) {
        Direction facing = facing(level, head);
        Direction foot = facing.getOpposite();
        Vec3 centre = Vec3.atBottomCenterOf(head);
        return new Vec3(centre.x + foot.getStepX() * 0.35, head.getY() + 0.56, centre.z + foot.getStepZ() * 0.35);
    }

    private static float yaw(Level level, BlockPos head) {
        return facing(level, head).getOpposite().toYRot();
    }

    private static Direction facing(Level level, BlockPos head) {
        BlockState state = level.getBlockState(head);
        return state.hasProperty(BedBlock.FACING) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
    }

    @Nullable
    private static BlockPos findBed(Level level, BlockPos origin) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-BED_RADIUS, -3, -BED_RADIUS), origin.offset(BED_RADIUS, 3, BED_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock) || !state.hasProperty(BedBlock.PART) || state.getValue(BedBlock.PART) != BedPart.HEAD) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static Set<BlockPos> eggsNear(ServerLevel level, BlockPos origin) {
        Set<BlockPos> found = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            if (level.getBlockState(pos).is(ModBlocks.GIRL_EGG.get())) found.add(pos.immutable());
        }
        return found;
    }

    @Nullable
    private static BlockPos newest(ServerLevel level, BlockPos origin, Set<BlockPos> before) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            if (!level.getBlockState(pos).is(ModBlocks.GIRL_EGG.get()) || before.contains(pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static boolean laysEggs(AbstractMonsterGirl girl) {
        GirlSpecies species = girl.getSpecies();
        return switch (species.family()) {
            case LIZARDFOLK, FLORA, CRUSTACEAN, MOLLUSK -> true;
            case LAMIA -> species == GirlSpecies.PYTHON_LAMIA || species == GirlSpecies.COBRA_LAMIA;
            case MERFOLK -> species == GirlSpecies.KOI_MERMAID || species == GirlSpecies.AXOLOTL_MERMAID;
            case BUG -> species == GirlSpecies.MOTH_BUG || species == GirlSpecies.PAPILLON_BUG;
            default -> false;
        };
    }

    /** Roe stays in the water. She does not heap a nest of leaves there. */
    private static boolean waterEggs(AbstractMonsterGirl girl) {
        GirlSpecies species = girl.getSpecies();
        if (species == GirlSpecies.KOI_MERMAID || species == GirlSpecies.AXOLOTL_MERMAID) return true;
        return species.family() == GirlSpecies.Family.MOLLUSK && species != GirlSpecies.SNAIL_MOLLUSK;
    }

    private static final class Session {
        private Phase phase = Phase.POSE;
        private int age;
        @Nullable
        private BlockPos bed;
        @Nullable
        private BlockPos clutch;
        private int laid;
        private int target;
        private boolean twins;
        private boolean twinsDecided;
        private boolean second;
        @Nullable
        private UUID father;
    }
}
