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

## Benchmark

`bench/run-bench.sh` measures the order placement path against a real Postgres
(the catalog reservation calls are stubbed so the number reflects the orders
service work plus the database write). It reports p50, p95, p99 latency and
throughput over 2000 measured placements after a warmup.

A local run (Apple M series, temurin 21) measured:

```
p50  2.925 ms
p95  3.774 ms
throughput  329 placements/sec
```

CI runs the benchmark twice on the same runner and `bench/compare.py` fails the
`bench-regress` job if the second run is more than 30 percent slower than the
first, which catches a placement path change that drops throughput sharply.
Numbers differ by machine, so the gate compares two runs on the same host
rather than against a fixed figure.

## Order placement saga

Placing an order spans two services: the catalog service owns inventory, the
orders service owns orders. There is no shared transaction across them, so the
orders service runs a saga.

For each line in the order:

1. reserve the units in the catalog service, and
2. record a compensating action (release those units) on a stack.

After all lines are reserved, the order is written to the orders database in its
own transaction. If any step fails (a reservation is rejected, the catalog
service errors, or the database write fails), the saga runs the compensating
actions in reverse order to release every reservation it already made, then
reports the placement as failed. The result is that a failed placement leaves no
units reserved and no order row behind.

The compensation logic lives in `OrderService.placeOrder`. The database write is
isolated in `OrderWriter` so it commits or rolls back on its own, separate from
the inventory calls.

`OrderSagaCompensationIT` proves this: it lets the first line reserve, injects a
failure on the second line's reservation, and asserts that the first
reservation is released back to zero and that no order was persisted.

## Inter service resilience

The gateway wraps each route in a Resilience4j circuit breaker with a response
timeout, and retries idempotent GETs on server errors. Each route has a fallback
that returns 503 with a short message. When a downstream service errors or is
unreachable, the breaker opens after a few failures and the gateway answers from
the fallback instead of forwarding every request to a service that cannot
respond, so a single unhealthy service does not make the gateway hang.

`CircuitBreakerTest` covers this: a failing catalog downstream trips the breaker
and the gateway returns the fallback, an open breaker stops forwarding to the
failing service, an unrelated healthy service keeps working, and a downstream
that is completely down still returns the fallback well within the timeout
rather than hanging.

## How this differs

ShopFlow is the microservices decomposition in this set of projects: several
Spring Boot services, a database per service, and a gateway in front. Compared
with the single application projects alongside it (a single Django app, a single
Spring service with Kafka, and a migration walk through), the distinct angle
here is the cross service order placement saga with compensation, plus inter
service resilience between the gateway and the services. The combination of
database per service, a gateway, and a compensating saga is what this repo is
about.

## Local Testcontainers notes

- Testcontainers BOM is pinned to a recent version so it negotiates the Docker
  daemon API correctly.
- CI runs on temurin 21 with the standard Docker daemon, where Testcontainers
  works without extra configuration.
