package config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.time.Instant

@TestConfiguration
class TestSecurityConfig {
    @Bean
    @Primary
    fun mockJwtDecoder(): JwtDecoder {
        return JwtDecoder { jwt() }
    }

    private fun jwt(): Jwt {
        val claims =
            mapOf<String, Any>(
                "sub" to AUTH0ID, // no incluimos firma
            )

        return Jwt(
            AUTH0_TOKEN,
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf<String, Any>("alg" to "none"),  // "none" porque no hay firma
            claims,
        )
    }

    companion object {
        const val AUTH0ID = "test"
        const val AUTH0_TOKEN = "token"
    }
}