package com.andaralto.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.Usuario;
import com.andaralto.repository.ApartamentoRepository;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.security.UsuarioDetails;

/** Lista de favoritos do comprador. */
@Service
public class FavoritoService {

    private final UsuarioRepository usuarios;
    private final ApartamentoRepository apartamentos;

    public FavoritoService(UsuarioRepository usuarios, ApartamentoRepository apartamentos) {
        this.usuarios = usuarios;
        this.apartamentos = apartamentos;
    }

    /** Favorita se ainda não era; desfavorita se já era. Devolve true quando ficou favoritado. */
    @Transactional
    public boolean alternar(Long usuarioId, Long apartamentoId) {
        Usuario u = usuarios.findById(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
        Apartamento a = apartamentos.findById(apartamentoId)
                .orElseThrow(() -> new NaoEncontradoException("Anúncio não encontrado."));

        if (u.getFavoritos().remove(a)) {
            return false;
        }
        if (a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Só é possível favoritar anúncios publicados.");
        }
        u.getFavoritos().add(a);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Apartamento> listar(Long usuarioId) {
        return usuarios.apartamentosFavoritos(usuarioId);
    }

    /** Ids favoritados pelo usuário logado; vazio para visitantes e para quem não é comprador. */
    @Transactional(readOnly = true)
    public Set<Long> idsDoComprador(UsuarioDetails usuario) {
        if (usuario == null || usuario.getPerfil() != Perfil.COMPRADOR) {
            return Set.of();
        }
        return new HashSet<>(usuarios.idsFavoritos(usuario.getId()));
    }
}
