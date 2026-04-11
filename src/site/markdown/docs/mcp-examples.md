# MCP Examples: Financial Services on Axis2/Java + WildFly

**Summary**: Apache Axis2/Java serves the same financial calculations as Axis2/C —
portfolio variance, Monte Carlo VaR, scenario analysis — over JSON on WildFly 32
with Spring Security JWT authentication. This document shows the same live demos
as the Axis2/C `MCP_EXAMPLES.md`, run against the Java implementation, with
head-to-head performance numbers.

The financial results are identical (same algorithms, same inputs, same outputs).
The implementations compete only on performance.

---

## Transport and Timing Note

Both Axis2/C and Axis2/Java support **HTTPS/HTTP2**. Axis2/C runs over
Apache httpd with mod_h2; Axis2/Java runs over WildFly or Tomcat with
ALPN-negotiated HTTP/2 on port 8443. Verified on WildFly 32, WildFly 39,
and Tomcat 11 — all negotiate `h2` via ALPN when accessed over TLS.

All timings in this document use the **server-reported `calcTimeUs` field**
— wall-clock time measured inside the service handler, after request
parsing and before response serialization. Transport overhead (TLS, HTTP/2
framing) is excluded. The computation comparison is apples-to-apples.

---

## Authentication

All examples use **HTTPS/HTTP2 on port 8443**. WildFly uses a self-signed certificate,
so `-k` skips certificate verification. Tomcat uses mTLS with CA-signed client certs
(see `mcp-architecture.md` for PKI details).

Axis2/Java requires JWT authentication via Spring Security. All financial service
calls need a `Bearer` token obtained from the login endpoint:

```bash
TOKEN=$(curl -s --http2 -k https://localhost:8443/axis2-json-api/services/loginService \
  -H 'Content-Type: application/json' \
  -d '{"doLogin":[{"arg0":{"email":"java-dev@axis.apache.org","credentials":"userguide"}}]}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['response']['token'])")
```

All subsequent examples assume `$TOKEN` is set.

---

## API Differences: Java vs C

The financial calculations are identical. The wire format differs:

| | Axis2/C | Axis2/Java |
|---|---|---|
| URL pattern | `.../portfolioVariance` | `.../FinancialBenchmarkService` |
| Request format | `{"n_assets": 5, ...}` | `{"portfolioVariance":[{"arg0":{...}}]}` |
| Response format | `{"status": "SUCCESS", ...}` | `{"response": {"status": "SUCCESS", ...}}` |
| Field naming | `snake_case` | `camelCase` |
| Authentication | None (HTTP/2 + TLS) | JWT Bearer token |
| Memory field | `memory_used_kb` (KB) | `memoryUsedMb` (MB) |
| Covariance input | Flat array (row-major) | 2D array `[[...],[...]]` |

---

## MCP Bridge

Axis2/Java exposes MCP via `axis2-mcp-bridge`, a stdio JAR that reads
`/openapi-mcp.json` and proxies `tools/call` to the Axis2 service. The
bridge handles authentication (mTLS on Tomcat, JWT on WildFly) so the AI
client sees only standard MCP JSON-RPC.

**Claude Desktop configuration** (WildFly, JWT auth):
```json
{
  "mcpServers": {
    "axis2-java-finbench": {
      "command": "java",
      "args": ["-jar", "/path/to/axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar",
               "--base-url", "https://localhost:8443/axis2-json-api"]
    }
  }
}
```

**MCP stdio call format** (what the bridge sends/receives):
```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{
  "name":"portfolioVariance","arguments":{...}}}' \
    | java -jar axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar \
        --base-url https://localhost:8443/axis2-json-api
```

All curl examples below include paired MCP stdio equivalents.

---

## Live Examples (Tested on WildFly 32.0.1.Final, 2026-04-08)

