package com.gabriel.tensuranewworld.integration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Clear only transient addon state; persisted progress belongs to each player's save. */
public final class GreatSageRuntimeScope {
    private static final List<String> FIELDS = List.of(
            "server.RaphaelEvolutionService#PENDING", "server.RaphaelEvolutionService#EVOLVING",
            "server.BattleMode#ACTIVE", "server.AwakeningWatcher#SNAPSHOTS", "server.AwakeningWatcher#SHIELDED",
            "server.AssessTracker#WATCHING", "server.TipWatcher#STATES", "server.TipWatcher#LAST_ANY",
            "server.RimuruBirth#RECENT", "GreatSageGameRules#SENT");
    private GreatSageRuntimeScope() { }
    public static void clearAll() { clear(null); }
    public static void clearPlayer(UUID id) { clear(id); }
    private static void clear(UUID id) {
        for (String reference : FIELDS) {
            String[] parts = reference.split("#");
            try {
                var field = Class.forName("dev.dedek.greatsage." + parts[0]).getDeclaredField(parts[1]);
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof Map<?, ?> map) { if (id == null) map.clear(); else map.remove(id); }
                else if (value instanceof Collection<?> entries) { if (id == null) entries.clear(); else entries.remove(id); }
            } catch (ClassNotFoundException ignored) {
                // The addon is optional.
            } catch (ReflectiveOperationException | LinkageError error) {
                org.slf4j.LoggerFactory.getLogger(GreatSageRuntimeScope.class).warn("Could not clear transient sage state {}", reference, error);
            }
        }
    }
}
