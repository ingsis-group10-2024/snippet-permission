package ingsis.permission.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

// Configura la seguridad para un servidor de recursos que utiliza tokens JWT emitidos por Auth0
@Configuration
@EnableWebSecurity
class OAuth2ResourceServerSecurityConfiguration(
    @Value("\${auth0.audience}") val audience: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") val issuer: String,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers(GET, "/permission/filetypes")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(DELETE, "/rules")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(DELETE, "/rules/*")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(GET, "/rules")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(GET, "/rules/*")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(POST, "/rules")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(POST, "/rules/*")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(GET, "/permission")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(GET, "/permission/*")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(POST, "/permission")
                    .hasAuthority("SCOPE_create:snippet")
                    .requestMatchers(POST, "/permission/snippets/share/*")
                    .hasAuthority("SCOPE_create:snippet")
                    .requestMatchers(GET, "/permission/testcases")
                    .hasAuthority("SCOPE_read:snippet")
                    .requestMatchers(POST, "/permission/testcases")
                    .hasAuthority("SCOPE_create:snippet")
                    .requestMatchers(GET, "/permission/users/*")
                    .hasAuthority("SCOPE_read:snippet")
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer {
                it.jwt { }
            }.cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        config.applyPermitDefaultValues()
        config.allowCredentials = true
        config.allowedOrigins = listOf("http://localhost:5173", "https://printscript-group10.duckdns.org")
        config.allowedHeaders = listOf("authorization", "content-type", "*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        source.registerCorsConfiguration("/**", config)
        return source
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuer).build()
        val audienceValidator: OAuth2TokenValidator<Jwt> = AudienceValidator(audience)
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuer)
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)
        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }
}
