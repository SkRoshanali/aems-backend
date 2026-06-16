# 🚀 Deploying AEMS RAG to Render

## Current Status

✅ **Spring Boot Backend** - Already deployed on Render  
❌ **Python RAG Service** - Needs to be deployed  
❌ **Database** - Needs pgvector extension and RAG tables  

---

## 📋 Step-by-Step Deployment

### **Step 1: Update Neon PostgreSQL Database**

1. **Login to Neon Console:** https://console.neon.tech/

2. **Select your database:** `neondb` (ep-shy-resonance-a9t6ax21)

3. **Open SQL Editor** and run the migration script:

```sql
-- Copy the entire content from database-migration-add-rag.sql
-- This adds:
-- - pgvector extension
-- - langchain_pg_collection table
-- - langchain_pg_embedding table (with vector column)
-- - query_history table
-- - Required indexes
```

4. **Verify installation:**
```sql
-- Check pgvector is installed
SELECT * FROM pg_extension WHERE extname='vector';

-- Should return: vector | 0.7.0 or similar
```

---

### **Step 2: Deploy Python RAG Service to Render**

#### **Option A: Deploy via Render Dashboard (Recommended)**

1. **Go to Render Dashboard:** https://dashboard.render.com/

2. **Click "New +" → "Web Service"**

3. **Connect Your Repository:**
   - Select: `SkRoshanali/aems-backend`
   - Branch: `main`

4. **Configure Service:**
   ```
   Name: aems-rag-service
   Region: Oregon (US West)
   Branch: main
   Root Directory: aems-rag-service
   Runtime: Python 3
   Build Command: pip install -r requirements.txt
   Start Command: uvicorn app.main:app --host 0.0.0.0 --port $PORT
   Plan: Free (or Starter for production)
   ```

5. **Add Environment Variables:**
   
   Click "Environment" tab and add:
   
   | Key | Value |
   |-----|-------|
   | `DATABASE_URL` | `postgresql://neondb_owner:npg_5XRN7FWdOpvo@ep-shy-resonance-a9t6ax21-pooler.gwc.azure.neon.tech/neondb?sslmode=require` |
   | `OPENAI_API_KEY` | `YOUR_OPENAI_API_KEY` ⚠️ **ADD YOUR KEY** |
   | `OPENAI_EMBEDDING_MODEL` | `text-embedding-3-small` |
   | `OPENAI_CHAT_MODEL` | `gpt-4-turbo-preview` |
   | `JWT_SECRET` | `3f8a2b1c9d4e7f6a5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1` |
   | `JWT_ALGORITHM` | `HS256` |
   | `CHUNK_SIZE` | `500` |
   | `CHUNK_OVERLAP` | `50` |
   | `SEARCH_TOP_K` | `5` |
   | `HOST` | `0.0.0.0` |
   | `LOG_LEVEL` | `INFO` |

6. **Click "Create Web Service"**

7. **Wait for deployment** (takes 3-5 minutes)

8. **Copy the service URL** - it will be something like:
   ```
   https://aems-rag-service.onrender.com
   ```

---

#### **Option B: Deploy via render.yaml (Advanced)**

1. **Add `render.yaml` to repository root:**

```yaml
services:
  - type: web
    name: aems-rag-service
    env: python
    rootDir: aems-rag-service
    buildCommand: pip install -r requirements.txt
    startCommand: uvicorn app.main:app --host 0.0.0.0 --port $PORT
    envVars:
      - key: DATABASE_URL
        value: postgresql://neondb_owner:npg_5XRN7FWdOpvo@ep-shy-resonance-a9t6ax21-pooler.gwc.azure.neon.tech/neondb?sslmode=require
      - key: OPENAI_API_KEY
        sync: false
      - key: JWT_SECRET
        value: 3f8a2b1c9d4e7f6a5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1
```

2. **Push to GitHub**
3. **In Render Dashboard:** New → Blueprint → Connect Repository

---

### **Step 3: Update Spring Boot Environment Variables**

Once Python service is deployed, update your Spring Boot service on Render:

1. **Go to your Spring Boot service** in Render Dashboard

2. **Add new environment variable:**
   ```
   Key: RAG_SERVICE_URL
   Value: https://aems-rag-service.onrender.com
   ```

3. **Click "Save Changes"** - this will redeploy your Spring Boot service

---

### **Step 4: Test the Integration**

#### **Test 1: Check Python Service Health**

```bash
curl https://aems-rag-service.onrender.com/health
```

**Expected Response:**
```json
{
  "status": "healthy",
  "service": "aems-rag-service",
  "components": {
    "rag_query": "operational",
    "ingestion": "operational",
    "vector_db": "connected"
  }
}
```

#### **Test 2: Test Document Ingestion**

