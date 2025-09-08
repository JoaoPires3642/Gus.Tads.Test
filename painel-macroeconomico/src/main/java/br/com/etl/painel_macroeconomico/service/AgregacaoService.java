package br.com.etl.painel_macroeconomico.service;

import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoAnual;
import br.com.etl.painel_macroeconomico.dto.ResultadoAgregacaoMensal;
import br.com.etl.painel_macroeconomico.model.IndicadorAgregadoAnual;
import br.com.etl.painel_macroeconomico.model.IndicadorAgregadoMensal;
import br.com.etl.painel_macroeconomico.repository.IndicadorAgregadoAnualRepository;
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
    private final IndicadorAgregadoMensalRepository agregadoMensalRepository;
    private final IndicadorAgregadoAnualRepository agregadoAnualRepository; // <- A dependência que estava faltando

    // CORREÇÃO: O construtor agora aceita os TRÊS repositórios
    public AgregacaoService(IndicadorEconomicoRepository indicadorRepository, 
                            IndicadorAgregadoMensalRepository agregadoMensalRepository, 
                            IndicadorAgregadoAnualRepository agregadoAnualRepository) {
        this.indicadorRepository = indicadorRepository;
        this.agregadoMensalRepository = agregadoMensalRepository;
        this.agregadoAnualRepository = agregadoAnualRepository; // <- Garantindo que seja inicializado
    }

    /**
     * Calcula e salva os dados agregados para um determinado mês.
     */
    @Transactional
    public void calcularEsalvarAgregadosParaMes(LocalDate mesDeReferencia) {
        LocalDate dataInicio = mesDeReferencia.withDayOfMonth(1);
        LocalDate dataFim = mesDeReferencia.withDayOfMonth(mesDeReferencia.lengthOfMonth());
        
        System.out.println("INFO: [AgregacaoService] Calculando agregados mensais para o período de " + dataInicio + " a " + dataFim);

        List<ResultadoAgregacaoMensal> resultados = indicadorRepository.calcularAgregadosMensais(dataInicio, dataFim);
        
        System.out.println("INFO: [AgregacaoService] " + resultados.size() + " agregações mensais calculadas.");

        for (ResultadoAgregacaoMensal res : resultados) {
            IndicadorAgregadoMensal agregado = agregadoMensalRepository
                    .findByCodigoBcAndAnoAndMes(res.codigoBc(), res.ano(), res.mes())
                    .orElse(new IndicadorAgregadoMensal()); 

            agregado.setCodigoBc(res.codigoBc());
            agregado.setAno(res.ano());
            agregado.setMes(res.mes());
            agregado.setValorMedio(BigDecimal.valueOf(res.valorMedio()));
            agregado.setValorMaximo(res.valorMaximo());
            agregado.setValorMinimo(res.valorMinimo());

            agregadoMensalRepository.save(agregado);
        }
        System.out.println("INFO: [AgregacaoService] Agregados mensais salvos com sucesso.");
    }

    /**
     * Calcula e salva os dados agregados para um determinado ano.
     */
    @Transactional
    public void calcularEsalvarAgregadosParaAno(LocalDate anoDeReferencia) {
        LocalDate dataInicio = LocalDate.of(anoDeReferencia.getYear(), 1, 1);
        LocalDate dataFim = LocalDate.of(anoDeReferencia.getYear(), 12, 31);

        System.out.println("INFO: [AgregacaoService] Calculando agregados anuais para o período de " + dataInicio + " a " + dataFim);

        List<ResultadoAgregacaoAnual> resultados = indicadorRepository.calcularAgregadosAnuais(dataInicio, dataFim);
        
        System.out.println("INFO: [AgregacaoService] " + resultados.size() + " agregações anuais calculadas.");

        for (ResultadoAgregacaoAnual res : resultados) {
            // O código agora pode usar o repositório com segurança
            IndicadorAgregadoAnual agregado = agregadoAnualRepository
                    .findByCodigoBcAndAno(res.codigoBc(), res.ano())
                    .orElse(new IndicadorAgregadoAnual());
                    
            agregado.setCodigoBc(res.codigoBc());
            agregado.setAno(res.ano());
            agregado.setValorMedio(BigDecimal.valueOf(res.valorMedio()));
            agregado.setValorMaximo(res.valorMaximo());
            agregado.setValorMinimo(res.valorMinimo());

            agregadoAnualRepository.save(agregado);
        }
        System.out.println("INFO: [AgregacaoService] Agregados anuais salvos com sucesso.");
    }
}

