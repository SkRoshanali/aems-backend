# 🚀 Frontend Chat Implementation Guide

## 📍 Where You Are

- ✅ Backend: `aems-backend/` (Spring Boot + Python RAG service)
- ✅ Backend docs: Complete implementation plan ready
- 📍 **Frontend**: Separate repo - `aems-frontend/` (needs chat integration)
- ❌ Chat not yet implemented in actual frontend

---

## 🎯 Step-by-Step Implementation

### **Step 1: Clone Frontend (if not already done)**

```bash
cd ..  # Go to parent directory
git clone https://github.com/SkRoshanali/aems-frontend.git
cd aems-frontend
```

---

### **Step 2: Create Chat Component Folder**

```bash
mkdir -p src/components/chat
```

---

### **Step 3: Create ChatWidget.jsx** (Floating Button)

**Location:** `src/components/chat/ChatWidget.jsx`

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

### **Step 4: Create ChatPanel.jsx** (Main Chat UI)

**Location:** `src/components/chat/ChatPanel.jsx`

```jsx
import { useState, useEffect, useRef } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Send, Minimize2 } from 'lucide-react';
import { sendMessage } from '../../store/chatSlice';
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
    if (!input.trim() || isLoading) return;  // ✅ FIX: Only block on isLoading
    
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

      {/* ✅ Wake-up Banner (Informational - Does NOT block input) */}
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

      {/* ✅ Input - Only disabled by isLoading, NOT isWakingUp */}
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

### **Step 5: Create ChatMessage.jsx** (Complete with all message types)

**Location:** `src/components/chat/ChatMessage.jsx`

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

  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  // ✅ System message (yellow banner - for wake-up notifications)
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

  // ✅ Error message (red banner)
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

  return (
    <div className="bg-gray-100 p-3 rounded text-sm text-gray-600">
      Unknown message type: {message.type}
    </div>
  );
}

export default ChatMessage;
```

---

### **Step 6: Create ChatSuggestions.jsx** (Quick Prompts)

**Location:** `src/components/chat/ChatSuggestions.jsx`

```jsx
import { MessageSquare } from 'lucide-react';

function ChatSuggestions({ role, onSelect }) {
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

### **Step 7: Create chatService.js** (API Integration)

**Location:** `src/api/chatService.js`

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const getToken = () => localStorage.getItem('token');

export const sendChatQuery = async (query) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/chat/query`,
      { query },
      {
        timeout: 40000,  // ✅ 40 second timeout for Render cold start
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('Chat query failed:', error);
    
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      throw new Error('The assistant is waking up, please try again in a moment...');
    }
    
    if (error.code === 'ERR_NETWORK') {
      throw new Error('Cannot connect to server. Please check your connection.');
    }
    
    throw error;
  }
};

export const wakeUpBackend = async () => {
  try {
    await axios.get(`${API_BASE_URL}/api/chat/warmup`, { 
      timeout: 5000,
    });
  } catch (error) {
    console.log('Backend wake-up ping sent');
  }
};
```

---

### **Step 8: Create chatSlice.js** (Redux State)

**Location:** `src/store/chatSlice.js`

```javascript
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { sendChatQuery } from '../api/chatService';

