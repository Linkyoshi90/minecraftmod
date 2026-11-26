package com.linkyoshi90.harmontide.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bare-chested copy of a girl's own texture. The top is painted into the chest and the bust, so those
 * faces are repainted with her belly's skin and a nipple, and any shell of clothing over them is cleared.
 * Models with no breast pair are left dressed.
 */
public final class Topless {
    private static final Map<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();
    private static final int ROSE = 0xC46A78;

    private Topless() {}

    /** Her clothed texture, or the bare copy while a bucket milking has her top off. */
    public static ResourceLocation worn(ResourceLocation texture, ResourceLocation model) {
        if (texture == null || model == null) return texture;
        return CACHE.computeIfAbsent(texture + "|" + model, key -> build(texture, model));
    }

    private static ResourceLocation build(ResourceLocation texture, ResourceLocation model) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return texture;
        NativeImage image = null;
        try {
            try (InputStream in = mc.getResourceManager().open(texture)) {
                image = NativeImage.read(in);
            }
            String json;
            try (InputStream in = mc.getResourceManager().open(model)) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!paint(image, cubes(json))) {
                image.close();
                return texture;
            }
            DynamicTexture dyn = new DynamicTexture(image);
            ResourceLocation id = HarmontideMod.id("dynamic/bare/" + Integer.toUnsignedString(texture.getPath().hashCode())
                    + "_" + Integer.toUnsignedString(model.getPath().hashCode()) + ".png");
            mc.getTextureManager().register(id, dyn);
            return id;
        } catch (Exception e) {
            if (image != null) image.close();
            return texture;
        }
    }

    private static boolean paint(NativeImage image, List<Cube> cubes) {
        List<Cube> body = new ArrayList<>();
        for (Cube c : cubes) if ("body".equals(c.bone) && c.inflate == null) body.add(c);
        List<Cube> breasts = breasts(body);
        if (breasts.size() < 2) return false;
        double breastY = breasts.get(0).cy;
        double frontZ = breasts.get(0).oz;
        for (Cube c : breasts) frontZ = Math.min(frontZ, c.oz);
        List<Cube> chest = new ArrayList<>();
        for (Cube c : body) {
            if (c.sx >= 3.5 && c.sy >= 1.8 && c.sz >= 2.0 && c.cy > breastY - 1.6 && c.cy < breastY + 1.8 && c.oz > frontZ + 0.15) chest.add(c);
        }
        if (chest.isEmpty()) return false;
        double bottom = Double.POSITIVE_INFINITY, top = Double.NEGATIVE_INFINITY;
        for (Cube c : chest) {
            bottom = Math.min(bottom, c.oy);
            top = Math.max(top, c.oy + c.sy);
        }
        List<Cube> mid = new ArrayList<>();
        for (Cube c : body) {
            if (c.sx >= 2.4 && c.sy >= 1.5 && c.sz >= 1.5 && c.cy < bottom - 0.15 && c.cy > bottom - 4.5) mid.add(c);
        }
        int shirt = median(image, chest, "north");
        int belly = median(image, mid, "north");
        int skin = belly != 0 && dist(belly, shirt) > 36 ? belly : forehead(image, cubes);
        if (skin == 0) skin = belly != 0 ? belly : 0xE8C4B0;
        for (Cube c : chest) fill(image, c, skin, false);
        for (Cube c : cubes) {
            if (!"body".equals(c.bone) || c.inflate != null || chest.contains(c) || breasts.contains(c)) continue;
            if (c.sy <= 0.6 && c.sx >= 3.5 && c.cy >= bottom - 0.2 && c.cy <= top + 0.45) fill(image, c, skin, false);
        }
        for (Cube c : breasts) fill(image, c, skin, true);
        for (Cube c : cubes) {
            if (c.inflate != null && ("body".equals(c.bone) || c.bone.startsWith("arm_") || c.bone.startsWith("forearm_"))) clear(image, c);
        }
        for (Cube c : cubes) {
            if (!"body".equals(c.bone) || c.inflate != null || breasts.contains(c) || chest.contains(c) || mid.contains(c)) continue;
            if (c.cy < bottom - 0.2 || c.cy > top + 2.2) continue;
            int col = median(image, List.of(c), "north");
            if (col == 0) col = median(image, List.of(c), "up");
            if (col != 0 && dist(col, skin) > 70) clear(image, c);
        }
        return true;
    }

    /** The fullest mirrored pair in front of the torso, plus the pieces that round them. */
    private static List<Cube> breasts(List<Cube> body) {
        Cube left = null, right = null;
        double bestZ = Double.POSITIVE_INFINITY;
        double bestVol = -1;
        for (int i = 0; i < body.size(); i++) {
            Cube a = body.get(i);
            if (a.sx < 1.0 || a.sx > 4.8 || a.sy < 0.7 || a.sy > 3.6 || a.sz > 2.8) continue;
            for (int j = i + 1; j < body.size(); j++) {
                Cube b = body.get(j);
                if (Math.abs(a.cy - b.cy) > 0.4 || Math.abs(a.sx - b.sx) > 0.4 || Math.abs(a.sy - b.sy) > 0.4) continue;
                if (Math.abs(a.cx + b.cx) > 0.9 || Math.abs(a.oz - b.oz) > 0.45 || a.cx * b.cx >= 0) continue;
                double z = Math.min(a.oz, b.oz);
                double vol = a.sx * a.sy * Math.max(a.sz, 0.2);
                if (z < bestZ - 0.05 || (Math.abs(z - bestZ) <= 0.05 && vol > bestVol)) {
                    bestZ = z;
                    bestVol = vol;
                    left = a.cx < 0 ? a : b;
                    right = a.cx < 0 ? b : a;
                }
            }
        }
        if (left == null) return List.of();
        double y = left.cy;
        double front = Math.min(left.oz, right.oz);
        List<Cube> out = new ArrayList<>();
        for (Cube c : body) {
            if (Math.abs(c.cy - y) > 1.15 || c.oz > front + 0.35) continue;
            if (c.sx < 0.7 || c.sx > 5.2 || c.sy < 0.45 || c.sy > 3.8 || c.sz > 3.2) continue;
            out.add(c);
        }
        return out;
    }

    private static int forehead(NativeImage image, List<Cube> cubes) {
        Cube head = null;
        double area = 0;
        for (Cube c : cubes) {
            if (!"head".equals(c.bone) || c.sx < 4 || c.sy < 4) continue;
            double a = c.sx * c.sy;
            if (a > area) {
                area = a;
                head = c;
            }
        }
        if (head == null) return 0;
        Rect r = head.uv.get("north");
        if (r == null) return 0;
        return medianRect(image, new Rect(r.x, r.y, r.w, Math.max(1, r.h / 2)));
    }

    private static void fill(NativeImage image, Cube cube, int skin, boolean nipple) {
        int sr = (skin >> 16) & 255, sg = (skin >> 8) & 255, sb = skin & 255;
        for (Map.Entry<String, Rect> e : cube.uv.entrySet()) {
            Rect r = e.getValue();
            float mul = switch (e.getKey()) {
                case "north" -> 1.0F;
                case "south" -> 0.72F;
                case "east" -> 0.84F;
                case "west" -> 0.86F;
                case "up" -> 1.08F;
                case "down" -> 0.70F;
                default -> 0.9F;
            };
            boolean nip = nipple && "north".equals(e.getKey()) && r.w >= 3 && r.h >= 3;
            float rad = Math.max(2.0F, Math.min(r.w, r.h) * 0.32F);
            for (int j = 0; j < r.h; j++) {
                for (int i = 0; i < r.w; i++) {
                    int x = r.x + i, y = r.y + j;
                    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) continue;
                    int n = ((i * 17 + j * 31) % 7) - 3;
                    int cr = clamp((int) (sr * mul) + n);
                    int cg = clamp((int) (sg * mul) + n);
                    int cb = clamp((int) (sb * mul) + n);
                    if (nip) {
                        float dx = (i + 0.5F - r.w / 2.0F) / rad;
                        float dy = (j + 0.5F - r.h / 2.0F) / rad;
                        float d = dx * dx + dy * dy;
                        if (d <= 1.0F) {
                            float t = 0.42F * (1.0F - d * 0.25F);
                            cr = mix(cr, (ROSE >> 16) & 255, t);
                            cg = mix(cg, (ROSE >> 8) & 255, t);
                            cb = mix(cb, ROSE & 255, t);
                        }
                        if (d <= 0.28F) {
                            cr = mix(cr, 150, 0.75F);
                            cg = mix(cg, 78, 0.75F);
                            cb = mix(cb, 90, 0.75F);
                        }
                    }
                    image.setPixelRGBA(x, y, (255 << 24) | (cb << 16) | (cg << 8) | cr);
                }
            }
        }
    }

    private static void clear(NativeImage image, Cube cube) {
        for (Rect r : cube.uv.values()) {
            for (int j = 0; j < r.h; j++) {
                for (int i = 0; i < r.w; i++) {
                    int x = r.x + i, y = r.y + j;
                    if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) image.setPixelRGBA(x, y, 0);
                }
            }
        }
    }

    private static int median(NativeImage image, List<Cube> cubes, String face) {
        int[] rs = new int[4096], gs = new int[4096], bs = new int[4096];
        int n = 0;
        for (Cube c : cubes) {
            Rect r = c.uv.get(face);
            if (r == null) continue;
            for (int j = 0; j < r.h && n < rs.length; j++) {
                for (int i = 0; i < r.w && n < rs.length; i++) {
                    int x = r.x + i, y = r.y + j;
                    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) continue;
                    int p = image.getPixelRGBA(x, y);
                    if ((p >>> 24) < 200) continue;
                    rs[n] = p & 255;
                    gs[n] = (p >> 8) & 255;
                    bs[n] = (p >> 16) & 255;
                    n++;
                }
            }
        }
        if (n == 0) return 0;
        return (mid(rs, n) << 16) | (mid(gs, n) << 8) | mid(bs, n);
    }

    private static int medianRect(NativeImage image, Rect r) {
        Cube fake = new Cube("head", 0, 0, 0, 1, 1, 1, null, Map.of("north", r));
        return median(image, List.of(fake), "north");
    }

    private static int mid(int[] v, int n) {
        java.util.Arrays.sort(v, 0, n);
        return v[n / 2];
    }

    private static int dist(int a, int b) {
        return Math.abs(((a >> 16) & 255) - ((b >> 16) & 255))
                + Math.abs(((a >> 8) & 255) - ((b >> 8) & 255))
                + Math.abs((a & 255) - (b & 255));
    }

    private static int mix(int a, int b, float t) {
        return clamp((int) (a + (b - a) * t));
    }

    private static int clamp(int c) {
        return Math.max(0, Math.min(255, c));
    }

    private static List<Cube> cubes(String json) {
        List<Cube> out = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (JsonElement be : root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones")) {
            JsonObject bone = be.getAsJsonObject();
            if (!bone.has("cubes")) continue;
            String name = bone.get("name").getAsString();
            for (JsonElement ce : bone.getAsJsonArray("cubes")) {
                JsonObject c = ce.getAsJsonObject();
                double[] o = arr(c, "origin"), s = arr(c, "size");
                if (o == null || s == null) continue;
                Double inflate = c.has("inflate") ? c.get("inflate").getAsDouble() : null;
                Map<String, Rect> uv = new HashMap<>();
                if (c.has("uv") && c.get("uv").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> face : c.getAsJsonObject("uv").entrySet()) {
                        if (!face.getValue().isJsonObject()) continue;
                        Rect rect = rect(face.getValue().getAsJsonObject());
                        if (rect != null) uv.put(face.getKey(), rect);
                    }
                }
                out.add(new Cube(name, o[0], o[1], o[2], s[0], s[1], s[2], inflate, uv));
            }
        }
        return out;
    }

    private static double[] arr(JsonObject o, String key) {
        if (!o.has(key)) return null;
        var a = o.getAsJsonArray(key);
        return new double[]{a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble()};
    }

    private static Rect rect(JsonObject face) {
        if (!face.has("uv") || !face.has("uv_size")) return null;
        var uv = face.getAsJsonArray("uv");
        var size = face.getAsJsonArray("uv_size");
        int u = uv.get(0).getAsInt(), v = uv.get(1).getAsInt();
        int dw = size.get(0).getAsInt(), dh = size.get(1).getAsInt();
        if (dw == 0 || dh == 0) return null;
        return new Rect(dw < 0 ? u + dw : u, dh < 0 ? v + dh : v, Math.abs(dw), Math.abs(dh));
    }

    private record Rect(int x, int y, int w, int h) {}

    private static final class Cube {
        final String bone;
        final double oy, oz, cx, cy, sx, sy, sz;
        final Double inflate;
        final Map<String, Rect> uv;

        Cube(String bone, double ox, double oy, double oz, double sx, double sy, double sz, Double inflate, Map<String, Rect> uv) {
            this.bone = bone;
            this.oy = oy;
            this.oz = oz;
            this.cx = ox + sx / 2.0;
            this.cy = oy + sy / 2.0;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
            this.inflate = inflate;
            this.uv = uv;
        }
    }
}
