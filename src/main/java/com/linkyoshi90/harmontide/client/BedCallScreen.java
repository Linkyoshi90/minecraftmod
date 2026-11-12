package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.BedCall;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

/**
 * The vanilla in-bed screen during a call from bed: Leave Bed reads Cancel (it does the same thing), and a button to
 * skip the night appears once someone has joined. The screen stays light while the player lies awake.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class BedCallScreen {
    private static int stage = BedCall.NONE;
    @Nullable
    private static Button leave;
    @Nullable
    private static Button skip;

    private BedCallScreen() {}

    public static void setStage(int newStage) {
        stage = newStage;
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InBedChatScreen screen)) return;
        leave = null;
        for (var listener : event.getListenersList()) {
            if (listener instanceof Button button) leave = button;
        }
        skip = Button.builder(Component.translatable("gui.harmontide.skip_night"), b -> PacketDistributor.sendToServer(new ModPayloads.SkipNight()))
                .bounds(screen.width / 2 - 100, screen.height - 64, 200, 20).build();
        event.addListener(skip);
        refresh();
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof InBedChatScreen) refresh();
    }

    private static void refresh() {
        if (skip != null) skip.visible = stage == BedCall.JOINED;
        if (leave != null) leave.setMessage(Component.translatable(stage == BedCall.NONE ? "multiplayer.stopSleeping" : "gui.cancel"));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (stage != BedCall.NONE && event.getEntity() instanceof LocalPlayer player && player.isSleeping()) player.sleepCounter = 0;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        stage = BedCall.NONE;
    }
}
