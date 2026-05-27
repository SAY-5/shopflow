import "@testing-library/jest-dom/vitest";
import { webcrypto } from "node:crypto";

if (!globalThis.crypto) {
  globalThis.crypto = webcrypto as Crypto;
}

class MemoryStorage {
  private store = new Map<string, string>();
  getItem(key: string): string | null {
    return this.store.has(key) ? this.store.get(key)! : null;
  }
  setItem(key: string, value: string): void {
    this.store.set(key, value);
  }
  removeItem(key: string): void {
    this.store.delete(key);
  }
  clear(): void {
    this.store.clear();
  }
}

Object.defineProperty(globalThis, "localStorage", {
  value: new MemoryStorage(),
  configurable: true,
});
