# AI Chat Feature - Quick Start Guide

## 🚀 How to Use the AI Chat Interface

### Starting the Application

```bash
# Terminal 1 - Backend (if needed later)
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm install
ng serve
```

Open browser: `http://localhost:4200`

---

## 👥 Available Test Users

Click the user avatar (bottom left of sidebar) to switch users:

| User | Role | Department | AI Access | Avatar Color |
|------|------|------------|-----------|--------------|
| **Sarah Chen** | Admin | USDS Leadership | ✅ Yes | Blue |
| **Marcus Rodriguez** | Analyst | Regulatory Analysis | ✅ Yes | Green (Default) |
| **Emily Washington** | Analyst | Regulatory Analysis | ✅ Yes | Purple |
| **James Thompson** | Auditor | Compliance & Audit | ✅ Yes | Red |
| **Guest User** | Visitor | Public Access | ❌ No | Gray |

---

## 💬 Using the Chat Interface

### 1. Create a New Conversation
- Click the **"+"** button in sidebar header
- Or click **"Start New Chat"** if no conversations exist

### 2. Send a Message
- Type in the input box at the bottom
- Press **Enter** to send (or click the send button ➤)
- Use **Shift+Enter** for multiple lines

### 3. Example Questions (Click to Try)
The welcome screen shows 3 clickable examples:
- "What are the key regulations in Title 7?"
- "Summarize recent changes to Title 21"
- "Compare Title 9 and Title 12 regulations"

### 4. Switch Conversations
- Click any conversation in the sidebar to view it
- Your conversations are saved automatically
- Each user has separate conversation history

### 5. Delete a Conversation
- Hover over a conversation in the sidebar
- Click the **"×"** button that appears
- Confirm deletion

### 6. Toggle Sidebar (Mobile)
- Click the hamburger menu **"☰"** in the top-left
- Sidebar slides in/out

---

## 🎨 Visual Layout

```
┌─────────────────────────────────────────────────────────────┐
│  [☰] AI Regulatory Assistant                                │
│      Powered by Ollama (Gemma 3 27B)                        │
├───────────┬─────────────────────────────────────────────────┤
│           │                                                 │
│  SIDEBAR  │  WELCOME SCREEN (No conversation)              │
│           │  - Welcome message                             │
│  [+] New  │  - Example prompts (clickable)                 │
│           │  - Capabilities list                           │
│  ┌─────┐  │                                                 │
│  │Conv1│  │  --- OR ---                                     │
│  └─────┘  │                                                 │
│  ┌─────┐  │  CHAT MESSAGES (Active conversation)           │
│  │Conv2│  │                                                 │
│  └─────┘  │  [U] Marcus: What are the key...               │
│           │  [🤖] AI: I'm a simulated AI...                │
│  ┌─────┐  │  [U] Marcus: Tell me more...                   │
│  │ MR  │  │  [🤖] AI: This is a placeholder...             │
│  └─────┘  │                                                 │
│  Marcus   │                                                 │
│  Analyst  │  ┌──────────────────────────────┐              │
│           │  │ Type your message... [Send]   │              │
│           │  └──────────────────────────────┘              │
└───────────┴─────────────────────────────────────────────────┘
```

---

## 🔐 Permission Testing

### Test Visitor Restrictions:
1. Switch to **"Guest User"** (Visitor role)
2. Notice the input is **disabled**
3. See warning message: *"You do not have permission to use AI features"*
4. Switch back to any other user to re-enable

### Test User Isolation:
1. Send some messages as **Marcus Rodriguez**
2. Switch to **Emily Washington**
3. Notice conversations are **empty** (different user)
4. Create conversations as Emily
5. Switch back to Marcus
6. Your original conversations are **still there**

---

## 📱 Responsive Design

### Desktop View:
- Sidebar always visible (280px width)
- Main content uses remaining space
- Side-by-side layout

### Mobile View (<768px):
- Sidebar hidden by default
- Click **☰** button to open sidebar
- Sidebar overlays main content
- Click outside sidebar to close

---

## 🔄 Data Persistence

### What's Saved:
- ✅ Current user selection
- ✅ All conversations (per user)
- ✅ All messages in conversations
- ✅ Conversation titles
- ✅ Timestamps

### Where It's Saved:
- **Browser LocalStorage**
- Keys: `usds_simulated_user`, `usds_ai_conversations_[userId]`
- Persists across page reloads
- Cleared when you clear browser data

### To Clear Data:
```javascript
// Open browser console (F12)
localStorage.clear();
location.reload();
```

---

## ⚠️ Current Limitations

### AI Responses are Simulated:
- Responses are **hardcoded placeholders**
- Random selection from 4 sample messages
- 1-2 second delay to simulate API call
- **NOT** connected to real AI model (yet)
- Will be replaced in Phase 3

### What the AI Says (Currently):
1. "I'm a simulated AI assistant. The backend integration is pending..."
2. "This is a placeholder response. Once the backend is connected..."
3. "Hello! I'm ready to help with regulatory analysis once..."
4. "Your question has been received. The AI model integration..."

---

## 🐛 Troubleshooting

### Sidebar Won't Open:
- Check screen width (mobile view requires click)
- Try refreshing the page
- Clear localStorage and reload

### Can't Send Messages:
- Check if you're logged in as Visitor (no AI access)
- Make sure you've typed a message
- Check if loading indicator is showing (wait for it)

### Conversations Not Saving:
- Check browser localStorage is enabled
- Try a different browser
- Check browser console for errors (F12)

### User Not Persisting After Reload:
- Check localStorage permissions
- Try switching users again
- Clear cache and try again

---

## 🎯 Next Development Steps

### Phase 3 (Backend AI Services):
- [ ] Create `AIService.java` - Ollama integration
- [ ] Create `AIQueryService.java` - CFR data queries
- [ ] Create `AIConversationService.java` - DB persistence
- [ ] Create JPA entities for conversations/messages
- [ ] Implement streaming responses

### Phase 4 (API Endpoints):
- [ ] `POST /api/ai/chat` - Send message
- [ ] `GET /api/ai/conversations` - List conversations
- [ ] `GET /api/ai/conversations/{id}` - Get conversation
- [ ] `DELETE /api/ai/conversations/{id}` - Delete conversation
- [ ] `POST /api/ai/conversations` - Create conversation

### Phase 5 (Frontend Integration):
- [ ] Replace simulated responses with real API calls
- [ ] Implement streaming (Server-Sent Events)
- [ ] Add error handling for API failures
- [ ] Add retry logic
- [ ] Show model selection dropdown

### Phase 6 (Testing & Polish):
- [ ] End-to-end testing
- [ ] Load testing with Ollama
- [ ] UI polish and accessibility
- [ ] Documentation
- [ ] Security audit

---

## 📞 Need Help?

### Check These Files:
- **User Service**: `frontend/src/app/services/user-simulation.service.ts`
- **Chat Component**: `frontend/src/app/ai-chat/ai-chat.ts`
- **Chat Template**: `frontend/src/app/ai-chat/ai-chat.html`
- **Chat Styles**: `frontend/src/app/ai-chat/ai-chat.css`

### Useful Browser Console Commands:
```javascript
// Check current user
localStorage.getItem('usds_simulated_user')

// Check conversations
localStorage.getItem('usds_ai_conversations_analyst-001')

// Clear everything
localStorage.clear()
```

---

**Last Updated**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")  
**Phase**: 2 Complete ✅  
**Status**: Ready for Backend Integration 🚀
