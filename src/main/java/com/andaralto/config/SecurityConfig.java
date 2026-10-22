package com.andaralto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.andaralto.security.LoginSuccessHandler;

/**
 * Quem pode acessar o quê. As regras são lidas de cima para baixo e a
 * primeira que combinar vence - por isso as mais específicas vêm antes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, LoginSuccessHandler sucesso) throws Exception {
        http
            .authorizeHttpRequests(regras -> regras
                // Arquivos estáticos, imagens e páginas públicas
                .requestMatchers("/css/**", "/js/**", "/imagens/**", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/", "/apartamentos", "/apartamentos/**", "/login", "/cadastro").permitAll()
                // Áreas por perfil
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/vendedor/**").hasRole("VENDEDOR")
                .requestMatchers("/comprador/**").hasRole("COMPRADOR")
                // Todo o resto exige apenas estar logado (ex.: /conta)
                .anyRequest().authenticated())
            .formLogin(login -> login
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(sucesso)
                .failureUrl("/login?erro")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/?saiu")
                .permitAll());
        // CSRF fica LIGADO (padrão): todo formulário POST precisa do token
        // que o Thymeleaf insere sozinho quando usamos th:action.
        return http.build();
    }

    /** BCrypt: hash com "sal" aleatório e lento de propósito (dificulta ataques). */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
