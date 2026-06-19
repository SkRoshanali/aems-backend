# 🔧 Vercel 500 Error Fix Guide

**Error:** `This Serverless Function has crashed` / `500: INTERNAL_SERVER_ERROR`

**Status:** Deployment succeeded but runtime failure

---

## 🎯 **Root Causes (In Order of Likelihood)**

### 1️⃣ **Missing Environment Variables** (Most Common)

**Issue:** Environment variables not set in Vercel Project Settings

**How to Fix:**

1. Go to **Vercel Dashboard** → Your project
2. Click **Settings** → **Environment Variables**
3. Add ALL these variables:

```
GOOGLE_API_KEY = AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g

DATABASE_URL = postgresql://username:password@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require

INTERNAL_SECRET = your-secret-key-12345

GOOGLE_EMBEDDING_MODEL = embedding-001

GOOGLE_CHAT_MODEL = gemini-1.5-flash

LOG_LEVEL = INFO
```

**⚠️ CRITICAL:** Make sure `DATABASE_URL` includes username and password!

4. After adding, click **Redeploy** in Deployments tab
5. Wait 2-3 minutes for redeployment

---

### 2️⃣ **Database Connection Failed**

**Issue:** DATABASE_URL is wrong or database is not accessible

**Check:**
```
Format: postgresql://username:password@host:port/database?sslmode=require
```

**Example (Neon):**
```
postgresql://default:abc123xyz@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
```

**How to get from Neon:**
1. Go to https://console.neon.tech
2. Select your project
3. Click "Connection string"
4. Copy the PostgreSQL URL
5. Paste in Vercel as DATABASE_URL

---

### 3️⃣ **pgvector Extension Not Enabled**

**Issue:** Database doesn't have pgvector extension for vector search

**Fix:**

Run this SQL command in Neon console:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Steps:
1. Go to Neon console
2. Open SQL Editor
3. Run the command above
4. Redeploy Vercel

---

### 4️⃣ **Import/Module Error**

**Issue:** Python module import failing (usually google-genai package)

**Check logs:**
```bash
vercel logs --follow
```

Look for import errors like:
```
ModuleNotFoundError: No module named 'google'
```

**Fix:** Requirements already updated, but if you see import errors:
- Check `aems-rag-service/requirements.txt` has `google-generativeai`
- Push change to GitHub
- Redeploy on Vercel

---

## 🚀 **Step-by-Step Troubleshooting**

### **Step 1: Check Environment Variables**

```bash
vercel env list
```

Should show all your variables set.

### **Step 2: View Logs**

```bash
vercel logs --follow
```

This shows REAL error messages. Look for:
- `ModuleNotFoundError`
- `psycopg2` connection errors
- `google` API errors
- `KeyError` on missing env vars

### **Step 3: Test Specific Endpoint**

If you have environment variables set but still getting 500:

```bash
# Test health check endpoint
curl https://your-vercel-project.vercel.app/health
```

This endpoint doesn't need database, so if it works, database is the issue.

### **Step 4: Check Root Cause**

Based on what endpoint fails:
- `/health` fails → environment/import issue
- `/health` works but `/chat/query` fails → database issue
- `/chat/query` timeout → Google API or cold start

---

## ✅ **Quick Fix Checklist**

- [ ] All 6 environment variables set in Vercel?
- [ ] DATABASE_URL includes username:password?
- [ ] DATABASE_URL format is `postgresql://` not `jdbc:`?
- [ ] Neon database is active and accessible?
- [ ] pgvector extension enabled in Neon?
- [ ] Redeploy after setting variables?
- [ ] Check logs for specific error messages?

---

## 📋 **Environment Variables - Copy/Paste Ready**

```
# Paste these into Vercel Settings → Environment Variables

GOOGLE_API_KEY
AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g

DATABASE_URL
postgresql://default:YOUR_PASSWORD@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require

INTERNAL_SECRET
your-secret-key-change-this

GOOGLE_EMBEDDING_MODEL
embedding-001

GOOGLE_CHAT_MODEL
gemini-1.5-flash

LOG_LEVEL
INFO
```

**⚠️ REPLACE `YOUR_PASSWORD` with your actual Neon password!**

---

## 🔍 **If Still Getting 500**

Check Vercel logs for the actual error:

```bash
# Install Vercel CLI if you don't have it
npm i -g vercel

# Login
vercel login

# View logs (real-time)
vercel logs --follow

# Or view deployment logs
vercel logs
```

Copy the error message from logs and we can debug further.

---

## 📞 **Next Steps**

1. **Set all environment variables** in Vercel
2. **Redeploy** the project
3. **Check logs** for errors
4. **Test `/health` endpoint**
5. **If still failing,** share the error from logs

**Most common solution:** Missing `DATABASE_URL` environment variable

