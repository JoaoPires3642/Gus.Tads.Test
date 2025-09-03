package br.com.etl.painel_macroeconomico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorEconomicoDTO {
    private String nome;       
    private Integer codigoBc;   
    private BigDecimal valor;  
    private LocalDate data;     
    private String frequencia;  
}