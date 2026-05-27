#!/usr/bin/env python3
"""Compares two benchmark result files and fails if throughput regressed past a threshold.

Both files are produced by OrderPlacementBenchmark on the same machine, so the comparison
reflects a code change rather than hardware differences. The gate is a smoke test: it catches
a placement-path change that drops throughput by more than the allowed drift.
"""
import json
import sys

DRIFT = 0.30  # allow up to 30 percent slower than the baseline run


def load(path):
    with open(path) as f:
        return json.load(f)


def main():
    if len(sys.argv) != 3:
        print("usage: compare.py <baseline.json> <candidate.json>", file=sys.stderr)
        return 2

    baseline = load(sys.argv[1])
    candidate = load(sys.argv[2])

    base_tput = baseline["throughput_ops_per_sec"]
    cand_tput = candidate["throughput_ops_per_sec"]
    floor = base_tput * (1.0 - DRIFT)

    print(f"baseline throughput : {base_tput:.1f} ops/sec")
    print(f"candidate throughput: {cand_tput:.1f} ops/sec")
    print(f"allowed floor       : {floor:.1f} ops/sec ({int(DRIFT * 100)} percent drift)")
    print(f"baseline p95        : {baseline['p95_ms']:.3f} ms")
    print(f"candidate p95       : {candidate['p95_ms']:.3f} ms")

    if cand_tput < floor:
        print("REGRESSION: candidate throughput is below the allowed floor", file=sys.stderr)
        return 1

    print("OK: within allowed drift")
    return 0


if __name__ == "__main__":
    sys.exit(main())
