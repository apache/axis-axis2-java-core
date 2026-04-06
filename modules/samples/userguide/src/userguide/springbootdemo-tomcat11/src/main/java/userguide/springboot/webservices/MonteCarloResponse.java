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
 * Response for Monte Carlo Value-at-Risk simulation.
 *
 * <p>Fixed fields ({@code var95}, {@code var99}, {@code cvar95}) are always
 * populated on success for backward compatibility. The {@code percentileVars}
 * list reflects the caller-specified {@code percentiles} array.
 */
public class MonteCarloResponse {

    private String status;
    private String errorMessage;

    // ── Distribution statistics ───────────────────────────────────────────────

    /** Mean of final portfolio values across all paths */
    private double meanFinalValue;

    /** Median (50th percentile) of final portfolio values */
    private double medianFinalValue;

    /** Standard deviation of final portfolio values */
    private double stdDevFinalValue;

    // ── Fixed VaR fields (always populated) ──────────────────────────────────

    /** Value at Risk at 95% confidence: initialValue − 5th-percentile final value */
    private double var95;

    /** Value at Risk at 99% confidence: initialValue − 1st-percentile final value */
    private double var99;

    /** Conditional VaR (Expected Shortfall) at 95%: initialValue − mean(worst 5%) */
    private double cvar95;

    // ── Additional risk metrics ───────────────────────────────────────────────

    /** Maximum drawdown observed across all simulation paths (0–1 fraction) */
    private double maxDrawdown;

    /** Fraction of paths where final value > initial value */
    private double probProfit;

    // ── Caller-specified percentile VaR ──────────────────────────────────────

    /**
     * VaR values for the percentile levels requested in {@code MonteCarloRequest.percentiles}.
     * Each entry: {@code {"percentile": 0.01, "var": 185432.10}}.
     */
    private List<PercentileVar> percentileVars;

    // ── Performance metrics ───────────────────────────────────────────────────

    /** Wall-clock time for the simulation in microseconds */
    private long calcTimeUs;

    /** Simulation throughput: nSimulations / (calcTimeUs / 1e6) */
    private double simulationsPerSecond;

    /** JVM heap used at response time in MB */
    private long memoryUsedMb;

    /** Echoed from request */
    private String requestId;

    // ── Inner types ──────────────────────────────────────────────────────────

    /**
     * A single percentile VaR entry in {@code percentileVars}.
     */
    public static class PercentileVar {
        private double percentile;
        private double var;

        public PercentileVar() {}

        public PercentileVar(double percentile, double var) {
            this.percentile = percentile;
            this.var = var;
        }

        public double getPercentile() { return percentile; }
        public void setPercentile(double percentile) { this.percentile = percentile; }
        public double getVar() { return var; }
        public void setVar(double var) { this.var = var; }
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    public static MonteCarloResponse failed(String errorMessage) {
        MonteCarloResponse r = new MonteCarloResponse();
        r.status = "FAILED";
        r.errorMessage = errorMessage;
        return r;
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public double getMeanFinalValue() { return meanFinalValue; }
    public void setMeanFinalValue(double meanFinalValue) { this.meanFinalValue = meanFinalValue; }

    public double getMedianFinalValue() { return medianFinalValue; }
    public void setMedianFinalValue(double medianFinalValue) { this.medianFinalValue = medianFinalValue; }

    public double getStdDevFinalValue() { return stdDevFinalValue; }
    public void setStdDevFinalValue(double stdDevFinalValue) { this.stdDevFinalValue = stdDevFinalValue; }

    public double getVar95() { return var95; }
    public void setVar95(double var95) { this.var95 = var95; }

    public double getVar99() { return var99; }
    public void setVar99(double var99) { this.var99 = var99; }

    public double getCvar95() { return cvar95; }
    public void setCvar95(double cvar95) { this.cvar95 = cvar95; }

    public double getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(double maxDrawdown) { this.maxDrawdown = maxDrawdown; }

    public double getProbProfit() { return probProfit; }
    public void setProbProfit(double probProfit) { this.probProfit = probProfit; }

    public List<PercentileVar> getPercentileVars() { return percentileVars; }
    public void setPercentileVars(List<PercentileVar> percentileVars) { this.percentileVars = percentileVars; }

    public long getCalcTimeUs() { return calcTimeUs; }
    public void setCalcTimeUs(long calcTimeUs) { this.calcTimeUs = calcTimeUs; }

    public double getSimulationsPerSecond() { return simulationsPerSecond; }
    public void setSimulationsPerSecond(double simulationsPerSecond) { this.simulationsPerSecond = simulationsPerSecond; }

    public long getMemoryUsedMb() { return memoryUsedMb; }
    public void setMemoryUsedMb(long memoryUsedMb) { this.memoryUsedMb = memoryUsedMb; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
