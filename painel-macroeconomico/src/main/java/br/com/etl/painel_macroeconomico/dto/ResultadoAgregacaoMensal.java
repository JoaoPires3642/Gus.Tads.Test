package br.com.etl.painel_macroeconomico.dto;
import java.math.BigDecimal;

public record ResultadoAgregacaoMensal(
    Integer codigoBc,
    Integer ano,
    Integer mes,
    Double valorMedio,
    BigDecimal valorMaximo,
    BigDecimal valorMinimo
) {}
