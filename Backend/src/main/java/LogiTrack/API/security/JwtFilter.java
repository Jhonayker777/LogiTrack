package LogiTrack.API.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Formato esperado: "Bearer <token>"
        if (header != null && header.startsWith("Bearer ")) {
            Claims claims = jwtService.validateToken(header.substring(7));

            if (claims != null) {
                String username = claims.getSubject();
                String rol = claims.get("rol", String.class);

                // Spring espera el prefijo ROLE_ para hasRole("ADMIN")
                List<SimpleGrantedAuthority> authorities = rol == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + rol));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, authorities));
            }
        }
        // Si no hay token o es inválido, se continúa sin autenticar: el entry point responde 401
        chain.doFilter(request, response);
    }
}
