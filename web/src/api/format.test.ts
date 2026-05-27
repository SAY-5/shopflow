import { describe, expect, it } from "vitest";
import { formatMoney } from "./format";

describe("formatMoney", () => {
  it("formats whole amounts with two decimals", () => {
    expect(formatMoney(18)).toBe("$18.00");
  });

  it("formats fractional amounts", () => {
    expect(formatMoney(12.5)).toBe("$12.50");
  });
});
