package com.minichat.auth.controller;

import com.minichat.auth.dto.AuthResponse;
import com.minichat.auth.dto.LoginRequest;
import com.minichat.auth.dto.RegisterRequest;
import com.minichat.auth.dto.RegisterResponse;
import com.minichat.auth.service.AuthService;
import com.minichat.shared.response.BaseResponse;
import com.minichat.shared.response.ResponseFactory;
import com.minichat.shared.error.UnauthorizedException;
import com.minichat.chat.service.PresenceService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final PresenceService presenceService;

    public AuthController(AuthService authService, PresenceService presenceService) {
        this.authService = authService;
        this.presenceService = presenceService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseFactory.success("Register successfully", response);
    }

    @PostMapping("/login")
    public BaseResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        presenceService.markActivity(response.username());
        return ResponseFactory.success("Login successfully", response);
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new UnauthorizedException("Unauthorized");
        }

        presenceService.userOffline(username);
        return ResponseFactory.success("Logout successfully");
    }
}

