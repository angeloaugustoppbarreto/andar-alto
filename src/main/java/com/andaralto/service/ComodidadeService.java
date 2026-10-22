package com.andaralto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Comodidade;
import com.andaralto.repository.ComodidadeRepository;

@Service
public class ComodidadeService {

    private final ComodidadeRepository comodidades;

    public ComodidadeService(ComodidadeRepository comodidades) {
        this.comodidades = comodidades;
    }

    @Transactional(readOnly = true)
    public List<Comodidade> listar() {
        return comodidades.findAllByOrderByNomeAsc();
    }

    @Transactional
    public Comodidade criar(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Informe o nome da comodidade.");
        }
        String limpo = nome.trim();
        if (limpo.length() > 60) {
            throw new RegraDeNegocioException("O nome pode ter até 60 caracteres.");
        }
        if (comodidades.existsByNomeIgnoreCase(limpo)) {
            throw new RegraDeNegocioException("Esta comodidade já existe.");
        }
        return comodidades.save(new Comodidade(limpo));
    }

    /** Ao excluir, o próprio banco remove a comodidade dos anúncios (ON DELETE CASCADE). */
    @Transactional
    public void excluir(Long id) {
        Comodidade c = comodidades.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Comodidade não encontrada."));
        comodidades.delete(c);
    }
}
