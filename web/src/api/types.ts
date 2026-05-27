export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  availableUnits: number;
}

export interface CartLine {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Cart {
  sessionId: string;
  items: CartLine[];
  subtotal: number;
  totalQuantity: number;
}

export interface OrderLine {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
}

export interface Order {
  id: number;
  customerRef: string;
  status: string;
  createdAt: string;
  total: number;
  lines: OrderLine[];
}
