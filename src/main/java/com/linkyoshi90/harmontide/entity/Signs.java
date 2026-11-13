package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.FootprintBlock;
import com.linkyoshi90.harmontide.block.FootprintBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Tracks and signs (2026-11-13). Wild girls leave signs of where they have been:
 * <ul>
 * <li><b>Footprints</b> (block/FootprintBlock) in sand, snow and mud, every couple of blocks as they go, pointing the way they went and
 * shaped by their kind ({@link #printOf}); they fade after {@link #FADE} ticks.</li>
 * <li><b>Feathers</b> dropped now and then by the bird kinds, and <b>bark stripped</b> from logs by the gnawers and the antlered kinds
 * (a stripped log where nobody took an axe).</li>
 * </ul>
 * Use an empty hand on footprints to read them ({@link #read}): whose they are, how fresh, which way she went. Each kind read goes into
 * your journal's field notes; advancements for the first and for {@link #MASTER} kinds. Config {@code tracks}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Signs {
    public static final int FADE = 24000, FRESH = 2000, RECENT = 12000, SIGN_EVERY = 2400, MASTER = 10, GAP = 2;
    private static final String NOTES = "FieldNotes";
    private static final Map<Block, Block> STRIPPED = Map.of(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG,
            Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG);

    private Signs() {}

    /** Ground that takes a print: sand, snow, mud. */
    public static boolean takesPrints(BlockState s) {
        return s.is(Blocks.SAND) || s.is(Blocks.RED_SAND) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.MUD) || s.is(Blocks.SOUL_SAND) || s.is(Blocks.SOUL_SOIL)
                || s.is(Blocks.POWDER_SNOW) || s.is(Blocks.CLAY);
    }

    /** The shape of her print, by her body. */
    public static FootprintBlock.Kind printOf(AbstractMonsterGirl g) {
        return switch (g.getSpecies().family()) {
            case CENTAUR, MINOTAUR, RIDER -> FootprintBlock.Kind.HOOF;
            case NEKOMATA, KITSUNE, CANID, JACKALOPE, RODENT, MARSUPIAL -> FootprintBlock.Kind.PAW;
            case LAMIA, MERFOLK, SLIME, MOLLUSK -> FootprintBlock.Kind.SLITHER;
            case ARACHNE, BUG, CRUSTACEAN, LIZARDFOLK, DRAGON, THEROPOD, ARMOURED -> FootprintBlock.Kind.CLAW;
            default -> FootprintBlock.Kind.FOOT;
        };
    }

    /** She leaves a print where she stands, if the ground takes one and there's none of hers just behind. Where, or null. */
    @Nullable
    public static BlockPos print(ServerLevel level, AbstractMonsterGirl g) {
        BlockPos at = g.blockPosition();
        if (!level.getBlockState(at).isAir() || !takesPrints(level.getBlockState(at.below()))) return null;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-GAP, -1, -GAP), at.offset(GAP, 1, GAP))) {
            if (level.getBlockState(p).is(ModBlocks.FOOTPRINT.get())) return null;
        }
        BlockState s = ModBlocks.FOOTPRINT.get().defaultBlockState().setValue(FootprintBlock.FACING, Direction.fromYRot(g.getYRot()))
                .setValue(FootprintBlock.KIND, printOf(g));
        level.setBlock(at, s, Block.UPDATE_ALL);
        if (level.getBlockEntity(at) instanceof FootprintBlockEntity f) f.set(g.getSpecies().id(), level.getGameTime());
        return at;
    }

    /** Now and then: a bird kind drops a feather, a gnawer or an antlered kind strips the bark from a log beside her. What she left, or null. */
    @Nullable
    public static String sign(ServerLevel level, AbstractMonsterGirl g) {
        Clade c = Clade.of(g);
        if (c == Clade.AVES) {
            level.addFreshEntity(new ItemEntity(level, g.getX(), g.getY() + 0.5, g.getZ(), new ItemStack(Items.FEATHER)));
            return "feather";
        }
        if (c == Clade.RODENTIA || c == Clade.CERVIDAE || g.getSpecies().family() == GirlSpecies.Family.RODENT) {
            for (BlockPos p : BlockPos.betweenClosed(g.blockPosition().offset(-2, 0, -2), g.blockPosition().offset(2, 2, 2))) {
                BlockState s = level.getBlockState(p);
                Block stripped = STRIPPED.get(s.getBlock());
                if (stripped != null) {
                    level.setBlockAndUpdate(p, stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, s.getValue(RotatedPillarBlock.AXIS)));
                    return "bark";
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl g) || !(g.level() instanceof ServerLevel level) || g.isBaby() || g.hasAnyAffection()
                || !HarmontideConfig.tracks()) return;
        if (g.tickCount % 10 == 5 && g.onGround() && g.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) print(level, g);
        if (g.tickCount % SIGN_EVERY == 1201 && g.getRandom().nextInt(3) == 0) sign(level, g);
    }

    // --- reading tracks ------------------------------------------------------------------------------------------------

    /** The player reads the footprints: whose, how fresh, which way. Recorded in their field notes. True if there was something to read. */
    public static boolean read(ServerLevel level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof FootprintBlockEntity f) || f.species().isEmpty()) return false;
        long age = level.getGameTime() - f.made();
        String fresh = age < FRESH ? "fresh" : age < RECENT ? "recent" : "old";
        Direction d = level.getBlockState(pos).getValue(FootprintBlock.FACING);
        player.displayClientMessage(Component.translatable("message.harmontide.tracks", Component.translatable("entity.harmontide." + f.species()),
                Component.translatable("message.harmontide.tracks." + fresh), Component.translatable("message.harmontide.tracks.dir." + d.getName())), true);
        note(player, f.species());
        return true;
    }

    /** A kind goes into the player's field notes (once). How many kinds they have now. */
    public static int note(Player player, String species) {
        CompoundTag j = player.getData(ModAttachments.JOURNAL);
        ListTag notes = j.getList(NOTES, Tag.TAG_STRING);
        for (Tag t : notes) if (t.getAsString().equals(species)) return notes.size();
        notes.add(StringTag.valueOf(species));
        j.put(NOTES, notes);
        player.setData(ModAttachments.JOURNAL, j);
        player.displayClientMessage(Component.translatable("message.harmontide.tracks.noted", Component.translatable("entity.harmontide." + species), notes.size()), false);
        Dairy.award(player, "tracker");
        if (notes.size() >= MASTER) Dairy.award(player, "master_tracker");
        return notes.size();
    }

    public static int noted(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(NOTES, Tag.TAG_STRING).size();
    }
}
