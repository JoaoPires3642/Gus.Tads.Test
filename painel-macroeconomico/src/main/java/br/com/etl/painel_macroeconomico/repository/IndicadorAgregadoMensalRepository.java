package br.com.etl.painel_macroeconomico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.etl.painel_macroeconomico.model.agregado.IndicadorAgregadoMensal;

import java.util.Optional;

@Repository
public interface IndicadorAgregadoMensalRepository extends JpaRepository<IndicadorAgregadoMensal, Long> {
    
    
     // Busca um registro de agregação específico para um indicador, ano e mês.
    Optional<IndicadorAgregadoMensal> findByCodigoBcAndAnoAndMes(Integer codigoBc, Integer ano, Integer mes);
}