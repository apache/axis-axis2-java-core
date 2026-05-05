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

import java.util.List;

/**
 * Request for scenario analysis and hash-vs-linear lookup benchmark.
 *
 * <p>Computes probability-weighted expected return, upside, and downside
 * for a portfolio under multiple price scenarios. Also benchmarks
 * {@code HashMap} O(1) lookup against {@code ArrayList} O(n) scan,
 * a common optimization pattern in portfolio analysis systems that
 * handle 500+ assets.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * {
 *   "assets": [{
 *     "assetId": 1001,
 *     "currentPrice": 150.00,
 *     "positionSize": 100.0,
 *     "scenarios": [
 *       {"price": 165.0, "probability": 0.40},
 *       {"price": 150.0, "probability": 0.35},
 *       {"price": 130.0, "probability": 0.25}
 *     ]
 *   }],
 *   "useHashLookup": true,
 *   "probTolerance": 0.0001
 * }
 * }</pre>
 */
public class ScenarioAnalysisRequest {

    /** List of assets with scenario prices and probabilities. Required. */
    private List<AssetScenario> assets;

    /**
     * When {@code true} (default), the service benchmarks both {@code HashMap}
     * and {@code ArrayList} lookups and reports the speedup ratio.
     * When {@code false}, only the linear scan is timed.
     */
    private boolean useHashLookup = true;

    /**
     * Tolerance for probability sum validation per asset.
     * Each asset's scenario probabilities must sum to 1.0 within this tolerance.
     * Default: 1e-4 (0.01%). Passing 0.0 or any negative value falls back to
     * this default. Positive values are capped at 0.1 (10%) to prevent a
     * too-loose tolerance from hiding real probability-sum bugs.
     * Loosen (e.g., 0.001) when aggregating externally-sourced probabilities
     * that carry rounding error; keep tight to catch genuinely miscounted scenarios.
     */
    private double probTolerance = 1e-4;

    /** Optional identifier echoed in the response for request tracing. */
    private String requestId;

    // ── Inner types ──────────────────────────────────────────────────────────

    /**
     * A single asset in the portfolio with associated scenario data.
     */
    public static class AssetScenario {

        /** Unique asset identifier (e.g., a security ID). */
        private long assetId;

        /** Current market price in currency units. Must be > 0. */
        private double currentPrice;

        /** Position size in shares/units. Used to scale upside/downside to dollar terms. */
        private double positionSize;

        /**
         * Scenario outcomes. Probabilities must sum to 1.0 (within {@code probTolerance}).
         * Up to {@link FinancialBenchmarkService#MAX_SCENARIOS} entries.
         */
        private List<Scenario> scenarios;

        public long getAssetId() { return assetId; }
        public void setAssetId(long assetId) { this.assetId = assetId; }

        public double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

        public double getPositionSize() { return positionSize; }
        public void setPositionSize(double positionSize) { this.positionSize = positionSize; }

        public List<Scenario> getScenarios() { return scenarios; }
        public void setScenarios(List<Scenario> scenarios) { this.scenarios = scenarios; }
    }

    /**
     * A single price scenario for an asset.
     */
    public static class Scenario {

        /** Target price in this scenario (currency units). */
        private double price;

        /** Probability weight in [0, 1]. All scenarios for an asset must sum to 1.0. */
        private double probability;

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public List<AssetScenario> getAssets() { return assets; }
    public void setAssets(List<AssetScenario> assets) { this.assets = assets; }

    public boolean isUseHashLookup() { return useHashLookup; }
    public void setUseHashLookup(boolean useHashLookup) { this.useHashLookup = useHashLookup; }

    public double getProbTolerance() {
        if (probTolerance <= 0.0) return 1e-4;
        return Math.min(probTolerance, 0.1);
    }
    public void setProbTolerance(double probTolerance) { this.probTolerance = probTolerance; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
