package com.ghostipedia.nebulaeae2.activity;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

final class ActivityHistoryIndex {
    private final Map<UUID, Set<UUID>> parents = new HashMap<>();

    UUID branch(UUID previous) {
        var id = UUID.randomUUID();
        parents.put(id, previous == null ? Set.of() : Set.of(previous));
        return id;
    }

    UUID importLegacy(Set<UUID> segments) {
        if (segments.isEmpty()) return null;
        var bytes = ByteBuffer.allocate(16 + 16 * segments.size());
        bytes.putLong(0x6e6562756c616541L).putLong(0x32686973746f7279L);
        segments.stream().sorted().forEach(id -> bytes.putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()));
        var id = UUID.nameUUIDFromBytes(bytes.array());
        parents.putIfAbsent(id, Set.copyOf(segments));
        return id;
    }

    UUID merge(UUID current, UUID incoming) {
        if (incoming == null || incoming.equals(current)) return current;
        if (current == null || contains(incoming, current)) return incoming;
        if (contains(current, incoming)) return current;
        var id = UUID.randomUUID();
        parents.put(id, Set.of(current, incoming));
        return id;
    }

    private boolean contains(UUID head, UUID target) {
        return walk(head, target).contains(target);
    }

    Set<UUID> resolve(UUID head) {
        return walk(head, null);
    }

    private Set<UUID> walk(UUID head, UUID stop) {
        var visited = new HashSet<UUID>();
        if (head == null) return visited;
        var pending = new ArrayDeque<UUID>();
        pending.add(head);
        while (!pending.isEmpty()) {
            var id = pending.removeLast();
            if (!visited.add(id)) continue;
            if (id.equals(stop)) break;
            pending.addAll(parents.getOrDefault(id, Set.of()));
        }
        return visited;
    }

    ListTag save() {
        var result = new ListTag();
        parents.forEach((id, ancestors) -> {
            var entry = new CompoundTag();
            entry.putUUID("id", id);
            var links = new ListTag();
            ancestors.forEach(parent -> links.add(NbtUtils.createUUID(parent)));
            entry.put("parents", links);
            result.add(entry);
        });
        return result;
    }

    void load(ListTag saved) {
        for (var raw : saved) {
            var entry = (CompoundTag) raw;
            if (!entry.hasUUID("id")) continue;
            var links = new HashSet<UUID>();
            for (var parent : entry.getList("parents", Tag.TAG_INT_ARRAY)) {
                try {
                    links.add(NbtUtils.loadUUID(parent));
                } catch (IllegalArgumentException ignored) {
                }
            }
            parents.put(entry.getUUID("id"), Set.copyOf(links));
        }
    }
}
