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
 * Monsterization, after Monster Girl Encyclopedia (villagers, pigs and cats are possessed instead, keeping their bodies: {@link #POSSESSED}): the Demon Realm's energy turns the creatures that linger in it into monster girls.
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
    /** Creatures the realm possesses instead of changing: what their young are born as. */
    public static final Map<EntityType<?>, List<GirlSpecies>> POSSESSED = new HashMap<>();
    /** A possessed creature breeds at most this often (half a day), with one of her own kind within BREED_RANGE, unless CROWD girls already live within 32. */
    public static final int BREED_EVERY = 12000, BREED_RANGE = 8, CROWD = 6;
    private static final String POSSESSED_TAG = "RealmPossessed", NEXT_BREED = "RealmNextBreed";

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
        KINDS.put(EntityType.OCELOT, List.of(GirlSpecies.OCELOT_NEKOMATA));
        KINDS.put(EntityType.WOLF, List.of(GirlSpecies.WOLF_CANID));
        KINDS.put(EntityType.POLAR_BEAR, List.of(GirlSpecies.POLAR_BEAR_CANID));
        KINDS.put(EntityType.PANDA, List.of(GirlSpecies.PANDA_CANID));
        KINDS.put(EntityType.FOX, List.of(GirlSpecies.RED_KITSUNE));
        KINDS.put(EntityType.RABBIT, List.of(GirlSpecies.JACK_JACKALOPE));
        KINDS.put(EntityType.COW, List.of(GirlSpecies.HOLSTAUR_MINOTAUR));
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
        KINDS.put(EntityType.IRON_GOLEM, List.of(GirlSpecies.GOLEM_CONSTRUCT));
        KINDS.put(EntityType.SNOW_GOLEM, List.of(GirlSpecies.SNOWGIRL_CONSTRUCT));
        // possessed: they keep their own bodies, and their young are girls (the list is weighted by repeats)
        POSSESSED.put(EntityType.VILLAGER, List.of(GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.DEMON_DEVIL));
        POSSESSED.put(EntityType.PIG, List.of(GirlSpecies.PIGLETTE_ARMOURED));
        POSSESSED.put(EntityType.CAT, List.of(GirlSpecies.BAKENEKO_NEKOMATA, GirlSpecies.BAKENEKO_NEKOMATA, GirlSpecies.CAIT_SITH_NEKOMATA, GirlSpecies.CHESHIRE_NEKOMATA));
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
        if (KINDS.containsKey(e.getType())) step(level, mob, dark(level, mob));
        else if (POSSESSED.containsKey(e.getType())) {
            if (possessed(mob)) breed(level, mob);
            else soak(level, mob, dark(level, mob));
        }
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

    /** One step in the realm for a creature that is possessed rather than changed. True once she is possessed. */
    public static boolean soak(ServerLevel level, Mob mob, boolean dark) {
        if (!POSSESSED.containsKey(mob.getType()) || mob.isBaby() || possessed(mob) || !MonsterGirlsConfig.monsterization()) return possessed(mob);
        if (!dark || !DemonRealm.inRealm(mob)) return false;
        int soaked = soaked(mob) + 1;
        mob.getPersistentData().putInt(SOAKED, soaked);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3 + soaked / 2, 0.3, 0.3, 0.3, 0.0);
        if (soaked < STEPS) return false;
        possess(level, mob);
        return true;
    }

    /** She is possessed, here and now: her own body still, the realm in her, and eager. */
    public static void possess(ServerLevel level, Mob mob) {
        mob.getPersistentData().remove(SOAKED);
        mob.getPersistentData().putBoolean(POSSESSED_TAG, true);
        mob.getPersistentData().putLong(NEXT_BREED, level.getGameTime() + 200);
        mob.setPersistenceRequired();
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + 0.5, mob.getZ(), 30, 0.5, 0.5, 0.5, 0.0);
        mob.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.7F, 1.6F);
        sync(mob);
    }

    /** Tells everyone who can see her whether she is possessed (her eyes glow). */
    public static void sync(Mob mob) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(mob,
                new com.linkyoshi90.monstergirls.network.ModPayloads.Possessed(mob.getId(), possessed(mob)));
    }

    /** A player who comes into sight of a possessed creature learns it too. */
    @SubscribeEvent
    public static void onStartTracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Mob mob && possessed(mob) && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.linkyoshi90.monstergirls.network.ModPayloads.Possessed(mob.getId(), true));
        }
    }

    public static boolean possessed(Mob mob) {
        return mob.getPersistentData().getBoolean(POSSESSED_TAG);
    }

    /** Holy water drives it out. True if there was something to drive out. */
    public static boolean exorcise(ServerLevel level, Mob mob) {
        if (!possessed(mob)) return false;
        mob.getPersistentData().remove(POSSESSED_TAG);
        mob.getPersistentData().remove(NEXT_BREED);
        level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        sync(mob);
        return true;
    }

    /** What a possessed creature's young is born as, or null if she isn't one. */
    @Nullable
    public static GirlSpecies offspringOf(Mob mob) {
        List<GirlSpecies> kinds = POSSESSED.get(mob.getType());
        return kinds == null || !possessed(mob) ? null : kinds.get(mob.getRandom().nextInt(kinds.size()));
    }

    /**
     * A possessed creature is eager: whenever she can (every BREED_EVERY), she finds a grown one of her own kind near her and
     * their young is a girl. Not while CROWD girls already live within 32 blocks. The young, or null.
     */
    @Nullable
    public static AbstractMonsterGirl breed(ServerLevel level, Mob mob) {
        if (!possessed(mob) || mob.isBaby() || !MonsterGirlsConfig.monsterization() || level.getGameTime() < mob.getPersistentData().getLong(NEXT_BREED)) return null;
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
        Mob partner = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(BREED_RANGE),
                m -> m != mob && m.getType() == mob.getType() && !m.isBaby() && m.isAlive()).stream().findFirst().orElse(null);
        if (partner == null) return null;
        if (level.getEntitiesOfClass(AbstractMonsterGirl.class, mob.getBoundingBox().inflate(32)).size() >= CROWD) return null;
        GirlSpecies kind = offspringOf(mob);
        if (kind == null) return null;
        mob.getPersistentData().putLong(NEXT_BREED, level.getGameTime() + BREED_EVERY);
        for (Mob m : List.of(mob, partner)) level.sendParticles(ParticleTypes.HEART, m.getX(), m.getY() + m.getBbHeight() + 0.3, m.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        AbstractMonsterGirl young = AbstractMonsterGirl.youngOf(level, ModGirls.type(kind), mob.blockPosition(), null);
        if (young != null) level.sendParticles(RealmGrassBlock.HAZE, young.getX(), young.getY() + 0.5, young.getZ(), 12, 0.3, 0.3, 0.3, 0.0);
        return young;
    }

    /** She changes, here and now. The girl, or null if the change didn't take. */
    @Nullable
    public static AbstractMonsterGirl change(ServerLevel level, Mob mob, GirlSpecies kind) {
        @Nullable UUID owner = mob instanceof OwnableEntity pet ? pet.getOwnerUUID() : null;
        AbstractMonsterGirl girl = mob.convertTo(ModGirls.type(kind), false);
        if (girl == null) return null;
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()), MobSpawnType.CONVERSION, null);
        if (girl instanceof com.linkyoshi90.monstergirls.entity.girl.Minotaur cow) cow.setMale(false);   // the realm turns cows, not bulls
        girl.setPersistenceRequired();                                  // a far player must not despawn the one creature this happened to
        girl.getPersistentData().remove(SOAKED);
        girl.getPersistentData().putBoolean(MONSTERIZED, true);
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

    private static final String PENDING_OWNER = "MonsterizedOwner", MONSTERIZED = "Monsterized";
    /** A changed girl's odds of joining a sleeping player, friend or not; affection only adds to it. */
    public static final float EAGER = 1.0F;

    /** Changed by the realm: she still burns with it, and is eager for any sleeper's bed (as a succubus is), friend or not. */
    public static boolean monsterized(net.minecraft.world.entity.Mob mob) {
        return mob.getPersistentData().getBoolean(MONSTERIZED);
    }

    /** A line for /monstergirls bonds, or null. */
    @Nullable
    public static Component describe(net.minecraft.world.entity.Mob mob) {
        return monsterized(mob) ? Component.translatable("bond.monstergirls.monsterized").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE) : null;
    }

    /** Now and then the realm's haze still rises from her. */
    @SubscribeEvent
    public static void onHaze(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 61 != 0 || !(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)
                || !monsterized(girl)) return;
        level.sendParticles(RealmGrassBlock.HAZE, girl.getX(), girl.getY() + girl.getBbHeight() * 0.6, girl.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
    }

    /** Her odds tonight with a player (0 if she isn't one who changed). */
    public static float eagerness(AbstractMonsterGirl girl, Player player) {
        return monsterized(girl) ? Math.min(1.0F, EAGER + girl.getAffection(player) / 400.0F) : 0.0F;
    }

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
