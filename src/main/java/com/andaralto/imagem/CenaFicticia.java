package com.andaralto.imagem;

import java.util.Locale;
import java.util.Optional;

/**
 * Os quatro "ambientes" que o sistema desenha para cada anúncio.
 * Todas as imagens do site são ILUSTRAÇÕES FICTÍCIAS geradas por código
 * (veja {@link GeradorSvg}): não há fotos reais nem arquivos de imagem no projeto.
 *
 * <p>A mesma "semente" (um número) em todas as cenas faz o anúncio ter
 * uma identidade visual coerente: mesma paleta de cores e mesmo horário do dia.
 */
public enum CenaFicticia {
    FACHADA("Fachada do edifício"),
    SALA("Sala de estar"),
    QUARTO("Quarto"),
    COZINHA("Cozinha");

    private final String legenda;

    CenaFicticia(String legenda) {
        this.legenda = legenda;
    }

    public String getLegenda() {
        return legenda;
    }

    /** Nome usado na URL: "fachada", "sala", "quarto" ou "cozinha". */
    public String getSlug() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Endereço da imagem: o {@code ImagemController} responde a ele desenhando o SVG na hora. */
    public String url(long semente) {
        return "/imagens/ficticias/" + getSlug() + "/" + semente + ".svg";
    }

    public static Optional<CenaFicticia> porSlug(String slug) {
        for (CenaFicticia cena : values()) {
            if (cena.getSlug().equals(slug)) {
                return Optional.of(cena);
            }
        }
        return Optional.empty();
    }
}
