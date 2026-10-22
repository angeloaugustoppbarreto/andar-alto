package com.andaralto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.config.AppProperties;
import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.Proposta;
import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.StatusProposta;
import com.andaralto.model.Usuario;
import com.andaralto.model.Venda;
import com.andaralto.repository.ApartamentoRepository;
import com.andaralto.repository.PropostaRepository;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.repository.VendaRepository;

/**
 * O "carrinho de compras" de um imóvel é a PROPOSTA: ninguém compra um
 * apartamento com um clique. Este service guarda o fluxo inteiro:
 * <pre>
 * comprador envia proposta -> vendedor aceita (anúncio RESERVADO)
 *                          -> vendedor conclui a venda (anúncio VENDIDO + registro em "vendas")
 * </pre>
 */
@Service
public class PropostaService {

    private static final List<StatusProposta> EM_ANDAMENTO = List.of(StatusProposta.PENDENTE, StatusProposta.ACEITA);

    private final PropostaRepository propostas;
    private final ApartamentoRepository apartamentos;
    private final UsuarioRepository usuarios;
    private final VendaRepository vendas;
    private final AppProperties props;

    public PropostaService(PropostaRepository propostas, ApartamentoRepository apartamentos,
                           UsuarioRepository usuarios, VendaRepository vendas, AppProperties props) {
        this.propostas = propostas;
        this.apartamentos = apartamentos;
        this.usuarios = usuarios;
        this.vendas = vendas;
        this.props = props;
    }

    // ---------------------------------------------------------------- consultas

    @Transactional(readOnly = true)
    public List<Proposta> listarDoComprador(Long compradorId) {
        return propostas.findByCompradorIdOrderByCriadoEmDesc(compradorId);
    }

    @Transactional(readOnly = true)
    public List<Proposta> listarRecebidas(Long vendedorId) {
        return propostas.findByApartamentoVendedorIdOrderByCriadoEmDesc(vendedorId);
    }

    /** Proposta pendente ou aceita do comprador neste apartamento (se houver). */
    @Transactional(readOnly = true)
    public Optional<Proposta> propostaEmAndamento(Long compradorId, Long apartamentoId) {
        return propostas.findFirstByApartamentoIdAndCompradorIdAndStatusIn(apartamentoId, compradorId, EM_ANDAMENTO);
    }

    // ---------------------------------------------------------------- comprador

    @Transactional
    public Proposta enviar(Long apartamentoId, Long compradorId, BigDecimal valor, String mensagem) {
        Apartamento a = apartamentos.findById(apartamentoId)
                .orElseThrow(() -> new NaoEncontradoException("Anúncio não encontrado."));
        if (a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Este anúncio não está recebendo propostas no momento.");
        }
        Usuario comprador = usuarios.findById(compradorId)
                .orElseThrow(() -> new NaoEncontradoException("Comprador não encontrado."));
        if (comprador.getPerfil() != Perfil.COMPRADOR) {
            throw new RegraDeNegocioException("Somente compradores podem enviar propostas.");
        }
        if (propostaEmAndamento(compradorId, apartamentoId).isPresent()) {
            throw new RegraDeNegocioException("Você já tem uma proposta em andamento para este apartamento.");
        }
        Proposta p = new Proposta();
        p.setApartamento(a);
        p.setComprador(comprador);
        p.setValorOfertado(valor);
        p.setMensagem(mensagem == null || mensagem.isBlank() ? null : mensagem.trim());
        return propostas.save(p);
    }

    /** O comprador desiste: vale para proposta pendente e também para reserva já aceita. */
    @Transactional
    public void cancelar(Long propostaId, Long compradorId) {
        Proposta p = buscar(propostaId);
        if (!p.getComprador().getId().equals(compradorId)) {
            throw new AccessDeniedException("Esta proposta pertence a outro comprador.");
        }
        Apartamento a = p.getApartamento();
        if (p.getStatus() == StatusProposta.PENDENTE) {
            p.setStatus(StatusProposta.CANCELADA);
            p.setRespondidoEm(LocalDateTime.now());
        } else if (p.getStatus() == StatusProposta.ACEITA && a.getStatus() == StatusAnuncio.RESERVADO) {
            p.setStatus(StatusProposta.CANCELADA);
            p.setResposta("O comprador desistiu da reserva.");
            p.setRespondidoEm(LocalDateTime.now());
            a.setStatus(StatusAnuncio.PUBLICADO); // o apartamento volta à vitrine
        } else {
            throw new RegraDeNegocioException("Esta proposta não pode mais ser cancelada.");
        }
    }

    // ----------------------------------------------------------------- vendedor

