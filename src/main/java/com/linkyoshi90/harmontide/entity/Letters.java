package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Letters home (after Expeditions): halfway through her day away, a girl on an expedition writes to her friend. A signed
 * book turns up in their pack with a few lines about the country she is crossing (by her family), and one of its finds
 * tucked inside. Her friend's journal counts the letters; the first earns an advancement.
 */
public final class Letters {
    public static final String WROTE = "Wrote", COUNT = "Letters", ADVANCEMENT = "pen_pal";
    /** How many lines each country has (letter.harmontide.<country>.<n>). */
    public static final int LINES = 3;

    private Letters() {
    }

    /** Her country, for the letter: which lines she writes. */
    public static String country(GirlSpecies.Family family) {
        return switch (family) {
            case MERFOLK, MOLLUSK, CRUSTACEAN -> "sea";
            case UNDEAD, DEVIL, RIDER -> "dark";
            case FLORA, ELF, BUG -> "wood";
            case LIZARDFOLK, LAMIA, DRAGON, THEROPOD -> "desert";
            case NEKOMATA, KITSUNE -> "east";
            case SLIME -> "swamp";
            case CONSTRUCT -> "ruins";
            case BAT, ARACHNE -> "caves";
            case CANID, MINOTAUR, CENTAUR, JACKALOPE, RODENT, MARSUPIAL, ARMOURED -> "plains";
        };
    }

    /** Every second or so: each girl away who is halfway and hasn't written yet writes. How many letters came. */
    public static int writeDue(ServerPlayer player) {
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList("Away", Tag.TAG_COMPOUND);
        ServerLevel level = player.serverLevel();
        int n = 0;
        for (Tag t : list) {
            CompoundTag e = (CompoundTag) t;
            long length = e.contains("Length") ? e.getLong("Length") : Expeditions.DURATION;
            if (e.getBoolean(WROTE) || level.getGameTime() < e.getLong("Back") - length / 2) continue;
            e.putBoolean(WROTE, true);
            if (write(level, player, e.getCompound("Girl"))) n++;
        }
        if (n > 0) {
            data.put("Away", list);
            player.setData(ModAttachments.EXPEDITIONS, data);
        }
        return n;
    }

    /** Her letter, from her saved self. Whether one came. */
    static boolean write(ServerLevel level, ServerPlayer player, CompoundTag saved) {
        Entity entity = EntityType.create(saved, level).orElse(null);
        if (!(entity instanceof AbstractMonsterGirl girl)) return false;
        ItemStack letter = letter(girl, player);
        Item[] land = Expeditions.land(girl.getSpecies().family());
        ItemStack find = new ItemStack(land[girl.getRandom().nextInt(land.length)]);
        for (ItemStack s : List.of(letter, find)) if (!player.addItem(s)) player.drop(s, false);
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("message.harmontide.letter", girl.getDisplayName()), false);
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.putInt(COUNT, journal.getInt(COUNT) + 1);
        player.setData(ModAttachments.JOURNAL, journal);
        AdvancementHolder a = player.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
        if (a != null) player.getAdvancements().award(a, "done");
        return true;
    }

    /** The signed book: a greeting and two lines of her country, then over the page what she tucked in, and her name. */
    public static ItemStack letter(AbstractMonsterGirl girl, @Nullable ServerPlayer player) {
        String c = country(girl.getSpecies().family());
        int a = girl.getRandom().nextInt(LINES), b = (a + 1 + girl.getRandom().nextInt(LINES - 1)) % LINES;
        String nl = "\n\n";
        MutableComponent page = Component.translatable("letter.harmontide.dear", player == null ? "" : player.getName().getString())
                .append(nl).append(Component.translatable("letter.harmontide." + c + "." + a))
                .append(" ").append(Component.translatable("letter.harmontide." + c + "." + b));
        MutableComponent end = Component.translatable("letter.harmontide.enclosed")       // over the page: one page held up to 18 lines of a book's 14
                .append(nl).append(Component.translatable("letter.harmontide.signed", girl.getDisplayName()));
        String name = girl.getDisplayName().getString();
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Letter"),
                name.length() > 32 ? name.substring(0, 32) : name, 0, List.of(Filterable.passThrough(page), Filterable.passThrough(end)), true));
        book.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.letter", girl.getDisplayName()));
        return book;
    }
}
