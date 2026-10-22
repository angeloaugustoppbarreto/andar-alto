package com.andaralto.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.imagem.CenaFicticia;
import com.andaralto.model.Apartamento;
import com.andaralto.model.ImagemApartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.Proposta;
import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.StatusProposta;
import com.andaralto.model.Usuario;
import com.andaralto.repository.ApartamentoRepository;
import com.andaralto.repository.ComodidadeRepository;
import com.andaralto.repository.PropostaRepository;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.security.UsuarioDetails;
import com.andaralto.web.form.ApartamentoForm;

import jakarta.persistence.criteria.Predicate;

/** Regras dos anúncios: busca, cadastro, edição e moderação. */
@Service
public class ApartamentoService {

    private static final int POR_PAGINA = 9;

    private final ApartamentoRepository apartamentos;
    private final ComodidadeRepository comodidades;
    private final UsuarioRepository usuarios;
    private final PropostaRepository propostas;

    public ApartamentoService(ApartamentoRepository apartamentos, ComodidadeRepository comodidades,
                              UsuarioRepository usuarios, PropostaRepository propostas) {
        this.apartamentos = apartamentos;
        this.comodidades = comodidades;
        this.usuarios = usuarios;
        this.propostas = propostas;
    }

    // =====================================================================
    // Consultas públicas
    // =====================================================================

