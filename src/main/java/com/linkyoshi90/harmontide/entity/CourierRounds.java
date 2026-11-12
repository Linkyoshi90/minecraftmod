package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.MailboxBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** A courier out on her round (block/MailboxBlockEntity#setOff) lands again when her time is up, wherever her box is. */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class CourierRounds {
    private CourierRounds() {}

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob m && m.tickCount % 20 == 13 && m.level() instanceof ServerLevel level) MailboxBlockEntity.landIfDue(level, m, false);
    }
}
