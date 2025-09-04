package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal;
import br.com.etl.painel_macroeconomico.model.IndicadorAgregadoMensal;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoMensalRepository;
import br.com.etl.painel_macroeconomico.repository.IndicadorEconomicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AgregacaoService {

    private final IndicadorEconomicoRepository indicadorRepository;
    private final IndicadorAgregadoMensalRepository agregadoRepository;

    public AgregacaoService(IndicadorEconomicoRepository indicadorRepository, IndicadorAgregadoMensalRepository agregadoRepository) {
        this.indicadorRepository = indicadorRepository;
        this.agregadoRepository = agregadoRepository;
    }

    // Calcula e salva os dados agregados para um determinado mês.
    @Transactional
    public void calcularEsalvarAgregadosParaMes(LocalDate mesDeReferencia) {
        LocalDate dataInicio = mesDeReferencia.withDayOfMonth(1);
        LocalDate dataFim = mesDeReferencia.withDayOfMonth(mesDeReferencia.lengthOfMonth());
        
        System.out.println("INFO: [AgregacaoService] Calculando agregados para o período de " + dataInicio + " a " + dataFim);

        // 1. Usa a query  para o banco de dados calcular 
        List<ResultadoAgregacaoMensal> resultados = indicadorRepository.calcularAgregadosMensais(dataInicio, dataFim);
        
        System.out.println("INFO: [AgregacaoService] " + resultados.size() + " agregações calculadas pelo banco de dados.");

        for (ResultadoAgregacaoMensal res : resultados) {
            IndicadorAgregadoMensal agregado = agregadoRepository
                .findByCodigoBcAndAnoAndMes(res.codigoBc(), res.ano(), res.mes())
                .orElse(new IndicadorAgregadoMensal()); 
            agregado.setCodigoBc(res.codigoBc());
            agregado.setAno(res.ano());
            agregado.setMes(res.mes());
            agregado.setValorMedio(BigDecimal.valueOf(res.valorMedio()));
            agregado.setValorMaximo(res.valorMaximo());
            agregado.setValorMinimo(res.valorMinimo());

            agregadoRepository.save(agregado);
        }
        System.out.println("INFO: [AgregacaoService] Agregados mensais salvos com sucesso.");
    }
    @Transactional
    public void calcularEsalvarAgregadosParaAno(LocalDate anoDeReferencia) {
        LocalDate dataInicio = anoDeReferencia.withDayOfYear(1);
        LocalDate dataFim = anoDeReferencia.withDayOfYear(anoDeReferencia.lengthOfYear());
        
        System.out.println("INFO: [AgregacaoService] Calculando agregados para o período de " + dataInicio + " a " + dataFim);

        // 1. Usa a query  para o banco de dados calcular 
        List<ResultadoAgregacaoMensal> resultados = indicadorRepository.calcularAgregadosMensais(dataInicio, dataFim);
        
        System.out.println("INFO: [AgregacaoService] " + resultados.size() + " agregações calculadas pelo banco de dados.");

        for (ResultadoAgregacaoMensal res : resultados) {
            IndicadorAgregadoMensal agregado = agregadoRepository
                .findByCodigoBcAndAnoAndMes(res.codigoBc(), res.ano(), res.mes())
                .orElse(new IndicadorAgregadoMensal()); 
            agregado.setCodigoBc(res.codigoBc());
            agregado.setAno(res.ano());
            agregado.setMes(res.mes());
            agregado.setValorMedio(BigDecimal.valueOf(res.valorMedio()));
            agregado.setValorMaximo(res.valorMaximo());
            agregado.setValorMinimo(res.valorMinimo());

            agregadoRepository.save(agregado);
        }
        System.out.println("INFO: [AgregacaoService] Agregados mensais salvos com sucesso.");
    }
}