### Portfolio Variance — 5 assets

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{
    "nAssets": 5,
    "weights": [0.25, 0.25, 0.20, 0.15, 0.15],
    "covarianceMatrix": [
      [0.0691, 0.0313, 0.0457, 0.0272, -0.0035],
      [0.0313, 0.0976, 0.0591, 0.0408,  0.0058],
      [0.0457, 0.0591, 0.1207, 0.0437, -0.0086],
      [0.0272, 0.0408, 0.0437, 0.0638,  0.0015],
      [-0.0035, 0.0058,-0.0086, 0.0015,  0.0303]
    ],
    "normalizeWeights": true
  }}]}'
```

```json
{
    "response": {
        "status": "SUCCESS",
        "portfolioVariance": 0.0392,
        "portfolioVolatility": 0.198,
        "annualizedVolatility": 3.143,
        "calcTimeUs": 1,
        "matrixOperations": 25,
        "memoryUsedMb": 198,
        "runtimeInfo": "Java (JVM heap tier: < 2 GB)"
    }
}
```

**MCP stdio equivalent:**
```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"portfolioVariance","arguments":{"nAssets":5,"weights":[0.25,0.25,0.20,0.15,0.15],"covarianceMatrix":[[0.0691,0.0313,0.0457,0.0272,-0.0035],[0.0313,0.0976,0.0591,0.0408,0.0058],[0.0457,0.0591,0.1207,0.0437,-0.0086],[0.0272,0.0408,0.0437,0.0638,0.0015],[-0.0035,0.0058,-0.0086,0.0015,0.0303]],"normalizeWeights":true}}}' \
    | java -jar axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar --base-url https://localhost:8443/axis2-json-api
```

### Portfolio Variance — 500 assets

```bash
# Generate 500-asset test data
python3 -c "
import json
n=500; w=[1.0/n]*n; c=[]
for i in range(n):
    row=[]
    for j in range(n):
        if i==j: row.append(0.04)
        else: row.append(0.01*max(0,1.0-abs(i-j)/50.0))
    c.append(row)
print(json.dumps({'portfolioVariance':[{'arg0':{'nAssets':n,'weights':w,'covarianceMatrix':c}}]}))" \
  > /tmp/pv500.json

curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d @/tmp/pv500.json
```

```json
{
    "response": {
        "status": "SUCCESS",
        "portfolioVariance": 0.001027,
        "portfolioVolatility": 0.0320,
        "calcTimeUs": 660,
        "matrixOperations": 250000,
        "memoryUsedMb": 229
    }
}
```

(MCP equivalent omitted for 500-asset — the arguments object is identical,
just wrapped in `tools/call` JSON-RPC as shown above.)

### Monte Carlo VaR — 100K simulations

Monte Carlo Value at Risk estimates portfolio loss at a given confidence
level by simulating thousands of random price paths using Geometric
Brownian Motion: `S(t+dt) = S(t) × exp((μ − σ²/2)·dt + σ·√dt·Z)` where
Z ~ N(0,1). Run 100,000 paths, sort the terminal values, read off the
1st percentile loss — that's your 99% VaR. Production risk systems run
this nightly for regulatory capital calculations.

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"monteCarlo":[{"arg0":{
    "nSimulations": 100000,
    "nPeriods": 252,
    "initialValue": 1000000,
    "expectedReturn": 0.10,
    "volatility": 0.198,
    "nPeriodsPerYear": 252
  }}]}'
```

```json
{
    "response": {
        "status": "SUCCESS",
        "meanFinalValue": 1104699.76,
        "var95": 219309.63,
        "var99": 317526.89,
        "cvar95": 279538.64,
        "maxDrawdown": 0.567,
        "probProfit": 0.657,
        "calcTimeUs": 1380378,
        "simulationsPerSecond": 72443,
        "memoryUsedMb": 142
    }
}
```

**MCP stdio equivalent:**
```bash
echo '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"monteCarlo","arguments":{"nSimulations":100000,"nPeriods":252,"initialValue":1000000,"expectedReturn":0.10,"volatility":0.198,"nPeriodsPerYear":252}}}' \
    | java -jar axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar --base-url https://localhost:8443/axis2-json-api
```

