package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Voices;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.SpeciesVoiceTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Every species' own voice (entity/Voices): the generated table matches what each species picks, and her sounds play as her events. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class VoiceTests {
    /** What every girl and harpy species picks for each slot now (vanilla sounds only), as "species|slot|sound". */
    static TreeSet<String> current(ServerLevel level) {
        TreeSet<String> out = new TreeSet<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!key.getNamespace().equals(HarmontideMod.MODID)) continue;
            Entity e;
            try {
                e = type.create(level);
            } catch (RuntimeException ex) {
                continue;
            }
            if (!(e instanceof Voices.Voiced voiced)) continue;
            for (String slot : Voices.SLOTS) {
                SoundEvent s;
                try {
                    s = voiced.slotSound(slot);
                } catch (RuntimeException ex) {
                    continue;
                }
                if (s != null && s.getLocation().getNamespace().equals("minecraft")) out.add(key.getPath() + "|" + slot + "|" + s.getLocation());
            }
            e.discard();
        }
        return out;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "everySpeciesHasHerVoice")
    public static void everySpeciesHasHerVoice(GameTestHelper helper) {
        TreeSet<String> now = current(helper.getLevel());
        TreeSet<String> table = new TreeSet<>();
        for (String[] row : SpeciesVoiceTable.ROWS) table.add(row[0] + "|" + row[1] + "|" + row[2]);
        if (!now.equals(table)) {
            Path dump = Path.of("species_voices_dump.txt").toAbsolutePath();
            try {
                Files.write(dump, new ArrayList<>(now), StandardCharsets.UTF_8);
            } catch (java.io.IOException ex) {
                throw new RuntimeException(ex);
            }
            List<String> added = new ArrayList<>(now);
            added.removeAll(table);
            List<String> gone = new ArrayList<>(table);
            gone.removeAll(now);
            helper.fail("the voice table is out of date (" + added.size() + " new, " + gone.size() + " gone, e.g. " + added.stream().limit(3).toList()
                    + gone.stream().limit(3).toList() + "): run tools/gen_species_voices.py " + dump);
            return;
        }
        check(now.size() > 4 * 300, "most species have their own voice: " + now.size());
        AbstractMonsterGirl serval = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        SoundEvent ambient = serval.slotSound("ambient");
        SoundEvent own = Voices.voice(serval, ambient);
        check(own != ambient && own.getLocation().equals(HarmontideMod.id(Voices.name("serval_nekomata", "ambient"))), "her ambient is her own event: " + own.getLocation());
        SoundEvent other = net.minecraft.sounds.SoundEvents.ANVIL_LAND;
        check(Voices.voice(serval, other) == other, "other sounds are left alone");
        helper.succeed();
    }
}