    @Transactional
    public void aceitar(Long propostaId, Long vendedorId) {
        Proposta p = doVendedor(propostaId, vendedorId);
        Apartamento a = p.getApartamento();
        if (p.getStatus() != StatusProposta.PENDENTE) {
            throw new RegraDeNegocioException("Esta proposta já foi respondida.");
        }
        if (a.getStatus() != StatusAnuncio.PUBLICADO) {
            throw new RegraDeNegocioException("Só é possível aceitar propostas de anúncios publicados.");
        }
        LocalDateTime agora = LocalDateTime.now();
        p.setStatus(StatusProposta.ACEITA);
        p.setRespondidoEm(agora);
        a.setStatus(StatusAnuncio.RESERVADO);

        // As demais propostas pendentes deste apartamento são recusadas automaticamente.
        for (Proposta outra : propostas.findByApartamentoIdAndStatus(a.getId(), StatusProposta.PENDENTE)) {
            if (!outra.getId().equals(p.getId())) {
                outra.setStatus(StatusProposta.RECUSADA);
                outra.setResposta("O vendedor aceitou outra proposta.");
                outra.setRespondidoEm(agora);
            }
        }
    }

    @Transactional
    public void recusar(Long propostaId, Long vendedorId, String resposta) {
        Proposta p = doVendedor(propostaId, vendedorId);
        if (p.getStatus() != StatusProposta.PENDENTE) {
            throw new RegraDeNegocioException("Esta proposta já foi respondida.");
        }
        if (resposta != null && resposta.trim().length() > 500) {
            throw new RegraDeNegocioException("A resposta pode ter até 500 caracteres.");
        }
        p.setStatus(StatusProposta.RECUSADA);
        p.setResposta(resposta == null || resposta.isBlank() ? null : resposta.trim());
        p.setRespondidoEm(LocalDateTime.now());
    }

    /** O vendedor desfaz a reserva: a proposta aceita é cancelada e o anúncio volta a ser publicado. */
    @Transactional
    public void cancelarReserva(Long apartamentoId, Long vendedorId) {
        Apartamento a = apartamentoReservadoDoVendedor(apartamentoId, vendedorId);
        for (Proposta p : propostas.findByApartamentoIdAndStatus(a.getId(), StatusProposta.ACEITA)) {
            p.setStatus(StatusProposta.CANCELADA);
            p.setResposta("O vendedor cancelou a reserva.");
            p.setRespondidoEm(LocalDateTime.now());
        }
        a.setStatus(StatusAnuncio.PUBLICADO);
    }

    /**
     * Conclui a venda: marca o anúncio como VENDIDO e grava o registro em "vendas"
     * já com a comissão da plataforma calculada.
     */
    @Transactional
    public Venda concluirVenda(Long apartamentoId, Long vendedorId) {
        Apartamento a = apartamentoReservadoDoVendedor(apartamentoId, vendedorId);
        Proposta aceita = propostas.findByApartamentoIdAndStatus(a.getId(), StatusProposta.ACEITA).stream()
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Não há proposta aceita para este anúncio."));

        BigDecimal percentual = props.comissaoPercentual();
        BigDecimal comissao = aceita.getValorOfertado()
                .multiply(percentual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Venda v = new Venda();
        v.setApartamento(a);
        v.setProposta(aceita);
        v.setComprador(aceita.getComprador());
        v.setVendedor(a.getVendedor());
        v.setValorFinal(aceita.getValorOfertado());
        v.setComissaoPercentual(percentual);
        v.setComissaoValor(comissao);

        a.setStatus(StatusAnuncio.VENDIDO);
        a.setDestaque(false);
        return vendas.save(v);
    }

    // ------------------------------------------------------------------- apoio

    private Proposta buscar(Long id) {
        return propostas.findById(id).orElseThrow(() -> new NaoEncontradoException("Proposta não encontrada."));
    }

    /** Garante que a proposta é de um anúncio do vendedor logado. */
    private Proposta doVendedor(Long propostaId, Long vendedorId) {
        Proposta p = buscar(propostaId);
        if (!p.getApartamento().getVendedor().getId().equals(vendedorId)) {
            throw new AccessDeniedException("Esta proposta é de um anúncio de outro vendedor.");
        }
        return p;
    }

    private Apartamento apartamentoReservadoDoVendedor(Long apartamentoId, Long vendedorId) {
        Apartamento a = apartamentos.findById(apartamentoId)
                .orElseThrow(() -> new NaoEncontradoException("Anúncio não encontrado."));
        if (!a.getVendedor().getId().equals(vendedorId)) {
            throw new AccessDeniedException("Este anúncio pertence a outro vendedor.");
        }
        if (a.getStatus() != StatusAnuncio.RESERVADO) {
            throw new RegraDeNegocioException("Este anúncio não está reservado.");
        }
        return a;
    }
}
