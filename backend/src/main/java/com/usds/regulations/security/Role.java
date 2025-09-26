package com.usds.regulations.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN(Permission.GENERATE_DATA, Permission.DOWNLOAD_FILES, Permission.VIEW_ALL, Permission.MANAGE_USERS),
    ANALYST(Permission.GENERATE_DATA, Permission.VIEW_ALL),
    AUDITOR(Permission.VIEW_ALL),
    VISITOR(Permission.VIEW_PUBLIC);

    private final Set<Permission> permissions;

    Role(Permission... permissions) {
        this.permissions = Set.of(permissions);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles: " + 
                Arrays.stream(Role.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }

    public static boolean isValidRole(String role) {
        try {
            fromString(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}