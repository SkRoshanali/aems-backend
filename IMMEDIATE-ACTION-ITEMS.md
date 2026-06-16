# ⚡ Immediate Action Items to Deploy RAG

## 🎯 Current Situation

Your Spring Boot backend is deployed and working, but **RAG functionality is not yet active** because:

1. ❌ Python RAG service is not deployed
2. ❌ Your Neon database doesn't have pgvector tables
3. ❌ Spring Boot can't connect to RAG service (no URL configured)

---

## ✅ **Action Items (In Order)**

### **1️⃣ Update Neon Database (5 minutes)**

**What:** Add pgvector extension and RAG tables to your existing database

**How:**

1. Go to: https://console.neon.tech/
2. Select your database: `neondb` (ep-shy-resonance-a9t6ax21)
3. Click "SQL Editor"
4. Copy **ALL content** from `database-migration-add-rag.sql`
5. Paste and click "Run"
6. Verify it worked:
   ```sql
   SELECT * FROM pg_extension WHERE extname='vector';
   ```
   Should return: `vector | 0.7.0 or similar`

**Why:** Your database needs vector storage capability for embeddings

---

### **2️⃣ Deploy Python RAG Service to Render (10 minutes)**

**What:** Deploy the Python FastAPI service alongside your Spring Boot backend

**How:**

1. Go to: https://dashboard.render.com/
2. Click "New +" → "Web Service"
3. Connect repository: `SkRoshanali/aems-backend`
4. Configure:
   ```
   Name: aems-rag-service
   Region: Oregon (US West)
   Branch: main
   Root Directory: aems-rag-service
   Runtime: Python 3
   Build Command: pip install -r requirements.txt
   Start Command: uvicorn app.main:app --host 0.0.0.0 --port $PORT
   Instance Type: Free
   ```

5. **Add Environment Variables** (click "Environment" tab):

   | Variable | Value |
   |----------|-------|
   | `DATABASE_URL` | `postgresql://neondb_owner:npg_5XRN7FWdOpvo@ep-shy-resonance-a9t6ax21-pooler.gwc.azure.neon.tech/neondb?sslmode=require` |
   | `OPENAI_API_KEY` | **⚠️ ADD YOUR KEY HERE** (get from https://platform.openai.com/api-keys) |
   | `JWT_SECRET` | `3f8a2b1c9d4e7f6a5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1` |
   | `JWT_ALGORITHM` | `HS256` |
   | `OPENAI_EMBEDDING_MODEL` | `text-embedding-3-small` |
   | `OPENAI_CHAT_MODEL` | `gpt-4-turbo-preview` |
   | `CHUNK_SIZE` | `500` |
   | `CHUNK_OVERLAP` | `50` |
   | `SEARCH_TOP_K` | `5` |
   | `HOST` | `0.0.0.0` |
   | `LOG_LEVEL` | `INFO` |

6. Click "Create Web Service"
7. **Wait 3-5 minutes** for deployment
8. **Copy the service URL** when it shows "Live"
   - Will be like: `https://aems-rag-service-xxxx.onrender.com`

**Why:** This service handles all AI/ML workloads (embeddings, vector search, GPT-4)

---

### **3️⃣ Update Spring Boot Service (2 minutes)**

**What:** Tell Spring Boot where to find the Python RAG service

**How:**

1. Go to: https://dashboard.render.com/
2. Find your **existing Spring Boot service**
3. Click on it → "Environment" tab
4. Click "Add Environment Variable"
5. Add:
   ```
   Key: RAG_SERVICE_URL
   Value: https://aems-rag-service-xxxx.onrender.com
   ```
   (Replace with YOUR Python service URL from step 2)
6. Click "Save Changes" (this will trigger a redeploy)

**Why:** Spring Boot needs to know where to send RAG requests

---

### **4️⃣ Test the Integration (5 minutes)**

**Test 1: Python Service Health**

```bash
curl https://aems-rag-service-xxxx.onrender.com/health
```

**Expected:**
```json
{"status": "healthy", "service": "aems-rag-service"}
```

---

**Test 2: Document Ingestion**

```bash
curl -X POST https://aems-rag-service-xxxx.onrender.com/api/ingest/document \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test: 1000kg Rice available. Price: $50/kg",
    "metadata": {"visibility": "public", "event_type": "test"}
  }'
```

**Expected:**
```json
{"status": "success", "chunks_created": 1}
```

---

**Test 3: Check Database**

In Neon SQL Editor:
```sql
SELECT COUNT(*) FROM langchain_pg_embedding;
-- Should return: 1 (from test above)

SELECT document FROM langchain_pg_embedding LIMIT 1;
-- Should return: "Test: 1000kg Rice available..."
```

---

**Test 4: End-to-End Business Flow**

1. **Register a buyer** via your frontend
2. **Check Render logs** for Python service
   - Should see: "Ingested document: buyer_application"
3. **Check database**:
   ```sql
   SELECT document, cmetadata 
   FROM langchain_pg_embedding 
   ORDER BY created_at DESC 
   LIMIT 1;
   ```
   - Should show the buyer registration info

---

## 🔍 Troubleshooting

### "Extension 'vector' not found"
**Fix:** Run the migration SQL again, ensuring you're on the correct database

### "Cannot connect to database"
**Fix:** Check `DATABASE_URL` is correct in Python service environment variables

### "OpenAI API error"
**Fix:** 
1. Verify API key is correct
2. Check you have credits: https://platform.openai.com/usage
3. Ensure no extra spaces in the key

### "JWT validation failed"
**Fix:** Ensure `JWT_SECRET` is EXACTLY the same in both Spring Boot and Python services

### Spring Boot still fails to connect to Python
**Fix:**
1. Check `RAG_SERVICE_URL` is set in Spring Boot
2. Verify Python service is "Live" in Render
3. Try the health check URL directly in browser

---

## 📊 How to Know It's Working

✅ **Python service shows "Live" in Render**  
✅ **Health check returns `{"status": "healthy"}`**  
✅ **Test ingestion creates row in `langchain_pg_embedding` table**  
✅ **When buyer registers, Render logs show "Ingested document"**  
✅ **Database shows buyer info in `langchain_pg_embedding`**  
✅ **RAG queries return relevant answers**  

---

## 💰 Costs

### OpenAI API
- ~$10-15/month for moderate usage
- Get API key: https://platform.openai.com/api-keys
- Check usage: https://platform.openai.com/usage

### Render
- **Free tier:** Both services can run free initially
- **Cold starts:** 30-60 seconds on free tier
- **Upgrade to paid ($7/service/month):** No cold starts, better performance

### Neon Database
- **Free tier:** 0.5 GB (sufficient for RAG)
- pgvector adds minimal storage overhead

---

## 🚀 After Deployment

Once everything is working:

1. **Test each role** (BUYER, EMPLOYEE, MANAGER, ADMIN)
2. **Monitor OpenAI costs** at https://platform.openai.com/usage
3. **Check Render logs** regularly
4. **Consider upgrading** to paid plans for production

---

## 📞 Need Help?

1. Check `RENDER-DEPLOYMENT-GUIDE.md` for detailed instructions
2. Review Render logs for errors
3. Test each component individually (database → Python → Spring Boot)

---

## ⏱️ Total Time: ~25 minutes

- Database update: 5 min
- Python service deploy: 10 min
- Spring Boot update: 2 min
- Testing: 8 min

---

**Start with Action Item #1 above! 👆**
