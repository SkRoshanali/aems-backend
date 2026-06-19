# 📋 PHASE-BY-PHASE ACTION PLAN

**Total Issues Found:** 24  
**Status:** Analysis complete, ready for implementation  
**Time to Production:** ~7 hours (spread across 4 phases)

---

## ⚡ PHASE 1: CRITICAL (MUST FIX - 1 Hour)

**Goal:** Make system deployable (compilation + core functionality)

### ✅ Fix #1: Java Main Method Parameter
**Status:** ALREADY FIXED ✅  
**File:** `AemsApplication.java`

### ✅ Fix #2: Python Database URL
**Status:** ALREADY FIXED ✅  
**File:** `aems-rag-service/.env.example`

### ⏳ Fix #3: OrderService Metadata Structure (15 min)
**File:** `aems-backend/src/main/java/com/aems/service/OrderService.java`  
**Lines:** 75-78

**Current (BROKEN):**
```java
Map<String, String> metadata = new HashMap<>();
metadata.put("visibility", "buyer:" + buyer.getId());
metadata.put("visibility_secondary", "management");  // ❌ Ignored
```

**Fixed:**
```java
Map<String, String> metadata = new HashMap<>();
// Use comma-separated visibility rules
String visibility = "buyer:" + buyer.getId() + ",management";
metadata.put("visibility", visibility);
metadata.put("buyer_id", buyer.getId().toString());
metadata.put("order_id", savedOrder.getId().toString());
metadata.put("event_type", "order_placed");
metadata.put("status", "pending");
```

**Verify:** Update Python filter in `rag_service.py` to parse comma-separated values

### ⏳ Fix #6: Python Query Validation (10 min)
**File:** `aems-rag-service/app/routers/rag_router.py`  
**Lines:** 14-19

**Add field validation:**
```python
class QueryRequest(BaseModel):
    query: str = Field(..., min_length=1, max_length=500)
    role: str = Field(..., min_length=1, max_length=50)
    buyer_id: Optional[str] = Field(None, max_length=50)
    buyer_status: Optional[str] = Field(None, max_length=50)
```

**Test:** Try sending 10KB query, should fail validation

### ⏳ Fix #5: Python Schema Retry Logic (20 min)
**File:** `aems-rag-service/app/services/ingestion_service.py`  
**Lines:** 28-40

**Add resilience:**
```python
from tenacity import retry, stop_after_attempt, wait_exponential

class IngestionService:
    def __init__(self):
        self.ensure_schema()
    
    @retry(
        stop=stop_after_attempt(5),
        wait=wait_exponential(multiplier=1, min=2, max=10)
    )
    def ensure_schema(self) -> None:
        try:
            with self.connect() as conn:
                # ... existing code
        except Exception as e:
            logger.error(f"Schema creation failed, retrying: {e}")
            raise  # Let retry decorator handle it
```

**Add to requirements.txt:**
```
tenacity==8.2.3
```

### ⏳ Fix #4: Verify Compilation (5 min)
```bash
cd aems-backend
mvn clean compile
# Should succeed with no errors
```

---

## 🎯 PHASE 1 CHECKLIST

- [ ] Fix #3: Update OrderService metadata (test parsing)
- [ ] Fix #6: Add validation to QueryRequest
- [ ] Fix #5: Add retry logic to ensure_schema
- [ ] Add tenacity to requirements.txt
- [ ] Run `mvn clean compile` - must pass
- [ ] Test: `mvn clean package` - creates JAR
- [ ] Commit with message: "Fix critical issues for Phase 1"
- [ ] **READY FOR DEPLOYMENT PHASE 1**

**Time: ~1 hour**

---

## 🟠 PHASE 2: HIGH PRIORITY (Before Production - 1.5 Hours)

**Goal:** Make system production-grade (error handling, security)

### ⏳ Fix #4: Health Check Model Name (5 min)
**File:** `aems-rag-service/app/routers/rag_router.py`  
**Lines:** 87-91

```python
from ..config import settings

@router.get("/health")
async def rag_health():
    return {
        "status": "healthy",
        "service": "rag_query",
        "llm_model": settings.GOOGLE_CHAT_MODEL,  # Use actual config
        "embedding_model": settings.GOOGLE_EMBEDDING_MODEL
    }
```

### ⏳ Fix #7: Exception Type Handling (15 min)
**File:** `aems-backend/src/main/java/com/aems/rag/client/RagIngestionClient.java`

