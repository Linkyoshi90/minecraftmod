package com.linkyoshi90.harmontide.registry;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.menu.FridgeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HarmontideMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FridgeMenu>> FRIDGE = MENUS.register("fridge", () -> new MenuType<>(FridgeMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}
}
