package com.andaralto.web;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.config.AppProperties;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Apartamento;
import com.andaralto.model.Perfil;
import com.andaralto.security.UsuarioDetails;
import com.andaralto.service.ApartamentoService;
import com.andaralto.service.FavoritoService;
import com.andaralto.service.FiltroBusca;
import com.andaralto.service.PropostaService;
import com.andaralto.service.UsuarioService;
import com.andaralto.web.form.CadastroForm;
import com.andaralto.web.form.PropostaForm;

import jakarta.validation.Valid;

/**
 * Páginas abertas a qualquer visitante: vitrine, busca, detalhe do anúncio,
 * login e cadastro. (Quem pode o quê está em SecurityConfig.)
 */
@Controller
public class PublicoController {

    private final ApartamentoService apartamentos;
    private final FavoritoService favoritos;
    private final PropostaService propostas;
    private final UsuarioService usuarios;
    private final AppProperties props;

    public PublicoController(ApartamentoService apartamentos, FavoritoService favoritos,
                             PropostaService propostas, UsuarioService usuarios, AppProperties props) {
        this.apartamentos = apartamentos;
        this.favoritos = favoritos;
        this.propostas = propostas;
        this.usuarios = usuarios;
        this.props = props;
    }

    // ------------------------------------------------------------------ vitrine

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("destaques", apartamentos.destaques());
        model.addAttribute("cidades", apartamentos.cidades());
        model.addAttribute("totalPublicados", apartamentos.totalPublicados());
        model.addAttribute("favoritosIds", favoritos.idsDoComprador(quem));
        return "index";
    }

    /**
     * Busca com filtros. O Spring preenche o objeto "filtro" com os parâmetros da
     * URL (?cidade=Recife&quartos=2...). O BindingResult evita erro 400 se alguém
     * digitar texto num campo numérico: o valor inválido é simplesmente ignorado.
     */
    @GetMapping("/apartamentos")
    public String buscar(@ModelAttribute("filtro") FiltroBusca filtro, BindingResult binding,
                         @RequestParam(defaultValue = "1") int pagina,
                         @AuthenticationPrincipal UsuarioDetails quem, Model model) {
        Page<Apartamento> resultado = apartamentos.buscar(filtro, pagina);
        model.addAttribute("resultado", resultado);
        model.addAttribute("cidades", apartamentos.cidades());
        model.addAttribute("favoritosIds", favoritos.idsDoComprador(quem));
        return "apartamentos/lista";
    }

    @GetMapping("/apartamentos/{id}")
    public String detalhe(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem, Model model) {
        Apartamento a = apartamentos.buscarVisivel(id, quem);
        boolean comprador = quem != null && quem.isComprador();

        model.addAttribute("apartamento", a);
        model.addAttribute("ehComprador", comprador);
        model.addAttribute("favorito", comprador && favoritos.idsDoComprador(quem).contains(id));
        model.addAttribute("propostaAtual",
                comprador ? propostas.propostaEmAndamento(quem.getId(), id).orElse(null) : null);

        // Se a proposta foi recusada na validação, o controller do comprador devolve o
        // formulário (com os erros) por "flash attribute"; senão, criamos um novo.
        if (!model.containsAttribute("propostaForm")) {
            PropostaForm form = new PropostaForm();
            form.setValor(a.getPreco());
            model.addAttribute("propostaForm", form);
        }
        return "apartamentos/detalhe";
    }

    // ------------------------------------------------------------ login / cadastro

    @GetMapping("/login")
    public String login(Model model) {
        // Com os dados de demonstração ligados, a tela de login lista as contas de teste.
        model.addAttribute("modoDemo", props.demo().dados());
        return "login";
    }

    /** {@code /cadastro?perfil=VENDEDOR} já abre o formulário com "vendedor" marcado. */
    @GetMapping("/cadastro")
    public String cadastro(@RequestParam(required = false) Perfil perfil, Model model) {
        CadastroForm form = new CadastroForm();
        if (perfil == Perfil.COMPRADOR || perfil == Perfil.VENDEDOR) {
            form.setPerfil(perfil);
        }
        model.addAttribute("form", form);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form, BindingResult binding,
                            RedirectAttributes redirect) {
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            binding.rejectValue("confirmacaoSenha", "senha.diferente", "As senhas não são iguais.");
        }
        if (binding.hasErrors()) {
            return "cadastro"; // reabre o formulário mostrando os erros
        }
        try {
            usuarios.cadastrarPublico(form.getNome(), form.getEmail(), form.getSenha(),
                    form.getTelefone(), form.getPerfil());
        } catch (RegraDeNegocioException e) {
            binding.reject("cadastro.recusado", e.getMessage());
            return "cadastro";
        }
        redirect.addFlashAttribute("sucesso", "Conta criada. Entre com seu e-mail e sua senha.");
        return "redirect:/login";
    }
}
