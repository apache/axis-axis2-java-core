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

/**
 * Response for scenario analysis and hash-vs-linear lookup benchmark.
 *
 * <p>Financial results (expected return, upside, downside) are always computed.
 * Benchmark fields ({@code hashLookupUs}, {@code linearLookupUs}, {@code lookupSpeedup})
 * are populated when {@code useHashLookup=true}.
 */
public class ScenarioAnalysisResponse {

    private String status;
    private String errorMessage;

    // ── Financial results ─────────────────────────────────────────────────────

    /**
     * Portfolio-level expected return: position-value-weighted average of
     * per-asset expected returns. E[r_i] = Σ_j (p_j × (price_j / currentPrice - 1)).
     */
    private double expectedReturn;

    /**
     * Probability-weighted portfolio value: Σ_asset Σ_scenario (p_j × price_j × positionSize).
     */
    private double weightedValue;

    /**
     * Upside potential in currency units: Σ (p_j × max(0, price_j − currentPrice) × positionSize).
     */
    private double upsidePotential;

    /**
     * Downside risk in currency units: Σ (p_j × max(0, currentPrice − price_j) × positionSize).
     */
    private double downsideRisk;

    /**
     * Upside/downside ratio.
     * <ul>
     *   <li>Finite positive value — the common case; &gt; 1 means more
     *       expected upside than downside.</li>
     *   <li>{@link Double#POSITIVE_INFINITY} — all-upside portfolio
     *       (downside is effectively zero, upside is positive).</li>
     *   <li>{@link Double#NaN} — both sides effectively zero (ratio
     *       genuinely undefined).</li>
     * </ul>
     * Note: Jackson serializes these as {@code "Infinity"} / {@code "NaN"}
     * string tokens, which are valid JavaScript Number literals but NOT
     * strict JSON per RFC 8259. Clients using strict JSON parsers should
     * configure their parser or map these to null before parsing.
     */
    private double upsideDownsideRatio;

    // ── Benchmark results ─────────────────────────────────────────────────────

    /** Wall-clock time for O(n) linear scan benchmark in microseconds */
    private long linearLookupUs;

    /** Wall-clock time for O(1) HashMap lookup benchmark in microseconds */
    private long hashLookupUs;

    /** Wall-clock time to build the HashMap in microseconds (amortized in real workloads) */
    private long hashBuildUs;

    /** Speedup: linearLookupUs / hashLookupUs. NaN when hash benchmark was skipped. */
    private double lookupSpeedup;

    /** Total lookup operations performed in each benchmark */
    private int lookupsPerformed;

    /**
     * Human-readable benchmark summary:
     * linear/hash times, speedup, found counts, asset and lookup counts.
     */
    private String lookupBenchmark;

    // ── Performance metadata ──────────────────────────────────────────────────

    /** Wall-clock time for the financial computation (separate from lookup benchmark) */
    private long calcTimeUs;

    /** JVM heap used at response time in MB */
    private long memoryUsedMb;

    /** Echoed from request */
    private String requestId;

    // ── Factory ──────────────────────────────────────────────────────────────

    public static ScenarioAnalysisResponse failed(String errorMessage) {
        ScenarioAnalysisResponse r = new ScenarioAnalysisResponse();
        r.status = "FAILED";
        r.errorMessage = errorMessage;
        return r;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(double expectedReturn) { this.expectedReturn = expectedReturn; }

    public double getWeightedValue() { return weightedValue; }
    public void setWeightedValue(double weightedValue) { this.weightedValue = weightedValue; }

    public double getUpsidePotential() { return upsidePotential; }
    public void setUpsidePotential(double upsidePotential) { this.upsidePotential = upsidePotential; }

    public double getDownsideRisk() { return downsideRisk; }
    public void setDownsideRisk(double downsideRisk) { this.downsideRisk = downsideRisk; }

    public double getUpsideDownsideRatio() { return upsideDownsideRatio; }
    public void setUpsideDownsideRatio(double upsideDownsideRatio) { this.upsideDownsideRatio = upsideDownsideRatio; }

    public long getLinearLookupUs() { return linearLookupUs; }
    public void setLinearLookupUs(long linearLookupUs) { this.linearLookupUs = linearLookupUs; }

    public long getHashLookupUs() { return hashLookupUs; }
    public void setHashLookupUs(long hashLookupUs) { this.hashLookupUs = hashLookupUs; }

    public long getHashBuildUs() { return hashBuildUs; }
    public void setHashBuildUs(long hashBuildUs) { this.hashBuildUs = hashBuildUs; }

    public double getLookupSpeedup() { return lookupSpeedup; }
    public void setLookupSpeedup(double lookupSpeedup) { this.lookupSpeedup = lookupSpeedup; }

    public int getLookupsPerformed() { return lookupsPerformed; }
    public void setLookupsPerformed(int lookupsPerformed) { this.lookupsPerformed = lookupsPerformed; }

    public String getLookupBenchmark() { return lookupBenchmark; }
    public void setLookupBenchmark(String lookupBenchmark) { this.lookupBenchmark = lookupBenchmark; }

    public long getCalcTimeUs() { return calcTimeUs; }
    public void setCalcTimeUs(long calcTimeUs) { this.calcTimeUs = calcTimeUs; }

    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
