package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The bucket close-up, from the front. Her top is already off on the model. She leans over a pail and her breasts
 * hang with the nipples pointing down. A drag from the top of a breast to the nipple draws the milk.
 * The server counts the pulls. When this closes she is still bare for a moment, then the top comes back.
 */
public final class MilkingScreen extends Screen {
    /** The lean over the pail, then the pulls. A pull is this many pixels downward. */
    private static final int LEAN = 20, SWIPE = 52;
    private static final int INTRO = LEAN;
    /** After the last pull she wipes her nipples. The top itself comes back on the model, once this closes. */
    private static final int WIPE = 36;
    private final int token;
    private final int skin;
    private final int nipple;
    private final int areola;
    private int need;
    private int strokes;
    private int beat;
    private int outro;
    private int frame;
    private int pending;
    private int lastSent = -100;
    private int hold;
    private boolean sealed;
    private boolean told;
    private int dragSide = -1;
    private double dragStartY;
    private double dragY;
    private boolean pulled;
    private final int[] splashFrame = new int[12];
    private final int[] splashSide = new int[12];
    private int splashes;
    private int leftX, rightX, breastTop, nippleY, bucketTop;

    private MilkingScreen(Component title, int token, int skin, int need) {
        super(title);
        this.token = token;
        this.skin = skin & 0xFFFFFF;
        int rose = mix(shade(this.skin, 0.75F), 0xC46A78, 0.58F);
        this.nipple = rose;
        this.areola = mix(this.skin, rose, 0.42F);
        this.need = Math.max(1, need);
    }

