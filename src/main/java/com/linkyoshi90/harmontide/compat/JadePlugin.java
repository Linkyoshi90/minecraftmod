package com.linkyoshi90.harmontide.compat;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
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
    public static final ResourceLocation BOND = HarmontideMod.id("bond");
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
            if (b.getGestation().isCarrying()) data.putInt("DueMinutes", Math.max(1, (b.getGestation().getTicksLeft() + 1199) / 1200));
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g && com.linkyoshi90.harmontide.entity.girl.Roles.of(g) != null) {
                data.putString("Role", com.linkyoshi90.harmontide.entity.girl.Roles.of(g).key());
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g && com.linkyoshi90.harmontide.entity.girl.Colds.hasCold(g)) data.putBoolean("Cold", true);
            if (accessor.getPlayer() != null && accessor.getEntity() instanceof net.minecraft.world.entity.Mob m && com.linkyoshi90.harmontide.entity.Visits.streak(m, accessor.getPlayer()) > 1) {
                data.putInt("Visits", com.linkyoshi90.harmontide.entity.Visits.streak(m, accessor.getPlayer()));
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g && accessor.getPlayer() != null) {
                net.minecraft.nbt.CompoundTag f = com.linkyoshi90.harmontide.entity.girl.Favours.of(g, accessor.getPlayer());
                if (f != null) {
                    data.putString("FavourItem", f.getString("Item"));
                    data.putInt("FavourCount", f.getInt("Count"));
                }
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g) {   // what anyone watching can tell: her place in the food web, her hours
                data.putString("FoodRole", com.linkyoshi90.harmontide.entity.FoodWeb.role(g).key());
                data.putString("Rhythm", g.rhythm().key());
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g && accessor.getPlayer() != null
                    && g.getAffection(accessor.getPlayer()) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION && !g.isBaby()) {   // what her friends know
                data.putInt("Birthday", com.linkyoshi90.harmontide.entity.girl.Treats.birthday(g) + 1);
                data.putString("Hobby", com.linkyoshi90.harmontide.entity.Hobbies.hobby(g).key());
                if (com.linkyoshi90.harmontide.entity.girl.Treats.knows(accessor.getPlayer(), g)) {
                    data.putString("Favourite", com.linkyoshi90.harmontide.entity.girl.Treats.favourite(g).getDescriptionId());
                }
            }
            if (accessor.getPlayer() != null && b.getAffection(accessor.getPlayer()) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) {   // her orders, for her friends
                com.linkyoshi90.harmontide.entity.Orders.Command order = com.linkyoshi90.harmontide.entity.Orders.current(b);
                if (order != com.linkyoshi90.harmontide.entity.Orders.Command.FREE_ROAM) data.putString("Order", order.name().toLowerCase(java.util.Locale.ROOT));
                if (b.getOrders().stance() != com.linkyoshi90.harmontide.entity.Orders.Stance.NATURAL) data.putString("Stance", b.getOrders().stance().name().toLowerCase(java.util.Locale.ROOT));
            }
            if (accessor.getPlayer() != null && accessor.getEntity() instanceof net.minecraft.world.entity.Mob dm) {   // dates and sweethearts
                if (com.linkyoshi90.harmontide.entity.Dates.canDate(dm, accessor.getPlayer())) {
                    data.putString("DatePlace", com.linkyoshi90.harmontide.entity.Dates.favourite(dm).name().toLowerCase(java.util.Locale.ROOT));
                }
                if (com.linkyoshi90.harmontide.entity.Sweethearts.sweetheart(dm) != null) data.putBoolean("SweetWed", com.linkyoshi90.harmontide.entity.Sweethearts.wed(dm));
                if (com.linkyoshi90.harmontide.entity.Marriage.married(dm, accessor.getPlayer())) data.putBoolean("Wife", com.linkyoshi90.harmontide.entity.Marriage.honeymoon(dm));
                else if (com.linkyoshi90.harmontide.entity.Marriage.engaged(dm, accessor.getPlayer())) data.putBoolean("Engaged", true);
            }
            if (accessor.getEntity() instanceof net.minecraft.world.entity.Mob g) {   // what she craves, once she has said
                net.minecraft.world.item.Item want = com.linkyoshi90.harmontide.entity.girl.Cravings.told(g) ? com.linkyoshi90.harmontide.entity.girl.Cravings.craving(g) : null;
                if (want != null && !com.linkyoshi90.harmontide.entity.girl.Cravings.answered(g)) data.putString("Craving", want.getDescriptionId());
            }
            com.linkyoshi90.harmontide.entity.Clade branch = com.linkyoshi90.harmontide.entity.Clade.of(accessor.getEntity());
            if (branch != null && accessor.getPlayer() != null) {                // how much of her branch of the tree of life they have met
                data.putInt("BranchMet", com.linkyoshi90.harmontide.entity.Journal.metOn(accessor.getPlayer(), branch));
                data.putInt("BranchAll", com.linkyoshi90.harmontide.entity.Journal.members(branch).size());
            }
            if (accessor.getEntity() instanceof LivingEntity l && l.level() instanceof net.minecraft.server.level.ServerLevel level) {
                int nursing = com.linkyoshi90.harmontide.entity.Nursing.status(level, l);
                if (nursing > 0) data.putInt("Nursing", nursing);
                if (com.linkyoshi90.harmontide.entity.Lactation.isFull(l)) data.putBoolean("MilkFull", true);
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            com.linkyoshi90.harmontide.entity.Rarity.Tier tier = com.linkyoshi90.harmontide.entity.Rarity.of(accessor.getEntity());
            if (tier != com.linkyoshi90.harmontide.entity.Rarity.Tier.COMMON) tooltip.add(Component.translatable(tier.key()).withStyle(tier.colour));
            com.linkyoshi90.harmontide.entity.Clade clade = com.linkyoshi90.harmontide.entity.Clade.of(accessor.getEntity());   // her branch of the tree of life
            if (clade != null) {
                Component name = Component.translatable("clade.harmontide." + clade.key());
                tooltip.add((data.contains("BranchAll") ? Component.translatable("jade.harmontide.branch", name, data.getInt("BranchMet"), data.getInt("BranchAll")) : name.copy())
                        .withStyle(clade.mythic() ? net.minecraft.ChatFormatting.DARK_PURPLE : net.minecraft.ChatFormatting.DARK_AQUA));
            }
            if (data.contains("Birthday")) tooltip.add(Component.translatable("jade.harmontide.birthday", data.getInt("Birthday")).withStyle(net.minecraft.ChatFormatting.GOLD));
            if (data.contains("Rhythm")) tooltip.add(Component.translatable("jade.harmontide.ecology", Component.translatable("foodweb.harmontide." + data.getString("FoodRole")),
                    Component.translatable("rhythm.harmontide." + data.getString("Rhythm"))).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
            if (data.contains("Hobby")) tooltip.add(Component.translatable("jade.harmontide.hobby", Component.translatable("hobby.harmontide." + data.getString("Hobby"))).withStyle(net.minecraft.ChatFormatting.GREEN));
            if (data.contains("Favourite")) tooltip.add(Component.translatable("jade.harmontide.favourite", Component.translatable(data.getString("Favourite"))).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (com.linkyoshi90.harmontide.entity.girl.Mining.isMiner(accessor.getEntity()) && !((net.minecraft.world.entity.LivingEntity) accessor.getEntity()).isBaby()) {
                tooltip.add(Component.translatable("jade.harmontide.miner").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            if (accessor.getEntity() instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl g && !g.isBaby()
                    && com.linkyoshi90.harmontide.entity.girl.Foraging.forages(g)) {
                tooltip.add(Component.translatable("jade.harmontide.forager").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            if (data.getInt("Visits") > 1) tooltip.add(Component.translatable("jade.harmontide.visits", data.getInt("Visits")).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("DatePlace")) tooltip.add(Component.translatable("jade.harmontide.date_place." + data.getString("DatePlace")).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("SweetWed")) tooltip.add(Component.translatable(data.getBoolean("SweetWed") ? "jade.harmontide.sweetheart.wed" : "jade.harmontide.sweetheart.courting").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("Wife")) tooltip.add(Component.translatable(data.getBoolean("Wife") ? "jade.harmontide.wife.honeymoon" : "jade.harmontide.wife").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.getBoolean("Engaged")) tooltip.add(Component.translatable("jade.harmontide.engaged").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            if (data.contains("Craving")) tooltip.add(Component.translatable("jade.harmontide.craving", Component.translatable(data.getString("Craving"))).withStyle(net.minecraft.ChatFormatting.GOLD));
            if (data.getBoolean("Cold")) tooltip.add(Component.translatable("jade.harmontide.cold").withStyle(net.minecraft.ChatFormatting.AQUA));
            if (data.contains("FavourItem")) {
                net.minecraft.world.item.Item want = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(data.getString("FavourItem")));
                tooltip.add(Component.translatable("jade.harmontide.favour", data.getInt("FavourCount"), want.getDescription()).withStyle(net.minecraft.ChatFormatting.YELLOW));
            }
            if (data.contains("Order") || data.contains("Stance")) {
                net.minecraft.network.chat.MutableComponent line = Component.empty();
                if (data.contains("Order")) line.append(Component.translatable("jade.harmontide.order." + data.getString("Order")));
                if (data.contains("Order") && data.contains("Stance")) line.append(", ");
                if (data.contains("Stance")) line.append(Component.translatable("jade.harmontide.stance." + data.getString("Stance")));
                tooltip.add(line.withStyle(net.minecraft.ChatFormatting.BLUE));
            }
            if (data.contains("Role")) tooltip.add(Component.translatable(data.getString("Role")).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
            if (data.contains("Pregnant")) {
                tooltip.add(Component.translatable("jade.harmontide.pregnant",
                        Component.translatable(data.getBoolean("Pregnant") ? "jade.harmontide.yes" : "jade.harmontide.no")));
            }
            if (data.getBoolean("Pregnant") && data.contains("DueMinutes")) tooltip.add(Component.translatable("jade.harmontide.due", data.getInt("DueMinutes")));
            if (data.contains("Nursing")) tooltip.add(Component.translatable(data.getInt("Nursing") == 2 ? "jade.harmontide.keeps_milk" : "jade.harmontide.nursing"));
            if (data.getBoolean("MilkFull")) tooltip.add(Component.translatable("jade.harmontide.milk_full").withStyle(net.minecraft.ChatFormatting.RED));
            if ((data.getBoolean("MilkFull") || data.contains("Nursing")) && accessor.getEntity() instanceof LivingEntity milky) {   // which milk she gives
                com.linkyoshi90.harmontide.item.FamilyMilk kind = com.linkyoshi90.harmontide.item.FamilyMilk.of(milky);
                if (kind != null) tooltip.add(Component.translatable("jade.harmontide.milk_kind",
                        Component.translatable("item.harmontide." + kind.id())).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            if (data.contains("Affection")) {
                int a = data.getInt("Affection");
                tooltip.add(Component.translatable("jade.harmontide.friendship", a, BondInfo.nextMilestone(a)));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return BOND;
        }
    }
}
