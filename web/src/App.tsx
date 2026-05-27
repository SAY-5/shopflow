import { NavLink, Route, Routes } from "react-router-dom";
import { CartProvider, useCart } from "./CartContext";
import { CatalogPage } from "./pages/CatalogPage";
import { CartPage } from "./pages/CartPage";
import { OrderConfirmationPage } from "./pages/OrderConfirmationPage";

function Header() {
  const { cart } = useCart();
  const count = cart?.totalQuantity ?? 0;
  return (
    <header className="site-header">
      <NavLink to="/" className="brand">
        ShopFlow
      </NavLink>
      <nav>
        <NavLink to="/">Catalog</NavLink>
        <NavLink to="/cart">Cart{count > 0 ? ` (${count})` : ""}</NavLink>
      </nav>
    </header>
  );
}

export function App() {
  return (
    <CartProvider>
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<CatalogPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/orders/:id" element={<OrderConfirmationPage />} />
        </Routes>
      </main>
    </CartProvider>
  );
}
