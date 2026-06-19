# 🔐 Environment Variables Setup Guide

**Status:** Ready to configure  
**Date:** June 19, 2026

---

## 📋 **Your Current Variables**

```
DATABASE_URL: jdbc:postgresql://ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
GOOGLE_API_KEY: AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g
INTERNAL_SECRET: (need to set)
JWT_SECRET: 3f8a2b1c9d4e7f6a5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1
ENCRYPTION_KEY: 4a7b3c9d2e8f1a6b5c4d3e2f1a0b9c8d
```

---

## ⚠️ **IMPORTANT: Database URL Format Issue**

**Problem:** You have JDBC format, but Python needs PostgreSQL format

### Current (JDBC - for Java/Spring Boot):
```
jdbc:postgresql://ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
```

### Correct (PostgreSQL - for Python):
```
postgresql://your-user:your-password@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
```

**You need to add username and password!**

Go to Neon dashboard and get:
- Username: `default` (or your user)
- Password: (your connection password)
- Then format as: `postgresql://username:password@host/database?sslmode=require`

---

## 🎯 **VERCEL (Python RAG Service) Environment Variables**

Set these in **Vercel Project Settings → Environment Variables:**

```
Name: GOOGLE_API_KEY
Value: AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g

Name: DATABASE_URL
Value: postgresql://username:password@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
(Replace username and password!)

Name: INTERNAL_SECRET
Value: your-internal-secret-key-12345
(Can be anything, must match Spring Boot)

Name: GOOGLE_EMBEDDING_MODEL
Value: embedding-001

Name: GOOGLE_CHAT_MODEL
Value: gemini-1.5-flash

Name: LOG_LEVEL
Value: INFO
```

---

## 🎯 **RENDER (Spring Boot Service) Environment Variables**

Set these in **Render Service Settings → Environment:**

```
# Database (JDBC format - for Spring Boot)
DATABASE_URL=jdbc:postgresql://ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require

# Google API (for chat warmup calls to Python)
GOOGLE_API_KEY=AIzaSyAb8RN6KYf2k8k4agGd1noSFRy-gaCrEFjV_b0rj1hNPi4WbK_g

# JWT Authentication
JWT_SECRET=3f8a2b1c9d4e7f6a5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1

# Encryption
ENCRYPTION_KEY=4a7b3c9d2e8f1a6b5c4d3e2f1a0b9c8d

# RAG Service Connection (Point to Vercel-deployed Python)
RAG_SERVICE_URL=https://your-vercel-project.vercel.app
RAG_INTERNAL_SECRET=your-internal-secret-key-12345

# Email (if configured)
MAIL_HOST=your-mail-host
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Environment
ENVIRONMENT=production
```

---

## 🔄 **Step-by-Step Setup Process**

### **Step 1: Fix Database URL**

1. Go to **Neon Console** → https://console.neon.tech
2. Select your project
3. Find your connection string that looks like:
   ```
   postgresql://default:yourpassword@ep-shy-resonance-a9t6ax2l-pooler.gwc.azure.neon.tech/neondb?sslmode=require
   ```
4. Copy the full string (this is correct PostgreSQL format)

### **Step 2: Deploy to Vercel**

1. Go to **Vercel Dashboard** → Your project
2. Settings → Environment Variables
3. Add all the variables from "VERCEL" section above
4. **Use the correct PostgreSQL format from Step 1** for DATABASE_URL
5. Click **Deploy** or manually redeploy

### **Step 3: Get Vercel URL**

1. After deployment succeeds
2. Copy the URL (e.g., `https://your-project-name.vercel.app`)
3. Save this for Step 4

### **Step 4: Update Render (Spring Boot)**

1. Go to **Render Dashboard** → Spring Boot service
2. Settings → Environment
3. Update/Add these:

```
RAG_SERVICE_URL=https://your-project-name.vercel.app
RAG_INTERNAL_SECRET=your-internal-secret-key-12345
```

4. Trigger redeploy
5. Wait for deployment to complete

### **Step 5: Test**

1. Open your app
2. Log in
3. Open Chat widget
4. Send a message
5. Should work! ✅

---

## 📊 **Variables by Service**

### **Both Services Need:**
```
GOOGLE_API_KEY              ✅
DATABASE_URL                ✅ (different formats)
INTERNAL_SECRET             ✅
```

### **Spring Boot (Render) Only:**
```
JWT_SECRET                  (for JWT token signing)
ENCRYPTION_KEY              (for data encryption)
RAG_SERVICE_URL             (points to Vercel)
RAG_INTERNAL_SECRET         (must match Python)
MAIL_HOST                   (optional, for emails)
```

