package de.jadenk.springcloud.service;

import de.jadenk.springcloud.model.Log;
import de.jadenk.springcloud.model.Role;
import de.jadenk.springcloud.model.User;
import de.jadenk.springcloud.repository.RoleRepository;
import de.jadenk.springcloud.repository.UserRepository;
import de.jadenk.springcloud.util.MessageService;
import de.jadenk.springcloud.util.WebhookEvent;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LogService logService;

    @Mock
    private MessageService messageService;

    @Mock
    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("plainPassword");

        Role role = new Role();
        role.setId(1L);

        Log log = new Log();
        log.setId(0L);

        when(roleRepo.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(logService.log(eq("testuser"), anyString())).thenReturn(log);

        userService.register(user);

        assertEquals("hashedPassword", user.getPassword());
        assertEquals(role, user.getRole());

        verify(userRepo).save(user);
        verify(webhookService).triggerWebhookEvent(eq(WebhookEvent.USER_REGISTERED), contains("testuser"), eq(log.getId()));
    }


    @Test
    void testUsernameExistsReturnsTrueIfFound() {
        when(userRepo.findByUsername("existingUser")).thenReturn(Optional.of(new User()));
        assertTrue(userService.usernameExists("existingUser"));
    }

    @Test
    void testUsernameExistsReturnsFalseIfNotFound() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertFalse(userService.usernameExists("nonexistent"));
    }

    @Test
    void testGetUserByIdReturnsUser() {
        User user = new User();
        user.setId(42L);
        when(userRepo.findById(42L)).thenReturn(Optional.of(user));
        assertEquals(user, userService.getUserById(42L));
    }

    @Test
    void testGetUserByIdThrowsIfNotFound() {
        when(userRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(123L));
    }

    @Test
    void testBanUserTogglesStatus() {
        User user = new User();
        user.setBanned(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        userService.banUser(1L);
        assertTrue(user.isBanned());

        userService.banUser(1L);
        assertFalse(user.isBanned());
    }
}
