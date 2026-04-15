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
 *
 * <h3>Field units and frequency consistency (important)</h3>
 * <ul>
 *   <li>{@code expectedReturn}, {@code volatility} — annualized decimals
 *     (0.08 = 8% per year, NOT 8).  These are the μ and σ of the GBM log-return
 *     process on an annual basis.</li>
 *   <li>{@code nPeriodsPerYear} — the number of time steps per year used to
 *     de-annualize μ and σ for the simulation step {@code dt = 1 / nPeriodsPerYear}.
 *     Picks the discretization granularity, not the input frequency (inputs
 *     are always annualized — see above).  For equity daily simulation: 252
 *     (US trading days) or 260 (calendar-day-ish convention).  For weekly:
 *     52.  For monthly: 12.  Mismatching the granularity with {@code nPeriods}
 *     (e.g., nPeriodsPerYear=1 with nPeriods=252 to simulate a trading year)
 *     silently produces a mathematically different VaR — the horizon becomes
 *     252 years of annual steps rather than one year of daily steps.</li>
 *   <li>{@code nPeriods} — number of time steps in the simulation horizon.
 *     Horizon in years = {@code nPeriods / nPeriodsPerYear}.  The default
 *     (nPeriods=252, nPeriodsPerYear=252) simulates one full trading year.
 *     For a 1-day VaR, use nPeriods=1.  For a 10-day regulatory VaR, use
 *     nPeriods=10.</li>
 *   <li>{@code initialValue} — portfolio starting value in base currency.
 *     Must be &gt; 0; GBM is undefined for non-positive initial values
 *     because it evolves multiplicatively through exp(...).</li>
 * </ul>
 *
 * <h3>Reproducibility</h3>
 * <p>Setting a non-zero {@code randomSeed} makes the run deterministic
 * AGAINST THIS IMPLEMENTATION (Java {@link java.util.Random}).  The same
 * seed will not produce the same output under other PRNGs — xorshift128+,
 * PCG64, Mersenne Twister, NumPy's Generator — so seeded reproducibility
 * is per-backend, not cross-backend.  Callers comparing VaR numbers
 * across systems should drive both from the same PRNG or compare
 * distributions rather than individual paths.
 *
 * <h3>Convergence behavior</h3>
 * <p>Monte Carlo VaR estimates have sampling error that decreases as
 * 1/√nSimulations.  Rough guidance for equity-style parameters
 * (vol ≈ 0.20, 1-year horizon):
 * <ul>
 *   <li>1,000 sims — coarse ±5% on the VaR estimate; good for smoke tests</li>
 *   <li>10,000 sims — ±1.5%; adequate for dashboards</li>
 *   <li>100,000 sims — ±0.5%; suitable for point estimates and
 *     model-vs-model comparisons</li>
 *   <li>1,000,000 sims — ±0.15%; use for regulatory reporting or when
 *     the tail probability itself is very small (e.g., 99.9% VaR)</li>
 * </ul>
 * <p>Standard error of a simulated quantile is non-parametric:
 * <pre>SE(VaR_p) ≈ √(p·(1−p)/N) / f(VaR_p)</pre>
 * where f is the probability density of the loss distribution at the
 * VaR point.  For GBM the final values are Log-normally distributed,
 * so f must be estimated from the simulation itself (histogram bin
 * density or KDE); the Normal-distribution closed form commonly cited
 * in textbook VaR treatments does NOT apply directly here.  In
 * practice the simplest way to quantify uncertainty is empirical:
 * run multiple seeded trials with different seeds and report the
 * range of VaR estimates rather than a single point.
 */
public class MonteCarloRequest {

    /**
     * Number of simulation paths. Each path independently samples from the
     * GBM process. Default: 10,000 (dashboard-grade). Max: 1,000,000.
     * Sampling error on VaR estimates scales as 1/√nSimulations; see the
     * class javadoc for convergence guidance.
     */
    private int nSimulations = 10_000;

    /**
     * Number of time steps per path. Combined with {@code nPeriodsPerYear},
     * this determines the simulation horizon: horizon_years = nPeriods /
     * nPeriodsPerYear. Default: 252 (one trading year when nPeriodsPerYear
     * is 252). Use nPeriods=1 for a 1-day VaR, nPeriods=10 for a 10-day
     * regulatory VaR, etc.
     */
    private int nPeriods = 252;

    /**
     * Initial portfolio value in base currency units. MUST be &gt; 0.
     * GBM is undefined for non-positive initial values because the process
     * evolves multiplicatively through exp(...) and any S(0) ≤ 0 collapses
     * every subsequent S(t) to 0 or makes the ratio undefined. Default:
     * $1,000,000 — chosen so that percentage losses translate directly to
     * readable dollar magnitudes (e.g., 5% loss → $50,000 VaR).
     */
    private double initialValue = 1_000_000.0;

