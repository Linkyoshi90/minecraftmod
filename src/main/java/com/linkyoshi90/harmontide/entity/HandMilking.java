package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.block.MilkingMachineBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.network.ModPayloads;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A bucket, when the private adult option is on, for any grown girl who can share a bed.
 * Expecting, or the day after, does not turn the bucket away: those are days she has milk.
 * She blushes and her top comes off (a texture made from her own model), then the close-up opens.
 * Cattle already have a lean clip. Everyone else keeps idle and leans in the same pose on the client, so a missing clip cannot freeze her.
 * Drag from the top of a breast down to the nipple. Afterwards she wipes them and the top comes back.
 * Off, or for anyone young, of childlike height, male, or her own father, a cow's bucket still fills at once.
 * The milking machine is a different path and does not come here.
 */
@EventBusSubscriber(modid = com.linkyoshi90.harmontide.HarmontideMod.MODID)
public final class HandMilking {
    /** Downward pulls, top of the breast to the nipple, before the pail fills. */
    public static final int STROKES = 8;
    /** Ticks the server demands between pulls, so a packet flood cannot skip the dragging. */
    public static final int MIN_GAP = 3;
    private static final double REACH_SQR = 64.0;
    private static final int IDLE = 600;
    /** Ticks she stands bare in the world before the close-up covers the view. */
    private static final int REVEAL = 35;
    /** After the last pull: the wipe on the screen, then a moment of her still bare, then the top. */
    private static final int REDRESS = 72;
    private static final Map<UUID, Session> OPEN = new HashMap<>();
    private static final Map<UUID, Pending> WAITING = new HashMap<>();
    private static int nextToken = 1;

    private record Session(int token, int girlId, InteractionHand hand, boolean bottle, int strokes, long touched) {}
    private record Pending(int token, long at) {}

    private HandMilking() {}

    /** Any grown girl the close-up can draw. Never a baby, and a bull is not a girl. */
    public static boolean compatible(LivingEntity girl) {
        if (girl.isBaby()) return false;
        if (girl instanceof Minotaur mino && mino.isMale()) return false;
        return girl instanceof AbstractMonsterGirl || girl instanceof AbstractHarpy || girl instanceof AbstractAnuran;
    }

    /** The close-up, rather than the old instant pail. A current pregnancy does not count her out. */
    public static boolean interested(LivingEntity girl, Player player) {
        if (!(girl instanceof Mob mob) || !Nsfw.on() || !compatible(girl) || !(mob instanceof Bonded bonded)) return false;
        UUID father = bonded.getFather();
        if (father != null && father.equals(player.getUUID())) return false;
        return bonded.getGestation().ignoringTerm(bonded::canShareBed);
    }

    /** She has milk ready, so the scene starts. Otherwise she says she has none. The bucket stays in hand. */
    public static void tryBegin(ServerLevel level, LivingEntity girl, ServerPlayer player, InteractionHand hand) {
        if (!ready(girl, level)) {
            player.displayClientMessage(Component.translatable("message.harmontide.no_milk", girl.getDisplayName()), true);
            return;
        }
        offer(level, girl, player, hand, false);
    }

    private static boolean ready(LivingEntity girl, ServerLevel level) {
        if (girl instanceof Minotaur mino) return mino.isMilkReady();
        if (girl instanceof Centaur centaur && centaur.isCamel()) return centaur.isMilkReady();
        return MilkingMachineBlockEntity.isReady(girl, level.getGameTime());
    }

