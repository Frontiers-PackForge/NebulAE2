package com.ghostipedia.nebulaeae2.activity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ActivityDelivery {
    private static final ThreadLocal<ArrayDeque<Scope>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);

    private ActivityDelivery() {}

    public static Scope begin() {
        var scope = new Scope();
        SCOPES.get().push(scope);
        return scope;
    }

    public static void standalone(ActivityArchive archive, UUID id, long amount) {
        var scopes = SCOPES.get();
        if (!scopes.isEmpty()) scopes.peek().claims.add(new Claim(archive, id, amount));
    }

    public static void consumed(long amount) {
        var scopes = SCOPES.get();
        if (!scopes.isEmpty()) scopes.peek().consumed += amount;
    }

    public static final class Scope implements AutoCloseable {
        private final List<Claim> claims = new ArrayList<>();
        private long consumed;

        public void accepted(long amount) {
            long remaining = Math.max(0, amount - consumed);
            for (var claim : claims) {
                long credited = Math.min(remaining, claim.amount);
                claim.archive.delivered(claim.id, credited);
                remaining -= credited;
            }
        }

        @Override
        public void close() {
            var scopes = SCOPES.get();
            scopes.pop();
            if (scopes.isEmpty()) SCOPES.remove();
        }
    }

    private record Claim(ActivityArchive archive, UUID id, long amount) {}
}
