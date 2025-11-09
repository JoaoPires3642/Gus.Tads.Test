package br.com.etl.painel_macroeconomico.exceptions;

import br.com.etl.painel_macroeconomico.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        // Act
        UserException exception = new UserException("Mensagem de teste");

        // Assert
        assertEquals("Mensagem de teste", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve lançar exceção para email vazio ou em branco")
    void deveLancarExcecaoParaEmailVazioOuEmBranco(String email) {
        assertThrows(UserException.class, () -> UserException.invalidEmail(email));
    }

    @Test
    @DisplayName("Deve lançar exceção para email sem @gmail.com")
    void deveLancarExcecaoParaEmailSemGmail() {
        assertThrows(UserException.class,
                () -> UserException.invalidEmail("teste@hotmail.com"));
    }

    @Test
    @DisplayName("Deve lançar exceção para email sem @")
    void deveLancarExcecaoParaEmailSemArroba() {
        assertThrows(UserException.class,
                () -> UserException.invalidEmail("testegmail.com"));
    }

    @Test
    @DisplayName("Não deve lançar exceção para email válido")
    void naoDeveLancarExcecaoParaEmailValido() {
        assertDoesNotThrow(() -> UserException.invalidEmail("teste@gmail.com"));
    }

    @Test
    @DisplayName("Deve lançar exceção para email já em uso")
    void deveLancarExcecaoParaEmailJaEmUso() {
        // Arrange
        UserRepository repository = mock(UserRepository.class);
        when(repository.existsByEmail("teste@gmail.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> UserException.emailAlreadyInUse(null, repository, "teste@gmail.com"));
    }

    @Test
    @DisplayName("Não deve lançar exceção para email não utilizado")
    void naoDeveLancarExcecaoParaEmailNaoUtilizado() {
        // Arrange
        UserRepository repository = mock(UserRepository.class);
        when(repository.existsByEmail("teste@gmail.com")).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() ->
                UserException.emailAlreadyInUse(null, repository, "teste@gmail.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve lançar exceção para senha vazia")
    void deveLancarExcecaoParaSenhaVazia(String senha) {
        assertThrows(UserException.class, () -> UserException.invalidPassword(senha));
    }

    @Test
    @DisplayName("Deve lançar exceção para senha com menos de 8 caracteres")
    void deveLancarExcecaoParaSenhaComMenosDe8Caracteres() {
        assertThrows(UserException.class,
                () -> UserException.invalidPassword("Abc123"));
    }

    @Test
    @DisplayName("Deve lançar exceção para senha sem letra maiúscula")
    void deveLancarExcecaoParaSenhaSemLetraMaiuscula() {
        assertThrows(UserException.class,
                () -> UserException.invalidPassword("abcdefgh1"));
    }

    @Test
    @DisplayName("Deve lançar exceção para senha sem letra minúscula")
    void deveLancarExcecaoParaSenhaSemLetraMinuscula() {
        assertThrows(UserException.class,
                () -> UserException.invalidPassword("ABCDEFGH1"));
    }

    @Test
    @DisplayName("Deve lançar exceção para senha sem número")
    void deveLancarExcecaoParaSenhaSemNumero() {
        assertThrows(UserException.class,
                () -> UserException.invalidPassword("Abcdefghi"));
    }

    @Test
    @DisplayName("Não deve lançar exceção para senha válida")
    void naoDeveLancarExcecaoParaSenhaValida() {
        assertDoesNotThrow(() -> UserException.invalidPassword("SenhaForte123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Deve lançar exceção para nome vazio")
    void deveLancarExcecaoParaNomeVazio(String nome) {
        assertThrows(UserException.class, () -> UserException.invalidName(nome));
    }

    @Test
    @DisplayName("Não deve lançar exceção para nome válido")
    void naoDeveLancarExcecaoParaNomeValido() {
        assertDoesNotThrow(() -> UserException.invalidName("João Silva"));
    }

    @Test
    @DisplayName("Deve lançar exceção para data de nascimento nula")
    void deveLancarExcecaoParaDataDeNascimentoNula() {
        assertThrows(UserException.class,
                () -> UserException.invalidBirthDate(null));
    }

    @Test
    @DisplayName("Deve lançar exceção para data de nascimento no futuro")
    void deveLancarExcecaoParaDataDeNascimentoNoFuturo() {
        LocalDate dataFutura = LocalDate.now().plusDays(1);
        assertThrows(UserException.class,
                () -> UserException.invalidBirthDate(dataFutura));
    }

    @Test
    @DisplayName("Deve lançar exceção para idade menor que 18 anos")
    void deveLancarExcecaoParaIdadeMenorQue18Anos() {
        LocalDate dataMenor = LocalDate.now().minusYears(17);
        assertThrows(UserException.class,
                () -> UserException.invalidBirthDate(dataMenor));
    }

    @Test
    @DisplayName("Não deve lançar exceção para data de nascimento válida")
    void naoDeveLancarExcecaoParaDataDeNascimentoValida() {
        LocalDate dataValida = LocalDate.of(1990, 5, 15);
        assertDoesNotThrow(() -> UserException.invalidBirthDate(dataValida));
    }

    @Test
    @DisplayName("Deve criar exceção userNotFound com ID correto")
    void deveCriarExcecaoUserNotFoundComIdCorreto() {
        // Act
        UserException exception = UserException.userNotFound(123L);

        // Assert
        assertTrue(exception.getMessage().contains("123"));
        assertTrue(exception.getMessage().contains("não encontrado"));
    }

    @Test
    @DisplayName("Deve aceitar data de nascimento exatamente 18 anos atrás")
    void deveAceitarDataDeNascimentoExatamente18AnosAtras() {
        LocalDate data18Anos = LocalDate.now().minusYears(18);
        assertDoesNotThrow(() -> UserException.invalidBirthDate(data18Anos));
    }
}