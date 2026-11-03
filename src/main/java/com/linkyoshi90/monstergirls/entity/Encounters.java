package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Meetings that start with a small event. One wild grown one in RARITY (encounterRate) waits for a player to come within STAGE, then:
 * WEBBED, caught in cobwebs at her feet and head until someone cuts them; CHASED, a few helmeted zombies after her and she runs to the
 * player until they are dead; LOST, in the rain, she follows the player until she is led under a roof. Helping her is REWARD friendship
 * and a criterion of "every_rescue". An encounter left alone for LIMIT ticks ends by itself. (Found hurt is the balm encounter: Injury.)
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Encounters {
    public enum Kind { WEBBED, CHASED, LOST }

    public static final int RARITY = 40, STAGE = 24, REWARD = 30, LIMIT = 6000, ROOF = 8;
    public static final String FIRST = "first_rescue", EVERY = "every_rescue";
    private static final String KEY = "Encounter", UNTIL = "EncounterUntil", CHASING = "ChasingFor";

    private Encounters() {}

    @Nullable
    public static Kind of(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.contains(KEY)) return null;
        try {
            return Kind.valueOf(data.getString(KEY));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Whether it is under way (a player came near), not just waiting. */
    public static boolean active(Mob mob) {
        return mob.getPersistentData().contains(UNTIL);
    }

    /** A wild spawn may carry an encounter, to begin when a player comes. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (event.isSpawnCancelled() || !(mob instanceof Bonded) || mob.isBaby()) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;   // the tests set their own going
        if (mob.getRandom().nextDouble() >= MonsterGirlsConfig.encounterChance(RARITY)) return;
        Kind kind = Kind.values()[mob.getRandom().nextInt(Kind.values().length)];
        mob.getPersistentData().putString(KEY, kind.name());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 10 != 0 || !(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        if (!mob.getPersistentData().contains(KEY)) return;
        if (!active(mob)) {
            Player p = level.getNearestPlayer(mob, STAGE);
            if (p != null && !p.isSpectator() && !p.isCreative()) begin(level, mob);
            return;
        }
        update(level, mob);
    }

    /** The player is here: it happens. Whether it could (a kind that can't happen here becomes another, or none). */
    public static boolean begin(ServerLevel level, Mob mob) {
        Kind kind = of(mob);
        List<Kind> order = new ArrayList<>(List.of(Kind.values()));
        if (kind != null) {
            order.remove(kind);
            order.add(0, kind);
        }
        for (Kind k : order) if (start(level, mob, k)) return true;
        clear(mob);
        return false;
    }

    /** Sets this encounter going, if it can happen to her here. */
    public static boolean start(ServerLevel level, Mob mob, Kind kind) {
        boolean water = mob instanceof Bonded b && b.needsWater() || mob.isInWater();
        switch (kind) {
            case WEBBED -> {
                if (water) return false;
                BlockPos feet = mob.blockPosition();
                int placed = 0;
                for (BlockPos pos : List.of(feet, feet.above())) {
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
                        placed++;
                    }
                }
                if (placed == 0) return false;
                mob.getNavigation().stop();
            }
            case CHASED -> {
                if (water || level.getDifficulty() == Difficulty.PEACEFUL || level.dimension() != Level.OVERWORLD) return false;
                int n = 2 + mob.getRandom().nextInt(2), made = 0;
                for (int i = 0; i < n; i++) {
                    double a = mob.getRandom().nextDouble() * Math.PI * 2;
                    BlockPos at = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            mob.blockPosition().offset((int) (Math.cos(a) * 9), 0, (int) (Math.sin(a) * 9)));
                    if (Math.abs(at.getY() - mob.getBlockY()) > 6) at = mob.blockPosition().offset((int) (Math.cos(a) * 4), 0, (int) (Math.sin(a) * 4));
                    Zombie z = EntityType.ZOMBIE.create(level);
                    if (z == null) continue;
                    z.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, mob.getRandom().nextFloat() * 360, 0);
                    z.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
                    z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));   // the sun won't end it for you
                    z.setDropChance(EquipmentSlot.HEAD, 0.0F);
                    z.getPersistentData().putUUID(CHASING, mob.getUUID());
                    z.setTarget(mob);
                    level.addFreshEntity(z);
                    made++;
                }
                if (made == 0) return false;
            }
            case LOST -> {
                if (!level.isRainingAt(mob.blockPosition()) && !level.isRaining() || sheltered(level, mob)) return false;
            }
        }
        mob.getPersistentData().putString(KEY, kind.name());
        mob.getPersistentData().putLong(UNTIL, level.getGameTime() + LIMIT);
        for (Player p : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(STAGE))) {
            p.displayClientMessage(Component.translatable("message.monstergirls.encounter." + kind.name().toLowerCase(java.util.Locale.ROOT), mob.getDisplayName()), false);
        }
        return true;
    }

    /** Twice a second while it goes on: how she is doing, and whether she has been helped. */
    public static void update(ServerLevel level, Mob mob) {
        Kind kind = of(mob);
        if (kind == null) return;
        if (level.getGameTime() > mob.getPersistentData().getLong(UNTIL)) {
            if (kind == Kind.WEBBED) for (BlockPos pos : webs(level, mob)) level.destroyBlock(pos, false);
            clear(mob);
            return;
        }
        Player near = level.getNearestPlayer(mob, 16);
        switch (kind) {
            case WEBBED -> {
                if (webs(level, mob).isEmpty()) {
                    help(level, mob, level.getNearestPlayer(mob, 6));
                    return;
                }
                mob.getNavigation().stop();
                if (mob.tickCount % 40 == 0) level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 2, 0.3, 0.3, 0.3, 0.0);
            }
            case CHASED -> {
                List<Mob> chasers = chasers(level, mob);
                if (chasers.isEmpty()) {
                    help(level, mob, near);
                    return;
                }
                for (Mob z : chasers) if (z.getTarget() == null) z.setTarget(mob);
                if (near != null && mob.distanceToSqr(near) > 3 * 3) mob.getNavigation().moveTo(near, 1.4);
            }
            case LOST -> {
                Player close = level.getNearestPlayer(mob, 6);
                if (sheltered(level, mob) && close != null) {
                    help(level, mob, close);
                    return;
                }
                if (near != null && mob.distanceToSqr(near) > 3 * 3) mob.getNavigation().moveTo(near, 1.0);
                if (mob.tickCount % 40 == 0) level.sendParticles(ParticleTypes.SPLASH, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 4, 0.3, 0.1, 0.3, 0.0);
            }
        }
    }

    public static List<BlockPos> webs(ServerLevel level, Mob mob) {
        List<BlockPos> out = new ArrayList<>();
        BlockPos feet = mob.blockPosition();
        for (BlockPos pos : List.of(feet, feet.above(), feet.above(2))) if (level.getBlockState(pos).is(Blocks.COBWEB)) out.add(pos);
        return out;
    }

    public static List<Mob> chasers(ServerLevel level, Mob mob) {
        UUID id = mob.getUUID();
        return level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(48), m -> m.isAlive()
                && m.getPersistentData().hasUUID(CHASING) && m.getPersistentData().getUUID(CHASING).equals(id));
    }

    /** A roof over her: something solid within ROOF blocks above her head (a canopy high above doesn't count). */
    public static boolean sheltered(ServerLevel level, LivingEntity e) {
        BlockPos head = BlockPos.containing(e.getX(), e.getY() + e.getBbHeight(), e.getZ());
        for (int dy = 0; dy < ROOF; dy++) if (level.getBlockState(head.above(dy)).blocksMotion()) return true;
        return false;
    }

    /** It's over and someone helped: she remembers who. */
    private static void help(ServerLevel level, Mob mob, @Nullable Player player) {
        Kind kind = of(mob);
        clear(mob);
        if (player == null || kind == null) return;
        if (mob instanceof Bonded b) b.addAffection(player, REWARD);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 8, 0.5, 0.3, 0.5, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.encounter.thanks", mob.getDisplayName()), false);
        if (player instanceof ServerPlayer sp) {
            award(sp, FIRST, "done");
            award(sp, EVERY, kind.name().toLowerCase(java.util.Locale.ROOT));
        }
    }

    private static void award(ServerPlayer sp, String id, String criterion) {
        AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(id));
        if (a != null) sp.getAdvancements().award(a, criterion);
    }

    private static void clear(Mob mob) {
        mob.getPersistentData().remove(KEY);
        mob.getPersistentData().remove(UNTIL);
    }
}
