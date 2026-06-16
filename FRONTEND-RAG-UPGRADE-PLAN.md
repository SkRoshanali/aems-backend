# 🎨 Frontend RAG Chat Integration Plan

## 📊 Current Frontend Analysis

### **Technology Stack:**
- **Framework:** React with Vite
- **State Management:** Redux Toolkit
- **Routing:** React Router DOM v7
- **Styling:** Tailwind CSS
- **HTTP Client:** Axios
- **Charts:** Recharts

### **Current Pages:**
- **Buyer:** Dashboard, Orders, Order Details, Invoices, Profile, Storefront
- **Internal (Admin/Manager/Employee):** Dashboard, Orders, Stock, Farmers, Imports, Shipments, Reports, Users, Audit, Pending Buyers
- **Public:** Landing, Login, Register

### **Missing:** AI Chat Assistant for role-based help 🤖

---

## 🎯 **What We're Adding:**

A **floating AI chat assistant** that:
- ✅ Appears on all authenticated pages
- ✅ Provides role-specific help (Buyer, Employee, Manager, Admin)
- ✅ Answers questions about orders, stock, procedures, etc.
- ✅ Uses the RAG backend we built
- ✅ Beautiful, modern UI with animations
- ✅ Chat history within session
- ✅ Minimize/maximize functionality

---

## 🏗️ **Implementation Plan**

### **Phase 1: Create Chat Components** (New Files)

#### 1. **ChatWidget.jsx** - Main floating chat button
```
src/components/chat/ChatWidget.jsx
```
- Floating button in bottom-right corner
- Click to open chat panel
- Badge showing unread AI suggestions
- Smooth animations

#### 2. **ChatPanel.jsx** - Chat interface
```
src/components/chat/ChatPanel.jsx
```
- Slide-in panel from right
- Message list with user/AI bubbles
- Input field with send button
- Loading states during AI response
- Minimize/maximize controls
- Role-specific header (different colors per role)

#### 3. **ChatMessage.jsx** - Individual message component
```
src/components/chat/ChatMessage.jsx
```
- User messages (right-aligned, blue)
- AI messages (left-aligned, gray)
- Timestamp
- Copy button for AI responses
- Source documents accordion (optional)

#### 4. **ChatService.js** - API integration
```
src/api/chatService.js
```
- Send query to backend
- Handle streaming responses (future)
- Error handling
- Retry logic

---

### **Phase 2: Redux State Management**

#### 5. **chatSlice.js** - Redux slice for chat state
```
src/store/chatSlice.js
```

**State:**
```javascript
{
  messages: [],           // Chat history
  isOpen: false,          // Panel open/closed
  isLoading: false,       // AI responding
  error: null,            // Error message
  userRole: null,         // Current user role
  suggestions: []         // Quick suggestions based on role
}
```

**Actions:**
- `openChat()`
- `closeChat()`
- `sendMessage(query)`
- `receiveMessage(response)`
- `setError(error)`
- `clearChat()`

---

### **Phase 3: Role-Specific Features**

#### **Buyer Chat Prompts:**
- "What products are available?"
- "Show me my recent orders"
- "What's the status of order #ORD-123?"
- "How do I place an order?"
- "What are your payment terms?"

#### **Employee Chat Prompts:**
- "How do I add new stock?"
- "Show me farmer list"
- "What's the quality grading process?"
- "How to create a shipment?"

#### **Manager Chat Prompts:**
- "Show me pending buyer applications"
- "What's today's revenue?"
- "List low stock items"
- "How many orders need approval?"

#### **Admin Chat Prompts:**
- "System health status"
- "User activity summary"
- "Show all pending tasks"
- "Generate monthly report"

---

### **Phase 4: UI/UX Design**