---

## Demo 1: Stress-test — "What if correlations spike?"

Same test as Axis2/C `MCP_EXAMPLES.md` Demo 1. Baseline portfolio at real
market correlations, then stressed to ρ = 0.8, then Monte Carlo on the
stressed portfolio.

Each curl below has an MCP equivalent — same `tools/call` pattern as
the Live Examples above, with the arguments object matching the `arg0`
payload. The bridge handles the Axis2 JSON-RPC wrapping transparently.

**Step 1 — Baseline:**

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{
    "nAssets": 5,
    "weights": [0.25, 0.25, 0.20, 0.15, 0.15],
    "covarianceMatrix": [
      [0.0691, 0.0313, 0.0457, 0.0272, -0.0035],
      [0.0313, 0.0976, 0.0591, 0.0408,  0.0058],
      [0.0457, 0.0591, 0.1207, 0.0437, -0.0086],
      [0.0272, 0.0408, 0.0437, 0.0638,  0.0015],
      [-0.0035, 0.0058,-0.0086, 0.0015,  0.0303]
    ],
    "normalizeWeights": true
  }}]}'
```

```json
{
    "response": {
        "status": "SUCCESS",
        "portfolioVariance": 0.0392,
        "portfolioVolatility": 0.198,
        "calcTimeUs": 1,
        "memoryUsedMb": 198
    }
}
```

**Step 2 — Stressed (all pairwise correlations → 0.8):**

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{
    "nAssets": 5,
    "weights": [0.25, 0.25, 0.20, 0.15, 0.15],
    "covarianceMatrix": [
      [0.0691, 0.0656, 0.0730, 0.0530, 0.0366],
      [0.0656, 0.0974, 0.0866, 0.0629, 0.0434],
      [0.0730, 0.0866, 0.1204, 0.0699, 0.0483],
      [0.0530, 0.0629, 0.0699, 0.0635, 0.0351],
      [0.0366, 0.0434, 0.0483, 0.0351, 0.0303]
    ],
    "normalizeWeights": true
  }}]}'
```

```json
{
    "response": {
        "status": "SUCCESS",
        "portfolioVariance": 0.0649,
        "portfolioVolatility": 0.2547,
        "calcTimeUs": 1,
        "memoryUsedMb": 198
    }
}
```

**Step 3 — Monte Carlo on stressed portfolio (100K paths):**

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"monteCarlo":[{"arg0":{
    "nSimulations": 100000,
    "nPeriods": 252,
    "initialValue": 1000000,
    "expectedReturn": 0.10,
    "volatility": 0.255,
    "nPeriodsPerYear": 252
  }}]}'
```

```json
{
    "response": {
        "status": "SUCCESS",
        "meanFinalValue": 1104718.26,
        "var95": 296582.35,
        "var99": 409644.96,
        "cvar95": 364494.42,
        "maxDrawdown": 0.668,
        "probProfit": 0.602,
        "calcTimeUs": 1437421,
        "simulationsPerSecond": 69569
    }
}
```

**MCP stdio equivalent (stressed MC):**
```bash
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"monteCarlo","arguments":{"nSimulations":100000,"nPeriods":252,"initialValue":1000000,"expectedReturn":0.10,"volatility":0.255,"nPeriodsPerYear":252}}}' \
    | java -jar axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar --base-url https://localhost:8443/axis2-json-api
