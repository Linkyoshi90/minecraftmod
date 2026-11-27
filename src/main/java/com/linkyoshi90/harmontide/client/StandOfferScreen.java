package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The question she asks in her season. Yes starts the standing press. No leaves it. Closing the screen does not answer.
 */
public final class StandOfferScreen extends Screen {
    private final int entityId;
    private final Component question = Component.translatable("message.harmontide.season.ask");

    public StandOfferScreen(int entityId) {
        super(Component.translatable("message.harmontide.season.ask"));
        this.entityId = entityId;
    }

    public static void open(int entityId) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null) return;
        minecraft.setScreen(new StandOfferScreen(entityId));
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
        PacketDistributor.sendToServer(new ModPayloads.StandAnswer(this.entityId, yes));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The screen blurs whatever was drawn before it. The question has to come after, or the words fade and only the buttons stay sharp.
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 150;
        int top = this.height / 2 - 52;
        int right = this.width / 2 + 150;
        graphics.fill(left, top, right, top + 44, 0xF0101010);
        graphics.drawWordWrap(this.font, this.question, left + 10, top + 8, right - left - 20, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
