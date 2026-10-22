package com.andaralto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.Usuario;

/**
 * Repository = porta de entrada para o banco. Só de estender JpaRepository
 * já ganhamos save, findById, findAll, delete... Os métodos abaixo são
 * "consultas derivadas": o Spring monta o SQL a partir do NOME do método.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByPerfil(Perfil perfil);

    List<Usuario> findAllByOrderByCriadoEmDesc();

    /** Ids dos apartamentos favoritados (para pintar o coração nas listas). */
    @Query("select a.id from Usuario u join u.favoritos a where u.id = :usuarioId")
    List<Long> idsFavoritos(@Param("usuarioId") Long usuarioId);

    @Query("select a from Usuario u join u.favoritos a where u.id = :usuarioId order by a.atualizadoEm desc")
    List<Apartamento> apartamentosFavoritos(@Param("usuarioId") Long usuarioId);

    @Query("select count(a) from Usuario u join u.favoritos a where u.id = :usuarioId")
    long contarFavoritos(@Param("usuarioId") Long usuarioId);
}
