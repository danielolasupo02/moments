package com.journal.journalbackend.service;

import com.journal.journalbackend.config.EmailConfig;
import com.journal.journalbackend.dto.request.ChangePasswordRequest;
import com.journal.journalbackend.dto.request.ForgotPasswordRequest;
import com.journal.journalbackend.dto.request.ResetPasswordRequest;
import com.journal.journalbackend.dto.request.UserRegistrationRequest;
import com.journal.journalbackend.model.PasswordResetToken;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.model.VerificationToken;
import com.journal.journalbackend.repository.PasswordResetTokenRepository;
import com.journal.journalbackend.repository.UserRepository;
import com.journal.journalbackend.repository.VerificationTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfig emailConfig;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailConfig emailConfig,
            PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailConfig = emailConfig;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional
    public User registerNewUser(UserRegistrationRequest registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create new user
        User newUser = new User(
                null,
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getUsername(),
                registrationDTO.getEmail(),
                passwordEncoder.encode(registrationDTO.getPassword()),
                ZoneId.of(registrationDTO.getTimezone())
        );

        // Save user
        User savedUser = userRepository.save(newUser);

        // Generate and save verification token
        String tokenString = generateVerificationToken();
        VerificationToken verificationToken = new VerificationToken(
                savedUser,
                tokenString,
                LocalDateTime.now().plusMinutes(1) // Token expires in 1 minutes
        );
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailConfig.sendVerificationEmail(savedUser.getEmail(), tokenString, savedUser.getFirstName());

        return savedUser;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Invalid verification token");
        }

        VerificationToken verificationToken = optionalToken.get();

        // Check if token is expired
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        // Mark user as verified
        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public VerificationToken resendVerificationToken(String email) {
        User savedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (savedUser.isVerified()) {
            throw new RuntimeException("User account has already been verified!");
        }

        VerificationToken token = savedUser.getVerificationToken();
        if (token != null) {
            token.setToken(generateVerificationToken());
            token.setExpiryDate(LocalDateTime.now().plusMinutes(1)); // Set expiry to 2 minutes
            // Send verification email
            emailConfig.sendVerificationEmail(savedUser.getEmail(), token.getToken(), savedUser.getFirstName());

            return verificationTokenRepository.save(token);
        } else {
            VerificationToken newToken = new VerificationToken(savedUser, generateVerificationToken(), LocalDateTime.now().plusMinutes(5));
            return verificationTokenRepository.save(newToken);
        }
    }


    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        int code = 100_000 + random.nextInt(900_000); // Generates a number between 100000 and 999999
        return String.valueOf(code);
    }

    @Transactional
    public boolean changePassword(String username, ChangePasswordRequest request) {
        System.out.println("Attempting to change password for user: " + username);

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }


        // Find user
        Optional<User> userOptional = userRepository.findByUsername(username);


        if (userOptional.isEmpty()) {
            System.out.println("User not found with username: " + username);
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();
        System.out.println("User found: " + user.getId() + " - " + user.getUsername());

        // Validate current password
        boolean passwordMatches = passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash());
        System.out.println("Password match result: " + passwordMatches);

        if (!passwordMatches) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // Always return the same message regardless of whether the email exists
        if (userOptional.isEmpty()) {
            // User not found, but we don't want to reveal this for security reasons
            return;
        }

        User user = userOptional.get();

        // Generate and hash the token
        String token = generateResetToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15); // Token expires in 15 minutes

        // Check if user already has a token and update it, otherwise create new
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);

        if (existingToken.isPresent()) {
            // Update existing token
            PasswordResetToken resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setUsed(false);
            resetToken.setExpiryDate(expiryDate);
            passwordResetTokenRepository.save(resetToken);
        } else {
            // Create new token
            PasswordResetToken resetToken = new PasswordResetToken(user, token, expiryDate);
            passwordResetTokenRepository.save(resetToken);
        }

        // Send reset email
        emailConfig.sendPasswordResetEmail(user.getEmail(), token, user.getFirstName());
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Find the token
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("Invalid password reset token");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // Check if token is expired or used
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }

        // Get the user
        User user = resetToken.getUser();

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all other tokens for this user
        passwordResetTokenRepository.invalidateAllTokensForUser(user);

        return true;
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    public List<User> getAllVerifiedUsers() {
        return userRepository.findByIsVerifiedTrue();
    }





}
