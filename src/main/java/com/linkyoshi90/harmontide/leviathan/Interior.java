package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.entity.Traversal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Inside the Dream-Leviathan (the harmontide:leviathan_interior dimension). Every landing gets its own string of rooms, laid out at
 * random like a roguelike dungeon from the visit's seed and built the first time someone climbs in, far from any other visit's rooms:
 * <ol>
 *   <li>the entry chamber behind her mouth (everyone arrives at the same spot; the way back out is in its wall),</li>
 *   <li>then five times a puzzle room followed by a fight against a wave of the infection (each puzzle kind once, in random order),</li>
 *   <li>and last the round chamber with her egg-cell hanging in the middle.</li>
 * </ol>
 * Two side rooms off the way hold what she swallowed. A sphincter of membrane closes each room's way on until it is solved or its wave
 * is beaten. The puzzles: strike her nerves as they light up ({@link Kind#NERVES}), slip through walls that clench with her heartbeat
 * ({@link Kind#HEARTBEAT}), cross the stomach acid on flesh raised by valves ({@link Kind#ACID}), cure infected cysts with antibodies
 * from a gland ({@link Kind#CYSTS}), and run over cilia that fold away under you ({@link Kind#CILIA}).
 * <p>
 * Dying in here, or using the way out, puts you outside beside her with everything you carried. If everyone leaves while a wave is
 * on, the wave starts over next time; beaten rooms stay beaten. Touching her egg-cell blesses everyone there (leviathan/DreamHeart)
 * and heals her (leviathan/LeviathanEvent). With the adult option on, that touch is a short press into the egg-cell, and the blessing
 * comes at the end of it.
 */
public class Interior extends SavedData {
    private static final String NAME = "harmontide_leviathan_interior";
    /** The floor height of every room, the distance between room centres, and between two visits' rooms. */
    public static final int FLOOR = 64, SPACING = 44, RUN_SPACING = 4096;
    public static final int WAVES = 5;

    public enum Kind { ENTRY, NERVES, HEARTBEAT, ACID, CYSTS, CILIA, WAVE, HEART, TREASURE }

    private final Map<Integer, RunState> runs = new HashMap<>();

    public static Interior get(MinecraftServer server) {
        ServerLevel level = server.getLevel(LeviathanRegistry.INTERIOR);
        if (level == null) level = server.overworld();
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Interior::new, Interior::load, null), NAME);
    }

    @Nullable
    public static ServerLevel level(MinecraftServer server) {
        return server.getLevel(LeviathanRegistry.INTERIOR);
    }

    /** Her insides' level, or (only in the gametest server, which loads no mod dimensions) the overworld, for the tests. */
    private static ServerLevel testLevel(MinecraftServer server) {
        ServerLevel level = level(server);
        return level != null ? level : server.overworld();
    }

    public static boolean inside(Level level) {
        return level.dimension() == LeviathanRegistry.INTERIOR;
    }

    // --- one visit's rooms ------------------------------------------------------------------------------------------------------

    /** What is kept of a visit: its seed, whether built, and which rooms are done. The layout and what is going on are rebuilt. */
    public static final class RunState {
        final int id;
        final long seed;
        boolean built;
        final Set<Integer> done = new HashSet<>();
        transient Layout layout;
        final transient Map<Integer, RoomState> live = new HashMap<>();

        RunState(int id, long seed) {
            this.id = id;
            this.seed = seed;
        }

        Layout layout() {
            if (this.layout == null) this.layout = Layout.make(this.id, this.seed);
            return this.layout;
        }

        public Layout layoutForTests() {
            return this.layout();
        }

        RoomState live(int room) {
            return this.live.computeIfAbsent(room, r -> new RoomState());
        }

        /** The first room on the way not yet done (the heart chamber at the end). */
        Room current() {
            for (Room r : this.layout().chain) if (!this.done.contains(r.index)) return r;
            return this.layout().chain.get(this.layout().chain.size() - 1);
        }
    }

    /** What is happening in a room right now (not saved). */
    static final class RoomState {
        boolean active;
        int step, pulses, total;
        long t0, lastSpawn, until1, until2;
        final List<BlockPos> order = new ArrayList<>();
        final Map<java.util.UUID, Integer> fallen = new HashMap<>();
        @Nullable
        ServerBossEvent bar;
    }

    /** One room of a visit: where it is, how big, which way the corridors leave it, and the parts of its puzzle. */
    public static final class Room {
        public final int index;
        public final Kind kind;
        public final int wave;
        public final BlockPos centre;
        public final int rx, ry, rz;
        @Nullable
        Direction in, out;
        final List<Direction> sides = new ArrayList<>();
        final List<BlockPos> door = new ArrayList<>();
        final List<BlockPos> nodes = new ArrayList<>(), cysts = new ArrayList<>(), seg1 = new ArrayList<>(), seg2 = new ArrayList<>();
        final List<List<BlockPos>> walls = new ArrayList<>();
        final List<BlockPos> spawns = new ArrayList<>();
        @Nullable
        BlockPos valve1, valve2, gland, chest;
        boolean learned;

        Room(int index, Kind kind, int wave, BlockPos centre, int rx, int ry, int rz) {
            this.index = index;
            this.kind = kind;
            this.wave = wave;
            this.centre = centre;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
        }

        boolean sphere() {
            return this.kind == Kind.HEART;
        }

        /** The heart chamber's middle (a sphere whose bottom is just under the floor). */
        Vec3 sphereCentre() {
            return new Vec3(this.centre.getX() + 0.5, FLOOR + this.ry - 2, this.centre.getZ() + 0.5);
        }

        /** Inside the room's open space (with some slack). */
        boolean contains(Vec3 p) {
            if (this.sphere()) return p.distanceTo(this.sphereCentre()) < this.ry + 0.8;
            double dx = (p.x - this.centre.getX() - 0.5) / (this.rx + 0.5), dz = (p.z - this.centre.getZ() - 0.5) / (this.rz + 0.5);
            double dy = Math.max(0, p.y - FLOOR) / (this.ry + 0.5);
            return dx * dx + dz * dz + dy * dy <= 1.0 && p.y > FLOOR - 9;
        }

        /** A point on the floor by the way in (or out), a little inside the room. */
        Vec3 edge(@Nullable Direction d) {
            if (d == null) return Vec3.atBottomCenterOf(this.centre);
            int r = d.getAxis() == Direction.Axis.X ? this.rx : this.rz;
            return Vec3.atBottomCenterOf(this.centre.relative(d, r - 2));
        }

        AABB box() {
            return new AABB(this.centre).inflate(this.rx + 2, this.ry + 10, this.rz + 2);
        }
    }

    /** A visit's rooms, laid out from its seed: the same every time it is worked out. */
    public static final class Layout {
        public final List<Room> chain = new ArrayList<>();
        public final List<Room> side = new ArrayList<>();
        public final BlockPos origin;
        final List<Room[]> corridors = new ArrayList<>();

        Layout(BlockPos origin) {
            this.origin = origin;
        }

        public List<Room> all() {
            List<Room> all = new ArrayList<>(this.chain);
            all.addAll(this.side);
            return all;
        }

        public BlockPos spawn() {
            return this.chain.get(0).centre;
        }

        static Layout make(int run, long seed) {
            Random rnd = new Random(seed);
            List<Kind> puzzles = new ArrayList<>(List.of(Kind.NERVES, Kind.HEARTBEAT, Kind.ACID, Kind.CYSTS, Kind.CILIA));
            Collections.shuffle(puzzles, rnd);
            List<Kind> kinds = new ArrayList<>();
            kinds.add(Kind.ENTRY);
            for (int w = 0; w < WAVES; w++) {
                kinds.add(puzzles.get(w));
                kinds.add(Kind.WAVE);
            }
            kinds.add(Kind.HEART);
            List<int[]> cells = null;
            for (int tries = 0; tries < 200 && cells == null; tries++) cells = walk(rnd, kinds.size());
            if (cells == null) {                                                       // never happens; a straight line will do
                cells = new ArrayList<>();
                for (int i = 0; i < kinds.size(); i++) cells.add(new int[]{i, 0});
            }
            Layout l = new Layout(new BlockPos(run * RUN_SPACING, FLOOR, 0));
            int wave = 0;
            for (int i = 0; i < kinds.size(); i++) {
                Kind k = kinds.get(i);
                if (k == Kind.WAVE) wave++;
                l.chain.add(l.room(i, k, k == Kind.WAVE ? wave : 0, cells.get(i), rnd));
            }
            for (int i = 0; i + 1 < l.chain.size(); i++) {
                Room a = l.chain.get(i), b = l.chain.get(i + 1);
                a.out = dir(cells.get(i), cells.get(i + 1));
                b.in = a.out.getOpposite();
                l.corridors.add(new Room[]{a, b});
            }
            Set<Long> used = new HashSet<>();
            for (int[] c : cells) used.add(key(c));
            for (int t = 0; t < 2; t++) {                                              // two side rooms with what she swallowed
                for (int tries = 0; tries < 30; tries++) {
                    int at = 2 + rnd.nextInt(kinds.size() - 4);
                    Direction d = Direction.from2DDataValue(rnd.nextInt(4));
                    int[] c = cells.get(at), n = {c[0] + d.getStepX(), c[1] + d.getStepZ()};
                    if (used.contains(key(n))) continue;
                    used.add(key(n));
                    Room side = l.room(100 + t, Kind.TREASURE, 0, n, rnd);
                    side.in = d.getOpposite();
                    l.side.add(side);
                    l.chain.get(at).sides.add(d);
                    l.corridors.add(new Room[]{l.chain.get(at), side});
                    break;
                }
            }
            for (Room r : l.all()) l.plan(r, rnd);
            return l;
        }

        private static long key(int[] c) {
            return ((long) c[0] << 32) ^ (c[1] & 0xffffffffL);
        }

        private static Direction dir(int[] a, int[] b) {
            return b[0] > a[0] ? Direction.EAST : b[0] < a[0] ? Direction.WEST : b[1] > a[1] ? Direction.SOUTH : Direction.NORTH;
        }

        /** A self-avoiding walk on the room grid, or null when it walked itself into a corner. */
        @Nullable
        private static List<int[]> walk(Random rnd, int n) {
            List<int[]> cells = new ArrayList<>();
            Set<Long> used = new HashSet<>();
            int[] at = {0, 0};
            cells.add(at);
            used.add(key(at));
            while (cells.size() < n) {
                List<int[]> next = new ArrayList<>();
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    int[] c = {at[0] + d.getStepX(), at[1] + d.getStepZ()};
                    if (!used.contains(key(c)) && c[0] >= 0) next.add(c);
                }
                if (next.isEmpty()) return null;
                at = next.get(rnd.nextInt(next.size()));
                cells.add(at);
                used.add(key(at));
            }
            return cells;
        }

        private Room room(int index, Kind k, int wave, int[] cell, Random rnd) {
            BlockPos c = this.origin.offset(cell[0] * SPACING, 0, cell[1] * SPACING);
            return switch (k) {
                case ENTRY -> new Room(index, k, wave, c, 8, 6, 8);
                case TREASURE -> new Room(index, k, wave, c, 6, 5, 6);
                case HEART -> new Room(index, k, wave, c, 12, 12, 12);
                case WAVE -> new Room(index, k, wave, c, 13 + rnd.nextInt(2), 9, 13 + rnd.nextInt(2));
                default -> new Room(index, k, wave, c, 12, 8, 12);
            };
        }

        /** Works out where a room's puzzle parts and spawn spots go (the blocks are placed by {@link #build}). */
        private void plan(Room r, Random rnd) {
            Vec3 e = r.edge(r.in), x = r.edge(r.out);
            switch (r.kind) {
                case WAVE -> {
                    for (int i = 0; i < 40 && r.spawns.size() < 10; i++) {
                        BlockPos p = r.centre.offset(rnd.nextInt(r.rx * 2 - 5) - r.rx + 3, 0, rnd.nextInt(r.rz * 2 - 5) - r.rz + 3);
                        if (Vec3.atBottomCenterOf(p).distanceTo(e) > 7 && r.contains(Vec3.atBottomCenterOf(p).add(0, 1, 0))) r.spawns.add(p);
                    }
                }
                case ACID -> {
                    Vec3 mid = Vec3.atBottomCenterOf(r.centre);
                    stones(e, mid, r.seg1, e, x, mid);
                    stones(mid, x, r.seg2, e, x, mid);
                    Vec3 toMid = mid.subtract(e).normalize();
                    r.valve1 = BlockPos.containing(e.add(toMid.scale(-0.5)).add(new Vec3(-toMid.z, 0, toMid.x).scale(2)));
                    r.valve2 = r.centre.relative(r.out == null ? Direction.NORTH : r.out.getClockWise(), 1);
                }
                case CYSTS -> r.gland = BlockPos.containing(e.add(new Vec3(r.in == null ? 1 : r.in.getClockWise().getStepX() * 2, 0,
                        r.in == null ? 0 : r.in.getClockWise().getStepZ() * 2)));
                case TREASURE -> r.chest = r.centre;
                default -> {
                }
            }
        }

        /** Stepping stones from a to b: every other block on the line, leaving a gap to jump. */
        private static void stones(Vec3 a, Vec3 b, List<BlockPos> out, Vec3 e, Vec3 x, Vec3 mid) {
            Vec3 d = b.subtract(a);
            int n = (int) Math.ceil(d.horizontalDistance());
            for (int i = 2; i < n - 1; i += 2) {
                Vec3 p = a.add(d.scale(i / (double) n));
                BlockPos at = new BlockPos(Mth.floor(p.x), FLOOR - 1, Mth.floor(p.z));
                Vec3 c = new Vec3(at.getX() + 0.5, FLOOR, at.getZ() + 0.5);
                if (c.distanceTo(e) < 3.6 || c.distanceTo(x) < 3.6 || c.distanceTo(mid) < 2.6) continue;    // ledge or island already
                if (!out.contains(at)) out.add(at);
            }
        }
    }

    // --- building --------------------------------------------------------------------------------------------------------------

    private static BlockState flesh(int x, int y, int z) {
        double v = Math.sin(x * 0.31 + Math.sin(z * 0.23) * 2.1) * Math.sin(y * 0.42 + x * 0.11 + Math.cos(z * 0.17) * 1.7);
        if (Math.abs(v) < 0.06) return LeviathanRegistry.VEIN.get().defaultBlockState();
        double d = Math.sin(x * 0.09 + y * 0.13) + Math.cos(z * 0.11 - y * 0.07);
        return (d > 0.9 ? LeviathanRegistry.DENSE_FLESH : LeviathanRegistry.FLESH).get().defaultBlockState();
    }

    private static boolean innerHas(Room r, int x, int y, int z) {
        if (r.sphere()) return r.sphereCentre().distanceTo(new Vec3(x + 0.5, y + 0.5, z + 0.5)) < r.ry;
        if (y < FLOOR) return false;
        double dx = (x - r.centre.getX()) / (double) r.rx, dz = (z - r.centre.getZ()) / (double) r.rz, dy = (y - FLOOR) / (double) r.ry;
        return dx * dx + dz * dz + dy * dy < 1.0;
    }

    private static boolean outerHas(Room r, int x, int y, int z) {
        if (r.sphere()) return r.sphereCentre().distanceTo(new Vec3(x + 0.5, y + 0.5, z + 0.5)) < r.ry + 2.2;
        if (y < FLOOR - 3) return false;
        double dx = (x - r.centre.getX()) / (r.rx + 2.2), dz = (z - r.centre.getZ()) / (r.rz + 2.2), dy = Math.max(0, y - FLOOR) / (r.ry + 2.2);
        return dx * dx + dz * dz + dy * dy < 1.0;
    }

    private static void set(ServerLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, 2);
    }

    /** Builds a visit's rooms (walls, floors, corridors, doors and every puzzle). */
    public static void build(ServerLevel level, RunState run) {
        Layout l = run.layout();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (Room r : l.all()) {                                                       // the flesh around each room
            int ext = r.sphere() ? r.ry + 3 : Math.max(r.rx, r.rz) + 3;
            int y0 = r.sphere() ? (int) r.sphereCentre().y - r.ry - 3 : FLOOR - 3, y1 = r.sphere() ? (int) r.sphereCentre().y + r.ry + 3 : FLOOR + r.ry + 3;
            for (int x = r.centre.getX() - ext; x <= r.centre.getX() + ext; x++) {
                for (int z = r.centre.getZ() - ext; z <= r.centre.getZ() + ext; z++) {
                    for (int y = y0; y <= y1; y++) {
                        if (outerHas(r, x, y, z) && !innerHas(r, x, y, z)) set(level, m.set(x, y, z), flesh(x, y, z));
                    }
                }
            }
        }
        for (Room[] c : l.corridors) corridor(level, l, c[0], c[1], true);
        for (Room[] c : l.corridors) corridor(level, l, c[0], c[1], false);
        for (Room r : l.all()) {                                                       // clear anything a corridor's wall put inside a room
            int ext = r.sphere() ? r.ry : Math.max(r.rx, r.rz);
            int y0 = r.sphere() ? (int) r.sphereCentre().y - r.ry : FLOOR, y1 = r.sphere() ? (int) r.sphereCentre().y + r.ry : FLOOR + r.ry;
            for (int x = r.centre.getX() - ext; x <= r.centre.getX() + ext; x++) {
                for (int z = r.centre.getZ() - ext; z <= r.centre.getZ() + ext; z++) {
                    for (int y = y0; y <= y1; y++) {
                        if (innerHas(r, x, y, z) && !level.getBlockState(m.set(x, y, z)).isAir()) set(level, m, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int i = 0; i < l.corridors.size(); i++) {
            Room[] c = l.corridors.get(i);
            if (c[1].kind != Kind.TREASURE) door(level, c[0], c[1]);
        }
        for (Room r : l.all()) features(level, run, r);
        for (Room r : l.chain) if (run.done.contains(r.index)) openDoor(level, r, false);
        run.built = true;
    }

    /** A corridor from one room's middle to the next: first its walls (where nothing is yet), then its open inside. */
    private static void corridor(ServerLevel level, Layout l, Room a, Room b, boolean walls) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        Direction d = a.out != null && a.centre.relative(a.out, SPACING).equals(b.centre) ? a.out
                : Direction.getNearest(b.centre.getX() - a.centre.getX(), 0, b.centre.getZ() - a.centre.getZ());
        Direction across = d.getClockWise();
        for (int s = 0; s <= SPACING; s++) {
            BlockPos at = a.centre.relative(d, s);
            for (int w = -4; w <= 4; w++) {
                for (int h = -3; h <= 5; h++) {
                    int x = at.getX() + across.getStepX() * w, z = at.getZ() + across.getStepZ() * w, y = FLOOR + h;
                    boolean open = Math.abs(w) <= 2 && h >= 0 && h <= 3 && !(Math.abs(w) == 2 && h == 3);
                    if (walls) {
                        if (!open && level.getBlockState(m.set(x, y, z)).isAir() && !innerAny(l, x, y, z)) set(level, m, flesh(x, y, z));
                    } else if (open) {
                        if (!level.getBlockState(m.set(x, y, z)).isAir()) set(level, m, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    private static boolean innerAny(Layout l, int x, int y, int z) {
        for (Room r : l.all()) if (innerHas(r, x, y, z)) return true;
        return false;
    }

    /** The sphincter between a room and the next, just outside the first room's wall. */
    private static void door(ServerLevel level, Room a, Room b) {
        Direction d = a.out;
        if (d == null) return;
        Direction across = d.getClockWise();
        int r = d.getAxis() == Direction.Axis.X ? a.rx : a.rz;
        BlockPos at = a.centre.relative(d, r + 3);
        a.door.clear();
        for (int w = -2; w <= 2; w++) {
            for (int h = 0; h <= 3; h++) {
                if (Math.abs(w) == 2 && h == 3) continue;
                BlockPos p = at.offset(across.getStepX() * w, h, across.getStepZ() * w).atY(FLOOR + h);
                a.door.add(p);
                set(level, p, LeviathanRegistry.MEMBRANE.get().defaultBlockState());
            }
        }
    }

    /** Works out the door cells again without building (after a restart the layout is new but the blocks are there). */
    private static void doorCells(Room a) {
        if (a.out == null || !a.door.isEmpty()) return;
        Direction across = a.out.getClockWise();
        int r = a.out.getAxis() == Direction.Axis.X ? a.rx : a.rz;
        BlockPos at = a.centre.relative(a.out, r + 3);
        for (int w = -2; w <= 2; w++) {
            for (int h = 0; h <= 3; h++) {
                if (Math.abs(w) == 2 && h == 3) continue;
                a.door.add(at.offset(across.getStepX() * w, 0, across.getStepZ() * w).atY(FLOOR + h));
            }
        }
    }

    private static void openDoor(ServerLevel level, Room r, boolean sound) {
        doorCells(r);
        for (BlockPos p : r.door) {
            BlockState s = level.getBlockState(p);
            if (s.is(LeviathanRegistry.MEMBRANE.get())) set(level, p, s.setValue(LeviathanBlocks.OPEN, true));
        }
        if (sound && !r.door.isEmpty()) {
            BlockPos p = r.door.get(r.door.size() / 2);
            level.playSound(null, p, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 2.0F, 0.5F);
            level.playSound(null, p, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0F, 0.8F);
        }
    }

    /** The first solid block from the room's middle outward along a direction, at a height: where a thing set in the wall goes. */
    @Nullable
    private static BlockPos wall(ServerLevel level, Room r, double angle, int height) {
        Vec3 from = Vec3.atBottomCenterOf(r.centre).add(0, height, 0);
        for (double t = 1; t < Math.max(r.rx, r.rz) + 4; t += 0.5) {
            BlockPos p = BlockPos.containing(from.x + Math.cos(angle) * t, from.y, from.z + Math.sin(angle) * t);
            if (!level.getBlockState(p).isAir()) return p;
        }
        return null;
    }

    private static double angleOf(@Nullable Direction d) {
        return d == null ? 0 : Math.atan2(d.getStepZ(), d.getStepX());
    }

    /** Is an angle far enough from the ways in and out (and the side rooms) to put something in the wall there? */
    private static boolean clearOf(Room r, double a) {
        List<Direction> ways = new ArrayList<>(r.sides);
        if (r.in != null) ways.add(r.in);
        if (r.out != null) ways.add(r.out);
        for (Direction d : ways) if (Math.abs(Mth.wrapDegrees(Math.toDegrees(a - angleOf(d)))) < 32) return false;
        return true;
    }

    private static void features(ServerLevel level, RunState run, Room r) {
        Random rnd = new Random(run.seed * 31 + r.index);
        Vec3 e = r.edge(r.in), x = r.edge(r.out);
        switch (r.kind) {
            case ENTRY -> {
                Direction back = r.out == null ? Direction.WEST : r.out.getOpposite();
                BlockPos w = wall(level, r, angleOf(back), 1);
                if (w != null) {
                    Direction across = back.getClockWise();
                    for (int a = -1; a <= 1; a++) for (int h = 0; h <= 2; h++) {
                        set(level, w.offset(across.getStepX() * a, h - 1, across.getStepZ() * a), LeviathanRegistry.EXIT.get().defaultBlockState());
                    }
                }
                if (!LeviathanEvent.nsfwEntry()) {                                    // baleen plates hanging round her mouth's inside
                    for (double a = 0; a < Math.PI * 2; a += 0.22) {
                        if (Math.abs(Mth.wrapDegrees(Math.toDegrees(a - angleOf(back)))) > 70) continue;
                        for (int h = 2; h <= 6; h++) {
                            BlockPos p = wall(level, r, a, h);
                            if (p != null) {
                                BlockPos in = BlockPos.containing(Vec3.atCenterOf(p).subtract(Math.cos(a), 0, Math.sin(a)));
                                if (level.getBlockState(in).isAir() && h >= 4) set(level, in, LeviathanRegistry.BALEEN.get().defaultBlockState());
                            }
                        }
                    }
                }
            }
            case NERVES -> {
                r.nodes.clear();
                for (int i = 0; i < 40 && r.nodes.size() < 4; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2;
                    if (!clearOf(r, a)) continue;
                    BlockPos p = wall(level, r, a, 2 + rnd.nextInt(4));
                    if (p == null) continue;
                    boolean near = false;
                    for (BlockPos o : r.nodes) near |= o.distSqr(p) < 25;
                    if (near) continue;
                    r.nodes.add(p);
                    set(level, p, LeviathanRegistry.NERVE_NODE.get().defaultBlockState());
                }
                pillars(level, r, rnd, 2, e, x);
            }
            case HEARTBEAT -> {
                r.walls.clear();
                Vec3 axis = x.subtract(e);
                double len = axis.horizontalDistance();
                Vec3 u = new Vec3(axis.x / len, 0, axis.z / len);
                for (double f : new double[]{0.3, 0.5, 0.7}) {
                    List<BlockPos> wall = new ArrayList<>();
                    double k = len * f;
                    for (BlockPos p : BlockPos.betweenClosed(r.centre.offset(-r.rx, 0, -r.rz), r.centre.offset(r.rx, r.ry, r.rz))) {
                        double dot = (p.getX() + 0.5 - e.x) * u.x + (p.getZ() + 0.5 - e.z) * u.z;
                        if (Math.abs(dot - k) < 0.75 && innerHas(r, p.getX(), p.getY(), p.getZ())) {
                            wall.add(p.immutable());
                            set(level, p, LeviathanRegistry.PULSE_WALL.get().defaultBlockState());
                        }
                    }
                    r.walls.add(wall);
                }
            }
            case ACID -> {
                Vec3 mid = Vec3.atBottomCenterOf(r.centre);
                for (BlockPos p : BlockPos.betweenClosed(r.centre.offset(-r.rx, -1, -r.rz), r.centre.offset(r.rx, -1, r.rz))) {
                    Vec3 c = Vec3.atBottomCenterOf(p).add(0, 1, 0);
                    if (!innerHas(r, p.getX(), FLOOR, p.getZ())) continue;
                    if (c.distanceTo(e.add(0, 0, 0)) < 3.6 || c.distanceTo(x) < 3.6 || c.distanceTo(mid.add(0, 0, 0)) < 2.6) continue;
                    set(level, p, LeviathanRegistry.ACID.get().defaultBlockState());
                }
                if (r.valve1 != null) set(level, r.valve1, LeviathanRegistry.VALVE.get().defaultBlockState());
                if (r.valve2 != null) set(level, r.valve2, LeviathanRegistry.VALVE.get().defaultBlockState());
            }
            case CYSTS -> {
                r.cysts.clear();
                for (int i = 0; i < 40 && r.cysts.size() < 3; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2;
                    if (!clearOf(r, a)) continue;
                    BlockPos p = wall(level, r, a, 1 + rnd.nextInt(2));
                    if (p == null) continue;
                    boolean near = false;
                    for (BlockPos o : r.cysts) near |= o.distSqr(p) < 36;
                    if (near) continue;
                    r.cysts.add(p);
                    set(level, p, LeviathanRegistry.CYST.get().defaultBlockState());
                }
                if (r.gland != null) set(level, r.gland, LeviathanRegistry.GLAND.get().defaultBlockState());
            }
            case CILIA -> {
                for (BlockPos p : BlockPos.betweenClosed(r.centre.offset(-r.rx - 2, -9, -r.rz - 2), r.centre.offset(r.rx + 2, -1, r.rz + 2))) {
                    double dx = (p.getX() - r.centre.getX()) / (double) r.rx, dz = (p.getZ() - r.centre.getZ()) / (double) r.rz;
                    double f = dx * dx + dz * dz;
                    if (f >= 1.25) continue;
                    Vec3 c = new Vec3(p.getX() + 0.5, FLOOR, p.getZ() + 0.5);
                    boolean pit = f < 0.85 && c.distanceTo(e) >= 3.6 && c.distanceTo(x) >= 3.6;
                    int y = p.getY();
                    if (pit) {
                        set(level, p, y <= FLOOR - 8 ? LeviathanRegistry.DENSE_FLESH.get().defaultBlockState()
                                : y == FLOOR - 7 ? LeviathanRegistry.ACID.get().defaultBlockState() : Blocks.AIR.defaultBlockState());
                    } else if (y < FLOOR - 3) {
                        set(level, p, flesh(p.getX(), y, p.getZ()));                   // the pit's walls below the old floor
                    }
                }
                Vec3 d = x.subtract(e);
                double len = d.horizontalDistance();
                Vec3 side = new Vec3(-d.z / len, 0, d.x / len);
                double phase = rnd.nextDouble() * 6;
                for (double t = 0; t <= len; t += 0.4) {
                    Vec3 p = e.add(d.scale(t / len)).add(side.scale(Math.sin(t * 0.45 + phase) * 4.0));
                    BlockPos at = new BlockPos(Mth.floor(p.x), FLOOR - 1, Mth.floor(p.z));
                    if (level.getBlockState(at).isAir()) set(level, at, LeviathanRegistry.CILIA.get().defaultBlockState());
                }
            }
            case WAVE -> pillars(level, r, rnd, 3, e, x);
            case HEART -> {
                Vec3 c = new Vec3(r.centre.getX() + 0.5, FLOOR + 3.5, r.centre.getZ() + 0.5);
                for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(c).offset(-3, -3, -3), BlockPos.containing(c).offset(3, 3, 3))) {
                    if (Vec3.atCenterOf(p).distanceTo(c) < 2.7) set(level, p, LeviathanRegistry.HEART.get().defaultBlockState());
                }
                Vec3 top = r.sphereCentre().add(0, r.ry - 0.5, 0);
                for (int k = 0; k < 5; k++) {                                           // veins holding her heart up
                    double a = k * Math.PI * 2 / 5;
                    Vec3 from = c.add(Math.cos(a) * 2.2, 1.8, Math.sin(a) * 2.2), to = top.add(Math.cos(a) * 5, 0, Math.sin(a) * 5);
                    for (double t = 0; t <= 1; t += 0.04) {
                        BlockPos p = BlockPos.containing(from.lerp(to, t));
                        if (level.getBlockState(p).isAir()) set(level, p, LeviathanRegistry.VEIN.get().defaultBlockState());
                    }
                }
            }
            case TREASURE -> {
                if (r.chest != null) {
                    set(level, r.chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, r.in == null ? Direction.NORTH : r.in));
                    if (level.getBlockEntity(r.chest) instanceof ChestBlockEntity chest) {
                        ResourceKey<LootTable> table = r.index % 2 == 0 ? net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_TREASURE
                                : net.minecraft.world.level.storage.loot.BuiltInLootTables.BURIED_TREASURE;
                        chest.setLootTable(table, rnd.nextLong());
                    }
                }
                for (int i = 0; i < 6; i++) {
                    BlockPos p = r.centre.offset(rnd.nextInt(7) - 3, 0, rnd.nextInt(7) - 3);
                    if (!p.equals(r.chest) && level.getBlockState(p).isAir()) set(level, p, (rnd.nextBoolean() ? Blocks.BONE_BLOCK : LeviathanRegistry.BALEEN.get()).defaultBlockState());
                }
            }
        }
    }

    /** Fleshy columns floor to ceiling for cover, away from the ways in and out. */
    private static void pillars(ServerLevel level, Room r, Random rnd, int n, Vec3 e, Vec3 x) {
        for (int i = 0, made = 0; i < 30 && made < n; i++) {
            BlockPos p = r.centre.offset(rnd.nextInt(r.rx) - r.rx / 2, 0, rnd.nextInt(r.rz) - r.rz / 2);
            Vec3 c = Vec3.atBottomCenterOf(p);
            if (c.distanceTo(e) < 5 || c.distanceTo(x) < 5 || c.distanceTo(Vec3.atBottomCenterOf(r.centre)) < 3) continue;
            made++;
            for (int y = FLOOR; y < FLOOR + r.ry + 1; y++) {
                for (int ox = 0; ox <= 1; ox++) for (int oz = 0; oz <= 1; oz++) {
                    BlockPos q = new BlockPos(p.getX() + ox, y, p.getZ() + oz);
                    if (level.getBlockState(q).isAir()) set(level, q, flesh(q.getX(), q.getY(), q.getZ()));
                }
            }
        }
    }

    /** Puzzle parts placed while building are found again here after a restart (the layout knows where to look). */
    private static void relearn(ServerLevel level, Room r) {
        if (r.learned) return;
        r.learned = true;
        if (r.kind == Kind.NERVES && r.nodes.isEmpty()) collect(level, r, LeviathanRegistry.NERVE_NODE.get(), r.nodes);
        if (r.kind == Kind.CYSTS && r.cysts.isEmpty()) collect(level, r, LeviathanRegistry.CYST.get(), r.cysts);
        if (r.kind == Kind.HEARTBEAT && r.walls.isEmpty()) {
            List<BlockPos> all = new ArrayList<>();
            collect(level, r, LeviathanRegistry.PULSE_WALL.get(), all);
            Vec3 e = r.edge(r.in), x = r.edge(r.out), axis = x.subtract(e);
            double len = axis.horizontalDistance();
            for (double f : new double[]{0.3, 0.5, 0.7}) {
                List<BlockPos> wall = new ArrayList<>();
                for (BlockPos p : all) {
                    double dot = ((p.getX() + 0.5 - e.x) * axis.x + (p.getZ() + 0.5 - e.z) * axis.z) / len;
                    if (Math.abs(dot - len * f) < 0.75) wall.add(p);
                }
                r.walls.add(wall);
            }
        }
    }

    private static void collect(ServerLevel level, Room r, net.minecraft.world.level.block.Block block, List<BlockPos> out) {
        for (BlockPos p : BlockPos.betweenClosed(r.centre.offset(-r.rx - 3, -2, -r.rz - 3), r.centre.offset(r.rx + 3, r.ry + 3, r.rz + 3))) {
            if (level.getBlockState(p).is(block)) out.add(p.immutable());
        }
    }

    // --- coming and going --------------------------------------------------------------------------------------------------------

    @Nullable
    public RunState run(int id) {
        return this.runs.get(id);
    }

    private RunState runFor(int id) {
        return this.runs.computeIfAbsent(id, i -> {
            this.setDirty();
            return new RunState(i, new Random(i * 7919L + System.nanoTime()).nextLong());
        });
    }

    /** Climbing in: builds this visit's rooms the first time, and puts the player in the entry chamber (the same spot for everyone). */
    public static void enter(ServerPlayer p, LeviathanEvent event) {
        ServerLevel level = level(p.server);
        if (level == null) return;
        Interior in = get(p.server);
        RunState run = in.runFor(event.run);
        if (!run.built) {
            build(level, run);
            in.setDirty();
        }
        Room entry = run.layout().chain.get(0);
        BlockPos at = entry.centre;
        float yaw = entry.out == null ? 0 : entry.out.toYRot();
        p.resetFallDistance();
        p.teleportTo(level, at.getX() + 0.5, FLOOR, at.getZ() + 0.5, yaw, 0);
        p.displayClientMessage(Component.translatable(LeviathanEvent.nsfwEntry() ? "message.harmontide.leviathan.entered_rear"
                : "message.harmontide.leviathan.entered_mouth").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 1.5F, 0.7F);
        Traversal.award(p, "inside_the_dream");
    }

    /** Puts the player back outside, beside her (or where she lay), with everything they carried. */
    public static void leave(ServerPlayer p, String key) {
        if (HUMPS.remove(p.getUUID()) != null) release(p);
        ServerLevel over = p.server.overworld();
        Vec3 out = LeviathanEvent.get(over).exitPoint(over);
        if (out == null) {
            BlockPos s = p.getRespawnPosition() != null && p.getRespawnDimension() == Level.OVERWORLD ? p.getRespawnPosition() : over.getSharedSpawnPos();
            out = Vec3.atBottomCenterOf(over.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s));
        }
        p.resetFallDistance();
        p.teleportTo(over, out.x, out.y, out.z, p.getYRot(), 0);
        p.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    /** Everyone inside her is put back outside (she died, or she is healed and rising). */
    public static void expelAll(MinecraftServer server, int run, String key) {
        ServerLevel level = level(server);
        if (level == null) return;
        for (ServerPlayer p : new ArrayList<>(level.players())) leave(p, key);
        Interior in = get(server);
        RunState rs = in.run(run);
        if (rs != null) {
            for (Room r : rs.layout().all()) reset(level, rs, r);
        }
    }

    /** Which visit's rooms a position is among (by where it is), or null. */
    @Nullable
    private RunState runAt(Vec3 p) {
        int id = Math.floorDiv((int) Math.floor(p.x) + RUN_SPACING / 4, RUN_SPACING);
        return this.runs.get(id);
    }

    private List<ServerPlayer> playersIn(ServerLevel level, RunState run) {
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer p : level.players()) if (this.runAt(p.position()) == run && !p.isSpectator()) out.add(p);
        return out;
    }

    // --- running the rooms ---------------------------------------------------------------------------------------------------------

    public static void tick(MinecraftServer server) {
        ServerLevel level = level(server);
        if (level == null) return;
        Interior in = get(server);
        LeviathanEvent event = LeviathanEvent.get(server.overworld());
        for (ServerPlayer p : new ArrayList<>(level.players())) {                     // nobody stays inside a visit that is over
            RunState rs = in.runAt(p.position());
            if (!p.isCreative() && !p.isSpectator() && (event.phase != LeviathanEvent.LANDED || rs == null || rs.id != event.run)) {
                leave(p, "message.harmontide.leviathan.left");
            }
        }
        for (RunState run : in.runs.values()) {
            if (!run.built) continue;
            List<ServerPlayer> players = in.playersIn(level, run);
            Room r = run.current();
            relearn(level, r);
            RoomState s = run.live(r.index);
            if (players.isEmpty()) {
                if (s.active) reset(level, run, r);
                continue;
            }
            if (!s.active) {
                boolean someone = false;
                for (ServerPlayer p : players) someone |= r.contains(p.position());
                if (!someone && r.kind != Kind.ENTRY) continue;
                s.active = true;
                s.t0 = level.getGameTime();
                start(level, run, r, s, players);
            }
            switch (r.kind) {
                case ENTRY -> finish(level, in, run, r, players);
                case WAVE -> tickWave(level, in, run, r, s, players);
                case NERVES -> {
                    if (level.getGameTime() - s.t0 > 160 && s.step > 0) restartNerves(level, r, s, true);
                }
                case HEARTBEAT -> tickHeartbeat(level, in, run, r, s, players);
                case ACID -> tickAcid(level, in, run, r, s, players);
                case CYSTS -> tickCysts(level, r, s, players);
                case CILIA -> tickCilia(level, in, run, r, s, players);
                default -> {
                }
            }
        }
    }

    private static void start(ServerLevel level, RunState run, Room r, RoomState s, List<ServerPlayer> players) {
        switch (r.kind) {
            case WAVE -> {
                s.total = Math.min(26, 4 + 2 * r.wave + 2 * (players.size() - 1)) + (r.wave == WAVES ? 1 : 0);
                s.pulses = 0;
                s.lastSpawn = level.getGameTime() - 1000;
                s.bar = new ServerBossEvent(Component.translatable("boss.harmontide.leviathan_wave", r.wave, WAVES),
                        BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
                tell(players, "message.harmontide.leviathan.wave_start", r.wave, WAVES);
            }
            case NERVES -> restartNerves(level, r, s, false);
            case CYSTS -> tell(players, "message.harmontide.leviathan.cysts_hint");
            case ACID -> tell(players, "message.harmontide.leviathan.acid_hint");
            case HEARTBEAT -> tell(players, "message.harmontide.leviathan.heartbeat_hint");
            case CILIA -> tell(players, "message.harmontide.leviathan.cilia_hint");
            case HEART -> tell(players, LeviathanEvent.nsfwEntry()
                    ? "message.harmontide.leviathan.egg_hint" : "message.harmontide.leviathan.heart_hint");
            default -> {
            }
        }
    }

    private static void tell(List<ServerPlayer> players, String key, Object... args) {
        for (ServerPlayer p : players) p.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    /** A room is solved or its wave beaten: its way on opens. */
    private static void finish(ServerLevel level, Interior in, RunState run, Room r, List<ServerPlayer> players) {
        if (!run.done.add(r.index)) return;
        RoomState s = run.live(r.index);
        s.active = false;
        if (s.bar != null) {
            s.bar.removeAllPlayers();
            s.bar = null;
        }
        openDoor(level, r, r.kind != Kind.ENTRY);
        in.setDirty();
        if (r.kind == Kind.WAVE) {
            LeviathanEvent.get(level.getServer().overworld()).setProgress(r.wave);
            tell(players, "message.harmontide.leviathan.wave_done", r.wave, WAVES);
        } else if (r.kind != Kind.ENTRY) {
            tell(players, "message.harmontide.leviathan.solved");
        }
        if (r.kind == Kind.HEARTBEAT) for (List<BlockPos> w : r.walls) setAll(level, w, true);
        if (r.kind == Kind.ACID) {
            raise(level, r.seg1, true);
            raise(level, r.seg2, true);
        }
    }

    /** Everyone left while a room was going: its wave starts over next time, its puzzle is set back. */
    private static void reset(ServerLevel level, RunState run, Room r) {
        RoomState s = run.live(r.index);
        s.active = false;
        if (s.bar != null) {
            s.bar.removeAllPlayers();
            s.bar = null;
        }
        if (run.done.contains(r.index)) return;
        String tag = tag(run, r);
        for (Mob m : level.getEntitiesOfClass(Mob.class, r.box().inflate(32), m -> m.getTags().contains(tag))) m.discard();
        s.pulses = 0;
        s.step = 0;
        s.until1 = s.until2 = 0;
        if (r.kind == Kind.NERVES) for (BlockPos p : r.nodes) lit(level, p, false);
        if (r.kind == Kind.ACID) {
            raise(level, r.seg1, false);
            raise(level, r.seg2, false);
        }
        if (r.kind == Kind.HEARTBEAT) for (List<BlockPos> w : r.walls) setAll(level, w, false);
    }

    private static String tag(RunState run, Room r) {
        return "harmontide.leviathan." + run.id + "." + r.index;
    }

    // --- waves ---------------------------------------------------------------------------------------------------------------------

    private static void tickWave(ServerLevel level, Interior in, RunState run, Room r, RoomState s, List<ServerPlayer> players) {
        long now = level.getGameTime();
        String tag = tag(run, r);
        int half = (s.total + 1) / 2;
        if (s.pulses < 2 && now - s.lastSpawn >= 160) {
            int n = s.pulses == 0 ? half : s.total - half;
            for (int i = 0; i < n; i++) spawnWaveMob(level, run, r, players, tag, i == 0 && s.pulses == 1 && r.wave == WAVES);
            s.pulses++;
            s.lastSpawn = now;
        }
        if (now % 10 != 0) return;
        List<Mob> alive = level.getEntitiesOfClass(Mob.class, r.box().inflate(96), m -> m.isAlive() && m.getTags().contains(tag));
        for (Mob m : alive) {                                                           // strays wandered off: back into the room
            if (!r.box().inflate(10).contains(m.position()) && !r.spawns.isEmpty()) {
                BlockPos sp = r.spawns.get(level.random.nextInt(r.spawns.size()));
                m.teleportTo(sp.getX() + 0.5, FLOOR, sp.getZ() + 0.5);
            }
        }
        if (s.bar != null) {
            for (ServerPlayer p : players) s.bar.addPlayer(p);
            s.bar.setProgress(s.total == 0 ? 0 : Mth.clamp((alive.size() + (s.pulses < 2 ? s.total - half : 0)) / (float) s.total, 0, 1));
        }
        for (Mob m : alive) {                                                           // keep them on the players (and in the room)
            if (m.getTarget() == null || !m.getTarget().isAlive()) {
                ServerPlayer near = nearest(players, m.position());
                if (near != null) m.setTarget(near);
            }
        }
        if (s.pulses >= 2 && alive.isEmpty()) finish(level, in, run, r, players);
    }

    @Nullable
    private static ServerPlayer nearest(List<ServerPlayer> players, Vec3 at) {
        ServerPlayer best = null;
        double bd = Double.MAX_VALUE;
        for (ServerPlayer p : players) {
            double d = p.distanceToSqr(at);
            if (d < bd && p.isAlive()) {
                bd = d;
                best = p;
            }
        }
        return best;
    }

    /** One of the infection: an overgrown parasite, more of them the deeper the wave, or a plain mob that wandered in. */
    private static void spawnWaveMob(ServerLevel level, RunState run, Room r, List<ServerPlayer> players, String tag, boolean host) {
        var rnd = level.random;
        boolean infested = host || rnd.nextFloat() < 0.25F + 0.12F * r.wave;
        int kind = rnd.nextInt(20);
        EntityType<? extends Mob> type = kind < 8 ? (infested ? LeviathanRegistry.INFESTED_ZOMBIE.get() : EntityType.ZOMBIE)
                : kind < 15 ? (infested ? LeviathanRegistry.INFESTED_SKELETON.get() : EntityType.SKELETON)
                : (infested ? LeviathanRegistry.INFESTED_CREEPER.get() : EntityType.CREEPER);
        if (host) type = LeviathanRegistry.INFESTED_ZOMBIE.get();
        Mob mob = type.create(level);
        if (mob == null) return;
        BlockPos at = r.spawns.isEmpty() ? r.centre : r.spawns.get(rnd.nextInt(r.spawns.size()));
        mob.moveTo(at.getX() + 0.5, FLOOR, at.getZ() + 0.5, rnd.nextFloat() * 360, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        mob.setPersistenceRequired();
        mob.addTag(tag);
        var follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) follow.setBaseValue(48);
        if (host) {
            mob.setCustomName(Component.translatable("entity.harmontide.bloated_host"));
            var scale = mob.getAttribute(Attributes.SCALE);
            if (scale != null) scale.setBaseValue(1.6);
            var hp = mob.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.setBaseValue(90);
            var dmg = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(9);
            mob.setHealth(90);
        }
        if (mob instanceof net.minecraft.world.entity.monster.Zombie z) z.setBaby(false);
        ServerPlayer near = nearest(players, mob.position());
        if (near != null) mob.setTarget(near);
        level.addFreshEntity(mob);
        level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY() + 0.5, mob.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    // --- nerves --------------------------------------------------------------------------------------------------------------------

    private static void lit(ServerLevel level, BlockPos p, boolean on) {
        BlockState s = level.getBlockState(p);
        if (s.is(LeviathanRegistry.NERVE_NODE.get()) && s.getValue(LeviathanBlocks.LIT) != on) set(level, p, s.setValue(LeviathanBlocks.LIT, on));
    }

    private static void restartNerves(ServerLevel level, Room r, RoomState s, boolean slipped) {
        for (BlockPos p : r.nodes) lit(level, p, false);
        s.order.clear();
        s.order.addAll(r.nodes);
        Collections.shuffle(s.order, new Random(level.getGameTime()));
        s.step = 0;
        s.t0 = level.getGameTime();
        if (!s.order.isEmpty()) lit(level, s.order.get(0), true);
        if (slipped && !s.order.isEmpty()) level.playSound(null, s.order.get(0), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 2.0F, 0.5F);
    }

    /** A nerve was struck (by hand or an arrow). */
    public static void onNerve(ServerLevel level, BlockPos pos, Player player) {
        Interior in = get(level.getServer());
        RunState run = in.runAt(Vec3.atCenterOf(pos));
        if (run == null) return;
        Room r = run.current();
        RoomState s = run.live(r.index);
        if (r.kind != Kind.NERVES || !s.active || !r.nodes.contains(pos)) return;
        if (s.step < s.order.size() && s.order.get(s.step).equals(pos)) {
            lit(level, pos, false);
            s.step++;
            s.t0 = level.getGameTime();
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 2.0F, 0.6F + 0.25F * s.step);
            if (s.step >= s.order.size()) finish(level, in, run, r, in.playersIn(level, run));
            else lit(level, s.order.get(s.step), true);
        } else {
            player.hurt(level.damageSources().magic(), 2.0F);
            level.playSound(null, pos, SoundEvents.SLIME_HURT, SoundSource.BLOCKS, 1.5F, 0.5F);
            restartNerves(level, r, s, false);
        }
    }

    // --- heartbeat -----------------------------------------------------------------------------------------------------------------

    private static final int BEAT = 50, OPEN_FOR = 22;

    private static void setAll(ServerLevel level, List<BlockPos> wall, boolean open) {
        for (BlockPos p : wall) {
            BlockState s = level.getBlockState(p);
            if (s.is(LeviathanRegistry.PULSE_WALL.get()) && s.getValue(LeviathanBlocks.OPEN) != open) set(level, p, s.setValue(LeviathanBlocks.OPEN, open));
        }
    }

    private static void tickHeartbeat(ServerLevel level, Interior in, RunState run, Room r, RoomState s, List<ServerPlayer> players) {
        long now = level.getGameTime();
        if (now % BEAT == 0) level.playSound(null, r.centre, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 3.0F, 0.6F);
        for (int k = 0; k < r.walls.size(); k++) {
            boolean open = (now + k * 17L) % BEAT < OPEN_FOR;
            List<BlockPos> wall = r.walls.get(k);
            if (wall.isEmpty()) continue;
            boolean was = level.getBlockState(wall.get(0)).is(LeviathanRegistry.PULSE_WALL.get()) && level.getBlockState(wall.get(0)).getValue(LeviathanBlocks.OPEN);
            if (was == open) continue;
            setAll(level, wall, open);
            if (!open) {                                                                // it clenches shut on whoever is in it
                Vec3 back = r.edge(r.in).subtract(Vec3.atBottomCenterOf(r.centre)).normalize();
                for (ServerPlayer p : players) {
                    if (wall.contains(p.blockPosition()) || wall.contains(p.blockPosition().above())) {
                        p.hurt(level.damageSources().inWall(), 3.0F);
                        p.push(back.x * 1.2, 0.3, back.z * 1.2);
                        p.hurtMarked = true;
                    }
                }
            }
        }
        for (ServerPlayer p : players) if (p.position().distanceTo(r.edge(r.out)) < 3.0) finish(level, in, run, r, players);
    }

    // --- stomach acid -------------------------------------------------------------------------------------------------------------

    private static void raise(ServerLevel level, List<BlockPos> stones, boolean up) {
        for (BlockPos p : stones) set(level, p, (up ? LeviathanRegistry.DENSE_FLESH.get() : LeviathanRegistry.ACID.get()).defaultBlockState());
    }

    /** A valve was squeezed: its stretch of stepping stones rises out of the acid for a while. */
    public static void onValve(ServerLevel level, BlockPos pos, Player player) {
        Interior in = get(level.getServer());
        RunState run = in.runAt(Vec3.atCenterOf(pos));
        if (run == null) return;
        Room r = run.current();
        RoomState s = run.live(r.index);
        if (r.kind != Kind.ACID || !s.active) return;
        long until = level.getGameTime() + 200;
        if (pos.equals(r.valve1)) {
            raise(level, r.seg1, true);
            s.until1 = until;
        } else if (pos.equals(r.valve2)) {
            raise(level, r.seg2, true);
            s.until2 = until;
        } else {
            return;
        }
        level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.BLOCKS, 2.0F, 0.6F);
        player.displayClientMessage(Component.translatable("message.harmontide.leviathan.valve"), true);
    }

    private static void tickAcid(ServerLevel level, Interior in, RunState run, Room r, RoomState s, List<ServerPlayer> players) {
        long now = level.getGameTime();
        for (int k = 0; k < 2; k++) {
            long until = k == 0 ? s.until1 : s.until2;
            List<BlockPos> seg = k == 0 ? r.seg1 : r.seg2;
            if (until == 0) continue;
            if (now >= until) {
                raise(level, seg, false);
                level.playSound(null, seg.isEmpty() ? r.centre : seg.get(0), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 2.0F, 0.5F);
                if (k == 0) s.until1 = 0;
                else s.until2 = 0;
            } else if (until - now < 50 && now % 5 == 0) {
                for (BlockPos p : seg) level.sendParticles(ParticleTypes.BUBBLE_POP, p.getX() + 0.5, p.getY() + 1.05, p.getZ() + 0.5, 2, 0.3, 0, 0.3, 0.01);
            }
        }
        for (ServerPlayer p : players) if (p.position().distanceTo(r.edge(r.out)) < 3.0 && p.getY() >= FLOOR - 0.1) finish(level, in, run, r, players);
    }

    // --- cysts -----------------------------------------------------------------------------------------------------------------------

    private static void tickCysts(ServerLevel level, Room r, RoomState s, List<ServerPlayer> players) {
        long now = level.getGameTime();
        if (now - s.lastSpawn < 220) return;
        s.lastSpawn = now;
        String tag = "harmontide.leviathan.cyst";
        int alive = level.getEntitiesOfClass(Mob.class, r.box().inflate(16), m -> m.isAlive() && m.getTags().contains(tag)).size();
        for (BlockPos c : r.cysts) {
            BlockState st = level.getBlockState(c);
            if (alive >= 6 || !st.is(LeviathanRegistry.CYST.get()) || st.getValue(LeviathanBlocks.CURED)) continue;
            Vec3 out = Vec3.atBottomCenterOf(c).add(Vec3.atBottomCenterOf(r.centre).subtract(Vec3.atBottomCenterOf(c)).normalize().scale(1.5));
            EntityType<? extends Mob> type = switch (level.random.nextInt(3)) {
                case 0 -> LeviathanRegistry.INFESTED_ZOMBIE.get();
                case 1 -> LeviathanRegistry.INFESTED_SKELETON.get();
                default -> LeviathanRegistry.INFESTED_CREEPER.get();
            };
            Mob mob = type.create(level);
            if (mob == null) continue;
            mob.moveTo(out.x, FLOOR, out.z, level.random.nextFloat() * 360, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(c), MobSpawnType.EVENT, null);
            mob.setPersistenceRequired();
            mob.addTag(tag);
            ServerPlayer near = nearest(players, mob.position());
            if (near != null) mob.setTarget(near);
            level.addFreshEntity(mob);
            level.sendParticles(ParticleTypes.ITEM_SLIME, out.x, FLOOR + 0.5, out.z, 16, 0.3, 0.5, 0.3, 0.1);
            level.playSound(null, c, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2F, 0.5F);
            alive++;
        }
    }

    /** An antibody was pressed into a cyst: once every cyst is cured, the room is done. */
    public static void onCystCured(ServerLevel level, BlockPos pos, Player player) {
        Interior in = get(level.getServer());
        RunState run = in.runAt(Vec3.atCenterOf(pos));
        if (run == null) return;
        Room r = run.current();
        if (r.kind != Kind.CYSTS) return;
        relearn(level, r);
        for (BlockPos c : r.cysts) {
            BlockState s = level.getBlockState(c);
            if (s.is(LeviathanRegistry.CYST.get()) && !s.getValue(LeviathanBlocks.CURED)) return;
        }
        finish(level, in, run, r, in.playersIn(level, run));
    }

    // --- cilia ------------------------------------------------------------------------------------------------------------------------

    private static void tickCilia(ServerLevel level, Interior in, RunState run, Room r, RoomState s, List<ServerPlayer> players) {
        for (ServerPlayer p : players) {
            if (p.position().distanceTo(r.edge(r.out)) < 3.0 && p.getY() >= FLOOR - 0.1) {
                finish(level, in, run, r, players);
                return;
            }
            if (r.contains(p.position()) && p.getY() < FLOOR - 3) {                     // fell in: her muscles heave you back up
                int t = s.fallen.merge(p.getUUID(), 1, Integer::sum);
                if (t > 30) {
                    s.fallen.remove(p.getUUID());
                    Vec3 back = r.edge(r.in);
                    p.resetFallDistance();
                    p.teleportTo(back.x, FLOOR, back.z);
                    p.displayClientMessage(Component.translatable("message.harmontide.leviathan.heaved"), true);
                }
            } else {
                s.fallen.remove(p.getUUID());
            }
        }
    }

    // --- her egg-cell -------------------------------------------------------------------------------------------------------------------

    /** How long a press into the egg-cell lasts, and how far from its axis he stands and rocks. */
    private static final int HUMP_FOR = 70;
    private static final double STAND = 1.35, THRUST = 0.32;
    private static final Map<UUID, Hump> HUMPS = new HashMap<>();

    /** One player's press: where the egg-cell is, which way is out from it, and how far the thrust has gone. */
    private static final class Hump {
        final BlockPos pos;
        final Vec3 axis, out;
        final float yaw;
        int ticks;

        Hump(BlockPos pos, Vec3 axis, Vec3 out, float yaw) {
            this.pos = pos;
            this.axis = axis;
            this.out = out;
            this.yaw = yaw;
        }
    }

    public static boolean humping(ServerPlayer p) {
        return HUMPS.containsKey(p.getUUID());
    }

    /**
     * Her egg-cell was touched. If the way here is cleared and the adult option is on, he rocks into it for a few seconds and the
     * blessing waits until that is over. Otherwise everyone inside is blessed, put outside, and she is healed straight away.
     */
    public static void onHeart(ServerLevel level, BlockPos pos, ServerPlayer toucher) {
        Interior in = get(level.getServer());
        RunState run = in.runAt(Vec3.atCenterOf(pos));
        if (run == null) return;
        Room r = run.current();
        if (r.kind != Kind.HEART) {
            toucher.displayClientMessage(Component.translatable("message.harmontide.leviathan.heart_not_yet"), true);
            return;
        }
        LeviathanEvent event = LeviathanEvent.get(level.getServer().overworld());
        if (event.phase != LeviathanEvent.LANDED || event.run != run.id) return;
        if (LeviathanEvent.nsfwEntry() && !toucher.isSpectator() && inside(level)) {
            if (!HUMPS.isEmpty()) return;
            beginHump(level, pos, r, toucher);
            return;
        }
        reward(level, pos, toucher, false);
    }

    private static void beginHump(ServerLevel level, BlockPos pos, Room r, ServerPlayer p) {
        Vec3 axis = new Vec3(r.centre.getX() + 0.5, FLOOR, r.centre.getZ() + 0.5);
        Vec3 flat = new Vec3(p.getX() - axis.x, 0, p.getZ() - axis.z);
        if (flat.lengthSqr() < 0.04) flat = new Vec3(0, 0, 1);
        Vec3 out = flat.normalize();
        float yaw = (float) Math.toDegrees(Mth.atan2(out.x, -out.z)); // look back along out, into the egg-cell
        HUMPS.put(p.getUUID(), new Hump(pos, axis, out, yaw));
        p.noPhysics = true;
        p.displayClientMessage(Component.translatable("message.harmontide.leviathan.egg_hump").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    /**
     * Runs after {@link #tick}, so the finishing tick can heal her and send him home without that tick's "visit is over" check
     * throwing him out first and wiping the line.
     */
    public static void tickHumps(MinecraftServer server) {
        if (HUMPS.isEmpty()) return;
        for (UUID id : new ArrayList<>(HUMPS.keySet())) {
            Hump h = HUMPS.get(id);
            if (h == null) continue;
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p == null || !p.isAlive() || !inside(p.level())) {
                HUMPS.remove(id);
                if (p != null) release(p);
                continue;
            }
            if (h.ticks >= HUMP_FOR) {
                HUMPS.remove(id);
                release(p);
                reward(p.serverLevel(), h.pos, p, true);
                continue;
            }
            pose(p, h);
            h.ticks++;
        }
    }

    private static void pose(ServerPlayer p, Hump h) {
        double reach = STAND + Math.sin(h.ticks * 0.85) * THRUST;
        Vec3 at = h.axis.add(h.out.scale(reach));
        p.noPhysics = true;
        p.teleportTo(p.serverLevel(), at.x, FLOOR, at.z, h.yaw, -35.0F);
        p.setDeltaMovement(Vec3.ZERO);
        p.setYHeadRot(h.yaw);
        p.yRotO = h.yaw;
        p.yBodyRot = p.yBodyRotO = h.yaw;
        p.xRotO = -35.0F;
        p.hurtMarked = true;
        p.resetFallDistance();
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 6, false, false, false));
        if (h.ticks % 8 == 0 && p.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, h.axis.x, FLOOR + 1.15, h.axis.z, 3, 0.35, 0.2, 0.35, 0.0);
            level.playSound(null, at.x, FLOOR + 1.0, at.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 0.7F, 0.8F);
        }
    }

    private static void release(ServerPlayer p) {
        p.noPhysics = false;
        p.setDeltaMovement(Vec3.ZERO);
        p.resetFallDistance();
        MobEffectInstance slow = p.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow != null && slow.getAmplifier() == 6 && slow.getDuration() <= 15) p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    /** The thanks: bless everyone still inside, heal her if she is still landed, and carry them out. */
    private static void reward(ServerLevel level, BlockPos pos, ServerPlayer toucher, boolean humped) {
        Interior in = get(level.getServer());
        RunState run = in.runAt(Vec3.atCenterOf(pos));
        if (run == null) return;
        Room r = run.current();
        if (r.kind != Kind.HEART) return;
        LeviathanEvent event = LeviathanEvent.get(level.getServer().overworld());
        run.done.add(r.index);
        in.setDirty();
        List<ServerPlayer> players = in.playersIn(level, run);
        if (!players.contains(toucher)) players.add(toucher);
        for (ServerPlayer p : players) DreamHeart.bless(p);
        if (event.phase == LeviathanEvent.LANDED && event.run == run.id) event.heal(level.getServer().overworld());
        String key = humped ? "message.harmontide.leviathan.egg_out" : "message.harmontide.leviathan.healed_out";
        for (ServerPlayer p : players) {
            leave(p, key);
            Traversal.award(p, "dream_blessing");
        }
    }

    // --- saving -------------------------------------------------------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (RunState r : this.runs.values()) {
            CompoundTag t = new CompoundTag();
            t.putInt("Id", r.id);
            t.putLong("Seed", r.seed);
            t.putBoolean("Built", r.built);
            t.putIntArray("Done", r.done.stream().mapToInt(Integer::intValue).toArray());
            list.add(t);
        }
        tag.put("Runs", list);
        return tag;
    }

    public static Interior load(CompoundTag tag, HolderLookup.Provider lookup) {
        Interior in = new Interior();
        for (Tag t : tag.getList("Runs", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            RunState r = new RunState(c.getInt("Id"), c.getLong("Seed"));
            r.built = c.getBoolean("Built");
            for (int i : c.getIntArray("Done")) r.done.add(i);
            in.runs.put(r.id, r);
        }
        return in;
    }

    /** For tests: the entry spot of a visit (building it if needed). */
    public static BlockPos entrySpot(MinecraftServer server, int run) {
        ServerLevel level = testLevel(server);
        Interior in = get(server);
        RunState rs = in.runFor(run);
        if (!rs.built && level != null) build(level, rs);
        return rs.layout().spawn();
    }

    @Nullable
    public static Room currentRoom(MinecraftServer server, int run) {
        RunState rs = get(server).run(run);
        return rs == null ? null : rs.current();
    }

    public static boolean isDone(MinecraftServer server, int run, int room) {
        RunState rs = get(server).run(run);
        return rs != null && rs.done.contains(room);
    }

    /** For tests and the test command: marks every room before the heart as done and opens their doors. */
    public static void clearTo(MinecraftServer server, int run, Kind upTo) {
        ServerLevel level = level(server);
        Interior in = get(server);
        RunState rs = in.run(run);
        if (rs == null || level == null) return;
        for (Room r : rs.layout().chain) {
            if (r.kind == upTo) break;
            rs.done.add(r.index);
            openDoor(level, r, false);
        }
        in.setDirty();
    }

    /** For tests: forgets a visit entirely (its blocks stay where they are and are rebuilt over). */
    public static void forget(MinecraftServer server, int run) {
        Interior in = get(server);
        if (in.runs.remove(run) != null) in.setDirty();
    }

    /** For the test command: marks the next rooms done (never the heart chamber), opening their ways on. */
    public static void skip(MinecraftServer server, int run, int rooms) {
        ServerLevel level = testLevel(server);
        Interior in = get(server);
        RunState rs = in.run(run);
        if (rs == null || level == null) return;
        for (int i = 0; i < rooms; i++) {
            Room r = rs.current();
            if (r.kind == Kind.HEART) break;
            reset(level, rs, r);
            finish(level, in, rs, r, in.playersIn(level, rs));
        }
    }

    public static ItemStack antibody() {
        return new ItemStack(LeviathanRegistry.ANTIBODY.get());
    }
}
