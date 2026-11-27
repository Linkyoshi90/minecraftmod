package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Some succubus dreams take the sleeper somewhere real for a while. A mine lasts about five minutes, and whatever
 * was mined is still in the pack afterwards. The other dream is a horde of that same succubus, topless: if one catches
 * him he wakes already under her, drained, with some of her milk. A grown daughter still needs a condom, and the dream
 * does not conceive. The young, the men, and anyone of childlike height are not the dream. The ordinary visit
 * ({@link DreamVisits}) still happens on the nights this one does not take. The rolls stay off on the game-test server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Dreaming {
    /** A dream-copy of her. Her own tricks do not run. */
    public static final String COPY = "DreamCopy";
    private static final String NIGHT = "DreamNight";
    private static final String ON = "DreamOn";
    private static final String TOOK = "DreamTook";
    private static final String DIM = "DreamDim";
    private static final String X = "DreamX", Y = "DreamY", Z = "DreamZ", YAW = "DreamYaw";
    private static final int ODDS = 5;
    private static final int MINE_FOR = 6000;
    private static final int CHASE_FOR = 1800;
    private static final int WAKE = 160;
    private static final int HORDE = 5;
    private static final double CATCH = 2.0;
    private static final double GAP = 0.62, ROCK = 0.10;
    private static final BlockPos MINE = new BlockPos(24000, 160, 24000);
    private static final BlockPos ARENA = new BlockPos(24000, 220, 24000);
    private static final BlockState[] RARE = {
            Blocks.DIAMOND_ORE.defaultBlockState(), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),
            Blocks.EMERALD_ORE.defaultBlockState(), Blocks.GOLD_ORE.defaultBlockState(),
            Blocks.ANCIENT_DEBRIS.defaultBlockState(), Blocks.LAPIS_ORE.defaultBlockState()
    };
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Set<UUID> BUSY = new HashSet<>();

    private enum Kind { MINE, CHASE, ACT }

    private static final class Session {
        final UUID player;
        final ResourceKey<Level> home;
        final double x, y, z;
        final float yaw;
        final UUID source;
        final double sx, sy, sz;
        final float syaw;
        final ResourceKey<Level> sourceLevel;
        Kind kind;
        int ticks;
        boolean paid;
        final List<UUID> copies = new ArrayList<>();

        Session(ServerPlayer player, AbstractMonsterGirl source) {
            this.player = player.getUUID();
            this.home = player.serverLevel().dimension();
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.yaw = player.getYRot();
            this.source = source.getUUID();
            this.sx = source.getX();
            this.sy = source.getY();
            this.sz = source.getZ();
            this.syaw = source.getYRot();
            this.sourceLevel = source.level().dimension();
        }
    }

    private Dreaming() {}

    /** She is the real one in the waking act, or one of the copies. */
    public static boolean active(Mob mob) {
        return mob != null && BUSY.contains(mob.getUUID());
    }

    /** This wake belongs to a dream that is about to take him. {@link DreamVisits} leaves it alone. */
    public static boolean tookTheNight(Player player) {
        return player.getPersistentData().getBoolean(TOOK);
    }

    @SubscribeEvent
    public static void onPlayer(PlayerTickEvent.Post event) {
        if (GameTestHooks.isGametestServer()) return;
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isSleeping() || player.isSpectator()) return;
        if (!Nsfw.on() || SESSIONS.containsKey(player.getUUID())) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        long night = (level.getDayTime() + 12000L) / 24000L;
        if (player.getPersistentData().getLong(NIGHT) == night) return;
        player.getPersistentData().putLong(NIGHT, night);
        if (player.getRandom().nextInt(ODDS) != 0) return;
        AbstractMonsterGirl source = nearest(level, player);
        if (source == null) return;
        if (player.getRandom().nextBoolean() && Condom.allows(source, player)) beginChase(level, player, source);
        else beginMine(level, player, source);
    }

    @SubscribeEvent
    public static void onServer(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !SESSIONS.containsKey(player.getUUID())) return;
        event.setCanceled(true);
        player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.5F));
        end(player.server, player.getUUID(), false);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getPersistentData().getBoolean(ON) || SESSIONS.containsKey(player.getUUID())) return;
        rescue(player);
    }

    /** One beat of every dream still going. */
    public static void tick(MinecraftServer server) {
        if (SESSIONS.isEmpty()) return;
        for (UUID id : new ArrayList<>(SESSIONS.keySet())) {
            Session session = SESSIONS.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (session == null || player == null || !player.isAlive()) {
                end(server, id, false);
                continue;
            }
            if (player.level().dimension() != pocket(session)) {
                end(server, id, false);
                continue;
            }
            session.ticks++;
            if (session.kind == Kind.MINE) {
                if (session.ticks >= MINE_FOR) finishMine(server, id);
            } else if (session.kind == Kind.CHASE) stepChase(server, player, session);
            else stepAct(server, player, session);
        }
    }

    private static void beginMine(ServerLevel level, ServerPlayer player, AbstractMonsterGirl source) {
        Session session = leaveBed(player, source);
        session.kind = Kind.MINE;
        SESSIONS.put(player.getUUID(), session);
        refreshMine(level);
        send(player, level, MINE.getX() + 8.5, MINE.getY() + 2.0, MINE.getZ() + 8.5);
        player.displayClientMessage(Component.translatable("message.harmontide.dream.mine").withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static void beginChase(ServerLevel level, ServerPlayer player, AbstractMonsterGirl source) {
        Session session = leaveBed(player, source);
        session.kind = Kind.CHASE;
        SESSIONS.put(player.getUUID(), session);
        refreshArena(level);
        spawnHorde(level, source, session);
        send(player, level, ARENA.getX() + 12.5, ARENA.getY() + 1.0, ARENA.getZ() + 12.5);
        player.displayClientMessage(Component.translatable("message.harmontide.dream.chase", source.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static Session leaveBed(ServerPlayer player, AbstractMonsterGirl source) {
        BlockPos bed = player.getSleepingPos().orElse(player.blockPosition());
        player.getPersistentData().putBoolean(TOOK, true);
        player.stopSleeping();
        player.getPersistentData().putBoolean(TOOK, false);
        Session session = new Session(player, source);
        // The bed, not the pose he was lying in.
        sessionRemember(player, session, bed.getX() + 0.5, bed.getY() + 0.6, bed.getZ() + 0.5);
        mark(player, session);
        return session;
    }

    /** The session is built from where he is lying; the return point is the bed itself. */
    private static void sessionRemember(ServerPlayer player, Session session, double x, double y, double z) {
        player.getPersistentData().putDouble(X, x);
        player.getPersistentData().putDouble(Y, y);
        player.getPersistentData().putDouble(Z, z);
        player.getPersistentData().putFloat(YAW, player.getYRot());
    }

    private static void mark(ServerPlayer player, Session session) {
        player.getPersistentData().putBoolean(ON, true);
        player.getPersistentData().putString(DIM, session.home.location().toString());
    }

    private static void stepChase(MinecraftServer server, ServerPlayer player, Session session) {
        MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (slow != null && slow.getAmplifier() == 0) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        ServerLevel level = player.serverLevel();
        boolean caught = false;
        for (UUID id : session.copies) {
            if (!(level.getEntity(id) instanceof Mob copy)) continue;
            chase(copy, player);
            bare(copy);
            if (copy.distanceToSqr(player) < CATCH * CATCH) caught = true;
        }
        if (caught) {
            beginAct(server, player, session);
            return;
        }
        if (session.ticks >= CHASE_FOR) {
            player.displayClientMessage(Component.translatable("message.harmontide.dream.escape").withStyle(ChatFormatting.LIGHT_PURPLE), false);
            end(server, player.getUUID(), false);
        }
    }

    private static void chase(Mob copy, ServerPlayer player) {
        Vec3 aim = new Vec3(player.getX() - copy.getX(), 0, player.getZ() - copy.getZ());
        if (aim.lengthSqr() < 0.04) return;
        Vec3 step = aim.normalize().scale(0.18);
        copy.setDeltaMovement(step.x, copy.onGround() ? 0.0 : copy.getDeltaMovement().y, step.z);
        copy.move(MoverType.SELF, new Vec3(step.x, 0.0, step.z));
        float yaw = (float) Math.toDegrees(Mth.atan2(-step.x, step.z));
        copy.setYRot(yaw);
        copy.setYBodyRot(yaw);
        copy.setYHeadRot(yaw);
    }

    private static void beginAct(MinecraftServer server, ServerPlayer player, Session session) {
        discardCopies(player.serverLevel(), session);
        returnHome(server, player, session);
        session.kind = Kind.ACT;
        session.ticks = 0;
        Mob girl = find(server, session.source, session.sourceLevel);
        if (!(girl instanceof AbstractMonsterGirl)) {
            pay(player, null);
            end(server, player.getUUID(), false);
            return;
        }
        ServerLevel home = server.getLevel(session.home);
        if (home == null) home = server.overworld();
        girl = place(girl, home, player.getX(), player.getY(), player.getZ(), player.getYRot());
        if (!(girl instanceof AbstractMonsterGirl)) {
            pay(player, null);
            end(server, player.getUUID(), false);
            return;
        }
        BUSY.add(girl.getUUID());
        bare(girl);
        if (girl instanceof Bonded bonded) bonded.blushFor(WAKE + 40);
        pay(player, girl);
        player.displayClientMessage(Component.translatable("message.harmontide.dream.caught", girl.getDisplayName(), sessionPaid(player)).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static void stepAct(MinecraftServer server, ServerPlayer player, Session session) {
        Mob girl = find(server, session.source, session.sourceLevel);
        if (girl == null || !girl.isAlive() || player.isSpectator() || player.isSleeping()) {
            end(server, player.getUUID(), true);
            return;
        }
        if (girl.getLastHurtByMob() == player && girl.hurtTime > 0) {
            end(server, player.getUUID(), true);
            return;
        }
        if (session.ticks >= WAKE) {
            end(server, player.getUUID(), true);
            return;
        }
        double reach = GAP + Math.sin(session.ticks * 0.5) * ROCK;
        float yaw = player.getYRot();
        float rad = yaw * Mth.DEG_TO_RAD;
        double x = player.getX() - Mth.sin(rad) * reach;
        double z = player.getZ() + Mth.cos(rad) * reach;
        girl.noPhysics = true;
        girl.setDeltaMovement(Vec3.ZERO);
        girl.getNavigation().stop();
        girl.moveTo(x, player.getY(), z, yaw + 180.0F, 0.0F);
        girl.setYBodyRot(yaw + 180.0F);
        girl.setYHeadRot(yaw + 180.0F);
        stand(girl);
        bare(girl);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3, false, false, false));
    }

    private static void finishMine(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.harmontide.dream.mine.done").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        end(server, id, false);
    }

    private static void end(MinecraftServer server, UUID id, boolean restoreGirl) {
        Session session = SESSIONS.remove(id);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (session != null) {
            ServerLevel pocket = server.getLevel(pocket(session));
            if (pocket != null) discardCopies(pocket, session);
            if (player != null && player.level().dimension() == pocket(session)) returnHome(server, player, session);
            if (restoreGirl || session.kind == Kind.ACT) restore(server, session);
            BUSY.remove(session.source);
        }
        if (player != null) {
            player.getPersistentData().putBoolean(ON, false);
            MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (slow != null && slow.getAmplifier() == 3 && slow.getDuration() <= 15) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    /** Puts her back where the dream found her, and dresses her. */
    private static void restore(MinecraftServer server, Session session) {
        Mob girl = find(server, session.source, session.sourceLevel);
        if (girl == null) return;
        girl.noPhysics = false;
        if (Nsfw.standing(girl)) clearStand(girl);
        cover(girl);
        ServerLevel home = server.getLevel(session.sourceLevel);
        if (home != null) place(girl, home, session.sx, session.sy, session.sz, session.syaw);
    }

    /** Same dimension is a plain move. Another dimension keeps her. */
    @Nullable
    private static Mob place(Mob mob, ServerLevel level, double x, double y, double z, float yaw) {
        if (mob.level() == level) {
            mob.teleportTo(x, y, z);
            mob.setYRot(yaw);
            mob.setYBodyRot(yaw);
            mob.setYHeadRot(yaw);
            return mob;
        }
        mob.getNavigation().stop();
        Entity moved = mob.changeDimension(new DimensionTransition(level, new Vec3(x, y, z), Vec3.ZERO, yaw, 0.0F, DimensionTransition.DO_NOTHING));
        return moved instanceof Mob next ? next : null;
    }

    private static void pay(ServerPlayer player, @Nullable Mob girl) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.paid) return;
        session.paid = true;
        int bottles = 1 + player.getRandom().nextInt(4);
        player.getPersistentData().putInt("DreamMilk", bottles);
        ItemStack milk = new ItemStack(ModItems.MILK.get(FamilyMilk.DEVIL).get(), bottles);
        if (!player.getInventory().add(milk)) player.drop(milk, false);
        int take = DreamVisits.taken(player);
        if (take > 0) player.giveExperiencePoints(-take);
        player.getFoodData().setFoodLevel(Math.max(2, player.getFoodData().getFoodLevel() - 6));
        if (girl != null && Condom.grownOffspring(girl, player) && girl.level() instanceof ServerLevel level && !Condom.take(player)) {
            Condom.mention(level, girl, player);
        }
    }

    private static int sessionPaid(ServerPlayer player) {
        return player.getPersistentData().getInt("DreamMilk");
    }

    private static void returnHome(MinecraftServer server, ServerPlayer player, Session session) {
        ServerLevel home = server.getLevel(session.home);
        if (home == null) home = server.overworld();
        double x = player.getPersistentData().contains(X) ? player.getPersistentData().getDouble(X) : session.x;
        double y = player.getPersistentData().contains(Y) ? player.getPersistentData().getDouble(Y) : session.y;
        double z = player.getPersistentData().contains(Z) ? player.getPersistentData().getDouble(Z) : session.z;
        float yaw = player.getPersistentData().contains(YAW) ? player.getPersistentData().getFloat(YAW) : session.yaw;
        player.fallDistance = 0.0F;
        player.teleportTo(home, x, y, z, yaw, 0.0F);
    }

    private static void rescue(ServerPlayer player) {
        MinecraftServer server = player.server;
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(player.getPersistentData().getString(DIM)));
        ServerLevel home = server.getLevel(key);
        if (home == null) home = server.overworld();
        player.fallDistance = 0.0F;
        player.teleportTo(home, player.getPersistentData().getDouble(X), player.getPersistentData().getDouble(Y), player.getPersistentData().getDouble(Z), player.getPersistentData().getFloat(YAW), 0.0F);
        player.getPersistentData().putBoolean(ON, false);
    }

    private static ResourceKey<Level> pocket(Session session) {
        return session.kind == Kind.ACT ? session.home : session.home;
    }

    @Nullable
    private static AbstractMonsterGirl nearest(ServerLevel level, ServerPlayer player) {
        AbstractMonsterGirl best = null;
        double bestDist = Double.MAX_VALUE;
        for (AbstractMonsterGirl girl : level.getEntitiesOfClass(AbstractMonsterGirl.class, player.getBoundingBox().inflate(DreamVisits.RANGE),
                g -> DreamVisits.VISITORS.contains(g.getSpecies()) && Nsfw.adult(g, player) && !g.getPersistentData().getBoolean(COPY) && !NpcSex.active(g))) {
            double dist = girl.distanceToSqr(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = girl;
            }
        }
        return best;
    }

    private static void spawnHorde(ServerLevel level, AbstractMonsterGirl source, Session session) {
        for (int i = 0; i < HORDE; i++) {
            if (!(source.getType().create(level) instanceof AbstractMonsterGirl copy)) continue;
            double ang = i / (double) HORDE * Math.PI * 2.0;
            copy.moveTo(ARENA.getX() + 12.5 + Math.cos(ang) * 10.0, ARENA.getY() + 1.0, ARENA.getZ() + 12.5 + Math.sin(ang) * 10.0, 0.0F, 0.0F);
            copy.setBaby(false);
            copy.setVariantIndex(source.getVariantIndex());
            copy.setPaint(source.getPaint());
            copy.getPersistentData().putBoolean(COPY, true);
            copy.setNoAi(true);
            copy.setInvulnerable(true);
            copy.setSilent(true);
            level.addFreshEntity(copy);
            bare(copy);
            session.copies.add(copy.getUUID());
            BUSY.add(copy.getUUID());
        }
    }

    private static void discardCopies(ServerLevel level, Session session) {
        for (UUID id : session.copies) {
            BUSY.remove(id);
            if (level.getEntity(id) instanceof Entity entity) entity.discard();
        }
        session.copies.clear();
    }

    private static void refreshMine(ServerLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) for (int y = 0; y < 8; y++) for (int z = 0; z < 16; z++) {
            pos.set(MINE.getX() + x, MINE.getY() + y, MINE.getZ() + z);
            boolean shell = x == 0 || z == 0 || x == 15 || z == 15 || y == 0 || y == 7;
            BlockState state;
            if (y == 0) state = Blocks.BEDROCK.defaultBlockState();
            else if (!shell && y >= 2 && y <= 5) state = Blocks.AIR.defaultBlockState();
            else if (shell && y == 7 && (x % 4 == 2) && (z % 4 == 2)) state = Blocks.GLOWSTONE.defaultBlockState();
            else if (shell && y > 0 && level.random.nextInt(8) == 0) state = RARE[level.random.nextInt(RARE.length)];
            else state = Blocks.STONE.defaultBlockState();
            level.setBlock(pos, state, 2);
        }
    }

    private static void refreshArena(ServerLevel level) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 24; x++) for (int y = 0; y < 4; y++) for (int z = 0; z < 24; z++) {
            pos.set(ARENA.getX() + x, ARENA.getY() + y, ARENA.getZ() + z);
            boolean wall = x == 0 || z == 0 || x == 23 || z == 23;
            BlockState state;
            if (y == 0) state = Blocks.BLACKSTONE.defaultBlockState();
            else if (wall && y < 3) state = Blocks.BLACKSTONE.defaultBlockState();
            else if (y == 3 && x % 6 == 3 && z % 6 == 3) state = Blocks.GLOWSTONE.defaultBlockState();
            else state = Blocks.AIR.defaultBlockState();
            level.setBlock(pos, state, 2);
        }
    }

    private static void send(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        player.fallDistance = 0.0F;
        player.teleportTo(level, x, y, z, player.getYRot(), 0.0F);
    }

    private static void stand(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.STAND);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.STAND);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.STAND);
    }

    private static void clearStand(Mob mob) {
        boolean sit = mob instanceof Bonded bonded && bonded.isOrderedToSit();
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(sit ? AbstractMonsterGirl.Action.SITTING : AbstractMonsterGirl.Action.NONE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(sit ? AbstractHarpy.Action.SITTING : AbstractHarpy.Action.NONE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(sit ? AbstractAnuran.Action.SITTING : AbstractAnuran.Action.NONE);
    }

    private static void bare(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.bareFor(20);
        else if (mob instanceof AbstractHarpy harpy) harpy.bareFor(20);
        else if (mob instanceof AbstractAnuran anuran) anuran.bareFor(20);
    }

    private static void cover(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.dressAfter(30);
        else if (mob instanceof AbstractHarpy harpy) harpy.dressAfter(30);
        else if (mob instanceof AbstractAnuran anuran) anuran.dressAfter(30);
    }

    @Nullable
    private static Mob find(MinecraftServer server, UUID id, ResourceKey<Level> key) {
        ServerLevel level = server.getLevel(key);
        if (level != null && level.getEntity(id) instanceof Mob mob) return mob;
        for (ServerLevel other : server.getAllLevels()) {
            if (other.getEntity(id) instanceof Mob mob) return mob;
        }
        return null;
    }
}
