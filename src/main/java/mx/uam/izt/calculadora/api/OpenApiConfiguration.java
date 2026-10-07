package mx.uam.izt.calculadora.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI calculadoraOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Calculadora API")
						.description("API REST para operaciones aritmeticas basicas.")
						.version("v1")
						.contact(new Contact().name("Equipo Calculadora")));
	}
}
