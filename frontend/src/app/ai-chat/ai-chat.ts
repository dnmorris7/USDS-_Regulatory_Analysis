import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserSimulationService, SimulatedUser } from '../services/user-simulation.service';
import { Subscription } from 'rxjs';

/**
 * AI Chat Component
 * 
 * ChatGPT-style interface for interacting with AI models.
 * Features:
 * - Conversation history sidebar
 * - Message streaming (ready for backend implementation)
 * - User simulation (testing without auth)
 * - LocalStorage persistence
 * - Responsive design
 * 
 * @author USDS Development Team
 */

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: Date;
  model?: string;
  isStreaming?: boolean;
}

export interface ChatConversation {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: Date;
  updatedAt: Date;
  userId: string;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat.html',
  styleUrls: ['./ai-chat.css']
})
export class AIChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  
  @ViewChild('messageContainer') private messageContainer?: ElementRef;
  @ViewChild('messageInput') private messageInput?: ElementRef;

  // User state
  currentUser: SimulatedUser | null = null;
  availableUsers: SimulatedUser[] = [];
  showUserSwitcher = false;
  private userSubscription?: Subscription;

  // Conversation state
  conversations: ChatConversation[] = [];
  activeConversation: ChatConversation | null = null;
  userMessage = '';
  isLoading = false;
  isSidebarOpen = true;

  // UI state
  private shouldScrollToBottom = false;
  private readonly STORAGE_KEY_PREFIX = 'usds_ai_conversations_';

  constructor(
    public userSimulationService: UserSimulationService
  ) {}

  ngOnInit(): void {
    // Subscribe to user changes
    this.userSubscription = this.userSimulationService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadConversations();
      } else {
        this.conversations = [];
        this.activeConversation = null;
      }
    });

    // Load available users for switcher
    this.availableUsers = this.userSimulationService.getAvailableUsers();

    // Check if user can use AI
    if (!this.userSimulationService.canUseAI()) {
      console.warn('[AIChat] Current user does not have AI access');
    }
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  /**
   * Create a new conversation
   */
  newConversation(): void {
    if (!this.currentUser) return;

    const conversation: ChatConversation = {
      id: this.generateId(),
      title: 'New Conversation',
      messages: [],
      createdAt: new Date(),
      updatedAt: new Date(),
      userId: this.currentUser.id
    };

    this.conversations.unshift(conversation);
    this.activeConversation = conversation;
    this.saveConversations();
    
    // Focus input
    setTimeout(() => this.messageInput?.nativeElement.focus(), 100);
  }

  /**
   * Select a conversation
   */
  selectConversation(conversation: ChatConversation): void {
    this.activeConversation = conversation;
    this.shouldScrollToBottom = true;
  }

  /**
   * Delete a conversation
   */
  deleteConversation(conversation: ChatConversation, event: Event): void {
    event.stopPropagation();
    
    if (confirm(`Delete conversation "${conversation.title}"?`)) {
      this.conversations = this.conversations.filter(c => c.id !== conversation.id);
      
      if (this.activeConversation?.id === conversation.id) {
        this.activeConversation = this.conversations[0] || null;
      }
      
      this.saveConversations();
    }
  }

  /**
   * Send a message
   */
  async sendMessage(): Promise<void> {
    if (!this.userMessage.trim() || this.isLoading || !this.currentUser) return;
    
    // Check AI permission
    if (!this.userSimulationService.canUseAI()) {
      alert('You do not have permission to use AI features. Please contact an administrator.');
      return;
    }

    // Create conversation if needed
    if (!this.activeConversation) {
      this.newConversation();
    }

    if (!this.activeConversation) return;

    // Add user message
    const userMsg: ChatMessage = {
      id: this.generateId(),
      role: 'user',
      content: this.userMessage.trim(),
      timestamp: new Date()
    };

    this.activeConversation.messages.push(userMsg);
    this.userMessage = '';
    this.isLoading = true;
    this.shouldScrollToBottom = true;

    // Update conversation title (first message)
    if (this.activeConversation.messages.length === 1) {
      this.activeConversation.title = this.truncateText(userMsg.content, 50);
    }

    this.activeConversation.updatedAt = new Date();
    this.saveConversations();

    try {
      // TODO: Replace with actual backend API call
      // For now, simulate AI response
      const assistantMsg = await this.simulateAIResponse(userMsg.content);
      
      this.activeConversation.messages.push(assistantMsg);
      this.activeConversation.updatedAt = new Date();
      this.saveConversations();
      this.shouldScrollToBottom = true;
      
    } catch (error) {
      console.error('[AIChat] Error sending message:', error);
      
      // Add error message
      const errorMsg: ChatMessage = {
        id: this.generateId(),
        role: 'system',
        content: 'Sorry, there was an error processing your message. Please try again.',
        timestamp: new Date()
      };
      
      this.activeConversation.messages.push(errorMsg);
      this.saveConversations();
      
    } finally {
      this.isLoading = false;
    }
  }

  /**
   * Simulate AI response (temporary - replace with backend call)
   */
  private async simulateAIResponse(userMessage: string): Promise<ChatMessage> {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1000));

    const responses = [
      "I'm a simulated AI assistant. The backend integration is pending. Your message was: " + userMessage,
      "This is a placeholder response. Once the backend is connected, I'll provide intelligent answers about federal regulations.",
      "Hello! I'm ready to help with regulatory analysis once the AI backend service is implemented.",
      "Your question has been received. The AI model integration (Ollama/GPT) will provide actual answers soon."
    ];

    return {
      id: this.generateId(),
      role: 'assistant',
      content: responses[Math.floor(Math.random() * responses.length)],
      timestamp: new Date(),
      model: 'GEMMA3_27B (simulated)'
    };
  }

  /**
   * Handle Enter key in textarea
   */
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /**
   * Toggle sidebar
   */
  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  /**
   * Switch simulated user
   */
  switchUser(user: SimulatedUser): void {
    this.userSimulationService.switchUser(user.id);
    this.showUserSwitcher = false;
  }

  /**
   * Toggle user switcher dropdown
   */
  toggleUserSwitcher(): void {
    this.showUserSwitcher = !this.showUserSwitcher;
  }

  /**
   * Format timestamp for display
   */
  formatTime(date: Date): string {
    const d = new Date(date);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return d.toLocaleDateString();
  }

  /**
   * Get conversation preview text
   */
  getConversationPreview(conversation: ChatConversation): string {
    if (conversation.messages.length === 0) {
      return 'No messages yet';
    }
    const lastMsg = conversation.messages[conversation.messages.length - 1];
    return this.truncateText(lastMsg.content, 60);
  }

  /**
   * Truncate text with ellipsis
   */
  private truncateText(text: string, maxLength: number): string {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + '...';
  }

  /**
   * Scroll to bottom of messages
   */
  private scrollToBottom(): void {
    try {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop = 
          this.messageContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('[AIChat] Error scrolling to bottom:', err);
    }
  }

  /**
   * Load conversations from localStorage
   */
  private loadConversations(): void {
    if (!this.currentUser) return;

    try {
      const key = this.STORAGE_KEY_PREFIX + this.currentUser.id;
      const stored = localStorage.getItem(key);
      
      if (stored) {
        const parsed = JSON.parse(stored);
        this.conversations = parsed.map((c: any) => ({
          ...c,
          createdAt: new Date(c.createdAt),
          updatedAt: new Date(c.updatedAt),
          messages: c.messages.map((m: any) => ({
            ...m,
            timestamp: new Date(m.timestamp)
          }))
        }));

        // Set active conversation to most recent
        if (this.conversations.length > 0) {
          this.activeConversation = this.conversations[0];
        }
      }
    } catch (error) {
      console.error('[AIChat] Error loading conversations:', error);
    }
  }

  /**
   * Save conversations to localStorage
   */
  private saveConversations(): void {
    if (!this.currentUser) return;

    try {
      const key = this.STORAGE_KEY_PREFIX + this.currentUser.id;
      localStorage.setItem(key, JSON.stringify(this.conversations));
    } catch (error) {
      console.error('[AIChat] Error saving conversations:', error);
    }
  }

  /**
   * Generate unique ID
   */
  private generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}
