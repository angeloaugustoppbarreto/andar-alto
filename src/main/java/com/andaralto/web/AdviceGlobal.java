package com.andaralto.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.security.UsuarioDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Código que vale para TODOS os controllers (@ControllerAdvice).
 */
@ControllerAdvice
public class AdviceGlobal {

    /**
     * Deixa o usuário logado disponível em todas as páginas como ${usuarioLogado}
     * (é null para visitantes). O menu do topo usa isso para saber o que mostrar.
     */
    @ModelAttribute("usuarioLogado")
    public UsuarioDetails usuarioLogado(@AuthenticationPrincipal UsuarioDetails usuario) {
        return usuario;
    }

    /**
     * Quando um service recusa algo por regra do negócio (ex.: "este anúncio já foi
     * vendido"), voltamos para a página anterior mostrando a mensagem em vermelho.
     * A mensagem viaja como "flash attribute": existe só até a próxima página abrir.
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public String regraDeNegocio(RegraDeNegocioException e, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", e.getMessage());
        return Navegacao.voltar(request, "/");
    }
}
