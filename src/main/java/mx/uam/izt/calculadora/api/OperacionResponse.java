package mx.uam.izt.calculadora.api;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de una operacion aritmetica")
public record OperacionResponse(
		@Schema(description = "Operacion ejecutada", example = "sumar")
		String operacion,
		@Schema(description = "Primer operando", example = "2.5")
		BigDecimal numero1,
		@Schema(description = "Segundo operando", example = "3.25")
		BigDecimal numero2,
		@Schema(description = "Resultado calculado", example = "5.75")
		BigDecimal resultado) {
}
