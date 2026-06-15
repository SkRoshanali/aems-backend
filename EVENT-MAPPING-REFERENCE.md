# AEMS Business Event → RAG Mapping Reference

## 📋 Complete Event Mapping Table

| Business Event | Spring Boot Service | RAG Content Generated | Visibility Tag | Accessible By |
|---------------|-------------------|---------------------|---------------|--------------|
| **Buyer Registration** | BuyerService.registerBuyer() | "Buyer {name} submitted application from {city}, {country}. Company: {company}. Status: Pending approval." | `management` | MANAGER, ADMIN, SUPER_ADMIN |
| **Buyer Approval** | BuyerService.approveBuyer() | "Buyer {name} (Company: {company}) approved on {date}. Account activated. Location: {city}, {country}." | `buyer:{id}` | Specific BUYER + MANAGER+ |
| **Buyer Rejection** | BuyerService.rejectBuyer() | "Buyer {name} application rejected on {date}. Reason: {reason}." | `management` | MANAGER, ADMIN, SUPER_ADMIN |
| **Stock Creation** | StockService.createStock() | "Stock batch {batch}: {quantity} {unit} of {crop} available. Quality: {grade}. Price: ${price} per {unit}. From {source}." | `public` | ALL ROLES |
| **Stock Deactivation** | StockService.deactivateStock() | "Stock batch {batch} deactivated. No longer available for order." | `internal` | EMPLOYEE+ |
| **Order Placement** | OrderService.createOrder() | "Order #{number}: Buyer {name} placed order for {qty} {unit} of {crop} at ${price} per unit. Total: ${total}. Delivery to: {city}, {country}." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |
| **Order Approval** | OrderService.approveOrder() | "Order #{number} approved by {approver} on {date}. Order for {qty} {unit} of {crop}. Stock deducted. Ready for shipment." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |
| **Order Rejection** | OrderService.rejectOrder() | "Order #{number} rejected by {approver}. Reason: {reason}. Buyer: {name}." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |
| **Order Status Change** | OrderService.updateOrderStatus() | "Order #{number} status updated to {status} at {time}. Current stage: {description}." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |
| **Shipment Created** | ShipmentService.createShipment() | "Shipment {tracking}: {qty} {unit} of {crop} shipped to {buyer}. Carrier: {carrier}. Expected delivery: {date}." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |
| **Farmer Added** | FarmerService.createFarmer() | "Farmer {name} registered. Location: {area}. Specializes in: {crops}. Quality grade: {grade}." | `internal` | EMPLOYEE, MANAGER, ADMIN |
| **Crop Added** | CropService.createCrop() | "New crop category: {name}. Description: {desc}. Typical grades: {grades}." | `public` | ALL ROLES |
| **Invoice Generated** | InvoiceService.generateInvoice() | "Invoice #{number} generated for order #{order}. Amount: ${total}. Due date: {date}. Buyer: {name}." | `buyer:{id}` + `management` | Specific BUYER + MANAGER+ |

---

## 🎯 Visibility Level Details

### `public`
- **Who sees it:** ALL authenticated users (BUYER, EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN)
- **Use cases:** Product catalog, general FAQs, onboarding guides, available stock
- **Examples:** Stock availability, crop categories, pricing information

### `internal`
- **Who sees it:** EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN
- **Use cases:** Operational procedures, farmer information, internal guidelines
- **Examples:** Farmer database, stock management procedures, quality grading standards

### `management`
- **Who sees it:** MANAGER, ADMIN, SUPER_ADMIN
- **Use cases:** Buyer applications, approval workflows, business analytics
- **Examples:** Pending buyer applications, order approval queues, revenue reports

### `buyer:{buyer_id}`
- **Who sees it:** Specific BUYER (identified by ID) + MANAGER, ADMIN, SUPER_ADMIN
- **Use cases:** Personal order history, application status, invoices
- **Examples:** "Your order #12345", "Your application approved", "Your invoice"

### Multi-visibility (using `visibility_secondary`)
Some events are tagged with multiple visibility levels:
```java
metadata.put("visibility", "buyer:123");
metadata.put("visibility_secondary", "management");
```

This means:
- Buyer #123 can see it
- All MANAGERs can see it
- All ADMINs can see it

---

## 🔄 Python Filter Logic

```python
def build_filter(role: str, buyer_id: str = None, buyer_status: str = None):
    if role == "BUYER" and buyer_status == "PENDING":
        # Pending buyers: only onboarding and catalog
        return {"visibility": "public"}
    
    if role == "BUYER" and buyer_status == "ACCEPTED":
        # Active buyers: catalog + their own documents
        return {
            "$or": [
                {"visibility": "public"},
                {"visibility": f"buyer:{buyer_id}"}
            ]
        }
    
    if role == "EMPLOYEE":
        # Employees: internal procedures + catalog
        return {
            "$or": [
                {"visibility": "internal"},
                {"visibility": "public"}
            ]
        }
    
    if role == "MANAGER":
        # Managers: internal + management + catalog
        return {
            "$or": [
                {"visibility": "internal"},
                {"visibility": "management"},
                {"visibility": "public"}
            ]
        }
    
    if role in ["ADMIN", "SUPER_ADMIN"]:
        # Admins: everything
        return None  # No filter
```

---

