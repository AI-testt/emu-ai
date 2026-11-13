package ains.world;

import ains.utils.RouletteStats;
import ains.utils.RouletteWheel;
import ains.world.QuestRouletteSimulator.Wheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Execution time of the selection algorithm alone (UP-11, UP-12), measured with System.nanoTime().
 * A warm-up is run first so JIT compilation does not end up in the samples. The time of spins
 * together with their log inserts comes from simulation_runs.DurationMs instead.
 */
public final class QuestRouletteBenchmark {

    public static final int WARMUP = 10_000;
    public static final int SINGLE_SAMPLES = 1_000;
    public static final int BATCHES = 30;
    public static final int BATCH_SIZE = 10_000;

    /** Keeps the JIT from discarding the spins as dead code. */
    private static volatile long sink;

    private QuestRouletteBenchmark() {
    }

    /** Times in milliseconds. */
    public record Result(RouletteStats.Summary single, RouletteStats.Summary batch, int batchSize) {
        public List<String> lines() {
            return List.of(
                    String.format(Locale.ROOT, "One selection (%,d samples after %,d warm-up): mean %.5f ms, median %.5f ms, P95 %.5f ms, max %.5f ms",
                            SINGLE_SAMPLES, WARMUP, single.mean(), single.median(), single.p95(), single.max()),
                    String.format(Locale.ROOT, "%,d selections (%d batches, algorithm only): mean %.3f ms, median %.3f ms, P95 %.3f ms, max %.3f ms",
                            batchSize, BATCHES, batch.mean(), batch.median(), batch.p95(), batch.max()));
        }
    }

    public static Result run(Wheel wheel) {
        return run(wheel, WARMUP, SINGLE_SAMPLES, BATCHES, BATCH_SIZE);
    }

    public static Result run(Wheel wheel, int warmup, int samples, int batches, int batchSize) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < wheel.weights().length; i++) slots.add(i);
        Random rng = new Random(1);
        long acc = 0;

        for (int i = 0; i < warmup; i++) acc += QuestRouletteSimulator.spinOnce(wheel, slots, rng).entry();

        double[] single = new double[samples];
        for (int i = 0; i < samples; i++) {
            long t0 = System.nanoTime();
            RouletteWheel.Spin<Integer> spin = QuestRouletteSimulator.spinOnce(wheel, slots, rng);
            single[i] = (System.nanoTime() - t0) / 1e6;
            acc += spin.entry();
        }

        double[] batch = new double[batches];
        for (int b = 0; b < batches; b++) {
            Random seeded = new Random(b + 1);
            long t0 = System.nanoTime();
            for (int i = 0; i < batchSize; i++) acc += QuestRouletteSimulator.spinOnce(wheel, slots, seeded).entry();
            batch[b] = (System.nanoTime() - t0) / 1e6;
        }
        sink = acc;
        return new Result(RouletteStats.summarize(single), RouletteStats.summarize(batch), batchSize);
    }
}
