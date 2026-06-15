# AEMS Role-Based RAG Architecture - Visual Flow

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                 │
│                     (React / Angular)                            │
└────────────┬────────────────────────────────────┬───────────────┘
             │                                    │
             │ Business Operations                │ AI Chat Queries
             │ (Orders, Stock, etc.)              │
             ▼                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                           │
│                        (Java 21)                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Controllers                                             │   │
│  │  • AuthController (JWT)                                  │   │
│  │  • BuyerController                                       │   │
│  │  • OrderController                                       │   │
│  │  • StockController                                       │   │
│  │  • RagController ← NEW                                   │   │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │                                              │
│  ┌────────────────▼───────────────────────────────────────┐    │
│  │  Services                                               │    │
│  │  • BuyerService → RagIngestionClient                    │    │
│  │  • OrderService → RagIngestionClient                    │    │
│  │  • StockService → RagIngestionClient                    │    │
│  └────────────────┬────────────────────────────────────────┘   │
│                   │                                              │
│  ┌────────────────▼───────────────────────────────────────┐    │
│  │  RagIngestionClient ← NEW                               │    │
│  │  • ingestDocument()                                     │    │
│  │  • ingestBatch()                                        │    │
│  │  • deleteByMetadata()                                   │    │
│  └─────────────────────────────────────────────────────────┘   │
└────────────┬──────────────────────────────────┬────────────────┘
             │                                  │
             │ HTTP POST                        │ HTTP POST
             │ /api/ingest/document             │ /api/rag/query
             ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                   PYTHON FASTAPI RAG SERVICE                     │
