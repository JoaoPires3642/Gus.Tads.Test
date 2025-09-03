package br.com.etl.painel_macroeconomico;

import org.springframework.boot.SpringApplication;

public class TestPainelMacroeconomicoApplication {

	public static void main(String[] args) {
		SpringApplication.from(PainelMacroeconomicoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
