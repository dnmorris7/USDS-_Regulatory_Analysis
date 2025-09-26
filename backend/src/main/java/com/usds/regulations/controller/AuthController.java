package com.usds.regulations.controller;

import com.usds.regulations.security.Role;
import com.usds.regulations.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/simulate-role")
    public ResponseEntity<?> simulateRole(@RequestBody Map<String, String> request) {
        String roleString = request.get("role");
        
        try {
            Role role = Role.fromString(roleString);
            Map<String, Object> response = permissionService.getPermissionMap(role);
            response.put("message", "Role simulation activated for: " + role.name());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "availableRoles", Arrays.stream(Role.values())
                    .map(Enum::name)
                    .collect(Collectors.toList())
            ));
        }
    }

    @GetMapping("/available-roles")
    public ResponseEntity<?> getAvailableRoles() {
        return ResponseEntity.ok(Map.of(
            "roles", Arrays.stream(Role.values())
                .collect(Collectors.toMap(
                    Enum::name,
                    role -> permissionService.getPermissionMap(role)
                ))
        ));
    }

    @GetMapping("/current-role")
    public ResponseEntity<?> getCurrentRole() {
        // Default simulation role
        Role defaultRole = Role.VISITOR;
        return ResponseEntity.ok(permissionService.getPermissionMap(defaultRole));
    }
}