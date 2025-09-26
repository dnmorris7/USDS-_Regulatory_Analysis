package com.usds.regulations.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/auth/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/admin/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/generate/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/download/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/export/**")).permitAll() // Temporarily allow for development
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/public/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .anyRequest().permitAll() // Allow all other requests for now
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow H2 console in iframe
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();

        UserDetails analyst = User.builder()
            .username("analyst")
            .password(passwordEncoder().encode("analyst123"))
            .roles("ANALYST")
            .build();

        UserDetails auditor = User.builder()
            .username("auditor")
            .password(passwordEncoder().encode("auditor123"))
            .roles("AUDITOR")
            .build();

        UserDetails visitor = User.builder()
            .username("visitor")
            .password(passwordEncoder().encode("visitor123"))
            .roles("VISITOR")
            .build();

        return new InMemoryUserDetailsManager(admin, analyst, auditor, visitor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}