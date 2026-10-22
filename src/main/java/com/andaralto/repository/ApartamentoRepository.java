package com.andaralto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.andaralto.model.Apartamento;
import com.andaralto.model.StatusAnuncio;

/**
 * JpaSpecificationExecutor permite montar filtros dinâmicos (cidade, preço,
 * quartos...) combinando só os que o usuário preencheu. Veja ApartamentoService.
 */
public interface ApartamentoRepository extends JpaRepository<Apartamento, Long>,
        JpaSpecificationExecutor<Apartamento> {

    List<Apartamento> findByVendedorIdOrderByAtualizadoEmDesc(Long vendedorId);

    List<Apartamento> findAllByOrderByAtualizadoEmDesc();

    List<Apartamento> findByStatusOrderByAtualizadoEmDesc(StatusAnuncio status);

    List<Apartamento> findTop6ByStatusOrderByDestaqueDescCriadoEmDesc(StatusAnuncio status);

    long countByStatus(StatusAnuncio status);

    long countByVendedorIdAndStatus(Long vendedorId, StatusAnuncio status);

    @Query("select distinct a.cidade from Apartamento a where a.status = :status order by a.cidade")
    List<String> cidadesComStatus(@Param("status") StatusAnuncio status);
}
