package com.usds.regulations.security;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    public boolean hasPermission(Role userRole, Permission permission) {
        return userRole.hasPermission(permission);
    }

    public Set<Permission> getUserPermissions(Role userRole) {
        return userRole.getPermissions();
    }

    public Map<String, Object> getPermissionMap(Role userRole) {
        return Map.of(
            "role", userRole.name(),
            "permissions", userRole.getPermissions().stream()
                .collect(Collectors.toMap(
                    Permission::getCode,
                    Permission::getDescription
                )),
            "capabilities", getCapabilityMap(userRole)
        );
    }

    private Map<String, Boolean> getCapabilityMap(Role userRole) {
        return Map.of(
            "canGenerate", userRole.hasPermission(Permission.GENERATE_DATA),
            "canDownload", userRole.hasPermission(Permission.DOWNLOAD_FILES),
            "canViewAll", userRole.hasPermission(Permission.VIEW_ALL),
            "canExportReports", userRole.hasPermission(Permission.EXPORT_REPORTS),
            "canExportCSV", userRole.hasPermission(Permission.EXPORT_CSV),
            "canManageUsers", userRole.hasPermission(Permission.MANAGE_USERS)
        );
    }
}