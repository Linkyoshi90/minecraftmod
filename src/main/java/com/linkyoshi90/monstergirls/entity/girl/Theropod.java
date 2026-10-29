package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The raptor family (theropod_field_guide.html): fernstalker, spinosaur, australovenator. Built like a 1920s museum
 * sketch of a T. rex but human: hunched forward over a long balancing tail, with a grin full of little sharp teeth.
 * Cousins of the lizardfolk, they bask, are cold-blooded and lay leathery clutches like them; they eat raw meat. The
 * fernstalker ambushes the monsters near her friend, the spinosaur lends her friend in water Dolphin's Grace, the
 * australovenator leaps on whatever her friend strikes.
 */
public class Theropod extends Lizardfolk {
    /** The australovenator's last pack hunt: the mob and her friend's hit it answered, so she leaps on each hit once. */
    private int huntedId = -1;
    private int huntedStamp = -1;

    public Theropod(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, stack -> stack.is(ItemTags.MEAT), false));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case SPINOSAUR_THEROPOD -> girlAttributes(34, 0.28, 7).add(Attributes.ARMOR, 3);
            case AUSTRALOVENATOR_THEROPOD -> girlAttributes(22, 0.36, 5).add(Attributes.ARMOR, 2);
            default -> girlAttributes(26, 0.32, 5).add(Attributes.ARMOR, 2);
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.MEAT) || super.isFood(stack);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.isBaby()) return;
        if (this.species == GirlSpecies.FERNSTALKER_THEROPOD && this.tickCount % 40 == 0) this.ambush(level);
        if (this.species == GirlSpecies.SPINOSAUR_THEROPOD && this.tickCount % 20 == 0) this.riverWader(level);
        if (this.species == GirlSpecies.AUSTRALOVENATOR_THEROPOD && this.tickCount % 5 == 0) this.packHunt(level);
    }

    /** The fernstalker pounces on the nearest monster within 8 blocks of her friend: four points of damage. 1 if she struck,
     *  0 if none was near, -1 without a friend. */
    public int ambush(ServerLevel level) {
        Player friend = this.bondedPlayer(Orders.AFFECTION, 24);
        if (friend == null) return -1;
        Monster m = level.getNearestEntity(Monster.class, TargetingConditions.forCombat(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(8));
        if (m == null) return 0;
        m.hurt(level.damageSources().mobAttack(this), 4.0F);
        this.getLookControl().setLookAt(m);
        return 1;
    }

    /** The spinosaur's friend within 10 blocks of her, in water, swims like a dolphin (Dolphin's Grace for three seconds).
     *  1 if they got it, 0 if they are not in water near her, -1 without a friend. */
    public int riverWader(ServerLevel level) {
        Player friend = this.bondedPlayer(Orders.AFFECTION, 16);
        if (friend == null) return -1;
        if (!friend.isInWater() || friend.distanceToSqr(this) > 10 * 10) return 0;
        friend.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, true), this);
        return 1;
    }

    /** When her friend has just struck a mob (within the last second) within 16 blocks of her, the australovenator leaps on it
     *  too: five points of damage, once per hit. Never on a player, a monster girl or someone's pet. 1 if she leapt, 0 if
     *  there was nothing to answer, -1 without a friend. */
    public int packHunt(ServerLevel level) {
        Player friend = this.bondedPlayer(Orders.AFFECTION, 24);
        if (friend == null) return -1;
        LivingEntity prey = friend.getLastHurtMob();
        int stamp = friend.getLastHurtMobTimestamp();
        if (prey == null || !prey.isAlive() || prey == this || friend.tickCount - stamp > 20 || prey.distanceToSqr(this) > 16 * 16) return 0;
        if (prey instanceof Player || prey instanceof AbstractMonsterGirl || prey instanceof OwnableEntity pet && pet.getOwnerUUID() != null) return 0;
        if (prey.getId() == this.huntedId && stamp == this.huntedStamp) return 0;
        this.huntedId = prey.getId();
        this.huntedStamp = stamp;
        prey.hurt(level.damageSources().mobAttack(this), 5.0F);
        this.getLookControl().setLookAt(prey);
        this.startTimedAction(Action.POUNCE, 10);
        return 1;
    }
}
