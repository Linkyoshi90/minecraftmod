package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Dream-Leviathan's blessing as a second row of hearts above your health (leviathan/DreamHeart): violet, starry hearts that take
 * the hurt first and fill again slowly. Drawn as its own HUD layer right after the health bar, pushing the armour bar up a row.
 */
public final class DreamHeartHud {
    private static final ResourceLocation FULL = HarmontideMod.id("hud/dream_heart_full"), HALF = HarmontideMod.id("hud/dream_heart_half"),
            EMPTY = HarmontideMod.id("hud/dream_heart_container");
    private static boolean blessed;
    private static float pool, shownPool;
    private static int flash;

    private DreamHeartHud() {
    }

    public static void update(boolean isBlessed, float value) {
        if (value < pool) flash = 10;
        blessed = isBlessed;
        pool = value;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!blessed || mc.options.hideGui || mc.player == null || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
        shownPool = Mth.lerp(0.25F, shownPool, pool);
        if (flash > 0) flash--;
        int x = g.guiWidth() / 2 - 91, y = g.guiHeight() - mc.gui.leftHeight;
        long t = mc.level == null ? 0 : mc.level.getGameTime();
        for (int i = 0; i < 10; i++) {
            int hx = x + i * 8, hy = y + (Math.round(pool) <= 4 && (t + i * 3) % 12 < 2 ? -1 : 0);
            g.blitSprite(EMPTY, hx, hy, 9, 9);
            float v = shownPool - i * 2;
            if (v >= 1.5F) g.blitSprite(FULL, hx, hy, 9, 9);
            else if (v >= 0.5F) g.blitSprite(HALF, hx, hy, 9, 9);
        }
        mc.gui.leftHeight += 10;
    }
}