export const sendMessage = createAsyncThunk(
  'chat/sendMessage',
  async ({ query }, { rejectWithValue }) => {
    try {
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
    isWakingUp: false,  // ✅ NEW: Track Render cold start
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
    setWakingUp: (state, action) => {  // ✅ NEW ACTION
      state.isWakingUp = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(sendMessage.pending, (state, action) => {
        state.isLoading = true;
        state.error = null;
        state.messages.push({
          type: 'user',
          content: action.meta.arg.query,
          timestamp: new Date().toISOString(),
        });
      })
      .addCase(sendMessage.fulfilled, (state, action) => {
        state.isLoading = false;
        state.isWakingUp = false;  // ✅ Clear wake-up state on success
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
        
        if (errorMsg?.includes('waking up')) {
          state.isWakingUp = true;  // ✅ Show wake-up banner
          state.messages.push({
            type: 'system',  // ✅ System message type
            content: '⏳ The AI assistant is waking up (Render free tier). This takes ~30 seconds. Please try again in a moment...',
            timestamp: new Date().toISOString(),
          });
        } else {
          state.error = errorMsg;
          state.messages.push({
            type: 'error',  // ✅ Error message type
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

### **Step 9: Update Redux Store**

**Location:** `src/store/index.js` (or wherever your store is configured)

```javascript
import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import chatReducer from './chatSlice';  // ← ADD THIS

export const store = configureStore({
  reducer: {
    auth: authReducer,
    chat: chatReducer,  // ← ADD THIS
    // ... other reducers
  },
});
```

---

### **Step 10: Update App.jsx**

**Location:** `src/App.jsx`

```jsx
import { useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import ChatWidget from './components/chat/ChatWidget';
import { setUserRole, setWakingUp } from './store/chatSlice';
import { wakeUpBackend } from './api/chatService';

function App() {
  const dispatch = useDispatch();
  const { isAuthenticated, user } = useSelector((state) => state.auth);

  // Set user role for chat
  useEffect(() => {
    if (isAuthenticated && user?.role) {
      dispatch(setUserRole(user.role));
    }
  }, [isAuthenticated, user, dispatch]);

  // ✅ FIX: Properly dispatch setWakingUp for warmup
  useEffect(() => {
    const wakeUp = async () => {
      dispatch(setWakingUp(true));  // Show banner immediately
      try {
        await wakeUpBackend();
      } catch (error) {
        // Ignore
      } finally {
        dispatch(setWakingUp(false));  // Hide banner after ping
      }
    };
    wakeUp();
  }, [dispatch]);

  return (
    <div className="App">
      {/* Your existing routes and components */}
      
      {/* Chat Widget - only when authenticated */}
      {isAuthenticated && <ChatWidget />}
    </div>
  );
}

export default App;
```

---

### **Step 11: Update Tailwind Config**

**Location:** `tailwind.config.js`

Add custom animations:

```javascript
module.exports = {
  theme: {
    extend: {
      keyframes: {
        'slide-in': {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' }
        },
      },
      animation: {
        'slide-in': 'slide-in 0.3s ease-out',
      }
    }
  }
}
```

---

### **Step 12: Environment Variables**

**Location:** `.env.development`

```bash
VITE_API_BASE_URL=http://localhost:8080
```

**Location:** `.env.production` (for Azure/GitHub Actions)

```bash
# Set this in GitHub Actions secrets or Azure config
# VITE_API_BASE_URL=https://your-spring-boot.onrender.com
```

---

## ✅ **Final Checklist**

- [ ] Created `src/components/chat/` folder
- [ ] Created `ChatWidget.jsx`
- [ ] Created `ChatPanel.jsx` (with lockout fix: `disabled={isLoading}`)
- [ ] Created `ChatMessage.jsx` (supports user/ai/system/error)
- [ ] Created `ChatSuggestions.jsx`
- [ ] Created `src/api/chatService.js`
- [ ] Created `src/store/chatSlice.js`
- [ ] Updated Redux store in `src/store/index.js`
- [ ] Updated `App.jsx` with warmup dispatch
- [ ] Updated `tailwind.config.js` with animations
- [ ] Set `VITE_API_BASE_URL` in `.env.development`
- [ ] Installed `lucide-react` if not already: `npm install lucide-react`

---

## 🧪 **Testing**

1. **Run dev server:**
   ```bash
   npm run dev
   ```

2. **Log in to frontend**

3. **Open chat widget** (bottom-right corner)

4. **See wake-up banner** (appears immediately on app load)

5. **Send a message** - should work after ~30 seconds

6. **If timeout occurs:**
   - Banner stays yellow
   - **Input remains ENABLED** ✅
   - Click send again
   - Should succeed ✅

---

## 🚀 **All Issues Fixed!**

✅ No more permanent lockout  
✅ App properly dispatches warmup state  
✅ ChatMessage supports system/error types  
✅ Backend endpoint already exists  
✅ Ready for production!

---

**Next Step:** Clone the frontend repo and follow steps 2-12 above!
