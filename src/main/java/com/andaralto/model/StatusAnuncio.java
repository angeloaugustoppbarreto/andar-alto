package com.andaralto.model;

/**
 * Ciclo de vida de um anúncio.
 * <pre>
 * EM_ANALISE --(admin aprova)--> PUBLICADO --(vendedor aceita proposta)--> RESERVADO --(conclui)--> VENDIDO
 *     |                              |  ^                                        |
 *     +--(admin rejeita)--> REJEITADO|  +--(vendedor reativa) PAUSADO            +--(cancela)--> PUBLICADO
 * </pre>
 * O campo "css" é usado nas páginas para colorir o selo de status.
 */
public enum StatusAnuncio {
    EM_ANALISE("Em análise", "analise"),
    PUBLICADO("Publicado", "publicado"),
    REJEITADO("Rejeitado", "rejeitado"),
    RESERVADO("Reservado", "reservado"),
    VENDIDO("Vendido", "vendido"),
    PAUSADO("Pausado", "pausado");

    private final String rotulo;
    private final String css;

    StatusAnuncio(String rotulo, String css) {
        this.rotulo = rotulo;
        this.css = css;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getCss() {
        return css;
    }
}
