package br.com.medisus.api.security;

import br.com.medisus.api.api.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.time.Instant;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            ObjectMapper mapper) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) ->
                                writeSecurityError(mapper, response, HttpStatus.UNAUTHORIZED,
                                        "Authentication is required", request.getRequestURI()))
                        .accessDeniedHandler((request, response, ex) ->
                                writeSecurityError(mapper, response, HttpStatus.FORBIDDEN,
                                        "You do not have permission to access this resource", request.getRequestURI())))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private static void writeSecurityError(ObjectMapper mapper, HttpServletResponse response, HttpStatus status,
                                           String message, String path) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), status.value(),
                status.getReasonPhrase(), message, path, Map.of()));
    }
}
