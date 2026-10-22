package com.andaralto.model;

/** Situação de uma proposta de compra. */
public enum StatusProposta {
    PENDENTE("Aguardando resposta", "analise"),
    ACEITA("Aceita", "publicado"),
    RECUSADA("Recusada", "rejeitado"),
    CANCELADA("Cancelada", "pausado");

    private final String rotulo;
    private final String css;

    StatusProposta(String rotulo, String css) {
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
