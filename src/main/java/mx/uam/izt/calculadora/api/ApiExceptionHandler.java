package mx.uam.izt.calculadora.api;

import java.net.URI;

import mx.uam.izt.calculadora.service.DivisionPorCeroException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DivisionPorCeroException.class)
	public ProblemDetail manejarDivisionPorCero(DivisionPorCeroException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Operacion no valida");
		problem.setType(URI.create("https://calculadora.local/errores/division-por-cero"));
		return problem;
	}
}