Create custom exceptions:
```java
// New file: aems-backend/src/main/java/com/aems/rag/exception/RagServiceException.java
public class RagServiceException extends RuntimeException {
    public RagServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// New file: aems-backend/src/main/java/com/aems/rag/exception/RagServiceUnavailableException.java
public class RagServiceUnavailableException extends RuntimeException {
    public RagServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Update RagIngestionClient:
```java
catch (ResourceAccessException e) {
    logger.warn("RAG service timeout, would retry...");
    throw new RagServiceUnavailableException("RAG service not responding", e);
} catch (HttpStatusCodeException e) {
    throw new RagServiceException("RAG service error: " + e.getStatusCode(), e);
}
```

### ⏳ Fix #8: CORS Configuration (10 min)
**File:** `aems-backend/src/main/resources/application.properties`

Replace line 52:
```properties
# OLD
spring.web.cors.allowed-origins=${ALLOWED_ORIGINS:-*}

# NEW
spring.web.cors.allowed-origins=https://your-frontend.azurestaticapps.net,http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
spring.web.cors.allowed-headers=Authorization,Content-Type
spring.web.cors.allow-credentials=true
spring.web.cors.max-age=3600
```

### ⏳ Fix #9: Auth Logging (10 min)
**File:** `aems-rag-service/app/auth.py`

```python
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

def verify_internal(x_internal_secret: str | None) -> None:
    if x_internal_secret != settings.INTERNAL_SECRET:
        logger.warning(
            f"SECURITY: Invalid RAG authentication attempt at {datetime.utcnow()}. "
            f"Provided secret: {x_internal_secret[:5] if x_internal_secret else 'None'}..."
        )
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden",
        )
    logger.info(f"RAG service authenticated successfully at {datetime.utcnow()}")
```

### ⏳ Fix #10: Retry Logic with Resilience4j (15 min)
**File:** `aems-backend/pom.xml`

Add dependency:
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

Update `RagIngestionClient.java`:
```java
import io.github.resilience4j.retry.annotation.Retry;

@Retry(name = "ragService", fallbackMethod = "ingestDocumentFallback")
public boolean ingestDocument(String content, Map<String, String> metadata) {
    try {
        // ... existing code
    } catch (Exception e) {
        logger.error("Failed to ingest document", e);
        return false;  // Let retry decorator handle
    }
}

public boolean ingestDocumentFallback(String content, Map<String, String> metadata, Exception e) {
    logger.error("RAG ingestion failed after retries", e);
    return false;
}
```

Add config in `application.properties`:
```properties
resilience4j.retry.instances.ragService.max-attempts=3
resilience4j.retry.instances.ragService.wait-duration=1000
resilience4j.retry.instances.ragService.retry-exceptions=org.springframework.web.client.ResourceAccessException
```

### ⏳ Fix #11: Connection Pooling (20 min)
**File:** `aems-rag-service/requirements.txt`

Add:
```
sqlalchemy==2.0.23
```

Create new file: `aems-rag-service/app/db_connection.py`:
```python
from sqlalchemy import create_engine
from sqlalchemy.pool import QueuePool
import logging
from .config import settings

logger = logging.getLogger(__name__)

engine = create_engine(
    settings.DATABASE_URL,
    poolclass=QueuePool,
    pool_size=10,
    max_overflow=5,
    pool_recycle=3600,
    pool_pre_ping=True,
    echo=False
)

def get_db_engine():
    return engine

def get_connection():
    return engine.raw_connection()
```

Update `ingestion_service.py` and `rag_service.py`:
```python
from ..db_connection import get_connection

def connect(self):
    return get_connection()  # Now pooled!
```

---

## 🎯 PHASE 2 CHECKLIST

- [ ] Fix #4: Update health check (verify with curl)
- [ ] Fix #7: Create exception classes, update RagIngestionClient
- [ ] Fix #8: Update CORS configuration for actual domain
- [ ] Fix #9: Add logging to auth.py (test with invalid secret)
- [ ] Fix #10: Add Resilience4j, test retry behavior
- [ ] Fix #11: Add SQLAlchemy, implement connection pooling
- [ ] Test locally: Send 10 parallel requests
- [ ] Run load test: `mvn clean package`
- [ ] Commit: "Add production-grade error handling and resilience"
- [ ] **READY FOR DEPLOYMENT PHASE 2**

**Time: ~1.5 hours**

---

## 🟡 PHASE 3: MEDIUM PRIORITY (Current Sprint - 2 Hours)

**Goal:** Add observability and optimization

### ⏳ Fix #12: Query Timeout (15 min)
**File:** `aems-rag-service/app/services/rag_service.py`

```python
import asyncio

