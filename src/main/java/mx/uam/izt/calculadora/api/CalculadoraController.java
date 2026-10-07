package mx.uam.izt.calculadora.api;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mx.uam.izt.calculadora.service.CalculadoraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ProblemDetail;

@RestController
@RequestMapping("/api/v1/calculadora")
@Tag(name = "Calculadora", description = "Operaciones aritmeticas con numeros decimales")
public class CalculadoraController {

	private static final Logger logger = LoggerFactory.getLogger(CalculadoraController.class);

	private final CalculadoraService calculadoraService;

	public CalculadoraController(CalculadoraService calculadoraService) {
		this.calculadoraService = calculadoraService;
	}

	@GetMapping("/sumar")
	@Operation(summary = "Sumar", description = "Suma dos numeros decimales.")
	@ApiResponses(@ApiResponse(
			responseCode = "400",
			description = "Falta un parametro o alguno no es un numero decimal valido",
			content = @Content(
					mediaType = "application/problem+json",
					schema = @Schema(implementation = ProblemDetail.class))))
	public OperacionResponse sumar(
			@Parameter(description = "Primer sumando", example = "2.5")
			@RequestParam BigDecimal numero1,
			@Parameter(description = "Segundo sumando", example = "3.25")
			@RequestParam BigDecimal numero2) {
		return ejecutar("sumar", numero1, numero2, calculadoraService::sumar);
	}

	@GetMapping("/restar")
	@Operation(summary = "Restar", description = "Resta el segundo numero al primero.")
	@ApiResponses(@ApiResponse(
			responseCode = "400",
			description = "Falta un parametro o alguno no es un numero decimal valido",
			content = @Content(
					mediaType = "application/problem+json",
					schema = @Schema(implementation = ProblemDetail.class))))
	public OperacionResponse restar(
			@Parameter(description = "Minuendo", example = "10")
			@RequestParam BigDecimal numero1,
			@Parameter(description = "Sustraendo", example = "4")
			@RequestParam BigDecimal numero2) {
		return ejecutar("restar", numero1, numero2, calculadoraService::restar);
	}

	@GetMapping("/multiplicar")
	@Operation(summary = "Multiplicar", description = "Multiplica dos numeros decimales.")
	@ApiResponses(@ApiResponse(
			responseCode = "400",
			description = "Falta un parametro o alguno no es un numero decimal valido",
			content = @Content(
					mediaType = "application/problem+json",
					schema = @Schema(implementation = ProblemDetail.class))))
	public OperacionResponse multiplicar(
			@Parameter(description = "Primer factor", example = "3")
			@RequestParam BigDecimal numero1,
			@Parameter(description = "Segundo factor", example = "4")
			@RequestParam BigDecimal numero2) {
		return ejecutar("multiplicar", numero1, numero2, calculadoraService::multiplicar);
	}

	@GetMapping("/dividir")
	@Operation(
			summary = "Dividir",
			description = "Divide el primer numero entre el segundo con precision DECIMAL128.")
	@ApiResponses({
		@ApiResponse(
				responseCode = "400",
				description = "El divisor es cero, falta un parametro o alguno no es un numero decimal valido",
				content = @Content(
						mediaType = "application/problem+json",
						schema = @Schema(implementation = ProblemDetail.class)))
	})
	public OperacionResponse dividir(
			@Parameter(description = "Dividendo", example = "10")
			@RequestParam BigDecimal numero1,
			@Parameter(description = "Divisor (no puede ser cero)", example = "4")
			@RequestParam BigDecimal numero2) {
		return ejecutar("dividir", numero1, numero2, calculadoraService::dividir);
	}

	private OperacionResponse ejecutar(
			String operacion,
			BigDecimal numero1,
			BigDecimal numero2,
			Operacion operador) {
		BigDecimal resultado = operador.aplicar(numero1, numero2);
		logger.info("Operacion calculada: operacion={}, numero1={}, numero2={}, resultado={}",
				operacion, numero1, numero2, resultado);
		return new OperacionResponse(operacion, numero1, numero2, resultado);
	}

	@FunctionalInterface
	private interface Operacion {
		BigDecimal aplicar(BigDecimal numero1, BigDecimal numero2);
	}
}
