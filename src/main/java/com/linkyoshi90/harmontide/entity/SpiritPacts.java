package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Spirit pacts (2026-11-11, after Monster Girl Encyclopedia's four elemental spirits and their sorcerers). Crouch and hand an
 * amethyst shard to a devoted ({@link #DEVOTED}) grown elemental spirit friend: she binds herself to you, and her element goes
 * with you wherever you are, not only by her side (her own gifts near her stay as they were). One spirit of each element per
 * player; each spirit binds to one player. The same again with a shard ends it; her death ends it too.
 * <ul>
 *   <li>FIRE, the salamander: your blows set what they hit alight.</li>
 *   <li>WATER, the undine: water heals you (Regeneration while you're in water or rain).</li>
 *   <li>WIND, the sylph: you are light on your feet (Jump Boost) and quick under the open sky (Speed).</li>
 *   <li>EARTH, the gnome: deep underground (no sky, below sea level) you dig fast (Haste) and stand firm (Resistance).</li>
 * </ul>
 * All four at once: the advancement "elementalist" (its criteria are the elements). Kept in the journal. Config "spiritPacts".
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class SpiritPacts {
    public enum Element { FIRE, WATER, WIND, EARTH }

    public static final int DEVOTED = 60, BURN = 4;
    public static final String ADVANCEMENT = "elementalist";
    private static final String PACTS = "SpiritPacts", ID = "Id", NAME = "Name", BOUND = "PactWith";

    private SpiritPacts() {}

    @Nullable
    public static Element element(GirlSpecies s) {
        return switch (s) {
            case SALAMANDER_LIZARDFOLK -> Element.FIRE;
            case UNDINE_SLIME -> Element.WATER;
            case SYLPH_ELF -> Element.WIND;
            case GNOME_ELF -> Element.EARTH;
            default -> null;
        };
    }

    private static CompoundTag pacts(Player p) {
        return p.getData(ModAttachments.JOURNAL).getCompound(PACTS);
    }

    private static void setPacts(Player p, CompoundTag pacts) {
        CompoundTag journal = p.getData(ModAttachments.JOURNAL);
        journal.put(PACTS, pacts);
        p.setData(ModAttachments.JOURNAL, journal);
    }

    public static boolean has(Player p, Element e) {
        return HarmontideConfig.spiritPacts() && pacts(p).contains(e.name());
    }

    public static Set<Element> held(Player p) {
        Set<Element> out = EnumSet.noneOf(Element.class);
        for (Element e : Element.values()) if (has(p, e)) out.add(e);
        return out;
    }

    @Nullable
    public static UUID boundTo(AbstractMonsterGirl girl) {
        return girl.getPersistentData().hasUUID(BOUND) ? girl.getPersistentData().getUUID(BOUND) : null;
    }

    // --- making and ending a pact -------------------------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level) || !p.isShiftKeyDown()) return;
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.AMETHYST_SHARD) || element(girl.getSpecies()) == null || !HarmontideConfig.spiritPacts()) return;
        UUID bound = boundTo(girl);
        InteractionResult r;
        if (p.getUUID().equals(bound)) {
            r = release(level, girl, p) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else {
            r = bind(level, girl, p) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            if (r == InteractionResult.SUCCESS && !p.getAbilities().instabuild) stack.shrink(1);
        }
        event.setCanceled(true);
        event.setCancellationResult(r);
    }

    /** She binds herself to the player. Whether she would (devoted, grown, not bound elsewhere, their element free). */
    public static boolean bind(ServerLevel level, AbstractMonsterGirl girl, Player p) {
        Element e = element(girl.getSpecies());
        if (e == null || girl.isBaby() || boundTo(girl) != null || girl.getAffection(p) < DEVOTED) {
            p.displayClientMessage(Component.translatable("message.harmontide.pact.refused", girl.getDisplayName()), true);
            return false;
        }
        if (pacts(p).contains(e.name())) {
            p.displayClientMessage(Component.translatable("message.harmontide.pact.taken", Component.translatable("pact.harmontide." + key(e))), true);
            return false;
        }
        CompoundTag entry = new CompoundTag();
        entry.putUUID(ID, girl.getUUID());
        entry.putString(NAME, girl.getDisplayName().getString());
        CompoundTag all = pacts(p);
        all.put(e.name(), entry);
        setPacts(p, all);
        girl.getPersistentData().putUUID(BOUND, p.getUUID());
        level.sendParticles(ParticleTypes.END_ROD, girl.getX(), girl.getY() + girl.getBbHeight() * 0.6, girl.getZ(), 30, 0.5, 0.6, 0.5, 0.05);
        level.playSound(null, girl.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.2F, 1.0F);
        p.displayClientMessage(Component.translatable("message.harmontide.pact.bound", girl.getDisplayName(),
                Component.translatable("pact.harmontide." + key(e))).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (p instanceof ServerPlayer sp) {
            var a = sp.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, key(e));
        }
        return true;
    }

    /** The player ends it. Whether there was one. */
    public static boolean release(ServerLevel level, AbstractMonsterGirl girl, Player p) {
        Element e = element(girl.getSpecies());
        if (e == null || !p.getUUID().equals(boundTo(girl))) return false;
        girl.getPersistentData().remove(BOUND);
        CompoundTag all = pacts(p);
        all.remove(e.name());
        setPacts(p, all);
        p.displayClientMessage(Component.translatable("message.harmontide.pact.released", girl.getDisplayName()), false);
        return true;
    }

    /** A bound spirit dies: her pact dies with her (if her player is about to hear of it). */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)) return;
        Element e = element(girl.getSpecies());
        UUID bound = boundTo(girl);
        if (e == null || bound == null) return;
        Player p = level.getServer().getPlayerList().getPlayer(bound);
        if (p == null) return;
        CompoundTag all = pacts(p);
        if (all.getCompound(e.name()).hasUUID(ID) && all.getCompound(e.name()).getUUID(ID).equals(girl.getUUID())) {
            all.remove(e.name());
            setPacts(p, all);
            p.displayClientMessage(Component.translatable("message.harmontide.pact.broken", girl.getDisplayName()).withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static String key(Element e) {
        return e.name().toLowerCase(Locale.ROOT);
    }

    // --- the elements at work -----------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % 20 == 3 && p.level() instanceof ServerLevel level) tick(level, p);
    }

    /** Once a second: what water, wind and earth do for the bearer. Which elements did something just now. */
    public static Set<Element> tick(ServerLevel level, Player p) {
        Set<Element> acted = EnumSet.noneOf(Element.class);
        if (p.isSpectator()) return acted;
        if (has(p, Element.WATER) && (p.isInWaterOrRain() || level.getFluidState(p.blockPosition()).is(net.minecraft.tags.FluidTags.WATER))) {
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
            acted.add(Element.WATER);
        }
        if (has(p, Element.WIND)) {
            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, true, false, true));
            if (level.canSeeSky(p.blockPosition().above())) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, true));
            acted.add(Element.WIND);
        }
        if (has(p, Element.EARTH) && deep(level, p)) {
            p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false, true));
            acted.add(Element.EARTH);
        }
        return acted;
    }

    /** Deep in the earth: under sea level with no sky overhead. */
    public static boolean deep(ServerLevel level, Player p) {
        return p.getBlockY() < level.getSeaLevel() && !level.canSeeSky(p.blockPosition().above());
    }

    /** Fire: the salamander's bearer sets alight whatever they strike. */
    @SubscribeEvent
    public static void onBlow(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || !(event.getSource().getEntity() instanceof Player p) || event.getSource().getDirectEntity() != p) return;
        if (!has(p, Element.FIRE) || target.fireImmune()) return;
        if (target instanceof Bonded b && b.getAffection(p) >= Orders.AFFECTION) return;    // never a friend
        target.igniteForSeconds(BURN);
    }
}
