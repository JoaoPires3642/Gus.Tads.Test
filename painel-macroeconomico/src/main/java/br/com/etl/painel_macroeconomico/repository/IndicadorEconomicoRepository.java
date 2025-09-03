package br.com.etl.painel_macroeconomico.repository;

import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IndicadorEconomicoRepository extends JpaRepository<IndicadorEconomico, Long> {


    Optional<IndicadorEconomico> findByCodigoBcAndData(Integer codigoBc, LocalDate data);
}