package com.linkyoshi90.monstergirls.compat;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

/**
 * Jade (optional): under a girl's name, "Pregnant: yes/no" and "Friendship: 12/25", the looking player's affection
 * out of the next milestone (BondInfo); a nursing mother says so (or that she keeps her milk), and one uncomfortably full
 * says she needs a milking machine. Both live on the server only, so the server sends them with Jade's data
 * packet. Jade finds this class by its annotation; nothing else refers to it, so the mod runs without Jade.
 */
@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    public static final ResourceLocation BOND = MonsterGirlsMod.id("bond");
    private static final List<Class<? extends LivingEntity>> GIRLS = List.of(AbstractMonsterGirl.class, AbstractHarpy.class, AbstractAnuran.class);

    @Override
    public void register(IWailaCommonRegistration registration) {
        for (Class<? extends LivingEntity> c : GIRLS) registration.registerEntityDataProvider(Provider.INSTANCE, c);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        for (Class<? extends LivingEntity> c : GIRLS) registration.registerEntityComponent(Provider.INSTANCE, c);
    }

    enum Provider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof Bonded b)) return;
            data.putInt("Affection", accessor.getPlayer() == null ? 0 : b.getAffection(accessor.getPlayer()));
            if (BondInfo.canCarry(accessor.getEntity())) data.putBoolean("Pregnant", b.getGestation().isCarrying());
            if (accessor.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && com.linkyoshi90.monstergirls.entity.girl.Roles.of(g) != null) {
                data.putString("Role", com.linkyoshi90.monstergirls.entity.girl.Roles.of(g).key());
            }
            if (accessor.getEntity() instanceof LivingEntity l && l.level() instanceof net.minecraft.server.level.ServerLevel level) {
                int nursing = com.linkyoshi90.monstergirls.entity.Nursing.status(level, l);
                if (nursing > 0) data.putInt("Nursing", nursing);
                if (com.linkyoshi90.monstergirls.entity.Lactation.isFull(l)) data.putBoolean("MilkFull", true);
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            com.linkyoshi90.monstergirls.entity.Rarity.Tier tier = com.linkyoshi90.monstergirls.entity.Rarity.of(accessor.getEntity());
            if (tier != com.linkyoshi90.monstergirls.entity.Rarity.Tier.COMMON) tooltip.add(Component.translatable(tier.key()).withStyle(tier.colour));
            if (data.contains("Role")) tooltip.add(Component.translatable(data.getString("Role")).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
            if (data.contains("Pregnant")) {
                tooltip.add(Component.translatable("jade.monstergirls.pregnant",
                        Component.translatable(data.getBoolean("Pregnant") ? "jade.monstergirls.yes" : "jade.monstergirls.no")));
            }
            if (data.contains("Nursing")) tooltip.add(Component.translatable(data.getInt("Nursing") == 2 ? "jade.monstergirls.keeps_milk" : "jade.monstergirls.nursing"));
            if (data.getBoolean("MilkFull")) tooltip.add(Component.translatable("jade.monstergirls.milk_full").withStyle(net.minecraft.ChatFormatting.RED));
            if (data.contains("Affection")) {
                int a = data.getInt("Affection");
                tooltip.add(Component.translatable("jade.monstergirls.friendship", a, BondInfo.nextMilestone(a)));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return BOND;
        }
    }
}
