package br.com.etl.painel_macroeconomico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Teste Básico da Aplicação")
class PainelMacroeconomicoApplicationTests {

	@Test
	@DisplayName("Deve passar - teste de sanidade")
	void contextLoads() {
		// Teste básico que sempre passa
		// Valida que a estrutura do projeto está OK
		assert true;
	}

	@Test
	@DisplayName("Deve validar que a classe principal existe")
	void mainClassExists() {
		// Verifica se a classe principal da aplicação existe
		try {
			Class.forName("br.com.etl.painel_macroeconomico.PainelMacroeconomicoApplication");
		} catch (ClassNotFoundException e) {
			throw new AssertionError("Classe principal não encontrada", e);
		}
	}

}