package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Milk after a live birth. A mother with milk (FamilyMilk, grown and full size) nurses her newborns: she keeps her milk until they are
 * weaned, and a friend weans a baby by hand-feeding her her first solid food. Left unweaned, a daughter grows up and her mother keeps
 * her milk for good: the player's choice. Every few minutes she sits with her baby held to her chest (NurseGoal, the cradle pose);
 * the baby grows a little faster for it, and it uses her milk as a milking would. A mother with milk also fosters orphans of any kind
 * (isOrphan): Romulus and Remus, the other way round.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Nursing {
    public static final String CHILDREN = "NursingChildren", MOTHER = "MotherId", WEANED = "Weaned";
    /** Ticks a feed takes, and the growing it is worth to the baby. */
    public static final int FEED_TICKS = 200, GROWTH = 1800;
    /** How far an orphan's own kind would be; who fostered her. */
    public static final int ORPHAN = 16;
    public static final String FOSTER = "FosterMother";

    private Nursing() {}

    /** A newborn: her mother nurses her, if she has milk to nurse with. */
    public static void born(AbstractMonsterGirl mother, AbstractMonsterGirl young) {
        if (FamilyMilk.of(mother) == null || mother.getBbHeight() < MilkingMachineBlockEntity.MIN_HEIGHT) return;
        young.getPersistentData().putUUID(MOTHER, mother.getUUID());
        ListTag list = mother.getPersistentData().getList(CHILDREN, Tag.TAG_INT_ARRAY);
        list.add(NbtUtils.createUUID(young.getUUID()));
        mother.getPersistentData().put(CHILDREN, list);
    }

    /** Whether she has a baby she has not weaned (grown or not). */
    public static boolean isNursing(LivingEntity mother) {
        return !mother.getPersistentData().getList(CHILDREN, Tag.TAG_INT_ARRAY).isEmpty();
    }

    public static List<UUID> children(LivingEntity mother) {
        List<UUID> out = new ArrayList<>();
        for (Tag t : mother.getPersistentData().getList(CHILDREN, Tag.TAG_INT_ARRAY)) out.add(NbtUtils.loadUUID(t));
        return out;
    }

    /** Her unweaned children here and loaded. */
    public static List<AbstractMonsterGirl> loadedChildren(ServerLevel level, LivingEntity mother) {
        List<AbstractMonsterGirl> out = new ArrayList<>();
        for (UUID id : children(mother)) if (level.getEntity(id) instanceof AbstractMonsterGirl c && c.isAlive()) out.add(c);
        return out;
    }

    /** Every unweaned child of hers grown up: she keeps her milk for good. */
    public static boolean keepsMilk(ServerLevel level, LivingEntity mother) {
        if (!isNursing(mother)) return false;
        for (UUID id : children(mother)) if (!(level.getEntity(id) instanceof LivingEntity c) || c.isBaby()) return false;
        return true;
    }

    @Nullable
    public static UUID motherOf(Entity young) {
        return young.getPersistentData().hasUUID(MOTHER) ? young.getPersistentData().getUUID(MOTHER) : null;
    }

    /** She is no longer nursing this child (weaned, or gone). */
    static void release(LivingEntity mother, UUID child) {
        ListTag list = mother.getPersistentData().getList(CHILDREN, Tag.TAG_INT_ARRAY);
        list.removeIf(t -> NbtUtils.loadUUID(t).equals(child));
        if (list.isEmpty()) mother.getPersistentData().remove(CHILDREN);
        else mother.getPersistentData().put(CHILDREN, list);
    }

    /** A friend hand-feeds a nursing baby her first solid food: she is weaned. Whether she was. */
    public static boolean wean(ServerLevel level, AbstractMonsterGirl young, @Nullable Player friend) {
        UUID mother = motherOf(young);
        if (!young.isBaby() || mother == null || young.getPersistentData().getBoolean(WEANED)) return false;
        young.getPersistentData().putBoolean(WEANED, true);
        Entity mom = level.getEntity(mother);
        if (mom instanceof LivingEntity m) release(m, young.getUUID());   // an unloaded mother lets go when she next looks (tick)
        if (friend != null) {
            friend.displayClientMessage(Component.translatable("message.monstergirls.weaned", young.getDisplayName(),
                    mom != null ? mom.getDisplayName() : Component.translatable("message.monstergirls.her_mother")), true);
        }
        return true;
    }

    /** Now and then: lets go of children who were weaned while she was away, or who have died. */
    public static void tick(ServerLevel level, LivingEntity mother) {
        if (mother.tickCount % 100 != 0) return;
        if (mother.isVehicle() && !(mother instanceof com.linkyoshi90.monstergirls.entity.girl.Marsupial) && mother instanceof AbstractMonsterGirl m) {   // after a reload, out of her arms
            for (Entity rider : List.copyOf(mother.getPassengers())) {
                if (!m.nursingRider(rider) && rider instanceof LivingEntity l && l.isBaby()) rider.stopRiding();
            }
        }
        if (!isNursing(mother)) return;
        for (UUID id : children(mother)) {
            Entity c = level.getEntity(id);
            if (c != null && (c.getPersistentData().getBoolean(WEANED) || !c.isAlive())) release(mother, id);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        UUID mother = motherOf(event.getEntity());
        if (mother != null && level.getEntity(mother) instanceof LivingEntity m) release(m, event.getEntity().getUUID());
    }

    /**
     * A baby of any kind that has nobody else to nurse her: no grown one of her kind within ORPHAN, and (for ours) her own mother not
     * here. Never a villager's child, nor a monster's.
     */
    public static boolean isOrphan(ServerLevel level, net.minecraft.world.entity.Mob baby) {
        if (!baby.isBaby() || !baby.isAlive() || baby instanceof net.minecraft.world.entity.npc.AbstractVillager || baby instanceof net.minecraft.world.entity.monster.Enemy) return false;
        if (!(baby instanceof net.minecraft.world.entity.animal.Animal) && !(baby instanceof Bonded)) return false;
        UUID mother = motherOf(baby);
        if (mother != null && level.getEntity(mother) instanceof LivingEntity m && m.isAlive()) return false;
        return level.getEntitiesOfClass(LivingEntity.class, baby.getBoundingBox().inflate(ORPHAN), e -> e.getType() == baby.getType() && !e.isBaby() && e.isAlive()).isEmpty();
    }

    /** One feed done: the baby grows, and the milk is used. A foster child remembers who nursed her. */
    public static void fed(ServerLevel level, LivingEntity mother, LivingEntity baby) {
        if (baby instanceof Bonded b) b.setGrowTicks(b.getGrowTicks() + GROWTH);
        else if (baby instanceof net.minecraft.world.entity.AgeableMob a) a.ageUp(GROWTH / 20);
        if (!mother.getUUID().equals(motherOf(baby))) baby.getPersistentData().putUUID(FOSTER, mother.getUUID());
        Lactation.relieve(level, mother, null);
        MilkingMachineBlockEntity.markMilked(mother, level.getGameTime() + com.linkyoshi90.monstergirls.entity.girl.Minotaur.MILK_COOLDOWN);
    }

    /** What the info overlay says: 0 none, 1 nursing a baby, 2 keeps her milk (her children grew up unweaned). */
    public static int status(ServerLevel level, LivingEntity mother) {
        return !isNursing(mother) ? 0 : keepsMilk(level, mother) ? 2 : 1;
    }
}
