package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Herds;
import com.linkyoshi90.harmontide.entity.Microhabitats;
import com.linkyoshi90.harmontide.registry.FidgetTable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Idle fidgets (2026-11-13). Standing about with nothing to do, every so often ({@link #MIN_GAP} to {@link #MAX_GAP} ticks) a girl plays a
 * short fidget of hers (tools/gen_fidgets.py made them, registry/FidgetTable lists them): shared ones by her body (a stretch, a yawn, a
 * look back, a tail swish...), her family's (a cat kneads, a dog shakes herself off, a centaur stamps...), and her own signature one.
 * Each girl leans to some more than others (her habits, from her UUID), and her mood leans her too: ill at ease where she is
 * (entity/Microhabitats) she fidgets twice as often and looks about; content, she stretches and yawns; in a herd she keeps looking back.
 * <p>
 * The playful families (cats, foxes, dogs, rabbit kinds, rodents) now and then get the zoomies ({@link ZoomiesGoal}): a sudden mad dash
 * round in a loop, then back to whatever she was doing. Config {@code fidgets}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Fidgets {
    public static final int MIN_GAP = 160, MAX_GAP = 600, ZOOMIES = 60, ZOOMIES_ODDS = 12;
    public static final Set<GirlSpecies.Family> PLAYFUL = Set.of(GirlSpecies.Family.NEKOMATA, GirlSpecies.Family.KITSUNE, GirlSpecies.Family.CANID,
            GirlSpecies.Family.JACKALOPE, GirlSpecies.Family.RODENT);
    private static final Map<String, Map<String, Integer>> PARSED = new HashMap<>();
    private static final String NEXT = "NextFidget", ZOOM_UNTIL = "ZoomiesUntil";

    private Fidgets() {}

    /** Her fidgets and their lengths in ticks. */
    public static Map<String, Integer> of(GirlSpecies s) {
        return PARSED.computeIfAbsent(s.id(), id -> {
            Map<String, Integer> out = new java.util.LinkedHashMap<>();
            String row = FidgetTable.FIDGETS.get(id);
            if (row != null) for (String part : row.split(",")) {
                String[] kv = part.split(":");
                if (kv.length == 2) out.put(kv[0], Integer.parseInt(kv[1]));
            }
            return out;
        });
    }

    /** Free to fidget: standing idle, grown, not busy, not moving. */
    public static boolean idle(AbstractMonsterGirl g) {
        return HarmontideConfig.fidgets() && g.getAction() == AbstractMonsterGirl.Action.NONE && !g.isInWater() && !g.isPassenger() && g.getTarget() == null
                && g.getNavigation().isDone() && g.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && !g.isSleeping() && !g.isBaby();
    }

    /** Which fidget she reaches for: her habits, then her mood. Null if she has none. */
    @Nullable
    public static String pick(AbstractMonsterGirl g, RandomSource r) {
        Map<String, Integer> all = of(g.getSpecies());
        if (all.isEmpty()) return null;
        List<String> names = new ArrayList<>(all.keySet());
        double[] w = new double[names.size()];
        long habit = g.getUUID().getMostSignificantBits();
        float comfort = Microhabitats.comfort(g);
        boolean herd = Herds.herds(g) && Herds.herd(g).size() > 1;
        double total = 0;
        for (int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            double v = 1.0 + ((habit >>> (i * 3)) & 3);                           // her habits: some she reaches for more
            if (n.equals("signature")) v += 2;
            if (comfort < Microhabitats.UNEASY && (n.equals("look_back") || n.equals("shift") || n.equals("ear_twitch"))) v += 4;
            if (comfort >= Microhabitats.CONTENT && (n.equals("stretch") || n.equals("yawn"))) v += 3;
            if (herd && n.equals("look_back")) v += 4;
            w[i] = v;
            total += v;
        }
        double x = r.nextDouble() * total;
        for (int i = 0; i < names.size(); i++) {
            x -= w[i];
            if (x <= 0) return names.get(i);
        }
        return names.get(names.size() - 1);
    }

    /** She fidgets now, if she is idle. What she played, or null. */
    @Nullable
    public static String fidget(AbstractMonsterGirl g, RandomSource r) {
        if (!idle(g)) return null;
        if (PLAYFUL.contains(g.getSpecies().family()) && r.nextInt(ZOOMIES_ODDS) == 0 && g.level() instanceof ServerLevel level) {
            g.getPersistentData().putLong(ZOOM_UNTIL, level.getGameTime() + ZOOMIES);
            return "zoomies";
        }
        String n = pick(g, r);
        if (n == null) return null;
        return g.fidget(n, of(g.getSpecies()).get(n)) ? n : null;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl g) || !(g.level() instanceof ServerLevel level) || g.tickCount % 20 != 13) return;
        if (g.getAction() == AbstractMonsterGirl.Action.FIDGET && g.getDeltaMovement().horizontalDistanceSqr() > 4.0E-3) {
            g.setAction(AbstractMonsterGirl.Action.NONE);                       // off she goes: the fidget breaks off
            return;
        }
        var pd = g.getPersistentData();
        long now = level.getGameTime();
        if (!pd.contains(NEXT)) pd.putLong(NEXT, now + MIN_GAP + g.getRandom().nextInt(MAX_GAP - MIN_GAP));
        if (now < pd.getLong(NEXT)) return;
        boolean uneasy = Microhabitats.comfort(g) < Microhabitats.UNEASY;
        int gap = MIN_GAP + g.getRandom().nextInt(MAX_GAP - MIN_GAP);
        pd.putLong(NEXT, now + (uneasy ? gap / 2 : gap));
        fidget(g, g.getRandom());
    }

    /** The zoomies: three quick legs of a loop at a dead run, then back to whatever she was doing. */
    public static final class ZoomiesGoal extends Goal {
        private final AbstractMonsterGirl girl;
        private int legs;

        public ZoomiesGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        private boolean on() {
            return this.girl.level().getGameTime() < this.girl.getPersistentData().getLong(ZOOM_UNTIL);
        }

        @Override
        public boolean canUse() {
            return this.on() && !this.girl.isPassenger();
        }

        @Override
        public void start() {
            this.legs = 0;
            this.next();
        }

        private void next() {
            double a = this.girl.getRandom().nextDouble() * Math.PI * 2 + this.legs * 2.1;
            Vec3 to = this.girl.position().add(Math.cos(a) * 5, 0, Math.sin(a) * 5);
            this.girl.getNavigation().moveTo(to.x, to.y, to.z, 1.7);
            this.legs++;
        }

        @Override
        public boolean canContinueToUse() {
            return this.on() && this.legs <= 3;
        }

        @Override
        public void tick() {
            if (this.girl.getNavigation().isDone()) this.next();
        }

        @Override
        public void stop() {
            this.girl.getNavigation().stop();
            this.girl.getPersistentData().remove(ZOOM_UNTIL);
        }
    }
}
