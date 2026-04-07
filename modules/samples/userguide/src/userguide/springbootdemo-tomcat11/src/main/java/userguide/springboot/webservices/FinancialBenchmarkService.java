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

        // Clamp negative variance from floating-point cancellation before sqrt
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
     * <p>Uses {@link Random#nextGaussian()} (polar method) for normal variates.
     * When {@code randomSeed != 0}, a seeded {@link Random} is used for reproducibility.
     * When {@code randomSeed == 0}, a fresh unseeded instance gives non-deterministic results.
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

        if (sigma < 0.0) {
            return MonteCarloResponse.failed("volatility must be >= 0.");
        }

        // ── Pre-computed GBM constants ────────────────────────────────────────
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

        Arrays.sort(finalValues);

        int idx5  = (int)(0.05 * nSims);
        int idx1  = (int)(0.01 * nSims);
        int idx50 = nSims / 2;

        // CVaR: mean of worst 5%
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
        response.setMedianFinalValue(finalValues[idx50]);
        response.setStdDevFinalValue(Math.sqrt(variance));
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

        // ── Step 1: Probability validation ────────────────────────────────────
        for (int i = 0; i < nAssets; i++) {
            ScenarioAnalysisRequest.AssetScenario asset = assets.get(i);
            if (asset.getScenarios() == null || asset.getScenarios().isEmpty()) continue;

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

        for (ScenarioAnalysisRequest.AssetScenario asset : assets) {
            if (asset.getCurrentPrice() <= 0.0 ||
                    asset.getScenarios() == null || asset.getScenarios().isEmpty()) continue;

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
        response.setUpsideDownsideRatio(
            totalDownside > 0.0 ? totalUpside / totalDownside : 0.0);
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
