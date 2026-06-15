# AEMS Role-Based RAG Architecture Implementation Guide

## 🎯 Architecture Overview

This implementation uses an **event-driven microservices architecture** where:

1. **Spring Boot Backend** - Handles business logic, authentication, and publishes events
2. **Python FastAPI RAG Service** - Handles AI/ML workloads (embeddings, vector search, LLM)
3. **PostgreSQL with pgvector** - Shared database with vector search capabilities

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│   Spring Boot Backend (Java)        │
│   • Authentication (JWT)            │
│   • Business Logic                  │
│   • Event Publishing to RAG         │
└────────┬────────────┬───────────────┘
         │            │
         │            │ (REST API)
         │            ▼
         │    ┌──────────────────────┐
         │    │  Python FastAPI RAG  │
         │    │  • Embeddings        │
         │    │  • Vector Search     │
         │    │  • LLM Generation    │
         │    └──────────┬───────────┘
         │               │
         ▼               ▼
┌─────────────────────────────────────┐
│   PostgreSQL + pgvector             │
│   • Business Data                   │
│   • Vector Embeddings               │
└─────────────────────────────────────┘
```

---

## 📦 What Was Implemented

### Spring Boot Changes

1. **RagIngestionClient.java** - HTTP client for pushing events to Python service
2. **Updated Services** - BuyerService, OrderService, StockService now publish events
3. **Event Hooks** - Automatic knowledge base updates on:
   - Buyer registration
   - Buyer approval
   - Order creation
   - Order approval/rejection
   - Stock creation

### Python FastAPI Service

1. **Ingestion Service** - Handles document embedding and storage
2. **RAG Service** - Handles role-based query and response generation
3. **API Endpoints**:
   - `POST /api/ingest/document` - Single document ingestion
   - `POST /api/ingest/batch` - Batch ingestion
   - `DELETE /api/ingest/delete` - Delete by metadata filter
   - `POST /api/rag/query` - Role-based question answering

### Database

1. **pgvector extension** enabled
2. **langchain_pg_embedding** table for vector storage
3. **Indexes** for fast similarity search and metadata filtering

---

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- OpenAI API Key
- Git

### Step 1: Clone and Setup

```bash
# You already have the repo cloned
cd aems-backend

# Create .env file from example
cp .env.example .env

# Edit .env and add your OpenAI API key
# OPENAI_API_KEY=sk-your-key-here
```

### Step 2: Create Missing Files

Create `aems-rag-service/app/__init__.py`:
```bash
mkdir -p aems-rag-service/app/models
mkdir -p aems-rag-service/app/services
mkdir -p aems-rag-service/app/routers
touch aems-rag-service/app/__init__.py
touch aems-rag-service/app/models/__init__.py
touch aems-rag-service/app/services/__init__.py
touch aems-rag-service/app/routers/__init__.py
```

Create `aems-rag-service/app/config.py`:
```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    DATABASE_URL: str
    OPENAI_API_KEY: str
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"
    OPENAI_CHAT_MODEL: str = "gpt-4-turbo-preview"
    JWT_SECRET: str
    JWT_ALGORITHM: str = "HS256"
    CHUNK_SIZE: int = 500
    CHUNK_OVERLAP: int = 50
    SEARCH_TOP_K: int = 5
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    class Config:
        env_file = ".env"

settings = Settings()
```

Create `aems-rag-service/app/auth.py`:
```python
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from .config import settings

security = HTTPBearer()

def verify_jwt_token(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> dict:
    token = credentials.credentials
    
    try:
        payload = jwt.decode(
            token, 
            settings.JWT_SECRET, 
            algorithms=[settings.JWT_ALGORITHM]
        )
        
        email: str = payload.get("sub")
        role: str = payload.get("role")
        
        if email is None or role is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid authentication credentials"
            )
        
        return {"email": email, "role": role}
    
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials"
        )
```

Create `aems-rag-service/requirements.txt`:
```
fastapi==0.109.0
uvicorn[standard]==0.27.0
python-jose[cryptography]==3.3.0
python-multipart==0.0.6
pydantic==2.5.3
pydantic-settings==2.1.0
sqlalchemy==2.0.25
psycopg2-binary==2.9.9
pgvector==0.2.4
langchain==0.1.4
langchain-openai==0.0.5
langchain-community==0.0.16
openai==1.10.0
tiktoken==0.5.2
httpx==0.26.0
python-dotenv==1.0.0
```

### Step 3: Start Services

```bash
# Start all services with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f

# Check health
curl http://localhost:8000/health  # Python RAG service
curl http://localhost:8080/actuator/health  # Spring Boot (if enabled)
```

### Step 4: Test the Integration

#### Test Ingestion
```bash
curl -X POST http://localhost:8000/api/ingest/document \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Product: Organic Basmati Rice. Grade: Premium. Price: $50 per kg. Available quantity: 1000kg",
    "metadata": {
      "visibility": "public",
      "event_type": "stock_created",
      "crop_name": "Basmati Rice"
    }
  }'
