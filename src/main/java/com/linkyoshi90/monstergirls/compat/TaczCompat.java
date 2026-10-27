package com.linkyoshi90.monstergirls.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Optional TaCZ (Timeless and Classics Zero, the unofficial NeoForge 1.21.1 port) support: the girls who fight for
 * their friend (GirlArms) can carry its guns. Without TaCZ every check here says no and nothing loads a TaCZ class;
 * with it, the calls go through {@link Tacz}, the only class that links against the mod.
 *
 * <p>TaCZ lets any living entity shoot (its LivingEntity mixin), and a mob reloads from its item-handler capability:
 * the girls' ammo pouch (AbstractMonsterGirl#ammoPouch, registered in MonsterGirlsMod).
 */
public final class TaczCompat {
    public static final String MODID = "tacz";
    private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded(MODID);

    private TaczCompat() {
    }

    public static boolean loaded() {
        return LOADED;
    }

    public static boolean isGun(ItemStack stack) {
        return LOADED && !stack.isEmpty() && Tacz.isGun(stack);
    }

    /** Whether the item is ammunition (or an ammo box) for the gun she holds. */
    public static boolean isAmmoFor(ItemStack gun, ItemStack stack) {
        return LOADED && !stack.isEmpty() && Tacz.isGun(gun) && Tacz.isAmmoFor(gun, stack);
    }

    /** She takes her gun in hand (TaCZ wants a draw before the first shot). */
    public static void draw(LivingEntity shooter) {
        if (LOADED && Tacz.isGun(shooter.getMainHandItem())) Tacz.draw(shooter);
    }

    /** One trigger pull at the target, or a reload when the magazine is empty. */
    public static void fireAt(LivingEntity shooter, LivingEntity target) {
        if (LOADED) Tacz.fireAt(shooter, target);
    }

    /** Everything that names a TaCZ class: loaded only once TaCZ is known to be there. */
    private static final class Tacz {
        static boolean isGun(ItemStack stack) {
            return com.tacz.guns.api.item.IGun.getIGunOrNull(stack) != null;
        }

        static boolean isAmmoFor(ItemStack gun, ItemStack stack) {
            var ammo = com.tacz.guns.api.item.IAmmo.getIAmmoOrNull(stack);
            if (ammo != null) return ammo.isAmmoOfGun(gun, stack);
            return stack.getItem() instanceof com.tacz.guns.api.item.IAmmoBox box && box.isAmmoBoxOfGun(gun, stack);
        }

        static void draw(LivingEntity shooter) {
            var op = com.tacz.guns.api.entity.IGunOperator.fromLivingEntity(shooter);
            op.initialData();
            op.draw(shooter::getMainHandItem);
        }

        static void fireAt(LivingEntity shooter, LivingEntity target) {
            ItemStack gunStack = shooter.getMainHandItem();
            var gun = com.tacz.guns.api.item.IGun.getIGunOrNull(gunStack);
            if (gun == null) return;
            var op = com.tacz.guns.api.entity.IGunOperator.fromLivingEntity(shooter);
            if (op.getSynReloadState().getStateType().isReloading()) return;
            double dx = target.getX() - shooter.getX();
            double dy = target.getEyeY() - 0.2 - shooter.getEyeY();
            double dz = target.getZ() - shooter.getZ();
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            var result = op.shoot(() -> pitch, () -> yaw);
            switch (result) {
                case NO_AMMO -> op.reload();
                case NOT_DRAW -> op.draw(shooter::getMainHandItem);
                default -> {
                }
            }
        }
    }
}
