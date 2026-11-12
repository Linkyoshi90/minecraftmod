package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The performance pass (2026-11-10): a crowd the size of a lived-in home (a girl of every family, a big family at the hearth with
 * three wives, a harpy roost of hoppers, a frog pond, the player among them) ticks for {@link #MEASURE} ticks while every entity
 * tick is timed. Writes run/perf_report.txt (the cost of each kind, dearest first) and fails if the crowd would eat a real share
 * of a tick, so a feature that scans the world every tick shows up here before it shows up as lag.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class PerformanceTests {
    public static final int WARM_UP = 20, MEASURE = 200;
    /** Budgets, generous on purpose (a slow CI machine, other tests' leftovers): average per entity tick, the dearest kind, the player, the whole crowd per tick. */
    public static final double MOB_AVG_MS = 0.25, KIND_MAX_MS = 1.5, PLAYER_MS = 3.0, CROWD_MS = 20.0;

    /** Times each entity tick of the crowd, between EntityTickEvent.Pre and Post. */
    public static final class Clock {
        final Set<Entity> watched = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<Entity, Long> started = new IdentityHashMap<>();
        final Map<String, long[]> byKind = new HashMap<>();                       // kind -> {nanos, ticks}
        boolean on;

        @SubscribeEvent
        public void pre(EntityTickEvent.Pre event) {
            if (this.on && this.watched.contains(event.getEntity())) this.started.put(event.getEntity(), System.nanoTime());
        }

        @SubscribeEvent
        public void post(EntityTickEvent.Post event) {
            Long t0 = this.on ? this.started.remove(event.getEntity()) : null;
            if (t0 == null) return;
            long[] k = this.byKind.computeIfAbsent(kind(event.getEntity()), x -> new long[2]);
            k[0] += System.nanoTime() - t0;
            k[1]++;
        }

        static String kind(Entity e) {
            if (e instanceof ServerPlayer) return "player";
            if (e instanceof AbstractMonsterGirl g) return g.getSpecies().family().name().toLowerCase() + ":" + g.getSpecies().id();
            return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
        }
    }

    @GameTest(template = PLATFORM, timeoutTicks = WARM_UP + MEASURE + 60, batch = "aLivedInHomeDoesNotLag")
    public static void aLivedInHomeDoesNotLag(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<Entity> crowd = new ArrayList<>();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(p.getUUID());
        crowd.add(p);
        for (int x = 10; x <= 13; x++) for (int z = 1; z <= 4; z++) {                 // the frog pond, glass-rimmed
            boolean rim = x == 10 || x == 13 || z == 1 || z == 4;
            helper.setBlock(new BlockPos(x, 1, z), rim ? Blocks.GLASS : Blocks.WATER);
        }
        List<Supplier<? extends EntityType<? extends Mob>>> frogs = List.of(ModEntities.TREE_FROG, ModEntities.BULLFROG, ModEntities.GLASS_FROG,
                ModEntities.POISON_DART_FROG, ModEntities.CANE_TOAD, ModEntities.MUCUS_TOAD);
        for (int i = 0; i < frogs.size(); i++) crowd.add(helper.spawn(frogs.get(i).get(), new BlockPos(11 + i % 2, 2, 2 + i % 2)));
        List<Supplier<? extends EntityType<? extends AbstractHarpy>>> harpies = List.of(ModEntities.SPARROW_HARPY, ModEntities.CROW_HARPY,
                ModEntities.OWL_HARPY, ModEntities.SEAGULL_HARPY, ModEntities.ALBATROSS_HARPY, ModEntities.TENGU_HARPY, ModEntities.POULTRY_HARPY,
                ModEntities.PHOENIX_HARPY);
        for (int i = 0; i < harpies.size(); i++) {                                     // the roost: a row of hoppers
            BlockPos hopper = new BlockPos(1 + i, 2, 13);
            helper.setBlock(hopper, Blocks.HOPPER);
            AbstractHarpy h = helper.spawn(harpies.get(i).get(), new BlockPos(1 + i, 3, 12));
            h.setAffection(p.getUUID(), 100);
            h.setRoost(helper.absolutePos(hopper));
            crowd.add(h);
        }
        Set<GirlSpecies.Family> seen = EnumSet.noneOf(GirlSpecies.Family.class);
        int n = 0, wives = 0;
        for (GirlSpecies s : GirlSpecies.values()) {                                   // a girl of every family, all friends of the house
            if (!seen.add(s.family())) continue;
            AbstractMonsterGirl g = spawn(helper, s, new BlockPos(2 + (n % 6) * 2, 2, 2 + (n / 6) * 2));
            g.setAffection(p.getUUID(), 100);
            if (wives < 3 && !g.isBaby() && s.family() != GirlSpecies.Family.KITSUNE) {   // a kitsune's wedding brings a sunshower: no weather here
                Marriage.wed(level, g, p, helper.absolutePos(hp));
                wives++;
            }
            crowd.add(g);
            n++;
        }
        Clock clock = new Clock();
        clock.watched.addAll(crowd);
        NeoForge.EVENT_BUS.register(clock);
        helper.runAfterDelay(WARM_UP, () -> clock.on = true);
        helper.runAfterDelay(WARM_UP + MEASURE, () -> {
            clock.on = false;
            NeoForge.EVENT_BUS.unregister(clock);
            try {
                report(helper, clock, crowd.size());
            } finally {
                HarpyTestUtil.remove(p);
                level.setWeatherParameters(6000, 0, false, false);                      // whatever the crowd did to the sky, clear for later tests
                for (Entity e : crowd) if (e != p && e.isAlive()) e.discard();       // strays mustn't wander into later tests
            }
        });
    }

    private static void report(GameTestHelper helper, Clock clock, int size) {
        List<Map.Entry<String, long[]>> kinds = new ArrayList<>(clock.byKind.entrySet());
        kinds.sort((a, b) -> Double.compare(ms(b.getValue()), ms(a.getValue())));
        long mobNanos = 0, mobTicks = 0;
        for (Map.Entry<String, long[]> e : kinds) {
            if (e.getKey().equals("player")) continue;
            mobNanos += e.getValue()[0];
            mobTicks += e.getValue()[1];
        }
        double mobAvg = mobTicks == 0 ? 0 : mobNanos / 1e6 / mobTicks;
        double crowdPerTick = (mobNanos + clock.byKind.getOrDefault("player", new long[2])[0]) / 1e6 / MEASURE;
        double player = ms(clock.byKind.getOrDefault("player", new long[]{0, 1}));
        List<String> lines = new ArrayList<>();
        lines.add(String.format("crowd of %d over %d ticks: %.3f ms per tick in all, %.4f ms per mob tick, player %.4f ms", size, MEASURE, crowdPerTick, mobAvg, player));
        lines.add("dearest first (ms per tick of one entity, ticks timed):");
        for (Map.Entry<String, long[]> e : kinds) lines.add(String.format("  %-40s %.4f  (%d)", e.getKey(), ms(e.getValue()), e.getValue()[1]));
        try {
            Files.write(Path.of("perf_report.txt").toAbsolutePath(), lines, StandardCharsets.UTF_8);
        } catch (java.io.IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String l : lines.subList(0, Math.min(8, lines.size()))) System.out.println("[perf] " + l);
        String dearest = kinds.isEmpty() ? "none" : kinds.get(0).getKey();
        HarpyTestUtil.check(mobTicks > (long) MEASURE * (size - 1) / 2, "most of the crowd ticked: " + mobTicks);
        HarpyTestUtil.check(mobAvg < MOB_AVG_MS, "a mob tick costs " + String.format("%.4f", mobAvg) + " ms on average (budget " + MOB_AVG_MS + "); see run/perf_report.txt");
        for (Map.Entry<String, long[]> e : kinds) {
            double cost = ms(e.getValue());
            HarpyTestUtil.check(e.getKey().equals("player") ? cost < PLAYER_MS : cost < KIND_MAX_MS,
                    e.getKey() + " costs " + String.format("%.4f", cost) + " ms a tick (dearest: " + dearest + "); see run/perf_report.txt");
        }
        HarpyTestUtil.check(crowdPerTick < CROWD_MS, "the whole crowd costs " + String.format("%.3f", crowdPerTick) + " ms a tick (budget " + CROWD_MS + ")");
        helper.succeed();
    }

    private static double ms(long[] k) {
        return k[1] == 0 ? 0 : k[0] / 1e6 / k[1];
    }
}