    /** Server to client. A gametest server has no screen to open. */
    public static void open(int token, int entityId, int need) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        int skin = 0xE8C4B0;
        Component title = Component.translatable("gui.harmontide.milk_slip");
        Entity entity = mc.level == null ? null : mc.level.getEntity(entityId);
        if (entity instanceof AbstractMonsterGirl girl) {
            skin = girl.getSpecies().eggColor();
            title = girl.getDisplayName();
        }
        mc.setScreen(new MilkingScreen(title, token, skin, need));
    }

    /** The server's count. need 0 means the session was dropped. */
    public static void progress(int token, int strokes, int need, boolean done) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof MilkingScreen screen) || screen.token != token) return;
        if (need <= 0) {
            screen.told = true;
            screen.onClose();
            return;
        }
        screen.strokes = strokes;
        screen.need = need;
        if (done) {
            screen.sealed = true;
            screen.hold = WIPE + 12;
            screen.pending = 0;
            screen.dragSide = -1;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.frame++;
        if (!this.sealed && this.beat < INTRO) this.beat++;
        if (this.sealed) this.outro++;
        if (this.pending > 0 && this.milking() && this.frame - this.lastSent >= 3) {
            this.pending--;
            this.lastSent = this.frame;
            PacketDistributor.sendToServer(new ModPayloads.MilkStroke(this.token));
        }
        if (this.sealed && this.hold > 0 && --this.hold == 0) this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.place();
        int cx = this.width / 2;
        graphics.fill(0, 0, this.width, this.height, 0xF0100C10);
        graphics.fill(cx - 132, this.breastTop - 132, cx + 132, this.bucketTop + 52, 0xFF1A1418);
        this.bucket(graphics, cx);
        this.head(graphics, cx);
        float press = this.dragSide >= 0 ? this.press() : 0;
        this.hang(graphics, this.leftX, press > 0 && this.dragSide == 0 ? press : 0);
        this.hang(graphics, this.rightX, press > 0 && this.dragSide == 1 ? press : 0);
        if (this.milking()) {
            this.streams(graphics);
            this.hands(graphics, mouseY);
        }
        if (this.sealed) this.wipe(graphics);
        this.caption(graphics, cx);
    }

    private void place() {
        int cx = this.width / 2;
        int base = this.height / 2;
        float lean = this.lean();
        this.leftX = cx - 58;
        this.rightX = cx + 58;
        this.breastTop = base - 36 + (int) (20 * lean);
        this.nippleY = this.breastTop + 70;
        this.bucketTop = this.nippleY + 22 + (int) (8 * (1.0F - lean));
    }

    private float lean() {
        if (this.beat >= INTRO) return 1;
        float u = this.beat / (float) LEAN;
        return u * u * (3.0F - 2.0F * u);
    }

    private boolean milking() {
        return this.beat >= INTRO && !this.sealed;
    }

    private float press() {
        if (this.dragSide < 0) return 0;
        return clampUnit((float) ((this.dragY - this.breastTop) / (double) Math.max(1, this.nippleY - this.breastTop)));
    }

    private void head(GuiGraphics graphics, int cx) {
        int y = this.breastTop - 78;
        disc(graphics, cx, y, 22, 24, opaque(shade(this.skin, 0.96F)));
        disc(graphics, cx - 9, y - 2, 3, 4, 0xFF2A2428);
        disc(graphics, cx + 9, y - 2, 3, 4, 0xFF2A2428);
        disc(graphics, cx - 9, y + 1, 2, 2, 0xFF161216);
        disc(graphics, cx + 9, y + 1, 2, 2, 0xFF161216);
        int blush = this.sealed ? 0xF2A0A8 : 0xE8B0B4;
        disc(graphics, cx - 16, y + 6, 6, 4, blush | 0xFF000000);
        disc(graphics, cx + 16, y + 6, 6, 4, blush | 0xFF000000);
        graphics.fill(cx - 28, y + 22, cx + 28, y + 30, opaque(shade(this.skin, 0.9F)));
    }

    /** One full drop: wide through the body, narrowing only into the nipple. */
    private void hang(GuiGraphics graphics, int x, float squeeze) {
        int rx = 48 - (int) (6 * squeeze);
        int top = this.breastTop + (int) (4 * squeeze);
        int nip = this.nippleY;
        int span = Math.max(8, nip - top);
        int tip = 8;
        for (int y = top; y <= nip + tip; y++) {
            float u = (y - top) / (float) span;
            float width;
            int rgb;
            if (u <= 1.0F) {
                width = rx * profile(u);
                float light = 0.78F + 0.18F * (1.0F - u);
                rgb = shade(this.skin, light);
            } else {
                float v = Math.min(1.0F, (u - 1.0F) / (tip / (float) span));
                float bulge = (float) Math.sin(Math.PI * v);
                float root = profile(1.0F);
                width = rx * root * (1.0F - 0.50F * v + 0.12F * bulge);
                rgb = mix(this.areola, this.nipple, 0.35F + 0.65F * v);
            }
            int hw = Math.max(1, Math.round(width));
            int edge = opaque(shade(rgb, 0.62F));
            int body = opaque(rgb);
            graphics.fill(x - hw, y, x + hw, y + 1, body);
            int ar = Math.round(rx * 0.30F);
            int dy = y - nip;
            if (dy * dy < ar * ar) {
                int spot = (int) Math.round(Math.sqrt(ar * (double) ar - dy * (double) dy));
                spot = Math.min(spot, Math.max(1, hw - 2));
                float t = 0.30F + 0.50F * (1.0F - (float) Math.sqrt(dy * dy) / ar);
                int rose = y >= nip ? this.nipple : this.areola;
                graphics.fill(x - spot, y, x + spot, y + 1, opaque(mix(rgb, rose, t)));
            }
            if (hw > 3) {
                int inset = Math.max(1, hw / 5);
                graphics.fill(x - hw, y, x - hw + inset, y + 1, edge);
                graphics.fill(x + hw - inset, y, x + hw, y + 1, edge);
                if (u < 0.62F) {
                    int hx = x - hw / 3;
                    graphics.fill(hx - 1, y, hx + Math.max(2, hw / 5), y + 1, opaque(shade(this.skin, 1.08F)));
                }
            }
        }
    }

    /** Width, as a fraction of the fullest point, from the chest (0) to the nipple (1). */
    private static float profile(float u) {
        float[] at = {0.0F, 0.16F, 0.36F, 0.58F, 0.78F, 0.90F, 1.0F};
        float[] w = {0.58F, 0.86F, 1.0F, 1.0F, 0.94F, 0.78F, 0.58F};
        if (u <= 0.0F) return w[0];
        if (u >= 1.0F) return w[w.length - 1];
        int i = 0;
        while (i < at.length - 2 && u > at[i + 1]) i++;
        float t = (u - at[i]) / (at[i + 1] - at[i]);
        t = t * t * (3.0F - 2.0F * t);
        return w[i] + (w[i + 1] - w[i]) * t;
    }

    private void bucket(GuiGraphics graphics, int cx) {
        if (this.lean() < 0.05F && !this.sealed) return;
        int top = this.bucketTop;
        int tall = 32;
        for (int i = 0; i < tall; i++) {
            float u = i / (float) tall;
            int w = (int) (34 * (1.08F - 0.22F * u));
            graphics.fill(cx - w, top + i, cx + w, top + i + 1, i < 3 ? 0xFFE4E8EE : 0xFFB7BEC8);
        }
        int innerTop = top + 6;
        int innerBot = top + tall - 3;
        for (int i = innerTop; i < innerBot; i++) {
            float u = (i - innerTop) / (float) (innerBot - innerTop);
            int w = (int) (26 * (1.02F - 0.16F * u));
            graphics.fill(cx - w, i, cx + w, i + 1, 0xFF2A3138);
        }
        float fill = this.strokes / (float) this.need;
        int milkTop = (int) (innerBot - (innerBot - innerTop - 2) * fill);
        for (int i = milkTop; i < innerBot; i++) {
            float u = (i - innerTop) / (float) (innerBot - innerTop);
            int w = (int) (24 * (1.0F - 0.14F * u));
            graphics.fill(cx - w, i, cx + w, i + 1, 0xFFF7FBFF);
        }
    }

    private void streams(GuiGraphics graphics) {
        if (this.strokes > 0) {
            this.drip(graphics, this.leftX, 1);
            this.drip(graphics, this.rightX, 0);
        }
        for (int i = 0; i < this.splashes; i++) {
            int age = this.frame - this.splashFrame[i];
            if (age < 0 || age > 14) continue;
            int x = this.splashSide[i] == 0 ? this.leftX : this.rightX;
            int y = this.bucketTop + 8;
            int r = 2 + age / 4;
            disc(graphics, x, y, r, 2, 0xFFF7FBFF);
            disc(graphics, x - 6 - age, y - age, 2, 3, 0xFFF4F8FF);
            disc(graphics, x + 6 + age, y - age / 2, 2, 3, 0xFFF4F8FF);
        }
    }

    private void drip(GuiGraphics graphics, int x, int offset) {
        int fall = (this.frame * 3 + offset * 7) % 18;
        graphics.fill(x - 1, this.nippleY + 8, x + 1, this.bucketTop + 4, 0xEEF7FBFF);
        disc(graphics, x, this.nippleY + 10 + fall, 2, 3, 0xFFF7FBFF);
    }

    private void hands(GuiGraphics graphics, int mouseY) {
        if (this.dragSide < 0 || !this.milking()) return;
        int x = this.dragSide == 0 ? this.leftX : this.rightX;
        int y = (int) Math.max(this.breastTop + 6, Math.min(this.nippleY - 4, mouseY));
        this.hand(graphics, x, y, opaque(shade(this.skin, 0.9F)));
    }

    private void wipe(GuiGraphics graphics) {
        if (this.outro > WIPE) return;
        float u = this.outro / (float) WIPE;
        float trip = u < 0.5F ? u / 0.5F : 1.0F - (u - 0.5F) / 0.5F;
        int y = (int) (this.breastTop + 8 + (this.nippleY - this.breastTop - 8) * trip);
        int tone = opaque(shade(this.skin, 0.9F));
        this.hand(graphics, this.leftX, y, tone);
        this.hand(graphics, this.rightX, y, tone);
    }

    private void hand(GuiGraphics graphics, int x, int y, int tone) {
        disc(graphics, x, y, 10, 8, tone);
        graphics.fill(x - 8, y - 10, x - 5, y - 2, tone);
        graphics.fill(x - 3, y - 12, x, y - 2, tone);
        graphics.fill(x + 2, y - 11, x + 5, y - 2, tone);
        graphics.fill(x + 6, y - 9, x + 9, y - 2, tone);
    }

    private void caption(GuiGraphics graphics, int cx) {
        Component hint;
        if (this.sealed) hint = Component.translatable("gui.harmontide.milk_wipe");
        else if (this.beat < INTRO) hint = Component.translatable("gui.harmontide.milk_lean");
        else if (this.strokes >= this.need) hint = Component.translatable("gui.harmontide.milk_empty");
        else hint = Component.translatable("gui.harmontide.milk_hint");
        center(graphics, this.title, cx, this.breastTop - 124, 0xFFFFF4F6);
        center(graphics, hint, cx, this.bucketTop + 40, 0xFFF0D0D4);
        center(graphics, Component.literal(this.strokes + " / " + this.need), cx, this.bucketTop + 28, 0xFFD8C4BE);
    }

    private void center(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(this.font, text, x - this.font.width(text) / 2, y, color, false);
    }

    private int sideAt(double mouseX, double mouseY) {
        if (mouseY < this.breastTop - 4 || mouseY > this.nippleY + 16) return -1;
        if (Math.abs(mouseX - this.leftX) <= 52) return 0;
        if (Math.abs(mouseX - this.rightX) <= 52) return 1;
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.place();
        if (button == 0 && this.milking()) {
            int side = this.sideAt(mouseX, mouseY);
            if (side >= 0 && mouseY < this.breastTop + (this.nippleY - this.breastTop) * 0.62) {
                this.dragSide = side;
                this.dragStartY = mouseY;
                this.dragY = mouseY;
                this.pulled = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) this.dragSide = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || this.dragSide < 0 || !this.milking()) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        this.place();
        this.dragY = mouseY;
        if (!this.pulled && mouseY - this.dragStartY >= SWIPE) {
            this.pulled = true;
            this.pending = Math.min(this.pending + 1, this.need);
            this.splashFrame[this.splashes % this.splashFrame.length] = this.frame;
            this.splashSide[this.splashes % this.splashSide.length] = this.dragSide;
            this.splashes++;
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!this.told) {
            this.told = true;
            if (!this.sealed) PacketDistributor.sendToServer(new ModPayloads.CloseMilk(this.token));
        }
        super.onClose();
    }

    private static void disc(GuiGraphics graphics, int cx, int cy, int rx, int ry, int argb) {
        if (rx < 1 || ry < 1) return;
        for (int dy = -ry; dy <= ry; dy++) {
            double span = 1.0 - (dy * (double) dy) / (ry * (double) ry);
            if (span <= 0) continue;
            int w = Math.max(1, (int) Math.round(rx * Math.sqrt(span)));
            graphics.fill(cx - w, cy + dy, cx + w, cy + dy + 1, argb);
        }
    }

    private static int shade(int rgb, float mul) {
        int r = clamp((int) (((rgb >> 16) & 255) * mul));
        int g = clamp((int) (((rgb >> 8) & 255) * mul));
        int b = clamp((int) ((rgb & 255) * mul));
        return (r << 16) | (g << 8) | b;
    }

    private static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(bl);
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    private static int clamp(int c) {
        return Math.max(0, Math.min(255, c));
    }

    private static float clampUnit(float c) {
        return Math.max(0.0F, Math.min(1.0F, c));
    }
}
