package br.com.etl.painel_macroeconomico.repository;

import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoAnual;
import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal;
import br.com.etl.painel_macroeconomico.model.IndicadorEconomico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IndicadorEconomicoRepository extends JpaRepository<IndicadorEconomico, Long> {


    Optional<IndicadorEconomico> findByCodigoBcAndData(Integer codigoBc, LocalDate data);
    

    //query que calcula os agregados (média, máximo, mínimo) para um período mensal.
    @Query("SELECT new br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal(" +
           "ie.codigoBc, " +
           "YEAR(ie.data), " +
           "MONTH(ie.data), " +
           "AVG(ie.valor), " +
           "MAX(ie.valor), " +
           "MIN(ie.valor)) " +
           "FROM IndicadorEconomico ie " +
           "WHERE ie.data >= :dataInicio AND ie.data <= :dataFim " +
           "GROUP BY ie.codigoBc, YEAR(ie.data), MONTH(ie.data)") //aqui agrupamento é por código do indicador, ano e mês
    List<ResultadoAgregacaoMensal> calcularAgregadosMensais(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

@Query("SELECT new br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoAnual(" +
           "ie.codigoBc, " +
           "YEAR(ie.data), " +
           "AVG(ie.valor), " +
           "MAX(ie.valor), " +
           "MIN(ie.valor)) " +
           "FROM IndicadorEconomico ie " +
           "WHERE ie.data >= :dataInicio AND ie.data <= :dataFim " +
           "GROUP BY ie.codigoBc, YEAR(ie.data)") //aqui agrupamento é por código do indicador e ano
    List<ResultadoAgregacaoAnual> calcularAgregadosAnuais(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
}