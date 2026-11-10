package com.linkyoshi90.monstergirls.entity.order;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.Marriage;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Order's chaplain (2026-11-10): the human counterpart to the realm censer's wedding. A sister the player spared (OrderSister#spare)
 * remembers the mercy: {@link #COMES_AFTER} later, some day the player is near their own hearth and not marked by the Order, she comes
 * back to it, unarmed now, in a chaplain's black and white, and stays {@link #STAY}. While she is there, every wedding and vow renewal
 * at a hearth near her is blessed: she walks to the fire and says the words, the bride is closer to them, the player has the Luck of
 * it for a while, the Order thinks better of them, and the family page remembers who blessed them. Nothing hunts her and she fights
 * nobody. Each sister spared promises one more visit (one at a time).
 */
public final class Chaplain {
    public static final String DUE = "ChaplainDue", NAME = "ChaplainName";
    public static final long COMES_AFTER = 2 * 24000L, STAY = 3 * 24000L;
    public static final int AFFECTION = 4, MERCY = 10, LUCK = 12000, KEEP = 16;
    public static final List<String> NAMES = List.of("Agnes", "Beatrix", "Cecilia", "Clare", "Edith", "Hildegard", "Imelda", "Margery",
            "Odile", "Perpetua", "Rosalind", "Theodora");

    private Chaplain() {
    }

    /** The name she goes by: fixed for a sister, from who she is. */
    public static String nameOf(OrderSister sister) {
        return NAMES.get(Math.floorMod(sister.getUUID().hashCode(), NAMES.size()));
    }

    /** Spared: she promises to come back (unless another already has). */
    public static void promise(ServerLevel level, Player player, OrderSister sister) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        if (journal.contains(DUE)) return;
        journal.putLong(DUE, level.getGameTime() + COMES_AFTER);
        journal.putString(NAME, nameOf(sister));
        player.setData(ModAttachments.JOURNAL, journal);
    }

    /** Whether a chaplain's visit is owed and its day has come (she still won't come while the Order has them marked). */
    public static boolean due(ServerLevel level, Player player) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        return journal.contains(DUE) && level.getGameTime() >= journal.getLong(DUE) && Order.notoriety(player) < Order.MARKED;
    }

    /** Test hook: the visit owed now. */
    public static void dueNow(ServerLevel level, Player player) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.putLong(DUE, level.getGameTime());
        player.setData(ModAttachments.JOURNAL, journal);
    }

    /** Every ten seconds from Order's player tick: a visit due, by day, the player by their own hearth. */
    public static void tick(ServerLevel level, Player player) {
        if (level.dimension() != Level.OVERWORLD || !level.isDay() || player.isSpectator() || !due(level, player)) return;
        BlockPos hearth = Order.ownHearth(level, player, Order.HEARTH_RANGE);
        if (hearth != null) visit(level, player, hearth);
    }

    /** She comes to the hearth. Who came, or null (no ground for her). */
    @Nullable
    public static OrderSister visit(ServerLevel level, Player player, BlockPos hearth) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        String name = journal.contains(NAME) ? journal.getString(NAME) : NAMES.get(0);
        List<OrderSister> came = Order.muster(level, hearth, 3, 6, List.of(OrderSister.Rank.KNIGHT));
        if (came.isEmpty()) return null;
        OrderSister sister = came.get(0);
        sister.becomeChaplain(name, hearth, level.getGameTime() + STAY);
        journal.remove(DUE);
        journal.remove(NAME);
        player.setData(ModAttachments.JOURNAL, journal);
        level.playSound(null, sister.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
        level.sendParticles(ParticleTypes.END_ROD, sister.getX(), sister.getY() + 1.2, sister.getZ(), 16, 0.3, 0.5, 0.3, 0.02);
        player.displayClientMessage(Component.translatable("message.monstergirls.order.chaplain.came", name).withStyle(ChatFormatting.GOLD), false);
        Dairy.award(player, "order_chaplain");
        return sister;
    }

    /** A chaplain staying near this hearth, ready to bless, or null. */
    @Nullable
    public static OrderSister near(ServerLevel level, BlockPos hearth) {
        return level.getEntitiesOfClass(OrderSister.class, new AABB(hearth).inflate(Marriage.GUESTS),
                s -> s.isChaplain() && s.standing()).stream().findFirst().orElse(null);
    }

    /** At a hearth wedding or renewal: her blessing, if she's there. Whether she gave it. */
    public static boolean bless(ServerLevel level, Mob bride, Player player, BlockPos hearth) {
        OrderSister sister = near(level, hearth);
        if (sister == null) return false;
        sister.getNavigation().moveTo(hearth.getX() + 0.5, hearth.getY(), hearth.getZ() + 0.5, 1.0);
        sister.getLookControl().setLookAt(bride);
        level.sendParticles(ParticleTypes.END_ROD, bride.getX(), bride.getEyeY() + 0.6, bride.getZ(), 24, 0.5, 0.4, 0.5, 0.02);
        level.playSound(null, hearth, SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.6F);
        if (bride instanceof Bonded b) b.addAffection(player, AFFECTION);
        player.addEffect(new MobEffectInstance(MobEffects.LUCK, LUCK, 0));
        Order.addNotoriety(player, -MERCY);
        String name = sister.chaplainName();
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);           // the family page remembers who blessed them
        ListTag family = journal.getList(Marriage.FAMILY_PAGE, Tag.TAG_COMPOUND);
        String id = bride.getUUID().toString();
        for (Tag t : family) if (((CompoundTag) t).getString("Id").equals(id)) ((CompoundTag) t).putString("Blessed", name);
        journal.put(Marriage.FAMILY_PAGE, family);
        player.setData(ModAttachments.JOURNAL, journal);
        Component words = Component.translatable("message.monstergirls.order.chaplain.blessed", name, player.getDisplayName(), bride.getDisplayName())
                .withStyle(ChatFormatting.GOLD);
        for (Player p : level.players()) if (p.distanceToSqr(bride) < 64 * 64) p.displayClientMessage(words, false);
        Dairy.award(player, "order_blessing");
        return true;
    }

    /** For the family page. */
    public static Component line(CompoundTag entry) {
        return entry.contains("Blessed") ? Component.translatable("journal.monstergirls.family.blessed", entry.getString("Blessed")) : Component.empty();
    }
}
