package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The scout pack (HARMONTIDE_CONCEPTS.md): hand one to a grown friend and she goes off for half a day to one of the biomes her kind lives in,
 * and brings the pack back full of that biome's materials. Friendship sets how much she brings (and the odds of something rare); a friend who
 * has been missing you (Missing, for the day after you meet again) brings half as much again, or twice as much if she missed you badly, scaled
 * by the missingRate setting. Unlike an expedition any Bonded can go, harpies and frogs too, and she brings nobody home. Saved on the player
 * beside the expeditions, under "Scouts".
 */
public final class Scouting {
    public static final long DURATION = 12000L;
    public static final int CAPACITY = 27, PARTY = 3, MIXED_BONUS = 2;
    private static final float MEMBER_SHARE = 0.6F;
    private static final String SCOUTS = "Scouts";

    /** Keywords of a biome's name, in order, with what's to be found there; the last item of each is the rare one. */
    private record Land(String words, Item... items) {
    }

    private static final List<Land> LANDS = List.of(
            new Land("crimson warped", Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.WEEPING_VINES, Items.SHROOMLIGHT, Items.NETHER_WART, Items.ANCIENT_DEBRIS),
            new Land("soul basalt nether", Items.QUARTZ, Items.GLOWSTONE_DUST, Items.MAGMA_CREAM, Items.BLAZE_POWDER, Items.SOUL_SAND, Items.ANCIENT_DEBRIS),
            new Land("the_end end_ _end", Items.CHORUS_FRUIT, Items.END_STONE, Items.ENDER_PEARL, Items.SHULKER_SHELL),
            new Land("deep_dark", Items.SCULK, Items.ECHO_SHARD, Items.BONE, Items.AMETHYST_SHARD, Items.ECHO_SHARD),
            new Land("dripstone", Items.POINTED_DRIPSTONE, Items.DRIPSTONE_BLOCK, Items.RAW_COPPER, Items.RAW_IRON, Items.DIAMOND),
            new Land("lush", Items.GLOW_BERRIES, Items.MOSS_BLOCK, Items.AZALEA, Items.SPORE_BLOSSOM, Items.CLAY_BALL, Items.LAPIS_LAZULI),
            new Land("mushroom", Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.MYCELIUM, Items.MUSHROOM_STEW, Items.GOLDEN_CARROT),
            new Land("ocean", Items.KELP, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.SEA_PICKLE, Items.SEAGRASS, Items.NAUTILUS_SHELL),
            new Land("river beach shore", Items.CLAY_BALL, Items.SAND, Items.SUGAR_CANE, Items.COD, Items.GRAVEL, Items.TURTLE_SCUTE),
            new Land("swamp mangrove", Items.MUD, Items.LILY_PAD, Items.VINE, Items.SLIME_BALL, Items.MANGROVE_PROPAGULE, Items.EMERALD),
            new Land("jungle bamboo", Items.BAMBOO, Items.COCOA_BEANS, Items.MELON_SLICE, Items.JUNGLE_LOG, Items.VINE, Items.GOLD_NUGGET),
            new Land("badlands", Items.RED_SAND, Items.TERRACOTTA, Items.GOLD_NUGGET, Items.DEAD_BUSH, Items.RAW_GOLD),
            new Land("desert", Items.SAND, Items.CACTUS, Items.SANDSTONE, Items.DEAD_BUSH, Items.GOLD_INGOT),
            new Land("savanna", Items.ACACIA_LOG, Items.WHEAT, Items.LEATHER, Items.SHORT_GRASS, Items.EMERALD),
            new Land("cherry meadow flower", Items.CHERRY_LOG, Items.PINK_PETALS, Items.HONEYCOMB, Items.POPPY, Items.DANDELION, Items.AMETHYST_SHARD),
            new Land("frozen ice snowy peaks slopes grove", Items.SNOWBALL, Items.ICE, Items.PACKED_ICE, Items.SPRUCE_LOG, Items.RABBIT_HIDE, Items.BLUE_ICE),
            new Land("taiga", Items.SPRUCE_LOG, Items.SWEET_BERRIES, Items.FERN, Items.PODZOL, Items.LEATHER, Items.EMERALD),
            new Land("dark_forest", Items.DARK_OAK_LOG, Items.RED_MUSHROOM, Items.COBWEB, Items.BONE, Items.EMERALD),
            new Land("forest", Items.OAK_LOG, Items.BIRCH_LOG, Items.APPLE, Items.STICK, Items.HONEYCOMB, Items.GOLDEN_APPLE),
            new Land("windswept hills stony", Items.COBBLESTONE, Items.RAW_COPPER, Items.COAL, Items.RAW_IRON, Items.EMERALD),
            new Land("", Items.WHEAT_SEEDS, Items.WHEAT, Items.LEATHER, Items.OAK_LOG, Items.FLINT, Items.GOLD_NUGGET));

