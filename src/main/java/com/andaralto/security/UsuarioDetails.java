package com.andaralto.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.andaralto.model.Perfil;
import com.andaralto.model.Usuario;

/**
 * "Tradutor" entre o nosso Usuario e o que o Spring Security entende.
 * O perfil vira uma autoridade "ROLE_VENDEDOR", usada nas regras de acesso.
 * Guardamos só o necessário (id, perfil...), nunca a entidade inteira, pois
 * este objeto fica na sessão do usuário.
 */
public class UsuarioDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final Perfil perfil;
    private final boolean ativo;

    public UsuarioDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senhaHash = usuario.getSenhaHash();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }

    // Atalhos usados pelas páginas (ex.: th:if="${usuarioLogado.vendedor}").
    public boolean isAdmin() { return perfil == Perfil.ADMIN; }
    public boolean isVendedor() { return perfil == Perfil.VENDEDOR; }
    public boolean isComprador() { return perfil == Perfil.COMPRADOR; }

    public String getPrimeiroNome() {
        int espaco = nome.indexOf(' ');
        return espaco > 0 ? nome.substring(0, espaco) : nome;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() { return senhaHash; }

    @Override
    public String getUsername() { return email; }

    /** Conta desativada pelo administrador não consegue entrar. */
    @Override
    public boolean isEnabled() { return ativo; }
}
