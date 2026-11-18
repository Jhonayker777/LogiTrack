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

    // Cifrado de contraseñas (lo usan AuthService, UsuarioService y SolicitudRegistroService)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // API REST con JWT (stateless): no se usa CSRF ni sesiones
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint) // 401 JSON
                        .accessDeniedHandler(accessDeniedHandler))          // 403 JSON
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Perfil propio: cualquier usuario autenticado puede consultar y editar sus datos personales
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/me").hasAnyRole("ADMIN", "EMPLEADO")

                        // Solo ADMIN: administración de usuarios, solicitudes de registro y auditoría
                        .requestMatchers("/api/usuarios/**", "/api/solicitudes/**", "/api/auditorias/**").hasRole("ADMIN")

                        // ADMIN y EMPLEADO: consultar todo y registrar movimientos
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/api/movimientos").hasAnyRole("ADMIN", "EMPLEADO")

                        // Cualquier otra escritura (crear/editar/eliminar bodegas, productos, stock...) solo ADMIN
                        .requestMatchers("/api/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                // El filtro JWT se ejecuta antes del filtro de usuario/contraseña
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // CORS para el frontend (carpeta frontend/ servida con Live Server u otro servidor estático)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://127.0.0.1:5501", "http://localhost:5500",
                "http://127.0.0.1:3000", "http://localhost:3000",
                "http://127.0.0.1:8081", "http://localhost:8081"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
