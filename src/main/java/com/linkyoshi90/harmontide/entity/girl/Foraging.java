package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Foraging errands, the mining trip's (Mining) easier cousin. Crouch and show a friend who gathers (plants, rabbits, rodents,
 * foxes, bugs, marsupials, centaurs) something that grows wild (berries, mushrooms, tar-shrooms, apples, flowers, saplings,
 * honeycomb...) and she goes out for it, gone like a miner, coming back with FORAGED of it and the sample: a few hours for
 * berries, days for the rare seeds. She comes back to her patrol spot (or where she stood), into a chest beside it or kept for
 * her friend, like a miner's haul. (Fishing is a chore already: Chores.)
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Foraging {
    public static final int FORAGED = 16;
    public static final String LIST = "Foraging";

    public static final Set<GirlSpecies.Family> FORAGING_FAMILIES = EnumSet.of(GirlSpecies.Family.FLORA, GirlSpecies.Family.JACKALOPE,
            GirlSpecies.Family.RODENT, GirlSpecies.Family.KITSUNE, GirlSpecies.Family.BUG, GirlSpecies.Family.MARSUPIAL, GirlSpecies.Family.CENTAUR);

    /** Days for a full load of each thing that grows wild. */
    private static final Map<Item, Double> WILD = Map.ofEntries(
            Map.entry(Items.SWEET_BERRIES, 0.25), Map.entry(Items.GLOW_BERRIES, 0.5), Map.entry(Items.APPLE, 0.5),
            Map.entry(Items.RED_MUSHROOM, 0.25), Map.entry(Items.BROWN_MUSHROOM, 0.25), Map.entry(Items.COCOA_BEANS, 0.5),
            Map.entry(Items.HONEYCOMB, 1.0), Map.entry(Items.WHEAT_SEEDS, 0.1), Map.entry(Items.STICK, 0.1),
            Map.entry(Items.FERN, 0.1), Map.entry(Items.MOSS_BLOCK, 0.5), Map.entry(Items.VINE, 0.25), Map.entry(Items.BAMBOO, 0.25),
            Map.entry(Items.SUGAR_CANE, 0.25), Map.entry(Items.CACTUS, 0.5), Map.entry(Items.KELP, 0.25), Map.entry(Items.LILY_PAD, 0.5),
            Map.entry(Items.PUMPKIN, 0.75), Map.entry(Items.MELON_SLICE, 0.5), Map.entry(Items.CARROT, 0.75), Map.entry(Items.POTATO, 0.75),
            Map.entry(Items.FEATHER, 0.5), Map.entry(Items.EGG, 0.5), Map.entry(Items.SPORE_BLOSSOM, 2.0), Map.entry(Items.PITCHER_POD, 3.0),
            Map.entry(Items.TORCHFLOWER_SEEDS, 3.0));

    private Foraging() {
    }

    public static boolean forages(AbstractMonsterGirl girl) {
        return FORAGING_FAMILIES.contains(girl.getSpecies().family());
    }

    /** Days for a load of this, or a negative number if it doesn't grow wild. */
    public static double wild(ItemStack stack) {
        if (stack.is(ModItems.TAR_SHROOM.get())) return 0.5;
        if (stack.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS)) return 0.1;
        if (stack.is(net.minecraft.tags.ItemTags.SAPLINGS)) return 0.5;
        return WILD.getOrDefault(stack.getItem(), -1.0);
    }

    /** From mobInteract: a crouched sample for a forager. Null if it's not an errand. */
    @Nullable
    public static InteractionResult interact(AbstractMonsterGirl girl, Player player, ItemStack stack) {
        if (girl.isBaby() || girl.getAffection(player) < Orders.AFFECTION) return null;
        if (!player.isShiftKeyDown() || !forages(girl) || wild(stack) <= 0) return null;
        if (girl.level() instanceof ServerLevel level) forage(level, girl, player, stack);
        return InteractionResult.sidedSuccess(girl.level().isClientSide);
    }

    private static BlockPos spot(AbstractMonsterGirl girl) {
        return girl.getOrders().command(girl) == Orders.Command.PATROL && girl.getOrders().anchor() != null ? girl.getOrders().anchor() : girl.blockPosition();
    }

    /** Off gathering what she was shown: whether she went. */
    public static boolean forage(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack sample) {
        double days = wild(sample);
        if (days <= 0 || girl.isPassenger() || girl.isLeashed()) return false;
        BlockPos at = spot(girl);
        List<ItemStack> haul = new ArrayList<>();
        int count = Math.min(FORAGED, sample.getMaxStackSize());
        haul.add(sample.copyWithCount(count));
        haul.add(sample.copyWithCount(1));                                         // and the sample back
        long ticks = Math.max(1200, Math.round(days * Mining.DAY));
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + 1.0, girl.getZ(), 8, 0.4, 0.4, 0.4, 0.0);
        if (!leave(level, girl, player, at, ticks, haul)) return false;
        sample.consume(1, player);
        player.displayClientMessage(Component.translatable("message.harmontide.foraging.off", girl.getDisplayName(), haul.get(1).getHoverName(),
                Math.max(1, ticks / 1200)), false);
        return true;
    }

    private static boolean leave(ServerLevel level, AbstractMonsterGirl girl, Player player, BlockPos at, long ticks, List<ItemStack> haul) {
        CompoundTag saved = new CompoundTag();
        if (!girl.saveAsPassenger(saved)) return false;
        CompoundTag e = new CompoundTag();
        e.put("Girl", saved);
        e.putLong("Back", level.getGameTime() + ticks);
        e.putString("Dim", level.dimension().location().toString());
        e.putLong("At", at.asLong());
        ListTag items = new ListTag();
        for (ItemStack s : haul) if (!s.isEmpty()) items.add(s.save(level.registryAccess()));
        e.put("Haul", items);
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(LIST, Tag.TAG_COMPOUND);
        list.add(e);
        data.put(LIST, list);
        player.setData(ModAttachments.EXPEDITIONS, data);
        girl.discard();
        return true;
    }

    /** How many of this player's girls are out foraging. */
    public static int away(Player player) {
        return player.getData(ModAttachments.EXPEDITIONS).getList(LIST, Tag.TAG_COMPOUND).size();
    }

    /** Brings back everyone whose time is up (all of them, with now) whose spot is loaded. The girls who came back. */
    public static List<AbstractMonsterGirl> returnDue(ServerPlayer player, boolean now) {
        List<AbstractMonsterGirl> back = new ArrayList<>();
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList(LIST, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return back;
        ListTag still = new ListTag();
        for (Tag t : list) {
            CompoundTag e = (CompoundTag) t;
            ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(e.getString("Dim"))));
            BlockPos at = BlockPos.of(e.getLong("At"));
            boolean due = level != null && (now || level.getGameTime() >= e.getLong("Back"));
            AbstractMonsterGirl girl = due && level.isLoaded(at) ? comeBack(level, player, e, at) : null;
            if (girl != null) {
                back.add(girl);
                continue;
            }
            if (due && !e.getBoolean("Told")) {
                e.putBoolean("Told", true);
                Component name = EntityType.create(e.getCompound("Girl"), level).map(Entity::getDisplayName).orElse(Component.literal("?"));
                player.displayClientMessage(Component.translatable("message.harmontide.foraging.waiting", name, at.getX(), at.getY(), at.getZ()), false);
            }
            still.add(e);
        }
        data.put(LIST, still);
        player.setData(ModAttachments.EXPEDITIONS, data);
        return back;
    }

    @Nullable
    private static AbstractMonsterGirl comeBack(ServerLevel level, ServerPlayer player, CompoundTag e, BlockPos at) {
        Entity entity = EntityType.loadEntityRecursive(e.getCompound("Girl"), level, x -> x);
        if (!(entity instanceof AbstractMonsterGirl girl)) return null;
        BlockPos spot = level.noCollision(girl.getType().getSpawnAABB(at.getX() + 0.5, at.getY(), at.getZ() + 0.5)) ? at
                : level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, at);
        girl.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, girl.getRandom().nextFloat() * 360F, 0);
        girl.setDeltaMovement(Vec3.ZERO);
        if (!level.addFreshEntity(girl)) return null;
        com.linkyoshi90.harmontide.entity.Missing.forgive(girl, player);
        List<ItemStack> haul = new ArrayList<>();
        for (Tag t : e.getList("Haul", Tag.TAG_COMPOUND)) {
            ItemStack s = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) t);
            if (!s.isEmpty()) haul.add(s);
        }
        if (haul.isEmpty()) return girl;
        ItemStack load = haul.get(0).copy();
        haul = Mining.stow(level, at, haul);
        if (!haul.isEmpty()) Mining.keep(level, girl, player, haul);
        girl.hearts(level, 3);
        level.playSound(null, spot, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.harmontide.foraging.back", girl.getDisplayName(), load.getCount(), load.getHoverName())
                .withStyle(ChatFormatting.GOLD), false);
        return girl;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 100 == 37) returnDue(player, false);
    }
}
