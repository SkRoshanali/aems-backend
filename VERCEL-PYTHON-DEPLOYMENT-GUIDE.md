# 🚀 Deploy Python RAG Service to Vercel

**Status:** Ready to deploy ✅  
**Service:** FastAPI (aems-rag-service)  
**Platform:** Vercel  
**Language:** Python 3.11+

---

## 📋 **Quick Summary**

Your Python RAG service is already configured for Vercel deployment. Just follow these steps:

1. Connect GitHub repo to Vercel
2. Set environment variables
3. Deploy
4. Update Spring Boot with the Vercel URL

---

## 🎯 **Step-by-Step Deployment**

### **Step 1: Connect Repository to Vercel**

1. Go to **https://vercel.com**
2. Sign in with GitHub
3. Click **"Add New..."** → **"Project"**
4. Select repository: **SkRoshanali/aems-backend**
5. Choose **"Deploy"** (Vercel auto-detects Python project)

### **Step 2: Configure Project Settings**

**Root Directory:** `aems-rag-service`

**Framework Preset:** Python (or leave as "Other")

**Build Command:** Leave blank (Vercel handles it)

**Output Directory:** Leave blank

### **Step 3: Set Environment Variables**

Before deploying, add these environment variables in Vercel:

**In Vercel Project Settings → Environment Variables:**

```
Name: GOOGLE_API_KEY
Value: AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g

Name: DATABASE_URL
Value: your-postgresql-neon-connection-string

Name: INTERNAL_SECRET
Value: your-secret-key-12345

Name: GOOGLE_EMBEDDING_MODEL
Value: embedding-001

Name: GOOGLE_CHAT_MODEL
Value: gemini-1.5-flash

Name: LOG_LEVEL
Value: INFO
```

**⚠️ IMPORTANT:** For `DATABASE_URL`, use your Neon PostgreSQL connection string format:
```
postgresql://user:password@host.neon.tech/database?sslmode=require
```

### **Step 4: Deploy**

Click **"Deploy"** button

Vercel will:
1. Clone your repository
2. Navigate to `aems-rag-service` folder
3. Install dependencies from `requirements.txt`
4. Build and deploy

**Deployment time:** 2-3 minutes

### **Step 5: Get Your URL**

After deployment succeeds, Vercel shows your URL:
```
https://your-project-name.vercel.app
```

This is your **RAG_SERVICE_URL** ✅

---

## 🔗 **Update Spring Boot Service**

Now update your Spring Boot service on Render with the Vercel URL:

### **In Render (Spring Boot service)**

Add/Update these environment variables:

```
RAG_SERVICE_URL=https://your-project-name.vercel.app
RAG_INTERNAL_SECRET=your-secret-key-12345
```

Then redeploy Spring Boot service.

---

## ✅ **Verify Deployment**

### **Test 1: Python Service Health Check**

Open in browser:
```
https://your-project-name.vercel.app/health
```

Should return:
```json
{
  "status": "healthy",
  "service": "rag_query",
  "llm_model": "gemini-1.5-flash"
}
```

### **Test 2: Test from Spring Boot**

1. Log in to your app
2. Open Chat widget
3. Send a message
4. Should work now! ✅

---

## 🚨 **Troubleshooting**

### **Issue: 500 Error on /chat/query**

**Possible Causes:**
1. `GOOGLE_API_KEY` not set in Vercel
2. `DATABASE_URL` incorrect
3. `INTERNAL_SECRET` mismatch between services

**Solution:**
- Check Vercel environment variables
- Check Vercel deployment logs: `vercel logs`
- Make sure secrets match exactly between Spring Boot and Python

### **Issue: Database Connection Failed**

**Possible Causes:**
1. `DATABASE_URL` format wrong
2. Neon database not accessible from Vercel
3. pgvector extension not enabled

**Solution:**
- Verify Neon connection string format
- Check Neon dashboard for active connections
- Ensure pgvector extension is enabled: `CREATE EXTENSION IF NOT EXISTS vector`

