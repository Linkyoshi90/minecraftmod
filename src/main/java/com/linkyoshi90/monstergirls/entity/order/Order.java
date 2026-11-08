package com.linkyoshi90.monstergirls.entity.order;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.DemonRealm;
import com.linkyoshi90.monstergirls.entity.Monsterization;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.HolyWaterItem;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Order: the Demon Realm's sworn enemy, after Monster Girl Encyclopedia. Its sisters (OrderSister) come in two ways.
 * <p>
 * <b>Patrols.</b> By day, near a realm patch, now and then a patrol of three (two knights, an inquisitrix) marches out of
 * the distance to purge it: the grass they walk turns back, the possessed they reach are freed, and the realm's own
 * (devils, girls it changed) are put to the sword. Ordinary wild girls they leave alone.
 * <p>
 * <b>Notoriety.</b> The Order hears of a player's ties to the realm: time spent in its air, devils and realm-changed girls
 * among their friends, heartfruit handed out, fallen sisters. It fades when those ties do, and mercy and exorcism lower it.
 * At {@link #MARKED} sisters fight the player on sight (and the friends at their side), and patrols come looking even with
 * no realm near. At {@link #RAID}, the next night, a crusade comes for the player's hearth: a captain and her band, a
 * boss bar to show how many still stand. All of them beaten (slain, kneeling or spared) and the household is the fonder of
 * its defender; still standing at dawn, they withdraw, bless the hearth's ground, and leave the household shaken.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Order {
    public static final int MARKED = 40, RAID = 100, MAX = 200;
    public static final int HEARTFRUIT = 2, FALL_NOTORIETY = 10, KILLED = 5, MERCY = 20, EXORCISM = 5;
    /** Patrol odds: one try every PATROL_EVERY ticks per player, one in PATROL_ODDS; never with sisters already within PATROL_ALONE. */
    public static final int PATROL_EVERY = 600, PATROL_ODDS = 6, PATROL_ALONE = 96, PATROL_SEARCH = 40, PATROL_DAY = 24000;
    public static final int TIES_EVERY = 1200, RAID_CHECK = 200, HEARTH_RANGE = 64, RAID_REACH = 32, RAID_SEARCH = 160;
    public static final int VICTORY_AFFECTION = 3, DAWN_LOSS = 3;

    private Order() {}

    // --- notoriety ----------------------------------------------------------------------------------------------------

    public static int notoriety(Player player) {
        return player.getData(ModAttachments.ORDER_NOTORIETY);
    }

    public static void setNotoriety(Player player, int value) {
        int before = notoriety(player), after = Math.max(0, Math.min(MAX, value));
        player.setData(ModAttachments.ORDER_NOTORIETY, after);
        if (before < MARKED && after >= MARKED) {
            player.displayClientMessage(Component.translatable("message.monstergirls.order.marked").withStyle(ChatFormatting.GOLD), false);
            Dairy.award(player, "order_marked");
        }
    }

    public static void addNotoriety(Player player, int amount) {
        if (!player.level().isClientSide) setNotoriety(player, notoriety(player) + amount);
    }

    /** The realm's own: devils and every girl the realm changed. */
    public static boolean realmsOwn(Entity e) {
        return e instanceof Mob m && Monsterization.monsterized(m)
                || e instanceof AbstractMonsterGirl g && g.getSpecies().family() == GirlSpecies.Family.DEVIL;
    }

    /** A player's ties to the realm this minute: in its air, and up to three of the realm's own among their friends nearby. */
    public static int ties(ServerLevel level, Player player) {
        int ties = (DemonRealm.realmAir(player) ? 1 : 0) + (com.linkyoshi90.monstergirls.entity.RealmTouched.kindred(player) ? 1 : 0);
        int own = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32),
                m -> m instanceof Bonded b && realmsOwn(m) && b.getAffection(player) >= Orders.AFFECTION).size();
        return ties + Math.min(3, own);
    }

    /** Who a sister fights. */
    public static boolean hunts(OrderSister sister, LivingEntity e) {
        if (!sister.standing() || !e.isAlive() || e instanceof OrderSister) return false;
        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator() && (notoriety(p) >= MARKED || p.getUUID().equals(sister.raidTarget()));
        }
        if (!(e instanceof Mob m) || Monsterization.possessed(m)) return false;    // the possessed are freed, not fought
        if (realmsOwn(m)) return true;
        if (!(m instanceof Bonded b)) return false;
        BlockPos hearth = sister.raidHearth();
        if (hearth != null && m.blockPosition().closerThan(hearth, RAID_REACH)) return true;   // a crusade takes the whole household
        for (Player p : m.level().players()) {                          // and a marked player's friends at their side
            if (p.distanceToSqr(m) <= 16 * 16 && b.getAffection(p) >= Orders.AFFECTION && notoriety(p) >= MARKED) return true;
        }
        return false;
    }

    // --- the realm, seen from outside -------------------------------------------------------------------------------

    /** The nearest realm grass on the surface within this radius (loaded ground only), or null. */
    @Nullable
    public static BlockPos findRealm(ServerLevel level, BlockPos centre, int radius) {
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx += 3) {
            for (int dz = -radius; dz <= radius; dz += 3) {
                int x = centre.getX() + dx, z = centre.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, centre.getY(), z))) continue;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                for (int y = top; y >= top - 2; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (com.linkyoshi90.monstergirls.block.RealmGrassBlock.realm(level.getBlockState(p))) {
                        double d = p.distSqr(centre);
                        if (d < bestD) {
                            bestD = d;
                            best = p;
                        }
                        break;
                    }
                }
            }
        }
        return best;
    }

    /** Any realm grass within r blocks (and a little up and down). */
    public static boolean realmNear(ServerLevel level, BlockPos pos, int r) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-r, -2, -r), pos.offset(r, 1, r))) {
            if (com.linkyoshi90.monstergirls.block.RealmGrassBlock.realm(level.getBlockState(p))) return true;
        }
        return false;
    }

    /** The Order's banner: white, a gold cross, a sky-blue border. A captain wears it, and drops it. */
    public static ItemStack banner(RegistryAccess access) {
        ItemStack stack = new ItemStack(Items.WHITE_BANNER);
        var patterns = access.lookupOrThrow(Registries.BANNER_PATTERN);
        stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                .add(patterns.getOrThrow(BannerPatterns.STRAIGHT_CROSS), DyeColor.YELLOW)
                .add(patterns.getOrThrow(BannerPatterns.CIRCLE_MIDDLE), DyeColor.YELLOW)
                .add(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.LIGHT_BLUE)
                .build());
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.monstergirls.order_banner").withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.RARITY, Rarity.UNCOMMON);
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, net.minecraft.util.Unit.INSTANCE);
        return stack;
    }

    // --- mustering -----------------------------------------------------------------------------------------------------

    public static EntityType<OrderSister> type(OrderSister.Rank rank) {
        return switch (rank) {
            case KNIGHT -> ModEntities.ORDER_KNIGHT.get();
            case INQUISITOR -> ModEntities.ORDER_INQUISITOR.get();
            case CAPTAIN -> ModEntities.ORDER_CAPTAIN.get();
        };
    }

    /**
     * Sisters of these ranks step out together somewhere between min and max blocks from {@code around}, on open ground.
     * Those who came (fewer if no ground was found).
     */
    public static List<OrderSister> muster(ServerLevel level, BlockPos around, int min, int max, List<OrderSister.Rank> ranks) {
        List<OrderSister> out = new ArrayList<>();
        BlockPos base = null;
        for (int i = 0; i < 16 && base == null; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2, d = min + level.random.nextDouble() * Math.max(0, max - min);
            base = ground(level, around.getX() + (int) Math.round(Math.cos(angle) * d), around.getZ() + (int) Math.round(Math.sin(angle) * d));
        }
        if (base == null) return out;
        for (OrderSister.Rank rank : ranks) {
            BlockPos at = null;
            for (int i = 0; i < 8 && at == null; i++) at = ground(level, base.getX() + level.random.nextInt(5) - 2, base.getZ() + level.random.nextInt(5) - 2);
            if (at == null) at = base;
            OrderSister sister = type(rank).create(level);
            if (sister == null) continue;
            sister.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            sister.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            level.addFreshEntity(sister);
            out.add(sister);
        }
        return out;
    }

    /** Open ground to stand on at this column (the top of it), or null: not loaded, water, lava. */
    @Nullable
    private static BlockPos ground(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return null;
        BlockPos at = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        if (!level.getFluidState(at.below()).isEmpty() || !level.getBlockState(at.below()).isSolid()) return null;
        if (!level.isEmptyBlock(at) || !level.isEmptyBlock(at.above())) return null;
        return at;
    }

    /** A patrol of two knights and an inquisitrix, marching to {@code target}. They go home after a day. */
    public static List<OrderSister> patrol(ServerLevel level, BlockPos around, BlockPos target, int min, int max) {
        List<OrderSister> band = muster(level, around, min, max, List.of(OrderSister.Rank.KNIGHT, OrderSister.Rank.INQUISITOR, OrderSister.Rank.KNIGHT));
        for (OrderSister s : band) {
            s.marchTo(target);
            s.leaveAt(level.getGameTime() + PATROL_DAY);
        }
        if (!band.isEmpty()) {
            OrderSister first = band.get(0);
            level.playSound(null, first.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.0F, 0.8F);
            for (Player p : level.getEntitiesOfClass(Player.class, first.getBoundingBox().inflate(64))) {
                p.displayClientMessage(Component.translatable("message.monstergirls.order.patrol").withStyle(ChatFormatting.GOLD), false);
                Dairy.award(p, "order_patrol");
            }
        }
        return band;
    }

    /** One player's patrol roll. The patrol, or empty. */
    public static List<OrderSister> tryPatrol(ServerLevel level, Player player, boolean roll) {
        if (!MonsterGirlsConfig.orderPatrols() || level.getDifficulty() == Difficulty.PEACEFUL || !level.isDay() || level.dimension() != Level.OVERWORLD
                || player.isSpectator() || roll && level.random.nextInt(PATROL_ODDS) != 0) return List.of();
        if (!level.getEntitiesOfClass(OrderSister.class, player.getBoundingBox().inflate(PATROL_ALONE)).isEmpty()) return List.of();
        BlockPos target = findRealm(level, player.blockPosition(), PATROL_SEARCH);
        if (target == null) {
            if (notoriety(player) < MARKED) return List.of();
            target = player.blockPosition();                            // no realm here: they have come for the marked one
        }
        return patrol(level, player.blockPosition(), target, 24, 36);
    }

    // --- crusades on a hearth ---------------------------------------------------------------------------------------

    public record Raid(int id, BlockPos hearth, UUID player, int total, long start) {
        CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.putInt("Id", this.id);
            t.put("Hearth", NbtUtils.writeBlockPos(this.hearth));
            t.putUUID("Player", this.player);
            t.putInt("Total", this.total);
            t.putLong("Start", this.start);
            return t;
        }

        static Raid load(CompoundTag t) {
            return new Raid(t.getInt("Id"), NbtUtils.readBlockPos(t, "Hearth").orElse(BlockPos.ZERO), t.getUUID("Player"), t.getInt("Total"), t.getLong("Start"));
        }
    }

    /** The crusades under way in a level (saved with it). */
    public static final class Raids extends SavedData {
        final List<Raid> raids = new ArrayList<>();
        int next = 1;

        static Raids load(CompoundTag tag, HolderLookup.Provider provider) {
            Raids r = new Raids();
            r.next = Math.max(1, tag.getInt("Next"));
            for (Tag t : tag.getList("Raids", Tag.TAG_COMPOUND)) r.raids.add(Raid.load((CompoundTag) t));
            return r;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            tag.putInt("Next", this.next);
            ListTag list = new ListTag();
            for (Raid r : this.raids) list.add(r.save());
            tag.put("Raids", list);
            return tag;
        }

        public List<Raid> all() {
            return List.copyOf(this.raids);
        }
    }

    public static Raids raids(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Raids::new, Raids::load, null), "monstergirls_order_raids");
    }

    private static final Map<Integer, ServerBossEvent> BARS = new HashMap<>();

    /** The hearth this player owns nearest them, within range and loaded, or null. */
    @Nullable
    public static BlockPos ownHearth(ServerLevel level, Player player, int range) {
        BlockPos best = null;
        double bestD = (double) range * range;
        for (BlockPos p : List.copyOf(HearthBlockEntity.loaded(level))) {
            double d = p.distToCenterSqr(player.position());
            if (d <= bestD && level.getBlockEntity(p) instanceof HearthBlockEntity h && player.getUUID().equals(h.owner())) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    /** The band for a hearth with this many residents: a captain, two knights, an inquisitrix, and one more per three residents (up to three). */
    public static List<OrderSister.Rank> band(int residents) {
        List<OrderSister.Rank> ranks = new ArrayList<>(List.of(OrderSister.Rank.CAPTAIN, OrderSister.Rank.KNIGHT, OrderSister.Rank.INQUISITOR, OrderSister.Rank.KNIGHT));
        OrderSister.Rank[] more = {OrderSister.Rank.INQUISITOR, OrderSister.Rank.KNIGHT, OrderSister.Rank.INQUISITOR};
        for (int i = 0; i < Math.min(3, residents / 3); i++) ranks.add(more[i]);
        return ranks;
    }

    /** A crusade sets out for this hearth. Its band (empty if no ground was found). */
    public static List<OrderSister> startRaid(ServerLevel level, BlockPos hearth, Player player, int min, int max) {
        int residents = level.getBlockEntity(hearth) instanceof HearthBlockEntity h ? h.residentCount() : 0;
        Raids data = raids(level);
        int id = data.next++;
        List<OrderSister> band = muster(level, hearth, min, max, band(residents));
        if (band.isEmpty()) {
            data.setDirty();
            return band;
        }
        for (OrderSister s : band) s.joinRaid(id, hearth, player.getUUID());
        data.raids.add(new Raid(id, hearth.immutable(), player.getUUID(), band.size(), level.getGameTime()));
        data.setDirty();
        setNotoriety(player, 0);                                        // they have come; it starts again from nothing
        level.playSound(null, hearth, SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 64.0F, 1.2F);
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(hearth).inflate(96))) {
            p.displayClientMessage(Component.translatable("message.monstergirls.order.raid").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        }
        return band;
    }

    /** The sisters of a crusade still on their feet. */
    public static List<OrderSister> standing(ServerLevel level, Raid raid) {
        return level.getEntitiesOfClass(OrderSister.class, new AABB(raid.hearth()).inflate(RAID_SEARCH), s -> s.raid() == raid.id() && s.standing());
    }

    /** One look at every crusade in the level: the boss bar, and the end of it if it is over. */
    public static void tickRaids(ServerLevel level) {
        Raids data = raids(level);
        if (data.raids.isEmpty()) return;
        for (Raid raid : List.copyOf(data.raids)) {
            if (!level.isLoaded(raid.hearth())) continue;               // nobody near: the crusade waits
            List<OrderSister> up = standing(level, raid);
            ServerBossEvent bar = BARS.computeIfAbsent(raid.id(), id -> new ServerBossEvent(Component.translatable("event.monstergirls.order_crusade"),
                    BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10));
            bar.setProgress(raid.total() == 0 ? 0.0F : Math.min(1.0F, up.size() / (float) raid.total()));
            for (ServerPlayer p : level.players()) {
                if (p.blockPosition().closerThan(raid.hearth(), 96)) bar.addPlayer(p);
                else bar.removePlayer(p);
            }
            long since = level.getGameTime() - raid.start();
            if (up.isEmpty()) end(level, raid, true);
            else if (level.isDay() && since > 1200 || since > 36000) {
                for (OrderSister s : up) {
                    level.sendParticles(ParticleTypes.CLOUD, s.getX(), s.getY() + 1.0, s.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
                    s.discard();
                }
                end(level, raid, false);
            }
        }
    }

    /** A crusade is over: beaten (won) or withdrawn at dawn. */
    public static void end(ServerLevel level, Raid raid, boolean won) {
        Raids data = raids(level);
        data.raids.removeIf(r -> r.id() == raid.id());
        data.setDirty();
        ServerBossEvent bar = BARS.remove(raid.id());
        if (bar != null) bar.removeAllPlayers();
        Player owner = level.getPlayerByUUID(raid.player());
        if (owner != null) setNotoriety(owner, won ? 0 : RAID / 2);
        if (!won) HolyWaterItem.cleanse(level, raid.hearth().below());
        if (owner != null && level.getBlockEntity(raid.hearth()) instanceof HearthBlockEntity h) {
            for (UUID id : h.residents()) {
                if (level.getEntity(id) instanceof Mob girl && girl instanceof Bonded b && girl.isAlive()) {
                    b.addAffection(owner, won ? VICTORY_AFFECTION : -DAWN_LOSS);
                    level.sendParticles(won ? ParticleTypes.HEART : ParticleTypes.SMOKE, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
                }
            }
        }
        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(raid.hearth()).inflate(96))) {
            p.displayClientMessage(Component.translatable(won ? "message.monstergirls.order.victory" : "message.monstergirls.order.dawn")
                    .withStyle(won ? ChatFormatting.GREEN : ChatFormatting.GRAY), false);
        }
        if (won) {
            Dairy.award(owner, "order_crusade");
            level.playSound(null, raid.hearth(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    /** Whether the night is young enough for a crusade to set out (dusk to a little past midnight). */
    public static boolean raidHour(Level level) {
        long t = level.getDayTime() % 24000L;
        return t >= 13000L && t <= 19000L;
    }

    /** One player's crusade check. The band, or empty. */
    public static List<OrderSister> tryRaid(ServerLevel level, Player player) {
        if (!MonsterGirlsConfig.orderRaids() || notoriety(player) < RAID || level.getDifficulty() == Difficulty.PEACEFUL || !raidHour(level)
                || player.isSpectator()) return List.of();
        BlockPos hearth = ownHearth(level, player, HEARTH_RANGE);
        if (hearth == null) return List.of();
        for (Raid r : raids(level).raids) if (r.hearth().equals(hearth) || r.player().equals(player.getUUID())) return List.of();
        return startRaid(level, hearth, player, 28, 36);
    }

    // --- events -----------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;
        int t = player.tickCount;
        if (t % TIES_EVERY == 300) {
            int ties = ties(level, player);
            addNotoriety(player, ties > 0 ? ties : -1);
        }
        if (t % PATROL_EVERY == 100) tryPatrol(level, player, true);
        if (t % RAID_CHECK == 50) tryRaid(level, player);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % 20 == 0) tickRaids(level);
    }

    /** A kneeling sister (or one going home) is out of the fight: no creature picks her as a target, only a player can still strike. */
    @SubscribeEvent
    public static void onTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof OrderSister s && !s.standing() && !(event.getEntity() instanceof Player)) event.setCanceled(true);
    }

    /** A sister slain: the Order hears of it. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof OrderSister && event.getSource().getEntity() instanceof Player p) addNotoriety(p, KILLED);
    }

    /** Heartfruit handed out: the Order hears of that too. */
    @SubscribeEvent
    public static void onHeartfruit(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide && event.getItemStack().is(ModItems.HEARTFRUIT.get()) && event.getTarget() instanceof Mob
                && !(event.getTarget() instanceof OrderSister)) addNotoriety(event.getEntity(), HEARTFRUIT);
    }

    /** A line for /monstergirls order: how the Order sees this player. */
    public static Component standingOf(Player player) {
        int n = notoriety(player);
        String key = n >= RAID ? "crusade" : n >= MARKED ? "marked" : n >= MARKED / 2 ? "rumoured" : "unknown";
        return Component.translatable("commands.monstergirls.order.standing." + key, n);
    }
}
