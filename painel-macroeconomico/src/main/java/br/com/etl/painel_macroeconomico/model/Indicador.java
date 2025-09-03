package br.com.etl.painel_macroeconomico.model;

/**
 * Enum que representa os indicadores econômicos a serem monitorados.
 * Cada indicador possui seu nome amigável, código no sistema SGS do Banco Central
 * e a frequência de atualização.
 */
public enum Indicador {

    
    DOLAR("Dólar Americano - Venda (PTAX)", 10813, "Diária"),
    EURO("Cotação do Euro", 21619, "Diária"),
    SELIC("Taxa Selic ", 11, "Diária"),
    SELICM("Taxa Selic - Mensal", 4390, "Mensal"),
    IPCA("IPCA - Variação Mensal", 10844, "Mensal"),
    RESERVAS_INTERNACIONAIS("Reservas Internacionais", 13982, "Diária");

   //BOVESPA("Índice Bovespa", 7, "Diária"); esta dando erro e falhando

    private final String nomeAmigavel;
    private final int codigoSgs;
    private final String frequencia;

    Indicador(String nomeAmigavel, int codigoSgs, String frequencia) {
        this.nomeAmigavel = nomeAmigavel;
        this.codigoSgs = codigoSgs;
        this.frequencia = frequencia;
    }

    // Métodos Getters (nenhuma alteração aqui)
    public String getNomeAmigavel() {
        return nomeAmigavel;
    }

    public int getCodigoSgs() {
        return codigoSgs;
    }

    public String getFrequencia() {
        return frequencia;
    }
}