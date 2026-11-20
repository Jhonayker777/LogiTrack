
package LogiTrack.API.config;

import LogiTrack.API.security.JwtFilter;
import LogiTrack.API.security.RestAccessDeniedHandler;
import LogiTrack.API.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Procesar CORS antes de la autenticación JWT.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth

                        // Permitir solicitudes preliminares CORS.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints públicos.
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/recuperar-password"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()

                        // Perfil propio.
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me")
                        .hasAnyRole("ADMIN", "EMPLEADO")

                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/me")
                        .hasAnyRole("ADMIN", "EMPLEADO")

                        // Solo ADMIN.
                        .requestMatchers(
                                "/api/usuarios/**",
                                "/api/solicitudes/**",
                                "/api/auditorias/**"
                        ).hasRole("ADMIN")

                        // ADMIN y EMPLEADO: consultas.
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyRole("ADMIN", "EMPLEADO")

                        // ADMIN y EMPLEADO: registrar movimientos.
                        .requestMatchers(HttpMethod.POST, "/api/movimientos")
                        .hasAnyRole("ADMIN", "EMPLEADO")

                        // Otras escrituras: solo ADMIN.
                        .requestMatchers("/api/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes autorizados.
        config.setAllowedOrigins(List.of(
                "https://jhonayker777.github.io",
                "https://logitrackjqo.netlify.app",

                // Red local.
                "http://192.168.38.36:5500",
                "http://192.168.38.36:5501",

                // Desarrollo local.
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5501",
                "http://127.0.0.1:5501",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8081",
                "http://127.0.0.1:8081"
        ));

        // Métodos HTTP permitidos.
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Encabezados permitidos.
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "ngrok-skip-browser-warning"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
