#!/usr/bin/env bash
# Runs the order placement benchmark and writes the result JSON.
# Usage: bench/run-bench.sh [output-path]
set -euo pipefail

OUT="${1:-$(pwd)/bench/last-run.json}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT"
mvn -B -ntp -pl orders-service -am test-compile >/dev/null

mvn -B -ntp -pl orders-service \
  -Dtest='OrderPlacementBenchmark' \
  -DfailIfNoTests=false \
  -Dbench.out="$OUT" \
  -Dsurefire.failIfNoSpecifiedTests=false \
  surefire:test

echo "wrote benchmark result to $OUT"
cat "$OUT"
