package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.FamilyTree;
import com.linkyoshi90.harmontide.entity.FamilyTree.Kind;
import com.linkyoshi90.harmontide.entity.FamilyTree.Line;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** The journal's family tree (entity/FamilyTree): wives, their little ones, and theirs, generation by generation. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FamilyTreeTests {
    private static CompoundTag entry(String id, String name, String motherId, String mother, boolean grand) {
        CompoundTag t = new CompoundTag();
        t.putString("Id", id);
        t.putString("Name", name);
        if (mother != null) t.putString("Mother", mother);
        if (motherId != null) t.putString("MotherId", motherId);
        if (grand) t.putBoolean("Grand", true);
        return t;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theFamilyTree")
    public static void theFamilyTree(GameTestHelper helper) {
        ListTag family = new ListTag();
        family.add(entry("w1", "Mira", null, null, false));
        CompoundTag gone = entry("w2", "Rosa", null, null, false);
        gone.putBoolean("Gone", true);
        family.add(gone);
        ListTag kids = new ListTag();
        kids.add(entry("c1", "Lia", "w1", "Mira", false));
        kids.add(entry("g1", "Pip", "c1", "Lia", true));                     // Lia's little one
        kids.add(entry("c2", "Nia", null, "Rosa", false));                   // an old entry: her mother by name only
        kids.add(entry("c3", "Tam", "x9", "Kiko", false));                   // a friend's, not a wife's
        kids.add(entry("c4", "Wren", null, null, false));                    // a hatchling: no mother known
        kids.add(entry("g2", "Lia", "w1", "Mira", false));                   // a second Lia, Mira's too
        kids.add(entry("g3", "Bo", "g1", "Pip", true));                      // a great-grandchild
        List<Line> t = FamilyTree.build("Alex", family, kids);
        List<String> drawn = t.stream().map(l -> l.depth() + ":" + l.name()).toList();
        check(drawn.equals(List.of("0:Alex", "1:Mira", "2:Lia", "3:Pip", "4:Bo", "2:Lia", "1:Rosa", "2:Nia", "1:Kiko", "2:Tam", "1:Wren")),
                "wife by wife, generation by generation: " + drawn);
        check(t.get(6).kind() == Kind.DEPARTED && t.get(3).kind() == Kind.GRANDCHILD && t.get(8).kind() == Kind.MOTHER, "what each is");
        check(FamilyTree.pages("Alex", new ListTag(), new ListTag()).isEmpty(), "no tree with nobody in it");
        ListTag many = new ListTag();
        for (int i = 0; i < 30; i++) many.add(entry("k" + i, "K" + i, null, null, false));
        check(FamilyTree.pages("Alex", family, many).size() == (2 + 1 + 30 + FamilyTree.LINES - 1) / FamilyTree.LINES, "a big family takes more pages");
        helper.succeed();
    }
}
