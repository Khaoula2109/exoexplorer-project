package com.example.exoExplorer.services;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.BadCredentialsException;
import com.example.exoExplorer.exceptions.ResourceNotFoundException;
import com.example.exoExplorer.exceptions.UserAlreadyExistsException;
import com.example.exoExplorer.exceptions.UserNotFoundException;
import com.example.exoExplorer.factory.UserFactory;
import com.example.exoExplorer.observer.UserActionSubject;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import com.example.exoExplorer.repositories.UserRepository;
import com.example.exoExplorer.strategy.OtpStrategyFactory;
import com.example.exoExplorer.strategy.OtpVerificationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExoplaneteRepository exoplaneteRepository;

    @Mock
    private MailService mailService;

    @Mock
    private UserFactory userFactory;

    @Mock
    private OtpStrategyFactory otpStrategyFactory;

    @Mock
    private OtpVerificationStrategy otpStrategy;

    @Mock
    private UserActionSubject userActionSubject;

    @InjectMocks
    private UserService userService;

    private User user;
    private Exoplanet exoplanet;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        user.setOtpVerified(false);

        exoplanet = new Exoplanet();
        exoplanet.setId(1);
        exoplanet.setName("Kepler-22b");
    }

    @Test
    @DisplayName("Register user - Success")
    void testRegisterUserSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userFactory.createRegularUser("test@example.com", "password")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User registered = userService.registerUser("test@example.com", "password");

        // Assert
        assertNotNull(registered);
        assertEquals("test@example.com", registered.getEmail());
        verify(userRepository).findByEmail("test@example.com");
        verify(userFactory).createRegularUser("test@example.com", "password");
        verify(userRepository).save(user);
        verify(userActionSubject).notifyObservers(any(), eq(user), isNull());
    }

    @Test
    @DisplayName("Register user - Already exists")
    void testRegisterUserAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser("test@example.com", "password"));

        assertEquals("Cet utilisateur existe déjà", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(userFactory, never()).createRegularUser(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Process login - Success")
    void testProcessLoginSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        assertDoesNotThrow(() -> userService.processLogin("test@example.com", "password"));

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(user);
        verify(mailService).sendOtpEmail(eq("test@example.com"), otpCaptor.capture());
        verify(userActionSubject).notifyObservers(any(), eq(user), isNull());

        String otp = otpCaptor.getValue();
        assertNotNull(otp);
        assertTrue(otp.length() == 6);
    }

    @Test
    @DisplayName("Process login - Wrong password")
    void testProcessLoginWrongPassword() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.processLogin("test@example.com", "wrongpassword"));

        assertEquals("Email ou mot de passe incorrect", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(mailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Toggle favorite - Add")
    void testToggleFavoriteAdd() {
        // Arrange
        Set<Exoplanet> favorites = new HashSet<>();
        user.setFavorites(favorites);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(exoplaneteRepository.findById(1)).thenReturn(Optional.of(exoplanet));

        // Act
        userService.toggleFavorite("test@example.com", 1);

        // Assert
        assertTrue(user.getFavorites().contains(exoplanet));
        verify(userRepository).findByEmail("test@example.com");
        verify(exoplaneteRepository).findById(1);
        verify(userRepository).save(user);
        verify(userActionSubject).notifyObservers(any(), eq(user), eq(exoplanet));
    }

    @Test
    @DisplayName("Toggle favorite - Remove")
    void testToggleFavoriteRemove() {
        // Arrange
        Set<Exoplanet> favorites = new HashSet<>();
        favorites.add(exoplanet);
        user.setFavorites(favorites);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(exoplaneteRepository.findById(1)).thenReturn(Optional.of(exoplanet));

        // Act
        userService.toggleFavorite("test@example.com", 1);

        // Assert
        assertFalse(user.getFavorites().contains(exoplanet));
        verify(userRepository).findByEmail("test@example.com");
        verify(exoplaneteRepository).findById(1);
        verify(userRepository).save(user);
        verify(userActionSubject).notifyObservers(any(), eq(user), eq(exoplanet));
    }

    @Test
    @DisplayName("Change password - Success")
    void testChangePasswordSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        assertDoesNotThrow(() -> userService.changePassword("test@example.com", "password", "newpassword"));

        // Assert
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(user);
        verify(userActionSubject).notifyObservers(any(), eq(user), isNull());
        assertTrue(BCrypt.checkpw("newpassword", user.getPassword()));
    }
}