#### **Color Scheme by Role:**
```javascript
const roleColors = {
  BUYER: {
    primary: 'blue',
    gradient: 'from-blue-500 to-blue-600'
  },
  EMPLOYEE: {
    primary: 'green',
    gradient: 'from-green-500 to-green-600'
  },
  MANAGER: {
    primary: 'purple',
    gradient: 'from-purple-500 to-purple-600'
  },
  ADMIN: {
    primary: 'red',
    gradient: 'from-red-500 to-red-600'
  },
  SUPER_ADMIN: {
    primary: 'indigo',
    gradient: 'from-indigo-500 to-indigo-600'
  }
}
```

#### **Chat Widget Position:**
- Fixed bottom-right: `bottom-6 right-6`
- Z-index: `z-50`
- Floating above all content
- Smooth slide animations

#### **Chat Panel:**
- Width: `w-96` (384px)
- Height: `h-[600px]`
- Fixed right edge
- Smooth slide-in/out
- Backdrop blur on mobile

---

## 📁 **New File Structure**

```
src/
├── components/
│   └── chat/                    ← NEW FOLDER
│       ├── ChatWidget.jsx       ← Floating button
│       ├── ChatPanel.jsx        ← Main chat UI
│       ├── ChatMessage.jsx      ← Message bubbles (supports user/ai/system/error)
│       ├── ChatSuggestions.jsx  ← Quick prompts
│       └── ChatLoading.jsx      ← Loading animation
│
├── api/
│   └── chatService.js           ← NEW - RAG API calls
│
├── store/
│   └── chatSlice.js             ← NEW - Redux slice
│
└── utils/
    └── chatHelpers.js           ← NEW - Helper functions
```

---

## 🔧 **Code Examples**

### **1. ChatWidget.jsx (Floating Button)**

```jsx
import { useState } from 'react';
import { MessageCircle, X } from 'lucide-react';
import ChatPanel from './ChatPanel';

function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      {/* Floating Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 z-50 bg-gradient-to-r from-blue-500 to-blue-600 
                   text-white p-4 rounded-full shadow-lg hover:shadow-xl transform 
                   hover:scale-110 transition-all duration-300"
        aria-label="Open AI Assistant"
      >
        {isOpen ? (
          <X size={24} />
        ) : (
          <MessageCircle size={24} />
        )}
      </button>

      {/* Chat Panel */}
      {isOpen && <ChatPanel onClose={() => setIsOpen(false)} />}
    </>
  );
}

export default ChatWidget;
```

---

### **2. ChatPanel.jsx (Chat Interface)**

```jsx
import { useState, useEffect, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Send, Minimize2 } from 'lucide-react';
import { sendMessage } from '../store/chatSlice';
import ChatMessage from './ChatMessage';
import ChatSuggestions from './ChatSuggestions';

function ChatPanel({ onClose }) {
  const [input, setInput] = useState('');
  const dispatch = useDispatch();
  const { messages, isLoading, isWakingUp, userRole } = useSelector((state) => state.chat);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = () => {
    if (!input.trim() || isLoading) return;  // FIXED: Only block on isLoading
    
    // Only send query - Spring Boot extracts role from JWT
    dispatch(sendMessage({ query: input }));
    setInput('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="fixed bottom-6 right-6 w-96 h-[600px] bg-white rounded-lg shadow-2xl 
                    flex flex-col z-40 animate-slide-in">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 
                      rounded-t-lg flex justify-between items-center">
        <div>
          <h3 className="font-semibold">AI Assistant</h3>
          <p className="text-xs opacity-90">Role: {userRole}</p>
        </div>
        <button onClick={onClose} className="hover:bg-white/20 p-1 rounded">
          <Minimize2 size={20} />
        </button>
      </div>

      {/* Wake-up Banner (Informational - Does NOT block input) */}
      {isWakingUp && (
        <div className="bg-yellow-50 border-b border-yellow-200 p-3">
          <div className="flex items-start gap-2">
            <div className="animate-spin h-4 w-4 border-2 border-yellow-500 border-t-transparent 
                            rounded-full mt-0.5" />
            <div>
              <p className="text-sm font-medium text-yellow-800">Waking up services...</p>
              <p className="text-xs text-yellow-700 mt-1">
                Render free tier is starting up (~30 seconds). You can still retry!
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        {messages.length === 0 ? (
          <ChatSuggestions role={userRole} onSelect={setInput} />
        ) : (
          messages.map((msg, idx) => (
            <ChatMessage key={idx} message={msg} />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input - FIXED: Only disabled by isLoading, NOT isWakingUp */}
      <div className="p-4 border-t bg-white rounded-b-lg">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Ask me anything..."
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 
                       focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
            disabled={isLoading}
          />
          <button
            onClick={handleSend}
            disabled={isLoading || !input.trim()}
            className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 
                       disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? (
              <div className="animate-spin h-5 w-5 border-2 border-white border-t-transparent 
                              rounded-full" />
            ) : (
              <Send size={20} />
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ChatPanel;
```

