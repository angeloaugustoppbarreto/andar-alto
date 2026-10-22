package com.andaralto.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.andaralto.model.Proposta;
import com.andaralto.model.StatusProposta;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    List<Proposta> findByCompradorIdOrderByCriadoEmDesc(Long compradorId);

    List<Proposta> findTop5ByCompradorIdOrderByCriadoEmDesc(Long compradorId);

    /** Propostas recebidas pelos anúncios de um vendedor. */
    List<Proposta> findByApartamentoVendedorIdOrderByCriadoEmDesc(Long vendedorId);

    List<Proposta> findByApartamentoIdAndStatus(Long apartamentoId, StatusProposta status);

    Optional<Proposta> findFirstByApartamentoIdAndCompradorIdAndStatusIn(
            Long apartamentoId, Long compradorId, Collection<StatusProposta> status);

    boolean existsByApartamentoId(Long apartamentoId);

    long countByStatus(StatusProposta status);

    long countByCompradorIdAndStatus(Long compradorId, StatusProposta status);

    long countByApartamentoVendedorIdAndStatus(Long vendedorId, StatusProposta status);
}
