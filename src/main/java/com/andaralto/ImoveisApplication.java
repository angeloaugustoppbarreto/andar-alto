package com.andaralto;

import com.andaralto.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Ponto de entrada da aplicação. O Spring Boot sobe o servidor web embutido,
 * conecta no banco, roda as migrações do Flyway e publica as páginas.
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ImoveisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImoveisApplication.class, args);
    }
}
