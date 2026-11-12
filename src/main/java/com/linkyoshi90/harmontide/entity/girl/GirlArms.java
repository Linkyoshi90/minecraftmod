package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.compat.TaczCompat;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Weapons for the girls who fight for their friend: the ones who go after the monsters near a befriended player (or
 * that player's attackers) on their own, not only when they are hurt themselves. Her friend (friendship 10 or more)
 * hands her a bow, a crossbow or, with TaCZ installed, a gun (or throws it her way: she fetches it, {@link FetchGoal});
 * she shoots the monsters she would have fought anyway. A TaCZ gun's ammunition goes into her ammo pouch (right-click
 * her with it, or throw it to her), where TaCZ finds it when she reloads.
 * Sneak + right-click with an empty hand takes the weapon and the pouch back. Bows and crossbows never run out of
 * arrows, like a skeleton's.
 *
 * <p>The elves are not on the list: the archers carry their own bows already (Elf).
 */
public final class GirlArms {
    private GirlArms() {
    }

    public static final Set<GirlSpecies> FIGHTERS = EnumSet.of(
            GirlSpecies.TRAPDOOR_SPIDER_ARACHNE, GirlSpecies.WOLF_SPIDER_ARACHNE,
            GirlSpecies.MOSQUITO_BUG, GirlSpecies.HORNET_BUG,
            GirlSpecies.PANDA_CANID,
            GirlSpecies.GOAT_CENTAUR, GirlSpecies.ZEBRA_CENTAUR,
            GirlSpecies.ONI_DEVIL,
            GirlSpecies.WADJET_LAMIA,
            GirlSpecies.IKA_MOLLUSK,
            GirlSpecies.SQUIRRICERAT_RODENT, GirlSpecies.KAMAITACHI_RODENT,
            GirlSpecies.FRANKENSTEIN_UNDEAD, GirlSpecies.SKELETON_UNDEAD);

    /** Whether she fights the monsters near her friend on her own (so she can carry a weapon). */
    public static boolean isFighter(GirlSpecies s) {
        return FIGHTERS.contains(s) || s.family() == GirlSpecies.Family.ARMOURED || s.family() == GirlSpecies.Family.CONSTRUCT
                || s.family() == GirlSpecies.Family.CRUSTACEAN || s.family() == GirlSpecies.Family.MARSUPIAL;
    }

