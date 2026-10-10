package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.registry.RaceRegistry;

/** Rules shared by transformation gameplay and its HUD. */
public final class BodyRules {
    private BodyRules() { }

    public static boolean raceNeedsFood(String raceId) {
        if (RaceRegistry.isSlime(raceId)) return false;
        var race = RaceRegistry.get(raceId).orElse(null);
        // These bodies do not have a biological digestive system.
        return race == null || !java.util.Set.of("wight", "daemon", "spirit").contains(race.lineage());
    }

    public static double correction(double target, double base, double nativeModifier) {
        return Double.isFinite(target) && Double.isFinite(base) && Double.isFinite(nativeModifier)
                ? target - base - nativeModifier : 0;
    }

    public static float rescaledHealth(float health, float previousMaximum, float newMaximum) {
        if (health <= 0 || previousMaximum <= 0 || newMaximum <= 0
                || !Float.isFinite(health) || !Float.isFinite(previousMaximum) || !Float.isFinite(newMaximum)) return 0;
        return Math.min(newMaximum, health / previousMaximum * newMaximum);
    }

    public static int barPixels(double value, double maximum, int width) {
        if (!Double.isFinite(value) || !Double.isFinite(maximum) || maximum <= 0 || width <= 0) return 0;
        return (int) Math.round(Math.clamp(value / maximum, 0, 1) * width);
    }
}