    private Scouting() {
    }

    /** How many finds at this friendship: 5 at the first friend's 10, 14 at 100. */
    public static int baseRolls(int affection) {
        return 4 + affection / 10;
    }

    /** With how much she has missed them (0 to 2), half as much again per stage, at the setting's 100%. */
    public static int rolls(int affection, int missedStage) {
        float rate = Math.min(2.0F, HarmontideConfig.missingRate() / 100.0F);
        return Math.round(baseRolls(affection) * (1.0F + missedStage * 0.5F * rate));
    }

    /** Out of 400, the odds of each find being the rare one: at 100 friendship, one in four. */
    private static boolean rare(RandomSource random, int affection) {
        return random.nextInt(400) < affection;
    }

    /** One of the biomes her kind spawns in, from her spawn tag (so modded biomes count), or where she is. */
    public static ResourceKey<Biome> homeBiome(ServerLevel level, Mob mob) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        TagKey<Biome> tag = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(HarmontideMod.MODID, "spawns_" + id.getPath()));
        Optional<HolderSet.Named<Biome>> set = level.registryAccess().registryOrThrow(Registries.BIOME).getTag(tag);
        if (set.isPresent() && set.get().size() > 0) {
            Holder<Biome> biome = set.get().get(mob.getRandom().nextInt(set.get().size()));
            if (biome.unwrapKey().isPresent()) return biome.unwrapKey().get();
        }
        return level.getBiome(mob.blockPosition()).unwrapKey().orElse(net.minecraft.world.level.biome.Biomes.PLAINS);
    }

    private static Land land(ResourceKey<Biome> biome) {
        String name = biome.location().getPath();
        for (Land land : LANDS) {
            for (String word : land.words().split(" ")) {
                if (word.isEmpty() || name.contains(word)) return land;
            }
        }
        return LANDS.get(LANDS.size() - 1);
    }

    /** What she finds in that biome. */
    public static List<ItemStack> finds(ResourceKey<Biome> biome, int rolls, int affection, RandomSource random) {
        Item[] items = land(biome).items();
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            boolean lucky = rare(random, affection);
            Item item = items[lucky ? items.length - 1 : random.nextInt(items.length - 1)];
            ItemStack stack = new ItemStack(item, lucky ? 1 : 1 + random.nextInt(Math.min(3, item.getDefaultMaxStackSize())));
            boolean merged = false;
            for (ItemStack held : out) {
                if (ItemStack.isSameItem(held, stack) && held.getCount() + stack.getCount() <= held.getMaxStackSize()) {
                    held.grow(stack.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged && out.size() < CAPACITY) out.add(stack);
        }
        return out;
    }

    /** Her friend hands her an empty pack: she sets off. Whether she went. */
    public static boolean send(Mob mob, Player player, ItemStack pack) {
        return send(mob, player, pack, false);
    }

    /** With {@code party} (the player crouching) up to two more friends within 6 go with her: each adds finds of her own home, and each family past the first adds two more. */
    public static boolean send(Mob mob, Player player, ItemStack pack, boolean party) {
        if (!(mob.level() instanceof ServerLevel level) || !(mob instanceof Bonded bonded) || mob.isBaby() || mob.isPassenger() || mob.isLeashed()) return false;
        int affection = bonded.getAffection(player);
        if (affection < Orders.AFFECTION || hasFinds(pack)) return false;
        CompoundTag saved = new CompoundTag();
        if (!mob.saveAsPassenger(saved)) return false;
        CompoundTag e = new CompoundTag();
        e.put("Girl", saved);
        e.putLong("Back", level.getGameTime() + DURATION);
        e.putString("Dim", level.dimension().location().toString());
        e.putInt("Rolls", rolls(affection, Missing.activeStage(mob, player)));
        e.putInt("Affection", affection);
        e.putString("Biome", homeBiome(level, mob).location().toString());
        if (party) {
            ListTag members = new ListTag();
            java.util.Set<String> families = new java.util.HashSet<>(List.of(family(mob)));
            for (Mob other : HomeLife.friendsOf(level, player, mob.getBoundingBox().inflate(6))) {
                if (other == mob || members.size() >= PARTY - 1 || other.isPassenger() || other.isLeashed() || !(other instanceof Bonded b)) continue;
                CompoundTag saved2 = new CompoundTag();
                if (!other.saveAsPassenger(saved2)) continue;
                CompoundTag m = new CompoundTag();
                m.put("Girl", saved2);
                m.putInt("Rolls", (int) (rolls(b.getAffection(player), Missing.activeStage(other, player)) * MEMBER_SHARE) + (families.add(family(other)) ? MIXED_BONUS : 0));
                m.putInt("Affection", b.getAffection(player));
                m.putString("Biome", homeBiome(level, other).location().toString());
                members.add(m);
                other.discard();
            }
            e.put("Party", members);
        }
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(SCOUTS, Tag.TAG_COMPOUND);
        list.add(e);
        data.put(SCOUTS, list);
        player.setData(ModAttachments.EXPEDITIONS, data);
        pack.consume(1, player);
        level.playSound(null, mob.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.harmontide.scout_set_off", mob.getDisplayName(), biomeName(e.getString("Biome"))), true);
        mob.discard();
        return true;
    }

    /** Her clade (a party of different kinds scouts better), or her base class for anything that has none. */
    private static String family(Mob mob) {
        Clade clade = Clade.of(mob);
        return clade != null ? clade.name() : mob.getClass().getSuperclass().getSimpleName();
    }

    private static Component biomeName(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        return Component.translatable("biome." + key.getNamespace() + "." + key.getPath());
    }

    public static int away(Player player) {
        return player.getData(ModAttachments.EXPEDITIONS).getList(SCOUTS, Tag.TAG_COMPOUND).size();
    }

    /** Brings home every scout whose time is up (or all of them, with {@code now}). The ones who came back. */
    public static List<Mob> returnDue(ServerPlayer player, boolean now) {
        List<Mob> back = new ArrayList<>();
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(SCOUTS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return back;
        ServerLevel level = player.serverLevel();
        ListTag still = new ListTag();
        for (Tag t : list) {
            CompoundTag e = (CompoundTag) t;
            boolean due = (now || level.getGameTime() >= e.getLong("Back")) && level.dimension().location().toString().equals(e.getString("Dim"));
            Mob mob = due ? comeHome(level, player, e) : null;
            if (mob != null) back.add(mob);
            else still.add(e);
        }
        data.put(SCOUTS, still);
        player.setData(ModAttachments.EXPEDITIONS, data);
        return back;
    }

    private static Mob comeHome(ServerLevel level, Player player, CompoundTag e) {
        Entity entity = EntityType.loadEntityRecursive(e.getCompound("Girl"), level, x -> x);
        if (!(entity instanceof Mob mob)) return null;
        BlockPos at = Expeditions.arrival(level, player, mob);
        mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, mob.getRandom().nextFloat() * 360F, 0);
        mob.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        if (!level.addFreshEntity(mob)) return null;
        Missing.forgive(mob, player);
        ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(e.getString("Biome")));
        ItemStack pack = new ItemStack(ModItems.SCOUT_PACK.get());
        List<ItemStack> all = new ArrayList<>(finds(biome, e.getInt("Rolls"), e.getInt("Affection"), mob.getRandom()));
        for (Tag t : e.getList("Party", Tag.TAG_COMPOUND)) {                // whoever went with her comes back with her
            CompoundTag m = (CompoundTag) t;
            Entity other = EntityType.loadEntityRecursive(m.getCompound("Girl"), level, x -> x);
            if (!(other instanceof Mob member)) continue;
            BlockPos near = Expeditions.arrival(level, player, member);
            member.moveTo(near.getX() + 0.5, near.getY(), near.getZ() + 0.5, member.getRandom().nextFloat() * 360F, 0);
            member.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            if (level.addFreshEntity(member)) Missing.forgive(member, player);
            all.addAll(finds(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(m.getString("Biome"))), m.getInt("Rolls"), m.getInt("Affection"), member.getRandom()));
        }
        pack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(all.size() > CAPACITY ? all.subList(0, CAPACITY) : all));
        mob.spawnAtLocation(pack);
        level.playSound(null, at, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6F, 1.4F);
        player.displayClientMessage(Component.translatable("message.harmontide.scout_back", mob.getDisplayName(), biomeName(e.getString("Biome"))), false);
        return mob;
    }

    public static boolean hasFinds(ItemStack pack) {
        ItemContainerContents contents = pack.get(DataComponents.CONTAINER);
        return contents != null && contents.nonEmptyStream().findAny().isPresent();
    }
}
