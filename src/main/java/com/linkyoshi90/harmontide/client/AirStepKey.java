package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A fresh press of jump while in the air asks the server for an air step (entity/Traversal#airStep): a kitsune's spirit leap,
 * a sylph's gust... The server decides whether anyone with you can give one. One ask per time in the air.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class AirStepKey {
    private static boolean wasDown;
    private static boolean asked;

    private AirStepKey() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.screen != null) {
            wasDown = false;
            return;
        }
        boolean down = mc.options.keyJump.isDown();
        if (p.onGround() || p.isInWater() || p.onClimbable()) asked = false;
        if (down && !wasDown && !asked && !p.onGround() && !p.isInWater() && !p.isPassenger() && !p.getAbilities().flying && !p.isFallFlying()) {
            asked = true;
            PacketDistributor.sendToServer(new ModPayloads.AirStep());
        }
        wasDown = down;
    }
}
