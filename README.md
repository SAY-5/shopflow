# ShopFlow

A small e-commerce system split into Spring Boot microservices behind a single
REST gateway, with a React storefront. Each service owns its own Postgres
database (database per service); the gateway is the only entry point the
storefront talks to.

## Services

| Component | Stack | Responsibility |
|-----------|-------|----------------|
| catalog-service | Spring Boot, Postgres | products and inventory levels, reserve and release units |
| cart-service | Spring Boot, Postgres | per session carts, line items, totals |
| orders-service | Spring Boot, Postgres | order placement, order history, order state machine |
| gateway | Spring Cloud Gateway | routes `/api/catalog`, `/api/cart`, `/api/orders` to the services |
| web | React, TypeScript, Vite | storefront: browse, cart, checkout, order confirmation |

The gateway maps each prefix to a service and strips the prefix before
forwarding, so `/api/catalog/products` reaches the catalog service as
`/products`.

## Layout

```
catalog-service/   product catalog and inventory
cart-service/      session carts
orders-service/    orders and the placement saga
gateway/           Spring Cloud Gateway routing
web/               React + TypeScript storefront
e2e/               Playwright checkout flow against the running stack
docker-compose.yml gateway + 3 services + 3 Postgres instances + web
```

## Running locally

Build the service jars and start everything with Compose:

```
mvn -DskipTests package
docker compose up --build
```

- Storefront: http://localhost:8088
- Gateway: http://localhost:8080

The catalog ships with seed products via Flyway, so the storefront has
something to show on first start.

## Tests

Each service is built and tested in isolation:

```
mvn -pl catalog-service -am verify
mvn -pl cart-service -am verify
mvn -pl orders-service -am verify
mvn -pl gateway -am verify
```

Integration tests use Testcontainers to run a real Postgres. A single container
is started per JVM and shared across test classes (see
`PostgresIntegrationTest`) so the Spring context cache stays warm. JaCoCo
enforces a line coverage floor on each module.

The storefront is checked with ESLint, the TypeScript compiler, and Vitest:

```
cd web && npm ci && npm run lint && npm run typecheck && npm test && npm run build
```

The Playwright suite in `e2e/` drives the storefront against a running Compose
stack and walks the browse, add to cart, checkout path.

### Cross service checks

- The gateway tests assert that each prefix routes to the right service with the
  path prefix stripped and the method and body preserved (MockWebServer stands
  in for the downstreams).
- Cart totals are covered by jqwik property tests: the subtotal always equals
  the sum of line totals, is never negative, and the total quantity matches.
- Order placement is checked to reject an order when the inventory reservation
  fails, leaving no order persisted.

## How this differs

ShopFlow is the microservices decomposition in this set of projects: several
Spring Boot services, a database per service, and a gateway in front. The angle
is the cross service order path, which is built up over the versions below into
an order placement saga with compensation and inter service resilience between
the gateway and the services.

## Local Testcontainers notes

- Testcontainers BOM is pinned to a recent version so it negotiates the Docker
  daemon API correctly.
- CI runs on temurin 21 with the standard Docker daemon, where Testcontainers
  works without extra configuration.
