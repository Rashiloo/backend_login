package com.login.login_backend.config;



import com.login.login_backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



import jakarta.servlet.http.HttpServletRequest;



import java.util.Arrays;



@Configuration

public class SecurityConfig {



    private final JwtAuthenticationFilter jwtAuthenticationFilter;



    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;

    }



    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        .anyRequest().permitAll()

                );



        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();

    }



    @Bean

    public CorsConfigurationSource corsConfigurationSource() {

        return new UrlBasedCorsConfigurationSource() {

            @Override

            public org.springframework.web.cors.CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();

                config.setAllowedOrigins(Arrays.asList("https://frontend-login-m0xf.onrender.com", "http://localhost:4200"));

                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                config.setAllowedHeaders(Arrays.asList("*"));

                config.setAllowCredentials(true);

                return config;

            }

        };

    }



    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }



    @Bean

    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();

    }

}



