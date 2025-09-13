# 📊 **Centralized Logging with Graylog**

## **What We've Built**

Your e-commerce platform now has **enterprise-level centralized logging**:

```
6 Microservices → Structured Logs → Graylog → Search/Filter/Alert
```

## **🚀 Quick Start**

### **1. Start Graylog Stack**
```bash
# Start logging infrastructure
docker-compose up -d mongo elasticsearch graylog

# Wait 2 minutes for Graylog to initialize
sleep 120
```

### **2. Access Graylog Web UI**
- **URL**: http://localhost:9000
- **Username**: `admin`
- **Password**: `admin`

### **3. Start Your Services**
```bash
# Option A: With Docker (logs go to Graylog)
docker-compose up -d

# Option B: IntelliJ (logs to console with correlation IDs)
# Run each service individually in IntelliJ
```

## **🔍 What You'll See**

### **Structured Logs with Context**
```json
{
  "timestamp": "2024-01-15T10:00:01.234Z",
  "level": "INFO",
  "service_name": "order-service",
  "service_port": "8081", 
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "logger": "com.example.order_service.service.OrderService",
  "message": "Order created successfully: order-123",
  "environment": "docker"
}
```

### **Complete Request Tracing**
Follow a single order across all services:
```
[a1b2c3d4] order-service    → Order 123 created
[a1b2c3d4] inventory-service → Stock reserved for order 123
[a1b2c3d4] payment-service  → Payment processed for order 123  
[a1b2c3d4] shipping-service → Shipment created for order 123
[a1b2c3d4] notification-service → SMS sent for order 123
```

## **🔧 Graylog Setup (First Time)**

### **1. Create GELF UDP Input**
1. Go to **System** → **Inputs**
2. Select **GELF UDP** from dropdown
3. Click **Launch new input**
4. Configure:
   - **Title**: `E-Commerce Services`
   - **Port**: `12201`
   - **Bind address**: `0.0.0.0`
5. Click **Save**

### **2. Create Search Streams** 
1. Go to **Streams**
2. Create streams for each service:
   - **Order Service Stream**: `service_name:order-service`
   - **Payment Service Stream**: `service_name:payment-service`
   - etc.

### **3. Set Up Dashboards**
Create dashboards for:
- **Service Health**: Error rates, response times
- **Business Metrics**: Orders created, payments processed
- **Performance**: Slow queries, high memory usage

## **📈 Key Searches**

### **Find All Logs for One Order**
```
correlationId:"a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

### **Find Errors in Last Hour**
```
level:ERROR AND timestamp:[now-1h TO now]
```

### **Find Slow Operations (> 1 second)**
```
message:*ms AND message:/[1-9][0-9]{3,}/
```

### **Payment Service Errors**
```
service_name:payment-service AND level:ERROR
```

### **Low Stock Alerts**
```
message:"Low stock alert" OR message:"SMS SENT"
```

## **🚨 Alerting Setup**

### **1. Payment Failures**
- **Condition**: `service_name:payment-service AND level:ERROR`
- **Threshold**: More than 5 in 5 minutes
- **Notification**: Email to team

### **2. Service Down**
- **Condition**: `service_name:order-service`
- **Threshold**: No messages in 2 minutes  
- **Notification**: Slack alert

### **3. High Error Rate**
- **Condition**: `level:ERROR`
- **Threshold**: More than 10 errors in 1 minute
- **Notification**: PagerDuty

## **🔍 Development vs Production**

### **Local Development (IntelliJ)**
```
10:30:15.123 [http-nio-8081-exec-1] INFO  [ORDER-SERVICE] [a1b2c3d4] o.e.order_service.OrderService - Order created: 123
```

### **Production (Docker + Graylog)**  
```
All logs centralized in Graylog with:
✅ Service identification
✅ Correlation ID tracing  
✅ Structured fields for searching
✅ Performance metrics
✅ Error stack traces
```

## **🐛 Troubleshooting**

### **Logs Not Appearing in Graylog**
```bash
# Check Graylog is running
curl http://localhost:9000

# Check GELF input is active
docker-compose logs graylog

# Test GELF connectivity
echo '{"version":"1.1","host":"test","short_message":"test"}' | nc -w 1 -u localhost 12201
```

### **Service Can't Connect to Graylog**
```bash
# Check network connectivity from service container
docker-compose exec order-service ping graylog
```

## **📊 Advanced Features**

### **Custom Fields**
Each log includes:
- `service_name` - Which microservice
- `service_port` - Service port number
- `correlationId` - Request tracing
- `environment` - dev/staging/production
- `level` - DEBUG/INFO/WARN/ERROR

### **Performance Monitoring**
- Method execution times (via LoggingAspect)
- HTTP request/response logging
- Database query timing
- Kafka message processing time

### **Business Intelligence**
- Orders created per hour
- Payment success rates
- Low stock alert frequency
- Customer notification delivery rates

## **🎯 Laravel vs Spring Boot Comparison**

### **Laravel (What You Know)**
```php
Log::info('Order created', ['orderId' => $orderId]);
Log::channel('slack')->error('Payment failed');
```

### **Spring Boot (What You Have Now)**
```java
@Slf4j
public class OrderService {
    log.info("Order created: {}", orderId);           // Basic logging
    MDC.put("orderId", orderId);                      // Context
    log.error("Payment failed for order: {}", orderId, exception);
}
```

## **🚀 Next Steps**

1. **Start services and create test orders**
2. **Watch logs flow into Graylog in real-time**
3. **Create custom dashboards for your metrics**
4. **Set up alerts for critical events**
5. **Use correlation IDs to trace complex flows**

---

## **🎉 Congratulations!**

You now have **enterprise-grade centralized logging** that will make debugging your distributed system incredibly easy. This is exactly what companies like Netflix, Uber, and Amazon use for their microservices!

**Graylog UI**: http://localhost:9000 (admin/admin)