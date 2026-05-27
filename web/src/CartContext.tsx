import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { api } from "./api/client";
import { sessionId } from "./api/format";
import type { Cart, Product } from "./api/types";

interface CartContextValue {
  cart: Cart | null;
  refresh: () => Promise<void>;
  add: (product: Product, quantity: number) => Promise<void>;
  updateQuantity: (productId: number, quantity: number) => Promise<void>;
  remove: (productId: number) => Promise<void>;
}

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const session = useMemo(() => sessionId(), []);
  const [cart, setCart] = useState<Cart | null>(null);

  const refresh = useCallback(async () => {
    setCart(await api.getCart(session));
  }, [session]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const add = useCallback(
    async (product: Product, quantity: number) => {
      setCart(await api.addToCart(session, product, quantity));
    },
    [session],
  );

  const updateQuantity = useCallback(
    async (productId: number, quantity: number) => {
      setCart(await api.updateQuantity(session, productId, quantity));
    },
    [session],
  );

  const remove = useCallback(
    async (productId: number) => {
      setCart(await api.removeFromCart(session, productId));
    },
    [session],
  );

  const value = useMemo(
    () => ({ cart, refresh, add, updateQuantity, remove }),
    [cart, refresh, add, updateQuantity, remove],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCart(): CartContextValue {
  const ctx = useContext(CartContext);
  if (!ctx) {
    throw new Error("useCart must be used inside a CartProvider");
  }
  return ctx;
}