```

**Comparison — Axis2/C vs Axis2/Java (same inputs, same day):**

| Metric | Normal ρ | Stressed ρ = 0.8 | Change |
|--------|----------|-------------------|--------|
| Portfolio vol | 19.8% | 25.5% | **+29%** |
| 95% VaR (1yr, $1M) | $219K | $297K | **+$78K** |
| 99% VaR | $318K | $410K | **+$92K** |
| Prob of profit | 65.7% | 60.2% | -5.5pp |

| Timing | Axis2/C | Axis2/Java | Ratio |
|--------|---------|------------|-------|
| `portfolioVariance` (×2) | < 1 μs each | 1 μs each | ~1x |
| `monteCarlo` (100K) | 727 ms | 1,437 ms | **2.0x** |
| Total compute | ~0.73 sec | ~1.44 sec | 2.0x |

Both produce the same financial results. Java's Monte Carlo is ~2x slower
on this run; the JIT-warmed steady state is typically 1.3-1.5x (see
convergence section below).

---

## Demo 2: Pre-trade risk — "Should I add this name?"

Same test as Axis2/C Demo 2. Two candidate 6-asset portfolios: European
semiconductor (high correlation to existing tech) vs Japanese peer (low
correlation). All calls have MCP equivalents via the bridge (same pattern
as Live Examples).

**Candidate A — European semi (vol 44%, ρ = 0.68 to tech):**

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{
    "nAssets": 6,
    "weights": [0.2425, 0.2425, 0.194, 0.1455, 0.1455, 0.03],
    "covarianceMatrix": [
      [0.0691, 0.0313, 0.0457, 0.0272,-0.0035, 0.0787],
      [0.0313, 0.0976, 0.0591, 0.0408, 0.0058, 0.0934],
      [0.0457, 0.0591, 0.1207, 0.0437,-0.0086, 0.1039],
      [0.0272, 0.0408, 0.0437, 0.0638, 0.0015, 0.0610],
      [-0.0035, 0.0058,-0.0086, 0.0015, 0.0303, 0.0115],
      [0.0787, 0.0934, 0.1039, 0.0610, 0.0115, 0.1936]
    ],
    "normalizeWeights": true
  }}]}'
```

```json
{"response": {"status": "SUCCESS", "portfolioVolatility": 0.2035, "calcTimeUs": 1}}
```

**Candidate B — Japanese peer (vol 38%, ρ = 0.31 to US tech):**

```bash
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{
    "nAssets": 6,
    "weights": [0.2425, 0.2425, 0.194, 0.1455, 0.1455, 0.03],
    "covarianceMatrix": [
      [0.0691, 0.0313, 0.0457, 0.0272,-0.0035, 0.0310],
      [0.0313, 0.0976, 0.0591, 0.0408, 0.0058, 0.0368],
      [0.0457, 0.0591, 0.1207, 0.0437,-0.0086, 0.0409],
      [0.0272, 0.0408, 0.0437, 0.0638, 0.0015, 0.0239],
      [-0.0035, 0.0058,-0.0086, 0.0015, 0.0303, 0.0066],
      [0.0310, 0.0368, 0.0409, 0.0239, 0.0066, 0.1444]
    ],
    "normalizeWeights": true
  }}]}'
```

```json
{"response": {"status": "SUCCESS", "portfolioVolatility": 0.1968, "calcTimeUs": 1}}
```

**Head-to-head Monte Carlo (100K paths each):**

```bash
# European candidate (vol 21.1%)
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"monteCarlo":[{"arg0":{"nSimulations":100000,"nPeriods":252,
       "initialValue":1000000,"expectedReturn":0.10,"volatility":0.211,
       "nPeriodsPerYear":252}}]}'

# Japanese candidate (vol 20.1%)
curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"monteCarlo":[{"arg0":{"nSimulations":100000,"nPeriods":252,
       "initialValue":1000000,"expectedReturn":0.10,"volatility":0.201,
       "nPeriodsPerYear":252}}]}'
```

**European candidate response:**
```json
{"response": {"var95": 237481, "var99": 340245, "cvar95": 300412, "probProfit": 0.643, "maxDrawdown": 0.610, "calcTimeUs": 1360351}}
```

**Japanese candidate response:**
```json
{"response": {"var95": 221367, "var99": 322087, "cvar95": 282371, "probProfit": 0.656, "maxDrawdown": 0.555, "calcTimeUs": 1364834}}
```

