import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, ROLES } from '../services/auth.service';

@Component({
  selector: 'app-role-switcher',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="role-switcher" [class.minimized]="isMinimized">
      <!-- Minimized View -->
      <div *ngIf="isMinimized" class="minimized-view" (click)="toggleMinimize()">
        <span class="role-badge-mini" [ngStyle]="{'background-color': currentRoleConfig.color}">
          {{currentRoleConfig.name.charAt(0)}}
        </span>
        <span class="expand-icon">⚡</span>
      </div>

      <!-- Full View -->
      <div *ngIf="!isMinimized" class="full-view">
        <div class="role-header">
          <div class="role-indicator">
            <span class="role-label">Dev Mode:</span>
            <span 
              class="role-badge"
              [ngStyle]="{'background-color': currentRoleConfig.color}"
            >
              {{currentRoleConfig.displayName}}
            </span>
          </div>
          <button class="minimize-btn" (click)="toggleMinimize()" title="Minimize role switcher">
            <span class="minimize-icon">−</span>
          </button>
        </div>
        
        <div class="role-buttons">
          <button 
            *ngFor="let role of availableRoles"
            class="role-btn"
            [class.active]="currentRole === role"
            [ngStyle]="{'--role-color': getRoleConfig(role).color}"
            [title]="getRoleConfig(role).description"
            (click)="handleRoleChange(role)"
          >
            {{getRoleConfig(role).displayName}}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .role-switcher {
      position: fixed;
      top: 20px;
      right: 20px;
      background: rgba(0, 0, 0, 0.9);
      border-radius: 8px;
      border: 2px solid #ff6b35;
      z-index: 1000;
      transition: all 0.3s ease;
      backdrop-filter: blur(10px);
    }

    .role-switcher.minimized {
      padding: 8px;
      cursor: pointer;
    }

    .role-switcher:not(.minimized) {
      padding: 15px;
    }

    /* Minimized View */
    .minimized-view {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      transition: transform 0.2s;
    }

    .minimized-view:hover {
      transform: scale(1.05);
    }

    .role-badge-mini {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: bold;
      color: white;
      text-shadow: 0 0 2px rgba(0,0,0,0.5);
    }

    .expand-icon {
      font-size: 14px;
      color: #ff6b35;
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }

    /* Full View */
    .role-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
    }

    .role-indicator {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .minimize-btn {
      background: rgba(255, 107, 53, 0.2);
      border: 1px solid #ff6b35;
      border-radius: 4px;
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: #ff6b35;
      font-weight: bold;
      transition: all 0.2s;
    }

    .minimize-btn:hover {
      background: #ff6b35;
      color: white;
    }

    .minimize-icon {
      font-size: 16px;
      line-height: 1;
    }

    .role-label {
      color: #fff;
      font-size: 12px;
      font-weight: bold;
    }

    .role-badge {
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 10px;
      font-weight: bold;
      text-transform: uppercase;
      color: white;
      text-shadow: 0 0 2px rgba(0,0,0,0.5);
    }

    .role-buttons {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
    }

    .role-btn {
      padding: 6px 10px;
      border: none;
      border-radius: 4px;
      font-size: 10px;
      font-weight: bold;
      cursor: pointer;
      transition: all 0.2s;
      background: #495057;
      color: white;
      position: relative;
      overflow: hidden;
    }

    .role-btn::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
      transition: left 0.5s;
    }

    .role-btn:hover::before {
      left: 100%;
    }

    .role-btn:hover {
      background: #ff6b35;
      transform: translateY(-1px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .role-btn.active {
      background: #ff6b35;
      box-shadow: 0 0 8px rgba(255, 107, 53, 0.6), inset 0 0 8px rgba(255,255,255,0.1);
      transform: translateY(-1px);
    }

    .role-btn.active::after {
      content: '✓';
      position: absolute;
      top: 2px;
      right: 2px;
      font-size: 8px;
      color: rgba(255,255,255,0.8);
    }
  `]
})
export class RoleSwitcherComponent {
  currentRole: string = 'VISITOR';
  availableRoles: string[] = [];
  currentRoleConfig: any = ROLES['VISITOR'];
  isMinimized: boolean = false;

  constructor(private authService: AuthService) {
    // Initialize values in constructor after service is available
    this.currentRole = this.authService.getCurrentRole();
    this.availableRoles = this.authService.getAvailableRoles();
    this.currentRoleConfig = this.authService.getRoleConfig(this.currentRole);
    
    // Load minimized state from localStorage
    this.isMinimized = localStorage.getItem('roleSwitcherMinimized') === 'true';
    
    // Subscribe to role changes
    this.authService.onRoleChange((newRole: string) => {
      this.currentRole = newRole;
      this.currentRoleConfig = this.authService.getRoleConfig(newRole);
    });
  }

  toggleMinimize() {
    this.isMinimized = !this.isMinimized;
    localStorage.setItem('roleSwitcherMinimized', this.isMinimized.toString());
  }

  async handleRoleChange(role: string) {
    try {
      await this.authService.simulateRole(role);
    } catch (error: any) {
      console.error('Failed to change role:', error);
      alert(error?.message || 'Failed to change role');
    }
  }

  getRoleConfig(role: string) {
    return ROLES[role];
  }
}