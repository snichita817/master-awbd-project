package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Role;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.RoleRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);
    }

    @Test
    void createUser_happyPath_savesAndReturnsUser() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("Alice", "alice@example.com", new BigDecimal("3000.00"));

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser("Alice", "alice@example.com", new BigDecimal("3000.00")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("alice@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_existingId_callsDeleteById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getUserById_existingId_returnsUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
    }

    @Test
    void getUserById_nonExistingId_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerUser_happyPath_encodesPasswordAndSaves() {
        Role userRole = new Role("ROLE_USER");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser("Alice", "alice@example.com", "secret", new BigDecimal("3000.00"));

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("Alice", "alice@example.com", "pass", new BigDecimal("1000.00")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateUser
    // -----------------------------------------------------------------------

    @Test
    void updateUser_happyPath_returnsUpdatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, "Alice Updated", "newemail@example.com", new BigDecimal("4000.00"));

        assertThat(result.getName()).isEqualTo("Alice Updated");
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_sameEmail_doesNotCheckDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(1L, "Alice", "alice@example.com", new BigDecimal("3500.00"));

        assertThat(result.getMonthlyIncome()).isEqualTo(new BigDecimal("3500.00"));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, "Alice", "taken@example.com", new BigDecimal("3000.00")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, "X", "x@example.com", BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // getUserByEmail / getAllUsers / searchUsers / existsByEmail
    // -----------------------------------------------------------------------

    @Test
    void getUserByEmail_returnsOptional() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void searchUsers_withQuery_callsSearchRepository() {
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("ali", "ali"))
                .thenReturn(List.of(user));

        List<User> result = userService.searchUsers("ali");

        assertThat(result).hasSize(1);
    }

    @Test
    void searchUsers_blankQuery_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.searchUsers("");

        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    void existsByEmail_returnsTrue() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThat(userService.existsByEmail("alice@example.com")).isTrue();
    }
}

