package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Flirting;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Held in the rain, the nearest grown friend within 8 comes and shares it, staying close; every minute under it together is 1
 * friendship, and one who can flirt blushes. Checked once a second.
 */
public class UmbrellaItem extends Item {
    public static final int RANGE = 8, MINUTE = 60;
    /** How near she keeps: the open canopy (models/item/umbrella_open.json) is three blocks across. */
    public static final double CLOSE = 1.2;
    private static final String SHARED = "UmbrellaShared";

    public UmbrellaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player) || entity.tickCount % 20 != 0) return;
        if (!selected && player.getOffhandItem() != stack) return;
        share(server, player);
    }

    /** One second under the umbrella: who shares it, or null. */
    @Nullable
    public static Mob share(ServerLevel level, Player player) {
        Mob friend = null;
        if (level.isRainingAt(player.blockPosition().above())) for (Mob m : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RANGE), m -> m.isAlive() && !m.isBaby() && m instanceof Bonded b && b.getAffection(player) >= HomeLife.FRIEND)) {
            if (friend == null || m.distanceToSqr(player) < friend.distanceToSqr(player)) friend = m;
        }
        if (friend == null || !level.isRainingAt(player.blockPosition().above())) {
            player.getPersistentData().remove(SHARED);
            return null;
        }
        if (friend.distanceToSqr(player) > CLOSE * CLOSE) friend.getNavigation().moveTo(player, 1.1);   // right beside them, under the canopy
        else friend.getNavigation().stop();
        friend.getLookControl().setLookAt(player);
        int shared = player.getPersistentData().getInt(SHARED) + 1;
        player.getPersistentData().putInt(SHARED, shared);
        if (shared == 1) player.displayClientMessage(Component.translatable("message.monstergirls.umbrella", friend.getDisplayName()), true);
        if (shared % MINUTE == 0) {
            ((Bonded) friend).addAffection(player, 1);
            if (Flirting.canFlirt(friend, player)) {
                ((Bonded) friend).blushFor(80);
                level.sendParticles(ParticleTypes.HEART, friend.getX(), friend.getEyeY() + 0.3, friend.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
            }
        }
        return friend;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.monstergirls.umbrella.tip").withStyle(ChatFormatting.GRAY));
    }
}
