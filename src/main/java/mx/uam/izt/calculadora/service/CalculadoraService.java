package mx.uam.izt.calculadora.service;

import java.math.BigDecimal;
import java.math.MathContext;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraService {

	private static final MathContext CONTEXTO_DIVISION = MathContext.DECIMAL128;

	public BigDecimal sumar(BigDecimal numero1, BigDecimal numero2) {
		return numero1.add(numero2);
	}

	public BigDecimal restar(BigDecimal numero1, BigDecimal numero2) {
		return numero1.subtract(numero2);
	}

	public BigDecimal multiplicar(BigDecimal numero1, BigDecimal numero2) {
		return numero1.multiply(numero2);
	}

	public BigDecimal dividir(BigDecimal numero1, BigDecimal numero2) {
		if (numero2.compareTo(BigDecimal.ZERO) == 0) {
			throw new DivisionPorCeroException();
		}
		return numero1.divide(numero2, CONTEXTO_DIVISION);
	}
}
