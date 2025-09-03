package br.com.etl.painel_macroeconomico;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PainelMacroeconomicoApplicationTests {

	@Test
	void contextLoads() {
	}

}
