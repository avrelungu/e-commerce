# 🚀 Event-Driven E-Commerce Microservices Platform

A complete **production-ready** e-commerce platform built with **Spring Boot**, **Apache Kafka**, and **event-driven architecture**. Features inventory management, payment processing, shipping, and real-time notifications through asynchronous event processing.

## 🏗️ **Architecture Overview**

### **Microservices**
| Service | Port | Purpose | Key Features |
|---------|------|---------|-------------|
| **API Gateway** | 8080 | Single entry point | Rate limiting, Circuit breakers, Routing |
| **Order Service** | 8081 | Order lifecycle management | State machine validation, Event publishing |
| **Inventory Service** | 8082 | Stock & catalog management | Auto-timeout reservations, Low stock alerts |
| **Payment Service** | 8083 | Payment processing | Retry logic, Mock gateway, Refunds |
| **Shipping Service** | 8084 | Shipment & tracking | Real-time tracking, Auto-creation |
| **Notification Service** | 8085 | Email/SMS notifications | Manager alerts, Customer updates |

### **Infrastructure**
- **Apache Kafka** - Event streaming & messaging
- **PostgreSQL** - Separate database per service
- **Redis** - Caching & idempotency
- **Schema Registry** - Avro schema management
- **Docker** - Containerized deployment

## 📋 **Prerequisites**

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose**
- **8GB RAM** minimum
- **Ports 8080-8090, 5432-5436, 6379, 9092** available

## 🚀 **Quick Start**

### **1. Clone & Build**
```bash
git clone <repository-url>
cd e-commerce
mvn clean package -DskipTests
```

### **2. Start Infrastructure**
```bash
# Start databases, Kafka, Redis
docker-compose up -d redis kafka zookeeper schema-registry
docker-compose up -d postgres-order postgres-inventory postgres-payment postgres-shipping postgres-notifications
```

### **3. Initialize Databases**
```bash
# Wait 30 seconds for databases to start
sleep 30

# Run schema migrations
docker exec -i postgres-order psql -U myuser -d order_service < order_service/src/main/resources/schema.sql
docker exec -i postgres-inventory psql -U myuser -d inventory_service < inventory_service/src/main/resources/schema.sql
docker exec -i postgres-payment psql -U myuser -d payment_service < payment-service/src/main/resources/schema.sql
docker exec -i postgres-shipping psql -U myuser -d shipping_service < shipping-service/src/main/resources/schema.sql
docker exec -i postgres-notifications psql -U myuser -d notification_service < notification-service/src/main/resources/schema.sql
```

### **4. Start All Services**
```bash
# Option A: Docker (Recommended)
docker-compose up -d

# Option B: Local Development
# Terminal 1: mvn spring-boot:run -f api-gateway/pom.xml
# Terminal 2: mvn spring-boot:run -f order_service/pom.xml
# Terminal 3: mvn spring-boot:run -f inventory_service/pom.xml
# Terminal 4: mvn spring-boot:run -f payment-service/pom.xml
# Terminal 5: mvn spring-boot:run -f shipping-service/pom.xml
# Terminal 6: mvn spring-boot:run -f notification-service/pom.xml
```

### **5. Verify System Health**
```bash
# Check all services are healthy
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Order Service
curl http://localhost:8082/actuator/health  # Inventory Service
curl http://localhost:8083/actuator/health  # Payment Service
curl http://localhost:8084/actuator/health  # Shipping Service
curl http://localhost:8085/actuator/health  # Notification Service
```

## 📊 **System URLs**

### **API Access**
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8081/swagger-ui.html (for each service)

### **Infrastructure**
- **Kafka UI**: http://localhost:9092 (if using Kafka UI)
- **Schema Registry**: http://localhost:8090

### **Health Checks**
- **Gateway Health**: http://localhost:8080/actuator/health
- **All Services**: http://localhost:808[1-5]/actuator/health

## 🔄 **Complete Order Flow Demo**

### **1. Create an Order**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: demo-order-001" \
  -d '{
    "customerId": "customer-123",
    "items": [
      {
        "productId": "product-456",
        "quantity": 2,
        "price": 29.99
      }
    ],
    "shippingAddress": {
      "street": "123 Main St",
      "city": "Demo City", 
      "zipCode": "12345"
    }
  }'
```

### **2. Watch the Event Flow**
```bash
# Monitor logs to see the complete flow:
# Order Created → Stock Reserved → Payment Processed → Shipment Created → Delivered

docker-compose logs -f order-service inventory-service payment-service shipping-service notification-service
```

### **3. Track Order Status**
```bash
# Get order status (replace {orderId} with actual ID)
curl http://localhost:8080/api/orders/{orderId}
```

## 🎯 **Key Features Demonstrated**

### **✅ Event-Driven Architecture**
- **Asynchronous processing** between all services
- **Event sourcing** for complete audit trail
- **Eventual consistency** across the system

### **✅ Resilience Patterns**
- **Circuit breakers** on critical services
- **Retry logic** with exponential backoff
- **Dead letter queues** for failed messages
- **Idempotency** to prevent duplicate processing

### **✅ Business Logic**
- **15-minute stock reservations** with auto-timeout
- **Order state machine** with validation
- **Per-product low stock thresholds**
- **SMS alerts to managers** for low inventory

### **✅ Production Ready**
- **Rate limiting** on API Gateway
- **Health checks** for all services
- **Centralized logging** and monitoring
- **Docker containerization**

## 🚀 **Congratulations!** 

You now have a **production-ready, event-driven e-commerce platform** that demonstrates enterprise-level microservices architecture. This system showcases:

- ✅ Complete order-to-delivery flow
- ✅ Real-time notifications  
- ✅ Resilient error handling
- ✅ Scalable architecture
- ✅ Production deployment ready

**Ready to ship! 🚀**
