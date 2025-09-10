package br.com.etl.painel_macroeconomico.repository;

import br.com.etl.painel_macroeconomico.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByIdAndEmail(Long id, String email);

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);

}
