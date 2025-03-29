package com.journal.journalbackend.config.SecurityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService users() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("admin")
                        .password(encoder.encode("admin")) // Encode with BCrypt
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll() // Allow Swagger UI
                        .requestMatchers("/api/users/register").permitAll() // Allow registration without authentication
                        .anyRequest().authenticated() // Secure all other endpoints
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // Default login endpoint
                        .defaultSuccessUrl("/api/auth/register") // Redirect after successful login
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // Default logout endpoint
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true); // Allow semicolons in URLs
        return firewall;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
