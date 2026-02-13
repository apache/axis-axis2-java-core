#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#
# Run all Axis2/Java fuzzers locally
#
# Usage: ./run-fuzzers.sh [duration_seconds]
#
# Prerequisites:
#   - Jazzer installed and in PATH (or set JAZZER_PATH)
#   - Axis2/Java built with: mvn install -DskipTests
#   - Fuzz module built with: cd modules/fuzz && mvn package
#

set -e

DURATION=${1:-60}  # Default 60 seconds per fuzzer
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/target/axis2-fuzz-2.0.1-SNAPSHOT.jar"

# Find Jazzer
JAZZER="${JAZZER_PATH:-jazzer}"
if ! command -v "$JAZZER" &> /dev/null; then
    echo "Error: Jazzer not found. Install from:"
    echo "  https://github.com/CodeIntelligenceTesting/jazzer/releases"
    exit 1
fi

# Check if JAR exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: Fuzz JAR not found at $JAR_PATH"
    echo "Build with: cd modules/fuzz && mvn package"
    exit 1
fi

echo "========================================"
echo "Axis2/Java Fuzz Testing"
echo "Duration per fuzzer: ${DURATION}s"
echo "========================================"

FUZZERS=(
    "org.apache.axis2.fuzz.XmlParserFuzzer"
    "org.apache.axis2.fuzz.JsonParserFuzzer"
    "org.apache.axis2.fuzz.HttpHeaderFuzzer"
    "org.apache.axis2.fuzz.UrlParserFuzzer"
)

RESULTS=()

for FUZZER in "${FUZZERS[@]}"; do
    echo ""
    echo "----------------------------------------"
    echo "Running: $FUZZER"
    echo "----------------------------------------"

    FUZZER_NAME=$(echo "$FUZZER" | sed 's/.*\.//')
    CORPUS_DIR="${SCRIPT_DIR}/corpus/${FUZZER_NAME}"
    mkdir -p "$CORPUS_DIR"

    if "$JAZZER" \
        --cp="$JAR_PATH" \
        --target_class="$FUZZER" \
        --keep_going=10 \
        "$CORPUS_DIR" \
        -- \
        -max_total_time="$DURATION" \
        -print_final_stats=1 2>&1; then
        RESULTS+=("✅ $FUZZER_NAME: PASS")
    else
        EXIT_CODE=$?
        if [ $EXIT_CODE -eq 77 ]; then
            RESULTS+=("⚠️  $FUZZER_NAME: Crash found (see crash-* file)")
        else
            RESULTS+=("❌ $FUZZER_NAME: Error (exit code $EXIT_CODE)")
        fi
    fi
done

echo ""
echo "========================================"
echo "Summary"
echo "========================================"
for RESULT in "${RESULTS[@]}"; do
    echo "$RESULT"
done
