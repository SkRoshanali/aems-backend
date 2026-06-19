# 🔧 RAG Integration Fixes for Missing Services

This document provides the exact code needed to add RAG integration to services that are currently missing it.

---

## 📋 Summary of Missing Integrations

| Service | Issue | Status |
|---------|-------|--------|
| ShipmentService | No RAG ingestion on shipment events | ❌ NEEDS FIX |
| InvoiceService | No RAG ingestion on invoice creation | ❌ NEEDS FIX |
| FarmerService | No RAG ingestion on farmer registration | ❌ NEEDS FIX |
| ImportSourceService | No RAG ingestion on source added | ❌ NEEDS FIX |

---

## 🔧 Fix #1: Add RAG to ShipmentService

### Location
`aems-backend/src/main/java/com/aems/service/ShipmentService.java`

### Current State
- Has email notifications ✅
- NO RAG integration ❌

### Required Change

**Step 1:** Add import at top of file
```java
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;
```

**Step 2:** Add @Autowired field
```java
@Autowired
private RagIngestionClient ragClient;
```

**Step 3:** Add RAG ingestion after saveShipment in createShipment() method

**After line 59:** `Shipment savedShipment = shipmentRepository.save(shipment);`

**Add this code:**
```java
// ✅ NEW: RAG INGESTION - Push shipment event to knowledge base
String ragContent = String.format(
    "Order %s has been shipped. " +
    "Tracking Number: %s. Carrier: %s. " +
    "Shipped Date: %s. Estimated Delivery: %s. " +
    "Status: %s. Customer: %s.",
    order.getOrderNumber(),
    savedShipment.getTrackingNumber(),
    savedShipment.getCarrier(),
    savedShipment.getShippedDate(),
    savedShipment.getEstimatedDelivery(),
    savedShipment.getStatus(),
    order.getBuyer().getFullName()
);

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "public");  // All roles can see
metadata.put("buyer_id", order.getBuyer().getId().toString());
metadata.put("order_id", order.getId().toString());
metadata.put("event_type", "shipment_created");

ragClient.ingestDocument(ragContent, metadata);
```

**Step 4:** Add RAG ingestion for status updates

**After line 80:** In updateShipmentStatus() method, after saving:

```java
// ✅ NEW: RAG INGESTION - Push status update
String statusContent = String.format(
    "Shipment %s status updated to %s. " +
    "Tracking: %s. Last update: %s.",
    shipment.getId(),
    status,
    shipment.getTrackingNumber(),
    LocalDateTime.now()
);

Map<String, String> statusMetadata = new HashMap<>();
statusMetadata.put("visibility", "public");
statusMetadata.put("order_id", shipment.getOrder().getId().toString());
statusMetadata.put("event_type", "shipment_status_updated");

ragClient.ingestDocument(statusContent, statusMetadata);
```

---

## 🔧 Fix #2: Add RAG to InvoiceService

### Location
`aems-backend/src/main/java/com/aems/service/InvoiceService.java`

### Required Changes

**Step 1:** Add imports
```java
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;
```

**Step 2:** Add @Autowired
```java
@Autowired
private RagIngestionClient ragClient;
```

**Step 3:** Add RAG ingestion in createInvoice() method after save

**Pattern:**
```java
// ✅ NEW: RAG INGESTION - Push invoice event
String ragContent = String.format(
    "Invoice %s created for Order %s. " +
    "Amount: %s. Due Date: %s. " +
    "Status: %s. Customer: %s.",
    savedInvoice.getInvoiceNumber(),
    order.getOrderNumber(),
    savedInvoice.getTotalAmount(),
    savedInvoice.getDueDate(),
    savedInvoice.getStatus(),
    order.getBuyer().getFullName()
);

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "management");  // Only management
metadata.put("buyer_id", order.getBuyer().getId().toString());
metadata.put("order_id", order.getId().toString());
metadata.put("event_type", "invoice_created");

ragClient.ingestDocument(ragContent, metadata);
```

---

## 🔧 Fix #3: Add RAG to FarmerService

### Location
`aems-backend/src/main/java/com/aems/service/FarmerService.java`

### Required Changes

**Step 1:** Add imports
```java
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;
```

**Step 2:** Add @Autowired
```java
@Autowired
private RagIngestionClient ragClient;
```

**Step 3:** Add RAG ingestion in registerFarmer() or createFarmer() method