│                         (Python 3.11)                            │
│  ┌──────────────────────┐         ┌────────────────────────┐   │
│  │  Ingestion Router     │         │   RAG Query Router     │   │
│  │  /api/ingest/*        │         │   /api/rag/query       │   │
│  └──────────┬───────────┘         └────────────┬───────────┘   │
│             │                                   │                │
│             ▼                                   ▼                │
│  ┌──────────────────────┐         ┌────────────────────────┐   │
│  │  IngestionService     │         │   RAGService           │   │
│  │  • Text chunking      │         │   • Role filtering     │   │
│  │  • Embedding gen      │         │   • Vector search      │   │
│  │  • Metadata tagging   │         │   • Context building   │   │
│  └──────────┬───────────┘         └────────────┬───────────┘   │
│             │                                   │                │
│             │                                   │                │
│             ▼                                   ▼                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              OpenAI API Integration                      │   │
│  │  • text-embedding-3-small (embeddings)                   │   │
│  │  • gpt-4-turbo-preview (chat completion)                 │   │
│  └──────────┬──────────────────────────────────┬───────────┘   │
└─────────────┼──────────────────────────────────┼───────────────┘
              │                                  │
              │ Store embeddings                 │ Retrieve similar
              ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                  POSTGRESQL + PGVECTOR                           │
│                      (Version 16)                                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Business Tables (Spring Boot JPA)                        │  │
│  │  • users                                                  │  │
│  │  • orders                                                 │  │
│  │  • stock                                                  │  │
│  │  • farmers                                                │  │
│  │  • crops                                                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  RAG Tables (LangChain + pgvector)                        │  │
│  │  • langchain_pg_embedding                                 │  │
│  │    - id: UUID                                             │  │
│  │    - embedding: vector(1536)                              │  │
│  │    - document: TEXT                                       │  │
│  │    - cmetadata: JSONB ← Role filtering happens here       │  │
│  │  • langchain_pg_collection                                │  │
│  │  • query_history (optional analytics)                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Event Flow: Business Operation → RAG Ingestion

```
┌─────────────┐
│   User      │
│  (Buyer)    │
└──────┬──────┘
       │
       │ 1. Place Order
       ▼
┌─────────────────────────────────────────────┐
│  Spring Boot: OrderController               │
│  POST /api/orders                           │
└──────┬──────────────────────────────────────┘
       │
       │ 2. Call Service
       ▼
┌─────────────────────────────────────────────┐
│  OrderService.createOrder()                 │
│  • Save order to DB                         │
│  • Send email                               │
│  • Call RagIngestionClient ← NEW            │
└──────┬──────────────────────────────────────┘
       │
       │ 3. Prepare RAG Content
       ▼
┌─────────────────────────────────────────────┐
│  String content = "Order #ORD-123:         │
│    Buyer John Doe placed order for 100kg   │
│    Rice at $50 per unit. Total: $5000.     │
│    Delivery to New York, USA."             │
│                                             │
│  Map metadata = {                           │
│    "visibility": "buyer:123",               │
│    "visibility_secondary": "management",    │
│    "event_type": "order_placed",            │
│    "order_id": "123",                       │
│    "buyer_id": "123"                        │
│  }                                          │
└──────┬──────────────────────────────────────┘
       │
       │ 4. HTTP POST to Python
       ▼
┌─────────────────────────────────────────────┐
│  Python: POST /api/ingest/document          │
│  {                                          │
│    "content": "Order #ORD-123...",          │
│    "metadata": {...}                        │
│  }                                          │
└──────┬──────────────────────────────────────┘
       │
       │ 5. Process & Embed
       ▼
┌─────────────────────────────────────────────┐
│  IngestionService.ingest_document()         │
│  • Split text into chunks (if needed)       │
│  • Generate embedding via OpenAI            │
│  • Store in pgvector with metadata          │
└──────┬──────────────────────────────────────┘
       │
       │ 6. Store embedding
       ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL: langchain_pg_embedding         │
│  INSERT INTO langchain_pg_embedding         │
│    (embedding, document, cmetadata)         │
│  VALUES (                                   │
│    [0.023, -0.456, ...],  ← 1536 dimensions │
│    'Order #ORD-123: Buyer John...',         │
│    '{"visibility": "buyer:123", ...}'       │
│  )                                          │
└─────────────────────────────────────────────┘
```

---

## 🔍 Query Flow: User Question → AI Answer

```
┌─────────────┐
│   User      │
│  (Manager)  │
└──────┬──────┘
       │
       │ 1. Ask Question: "Show me pending orders"
       ▼
┌─────────────────────────────────────────────┐
│  Frontend → Spring Boot: RagController      │
│  POST /api/chat/query                       │
│  Headers: Authorization: Bearer <JWT>       │
│  Body: { "query": "Show pending orders" }   │
└──────┬──────────────────────────────────────┘
       │
       │ 2. Extract Role from JWT
       ▼
┌─────────────────────────────────────────────┐
│  RagController                              │
│  • Validate JWT                             │
│  • Extract role: "MANAGER"                  │
│  • Forward to Python                        │
└──────┬──────────────────────────────────────┘
       │
       │ 3. HTTP POST to Python
       ▼
┌─────────────────────────────────────────────┐
│  Python: POST /api/rag/query                │
│  {                                          │
│    "query": "Show me pending orders",       │
│    "role": "MANAGER"                        │
│  }                                          │
└──────┬──────────────────────────────────────┘
       │
       │ 4. Build Role Filter
       ▼
┌─────────────────────────────────────────────┐
│  RAGService.build_metadata_filter()         │
│  role = "MANAGER"                           │
│  → filter = {                               │
│      "$or": [                               │
│        {"visibility": "internal"},          │
│        {"visibility": "management"},        │
│        {"visibility": "public"}             │
│      ]                                      │
│    }                                        │
└──────┬──────────────────────────────────────┘
       │
       │ 5. Vector Search with Filter
       ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL: pgvector similarity search     │
│  SELECT document, cmetadata                 │
│  FROM langchain_pg_embedding                │
│  WHERE cmetadata->>'visibility' IN          │
│        ('internal', 'management', 'public') │
│  ORDER BY embedding <=> query_embedding     │
│  LIMIT 5                                    │
└──────┬──────────────────────────────────────┘
       │
       │ 6. Retrieved Documents
       ▼
┌─────────────────────────────────────────────┐
│  Top 5 Similar Documents:                   │
│  1. "Order #ORD-123: Buyer John placed..."  │
│  2. "Order #ORD-456: Status: Pending..."    │
│  3. "Stock batch: 1000kg Rice available..." │
│  4. "Buyer Jane Doe applied from London..." │
│  5. "Order #ORD-789: Approved by Manager..." │
└──────┬──────────────────────────────────────┘
       │
       │ 7. Build Context & Call LLM
       ▼
┌─────────────────────────────────────────────┐
│  RAGService.query()                         │
│  • Concatenate retrieved docs               │
│  • Build prompt with role-specific system   │
│  • Call OpenAI GPT-4                        │
└──────┬──────────────────────────────────────┘
       │
       │ 8. OpenAI API Call
       ▼
┌─────────────────────────────────────────────┐
│  OpenAI GPT-4 Prompt:                       │
│  System: "You are an AI assistant for       │
│           Managers in AEMS..."              │
│  Context: "[Doc 1][Doc 2][Doc 3]..."        │
│  Question: "Show me pending orders"         │
│                                             │
│  → AI generates contextual answer           │
└──────┬──────────────────────────────────────┘
       │
       │ 9. Return Answer
       ▼
┌─────────────────────────────────────────────┐
│  Python Response:                           │
│  {                                          │
│    "answer": "You have 3 pending orders:    │
│               1. Order #ORD-123 from        │
│                  John Doe for 100kg Rice    │
│               2. Order #ORD-456 from...",   │
│    "sources": [                             │
│      {"content": "Order #ORD-123...",       │
│       "metadata": {...}},                   │
│      ...                                    │
│    ]                                        │
│  }                                          │
└──────┬──────────────────────────────────────┘
       │
       │ 10. Spring Boot forwards response
       ▼
┌─────────────────────────────────────────────┐
│  Frontend displays answer to user           │
└─────────────────────────────────────────────┘
```

---

## 🎭 Role-Based Filtering Visualization

```
┌──────────────────────────────────────────────────────────────┐
│                   Vector Database (pgvector)                  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Document 1: "Stock: 1000kg Rice available"           │   │
│  │ Metadata: {"visibility": "public"}                   │   │
│  │ ✅ Accessible by: ALL ROLES                          │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Document 2: "Farmer John, grows rice, Grade A"       │   │
│  │ Metadata: {"visibility": "internal"}                 │   │
│  │ ✅ Accessible by: EMPLOYEE, MANAGER, ADMIN           │   │
│  │ ❌ NOT accessible by: BUYER                          │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Document 3: "Buyer Jane applied from London"         │   │
│  │ Metadata: {"visibility": "management"}               │   │
│  │ ✅ Accessible by: MANAGER, ADMIN                     │   │
│  │ ❌ NOT accessible by: BUYER, EMPLOYEE                │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ Document 4: "Order #123: Your order for 100kg Rice"  │   │
│  │ Metadata: {"visibility": "buyer:123"}                │   │
│  │ ✅ Accessible by: Buyer #123, MANAGER, ADMIN         │   │
│  │ ❌ NOT accessible by: Other buyers, EMPLOYEE         │   │
│  └──────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘

When BUYER #123 queries:
  Filter: {"$or": [{"visibility": "public"}, {"visibility": "buyer:123"}]}
  Result: Documents 1, 4

When EMPLOYEE queries:
  Filter: {"$or": [{"visibility": "public"}, {"visibility": "internal"}]}
  Result: Documents 1, 2

When MANAGER queries:
  Filter: {"$or": [{"visibility": "public"}, {"visibility": "internal"}, {"visibility": "management"}]}
  Result: Documents 1, 2, 3 (+ buyer:123 via secondary visibility)

When ADMIN queries:
  Filter: None
  Result: ALL documents (1, 2, 3, 4)
```

---

## 🔐 Security Layer

```
┌─────────────────────────────────────────────────────────┐
│                    Security Boundary                     │
│                                                          │
│  Frontend Request                                        │
│  └─► Authorization: Bearer <JWT>                        │
│       │                                                  │
│       ▼                                                  │
│  ┌────────────────────────────────────────────────┐    │
│  │  Spring Boot: JwtAuthenticationFilter           │    │
│  │  • Validate JWT signature                       │    │
│  │  • Extract user email & role                    │    │
│  │  • Set SecurityContext                          │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Controller: @PreAuthorize checks               │    │
│  │  @PreAuthorize("hasRole('MANAGER')")            │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Forward JWT to Python (Authorization header)   │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Python: verify_jwt_token()                     │    │
│  │  • Decode JWT with same JWT_SECRET              │    │
│  │  • Extract role                                 │    │
│  │  • Validate role matches request                │    │
│  └────────────┬───────────────────────────────────┘    │
│               │                                          │
│               ▼                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Apply role-based filter to vector search       │    │
│  └──────────────────────────────────────────────────    │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Data Flow Summary

1. **User Action** → Frontend
2. **API Call** → Spring Boot (with JWT)
3. **Business Logic** → Service Layer
4. **Database Operation** → PostgreSQL (business tables)
5. **Event Publishing** → RagIngestionClient
6. **HTTP Request** → Python FastAPI
7. **Embedding Generation** → OpenAI API
8. **Vector Storage** → pgvector (RAG tables)

---

**Query Flow:**

1. **User Question** → Frontend
2. **API Call** → Spring Boot (with JWT)
3. **Role Extraction** → JWT validation
4. **Forward Request** → Python FastAPI
5. **Role Filter** → Metadata filter builder
6. **Vector Search** → pgvector similarity search
7. **Context Building** → Retrieved documents
8. **LLM Call** → OpenAI GPT-4
9. **AI Response** → Python FastAPI
10. **Return to User** → Spring Boot → Frontend

---

This architecture ensures:
- ✅ **Separation of concerns** (Business vs AI logic)
- ✅ **Independent scaling** (Each service scales separately)
- ✅ **Role-based security** (Enforced at multiple layers)
- ✅ **Automatic knowledge base** (Every action feeds RAG)
- ✅ **Production-ready** (Health checks, error handling, logging)
