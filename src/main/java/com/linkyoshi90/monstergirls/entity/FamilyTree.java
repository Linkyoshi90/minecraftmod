package com.linkyoshi90.monstergirls.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The journal's family tree (2026-11-10): the player at the root, each wife (the departed in grey) with the little ones she bore
 * under her, their own little ones under them, and so on down the generations; little ones of a mother who isn't a wife under
 * "with her name", and those whose mother isn't known (a harpy's chick, a frog's young) straight under the player. Built from the
 * journal's own lists (Marriage.family and Marriage.childList), so it remembers those no longer loaded, or no longer living.
 * A child is linked to her mother by the mother's id (MotherId, since this day) or, for older entries, by her name.
 */
public final class FamilyTree {
    /** Lines on a page under its title (a page holds 14; the title takes two). */
    public static final int LINES = 11;
    /** How deep the tree is drawn (the root is 0; a great-great-grandchild is 5). */
    public static final int DEEPEST = 6;

    private FamilyTree() {
    }

    /** One line of the tree: how deep, who, and what she is to the player. */
    public record Line(int depth, String name, Kind kind) {
    }

    public enum Kind {ROOT, WIFE, DEPARTED, MOTHER, CHILD, GRANDCHILD}

    /** The tree as lines, top to bottom. */
    public static List<Line> build(String root, ListTag family, ListTag kids) {
        List<Line> out = new ArrayList<>();
        out.add(new Line(0, root, Kind.ROOT));
        Set<Integer> placed = new HashSet<>();
        for (int i = 0; i < family.size(); i++) {
            CompoundTag w = family.getCompound(i);
            out.add(new Line(1, w.getString("Name"), w.getBoolean("Gone") ? Kind.DEPARTED : Kind.WIFE));
            under(out, kids, placed, w.getString("Id"), w.getString("Name"), 2);
        }
        Map<String, List<Integer>> others = new LinkedHashMap<>();         // mothers who aren't wives (nor listed themselves)
        List<Integer> unknown = new ArrayList<>();
        for (int i = 0; i < kids.size(); i++) {
            if (placed.contains(i)) continue;
            CompoundTag k = kids.getCompound(i);
            if (!k.contains("Mother")) unknown.add(i);
            else if (!listed(kids, k)) others.computeIfAbsent(k.getString("Mother"), x -> new ArrayList<>()).add(i);
        }
        for (Map.Entry<String, List<Integer>> e : others.entrySet()) {
            out.add(new Line(1, e.getKey(), Kind.MOTHER));
            for (int i : e.getValue()) place(out, kids, placed, i, 2);
        }
        for (int i : unknown) if (!placed.contains(i)) place(out, kids, placed, i, 1);
        for (int i = 0; i < kids.size(); i++) if (!placed.contains(i)) place(out, kids, placed, i, 1);   // anyone left (a loop in old data)
        return out;
    }

    /** Whether her mother is herself one of the listed little ones (then she goes under her mother). */
    private static boolean listed(ListTag kids, CompoundTag k) {
        for (int j = 0; j < kids.size(); j++) {
            CompoundTag m = kids.getCompound(j);
            if (m != k && isMother(m.getString("Id"), m.getString("Name"), k)) return true;
        }
        return false;
    }

    private static boolean isMother(String id, String name, CompoundTag child) {
        if (child.contains("MotherId")) return child.getString("MotherId").equals(id);
        return child.contains("Mother") && child.getString("Mother").equals(name);
    }

    /** The little ones of this mother, each followed by her own. */
    private static void under(List<Line> out, ListTag kids, Set<Integer> placed, String id, String name, int depth) {
        for (int i = 0; i < kids.size(); i++) {
            if (!placed.contains(i) && isMother(id, name, kids.getCompound(i))) place(out, kids, placed, i, depth);
        }
    }

    private static void place(List<Line> out, ListTag kids, Set<Integer> placed, int i, int depth) {
        placed.add(i);
        CompoundTag k = kids.getCompound(i);
        out.add(new Line(Math.min(depth, DEEPEST), k.getString("Name"), k.getBoolean("Grand") ? Kind.GRANDCHILD : Kind.CHILD));
        under(out, kids, placed, k.getString("Id"), k.getString("Name"), depth + 1);
    }

    /** A line drawn for the book: indented by its depth, with a mark for what she is. */
    public static Component draw(Line l) {
        String indent = " ".repeat(Math.max(0, l.depth() - 1) * 2);
        return switch (l.kind()) {
            case ROOT -> Component.literal(l.name()).withStyle(ChatFormatting.RESET, ChatFormatting.BOLD, ChatFormatting.DARK_BLUE);
            case WIFE -> Component.literal(indent + "♥ " + l.name()).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_RED);
            case DEPARTED -> Component.literal(indent + "♥ " + l.name()).withStyle(ChatFormatting.RESET, ChatFormatting.GRAY);
            case MOTHER -> Component.literal(indent).append(Component.translatable("journal.monstergirls.tree.with", l.name()))
                    .withStyle(ChatFormatting.RESET, ChatFormatting.DARK_PURPLE);
            case CHILD -> Component.literal(indent + "- " + l.name()).withStyle(ChatFormatting.RESET, ChatFormatting.BLACK);
            case GRANDCHILD -> Component.literal(indent + "· " + l.name()).withStyle(ChatFormatting.RESET, ChatFormatting.DARK_GREEN);
        };
    }

    /** The tree's pages (none while there's nobody but the player). */
    public static List<Component> pages(String root, ListTag family, ListTag kids) {
        List<Component> pages = new ArrayList<>();
        if (family.isEmpty() && kids.isEmpty()) return pages;
        List<Line> lines = build(root, family, kids);
        String nl = String.valueOf((char) 10);
        for (int i = 0; i < lines.size(); i += LINES) {
            MutableComponent page = Component.translatable("journal.monstergirls.tree.title").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE).append(nl);
            for (int j = i; j < Math.min(i + LINES, lines.size()); j++) page.append(nl).append(draw(lines.get(j)));
            pages.add(page);
        }
        return pages;
    }
}
