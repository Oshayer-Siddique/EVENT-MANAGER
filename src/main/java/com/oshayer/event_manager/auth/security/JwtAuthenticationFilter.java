package com.oshayer.event_manager.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;
        String email = null;



        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            System.out.println("JWT token received: " + token);

            email = jwtTokenProvider.getEmailFromJwt(token);
            System.out.println("Email from JWT: " + email);

        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtTokenProvider.validateToken(token)) {
                System.out.println("Token is valid. Loading user...");

                var userDetails = customUserDetailsService.loadUserByUsername(email);
                System.out.println("User found: " + userDetails.getUsername());
                System.out.println("Authorities: " + userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
