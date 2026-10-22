package com.andaralto.model;

/** Os três tipos de usuário do site. */
public enum Perfil {
    COMPRADOR("Comprador"),
    VENDEDOR("Vendedor"),
    ADMIN("Administrador");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
