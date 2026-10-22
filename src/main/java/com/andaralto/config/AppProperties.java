package com.andaralto.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configurações próprias do negócio, lidas do application.properties
 * (tudo que começa com "app."). Um record é uma classe imutável e enxuta.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("2.0") BigDecimal comissaoPercentual,
        @DefaultValue Demo demo,
        @DefaultValue Admin admin) {

    public record Demo(@DefaultValue("false") boolean dados) {
    }

    public record Admin(@DefaultValue("admin@andaralto.com") String email,
                        @DefaultValue("admin123") String senha) {
    }
}
