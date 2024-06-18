package be.upload_s3.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Admin", description = "Admin management APIs")
@RestController
@RequestMapping("/api/auth")
public class AdminController {
    @GetMapping(value = "/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAuthenticatedAdmin() {
        return "TEST";
    }

}
