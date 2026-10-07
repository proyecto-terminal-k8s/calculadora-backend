package mx.uam.izt.calculadora.observability;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		long start = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
		} finally {
			long durationMs = (System.nanoTime() - start) / 1_000_000;
			logger.info("Peticion HTTP: metodo={}, ruta={}, estado={}, duracion_ms={}",
					request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
		}
	}
}
