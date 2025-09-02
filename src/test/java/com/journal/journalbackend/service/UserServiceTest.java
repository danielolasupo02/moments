//package com.journal.journalbackend.service;
//
//import com.journal.journalbackend.config.EmailConfig;
//import com.journal.journalbackend.dto.request.UserRegistrationRequest;
//import com.journal.journalbackend.model.User;
//import com.journal.journalbackend.model.VerificationToken;
//import com.journal.journalbackend.repository.UserRepository;
//import com.journal.journalbackend.repository.VerificationTokenRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//
//class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private VerificationTokenRepository verificationTokenRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private EmailConfig emailConfig;
//
//    @InjectMocks
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void shouldRegisterUserSuccessfully() {
//        // Arrange
//        UserRegistrationRequest request = createValidRegistrationRequest();
//
//        when(userRepository.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
//        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
//            User savedUser = invocation.getArgument(0);
//            return savedUser;
//        });
//        doNothing().when(emailConfig).sendVerificationEmail(anyString(), anyString(), anyString());
//
//        // Act
//        User registeredUser = userService.registerNewUser(request);
//
//        // Assert
//        assertNotNull(registeredUser);
//        assertEquals(request.getEmail(), registeredUser.getEmail());
//        verify(userRepository).save(any(User.class));
//        verify(emailConfig).sendVerificationEmail(anyString(), anyString(), anyString());
//    }
//
//    @Test
//    void shouldFailToRegisterUserWithDuplicateEmail() {
//        // Arrange
//        UserRegistrationRequest request = createValidRegistrationRequest();
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.registerNewUser(request);
//        });
//        assertEquals("Email already in use", exception.getMessage());
//    }
//
//    @Test
//    void shouldVerifyUserWithValidToken() {
//        // Arrange
//        String token = "123456";
//        User user = new User();
//        user.setVerified(false);
//
//        VerificationToken verificationToken = new VerificationToken(
//                user,
//                token,
//                LocalDateTime.now().plusHours(24)
//        );
//
//        when(verificationTokenRepository.findByToken(token))
//                .thenReturn(Optional.of(verificationToken));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        // Act
//        boolean result = userService.verifyUser(token);
//
//        // Assert
//        assertTrue(result);
//        assertTrue(user.isVerified());
//        verify(userRepository).save(user);
//        verify(verificationTokenRepository).delete(verificationToken);
//    }
//
//    @Test
//    void shouldFailToVerifyUserWithExpiredToken() {
//        // Arrange
//        String token = "123456";
//        User user = new User();
//
//        VerificationToken verificationToken = new VerificationToken(
//                user,
//                token,
//                LocalDateTime.now().minusHours(24)
//        );
//
//        when(verificationTokenRepository.findByToken(token))
//                .thenReturn(Optional.of(verificationToken));
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.verifyUser(token);
//        });
//        assertEquals("Verification token has expired", exception.getMessage());
//    }
//
//    @Test
//    void shouldFailToVerifyUserWithInvalidToken() {
//        // Arrange
//        String token = "123456";
//
//        when(verificationTokenRepository.findByToken(token))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.verifyUser(token);
//        });
//        assertEquals("Invalid verification token", exception.getMessage());
//    }
//
//    // Helpers for test data creation
//    private UserRegistrationRequest createValidRegistrationRequest() {
//        UserRegistrationRequest request = new UserRegistrationRequest();
//        request.setFirstName("John");
//        request.setLastName("Doe");
//        request.setUsername("johndoe");
//        request.setEmail("john.doe@example.com");
//        request.setPassword("StrongPassword123!");
//        return request;
//    }
//
//    // Note: The methods for invalid email and short password validation
//    // are not present in the current implementation, so these tests cannot be directly written
//    @Test
//    void shouldFailToRegisterUserWithInvalidEmail() {
//        // This test depends on email validation logic which is not in the current implementation
//        // You would need to add email validation in the UserService or have a separate validator
//        UserRegistrationRequest request = createValidRegistrationRequest();
//        request.setEmail("invalid-email");
//
//        assertThrows(RuntimeException.class, () -> {
//            userService.registerNewUser(request);
//        });
//    }
//
//    @Test
//    void shouldFailToRegisterUserWithShortPassword() {
//        // This test depends on password strength validation logic
//        // which is not in the current implementation
//        UserRegistrationRequest request = createValidRegistrationRequest();
//        request.setPassword("short");
//
//        assertThrows(RuntimeException.class, () -> {
//            userService.registerNewUser(request);
//        });
//    }
//}