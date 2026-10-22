package com.andaralto.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.model.StatusAnuncio;
import com.andaralto.model.Usuario;
import com.andaralto.security.UsuarioDetails;
import com.andaralto.service.ApartamentoService;
import com.andaralto.service.ComodidadeService;
import com.andaralto.service.PainelService;
import com.andaralto.service.UsuarioService;
import com.andaralto.service.VendaService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Área do ADMINISTRADOR (/admin/**): moderação dos anúncios, usuários,
 * comodidades e faturamento da plataforma.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PainelService painel;
    private final ApartamentoService apartamentos;
    private final UsuarioService usuarios;
    private final ComodidadeService comodidades;
    private final VendaService vendas;

    public AdminController(PainelService painel, ApartamentoService apartamentos, UsuarioService usuarios,
                           ComodidadeService comodidades, VendaService vendas) {
        this.painel = painel;
        this.apartamentos = apartamentos;
        this.usuarios = usuarios;
        this.comodidades = comodidades;
        this.vendas = vendas;
    }

    @GetMapping
    public String resumo(Model model) {
        model.addAttribute("painel", painel.admin());
        return "admin/painel";
    }

    // ---------------------------------------------------------------- anúncios

    /** Lista de todos os anúncios; {@code ?status=EM_ANALISE} filtra por situação. */
    @GetMapping("/anuncios")
    public String anuncios(@RequestParam(required = false) StatusAnuncio status, Model model) {
        model.addAttribute("anuncios", apartamentos.listarParaAdmin(status));
        model.addAttribute("statusAtual", status);
        model.addAttribute("situacoes", StatusAnuncio.values());
        return "admin/anuncios";
    }

    @PostMapping("/anuncios/{id}/aprovar")
    public String aprovar(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        apartamentos.aprovar(id);
        redirect.addFlashAttribute("sucesso", "Anúncio aprovado e publicado na vitrine.");
        return Navegacao.voltar(request, "/admin/anuncios");
    }

    @PostMapping("/anuncios/{id}/rejeitar")
    public String rejeitar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           HttpServletRequest request, RedirectAttributes redirect) {
        apartamentos.rejeitar(id, motivo);
        redirect.addFlashAttribute("sucesso", "Anúncio rejeitado. O vendedor verá o motivo.");
        return Navegacao.voltar(request, "/admin/anuncios");
    }

    @PostMapping("/anuncios/{id}/destaque")
    public String destaque(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        apartamentos.alternarDestaque(id);
        redirect.addFlashAttribute("sucesso", "Destaque atualizado.");
        return Navegacao.voltar(request, "/admin/anuncios");
    }

    // ---------------------------------------------------------------- usuários

    @GetMapping("/usuarios")
    public String listaUsuarios(Model model) {
        model.addAttribute("usuarios", usuarios.listarTodos());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/alternar")
    public String alternarUsuario(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                                  RedirectAttributes redirect) {
        Usuario u = usuarios.alternarAtivo(quem.getId(), id);
        redirect.addFlashAttribute("sucesso",
                u.isAtivo() ? "Conta de " + u.getNome() + " reativada." : "Conta de " + u.getNome() + " desativada.");
        return "redirect:/admin/usuarios";
    }

    // ------------------------------------------------------------------ vendas

    @GetMapping("/vendas")
    public String listaVendas(Model model) {
        model.addAttribute("vendas", vendas.listarTodas());
        model.addAttribute("painel", painel.admin());
        return "admin/vendas";
    }

    // ------------------------------------------------------------ comodidades

    @GetMapping("/comodidades")
    public String listaComodidades(Model model) {
        model.addAttribute("comodidades", comodidades.listar());
        return "admin/comodidades";
    }

    @PostMapping("/comodidades")
    public String criarComodidade(@RequestParam String nome, RedirectAttributes redirect) {
        comodidades.criar(nome);
        redirect.addFlashAttribute("sucesso", "Comodidade adicionada.");
        return "redirect:/admin/comodidades";
    }

    @PostMapping("/comodidades/{id}/excluir")
    public String excluirComodidade(@PathVariable Long id, RedirectAttributes redirect) {
        comodidades.excluir(id);
        redirect.addFlashAttribute("sucesso", "Comodidade removida dos anúncios e da lista.");
        return "redirect:/admin/comodidades";
    }
}
