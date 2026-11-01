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
    /** The guide pages after the contents: befriending, the hearth, expeditions, weapons. */
    public static final List<String> GUIDE = List.of("friends", "hearth", "expedition", "weapons");

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
            if (player.hasLineOfSight(e)) record(player, e, ((Bonded) e).getAffection(player) >= Orders.AFFECTION);
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
        boolean news = add(data, "Met", id) | (befriended && add(data, "Friends", id));
        if (news) player.setData(ModAttachments.JOURNAL, data);
        return news;
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
        List<Filterable<Component>> pages = new ArrayList<>();
        MutableComponent cover = Component.translatable("journal.monstergirls.title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                .append("\n\n").append(Component.translatable("journal.monstergirls.girls", girls.size(), GirlSpecies.values().length).withStyle(ChatFormatting.RESET))
                .append("\n").append(Component.translatable("journal.monstergirls.friends", friends.size()).withStyle(ChatFormatting.RESET))
                .append("\n").append(Component.translatable("journal.monstergirls.others", others.size()).withStyle(ChatFormatting.RESET));
        String missedBy = player.getData(ModAttachments.JOURNAL).getString("MissedBy");
        if (!missedBy.isEmpty()) cover.append("\n\n").append(Component.translatable("journal.monstergirls.missed", missedBy).withStyle(ChatFormatting.RESET));
        if (met.isEmpty()) cover.append("\n\n").append(Component.translatable("journal.monstergirls.empty").withStyle(ChatFormatting.RESET, ChatFormatting.ITALIC));
        pages.add(Filterable.passThrough(cover));
        for (String guide : GUIDE) {                                                 // how to live with them: hearth, expeditions, weapons
            pages.add(Filterable.passThrough(Component.translatable("journal.monstergirls.guide." + guide + ".title").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                    .append("\n\n").append(Component.translatable("journal.monstergirls.guide." + guide).withStyle(ChatFormatting.RESET, ChatFormatting.BLACK))));
        }
        List<String> all = new ArrayList<>(girls);
        all.addAll(others);
        for (String id : all) {
            boolean girl = girls.contains(id);
            MutableComponent page = Component.translatable("entity.monstergirls." + id).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE)
                    .append("\n").append((friends.contains(id) ? Component.literal("★ ").append(Component.translatable("journal.monstergirls.befriended"))
                            : Component.translatable("journal.monstergirls.met")).withStyle(ChatFormatting.RESET, friends.contains(id) ? ChatFormatting.GOLD : ChatFormatting.GRAY));
            if (girl) {
                page.append("\n\n").append(Component.translatable("journal.monstergirls.home",
                        Component.translatable("journal.monstergirls." + id + ".home")).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_GREEN));
                page.append("\n\n").append(Component.translatable("journal.monstergirls." + id + ".signature").withStyle(ChatFormatting.RESET, ChatFormatting.BLACK));
            }
            pages.add(Filterable.passThrough(page));
        }
        return new WrittenBookContent(Filterable.passThrough("Harmontide Journal"), player.getName().getString(), 0, pages, true);
    }
}
