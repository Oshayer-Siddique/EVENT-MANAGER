package com.oshayer.event_manager.shared.security;

import com.oshayer.event_manager.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chain 1: Public APIs — no authentication, no JWT filter
     * This includes Venues, Seat Layouts, Events, Test, and Auth.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain openChain(HttpSecurity http) throws Exception {
        http
                // Apply only to these public routes
                .securityMatcher(
                        "/api/venues/**",
                        "/api/seat-layouts/**",
                        "/api/events/**",
                        "/api/test/**",
                        "/api/auth/**",
                        "/api/business-organizations/**",
                        "/api/artists/**",
                        "/api/sponsors/**"
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        // no JWT filter added here
        return http.build();
    }

    /**
     * Chain 2: Protected APIs — require JWT authentication
     */
    @Bean
    @Order(2)
    public SecurityFilterChain protectedChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
