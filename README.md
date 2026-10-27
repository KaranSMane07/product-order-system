# Product & Order Management System

Backend-focused assignment: Java 17, Spring Boot 3, Spring Data JPA/Hibernate, PostgreSQL, REST APIs, plus a minimal HTML frontend.

Roles: **ADMIN** (manage products) and **USER** (browse, cart, order).

## Tech stack
Spring Boot 3.3 · Spring Web · Spring Data JPA · Spring Security (HTTP Basic, BCrypt) · Bean Validation · PostgreSQL · Lombok · Maven

## Architecture
```
controller  ->  service  ->  repository  ->  PostgreSQL
   (DTOs in/out)   (business rules, @Transactional)
exception: custom exceptions + @RestControllerAdvice
```

## Setup
1. Install Java 17, Maven and PostgreSQL.
2. Create the database:
   ```sql
   CREATE DATABASE shopdb;
   ```
3. Edit `src/main/resources/application.properties` (`spring.datasource.username` / `password`).
4. Run:
   ```bash
   mvn spring-boot:run
   ```
5. Open http://localhost:8080 (tables are auto-created by Hibernate).

Seeded on first start: admin `admin@shop.com` / `admin123` and 3 demo products.

## Database schema
| Table | Columns | Relations |
|---|---|---|
| users | id, name, email (unique), password (BCrypt), role | 1-1 cart, 1-N orders |
| products | id, name, description, price, quantity, enabled | referenced by cart_items, order_items |
| cart | id, user_id (unique) | 1-N cart_items |
| cart_items | id, cart_id, product_id, quantity | N-1 cart, N-1 product |
| orders | id, user_id, total_amount, status, created_at | 1-N order_items |
| order_items | id, order_id, product_id, quantity, price (price at purchase) | N-1 order, N-1 product |

```
users 1──1 cart 1──N cart_items N──1 products
users 1──N orders 1──N order_items N──1 products
```

## API list
| Method | URL | Role | Description |
|---|---|---|---|
| POST | /api/auth/register | public | Register a USER |
| GET | /api/auth/me | any | Current user + role |
| GET | /api/products | any | List enabled products |
| GET | /api/admin/products | ADMIN | List all products |
| POST | /api/admin/products | ADMIN | Add product |
| PUT | /api/admin/products/{id} | ADMIN | Update price and/or quantity |
| PATCH | /api/admin/products/{id}/enable | ADMIN | Enable product |
| PATCH | /api/admin/products/{id}/disable | ADMIN | Disable product |
| GET | /api/cart | USER | View cart |
| POST | /api/cart/items | USER | Add item `{productId, quantity}` |
| PUT | /api/cart/items/{itemId} | USER | Update quantity `{quantity}` |
| DELETE | /api/cart/items/{itemId} | USER | Remove item |
| POST | /api/orders | USER | Place order from cart |
| GET | /api/orders | USER | My orders |

Auth is HTTP Basic (`-u email:password`).

## Sample requests
```bash
# Register
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Ravi","email":"ravi@mail.com","password":"123456"}'

# Admin: add product
curl -u admin@shop.com:admin123 -X POST localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Monitor","description":"24 inch","price":8999.00,"quantity":10}'

# Admin: update price/quantity
curl -u admin@shop.com:admin123 -X PUT localhost:8080/api/admin/products/1 \
  -H "Content-Type: application/json" -d '{"price":1799.00,"quantity":20}'

# Admin: disable / enable
curl -u admin@shop.com:admin123 -X PATCH localhost:8080/api/admin/products/1/disable
curl -u admin@shop.com:admin123 -X PATCH localhost:8080/api/admin/products/1/enable

# User: view products, add to cart, update, remove, order
curl -u ravi@mail.com:123456 localhost:8080/api/products
curl -u ravi@mail.com:123456 -X POST localhost:8080/api/cart/items \
  -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}'
curl -u ravi@mail.com:123456 -X PUT localhost:8080/api/cart/items/1 \
  -H "Content-Type: application/json" -d '{"quantity":3}'
curl -u ravi@mail.com:123456 -X DELETE localhost:8080/api/cart/items/1
curl -u ravi@mail.com:123456 -X POST localhost:8080/api/orders
curl -u ravi@mail.com:123456 localhost:8080/api/orders
```

## Business rules and how they are enforced
- **Inventory must be available**: checked when adding to cart and again at order time.
- **Order fails if quantity is insufficient**: `InsufficientStockException` (HTTP 409).
- **Inventory reduces after a successful order**: done inside `OrderService.placeOrder`.
- **Transactional order placement**: `@Transactional(rollbackFor = Exception.class)`; any failure rolls back stock changes and the order.
- **No overselling under concurrency**: product rows are locked with `PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`).
- Disabled products cannot be added to a cart or ordered.
- Order items store the unit price at purchase time.

## Error format
```json
{ "timestamp": "...", "status": 409, "error": "Conflict", "message": "Insufficient stock for Wireless Mouse (available: 2, requested: 5)" }
```
Validation errors return `message` as a map of field to message.
