# 🔒 Critical Security Fixes Required

## ⚠️ Issues Identified & Fixed

### **1. Role Spoofing Vulnerability (CRITICAL - FIXED)**

**❌ Original Vulnerability:**
```javascript
// NEVER DO THIS - allows role spoofing!
export const sendDirectRAGQuery = async (query, role, buyerId) => {
  await axios.post(`${RAG_URL}/api/rag/query`, { 
    query, 
    role,  // ← Attacker can change this to "ADMIN"
    buyer_id: buyerId 
  });
};
```

**Problem:**
- Buyer opens DevTools
- Changes `role: "BUYER"` to `role: "ADMIN"` in request
- Python service trusts the role from request body
- Buyer sees all admin documents

**✅ Fixed Solution:**
```javascript
// Only call Spring Boot - it extracts role from JWT
export const sendChatQuery = async (query) => {
  await axios.post(`${API_URL}/api/chat/query`, { 
    query  // No role - Spring Boot gets it from JWT
  });
};
```

**Why This Works:**
1. Frontend sends only query
2. Spring Boot validates JWT
3. Spring Boot extracts verified role from JWT
4. Spring Boot forwards to Python with **server-verified** role
5. Attacker cannot modify role (it's server-side)

---

### **2. CORS Configuration (REQUIRED)**

**Current Issue:**
Your Spring Boot `CorsConfig.java` likely has:
```java
@CrossOrigin(origins = "*")  // Too permissive
```

**✅ Required Fix:**

Update `aems-backend/src/main/java/com/aems/config/CorsConfig.java`:

```java
@Configuration
public class CorsConfig {
    
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // PRODUCTION: Set exact origins
        configuration.setAllowedOrigins(Arrays.asList(
            "https://kind-rock-0f674fd00.1.azurestaticapps.net",  // Your Azure frontend
            "http://localhost:5173",  // Local Vite dev
            "http://localhost:3000"   // Alternative local port
        ));
        
        // Required for Authorization header
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);  // Required for JWT
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Environment Variable in Render:**
```
Key: ALLOWED_ORIGINS
Value: https://kind-rock-0f674fd00.1.azurestaticapps.net,http://localhost:5173
```

**Why `*` Doesn't Work:**
- Wildcard `*` is incompatible with `credentials: true`
- When sending `Authorization` header, browser requires explicit origins
- Without this fix, frontend gets CORS errors

---

### **3. Render Free Tier Cold Start Handling (REQUIRED)**

**Problem:**
- Render free tier spins down after 15 minutes of inactivity
- First request takes 20-30 seconds to wake up
- Chat appears broken to users

**✅ Solution 1: Wake-Up Ping on App Load**

```javascript
// In App.jsx
useEffect(() => {
  const wakeUp = async () => {
    try {
      await axios.get(`${API_BASE_URL}/actuator/health`, { timeout: 5000 });
    } catch (error) {
      // Ignore errors
    }
  };
  wakeUp();
}, []);
```

**✅ Solution 2: Loading State in Chat**

```javascript
// In chatSlice.js
.addCase(sendMessage.rejected, (state, action) => {
  if (action.payload?.includes('timeout') || action.payload?.includes('waking up')) {
    state.isWakingUp = true;
    state.messages.push({
      type: 'system',
      content: '⏳ The AI assistant is waking up (Render free tier). This takes ~30 seconds. Please try again...',
    });
  }
});
```

**✅ Solution 3: UI Feedback**

```jsx
// In ChatPanel.jsx
{isWakingUp && (
  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-4">
    <div className="flex items-center gap-2">
      <div className="animate-spin h-4 w-4 border-2 border-yellow-500 border-t-transparent rounded-full" />
      <p className="text-sm text-yellow-800">
        Waking up the assistant... This happens on Render's free tier. (~30 seconds)
      </p>
    </div>
  </div>
)}
```

---

### **4. Environment Variables at Build Time (CRITICAL)**

**Problem:**
Vite bakes `VITE_*` variables into JavaScript bundle **at build time**, not runtime.

**❌ Wrong Approach:**
```bash
# .env.production file in repo
VITE_API_BASE_URL=https://my-backend.onrender.com
```
This only works if the file exists during `npm run build`.

**✅ Correct Approach for Azure Static Web Apps:**

**Option A: GitHub Actions Workflow**

Update `.github/workflows/azure-static-web-apps.yml`:
```yaml
env:
  VITE_API_BASE_URL: https://your-spring-boot.onrender.com

steps:
  - name: Build
    run: npm run build
```

**Option B: Azure Configuration**

1. Go to Azure Static Web Apps configuration
2. Add environment variable:
   ```
   Name: VITE_API_BASE_URL
   Value: https://your-spring-boot.onrender.com
   ```
3. Trigger a rebuild

**Option C: Repository Secret (Recommended)**

1. GitHub → Repository → Settings → Secrets
2. Add secret: `VITE_API_BASE_URL`
3. Update workflow:
   ```yaml
   env:
     VITE_API_BASE_URL: ${{ secrets.VITE_API_BASE_URL }}
   ```

---

## 📋 **Required Actions Checklist**

### **Backend (Spring Boot):**
- [ ] Update `CorsConfig.java` with exact Azure origin
- [ ] Add `ALLOWED_ORIGINS` environment variable in Render
- [ ] Remove any `@CrossOrigin(origins = "*")` annotations
- [ ] Test CORS with `Authorization` header from Azure domain
- [ ] Verify `/actuator/health` endpoint is accessible (for wake-up ping)

### **Backend (Python RAG):**
- [ ] Verify `auth.py` validates JWT properly
- [ ] Ensure role comes from JWT payload, not request body
- [ ] Add CORS configuration if frontend ever calls directly (it shouldn't)

### **Frontend:**
- [ ] Remove `sendDirectRAGQuery` function entirely
- [ ] Remove `VITE_RAG_BASE_URL` environment variable
- [ ] Add wake-up ping on app load
- [ ] Add "waking up" UI state for Render cold starts
- [ ] Set `VITE_API_BASE_URL` in Azure/GitHub Actions (not .env file)
- [ ] Test with actual Azure domain

---

## 🧪 **Testing the Fixes**

### **Test 1: CORS with Authorization Header**

```javascript
// Run this in browser console on your Azure domain
fetch('https://your-spring-boot.onrender.com/api/chat/query', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer your-jwt-token',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ query: 'test' })
})
.then(res => res.json())
.then(console.log)
.catch(console.error);
```

**Expected:** Response (not CORS error)

---

### **Test 2: Role Spoofing Prevention**

**Scenario:** Buyer tries to see admin data

1. Login as BUYER
2. Open DevTools → Network tab
3. Send chat query
4. Intercept and modify request to add `role: "ADMIN"`
5. **Expected Result:** No effect - Spring Boot ignores client role

**Verification:**
```java
// In Spring Boot RagController
@PostMapping("/api/chat/query")
public ResponseEntity<RAGResponse> query(
    @RequestBody QueryRequest request,
    Authentication authentication
) {
    // Extract role from JWT (server-verified)
    String role = authentication.getAuthorities().stream()
        .findFirst()
        .map(auth -> auth.getAuthority())
        .orElseThrow();
    
    // Forward to Python with server-verified role
    // (ignoring any role sent in request body)
    return ragClient.query(request.getQuery(), role);
}
```

---

### **Test 3: Cold Start Handling**

1. Wait 20 minutes (Render spins down)
2. Try chat query
3. **Expected:** "Waking up" message appears
4. Wait 30 seconds
5. Try again
6. **Expected:** Normal response

---

## 🔐 **Security Best Practices**

### **1. Never Trust Client Input**
```java
// ❌ WRONG - trusts client
String role = request.getRole();

