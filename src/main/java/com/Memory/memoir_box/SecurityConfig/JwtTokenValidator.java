package com.Memory.memoir_box.SecurityConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtTokenValidator extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    public JwtTokenValidator() {
        this.userDetailsService = null; // We'll handle this differently
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("Incoming request to: " + request.getRequestURI());
        System.out.println("Authorization header: " + authHeader);

        if (request.getServletPath().equals("/") || request.getServletPath().startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Missing or invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            System.out.println("JWT token: " + jwt.substring(0, Math.min(10, jwt.length())) + "..."); // Log first 10 chars

            // Validate JWT token
            String email = JwtProvider.getEmailFromJwtToken(jwt);
            
            if (email != null && !email.isEmpty()) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("Authentication set for user: " + email);
                filterChain.doFilter(request, response);
            } else {
                System.out.println("Invalid token - no email found");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            }

        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }
}