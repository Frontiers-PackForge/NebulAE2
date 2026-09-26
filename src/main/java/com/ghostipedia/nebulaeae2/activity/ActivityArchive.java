package com.ghostipedia.nebulaeae2.activity;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.parts.AEBasePart;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

public final class ActivityArchive extends SavedData {
    public static final int PAGE_SIZE = 6;
    private final Map<UUID, Segment> segments = new HashMap<>();
    private final Map<UUID, CompoundTag> jobs = new HashMap<>();
    private long clock;
    private long previousNanos;
    private long fractionalNanos;
    private boolean ticking;
    private long lastPrune;

    public static ActivityArchive get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ActivityArchive::new, ActivityArchive::load), "nebulae_activity");
    }

    public void tick(long nowNanos, long nowMillis) {
        if (ticking) {
            long delta = Math.max(0, nowNanos - previousNanos);
            clock += delta / 1_000_000;
            fractionalNanos += delta % 1_000_000;
            clock += fractionalNanos / 1_000_000;
            fractionalNanos %= 1_000_000;
        }
        ticking = true;
        previousNanos = nowNanos;
        if (clock - lastPrune >= 60_000) {
            prune(nowMillis);
            lastPrune = clock;
        }
        setDirty();
    }

    public long clock() {
        return clock;
    }

    public void ensureSegment(UUID id) {
        segments.computeIfAbsent(id, ignored -> new Segment(clock));
        setDirty();
    }

    public void recordFlow(UUID segmentId, AEKey key, long amount, boolean incoming) {
        if (amount <= 0) return;
        ensureSegment(segmentId);
        var segment = segments.get(segmentId);
        segment.add(segment.seconds, clock / 1_000, key, amount, incoming);
        segment.add(segment.minutes, clock / 60_000, key, amount, incoming);
        setDirty();
    }

    public void start(UUID segmentId, UUID jobId, ICraftingPlan plan, IActionSource source,
            String cpuName, HolderLookup.Provider registries) {
        if (jobs.containsKey(jobId)) return;
        ensureSegment(segmentId);
        var data = new CompoundTag();
        data.putUUID("id", jobId);
        data.putUUID("segment", segmentId);
        data.putLong("started", System.currentTimeMillis());
        data.putLong("bytes", plan.bytes());
        data.put("output", GenericStack.writeTag(registries, plan.finalOutput()));
        data.putString("cpu", cpuName);
        data.putBoolean("automated", source.player().isEmpty());
        data.putString("status", "active");
        source.player().ifPresent(player -> {
            data.putUUID("player", player.getUUID());
            data.putString("requester", player.getGameProfile().getName());
        });
        if (source.player().isEmpty()) {
            source.machine().ifPresent(machine -> {
                Object owner = machine;
                var node = machine.getActionableNode();
                if (node != null) owner = node.getOwner();
                if (owner instanceof Nameable named) {
                    data.putString("requester", named.getDisplayName().getString());
                } else {
                    BlockEntity block = owner instanceof BlockEntity entity ? entity
                            : owner instanceof AEBasePart part ? part.getBlockEntity() : null;
                    if (block != null) data.putString("requester", block.getBlockState().getBlock().getName().getString());
                }
            });
        }
        data.put("materials", counters(plan.usedItems(), registries));
        data.put("emitted", counters(plan.emittedItems(), registries));
        var patterns = new ListTag();
        plan.patternTimes().forEach((pattern, count) -> {
            var entry = new CompoundTag();
            entry.put("pattern", pattern.getDefinition().toTag(registries));
            entry.putLong("executions", count);
            patterns.add(entry);
        });
        data.put("patterns", patterns);
        jobs.put(jobId, data);
        setDirty();
    }

    private static ListTag counters(KeyCounter values, HolderLookup.Provider registries) {
        var list = new ListTag();
        for (var entry : values) {
            if (entry.getLongValue() > 0) list.add(GenericStack.writeTag(registries,
                    new GenericStack(entry.getKey(), entry.getLongValue())));
        }
        return list;
    }

    public void update(UUID id, long elapsedNanos) {
        var job = jobs.get(id);
        if (job != null && job.getString("status").equals("active")) {
            job.putLong("elapsed", Math.max(job.getLong("elapsed"), elapsedNanos));
            setDirty();
        }
    }

    public void delivered(UUID id, long amount) {
        var job = jobs.get(id);
        if (job != null && amount > 0) {
            long prior = job.getLong("delivered");
            job.putLong("delivered", amount > Long.MAX_VALUE - prior ? Long.MAX_VALUE : prior + amount);
            setDirty();
        }
    }

    public void finish(UUID id, boolean success, long elapsedNanos) {
        var job = jobs.get(id);
        if (job != null) {
            update(id, elapsedNanos);
            job.putLong("finished", System.currentTimeMillis());
            job.putString("status", success ? "completed" : "canceled");
            setDirty();
        }
    }

    public CompoundTag query(Set<UUID> visible, int tab, int page, int windowIndex, String search,
            String status, UUID mine, UUID selected, int detailPage, String sort, boolean reverse,
            HolderLookup.Provider registries) {
        var result = new CompoundTag();
        result.putInt("tab", tab);
        result.putInt("page", Math.max(0, page));
        String needle = search.toLowerCase(Locale.ROOT);
        if (tab == 2) {
            queryFlow(result, visible, page, windowIndex, needle, sort, reverse, registries);
            return result;
        }
        var matching = jobs.values().stream().filter(job -> visible.contains(job.getUUID("segment")))
                .filter(job -> !ActivityRetention.expired(job.getBoolean("automated"), job.getLong("finished"), System.currentTimeMillis()))
                .filter(job -> job.getBoolean("automated") == (tab == 1))
                .filter(job -> mine == null || job.hasUUID("player") && mine.equals(job.getUUID("player")))
                .filter(job -> status.isEmpty() || status.equals(job.getString("status")))
                .filter(job -> matchesOutput(job, needle, registries))
                .sorted(Comparator.comparingLong((CompoundTag job) -> job.getLong("started")).reversed()
                        .thenComparing(job -> job.getUUID("id"))).toList();
        result.putInt("total", matching.size());
        var rows = new ListTag();
        int from = Math.min(matching.size(), Math.max(0, page) * PAGE_SIZE);
        for (int i = from; i < Math.min(from + PAGE_SIZE, matching.size()); i++) {
            var row = matching.get(i).copy();
            row.remove("materials");
            row.remove("emitted");
            row.remove("patterns");
            rows.add(row);
        }
        result.put("rows", rows);
        if (selected != null) {
            var job = jobs.get(selected);
            if (job != null && matching.contains(job)) {
                var detail = new CompoundTag();
                detail.putUUID("id", selected);
                detail.putLong("bytes", job.getLong("bytes"));
                var all = job.getList("materials", Tag.TAG_COMPOUND);
                var emitted = job.getList("emitted", Tag.TAG_COMPOUND);
                detail.putInt("total", all.size() + emitted.size());
                var materials = new ListTag();
                int start = Math.max(0, detailPage) * PAGE_SIZE;
                for (int i = start; i < Math.min(start + PAGE_SIZE, all.size() + emitted.size()); i++) {
                    var entry = (i < all.size() ? all.getCompound(i) : emitted.getCompound(i - all.size())).copy();
                    entry.putBoolean("emitted", i >= all.size());
                    materials.add(entry);
                }
                detail.put("materials", materials);
                result.put("detail", detail);
            }
        }
        return result;
    }

    private static boolean matchesOutput(CompoundTag job, String search, HolderLookup.Provider registries) {
        if (search.isEmpty()) return true;
        var stack = GenericStack.readTag(registries, job.getCompound("output"));
        return stack != null && (stack.what().getDisplayName().getString().toLowerCase(Locale.ROOT).contains(search)
                || stack.what().toString().toLowerCase(Locale.ROOT).contains(search))
                || job.getString("requester").toLowerCase(Locale.ROOT).contains(search);
    }

    private void queryFlow(CompoundTag result, Set<UUID> visible, int page, int windowIndex,
            String search, String sort, boolean reverse, HolderLookup.Provider registries) {
        long window = ActivityRetention.WINDOWS[Math.clamp(windowIndex, 0, ActivityRetention.WINDOWS.length - 1)];
        long resolution = window == 60_000 ? 1_000 : 60_000;
        long first = (Math.max(0, clock - window) + resolution - 1) / resolution;
        var totals = new HashMap<AEKey, Amounts>();
        long started = clock;
        for (var id : visible) {
            var segment = segments.get(id);
            if (segment == null) continue;
            started = Math.min(started, segment.started);
            var buckets = resolution == 1_000 ? segment.seconds : segment.minutes;
            for (var bucket : buckets.tailMap(first, true).values()) {
                bucket.forEach((key, amount) -> totals.computeIfAbsent(key, ignored -> new Amounts()).add(amount));
            }
        }
        result.putLong("coverage", ActivityRetention.coverage(clock, Math.max(started, first * resolution), window));
        result.putLong("window", window);
        result.putLong("resolution", resolution);
        var sorted = totals.entrySet().stream().filter(entry -> search.isEmpty()
                        || entry.getKey().getDisplayName().getString().toLowerCase(Locale.ROOT).contains(search)
                        || entry.getKey().toString().toLowerCase(Locale.ROOT).contains(search))
                .sorted(flowOrder(sort, reverse, registries)).toList();
        result.putInt("total", sorted.size());
        var rows = new ListTag();
        int from = Math.min(sorted.size(), Math.max(0, page) * PAGE_SIZE);
        for (int i = from; i < Math.min(from + PAGE_SIZE, sorted.size()); i++) {
            var entry = sorted.get(i);
            var row = entry.getValue().save();
            row.put("key", entry.getKey().toTagGeneric(registries));
            rows.add(row);
        }
        result.put("rows", rows);
    }

    private static Comparator<Map.Entry<AEKey, Amounts>> flowOrder(String sort, boolean reverse,
            HolderLookup.Provider registries) {
        Comparator<Map.Entry<AEKey, Amounts>> order;
        if ("name".equals(sort)) {
            order = Comparator.comparing(entry -> entry.getKey().getDisplayName().getString(),
                    String.CASE_INSENSITIVE_ORDER);
        } else {
            order = (left, right) -> {
                var leftScaled = flowValue(left.getValue(), sort)
                        .multiply(BigInteger.valueOf(right.getKey().getAmountPerUnit()));
                var rightScaled = flowValue(right.getValue(), sort)
                        .multiply(BigInteger.valueOf(left.getKey().getAmountPerUnit()));
                return rightScaled.compareTo(leftScaled);
            };
        }
        order = order.thenComparing(entry -> entry.getKey().toTagGeneric(registries).toString());
        return reverse ? order.reversed() : order;
    }

    private static BigInteger flowValue(Amounts amounts, String sort) {
        return switch (sort) {
            case "incoming" -> amounts.in;
            case "outgoing" -> amounts.out;
            case "net" -> amounts.in.subtract(amounts.out);
            default -> amounts.in.add(amounts.out);
        };
    }

    private void prune(long now) {
        jobs.values().removeIf(job -> ActivityRetention.expired(job.getBoolean("automated"), job.getLong("finished"), now));
        for (var segment : segments.values()) {
            segment.seconds.headMap(Math.max(0, clock - 60_000) / 1_000, false).clear();
            segment.minutes.headMap(Math.max(0, clock - ActivityRetention.DAY) / 60_000, false).clear();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("clock", clock);
        var history = new ListTag();
        jobs.values().forEach(job -> history.add(job.copy()));
        tag.put("jobs", history);
        var archives = new ListTag();
        segments.forEach((id, segment) -> {
            var data = new CompoundTag();
            data.putUUID("id", id);
            data.putLong("started", segment.started);
            data.put("seconds", saveBuckets(segment.seconds, registries));
            data.put("minutes", saveBuckets(segment.minutes, registries));
            archives.add(data);
        });
        tag.put("segments", archives);
        return tag;
    }

    private static ListTag saveBuckets(TreeMap<Long, Map<AEKey, Amounts>> buckets, HolderLookup.Provider registries) {
        var list = new ListTag();
        buckets.forEach((time, amounts) -> {
            var bucket = new CompoundTag();
            bucket.putLong("time", time);
            var entries = new ListTag();
            amounts.forEach((key, value) -> {
                var entry = value.save();
                entry.put("key", key.toTagGeneric(registries));
                entries.add(entry);
            });
            bucket.put("entries", entries);
            list.add(bucket);
        });
        return list;
    }

    static ActivityArchive load(CompoundTag tag, HolderLookup.Provider registries) {
        var archive = new ActivityArchive();
        archive.clock = Math.max(0, tag.getLong("clock"));
        for (var raw : tag.getList("jobs", Tag.TAG_COMPOUND)) {
            var job = (CompoundTag) raw;
            if (job.hasUUID("id") && job.hasUUID("segment")) archive.jobs.put(job.getUUID("id"), job.copy());
        }
        for (var raw : tag.getList("segments", Tag.TAG_COMPOUND)) {
            var data = (CompoundTag) raw;
            if (!data.hasUUID("id")) continue;
            var segment = new Segment(data.getLong("started"));
            loadBuckets(segment.seconds, data.getList("seconds", Tag.TAG_COMPOUND), registries);
            loadBuckets(segment.minutes, data.getList("minutes", Tag.TAG_COMPOUND), registries);
            archive.segments.put(data.getUUID("id"), segment);
        }
        archive.prune(System.currentTimeMillis());
        return archive;
    }

    private static void loadBuckets(TreeMap<Long, Map<AEKey, Amounts>> target, ListTag source, HolderLookup.Provider registries) {
        for (var raw : source) {
            var bucket = (CompoundTag) raw;
            var values = new HashMap<AEKey, Amounts>();
            for (var entryRaw : bucket.getList("entries", Tag.TAG_COMPOUND)) {
                var entry = (CompoundTag) entryRaw;
                var key = AEKey.fromTagGeneric(registries, entry.getCompound("key"));
                if (key != null) {
                    var amounts = new Amounts();
                    amounts.in = new BigInteger(entry.getString("in")).max(BigInteger.ZERO);
                    amounts.out = new BigInteger(entry.getString("out")).max(BigInteger.ZERO);
                    values.put(key, amounts);
                }
            }
            target.put(bucket.getLong("time"), values);
        }
    }

    private static final class Segment {
        private final long started;
        private final TreeMap<Long, Map<AEKey, Amounts>> seconds = new TreeMap<>();
        private final TreeMap<Long, Map<AEKey, Amounts>> minutes = new TreeMap<>();

        private Segment(long started) { this.started = started; }

        private void add(TreeMap<Long, Map<AEKey, Amounts>> buckets, long time, AEKey key, long amount, boolean incoming) {
            var values = buckets.computeIfAbsent(time, ignored -> new HashMap<>()).computeIfAbsent(key, ignored -> new Amounts());
            if (incoming) values.in = values.in.add(BigInteger.valueOf(amount));
            else values.out = values.out.add(BigInteger.valueOf(amount));
        }
    }

    private static final class Amounts {
        private BigInteger in = BigInteger.ZERO;
        private BigInteger out = BigInteger.ZERO;

        private void add(Amounts other) { in = in.add(other.in); out = out.add(other.out); }

        private CompoundTag save() {
            var tag = new CompoundTag();
            tag.putString("in", in.toString());
            tag.putString("out", out.toString());
            return tag;
        }
    }
}
