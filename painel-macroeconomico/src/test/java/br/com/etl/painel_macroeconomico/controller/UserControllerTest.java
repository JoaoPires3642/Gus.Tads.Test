package br.com.etl.painel_macroeconomico.controller;

import br.com.etl.painel_macroeconomico.model.UserModel;
import br.com.etl.painel_macroeconomico.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private UserModel userModel;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new br.com.etl.painel_macroeconomico.exceptions.handler.RestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para suportar LocalDate

        userModel = new UserModel(
                "João Silva",
                "joao@gmail.com",
                "SenhaForte123",
                LocalDate.of(1990, 5, 15)
        );
        userModel.setId(1L);
    }

    @Test
    @DisplayName("GET /api/users - Deve retornar lista de usuários")
    void deveRetornarListaDeUsuarios() throws Exception {
        // Arrange
        List<UserModel> usuarios = Arrays.asList(userModel);
        when(userService.getAllUsers()).thenReturn(usuarios);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[0].email").value("joao@gmail.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar usuário por ID")
    void deveRetornarUsuarioPorId() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(userModel));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@gmail.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar 404 quando usuário não existe")
    void deveRetornar404QuandoUsuarioNaoExiste() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("POST /api/users - Deve criar novo usuário")
    void deveCriarNovoUsuario() throws Exception {
        // Arrange
        when(userService.createUser(any(UserModel.class))).thenReturn(userModel);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userModel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@gmail.com"));

        verify(userService).createUser(any(UserModel.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Deve atualizar usuário")
    void deveAtualizarUsuario() throws Exception {
        // Arrange
        UserModel userAtualizado = new UserModel(
                "João Silva Atualizado",
                "joao@gmail.com",
                "NovaSenha123",
                LocalDate.of(1990, 5, 15)
        );
        userAtualizado.setId(1L);

        when(userService.updateUser(eq(1L), any(UserModel.class)))
                .thenReturn(userAtualizado);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userAtualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva Atualizado"));

        verify(userService).updateUser(eq(1L), any(UserModel.class));
    }

    @Test
    @DisplayName("POST /api/users - Deve retornar 400 para dados inválidos")
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        // Arrange
        UserModel userInvalido = new UserModel(
                "",
                "email-invalido",
                "123",
                LocalDate.now().plusDays(1)
        );

        when(userService.createUser(any(UserModel.class)))
                .thenThrow(new RuntimeException("Dados inválidos"));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInvalido)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/users - Deve retornar lista vazia quando não há usuários")
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}