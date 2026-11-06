package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Lessons: the first friend of each branch of the tree of life (entity.Clade) to grow devoted to a player teaches them one small
 * thing her kind is good at, for good: a cat to land on their feet, a hare to jump, a whale to hold their breath. One lesson a branch,
 * each a small attribute modifier (all of them together come to a heart, a few points of armour and so on). Kept in the journal
 * (it survives death), with who taught it; part of the comfort setting.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Lessons {
    public static final String LESSONS = "Lessons", WELL_TAUGHT_ADVANCEMENT = "well_taught";

    public record Lesson(Holder<Attribute> attribute, double amount, Operation op) {
    }

    public static final Map<Clade, Lesson> TAUGHT = new EnumMap<>(Clade.class);

    static {
        TAUGHT.put(Clade.FELIFORMIA, new Lesson(Attributes.SAFE_FALL_DISTANCE, 2.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.CANIDAE, new Lesson(Attributes.MOVEMENT_SPEED, 0.04, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.ARCTOIDEA, new Lesson(Attributes.MAX_HEALTH, 2.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.RODENTIA, new Lesson(Attributes.BLOCK_BREAK_SPEED, 0.08, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.LAGOMORPHA, new Lesson(Attributes.JUMP_STRENGTH, 0.06, Operation.ADD_VALUE));
        TAUGHT.put(Clade.PRIMATES, new Lesson(Attributes.BLOCK_INTERACTION_RANGE, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.CHIROPTERA, new Lesson(Attributes.SNEAKING_SPEED, 0.15, Operation.ADD_VALUE));
        TAUGHT.put(Clade.MARSUPIALIA, new Lesson(Attributes.ATTACK_KNOCKBACK, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.XENARTHRA, new Lesson(Attributes.ARMOR, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.PAENUNGULATA, new Lesson(Attributes.KNOCKBACK_RESISTANCE, 0.1, Operation.ADD_VALUE));
        TAUGHT.put(Clade.PERISSODACTYLA, new Lesson(Attributes.MOVEMENT_EFFICIENCY, 0.25, Operation.ADD_VALUE));
        TAUGHT.put(Clade.BOVIDAE, new Lesson(Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.CERVIDAE, new Lesson(Attributes.SAFE_FALL_DISTANCE, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.CAMELIDAE, new Lesson(Attributes.BURNING_TIME, -0.25, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.SUINA, new Lesson(Attributes.LUCK, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.WHIPPOMORPHA, new Lesson(Attributes.OXYGEN_BONUS, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.AVES, new Lesson(Attributes.FALL_DAMAGE_MULTIPLIER, -0.15, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.ARCHOSAURIA, new Lesson(Attributes.ARMOR_TOUGHNESS, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.TESTUDINES, new Lesson(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.25, Operation.ADD_VALUE));
        TAUGHT.put(Clade.SQUAMATA, new Lesson(Attributes.ATTACK_SPEED, 0.05, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.AMPHIBIA, new Lesson(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.15, Operation.ADD_VALUE));
        TAUGHT.put(Clade.ACTINOPTERYGII, new Lesson(Attributes.SUBMERGED_MINING_SPEED, 0.25, Operation.ADD_VALUE));
        TAUGHT.put(Clade.CHELICERATA, new Lesson(Attributes.ENTITY_INTERACTION_RANGE, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.HEXAPODA, new Lesson(Attributes.MINING_EFFICIENCY, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.MALACOSTRACA, new Lesson(Attributes.ARMOR, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.MOLLUSCA, new Lesson(Attributes.OXYGEN_BONUS, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.FLORA, new Lesson(Attributes.KNOCKBACK_RESISTANCE, 0.1, Operation.ADD_VALUE));
        TAUGHT.put(Clade.FAE, new Lesson(Attributes.LUCK, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.DRAGON, new Lesson(Attributes.ATTACK_DAMAGE, 0.5, Operation.ADD_VALUE));
        TAUGHT.put(Clade.UNDEAD, new Lesson(Attributes.OXYGEN_BONUS, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.DEVIL, new Lesson(Attributes.BURNING_TIME, -0.25, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.SLIME, new Lesson(Attributes.FALL_DAMAGE_MULTIPLIER, -0.15, Operation.ADD_MULTIPLIED_BASE));
        TAUGHT.put(Clade.CONSTRUCT, new Lesson(Attributes.ARMOR, 1.0, Operation.ADD_VALUE));
        TAUGHT.put(Clade.RIDER, new Lesson(Attributes.ATTACK_SPEED, 0.05, Operation.ADD_MULTIPLIED_BASE));
    }

    private Lessons() {
    }

    public static ResourceLocation id(Clade clade) {
        return ResourceLocation.fromNamespaceAndPath(MonsterGirlsMod.MODID, "lesson_" + clade.key());
    }

    /** The lessons they have learned, in the order learned: each the clade's name and who taught it. */
    public static ListTag learned(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(LESSONS, Tag.TAG_COMPOUND);
    }

    public static boolean knows(Player player, Clade clade) {
        for (Tag t : learned(player)) if (((CompoundTag) t).getString("Clade").equals(clade.name())) return true;
        return false;
    }

    /** She is newly devoted to them: if her branch hasn't taught them yet, she does. The lesson, or null. */
    @Nullable
    public static Lesson learn(Player player, Entity teacher) {
        Clade clade = Clade.of(teacher);
        if (clade == null || !MonsterGirlsConfig.comfort() || !TAUGHT.containsKey(clade) || knows(player, clade)) return null;
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        ListTag list = data.getList(LESSONS, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putString("Clade", clade.name());
        entry.putString("Teacher", teacher.getName().getString());
        list.add(entry);
        data.put(LESSONS, list);
        player.setData(ModAttachments.JOURNAL, data);
        apply(player);
        if (player instanceof ServerPlayer sp && list.size() >= TAUGHT.size()) {         // every branch has taught them
            var a = sp.server.getAdvancements().get(MonsterGirlsMod.id(WELL_TAUGHT_ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
        if (player instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.lesson", teacher.getName(),
                Component.translatable("lesson.monstergirls." + clade.key())).withStyle(ChatFormatting.AQUA), false);
        return TAUGHT.get(clade);
    }

    /** Every lesson they know, on them (re-applied after death and on login: the modifiers are not saved). How many. */
    public static int apply(Player player) {
        int n = 0;
        for (Tag t : learned(player)) {
            Clade clade;
            try {
                clade = Clade.valueOf(((CompoundTag) t).getString("Clade"));
            } catch (IllegalArgumentException e) {
                continue;
            }
            Lesson lesson = TAUGHT.get(clade);
            if (lesson == null) continue;
            AttributeInstance attr = player.getAttribute(lesson.attribute());
            if (attr == null) continue;
            if (!attr.hasModifier(id(clade))) attr.addTransientModifier(new AttributeModifier(id(clade), lesson.amount(), lesson.op()));
            n++;
        }
        return n;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.tickCount % 20 == 5) apply(p);
    }
}
