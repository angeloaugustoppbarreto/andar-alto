package com.andaralto.web;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

/** Pequenos auxiliares de navegação usados pelos controllers. */
final class Navegacao {

    private Navegacao() {
    }

    /**
     * Devolve "redirect:/caminho" para a página de onde o usuário veio (cabeçalho
     * Referer), mantendo filtros da URL. Assim, depois de aprovar um anúncio dentro
     * de uma lista filtrada, a pessoa volta para a mesma lista.
     *
     * <p>Só o CAMINHO é aproveitado (nunca o endereço completo): isso impede que
     * um link malicioso use o nosso site para mandar o usuário para outro domínio.
     */
    static String voltar(HttpServletRequest request, String padrao) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                String caminho = uri.getRawPath();
                if (caminho != null && caminho.startsWith("/") && !caminho.startsWith("//")) {
                    String consulta = uri.getRawQuery();
                    return "redirect:" + caminho + (consulta == null ? "" : "?" + consulta);
                }
            } catch (IllegalArgumentException e) {
                // Referer mal formado: cai no endereço padrão abaixo.
            }
        }
        return "redirect:" + padrao;
    }
}