### **Issue: Vercel Timeout (>10 seconds)**

Vercel has strict timeout limits (10s for free tier, 60s for pro).

**Solution:**
- Upgrade to Vercel Pro tier
- Or deploy to Render instead (see below)

---

## 🎯 **Alternative: Deploy to Render (Recommended)**

If you prefer Render (longer timeout, better for Python):

### **Step 1: Create New Render Service**

1. Go to **https://dashboard.render.com**
2. Click **"New +"** → **"Web Service"**
3. Connect GitHub: `SkRoshanali/aems-backend`
4. **Root Directory:** `aems-rag-service`
5. **Runtime:** Python
6. **Build Command:** `pip install -r requirements.txt`
7. **Start Command:** `uvicorn app.main:app --host 0.0.0.0 --port $PORT`

### **Step 2: Set Environment Variables (Same as Vercel)**

```
GOOGLE_API_KEY=AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g
DATABASE_URL=your-neon-postgresql-url
INTERNAL_SECRET=your-secret-key-12345
GOOGLE_EMBEDDING_MODEL=embedding-001
GOOGLE_CHAT_MODEL=gemini-1.5-flash
```

### **Step 3: Deploy**

Click **"Create Web Service"** and wait for deployment.

**Render URL:** `https://your-service.onrender.com`

### **Step 4: Update Spring Boot**

```
RAG_SERVICE_URL=https://your-service.onrender.com
```

---

## 📊 **Comparison: Vercel vs Render for Python**

| Feature | Vercel | Render |
|---------|--------|--------|
| **Cold Start** | ~10s | ~20s |
| **Timeout** | 10s (free) / 60s (pro) | 60s (free) |
| **Cost** | Free tier available | Free tier available |
| **Best For** | Fast responses | Long-running tasks, RAG |
| **Python Support** | ✅ Good | ✅ Excellent |

**Recommendation:** Use **Render** for RAG service (more reliable with timeouts)

---

## 🔐 **Security Notes**

### **Keep Secrets Secure:**

✅ **DO:**
- Set Google API key in Vercel/Render environment variables
- Don't commit `.env` file with real keys
- Use different secrets for dev vs production

❌ **DON'T:**
- Hardcode API keys in code
- Commit `.env` file to GitHub
- Share environment variable values

### **Verify Keys are Hidden:**

In Vercel/Render dashboard, your secrets should show as:
```
GOOGLE_API_KEY = ••••••••••
```

Not the actual key value!

---

## 📋 **Deployment Checklist**

### **Before Deploying:**
- [ ] Repository committed and pushed to GitHub
- [ ] `aems-rag-service/` folder exists
- [ ] `requirements.txt` has all dependencies
- [ ] `vercel.json` configured correctly
- [ ] Google API key obtained and tested

### **During Deployment:**
- [ ] Connected GitHub to Vercel/Render
- [ ] Set all environment variables
- [ ] Set correct Root Directory: `aems-rag-service`
- [ ] Deployment succeeds (no errors in logs)

### **After Deployment:**
- [ ] Health check endpoint works: `/health`
- [ ] Updated Spring Boot with `RAG_SERVICE_URL`
- [ ] Chat functionality works end-to-end
- [ ] No 500 errors in frontend console

---

## 🚀 **Next Steps**

1. **Choose platform:** Vercel (faster) or Render (reliable)
2. **Deploy Python service** using steps above
3. **Get the deployment URL**
4. **Update Spring Boot** with `RAG_SERVICE_URL`
5. **Test chat functionality**

**Estimated time to complete:** 15 minutes

---

## 📞 **Need Help?**

If deployment fails:

1. **Check Vercel/Render logs:**
   ```bash
   vercel logs --follow
   # or
   render logs
   ```

2. **Check environment variables:**
   - All required vars set?
   - Values correct?
   - No extra spaces?

3. **Test locally first:**
   ```bash
   cd aems-rag-service
   python -m uvicorn app.main:app --reload
   ```

---

**You're almost there! Deploy the Python service and chat will work! 🎉**

