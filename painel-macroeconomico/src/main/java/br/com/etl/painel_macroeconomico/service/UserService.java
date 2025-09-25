package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.exceptions.UserException;
import br.com.etl.painel_macroeconomico.model.UserModel;
import br.com.etl.painel_macroeconomico.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserModel createUser(UserModel user) {

        UserException.invalidName(user.getNome());
        UserException.invalidEmail(user.getEmail());
        UserException.emailAlreadyInUse(user.getId(), userRepository, user.getEmail());
        UserException.invalidPassword(user.getSenha());
        UserException.invalidBirthDate(user.getDataNascimento());

        return userRepository.save(user);
    }

    public UserModel updateUser(Long id, UserModel userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNome(userDetails.getNome());
                    user.setEmail(userDetails.getEmail());
                    user.setSenha(userDetails.getSenha());
                    user.setDataNascimento(userDetails.getDataNascimento());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> UserException.userNotFound(id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<UserModel> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
