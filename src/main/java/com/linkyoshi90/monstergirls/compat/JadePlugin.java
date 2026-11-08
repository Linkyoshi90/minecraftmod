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
            if (accessor.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && com.linkyoshi90.monstergirls.entity.girl.Colds.hasCold(g)) data.putBoolean("Cold", true);
            if (accessor.getPlayer() != null && accessor.getEntity() instanceof net.minecraft.world.entity.Mob m && com.linkyoshi90.monstergirls.entity.Visits.streak(m, accessor.getPlayer()) > 1) {
                data.putInt("Visits", com.linkyoshi90.monstergirls.entity.Visits.streak(m, accessor.getPlayer()));
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && accessor.getPlayer() != null) {
                net.minecraft.nbt.CompoundTag f = com.linkyoshi90.monstergirls.entity.girl.Favours.of(g, accessor.getPlayer());
                if (f != null) {
                    data.putString("FavourItem", f.getString("Item"));
                    data.putInt("FavourCount", f.getInt("Count"));
                }
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && accessor.getPlayer() != null
                    && g.getAffection(accessor.getPlayer()) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION && !g.isBaby()) {   // what her friends know
                data.putInt("Birthday", com.linkyoshi90.monstergirls.entity.girl.Treats.birthday(g) + 1);
                data.putString("Hobby", com.linkyoshi90.monstergirls.entity.Hobbies.hobby(g).key());
                if (com.linkyoshi90.monstergirls.entity.girl.Treats.knows(accessor.getPlayer(), g)) {
                    data.putString("Favourite", com.linkyoshi90.monstergirls.entity.girl.Treats.favourite(g).getDescriptionId());
                }
            }
            if (accessor.getPlayer() != null && b.getAffection(accessor.getPlayer()) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {   // her orders, for her friends
                com.linkyoshi90.monstergirls.entity.Orders.Command order = com.linkyoshi90.monstergirls.entity.Orders.current(b);
                if (order != com.linkyoshi90.monstergirls.entity.Orders.Command.FREE_ROAM) data.putString("Order", order.name().toLowerCase(java.util.Locale.ROOT));
                if (b.getOrders().stance() != com.linkyoshi90.monstergirls.entity.Orders.Stance.NATURAL) data.putString("Stance", b.getOrders().stance().name().toLowerCase(java.util.Locale.ROOT));
            }
            if (accessor.getPlayer() != null && accessor.getEntity() instanceof net.minecraft.world.entity.Mob dm) {   // dates and sweethearts
                if (com.linkyoshi90.monstergirls.entity.Dates.canDate(dm, accessor.getPlayer())) {
                    data.putString("DatePlace", com.linkyoshi90.monstergirls.entity.Dates.favourite(dm).name().toLowerCase(java.util.Locale.ROOT));
                }
                if (com.linkyoshi90.monstergirls.entity.Sweethearts.sweetheart(dm) != null) data.putBoolean("SweetWed", com.linkyoshi90.monstergirls.entity.Sweethearts.wed(dm));
            }
            com.linkyoshi90.monstergirls.entity.Clade branch = com.linkyoshi90.monstergirls.entity.Clade.of(accessor.getEntity());
            if (branch != null && accessor.getPlayer() != null) {                // how much of her branch of the tree of life they have met
                data.putInt("BranchMet", com.linkyoshi90.monstergirls.entity.Journal.metOn(accessor.getPlayer(), branch));
                data.putInt("BranchAll", com.linkyoshi90.monstergirls.entity.Journal.members(branch).size());
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
            com.linkyoshi90.monstergirls.entity.Clade clade = com.linkyoshi90.monstergirls.entity.Clade.of(accessor.getEntity());   // her branch of the tree of life
            if (clade != null) {
                Component name = Component.translatable("clade.monstergirls." + clade.key());
                tooltip.add((data.contains("BranchAll") ? Component.translatable("jade.monstergirls.branch", name, data.getInt("BranchMet"), data.getInt("BranchAll")) : name.copy())
                        .withStyle(clade.mythic() ? net.minecraft.ChatFormatting.DARK_PURPLE : net.minecraft.ChatFormatting.DARK_AQUA));
            }
            if (data.contains("Birthday")) tooltip.add(Component.translatable("jade.monstergirls.birthday", data.getInt("Birthday")).withStyle(net.minecraft.ChatFormatting.GOLD));
            if (data.contains("Hobby")) tooltip.add(Component.translatable("jade.monstergirls.hobby", Component.translatable("hobby.monstergirls." + data.getString("Hobby"))).withStyle(net.minecraft.ChatFormatting.GREEN));
            if (data.contains("Favourite")) tooltip.add(Component.translatable("jade.monstergirls.favourite", Component.translatable(data.getString("Favourite"))).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (com.linkyoshi90.monstergirls.entity.girl.Mining.isMiner(accessor.getEntity()) && !((net.minecraft.world.entity.LivingEntity) accessor.getEntity()).isBaby()) {
                tooltip.add(Component.translatable("jade.monstergirls.miner").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && !g.isBaby()
                    && com.linkyoshi90.monstergirls.entity.girl.Foraging.forages(g)) {
                tooltip.add(Component.translatable("jade.monstergirls.forager").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            if (data.getInt("Visits") > 1) tooltip.add(Component.translatable("jade.monstergirls.visits", data.getInt("Visits")).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("DatePlace")) tooltip.add(Component.translatable("jade.monstergirls.date_place." + data.getString("DatePlace")).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("SweetWed")) tooltip.add(Component.translatable(data.getBoolean("SweetWed") ? "jade.monstergirls.sweetheart.wed" : "jade.monstergirls.sweetheart.courting").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.getBoolean("Cold")) tooltip.add(Component.translatable("jade.monstergirls.cold").withStyle(net.minecraft.ChatFormatting.AQUA));
            if (data.contains("FavourItem")) {
                net.minecraft.world.item.Item want = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(data.getString("FavourItem")));
                tooltip.add(Component.translatable("jade.monstergirls.favour", data.getInt("FavourCount"), want.getDescription()).withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
            if (data.contains("Order") || data.contains("Stance")) {
                net.minecraft.network.chat.MutableComponent line = Component.empty();
                if (data.contains("Order")) line.append(Component.translatable("jade.monstergirls.order." + data.getString("Order")));
                if (data.contains("Order") && data.contains("Stance")) line.append(", ");
                if (data.contains("Stance")) line.append(Component.translatable("jade.monstergirls.stance." + data.getString("Stance")));
                tooltip.add(line.withStyle(net.minecraft.ChatFormatting.BLUE));
            }
            if (data.contains("Role")) tooltip.add(Component.translatable(data.getString("Role")).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
            if (data.contains("Pregnant")) {
                tooltip.add(Component.translatable("jade.monstergirls.pregnant",
                        Component.translatable(data.getBoolean("Pregnant") ? "jade.monstergirls.yes" : "jade.monstergirls.no")));
            }
            if (data.contains("Nursing")) tooltip.add(Component.translatable(data.getInt("Nursing") == 2 ? "jade.monstergirls.keeps_milk" : "jade.monstergirls.nursing"));
            if (data.getBoolean("MilkFull")) tooltip.add(Component.translatable("jade.monstergirls.milk_full").withStyle(net.minecraft.ChatFormatting.RED));
            if ((data.getBoolean("MilkFull") || data.contains("Nursing")) && accessor.getEntity() instanceof LivingEntity milky) {   // which milk she gives
                com.linkyoshi90.monstergirls.item.FamilyMilk kind = com.linkyoshi90.monstergirls.item.FamilyMilk.of(milky);
                if (kind != null) tooltip.add(Component.translatable("jade.monstergirls.milk_kind",
                        Component.translatable("item.monstergirls." + kind.id())).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
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
