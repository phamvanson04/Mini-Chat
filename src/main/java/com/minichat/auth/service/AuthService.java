package com.minichat.auth.service;

import com.minichat.auth.dto.response.AuthResponse;
import com.minichat.auth.dto.request.LoginRequest;
import com.minichat.auth.dto.request.RegisterRequest;
import com.minichat.auth.dto.response.RegisterResponse;
import com.minichat.auth.model.AppUser;
import com.minichat.auth.model.Role;
import com.minichat.auth.repository.AppUserRepository;
import com.minichat.auth.security.JwtService;
import com.minichat.shared.error.BadRequestException;
import com.minichat.shared.error.ConflictException;
import com.minichat.shared.error.UnauthorizedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String username = request.username().trim();
        String email = request.email().trim();

        if (appUserRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        return new RegisterResponse(savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new BadRequestException("Username and password are required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username().trim(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username or password");
        }

        AppUser user = appUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole().name());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (isBlank(request.username())) {
            throw new BadRequestException("Username is required");
        }
        if (isBlank(request.email())) {
            throw new BadRequestException("Email is required");
        }
        if (isBlank(request.password()) || request.password().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

