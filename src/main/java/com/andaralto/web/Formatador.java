package com.andaralto.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * Formata valores no padrão brasileiro para as páginas (R$ 1.250.000,00 e 31/12/2026).
 *
 * <p>É um "bean" com o nome {@code fmt}: nas páginas Thymeleaf chamamos assim:
 * {@code ${@fmt.moeda(apartamento.preco)}}. Montamos os símbolos (ponto de milhar e
 * vírgula decimal) à mão para o resultado ser igual em qualquer computador,
 * independente do idioma configurado no sistema operacional.
 */
@Component("fmt")
public class Formatador {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String VAZIO = "—";

    private final DecimalFormatSymbols simbolos;

    public Formatador() {
        this.simbolos = new DecimalFormatSymbols();
        this.simbolos.setGroupingSeparator('.');
        this.simbolos.setDecimalSeparator(',');
    }

    /** Preço de imóvel, sem centavos: {@code R$ 890.000}. */
    public String moeda(BigDecimal valor) {
        return formatar(valor, "#,##0");
    }

    /** Valor com centavos: {@code R$ 17.800,00}. Usado em comissões e totais. */
    public String moedaComCentavos(BigDecimal valor) {
        return formatar(valor, "#,##0.00");
    }

    /** Porcentagem com uma casa decimal: {@code 2,5%}. */
    public String percentual(BigDecimal valor) {
        if (valor == null) {
            return VAZIO;
        }
        DecimalFormat formato = new DecimalFormat("#,##0.0", simbolos);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato.format(valor) + "%";
    }

    public String data(LocalDateTime data) {
        return data == null ? VAZIO : DATA.format(data);
    }

    public String dataHora(LocalDateTime data) {
        return data == null ? VAZIO : DATA_HORA.format(data);
    }

    private String formatar(BigDecimal valor, String padrao) {
        if (valor == null) {
            return VAZIO;
        }
        // DecimalFormat não é seguro para uso simultâneo por várias threads: criamos um por chamada.
        DecimalFormat formato = new DecimalFormat(padrao, simbolos);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return "R$ " + formato.format(valor);
    }
}
