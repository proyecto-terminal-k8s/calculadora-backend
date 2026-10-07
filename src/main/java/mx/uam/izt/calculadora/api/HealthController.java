package mx.uam.izt.calculadora.api;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Salud", description = "Estado de disponibilidad de la API")
public class HealthController {

	@GetMapping("/health")
	@Operation(summary = "Comprobar salud", description = "Indica si la API esta disponible.")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
