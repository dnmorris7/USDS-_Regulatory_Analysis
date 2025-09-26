import { Injectable } from '@angular/core';

export interface RoleConfig {
  name: string;
  displayName: string;
  color: string;
  description: string;
}

export const ROLES: { [key: string]: RoleConfig } = {
  ADMIN: {
    name: 'ADMIN',
    displayName: 'Administrator',
    color: '#dc3545',
    description: 'Full system access with all privileges'
  },
  ANALYST: {
    name: 'ANALYST',
    displayName: 'Data Analyst',
    color: '#007bff',
    description: 'Can generate data and view analytics'
  },
  AUDITOR: {
    name: 'AUDITOR',
    displayName: 'Compliance Auditor',
    color: '#28a745',
    description: 'Read-only access with export capabilities'
  },
  VISITOR: {
    name: 'VISITOR',
    displayName: 'Public Visitor',
    color: '#6c757d',
    description: 'Limited read-only access'
  }
};

export const PERMISSIONS = {
    GENERATE_DATA: 'generate:data',
    DOWNLOAD_FILES: 'download:files',
    VIEW_ALL: 'view:all',
    VIEW_PUBLIC: 'view:public',
    MANAGE_USERS: 'admin:users',
    EXPORT_REPORTS: 'export:reports',
    EXPORT_CSV: 'export:csv'
};@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentRole = localStorage.getItem('simulatedRole') || 'VISITOR';
  private permissions = new Map<string, Set<string>>();
  private roleChangeCallbacks: ((role: string) => void)[] = [];

  constructor() {
    this.loadDefaultPermissions();
  }

  private loadDefaultPermissions() {
    this.permissions.set('ADMIN', new Set(Object.values(PERMISSIONS)));
    this.permissions.set('ANALYST', new Set([PERMISSIONS.GENERATE_DATA, PERMISSIONS.VIEW_ALL, PERMISSIONS.EXPORT_REPORTS]));
    this.permissions.set('AUDITOR', new Set([PERMISSIONS.VIEW_ALL, PERMISSIONS.EXPORT_REPORTS, PERMISSIONS.EXPORT_CSV]));
    this.permissions.set('VISITOR', new Set([PERMISSIONS.VIEW_PUBLIC]));
  }

  async simulateRole(role: string): Promise<void> {
    if (!this.isValidRole(role)) {
      throw new Error(`Invalid role: ${role}. Available roles: ${this.getAvailableRoles().join(', ')}`);
    }
    
    this.currentRole = role;
    localStorage.setItem('simulatedRole', role);
    
    this.roleChangeCallbacks.forEach(callback => callback(role));
    
    try {
      const response = await fetch('http://localhost:8081/api/auth/simulate-role', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ role })
      });
      
      if (!response.ok) {
        console.warn('Server role simulation failed, continuing with client-side simulation');
      }
    } catch (error) {
      console.warn('Server role simulation failed, continuing with client-side simulation');
    }
  }

  isValidRole(role: string): boolean {
    return Object.keys(ROLES).includes(role.toUpperCase());
  }

  getAvailableRoles(): string[] {
    return Object.keys(ROLES);
  }

  getCurrentRole(): string {
    return this.currentRole;
  }

  hasPermission(permission: string): boolean {
    const rolePermissions = this.permissions.get(this.currentRole);
    return rolePermissions ? rolePermissions.has(permission) : false;
  }

  getRoleConfig(role: string = this.currentRole): RoleConfig {
    return ROLES[role] || ROLES['VISITOR'];
  }

  canGenerate(): boolean {
    return this.hasPermission(PERMISSIONS.GENERATE_DATA);
  }

  canDownload(): boolean {
    return this.hasPermission(PERMISSIONS.DOWNLOAD_FILES);
  }

  canViewAll(): boolean {
    return this.hasPermission(PERMISSIONS.VIEW_ALL);
  }

  canExportReports(): boolean {
    return this.hasPermission(PERMISSIONS.EXPORT_REPORTS);
  }

  canManageUsers(): boolean {
    return this.hasPermission(PERMISSIONS.MANAGE_USERS);
  }

  canExportCSV(): boolean {
    return this.hasPermission(PERMISSIONS.EXPORT_CSV);
  }

  onRoleChange(callback: (role: string) => void): void {
    this.roleChangeCallbacks.push(callback);
  }

  removeRoleChangeListener(callback: (role: string) => void): void {
    const index = this.roleChangeCallbacks.indexOf(callback);
    if (index > -1) {
      this.roleChangeCallbacks.splice(index, 1);
    }
  }
}