**Results summary (2026-04-08):**

| | Before (5 names) | + European semi | + Japanese peer |
|---|---|---|---|
| Portfolio vol | 19.8% | **20.3%** (+55bp) | **19.7%** (-13bp) |
| 95% VaR ($1M) | $219K | $237K | $221K |
| 99% VaR | $318K | $340K | $322K |
| CVaR 95% | $280K | $300K | $282K |
| Prob of profit | 65.7% | 64.3% | 65.6% |

**Timing comparison:**

| Call | Axis2/C | Axis2/Java | Ratio |
|------|---------|------------|-------|
| `portfolioVariance` (×4) | < 1 μs each | 1 μs each | ~1x |
| `monteCarlo` European | 716 ms | 1,360 ms | 1.9x |
| `monteCarlo` Japanese | 672 ms | 1,365 ms | 2.0x |
| Total compute | ~1.4 sec | ~2.7 sec | 1.9x |

Financial conclusions are identical — the Japanese name provides genuine
diversification. Java takes roughly twice as long for the Monte Carlo
simulations.

---

## Demo 3: Convergence — "How much compute do I actually need?"

Run `monteCarlo` at 1K, 10K, 100K, and 1M paths:

```bash
for N in 1000 10000 100000 1000000; do
  curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
    -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
    -d "{\"monteCarlo\":[{\"arg0\":{\"nSimulations\":$N,\"nPeriods\":252,
         \"initialValue\":1000000,\"expectedReturn\":0.10,\"volatility\":0.198,
         \"nPeriodsPerYear\":252}}]}"
done
```

**MCP stdio equivalent (example for 100K):**
```bash
echo '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"monteCarlo","arguments":{"nSimulations":100000,"nPeriods":252,"initialValue":1000000,"expectedReturn":0.10,"volatility":0.198,"nPeriodsPerYear":252}}}' \
    | java -jar axis2-mcp-bridge-2.0.1-SNAPSHOT-exe.jar --base-url https://localhost:8443/axis2-json-api
```

**Axis2/Java results (2026-04-08):**

| Simulations | 95% VaR | 99% VaR | Calc time | Sims/sec |
|-------------|---------|---------|-----------|----------|
| 1,000 | $221,665 | $325,340 | **13 ms** | 77,863 |
| 10,000 | $216,407 | $312,895 | **136 ms** | 73,378 |
| 100,000 | $218,868 | $315,163 | **1.37 sec** | 73,211 |
| 1,000,000 | $217,286 | $315,700 | **13.8 sec** | 72,710 |

**Head-to-head with Axis2/C (same inputs, same machine):**

| Simulations | C time | Java time | Ratio | C sims/sec | Java sims/sec |
|-------------|--------|-----------|-------|------------|---------------|
| 1,000 | 6 ms | 13 ms | 2.1x | 164,295 | 77,863 |
| 10,000 | 66 ms | 136 ms | 2.1x | 152,423 | 73,378 |
| 100,000 | 716 ms | 1,370 ms | 1.9x | 139,650 | 73,211 |
| 1,000,000 | 6.6 sec | 13.8 sec | 2.1x | 150,773 | 72,710 |

The ratio is a consistent **~2x** across all simulation counts. C processes
~150K simulations/sec vs Java's ~73K sims/sec.

**Production capacity math (Java)**: at 1.37 sec per 100K-path run, a single
core processes **44 funds per minute**. A 500-fund universe completes in
~11.4 minutes on one core, or **~69 seconds on a 10-core node**.

For comparison, Axis2/C: 500 funds in 6 minutes on one core, 36 seconds
on 10 cores.

---

## 500-Asset Portfolio Variance — The Big Comparison

This is where the gap widens. O(n^2) matrix math at n=500 means 250,000
multiply-accumulate operations on a flat array (C) vs a 2D Java array
with bounds checking.