    /**
     * Expected ANNUALIZED return as a decimal. 0.08 means 8% per year, not 8.
     * This is the μ (drift) parameter of the GBM log-return process.
     * Must match the frequency basis of {@code nPeriodsPerYear}.
     * Default: 0.08.
     */
    private double expectedReturn = 0.08;

    /**
     * ANNUALIZED volatility as a decimal. 0.20 means 20% annualized std dev,
     * not 20. This is the σ parameter of the GBM log-return process. Must
     * be &gt;= 0 (the service rejects negative σ). For extreme values
     * (σ &gt; 1.0, i.e., &gt;100% annualized), the exp(drift + σ·√dt·Z) term
     * can overflow to +Infinity, which cascades to NaN through the sort
     * and the entire response becomes meaningless. The service does NOT
     * clamp this — callers running extreme-vol scenarios must cap σ
     * upstream (values around 0.90–1.00 are a practical ceiling).
     * Default: 0.20.
     */
    private double volatility = 0.20;

    /**
     * Random seed for reproducibility. 0 (default) → non-deterministic
     * (each call yields different results). A non-zero value seeds
     * {@link java.util.Random} so repeated calls with the same seed give
     * bit-identical output.
     *
     * <p>Cross-PRNG caveat: the same seed in a different language or PRNG
     * (xorshift128+ in C, PCG64 in NumPy, etc.) will NOT produce the same
     * numbers. Seeded reproducibility is per-backend.
     */
    private long randomSeed = 0;

    /**
     * Trading periods per year. Controls GBM time step: dt = 1/nPeriodsPerYear.
     * Must match the frequency basis of {@code expectedReturn} and {@code volatility}
     * (both must be annualized). Default: 252. Common alternatives: 260, 365, 12.
     */
    private int nPeriodsPerYear = 252;

    /**
     * Percentile tail levels for VaR reporting. Each value p must be in
     * (0, 1) and represents the probability of losing AT LEAST VaR_p.
     *
     * <p>For each p, the response includes a VaR entry where:
     * <pre>VaR_p = initialValue − sorted_final_values[floor(p × nSimulations)]</pre>
     *
     * <p>Convention warning: p here is the LOSS probability (e.g., 0.05
     * means "5% chance of losing at least this much"), so 0.05 maps to
     * what is commonly called "95% VaR" in industry language. Some
     * systems parameterize by confidence level (1 − p); this API uses
     * tail probability directly.
     *
     * <p>Estimator convention (important for reconciliation):
     *   This service uses {@code floor(p × N)} to index into the
     *   ascending-sorted final-value array.  For {@code p = 0.05} and
     *   {@code N = 10,000} that means index 500, which is the 501-st
     *   smallest outcome.  Some risk systems instead use
     *   {@code ceil(p × N) − 1} (index 499, the 500-th smallest).  The
     *   two conventions differ by exactly one observation and produce
     *   VaR estimates that agree to O(1/N) — immaterial for typical
     *   N &gt; 1,000 but worth noting when reconciling against another
     *   system.  The estimator here matches the CVaR estimator in the
     *   service (CVaR averages indices 0 .. floor(p × N) − 1, so VaR is
     *   the first value OUTSIDE that tail set).
     *
     * <p>Default: [0.01, 0.05] (1% and 5% tail, i.e., 99% and 95% VaR).
     * Up to 8 entries; extras are silently truncated.
     */
    private double[] percentiles = {0.01, 0.05};

    /** Optional identifier echoed in the response for request tracing. */
    private String requestId;

    // ── getters — all enforce defaults so service code has no ternary clutter ─

    public int getNSimulations() { return nSimulations > 0 ? nSimulations : 10_000; }
    public int getNPeriods() { return nPeriods > 0 ? nPeriods : 252; }
    /**
     * Returns the raw {@code initialValue} field (no default substitution).
     * The service layer validates {@code initialValue > 0} and returns an
     * explicit error for non-positive inputs; masking with a default here
     * would hide a real client bug (e.g., a zeroed-out portfolio) behind
     * plausible-looking $1,000,000 output.  The field initializer already
     * supplies the default for the "never set" case.
     */
    public double getInitialValue() { return initialValue; }
    public double getExpectedReturn() { return expectedReturn; }
    public double getVolatility() { return volatility; }
    public long getRandomSeed() { return randomSeed; }
    public int getNPeriodsPerYear() { return nPeriodsPerYear > 0 ? nPeriodsPerYear : 252; }
    public double[] getPercentiles() { return percentiles != null ? percentiles : new double[]{0.01, 0.05}; }
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
