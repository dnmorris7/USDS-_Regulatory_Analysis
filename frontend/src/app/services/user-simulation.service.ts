import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * User Simulation Service
 * 
 * Simulates authenticated users for testing AI chat functionality
 * WITHOUT implementing full authentication system.
 * 
 * Purpose: Allow testing of AI features with different user roles
 * before production authentication is implemented.
 * 
 * Security Note: This is for DEVELOPMENT/TESTING ONLY.
 * Replace with real authentication before production deployment.
 * 
 * @author USDS Development Team
 */

export interface SimulatedUser {
  id: string;
  username: string;
  displayName: string;
  email: string;
  role: UserRole;
  department: string;
  avatarColor: string;
  avatarInitials: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  ANALYST = 'ANALYST',
  AUDITOR = 'AUDITOR',
  VISITOR = 'VISITOR'
}

@Injectable({
  providedIn: 'root'
})
export class UserSimulationService {
  
  // Available simulated users
  private readonly SIMULATED_USERS: SimulatedUser[] = [
    {
      id: 'admin-001',
      username: 'admin',
      displayName: 'Sarah Chen',
      email: 'sarah.chen@usds.gov',
      role: UserRole.ADMIN,
      department: 'USDS Leadership',
      avatarColor: '#1e40af', // Blue
      avatarInitials: 'SC'
    },
    {
      id: 'analyst-001',
      username: 'analyst1',
      displayName: 'Marcus Rodriguez',
      email: 'marcus.rodriguez@usds.gov',
      role: UserRole.ANALYST,
      department: 'Regulatory Analysis',
      avatarColor: '#059669', // Green
      avatarInitials: 'MR'
    },
    {
      id: 'analyst-002',
      username: 'analyst2',
      displayName: 'Emily Washington',
      email: 'emily.washington@usds.gov',
      role: UserRole.ANALYST,
      department: 'Regulatory Analysis',
      avatarColor: '#7c3aed', // Purple
      avatarInitials: 'EW'
    },
    {
      id: 'auditor-001',
      username: 'auditor',
      displayName: 'James Thompson',
      email: 'james.thompson@usds.gov',
      role: UserRole.AUDITOR,
      department: 'Compliance & Audit',
      avatarColor: '#dc2626', // Red
      avatarInitials: 'JT'
    },
    {
      id: 'visitor-001',
      username: 'visitor',
      displayName: 'Guest User',
      email: 'guest@example.com',
      role: UserRole.VISITOR,
      department: 'Public Access',
      avatarColor: '#64748b', // Gray
      avatarInitials: 'GU'
    }
  ];

  // Current user state
  private currentUserSubject: BehaviorSubject<SimulatedUser | null>;
  public currentUser$: Observable<SimulatedUser | null>;

  // Local storage key
  private readonly STORAGE_KEY = 'usds_simulated_user';

  constructor() {
    // Load user from localStorage or default to first analyst
    const storedUser = this.loadUserFromStorage();
    const initialUser = storedUser || this.SIMULATED_USERS[1]; // Default to Marcus Rodriguez
    
    this.currentUserSubject = new BehaviorSubject<SimulatedUser | null>(initialUser);
    this.currentUser$ = this.currentUserSubject.asObservable();
    
    // Save initial user
    if (initialUser) {
      this.saveUserToStorage(initialUser);
    }
  }

  /**
   * Get the current user (synchronous)
   */
  getCurrentUser(): SimulatedUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get all available simulated users
   */
  getAvailableUsers(): SimulatedUser[] {
    return [...this.SIMULATED_USERS];
  }

  /**
   * Switch to a different simulated user
   */
  switchUser(userId: string): void {
    const user = this.SIMULATED_USERS.find(u => u.id === userId);
    if (user) {
      this.currentUserSubject.next(user);
      this.saveUserToStorage(user);
      console.log(`[UserSimulation] Switched to user: ${user.displayName} (${user.role})`);
    } else {
      console.error(`[UserSimulation] User not found: ${userId}`);
    }
  }

  /**
   * Switch user by username
   */
  switchUserByUsername(username: string): void {
    const user = this.SIMULATED_USERS.find(u => u.username === username);
    if (user) {
      this.switchUser(user.id);
    } else {
      console.error(`[UserSimulation] Username not found: ${username}`);
    }
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: UserRole): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.hasRole(UserRole.ADMIN);
  }

  /**
   * Check if user is analyst
   */
  isAnalyst(): boolean {
    return this.hasRole(UserRole.ANALYST);
  }

  /**
   * Check if user is auditor
   */
  isAuditor(): boolean {
    return this.hasRole(UserRole.AUDITOR);
  }

  /**
   * Check if user has permission to use AI features
   */
  canUseAI(): boolean {
    const user = this.getCurrentUser();
    // Visitors cannot use AI features
    return user?.role !== UserRole.VISITOR;
  }

  /**
   * Logout (clear current user)
   */
  logout(): void {
    this.currentUserSubject.next(null);
    localStorage.removeItem(this.STORAGE_KEY);
    console.log('[UserSimulation] User logged out');
  }

  /**
   * Load user from localStorage
   */
  private loadUserFromStorage(): SimulatedUser | null {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const user = JSON.parse(stored);
        // Validate that the user still exists in our list
        const valid = this.SIMULATED_USERS.find(u => u.id === user.id);
        return valid || null;
      }
    } catch (error) {
      console.error('[UserSimulation] Error loading user from storage:', error);
    }
    return null;
  }

  /**
   * Save user to localStorage
   */
  private saveUserToStorage(user: SimulatedUser): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
    } catch (error) {
      console.error('[UserSimulation] Error saving user to storage:', error);
    }
  }

  /**
   * Get user display info for UI
   */
  getUserDisplayInfo(): string {
    const user = this.getCurrentUser();
    if (!user) return 'Not logged in';
    return `${user.displayName} (${user.role})`;
  }

  /**
   * Get avatar style for current user
   */
  getAvatarStyle(): { backgroundColor: string; color: string } {
    const user = this.getCurrentUser();
    return {
      backgroundColor: user?.avatarColor || '#64748b',
      color: '#ffffff'
    };
  }
}
