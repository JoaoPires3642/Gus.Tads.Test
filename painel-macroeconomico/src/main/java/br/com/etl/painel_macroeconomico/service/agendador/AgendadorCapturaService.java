package br.com.etl.painel_macroeconomico.service.agendador;

import br.com.etl.painel_macroeconomico.model.Indicador;
import br.com.etl.painel_macroeconomico.service.PublisherService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AgendadorCapturaService {

    private final PublisherService publisherService;

    // Injetamos o PublisherService que você já criou
    public AgendadorCapturaService(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    /**
     * Método agendado para rodar todo dia às 14h no fuso horário de São Paulo.
     * cron primeiro numeros 0 é o segundos o segundo numero minutos e o terceiro Horas
     * Ele busca todos os indicadores definidos no Enum Indicador.
     */
    @Scheduled(cron = "0 37 14 * * ?", zone = "America/Sao_Paulo")  
    public void capturarIndicadoresAgendados() {
        System.out.println("==========================================================");
        System.out.println("INICIANDO CAPTURA AGENDADA DE INDICADORES...");
        System.out.println("Horário: " + java.time.LocalDateTime.now());
        System.out.println("==========================================================");

        LocalDate hoje = LocalDate.now();

    
        for (Indicador indicador : Indicador.values()) {
            
            System.out.println("-> Verificando indicador: " + indicador.getNomeAmigavel());

            boolean deveBuscar = false;
            LocalDate dataInicial = null;
            LocalDate dataFinal = null;

           
            if ("Diária".equalsIgnoreCase(indicador.getFrequencia())) {
                deveBuscar = true;
                // Busca os dados do dia anterior e hoje, para garantir o último valor publicado.
                dataInicial = hoje.minusDays(1);
                dataFinal = hoje;
            
            
            } else if ("Mensal".equalsIgnoreCase(indicador.getFrequencia())) {
                //Aqui pega para o segundo dia do mês que geralmente é quando o BC publica os dados do mês anterior
                if (hoje.getDayOfMonth() == 2) {
                    deveBuscar = true;
                    // Busca os dados referentes ao mês anterior completo.
                    LocalDate primeiroDiaMesAnterior = hoje.minusMonths(1).withDayOfMonth(1);
                    LocalDate ultimoDiaMesAnterior = primeiroDiaMesAnterior.withDayOfMonth(primeiroDiaMesAnterior.lengthOfMonth());
                    dataInicial = primeiroDiaMesAnterior;
                    dataFinal = ultimoDiaMesAnterior;
                }
            }

            if (deveBuscar) {
                System.out.println("   Disparando captura para o período de " + dataInicial + " a " + dataFinal);
                try {
                    publisherService.publicarIndicador(
                            indicador.getNomeAmigavel(),
                            indicador.getCodigoSgs(),
                            indicador.getFrequencia(),
                            dataInicial,
                            dataFinal
                    );
                } catch (Exception e) {
                    System.err.println("   ⚠️ Erro ao processar indicador '"
                            + indicador.getNomeAmigavel() + "': " + e.getMessage());
                }
            } else {
                System.out.println("   Indicador '" + indicador.getNomeAmigavel() + "' não agendado para hoje. Ignorando.");
            }
        }

        System.out.println("==========================================================");
        System.out.println("CAPTURA AGENDADA FINALIZADA.");
        System.out.println("==========================================================");
    }
}

