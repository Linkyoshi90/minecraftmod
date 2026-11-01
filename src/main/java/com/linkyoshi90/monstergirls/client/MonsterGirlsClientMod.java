package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** The settings page: Mods > MonsterGirls > Config, built by NeoForge from the config specs. */
@Mod(value = MonsterGirlsMod.MODID, dist = Dist.CLIENT)
public final class MonsterGirlsClientMod {
    public MonsterGirlsClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