async def generate_answer_with_timeout(self, query: str, chunks: List[str]) -> str:
    try:
        return await asyncio.wait_for(
            asyncio.to_thread(self._generate_answer_sync, query, chunks),
            timeout=30.0
        )
    except asyncio.TimeoutError:
        logger.error(f"LLM generation timeout for query: {query[:100]}")
        return "I took too long to process that. Please try a simpler question."

def _generate_answer_sync(self, query: str, chunks: List[str]) -> str:
    context = "\n\n---\n\n".join(chunks) if chunks else "No matching context found."
    prompt = f"""..."""
    response = client.models.generate_content(...)
    return response.text or "..."
```

### ⏳ Fix #13: Batch Processing Optimization (20 min)
**File:** `aems-rag-service/app/services/ingestion_service.py`

```python
def ingest_batch(self, documents: List[Dict]) -> Dict:
    try:
        all_chunks = []
        doc_metadata = []
        
        # Collect all chunks and metadata
        for doc in documents:
            chunks = chunk_text(
                doc.get("content", ""),
                settings.CHUNK_SIZE,
                settings.CHUNK_OVERLAP
            )
            for chunk in chunks:
                all_chunks.append(chunk)
                doc_metadata.append(doc.get("metadata", {}))
        
        if not all_chunks:
            return {"status": "error", "message": "No documents to ingest"}
        
        # Batch embed all chunks at once
        embeddings = self._batch_embed(all_chunks)
        
        # Insert all in one transaction
        with self.connect() as conn:
            with conn.cursor() as cur:
                for chunk, embedding, metadata in zip(all_chunks, embeddings, doc_metadata):
                    cur.execute(
                        "INSERT INTO document_chunks (content, embedding, visibility, metadata) VALUES (%s, %s::vector, %s, %s)",
                        (chunk, vector_literal(embedding), metadata.get("visibility", "public"), Json(metadata))
                    )
            conn.commit()
        
        logger.info(f"Batch ingested {len(documents)} documents, {len(all_chunks)} chunks")
        return {"status": "success", "documents_ingested": len(documents), "total_chunks": len(all_chunks)}
    except Exception as exc:
        logger.error(f"Batch ingestion failed: {exc}")
        return {"status": "error", "message": str(exc)}

def _batch_embed(self, texts: List[str]) -> List[List[float]]:
    # More efficient than embedding one by one
    embeddings = []
    # Process in chunks of 50 if needed (API limits)
    for i in range(0, len(texts), 50):
        batch = texts[i:i+50]
        result = client.models.embed_content(
            model=settings.GOOGLE_EMBEDDING_MODEL,
            contents=batch,
            config=types.EmbedContentConfig(output_dimensionality=768)
        )
        embeddings.extend([list(e.values) for e in result.embeddings])
    return embeddings
```

### ⏳ Fix #14: Vector Index Tuning (10 min)
**File:** `init-db.sql`

```sql
-- For current scale (<100k documents), IVFFLAT is more efficient
DROP INDEX IF EXISTS document_chunks_embedding_hnsw_idx;

