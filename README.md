# 🌾 AEMS - Agri Export Management System with Role-Based RAG

> **Production-ready event-driven RAG architecture** for intelligent, role-based question answering in agricultural export management.

## 🎯 What Is This?

This system combines a **Spring Boot backend** (Java) for business operations with a **Python FastAPI service** for AI/ML workloads, creating an intelligent knowledge base that learns from every business transaction.

### Key Features

- ✅ **Event-Driven Knowledge Base** - Every order, stock update, and buyer registration automatically feeds the AI
- ✅ **Role-Based Access Control** - Buyers see their data, Managers see applications, Employees see procedures
- ✅ **Microservices Architecture** - Independent scaling of business logic and AI workloads
- ✅ **Vector Search** - Semantic search using OpenAI embeddings and pgvector
- ✅ **Production-Ready** - Docker orchestration, health checks, comprehensive documentation

---

## 🏗️ Architecture

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│   Spring Boot (Java)    │  ──► Business Logic
│   • Authentication      │      Order Management
│   • Order Management    │      Stock Management
│   • Event Publishing    │      User Management
└──────────┬──────────────┘
           │
           │ HTTP REST
           ▼
┌──────────────────────────┐
│  Python FastAPI (Python) │  ──► AI/ML Workloads
│  • Document Embedding    │      Embeddings
│  • Vector Search         │      RAG Queries
│  • LLM Integration       │      Context Building
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│  PostgreSQL + pgvector   │  ──► Data Storage
│  • Business Data         │      Vector Storage
│  • Vector Embeddings     │      Similarity Search
└──────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- OpenAI API Key ([Get one here](https://platform.openai.com/api-keys))
- Git

### 1. Clone Repository

```bash
cd backend-aems
# Repository already cloned
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env and add your OpenAI API key
# OPENAI_API_KEY=sk-your-key-here
```

### 3. Start Services

**Windows:**
```cmd
start-dev.bat
```

**Linux/Mac:**
```bash
chmod +x start-dev.sh
./start-dev.sh
```

**Manual (All platforms):**
```bash
docker-compose up -d
```

### 4. Verify Services

```bash
# Check all services are running
docker-compose ps

# Health checks
curl http://localhost:8000/health  # Python RAG Service
curl http://localhost:8080/        # Spring Boot Backend
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| **[IMPLEMENTATION-SUMMARY.md](IMPLEMENTATION-SUMMARY.md)** | 📖 Start here - Overview & benefits |
| **[RAG-IMPLEMENTATION-GUIDE.md](RAG-IMPLEMENTATION-GUIDE.md)** | 🛠️ Complete setup guide |
| **[ARCHITECTURE-DIAGRAM.md](ARCHITECTURE-DIAGRAM.md)** | 🏗️ Visual architecture & flows |
| **[EVENT-MAPPING-REFERENCE.md](EVENT-MAPPING-REFERENCE.md)** | 📋 Business events → RAG mapping |
| **[QUICK-REFERENCE.md](QUICK-REFERENCE.md)** | ⚡ Commands & troubleshooting |
| **[FILES-CREATED.md](FILES-CREATED.md)** | 📁 Complete file inventory |

---

## 🧪 Testing

### Test Document Ingestion

```bash
curl -X POST http://localhost:8000/api/ingest/document \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Product: Organic Basmati Rice. Grade: Premium. Price: $50/kg. Available: 1000kg",
    "metadata": {
      "visibility": "public",
      "event_type": "stock_created",
      "crop_name": "Basmati Rice"
    }
  }'
```

**Expected Response:**
```json
{
  "status": "success",
  "chunks_created": 1,
  "document_ids": ["..."]
}
```

### Test RAG Query

1. **Login to get JWT token:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@aems.com",
    "password": "admin123"
  }'
```

2. **Query RAG with token:**
```bash
curl -X POST http://localhost:8000/api/rag/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "What products are available?",
    "role": "ADMIN"
  }'
```

**Expected Response:**
```json
{
  "answer": "We have Organic Basmati Rice available - 1000kg at $50 per kg, Premium grade.",
  "sources": [...],
  "role": "ADMIN",
  "filter_applied": false
}
```

---

## 🎭 Role-Based Access

| Role | Can See | Use Cases |
|------|---------|-----------|
| **BUYER (PENDING)** | Public catalog only | Browse products before approval |
| **BUYER (ACCEPTED)** | Public + own orders | View products & track orders |
| **EMPLOYEE** | Public + internal procedures | Daily operations, stock management |
| **MANAGER** | Public + internal + applications | Approve buyers, manage inventory |
| **ADMIN** | Everything | Full system access |

---

## 🔄 How It Works

### 1. Business Event Triggers Ingestion

```java
// In BuyerService.java
User saved = userRepository.save(buyer);

// Automatically ingest to RAG
ragClient.ingestDocument(
    "Buyer John Doe submitted application from New York. Company: ACME Corp.",
    Map.of(
        "visibility", "management",
        "buyer_id", "123",
        "event_type", "buyer_application"
    )
);
```

### 2. Python Embeds & Stores

```python
# In ingestion_service.py
embedding = openai.embed(content)  # [0.023, -0.456, ...] (1536 dims)
vector_store.add_texts([content], [metadata])
```

### 3. User Queries with Role Filter

```python
# In rag_service.py
if role == "MANAGER":
    filter = {"visibility": ["internal", "management", "public"]}

docs = vector_store.similarity_search(query, filter=filter)
answer = llm.generate(context=docs, question=query)
```

---

## 📦 Services

| Service | Port | Purpose |
|---------|------|---------|
| **Spring Boot** | 8080 | Business API, Authentication |
| **Python FastAPI** | 8000 | AI/ML, RAG endpoints |
| **PostgreSQL** | 5432 | Data & vector storage |

### API Endpoints

#### Python RAG Service
- `POST /api/ingest/document` - Ingest single document
- `POST /api/ingest/batch` - Batch ingest
- `DELETE /api/ingest/delete` - Delete by metadata
- `POST /api/rag/query` - Ask questions (role-filtered)
- `GET /health` - Health check
- `GET /docs` - Swagger UI

#### Spring Boot Backend
- `POST /api/auth/login` - User authentication
- `POST /api/orders` - Place order (triggers RAG)
- `POST /api/stock` - Add stock (triggers RAG)
- `POST /api/buyers/register` - Register buyer (triggers RAG)
- `POST /api/chat/query` - Query RAG (via Python)

---

## 🛠️ Development

### Project Structure

```
backend-aems/
├── aems-backend/              # Spring Boot application
│   └── src/main/java/com/aems/
│       ├── rag/client/        # RAG integration
│       └── service/           # Business services (modified)
│
├── aems-rag-service/          # Python FastAPI service
│   ├── app/
│   │   ├── services/          # RAG & ingestion logic
│   │   └── routers/           # API endpoints
│   └── requirements.txt
│
├── docker-compose.yml         # Service orchestration
├── init-db.sql               # Database setup
└── *.md                      # Documentation
```

### Adding New RAG Events

1. **In your Java Service:**

```java
@Autowired
private RagIngestionClient ragClient;

public Entity yourMethod() {
    Entity saved = repository.save(entity);
    
    ragClient.ingestDocument(
        "Natural language description of what happened",
        Map.of(
            "visibility", "public",  // or internal/management/buyer:id
            "event_type", "your_event",
            "entity_id", saved.getId().toString()
        )
    );
    
    return saved;
}
```

2. **Test it:**

```bash
# Check if embedded
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT COUNT(*) FROM langchain_pg_embedding;"
```

---

## 🐛 Troubleshooting

### Services Won't Start

```bash
# Check logs
docker-compose logs -f

# Common issues:
# 1. Missing OPENAI_API_KEY in .env
# 2. Port conflicts (8000, 8080, 5432)
# 3. Docker not running
```

### RAG Not Returning Results

```bash
# Check if documents exist
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT COUNT(*) FROM langchain_pg_embedding;"

# Should show > 0 if ingestion worked
```

### JWT Validation Failing

```bash
# Ensure JWT_SECRET matches in both services
# Check .env file and Spring Boot application.properties
```

**See [QUICK-REFERENCE.md](QUICK-REFERENCE.md) for more troubleshooting.**

---

## 📊 Monitoring

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f rag-service
docker-compose logs -f spring-backend
docker-compose logs -f postgres
```

### Health Checks

```bash
# Python RAG Service
curl http://localhost:8000/health

# Spring Boot (if actuator enabled)
curl http://localhost:8080/actuator/health

# PostgreSQL
docker exec aems-postgres pg_isready -U aems_user
```

### Database Queries

```bash
# Count embedded documents
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT COUNT(*) FROM langchain_pg_embedding;"

# View recent documents
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT document, cmetadata FROM langchain_pg_embedding ORDER BY created_at DESC LIMIT 5;"
```

---

## 🚀 Production Deployment

### Pre-deployment Checklist

- [ ] Secure JWT_SECRET (use strong random value)
- [ ] Configure CORS properly (not `*`)
- [ ] Enable rate limiting on RAG endpoints
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure backup strategy for PostgreSQL
- [ ] Use secrets manager (AWS Secrets Manager, Vault)
- [ ] Enable HTTPS/TLS
- [ ] Review OpenAI API rate limits & costs

### Scaling

Each service can scale independently:

```bash
# Scale Python RAG service
docker-compose up -d --scale rag-service=3

# Scale Spring Boot
docker-compose up -d --scale spring-backend=2
```

---

## 📈 Cost Estimation

### OpenAI API Costs (Approximate)

**Embeddings (text-embedding-3-small):**
- $0.02 per 1M tokens
- ~1000 business events/day = ~$0.50/month

**Chat (gpt-4-turbo-preview):**
- $10 per 1M input tokens
- $30 per 1M output tokens
- ~100 queries/day = ~$3-5/month

**Total: ~$5-10/month for moderate usage**

---

## 🤝 Contributing

This is a production implementation for AEMS. For modifications:

1. Review documentation first
2. Test locally with Docker Compose
3. Check role-based filtering works
4. Update relevant documentation

---

## 📝 License

Proprietary - Agri Export Management System

---

## 📞 Support

### Quick Help

1. Check [QUICK-REFERENCE.md](QUICK-REFERENCE.md)
2. View logs: `docker-compose logs -f`
3. Review [RAG-IMPLEMENTATION-GUIDE.md](RAG-IMPLEMENTATION-GUIDE.md)

### Common Commands

```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# Restart after code changes
docker-compose restart rag-service
docker-compose restart spring-backend

# View logs
docker-compose logs -f rag-service

# Clean restart (removes volumes)
docker-compose down -v
docker-compose up -d
```

---

## ✅ Success Indicators

Your system is working correctly when:

- ✅ All 3 services show as "healthy" in `docker-compose ps`
- ✅ Buyer registration triggers document ingestion (check logs)
- ✅ Stock creation appears in vector database
- ✅ RAG queries return relevant answers
- ✅ Role filtering works (Buyers can't see management data)
- ✅ Each role sees appropriate information

---

## 🎉 Features

- **Automatic Knowledge Base** - Learns from business operations
- **Role-Based Security** - Enforced at vector search level
- **Semantic Search** - Understands meaning, not just keywords
- **Contextual Answers** - AI provides relevant, accurate responses
- **Scalable Architecture** - Independent service scaling
- **Production-Ready** - Health checks, error handling, logging
- **Comprehensive Docs** - 6 detailed guides included

---

**Built with ❤️ for intelligent agricultural export management**

**Ready to deploy! 🚀**

For detailed setup instructions, see [RAG-IMPLEMENTATION-GUIDE.md](RAG-IMPLEMENTATION-GUIDE.md)
