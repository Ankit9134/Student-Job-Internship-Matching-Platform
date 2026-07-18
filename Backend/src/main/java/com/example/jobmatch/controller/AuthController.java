package com.example.jobmatch.controller;

import com.example.jobmatch.config.JwtUtil;
import com.example.jobmatch.entity.User;
import com.example.jobmatch.entity.User.Role;
import com.example.jobmatch.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .fullName(req.getFullName())
            .role(req.getRole())
            .createdAt(Instant.now())
            .build();
        userRepo.save(user);
        String token = jwtUtil.generate(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getFullName(), user.getId(), user.getStudentId());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtUtil.generate(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getFullName(), user.getId(), user.getStudentId());
    }

    // ---- DTOs ----

    @Getter @Setter
    public static class SignupRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String fullName;
        @NotNull
        private Role role;
    }

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Getter @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String role;
        private String fullName;
        private Long userId;
        private Long studentId;
    }
}
