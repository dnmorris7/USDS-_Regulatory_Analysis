import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { UserSimulationService, SimulatedUser } from '../services/user-simulation.service';
import { RegulationService } from '../services/regulation';
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
  imports: [CommonModule, FormsModule, MarkdownModule],
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

  // AI state
  availableModels: string[] = []; // Array of model enum names (e.g., "GEMMA3_27B")
  modelDetails: Map<string, any> = new Map(); // Map of enum name -> full model details
  selectedModel: string = ''; // Will be set from backend's defaultModel
  ollamaStatus: 'checking' | 'available' | 'unavailable' = 'checking';

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
    public userSimulationService: UserSimulationService,
    private regulationService: RegulationService
  ) {
    // Markdown parsing handled by ngx-markdown
  }

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

    // Load available models and check backend status
    this.loadAvailableModels();
    this.checkBackendStatus();
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
    const messageContent = this.userMessage.trim();
    this.userMessage = '';
    this.isLoading = true;
    this.shouldScrollToBottom = true;

    // Update conversation title (first message)
    if (this.activeConversation.messages.length === 1) {
      this.activeConversation.title = this.truncateText(userMsg.content, 50);
    }

    this.activeConversation.updatedAt = new Date();
    this.saveConversations();

    // Create assistant message placeholder for streaming
    const assistantMsg: ChatMessage = {
      id: this.generateId(),
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      model: this.selectedModel,
      isStreaming: true
    };

    this.activeConversation.messages.push(assistantMsg);

    try {
      // Use real backend streaming
      this.regulationService.sendMessageStreaming(
        messageContent,
        this.selectedModel,
        this.activeConversation.id
      ).subscribe({
        next: (chunk: string) => {
          // Append streaming chunk
          assistantMsg.content += chunk;
          this.shouldScrollToBottom = true;
        },
        error: (error: any) => {
          console.error('[AIChat] Streaming error:', error);
          assistantMsg.isStreaming = false;
          assistantMsg.content = 'Sorry, there was an error processing your message. Please try again.';
          assistantMsg.role = 'system';
          this.isLoading = false;
          this.saveConversations();
        },
        complete: () => {
          // Streaming complete
          assistantMsg.isStreaming = false;
          this.isLoading = false;
          this.activeConversation!.updatedAt = new Date();
          this.saveConversations();
          this.shouldScrollToBottom = true;
        }
      });
      
    } catch (error) {
      console.error('[AIChat] Error sending message:', error);
      
      // Update assistant message with error
      assistantMsg.isStreaming = false;
      assistantMsg.content = 'Sorry, there was an error processing your message. Please try again.';
      assistantMsg.role = 'system';
      this.isLoading = false;
      this.saveConversations();
    }
  }

  /**
   * Load available AI models from backend
   */
  private loadAvailableModels(): void {
    this.regulationService.getAvailableModels().subscribe({
      next: (response: any) => {
        if (response && response.models) {
          // Backend returns: { models: [{id: "GEMMA3_27B", displayName: "...", ...}], defaultModel: "GEMMA3_27B", ... }
          
          // Store full model details
          this.modelDetails.clear();
          response.models.forEach((m: any) => {
            this.modelDetails.set(m.id, m);
          });
          
          // Extract enum names for dropdown
          this.availableModels = response.models.map((m: any) => m.id);
          console.log('[AIChat] Loaded models:', this.availableModels);
          
          // Use backend's default model
          if (response.defaultModel) {
            this.selectedModel = response.defaultModel;
          } else if (this.availableModels.length > 0) {
            this.selectedModel = this.availableModels[0];
          }
        }
      },
      error: (error: any) => {
        console.error('[AIChat] Error loading models:', error);
        this.availableModels = [];
        this.modelDetails.clear();
      }
    });
  }

  /**
   * Get display name for a model
   */
  getModelDisplayName(modelId: string): string {
    const details = this.modelDetails.get(modelId);
    return details?.displayName || modelId;
  }

  /**
   * Check backend/Ollama status
   */
  private checkBackendStatus(): void {
    this.ollamaStatus = 'checking';
    
    this.regulationService.checkOllamaStatus().subscribe({
      next: (response: any) => {
        // Backend returns: { available: true/false, baseUrl: "...", pulledModels: [...] }
        if (response.available === true) {
          this.ollamaStatus = 'available';
          console.log('[AIChat] Backend status: Available');
        } else {
          this.ollamaStatus = 'unavailable';
          console.warn('[AIChat] Backend status: Unavailable');
        }
      },
      error: (error: any) => {
        console.error('[AIChat] Error checking status:', error);
        this.ollamaStatus = 'unavailable';
      }
    });
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
