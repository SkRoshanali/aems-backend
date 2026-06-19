# 🔧 PRODUCTION ISSUES & FIXES

**Status:** Backend deployed ✅ | Frontend issues identified 🔴  
**Date:** June 19, 2026  
**Environment:** Render (Spring Boot) + Azure Static Web Apps (React)

---

## 🎯 ISSUES FOUND

### 1️⃣ **CRITICAL: Chat URL Double `/api/api` Path**

**Error:** `aems-backend-1-w9zj.onrender.com/api/api/chat/query:1  Failed to load resource: status 500`

**Problem:**
- Frontend is calling `/api/api/chat/query` instead of `/api/chat/query`
- Axios baseURL likely includes `/api`, then code adds `/api` again

**Root Cause Location:** `src/api/chatService.js` or `baseApi.js`

**Fix:**
```javascript
// WRONG (creates /api/api):
const response = await axios.post('/api/chat/query', ...)

// CORRECT (uses /api from baseURL):
const response = await axios.post('/chat/query', ...)
```

**Status:** 🔴 **NEEDS FIX**

---

### 2️⃣ **CRITICAL: Chat Redux Error - `.includes()` not a function**

**Error:** `TypeError: n?.includes is not a function`

**Problem:**
- Chat error handler trying to call `.includes()` on non-string value
- Error message is likely an object, not a string
- Redux trying to check if error message includes "waking up"

**Root Cause Location:** `src/store/chatSlice.js` in `sendMessage.rejected`

**Current Code (BROKEN):**
```javascript
.addCase(sendMessage.rejected, (state, action) => {
  const errorMsg = action.payload;
  
  if (errorMsg?.includes('waking up')) {  // ❌ ERROR HERE
    // ...
  }
})
```

**Fix:**
```javascript
.addCase(sendMessage.rejected, (state, action) => {
  let errorMsg = action.payload;
  
  // Convert to string if it's an object
  if (typeof errorMsg !== 'string') {
    errorMsg = errorMsg?.message || errorMsg?.error || 'Unknown error';
  }
  
  if (errorMsg?.includes?.('waking up')) {
    state.isWakingUp = true;
    state.messages.push({
      type: 'system',
      content: '⏳ The AI assistant is waking up (Render free tier). Please try again in a moment...',
      timestamp: new Date().toISOString(),
    });
  } else {
    state.error = errorMsg;
    state.messages.push({
      type: 'error',
      content: 'Sorry, I encountered an error: ' + errorMsg,
      timestamp: new Date().toISOString(),
    });
  }
})
```

**Status:** 🔴 **NEEDS FIX**

---

### 3️⃣ **HIGH: Chat UI - Cancel Button Overlapping Send Button**

**Problem:**
- Close button (X) is positioned on top of send button
- User can't click send button
- Panel header close button interferes with input area

**Root Cause:** ChatPanel.jsx header layout issue

**Fix Location:** `src/components/chat/ChatPanel.jsx` header section

**Current (BROKEN):**
```jsx
<div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 
                rounded-t-lg flex justify-between items-center">
  {/* This close button might be overlapping */}
  <button onClick={onClose} className="hover:bg-white/20 p-1 rounded">
    <Minimize2 size={20} />
  </button>
</div>
```

**Fix:**
```jsx
<div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 
                rounded-t-lg flex justify-between items-center z-10">
  <div>
    <h3 className="font-semibold">AI Assistant</h3>
    <p className="text-xs opacity-90">Role: {userRole}</p>
  </div>
  <button onClick={onClose} className="hover:bg-white/20 p-1 rounded flex-shrink-0 ml-2">
    <Minimize2 size={20} />
  </button>
</div>
```

**Status:** 🔴 **NEEDS FIX**

---

### 4️⃣ **HIGH: Order Approve/Reject Returning 400 Errors**

**Error:** `aems-backend-1-w9zj.onrender.com/api/orders/3/approve:1  Failed to load resource: the server responded with a status of 400`

**Problem:**
- Multiple 400 errors on `/api/orders/{id}/approve`
- Likely validation or authentication issue
- Endpoint requires `@RequestParam String reason` for reject but approve should work

**Possible Causes:**
1. Missing JWT token in request
2. User doesn't have MANAGER/ADMIN role
3. Order not in correct status for approval
4. Database constraint violation

**Investigation Needed:**
1. Check browser Network tab to see request headers (JWT token present?)
2. Check current user role (should be ADMIN or MANAGER)
3. Check order status before approval

**Status:** 🟡 **NEEDS INVESTIGATION** (might work if user has correct role)

---

### 5️⃣ **HIGH: Farmer Verify Returning 400 Errors**

**Error:** `aems-backend-1-w9zj.onrender.com/api/farmers/5/verify:1  Failed to load resource: the server responded with a status of 400`

**Problem:**
- `/api/farmers/{id}/verify` endpoint returning 400
- Multiple retry attempts all fail

**Possible Causes:**
1. Missing authentication header
2. User role insufficient
3. Farmer not in correct status

