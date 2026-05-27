import { useEffect, useState } from "react";
import { useLocation, useParams, Link } from "react-router-dom";
import { formatMoney } from "../api/format";
import type { Order } from "../api/types";

export function OrderConfirmationPage() {
  const { id } = useParams();
  const location = useLocation();
  const passed = (location.state as { order?: Order } | null)?.order ?? null;
  const [order, setOrder] = useState<Order | null>(passed);

  useEffect(() => {
    if (order || !id) {
      return;
    }
    fetch(`/api/orders/orders/${id}`)
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error("not found"))))
      .then(setOrder)
      .catch(() => setOrder(null));
  }, [id, order]);

  if (!order) {
    return (
      <section>
        <h1>Order</h1>
        <p className="empty">Order details are not available.</p>
        <Link to="/">Back to catalog</Link>
      </section>
    );
  }

  return (
    <section>
      <h1>Order confirmed</h1>
      <div className="order-confirmation">
        <p>
          Order <strong>#{order.id}</strong> is <strong>{order.status}</strong>.
        </p>
        <table className="cart">
          <tbody>
            {order.lines.map((line) => (
              <tr key={line.productId}>
                <td>{line.productName}</td>
                <td className="num">{line.quantity}</td>
                <td className="num">{formatMoney(line.unitPrice * line.quantity)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="cart-summary">
          <span className="total">Total {formatMoney(order.total)}</span>
          <Link to="/">Continue shopping</Link>
        </div>
      </div>
    </section>
  );
}
