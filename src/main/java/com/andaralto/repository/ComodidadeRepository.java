package com.andaralto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.andaralto.model.Comodidade;

public interface ComodidadeRepository extends JpaRepository<Comodidade, Long> {

    List<Comodidade> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
