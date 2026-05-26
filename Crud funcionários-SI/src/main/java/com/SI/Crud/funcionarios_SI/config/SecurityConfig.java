package com.SI.Crud.funcionarios_SI.config;

import com.SI.Crud.funcionarios_SI.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // público
                        .requestMatchers("/", "/login", "/register",
                                "/css/**", "/js/**", "/images/**",
                                "/api/auth/**").permitAll()

                        // só ADMIN pode excluir permanente e gerir utilizadores
                        .requestMatchers(HttpMethod.DELETE, "/api/trash/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")

                        // lixeira: ADMIN e RH
                        .requestMatchers("/api/trash/**").hasAnyRole("ADMIN", "RH")

                        // salário: ADMIN e RH
                        .requestMatchers(HttpMethod.PATCH, "/api/employees/*/salary")
                                .hasAnyRole("ADMIN", "RH")

                        // dashboard e audit: ADMIN e RH
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "RH", "GESTOR")
                        .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "RH")

                        // todo o resto precisa estar autenticado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}