```bash
curl -X POST https://aems-rag-service.onrender.com/api/ingest/document \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test stock: 1000kg Organic Rice available. Grade: Premium. Price: $50/kg",
    "metadata": {
      "visibility": "public",
      "event_type": "stock_created",
      "stock_id": "1"
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

#### **Test 3: End-to-End Test**

1. **Register a buyer** via your frontend or API:
   ```
   POST https://your-spring-boot.onrender.com/api/buyers/register
   ```

2. **Check Neon database** to verify embedding was created:
   ```sql
   SELECT COUNT(*) FROM langchain_pg_embedding;
   -- Should be > 0
   
   SELECT document, cmetadata 
   FROM langchain_pg_embedding 
   ORDER BY created_at DESC 
   LIMIT 5;
   ```

3. **Query RAG** (after login):
   ```bash
   # Get JWT token first
   TOKEN=$(curl -X POST https://your-spring-boot.onrender.com/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@aems.com","password":"admin123"}' \
     | jq -r .accessToken)
   
   # Query RAG
   curl -X POST https://aems-rag-service.onrender.com/api/rag/query \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "query": "What buyers have applied recently?",
       "role": "ADMIN"
     }'
   ```

---

## 🔧 Troubleshooting

### Issue: "pgvector extension not found"

**Solution:**
```sql
-- In Neon SQL Editor
CREATE EXTENSION vector;
```

### Issue: "Connection refused" between Spring Boot and Python

**Solution:**
- Verify `RAG_SERVICE_URL` is set in Spring Boot environment
- Check Python service is running (visit /health endpoint)
- Ensure URL doesn't have trailing slash

### Issue: "OpenAI API key invalid"

**Solution:**
- Verify OPENAI_API_KEY is set correctly in Python service
- Test key: https://platform.openai.com/api-keys
- Ensure key has sufficient credits

### Issue: "JWT validation failed"

**Solution:**
- Ensure `JWT_SECRET` is EXACTLY the same in both services
- Check for extra spaces or newlines

---

## 📊 Monitoring

### View Logs

**Python Service:**
```bash
# In Render Dashboard → aems-rag-service → Logs
```

**Check for:**
- "Ingested document" - confirms ingestion working
- "RAG query executed" - confirms queries working
- Any errors or warnings

### Database Queries

**Check document count:**
```sql
SELECT COUNT(*) as total_documents FROM langchain_pg_embedding;
```

**View recent documents:**
```sql
SELECT 
    document,
    cmetadata->>'event_type' as event_type,
    cmetadata->>'visibility' as visibility,
    created_at
FROM langchain_pg_embedding 
ORDER BY created_at DESC 
LIMIT 10;
```

**Check by visibility:**
```sql
SELECT 
    cmetadata->>'visibility' as visibility,
    COUNT(*) as doc_count
FROM langchain_pg_embedding 
GROUP BY cmetadata->>'visibility';
```

---

## 💰 Cost Estimation

### Render Costs
- **Free Tier:** Both services can run on free tier initially
- **Paid Plans:** $7/month each service for production

### OpenAI Costs (Estimated)
- **Embeddings:** ~$0.02 per 1M tokens (~$1-2/month)
- **GPT-4 Chat:** ~$10-30 per 1M tokens (~$5-10/month)
- **Total:** ~$10-15/month for moderate usage

### Neon Database
- **Free Tier:** 0.5 GB storage (sufficient for RAG)
- **Paid Plans:** Start at $19/month if you exceed free tier

---

## ✅ Deployment Checklist

- [ ] Neon database updated with pgvector extension
- [ ] RAG tables created (langchain_pg_embedding, etc.)
- [ ] Python RAG service deployed to Render
- [ ] OPENAI_API_KEY added to Python service
- [ ] JWT_SECRET matches between both services
- [ ] RAG_SERVICE_URL added to Spring Boot service
- [ ] Health check passes on Python service
- [ ] Test ingestion works
- [ ] Test RAG query works
- [ ] Business events trigger ingestion (check logs)
- [ ] Role-based filtering verified

---

## 🚀 Production Recommendations

1. **Upgrade to Paid Plans** for better performance:
   - Render: Starter plan ($7/month each)
   - No cold starts, better reliability

2. **Enable HTTPS** (automatic on Render)

3. **Set up Monitoring:**
   - Render provides basic metrics
   - Add application monitoring (Sentry, LogRocket)

4. **Configure CORS properly:**
   - Update `ALLOWED_ORIGINS` in Spring Boot
   - Don't use `*` in production

5. **Database Backups:**
   - Neon provides automatic backups
   - Test restore procedure

6. **Rate Limiting:**
   - Add rate limits to RAG endpoints
   - Protect against API abuse

---

## 📞 Support

**If deployment fails:**

1. Check Render logs for Python service
2. Verify all environment variables are set
3. Test Neon database connection
4. Ensure OPENAI_API_KEY is valid
5. Review this guide step-by-step

**Common URLs:**
- Render Dashboard: https://dashboard.render.com/
- Neon Console: https://console.neon.tech/
- OpenAI Platform: https://platform.openai.com/

---

**Deployment complete when:**
- ✅ Python service shows "Live" in Render
- ✅ Health check returns healthy
- ✅ Business events create embeddings in database
- ✅ RAG queries return relevant answers
- ✅ All roles can query with proper filtering

**Ready to deploy! 🚀**
