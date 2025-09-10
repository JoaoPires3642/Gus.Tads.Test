package br.com.etl.painel_macroeconomico.model;

/**
 * Enum que representa os indicadores econômicos a serem monitorados.
 * Cada indicador possui seu nome amigável, código no sistema SGS do Banco Central
 * e a frequência de atualização.
 */
public enum Indicador {

    // Câmbio e Reservas
    DOLAR("Dólar Americano (Venda)", 10813, "Diária"),
    EURO("Cotação do Euro", 21619, "Diária"),
    RESERVAS_INTERNACIONAIS("Reservas Internacionais", 13982, "Diária"),

    // Juros e Inflação
    SELIC("Taxa Selic (Acum. Mês)", 4390, "Diária"),
    IPCA("IPCA (Variação Mensal %)", 10844, "Mensal"),
    
    // Atividade Econômica e Emprego
    IBC_BR("IBC-Br (Prévia do PIB)", 24369, "Mensal"),
    CAGED("Saldo de Empregos Formais (CAGED)", 28763, "Mensal"),
 

    // Fiscal
    DIVIDA_LIQUIDA_SP("Dívida Líquida do Setor Público (% PIB)", 4513, "Mensal");

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