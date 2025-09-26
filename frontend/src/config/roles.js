export const ROLES = {
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
    EXPORT_REPORTS: 'export:reports'
};