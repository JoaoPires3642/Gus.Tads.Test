package br.com.etl.painel_macroeconomico.dto;
import java.math.BigDecimal;


public record ResultadoAgregacaoAnual(
    Integer codigoBc,
    Integer ano,
    Double valorMedio,
    BigDecimal valorMaximo,
    BigDecimal valorMinimo
) {}
