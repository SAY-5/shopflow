import type { Cart, Order, Product } from "./types";

const BASE = "/api";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `request failed: ${response.status}`);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const api = {
  listProducts: () => request<Product[]>("/catalog/products"),

  getCart: (sessionId: string) => request<Cart>(`/cart/carts/${sessionId}`),

  addToCart: (sessionId: string, product: Product, quantity: number) =>
    request<Cart>(`/cart/carts/${sessionId}/items`, {
      method: "POST",
      body: JSON.stringify({
        productId: product.id,
        productName: product.name,
        unitPrice: product.price,
        quantity,
      }),
    }),

  updateQuantity: (sessionId: string, productId: number, quantity: number) =>
    request<Cart>(`/cart/carts/${sessionId}/items/${productId}`, {
      method: "PUT",
      body: JSON.stringify({ quantity }),
    }),

  removeFromCart: (sessionId: string, productId: number) =>
    request<Cart>(`/cart/carts/${sessionId}/items/${productId}`, { method: "DELETE" }),

  placeOrder: (customerRef: string, cart: Cart) =>
    request<Order>("/orders/orders", {
      method: "POST",
      body: JSON.stringify({
        customerRef,
        lines: cart.items.map((item) => ({
          productId: item.productId,
          productName: item.productName,
          unitPrice: item.unitPrice,
          quantity: item.quantity,
        })),
      }),
    }),
};