---

### **3. ChatMessage.jsx (Message Display Component)**

```jsx
import { Copy, CheckCircle, AlertCircle, Info } from 'lucide-react';
import { useState } from 'react';

function ChatMessage({ message }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(message.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Format timestamp
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  // System message (yellow banner - for wake-up notifications)
  if (message.type === 'system') {
    return (
      <div className="bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded">
        <div className="flex items-start gap-2">
          <Info size={18} className="text-yellow-600 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-sm text-yellow-800">{message.content}</p>
            <span className="text-xs text-yellow-600 mt-1 block">
              {formatTime(message.timestamp)}
            </span>
          </div>
        </div>
      </div>
    );
  }

  // Error message (red banner)
  if (message.type === 'error') {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-3 rounded">
        <div className="flex items-start gap-2">
          <AlertCircle size={18} className="text-red-600 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-sm text-red-800">{message.content}</p>
            <span className="text-xs text-red-600 mt-1 block">
              {formatTime(message.timestamp)}
            </span>
          </div>
        </div>
      </div>
    );
  }

  // User message (right-aligned, blue)
  if (message.type === 'user') {
    return (
      <div className="flex justify-end">
        <div className="max-w-[75%]">
          <div className="bg-blue-500 text-white px-4 py-2 rounded-lg rounded-br-sm">
            <p className="text-sm">{message.content}</p>
          </div>
          <span className="text-xs text-gray-500 mt-1 block text-right">
            {formatTime(message.timestamp)}
          </span>
        </div>
      </div>
    );
  }

  // AI message (left-aligned, gray, with copy button)
  if (message.type === 'ai') {
    return (
      <div className="flex justify-start">
        <div className="max-w-[75%]">
          <div className="bg-gray-200 text-gray-800 px-4 py-2 rounded-lg rounded-bl-sm">
            <p className="text-sm whitespace-pre-wrap">{message.content}</p>
            
            {/* Sources (if available) */}
            {message.sources && message.sources.length > 0 && (
              <div className="mt-2 pt-2 border-t border-gray-300">
                <p className="text-xs text-gray-600 font-semibold mb-1">Sources:</p>
                <ul className="text-xs text-gray-600 space-y-0.5">
                  {message.sources.slice(0, 3).map((source, idx) => (
                    <li key={idx} className="truncate">
                      • {source.metadata?.document_type || 'Document'} 
                      {source.metadata?.entity_id && ` (${source.metadata.entity_id})`}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
          
          <div className="flex items-center justify-between mt-1">
            <span className="text-xs text-gray-500">
              {formatTime(message.timestamp)}
            </span>
            <button
              onClick={handleCopy}
              className="text-gray-500 hover:text-gray-700 p-1 rounded hover:bg-gray-100 
                         transition-colors"
              title="Copy message"
            >
              {copied ? (
                <CheckCircle size={14} className="text-green-500" />
              ) : (
                <Copy size={14} />
              )}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Fallback for unknown message types
  return (
    <div className="bg-gray-100 p-3 rounded text-sm text-gray-600">
      Unknown message type: {message.type}
    </div>
  );
}

export default ChatMessage;
```

---

### **4. ChatSuggestions.jsx (Quick Prompt Buttons)**

