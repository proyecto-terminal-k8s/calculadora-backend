package mx.uam.izt.calculadora.service;

public class DivisionPorCeroException extends RuntimeException {

	public DivisionPorCeroException() {
		super("No es posible dividir entre cero");
	}
}
