package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The honeymoon trip (2026-11-10): at the wedding, and again at each vow renewal, a wife names a place she has always wanted to see,
 * one of the lovelier biomes of the overworld. Go there together (the player standing in it, she within {@link #TOGETHER}) and the
 * trip is made: she gives the player a souvenir of the place with her name and the day on it, and a scout pack full of what the two of
 * them found there (Scouting.finds, as if two had gone scouting at her friendship); she is {@link #AFFECTION} closer and the honeymoon
 * runs a day longer. The family page remembers the places they've been, and the one she's still dreaming of.
 */
public final class HoneymoonTrip {
    public static final String WISH = "HoneymoonWish";
    public static final double TOGETHER = 16;
    public static final int AFFECTION = 6, ROLLS = 10;

    /** The places she may wish for, and the souvenir of each. */
    public static final Map<ResourceKey<Biome>, Item> PLACES = new LinkedHashMap<>();

    static {
        PLACES.put(Biomes.CHERRY_GROVE, Items.PINK_PETALS);
        PLACES.put(Biomes.FLOWER_FOREST, Items.LILY_OF_THE_VALLEY);
        PLACES.put(Biomes.SUNFLOWER_PLAINS, Items.SUNFLOWER);
        PLACES.put(Biomes.MEADOW, Items.CORNFLOWER);
        PLACES.put(Biomes.BEACH, Items.NAUTILUS_SHELL);
        PLACES.put(Biomes.WARM_OCEAN, Items.BRAIN_CORAL);
        PLACES.put(Biomes.ICE_SPIKES, Items.BLUE_ICE);
        PLACES.put(Biomes.MUSHROOM_FIELDS, Items.RED_MUSHROOM);
        PLACES.put(Biomes.BADLANDS, Items.ORANGE_TERRACOTTA);
        PLACES.put(Biomes.JUNGLE, Items.COCOA_BEANS);
        PLACES.put(Biomes.BAMBOO_JUNGLE, Items.BAMBOO);
        PLACES.put(Biomes.DESERT, Items.CACTUS);
        PLACES.put(Biomes.SAVANNA, Items.ACACIA_SAPLING);
        PLACES.put(Biomes.LUSH_CAVES, Items.SPORE_BLOSSOM);
    }

    private HoneymoonTrip() {
    }

    @Nullable
    public static ResourceKey<Biome> wish(Mob wife) {
        CompoundTag pd = wife.getPersistentData();
        return pd.contains(WISH) ? ResourceKey.create(Registries.BIOME, ResourceLocation.parse(pd.getString(WISH))) : null;
    }

    /** Test hook and planner: she wishes for this place. */
    public static void wishFor(Mob wife, ResourceKey<Biome> place) {
        wife.getPersistentData().putString(WISH, place.location().toString());
    }

    public static Component name(ResourceKey<Biome> biome) {
        return Component.translatable("biome." + biome.location().getNamespace() + "." + biome.location().getPath());
    }

    /** At the wedding or a renewal: she names a place she'd love to see with them (not where she is now, nor one they've been). */
    @Nullable
    public static ResourceKey<Biome> plan(ServerLevel level, Mob wife, Player player) {
        ResourceKey<Biome> here = level.getBiome(wife.blockPosition()).unwrapKey().orElse(null);
        List<String> been = been(player, wife);
        List<ResourceKey<Biome>> choices = new ArrayList<>();
        for (ResourceKey<Biome> b : PLACES.keySet()) if (!b.equals(here) && !been.contains(b.location().toString())) choices.add(b);
        if (choices.isEmpty()) for (ResourceKey<Biome> b : PLACES.keySet()) if (!b.equals(here)) choices.add(b);
        ResourceKey<Biome> pick = choices.get(wife.getRandom().nextInt(choices.size()));
        wishFor(wife, pick);
        note(player, wife, pick.location().toString());
        player.displayClientMessage(Component.translatable("message.harmontide.honeymoon_trip.wish", wife.getDisplayName(), name(pick))
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return pick;
    }

    /** The places this wife and player have been together, from her family-page entry. */
    public static List<String> been(Player player, Mob wife) {
        List<String> out = new ArrayList<>();
        CompoundTag e = entry(player, wife);
        if (e != null) for (Tag t : e.getList("Trips", Tag.TAG_STRING)) out.add(t.getAsString());
        return out;
    }

    @Nullable
    private static CompoundTag entry(Player player, Mob wife) {
        String id = wife.getUUID().toString();
        for (Tag t : Marriage.family(player)) if (((CompoundTag) t).getString("Id").equals(id)) return (CompoundTag) t;
        return null;
    }

    /** Checked every couple of seconds: a wife beside the player in the place she wished for. Who, or null. */
    @Nullable
    public static Mob arrived(ServerLevel level, Player player) {
        if (Marriage.count(player) == 0) return null;
        ResourceKey<Biome> here = level.getBiome(player.blockPosition()).unwrapKey().orElse(null);
        if (here == null) return null;
        for (Mob wife : Marriage.spousesNear(level, player, player, TOGETHER, null)) {
            if (here.equals(wish(wife))) {
                made(level, wife, player, here);
                return wife;
            }
        }
        return null;
    }

    /** The trip made: the souvenir, the pack of finds, the family page. */
    public static void made(ServerLevel level, Mob wife, Player player, ResourceKey<Biome> place) {
        wife.getPersistentData().remove(WISH);
        note(player, wife, null);
        Bonded b = (Bonded) wife;
        b.addAffection(player, AFFECTION);
        b.blushFor(600);
        Marriage.extendHoneymoon(level, wife, 24000L);
        long day = MatingSeason.today(level);
        ItemStack souvenir = new ItemStack(PLACES.getOrDefault(place, Items.FLOWER_POT));
        souvenir.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.souvenir", name(place)).withStyle(ChatFormatting.LIGHT_PURPLE));
        souvenir.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable("item.harmontide.souvenir.lore", wife.getName(), day).withStyle(ChatFormatting.GRAY))));
        give(player, souvenir);
        ItemStack pack = new ItemStack(ModItems.SCOUT_PACK.get());
        List<ItemStack> finds = Scouting.finds(place, ROLLS, b.getAffection(player), wife.getRandom());
        pack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(finds));
        give(player, pack);
        level.sendParticles(ParticleTypes.HEART, wife.getX(), wife.getEyeY() + 0.4, wife.getZ(), 16, 0.6, 0.5, 0.6, 0.0);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, wife.getX(), wife.getEyeY() + 0.6, wife.getZ(), 24, 0.8, 0.5, 0.8, 0.0);
        level.playSound(null, wife.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.8F, 1.3F);
        player.displayClientMessage(Component.translatable("message.harmontide.honeymoon_trip.made", wife.getDisplayName(), name(place))
                .withStyle(ChatFormatting.GOLD), false);
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);           // the family page remembers where they went
        ListTag family = journal.getList(Marriage.FAMILY_PAGE, Tag.TAG_COMPOUND);
        String id = wife.getUUID().toString();
        for (Tag t : family) {
            CompoundTag e = (CompoundTag) t;
            if (!e.getString("Id").equals(id)) continue;
            ListTag trips = e.getList("Trips", Tag.TAG_STRING);
            if (!trips.contains(net.minecraft.nbt.StringTag.valueOf(place.location().toString()))) trips.add(net.minecraft.nbt.StringTag.valueOf(place.location().toString()));
            e.put("Trips", trips);
        }
        journal.put(Marriage.FAMILY_PAGE, family);
        player.setData(ModAttachments.JOURNAL, journal);
        Dairy.award(player, "honeymoon_trip");
    }

    /** The wish on her family-page entry too, so the journal shows it while she's away. */
    private static void note(Player player, Mob wife, @Nullable String wish) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag family = journal.getList(Marriage.FAMILY_PAGE, Tag.TAG_COMPOUND);
        String id = wife.getUUID().toString();
        for (Tag t : family) {
            CompoundTag e = (CompoundTag) t;
            if (!e.getString("Id").equals(id)) continue;
            if (wish == null) e.remove("Wish");
            else e.putString("Wish", wish);
        }
        journal.put(Marriage.FAMILY_PAGE, family);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.addItem(stack)) player.drop(stack, false);
    }

    /** For the family page: where they've been, and where she still wants to go. */
    public static Component line(CompoundTag entry) {
        String wish = entry.contains("Wish") ? entry.getString("Wish") : null;
        net.minecraft.network.chat.MutableComponent out = Component.empty();
        ListTag trips = entry.getList("Trips", Tag.TAG_STRING);
        if (!trips.isEmpty()) {
            net.minecraft.network.chat.MutableComponent places = Component.empty();
            for (int i = 0; i < trips.size(); i++) {
                if (i > 0) places.append(", ");
                places.append(name(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(trips.getString(i)))));
            }
            out.append(Component.translatable("journal.harmontide.family.trips", places));
        }
        if (wish != null) out.append(Component.translatable("journal.harmontide.family.wish", name(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(wish)))));
        return out;
    }
}
