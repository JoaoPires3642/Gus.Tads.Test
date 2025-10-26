package br.com.etl.painel_macroeconomico.service;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.etl.painel_macroeconomico.exceptions.UserException;

public class UserServiceTest {

    @ParameterizedTest
    @DisplayName("Deve lançar UserException para e-mails inválidos")
    @ValueSource(strings = {
            "",                 
            "   ",              
            "teste@",           
            "teste@gmail",      
            "teste@hotmail.com" 
    })
    void deveLancarExcecaoParaEmailInvalido(String email) {
        UserException ex = assertThrows(UserException.class, () -> UserException.invalidEmail(email));
        assertTrue(ex.getMessage().contains("E-mail"));
    }

    @ParameterizedTest
    @DisplayName("Não deve lançar exceção para e-mails válidos do Gmail")
    @ValueSource(strings = {
            "teste@gmail.com",
            "usuario123@gmail.com",
            "user.name@gmail.com"
    })
    void naoDeveLancarExcecaoParaEmailValido(String email) {
        assertDoesNotThrow(() -> UserException.invalidEmail(email));
    }

    @ParameterizedTest
    @DisplayName("Deve lançar UserException para senhas inválidas")
    @ValueSource(strings = {
            "",                
            "abc",             
            "abcdefghi",       
            "abcdefghi1",      
            "ABCDEFGH1",       
            "Abcdefghi"        
    })
    void deveLancarExcecaoParaSenhaInvalida(String senha) {
        assertThrows(UserException.class, () -> UserException.invalidPassword(senha));
    }

    @ParameterizedTest
    @DisplayName("Não deve lançar exceção para senhas válidas")
    @ValueSource(strings = {
            "Abcdefg1",
            "SenhaForte123",
            "JavaRocks9"
    })
    void naoDeveLancarExcecaoParaSenhaValida(String senha) {
        assertDoesNotThrow(() -> UserException.invalidPassword(senha));
    }

    static Stream<Arguments> datasInvalidas() {
        return Stream.of(
                Arguments.of(null, "A data é obrigatória"),
                Arguments.of(LocalDate.now().plusDays(1), "no futuro"),
                Arguments.of(LocalDate.now().minusYears(17), "18 anos")
        );
    }

    @ParameterizedTest
    @DisplayName("Deve lançar UserException para datas de nascimento inválidas")
    @MethodSource("datasInvalidas")
    void deveLancarExcecaoParaDataInvalida(LocalDate data, String mensagemEsperada) {
        UserException ex = assertThrows(UserException.class, () -> UserException.invalidBirthDate(data));
        assertTrue(ex.getMessage().contains(mensagemEsperada));
    }

    @ParameterizedTest
    @DisplayName("Não deve lançar exceção para datas de nascimento válidas")
    @ValueSource(strings = {
            "2000-01-01",
            "1990-05-15",
            "1985-12-31"
    })
    void naoDeveLancarExcecaoParaDataValida(String data) {
        LocalDate nascimento = LocalDate.parse(data);
        assertDoesNotThrow(() -> UserException.invalidBirthDate(nascimento));
    }

}
