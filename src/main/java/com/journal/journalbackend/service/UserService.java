package com.journal.journalbackend.service;

import com.journal.journalbackend.config.EmailConfig.EmailConfig;
import com.journal.journalbackend.dto.request.UserRegistrationRequest;
import com.journal.journalbackend.model.User;
import com.journal.journalbackend.model.VerificationToken;
import com.journal.journalbackend.repository.UserRepository;
import com.journal.journalbackend.repository.VerificationTokenRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfig emailConfig;

    public UserService(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailConfig emailConfig
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailConfig = emailConfig;
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
                passwordEncoder.encode(registrationDTO.getPassword())
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
}
