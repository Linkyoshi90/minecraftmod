package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Territories and scent marks (2026-11-13). A wild hunter (FoodWeb role HUNTER) claims a home range of {@link #RADIUS} blocks round
 * where she settles, and now and then patrols out to its edge to leave a scent mark there ({@link PatrolGoal}, a mark lasts
 * {@link #FRESH} ticks). Rivals of her own kind keep off: where one comes across a fresh mark that isn't hers she turns away and
 * makes her home somewhere else ({@link #yields}), so a land fills up with territories side by side instead of crowds.
 * <p>
 * People can't smell a thing, but a sharp nose can: with a befriended dog, cat, bear or snake kind within {@link #SNIFFER} of you,
 * the marks within {@link #SHOW} of you show as faint wisps in the colour of the kind that left them. Config {@code territories}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Territories extends SavedData {
    public static final int RADIUS = 24, FRESH = 48000, PATROL = 1200, KEPT = 2048, SHOW = 24;
    public static final double NOTICE = 8, SNIFFER = 16;
    private static final String HOME_X = "TerritoryX", HOME_Z = "TerritoryZ";

    public record Mark(BlockPos pos, String species, UUID owner, long time) {}

    private final List<Mark> marks = new ArrayList<>();

    public static Territories get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Territories::new, Territories::load, null), "harmontide_territories");
    }

    public List<Mark> marks() {
        return this.marks;
    }

    // --- who holds ground --------------------------------------------------------------------------------------------

    public static boolean holdsGround(AbstractMonsterGirl g) {
        return HarmontideConfig.territories() && FoodWeb.forages(g) && FoodWeb.role(g) == FoodWeb.Role.HUNTER;
    }

    /** Her home range's middle; claimed where she is the first time it's asked. */
    public static BlockPos home(AbstractMonsterGirl g) {
        CompoundTag pd = g.getPersistentData();
        if (!pd.contains(HOME_X)) setHome(g, g.blockPosition());
        return new BlockPos(pd.getInt(HOME_X), g.getBlockY(), pd.getInt(HOME_Z));
    }

    public static void setHome(AbstractMonsterGirl g, BlockPos at) {
        g.getPersistentData().putInt(HOME_X, at.getX());
        g.getPersistentData().putInt(HOME_Z, at.getZ());
    }

    /** She marks where she stands. */
    public static Mark mark(ServerLevel level, AbstractMonsterGirl g, BlockPos at) {
        Territories t = get(level);
        Mark m = new Mark(at.immutable(), g.getSpecies().id(), g.getUUID(), level.getGameTime());
        t.marks.removeIf(x -> x.owner().equals(g.getUUID()) && x.pos().closerThan(at, 4) || level.getGameTime() - x.time() > FRESH);
        t.marks.add(m);
        while (t.marks.size() > KEPT) t.marks.remove(0);
        t.setDirty();
        g.startTimedAction(AbstractMonsterGirl.Action.DIG, 30);               // a scrape and a rub
        return m;
    }

    /** A fresh mark near a spot left by a rival of her kind, or null. */
    @Nullable
    public static Mark rivalMark(ServerLevel level, AbstractMonsterGirl g, BlockPos near, double range) {
        for (Mark m : get(level).marks) {
            if (m.owner().equals(g.getUUID()) || !m.species().equals(g.getSpecies().id()) || level.getGameTime() - m.time() > FRESH) continue;
            if (m.pos().closerThan(near, range)) return m;
        }
        return null;
    }

    /**
     * She comes across a rival's fresh mark: she turns away and makes her home on the far side, out of the rival's range. True if she
     * yielded.
     */
    public static boolean yields(ServerLevel level, AbstractMonsterGirl g) {
        Mark m = rivalMark(level, g, g.blockPosition(), NOTICE);
        if (m == null) return false;
        double dx = g.getX() - m.pos().getX(), dz = g.getZ() - m.pos().getZ();
        double len = Math.max(0.5, Math.sqrt(dx * dx + dz * dz));
        BlockPos away = BlockPos.containing(g.getX() + dx / len * RADIUS * 1.5, g.getY(), g.getZ() + dz / len * RADIUS * 1.5);
        setHome(g, away);
        g.getNavigation().moveTo(away.getX() + 0.5, away.getY(), away.getZ() + 0.5, 1.0);
        return true;
    }

    // --- patrols -------------------------------------------------------------------------------------------------------

    /** Now and then out to the edge of her range, to mark it; and away, if she finds herself on a rival's ground. */
    public static final class PatrolGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable private BlockPos to;
        private int cooldown, ticks;

        public PatrolGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level) || !holdsGround(this.girl)) return false;
            this.cooldown = PATROL / 2 + this.girl.getRandom().nextInt(PATROL);
            if (yields(level, this.girl)) return false;
            BlockPos home = home(this.girl);
            double a = this.girl.getRandom().nextDouble() * Math.PI * 2;
            int x = home.getX() + (int) (Math.cos(a) * RADIUS), z = home.getZ() + (int) (Math.sin(a) * RADIUS);
            this.to = level.hasChunkAt(new BlockPos(x, 0, z)) ? level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)) : null;
            return this.to != null;
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 0.9);
        }

        @Override
        public boolean canContinueToUse() {
            return this.to != null && this.ticks < 600 && holdsGround(this.girl) && this.girl.getTarget() == null;
        }

        @Override
        public void tick() {
            if (++this.ticks % 20 != 0 || !(this.girl.level() instanceof ServerLevel level)) return;
            if (yields(level, this.girl)) {
                this.to = null;
                return;
            }
            if (this.girl.blockPosition().closerThan(this.to, 3) || this.girl.getNavigation().isDone()) {
                mark(level, this.girl, this.girl.blockPosition());
                this.to = null;
            } else if (this.ticks % 100 == 0) {
                this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 0.9);
            }
        }
    }

    // --- sharp noses ---------------------------------------------------------------------------------------------------

    /** A friend with a nose for it: a befriended dog, cat, bear or snake kind. */
    public static boolean sniffer(AbstractMonsterGirl g, ServerPlayer p) {
        Clade c = Clade.of(g);
        return c != null && (c == Clade.CANIDAE || c == Clade.FELIFORMIA || c == Clade.ARCTOIDEA || c == Clade.SQUAMATA)
                && g.getAffection(p) >= Orders.AFFECTION && !g.isBaby();
    }

    public static boolean smells(ServerLevel level, ServerPlayer p) {
        return !level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(SNIFFER), g -> sniffer(g, p)).isEmpty();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 40 != 21 || !(p.level() instanceof ServerLevel level) || !HarmontideConfig.territories()) return;
        List<Mark> marks = get(level).marks;
        if (marks.isEmpty() || !smells(level, p)) return;
        long now = level.getGameTime();
        for (Mark m : marks) {
            if (now - m.time() > FRESH || !m.pos().closerThan(p.blockPosition(), SHOW)) continue;
            var sp = com.linkyoshi90.harmontide.entity.girl.GirlSpecies.byId(m.species());
            int c = sp == null ? 0xB08A50 : sp.eggColor();
            float fade = 1.0F - (now - m.time()) / (float) FRESH;
            DustParticleOptions wisp = new DustParticleOptions(new Vector3f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F), 0.6F + 0.6F * fade);
            level.sendParticles(p, wisp, true, m.pos().getX() + 0.5, m.pos().getY() + 0.4, m.pos().getZ() + 0.5, 2 + (int) (4 * fade), 0.3, 0.3, 0.3, 0.0);
        }
    }

    // --- saving --------------------------------------------------------------------------------------------------------

    private static Territories load(CompoundTag tag, HolderLookup.Provider lookup) {
        Territories t = new Territories();
        for (Tag x : tag.getList("Marks", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) x;
            t.marks.add(new Mark(BlockPos.of(c.getLong("Pos")), c.getString("Species"), c.getUUID("Owner"), c.getLong("Time")));
        }
        return t;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (Mark m : this.marks) {
            CompoundTag c = new CompoundTag();
            c.putLong("Pos", m.pos().asLong());
            c.putString("Species", m.species());
            c.putUUID("Owner", m.owner());
            c.putLong("Time", m.time());
            list.add(c);
        }
        tag.put("Marks", list);
        return tag;
    }
}
