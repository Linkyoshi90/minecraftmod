package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Monsterization, after Monster Girl Encyclopedia: the Demon Realm's energy turns the creatures that linger in it into monster girls.
 * A grown vanilla animal or monster standing on realm grass in the dark (at night, or out of the sky) soaks it up, one step every
 * {@link #EVERY} ticks; after {@link #STEPS} she changes into the girl of her kind ({@link #KINDS}): a witch into a succubus, a cow
 * into a holstaur, a slime into a slime girl. Only vanilla creatures change: girls of the mod stay who they are.
 * <p>
 * She keeps her name. A tamed pet keeps her owner, now as a devoted friend ({@link #PET_AFFECTION}). The realm's light rules hold:
 * no realm, no change. Behind the monsterization option.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Monsterization {
    public static final int EVERY = 199, STEPS = 12, PET_AFFECTION = 60;
    private static final String SOAKED = "RealmSoaked";
    public static final Map<EntityType<?>, List<GirlSpecies>> KINDS = new HashMap<>();

    static {
        KINDS.put(EntityType.WITCH, List.of(GirlSpecies.SUCCUBUS_DEVIL));
        KINDS.put(EntityType.VEX, List.of(GirlSpecies.LESSER_DEVIL));
        KINDS.put(EntityType.ZOMBIE, List.of(GirlSpecies.ZOMBIE_UNDEAD));
        KINDS.put(EntityType.HUSK, List.of(GirlSpecies.MUMMY_UNDEAD));
        KINDS.put(EntityType.DROWNED, List.of(GirlSpecies.DROWNED_UNDEAD));
        KINDS.put(EntityType.SKELETON, List.of(GirlSpecies.SKELETON_UNDEAD));
        KINDS.put(EntityType.PHANTOM, List.of(GirlSpecies.PHANTOM_UNDEAD));
        KINDS.put(EntityType.SLIME, List.of(GirlSpecies.VERDANT_SLIME));
        KINDS.put(EntityType.MAGMA_CUBE, List.of(GirlSpecies.MAGMA_SLIME));
        KINDS.put(EntityType.SPIDER, List.of(GirlSpecies.TARANTULA_ARACHNE, GirlSpecies.WOLF_SPIDER_ARACHNE));
        KINDS.put(EntityType.CAVE_SPIDER, List.of(GirlSpecies.CAVE_SPIDER_ARACHNE));
        KINDS.put(EntityType.BEE, List.of(GirlSpecies.BEE_BUG));
        KINDS.put(EntityType.ALLAY, List.of(GirlSpecies.FAIRY_BUG));
        KINDS.put(EntityType.BAT, List.of(GirlSpecies.CAVE_BAT));
        KINDS.put(EntityType.CAT, List.of(GirlSpecies.BAKENEKO_NEKOMATA));
        KINDS.put(EntityType.OCELOT, List.of(GirlSpecies.OCELOT_NEKOMATA));
        KINDS.put(EntityType.WOLF, List.of(GirlSpecies.WOLF_CANID));
        KINDS.put(EntityType.POLAR_BEAR, List.of(GirlSpecies.POLAR_BEAR_CANID));
        KINDS.put(EntityType.PANDA, List.of(GirlSpecies.PANDA_CANID));
        KINDS.put(EntityType.FOX, List.of(GirlSpecies.RED_KITSUNE));
        KINDS.put(EntityType.RABBIT, List.of(GirlSpecies.JACK_JACKALOPE));
        KINDS.put(EntityType.COW, List.of(GirlSpecies.CATTLE_MINOTAUR));
        KINDS.put(EntityType.MOOSHROOM, List.of(GirlSpecies.SHROOMOTAUR_MINOTAUR));
        KINDS.put(EntityType.SHEEP, List.of(GirlSpecies.SHEEP_MINOTAUR));
        KINDS.put(EntityType.GOAT, List.of(GirlSpecies.GOAT_CENTAUR));
        KINDS.put(EntityType.HORSE, List.of(GirlSpecies.HORSE_CENTAUR));
        KINDS.put(EntityType.DONKEY, List.of(GirlSpecies.DONKEY_CENTAUR));
        KINDS.put(EntityType.LLAMA, List.of(GirlSpecies.VICUNERO_CENTAUR));
        KINDS.put(EntityType.CAMEL, List.of(GirlSpecies.CAMEL_CENTAUR));
        KINDS.put(EntityType.ARMADILLO, List.of(GirlSpecies.ARMADILLO_ARMOURED));
        KINDS.put(EntityType.AXOLOTL, List.of(GirlSpecies.AXOLOTL_MERMAID));
        KINDS.put(EntityType.DOLPHIN, List.of(GirlSpecies.DOLPHIN_MERMAID));
        KINDS.put(EntityType.SQUID, List.of(GirlSpecies.SQUIDKIN_MOLLUSK));
    }

    private Monsterization() {
    }

    /** The girl this creature would become, or null if she can't change (not a vanilla kind on the list, or a baby). */
    @Nullable
    public static GirlSpecies kindOf(Mob mob) {
        List<GirlSpecies> kinds = KINDS.get(mob.getType());
        if (kinds == null || mob.isBaby()) return null;
        if (mob instanceof Fox fox && fox.getVariant() == Fox.Type.SNOW) return GirlSpecies.ARCTIC_KITSUNE;
        return kinds.get(Math.floorMod(mob.getUUID().hashCode(), kinds.size()));
    }

    /** Dark enough for the realm to work: night, or under a roof. */
    public static boolean dark(ServerLevel level, Mob mob) {
        return !level.isDay() || !level.canSeeSky(mob.blockPosition().above());
    }

    public static int soaked(Mob mob) {
        return mob.getPersistentData().getInt(SOAKED);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % EVERY != 0 || !(e instanceof Mob mob) || e instanceof Bonded || !(e.level() instanceof ServerLevel level)) return;
        if (!KINDS.containsKey(e.getType())) return;
        step(level, mob, dark(level, mob));
    }

    /** One step in the realm (dark: whether it is dark enough). The girl she became, or null. */
    @Nullable
    public static AbstractMonsterGirl step(ServerLevel level, Mob mob, boolean dark) {
        GirlSpecies kind = kindOf(mob);
        if (kind == null || !MonsterGirlsConfig.monsterization()) return null;
        if (!dark || !DemonRealm.inRealm(mob)) return null;
        int soaked = soaked(mob) + 1;
        mob.getPersistentData().putInt(SOAKED, soaked);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3 + soaked / 2, 0.3, 0.3, 0.3, 0.0);
        return soaked >= STEPS ? change(level, mob, kind) : null;
    }

    /** She changes, here and now. The girl, or null if the change didn't take. */
    @Nullable
    public static AbstractMonsterGirl change(ServerLevel level, Mob mob, GirlSpecies kind) {
        @Nullable UUID owner = mob instanceof OwnableEntity pet ? pet.getOwnerUUID() : null;
        AbstractMonsterGirl girl = mob.convertTo(ModGirls.type(kind), false);
        if (girl == null) return null;
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()), MobSpawnType.CONVERSION, null);
        girl.setPersistenceRequired();                                  // a far player must not despawn the one creature this happened to
        girl.getPersistentData().remove(SOAKED);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY(), girl.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles(RealmGrassBlock.HAZE, girl.getX(), girl.getY() + 1.0, girl.getZ(), 30, 0.5, 0.7, 0.5, 0.0);
        girl.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.7F, 1.4F);
        if (owner != null) {
            Player p = level.getPlayerByUUID(owner);
            if (p != null) {
                girl.addAffection(p, PET_AFFECTION);
                p.displayClientMessage(Component.translatable("message.monstergirls.monsterized.pet", girl.getDisplayName()), false);
            } else {
                girl.getPersistentData().putUUID(PENDING_OWNER, owner);   // she greets them when they're back
            }
        }
        return girl;
    }

    private static final String PENDING_OWNER = "MonsterizedOwner";

    /** An owner who was away when their pet changed: the first time they come near, she is their devoted friend. */
    @SubscribeEvent
    public static void onGirlTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 41 != 0 || !(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)
                || !girl.getPersistentData().hasUUID(PENDING_OWNER)) return;
        Player p = level.getPlayerByUUID(girl.getPersistentData().getUUID(PENDING_OWNER));
        if (p == null || p.distanceToSqr(girl) > 32 * 32) return;
        girl.getPersistentData().remove(PENDING_OWNER);
        girl.addAffection(p, PET_AFFECTION);
        p.displayClientMessage(Component.translatable("message.monstergirls.monsterized.pet", girl.getDisplayName()), false);
    }
}
