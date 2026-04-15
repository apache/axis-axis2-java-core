/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package userguide.springboot.webservices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Java reference implementation of the Axis2/C Financial Benchmark Service.
 *
 * <p>Provides identical financial calculations and API to the C service in
 * {@code axis-axis2-c-core/samples/user_guide/financial-benchmark-service},
 * enabling side-by-side performance comparison on the same hardware:
 *
 * <table border="1">
 *   <tr><th>Operation</th><th>Complexity</th><th>C edge-device (2 GB)</th><th>Java enterprise minimum</th></tr>
 *   <tr><td>portfolioVariance (500 assets)</td><td>O(n²)</td><td>~5 ms / 30 MB</td><td>requires 16–32 GB JVM</td></tr>
 *   <tr><td>monteCarlo (10k sims)</td><td>O(sims × periods)</td><td>~100 ms</td><td>requires 16–32 GB JVM</td></tr>
 *   <tr><td>scenarioAnalysis (1000 assets)</td><td>O(n) linear / O(1) hash</td><td>&lt;5 ms</td><td>requires 16–32 GB JVM</td></tr>
 * </table>
 *
 * <p><i>Note on the memory delta:</i> the C column reflects raw working-set
 * size on an embedded Linux target.  The Java column reflects a typical
 * production JVM baseline (Tomcat/WildFly + Spring Boot + Axis2 Databinding
 * (ADB) + JIT code cache + GC headroom + Axiom/Axis2 libraries) before the
 * service does any
 * allocation of its own.  Both implementations perform the same financial
 * math on similar primitive arrays; the difference is almost entirely
 * fixed JVM/container overhead that a tuned server could reduce with
 * {@code -XX:MinRAMPercentage}, alternative GCs (ZGC, Shenandoah), or
 * native-image builds.  The numbers above are for baseline-configured
 * deployments, not a tuned ceiling.
 *
 * <h3>Operations</h3>
 * <ul>
 *   <li>{@link #portfolioVariance} — O(n²) covariance matrix: σ²_p = Σ_i Σ_j w_i·w_j·σ_ij</li>
 *   <li>{@link #monteCarlo} — GBM simulation for VaR: S(t+dt) = S(t)·exp((μ−σ²/2)·dt + σ·√dt·Z)</li>
 *   <li>{@link #scenarioAnalysis} — expected return + HashMap vs ArrayList lookup benchmark</li>
 * </ul>
 *
 * <h3>API parity with C implementation</h3>
 * All request/response fields match the C structs in {@code financial_benchmark_service.h}:
 * normalizeWeights, nPeriodsPerYear, percentiles, probTolerance.
 */
@Component
public class FinancialBenchmarkService {

    private static final Logger logger = LogManager.getLogger(FinancialBenchmarkService.class);

    /** Maximum assets accepted to bound memory allocation (matches C FINBENCH_MAX_ASSETS). */
    public static final int MAX_ASSETS = 2_000;

    /** Maximum Monte Carlo paths (matches C FINBENCH_MAX_SIMULATIONS). */
    public static final int MAX_SIMULATIONS = 1_000_000;

    /** Maximum scenario count per asset (matches C FINBENCH_MAX_SCENARIOS). */
    public static final int MAX_SCENARIOS = 10;

    /** Maximum number of percentile levels in a Monte Carlo request. */
    public static final int MAX_PERCENTILES = 8;

    // ── Portfolio Variance ────────────────────────────────────────────────────

    /**
     * Calculates portfolio variance using O(n²) covariance matrix multiplication.
     *
     * <p>Formula: σ²_p = Σ_i Σ_j w_i · w_j · σ_ij
     *
     * <p>Input validation mirrors the C implementation: weight count must match
     * n_assets, covariance matrix must be n×n, weights must sum to 1.0 (unless
     * {@code normalizeWeights=true}).
     *
     * <p>Intuition for readers new to portfolio math:
     *   A portfolio of N assets has N individual volatilities (how much each
     *   asset moves) AND N×N pairwise correlations (how they move together).
     *   The covariance matrix packages both: cov[i][j] = vol_i · vol_j · corr_ij.
     *   Portfolio variance is not just the weighted average of individual
     *   variances — correlation effects dominate.  Negative correlations
     *   reduce portfolio risk (diversification benefit); correlations that
     *   converge to 1.0 during crises make "diversified" portfolios behave
     *   like a single asset.
     *
     * <p>Numerical notes preserved in this implementation:
     *   - Variance can become slightly negative from floating-point
     *     cancellation in the O(n²) accumulator; clamp to zero before sqrt
     *     (see {@link #portfolioVariance} body).
     *   - Non-positive-semi-definite covariance matrices silently yield
     *     {@code portfolioVariance ≈ 0} after clamping.  Callers SHOULD
     *     validate PSD upstream (e.g., check that the smallest eigenvalue
     *     is >= -tolerance) rather than trusting a zero result.
     *
     * <p>Time basis (IMPORTANT — frequency consistency):
     *   The covariance matrix MUST be expressed on the same time basis as
     *   the period implied by {@code nPeriodsPerYear}.  The output field
     *   {@code annualizedVolatility = sqrt(w'Σw) · sqrt(nPeriodsPerYear)}
     *   assumes the input Σ is <em>per-period</em> covariance (e.g., daily
     *   covariance with nPeriodsPerYear=252, weekly with 52, monthly with
     *   12).  If a caller submits an <em>already-annualized</em> matrix
     *   while leaving nPeriodsPerYear=252 at its default, the reported
     *   annualized vol is inflated by √252 ≈ 15.9× and is meaningless.
     *   There is no way for the service to detect this — annualized and
     *   per-period matrices are both valid PSD covariance matrices — so
     *   callers must self-enforce the convention.  If your covariance is
     *   already annualized, pass {@code nPeriodsPerYear=1} and read
     *   {@code portfolioVolatility} (which equals the annualized vol in
     *   that case) instead of {@code annualizedVolatility}.
     */
    public PortfolioVarianceResponse portfolioVariance(PortfolioVarianceRequest request) {
        String uuid = UUID.randomUUID().toString();
        String logPrefix = "FinancialBenchmarkService.portfolioVariance uuid=" + uuid + " ";

        if (request == null || request.getWeights() == null) {
            return PortfolioVarianceResponse.failed(
                "Missing required field: \"weights\" array (nAssets elements summing to 1.0).");
        }

        int n = request.getNAssets();
        if (n <= 0 || n > MAX_ASSETS) {
            String err = "n_assets=" + n + " is out of range [1, " + MAX_ASSETS + "].";
            logger.warn("{} validation failed: {}", logPrefix, err);
            return PortfolioVarianceResponse.failed(err);
        }

        // ── Resolve covariance matrix ─────────────────────────────────────────
        double[][] cov = resolveCovarianceMatrix(request, n);
        if (cov == null) {
            String err = "Missing or malformed \"covarianceMatrix\": provide a " + n + "×" + n +
                " 2D array or a flat array of " + (long) n * n + " elements in row-major order.";
            logger.warn("{} validation failed: {}", logPrefix, err);
            return PortfolioVarianceResponse.failed(err);
        }
        if (cov.length != n) {
            String err = "covarianceMatrix row count " + cov.length + " != nAssets " + n + ".";
            logger.warn("{} validation failed: {}", logPrefix, err);
            return PortfolioVarianceResponse.failed(err);
        }
        for (int i = 0; i < n; i++) {
            if (cov[i] == null || cov[i].length != n) {
                String err = "covarianceMatrix row " + i + " has " +
                    (cov[i] == null ? 0 : cov[i].length) + " columns, expected " + n + ".";
                logger.warn("{} validation failed: {}", logPrefix, err);
                return PortfolioVarianceResponse.failed(err);
            }
        }

        // ── Weight validation / normalization ─────────────────────────────────
        double[] weights = Arrays.copyOf(request.getWeights(), n);
        double weightSum = 0.0;
        for (double w : weights) weightSum += w;

        // Weights are fractions of portfolio value per asset (e.g.,
        //   [0.25, 0.25, 0.20, 0.15, 0.15] for a 5-asset equal-weight-ish
        //   portfolio).  They MUST sum to 1.0 for the math to represent a
        //   real portfolio.
        //
        // Edge cases documented for callers:
        //   - Zero-sum weights (e.g., [0.5, 0.5, -0.5, -0.5], a long-short
        //     portfolio with zero net exposure) CANNOT use normalizeWeights=true.
        //     The service rejects this because dividing by sum=0 is undefined.
        //     Callers with zero-sum portfolios should compute normalized
        //     weights client-side and pass normalizeWeights=false.  The
        //     portfolio variance formula w'Σw is still well-defined for
        //     zero-sum weights; only the normalization step is ill-defined.
        //   - Gross exposure is lost under normalization: submitting
        //     [1.3, 0.3] (sum 1.6, 160% gross leverage) becomes
        //     [0.8125, 0.1875] after normalization (100% gross).  A caller
        //     expecting to measure leveraged risk will instead measure
        //     un-leveraged risk.  This is inherent to normalization, not a
        //     service bug, but callers should be aware.
        boolean weightsNormalized = false;
        if (request.isNormalizeWeights()) {
            if (weightSum <= 0.0) {
                String err = "normalizeWeights=true but weights sum to " + weightSum +
                    ". Cannot normalize a zero-weight portfolio.";
                logger.warn("{} validation failed: {}", logPrefix, err);
                return PortfolioVarianceResponse.failed(err);
            }
            if (Math.abs(weightSum - 1.0) > 1e-10) {
                for (int i = 0; i < n; i++) weights[i] /= weightSum;
                weightsNormalized = true;
                logger.info(logPrefix + "normalized weights (sum was " + weightSum + ")");
            }
        } else {
            if (Math.abs(weightSum - 1.0) > 1e-4) {
                String err = "weights sum to " + String.format("%.8f", weightSum) +
                    ", expected 1.0 (tolerance 1e-4). " +
                    "Pass normalizeWeights=true to rescale automatically.";
                logger.warn("{} validation failed: {}", logPrefix, err);
                return PortfolioVarianceResponse.failed(err);
            }
        }

        // ── O(n²) variance calculation ────────────────────────────────────────
        logger.info(logPrefix + "starting O(n²) variance for " + n + " assets");
        long startNs = System.nanoTime();

        double variance = 0.0;
        long ops = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                variance += weights[i] * weights[j] * cov[i][j];
                ops++;
            }
        }

        long elapsedUs = (System.nanoTime() - startNs) / 1_000;

        // Clamp negative variance from floating-point cancellation before sqrt.
        //
        // Two independent causes can produce variance < 0 here:
        //   1. Rounding error in the O(n²) accumulator when correlations are
        //      strongly negative and weights are large.  Common, harmless,
        //      magnitude typically 1e-16 to 1e-12.
        //   2. A non-positive-semi-definite input covariance matrix.  This is
        //      a math error — a real covariance matrix is PSD by construction.
        //      Non-PSD inputs can arise from mixed-frequency estimation,
        //      shrinkage that went too far, or copy-paste errors in client
        //      code.  The magnitude can be arbitrarily large (e.g., -0.02).
        //
        // Clamping treats both cases identically: report vol = 0 rather than
        // NaN from sqrt(negative).  This is safe for case 1 but silently
        // masks case 2.  The service documentation advises callers to
        // validate PSD upstream.  Alternative: replace the clamp with an
        // explicit error when |variance| > tolerance and variance < 0,
        // which would force callers to fix upstream data errors.  Kept as
        // a clamp here to match the C reference implementation.
        if (variance < 0.0) variance = 0.0;
        double volatility = Math.sqrt(variance);
        int npy = request.getNPeriodsPerYear();

        PortfolioVarianceResponse response = new PortfolioVarianceResponse();
        response.setStatus("SUCCESS");
        response.setPortfolioVariance(variance);
        response.setPortfolioVolatility(volatility);
        response.setAnnualizedVolatility(volatility * Math.sqrt(npy));
        response.setWeightSum(weightSum);
        response.setWeightsNormalized(weightsNormalized);
        response.setCalcTimeUs(elapsedUs);
        response.setMatrixOperations(ops);
        response.setOpsPerSecond(elapsedUs > 0 ? ops / (elapsedUs / 1_000_000.0) : 0);
        response.setMemoryUsedMb(heapUsedMb());
        response.setRuntimeInfo(runtimeInfo());
        if (request.getRequestId() != null) response.setRequestId(request.getRequestId());

        logger.info(logPrefix + "completed: n=" + n + " variance=" +
                String.format("%.6f", variance) + " elapsed=" + elapsedUs + "us ops=" + ops);
        return response;
    }

    // ── Monte Carlo ───────────────────────────────────────────────────────────

    /**
     * Runs a Monte Carlo VaR simulation using Geometric Brownian Motion.
     *
     * <p>S(t+dt) = S(t) × exp((μ − σ²/2)·dt + σ·√dt·Z), Z ~ N(0,1)
     *
     * <p>Intuition for readers new to GBM:
     *   GBM is the standard "a stock moves continuously in log-returns
     *   with constant drift μ and constant volatility σ" assumption.
     *   Each time step adds a normally-distributed shock scaled by
     *   σ·√dt plus a deterministic drift.  The {@code −σ²/2} correction
     *   (Itô's lemma) ensures that E[S(T)] = S(0)·exp(μ·T); without it,
     *   the expected value drifts upward with higher volatility.  This
     *   correction is the single most common bug in home-grown GBM code
     *   and should be preserved verbatim in any re-implementation.
     *
     * <p>Sampling behavior:
     *   Uses {@link Random#nextGaussian()} (polar method) for normal variates.
     *   When {@code randomSeed != 0}, a seeded {@link Random} is used for
     *   reproducibility (same seed → bit-identical output).  When
     *   {@code randomSeed == 0}, a fresh unseeded instance gives
     *   non-deterministic results.
     *
     *   Warning for cross-implementation reproducibility: {@code java.util.Random}
     *   uses a linear congruential generator (LCG).  An implementation in a
     *   different language or using a different PRNG (e.g., xorshift128+,
     *   PCG64, NumPy's default) will produce DIFFERENT numbers for the
     *   SAME seed.  Reproducibility is per-PRNG, not cross-PRNG.
     *
     * <p>Numerical edge cases:
     *   - <b>Guarded in the body below:</b> the variance accumulator
     *     {@code (sumSqFinal/nSims - mean²)} can go slightly negative from
     *     floating-point cancellation; it is clamped to 0.0 before the final
     *     sqrt so the reported stdDev is never NaN.
     *   - <b>NOT guarded (caller responsibility):</b> {@code Math.exp()} can
     *     overflow to {@code +Infinity} when the exponent exceeds ~709 (the
     *     natural log of {@code Double.MAX_VALUE}).  For typical equity
     *     parameters (σ ≤ 40%, horizons ≤ 1 year) this is unreachable, but
     *     extreme stress scenarios (pandemic-era σ ≈ 90%, multi-year
     *     horizons) can approach it.  If an Infinity appears in
     *     {@code finalValues[]}, the downstream sum/sort becomes NaN and the
     *     entire result is corrupted.  This service deliberately does NOT
     *     clamp the exponent — silently capping would mask an unrealistic
     *     input and produce a confidently wrong VaR.  Callers running
     *     extreme scenarios must validate inputs upstream (e.g., reject
     *     {@code σ·√T > ~20}).
     */
    public MonteCarloResponse monteCarlo(MonteCarloRequest request) {
        String uuid = UUID.randomUUID().toString();
        String logPrefix = "FinancialBenchmarkService.monteCarlo uuid=" + uuid + " ";

        if (request == null) {
            return MonteCarloResponse.failed("Request must not be null.");
        }

        int nSims = Math.min(request.getNSimulations(), MAX_SIMULATIONS);
        int nPeriods = request.getNPeriods();
        double initialValue = request.getInitialValue();
        double mu = request.getExpectedReturn();
        double sigma = request.getVolatility();
        int npy = request.getNPeriodsPerYear();

        // GBM is defined only for strictly positive price paths: the process
        // evolves multiplicatively via exp(...), so S(0) must be > 0 or every
        // S(t) collapses to 0 and all VaR / CVaR statistics are degenerate.
        // The request POJO applies a default when missing, but an explicit
        // zero or negative value reaches here via direct field access paths
        // and must be rejected at the service boundary.
        if (initialValue <= 0.0) {
            return MonteCarloResponse.failed(
                "initialValue must be > 0 (GBM is undefined for non-positive starting values).");
        }
        if (sigma < 0.0) {
            return MonteCarloResponse.failed("volatility must be >= 0.");
        }

        // ── Pre-computed GBM constants ────────────────────────────────────────
        // dt         — length of one time step in years (e.g., 1/252 for one
        //              trading day when nPeriodsPerYear = 252)
        // drift      — (μ − σ²/2)·dt.  The (−σ²/2) term is the Itô correction
        //              that keeps E[S(T)] = S(0)·exp(μ·T).  Do not drop it.
        // volSqrtDt  — σ·√dt.  Scales each standard-normal shock by the
        //              one-step standard deviation.
        double dt = 1.0 / npy;
        double drift = (mu - 0.5 * sigma * sigma) * dt;
        double volSqrtDt = sigma * Math.sqrt(dt);

        // ── PRNG: seeded for reproducibility, unseeded for production ─────────
        Random rng = request.getRandomSeed() != 0
            ? new Random(request.getRandomSeed())
            : new Random();

        logger.info(logPrefix + "starting " + nSims + " sims × " + nPeriods + " periods" +
                " (seed=" + request.getRandomSeed() + ", npy=" + npy + ")");

        double[] finalValues = new double[nSims];
        double sumFinal = 0.0;
        double sumSqFinal = 0.0;
        int profitCount = 0;
        double maxDrawdown = 0.0;

        long startNs = System.nanoTime();

        for (int sim = 0; sim < nSims; sim++) {
            double value = initialValue;
            double peak = value;
            double simMaxDrawdown = 0.0;

            for (int period = 0; period < nPeriods; period++) {
                double z = rng.nextGaussian();
                value *= Math.exp(drift + volSqrtDt * z);

                if (value > peak) {
                    peak = value;
                } else {
                    double dd = (peak - value) / peak;
                    if (dd > simMaxDrawdown) simMaxDrawdown = dd;
                }
            }

            finalValues[sim] = value;
            sumFinal += value;
            sumSqFinal += value * value;
            if (value > initialValue) profitCount++;
            if (simMaxDrawdown > maxDrawdown) maxDrawdown = simMaxDrawdown;
        }

        long elapsedUs = (System.nanoTime() - startNs) / 1_000;

        // ── Statistics ────────────────────────────────────────────────────────
        double mean = sumFinal / nSims;
        double variance = (sumSqFinal / nSims) - (mean * mean);
        if (variance < 0.0) variance = 0.0;

        // Sort ascending so finalValues[0] is the worst outcome and
        // finalValues[nSims-1] is the best.  VaR and CVaR then become
        // simple index lookups: the p-th percentile loss corresponds to
        // finalValues[floor(p * nSims)].
        Arrays.sort(finalValues);

        int idx5  = (int)(0.05 * nSims);
        int idx1  = (int)(0.01 * nSims);

        // Sample median of a sorted array: for odd N take the middle element,
        // for even N average the two central elements.  Using a single index
        // (e.g., nSims/2) is only an approximation for even N and can produce
        // small reconciliation differences against NumPy/R, which both
        // implement the average-of-two rule.
        double median = (nSims % 2 == 0)
            ? (finalValues[nSims / 2 - 1] + finalValues[nSims / 2]) / 2.0
            : finalValues[nSims / 2];

        // CVaR95 (a.k.a. Expected Shortfall at 95%): the arithmetic mean of
        // the {@code idx5} worst final values after ascending sort.  In
        // This is a common discrete-sample estimator for Expected Shortfall
        // E[L | L ≥ VaR₉₅] — representing the average loss in the worst 5%
        // of simulated outcomes.  For a continuous distribution the two are
        // identical; on a finite sample they can differ slightly depending
        // on how many observations fall exactly at the VaR threshold.
        //
        // Estimator detail: this averages the floor(0.05 · nSims) WORST
        // observations, i.e., positions 0 through idx5-1 (inclusive).  For
        // a large nSims this matches the textbook definition to within one
        // observation.  Callers reconciling against another risk system
        // should be aware that different estimators (e.g., one that
        // averages L values that strictly exceed VaR rather than the
        // bottom k outcomes) can give minutely different numbers.
        double cvarSum = 0.0;
        for (int i = 0; i < idx5; i++) cvarSum += finalValues[i];
        double cvar95 = (idx5 > 0) ? (cvarSum / idx5) : finalValues[0];

        // Caller-specified percentile VaR
        List<MonteCarloResponse.PercentileVar> percentileVars = new ArrayList<>();
        if (request.getPercentiles() != null) {
            int maxPct = Math.min(request.getPercentiles().length, MAX_PERCENTILES);
            for (int pi = 0; pi < maxPct; pi++) {
                double p = request.getPercentiles()[pi];
                if (p <= 0.0 || p >= 1.0) continue;
                int idx = (int)(p * nSims);
                if (idx >= nSims) idx = nSims - 1;
                percentileVars.add(new MonteCarloResponse.PercentileVar(
                    p, initialValue - finalValues[idx]));
            }
        }

        MonteCarloResponse response = new MonteCarloResponse();
        response.setStatus("SUCCESS");
        response.setMeanFinalValue(mean);
        response.setMedianFinalValue(median);
        response.setStdDevFinalValue(Math.sqrt(variance));
        // Sign convention: VaR and CVaR are returned as POSITIVE LOSS
        // MAGNITUDES in base-currency units.  So {@code var95 = $252,000}
        // means "worst-5% outcome is a $252,000 loss from initialValue",
        // not "portfolio value of $252,000" and not "-$252,000".
        // Convention mirrors the Basel / regulatory reporting standard.
        response.setVar95(initialValue - finalValues[idx5]);
        response.setVar99(initialValue - finalValues[idx1]);
        response.setCvar95(initialValue - cvar95);
        response.setMaxDrawdown(maxDrawdown);
        response.setProbProfit((double) profitCount / nSims);
        response.setPercentileVars(percentileVars);
        response.setCalcTimeUs(elapsedUs);
        response.setSimulationsPerSecond(elapsedUs > 0 ? nSims / (elapsedUs / 1_000_000.0) : 0);
        response.setMemoryUsedMb(heapUsedMb());
        if (request.getRequestId() != null) response.setRequestId(request.getRequestId());

        logger.info(logPrefix + "completed: " + nSims + " sims in " + elapsedUs + "us" +
                " VaR95=" + String.format("%.2f", response.getVar95()) +
                " sims/sec=" + String.format("%.0f", response.getSimulationsPerSecond()));
        return response;
    }

    // ── Scenario Analysis ─────────────────────────────────────────────────────

    /**
     * Computes probability-weighted portfolio scenario analysis and benchmarks
     * {@code HashMap} O(1) lookup against {@code ArrayList} O(n) linear scan.
     *
     * <p>Financial formulas per asset:
     * <ul>
     *   <li>E[r_i] = Σ_j p_j × (price_j / currentPrice − 1)</li>
     *   <li>Upside_i = Σ_j p_j × max(0, price_j − currentPrice) × positionSize</li>
     *   <li>Downside_i = Σ_j p_j × max(0, currentPrice − price_j) × positionSize</li>
     * </ul>
     * Portfolio E[r] = Σ_i (E[r_i] × positionValue_i) / Σ_i positionValue_i
     *
     * <p>Intuition for readers new to scenario analysis:
     *   Scenario analysis is a "what if" tool, not a statistical forecast.
     *   Given a small, discrete set of future price outcomes for each asset
     *   (typically 3-5: base / bull / bear / crash / etc.) each with an
     *   assigned probability, we compute the probability-weighted average
     *   return.  This is distinct from Monte Carlo, which samples from a
     *   continuous distribution rather than a fixed scenario set.
     *
     *   Upside and downside are separated so the caller can see the
     *   asymmetry of the distribution independently — a symmetric scenario
     *   set around the current price yields equal upside and downside,
     *   while a long-tailed distribution (common in tech stocks) shows
     *   larger upside than downside at the same probability weight.
     *
     * <p>The HashMap-vs-linear-scan benchmark is an implementation timing
     * study (O(1) vs O(n) lookup cost) and is independent of the financial
     * math.  It exists to give callers real numbers for the data-structure
     * choice when building their own scenario analysis pipelines.
     */
    public ScenarioAnalysisResponse scenarioAnalysis(ScenarioAnalysisRequest request) {
        String uuid = UUID.randomUUID().toString();
        String logPrefix = "FinancialBenchmarkService.scenarioAnalysis uuid=" + uuid + " ";

        if (request == null || request.getAssets() == null || request.getAssets().isEmpty()) {
            return ScenarioAnalysisResponse.failed(
                "Missing required field: \"assets\" array. " +
                "Each entry must have assetId, currentPrice, positionSize, and scenarios " +
                "[{price, probability}].");
        }

        List<ScenarioAnalysisRequest.AssetScenario> assets = request.getAssets();
        int nAssets = assets.size();
        if (nAssets > MAX_ASSETS) {
            String err = "assets count " + nAssets + " exceeds maximum " + MAX_ASSETS + ".";
            logger.warn("{} validation failed: {}", logPrefix, err);
            return ScenarioAnalysisResponse.failed(err);
        }

        double probTolerance = request.getProbTolerance();

        // ── Step 1: Input validation (fail fast on bad assets) ────────────────
        // Validates every asset up-front and fails with a specific error
        // rather than silently skipping assets during the financial
        // calculation.  Silent skipping would produce a portfolio
        // aggregate that omits the invalid asset without any indication
        // to the caller — a dangerous default for a risk system.
        //
        // Per-asset requirements:
        //   - currentPrice > 0    — the return formula (price_j / currentPrice
        //                           − 1) is undefined for non-positive
        //                           currentPrice; skipping would corrupt
        //                           portfolio-level aggregates.
        //   - at least one scenario — required to compute a probability-
        //                             weighted expectation; an empty
        //                             scenario list is never a valid model.
        //   - probabilities sum to 1.0 within {@code probTolerance} — a
        //     non-unit sum is usually a data entry error (e.g., three
        //     scenarios at 0.3 each summing to 0.9).  The default
        //     tolerance of 1e-4 accommodates JSON round-trip precision
        //     loss but rejects genuine mistakes.
        for (int i = 0; i < nAssets; i++) {
            ScenarioAnalysisRequest.AssetScenario asset = assets.get(i);

            if (asset.getCurrentPrice() <= 0.0) {
                String err = String.format(
                    "Asset index %d (id=%d): currentPrice=%.8f is not positive. " +
                    "Scenario analysis computes return as (price / currentPrice − 1), " +
                    "which is undefined for non-positive currentPrice.",
                    i, asset.getAssetId(), asset.getCurrentPrice());
                logger.warn("{} validation failed: {}", logPrefix, err);
                return ScenarioAnalysisResponse.failed(err);
            }

            if (asset.getScenarios() == null || asset.getScenarios().isEmpty()) {
                String err = String.format(
                    "Asset index %d (id=%d): scenarios array is missing or empty. " +
                    "At least one scenario {price, probability} is required.",
                    i, asset.getAssetId());
                logger.warn("{} validation failed: {}", logPrefix, err);
                return ScenarioAnalysisResponse.failed(err);
            }

            if (asset.getScenarios().size() > MAX_SCENARIOS) {
                String err = String.format(
                    "Asset index %d (id=%d): scenarios count %d exceeds maximum %d. " +
                    "Coalesce low-probability outcomes to stay within the cap.",
                    i, asset.getAssetId(),
                    asset.getScenarios().size(), MAX_SCENARIOS);
                logger.warn("{} validation failed: {}", logPrefix, err);
                return ScenarioAnalysisResponse.failed(err);
            }

            double probSum = 0.0;
            for (ScenarioAnalysisRequest.Scenario s : asset.getScenarios()) {
                probSum += s.getProbability();
            }
            if (Math.abs(probSum - 1.0) > probTolerance) {
                String err = String.format(
                    "Asset index %d (id=%d): scenario probabilities sum to %.8f, " +
                    "expected 1.0 (tolerance %.2g). " +
                    "All %d scenario probabilities must sum to exactly 1.0. " +
                    "Pass probTolerance to adjust validation strictness.",
                    i, asset.getAssetId(), probSum, probTolerance,
                    asset.getScenarios().size());
                logger.warn("{} validation failed: {}", logPrefix, err);
                return ScenarioAnalysisResponse.failed(err);
            }
        }

        // ── Step 2: Financial computation ─────────────────────────────────────
        long calcStartNs = System.nanoTime();

        double portfolioExpectedReturn = 0.0;
        double totalPositionValue = 0.0;
        double portfolioWeightedValue = 0.0;
        double totalUpside = 0.0;
        double totalDownside = 0.0;

        // Step 1 above guarantees all assets have currentPrice > 0 and a
        // non-empty scenarios list, so no per-asset skip is needed here.
        for (ScenarioAnalysisRequest.AssetScenario asset : assets) {
            double assetExpectedReturn = 0.0;
            double assetWeightedValue = 0.0;
            double assetUpside = 0.0;
            double assetDownside = 0.0;

            for (ScenarioAnalysisRequest.Scenario scenario : asset.getScenarios()) {
                double p = scenario.getProbability();
                double price = scenario.getPrice();
                double ret = (price / asset.getCurrentPrice()) - 1.0;

                assetExpectedReturn += p * ret;
                assetWeightedValue  += p * price * asset.getPositionSize();

                if (price > asset.getCurrentPrice()) {
                    assetUpside   += p * (price - asset.getCurrentPrice()) * asset.getPositionSize();
                } else if (price < asset.getCurrentPrice()) {
                    assetDownside += p * (asset.getCurrentPrice() - price) * asset.getPositionSize();
                }
            }

            double positionValue = asset.getCurrentPrice() * asset.getPositionSize();
            portfolioExpectedReturn += assetExpectedReturn * positionValue;
            totalPositionValue      += positionValue;
            portfolioWeightedValue  += assetWeightedValue;
            totalUpside             += assetUpside;
            totalDownside           += assetDownside;
        }

        if (totalPositionValue > 0.0) {
            portfolioExpectedReturn /= totalPositionValue;
        }

        long calcElapsedUs = (System.nanoTime() - calcStartNs) / 1_000;

        // ── Step 3: O(n) linear scan benchmark ───────────────────────────────
        int nLookups = nAssets * 10;
        long linearStart = System.nanoTime();
        long linearFound = 0;

        for (int q = 0; q < nLookups; q++) {
            long targetId = assets.get(q % nAssets).getAssetId();
            for (ScenarioAnalysisRequest.AssetScenario asset : assets) {
                if (asset.getAssetId() == targetId) {
                    linearFound++;
                    break;
                }
            }
        }
        long linearUs = (System.nanoTime() - linearStart) / 1_000;

        // ── Step 4: O(1) HashMap benchmark ────────────────────────────────────
        long hashBuildUs = 0;
        long hashLookupUs = 0;
        long hashFound = 0;

        if (request.isUseHashLookup()) {
            long buildStart = System.nanoTime();
            Map<Long, ScenarioAnalysisRequest.AssetScenario> hashMap =
                new HashMap<>(nAssets * 2);
            for (ScenarioAnalysisRequest.AssetScenario asset : assets) {
                hashMap.put(asset.getAssetId(), asset);
            }
            hashBuildUs = (System.nanoTime() - buildStart) / 1_000;

            long lookupStart = System.nanoTime();
            for (int q = 0; q < nLookups; q++) {
                long targetId = assets.get(q % nAssets).getAssetId();
                if (hashMap.get(targetId) != null) hashFound++;
            }
            hashLookupUs = (System.nanoTime() - lookupStart) / 1_000;
        }

        // ── Build response ─────────────────────────────────────────────────────
        ScenarioAnalysisResponse response = new ScenarioAnalysisResponse();
        response.setStatus("SUCCESS");
        response.setExpectedReturn(portfolioExpectedReturn);
        response.setWeightedValue(portfolioWeightedValue);
        response.setUpsidePotential(totalUpside);
        response.setDownsideRisk(totalDownside);
        // Upside/downside ratio edge cases:
        //   downside > 0              → finite positive ratio (the common path)
        //   downside ≈ 0, upside > 0  → Double.POSITIVE_INFINITY (all-upside
        //                               portfolio; returning 0.0 here would
        //                               falsely imply "no upside")
        //   downside ≈ 0, upside ≈ 0  → Double.NaN (no variance either way;
        //                               the ratio is genuinely undefined)
        // The 1e-9 threshold avoids treating floating-point residue from the
        // probability-weighted sums as a real non-zero downside.
        // Note on JSON: Jackson serializes these as "Infinity" / "NaN" which
        // are valid JavaScript Number literals but NOT strict JSON per RFC
        // 8259. Clients that parse with strict libraries should configure
        // their parser or map these to nulls.
        double udRatio;
        if (totalDownside > 1e-9) {
            udRatio = totalUpside / totalDownside;
        } else if (totalUpside > 1e-9) {
            udRatio = Double.POSITIVE_INFINITY;
        } else {
            udRatio = Double.NaN;
        }
        response.setUpsideDownsideRatio(udRatio);
        response.setCalcTimeUs(calcElapsedUs);
        response.setLinearLookupUs(linearUs);
        response.setHashLookupUs(hashLookupUs);
        response.setHashBuildUs(hashBuildUs);
        response.setLookupSpeedup(
            hashLookupUs > 0 ? (double) linearUs / hashLookupUs : Double.NaN);
        response.setLookupsPerformed(nLookups);
        response.setMemoryUsedMb(heapUsedMb());

        String benchmarkSummary = String.format(
            "linear_us=%d hash_lookup_us=%d speedup=%.1fx hash_build_us=%d " +
            "(linear=%d found, hash=%d found, n_assets=%d, n_lookups=%d)",
            linearUs, hashLookupUs,
            hashLookupUs > 0 ? (double) linearUs / hashLookupUs : 0.0,
            hashBuildUs, linearFound, hashFound, nAssets, nLookups);
        response.setLookupBenchmark(benchmarkSummary);

        if (request.getRequestId() != null) response.setRequestId(request.getRequestId());

        logger.info(logPrefix + "completed: " + nAssets + " assets " +
                nLookups + " lookups — linear=" + linearUs + "us " +
                "hash_lookup=" + hashLookupUs + "us " +
                "speedup=" + String.format("%.1f", response.getLookupSpeedup()) + "x " +
                "E[r]=" + String.format("%.4f", portfolioExpectedReturn));
        return response;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /**
     * Resolves the covariance matrix from a request, preferring the 2D form.
     * Returns null if neither form is present or if the flat array has wrong length.
     */
    private double[][] resolveCovarianceMatrix(PortfolioVarianceRequest request, int n) {
        if (request.getCovarianceMatrix() != null) {
            return request.getCovarianceMatrix();
        }
        double[] flat = request.getCovarianceMatrixFlat();
        if (flat == null) return null;
        if (flat.length != (long) n * n) return null;

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(flat, i * n, matrix[i], 0, n);
        }
        return matrix;
    }

    /** JVM heap in use at call time, in MB. */
    private long heapUsedMb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    /**
     * Runtime identifier for response metadata.
     * Reports the JVM family and heap class without exposing version numbers
     * or precise memory configuration (which would aid fingerprinting).
     */
    private String runtimeInfo() {
        Runtime rt = Runtime.getRuntime();
        long maxMb = rt.maxMemory() / (1024 * 1024);
        // Heap tier, not exact size — enough for C vs JVM comparison context
        String heapTier = maxMb >= 16_000 ? "16+ GB" :
                          maxMb >=  8_000 ? "8+ GB"  :
                          maxMb >=  4_000 ? "4+ GB"  :
                          maxMb >=  2_000 ? "2+ GB"  : "< 2 GB";
        return "Java (JVM heap tier: " + heapTier + ")";
    }
}