    @Transactional(readOnly = true)
    public Page<Apartamento> buscar(FiltroBusca filtro, int pagina) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 1) - 1, POR_PAGINA, ordenacao(filtro.getOrdem()));
        return apartamentos.findAll(especificacao(filtro), pageable);
    }

    @Transactional(readOnly = true)
    public List<Apartamento> destaques() {
        return apartamentos.findTop6ByStatusOrderByDestaqueDescCriadoEmDesc(StatusAnuncio.PUBLICADO);
    }

    @Transactional(readOnly = true)
    public List<String> cidades() {
        return apartamentos.cidadesComStatus(StatusAnuncio.PUBLICADO);
    }

    @Transactional(readOnly = true)
    public long totalPublicados() {
        return apartamentos.countByStatus(StatusAnuncio.PUBLICADO);
    }

    /**
     * Página de detalhe. Anúncios publicados, reservados e vendidos são
     * públicos; os demais (em análise, rejeitados, pausados) só o dono
     * e o administrador enxergam. Para os outros, finge que não existe (404).
     */
    @Transactional(readOnly = true)
    public Apartamento buscarVisivel(Long id, UsuarioDetails quem) {
        Apartamento a = buscarPorId(id);
        StatusAnuncio s = a.getStatus();
        if (s == StatusAnuncio.PUBLICADO || s == StatusAnuncio.RESERVADO || s == StatusAnuncio.VENDIDO) {
            return a;
        }
        boolean dono = quem != null && a.getVendedor().getId().equals(quem.getId());
        boolean admin = quem != null && quem.getPerfil() == Perfil.ADMIN;
        if (dono || admin) {
            return a;
        }
        throw new NaoEncontradoException("Anúncio não encontrado.");
    }

    @Transactional(readOnly = true)
    public Apartamento buscarPorId(Long id) {
        return apartamentos.findById(id).orElseThrow(() -> new NaoEncontradoException("Anúncio não encontrado."));
    }

    // =====================================================================
    // Área do vendedor
    // =====================================================================

    @Transactional(readOnly = true)
    public List<Apartamento> listarDoVendedor(Long vendedorId) {
        return apartamentos.findByVendedorIdOrderByAtualizadoEmDesc(vendedorId);
    }

    @Transactional(readOnly = true)
    public Apartamento buscarDoVendedor(Long id, Long vendedorId) {
        return doVendedor(id, vendedorId);
    }

    @Transactional
    public Apartamento criar(ApartamentoForm form, Long vendedorId) {
        Usuario vendedor = usuarios.findById(vendedorId)
                .orElseThrow(() -> new NaoEncontradoException("Vendedor não encontrado."));
        if (vendedor.getPerfil() != Perfil.VENDEDOR) {
            throw new RegraDeNegocioException("Somente vendedores podem criar anúncios.");
        }
        Apartamento a = new Apartamento();
        a.setVendedor(vendedor);
        preencher(a, form);
        a.setStatus(StatusAnuncio.EM_ANALISE); // todo anúncio novo passa pela análise do administrador

        // Imagens FICTÍCIAS: o sistema gera 4 ilustrações (fachada, sala, quarto e cozinha)
        // com a mesma "semente", então combinam entre si.
        long semente = ThreadLocalRandom.current().nextLong(1, 1_000_000);
        int ordem = 0;
        for (CenaFicticia cena : CenaFicticia.values()) {
            a.adicionarImagem(new ImagemApartamento(cena.url(semente), cena.getLegenda(), ordem++));
        }
        return apartamentos.save(a);
    }

    @Transactional
    public Apartamento atualizar(Long id, ApartamentoForm form, Long vendedorId) {
        Apartamento a = doVendedor(id, vendedorId);
        if (a.getStatus() == StatusAnuncio.RESERVADO || a.getStatus() == StatusAnuncio.VENDIDO) {
            throw new RegraDeNegocioException("Anúncios reservados ou vendidos não podem ser editados.");
        }
        preencher(a, form);
        // Anúncio rejeitado e corrigido volta para nova análise.
        if (a.getStatus() == StatusAnuncio.REJEITADO) {
            a.setStatus(StatusAnuncio.EM_ANALISE);
            a.setMotivoRejeicao(null);
        }
        return a; // sem save(): o Hibernate grava a mudança ao final da transação
    }

    @Transactional
    public void pausar(Long id, Long vendedorId) {
        Apartamento a = doVendedor(id, vendedorId);
        if (a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Só é possível pausar anúncios publicados.");
        }
        a.setStatus(StatusAnuncio.PAUSADO);
        a.setDestaque(false);
    }

    @Transactional
    public void reativar(Long id, Long vendedorId) {
        Apartamento a = doVendedor(id, vendedorId);
        if (a.getStatus() != StatusAnuncio.PAUSADO) {
            throw new RegraDeNegocioException("Só é possível reativar anúncios pausados.");
        }
        a.setStatus(StatusAnuncio.PUBLICADO);
    }

    @Transactional
    public void excluir(Long id, Long vendedorId) {
        Apartamento a = doVendedor(id, vendedorId);
        if (a.getStatus() == StatusAnuncio.RESERVADO || a.getStatus() == StatusAnuncio.VENDIDO) {
            throw new RegraDeNegocioException("Anúncios reservados ou vendidos não podem ser excluídos.");
        }
        if (propostas.existsByApartamentoId(id)) {
            throw new RegraDeNegocioException(
                    "Este anúncio já recebeu propostas e não pode ser excluído. Pause-o para tirá-lo do ar.");
        }
        apartamentos.delete(a);
    }

    // =====================================================================
    // Área do administrador (moderação)
    // =====================================================================

    @Transactional(readOnly = true)
    public List<Apartamento> listarParaAdmin(StatusAnuncio status) {
        return status == null
                ? apartamentos.findAllByOrderByAtualizadoEmDesc()
                : apartamentos.findByStatusOrderByAtualizadoEmDesc(status);
    }

    @Transactional
    public void aprovar(Long id) {
        Apartamento a = buscarPorId(id);
        if (a.getStatus() != StatusAnuncio.EM_ANALISE) {
            throw new RegraDeNegocioException("Só é possível aprovar anúncios que estão em análise.");
        }
        a.setStatus(StatusAnuncio.PUBLICADO);
        a.setMotivoRejeicao(null);
    }

    @Transactional
    public void rejeitar(Long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Explique o motivo da rejeição para o vendedor corrigir o anúncio.");
        }
        if (motivo.trim().length() > 500) {
            throw new RegraDeNegocioException("O motivo pode ter até 500 caracteres.");
        }
        Apartamento a = buscarPorId(id);
        if (a.getStatus() != StatusAnuncio.EM_ANALISE && a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Este anúncio não pode ser rejeitado no status atual.");
        }
        a.setStatus(StatusAnuncio.REJEITADO);
        a.setMotivoRejeicao(motivo.trim());
        a.setDestaque(false);
        // Propostas ainda abertas deixam de fazer sentido: o anúncio saiu do ar.
        for (Proposta p : propostas.findByApartamentoIdAndStatus(id, StatusProposta.PENDENTE)) {
            p.setStatus(StatusProposta.RECUSADA);
            p.setResposta("O anúncio foi removido pela moderação.");
            p.setRespondidoEm(java.time.LocalDateTime.now());
        }
    }

    @Transactional
    public void alternarDestaque(Long id) {
        Apartamento a = buscarPorId(id);
        if (a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Só anúncios publicados podem ficar em destaque.");
        }
        a.setDestaque(!a.isDestaque());
    }

    // =====================================================================
    // Apoio
    // =====================================================================

    /** Garante que o anúncio existe E pertence ao vendedor logado. */
    private Apartamento doVendedor(Long id, Long vendedorId) {
        Apartamento a = buscarPorId(id);
        if (!a.getVendedor().getId().equals(vendedorId)) {
            throw new AccessDeniedException("Este anúncio pertence a outro vendedor.");
        }
        return a;
    }

    private void preencher(Apartamento a, ApartamentoForm f) {
        a.setTitulo(f.getTitulo().trim());
        a.setDescricao(f.getDescricao().trim());
        a.setPreco(f.getPreco());
        a.setValorCondominio(f.getValorCondominio());
        a.setAreaM2(f.getAreaM2());
        a.setQuartos(f.getQuartos());
        a.setSuites(f.getSuites());
        a.setBanheiros(f.getBanheiros());
        a.setVagas(f.getVagas());
        a.setAndar(f.getAndar());
        a.setCep(vazioParaNulo(f.getCep()));
        a.setLogradouro(f.getLogradouro().trim());
        a.setNumero(vazioParaNulo(f.getNumero()));
        a.setBairro(f.getBairro().trim());
        a.setCidade(f.getCidade().trim());
        a.setUf(f.getUf().trim().toUpperCase());
        List<Long> ids = f.getComodidadeIds() == null ? List.of() : f.getComodidadeIds();
        a.setComodidades(new LinkedHashSet<>(comodidades.findAllById(ids)));
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    private Sort ordenacao(String ordem) {
        String o = ordem == null ? "" : ordem;
        return switch (o) {
            case "menor-preco" -> Sort.by("preco").ascending();
            case "maior-preco" -> Sort.by("preco").descending();
            case "maior-area" -> Sort.by("areaM2").descending();
            // padrão: destaques primeiro, depois os mais recentes
            default -> Sort.by(Sort.Order.desc("destaque"), Sort.Order.desc("criadoEm"));
        };
    }

    /**
     * Specification = filtro montado em tempo de execução. Só adicionamos uma
     * condição se a pessoa preencheu aquele campo. O Spring Data transforma
     * isso em SQL com WHERE ... AND ...
     */
    private Specification<Apartamento> especificacao(FiltroBusca f) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            condicoes.add(cb.equal(root.get("status"), StatusAnuncio.PUBLICADO));

            if (temTexto(f.getCidade())) {
                condicoes.add(cb.equal(cb.lower(root.<String>get("cidade")), f.getCidade().trim().toLowerCase()));
            }
            if (temTexto(f.getQ())) {
                String padrao = "%" + f.getQ().trim().toLowerCase() + "%";
                condicoes.add(cb.or(
                        cb.like(cb.lower(root.<String>get("titulo")), padrao),
                        cb.like(cb.lower(root.<String>get("bairro")), padrao),
                        cb.like(cb.lower(root.<String>get("cidade")), padrao),
                        cb.like(cb.lower(root.<String>get("logradouro")), padrao)));
            }
            BigDecimal min = f.getPrecoMin();
            BigDecimal max = f.getPrecoMax();
            if (min != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("preco"), min));
            }
            if (max != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("preco"), max));
            }
            if (f.getQuartos() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<Integer>get("quartos"), f.getQuartos()));
            }
            if (f.getVagas() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<Integer>get("vagas"), f.getVagas()));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
