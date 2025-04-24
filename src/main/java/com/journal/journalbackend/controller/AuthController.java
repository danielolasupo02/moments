package com.journal.journalbackend.controller;

import com.journal.journalbackend.dto.request.*;
import com.journal.journalbackend.dto.response.LoginResponse;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.repository.UserRepository;
import com.journal.journalbackend.service.UserService;
import com.journal.journalbackend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        try {
            userService.registerNewUser(userRegistrationRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully. Please check your email to verify your account.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        try {
            boolean verified = userService.verifyUser(token);
            return ResponseEntity.ok("Email verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationToken(@RequestParam("email") String email) {
        try {
            userService.resendVerificationToken(email);
            return ResponseEntity.ok("Verification token resent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Special case for admin user
            if ("admin".equals(loginRequest.getUsername())) {
                try {
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.getUsername(),
                                    loginRequest.getPassword()
                            )
                    );

                    // Generate JWT token
                    String jwtToken = jwtTokenProvider.generateToken(authentication);

                    // Admin login successful
                    LoginResponse response = new LoginResponse(
                            0L, // Admin ID
                            "admin",
                            "admin@test.com",
                            "Admin",
                            "User",
                            "Admin login successful",
                            jwtToken
                    );

                    return ResponseEntity.ok(response);
                } catch (BadCredentialsException e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Invalid admin credentials");
                }
            }

            // Regular user login logic
            Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

            if (userOptional.isEmpty()) {
                System.out.println("User not found in database: " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found: " + loginRequest.getUsername());
            }

            User user = userOptional.get();

            // Verify password against database hash
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
            System.out.println("Password match result: " + passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials");
            }

            // Check if user is verified
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Account is not verified. Please check your email for verification instructions.");
            }

            // Create authentication token
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null, // Don't include password in token
                    authorities
            );

            // Generate JWT token
            String jwtToken = jwtTokenProvider.generateToken(authentication);

            // Return user information with JWT token
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    "Login Successful",
                    jwtToken
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Login exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            // Debug authentication object
            if (authentication == null) {
                System.out.println("Authentication is null!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            System.out.println("Authentication class: " + authentication.getClass().getName());
            System.out.println("Authentication principal: " + authentication.getPrincipal());
            String username = authentication.getName();
            System.out.println("Username from authentication: " + username);

            userService.changePassword(username, changePasswordRequest);
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            System.out.println("Error in changePassword: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> initiatePasswordReset(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        try {
            userService.initiatePasswordReset(forgotPasswordRequest);
            return ResponseEntity.ok("If this email is registered, a password reset link has been sent");
        } catch (Exception e) {
            // Log the error but return a generic message
            System.err.println("Error in forgot password: " + e.getMessage());
            return ResponseEntity.ok("If this email is registered, a password reset link has been sent");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            userService.resetPassword(resetPasswordRequest);
            return ResponseEntity.ok("Password has been reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}