## 💡 Example Queries by Role

### BUYER (PENDING) asks: "What products do you have?"
**Retrieves:**
- Stock creation events tagged `public`
- Crop catalog entries tagged `public`
- General product information

**Cannot see:**
- Other buyers' orders
- Internal farmer information
- Management dashboards

---

### BUYER (ACCEPTED, ID=123) asks: "What's the status of my orders?"
**Retrieves:**
- Their own order events tagged `buyer:123`
- Stock information tagged `public`
- Their application approval event

**Cannot see:**
- Other buyers' orders (buyer:456, buyer:789)
- Internal procedures
- Management discussions

---

### EMPLOYEE asks: "Which farmers supply rice?"
**Retrieves:**
- Farmer creation events tagged `internal`
- Stock creation events showing farmer sources
- Crop information tagged `public`

**Cannot see:**
- Buyer application details (management)
- Other buyers' orders (buyer:123)

---

### MANAGER asks: "Show me pending buyer applications"
**Retrieves:**
- Buyer registration events tagged `management`
- Application status updates
- All internal procedures
- All public information

**Cannot see:**
- Nothing (has broad access)

---

### ADMIN asks: "Give me a summary of today's activity"
**Retrieves:**
- EVERYTHING (no filter applied)
- All business events across all visibility levels

---

## 📊 Metadata Schema

Every ingested document includes these metadata fields:

```json
{
  "visibility": "public|internal|management|buyer:{id}",
  "event_type": "buyer_application|stock_created|order_placed|...",
  "status": "pending|approved|rejected|active|...",
  "buyer_id": "123",
  "order_id": "456",
  "stock_id": "789",
  "crop_name": "Basmati Rice",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🔍 Query Examples

### "What products are available?"
**Metadata filter:** `visibility: "public"`
**Retrieved documents:**
- All stock creation events
- Crop catalog entries
- Product descriptions

**LLM Response:**
"We currently have the following products available:
- Organic Basmati Rice: 1000kg at $50/kg, Premium grade
- Yellow Corn: 2000kg at $30/kg, Grade A
- Wheat Flour: 500kg at $25/kg, Standard grade"

---

### "Show me my recent orders" (Buyer ID=123)
**Metadata filter:** `visibility: "buyer:123" OR "public"`
**Retrieved documents:**
- Order #ORD-12345 placement event
- Order #ORD-12345 approval event
- Invoice #INV-001 for order #ORD-12345

**LLM Response:**
"Your recent orders:
1. Order #ORD-12345: 100kg Basmati Rice, Status: Approved, Delivery to New York
2. Order #ORD-12346: 50kg Corn, Status: Pending approval"

---

### "Which buyers applied this week?" (Manager)
**Metadata filter:** `visibility: "management" OR "internal" OR "public"`
**Retrieved documents:**
- Buyer application events from this week
- Approval/rejection events

**LLM Response:**
"This week, 5 buyers applied:
1. John Doe (ACME Corp, New York) - Pending
2. Jane Smith (GlobalTrade LLC, London) - Approved
3. ..."

---

## 🚀 Adding New Events

To add a new business event to RAG:

### 1. Identify the Event
Example: "Admin updates crop pricing"

### 2. Choose Visibility Level
- Who should see this? → EMPLOYEE+ (internal)

### 3. Add to Service
```java
// In CropService.updatePrice()
String ragContent = String.format(
    "Crop %s price updated to $%s per %s. Effective from %s. Updated by %s.",
    crop.getName(),
    newPrice,
    crop.getUnit(),
    LocalDateTime.now(),
    admin.getFullName()
);

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "internal");
metadata.put("crop_id", crop.getId().toString());
metadata.put("event_type", "price_update");
metadata.put("status", "active");

ragClient.ingestDocument(ragContent, metadata);
```

### 4. Test with Different Roles
- BUYER: Should NOT see price updates
- EMPLOYEE: Should see price updates
- MANAGER: Should see price updates

---

## ✅ Best Practices

1. **Keep content descriptive but concise** (500-1000 chars ideal)
2. **Include key searchable terms** (buyer name, crop name, order number)
3. **Use consistent metadata keys** across similar events
4. **Tag time-sensitive info** with timestamps
5. **Include context** (who, what, when, where, why)
6. **Don't duplicate** - one event, one document
7. **Update vs Create** - For status changes, create new document (shows history)

---

## 🔒 Security Notes

1. **Never put sensitive data** in RAG content:
   - ❌ Passwords
   - ❌ API keys
   - ❌ Full credit card numbers
   - ❌ SSNs or tax IDs

2. **Metadata is NOT encrypted** - don't store secrets there

3. **Trust the filter** - Python service enforces role-based access

4. **Audit trail** - Every query can be logged with user role

---

## 📈 Analytics Use Cases

Track these metrics in `query_history` table:

1. **Popular queries by role**
   - What do BUYERs ask most?
   - What do MANAGERs need help with?

2. **Knowledge gaps**
   - Queries with no results
   - Low-confidence answers

3. **Content quality**
   - Which documents are retrieved most?
   - Which are never used?

4. **Response time**
   - Average time to answer
   - Slow queries needing optimization

---

This reference should be your go-to guide when:
- Adding new business events
- Debugging visibility issues
- Understanding what each role can access
- Testing RAG query responses
