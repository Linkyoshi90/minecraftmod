package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.MailboxBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** A courier out on her round (block/MailboxBlockEntity#setOff) lands again when her time is up, wherever her box is. */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class CourierRounds {
    private CourierRounds() {}

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob m && m.tickCount % 20 == 13 && m.level() instanceof ServerLevel level) MailboxBlockEntity.landIfDue(level, m, false);
    }
}
