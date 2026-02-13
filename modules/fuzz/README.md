# Apache Axis2/Java Fuzz Testing Module

Comprehensive fuzz testing for Axis2/Java parsers, mirroring the [Axis2/C OSS-Fuzz approach](https://github.com/apache/axis-axis2-c-core/tree/master/fuzz).

## Overview

This module provides Jazzer-compatible fuzz targets for security testing:

| Fuzzer | Component | Attack Vectors |
|--------|-----------|----------------|
| `XmlParserFuzzer` | AXIOM/StAX | XXE, XML bombs, buffer overflows |
| `JsonParserFuzzer` | Gson | Deep nesting, integer overflow, malformed JSON |
| `HttpHeaderFuzzer` | HTTP headers | CRLF injection, header parsing |
| `UrlParserFuzzer` | URL/URI parsing | SSRF, path traversal, malformed URLs |

## Running Fuzzers Locally

### Prerequisites

```bash
# Install Jazzer
# Option 1: Download from GitHub releases
wget https://github.com/CodeIntelligenceTesting/jazzer/releases/download/v0.22.1/jazzer-linux.tar.gz
tar xzf jazzer-linux.tar.gz

# Option 2: Use Docker
docker pull cifuzz/jazzer
```

### Build the Fuzz Module

```bash
cd /path/to/axis-axis2-java-core

# Build all modules first
mvn install -DskipTests

# Build the fuzz module
cd modules/fuzz
mvn package
```

### Run Individual Fuzzers

```bash
# XML Parser Fuzzer
./jazzer --cp=target/axis2-fuzz-2.0.1-SNAPSHOT.jar \
    --target_class=org.apache.axis2.fuzz.XmlParserFuzzer \
    --instrumentation_includes=org.apache.axiom.** \
    -max_total_time=300

# JSON Parser Fuzzer
./jazzer --cp=target/axis2-fuzz-2.0.1-SNAPSHOT.jar \
    --target_class=org.apache.axis2.fuzz.JsonParserFuzzer \
    --instrumentation_includes=com.google.gson.** \
    -max_total_time=300

# HTTP Header Fuzzer
./jazzer --cp=target/axis2-fuzz-2.0.1-SNAPSHOT.jar \
    --target_class=org.apache.axis2.fuzz.HttpHeaderFuzzer \
    --instrumentation_includes=org.apache.axis2.** \
    -max_total_time=300

# URL Parser Fuzzer
./jazzer --cp=target/axis2-fuzz-2.0.1-SNAPSHOT.jar \
    --target_class=org.apache.axis2.fuzz.UrlParserFuzzer \
    --instrumentation_includes=org.apache.axis2.** \
    -max_total_time=300
```

### Run with JUnit (Regression Testing)

The fuzzers can also be run as JUnit tests for CI integration:

```bash
mvn test -Djazzer.fuzz=true
```

## Understanding Output

### Successful Run
```
INFO: Seed: 1234567890
#1000000 DONE cov: 1234 ft: 5678 corp: 100/10Kb exec/s: 50000
```

### Crash Found
```
== Java Exception: java.lang.OutOfMemoryError
    at org.apache.axiom.om.impl.builder.StAXOMBuilder.<init>
Crash file: crash-abc123def456
```

The crash file contains the input that triggered the bug. Reproduce with:
```bash
./jazzer --cp=target/axis2-fuzz-2.0.1-SNAPSHOT.jar \
    --target_class=org.apache.axis2.fuzz.XmlParserFuzzer \
    crash-abc123def456
```

## Comparison with Axis2/C Fuzzers

| Axis2/C | Axis2/Java | Component |
|---------|------------|-----------|
| `fuzz_xml_parser.c` | `XmlParserFuzzer.java` | XML/AXIOM |
| `fuzz_json_parser.c` | `JsonParserFuzzer.java` | JSON |
| `fuzz_json_reader.c` | (integrated in JsonParserFuzzer) | JSON→XML |
| `fuzz_http_header.c` | `HttpHeaderFuzzer.java` | HTTP headers |
| `fuzz_url_parser.c` | `UrlParserFuzzer.java` | URL parsing |

## Security Vulnerability Reporting

If fuzzing finds a security vulnerability:

1. **Do NOT** open a public GitHub issue
2. Report to Apache Security Team: security@apache.org
3. Include:
   - Crash file (input that triggers the bug)
   - Stack trace
   - Axis2/Java version

## Related Documentation

- [Axis2/C OSS-Fuzz Integration](https://github.com/apache/axis-axis2-c-core/blob/master/docs/OSS-FUZZ.md)
- [Jazzer Documentation](https://github.com/CodeIntelligenceTesting/jazzer)
- [OSS-Fuzz Documentation](https://google.github.io/oss-fuzz/)
