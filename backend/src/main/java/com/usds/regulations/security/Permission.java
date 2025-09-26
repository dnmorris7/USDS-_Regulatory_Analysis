package com.usds.regulations.security;

public enum Permission {
    // Data Operations
    GENERATE_DATA("generate:data", "Generate test data and mock relationships"),
    DOWNLOAD_FILES("download:files", "Download generated files and reports"),
    
    // View Permissions
    VIEW_ALL("view:all", "View all CFR titles and detailed analytics"),
    VIEW_PUBLIC("view:public", "View public information only"),
    
    // Administrative
    MANAGE_USERS("admin:users", "Manage user accounts and permissions"),
    
    // Reporting (for future implementation)
    EXPORT_REPORTS("export:reports", "Export analytical reports and data"),
    EXPORT_CSV("export:csv", "Export data to CSV format");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}