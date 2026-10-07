package mx.uam.izt.calculadora;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CalculadoraApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void healthEndpointIndicaQueLaApiEstaActiva() throws Exception {
		mockMvc.perform(get("/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void sumarDosNumeros() throws Exception {
		mockMvc.perform(get("/api/v1/calculadora/sumar")
				.param("numero1", "2.5")
				.param("numero2", "3.25"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.operacion").value("sumar"))
			.andExpect(jsonPath("$.resultado").value(5.75));
	}

	@Test
	void restarDosNumeros() throws Exception {
		mockMvc.perform(get("/api/v1/calculadora/restar")
				.param("numero1", "10")
				.param("numero2", "4"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultado").value(6));
	}

	@Test
	void multiplicarDosNumeros() throws Exception {
		mockMvc.perform(get("/api/v1/calculadora/multiplicar")
				.param("numero1", "3")
				.param("numero2", "4"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultado").value(12));
	}

	@Test
	void dividirDosNumeros() throws Exception {
		mockMvc.perform(get("/api/v1/calculadora/dividir")
				.param("numero1", "1")
				.param("numero2", "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultado").value(0.3333333333333333));
	}

	@Test
	void dividirEntreCeroDevuelveErrorDeCliente() throws Exception {
		mockMvc.perform(get("/api/v1/calculadora/dividir")
				.param("numero1", "1")
				.param("numero2", "0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Operacion no valida"))
			.andExpect(jsonPath("$.detail").value("No es posible dividir entre cero"));
	}

	@Test
	void exponeMetricasDePrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("# HELP")));
	}

	@Test
	void exponeDocumentacionOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.info.title").value("Calculadora API"))
			.andExpect(jsonPath("$.paths['/api/v1/calculadora/sumar']").exists())
			.andExpect(jsonPath("$.paths['/api/v1/calculadora/dividir'].get.responses['400'].description")
					.value("El divisor es cero, falta un parametro o alguno no es un numero decimal valido"))
			.andExpect(jsonPath("$.paths['/health']").exists());
	}
}
