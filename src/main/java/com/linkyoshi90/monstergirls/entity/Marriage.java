package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Marriage, where Monmusu's exchange programme was always heading. A wedding ring given to a grown, full-size friend at
 * {@link Bonded#FULL_AFFECTION} who would flirt with the player is a proposal: she says yes and they are engaged. Once the two of them
 * stand by a hearth together ({@link #WED_RANGE}) the wedding is held: the friends within {@link #GUESTS} gather round the fire, a bell
 * rings, and she wears a veil for the day (FLAG_VEIL).
 * <p>
 * A spouse forgives any absence (no friendship lost to Missing), is half again likelier to come to bed ({@link #BED}), and while she is within
 * {@link #NEAR} her player has {@link #HEALTH} more health. Every {@link #YEAR} days the wedding day comes round again: the veil, hearts, and
 * a little more fondness. How many spouses a player may have is the maxSpouses setting (0 for any number, the default).
 * <p>
 * Many spouses make one happy family, never a jealous one. A spouse nearby gives the next bride her blessing; the wives come to the
 * wedding as bridesmaids; the hearts add up (to {@link #BLISS_CAP} spouses); love shown to one warms the others within {@link #FAMILY}
 * (a little affection each, every {@link #WARMTH_COOLDOWN} ticks); and when one is lost, the others hold their player close.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Marriage {
    public static final double WED_RANGE = 8, GUESTS = 32, NEAR = 32;
    public static final float BED = 0.5F;
    public static final int HEALTH = 4, YEAR = 40, VEIL = 24000, EVERY = 41;
    public static final ResourceLocation BLISS = ResourceLocation.fromNamespaceAndPath(MonsterGirlsMod.MODID, "wedded_bliss");
    private static final String SPOUSE = "SpouseOf", ENGAGED = "EngagedTo", WED_DAY = "WedDay", VEIL_UNTIL = "VeilUntil", LAST_ANNIVERSARY = "WedAnniversary";
    private static final String SPOUSES = "Spouses", WARMED = "FamilyWarmedAt";
    public static final double FAMILY = 8;
    public static final int BLISS_CAP = 3, WARMTH_COOLDOWN = 1200, BIG_FAMILY = 3;
    /** Tests set this to stand in for the maxSpouses setting; negative = use the setting. */
    public static int limitOverride = -1;
    public static final int HONEYMOON = 3 * 24000;
    public static final float HONEYMOON_BED = 0.25F;
    private static final String HONEYMOON_UNTIL = "HoneymoonUntil";
    public static final java.util.List<net.minecraft.world.item.Item> PRESENTS = java.util.List.of(net.minecraft.world.item.Items.CAKE,
            net.minecraft.world.item.Items.HONEY_BOTTLE, net.minecraft.world.item.Items.COOKIE, net.minecraft.world.item.Items.POPPY, net.minecraft.world.item.Items.CORNFLOWER,
            net.minecraft.world.item.Items.CANDLE, net.minecraft.world.item.Items.WHITE_WOOL, net.minecraft.world.item.Items.EMERALD, net.minecraft.world.item.Items.GOLDEN_APPLE,
            net.minecraft.world.item.Items.GLOW_BERRIES, net.minecraft.world.item.Items.PUMPKIN_PIE, net.minecraft.world.item.Items.PINK_BED);

    public enum Answer { SHY, TAKEN, ALREADY, ENGAGED }

    private Marriage() {
    }

    // --- who is whose --------------------------------------------------------------------------------------------------

    @Nullable
    public static UUID spouse(Mob mob) {
        return mob.getPersistentData().hasUUID(SPOUSE) ? mob.getPersistentData().getUUID(SPOUSE) : null;
    }

    public static boolean married(Mob mob, Player player) {
        return player.getUUID().equals(spouse(mob));
    }

    public static boolean engaged(Mob mob, Player player) {
        return mob.getPersistentData().hasUUID(ENGAGED) && player.getUUID().equals(mob.getPersistentData().getUUID(ENGAGED));
    }

    private static ListTag spouses(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(SPOUSES, Tag.TAG_STRING);
    }

    /** How many a player is married to, or engaged to. */
    public static int count(Player player) {
        return spouses(player).size();
    }

    private static void setSpouses(Player player, ListTag list) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.put(SPOUSES, list);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    private static void remember(Player player, Mob mob) {
        ListTag list = spouses(player);
        String id = mob.getUUID().toString();
        for (Tag t : list) if (t.getAsString().equals(id)) return;
        list.add(StringTag.valueOf(id));
        setSpouses(player, list);
    }

    private static void forget(Player player, UUID mob) {
        ListTag list = spouses(player);
        list.removeIf(t -> t.getAsString().equals(mob.toString()));
        setSpouses(player, list);
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);         // the family page remembers her
        ListTag family = journal.getList(FAMILY_PAGE, Tag.TAG_COMPOUND);
        for (Tag t : family) if (((CompoundTag) t).getString("Id").equals(mob.toString())) ((CompoundTag) t).putBoolean("Gone", true);
        journal.put(FAMILY_PAGE, family);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    public static final String FAMILY_PAGE = "Family", CHILDREN = "Children", FAMILY_PAINTING = "FamilyPainting";

    /** The wedding goes in the journal's family page: her name, her kind and the day. */
    private static void writeFamily(Player player, Mob mob) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag family = journal.getList(FAMILY_PAGE, Tag.TAG_COMPOUND);
        String id = mob.getUUID().toString();
        for (Tag t : family) if (((CompoundTag) t).getString("Id").equals(id)) return;
        CompoundTag entry = new CompoundTag();
        entry.putString("Id", id);
        entry.putString("Name", mob.getName().getString());
        entry.putString("Kind", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath());
        entry.putLong("Day", mob.level().getDayTime() / 24000L + 1);
        family.add(entry);
        journal.put(FAMILY_PAGE, family);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    public static final String CHILD_LIST = "ChildList";
    public static final int MAX_CHILDREN_LISTED = 120;

    /** One more little one born devoted to this player: counted, and listed (her name, kind and birth day) in the family journal. */
    public static void childBorn(Player player, Entity young) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.putInt(CHILDREN, journal.getInt(CHILDREN) + 1);
        ListTag list = journal.getList(CHILD_LIST, Tag.TAG_COMPOUND);
        if (list.size() < MAX_CHILDREN_LISTED) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", young.getUUID().toString());
            entry.putString("Name", young.getName().getString());
            entry.putString("Kind", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(young.getType()).getPath());
            entry.putLong("Day", young.level().getDayTime() / 24000L + 1);
            list.add(entry);
            journal.put(CHILD_LIST, list);
        }
        player.setData(ModAttachments.JOURNAL, journal);
    }

    /** Her mother, noted beside a little one already listed (live births: the mother is known at the birth). */
    public static void motherOf(Player player, Entity young, Entity mother) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag list = journal.getList(CHILD_LIST, Tag.TAG_COMPOUND);
        for (Tag t : list) if (((CompoundTag) t).getString("Id").equals(young.getUUID().toString())) ((CompoundTag) t).putString("Mother", mother.getName().getString());
        journal.put(CHILD_LIST, list);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    public static ListTag childList(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(CHILD_LIST, Tag.TAG_COMPOUND);
    }

    public static final String NOTES = "HomeNotes";
    public static final int NOTE_KINDS = 10, NOTES_KEPT = 12;

    /** A wife who missed them while they were away leaves a note in their journal (Notes from home, the latest NOTES_KEPT). */
    public static void leaveNote(Player player, Mob wife) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag notes = journal.getList(NOTES, Tag.TAG_COMPOUND);
        CompoundTag note = new CompoundTag();
        note.putString("Name", wife.getName().getString());
        note.putInt("Line", wife.getRandom().nextInt(NOTE_KINDS));
        note.putLong("Day", wife.level().getDayTime() / 24000L + 1);
        notes.add(note);
        while (notes.size() > NOTES_KEPT) notes.remove(0);
        journal.put(NOTES, notes);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    public static ListTag notes(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(NOTES, Tag.TAG_COMPOUND);
    }

    public static ListTag family(Player player) {
        return player.getData(ModAttachments.JOURNAL).getList(FAMILY_PAGE, Tag.TAG_COMPOUND);
    }

    public static int children(Player player) {
        return player.getData(ModAttachments.JOURNAL).getInt(CHILDREN);
    }

    /** Whether the player already has as many spouses (and fiancées) as the maxSpouses setting allows. */
    public static boolean atLimit(Player player) {
        int limit = limitOverride >= 0 ? limitOverride : MonsterGirlsConfig.maxSpouses();
        return limit > 0 && count(player) >= limit;
    }

    // --- the proposal --------------------------------------------------------------------------------------------------

    /** The ring is offered. Her answer; ENGAGED means it was taken. */
    public static Answer propose(ServerLevel level, Mob mob, Player player) {
        Bonded b = (Bonded) mob;
        if (married(mob, player) || engaged(mob, player)) {
            say(player, "marriage.already", mob, true);
            return Answer.ALREADY;
        }
        if (spouse(mob) != null || mob.getPersistentData().hasUUID(ENGAGED)) {
            say(player, "marriage.taken", mob, true);
            return Answer.TAKEN;
        }
        if (atLimit(player)) {
            say(player, "marriage.limit", mob, true);
            return Answer.TAKEN;
        }
        if (!Flirting.canFlirt(mob, player) || b.getAffection(player) < Bonded.FULL_AFFECTION) {
            b.blushFor(100);
            say(player, "marriage.shy", mob, true);
            return Answer.SHY;
        }
        Mob blessing = nearestSpouse(level, mob, player, GUESTS);
        mob.getPersistentData().putUUID(ENGAGED, player.getUUID());
        remember(player, mob);
        b.blushFor(400);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 12, 0.5, 0.4, 0.5, 0.0);
        mob.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("message.monstergirls.marriage.yes." + mob.getRandom().nextInt(4), mob.getDisplayName()), false);
        UUID parentId = b.getFather();                                           // someone's daughter: her parent hears of it
        if (parentId != null && !parentId.equals(player.getUUID()) && level.getServer().getPlayerList().getPlayer(parentId) instanceof Player parent) {
            parent.displayClientMessage(Component.translatable("message.monstergirls.marriage.daughter_engaged", player.getDisplayName(), mob.getDisplayName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        if (Dates.fits(level, mob.blockPosition(), Dates.favourite(mob))) {      // asked at her kind of place: perfect
            b.addAffection(player, PERFECT);
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 24000, 0));
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.5, mob.getZ(), 20, 0.8, 0.6, 0.8, 0.0);
            player.displayClientMessage(Component.translatable("message.monstergirls.marriage.perfect", mob.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            Dairy.award(player, "perfect_proposal");
        }
        if (blessing != null) {                                                  // the family grows, and is glad of it
            ((Bonded) blessing).blushFor(100);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, blessing.getX(), blessing.getEyeY() + 0.3, blessing.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
            player.displayClientMessage(Component.translatable("message.monstergirls.marriage.blessing." + mob.getRandom().nextInt(3), blessing.getDisplayName(), mob.getDisplayName()), false);
        }
        return Answer.ENGAGED;
    }

    /** The loaded spouses of this player within range of a spot, other than the one given. */
    public static java.util.List<Mob> spousesNear(ServerLevel level, Player player, Entity around, double range, @Nullable Mob except) {
        if (count(player) == 0) return java.util.List.of();
        return level.getEntitiesOfClass(Mob.class, around.getBoundingBox().inflate(range), m -> m != except && m.isAlive() && married(m, player));
    }

    @Nullable
    private static Mob nearestSpouse(ServerLevel level, Mob mob, Player player, double range) {
        Mob best = null;
        for (Mob m : spousesNear(level, player, mob, range, mob)) if (best == null || m.distanceToSqr(mob) < best.distanceToSqr(mob)) best = m;
        return best;
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItems.WEDDING_RING.get()) || !(event.getTarget() instanceof Mob mob) || !(mob instanceof Bonded)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level && propose(level, mob, event.getEntity()) == Answer.ENGAGED) event.getItemStack().consume(1, event.getEntity());
    }

    // --- the wedding ---------------------------------------------------------------------------------------------------

    /** The hearth both of them stand by, or null. */
    @Nullable
    public static BlockPos hearthFor(ServerLevel level, Mob mob, Player player) {
        for (BlockPos h : HearthBlockEntity.loaded(level)) {
            double r = WED_RANGE * WED_RANGE;
            if (mob.distanceToSqr(h.getCenter()) <= r && player.distanceToSqr(h.getCenter()) <= r) return h;
        }
        return null;
    }

    /** A lit realm censer both of them stand by, or null: the Demon Realm's own kind of wedding. */
    @Nullable
    public static BlockPos censerFor(ServerLevel level, Mob mob, Player player) {
        return level.getPoiManager().findClosest(h -> h.is(com.linkyoshi90.monstergirls.registry.ModVillagers.CENSER.getKey()),
                p -> player.distanceToSqr(p.getCenter()) <= WED_RANGE * WED_RANGE, mob.blockPosition(), (int) WED_RANGE,
                net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY).orElse(null);
    }

    /** Engaged and both by a hearth (or a lit realm censer): the wedding. True if it was held. */
    public static boolean tryWed(ServerLevel level, Mob mob, Player player) {
        if (!engaged(mob, player)) return false;
        BlockPos hearth = hearthFor(level, mob, player);
        boolean realm = hearth == null;
        if (realm) hearth = censerFor(level, mob, player);
        if (hearth == null) return false;
        java.util.List<Mob> brides = new java.util.ArrayList<>();             // every fiancée of theirs standing by it is wed in the one ceremony
        brides.add(mob);
        for (Mob other : level.getEntitiesOfClass(Mob.class, new AABB(hearth).inflate(WED_RANGE), m -> m != mob && m.isAlive() && engaged(m, player))) brides.add(other);
        for (Mob bride : brides) wed(level, bride, player, hearth, realm);
        if (brides.size() > 1) player.displayClientMessage(Component.translatable("message.monstergirls.marriage.group", brides.size()).withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static int wed(ServerLevel level, Mob mob, Player player, BlockPos hearth) {
        return wed(level, mob, player, hearth, false);
    }

    /**
     * The wedding itself. How many guests came. A realm wedding (by a lit censer) is the Demon Realm's kind: violet smoke instead of
     * fireworks, and the vows leave the player {@link #REALM_VOWS} more realm-touched (RealmTouched).
     */
    public static int wed(ServerLevel level, Mob mob, Player player, BlockPos hearth, boolean realm) {
        if (realm) {
            RealmTouched.addTouch(player, REALM_VOWS);
            level.sendParticles(ParticleTypes.WITCH, hearth.getX() + 0.5, hearth.getY() + 1.5, hearth.getZ() + 0.5, 60, 1.5, 1.0, 1.5, 0.05);
            level.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getEyeY(), mob.getZ(), 40, 0.6, 0.6, 0.6, 0.3);
            player.displayClientMessage(Component.translatable("message.monstergirls.marriage.realm", mob.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE), false);
            Dairy.award(player, "realm_wedding");
        }
        CompoundTag pd = mob.getPersistentData();
        pd.remove(ENGAGED);
        pd.putUUID(SPOUSE, player.getUUID());
        pd.putLong(WED_DAY, MatingSeason.today(level));
        pd.putLong(LAST_ANNIVERSARY, MatingSeason.today(level));
        pd.putLong(VEIL_UNTIL, level.getGameTime() + VEIL);
        pd.putLong(HONEYMOON_UNTIL, level.getGameTime() + HONEYMOON);
        remember(player, mob);
        writeFamily(player, mob);
        Bonded b = (Bonded) mob;
        b.setAffection(player.getUUID(), Math.max(b.getAffection(player), Bonded.FULL_AFFECTION));
        b.blushFor(VEIL);
        veil(level, mob);
        int guests = 0, bridesmaids = 0;
        java.util.List<Mob> giving = new java.util.ArrayList<>();
        for (Mob m : HomeLife.friendsOf(level, player, new AABB(hearth).inflate(GUESTS))) {
            if (m == mob) continue;
            if (!married(m, player)) giving.add(m);
            m.getNavigation().moveTo(hearth.getX() + 0.5, hearth.getY(), hearth.getZ() + 0.5, 1.0);
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 2, 0.2, 0.1, 0.2, 0.0);
            guests++;
            if (married(m, player)) {                                               // the wives stand beside her, scattering petals
                bridesmaids++;
                ((Bonded) m).blushFor(400);
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, m.getX(), m.getEyeY() + 0.5, m.getZ(), 20, 0.6, 0.4, 0.6, 0.0);
            }
        }
        level.playSound(null, hearth, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.5F, 1.0F);
        level.playSound(null, hearth, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 20, 0.7, 0.5, 0.7, 0.0);
        if (!realm) level.sendParticles(ParticleTypes.FIREWORK, hearth.getX() + 0.5, hearth.getY() + 2.5, hearth.getZ() + 0.5, 60, 1.5, 1.0, 1.5, 0.08);
        Component news = Component.translatable("message.monstergirls.marriage.wed", player.getDisplayName(), mob.getDisplayName(), guests).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (Player p : level.players()) if (p.distanceToSqr(mob) < 64 * 64) p.displayClientMessage(news, false);
        flourish(level, mob, player);
        bouquet(level, mob, player, giving);
        net.minecraft.world.item.ItemStack keepsake = com.linkyoshi90.monstergirls.block.FigurineBlockEntity.of(mob);   // her in her veil, for the mantelpiece
        if (!keepsake.isEmpty()) {
            if (!player.addItem(keepsake)) player.drop(keepsake, false);
            player.displayClientMessage(Component.translatable("message.monstergirls.marriage.figurine", mob.getDisplayName()), false);
        }
        if (presents(level, giving, hearth) > 0) player.displayClientMessage(Component.translatable("message.monstergirls.marriage.presents", giving.size()), false);
        if (bridesmaids > 0) player.displayClientMessage(Component.translatable("message.monstergirls.marriage.bridesmaids", bridesmaids, mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        Dairy.award(player, "wedding");
        if (wedNow(player) >= BIG_FAMILY) {
            Dairy.award(player, "big_family");
            CompoundTag journal = player.getData(ModAttachments.JOURNAL);
            if (!journal.getBoolean(FAMILY_PAINTING)) {                          // the whole family, once, for the wall
                journal.putBoolean(FAMILY_PAINTING, true);
                player.setData(ModAttachments.JOURNAL, journal);
                net.minecraft.world.item.ItemStack painting = Hobbies.painting(MonsterGirlsMod.id("whole_family"));
                if (!player.addItem(painting)) player.drop(painting, false);
                player.displayClientMessage(Component.translatable("message.monstergirls.marriage.family_painting"), false);
            }
        }
        return guests;
    }

    /** How many on the player's list are wed rather than engaged, counting the loaded ones' own say and the rest as wed. */
    private static int wedNow(Player player) {
        int n = 0;                                                             // the family page lists only weddings
        for (Tag t : family(player)) if (!((CompoundTag) t).getBoolean("Gone")) n++;
        return n;
    }

    private static void veil(ServerLevel level, Mob mob) {
        if (mob instanceof AbstractMonsterGirl g) g.setFlag(AbstractMonsterGirl.FLAG_VEIL, level.getGameTime() < mob.getPersistentData().getLong(VEIL_UNTIL));
    }

    // --- married life --------------------------------------------------------------------------------------------------

    public static float bedBonus(Mob mob, Player player) {
        return married(mob, player) ? BED + (honeymoon(mob) ? HONEYMOON_BED : 0.0F) : 0.0F;
    }

    /** The first HONEYMOON ticks after the wedding: keener still for bed, and every pat brings a tease (Flirting.chance). */
    public static boolean honeymoon(Mob mob) {
        return mob.getPersistentData().hasUUID(SPOUSE) && mob.level().getGameTime() < mob.getPersistentData().getLong(HONEYMOON_UNTIL);
    }

    /** Morning after two or more wives shared the bed: a warm tangle, and the player wakes well fed and sturdy. True if so. */
    public static boolean familyBed(ServerLevel level, Player player, int wives) {
        if (wives < 2) return false;
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, Math.min(wives, BLISS_CAP) - 1));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SATURATION, 20, 0));
        player.displayClientMessage(Component.translatable("message.monstergirls.marriage.family_bed." + level.random.nextInt(3), wives).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return true;
    }

    /**
     * The bride tosses her bouquet. A grown daughter without a sweetheart catches it first if one is there, and takes a shine to the
     * nearest free villager (Sweethearts); otherwise a guest who would flirt with the player catches it, blushes and is
     * {@link #BOUQUET} closer to them: she might be next. Who caught it, or null.
     */
    @Nullable
    public static Mob bouquet(ServerLevel level, Mob bride, Player player, java.util.List<Mob> guests) {
        Mob catcher = null;
        for (Mob g : guests) if (Sweethearts.eligible(g) && Sweethearts.sweetheart(g) == null) catcher = g;
        boolean daughter = catcher != null;
        if (catcher == null) for (Mob g : guests) if (Flirting.canFlirt(g, player)) catcher = g;
        if (catcher == null) return null;
        ((Bonded) catcher).blushFor(300);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, catcher.getX(), catcher.getEyeY() + 0.6, catcher.getZ(), 15, 0.3, 0.3, 0.3, 0.0);
        if (daughter) {
            final Mob d = catcher;
            for (net.minecraft.world.entity.npc.Villager v : level.getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class, d.getBoundingBox().inflate(Sweethearts.NEAR),
                    v -> Sweethearts.free(level, v))) {
                if (Sweethearts.court(level, d, v)) break;
            }
        } else {
            ((Bonded) catcher).addAffection(player, BOUQUET);
        }
        player.displayClientMessage(Component.translatable(daughter ? "message.monstergirls.marriage.bouquet.daughter" : "message.monstergirls.marriage.bouquet",
                bride.getDisplayName(), catcher.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return catcher;
    }

    public static final int BOUQUET = 5, REALM_VOWS = 15, SUNSHOWER = 1200, PERFECT = 10;

    /**
     * Each kind weds in her own way: a kitsune's wedding brings a sunshower (the fox's wedding: rain from a clear day sky, for
     * {@link #SUNSHOWER} ticks), a harpy's a flurry of feathers, a mermaid's bubbles, a dragon's a plume of flame, the undead's soul
     * fire, a slime's a happy splash. The flourish's key (message.monstergirls.marriage.flourish.KEY), or null for none.
     */
    @Nullable
    public static String flourish(ServerLevel level, Mob bride, Player player) {
        String key = null;
        net.minecraft.core.particles.ParticleOptions particle = null;
        if (bride instanceof AbstractHarpy) {
            key = "feathers";
            particle = new net.minecraft.core.particles.ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER));
        } else if (bride instanceof AbstractMonsterGirl g) {
            switch (g.getSpecies().family()) {
                case KITSUNE -> {
                    key = "sunshower";
                    if (level.isDay() && !level.isRaining() && level.dimensionType().natural()) level.setWeatherParameters(0, SUNSHOWER, true, false);
                    particle = ParticleTypes.FALLING_WATER;
                }
                case MERFOLK -> {
                    key = "bubbles";
                    particle = ParticleTypes.BUBBLE_POP;
                }
                case DRAGON -> {
                    key = "flame";
                    particle = ParticleTypes.FLAME;
                }
                case UNDEAD -> {
                    key = "souls";
                    particle = ParticleTypes.SOUL_FIRE_FLAME;
                }
                case SLIME -> {
                    key = "splash";
                    particle = ParticleTypes.ITEM_SLIME;
                }
                default -> {
                }
            }
        }
        if (key == null) return null;
        level.sendParticles(particle, bride.getX(), bride.getEyeY() + 1.0, bride.getZ(), 40, 1.2, 0.8, 1.2, 0.05);
        player.displayClientMessage(Component.translatable("message.monstergirls.marriage.flourish." + key, bride.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return key;
    }
    private static final String ASKED = "AskedToMarry";

    /**
     * After a lovely date, a girl at FULL_AFFECTION who could marry this player and hasn't asked before doesn't wait to be asked: she
     * presses a wedding ring into their hand. Giving it back to her is the proposal. Once per girl. True if she asked.
     */
    public static boolean sheAsks(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || mob.getPersistentData().getBoolean(ASKED) || spouse(mob) != null || mob.getPersistentData().hasUUID(ENGAGED)
                || b.getAffection(player) < Bonded.FULL_AFFECTION || !Flirting.canFlirt(mob, player)) return false;
        if (atLimit(player)) return false;
        mob.getPersistentData().putBoolean(ASKED, true);
        net.minecraft.world.item.ItemStack ring = new net.minecraft.world.item.ItemStack(ModItems.WEDDING_RING.get());
        if (!player.addItem(ring)) player.drop(ring, false);
        b.blushFor(400);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.marriage.she_asks", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return true;
    }

    /**
     * A girl has given birth: her little ones' parent hears of it wherever they are, and the parent's wives near the mother gather
     * round (hearts and blushes). How many wives came.
     */
    public static int announceBirth(ServerLevel level, Mob mother, @Nullable UUID parent, boolean twins) {
        Player p = parent == null ? null : level.getServer().getPlayerList().getPlayer(parent);
        if (p == null) return 0;
        p.displayClientMessage(Component.translatable(twins ? "message.monstergirls.birth.twins" : "message.monstergirls.birth", mother.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        int came = 0;
        for (Mob wife : spousesNear(level, p, mother, GUESTS, mother)) {
            wife.getNavigation().moveTo(mother, 1.0);
            ((Bonded) wife).blushFor(100);
            level.sendParticles(ParticleTypes.HEART, wife.getX(), wife.getEyeY() + 0.3, wife.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            came++;
        }
        return came;
    }

    public static final int MORNING = 4000;
    public static final double LUNCH_NEAR = 6;
    private static final String LUNCH_DAY = "LunchDay";
    public static final java.util.List<net.minecraft.world.item.Item> LUNCHES = java.util.List.of(net.minecraft.world.item.Items.COOKED_BEEF,
            net.minecraft.world.item.Items.COOKED_PORKCHOP, net.minecraft.world.item.Items.COOKED_CHICKEN, net.minecraft.world.item.Items.COOKED_SALMON,
            net.minecraft.world.item.Items.BAKED_POTATO, net.minecraft.world.item.Items.PUMPKIN_PIE, net.minecraft.world.item.Items.RABBIT_STEW);

    /**
     * A packed lunch: the first time each morning (before {@link #MORNING}) a player is within {@link #LUNCH_NEAR} of one of their wives,
     * she hands them a lunch made with love (a named food). One a day, from whichever wife they pass first. Who gave it, or null.
     */
    @Nullable
    public static Mob lunch(ServerLevel level, Player p) {
        if (count(p) == 0 || level.getDayTime() % 24000L >= MORNING) return null;
        long today = level.getDayTime() / 24000L;
        CompoundTag journal = p.getData(ModAttachments.JOURNAL);
        if (journal.contains(LUNCH_DAY) && journal.getLong(LUNCH_DAY) == today) return null;
        java.util.List<Mob> near = spousesNear(level, p, p, LUNCH_NEAR, null);
        if (near.isEmpty()) return null;
        Mob wife = near.get(level.random.nextInt(near.size()));
        journal.putLong(LUNCH_DAY, today);
        p.setData(ModAttachments.JOURNAL, journal);
        net.minecraft.world.item.ItemStack food = new net.minecraft.world.item.ItemStack(LUNCHES.get(wife.getRandom().nextInt(LUNCHES.size())), 2);
        food.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.packed_lunch", wife.getName()));
        food.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(java.util.List.of(
                Component.translatable("item.monstergirls.packed_lunch.lore"))));
        if (!p.addItem(food)) p.drop(food, false);
        ((Bonded) wife).blushFor(80);
        level.sendParticles(ParticleTypes.HEART, wife.getX(), wife.getEyeY() + 0.3, wife.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        p.displayClientMessage(Component.translatable("message.monstergirls.marriage.lunch." + wife.getRandom().nextInt(3), wife.getDisplayName()), false);
        return wife;
    }

    /** The places a wife may dream of, by the name the dream line uses (message.monstergirls.marriage.dream.place.NAME). */
    public static final java.util.Map<String, net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure>> DREAM_PLACES = java.util.Map.of(
            "oasis", com.linkyoshi90.monstergirls.registry.ModStructures.OASIS_KEY, "island", com.linkyoshi90.monstergirls.registry.ModStructures.SKY_ISLAND_KEY,
            "shrine", com.linkyoshi90.monstergirls.registry.ModStructures.KITSUNE_SHRINE_KEY, "inn", com.linkyoshi90.monstergirls.registry.ModStructures.WAYSIDE_INN_KEY,
            "grotto", com.linkyoshi90.monstergirls.registry.ModStructures.MERFOLK_GROTTO_KEY, "aerie", com.linkyoshi90.monstergirls.registry.ModStructures.HARPY_AERIE_KEY,
            "camp", com.linkyoshi90.monstergirls.registry.ModStructures.CENTAUR_CAMP_KEY, "labyrinth", com.linkyoshi90.monstergirls.registry.ModStructures.LABYRINTH_KEY,
            "lair", com.linkyoshi90.monstergirls.registry.ModStructures.DRAGON_LAIR_KEY, "den", com.linkyoshi90.monstergirls.registry.ModStructures.SILK_DEN_KEY);
    public static final int DREAM_ODDS = 3, DREAM_CHUNKS = 40;

    /**
     * A shared dream: the morning after a wife shared the bed, one time in {@link #DREAM_ODDS} she wakes having dreamt of a place, and
     * it is real: one of the landmarks above, the nearest of a kind picked at random within {@link #DREAM_CHUNKS} chunks, and she
     * says which way and roughly how far. The place's name, or null (no dream, or nothing of that kind near).
     */
    @Nullable
    public static String sharedDream(ServerLevel level, Player player, Mob wife, boolean force) {
        if (!force && level.random.nextInt(DREAM_ODDS) != 0) return null;
        java.util.List<String> kinds = new java.util.ArrayList<>(DREAM_PLACES.keySet());
        java.util.Collections.sort(kinds);
        String kind = kinds.get(level.random.nextInt(kinds.size()));
        var holder = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getHolder(DREAM_PLACES.get(kind));
        if (holder.isEmpty()) return null;
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, net.minecraft.core.HolderSet.direct(holder.get()),
                player.blockPosition(), DREAM_CHUNKS, false);
        if (found == null) return null;
        BlockPos at = found.getFirst();
        int far = (int) Math.round(Math.sqrt(player.blockPosition().distSqr(new BlockPos(at.getX(), player.getBlockY(), at.getZ()))) / 100.0) * 100;
        String way = com.linkyoshi90.monstergirls.item.FriendCompassItem.way(player, at.getCenter());
        player.displayClientMessage(Component.translatable("message.monstergirls.marriage.dream", wife.getDisplayName(),
                Component.translatable("message.monstergirls.marriage.dream.place." + kind), Component.translatable("direction.monstergirls." + way),
                Math.max(100, far)).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return kind;
    }

    /** Each guest who isn't family brings a little wedding present, left by the hearth. How many. */
    public static int presents(ServerLevel level, java.util.List<Mob> guests, BlockPos hearth) {
        int n = 0;
        for (Mob g : guests) {
            net.minecraft.world.item.Item gift = PRESENTS.get(g.getRandom().nextInt(PRESENTS.size()));
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level, hearth.getX() + 0.5 + (g.getRandom().nextDouble() - 0.5) * 2,
                    hearth.getY() + 1.0, hearth.getZ() + 0.5 + (g.getRandom().nextDouble() - 0.5) * 2, new net.minecraft.world.item.ItemStack(gift));
            item.setPickUpDelay(20);
            level.addFreshEntity(item);
            n++;
        }
        return n;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % EVERY != 0 || !(e instanceof Mob mob) || !(e instanceof Bonded) || !(e.level() instanceof ServerLevel level)) return;
        CompoundTag pd = mob.getPersistentData();
        if (pd.hasUUID(ENGAGED)) {
            Player p = level.getPlayerByUUID(pd.getUUID(ENGAGED));
            if (p != null) tryWed(level, mob, p);
        }
        if (pd.hasUUID(SPOUSE)) {
            anniversary(level, mob);
            veil(level, mob);
        }
    }

    /** Every YEAR days after the wedding: the veil again, hearts and a line for her spouse. True on the day. */
    public static boolean anniversary(ServerLevel level, Mob mob) {
        CompoundTag pd = mob.getPersistentData();
        long today = MatingSeason.today(level), since = today - pd.getLong(WED_DAY);
        if (since <= 0 || since % YEAR != 0 || pd.getLong(LAST_ANNIVERSARY) == today) return false;
        Player p = level.getPlayerByUUID(pd.getUUID(SPOUSE));
        if (p == null || p.distanceToSqr(mob) > NEAR * NEAR) return false;            // she waits until they are together on the day
        pd.putLong(LAST_ANNIVERSARY, today);
        pd.putLong(VEIL_UNTIL, level.getGameTime() + VEIL);
        pd.putLong(HONEYMOON_UNTIL, level.getGameTime() + 24000L);                // a second honeymoon, for the day
        Bonded b = (Bonded) mob;
        b.blushFor(400);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 10, 0.5, 0.4, 0.5, 0.0);
        p.displayClientMessage(Component.translatable("message.monstergirls.marriage.anniversary", mob.getDisplayName(), since / YEAR), false);
        b.addAffection(p, 2);                                                  // and a present for the day, as she had at the wedding
        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, mob.getX(), mob.getY() + 0.5, mob.getZ(),
                new net.minecraft.world.item.ItemStack(PRESENTS.get(mob.getRandom().nextInt(PRESENTS.size())))));
        return true;
    }

    /** Logging in: one of the player's wives (loaded and near, else any on the list) says she missed them. Who, or null. */
    @SubscribeEvent
    public static void onLogIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getPersistentData().putInt(WELCOME, WELCOME_DELAY);   // once the world round them has loaded
    }

    private static final String WELCOME = "WelcomeBackIn";
    public static final int WELCOME_DELAY = 200;

    @Nullable
    public static Mob welcomeBack(ServerLevel level, Player p) {
        java.util.List<Mob> near = spousesNear(level, p, p, GUESTS, null);
        if (near.isEmpty()) return null;
        Mob wife = near.get(level.random.nextInt(near.size()));
        ((Bonded) wife).blushFor(100);
        p.displayClientMessage(Component.translatable("message.monstergirls.marriage.welcome." + level.random.nextInt(3), wife.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        return wife;
    }

    /** Wedded bliss: more health while a spouse is near. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 == 23 && event.getEntity().level() instanceof ServerLevel level) {
            bliss(level, event.getEntity());
            lunch(level, event.getEntity());
            CompoundTag pd = event.getEntity().getPersistentData();
            if (pd.contains(WELCOME)) {
                int left = pd.getInt(WELCOME) - 40;
                if (left > 0) pd.putInt(WELCOME, left);
                else {
                    pd.remove(WELCOME);
                    welcomeBack(level, event.getEntity());
                }
            }
        }
    }

    /** Whether a spouse is near; applies or lifts the health bonus. */
    public static boolean bliss(ServerLevel level, Player p) {
        return blissFrom(level, p) > 0;
    }

    /** How many spouses are near (the bonus counts up to BLISS_CAP of them); applies, resizes or lifts the health bonus. */
    public static int blissFrom(ServerLevel level, Player p) {
        AttributeInstance attr = p.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return 0;
        int near = spousesNear(level, p, p, NEAR, null).size();
        double want = HEALTH * Math.min(near, BLISS_CAP);
        AttributeModifier have = attr.getModifier(BLISS);
        if (have != null && have.amount() != want) attr.removeModifier(BLISS);
        if (want > 0 && !attr.hasModifier(BLISS)) attr.addTransientModifier(new AttributeModifier(BLISS, want, AttributeModifier.Operation.ADD_VALUE));
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        return near;
    }

    /**
     * Love shown to one spouse warms the rest of the family nearby: they smile and blush, and each gains a little affection (at most once per
     * WARMTH_COOLDOWN). Called on every pat. How many were warmed.
     */
    public static int warmth(ServerLevel level, Mob petted, Player player) {
        if (!married(petted, player)) return 0;
        int warmed = 0;
        for (Mob m : spousesNear(level, player, petted, FAMILY, petted)) {
            Bonded b = (Bonded) m;
            b.blushFor(60);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, m.getX(), m.getEyeY() + 0.3, m.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            CompoundTag pd = m.getPersistentData();
            if (level.getGameTime() < pd.getLong(WARMED)) continue;
            pd.putLong(WARMED, level.getGameTime() + WARMTH_COOLDOWN);
            b.addAffection(player, 1);
            if (warmed++ == 0) player.displayClientMessage(Component.translatable("message.monstergirls.marriage.warmth." + m.getRandom().nextInt(3), m.getDisplayName(), petted.getDisplayName()), true);
        }
        return warmed;
    }

    /** One of the family is gone: the others nearby come to hold their player. How many came. */
    public static int comfort(ServerLevel level, Player p, Mob lost) {
        java.util.List<Mob> family = spousesNear(level, p, p, NEAR, lost);
        for (Mob m : family) {
            m.getNavigation().moveTo(p, 1.1);
            ((Bonded) m).blushFor(200);
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        }
        if (!family.isEmpty()) {
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200 * Math.min(family.size(), BLISS_CAP), 0));
            p.displayClientMessage(Component.translatable(family.size() == 1 ? "message.monstergirls.marriage.comfort.one" : "message.monstergirls.marriage.comfort.many",
                    family.get(0).getDisplayName(), family.size() - 1).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return family.size();
    }

    /** A spouse or fiancée dies: the player is told, and free to marry again. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        CompoundTag pd = mob.getPersistentData();
        UUID who = pd.hasUUID(SPOUSE) ? pd.getUUID(SPOUSE) : pd.hasUUID(ENGAGED) ? pd.getUUID(ENGAGED) : null;
        if (who == null) return;
        Player p = level.getServer().getPlayerList().getPlayer(who);
        if (p == null) return;
        forget(p, mob.getUUID());
        p.displayClientMessage(Component.translatable("message.monstergirls.marriage.lost", mob.getDisplayName()).withStyle(ChatFormatting.GRAY), false);
        net.minecraft.world.item.ItemStack ring = new net.minecraft.world.item.ItemStack(ModItems.WEDDING_RING.get());   // her ring stays behind
        ring.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.her_ring", mob.getName()));
        mob.spawnAtLocation(ring);
        if (p.level() == level) comfort(level, p, mob);
    }

    /** A line for /monstergirls bonds, or null. */
    @Nullable
    public static Component describe(Mob mob, Player player) {
        if (married(mob, player)) return Component.translatable("bond.monstergirls.spouse").withStyle(ChatFormatting.LIGHT_PURPLE);
        if (engaged(mob, player)) return Component.translatable("bond.monstergirls.engaged").withStyle(ChatFormatting.LIGHT_PURPLE);
        return null;
    }

    private static void say(Player player, String key, Mob mob, boolean bar) {
        player.displayClientMessage(Component.translatable("message.monstergirls." + key, mob.getDisplayName()), bar);
    }
}
