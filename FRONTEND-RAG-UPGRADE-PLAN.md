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
│       ├── ChatMessage.jsx      ← Message bubbles
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
  const { messages, isLoading, userRole } = useSelector((state) => state.chat);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = () => {
    if (!input.trim() || isLoading) return;
    
    dispatch(sendMessage({ query: input, role: userRole }));
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

      {/* Input */}
      <div className="p-4 border-t bg-white rounded-b-lg">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Ask me anything..."
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 
                       focus:ring-blue-500"
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

### **3. chatService.js (API Integration)**

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const RAG_BASE_URL = import.meta.env.VITE_RAG_BASE_URL || 'http://localhost:8000';

// Get token from localStorage
const getToken = () => localStorage.getItem('token');

// Send query to RAG backend
export const sendChatQuery = async (query, role) => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/chat/query`,  // Spring Boot endpoint
      { query },
      {
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('Chat query failed:', error);
    throw error;
  }
};

// Alternative: Call Python RAG service directly (if needed)
export const sendDirectRAGQuery = async (query, role, buyerId, buyerStatus) => {
  try {
    const response = await axios.post(
      `${RAG_BASE_URL}/api/rag/query`,
      { 
        query, 
        role,
        buyer_id: buyerId,
        buyer_status: buyerStatus
      },
      {
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('Direct RAG query failed:', error);
    throw error;
  }
};
```

---

### **4. chatSlice.js (Redux State)**

```javascript
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { sendChatQuery } from '../api/chatService';

// Async thunk for sending messages
export const sendMessage = createAsyncThunk(
  'chat/sendMessage',
  async ({ query, role }, { rejectWithValue }) => {
    try {
      const response = await sendChatQuery(query, role);
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
        state.error = action.payload;
        // Add error message
        state.messages.push({
          type: 'error',
          content: 'Sorry, I encountered an error. Please try again.',
          timestamp: new Date().toISOString(),
        });
      });
  },
});

export const { openChat, closeChat, setUserRole, clearChat } = chatSlice.actions;
export default chatSlice.reducer;
```

---

### **5. Add to App.jsx**

```jsx
import ChatWidget from './components/chat/ChatWidget';
import { useSelector } from 'react-redux';

function App() {
  const { isAuthenticated } = useSelector((state) => state.auth);

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

### **6. Add chatSlice to Redux Store**

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
# Backend API
VITE_API_BASE_URL=http://localhost:8080

# RAG Service (optional - if calling directly)
VITE_RAG_BASE_URL=http://localhost:8000

# For production
# VITE_API_BASE_URL=https://your-spring-boot.onrender.com
# VITE_RAG_BASE_URL=https://your-rag-service.onrender.com
```

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

2. **Update environment variables** in Azure Static Web Apps:
   - `VITE_API_BASE_URL` = Your Spring Boot URL
   - `VITE_RAG_BASE_URL` = Your Python RAG service URL (if needed)

3. **Deploy to Azure Static Web Apps** (already configured)

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
