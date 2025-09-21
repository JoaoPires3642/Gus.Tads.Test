package br.com.etl.painel_macroeconomico.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import br.com.etl.painel_macroeconomico.model.UserModel;
import br.com.etl.painel_macroeconomico.repository.UserRepository;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        UserModel user = new UserModel("Gustavo", "gustavo@gmail.com ", "1234", LocalDate.parse("2000-01-01"));
        when(userRepository.save(user)).thenReturn(user);
        
        UserModel created = userService.createUser(user);

        assertEquals(user.getNome(), created.getNome());
    }

}