// ✅ RIGHT - extracts from JWT
String role = authentication.getAuthorities()...
```

### **2. Validate JWT on Both Sides**
- Spring Boot validates JWT (primary)
- Python validates JWT (defense in depth)
- Both use same `JWT_SECRET`

### **3. Explicit CORS Origins**
```java
// ❌ WRONG
configuration.setAllowedOrigins(Arrays.asList("*"));

// ✅ RIGHT
configuration.setAllowedOrigins(Arrays.asList(
    "https://specific-domain.com"
));
```

### **4. Environment Variables**
- Use GitHub Secrets for sensitive values
- Never commit `.env` files with real secrets
- Use different secrets for dev/staging/prod

### **5. Rate Limiting (Future Enhancement)**
```java
// Add to Spring Boot for production
@Bean
public RateLimiter rateLimiter() {
    return RateLimiter.of("chatApi", RateLimiterConfig.custom()
        .limitForPeriod(10)  // 10 requests
        .limitRefreshPeriod(Duration.ofMinutes(1))
        .build());
}
```

---

## 📞 **Deployment Order**

Follow this exact order to avoid issues:

1. ✅ **Deploy Python RAG Service** (with JWT validation)
2. ✅ **Update Database** (run pgvector migration)
3. ✅ **Update Spring Boot CORS** (add Azure origin)
4. ✅ **Add `RAG_SERVICE_URL` to Spring Boot** environment
5. ✅ **Test backend APIs** with Postman/curl
6. ✅ **Build frontend** with correct `VITE_API_BASE_URL`
7. ✅ **Deploy frontend** to Azure
8. ✅ **Test end-to-end** from Azure domain

**Key Point:** Test deployed backend BEFORE adding frontend. This catches CORS/auth bugs early.

---

## ✅ **Summary of Fixes**

| Issue | Impact | Fix | Status |
|-------|--------|-----|--------|
| Role spoofing vulnerability | CRITICAL | Remove direct Python calls | ✅ Fixed in docs |
| CORS misconfiguration | HIGH | Add exact Azure origin | ⚠️ Needs implementation |
| Render cold start UX | MEDIUM | Add wake-up ping + UI | ✅ Fixed in docs |
| Env vars at build time | MEDIUM | Use GitHub Secrets | ⚠️ Needs configuration |
| Wildcard CORS + credentials | HIGH | Use explicit origins | ⚠️ Needs implementation |

---

**All security issues documented. Ready to implement fixes! 🔒**
