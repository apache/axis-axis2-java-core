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
 * Request for Monte Carlo Value-at-Risk simulation.
 *
 * <p>Simulates portfolio value paths using Geometric Brownian Motion:
 * <pre>S(t+dt) = S(t) × exp((μ − σ²/2)·dt + σ·√dt·Z)</pre>
 * where dt = 1/nPeriodsPerYear and Z ~ N(0,1).
 *
 * <p>All fields have defaults so a minimal {@code {}} request body is valid.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * {
 *   "nSimulations": 50000,
 *   "nPeriods": 252,
 *   "initialValue": 1000000.0,
 *   "expectedReturn": 0.08,
 *   "volatility": 0.20,
 *   "randomSeed": 42,
 *   "percentiles": [0.01, 0.05, 0.10]
 * }
 * }</pre>
 */
public class MonteCarloRequest {

    /** Number of simulation paths. Default: 10,000. Max: 1,000,000. */
    private int nSimulations = 10_000;

    /** Number of time steps per path (e.g., 252 trading days). Default: 252. */
    private int nPeriods = 252;

    /** Initial portfolio value in currency units. Default: $1,000,000. */
    private double initialValue = 1_000_000.0;

    /** Expected annualized return (e.g., 0.08 for 8%). Default: 0.08. */
    private double expectedReturn = 0.08;

    /** Annualized volatility (e.g., 0.20 for 20%). Default: 0.20. */
    private double volatility = 0.20;

    /**
     * Random seed for reproducibility. 0 (default) → non-deterministic.
     * Seeded runs produce identical results across calls, enabling diff testing.
     */
    private long randomSeed = 0;

    /**
     * Trading periods per year. Controls GBM time step: dt = 1/nPeriodsPerYear.
     * Must match the frequency basis of {@code expectedReturn} and {@code volatility}
     * (both must be annualized). Default: 252. Common alternatives: 260, 365, 12.
     */
    private int nPeriodsPerYear = 252;

    /**
     * Percentile tail levels for VaR reporting. Values in (0, 1).
     * Each entry p produces: VaR_p = initialValue − sorted_final_values[p × nSimulations].
     * Default: [0.01, 0.05] (99% and 95% VaR). Up to 8 entries; extras are ignored.
     */
    private double[] percentiles = {0.01, 0.05};

    /** Optional identifier echoed in the response for request tracing. */
    private String requestId;

    // ── getters ──────────────────────────────────────────────────────────────

    public int getNSimulations() { return nSimulations; }
    public int getNPeriods() { return nPeriods; }
    public double getInitialValue() { return initialValue; }
    public double getExpectedReturn() { return expectedReturn; }
    public double getVolatility() { return volatility; }
    public long getRandomSeed() { return randomSeed; }
    public int getNPeriodsPerYear() { return nPeriodsPerYear > 0 ? nPeriodsPerYear : 252; }
    public double[] getPercentiles() { return percentiles; }
    public String getRequestId() { return requestId; }

    // ── setters ──────────────────────────────────────────────────────────────

    public void setNSimulations(int nSimulations) { this.nSimulations = nSimulations; }
    public void setNPeriods(int nPeriods) { this.nPeriods = nPeriods; }
    public void setInitialValue(double initialValue) { this.initialValue = initialValue; }
    public void setExpectedReturn(double expectedReturn) { this.expectedReturn = expectedReturn; }
    public void setVolatility(double volatility) { this.volatility = volatility; }
    public void setRandomSeed(long randomSeed) { this.randomSeed = randomSeed; }
    public void setNPeriodsPerYear(int nPeriodsPerYear) { this.nPeriodsPerYear = nPeriodsPerYear; }
    public void setPercentiles(double[] percentiles) { this.percentiles = percentiles; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
