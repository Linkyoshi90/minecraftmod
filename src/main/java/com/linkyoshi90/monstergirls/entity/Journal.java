package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The Harmontide journal: every kind of creature a player has met (one of ours in sight within 12 blocks) and the
 * ones they have befriended (friendship 10 or more), kept on the player (it survives death) and shown as a book by
 * the journal item: a contents page, then a page for each kind met, its home and what it does once befriended.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Journal {
    private static final double SIGHT = 12.0;
    /** The guide pages after the contents: befriending, the hearth, expeditions, weapons, scouting, life at home, the milking machine, keepsakes, dairy, nursing. */
    public static final List<String> GUIDE = List.of("friends", "hearth", "expedition", "weapons", "scouting", "home", "milking", "keepsakes", "dairy", "nursing", "legends", "encounters", "chores", "festivals", "market", "rivals", "roles", "teamwork", "bestiary", "treats", "games", "mining", "foraging", "longing", "stargazing", "favours", "stories", "colds", "visits", "watch", "tree", "rain", "habits", "nesting");
    /** Bestiary: sightings (one per two seconds in view) before her home shows, and before her whole page does (befriending her does too). */
    public static final int KNOWN = 3, STUDIED = 20, NATURALIST = 25;
    public static final String SEEN = "Seen", NATURALIST_ADVANCEMENT = "naturalist";
    /** How many givers the milk page lists. */
    public static final int MILK_LINES = 8;
    /** The tree of life: branches whose every kind they have met, and how many branch lines fit on a page. */
    public static final String BRANCHES = "Branches", WHOLE_BRANCH_ADVANCEMENT = "whole_branch";
    /** A branch with this many kinds or fewer left to meet gets hints where they live ('Still to meet'), this many to a page. */
    public static final int LAST_FEW = 2, STILL_LINES = 4;
    public static final int TREE_LINES = 11;                                // a page is 14 lines: the title, a blank, and room for one name that wraps
    /** Word of a first friend reaches wild kin this far away, and warms them this much. */
    public static final double WORD_RANGE = 24.0;
    public static final int WORD = 3;
    private static java.util.Map<Clade, List<String>> members;
    private static final String NL_TEXT = String.valueOf((char) 10);
    /** Every milk they have drunk (the clades' names), and the advancement for all of them. */
    public static final String TASTED = "Tasted", CONNOISSEUR_ADVANCEMENT = "connoisseur";

    private Journal() {
    }

    /** Every player knows the journal's and the hearth's recipes; a player's first visit brings a journal. */
    @SubscribeEvent
    public static void onLogin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.awardRecipesByKey(List.of(MonsterGirlsMod.id("harmontide_journal"), MonsterGirlsMod.id("hearth")));
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        if (!data.getBoolean("Given") && !net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) {   // the tests' players keep empty hands
            data.putBoolean("Given", true);
            player.setData(ModAttachments.JOURNAL, data);
            net.minecraft.world.item.ItemStack book = new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.HARMONTIDE_JOURNAL.get());
            if (!player.getInventory().add(book)) player.drop(book, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0 || player.isSpectator()) return;
        for (Entity e : player.level().getEntities(player, player.getBoundingBox().inflate(SIGHT), e -> e instanceof Bonded && isOurs(e))) {
            if (player.hasLineOfSight(e)) {
                record(player, e, ((Bonded) e).getAffection(player) >= Orders.AFFECTION);
                if (((Bonded) e).getAffection(player) >= DEVOTED && !((LivingEntity) e).isBaby()) devoted(player, e);
                charmed(player, e);
                watch(player, e);
            }
        }
    }

    private static boolean isOurs(Entity e) {
        return MonsterGirlsMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace());
    }

    /** She has been seen (and perhaps befriended). Whether that was news for the journal. */
    public static boolean record(Player player, Entity creature, boolean befriended) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType()).getPath();
        if (creature instanceof LivingEntity l && l.isBaby()) befriended = false;   // a child's friendship is her mother's page
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        boolean newFriend = befriended && read(player, "Friends").stream().noneMatch(id::equals);
        boolean news = add(data, "Met", id) | (befriended && add(data, "Friends", id));
        if (newFriend) wordGetsAround(player, creature);
        if (news) {
            player.setData(ModAttachments.JOURNAL, data);
            Rarity.recorded(player, creature, befriended);
            branch(player, Clade.of(id));
        }
        return news;
    }

    /**
     * The first friend of a kind: word gets around her branch of the tree of life. Wild grown ones of the same clade nearby who
     * have never warmed to this player start a little fond of them. How many heard.
     */
    public static int wordGetsAround(Player player, Entity friend) {
        Clade clade = Clade.of(friend);
        if (clade == null || !(player.level() instanceof net.minecraft.server.level.ServerLevel level)) return 0;
        int n = 0;
        for (Entity e : level.getEntities(friend, friend.getBoundingBox().inflate(WORD_RANGE), e -> e instanceof Bonded && Clade.of(e) == clade)) {
            if (!(e instanceof LivingEntity l) || l.isBaby() || ((Bonded) e).getAffection(player) > 0) continue;
            ((Bonded) e).addAffection(player, WORD);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getEyeY() + 0.3, e.getZ(), 3, 0.3, 0.2, 0.3, 0);
            n++;
        }
        if (n > 0 && player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("message.monstergirls.word_gets_around", friend.getName(),
                    Component.translatable("clade.monstergirls." + clade.key())).withStyle(ChatFormatting.GREEN), true);
        }
        return n;
    }

    public static final String CHARMED = "CharmGreeted";
    public static final int CHARM_WARMTH = 2;

    /**
     * A wild grown one sees her own branch's charm in their offhand: she knows they have met all her kin, and starts a little fond
     * of them ({@link #CHARM_WARMTH}, once per girl). Whether she was charmed.
     */
    public static boolean charmed(Player player, Entity e) {
        if (!(e instanceof Bonded b) || !(e instanceof LivingEntity l) || l.isBaby() || b.getAffection(player) > 0 || e.getPersistentData().getBoolean(CHARMED)) return false;
        Clade clade = Clade.of(e);
        if (clade == null || player.getOffhandItem().get(com.linkyoshi90.monstergirls.registry.ModComponents.CLADE.get()) != clade) return false;
        e.getPersistentData().putBoolean(CHARMED, true);
        b.addAffection(player, CHARM_WARMTH);
        if (e.level() instanceof net.minecraft.server.level.ServerLevel level) level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getEyeY() + 0.3, e.getZ(), 4, 0.3, 0.2, 0.3, 0);
        if (player instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.charmed", e.getName()).withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    /** Affection at which a friend is devoted, and goes in the journal's list of dearest friends; how many lines fit a page. */
    public static final int DEVOTED = 60, DEAREST_LINES = 4;                // each line "name, kind (day n)" wraps to two or three of a page's 14
    public static final String DEAREST = "Dearest";

    /** She is devoted to them: the first time, the journal notes her name, her kind and the day. Whether that was news. */
    public static boolean devoted(Player player, Entity girl) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        ListTag list = data.getList(DEAREST, Tag.TAG_COMPOUND);
        String id = girl.getUUID().toString();
        for (Tag t : list) if (((CompoundTag) t).getString("Id").equals(id)) return false;
        CompoundTag entry = new CompoundTag();
        entry.putString("Id", id);
        entry.putString("Name", girl.getName().getString());
        entry.putString("Kind", BuiltInRegistries.ENTITY_TYPE.getKey(girl.getType()).getPath());
        entry.putLong("Day", player.level().getDayTime() / 24000L + 1);
        list.add(entry);
        data.put(DEAREST, list);
        player.setData(ModAttachments.JOURNAL, data);
        if (player instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.devoted", girl.getName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }

    /** Every kind on each branch of the tree of life, in registry order (the harpies' and anurans' young have no branch of their own). */
    public static List<String> members(Clade clade) {
        if (members == null) {
            java.util.Map<Clade, List<String>> m = new java.util.EnumMap<>(Clade.class);
            for (Clade c : Clade.values()) m.put(c, new ArrayList<>());
            for (var key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                Clade c = key.getNamespace().equals(MonsterGirlsMod.MODID) ? Clade.of(key.getPath()) : null;
                if (c != null) m.get(c).add(key.getPath());
            }
            members = m;
        }
        return members.get(clade);
    }

    /** How many kinds on this branch they have met. */
    public static int metOn(Player player, Clade clade) {
        Set<String> met = met(player);
        int n = 0;
        for (String id : members(clade)) if (met.contains(id)) n++;
        return n;
    }

    /** They met a kind on this branch: if that was the last one, the branch is whole (a message, once, and the advancement). Whether it was. */
    /**
     * Bigger boughs of the tree of life: every branch of one of these complete is an advancement of its own (grove_<name>),
     * and every branch of all is the whole tree.
     */
    public static final java.util.Map<String, List<Clade>> BOUGHS = java.util.Map.of(
            "carnivores", List.of(Clade.FELIFORMIA, Clade.CANIDAE, Clade.ARCTOIDEA),
            "gnawers", List.of(Clade.RODENTIA, Clade.LAGOMORPHA),
            "hooves", List.of(Clade.PERISSODACTYLA, Clade.BOVIDAE, Clade.CERVIDAE, Clade.CAMELIDAE, Clade.SUINA, Clade.WHIPPOMORPHA),
            "reptiles", List.of(Clade.ARCHOSAURIA, Clade.TESTUDINES, Clade.SQUAMATA),
            "arthropods", List.of(Clade.CHELICERATA, Clade.HEXAPODA, Clade.MALACOSTRACA),
            "legends", java.util.Arrays.stream(Clade.values()).filter(Clade::mythic).toList());
    public static final String WHOLE_TREE_ADVANCEMENT = "whole_tree";

    /** The boughs (BOUGHS keys) whose every branch they have made whole. */
    public static List<String> boughs(Player player) {
        Set<String> whole = wholeBranches(player);
        List<String> out = new ArrayList<>();
        for (var e : BOUGHS.entrySet()) if (e.getValue().stream().allMatch(c -> whole.contains(c.name()))) out.add(e.getKey());
        return out;
    }

    private static void boughsGrown(ServerPlayer sp, Clade clade) {
        Set<String> whole = wholeBranches(sp);
        for (var e : BOUGHS.entrySet()) {
            if (!e.getValue().contains(clade) || !e.getValue().stream().allMatch(c -> whole.contains(c.name()))) continue;
            sp.displayClientMessage(Component.translatable("message.monstergirls.bough", Component.translatable("bough.monstergirls." + e.getKey()))
                    .withStyle(ChatFormatting.GOLD), false);
            var a = sp.server.getAdvancements().get(MonsterGirlsMod.id("bough_" + e.getKey()));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
        if (whole.size() >= Clade.values().length) {
            var a = sp.server.getAdvancements().get(MonsterGirlsMod.id(WHOLE_TREE_ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
    }

    public static boolean branch(Player player, @javax.annotation.Nullable Clade clade) {
        if (clade == null || metOn(player, clade) < members(clade).size()) return false;
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        if (!add(data, BRANCHES, clade.name())) return false;
        player.setData(ModAttachments.JOURNAL, data);
        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("message.monstergirls.whole_branch",
                    Component.translatable("clade.monstergirls." + clade.key())).withStyle(ChatFormatting.DARK_GREEN), false);
            var a = sp.server.getAdvancements().get(MonsterGirlsMod.id(WHOLE_BRANCH_ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
            boughsGrown(sp, clade);
        }
        net.minecraft.world.item.ItemStack charm = com.linkyoshi90.monstergirls.item.BranchCharmItem.of(clade);   // the branch's charm, to keep
        if (!player.getInventory().add(charm)) player.drop(charm, false);
        return true;
    }

    /** They drank a bucket of this milk: the first of its kind is noted (a message with the count), all of them is the advancement. Whether it was new. */
    public static boolean taste(Player player, com.linkyoshi90.monstergirls.item.FamilyMilk milk) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        if (!add(data, TASTED, milk.name())) return false;
        player.setData(ModAttachments.JOURNAL, data);
        int n = tasted(player);
        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("message.monstergirls.tasted",
                    Component.translatable("item.monstergirls." + milk.id()), n, com.linkyoshi90.monstergirls.item.FamilyMilk.values().length).withStyle(ChatFormatting.DARK_AQUA), true);
            if (n >= com.linkyoshi90.monstergirls.item.FamilyMilk.values().length) {
                var a = sp.server.getAdvancements().get(MonsterGirlsMod.id(CONNOISSEUR_ADVANCEMENT));
                if (a != null) sp.getAdvancements().award(a, "done");
            }
        }
        return true;
    }

    /** How many different milks they have drunk. */
    public static int tasted(Player player) {
        int n = 0;
        for (String s : read(player, TASTED)) if (java.util.Arrays.stream(com.linkyoshi90.monstergirls.item.FamilyMilk.values()).anyMatch(m -> m.name().equals(s))) n++;
        return n;
    }

    /** The branches whose every kind they have met. */
    public static Set<String> wholeBranches(Player player) {
        return read(player, BRANCHES);
    }

    /** One more sighting of her kind for the bestiary (up to STUDIED). How many sightings now. */
    public static int watch(Player player, Entity creature) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType()).getPath();
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        CompoundTag seen = data.getCompound(SEEN);
        int n = seen.getInt(id);
        if (n >= STUDIED) return n;
        seen.putInt(id, ++n);
        data.put(SEEN, seen);
        player.setData(ModAttachments.JOURNAL, data);
        if (n == STUDIED && player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.translatable("message.monstergirls.studied", creature.getType().getDescription()).withStyle(ChatFormatting.DARK_GREEN), true);
            if (studied(sp) >= NATURALIST) {
                var a = sp.server.getAdvancements().get(MonsterGirlsMod.id(NATURALIST_ADVANCEMENT));
                if (a != null) sp.getAdvancements().award(a, "done");
            }
        }
        return n;
    }

    /** Heard of a kind (a story by the fire): her page opens as if seen KNOWN times. Whether that was news. */
    public static boolean hearOf(Player player, String id) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        CompoundTag seen = data.getCompound(SEEN);
        if (seen.getInt(id) >= KNOWN) return false;
        seen.putInt(id, KNOWN);
        data.put(SEEN, seen);
        add(data, "Heard", id);
        player.setData(ModAttachments.JOURNAL, data);
        return true;
    }

    public static int sightings(Player player, String id) {
        return player.getData(ModAttachments.JOURNAL).getCompound(SEEN).getInt(id);
    }

    /** Whether her whole page is known: watched long enough, or befriended. */
    public static boolean isStudied(Player player, String id) {
        return sightings(player, id) >= STUDIED || friends(player).contains(id);
    }

    /** How many kinds are fully known. */
    public static int studied(Player player) {
        int n = 0;
        for (String id : met(player)) if (isStudied(player, id)) n++;
        return n;
    }

    /** Who last missed them: a line on the cover. */
    public static void recordMissed(Player player, Entity creature) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        data.putString("MissedBy", creature.getName().getString());
        player.setData(ModAttachments.JOURNAL, data);
    }

    private static boolean add(CompoundTag data, String key, String id) {
        ListTag list = data.getList(key, Tag.TAG_STRING);
        for (Tag t : list) if (t.getAsString().equals(id)) return false;
        list.add(StringTag.valueOf(id));
        data.put(key, list);
        return true;
    }

    public static Set<String> met(Player player) {
        return read(player, "Met");
    }

    public static Set<String> friends(Player player) {
        return read(player, "Friends");
    }

    private static Set<String> read(Player player, String key) {
        Set<String> out = new LinkedHashSet<>();
        for (Tag t : player.getData(ModAttachments.JOURNAL).getList(key, Tag.TAG_STRING)) out.add(t.getAsString());
        return out;
    }

    /** The journal's pages for this player, as a written book. */
    public static WrittenBookContent book(Player player) {
        Set<String> met = met(player), friends = friends(player);
        List<String> girls = new ArrayList<>(), others = new ArrayList<>();
        for (String id : met) (GirlSpecies.byId(id) != null ? girls : others).add(id);
        int metGirls = girls.size();
        for (String id : read(player, "Heard")) if (!met.contains(id)) girls.add(id);   // known only from stories by the fire
        List<Filterable<Component>> pages = new ArrayList<>();
        MutableComponent cover = Component.translatable("journal.monstergirls.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                .append("\n\n").append(Component.translatable("journal.monstergirls.girls", metGirls, GirlSpecies.values().length).withStyle(ChatFormatting.RESET))
                .append("\n").append(Component.translatable("journal.monstergirls.friends", friends.size()).withStyle(ChatFormatting.RESET))
                .append("\n").append(Component.translatable("journal.monstergirls.others", others.size()).withStyle(ChatFormatting.RESET))
                .append("\n").append(Component.translatable("journal.monstergirls.studied", studied(player), met.size()).withStyle(ChatFormatting.RESET));
        String missedBy = player.getData(ModAttachments.JOURNAL).getString("MissedBy");
        int letters = player.getData(ModAttachments.JOURNAL).getInt(Letters.COUNT);
        if (met.isEmpty()) cover.append("\n\n").append(Component.translatable("journal.monstergirls.empty").withStyle(ChatFormatting.RESET, ChatFormatting.ITALIC));
        pages.add(Filterable.passThrough(cover));
        cover = Component.translatable("journal.monstergirls.milestones").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE).append("\n");   // over the page: one held up to 21 lines of 14
        List<String> gone = NightWatch.remembered(player);
        if (!gone.isEmpty()) cover.append("\n").append(Component.translatable("journal.monstergirls.remembered", String.join(", ", gone)).withStyle(ChatFormatting.RESET, ChatFormatting.GRAY));
        int stars = player.getData(ModAttachments.JOURNAL).getList(Stargazing.CHART, Tag.TAG_STRING).size();
        if (stars > 0) cover.append("\n").append(Component.translatable("journal.monstergirls.constellations", stars, Stargazing.CONSTELLATIONS.size()).withStyle(ChatFormatting.RESET));
        int branchesWhole = wholeBranches(player).size();
        if (branchesWhole > 0) cover.append(NL_TEXT).append(Component.translatable("journal.monstergirls.branches", branchesWhole, Clade.values().length,
                boughs(player).size(), BOUGHS.size()).withStyle(ChatFormatting.RESET));
        int tasted = tasted(player);
        if (tasted > 0) cover.append("\n").append(Component.translatable("journal.monstergirls.tasted", tasted, com.linkyoshi90.monstergirls.item.FamilyMilk.values().length).withStyle(ChatFormatting.RESET));
        if (letters > 0) cover.append("\n").append(Component.translatable("journal.monstergirls.letters", letters).withStyle(ChatFormatting.RESET));
        if (!missedBy.isEmpty()) cover.append("\n\n").append(Component.translatable("journal.monstergirls.missed", missedBy).withStyle(ChatFormatting.RESET));
        int milestones = (gone.isEmpty() ? 0 : 1) + stars + branchesWhole + tasted + letters + (missedBy.isEmpty() ? 0 : 1);
        if (milestones > 0) pages.add(Filterable.passThrough(cover));
        for (String guide : GUIDE) {                                                 // how to live with them: hearth, expeditions, weapons
            int parts = GuidePages.of(guide);                                        // a long guide runs over pages (tools/split_guides.py)
            MutableComponent page = null;
            for (int i = 1; i <= parts; i++) {
                if (page != null) pages.add(Filterable.passThrough(page));
                page = Component.translatable("journal.monstergirls.guide." + guide + ".title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                        .append("\n\n").append(Component.translatable("journal.monstergirls.guide." + guide + (parts > 1 ? ".p" + i : ""))
                                .withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
            }
            if (guide.equals("festivals")) {                                         // and when the next one is
                long time = player.level().getDayTime();
                var next = com.linkyoshi90.monstergirls.entity.girl.Festivals.next(time);
                int days = com.linkyoshi90.monstergirls.entity.girl.Festivals.daysUntil(time);
                page.append("\n\n").append(Component.translatable(days == 0 ? "journal.monstergirls.festival.today" : "journal.monstergirls.festival.next",
                        Component.translatable("festival.monstergirls." + next.key()), days).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_PURPLE));
            }
            pages.add(Filterable.passThrough(page));
        }
        CompoundTag milk = player.getData(ModAttachments.JOURNAL).getCompound("Milk");
        if (!milk.isEmpty()) {                                                       // every bucket a friend has given them, most first
            MutableComponent page = Component.translatable("journal.monstergirls.milk.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                    .append("\n").append(Component.translatable("journal.monstergirls.milk.total", player.getData(ModAttachments.JOURNAL).getInt("MilkTotal")).withStyle(ChatFormatting.RESET, ChatFormatting.GRAY))
                    .append("\n");
            List<String> givers = new ArrayList<>(milk.getAllKeys());
            givers.sort((a, b) -> milk.getInt(b) - milk.getInt(a));
            for (String name : givers.subList(0, Math.min(MILK_LINES, givers.size()))) {
                page.append("\n").append(Component.translatable("journal.monstergirls.milk.line", name, milk.getInt(name)).withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
            }
            pages.add(Filterable.passThrough(page));
        }
        Set<String> whole = wholeBranches(player);
        List<MutableComponent> tree = new ArrayList<>();                             // each branch started: met of all, whole ones starred
        for (Clade clade : Clade.values()) {
            int n = metOn(player, clade);
            if (n == 0) continue;
            boolean done = whole.contains(clade.name());
            tree.add(Component.literal(done ? "★ " : "").append(Component.translatable("journal.monstergirls.tree.line",
                    Component.translatable("family.monstergirls." + clade.key()), n, members(clade).size()))
                    .withStyle(ChatFormatting.RESET, done ? ChatFormatting.GOLD : clade.mythic() ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_GREEN));
        }
        for (int i = 0; i < tree.size(); i += TREE_LINES) {
            MutableComponent page = Component.translatable("journal.monstergirls.tree.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE).append("\n");
            for (MutableComponent line : tree.subList(i, Math.min(i + TREE_LINES, tree.size()))) page.append("\n").append(line);
            pages.add(Filterable.passThrough(page));
        }
        List<MutableComponent> still = new ArrayList<>();                            // a branch nearly whole: where its last kinds live, not who they are
        for (Clade clade : Clade.values()) {
            int n = metOn(player, clade);
            if (n == 0 || members(clade).size() - n > LAST_FEW) continue;
            for (String id : members(clade)) {
                if (met.contains(id) || GirlSpecies.byId(id) == null) continue;
                still.add(Component.translatable("journal.monstergirls.still.line", Component.translatable("family.monstergirls." + clade.key()),
                        Component.translatable("journal.monstergirls." + id + ".home")).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_GREEN));
            }
        }
        for (int i = 0; i < still.size(); i += STILL_LINES) {
            MutableComponent page = Component.translatable("journal.monstergirls.still.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE).append(NL_TEXT);
            for (MutableComponent line : still.subList(i, Math.min(i + STILL_LINES, still.size()))) page.append(NL_TEXT).append(line);
            pages.add(Filterable.passThrough(page));
        }
        ListTag dearest = player.getData(ModAttachments.JOURNAL).getList(DEAREST, Tag.TAG_COMPOUND);
        for (int i = 0; i < dearest.size(); i += DEAREST_LINES) {                // the friends devoted to them, with the day it happened
            MutableComponent page = Component.translatable("journal.monstergirls.dearest.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE).append(NL_TEXT);
            for (int j = i; j < Math.min(i + DEAREST_LINES, dearest.size()); j++) {
                CompoundTag d = dearest.getCompound(j);
                page.append(NL_TEXT).append(Component.translatable("journal.monstergirls.dearest.line", d.getString("Name"),
                        Component.translatable("entity.monstergirls." + d.getString("Kind")), d.getLong("Day")).withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
            }
            pages.add(Filterable.passThrough(page));
        }
        List<String> all = new ArrayList<>(girls);
        all.addAll(others);
        all.sort(java.util.Comparator.comparingInt(id -> {                           // branch by branch, as they were met within one
            Clade c = Clade.of(id);
            return c == null ? Clade.values().length : c.ordinal();
        }));
        for (String id : all) {
            boolean girl = girls.contains(id);
            MutableComponent page = Component.translatable("entity.monstergirls." + id).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                    .append("\n").append((friends.contains(id) ? Component.literal("★ ").append(Component.translatable("journal.monstergirls.befriended"))
                            : Component.translatable(met.contains(id) ? "journal.monstergirls.met" : "journal.monstergirls.heard")).withStyle(ChatFormatting.RESET, friends.contains(id) ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            Clade clade = Clade.of(id);
            if (clade != null) page.append("\n").append(Component.translatable("clade.monstergirls." + clade.key())
                    .withStyle(ChatFormatting.RESET, clade.mythic() ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_AQUA));
            Rarity.Tier tier = Rarity.of(id);
            if (tier != Rarity.Tier.COMMON) page.append("\n").append(Component.translatable(tier.key()).withStyle(ChatFormatting.RESET, tier == Rarity.Tier.LEGENDARY ? ChatFormatting.GOLD : ChatFormatting.DARK_AQUA));
            int seen = sightings(player, id);
            boolean known = seen >= KNOWN || friends.contains(id), studied = isStudied(player, id);
            if (!known) {                                                            // a glimpse: just her name, for now
                page.append("\n\n").append(Component.translatable("journal.monstergirls.glimpsed").withStyle(ChatFormatting.RESET, ChatFormatting.ITALIC, ChatFormatting.GRAY));
                pages.add(Filterable.passThrough(page));
                continue;
            }
            if (Clade.guest(id)) page.append("\n").append(Component.translatable("journal.monstergirls.guest").withStyle(ChatFormatting.RESET, ChatFormatting.ITALIC, ChatFormatting.GRAY));
            SpawnConditions.When when = SpawnConditions.of(id);
            if (when != null) page.append("\n").append(Component.translatable(when.key()).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_PURPLE));
            if (girl) {
                page.append("\n\n").append(Component.translatable("journal.monstergirls.home",
                        Component.translatable("journal.monstergirls." + id + ".home")).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_GREEN));
                if (!studied) page.append("\n\n").append(Component.translatable("journal.monstergirls.watch_more", seen, STUDIED).withStyle(ChatFormatting.RESET, ChatFormatting.ITALIC, ChatFormatting.GRAY));
                else {                                                               // what she does, on its own page(s) under her name (a long one runs over)
                    int parts = GuidePages.signature(id);
                    for (int i = 1; i <= parts; i++) {
                        pages.add(Filterable.passThrough(page));
                        page = Component.translatable("entity.monstergirls." + id).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                                .append("\n\n").append(Component.translatable("journal.monstergirls." + id + ".signature" + (parts > 1 ? ".p" + i : ""))
                                        .withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
                    }
                }
            }
            pages.add(Filterable.passThrough(page));
        }
        return new WrittenBookContent(Filterable.passThrough("Harmontide Journal"), player.getName().getString(), 0, pages, true);
    }
}
