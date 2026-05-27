import { useEffect, useState } from "react";
import { api } from "../api/client";
import { formatMoney } from "../api/format";
import type { Product } from "../api/types";
import { useCart } from "../CartContext";

export function CatalogPage() {
  const { add } = useCart();
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .listProducts()
      .then(setProducts)
      .catch((e: Error) => setError(e.message));
  }, []);

  return (
    <section>
      <h1>Catalog</h1>
      {error && <p className="notice">Could not load products: {error}</p>}
      <div className="product-grid">
        {products.map((product) => (
          <article key={product.id} className="product-card">
            <h3>{product.name}</h3>
            <p className="desc">{product.description}</p>
            <p className="price">{formatMoney(product.price)}</p>
            <p className="stock">{product.availableUnits} in stock</p>
            <button
              disabled={product.availableUnits === 0}
              onClick={() => void add(product, 1)}
            >
              Add to cart
            </button>
          </article>
        ))}
      </div>
    </section>
  );
}
