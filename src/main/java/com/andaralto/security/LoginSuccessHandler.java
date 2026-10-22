package com.andaralto.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Depois do login: se a pessoa tentou abrir uma página protegida antes,
 * volta para ela; senão, vai para o painel do seu perfil.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SavedRequest salva = requestCache.getRequest(request, response);
        String destino = (salva != null) ? salva.getRedirectUrl() : destinoPorPerfil(authentication);

        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
        requestCache.removeRequest(request, response);
        redirectStrategy.sendRedirect(request, response, destino);
    }

    private String destinoPorPerfil(Authentication auth) {
        boolean admin = temPapel(auth, "ROLE_ADMIN");
        boolean vendedor = temPapel(auth, "ROLE_VENDEDOR");
        if (admin) {
            return "/admin";
        }
        return vendedor ? "/vendedor" : "/";
    }

    private boolean temPapel(Authentication auth, String papel) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(papel));
    }
}