**Axis2/C:**
```json
{"portfolio_volatility": 0.0320, "calc_time_us": 232, "matrix_operations": 250000}
```

**Axis2/Java:**
```json
{"portfolioVolatility": 0.0320, "calcTimeUs": 660, "matrixOperations": 250000}
```

| | Axis2/C | Axis2/Java | Ratio |
|---|---|---|---|
| Calc time | **232 μs** | **660 μs** | 2.8x |
| Memory | ~193 MB RSS | 229 MB heap | 1.2x |
| Result | 0.0320 | 0.0320 | identical |

At 500 assets Java is 2.8x slower — the JVM's array bounds checking and
object overhead becomes measurable at O(n^2). Both are still sub-millisecond,
which is fast enough for interactive use.

---

## Full Performance Summary

All measurements from 2026-04-08, same machine (Linux, 32 GB RAM).
Timings are server-reported `calcTimeUs` — pure computation time, no
transport overhead. Monte Carlo timings vary ±5% across runs due to JIT
warmup and GC; values below are from the Demo 3 convergence runs (JIT-warm
steady state).

| Benchmark | Axis2/C | Axis2/Java | Ratio |
|-----------|---------|------------|-------|
| portfolioVariance (5 assets) | < 1 μs | 1 μs | ~1x |
| portfolioVariance (500 assets) | 232 μs | 660 μs | 2.8x |
| monteCarlo (1K × 252) | 6 ms | 13 ms | 2.1x |
| monteCarlo (10K × 252) | 66 ms | 136 ms | 2.1x |
| monteCarlo (100K × 252) | 716 ms | 1,366 ms | 1.9x |
| monteCarlo (1M × 252) | 6.6 sec | 13.8 sec | 2.1x |
| MC throughput (sims/sec) | ~150K | ~73K | 2.1x |
| Peak memory (500-asset PV) | ~193 MB | 229 MB | 1.2x |
| Peak memory (100K MC) | ~44 MB | 142 MB | 3.2x |
| Startup | instant | ~7 sec (WildFly) | N/A |

### When to use which

- **Axis2/C**: Edge devices, Android (1-2 GB RAM), IoT gateways, latency-critical
  paths, environments where a JVM cannot run or its memory overhead is unacceptable.
  Sub-millisecond portfolio variance at 500 assets. Startup in milliseconds.

- **Axis2/Java**: Standard Java EE/Jakarta EE deployment on WildFly or Tomcat,
  teams with existing Java infrastructure, environments requiring Spring Security
  (JWT/mTLS), or integration with Java-based data providers. 2x slower on Monte
  Carlo but still interactive (1.4 sec for 100K paths). Deploys as a standard WAR
  with OpenAPI/Swagger UI and MCP bridge included.

Both implement the same MCP tool schemas. An AI assistant configured with
either backend gets the same financial capabilities — the same questions
produce the same answers. The choice is deployment context, not functionality.

---

## MCP Tool Discovery

Axis2/Java exposes an MCP tool catalog at:

```
GET https://localhost:8443/axis2-json-api/openapi-mcp.json
```

This endpoint returns the same tool schema structure that Claude Desktop
and other MCP clients consume. The three financial tools (`portfolioVariance`,
`monteCarlo`, `scenarioAnalysis`) are described with full input schemas,
parameter types, constraints, and defaults — identical in capability to the
Axis2/C MCP stdio server.

---

## Container/JDK Testing Matrix

MCP bridge and OpenAPI endpoints need validation across all supported containers
and JDK versions before the 2.0.1 release:

| Container | JDK | MCP Bridge | OpenAPI/Swagger UI | Status |
|-----------|-----|------------|-------------------|--------|
| WildFly 32 | OpenJDK 21 | ✅ Tested | ✅ Tested | Validated |
| WildFly 39 | OpenJDK 25 | ✅ Tested | ✅ Tested | Validated |
| Tomcat 11 | OpenJDK 21 | ✅ Tested | ✅ Tested | Validated |
| Tomcat 11 | OpenJDK 25 | ✅ Tested | ✅ Tested | Validated |

