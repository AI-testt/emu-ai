package ains.utils;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Statistics for the roulette tests (subbab 2.2.7): Chi-Square goodness of fit (Rumus 2.4),
 * relative error, MAE and RMSE (2.5), Shannon entropy (2.6), standardized residuals (2.8),
 * Chi-Square homogeneity and Holm-Bonferroni for the per-seed tests.
 */
public final class RouletteStats {

    private RouletteStats() {
    }

    public record ChiSquare(double statistic, int degreesOfFreedom, double pValue) {
        public boolean rejects(double alpha) {
            return pValue <= alpha;
        }
    }

    /** Probabilities P(i) = w(i) / sum(w); non-positive weights get 0. */
    public static double[] probabilities(double[] weights) {
        double total = 0;
        for (double w : weights) total += w > 0 ? w : 0;
        double[] p = new double[weights.length];
        for (int i = 0; i < weights.length; i++) p[i] = weights[i] > 0 ? weights[i] / total : 0;
        return p;
    }

    public static long sum(long[] observed) {
        long n = 0;
        for (long o : observed) n += o;
        return n;
    }

    /** Rumus 2.4 against probabilities p; categories with p = 0 are left out of the test. */
    public static ChiSquare goodnessOfFit(double[] p, long[] observed) {
        long n = sum(observed);
        double chi = 0;
        int k = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] <= 0) continue;
            double e = n * p[i];
            double d = observed[i] - e;
            chi += d * d / e;
            k++;
        }
        return of(chi, k - 1);
    }

    /**
     * Chi-Square homogeneity on an r x c table (rows = samples, columns = items).
     * Columns that are empty in every row are left out.
     */
    public static ChiSquare homogeneity(long[][] table) {
        int rows = table.length, cols = table[0].length;
        long[] rowSum = new long[rows];
        long[] colSum = new long[cols];
        long total = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rowSum[r] += table[r][c];
                colSum[c] += table[r][c];
                total += table[r][c];
            }
        }
        double chi = 0;
        int usedCols = 0;
        for (int c = 0; c < cols; c++) {
            if (colSum[c] == 0) continue;
            usedCols++;
            for (int r = 0; r < rows; r++) {
                double e = (double) rowSum[r] * colSum[c] / total;
                double d = table[r][c] - e;
                chi += d * d / e;
            }
        }
        return of(chi, (rows - 1) * (usedCols - 1));
    }

    /** Rumus 2.8: z(i) = (O - E) / sqrt(N p (1 - p)); 0 for p = 0 or p = 1. */
    public static double[] zScores(double[] p, long[] observed) {
        long n = sum(observed);
        double[] z = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            double sd = Math.sqrt(n * p[i] * (1 - p[i]));
            z[i] = sd > 0 ? (observed[i] - n * p[i]) / sd : 0;
        }
        return z;
    }

    /** Rumus 2.5: largest |O - E| / E over items with p > 0. */
    public static double maxRelativeError(double[] p, long[] observed) {
        long n = sum(observed);
        double max = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] <= 0) continue;
            double e = n * p[i];
            max = Math.max(max, Math.abs(observed[i] - e) / e);
        }
        return max;
    }

    /** Rumus 2.5: mean |O/N - P(i)|, as a proportion (multiply by 100 for percentage points). */
    public static double mae(double[] p, long[] observed) {
        long n = sum(observed);
        double s = 0;
        for (int i = 0; i < p.length; i++) s += Math.abs((double) observed[i] / n - p[i]);
        return s / p.length;
    }

    /** Rumus 2.5: sqrt(mean (O/N - P(i))^2), as a proportion. */
    public static double rmse(double[] p, long[] observed) {
        long n = sum(observed);
        double s = 0;
        for (int i = 0; i < p.length; i++) {
            double d = (double) observed[i] / n - p[i];
            s += d * d;
        }
        return Math.sqrt(s / p.length);
    }

    /** Rumus 2.6 on probabilities q, in bits. */
    public static double entropyBits(double[] q) {
        double h = 0;
        for (double x : q) if (x > 0) h -= x * Math.log(x) / Math.log(2);
        return h;
    }

    public static double entropyBits(long[] observed) {
        long n = sum(observed);
        double[] q = new double[observed.length];
        for (int i = 0; i < q.length; i++) q[i] = (double) observed[i] / n;
        return entropyBits(q);
    }

    /** H / log2(k); 1 when there is a single category. */
    public static double normalize(double entropyBits, int k) {
        return k > 1 ? entropyBits / (Math.log(k) / Math.log(2)) : 1;
    }

    /**
     * Standard deviation of the observed entropy for n draws (delta method):
     * sqrt((sum p (log2 p)^2 - H^2) / n). Used for the entropy tolerance in UP-05.
     */
    public static double entropyStandardDeviation(double[] p, long n) {
        double h = entropyBits(p);
        double s = 0;
        for (double x : p) if (x > 0) {
            double l = Math.log(x) / Math.log(2);
            s += x * l * l;
        }
        return Math.sqrt(Math.max(s - h * h, 0) / n);
    }

    /** Two-sided Bonferroni bound for k simultaneous z tests, e.g. 2.807 for k = 10 and alpha 0.05. */
    public static double bonferroniZ(int k, double alpha) {
        return new NormalDistribution().inverseCumulativeProbability(1 - alpha / (2.0 * k));
    }

    /**
     * Holm-Bonferroni step-down (Holm, 1979). Returns, per hypothesis, whether it is rejected
     * at family-wise level alpha.
     */
    public static boolean[] holm(double[] pValues, double alpha) {
        int m = pValues.length;
        Integer[] order = IntStream.range(0, m).boxed().toArray(Integer[]::new);
        Arrays.sort(order, Comparator.comparingDouble(i -> pValues[i]));
        boolean[] rejected = new boolean[m];
        for (int k = 0; k < m; k++) {
            int i = order[k];
            if (pValues[i] > alpha / (m - k)) break;
            rejected[i] = true;
        }
        return rejected;
    }

    public record Summary(double mean, double median, double p95, double max) {
    }

    /** Mean, median, nearest-rank P95 and maximum. */
    public static Summary summarize(double[] values) {
        double[] v = values.clone();
        Arrays.sort(v);
        int n = v.length;
        double mean = 0;
        for (double x : v) mean += x;
        mean /= n;
        double median = n % 2 == 1 ? v[n / 2] : (v[n / 2 - 1] + v[n / 2]) / 2;
        double p95 = v[(int) Math.ceil(0.95 * n) - 1];
        return new Summary(mean, median, p95, v[n - 1]);
    }

    private static ChiSquare of(double chi, int df) {
        double p = df > 0 ? 1 - new ChiSquaredDistribution(df).cumulativeProbability(chi) : 1;
        return new ChiSquare(chi, df, p);
    }
}
