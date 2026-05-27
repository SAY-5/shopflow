import { render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, expect, it, vi } from "vitest";
import { CatalogPage } from "./CatalogPage";
import { CartProvider } from "../CartContext";

const products = [
  { id: 1, name: "Ceramic Mug", description: "Stoneware mug", price: 18, availableUnits: 80 },
];

beforeEach(() => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (url: string) => {
      if (url.endsWith("/catalog/products")) {
        return new Response(JSON.stringify(products), { status: 200 });
      }
      return new Response(
        JSON.stringify({ sessionId: "s", items: [], subtotal: 0, totalQuantity: 0 }),
        { status: 200 },
      );
    }),
  );
});

afterEach(() => {
  vi.unstubAllGlobals();
});

it("renders products returned by the catalog endpoint", async () => {
  render(
    <CartProvider>
      <CatalogPage />
    </CartProvider>,
  );
  await waitFor(() => expect(screen.getByText("Ceramic Mug")).toBeInTheDocument());
  expect(screen.getByText("$18.00")).toBeInTheDocument();
});
