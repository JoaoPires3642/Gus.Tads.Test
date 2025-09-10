package br.com.etl.painel_macroeconomico.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoAnual;


@Repository
public interface IndicadorAgregadoAnualRepository extends JpaRepository<IndicadorAgregadoAnual, Long> {
    
    Optional<IndicadorAgregadoAnual> findByCodigoBcAndAno(Integer codigoBc, Integer ano);
}
