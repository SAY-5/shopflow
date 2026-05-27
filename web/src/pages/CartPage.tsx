import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { formatMoney } from "../api/format";
import { useCart } from "../CartContext";

export function CartPage() {
  const { cart, updateQuantity, remove, refresh } = useCart();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [placing, setPlacing] = useState(false);

  if (!cart || cart.items.length === 0) {
    return (
      <section>
        <h1>Your cart</h1>
        <p className="empty">Your cart is empty.</p>
      </section>
    );
  }

  async function checkout() {
    if (!cart) {
      return;
    }
    setPlacing(true);
    setError(null);
    try {
      const order = await api.placeOrder("web-customer", cart);
      await Promise.all(cart.items.map((item) => remove(item.productId)));
      await refresh();
      navigate(`/orders/${order.id}`, { state: { order } });
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setPlacing(false);
    }
  }

  return (
    <section>
      <h1>Your cart</h1>
      {error && <p className="notice">Checkout failed: {error}</p>}
      <table className="cart">
        <thead>
          <tr>
            <th>Item</th>
            <th className="num">Price</th>
            <th className="num">Qty</th>
            <th className="num">Line total</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {cart.items.map((item) => (
            <tr key={item.productId}>
              <td>{item.productName}</td>
              <td className="num">{formatMoney(item.unitPrice)}</td>
              <td className="num">
                <input
                  className="qty-input"
                  type="number"
                  min={0}
                  value={item.quantity}
                  onChange={(e) =>
                    void updateQuantity(item.productId, Number(e.target.value))
                  }
                />
              </td>
              <td className="num">{formatMoney(item.lineTotal)}</td>
              <td className="num">
                <button className="ghost" onClick={() => void remove(item.productId)}>
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="cart-summary">
        <span className="total">Subtotal {formatMoney(cart.subtotal)}</span>
        <button disabled={placing} onClick={() => void checkout()}>
          {placing ? "Placing order..." : "Checkout"}
        </button>
      </div>
    </section>
  );
}
