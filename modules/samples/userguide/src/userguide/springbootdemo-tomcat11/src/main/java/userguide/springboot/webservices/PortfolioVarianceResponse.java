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
 * Response for portfolio variance calculation.
 *
 * <p>Contains the computed variance, volatility, and performance metrics.
 * When {@code status == "FAILED"}, only {@code errorMessage} is meaningful.
 */
public class PortfolioVarianceResponse {

    private String status;
    private String errorMessage;

    // ── Results ──────────────────────────────────────────────────────────────

    /** Portfolio variance σ²_p = Σ_i Σ_j w_i · w_j · σ_ij */
    private double portfolioVariance;

    /** Portfolio volatility σ = sqrt(σ²_p) */
    private double portfolioVolatility;

    /** Annualized volatility σ × sqrt(nPeriodsPerYear) */
    private double annualizedVolatility;

    /** Actual sum of weights as received (before normalization if applied) */
    private double weightSum;

    /** True when weights were rescaled to sum to 1.0 (normalizeWeights=true was effective) */
    private boolean weightsNormalized;

    // ── Performance metrics ───────────────────────────────────────────────────

    /** Wall-clock time for the O(n²) calculation in microseconds */
    private long calcTimeUs;

    /** Number of multiply-add operations: n_assets² */
    private long matrixOperations;

    /** Throughput: matrixOperations / (calcTimeUs / 1e6) */
    private double opsPerSecond;

    /** JVM heap used at response time in MB */
    private long memoryUsedMb;

    /** Runtime identifier (JVM version, heap config) */
    private String runtimeInfo;

    /** Echoed from request */
    private String requestId;

    // ── Constructors ─────────────────────────────────────────────────────────

    public static PortfolioVarianceResponse failed(String errorMessage) {
        PortfolioVarianceResponse r = new PortfolioVarianceResponse();
        r.status = "FAILED";
        r.errorMessage = errorMessage;
        return r;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public double getPortfolioVariance() { return portfolioVariance; }
    public void setPortfolioVariance(double portfolioVariance) { this.portfolioVariance = portfolioVariance; }

    public double getPortfolioVolatility() { return portfolioVolatility; }
    public void setPortfolioVolatility(double portfolioVolatility) { this.portfolioVolatility = portfolioVolatility; }

    public double getAnnualizedVolatility() { return annualizedVolatility; }
    public void setAnnualizedVolatility(double annualizedVolatility) { this.annualizedVolatility = annualizedVolatility; }

    public double getWeightSum() { return weightSum; }
    public void setWeightSum(double weightSum) { this.weightSum = weightSum; }

    public boolean isWeightsNormalized() { return weightsNormalized; }
    public void setWeightsNormalized(boolean weightsNormalized) { this.weightsNormalized = weightsNormalized; }

    public long getCalcTimeUs() { return calcTimeUs; }
    public void setCalcTimeUs(long calcTimeUs) { this.calcTimeUs = calcTimeUs; }

    public long getMatrixOperations() { return matrixOperations; }
    public void setMatrixOperations(long matrixOperations) { this.matrixOperations = matrixOperations; }

    public double getOpsPerSecond() { return opsPerSecond; }
    public void setOpsPerSecond(double opsPerSecond) { this.opsPerSecond = opsPerSecond; }

    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }

    public String getRuntimeInfo() { return runtimeInfo; }
    public void setRuntimeInfo(String runtimeInfo) { this.runtimeInfo = runtimeInfo; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
