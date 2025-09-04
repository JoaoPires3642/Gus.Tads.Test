package br.com.etl.painel_macroeconomico.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;





@Data
@Entity
@Table(name = "indicador_agregado_mensal", schema = "test")
public class IndicadorAgregadoMensal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_bc", nullable = false)
    private Integer codigoBc;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "valor_medio", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorMedio;

    @Column(name = "valor_maximo", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorMaximo;

    @Column(name = "valor_minimo", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorMinimo;
}