```

#### Test RAG Query (requires JWT)
First, login to get JWT token:
```bash
# Login as buyer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "buyer@example.com",
    "password": "password123"
  }'
```

Then query RAG:
```bash
curl -X POST http://localhost:8000/api/rag/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "query": "What products are available?",
    "role": "BUYER",
    "buyer_id": "1",
    "buyer_status": "ACCEPTED"
  }'
```

---

## 🔄 How the Event Flow Works

### Example: Buyer Registration

1. **Frontend** → POST `/api/buyers/register` → **Spring Boot**
2. **Spring Boot** → Saves buyer to PostgreSQL
3. **Spring Boot** → Calls `ragClient.ingestDocument()` with buyer info
4. **RagIngestionClient** → POST `/api/ingest/document` → **Python Service**
5. **Python Service** → Generates embedding using OpenAI
6. **Python Service** → Stores embedding in pgvector with metadata `visibility: "management"`
7. **Python Service** → Returns success

### Example: Manager Queries Pending Buyers

1. **Frontend** → POST `/api/chat/query` → **Spring Boot**
2. **Spring Boot** → Extracts JWT role = "MANAGER"
3. **Spring Boot** → Forwards to Python `/api/rag/query`
4. **Python Service** → Builds filter: `visibility IN ["management", "internal", "public"]`
5. **Python Service** → Searches pgvector with filter
6. **Python Service** → Retrieves relevant documents about pending buyers
7. **Python Service** → Feeds context to GPT-4
8. **Python Service** → Returns AI-generated answer
9. **Spring Boot** → Returns to frontend

---

## 🎭 Role-Based Access Matrix

| Role | Can See |
|------|---------|
| **BUYER (PENDING)** | Public catalog, onboarding info |
| **BUYER (ACCEPTED)** | Public catalog + their own orders/applications |
| **EMPLOYEE** | Internal procedures + public catalog |
| **MANAGER** | Internal + management dashboards + public |
| **ADMIN/SUPER_ADMIN** | Everything (no filter) |

---

## 📊 Metadata Tags Reference

### Event Types
- `buyer_application` - Buyer submitted registration
- `buyer_approved` - Manager approved buyer
- `stock_created` - Employee added stock
- `order_placed` - Buyer placed order
- `order_approved` - Manager approved order
- `order_rejected` - Manager rejected order

### Visibility Levels
- `public` - Everyone can see
- `internal` - Employees and above
- `management` - Managers and above
- `buyer:{id}` - Specific buyer only + management

---

## 🛠️ Troubleshooting

### Python service won't start
```bash
# Check logs
docker-compose logs rag-service

# Common issues:
# 1. Missing OPENAI_API_KEY in .env
# 2. PostgreSQL not ready - wait 30 seconds and restart
# 3. Port 8000 already in use
```

### Ingestion failing
```bash
# Test PostgreSQL connection
docker exec -it aems-postgres psql -U aems_user -d aems_db -c "SELECT 1;"

# Check if pgvector is installed
docker exec -it aems-postgres psql -U aems_user -d aems_db -c "SELECT * FROM pg_extension WHERE extname='vector';"
```

### RAG not returning relevant results
```bash
# Check if documents exist
docker exec -it aems-postgres psql -U aems_user -d aems_db -c "SELECT COUNT(*) FROM langchain_pg_embedding;"

# Check metadata
docker exec -it aems-postgres psql -U aems_user -d aems_db -c "SELECT cmetadata FROM langchain_pg_embedding LIMIT 5;"
```

---

## 🚀 Next Steps

1. **Test each business flow** - Register buyer, create stock, place order
2. **Monitor ingestion** - Check Python logs for successful embeddings
3. **Test RAG queries** - Try different roles and questions
4. **Tune prompts** - Adjust system prompts in `rag_service.py`
5. **Add more events** - Extend to Shipments, Invoices, Farmers
6. **Analytics** - Track popular queries, response quality

---

## 📝 Production Considerations

1. **Rate Limiting** - Add rate limits to RAG endpoint
2. **Caching** - Cache frequent queries
3. **Monitoring** - Add Prometheus/Grafana
4. **Error Handling** - Retry logic for OpenAI API
5. **Secrets Management** - Use AWS Secrets Manager or Vault
6. **Scaling** - Horizontal scaling of Python service
7. **Cost Control** - Monitor OpenAI usage

---

## 📚 Additional Resources

- [LangChain Documentation](https://python.langchain.com/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [OpenAI Embeddings](https://platform.openai.com/docs/guides/embeddings)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)

---

## ✅ Checklist

- [ ] Docker Compose running
- [ ] PostgreSQL accessible
- [ ] pgvector extension installed
- [ ] Python service healthy
- [ ] Spring Boot connecting to Python
- [ ] OpenAI API key configured
- [ ] JWT secret matches in both services
- [ ] Test ingestion successful
- [ ] Test RAG query successful
- [ ] Role-based filtering working

---

**Questions?** Check the logs first: `docker-compose logs -f`
