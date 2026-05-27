import { expect, test } from "@playwright/test";

test("a shopper can browse the catalog, add an item, and check out", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Catalog" })).toBeVisible();

  const firstCard = page.locator(".product-card").first();
  await expect(firstCard).toBeVisible();
  await firstCard.getByRole("button", { name: "Add to cart" }).click();

  await page.getByRole("link", { name: /Cart/ }).click();
  await expect(page.getByRole("heading", { name: "Your cart" })).toBeVisible();
  await expect(page.locator("table.cart tbody tr")).toHaveCount(1);

  await page.getByRole("button", { name: "Checkout" }).click();
  await expect(page.getByRole("heading", { name: "Order confirmed" })).toBeVisible();
  await expect(page.getByText(/Order #\d+/)).toBeVisible();
});
