# 🚀 AEMS RAG Quick Reference Card

## 📦 What You Have

```
Spring Boot (Java) ←→ Python FastAPI ←→ PostgreSQL + pgvector
   Business Logic        AI/ML Logic         Vector Storage
```

---

## ⚡ Quick Commands

### Start Everything
```bash
# Windows
start-dev.bat

# Linux/Mac
./start-dev.sh
```

### Check Status
```bash
docker-compose ps
docker-compose logs -f rag-service
```

### Stop Everything
```bash
docker-compose down
```

### Restart After Code Changes
```bash
docker-compose restart rag-service
docker-compose restart spring-backend
```

---

## 🔗 Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Python RAG API | http://localhost:8000 | AI endpoints |
| Python Docs | http://localhost:8000/docs | Swagger UI |
| Spring Boot | http://localhost:8080 | Business API |
| PostgreSQL | localhost:5432 | Database |

---

## 📡 API Endpoints

### Ingestion (Called by Spring Boot)
```bash
POST /api/ingest/document
POST /api/ingest/batch
DELETE /api/ingest/delete
```

### Query (Called by Frontend via Spring Boot)
```bash
POST /api/rag/query
```

---

## 🎭 Role Visibility Matrix

| Role | Sees |
|------|------|
| BUYER (PENDING) | `public` only |
| BUYER (ACCEPTED) | `public` + `buyer:{id}` |
| EMPLOYEE | `public` + `internal` |
| MANAGER | `public` + `internal` + `management` |
| ADMIN | Everything (no filter) |

---

## 📝 Adding New RAG Events

### 1. In Your Java Service
```java
@Autowired
private RagIngestionClient ragClient;

public void yourMethod() {
    // Your business logic
    Entity saved = repository.save(entity);
    
    // RAG ingestion
    String content = "Describe what happened in natural language";
    Map<String, String> metadata = Map.of(
        "visibility", "public",  // or internal/management/buyer:id
        "event_type", "your_event_type",
        "entity_id", saved.getId().toString()
    );
    ragClient.ingestDocument(content, metadata);
}
```

### 2. Test It
```bash
# Check if embedded
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT cmetadata FROM langchain_pg_embedding ORDER BY created_at DESC LIMIT 1;"
```

---

## 🧪 Test Commands

### Test Ingestion
```bash
curl -X POST http://localhost:8000/api/ingest/document \
  -H "Content-Type: application/json" \
  -d '{"content":"Test","metadata":{"visibility":"public"}}'
```

### Test RAG Query
```bash
# 1. Get JWT
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@aems.com","password":"admin123"}' \
  | jq -r .accessToken)

# 2. Query RAG
curl -X POST http://localhost:8000/api/rag/query \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"What products are available?","role":"ADMIN"}'
```

---

## 🔍 Debugging

### Check if pgvector is installed
```bash
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT * FROM pg_extension WHERE extname='vector';"
```

### Count embedded documents
```bash
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT COUNT(*) FROM langchain_pg_embedding;"
```

### View recent documents
```bash
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT document, cmetadata FROM langchain_pg_embedding ORDER BY created_at DESC LIMIT 5;"
```

### Python service logs
```bash
docker-compose logs -f rag-service
```

### Spring Boot logs
```bash
docker-compose logs -f spring-backend
```

---

## 🛑 Common Issues

### "Connection refused" when Spring Boot calls Python
```bash
# Check if rag-service is running
docker-compose ps rag-service

# Check RAG_SERVICE_URL in docker-compose.yml
# Should be: http://rag-service:8000 (not localhost!)
```

### "OpenAI API key not found"
```bash
# Check .env file
cat .env | grep OPENAI_API_KEY

# Restart services after changing .env
docker-compose restart rag-service
```

### "No documents found" when querying
```bash
# Check if ingestion is working
docker-compose logs rag-service | grep "Ingested document"

# Verify documents exist
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT COUNT(*) FROM langchain_pg_embedding;"
```

---

## 📊 Monitoring

### Check service health
```bash
curl http://localhost:8000/health
curl http://localhost:8080/actuator/health  # If Spring Actuator enabled
```

### PostgreSQL connection
```bash
docker exec -it aems-postgres pg_isready -U aems_user -d aems_db
```

### View active connections
```bash
docker exec -it aems-postgres psql -U aems_user -d aems_db \
  -c "SELECT * FROM pg_stat_activity WHERE datname='aems_db';"
```

---

## 🔧 Environment Variables

### Required in `.env`
```bash
OPENAI_API_KEY=sk-...           # Get from OpenAI
JWT_SECRET=your-secret-key       # Same in Spring Boot
```

### Optional
```bash
ENCRYPTION_KEY=...
MAIL_USERNAME=...
MAIL_PASSWORD=...
ALLOWED_ORIGINS=...
```

---

## 📁 File Locations

### Spring Boot
```
aems-backend/src/main/java/com/aems/
├── rag/client/RagIngestionClient.java  ← HTTP client
└── service/
    ├── BuyerService.java               ← Modified
    ├── OrderService.java               ← Modified
    └── StockService.java               ← Modified
```

### Python
```
aems-rag-service/app/
├── main.py                     ← FastAPI app
├── services/
│   ├── rag_service.py          ← Query logic
│   └── ingestion_service.py    ← Embedding logic
└── routers/
    ├── rag_router.py           ← Query endpoints
    └── ingestion_router.py     ← Ingest endpoints
```

---

## 💡 Pro Tips

1. **Start small** - Test with one event (e.g., stock creation)
2. **Check logs first** - Most issues visible in logs
3. **Verify filters** - Test with different roles
4. **Monitor tokens** - OpenAI API costs
5. **Tune prompts** - Adjust in `rag_service.py`

---

## 📞 Quick Help

### Something not working?
1. `docker-compose logs -f` ← Start here
2. Check `.env` file
3. Verify services running: `docker-compose ps`
4. Test individually: PostgreSQL → Python → Spring Boot

### Want to add a new event?
1. See "Adding New RAG Events" section above
2. Check `EVENT-MAPPING-REFERENCE.md`
3. Test with curl command

### Need to change something?
- **Python code** → Edit → `docker-compose restart rag-service`
- **Java code** → Edit → Rebuild → `docker-compose restart spring-backend`
- **Database** → Edit `init-db.sql` → `docker-compose down -v` → `docker-compose up -d`

---

## ✅ Success Checklist

- [ ] All services running (`docker-compose ps`)
- [ ] pgvector installed (check command above)
- [ ] Test ingestion works (curl command above)
- [ ] Test RAG query works (curl command above)
- [ ] Business events trigger ingestion (check logs)
- [ ] Role filtering works (test with different roles)

---

## 📚 Full Documentation

- `RAG-IMPLEMENTATION-GUIDE.md` - Complete setup guide
- `EVENT-MAPPING-REFERENCE.md` - All events and filters
- `IMPLEMENTATION-SUMMARY.md` - Architecture overview
- `QUICK-REFERENCE.md` - This file

---

**Keep this file handy for daily development! 🚀**
