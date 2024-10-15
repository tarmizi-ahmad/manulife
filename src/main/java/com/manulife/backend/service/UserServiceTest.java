package com.manulife.backend.service;

import com.manulife.backend.model.User;
import com.manulife.backend.repository.UserRepository;
import com.manulife.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class UserServiceTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserService userService = new UserServiceImpl(userRepository);

    @Test
    public void testSaveUser_EmailExists_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");

        Mockito.when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.saveUser(user));
    }

    @Test
    public void testGetUserById_NotFound_ThrowsException() {
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
    }
}
