package com.andaralto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.model.Proposta;
import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.StatusProposta;
import com.andaralto.model.Usuario;
import com.andaralto.model.Venda;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.repository.VendaRepository;
import com.andaralto.service.ApartamentoService;
import com.andaralto.service.PainelService;
import com.andaralto.service.PropostaService;
import com.andaralto.service.UsuarioService;
import com.andaralto.web.form.ApartamentoForm;

/**
 * Testa as REGRAS do negócio direto nos services (sem passar pelas páginas):
 * o caminho feliz anúncio -> proposta -> reserva -> venda e as proibições mais importantes.
 *
 * Cada teste cria o seu próprio anúncio, então os testes não dependem uns dos outros.
 */
@SpringBootTest
@ActiveProfiles("test")
class FluxoDeNegocioTest {

    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private VendaRepository vendas;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ApartamentoService apartamentos;
    @Autowired
    private PropostaService propostas;
    @Autowired
    private PainelService painel;

    private Usuario usuario(String email) {
        return usuarios.findByEmailIgnoreCase(email).orElseThrow();
    }

    private Usuario ana() { return usuario("ana@andaralto.demo"); }          // compradora
    private Usuario carla() { return usuario("carla@andaralto.demo"); }      // vendedora
    private Usuario roberto() { return usuario("roberto@andaralto.demo"); }  // vendedor

    private ApartamentoForm formulario() {
        ApartamentoForm f = new ApartamentoForm();
        f.setTitulo("Apartamento de teste");
        f.setDescricao("Descrição criada pelo teste automatizado.");
        f.setPreco(new BigDecimal("700000"));
        f.setAreaM2(60);
        f.setQuartos(2);
        f.setSuites(0);
        f.setBanheiros(1);
        f.setVagas(1);
        f.setAndar(3);
        f.setLogradouro("Rua do Teste");
        f.setNumero("10");
        f.setBairro("Centro");
        f.setCidade("Recife");
        f.setUf("pe");
        return f;
    }

    /** Anúncio novo do Roberto, já aprovado pela administração (portanto PUBLICADO). */
    private Apartamento anuncioPublicado() {
        Apartamento a = apartamentos.criar(formulario(), roberto().getId());
        apartamentos.aprovar(a.getId());
        return a;
    }

    // ------------------------------------------------------------ dados de demonstração

    @Test
    void demonstracaoCriaOsTresPerfis() {
        assertTrue(usuarios.countByPerfil(Perfil.ADMIN) >= 1);
        assertTrue(usuarios.countByPerfil(Perfil.VENDEDOR) >= 2);
        assertTrue(usuarios.countByPerfil(Perfil.COMPRADOR) >= 3);
    }

    @Test
    void demonstracaoTemUmaVendaComComissaoCongelada() {
        Venda venda = vendas.findAllByOrderByVendidoEmDesc().stream()
                .filter(v -> v.getValorFinal().compareTo(new BigDecimal("620000")) == 0)
                .findFirst()
                .orElseThrow();
        assertEquals(0, venda.getComissaoPercentual().compareTo(new BigDecimal("2.0")));
        assertEquals(0, venda.getComissaoValor().compareTo(new BigDecimal("12400.00"))); // 2% de 620.000
    }

    @Test
    void painelDaAdministracaoContaTodasAsSituacoes() {
        PainelService.PainelAdmin p = painel.admin();
        assertEquals(StatusAnuncio.values().length, p.anunciosPorStatus().size());
        assertTrue(p.totalVendas() >= 1);
        assertTrue(p.volumeVendas().signum() > 0);
    }

    @Test
    void senhaFicaGuardadaComoHashBcrypt() {
        String hash = ana().getSenhaHash();
        assertNotEquals("demo123", hash);
        assertTrue(hash.startsWith("$2"), "BCrypt começa com $2a$, $2b$ ou $2y$");
    }

    // ------------------------------------------------------------------- cadastro

