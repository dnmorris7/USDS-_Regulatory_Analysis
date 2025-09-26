import { ROLES, PERMISSIONS } from '../config/roles.js';

class AuthService {
    constructor() {
        this.currentRole = localStorage.getItem('simulatedRole') || 'VISITOR';
        this.permissions = new Map();
        this.roleChangeCallbacks = [];
        this.loadPermissions();
    }

    async loadPermissions() {
        try {
            const response = await fetch('http://localhost:8081/api/auth/available-roles');
            const data = await response.json();
            
            // Cache all role permissions
            Object.entries(data.roles).forEach(([roleName, roleData]) => {
                this.permissions.set(roleName, new Set(Object.keys(roleData.permissions || {})));
            });
        } catch (error) {
            console.warn('Failed to load permissions from server, using defaults');
            this.loadDefaultPermissions();
        }
    }

    loadDefaultPermissions() {
        // Fallback permissions if server is unavailable
        this.permissions.set('ADMIN', new Set(Object.values(PERMISSIONS)));
        this.permissions.set('ANALYST', new Set([PERMISSIONS.GENERATE_DATA, PERMISSIONS.VIEW_ALL]));
        this.permissions.set('AUDITOR', new Set([PERMISSIONS.VIEW_ALL]));
        this.permissions.set('VISITOR', new Set([PERMISSIONS.VIEW_PUBLIC]));
    }

    async simulateRole(role) {
        if (!this.isValidRole(role)) {
            throw new Error(`Invalid role: ${role}. Available roles: ${this.getAvailableRoles().join(', ')}`);
        }
        
        this.currentRole = role;
        localStorage.setItem('simulatedRole', role);
        
        // Notify all subscribers of role change
        this.roleChangeCallbacks.forEach(callback => callback(role));
        
        try {
            return await fetch('http://localhost:8081/api/auth/simulate-role', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ role })
            });
        } catch (error) {
            console.warn('Server role simulation failed, continuing with client-side simulation');
            return Promise.resolve();
        }
    }

    isValidRole(role) {
        return Object.keys(ROLES).includes(role.toUpperCase());
    }

    getAvailableRoles() {
        return Object.keys(ROLES);
    }

    getCurrentRole() {
        return this.currentRole;
    }

    hasPermission(permission) {
        const rolePermissions = this.permissions.get(this.currentRole);
        return rolePermissions ? rolePermissions.has(permission) : false;
    }

    getRoleConfig(role = this.currentRole) {
        return ROLES[role] || ROLES.VISITOR;
    }

    // Capability helpers for backward compatibility and cleaner templates
    canGenerate() {
        return this.hasPermission(PERMISSIONS.GENERATE_DATA);
    }

    canDownload() {
        return this.hasPermission(PERMISSIONS.DOWNLOAD_FILES);
    }

    canViewAll() {
        return this.hasPermission(PERMISSIONS.VIEW_ALL);
    }

    canExportReports() {
        return this.hasPermission(PERMISSIONS.EXPORT_REPORTS);
    }

    canManageUsers() {
        return this.hasPermission(PERMISSIONS.MANAGE_USERS);
    }

    onRoleChange(callback) {
        this.roleChangeCallbacks.push(callback);
    }

    removeRoleChangeListener(callback) {
        const index = this.roleChangeCallbacks.indexOf(callback);
        if (index > -1) {
            this.roleChangeCallbacks.splice(index, 1);
        }
    }
}

export default new AuthService();