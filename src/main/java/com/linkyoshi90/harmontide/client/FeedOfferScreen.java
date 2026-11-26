package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The question she asks when he is hungry and she has milk. Yes starts the feed. No, or closing the screen, leaves it.
 */
public final class FeedOfferScreen extends Screen {
    private final int entityId;
    private final Component question = Component.translatable("message.harmontide.breastfeed.ask");

    public FeedOfferScreen(int entityId) {
        super(Component.translatable("message.harmontide.breastfeed.ask"));
        this.entityId = entityId;
    }

    public static void open(int entityId) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null) return;
        minecraft.setScreen(new FeedOfferScreen(entityId));
    }

    @Override
    protected void init() {
        int y = this.height / 2 + 24;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.harmontide.breastfeed.yes"), b -> this.answer(true))
                .bounds(this.width / 2 - 102, y, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.harmontide.breastfeed.no"), b -> this.answer(false))
                .bounds(this.width / 2 + 4, y, 98, 20).build());
    }

    private void answer(boolean yes) {
        PacketDistributor.sendToServer(new ModPayloads.FeedAnswer(this.entityId, yes));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x88000000);
        graphics.drawWordWrap(this.font, this.question, this.width / 2 - 140, this.height / 2 - 36, 280, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