**Pattern:**
```java
// ✅ NEW: RAG INGESTION - Push farmer event
String ragContent = String.format(
    "New farmer registered: %s. " +
    "Location: %s, %s. Phone: %s. " +
    "Crops: %s. Registration Date: %s.",
    farmer.getFullName(),
    farmer.getCity(),
    farmer.getCountry(),
    farmer.getPhoneNumber(),
    farmer.getCropsGrown() != null ? farmer.getCropsGrown() : "Not specified",
    farmer.getCreatedAt()
);

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "employee");  // Employees can see
metadata.put("farmer_id", farmer.getId().toString());
metadata.put("event_type", "farmer_registered");

ragClient.ingestDocument(ragContent, metadata);
```

---

## 🔧 Fix #4: Add RAG to ImportSourceService

### Location
`aems-backend/src/main/java/com/aems/service/ImportSourceService.java`

### Required Changes

**Step 1:** Add imports
```java
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;
```

**Step 2:** Add @Autowired
```java
@Autowired
private RagIngestionClient ragClient;
```

**Step 3:** Add RAG ingestion in create/add method

**Pattern:**
```java
// ✅ NEW: RAG INGESTION - Push import source event
String ragContent = String.format(
    "New import source added: %s. " +
    "Country: %s. Contact: %s. " +
    "Specializes in: %s. Added on: %s.",
    importSource.getSourceName(),
    importSource.getCountry(),
    importSource.getContactEmail(),
    importSource.getSpecialization(),
    importSource.getCreatedAt()
);

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "management");  // Only management
metadata.put("source_id", importSource.getId().toString());
metadata.put("event_type", "import_source_added");

ragClient.ingestDocument(ragContent, metadata);
```

---

## ✅ Testing the Fixes

### After applying fixes, verify:

1. **Services compile without errors**
   ```bash
   mvn clean compile
   ```

2. **Test RAG ingestion with each service**
   - Create a new shipment → Check RAG knowledge base
   - Create an invoice → Check RAG knowledge base
   - Register a farmer → Check RAG knowledge base
   - Add import source → Check RAG knowledge base

3. **Query RAG with new information**
   ```bash
   curl -X POST http://localhost:8000/api/rag/query \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT" \
     -d '{"query": "Show me recent shipments"}'
   ```

---

## 🔍 Visibility Levels Reference

| Level | Who Can See | Use When |
|-------|------------|----------|
| `public` | Everyone | Stock availability, general info |
| `employee` | Employees + Managers + Admins | Procedures, processes |
| `management` | Managers + Admins | Buyer applications, financial data |
| `admin` | Admins only | System operations, sensitive data |
| `buyer:{id}` | Specific buyer + admins | Order details, buyer-specific info |

---

## 📝 Pattern for New Services

When adding RAG integration to any new service, follow this pattern:

```java
// 1. Import
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;

// 2. Inject
@Autowired
private RagIngestionClient ragClient;

// 3. Ingest after entity creation
String ragContent = "Human-readable description...";

Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "appropriate_level");
metadata.put("entity_id", entity.getId().toString());
metadata.put("event_type", "event_name");

ragClient.ingestDocument(ragContent, metadata);

// That's it! RAG will index it automatically.
```

---

## 🚀 Deployment Impact

**After these fixes:**
- ✅ All business events feed the RAG system
- ✅ Knowledge base is complete
- ✅ Users can query about any business entity
- ✅ Better AI responses
- ✅ Complete audit trail

**No breaking changes:**
- Backward compatible
- Existing functionality unchanged
- Only adds RAG ingestion
- Can be deployed independently

---

## 🔄 Next Steps

1. Apply each fix to corresponding service file
2. Run `mvn clean compile` to verify
3. Deploy backend
4. Test each service creates RAG documents
5. Query RAG to verify data is indexed
6. Monitor RAG performance with new volume

---

## 📞 Quick Reference

**Files to Modify:**
- `ShipmentService.java` - Add after line 59 and line 80
- `InvoiceService.java` - Add after invoice save
- `FarmerService.java` - Add after farmer save
- `ImportSourceService.java` - Add after source save

**Import needed in all:**
```java
import com.aems.rag.client.RagIngestionClient;
import java.util.HashMap;
import java.util.Map;
```

**Autowired in all:**
```java
@Autowired
private RagIngestionClient ragClient;
```

---

**All fixes are ready to implement! These will ensure complete knowledge base coverage.**
