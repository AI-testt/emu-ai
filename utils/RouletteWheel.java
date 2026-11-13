package ains.utils;

import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Roulette Wheel Selection (fitness-proportionate selection, Goldberg 1989).
 * Each entry owns a slice of the wheel proportional to its weight, P(i) = w(i) / sum(w).
 * Entries with a weight of zero, a negative weight, or NaN never get a slice.
 */
public final class RouletteWheel {

    private RouletteWheel() {
    }

    /**
     * @param entry       the selected entry
     * @param random      the random number r in [0, totalWeight) that picked it
     * @param totalWeight sum of all positive weights on the wheel
     */
    public record Spin<T>(T entry, double random, double totalWeight) {
    }

    public static <T> double totalWeight(List<T> entries, ToDoubleFunction<T> weightOf) {
        double total = 0;
        for (T entry : entries) {
            total += sliceOf(entry, weightOf);
        }
        return total;
    }

    /**
     * Spins the wheel once. Returns null when no entry has a positive weight.
     */
    public static <T> Spin<T> spin(List<T> entries, ToDoubleFunction<T> weightOf, Random rng) {
        double total = totalWeight(entries, weightOf);
        if (total <= 0) return null;

        double r = rng.nextDouble() * total;
        double cumulative = 0;
        T last = null;

        for (T entry : entries) {
            double w = sliceOf(entry, weightOf);
            if (w <= 0) continue;

            cumulative += w;
            last = entry;
            // C(k-1) <= r < C(k)
            if (r < cumulative) return new Spin<>(entry, r, total);
        }

        // floating-point rounding can leave r a hair above the final cumulative sum
        return new Spin<>(last, r, total);
    }

    private static <T> double sliceOf(T entry, ToDoubleFunction<T> weightOf) {
        double w = weightOf.applyAsDouble(entry);
        return (Double.isNaN(w) || w < 0) ? 0 : w;
    }
}
