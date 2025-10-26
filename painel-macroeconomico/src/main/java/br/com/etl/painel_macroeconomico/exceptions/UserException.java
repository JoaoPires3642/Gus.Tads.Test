package br.com.etl.painel_macroeconomico.exceptions;

import java.time.LocalDate;
import java.time.Period;

import br.com.etl.painel_macroeconomico.repository.UserRepository;

public class UserException extends RuntimeException {

    public static void invalidEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserException("E-mail inválido! O e-mail é obrigatório.");
        }
        email = email.trim();
        if (!email.matches("^[\\w._%+-]+@gmail\\.com$")) {
            throw new UserException("E-mail inválido! O email deve conter '@gmail.com'.");
        }
    }

    public static void emailAlreadyInUse(Long id, UserRepository userRepository, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já está em uso!");
        }
    }

    public static void invalidPassword(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new UserException("A senha é obrigatória.");
        }
        if (senha.length() < 8) {
            throw new UserException("A senha deve ter pelo menos 8 caracteres.");
        }
        if (!senha.matches(".*[A-Z].*")) {
            throw new UserException("A senha deve conter pelo menos uma letra maiúscula.");
        }
        if (!senha.matches(".*[a-z].*")) {
            throw new UserException("A senha deve conter pelo menos uma letra minúscula.");
        }
        if (!senha.matches(".*[0-9].*")) {
            throw new UserException("A senha deve conter pelo menos um número.");
        }
    }

    public static void invalidName(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new UserException("O nome é obrigatório.");
        }
    }

   public static void invalidBirthDate(LocalDate dataNascimento) {
    if (dataNascimento == null) {
        throw new UserException("A data de nascimento é obrigatória.");
    }
    if (dataNascimento.isAfter(LocalDate.now())) {
        throw new UserException("A data de nascimento não pode ser no futuro.");
    }
    int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
    if (idade < 18) {
        throw new UserException("O usuário deve ter pelo menos 18 anos.");
    }
}

    public static UserException userNotFound(Long id) {
        return new UserException("Usuário não encontrado com id: " + id);
    }

    public UserException(String message) {
        super(message);
    }

}