    /**
     * She undresses and, a moment later, the close-up opens. The bucket stays in hand.
     * False when the option is off or she is not a grown girl: the caller fills the pail the old way.
     */
    public static boolean offer(ServerLevel level, LivingEntity girl, ServerPlayer player, InteractionHand hand, boolean bottle) {
        if (!interested(girl, player)) return false;
        int token = nextToken++;
        OPEN.put(player.getUUID(), new Session(token, girl.getId(), hand, bottle, 0, level.getGameTime()));
        undress(girl, 600);
        player.displayClientMessage(Component.translatable("message.harmontide.milk_undress", girl.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.2, girl.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
        WAITING.put(player.getUUID(), new Pending(token, level.getGameTime() + REVEAL));
        return true;
    }

    public static boolean isOpen(Player player) {
        return OPEN.containsKey(player.getUUID());
    }

    /** Pulls counted on the open session, or 0 when there is none. */
    public static int strokes(Player player) {
        Session s = OPEN.get(player.getUUID());
        return s == null ? 0 : s.strokes;
    }

    /** One pull. The server counts. Too soon, too far, or the wrong vessel, and it does not. */
    public static boolean stroke(ServerPlayer player) {
        Session s = OPEN.get(player.getUUID());
        return s != null && stroke(player, s.token);
    }

    public static boolean stroke(ServerPlayer player, int token) {
        Session s = OPEN.get(player.getUUID());
        if (s == null || s.token != token || !(player.level() instanceof ServerLevel level)) return false;
        if (!present(player, s)) {
            cancel(player, s);
            return false;
        }
        long now = level.getGameTime();
        if (now - s.touched < MIN_GAP) return false;
        if (!(level.getEntity(s.girlId) instanceof LivingEntity girl) || !compatible(girl)) {
            cancel(player, s);
            return false;
        }
        int next = s.strokes + 1;
        if (next < STROKES) {
            OPEN.put(player.getUUID(), new Session(s.token, s.girlId, s.hand, s.bottle, next, now));
            undress(girl, 600);
            squirt(level, girl);
            Lactation.drip(level, girl);
            send(player, new ModPayloads.MilkProgress(s.token, next, STROKES, false));
            return true;
        }
        OPEN.remove(player.getUUID());
        WAITING.remove(player.getUUID());
        dress(girl, REDRESS);
        if (girl instanceof Bonded bonded) bonded.blushFor(120);
        if (girl instanceof Minotaur posed) posed.startTimedAction(AbstractMonsterGirl.Action.MILK, 40);
        ItemStack stack = player.getItemInHand(s.hand);
        if (girl instanceof Minotaur mino) mino.giveMilk(level, player, stack, s.bottle);
        else if (girl instanceof Centaur camel && camel.isCamel()) camel.giveCamelMilk(level, player, stack);
        else giveFamilyMilk(level, girl, player, stack);
        player.displayClientMessage(Component.translatable("message.harmontide.milk_empty", girl.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY(), girl.getZ(), 4, 0.2, 0.15, 0.2, 0.0);
        send(player, new ModPayloads.MilkProgress(token, STROKES, STROKES, true));
        return true;
    }

    private static void undress(LivingEntity girl, int ticks) {
        if (girl instanceof Bonded bonded) bonded.blushFor(ticks);
        if (girl instanceof AbstractMonsterGirl g) {
            g.bareFor(ticks);
            if (g instanceof Minotaur && (g.getAction() == AbstractMonsterGirl.Action.NONE || g.getAction() == AbstractMonsterGirl.Action.SITTING
                    || g.getAction() == AbstractMonsterGirl.Action.MILK)) {
                g.startTimedAction(AbstractMonsterGirl.Action.MILK, 220);   // her file has the clip; other kinds lean in MilkPose
            }
        } else if (girl instanceof AbstractHarpy harpy) harpy.bareFor(ticks);
        else if (girl instanceof AbstractAnuran anuran) anuran.bareFor(ticks);
    }

    private static void dress(LivingEntity girl, int ticks) {
        if (girl instanceof AbstractMonsterGirl g) g.dressAfter(ticks);
        else if (girl instanceof AbstractHarpy harpy) harpy.dressAfter(ticks);
        else if (girl instanceof AbstractAnuran anuran) anuran.dressAfter(ticks);
    }

    private static void squirt(ServerLevel level, LivingEntity girl) {
        girl.playSound(SoundEvents.COW_MILK, 0.4F, 1.45F);
        level.playSound(null, girl.blockPosition(), SoundEvents.BUCKET_FILL, girl.getSoundSource(), 0.35F, 1.3F);
    }

    /** Her clade's milk, or a plain pail when her clade has none. */
    private static void giveFamilyMilk(ServerLevel level, LivingEntity girl, ServerPlayer player, ItemStack stack) {
        FamilyMilk milk = FamilyMilk.of(girl);
        stack.consume(1, player);
        MilkingMachineBlockEntity.markMilked(girl, level.getGameTime() + Minotaur.MILK_COOLDOWN);
        Lactation.relieve(level, girl, player);
        ItemStack filled = milk == null ? new ItemStack(Items.MILK_BUCKET) : new ItemStack(ModItems.MILK.get(milk).get());
        filled = Dairy.flavoured(girl, filled);
        if (!player.getInventory().add(filled)) player.drop(filled, false);
        Dairy.recordMilk(player, girl);
        if (girl instanceof Bonded bonded) bonded.addAffection(player, 1);
        girl.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
    }

    /** She closed the screen, or the test did. Nothing is given. */
    public static void close(ServerPlayer player) {
        WAITING.remove(player.getUUID());
        Session s = OPEN.remove(player.getUUID());
        if (s != null) stopPosing(player, s);
    }

    public static void close(ServerPlayer player, int token) {
        Session s = OPEN.get(player.getUUID());
        if (s == null || s.token != token) return;
        WAITING.remove(player.getUUID());
        OPEN.remove(player.getUUID());
        stopPosing(player, s);
    }

    private static void cancel(ServerPlayer player, Session s) {
        WAITING.remove(player.getUUID());
        if (OPEN.remove(player.getUUID()) == null) return;
        stopPosing(player, s);
        send(player, new ModPayloads.MilkProgress(s.token, s.strokes, 0, false));
    }

    private static void stopPosing(ServerPlayer player, Session s) {
        if (!(player.level().getEntity(s.girlId) instanceof LivingEntity girl)) return;
        dress(girl, 0);
        if (girl instanceof AbstractMonsterGirl posed && posed.getAction() == AbstractMonsterGirl.Action.MILK) {
            posed.startTimedAction(AbstractMonsterGirl.Action.NONE, 1);
        }
    }

    private static boolean present(ServerPlayer player, Session s) {
        if (!(player.level().getEntity(s.girlId) instanceof LivingEntity girl) || !girl.isAlive()) return false;
        if (girl.distanceToSqr(player) > REACH_SQR) return false;
        if (player.level().getGameTime() - s.touched > IDLE) return false;
        return holding(player, s);
    }

    private static boolean holding(ServerPlayer player, Session s) {
        ItemStack stack = player.getItemInHand(s.hand);
        return s.bottle ? stack.is(Items.GLASS_BOTTLE) : stack.is(Items.BUCKET);
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload.type())) PacketDistributor.sendToPlayer(player, payload);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) return;
        if (player.level() instanceof ServerLevel level) openDue(player, level);
        if (player.tickCount % 10 != 0) return;
        Session s = OPEN.get(player.getUUID());
        if (s != null && !present(player, s)) cancel(player, s);
    }

    private static void openDue(ServerPlayer player, ServerLevel level) {
        Pending pending = WAITING.get(player.getUUID());
        if (pending == null || level.getGameTime() < pending.at) return;
        WAITING.remove(player.getUUID());
        Session s = OPEN.get(player.getUUID());
        if (s != null && s.token == pending.token) send(player, new ModPayloads.OpenMilk(s.token, s.girlId, STROKES));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) close(player);
    }
}
