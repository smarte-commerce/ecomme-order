# Order Service

## Overview

The Order Service manages the complete order lifecycle within the e-commerce platform, from order creation through fulfillment. It handles order processing, state transitions, payment verification, and integration with other services (payment, shipping, inventory) through asynchronous event-driven architecture and the SAGA pattern.

## Key Features

- **Complete Order Lifecycle Management**: Creation, processing, fulfillment, cancellation
- **SAGA Pattern Implementation**: Distributed transactions with compensation
- **Event-Driven Architecture**: Integration with Kafka for event processing
- **Inventory Management**: Reservation and release of inventory
- **Payment Processing**: Verification and handling of payment status
- **Shipping Integration**: Coordination with shipping service
- **Multi-vendor Support**: Orders with items from multiple vendors
- **Order Tracking**: Real-time status tracking and updates
- **Regional Support**: Region-specific order handling

## Technical Stack

- Spring Boot 3.x
- Spring Data JPA
- Spring Cloud (Eureka, OpenFeign, Circuit Breakers)
- Kafka for event processing
- PostgreSQL/CockroachDB for data persistence
- Redis for caching
- Resilience4j for fault tolerance

## Project Structure

- `/src/main/java/com/winnguyen1905/order/`
  - `/common/`: Common utilities and constants
  - `/config/`: Configuration classes
  - `/core/`: Core business logic
  - `/feign/`: Feign clients for service integration
  - `/model/`: Domain models and DTOs
  - `/persistance/`: Data access layer
    - `/entity/`: JPA entities
    - `/repository/`: JPA repositories
  - `/rest/`: REST controllers
  - `/secure/`: Security configuration
  - `/util/`: Utility classes

## Order States & Transitions

```
CREATED → PAYMENT_PENDING → PAYMENT_CONFIRMED → PROCESSING → READY_FOR_SHIPPING → SHIPPED → DELIVERED
    │              │                                │                                              │
    └─────────────►└────────────────────────────────┴───────────────────────────────────────────▶CANCELLED
```

## API Endpoints

### Order Management

- `POST /api/v1/orders`: Create new order
- `GET /api/v1/orders/{id}`: Get order by ID
- `GET /api/v1/orders`: List orders with filtering
- `PATCH /api/v1/orders/{id}/cancel`: Cancel order
- `GET /api/v1/orders/{id}/tracking`: Track order status

### Order Items

- `GET /api/v1/orders/{orderId}/items`: Get order items
- `POST /api/v1/orders/{orderId}/items`: Add item to order (admin only)
- `DELETE /api/v1/orders/{orderId}/items/{itemId}`: Remove order item (admin only)

### Customer Order Management

- `GET /api/v1/customers/{customerId}/orders`: Get customer's orders
- `GET /api/v1/customers/current/orders`: Get current customer's orders

### Admin Operations

- `PATCH /api/v1/orders/{id}/status`: Update order status
- `GET /api/v1/orders/reports`: Generate order reports
- `GET /api/v1/orders/analytics`: Get order analytics

### Vendor Order Management

- `GET /api/v1/vendors/{vendorId}/orders`: Get vendor's orders
- `PATCH /api/v1/vendors/orders/{id}/status`: Update vendor order status

## SAGA Orchestration

The Order Service implements the SAGA pattern for distributed transactions:

1. **Order Creation**: 
   - Create order in CREATED state
   - Reserve inventory (compensate by releasing inventory)

2. **Payment Processing**:
   - Request payment (compensate by refunding)
   - Update order status to PAYMENT_CONFIRMED

3. **Order Processing**:
   - Process order items (compensate by cancelling processing)
   - Update order status to PROCESSING

4. **Shipping Coordination**:
   - Create shipment request (compensate by cancelling shipment)
   - Update order status to SHIPPED

## Event-Driven Integration

The Order Service publishes and consumes events for integration:

### Published Events

- `OrderCreatedEvent`
- `OrderConfirmedEvent`
- `OrderCancelledEvent`
- `OrderStatusChangedEvent`
- `PaymentRequestedEvent`

### Consumed Events

- `PaymentConfirmedEvent`
- `PaymentFailedEvent`
- `InventoryReservedEvent`
- `InventoryReservationFailedEvent`
- `ShipmentCreatedEvent`
- `ShipmentUpdatedEvent`

## Security

- JWT-based authentication
- Role-based authorization (CUSTOMER, ADMIN, VENDOR)
- Order ownership validation
- Secure communication between services

## Database Schema

- **Orders**: Main order information (status, dates, totals)
- **OrderItems**: Individual items in an order
- **OrderHistory**: Audit trail of status changes
- **OrderPayments**: Payment information
- **OrderShipping**: Shipping details
- **OrderTaxes**: Tax calculations
- **OrderDiscounts**: Applied discounts

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose
- Kafka
- PostgreSQL/CockroachDB

### Setup

1. Configure database and Kafka in `application.yaml`
2. Initialize the database:
   ```bash
   cat src/main/resources/order_service_sql.sql | psql -U postgres
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Docker

```bash
# Build Docker image
docker build -t order-service .

# Run with Docker Compose
docker-compose up -d
```

## Documentation

- Swagger UI: `/swagger-ui.html` (when application is running)
- Database schema: See `src/main/resources/order_service_sql.sql`
