package br.com.etl.painel_macroeconomico.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class AgendadorAgregacaoService {

    private final AgregacaoService agregacaoService;

    public AgendadorAgregacaoService(AgregacaoService agregacaoService) {
        this.agregacaoService = agregacaoService;
    }

    // Executa diariamente às 07:55 AM, horário de São Paulo
    // Recalcula os agregados do mês atual e, se for o primeiro dia do mês,
    // também recalcula os agregados do mês anterior.
    @Scheduled(cron = "0 33 08 * * ?", zone = "America/Sao_Paulo")
    public void executarAgregacaoDiaria() {
        System.out.println("==========================================================");
        System.out.println("INICIANDO PROCESSO AGENDADO DE AGREGAÇÃO DE DADOS...");
        System.out.println("==========================================================");
        
        // Recalcula os agregados para o mês atual
        agregacaoService.calcularEsalvarAgregadosParaMes(LocalDate.now());
        
        // No primeiro dia do mês, recalcula o mês anterior para garantir a consolidação final.
        if (LocalDate.now().getDayOfMonth() == 1) {
            System.out.println("INFO: [AgendadorAgregacao] Primeiro dia do mês, recalculando agregados do mês anterior.");
            agregacaoService.calcularEsalvarAgregadosParaMes(LocalDate.now().minusMonths(1));
        }

        System.out.println("==========================================================");
        System.out.println("PROCESSO AGENDADO DE AGREGAÇÃO FINALIZADO.");
        System.out.println("==========================================================");
    }
}