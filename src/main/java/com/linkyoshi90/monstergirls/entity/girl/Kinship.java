package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.compat.AlexsMobs;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import net.minecraft.core.registries.BuiltInRegistries;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Kinship: a girl with a vanilla counterpart (the ocelot nekomata and ocelots, the zombie girl and zombies...) draws
 * her kin to her. While she is near and does not see a player as prey, her kin ignore that player: no fleeing, no
 * attacking. Feed her and one of her kin her family's food within 30 seconds of each other, and she introduces you:
 * the vanilla mob becomes yours (tamed where vanilla has taming, otherwise a persistent friend that follows you and
 * never targets a player). Families without a vanilla counterpart only bond and breed with players, as before.
 * Harpies count parrots (the ostrich and the penguin: chickens) as kin, anurans frogs; with Alex's Mobs installed its
 * animals join their girls' kin too (compat/AlexsMobs).
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Kinship {
    private static final String FED_BY = "monstergirls:kin_fed_by";
    private static final String FED_AT = "monstergirls:kin_fed_at";
    private static final String FRIEND = "monstergirls:kin_friend";
    static final int WINDOW = 600;              // feed the other one within 30 seconds
    private static final double RANGE = 16.0;   // how far her pull on her kin reaches
    private static final double MATCH = 8.0;    // how close the two must be for an introduction

    /** Built on first use, when every mod's entities are registered (so Alex's Mobs can be looked up). */
    private static final class Table {
        static final Map<GirlSpecies, Set<EntityType<?>>> KIN = new EnumMap<>(GirlSpecies.class);
        /** Harpies and anurans, by entity id path. */
        static final Map<String, Set<EntityType<?>>> OTHERS = new java.util.HashMap<>();
        static final Set<EntityType<?>> ALL = new HashSet<>();

        static {
            for (GirlSpecies s : GirlSpecies.values()) {
                Set<EntityType<?>> kin = new HashSet<>(kinOf(s));
                kin.addAll(AlexsMobs.kinOf(s.id()));
                KIN.put(s, Set.copyOf(kin));
                ALL.addAll(kin);
            }
            for (String h : List.of("sparrow_harpy", "crow_harpy", "owl_harpy", "seagull_harpy", "albatross_harpy", "riflebird_harpy", "tengu_harpy", "toucan_harpy",
                    "finch_harpy", "flamingo_harpy", "coin_magpie_harpy", "trickster_raven_harpy", "rookmourn_harpy", "pumpkingull_harpy", "condorune_harpy", "mirror_crane_harpy",
                    "ironbeak_harpy", "ibiset_harpy", "stigowl_harpy")) {
                other(h, EntityType.PARROT);
            }
            other("ostrich_harpy", EntityType.CHICKEN);
            other("penguin_harpy", EntityType.CHICKEN);
            other("dodobold_harpy", EntityType.CHICKEN);
            other("emberu_harpy", EntityType.CHICKEN);
            other("peaflare_harpy", EntityType.CHICKEN);
            for (String a : List.of("bullywug", "mucus_toad", "bullfrog", "tree_frog", "poison_dart_frog", "glass_frog", "goliath_frog", "desert_rain_frog",
                    "fire_bellied_toad", "cane_toad", "natterjack", "surinam_toad", "vodyanoy", "mailtoad")) {
                other(a, EntityType.FROG);
            }
        }

        private static void other(String girl, EntityType<?> vanilla) {
            Set<EntityType<?>> kin = new HashSet<>(AlexsMobs.kinOf(girl));
            kin.add(vanilla);
            OTHERS.put(girl, Set.copyOf(kin));
            ALL.addAll(kin);
        }
    }

    private Kinship() {}

    /** The vanilla mobs a species counts as kin: the species' own first, else her family's. */
    private static Set<EntityType<?>> kinOf(GirlSpecies s) {
        return switch (s) {
            case OCELOT_NEKOMATA -> Set.of(EntityType.OCELOT);
            case BOGHARE_JACKALOPE -> Set.of(EntityType.RABBIT, EntityType.FROG);
            case HORSE_CENTAUR, ZEBRA_CENTAUR, PEGASUS_CENTAUR, UNICORN_CENTAUR -> Set.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE);
            case ALPACA_CENTAUR -> Set.of(EntityType.LLAMA, EntityType.TRADER_LLAMA);
            case GOAT_CENTAUR -> Set.of(EntityType.GOAT);
            case CAMEL_CENTAUR, DROMEDARY_CENTAUR -> Set.of(EntityType.CAMEL);
            case WOLF_CANID, KOBOLD_CANID -> Set.of(EntityType.WOLF);
            case POLAR_BEAR_CANID, GRIZZLY_CANID, BLACK_BEAR_CANID -> Set.of(EntityType.POLAR_BEAR);
            case PANDA_CANID -> Set.of(EntityType.PANDA);
            case ORCA_MERMAID, DOLPHIN_MERMAID -> Set.of(EntityType.DOLPHIN);
            case AXOLOTL_MERMAID -> Set.of(EntityType.AXOLOTL);
            case MAGMA_SLIME -> Set.of(EntityType.MAGMA_CUBE);
            case KRAKEN_MOLLUSK, IKA_MOLLUSK, OCTOPUS_MOLLUSK -> Set.of(EntityType.SQUID, EntityType.GLOW_SQUID);
            case ZOMBIE_UNDEAD -> Set.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER);
            case PHANTOM_UNDEAD -> Set.of(EntityType.PHANTOM);
            case VAMPIRE_UNDEAD -> Set.of(EntityType.BAT);
            case YUKI_ONNA_UNDEAD -> Set.of(EntityType.SNOW_GOLEM);
            case GOLEM_CONSTRUCT -> Set.of(EntityType.IRON_GOLEM);
            case ARMADILLO_ARMOURED -> Set.of(EntityType.ARMADILLO);
            case SHEEP_MINOTAUR -> Set.of(EntityType.SHEEP);
            case CAVE_BAT -> Set.of(EntityType.BAT);
            case DARK_ELF -> Set.of(EntityType.WARDEN);
            case BLAZE_CAT_NEKOMATA -> Set.of(EntityType.BLAZE);
            case AEGIS_BOAR_ARMOURED -> Set.of(EntityType.HOGLIN);
            case AXOLUME_MERMAID -> Set.of(EntityType.AXOLOTL);
            case MELONCOLLIE_CANID -> Set.of(EntityType.WOLF);
            case ENDER_SLIME -> Set.of(EntityType.ENDERMITE);
            case DULLAHAN_UNDEAD -> Set.of(EntityType.SKELETON_HORSE);
            case LICH_UNDEAD -> Set.of(EntityType.SKELETON, EntityType.STRAY);
            case FAIRY_BUG, FAIRY_QUEEN_BUG -> Set.of(EntityType.ALLAY);
            case LESSER_DEVIL -> Set.of(EntityType.VEX);
            case KOI_MERMAID -> Set.of(EntityType.TROPICAL_FISH);
            case MATANGO_FLORA -> Set.of(EntityType.MOOSHROOM);
            case TURTLE_LIZARDFOLK, SNAPPING_TURTLE_LIZARDFOLK, BLACK_TORTOISE_LIZARDFOLK, RED_EARED_SLIDER_LIZARDFOLK, GALAPAGOS_TORTOISE_LIZARDFOLK,
                 BOX_TURTLE_LIZARDFOLK, SOFTSHELL_TURTLE_LIZARDFOLK, MATAMATA_LIZARDFOLK -> Set.of(EntityType.TURTLE);
            case BEE_BUG, QUEEN_BEE_BUG -> Set.of(EntityType.BEE);
            case REINDEER_CENTAUR, TAPIR_CENTAUR, SHARK_MERMAID, SEAL_MERMAID, DUGONG_MERMAID, SNAIL_MOLLUSK, AMMONITE_MOLLUSK -> Set.of();
            default -> switch (s.family()) {
                case NEKOMATA -> Set.of(EntityType.CAT);
                case KITSUNE -> Set.of(EntityType.FOX);
                case ARACHNE -> Set.of(EntityType.SPIDER, EntityType.CAVE_SPIDER);
                case SLIME -> Set.of(EntityType.SLIME);
                case JACKALOPE -> Set.of(EntityType.RABBIT);
                case MINOTAUR -> Set.of(EntityType.COW, EntityType.MOOSHROOM);
                default -> Set.of();
            };
        };
    }

    public static Set<EntityType<?>> kin(GirlSpecies s) {
        return Table.KIN.get(s);
    }

    /** Whatever counts as her kin: a monster girl's, a harpy's or an anuran's (empty for anything else). */
    public static Set<EntityType<?>> kinOf(Entity girl) {
        if (girl instanceof AbstractMonsterGirl g) return Table.KIN.get(g.getSpecies());
        if (girl instanceof AbstractHarpy || girl instanceof AbstractAnuran) {
            return Table.OTHERS.getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(girl.getType()).getPath(), Set.of());
        }
        return Set.of();
    }

    private static boolean isGirl(Entity e) {
        return e instanceof AbstractMonsterGirl || e instanceof AbstractHarpy || e instanceof AbstractAnuran;
    }

    private static boolean eats(Entity girl, ItemStack stack) {
        if (girl instanceof AbstractMonsterGirl g) return g.isFood(stack);
        if (girl instanceof AbstractHarpy h) return h.isEdible(stack);
        return girl instanceof AbstractAnuran a && a.isFood(stack);
    }

    private static boolean hostileTo(Entity girl, Player player) {
        if (girl instanceof AbstractMonsterGirl g) return g.isHostileTo(player);
        return girl instanceof Mob m && m.getTarget() == player;
    }

    /** The nearest grown girl (a monster girl, harpy or anuran) within range whose kin this mob is. */
    @Nullable
    public static PathfinderMob girlOf(Entity mob, double range) {
        PathfinderMob best = null;
        double bestDist = range * range;
        for (PathfinderMob g : mob.level().getEntitiesOfClass(PathfinderMob.class, mob.getBoundingBox().inflate(range),
                g -> g.isAlive() && !g.isBaby() && isGirl(g) && kinOf(g).contains(mob.getType()))) {
            double d = g.distanceToSqr(mob);
            if (d <= bestDist) {
                best = g;
                bestDist = d;
            }
        }
        return best;
    }

    /** The player a vanilla mob was introduced to, if any. */
    @Nullable
    public static UUID friendOf(Entity mob) {
        CompoundTag tag = mob.getPersistentData();
        return tag.hasUUID(FRIEND) ? tag.getUUID(FRIEND) : null;
    }

    /** Whether this kin mob leaves the player alone: it is someone's friend, or a girl of its kind nearby is friendly to them. */
    public static boolean ignores(Entity mob, Player player) {
        if (!Table.ALL.contains(mob.getType())) return false;
        if (friendOf(mob) != null) return true;
        PathfinderMob girl = girlOf(mob, RANGE);
        if (mob.getType() == EntityType.WARDEN || mob.getType() == EntityType.BLAZE || mob.getType() == EntityType.HOGLIN) {   // fierce kin spare only her friends
            return girl instanceof AbstractMonsterGirl g && g.getAffection(player) >= Orders.AFFECTION;
        }
        return girl != null && !hostileTo(girl, player);
    }

    // --- feeding and introductions --------------------------------------------------------------------------------

    private static void markFed(Entity e, Player player) {
        e.getPersistentData().putUUID(FED_BY, player.getUUID());
        e.getPersistentData().putLong(FED_AT, e.level().getGameTime());
    }

    private static boolean fedBy(Entity e, Player player) {
        CompoundTag tag = e.getPersistentData();
        return tag.hasUUID(FED_BY) && tag.getUUID(FED_BY).equals(player.getUUID()) && e.level().getGameTime() - tag.getLong(FED_AT) <= WINDOW;
    }

    /** Feeding a girl, or a kin mob near her, with her family's food. True when the event should stop there. */
    public static boolean feed(ServerLevel level, Player player, Entity target, ItemStack stack) {
        if (isGirl(target) && target instanceof PathfinderMob girl) {
            if (!girl.isBaby() && !kinOf(girl).isEmpty() && eats(girl, stack) && !hostileTo(girl, player)) {
                markFed(girl, player);
                match(level, girl, player);
            }
            return false;                                      // she eats it the usual way
        }
        if (!(target instanceof Mob mob) || !Table.ALL.contains(mob.getType()) || friendOf(mob) != null || mob.getType() == EntityType.WARDEN) return false;
        PathfinderMob girl = girlOf(mob, MATCH);
        if (girl == null || !eats(girl, stack) || hostileTo(girl, player)) return false;
        markFed(mob, player);
        boolean vanillaEats = mob instanceof Animal a && a.isFood(stack);   // its own food: vanilla still feeds (and may breed) it
        if (!vanillaEats) {
            stack.consume(1, player);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
        }
        match(level, girl, player);
        return !vanillaEats;
    }

    /** Both fed by the same player in time: every such kin mob near her is introduced. */
    private static void match(ServerLevel level, PathfinderMob girl, Player player) {
        if (!fedBy(girl, player)) return;
        Set<EntityType<?>> kin = kinOf(girl);
        List<Mob> ready = level.getEntitiesOfClass(Mob.class, girl.getBoundingBox().inflate(MATCH),
                m -> kin.contains(m.getType()) && friendOf(m) == null && fedBy(m, player));
        for (Mob m : ready) befriend(level, m, player, girl);
    }

    /** The vanilla mob becomes the player's: tamed where vanilla allows it, otherwise a persistent friend. */
    public static void befriend(ServerLevel level, Mob mob, Player player, PathfinderMob girl) {
        CompoundTag tag = mob.getPersistentData();
        tag.putUUID(FRIEND, player.getUUID());
        tag.remove(FED_BY);
        tag.remove(FED_AT);
        mob.setPersistenceRequired();
        if (mob.getTarget() instanceof Player) mob.setTarget(null);
        Orders kinOrders = orders(mob);
        kinOrders.set(Orders.Command.FOLLOW, player, null);
        saveOrders(mob, kinOrders);
        if (mob instanceof TamableAnimal t && !t.isTame()) t.tame(player);
        else if (mob instanceof AbstractHorse h && !h.isTamed()) h.tameWithName(player);
        else if (mob instanceof Ocelot o) o.setTrusting(true);
        else if (mob instanceof Fox f && !f.trusts(player.getUUID())) f.addTrustedUUID(player.getUUID());
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getBbHeight() + 0.2, girl.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.introduced", girl.getDisplayName(), mob.getDisplayName()), false);
    }

    // --- events --------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getItemStack().is(net.minecraft.world.item.Items.STICK) && event.getTarget() instanceof Mob mob
                && event.getEntity().getUUID().equals(friendOf(mob))) {
            command(mob, event.getEntity());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        if (feed(level, event.getEntity(), event.getTarget(), event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target instanceof Player p && ignores(event.getEntity(), p)) {
            event.setCanceled(true);
        } else if (target != null && event.getEntity() instanceof Mob mob && friendOf(mob) != null) {
            Orders o = orders(mob);
            if (o.stance() != Orders.Stance.NATURAL && !o.wants(mob, target)) event.setCanceled(true);   // a friend's stance decides
        }
    }

    // --- stick orders for befriended vanilla mobs ---------------------------------------------------------------------

    private static final String ORDERS = "monstergirls:kin_orders";
    private static final Map<Mob, Orders> ORDER_CACHE = new java.util.WeakHashMap<>();

    /** A befriended mob's orders (follow by default). */
    public static Orders orders(Mob mob) {
        return ORDER_CACHE.computeIfAbsent(mob, m -> {
            Orders o = new Orders();
            CompoundTag tag = m.getPersistentData().getCompound(ORDERS);
            if (tag.isEmpty()) {
                UUID friend = friendOf(m);
                o.set(Orders.Command.FOLLOW, friend == null ? null : m.level().getPlayerByUUID(friend), null);
            } else {
                o.load(tag);
            }
            return o;
        });
    }

    private static void saveOrders(Mob mob, Orders o) {
        mob.getPersistentData().put(ORDERS, o.save());
    }

    /** A stick from her friend: the next order (sit, patrol here, follow, roam), or with sneaking the next stance. */
    public static void command(Mob mob, Player player) {
        Orders o = orders(mob);
        if (player.isShiftKeyDown()) {
            Orders.Stance next = switch (o.stance()) {
                case NATURAL, PEACEFUL -> Orders.Stance.HOSTILE;
                case HOSTILE -> Orders.Stance.NEUTRAL;
                case NEUTRAL -> Orders.Stance.SELF_DEFENCE;
                case SELF_DEFENCE -> Orders.Stance.PEACEFUL;
            };
            o.setStance(next, player);
            if (mob.getTarget() != null && !o.wants(mob, mob.getTarget())) mob.setTarget(null);
            player.displayClientMessage(Component.translatable("message.monstergirls.stance." + next.name().toLowerCase(), mob.getDisplayName()), true);
        } else {
            Orders.Command next = switch (o.rawCommand()) {
                case FREE_ROAM -> Orders.Command.SIT;
                case SIT -> Orders.Command.PATROL;
                case PATROL -> Orders.Command.FOLLOW;
                case FOLLOW -> Orders.Command.FREE_ROAM;
            };
            o.set(next, player, next == Orders.Command.PATROL ? mob.blockPosition() : null);
            if (mob instanceof TamableAnimal t) {
                t.setOrderedToSit(next == Orders.Command.SIT);
                t.setInSittingPose(next == Orders.Command.SIT);
            }
            mob.getNavigation().stop();
            String key = switch (next) {
                case SIT -> "message.monstergirls.sits";
                case PATROL -> "message.monstergirls.order.patrol";
                case FOLLOW -> "message.monstergirls.order.follow";
                case FREE_ROAM -> "message.monstergirls.order.free";
            };
            player.displayClientMessage(Component.translatable(key, mob.getDisplayName()), true);
        }
        saveOrders(mob, o);
    }

    /** Kin mobs follow their girl (or, once introduced, their player) and stop running from players she likes. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof PathfinderMob mob) || !Table.ALL.contains(mob.getType())) return;
        for (WrappedGoal w : List.copyOf(mob.goalSelector.getAvailableGoals())) {
            if (w.getGoal() instanceof AvoidEntityGoal<?> avoid && avoid.avoidClass == Player.class) {
                mob.goalSelector.removeGoal(avoid);
                mob.goalSelector.addGoal(w.getPriority(), new UnlessKin(mob, avoid));
            }
        }
        for (WrappedGoal w : List.copyOf(mob.goalSelector.getAvailableGoals())) {        // a tamed pet follows its owner only when told to
            if (w.getGoal() instanceof net.minecraft.world.entity.ai.goal.FollowOwnerGoal follow) {
                mob.goalSelector.removeGoal(follow);
                mob.goalSelector.addGoal(w.getPriority(), new Gated(follow, () -> friendOf(mob) == null || orders(mob).rawCommand() == Orders.Command.FOLLOW));
            }
        }
        mob.goalSelector.addGoal(5, new FollowEntityGoal(mob, () -> leaderOf(mob), 1.0, 6.0F, 3.0F));
        mob.goalSelector.addGoal(1, new KinOrderGoal(mob));
        mob.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(mob, LivingEntity.class, 10, true, false,
                e -> friendOf(mob) != null && orders(mob).wants(mob, e)) {
            @Override
            public boolean canUse() {
                if (friendOf(mob) == null) return false;
                Orders.Stance s = orders(mob).stance();
                return s != Orders.Stance.NATURAL && s != Orders.Stance.PEACEFUL && super.canUse();
            }
        });
    }

    /** Before she has a friend, a kin mob trails its girl (once befriended, its orders move it: KinOrderGoal). */
    @Nullable
    private static LivingEntity leaderOf(Mob mob) {
        return friendOf(mob) != null ? null : girlOf(mob, RANGE);
    }

    /** Sit, patrol and follow for befriended vanilla mobs (tamed pets sit the vanilla way). */
    private static final class KinOrderGoal extends Goal {
        private final PathfinderMob mob;
        @Nullable
        private net.minecraft.world.phys.Vec3 centre;
        private int cooldown;
        private int repath;

        KinOrderGoal(PathfinderMob mob) {
            this.mob = mob;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        private Orders.Command command() {
            return friendOf(this.mob) == null ? Orders.Command.FREE_ROAM : orders(this.mob).rawCommand();
        }

        private double radius() {
            return this.command() == Orders.Command.PATROL ? Orders.PATROL_RADIUS : Orders.FOLLOW_RADIUS;
        }

        @Nullable
        private net.minecraft.world.phys.Vec3 findCentre() {
            Orders o = orders(this.mob);
            return switch (this.command()) {
                case PATROL -> o.anchor() == null ? null : net.minecraft.world.phys.Vec3.atBottomCenterOf(o.anchor());
                case FOLLOW -> {
                    Player p = this.mob.level().getPlayerByUUID(friendOf(this.mob));
                    yield p != null && !p.isSpectator() ? p.position() : null;
                }
                default -> null;
            };
        }

        @Override
        public boolean canUse() {
            Orders.Command c = this.command();
            if (c == Orders.Command.SIT) return !(this.mob instanceof TamableAnimal);
            if (c == Orders.Command.FREE_ROAM || --this.cooldown > 0) return false;
            this.cooldown = 5;
            this.centre = this.findCentre();
            double r = this.mob.getTarget() == null ? this.radius() : this.radius() * 2;
            return this.centre != null && this.mob.distanceToSqr(this.centre) > r * r;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.command() == Orders.Command.SIT) return !(this.mob instanceof TamableAnimal);
            this.centre = this.findCentre();
            double r = this.radius() * 0.5;
            return this.centre != null && this.mob.distanceToSqr(this.centre) > r * r;
        }

        @Override
        public void start() {
            this.repath = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.command() == Orders.Command.SIT) {
                this.mob.getNavigation().stop();
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.0, 1.0, 0.0));
                return;
            }
            if (this.command() == Orders.Command.FOLLOW && this.mob.distanceToSqr(this.centre) > 40 * 40) {
                this.mob.teleportTo(this.centre.x, this.centre.y, this.centre.z);
                this.mob.getNavigation().stop();
                return;
            }
            if (--this.repath <= 0) {
                this.repath = 20;
                this.mob.getNavigation().moveTo(this.centre.x, this.centre.y, this.centre.z, 1.15);
            }
        }
    }

    /** A vanilla goal that only runs while a condition holds. */
    private static final class Gated extends Goal {
        private final Goal inner;
        private final java.util.function.BooleanSupplier when;

        Gated(Goal inner, java.util.function.BooleanSupplier when) {
            this.inner = inner;
            this.when = when;
            this.setFlags(inner.getFlags());
        }

        @Override
        public boolean canUse() {
            return this.when.getAsBoolean() && this.inner.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.when.getAsBoolean() && this.inner.canContinueToUse();
        }

        @Override
        public void start() {
            this.inner.start();
        }

        @Override
        public void stop() {
            this.inner.stop();
        }

        @Override
        public void tick() {
            this.inner.tick();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.inner.requiresUpdateEveryTick();
        }
    }

    /** A vanilla "run from players" goal, switched off while the nearest player is one this mob ignores. */
    private static final class UnlessKin extends Goal {
        private final Mob mob;
        private final Goal inner;

        UnlessKin(Mob mob, Goal inner) {
            this.mob = mob;
            this.inner = inner;
            this.setFlags(inner.getFlags());
        }

        @Override
        public boolean canUse() {
            Player p = this.mob.level().getNearestPlayer(this.mob, 12.0);
            return (p == null || !ignores(this.mob, p)) && this.inner.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.inner.canContinueToUse();
        }

        @Override
        public void start() {
            this.inner.start();
        }

        @Override
        public void stop() {
            this.inner.stop();
        }

        @Override
        public void tick() {
            this.inner.tick();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.inner.requiresUpdateEveryTick();
        }

        @Override
        public boolean isInterruptable() {
            return this.inner.isInterruptable();
        }
    }
}
