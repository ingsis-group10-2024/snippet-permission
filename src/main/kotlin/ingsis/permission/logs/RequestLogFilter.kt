package ingsis.permission.logs

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// Registra info sobre las solicitudes HTTP y sus responses
// Se aplica este filtro a cada request entrante
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
class RequestLogFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response) // Pasa request y response al siguiente filtro en la cadena
        } finally {
            logger.info(
                "Request: ${request.method} ${request.requestURI} - Response: ${response.status}",
            )
        }
    }
}
