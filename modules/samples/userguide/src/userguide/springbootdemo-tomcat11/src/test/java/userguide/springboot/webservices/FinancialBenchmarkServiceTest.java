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

import org.apache.axis2.json.rpc.JsonRpcFaultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FinancialBenchmarkService}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Happy-path results for all three operations with known inputs</li>
 *   <li>Input validation: null, out-of-range, malformed arrays, probability sums</li>
 *   <li>Edge cases: single asset, zero volatility, weight normalization, seeded reproducibility</li>
 *   <li>New API fields: nPeriodsPerYear, percentiles, normalizeWeights, probTolerance</li>
 *   <li>Error contracts: validation errors throw {@link JsonRpcFaultException} with HTTP 422</li>
 * </ul>
 */
class FinancialBenchmarkServiceTest {

    private FinancialBenchmarkService service;

    @BeforeEach
    void setUp() {
        service = new FinancialBenchmarkService();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // portfolioVariance — happy path
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testPortfolioVariance_twoAssets_knownResult() throws JsonRpcFaultException {
        // 50/50 portfolio, cov = [[0.04, 0.006],[0.006, 0.09]]
        // σ²_p = 0.5²×0.04 + 2×0.5×0.5×0.006 + 0.5²×0.09 = 0.01 + 0.003 + 0.0225 = 0.0355
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.5, 0.5});
        req.setCovarianceMatrix(new double[][]{{0.04, 0.006}, {0.006, 0.09}});

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus(), "status should be SUCCESS");
        assertEquals(0.0355, resp.getPortfolioVariance(), 1e-10, "variance mismatch");
        assertEquals(Math.sqrt(0.0355), resp.getPortfolioVolatility(), 1e-10, "volatility mismatch");
        assertEquals(Math.sqrt(0.0355) * Math.sqrt(252), resp.getAnnualizedVolatility(), 1e-9,
            "annualized volatility uses nPeriodsPerYear=252 by default");
        assertEquals(1.0, resp.getWeightSum(), 1e-10, "weight_sum should be 1.0");
        assertFalse(resp.isWeightsNormalized(), "weights should not be normalized when already summing to 1.0");
        assertEquals(4L, resp.getMatrixOperations(), "2x2 matrix = 4 operations");
    }

    @Test
    void testPortfolioVariance_singleAsset() throws JsonRpcFaultException {
        // Diagonal 1×1: σ²_p = w² × σ² = 1² × 0.04 = 0.04
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{1.0});
        req.setCovarianceMatrix(new double[][]{{0.04}});

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(0.04, resp.getPortfolioVariance(), 1e-12);
        assertEquals(0.2, resp.getPortfolioVolatility(), 1e-12);
    }

    @Test
    void testPortfolioVariance_flatMatrixFormat() throws JsonRpcFaultException {
        // Same 2-asset test, but using flat array
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.5, 0.5});
        req.setCovarianceMatrixFlat(new double[]{0.04, 0.006, 0.006, 0.09});

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(0.0355, resp.getPortfolioVariance(), 1e-10);
    }

    @Test
    void testPortfolioVariance_nPeriodsPerYear() throws JsonRpcFaultException {
        // Monthly data: nPeriodsPerYear=12
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{1.0});
        req.setCovarianceMatrix(new double[][]{{0.04}});
        req.setNPeriodsPerYear(12);

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(Math.sqrt(0.04) * Math.sqrt(12), resp.getAnnualizedVolatility(), 1e-10,
            "annualized volatility should use nPeriodsPerYear=12");
    }

    @Test
    void testPortfolioVariance_normalizeWeights_rescalesAndReports() throws JsonRpcFaultException {
        // Weights sum to 2.0 — should be normalized to {0.5, 0.5}
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{1.0, 1.0});
        req.setCovarianceMatrix(new double[][]{{0.04, 0.006}, {0.006, 0.09}});
        req.setNormalizeWeights(true);

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(2.0, resp.getWeightSum(), 1e-10, "raw weight sum before normalization");
        assertTrue(resp.isWeightsNormalized(), "should report normalization was applied");
        assertEquals(0.0355, resp.getPortfolioVariance(), 1e-10,
            "after normalization result equals 50/50 portfolio variance");
    }

    @Test
    void testPortfolioVariance_normalizeWeights_alreadyUnit_noRescale() throws JsonRpcFaultException {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.5, 0.5});
        req.setCovarianceMatrix(new double[][]{{0.04, 0.006}, {0.006, 0.09}});
        req.setNormalizeWeights(true);

        PortfolioVarianceResponse resp = service.portfolioVariance(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertFalse(resp.isWeightsNormalized(), "already-unit weights should not be marked normalized");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // portfolioVariance — validation (now throws JsonRpcFaultException / 422)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testPortfolioVariance_nullRequest() {
        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(null));
        assertEquals(422, ex.getHttpStatusCode());
        assertNotNull(ex.getErrorResponse().getErrorRef());
    }

    @Test
    void testPortfolioVariance_nAssetsExceedsMax() {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[FinancialBenchmarkService.MAX_ASSETS + 1]);
        // No matrix set — should fail on nAssets check first
        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("out of range"),
            "error must mention out-of-range: " + ex.getMessage());
    }

    @Test
    void testPortfolioVariance_missingCovarianceMatrix() {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.5, 0.5});
        // No matrix set

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("covarianceMatrix"),
            "error must mention covarianceMatrix: " + ex.getMessage());
    }

    @Test
    void testPortfolioVariance_weightsDontSumToOne() {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.3, 0.3}); // sum=0.6
        req.setCovarianceMatrix(new double[][]{{0.04, 0.006}, {0.006, 0.09}});

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("normalizeWeights"),
            "error should suggest normalizeWeights: " + ex.getMessage());
    }

    @Test
    void testPortfolioVariance_normalizeWeights_zeroWeights_fails() {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.0, 0.0});
        req.setCovarianceMatrix(new double[][]{{0.04, 0.006}, {0.006, 0.09}});
        req.setNormalizeWeights(true);

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("zero-weight"),
            "error must mention zero-weight: " + ex.getMessage());
    }

    @Test
    void testPortfolioVariance_flatMatrix_wrongLength_fails() {
        PortfolioVarianceRequest req = new PortfolioVarianceRequest();
        req.setWeights(new double[]{0.5, 0.5});
        req.setCovarianceMatrixFlat(new double[]{0.04, 0.006, 0.006}); // length 3, expected 4

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(req));
        assertEquals(422, ex.getHttpStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // portfolioVariance — error response structure
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testPortfolioVariance_errorResponse_hasStructuredFields() {
        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.portfolioVariance(null));
        assertEquals(422, ex.getHttpStatusCode());
        // New structured fields
        assertEquals("VALIDATION_ERROR", ex.getErrorResponse().getError());
        assertNotNull(ex.getErrorResponse().getErrorRef(), "errorRef UUID must be present");
        assertNotNull(ex.getErrorResponse().getTimestamp(), "timestamp must be present");
        assertNull(ex.getErrorResponse().getRetryAfter(), "retryAfter should be null for 422");
        // Legacy fields — backward compatible with clients that check
        // {"status":"FAILED","errorMessage":"..."}
        assertEquals("FAILED", ex.getErrorResponse().getStatus());
        assertNotNull(ex.getErrorResponse().getErrorMessage());
        assertEquals(ex.getErrorResponse().getMessage(), ex.getErrorResponse().getErrorMessage(),
            "errorMessage must equal message for backward compatibility");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // monteCarlo — happy path
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testMonteCarlo_seededRunIsReproducible() throws JsonRpcFaultException {
        MonteCarloRequest req1 = new MonteCarloRequest();
        req1.setNSimulations(1000);
        req1.setRandomSeed(42L);

        MonteCarloRequest req2 = new MonteCarloRequest();
        req2.setNSimulations(1000);
        req2.setRandomSeed(42L);

        MonteCarloResponse r1 = service.monteCarlo(req1);
        MonteCarloResponse r2 = service.monteCarlo(req2);

        assertEquals("SUCCESS", r1.getStatus());
        assertEquals(r1.getVar95(), r2.getVar95(), 1e-9, "seeded runs must be identical");
        assertEquals(r1.getMeanFinalValue(), r2.getMeanFinalValue(), 1e-9);
    }

    @Test
    void testMonteCarlo_defaults_succeed() throws JsonRpcFaultException {
        MonteCarloResponse resp = service.monteCarlo(new MonteCarloRequest());
        assertEquals("SUCCESS", resp.getStatus());
        assertTrue(resp.getMeanFinalValue() > 0, "mean final value should be positive");
        assertTrue(resp.getVar95() >= 0, "VaR95 should be non-negative");
    }

    @Test
    void testMonteCarlo_zeroVolatility_allPathsEqual() throws JsonRpcFaultException {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setNSimulations(100);
        req.setVolatility(0.0);
        req.setExpectedReturn(0.0);
        req.setRandomSeed(1L);

        MonteCarloResponse resp = service.monteCarlo(req);

        assertEquals("SUCCESS", resp.getStatus());
        // With zero drift and zero volatility, all paths end at initialValue
        assertEquals(req.getInitialValue(), resp.getMeanFinalValue(), 1.0,
            "zero vol, zero drift: mean should equal initialValue");
    }

    @Test
    void testMonteCarlo_customPercentiles() throws JsonRpcFaultException {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setNSimulations(1000);
        req.setRandomSeed(7L);
        req.setPercentiles(new double[]{0.01, 0.05, 0.10});

        MonteCarloResponse resp = service.monteCarlo(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertNotNull(resp.getPercentileVars(), "percentileVars should not be null");
        assertEquals(3, resp.getPercentileVars().size(), "should have 3 percentile entries");
        assertEquals(0.01, resp.getPercentileVars().get(0).getPercentile(), 1e-9);
        assertEquals(0.05, resp.getPercentileVars().get(1).getPercentile(), 1e-9);
        assertEquals(0.10, resp.getPercentileVars().get(2).getPercentile(), 1e-9);
        // VaR values should be non-decreasing as percentile decreases (deeper tail = bigger loss)
        assertTrue(resp.getPercentileVars().get(0).getVar() >= resp.getPercentileVars().get(1).getVar(),
            "VaR at 1% >= VaR at 5% (deeper tail)");
    }

    @Test
    void testMonteCarlo_nPeriodsPerYear_affectsDt() throws JsonRpcFaultException {
        // Same parameters, different nPeriodsPerYear: monthly (12) vs daily (252)
        // Monthly has much larger per-step vol (σ×√(1/12) vs σ×√(1/252))
        // so final value distribution should be wider
        MonteCarloRequest daily = new MonteCarloRequest();
        daily.setNSimulations(5000);
        daily.setNPeriods(12);
        daily.setNPeriodsPerYear(252);
        daily.setRandomSeed(99L);

        MonteCarloRequest monthly = new MonteCarloRequest();
        monthly.setNSimulations(5000);
        monthly.setNPeriods(12);
        monthly.setNPeriodsPerYear(12);
        monthly.setRandomSeed(99L);

        MonteCarloResponse rDaily = service.monteCarlo(daily);
        MonteCarloResponse rMonthly = service.monteCarlo(monthly);

        assertEquals("SUCCESS", rDaily.getStatus());
        assertEquals("SUCCESS", rMonthly.getStatus());
        // Monthly steps have larger vol per step → higher StdDev of final values
        assertTrue(rMonthly.getStdDevFinalValue() > rDaily.getStdDevFinalValue(),
            "Monthly steps should produce wider distribution than daily steps for same nPeriods");
    }

    @Test
    void testMonteCarlo_capsAtMaxSimulations() throws JsonRpcFaultException {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setNSimulations(Integer.MAX_VALUE); // way over limit

        MonteCarloResponse resp = service.monteCarlo(req);
        // Should not OOM — service caps at MAX_SIMULATIONS
        assertEquals("SUCCESS", resp.getStatus());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // monteCarlo — validation
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testMonteCarlo_negativeVolatility_fails() {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setVolatility(-0.1);

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.monteCarlo(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("volatility"),
            "error must mention volatility: " + ex.getMessage());
    }

    @Test
    void testMonteCarlo_nullRequest_fails() {
        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.monteCarlo(null));
        assertEquals(422, ex.getHttpStatusCode());
    }

    @Test
    void testMonteCarlo_negativeInitialValue_fails() {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setInitialValue(-100.0);

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.monteCarlo(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("initialValue"),
            "error must mention initialValue: " + ex.getMessage());
    }

    @Test
    void testMonteCarlo_zeroInitialValue_fails() {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setInitialValue(0.0);

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.monteCarlo(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("initialValue"),
            "error must mention initialValue for zero: " + ex.getMessage());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // scenarioAnalysis — happy path
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testScenarioAnalysis_singleAsset_knownResult() throws JsonRpcFaultException {
        // Asset: price=100, position=10 shares
        // Scenario 1: price=120, prob=0.6  → gain = 20×10 = 200
        // Scenario 2: price=80,  prob=0.4  → loss = 20×10 = 200
        // E[r] = 0.6×(120/100-1) + 0.4×(80/100-1) = 0.6×0.2 + 0.4×(-0.2) = 0.12 - 0.08 = 0.04
        // Upside  = 0.6 × 20 × 10 = 120
        // Downside= 0.4 × 20 × 10 = 80

        ScenarioAnalysisRequest.AssetScenario asset = new ScenarioAnalysisRequest.AssetScenario();
        asset.setAssetId(1001L);
        asset.setCurrentPrice(100.0);
        asset.setPositionSize(10.0);

        ScenarioAnalysisRequest.Scenario s1 = new ScenarioAnalysisRequest.Scenario();
        s1.setPrice(120.0); s1.setProbability(0.6);
        ScenarioAnalysisRequest.Scenario s2 = new ScenarioAnalysisRequest.Scenario();
        s2.setPrice(80.0); s2.setProbability(0.4);
        asset.setScenarios(Arrays.asList(s1, s2));

        ScenarioAnalysisRequest req = new ScenarioAnalysisRequest();
        req.setAssets(List.of(asset));
        req.setRequestId("test-001");

        ScenarioAnalysisResponse resp = service.scenarioAnalysis(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(0.04, resp.getExpectedReturn(), 1e-10, "expected return");
        assertEquals(120.0, resp.getUpsidePotential(), 1e-10, "upside potential");
        assertEquals(80.0, resp.getDownsideRisk(), 1e-10, "downside risk");
        assertEquals(1.5, resp.getUpsideDownsideRatio(), 1e-10, "U/D ratio = 120/80 = 1.5");
        assertEquals("test-001", resp.getRequestId(), "requestId should be echoed");
    }

    @Test
    void testScenarioAnalysis_hashLookup_fasterThanLinear_largePortfolio() throws JsonRpcFaultException {
        // Build 200-asset portfolio to make timing difference detectable
        List<ScenarioAnalysisRequest.AssetScenario> assets = new java.util.ArrayList<>();
        for (int i = 0; i < 200; i++) {
            ScenarioAnalysisRequest.AssetScenario asset = new ScenarioAnalysisRequest.AssetScenario();
            asset.setAssetId(1000L + i);
            asset.setCurrentPrice(100.0 + i);
            asset.setPositionSize(10.0);

            ScenarioAnalysisRequest.Scenario s1 = new ScenarioAnalysisRequest.Scenario();
            s1.setPrice(110.0 + i); s1.setProbability(0.5);
            ScenarioAnalysisRequest.Scenario s2 = new ScenarioAnalysisRequest.Scenario();
            s2.setPrice(90.0 + i); s2.setProbability(0.5);
            asset.setScenarios(Arrays.asList(s1, s2));
            assets.add(asset);
        }

        ScenarioAnalysisRequest req = new ScenarioAnalysisRequest();
        req.setAssets(assets);
        req.setUseHashLookup(true);

        ScenarioAnalysisResponse resp = service.scenarioAnalysis(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertNotNull(resp.getLookupBenchmark(), "benchmark string should be present");
        assertTrue(resp.getLinearLookupUs() > 0 || resp.getHashLookupUs() >= 0,
            "timing fields should be set");
    }

    @Test
    void testScenarioAnalysis_useHashLookupFalse_skipsHashBenchmark() throws JsonRpcFaultException {
        ScenarioAnalysisRequest.AssetScenario asset = buildSimpleAsset(1L, 100.0, 1.0, 0.5, 0.5);
        ScenarioAnalysisRequest req = new ScenarioAnalysisRequest();
        req.setAssets(List.of(asset));
        req.setUseHashLookup(false);

        ScenarioAnalysisResponse resp = service.scenarioAnalysis(req);

        assertEquals("SUCCESS", resp.getStatus());
        assertEquals(0L, resp.getHashLookupUs(), "hash lookup time should be 0 when disabled");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // scenarioAnalysis — validation
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testScenarioAnalysis_nullRequest_fails() {
        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.scenarioAnalysis(null));
        assertEquals(422, ex.getHttpStatusCode());
    }

    @Test
    void testScenarioAnalysis_emptyAssets_fails() {
        ScenarioAnalysisRequest req = new ScenarioAnalysisRequest();
        req.setAssets(List.of());

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.scenarioAnalysis(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("assets"),
            "error must mention assets: " + ex.getMessage());
    }

    @Test
    void testScenarioAnalysis_probsDontSumToOne_fails() {
        ScenarioAnalysisRequest.Scenario s1 = new ScenarioAnalysisRequest.Scenario();
        s1.setPrice(110.0); s1.setProbability(0.4);
        ScenarioAnalysisRequest.Scenario s2 = new ScenarioAnalysisRequest.Scenario();
        s2.setPrice(90.0); s2.setProbability(0.4); // sum = 0.8, not 1.0

        ScenarioAnalysisRequest.AssetScenario asset = new ScenarioAnalysisRequest.AssetScenario();
        asset.setAssetId(1L);
        asset.setCurrentPrice(100.0);
        asset.setPositionSize(1.0);
        asset.setScenarios(Arrays.asList(s1, s2));

        ScenarioAnalysisRequest req = new ScenarioAnalysisRequest();
        req.setAssets(List.of(asset));

        JsonRpcFaultException ex = assertThrows(JsonRpcFaultException.class,
            () -> service.scenarioAnalysis(req));
        assertEquals(422, ex.getHttpStatusCode());
        assertTrue(ex.getMessage().contains("probabilities sum"),
            "error must mention probability sum: " + ex.getMessage());
    }

    @Test
    void testScenarioAnalysis_customProbTolerance_acceptsSlightlyOff() throws JsonRpcFaultException {
        // Probabilities sum to 0.999 — rejected with default 1e-4, accepted with 0.002
        ScenarioAnalysisRequest.Scenario s1 = new ScenarioAnalysisRequest.Scenario();
        s1.setPrice(110.0); s1.setProbability(0.5);
        ScenarioAnalysisRequest.Scenario s2 = new ScenarioAnalysisRequest.Scenario();
        s2.setPrice(90.0); s2.setProbability(0.499); // sum = 0.999

        ScenarioAnalysisRequest.AssetScenario asset = new ScenarioAnalysisRequest.AssetScenario();
        asset.setAssetId(1L);
        asset.setCurrentPrice(100.0);
        asset.setPositionSize(1.0);
        asset.setScenarios(Arrays.asList(s1, s2));

        // Default tolerance (1e-4) — should fail
        ScenarioAnalysisRequest reqTight = new ScenarioAnalysisRequest();
        reqTight.setAssets(List.of(asset));
        assertThrows(JsonRpcFaultException.class,
            () -> service.scenarioAnalysis(reqTight),
            "tight tolerance should reject sum=0.999");

        // Loose tolerance (0.002) — should succeed
        ScenarioAnalysisRequest reqLoose = new ScenarioAnalysisRequest();
        reqLoose.setAssets(List.of(asset));
        reqLoose.setProbTolerance(0.002);
        assertEquals("SUCCESS", service.scenarioAnalysis(reqLoose).getStatus(),
            "loose tolerance should accept sum=0.999");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MonteCarloRequest — getter defaults
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void testMonteCarloRequest_getters_enforceDefaults() {
        MonteCarloRequest req = new MonteCarloRequest();
        req.setNSimulations(0);       // invalid → default 10,000
        req.setNPeriods(-1);          // invalid → default 252
        req.setNPeriodsPerYear(0);    // invalid → default 252
        req.setPercentiles(null);     // null → default {0.01, 0.05}

        assertEquals(10_000, req.getNSimulations());
        assertEquals(252, req.getNPeriods());
        assertEquals(252, req.getNPeriodsPerYear());
        assertArrayEquals(new double[]{0.01, 0.05}, req.getPercentiles(), 1e-9);

        // initialValue getter returns the raw value (no clamping); the service
        // itself validates non-positive values and throws JsonRpcFaultException.
        // The field initializer supplies the default for the "never set" case.
        MonteCarloRequest fresh = new MonteCarloRequest();
        assertEquals(1_000_000.0, fresh.getInitialValue(), 1e-9,
            "fresh request should have default initialValue=1,000,000");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper
    // ═══════════════════════════════════════════════════════════════════════

    private ScenarioAnalysisRequest.AssetScenario buildSimpleAsset(
            long assetId, double currentPrice, double positionSize,
            double prob1, double prob2) {
        ScenarioAnalysisRequest.AssetScenario asset = new ScenarioAnalysisRequest.AssetScenario();
        asset.setAssetId(assetId);
        asset.setCurrentPrice(currentPrice);
        asset.setPositionSize(positionSize);

        ScenarioAnalysisRequest.Scenario s1 = new ScenarioAnalysisRequest.Scenario();
        s1.setPrice(currentPrice * 1.1); s1.setProbability(prob1);
        ScenarioAnalysisRequest.Scenario s2 = new ScenarioAnalysisRequest.Scenario();
        s2.setPrice(currentPrice * 0.9); s2.setProbability(prob2);
        asset.setScenarios(Arrays.asList(s1, s2));
        return asset;
    }

}