    @Test
    void cadastroPublicoNaoCriaAdministrador() {
        assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.cadastrarPublico("Intruso", "intruso@teste.com", "123456", null, Perfil.ADMIN));
        assertFalse(usuarios.existsByEmailIgnoreCase("intruso@teste.com"));
    }

    @Test
    void emailNaoPodeSerCadastradoDuasVezes() {
        assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.cadastrarPublico("Outra Ana", "ANA@andaralto.demo", "123456", null, Perfil.COMPRADOR));
    }

    // ---------------------------------------------------------------- caminho feliz

    @Test
    void fluxoCompletoDoAnuncioAteAVenda() {
        Usuario ana = ana();
        Usuario roberto = roberto();

        // 1) Anúncio novo nasce "em análise" e já vem com as 4 ilustrações fictícias.
        Apartamento a = apartamentos.criar(formulario(), roberto.getId());
        assertEquals(StatusAnuncio.EM_ANALISE, a.getStatus());
        assertEquals(4, a.getImagens().size());
        assertEquals("PE", apartamentos.buscarPorId(a.getId()).getUf());

        // 2) Enquanto não for aprovado, não recebe proposta.
        assertThrows(RegraDeNegocioException.class,
                () -> propostas.enviar(a.getId(), ana.getId(), new BigDecimal("690000"), null));

        // 3) Aprovado, recebe a proposta da Ana (e só uma por comprador).
        apartamentos.aprovar(a.getId());
        Proposta p = propostas.enviar(a.getId(), ana.getId(), new BigDecimal("700000"), "Fechamos?");
        assertEquals(StatusProposta.PENDENTE, p.getStatus());
        assertThrows(RegraDeNegocioException.class,
                () -> propostas.enviar(a.getId(), ana.getId(), new BigDecimal("710000"), null));

        // 4) Outro vendedor não pode aceitar a proposta; o dono pode.
        Long carlaId = carla().getId();
        assertThrows(AccessDeniedException.class, () -> propostas.aceitar(p.getId(), carlaId));
        propostas.aceitar(p.getId(), roberto.getId());
        assertEquals(StatusAnuncio.RESERVADO, apartamentos.buscarPorId(a.getId()).getStatus());

        // 5) Concluir a venda congela a comissão (2% de 700.000 = 14.000,00).
        Venda venda = propostas.concluirVenda(a.getId(), roberto.getId());
        assertEquals(0, venda.getValorFinal().compareTo(new BigDecimal("700000")));
        assertEquals(0, venda.getComissaoValor().compareTo(new BigDecimal("14000.00")));
        assertEquals(StatusAnuncio.VENDIDO, apartamentos.buscarPorId(a.getId()).getStatus());

        // 6) Anúncio vendido não pode ser editado nem receber novas propostas.
        Long anuncioId = a.getId();
        assertThrows(RegraDeNegocioException.class,
                () -> apartamentos.atualizar(anuncioId, formulario(), roberto.getId()));
        assertThrows(RegraDeNegocioException.class,
                () -> propostas.enviar(anuncioId, ana.getId(), new BigDecimal("700000"), null));
    }

    // ------------------------------------------------------------ outras regras

    @Test
    void compradorQueDesisteDaReservaDevolveOAnuncioParaAVitrine() {
        Apartamento a = anuncioPublicado();
        Proposta p = propostas.enviar(a.getId(), ana().getId(), new BigDecimal("690000"), null);
        propostas.aceitar(p.getId(), roberto().getId());
        assertEquals(StatusAnuncio.RESERVADO, apartamentos.buscarPorId(a.getId()).getStatus());

        propostas.cancelar(p.getId(), ana().getId());

        assertEquals(StatusAnuncio.PUBLICADO, apartamentos.buscarPorId(a.getId()).getStatus());
    }

    @Test
    void aceitarUmaPropostaRecusaAsOutrasPendentes() {
        Apartamento a = anuncioPublicado();
        Usuario bruno = usuario("bruno@andaralto.demo");
        Proposta daAna = propostas.enviar(a.getId(), ana().getId(), new BigDecimal("680000"), null);
        Proposta doBruno = propostas.enviar(a.getId(), bruno.getId(), new BigDecimal("695000"), null);

        propostas.aceitar(doBruno.getId(), roberto().getId());

        Proposta recarregada = propostas.listarDoComprador(ana().getId()).stream()
                .filter(x -> x.getId().equals(daAna.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(StatusProposta.RECUSADA, recarregada.getStatus());
    }

    @Test
    void rejeitarAnuncioExigeMotivoERecusaPropostasPendentes() {
        Apartamento a = anuncioPublicado();
        Proposta p = propostas.enviar(a.getId(), ana().getId(), new BigDecimal("680000"), null);

        assertThrows(RegraDeNegocioException.class, () -> apartamentos.rejeitar(a.getId(), "  "));

        apartamentos.rejeitar(a.getId(), "Fotos e descrição incompletas.");

        assertEquals(StatusAnuncio.REJEITADO, apartamentos.buscarPorId(a.getId()).getStatus());
        Proposta recarregada = propostas.listarDoComprador(ana().getId()).stream()
                .filter(x -> x.getId().equals(p.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(StatusProposta.RECUSADA, recarregada.getStatus());
    }

    @Test
    void vendedorNaoMexeEmAnuncioDeOutroVendedor() {
        Apartamento a = anuncioPublicado(); // do Roberto
        Long carlaId = carla().getId();
        assertThrows(AccessDeniedException.class, () -> apartamentos.pausar(a.getId(), carlaId));
        assertThrows(AccessDeniedException.class, () -> apartamentos.excluir(a.getId(), carlaId));
    }

    @Test
    void anuncioComPropostaNaoPodeSerExcluido() {
        Apartamento a = anuncioPublicado();
        propostas.enviar(a.getId(), ana().getId(), new BigDecimal("690000"), null);
        assertThrows(RegraDeNegocioException.class, () -> apartamentos.excluir(a.getId(), roberto().getId()));
    }
}
