package com.riquitos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/images/**",
                "/icons/**",
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",
                "/api/migracion/**"
            ).permitAll()
        );

        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });

        http.rememberMe(remember -> remember
            .key("claveSecretaRiquitos2026")
            .tokenValiditySeconds(60 * 60 * 24 * 30)
            .alwaysRemember(true)
        );

        return http.build();
    }
    
    @Bean
    public UserDetailsManager userDetailsService() {
        UserDetails operario = User.withUsername("fabrica")
                .password("{noop}1234")
                .roles("OPERARIO")
                .build();

        UserDetails vendedor = User.withUsername("oficina")
                .password("{noop}fghunbvtrfda")
                .roles("VENDEDOR")
                .build();

        UserDetails admin = User.withUsername("juan")
                .password("{noop}juanpe0510")
                .roles("ADMIN", "VENDEDOR", "OPERARIO")
                .build();

        return new InMemoryUserDetailsManager(operario, vendedor, admin);
    }
}
