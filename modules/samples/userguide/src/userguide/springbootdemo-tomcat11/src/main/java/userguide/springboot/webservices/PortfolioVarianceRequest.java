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
 * Request for portfolio variance calculation.
 *
 * <p>Computes σ²_p = Σ_i Σ_j w_i · w_j · σ_ij — an O(n²) operation
 * that mirrors correlation/risk calculations in typical portfolio risk
 * platforms.
 *
 * <h3>Covariance matrix formats</h3>
 * <ul>
 *   <li><b>2D array</b> (preferred): {@code covarianceMatrix[i][j]} — natural JSON nested array</li>
 *   <li><b>Flat array</b> (alternative): {@code covarianceMatrixFlat} of length n² in row-major order</li>
 * </ul>
 * If both are supplied, {@code covarianceMatrix} takes precedence.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * {
 *   "weights": [0.4, 0.6],
 *   "covarianceMatrix": [[0.04, 0.006], [0.006, 0.09]],
 *   "normalizeWeights": false,
 *   "nPeriodsPerYear": 252
 * }
 * }</pre>
 */
public class PortfolioVarianceRequest {

    /** Portfolio weights. Length determines n_assets when nAssets is not set. */
    private double[] weights;

    /**
     * Covariance matrix in 2D format: {@code covarianceMatrix[i][j]}.
     * Takes precedence over {@code covarianceMatrixFlat} if both are provided.
     *
     * <p>Time basis (important): the matrix MUST be on the same frequency
     * basis as {@code nPeriodsPerYear}.  The service's
     * {@code annualizedVolatility} output is computed as
     * {@code sqrt(w'Σw) · sqrt(nPeriodsPerYear)}, which is only correct
     * when Σ is <em>per-period</em> covariance (daily Σ with
     * nPeriodsPerYear=252, weekly with 52, monthly with 12).  Passing an
     * already-annualized Σ while leaving nPeriodsPerYear=252 inflates the
     * reported annualized vol by √252 ≈ 15.9×.  If your matrix is already
     * annualized, set {@code nPeriodsPerYear=1} and read
     * {@code portfolioVolatility} instead.
     */
    private double[][] covarianceMatrix;

    /**
     * Covariance matrix in flat row-major format: element (i,j) is at index
     * {@code i * nAssets + j}. Length must be nAssets². Used when the caller
     * cannot produce a nested JSON array (e.g., numpy {@code .flatten()}).
     * Same time-basis convention as {@link #covarianceMatrix} applies.
     */
    private double[] covarianceMatrixFlat;

    /**
     * When {@code true}, weights are rescaled to sum to 1.0 before computing
     * variance. Allows callers to pass unnormalized exposures (e.g., notional
     * position values) without a client-side preprocessing step.
     * When {@code false} (default), weights that deviate from 1.0 by more than
     * 1e-4 return an error.
     */
    private boolean normalizeWeights = false;

    /**
     * Trading periods per year used to annualize volatility.
     * {@code annualizedVolatility = portfolioVolatility × sqrt(nPeriodsPerYear)}.
     * Common values: 252 (equity, default), 260 (some fixed-income conventions),
     * 365 (crypto), 12 (monthly factor models).
     *
     * <p>MUST match the frequency basis of the supplied covariance matrix
     * (see {@link #covarianceMatrix}).  Mismatches silently inflate or
     * deflate {@code annualizedVolatility} by {@code sqrt(nPeriodsPerYear)};
     * the service cannot detect this because both per-period and annualized
     * matrices are valid PSD covariance matrices.  If your covariance is
     * already annualized, pass {@code 1} here.
     */
    private int nPeriodsPerYear = 252;

    /** Optional identifier echoed in the response for request tracing. */
    private String requestId;

    // ── getters ──────────────────────────────────────────────────────────────

    public double[] getWeights() { return weights; }
    public double[][] getCovarianceMatrix() { return covarianceMatrix; }
    public double[] getCovarianceMatrixFlat() { return covarianceMatrixFlat; }
    public boolean isNormalizeWeights() { return normalizeWeights; }
    public int getNPeriodsPerYear() { return nPeriodsPerYear > 0 ? nPeriodsPerYear : 252; }
    public String getRequestId() { return requestId; }

    // ── setters ──────────────────────────────────────────────────────────────

    public void setWeights(double[] weights) { this.weights = weights; }
    public void setCovarianceMatrix(double[][] covarianceMatrix) { this.covarianceMatrix = covarianceMatrix; }
    public void setCovarianceMatrixFlat(double[] covarianceMatrixFlat) { this.covarianceMatrixFlat = covarianceMatrixFlat; }
    public void setNormalizeWeights(boolean normalizeWeights) { this.normalizeWeights = normalizeWeights; }
    public void setNPeriodsPerYear(int nPeriodsPerYear) { this.nPeriodsPerYear = nPeriodsPerYear; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    /** Derived: number of assets inferred from weights array length. */
    public int getNAssets() {
        return weights != null ? weights.length : 0;
    }
}