### **Python (Vercel) Only:**
```
GOOGLE_EMBEDDING_MODEL      (embedding-001)
GOOGLE_CHAT_MODEL           (gemini-1.5-flash)
LOG_LEVEL                   (INFO/DEBUG)
```

---

## 🔒 **Security Checklist**

✅ **DO:**
- [ ] Use same `INTERNAL_SECRET` for both services
- [ ] Different secret per environment (dev/prod)
- [ ] Store secrets in Vercel/Render UI, not in code
- [ ] Rotate secrets periodically
- [ ] Never commit `.env` files

❌ **DON'T:**
- [ ] Hardcode secrets in source code
- [ ] Use same secret for multiple apps
- [ ] Share secrets via email/chat
- [ ] Commit secrets to GitHub

---

## 🚨 **Common Mistakes**

### ❌ **Mistake 1: Wrong Database URL Format**

```
WRONG (JDBC - Java format):
jdbc:postgresql://host/db

CORRECT (PostgreSQL - Python format):
postgresql://user:pass@host/db
```

### ❌ **Mistake 2: Mismatched INTERNAL_SECRET**

**Vercel Python:**
```
INTERNAL_SECRET=secret123
```

**Render Spring Boot:**
```
RAG_INTERNAL_SECRET=secret456  ❌ DIFFERENT!
```

Should be the SAME! Use `secret123` in both.

### ❌ **Mistake 3: Missing RAG_SERVICE_URL**

Spring Boot can't find Python service if `RAG_SERVICE_URL` not set.

Result: Chat returns 500 error

### ❌ **Mistake 4: Expired Google API Key**

If key has usage limits or is revoked:
- Get new key from Google Cloud Console
- Update in both Vercel and Render

---

## ✅ **Verification Steps**

### **Test 1: Check Vercel Python Service**

```
curl https://your-vercel-project.vercel.app/health
```

Should return:
```json
{
  "status": "healthy",
  "service": "rag_query",
  "llm_model": "gemini-1.5-flash"
}
```

### **Test 2: Check Spring Boot Service**

```
curl https://aems-backend-1-xxx.onrender.com/api/actuator/health
```

Should return:
```json
{
  "status": "UP"
}
```

### **Test 3: Test Chat End-to-End**

1. Log in to app
2. Open Chat widget
3. Send message: "Hello"
4. Should get AI response within 45 seconds
5. No 500 errors in console ✅

---

## 📋 **Final Checklist Before Production**

- [ ] Database URL in correct format (PostgreSQL, not JDBC)
- [ ] Added username:password to database URL
- [ ] Vercel environment variables set
- [ ] Render environment variables set
- [ ] INTERNAL_SECRET matches in both services
- [ ] RAG_SERVICE_URL points to Vercel deployment
- [ ] Vercel service deployed successfully
- [ ] Render service redeployed
- [ ] Health check endpoints work
- [ ] Chat functionality tested
- [ ] No 500 errors in console
- [ ] Secrets not committed to GitHub

---

## 🆘 **Troubleshooting**

### **Chat returns 500 error**

Check:
1. Is Vercel service deployed? (Check deployment logs)
2. Is DATABASE_URL correct? (Try connection locally)
3. Is INTERNAL_SECRET set in both services?
4. Is RAG_SERVICE_URL pointing to correct Vercel URL?

### **Database connection failed**

Check:
1. PostgreSQL URL format: `postgresql://user:pass@host/db?sslmode=require`
2. Username and password correct
3. Neon database still exists
4. IP whitelist allows Vercel/Render IPs (usually auto-allowed)

### **Timeout or slow response**

Check:
1. Vercel cold start (first request is slow, ~10 seconds)
2. Large chat queries might timeout on Vercel free tier
3. Consider upgrading to Vercel Pro if needed

---

## 🎯 **Your Action Items**

1. **Get correct PostgreSQL URL from Neon**
   - Format: `postgresql://user:password@host/db?sslmode=require`

2. **Deploy to Vercel** with Python environment variables
   - Use correct PostgreSQL format for DATABASE_URL
   - Use same INTERNAL_SECRET for both services

3. **Update Render** with RAG_SERVICE_URL
   - Copy Vercel URL after deployment

4. **Test chat** - should work! ✅

---

**You have all the pieces! Just organize them correctly and deploy! 🚀**

