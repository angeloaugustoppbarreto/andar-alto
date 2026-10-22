package com.andaralto.service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.Proposta;
import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.StatusProposta;
import com.andaralto.repository.ApartamentoRepository;
import com.andaralto.repository.PropostaRepository;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.repository.VendaRepository;

/** Números dos painéis de cada perfil (resumo na primeira tela depois do login). */
@Service
@Transactional(readOnly = true)
public class PainelService {

    /** Resumo do administrador. Records são classes imutáveis só para carregar dados. */
    public record PainelAdmin(long totalUsuarios, long compradores, long vendedores,
                              Map<StatusAnuncio, Long> anunciosPorStatus, long totalAnuncios,
                              long propostasPendentes, long totalVendas,
                              BigDecimal volumeVendas, BigDecimal comissaoTotal,
                              List<Apartamento> aguardandoAnalise) {
    }

    public record PainelVendedor(Map<StatusAnuncio, Long> anunciosPorStatus, long totalAnuncios,
                                 long propostasPendentes, long vendas, BigDecimal totalVendido,
                                 List<Proposta> propostasPendentesLista) {
    }

    public record PainelComprador(long favoritos, long propostasPendentes, long propostasAceitas,
                                  List<Proposta> ultimasPropostas) {
    }

    private final UsuarioRepository usuarios;
    private final ApartamentoRepository apartamentos;
    private final PropostaRepository propostas;
    private final VendaRepository vendas;

    public PainelService(UsuarioRepository usuarios, ApartamentoRepository apartamentos,
                         PropostaRepository propostas, VendaRepository vendas) {
        this.usuarios = usuarios;
        this.apartamentos = apartamentos;
        this.propostas = propostas;
        this.vendas = vendas;
    }

    public PainelAdmin admin() {
        Map<StatusAnuncio, Long> porStatus = new EnumMap<>(StatusAnuncio.class);
        long total = 0;
        for (StatusAnuncio s : StatusAnuncio.values()) {
            long qtd = apartamentos.countByStatus(s);
            porStatus.put(s, qtd);
            total += qtd;
        }
        return new PainelAdmin(
                usuarios.count(),
                usuarios.countByPerfil(Perfil.COMPRADOR),
                usuarios.countByPerfil(Perfil.VENDEDOR),
                porStatus,
                total,
                propostas.countByStatus(StatusProposta.PENDENTE),
                vendas.count(),
                vendas.somaValorFinal(),
                vendas.somaComissao(),
                apartamentos.findByStatusOrderByAtualizadoEmDesc(StatusAnuncio.EM_ANALISE));
    }

    public PainelVendedor vendedor(Long vendedorId) {
        Map<StatusAnuncio, Long> porStatus = new EnumMap<>(StatusAnuncio.class);
        long total = 0;
        for (StatusAnuncio s : StatusAnuncio.values()) {
            long qtd = apartamentos.countByVendedorIdAndStatus(vendedorId, s);
            porStatus.put(s, qtd);
            total += qtd;
        }
        List<Proposta> pendentes = propostas.findByApartamentoVendedorIdOrderByCriadoEmDesc(vendedorId).stream()
                .filter(Proposta::isPendente)
                .toList();
        return new PainelVendedor(
                porStatus,
                total,
                pendentes.size(),
                vendas.countByVendedorId(vendedorId),
                vendas.somaValorFinalDoVendedor(vendedorId),
                pendentes);
    }

    public PainelComprador comprador(Long compradorId) {
        return new PainelComprador(
                usuarios.contarFavoritos(compradorId),
                propostas.countByCompradorIdAndStatus(compradorId, StatusProposta.PENDENTE),
                propostas.countByCompradorIdAndStatus(compradorId, StatusProposta.ACEITA),
                propostas.findTop5ByCompradorIdOrderByCriadoEmDesc(compradorId));
    }
}