```jsx
import { MessageSquare } from 'lucide-react';

function ChatSuggestions({ role, onSelect }) {
  // Role-specific suggestions
  const suggestions = {
    BUYER: [
      "What products are available?",
      "Show me my recent orders",
      "How do I place an order?",
      "What are your payment terms?"
    ],
    EMPLOYEE: [
      "How do I add new stock?",
      "Show me farmer list",
      "What's the quality grading process?",
      "How to create a shipment?"
    ],
    MANAGER: [
      "Show me pending buyer applications",
      "What's today's revenue?",
      "List low stock items",
      "How many orders need approval?"
    ],
    ADMIN: [
      "System health status",
      "User activity summary",
      "Show all pending tasks",
      "Generate monthly report"
    ],
    SUPER_ADMIN: [
      "System overview",
      "User management help",
      "Security best practices",
      "Database backup status"
    ]
  };

  const roleSuggestions = suggestions[role] || suggestions.BUYER;

  return (
    <div className="space-y-4">
      <div className="text-center">
        <MessageSquare className="mx-auto text-gray-400 mb-2" size={48} />
        <h3 className="text-lg font-semibold text-gray-700">How can I help you?</h3>
        <p className="text-sm text-gray-500 mt-1">Try asking me something...</p>
      </div>
      
      <div className="space-y-2">
        <p className="text-xs text-gray-600 font-semibold">Suggestions:</p>
        {roleSuggestions.map((suggestion, idx) => (
          <button
            key={idx}
            onClick={() => onSelect(suggestion)}
            className="w-full text-left px-4 py-2 bg-white border border-gray-200 
                       rounded-lg hover:bg-blue-50 hover:border-blue-300 transition-colors 
                       text-sm text-gray-700"
          >
            {suggestion}
          </button>
        ))}
      </div>
    </div>
  );
}

export default ChatSuggestions;
```

---

### **5. Add to App.jsx**

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Get token from localStorage
const getToken = () => localStorage.getItem('token');

/**
 * Send chat query to Spring Boot backend
 * 
 * SECURITY: Always go through Spring Boot, never call Python RAG directly!
 * Spring Boot extracts role from verified JWT and forwards to Python.
 * This prevents role spoofing attacks where a buyer could claim to be admin.
 */
export const sendChatQuery = async (query) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/chat/query`,
      { query },  // Only send query, NOT role - Spring Boot gets role from JWT
      {
        timeout: 40000, // 40 seconds - generous for Render cold start + RAG pipeline
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('Chat query failed:', error);
    
    // Handle timeout (Render cold start)
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      throw new Error('The assistant is waking up, please try again in a moment...');
    }
    
    // Handle network errors
    if (error.code === 'ERR_NETWORK') {
      throw new Error('Cannot connect to server. Please check your connection.');
    }
    
    throw error;
  }
};

/**
 * Warm up both Spring Boot AND Python RAG service
 * Render free tier spins down both services independently
 * Call this on app load to reduce initial chat latency
 */
export const wakeUpBackend = async () => {
  try {
    // Wake up Spring Boot + Python in one call
    await axios.get(`${API_BASE_URL}/api/chat/warmup`, { 
      timeout: 5000,
      // No auth needed for warmup endpoint
    });
  } catch (error) {
    // Ignore errors - this is best-effort ping
    console.log('Backend wake-up ping sent (may timeout, but still wakes services)');
  }
};
```

---

### **7. chatSlice.js (Redux State)**

