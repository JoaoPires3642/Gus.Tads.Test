package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.model.UserModel;
import br.com.etl.painel_macroeconomico.repository.UserRepository;
import br.com.etl.painel_macroeconomico.service.docker.AbstractPostgresDockerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTestContainer extends AbstractPostgresDockerTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;


    // Injeta as propriedades do container no Spring Boot
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Print para debug
        System.out.println("🔹 JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("🔹 Username: " + postgres.getUsername());
        System.out.println("🔹 Password: " + postgres.getPassword());
    }

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository);
        userRepository.deleteAll(); // limpa a tabela antes de cada teste
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        UserModel user = new UserModel();
        user.setNome("Fulano Teste");
        user.setEmail("fulano@gmail.com");
        user.setSenha("SenhaForte1");
        user.setDataNascimento(LocalDate.of(1995, 6, 15));

        UserModel saved = userService.createUser(user);
        assertNotNull(saved.getId());

        // Recupera do banco usando o ID gerado
        Optional<UserModel> fromDb = userRepository.findById(saved.getId());

        // Print no console
        fromDb.ifPresent(u -> System.out.println(
                "ID: " + u.getId() +
                        ", Nome: " + u.getNome() +
                        ", Email: " + u.getEmail() +
                        ", Data Nascimento: " + u.getDataNascimento()
        ));
    }

    @Test
    void deveAtualizarUsuarioExistente() {
        UserModel user = new UserModel();
        user.setNome("Ciclano");
        user.setEmail("ciclano@gmail.com");
        user.setSenha("SenhaForte2");
        user.setDataNascimento(LocalDate.of(1990, 1, 10));

        UserModel created = userService.createUser(user);

        UserModel updateData = new UserModel();
        updateData.setNome("Ciclano Atualizado");
        updateData.setEmail("ciclano@gmail.com");
        updateData.setSenha("NovaSenha123");
        updateData.setDataNascimento(LocalDate.of(1990, 1, 10));

        UserModel updated = userService.updateUser(created.getId(), updateData);
        assertEquals("Ciclano Atualizado", updated.getNome());
    }

    @Test
    void deveExcluirUsuario() {
        UserModel user = new UserModel();
        user.setNome("Excluido");
        user.setEmail("excluido@gmail.com");
        user.setSenha("SenhaForte4");
        user.setDataNascimento(LocalDate.of(1998, 8, 8));

        UserModel created = userService.createUser(user);
        userService.deleteUser(created.getId());

        Optional<UserModel> found = userService.getUserById(created.getId());
        assertTrue(found.isEmpty());
    }

}
