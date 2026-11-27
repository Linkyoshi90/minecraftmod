package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Set;

/**
 * While she is with him in the bed, the bars and the sleep fade stay off so the scene can be seen.
 * Out in the world he is not asleep, so a tackle leaves the bars where they are. Chat stays.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class BedSceneHud {
    private static final Set<ResourceLocation> HIDDEN = Set.of(
            VanillaGuiLayers.CROSSHAIR,
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.SELECTED_ITEM_NAME,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.EFFECTS,
            VanillaGuiLayers.SLEEP_OVERLAY
    );
    private static int checked = Integer.MIN_VALUE;
    private static boolean hide;

    private BedSceneHud() {}

    @SubscribeEvent
    public static void onLayer(RenderGuiLayerEvent.Pre event) {
        if (!HIDDEN.contains(event.getName())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        int now = mc.player.tickCount;
        if (checked != now) {
            checked = now;
            hide = inBed(mc);
        }
        if (hide) event.setCanceled(true);
    }

    /** Asleep, and a girl on the bed is in the scene. */
    private static boolean inBed(Minecraft mc) {
        var player = mc.player;
        if (player == null || mc.level == null || !player.isSleeping()) return false;
        AABB box = player.getBoundingBox().inflate(6.0);
        for (Mob mob : mc.level.getEntitiesOfClass(Mob.class, box)) {
            if (posing(mob)) return true;
        }
        return false;
    }

    private static boolean posing(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.KNEEL || action == AbstractMonsterGirl.Action.SITBACK
                    || action == AbstractMonsterGirl.Action.COUPLE || action == AbstractMonsterGirl.Action.ORAL
                    || action == AbstractMonsterGirl.Action.FACE;
        }
        if (mob instanceof AbstractHarpy harpy) {
            AbstractHarpy.Action action = harpy.getAction();
            return action == AbstractHarpy.Action.KNEEL || action == AbstractHarpy.Action.ORAL
                    || action == AbstractHarpy.Action.FACE;
        }
        if (mob instanceof AbstractAnuran anuran) {
            AbstractAnuran.Action action = anuran.getAction();
            return action == AbstractAnuran.Action.KNEEL || action == AbstractAnuran.Action.ORAL
                    || action == AbstractAnuran.Action.FACE;
        }
        return false;
    }
}
