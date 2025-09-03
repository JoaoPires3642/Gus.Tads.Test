package br.com.etl.painel_macroeconomico.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "indicador_economico", schema = "test")
public class IndicadorEconomico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "codigo_bc", nullable = false)
    private Integer codigoBc;

    @Column(name = "valor", nullable = false, precision = 18, scale = 4)
    private BigDecimal valor;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "frequencia", nullable = false)
    private String frequencia;
    
    @Column(name = "created_at", updatable = false)
private OffsetDateTime createdAt;
}