```javascript
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { sendChatQuery } from '../api/chatService';

// Async thunk for sending messages
export const sendMessage = createAsyncThunk(
  'chat/sendMessage',
  async ({ query }, { rejectWithValue }) => {
    try {
      // Only send query - Spring Boot extracts role from JWT
      const response = await sendChatQuery(query);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const chatSlice = createSlice({
  name: 'chat',
  initialState: {
    messages: [],
    isOpen: false,
    isLoading: false,
    isWakingUp: false,  // NEW: Track Render cold start
    error: null,
    userRole: null,
  },
  reducers: {
    openChat: (state) => {
      state.isOpen = true;
    },
    closeChat: (state) => {
      state.isOpen = false;
    },
    setUserRole: (state, action) => {
      state.userRole = action.payload;
    },
    clearChat: (state) => {
      state.messages = [];
      state.error = null;
    },
    setWakingUp: (state, action) => {
      state.isWakingUp = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(sendMessage.pending, (state, action) => {
        state.isLoading = true;
        state.error = null;
        // Add user message immediately
        state.messages.push({
          type: 'user',
          content: action.meta.arg.query,
          timestamp: new Date().toISOString(),
        });
      })
      .addCase(sendMessage.fulfilled, (state, action) => {
        state.isLoading = false;
        state.isWakingUp = false;
        // Add AI response
        state.messages.push({
          type: 'ai',
          content: action.payload.answer,
          sources: action.payload.sources,
          timestamp: new Date().toISOString(),
        });
      })
      .addCase(sendMessage.rejected, (state, action) => {
        state.isLoading = false;
        const errorMsg = action.payload;
        
        // Check if it's a cold start timeout
        if (errorMsg?.includes('waking up')) {
          state.isWakingUp = true;
          state.messages.push({
            type: 'system',
            content: '⏳ The AI assistant is waking up (Render free tier). This takes ~30 seconds. Please try again in a moment...',
            timestamp: new Date().toISOString(),
          });
        } else {
          state.error = errorMsg;
          state.messages.push({
            type: 'error',
            content: 'Sorry, I encountered an error. Please try again.',
            timestamp: new Date().toISOString(),
          });
        }
      });
  },
});

export const { openChat, closeChat, setUserRole, clearChat, setWakingUp } = chatSlice.actions;
export default chatSlice.reducer;
```

---

### **5. Add to App.jsx**

```jsx
import { useEffect } from 'react';
import ChatWidget from './components/chat/ChatWidget';
import { useSelector, useDispatch } from 'react-redux';
import { setUserRole, setWakingUp } from './store/chatSlice';
import { wakeUpBackend } from './api/chatService';

function App() {
  const dispatch = useDispatch();
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  // Set user role for chat when user logs in
  useEffect(() => {
    if (isAuthenticated && user?.role) {
      dispatch(setUserRole(user.role));
    }
  }, [isAuthenticated, user, dispatch]);

  // FIXED: Wake up backend on app load with proper state updates
  useEffect(() => {
    const wakeUp = async () => {
      dispatch(setWakingUp(true));  // Show banner immediately
      try {
        await wakeUpBackend();
      } catch (error) {
        // Ignore errors, this is best-effort
      } finally {
        dispatch(setWakingUp(false));  // Hide banner after ping
      }
    };
    wakeUp();
  }, [dispatch]);

  return (
    <div className="App">
      {/* Your existing routes and components */}
      
      {/* Chat Widget - only show when authenticated */}
      {isAuthenticated && <ChatWidget />}
    </div>
  );
}

export default App;
```

---

### **8. Add chatSlice to Redux Store**

```javascript
// In src/store/index.js or store.js
import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import chatReducer from './chatSlice';  // ← ADD THIS

export const store = configureStore({
  reducer: {
    auth: authReducer,
    chat: chatReducer,  // ← ADD THIS
    // ...other reducers
  },
});
```

---

## 🎨 **Tailwind Custom Animations**

Add to `tailwind.config.js`:

```javascript
module.exports = {
  theme: {
    extend: {
      keyframes: {
        'slide-in': {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' }
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' }
        }
      },
      animation: {
        'slide-in': 'slide-in 0.3s ease-out',
        'fade-in': 'fade-in 0.2s ease-in'
      }
    }
  }
}
```

---

## 📝 **Environment Variables**

Update `.env.development` and `.env.production`:

```bash
# Backend API (Spring Boot)
VITE_API_BASE_URL=http://localhost:8080

# For production (Azure Static Web Apps)
# Set this in Azure configuration, NOT in .env file!
# VITE_API_BASE_URL=https://your-spring-boot.onrender.com

# IMPORTANT: Do NOT add VITE_RAG_BASE_URL
# Frontend should never call Python RAG service directly (security risk)
```