    public static boolean isRangedWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || TaczCompat.isGun(stack);
    }

    /** Manako's look of the cyclops, the one eye made for a scope: she carries weapons, and a sniper rifle best of all. */
    public static boolean isMarksman(AbstractMonsterGirl girl) {
        return girl.getSpecies() == GirlSpecies.CYCLOPS_DEVIL && "tactical_eye".equals(girl.getVariantName());
    }

    public static boolean isFighter(AbstractMonsterGirl girl) {
        return isFighter(girl.getSpecies()) || isMarksman(girl);
    }

    /** Whether she is the marksman with a sniper rifle in her hands. */
    public static boolean isSniping(AbstractMonsterGirl girl) {
        return isMarksman(girl) && TaczCompat.isSniper(girl.getMainHandItem());
    }

    /** How far away she will fire, with this weapon in hand. */
    public static double fireRange(boolean sniper) {
        return sniper ? 64.0 : 24.0;
    }

    /** How close she walks up before she stops and shoots. */
    public static double holdRange(boolean sniper) {
        return sniper ? 48.0 : 14.0;
    }

    /** How close a target gets before she backs away. */
    public static double backOffRange(boolean sniper) {
        return sniper ? 10.0 : 4.0;
    }

    /** Ticks with the target in sight before her first shot (a sniper settles her aim). */
    public static int aimTicks(boolean sniper) {
        return sniper ? 20 : 5;
    }

    public static boolean isArmed(AbstractMonsterGirl girl) {
        return isRangedWeapon(girl.getMainHandItem());
    }

    /**
     * Her friend offers her a weapon or ammunition, or (sneaking, empty hand) asks for them back. The result, or null
     * when this has nothing to do with weapons.
     */
    @Nullable
    public static InteractionResult interact(AbstractMonsterGirl girl, Player player, ItemStack stack) {
        if (!isFighter(girl) || girl.isBaby() || girl.getAffection(player) < Orders.AFFECTION) return null;
        boolean client = girl.level().isClientSide;
        if (isRangedWeapon(stack)) {
            if (!client) {
                giveBack(girl, player);
                arm(girl, stack.split(1), player);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (TaczCompat.isAmmoFor(girl.getMainHandItem(), stack)) {
            if (!client) stock(girl, stack);
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.isEmpty() && player.isShiftKeyDown() && (isArmed(girl) || !girl.ammoPouch().isEmpty())) {
            if (!client) {
                giveBack(girl, player);
                player.displayClientMessage(Component.translatable("message.harmontide.disarmed", girl.getDisplayName()), true);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    /** She takes the weapon in hand (one item). */
    static void arm(AbstractMonsterGirl girl, ItemStack weapon, @Nullable Player friend) {
        girl.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        girl.setDropChance(EquipmentSlot.MAINHAND, 2.0F);                   // hers to keep: it always drops if she dies
        TaczCompat.draw(girl);
        girl.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0F, 1.0F);
        if (friend != null) friend.displayClientMessage(Component.translatable("message.harmontide.armed", girl.getDisplayName()), true);
    }

    /** Ammunition into her pouch; what does not fit stays in the stack. */
    static void stock(AbstractMonsterGirl girl, ItemStack ammo) {
        ItemStack rest = girl.ammoPouch().addItem(ammo.copy());
        ammo.setCount(rest.getCount());
        girl.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
    }

    /** Whether she would fetch this item lying on the ground: a weapon or ammunition for her gun, thrown by a friend. */
    public static boolean wantsThrown(AbstractMonsterGirl girl, ItemEntity item) {
        if (!item.isAlive() || item.getItem().isEmpty() || girl.isBaby() || !isFighter(girl)) return false;
        if (!(item.getOwner() instanceof Player thrower) || girl.getAffection(thrower) < Orders.AFFECTION) return false;
        ItemStack s = item.getItem();
        return isRangedWeapon(s) || TaczCompat.isAmmoFor(girl.getMainHandItem(), s) && girl.ammoPouch().canAddItem(s);
    }

    /** She picks up a thrown weapon (her old one drops at her feet) or ammunition. Whether she took anything. */
    public static boolean pickUp(AbstractMonsterGirl girl, ItemEntity item) {
        if (!wantsThrown(girl, item)) return false;
        ItemStack s = item.getItem().copy();
        Player friend = item.getOwner() instanceof Player p ? p : null;
        if (isRangedWeapon(s)) {
            ItemStack old = girl.getMainHandItem();
            if (isRangedWeapon(old)) girl.spawnAtLocation(old.copy());
            arm(girl, s.split(1), friend);
        } else {
            stock(girl, s);
        }
        girl.take(item, 1);
        if (s.isEmpty()) item.discard();
        else item.setItem(s);
        return true;
    }

    /** How far she looks for a weapon or ammunition thrown her way. */
    public static final double FETCH_RANGE = 10.0;

    /** Thrown her way by her friend: she goes and gets it, even mid-fight (a gun or ammunition is what she needs then). */
    public static class FetchGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable
        private ItemEntity item;

        public FetchGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.girl.tickCount % 10 != 0 || this.girl.getOrders().command(this.girl) == Orders.Command.SIT) return false;
            List<ItemEntity> near = this.girl.level().getEntitiesOfClass(ItemEntity.class, this.girl.getBoundingBox().inflate(FETCH_RANGE),
                    i -> wantsThrown(this.girl, i));
            this.item = near.isEmpty() ? null : near.get(0);
            return this.item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.item != null && wantsThrown(this.girl, this.item) && this.item.distanceToSqr(this.girl) < FETCH_RANGE * FETCH_RANGE * 2;
        }

        @Override
        public void start() {
            if (this.item != null) this.girl.getNavigation().moveTo(this.item, 1.2);
        }

        @Override
        public void stop() {
            this.item = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.item == null) return;
            if (this.girl.distanceToSqr(this.item) < 2.25) {
                pickUp(this.girl, this.item);
                this.item = null;
            } else if (this.girl.tickCount % 10 == 0) {
                this.girl.getNavigation().moveTo(this.item, 1.2);
            }
        }
    }

    /** Her weapon and whatever is in her pouch go back to the player. */
    static void giveBack(AbstractMonsterGirl girl, Player player) {
        ItemStack held = girl.getMainHandItem();
        if (isRangedWeapon(held)) {
            girl.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!player.getInventory().add(held)) player.drop(held, false);
        }
        for (ItemStack s : girl.ammoPouch().removeAllItems()) {
            if (!player.getInventory().add(s)) player.drop(s, false);
        }
    }

    /**
     * Armed and with someone to shoot: she keeps her distance and looses arrows (or fires her gun). It outranks her
     * own melee goals while she holds a weapon, and steps aside when she doesn't.
     */
    public static class RangedGoal extends Goal {
        private final AbstractMonsterGirl girl;
        private int cooldown;
        private int seen;

        public RangedGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.girl.getTarget();
            return t != null && t.isAlive() && isArmed(this.girl);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void stop() {
            this.seen = 0;
            this.girl.getNavigation().stop();
            this.girl.setAim(AbstractMonsterGirl.AIM_NONE);
            TaczCompat.aim(this.girl, false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity t = this.girl.getTarget();
            if (t == null) return;
            double d2 = this.girl.distanceToSqr(t);
            boolean sees = this.girl.getSensing().hasLineOfSight(t);
            this.seen = sees ? this.seen + 1 : 0;
            this.girl.getLookControl().setLookAt(t, 30.0F, 30.0F);
            this.girl.setAim(aimOf(this.girl.getMainHandItem()));
            boolean sniper = isSniping(this.girl);
            double hold = holdRange(sniper), back = backOffRange(sniper), fire = fireRange(sniper);
            if (d2 > hold * hold || !sees) this.girl.getNavigation().moveTo(t, 1.0);
            else if (d2 < back * back) this.girl.getNavigation().moveTo(this.girl.getX() * 2 - t.getX(), this.girl.getY(), this.girl.getZ() * 2 - t.getZ(), 1.1);
            else this.girl.getNavigation().stop();
            if (this.cooldown > 0) this.cooldown--;
            if (sniper) TaczCompat.aim(this.girl, sees && d2 >= back * back);
            if (!sees || this.seen < aimTicks(sniper) || d2 > fire * fire || !(this.girl.level() instanceof ServerLevel)) return;
            ItemStack weapon = this.girl.getMainHandItem();
            if (TaczCompat.isGun(weapon)) {
                if (TaczCompat.fireAt(this.girl, t)) this.girl.announceShot();   // TaCZ keeps the gun's own fire rate and reloads
            } else if (this.cooldown <= 0) {
                shootArrow(this.girl, t, weapon);
                this.cooldown = weapon.getItem() instanceof CrossbowItem ? 40 : 30;
            }
        }
    }

    /** With a sniper rifle she watches far past what she would otherwise notice, while a friend is near. */
    public static class SpotterGoal extends Goal {
        private final AbstractMonsterGirl girl;

        public SpotterGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
        }

        @Override
        public boolean canUse() {
            return this.girl.tickCount % 20 == 0 && this.girl.getTarget() == null && isSniping(this.girl) && this.girl.bondedPlayer(Orders.AFFECTION, 32) != null;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            double range = fireRange(true);
            Monster nearest = null;
            for (Monster m : this.girl.level().getEntitiesOfClass(Monster.class, this.girl.getBoundingBox().inflate(range), m -> m.isAlive() && this.girl.distanceToSqr(m) < range * range
                    && this.girl.getSensing().hasLineOfSight(m))) {
                if (nearest == null || this.girl.distanceToSqr(m) < this.girl.distanceToSqr(nearest)) nearest = m;
            }
            if (nearest != null) this.girl.setTarget(nearest);
        }
    }

    /** The pose a weapon calls for (client.AimPose). */
    public static int aimOf(ItemStack weapon) {
        if (weapon.getItem() instanceof BowItem) return AbstractMonsterGirl.AIM_BOW;
        if (weapon.getItem() instanceof CrossbowItem) return AbstractMonsterGirl.AIM_CROSSBOW;
        return TaczCompat.isGun(weapon) ? AbstractMonsterGirl.AIM_GUN : AbstractMonsterGirl.AIM_NONE;
    }

    /** A skeleton's shot, from her bow or crossbow (the crossbow's bolt flies faster and straighter). */
    public static void shootArrow(AbstractMonsterGirl girl, LivingEntity target, ItemStack weapon) {
        boolean crossbow = weapon.getItem() instanceof CrossbowItem;
        AbstractArrow arrow = ProjectileUtil.getMobArrow(girl, new ItemStack(Items.ARROW), crossbow ? 1.6F : 1.0F, weapon);
        double dx = target.getX() - girl.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - girl.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + flat * 0.2, dz, crossbow ? 2.2F : 1.6F, crossbow ? 4.0F : 8.0F);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        girl.level().addFreshEntity(arrow);
        girl.announceShot();
        girl.playSound(crossbow ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (girl.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
