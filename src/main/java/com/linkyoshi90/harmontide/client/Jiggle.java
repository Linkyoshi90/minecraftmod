package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A chest bone, made from the breast cubes already on the body, that lags behind her movement.
 * Babies, anyone of childlike height, and the bulls stay still. The bone is added once per model.
 */
final class Jiggle {
    private static final Map<Integer, Spring> SPRINGS = new ConcurrentHashMap<>();

    private Jiggle() {}

    /** Move her breast cubes onto a child bone, once, so a later pose can swing them. */
    static void install(AnimationProcessor<?> proc) {
        if (proc.getBone("bust") != null) return;
        GeoBone body = proc.getBone("body");
        if (body == null) return;
        List<Cube> cubes = new ArrayList<>();
        for (GeoCube cube : body.getCubes()) {
            Cube box = measure(cube);
            if (box != null) cubes.add(box);
        }
        List<Cube> breasts = breasts(cubes);
        if (breasts.size() < 2) return;
        double cy = 0, oz = 0, sz = 0;
        for (Cube c : breasts) {
            cy += c.cy;
            oz += c.oz;
            sz += c.sz;
        }
        cy /= breasts.size();
        oz /= breasts.size();
        sz /= breasts.size();
        GeoBone bust = new GeoBone(body, "bust", null, null, null, null);
        bust.updatePivot(0, (float) cy, (float) (oz + sz * 0.85));
        for (Cube c : breasts) {
            body.getCubes().remove(c.cube);
            bust.getCubes().add(c.cube);
        }
        body.getChildBones().add(bust);
        proc.registerGeoBone(bust);
    }

    /** Swing the chest bone. A walk, a landing and the kneel bounce all feed it. */
    static void apply(AnimationProcessor<?> proc, Mob mob, float partialTick) {
        GeoBone bust = proc.getBone("bust");
        if (bust == null) return;
        if (mob.isBaby() || mob.getBbHeight() < Bonded.CHILDLIKE_HEIGHT || mob instanceof Minotaur mino && mino.isMale()) {
            CowgirlPose.setRot(proc, "bust", 0, 0, 0);
            return;
        }
        Spring spring = SPRINGS.computeIfAbsent(mob.getId(), id -> new Spring());
        float vy = (float) (mob.getY() - mob.yOld);
        float hx = (float) (mob.getX() - mob.xOld);
        float hz = (float) (mob.getZ() - mob.zOld);
        float speed = Mth.sqrt(hx * hx + hz * hz);
        float phase = mob.tickCount + partialTick;
        float target = Mth.clamp(-vy * 160.0F, -12.0F, 12.0F)
                + Mth.sin(phase * 1.35F) * Mth.clamp(speed * 70.0F, 0.0F, 7.0F)
                + Mth.sin(phase * 0.17F) * 1.6F;
        spring.vel += (target - spring.angle) * 0.28F;
        spring.vel *= 0.62F;
        spring.angle += spring.vel;
        float sway = Mth.clamp(hx * 40.0F, -4.0F, 4.0F);
        spring.rollVel += (sway - spring.roll) * 0.2F;
        spring.rollVel *= 0.7F;
        spring.roll += spring.rollVel;
        CowgirlPose.setRot(proc, "bust", spring.angle, 0, spring.roll);
    }

    private static List<Cube> breasts(List<Cube> body) {
        Cube bestA = null, bestB = null;
        double bestZ = Double.POSITIVE_INFINITY;
        double bestVol = -1;
        for (int i = 0; i < body.size(); i++) {
            Cube a = body.get(i);
            if (!a.soft()) continue;
            for (int j = i + 1; j < body.size(); j++) {
                Cube b = body.get(j);
                if (!b.soft() || !a.mirrors(b)) continue;
                double z = Math.min(a.oz, b.oz);
                double vol = a.volume() + b.volume();
                if (z < bestZ - 0.05 || z <= bestZ + 0.05 && vol > bestVol) {
                    bestZ = z;
                    bestVol = vol;
                    bestA = a;
                    bestB = b;
                }
            }
        }
        List<Cube> out = new ArrayList<>();
        if (bestA == null) return out;
        double y = (bestA.cy + bestB.cy) * 0.5;
        double front = Math.min(bestA.oz, bestB.oz);
        for (Cube c : body) {
            if (!c.soft()) continue;
            if (Math.abs(c.cy - y) > 1.15) continue;
            if (c.oz > front + 0.35) continue;
            if (c.sx < 0.7 || c.sx > 5.2 || c.sy < 0.45 || c.sy > 3.8 || c.sz > 3.2) continue;
            out.add(c);
        }
        return out;
    }

    private static Cube measure(GeoCube cube) {
        if (cube.quads() == null || cube.size() == null) return null;
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        int n = 0;
        for (GeoQuad quad : cube.quads()) {
            if (quad.vertices() == null) continue;
            for (GeoVertex vertex : quad.vertices()) {
                var p = vertex.position();
                minX = Math.min(minX, p.x());
                minY = Math.min(minY, p.y());
                minZ = Math.min(minZ, p.z());
                maxX = Math.max(maxX, p.x());
                maxY = Math.max(maxY, p.y());
                maxZ = Math.max(maxZ, p.z());
                n++;
            }
        }
        if (n == 0) return null;
        double inf = cube.inflate();
        Vec3 size = cube.size();
        double ox = -(maxX - inf) * 16.0;
        double oy = (minY + inf) * 16.0;
        double oz = (minZ + inf) * 16.0;
        return new Cube(cube, ox, oy, oz, size.x, size.y, size.z, inf * 16.0);
    }

    private static final class Spring {
        float angle, vel, roll, rollVel;
    }

    private static final class Cube {
        final GeoCube cube;
        final double ox, oy, oz, sx, sy, sz, inflate, cx, cy;

        Cube(GeoCube cube, double ox, double oy, double oz, double sx, double sy, double sz, double inflate) {
            this.cube = cube;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
            this.inflate = inflate;
            this.cx = ox + sx * 0.5;
            this.cy = oy + sy * 0.5;
        }

        boolean soft() {
            return this.inflate < 0.05 && this.sx >= 1.0 && this.sx <= 4.8 && this.sy >= 0.7 && this.sy <= 3.6 && this.sz <= 2.8 && this.sz > 0.2;
        }

        boolean mirrors(Cube other) {
            if (this.cx * other.cx >= 0) return false;
            if (Math.abs(this.cx + other.cx) > 0.9) return false;
            if (Math.abs(this.cy - other.cy) > 0.4) return false;
            if (Math.abs(this.oz - other.oz) > 0.45) return false;
            if (Math.abs(this.sx - other.sx) > 0.6 || Math.abs(this.sy - other.sy) > 0.6) return false;
            return true;
        }

        double volume() {
            return this.sx * this.sy * this.sz;
        }
    }
}
