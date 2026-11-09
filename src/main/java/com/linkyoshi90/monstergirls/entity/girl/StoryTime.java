package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Story time (2026-11-09). Show a little one a written book and read it to her: the little ones within {@link #GATHER} sit down to
 * listen. Each is {@link #CLOSER} closer (once a day each) and a little further along to grown ({@link #GROWTH} ticks). The book
 * isn't used up: a favourite story can be read every night.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class StoryTime {
    public static final double GATHER = 6;
    public static final int CLOSER = 3, GROWTH = 1200, LISTEN = 200;
    static final String HEARD = "StoryDay";

    private StoryTime() {
    }

    /** Reads the book to the little ones near. How many listened (0 if none was a little one). */
    public static int read(ServerLevel level, net.minecraft.world.entity.Mob asked, Player reader, ItemStack book) {
        if (!asked.isBaby()) return 0;
        long today = level.getDayTime() / 24000L;
        int n = 0;
        for (net.minecraft.world.entity.Mob young : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(asked.blockPosition()).inflate(GATHER),
                m -> m.isBaby() && m instanceof com.linkyoshi90.monstergirls.entity.Bonded)) {
            n++;
            young.getNavigation().stop();
            young.getLookControl().setLookAt(reader);
            if (young instanceof AbstractMonsterGirl g) g.startTimedAction(AbstractMonsterGirl.Action.SITTING, LISTEN);
            level.sendParticles(ParticleTypes.NOTE, young.getX(), young.getEyeY() + 0.4, young.getZ(), 1, 0.2, 0.1, 0.2, 0.3);
            var pd = young.getPersistentData();
            if (pd.contains(HEARD) && pd.getLong(HEARD) == today) continue;
            pd.putLong(HEARD, today);
            com.linkyoshi90.monstergirls.entity.Bonded b = (com.linkyoshi90.monstergirls.entity.Bonded) young;
            b.addAffection(reader, CLOSER);
            b.setGrowTicks(b.getGrowTicks() + GROWTH);
        }
        WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        String title = content == null ? "" : content.title().raw();
        level.playSound(null, reader.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        reader.displayClientMessage(title.isEmpty() ? Component.translatable("message.monstergirls.story_time.untitled", n)
                : Component.translatable("message.monstergirls.story_time", title, n), false);
        return n;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(Items.WRITTEN_BOOK) || !(event.getTarget() instanceof net.minecraft.world.entity.Mob young)
                || !(young instanceof com.linkyoshi90.monstergirls.entity.Bonded) || !young.isBaby()) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level) read(level, young, event.getEntity(), event.getItemStack());
    }
}
