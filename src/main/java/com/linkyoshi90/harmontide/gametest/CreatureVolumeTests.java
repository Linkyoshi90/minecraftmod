package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CreatureVolume;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The creature volume and chatter sliders (entity/CreatureVolume): which sounds they turn down or skip. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class CreatureVolumeTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40)
    public static void sliderFindsCreatureSounds(GameTestHelper helper) {
        check(CreatureVolume.factor(100) == 1.0F && CreatureVolume.factor(0) == 0.0F && CreatureVolume.factor(50) == 0.5F, "percent to factor");
        check(CreatureVolume.factor(250) == 1.0F && CreatureVolume.factor(-5) == 0.0F, "out of range is clamped");

        check(CreatureVolume.isCreatureEvent(HarmontideMod.id("entity.sparrow_harpy.ambient")), "her own events count");
        check(!CreatureVolume.isCreatureEvent(HarmontideMod.id("block.milk_tank.fill")), "the mod's block sounds do not");
        check(!CreatureVolume.isCreatureEvent(ResourceLocation.withDefaultNamespace("entity.parrot.ambient")), "vanilla events alone do not");

        check(CreatureVolume.isChatter(HarmontideMod.id("entity.sparrow_harpy.ambient")), "her ambient is chatter");
        check(CreatureVolume.isChatter(HarmontideMod.id("entity.bullfrog.bellow")), "a bellow is chatter");
        check(!CreatureVolume.isChatter(HarmontideMod.id("entity.sparrow_harpy.hurt")), "hurt always plays");
        check(!CreatureVolume.isChatter(ResourceLocation.withDefaultNamespace("entity.parrot.ambient")), "vanilla ambient is not hers");
        check(!CreatureVolume.chatterPlays(0.0F, 0.0F) && CreatureVolume.chatterPlays(1.0F, 0.999F), "0% never, 100% always");
        int played = 0;
        for (int i = 0; i < 100; i++) if (CreatureVolume.chatterPlays(0.25F, i / 100.0F)) played++;
        check(played == 25, "25% lets a quarter through: " + played);

        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(2, 2, 2));
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(6, 2, 6));
        check(CreatureVolume.isCreature(girl), "a girl is a creature");
        check(!CreatureVolume.isCreature(cow), "a cow is not");

        ResourceLocation parrot = ResourceLocation.withDefaultNamespace("entity.parrot.ambient");
        Vec3 at = girl.position();
        check(CreatureVolume.applies(helper.getLevel(), parrot, girl.getSoundSource(), at.x, at.y, at.z),
                "a borrowed vanilla sound played on her counts");
        check(!CreatureVolume.applies(helper.getLevel(), parrot, SoundSource.MUSIC, at.x, at.y, at.z), "music on her does not");
        Vec3 c = cow.position();
        check(!CreatureVolume.applies(helper.getLevel(), ResourceLocation.withDefaultNamespace("entity.cow.ambient"), SoundSource.NEUTRAL, c.x, c.y, c.z),
                "a cow's moo away from her does not");
        helper.succeed();
    }
}