**Status:** 🟡 **NEEDS INVESTIGATION**

---

### 6️⃣ **HIGH: Order Reject Returning 500 Errors**

**Error:** `aems-backend-1-w9zj.onrender.com/api/orders/6/reject:1  Failed to load resource: the server responded with a status of 500`

**Problem:**
- 500 = Server error (backend crash)
- Missing `reason` query parameter

**Root Cause:**
Reject endpoint requires reason:
```java
@PutMapping("/{id}/reject")
public ResponseEntity<Order> rejectOrder(
    @PathVariable Long id,
    @RequestParam String reason,  // ← REQUIRED
    Authentication authentication)
```

**Fix in Frontend:**
Make sure reject calls include reason parameter:
```javascript
// WRONG (causes 500):
axios.put(`/api/orders/${id}/reject`)

// CORRECT:
axios.put(`/api/orders/${id}/reject?reason=Quality%20not%20met`)
```

**Status:** 🔴 **NEEDS FIX** (missing parameter)

---

### 7️⃣ **MEDIUM: Chat Timeout Still Happening**

**Error:** `Chat query failed: AxiosError: timeout of 40000ms exceeded`

**Problem:**
- Render cold start still taking >40 seconds
- Wake-up ping might not be helping
- Python RAG service might be slow

**Possible Causes:**
1. Python service not starting fast enough
2. Warmup endpoint not waking Python correctly
3. Render free tier spinning down both services independently

**Potential Fix:**
Increase timeout to 60 seconds or handle timeout gracefully

**Status:** 🟡 **ACCEPTABLE** (handled with banner + retry)

---

## ✅ FIXES TO APPLY (IN ORDER)

### Priority 1: CRITICAL (Blocks Chat & Operations)

#### **Fix 1: Chat URL Double `/api` Issue**
**File:** `src/api/chatService.js`

Change:
```javascript
// FROM:
await axios.post(`${API_BASE_URL}/api/chat/query`, ...)

// TO:
await axios.post(`${API_BASE_URL}/chat/query`, ...)
```

Also check `baseApi.js` - if it already has `/api` prefix in baseURL, don't add it again.

---

#### **Fix 2: Chat Redux `.includes()` Error**
**File:** `src/store/chatSlice.js`

Update the `sendMessage.rejected` handler to safely check error messages (see issue #2 above).

---

#### **Fix 3: Chat UI - Button Overlap**
**File:** `src/components/chat/ChatPanel.jsx`

Update header layout to prevent button overlap (see issue #3 above).

---

### Priority 2: HIGH (Blocks Order Operations)

#### **Fix 4: Order Reject Missing Reason Parameter**
**File:** `src/pages/OrderDetails.jsx` or wherever order reject is called

Ensure reject calls include `reason` query parameter:
```javascript
const response = await axios.put(
  `${API_BASE_URL}/api/orders/${orderId}/reject?reason=Rejected%20by%20user`,
  {},
  {
    headers: { Authorization: `Bearer ${token}` }
  }
);
```

---

#### **Fix 5: Verify Order/Farmer Authorization**
Check if user is logged in with correct role (ADMIN/MANAGER for order approval, etc.)

---

## 🔍 DEBUGGING STEPS

### For Chat Issues:

1. **Open Browser DevTools** → Network tab
2. **Send a chat message**
3. **Look for the request:**
   - Should be `/api/chat/query` ✅
   - Check status (should be 200/201, not 500)
   - Check request headers (should have `Authorization: Bearer <token>`)
   - Check response body for error details

### For Order/Farmer Issues:

1. **Check current user role** (should show in dashboard)
2. **Check if token is valid** (Session expires at: ...)
3. **Try with correct role** (login as ADMIN to approve orders)

---

## 📋 SUMMARY TABLE

| Issue | Type | Location | Fix | Status |
|-------|------|----------|-----|--------|
| Chat URL `/api/api` | CRITICAL | chatService.js | Remove extra `/api` | 🔴 BLOCKED |
| Redux `.includes()` error | CRITICAL | chatSlice.js | Safe error handling | 🔴 BLOCKED |
| Chat button overlap | HIGH | ChatPanel.jsx | Fix header layout | 🔴 BLOCKED |
| Order approve 400 | HIGH | Frontend headers | Check JWT token | 🟡 CHECK |
| Farmer verify 400 | HIGH | Frontend headers | Check JWT token | 🟡 CHECK |
| Order reject 500 | HIGH | Order API call | Add reason param | 🔴 BLOCKED |
| Chat timeout | MEDIUM | chatService.js | Handle gracefully | 🟡 ACCEPTABLE |

---

## 🚀 NEXT STEPS

1. **Fix Priority 1 issues first** (chat is completely broken)
2. **Test chat functionality** after fixes
3. **Fix Priority 2 issues** (order operations)
4. **Redeploy frontend** to Azure Static Web Apps
5. **Retest all functionality**

**Estimated Fix Time:** 30 minutes for all issues