CREATE INDEX IF NOT EXISTS document_chunks_embedding_idx
ON document_chunks USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- For future scale (>100k), consider:
-- WITH (lists = 1000)
```

### ⏳ Fix #15: Audit Logging (30 min)
**File:** `aems-rag-service/app/services/rag_service.py`

Create table first:
```sql
CREATE TABLE IF NOT EXISTS query_history (
    id BIGSERIAL PRIMARY KEY,
    user_role VARCHAR(50),
    query TEXT,
    chunks_count INT,
    latency_ms FLOAT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

Add logging:
```python
import time

class RAGService:
    def query(self, query: str, user_role: str, buyer_id: Optional[str] = None, buyer_status: Optional[str] = None) -> Dict:
        start_time = time.time()
        
        chunks = self.search_chunks(query, user_role, buyer_id, buyer_status)
        answer = await self.generate_answer_with_timeout(query, chunks)
        
        latency_ms = (time.time() - start_time) * 1000
        
        # Log to database
        self._log_query(user_role, query, len(chunks), latency_ms)
        
        logger.info(f"Query executed for {user_role}: {len(chunks)} chunks, {latency_ms:.0f}ms")
        
        return {"answer": answer, "sources": chunks}
    
    def _log_query(self, user_role: str, query: str, chunks_count: int, latency_ms: float) -> None:
        try:
            with self.connect() as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        "INSERT INTO query_history (user_role, query, chunks_count, latency_ms) VALUES (%s, %s, %s, %s)",
                        (user_role, query[:500], chunks_count, latency_ms)
                    )
                conn.commit()
        except Exception as e:
            logger.error(f"Failed to log query: {e}")
```

### ⏳ Fix #17: Circuit Breaker Pattern (20 min)
**File:** `aems-backend/src/main/resources/application.properties`

Add:
```properties
resilience4j.circuitbreaker.instances.ragService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.ragService.wait-duration-in-open-state=10000
resilience4j.circuitbreaker.instances.ragService.permitted-number-of-calls-in-half-open-state=3
```

Update `RagIngestionClient.java`:
```java
@CircuitBreaker(name = "ragService", fallbackMethod = "queryRagFallback")
@Retry(name = "ragService")
public Map<String, Object> queryRag(...) {
    // ... existing code
}

public Map<String, Object> queryRagFallback(..., IOException e) {
    logger.error("RAG service circuit breaker triggered", e);
    return new HashMap<>() {{
        put("error", "RAG service temporarily unavailable");
        put("answer", "I'm currently unavailable. Please try again later.");
        put("sources", new ArrayList<>());
    }};
}
```

### ⏳ Fix #18: Monitoring & Metrics (45 min)
Add Prometheus metrics:

**Spring Boot:** Add dependency to `pom.xml`
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Python:** Add dependency to `requirements.txt`
```
prometheus-client==0.19.0
```

Add Python metrics:
```python
from prometheus_client import Counter, Histogram, generate_latest

query_counter = Counter('rag_queries_total', 'Total RAG queries', ['role'])
query_latency = Histogram('rag_query_latency_ms', 'RAG query latency', ['role'])
error_counter = Counter('rag_errors_total', 'Total RAG errors', ['type'])

@app.get("/metrics")
async def metrics():
    return generate_latest()
```

---

## 🎯 PHASE 3 CHECKLIST

- [ ] Fix #12: Add query timeout, test with slow API
- [ ] Fix #13: Optimize batch processing (test with 1000 documents)
- [ ] Fix #14: Update index configuration
- [ ] Fix #15: Create query_history table, add logging
- [ ] Fix #17: Add circuit breaker, test with service down
- [ ] Fix #18: Add Prometheus metrics, verify /metrics endpoint
- [ ] Load test: 100 concurrent requests, check metrics
- [ ] Commit: "Add observability and optimization"
- [ ] **READY FOR FULL PRODUCTION DEPLOYMENT**

**Time: ~2 hours**

---

## 🔵 PHASE 4: POLISH (Next Sprint - 1.5 Hours)

### ⏳ Fix #19-24: Documentation, Logging, Security Hardening
- Complete Swagger/OpenAPI docs (20 min)
- Manage embedding dimensions (15 min)
- Structured JSON logging (15 min)
- Rate limiting (20 min)
- Secrets management via AWS/Azure (10 min)
- Feature flags (30 min)

---

## ✅ FINAL READINESS CHECKLIST

### Before Phase 1 Commit:
- [ ] `mvn clean compile` passes
- [ ] Fix #3, #5, #6 applied
- [ ] No compilation errors
- [ ] Docker builds successfully

### Before Phase 2 Commit:
- [ ] Retry logic tested (simulate failures)
- [ ] Connection pooling verified (monitor connections)
- [ ] Exceptions properly handled
- [ ] CORS working with actual domain
- [ ] Auth logging detects invalid attempts

### Before Phase 3 Commit:
- [ ] Timeouts prevent hanging requests
- [ ] Batch processing faster than individual
- [ ] Query history populated in database
- [ ] Circuit breaker trips when service down
- [ ] Metrics collected and visible

### Before Phase 4 Commit:
- [ ] All endpoints documented
- [ ] Logging structured and parseable
- [ ] Rate limiting prevents abuse
- [ ] No secrets in logs
- [ ] Feature flags working

---

## 📈 Progress Tracking

```
Phase 1: CRITICAL    [████████__] 80% Complete (2/3 done)
Phase 2: HIGH        [__________]  0% Not Started
Phase 3: MEDIUM      [__________]  0% Not Started
Phase 4: POLISH      [__________]  0% Not Started

Total: 25% Complete
```

---

**READY TO START PHASE 1? → Begin with Fix #3 (OrderService metadata)**
