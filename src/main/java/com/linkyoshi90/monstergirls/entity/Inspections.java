package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModVillagers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Liaison inspections (2026-11-11, after Monster Musume's exchange coordinator). A player who carries a homestay permit and keeps
 * a hearth with residents gets a visit from the exchange office: the first {@link #FIRST} ticks after both are true, then every
 * {@link #INTERVAL}. By day, with the player within {@link Order#HEARTH_RANGE} of their hearth and at least one resident there,
 * an inspector (a liaison villager, named) walks up from {@link #ARRIVE} blocks off, looks the home over at the fire and hands the
 * player her report (a written book), then walks off and is gone.
 * <p>
 * Five checks of {@link #POINTS} each, around the hearth within {@link #AROUND}: a bed for every resident there; at least
 * {@link #FOOD} food put away (fridges, chests, barrels); no resident hurt or under half health; the residents fond of their host
 * (on average {@link #FOND}); no monster about. Pay is an emerald a point; a perfect home ({@link #MODEL}) also earns the
 * Hero of the Village for a day, and the advancement "model_home". Below {@link #PASS} the report is a warning and there is no pay.
 * Config "inspections".
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Inspections {
    public enum Check { BEDS, FOOD, HEALTH, FONDNESS, SAFETY }

    public static final long FIRST = 3 * 24000L, INTERVAL = 7 * 24000L;
    public static final int POINTS = 2, MODEL = 10, PASS = 6, AROUND = 16, FOOD = 8, FOND = 40, ARRIVE = 10, NEAR = 3,
            LOOK = 600, LEAVE = 400, CHECK_EVERY = 200, HERO = 24000;
    private static final String NEXT = "NextInspection", TAG = "Inspector", HEARTH = "Hearth", OWNER = "Owner", UNTIL = "Until", DONE = "Done";

    private Inspections() {}

    // --- when -------------------------------------------------------------------------------------------------------

    /** When the next one is due (0: not yet scheduled). */
    public static long next(Player player) {
        return player.getData(ModAttachments.JOURNAL).getLong(NEXT);
    }

    public static void setNext(Player player, long time) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.putLong(NEXT, time);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % CHECK_EVERY == 113 && player.level() instanceof ServerLevel level) tick(level, player);
    }

    /** Every ten seconds: schedule the first one, or send the inspector when one is due and the host is home. */
    public static void tick(ServerLevel level, Player player) {
        if (!MonsterGirlsConfig.inspections() || player.isSpectator() || level.dimension() != Level.OVERWORLD || !Liaison.hasPermit(player)) return;
        BlockPos hearth = Order.ownHearth(level, player, Order.HEARTH_RANGE);
        if (hearth == null || residentsThere(level, hearth).isEmpty()) return;
        long next = next(player);
        if (next == 0) {
            setNext(player, level.getGameTime() + FIRST);
            return;
        }
        if (level.getGameTime() < next || !level.isDay() || inspectorFor(level, player) != null) return;
        if (visit(level, player, hearth) != null) setNext(player, level.getGameTime() + INTERVAL);
    }

    // --- the inspector ----------------------------------------------------------------------------------------------

    /** She comes: a liaison villager a little way off, on her way to the hearth. Who came, or null (no ground for her). */
    @Nullable
    public static Villager visit(ServerLevel level, Player player, BlockPos hearth) {
        Villager v = EntityType.VILLAGER.create(level);
        if (v == null) return null;
        double a = level.random.nextDouble() * Math.PI * 2;
        BlockPos at = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                hearth.offset((int) (Math.cos(a) * ARRIVE), 0, (int) (Math.sin(a) * ARRIVE)));
        if (Math.abs(at.getY() - hearth.getY()) > 8) at = hearth.offset(2, 0, 2);      // a cliff or a roof: by the door, then
        v.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360, 0);
        v.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        v.setVillagerData(v.getVillagerData().setProfession(ModVillagers.LIAISON.get()).setLevel(2));
        v.setVillagerXp(1);                                              // she keeps her trade
        v.setCustomName(Component.translatable("entity.monstergirls.inspector"));
        CompoundTag tag = new CompoundTag();
        tag.put(HEARTH, NbtUtils.writeBlockPos(hearth));
        tag.putUUID(OWNER, player.getUUID());
        tag.putLong(UNTIL, level.getGameTime() + LOOK);
        v.getPersistentData().put(TAG, tag);
        level.addFreshEntity(v);
        player.displayClientMessage(Component.translatable("message.monstergirls.inspection.came").withStyle(ChatFormatting.GOLD), false);
        level.playSound(null, v.blockPosition(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.NEUTRAL, 1.0F, 1.0F);
        return v;
    }

    public static boolean isInspector(Entity e) {
        return e instanceof Villager v && v.getPersistentData().contains(TAG);
    }

    @Nullable
    private static Villager inspectorFor(ServerLevel level, Player player) {
        for (Villager v : level.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(Order.HEARTH_RANGE * 2), Inspections::isInspector)) {
            CompoundTag tag = v.getPersistentData().getCompound(TAG);
            if (tag.hasUUID(OWNER) && tag.getUUID(OWNER).equals(player.getUUID())) return v;
        }
        return null;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 != 0 || !(event.getEntity() instanceof Villager v) || !(v.level() instanceof ServerLevel level) || !isInspector(v)) return;
        update(level, v);
    }

    /** Once a second: on her way, looking the home over, or on her way out. */
    public static void update(ServerLevel level, Villager v) {
        CompoundTag tag = v.getPersistentData().getCompound(TAG);
        BlockPos hearth = NbtUtils.readBlockPos(tag, HEARTH).orElse(v.blockPosition());
        if (tag.getBoolean(DONE)) {                                   // gone, once well away or her time is up
            if (level.getGameTime() > tag.getLong(UNTIL) || v.distanceToSqr(hearth.getCenter()) > 40 * 40) v.discard();
            return;
        }
        boolean there = v.distanceToSqr(hearth.getCenter()) <= NEAR * NEAR;
        if (!there && level.getGameTime() <= tag.getLong(UNTIL)) {
            v.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(hearth, 0.6F, NEAR - 1));
            return;
        }
        Player owner = tag.hasUUID(OWNER) ? level.getPlayerByUUID(tag.getUUID(OWNER)) : null;
        if (owner != null) inspect(level, v, hearth, owner);
        leave(level, v, hearth);
    }

    /** Her work done: off the way she came, and gone in a little while. */
    public static void leave(ServerLevel level, Villager v, BlockPos hearth) {
        CompoundTag tag = v.getPersistentData().getCompound(TAG);
        tag.putBoolean(DONE, true);
        tag.putLong(UNTIL, level.getGameTime() + LEAVE);
        v.getPersistentData().put(TAG, tag);
        BlockPos away = hearth.offset((int) Math.signum(v.getX() - hearth.getX() + 0.01) * 48, 0, (int) Math.signum(v.getZ() - hearth.getZ() + 0.01) * 48);
        v.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(away, 0.6F, 4));
    }

    // --- the inspection ---------------------------------------------------------------------------------------------

    /** The residents of this hearth who are there now (loaded, within AROUND). */
    public static List<LivingEntity> residentsThere(ServerLevel level, BlockPos hearth) {
        List<LivingEntity> out = new ArrayList<>();
        if (!(level.getBlockEntity(hearth) instanceof HearthBlockEntity h)) return out;
        for (UUID id : h.residents()) {
            if (level.getEntity(id) instanceof LivingEntity e && e.isAlive() && e.distanceToSqr(hearth.getCenter()) <= AROUND * AROUND) out.add(e);
        }
        return out;
    }

    /** Which checks this home passes, for this host. */
    public static List<Check> passed(ServerLevel level, BlockPos hearth, Player owner) {
        List<LivingEntity> residents = residentsThere(level, hearth);
        List<Check> out = new ArrayList<>();
        int beds = 0, food = 0;
        for (BlockPos pos : BlockPos.betweenClosed(hearth.offset(-AROUND, -4, -AROUND), hearth.offset(AROUND, 4, AROUND))) {
            BlockState s = level.getBlockState(pos);
            if (s.is(BlockTags.BEDS) && s.getValue(BedBlock.PART) == BedPart.HEAD) beds++;
            BlockEntity be = s.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            Container c = be instanceof FridgeBlockEntity f ? f.store() : be instanceof Container cc ? cc : null;
            if (c != null) for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack st = c.getItem(i);
                if (st.has(DataComponents.FOOD)) food += st.getCount();
            }
        }
        if (beds >= Math.max(1, residents.size())) out.add(Check.BEDS);
        if (food >= FOOD) out.add(Check.FOOD);
        if (residents.stream().noneMatch(e -> e.getHealth() < e.getMaxHealth() / 2 || e instanceof AbstractMonsterGirl g && g.isInjured())) out.add(Check.HEALTH);
        int sum = 0, n = 0;
        for (LivingEntity e : residents) if (e instanceof Bonded b) {
            sum += b.getAffection(owner);
            n++;
        }
        if (n > 0 && sum >= FOND * n) out.add(Check.FONDNESS);
        if (level.getEntitiesOfClass(LivingEntity.class, new AABB(hearth).inflate(24), e -> e instanceof Enemy && e.isAlive()
                && !(e instanceof Bonded)).isEmpty()) out.add(Check.SAFETY);
        return out;
    }

    public static int score(List<Check> passed) {
        return passed.size() * POINTS;
    }

    /** She looks the home over and hands the host her report, with the pay it earns. The score. */
    public static int inspect(ServerLevel level, @Nullable Villager inspector, BlockPos hearth, Player owner) {
        List<Check> passed = passed(level, hearth, owner);
        int score = score(passed);
        ItemStack report = report(owner, passed, score);
        if (!owner.getInventory().add(report)) owner.drop(report, false);
        if (score >= PASS) {
            ItemStack pay = new ItemStack(Items.EMERALD, score);
            if (!owner.getInventory().add(pay)) owner.drop(pay, false);
        }
        if (score >= MODEL) {
            owner.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, HERO, 0));
            Dairy.award(owner, "model_home");
        }
        String verdict = score >= MODEL ? "model" : score >= PASS ? "pass" : "warning";
        owner.displayClientMessage(Component.translatable("message.monstergirls.inspection." + verdict, score)
                .withStyle(score >= PASS ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        if (inspector != null) {
            inspector.getLookControl().setLookAt(owner);
            level.sendParticles(score >= PASS ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE, inspector.getX(), inspector.getY() + 2.1, inspector.getZ(), 8, 0.3, 0.2, 0.3, 0.0);
            inspector.playSound(score >= PASS ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
        }
        return score;
    }

    /** The written report: the verdict, then each check with what she saw or what to do about it. */
    public static ItemStack report(Player owner, List<Check> passed, int score) {
        String verdict = score >= MODEL ? "model" : score >= PASS ? "pass" : "warning";
        MutableComponent first = Component.translatable("report.monstergirls.inspection.head", owner.getName().getString(), score, MODEL)
                .append("\n\n").append(Component.translatable("report.monstergirls.inspection." + verdict));
        MutableComponent second = Component.empty();
        for (Check c : Check.values()) {
            String key = c.name().toLowerCase(java.util.Locale.ROOT);
            boolean ok = passed.contains(c);
            second.append(Component.literal(ok ? "+ " : "- ").withStyle(ok ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED))
                    .append(Component.translatable("report.monstergirls.inspection." + key + (ok ? ".ok" : ".bad"))).append("\n");
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Inspection report"), "Exchange Office", 0,
                List.of(Filterable.passThrough(first), Filterable.passThrough(second)), true));
        book.set(DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.inspection_report"));
        return book;
    }
}