All four container/JDK combinations negotiate HTTP/2 via ALPN over TLS.

---

## Build and Deploy

### WildFly

Full build, deploy, and test instructions are in
`modules/samples/userguide/src/userguide/springbootdemo-wildfly/README.md`.
Quick start:

```bash
cd modules/samples/userguide/src/userguide/springbootdemo-wildfly
mvn -Dmaven.test.skip.exec clean install

# Deploy exploded WAR to WildFly
rsync -a --delete target/deploy/axis2-json-api/ ~/wildfly/standalone/deployments/axis2-json-api.war/
touch ~/wildfly/standalone/deployments/axis2-json-api.war.dodeploy
```

### Tomcat 11

Full build, deploy, and test instructions are in
`modules/samples/userguide/src/userguide/springbootdemo-tomcat11/README.md`.
Quick start:

```bash
cd modules/samples/userguide/src/userguide/springbootdemo-tomcat11
mvn -Dmaven.test.skip.exec clean install

# Deploy exploded WAR to Tomcat
cp -r target/deploy/axis2-json-api /path/to/tomcat/webapps/
```

### Verify all endpoints after deploy

**Tomcat 11** (HTTPS/HTTP2 on port 8443 with mTLS):

```bash
CERTS=/path/to/axis-axis2-java-core/certs
CURL_MTLS="curl -s --http2 --cert $CERTS/client.crt --key $CERTS/client.key --cacert $CERTS/ca.crt"

# OpenAPI and MCP (no auth required, but mTLS handshake still needed)
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi.json
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi.yaml
$CURL_MTLS https://localhost:8443/axis2-json-api/openapi-mcp.json
$CURL_MTLS https://localhost:8443/axis2-json-api/swagger-ui

# Login
TOKEN=$($CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/loginService \
  -H 'Content-Type: application/json' \
  -d '{"doLogin":[{"arg0":{"email":"java-dev@axis.apache.org","credentials":"userguide"}}]}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['response']['token'])")

# Financial benchmark
$CURL_MTLS -X POST https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{"nAssets":2,"weights":[0.6,0.4],"covarianceMatrix":[[0.04,0.006],[0.006,0.09]]}}]}'
```

**WildFly 32/39** (HTTPS/HTTP2 on port 8443 with self-signed cert, JWT auth):

```bash
# OpenAPI and MCP
curl -s --http2 -k https://localhost:8443/axis2-json-api/openapi.json
curl -s --http2 -k https://localhost:8443/axis2-json-api/openapi-mcp.json

# Login + financial benchmark
TOKEN=$(curl -s --http2 -k https://localhost:8443/axis2-json-api/services/loginService \
  -H 'Content-Type: application/json' \
  -d '{"doLogin":[{"arg0":{"email":"java-dev@axis.apache.org","credentials":"userguide"}}]}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['response']['token'])")

curl -s --http2 -k https://localhost:8443/axis2-json-api/services/FinancialBenchmarkService \
  -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
  -d '{"portfolioVariance":[{"arg0":{"nAssets":2,"weights":[0.6,0.4],"covarianceMatrix":[[0.04,0.006],[0.006,0.09]]}}]}'
```

See the sample READMEs for the complete test flow covering all services.

---

## WildFly 32 Deployment Notes

See `WILDFLY32_DEPLOY_STATE.md` in the Axis2/C repo for the full deployment
walkthrough. Key points:

- WildFly 32.0.1.Final with `--add-modules=java.se` in `standalone.conf`
- `jboss-deployment-structure.xml` from production deployment template (includes `jdk.net` module dependency)
- `beans.xml` with `bean-discovery-mode="none"` (satisfies Weld without CDI scanning)
- Spring Boot 3.4.3 starts in ~0.9 seconds inside WildFly
- WAR: `axis2-json-api-0.0.1-SNAPSHOT.war`
