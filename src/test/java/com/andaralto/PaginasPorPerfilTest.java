package com.andaralto;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.Usuario;
import com.andaralto.repository.ApartamentoRepository;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.security.UsuarioDetails;

/**
 * Teste de integração: sobe o sistema inteiro (com banco H2 em memória e os dados de
 * demonstração) e simula um navegador com o MockMvc. Verifica duas coisas:
 * 1) toda página de cada perfil abre sem erro (inclui os templates Thymeleaf);
 * 2) cada perfil só entra na sua própria área.
 *
 * Para rodar: mvn test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaginasPorPerfilTest {

    private static final String ANA = "ana@andaralto.demo";          // compradora
    private static final String CARLA = "carla@andaralto.demo";      // vendedora
    private static final String ROBERTO = "roberto@andaralto.demo";  // vendedor
    private static final String ADMIN = "admin@andaralto.com";       // administrador

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private ApartamentoRepository apartamentos;

    /** Faz a requisição "como se" a pessoa tivesse entrado com este e-mail. */
    private RequestPostProcessor como(String email) {
        Usuario u = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        return user(new UsuarioDetails(u));
    }

    private Long idDeAnuncio(StatusAnuncio status) {
        return apartamentos.findByStatusOrderByAtualizadoEmDesc(status).get(0).getId();
    }

    private Long idDeAnuncioDoVendedor(String email) {
        Usuario v = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        return apartamentos.findByVendedorIdOrderByAtualizadoEmDesc(v.getId()).get(0).getId();
    }

    // ------------------------------------------------------------------ visitante

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/apartamentos",
            "/apartamentos?cidade=Recife&quartos=2&ordem=menor-preco",
            "/apartamentos?precoMin=500000&precoMax=900000&vagas=1",
            "/apartamentos?q=boa+viagem",
            "/login",
            "/cadastro",
            "/cadastro?perfil=VENDEDOR"
    })
    void visitanteAbrePaginasPublicas(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void visitanteVeDetalheDeAnuncioPublicado() throws Exception {
        mvc.perform(get("/apartamentos/{id}", idDeAnuncio(StatusAnuncio.PUBLICADO)))
                .andExpect(status().isOk());
    }

    @Test
    void anuncioEmAnaliseNaoApareceParaOPublico() throws Exception {
        mvc.perform(get("/apartamentos/{id}", idDeAnuncio(StatusAnuncio.EM_ANALISE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void imagemFicticiaEhUmSvg() throws Exception {
        mvc.perform(get("/imagens/ficticias/sala/42.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/vendedor", "/comprador", "/comprador/favoritos", "/conta"})
    void visitanteEhMandadoParaOLogin(String url) throws Exception {
        mvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ------------------------------------------------------------------ comprador

    @ParameterizedTest
    @ValueSource(strings = {"/comprador", "/comprador/favoritos", "/comprador/propostas", "/conta", "/", "/apartamentos"})
    void compradorAbreSuasPaginas(String url) throws Exception {
        mvc.perform(get(url).with(como(ANA))).andExpect(status().isOk());
    }

    @Test
    void compradorVeDetalheComFormularioDeProposta() throws Exception {
        mvc.perform(get("/apartamentos/{id}", idDeAnuncio(StatusAnuncio.PUBLICADO)).with(como(ANA)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/usuarios", "/vendedor", "/vendedor/anuncios/novo"})
    void compradorNaoEntraEmAreaDeOutroPerfil(String url) throws Exception {
        mvc.perform(get(url).with(como(ANA))).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------- vendedor

    @ParameterizedTest
    @ValueSource(strings = {"/vendedor", "/vendedor/anuncios", "/vendedor/anuncios/novo",
            "/vendedor/propostas", "/vendedor/vendas", "/conta"})
    void vendedorAbreSuasPaginas(String url) throws Exception {
        mvc.perform(get(url).with(como(CARLA))).andExpect(status().isOk());
    }

    @Test
    void vendedorEditaOProprioAnuncio() throws Exception {
        mvc.perform(get("/vendedor/anuncios/{id}/editar", idDeAnuncioDoVendedor(CARLA)).with(como(CARLA)))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorNaoEditaAnuncioDeOutroVendedor() throws Exception {
        mvc.perform(get("/vendedor/anuncios/{id}/editar", idDeAnuncioDoVendedor(ROBERTO)).with(como(CARLA)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/comprador", "/comprador/favoritos"})
    void vendedorNaoEntraEmAreaDeOutroPerfil(String url) throws Exception {
        mvc.perform(get(url).with(como(CARLA))).andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------- administrador

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/anuncios", "/admin/anuncios?status=EM_ANALISE",
            "/admin/anuncios?status=REJEITADO", "/admin/usuarios", "/admin/vendas", "/admin/comodidades", "/conta"})
    void administradorAbreSuasPaginas(String url) throws Exception {
        mvc.perform(get(url).with(como(ADMIN))).andExpect(status().isOk());
    }

    @Test
    void administradorVeAnuncioEmAnalise() throws Exception {
        mvc.perform(get("/apartamentos/{id}", idDeAnuncio(StatusAnuncio.EM_ANALISE)).with(como(ADMIN)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/vendedor", "/comprador"})
    void administradorNaoUsaAsAreasDeVendedorEComprador(String url) throws Exception {
        mvc.perform(get(url).with(como(ADMIN))).andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------------- login

    @Test
    void loginDeCompradorVaiParaAVitrine() throws Exception {
        mvc.perform(formLogin("/login").user("email", ANA).password("senha", "demo123"))
                .andExpect(authenticated().withRoles("COMPRADOR"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginDeVendedorVaiParaOPainelDoVendedor() throws Exception {
        mvc.perform(formLogin("/login").user("email", CARLA).password("senha", "demo123"))
                .andExpect(authenticated().withRoles("VENDEDOR"))
                .andExpect(redirectedUrl("/vendedor"));
    }

    @Test
    void loginDeAdministradorVaiParaOPainelDaAdministracao() throws Exception {
        mvc.perform(formLogin("/login").user("email", ADMIN).password("senha", "admin123"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void senhaErradaNaoEntra() throws Exception {
        mvc.perform(formLogin("/login").user("email", ANA).password("senha", "senha-errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }
}
