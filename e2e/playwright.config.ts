import { defineConfig } from "@playwright/test";

const baseURL = process.env.SHOPFLOW_WEB_URL ?? "http://localhost:8088";

export default defineConfig({
  testDir: "./tests",
  timeout: 60_000,
  expect: { timeout: 15_000 },
  retries: 1,
  use: {
    baseURL,
    trace: "on-first-retry",
  },
});
