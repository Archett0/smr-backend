package com.team12.demoservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/onlyAdmin")
    public ResponseEntity<String> onlyAdminCanAccess() {
        return ResponseEntity.ok("Welcome, admin");
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/agentOrAdmin")
    public ResponseEntity<String> agentOrAdminCanAccess() {
        return ResponseEntity.ok("Welcome, ");
    }

    @PreAuthorize("hasAnyRole('TENANT', 'AGENT')")
    @GetMapping("/adminCanNot")
    public ResponseEntity<String> adminCanNot() {
        return ResponseEntity.ok("Welcome, ");
    }

    @PreAuthorize("hasAnyRole('TENANT', 'AGENT', 'ADMIN')")
    @GetMapping("/allCan")
    public ResponseEntity<String> allCanAccess() {
        return ResponseEntity.ok("Welcome, ");
    }
}