**⚠️ CRITICAL SECURITY NOTE:**
- Frontend only calls Spring Boot (`/api/chat/query`)
- Spring Boot extracts role from JWT (server-verified)
- Spring Boot forwards to Python with verified role
- **NEVER** let frontend call Python directly with role in body (spoofing risk!)

**⚠️ AZURE DEPLOYMENT NOTE:**
- `.env.production` file is **NOT** used at runtime
- Set `VITE_API_BASE_URL` in Azure Static Web Apps configuration
- Or add to GitHub Actions workflow as repository secret
- Vite bakes env vars into bundle at **build time**

---

## 📋 **Implementation Checklist**

### **Phase 1: Setup (5 minutes)**
- [ ] Create `src/components/chat/` folder
- [ ] Create `src/api/chatService.js`
- [ ] Create `src/store/chatSlice.js`
- [ ] Update environment variables

### **Phase 2: Components (30 minutes)**
- [ ] Create `ChatWidget.jsx`
- [ ] Create `ChatPanel.jsx`
- [ ] Create `ChatMessage.jsx`
- [ ] Create `ChatSuggestions.jsx`
- [ ] Create `ChatLoading.jsx`

### **Phase 3: Integration (15 minutes)**
- [ ] Add chatSlice to Redux store
- [ ] Add ChatWidget to App.jsx
- [ ] Update Tailwind config with animations
- [ ] Set user role in chatSlice on login

### **Phase 4: Testing (15 minutes)**
- [ ] Test chat widget appears
- [ ] Test sending messages
- [ ] Test role-based filtering
- [ ] Test error handling
- [ ] Test on mobile

### **Phase 5: Polish (10 minutes)**
- [ ] Add loading animations
- [ ] Add sound effects (optional)
- [ ] Add notification badge
- [ ] Add keyboard shortcuts
- [ ] Test accessibility

---

## 🚀 **Deployment Notes**

1. **Build frontend:**
   ```bash
   npm run build
   ```

2. **Set environment variable** in Azure Static Web Apps configuration:
   - `VITE_API_BASE_URL` = Your Spring Boot URL (e.g., `https://your-backend.onrender.com`)
   - ⚠️ **DO NOT** add any Python RAG service URL - frontend should never know it exists

3. **Configure in GitHub Actions:**
   Add to `.github/workflows/azure-static-web-apps.yml`:
   ```yaml
   env:
     VITE_API_BASE_URL: ${{ secrets.VITE_API_BASE_URL }}
   
   steps:
     - name: Build
       run: npm run build
   ```

4. **Deploy to Azure Static Web Apps** (already configured in your repo)

---

## 📊 **Expected User Experience**

### **Buyer Flow:**
1. Logs in → Chat widget appears in bottom-right
2. Clicks widget → Chat panel slides in
3. Sees suggestions: "What products are available?"
4. Asks: "Show me my recent orders"
5. AI responds with their order history from RAG
6. Clicks on order number → Navigates to order details

### **Manager Flow:**
1. Logs in → Chat widget appears (purple color)
2. Asks: "Show me pending buyer applications"
3. AI lists applications from knowledge base
4. Manager can approve directly or via chat guidance

---

## ✅ **Success Criteria**

- [ ] Chat widget visible on all authenticated pages
- [ ] Role-based color themes working
- [ ] Messages sent and received successfully
- [ ] Loading states display correctly
- [ ] Error handling works gracefully
- [ ] Chat history preserved within session
- [ ] Responsive on mobile devices
- [ ] Smooth animations
- [ ] Accessible (keyboard navigation, screen readers)

---

## 📞 **Support & Next Steps**

Ready to implement? Follow these steps:

1. **Read this entire document**
2. **Start with Phase 1** (Setup)
3. **Implement components** one by one
4. **Test after each component**
5. **Commit changes** as you go

**Total Implementation Time: ~2 hours**

---

**Let's build an amazing AI-powered chat experience for your users! 🚀**
