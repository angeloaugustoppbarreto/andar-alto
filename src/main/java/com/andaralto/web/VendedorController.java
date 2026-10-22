package com.andaralto.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.model.Apartamento;
import com.andaralto.security.UsuarioDetails;
import com.andaralto.service.ApartamentoService;
import com.andaralto.service.ComodidadeService;
import com.andaralto.service.PainelService;
import com.andaralto.service.PropostaService;
import com.andaralto.service.VendaService;
import com.andaralto.web.form.ApartamentoForm;

import jakarta.validation.Valid;

/**
 * Área do VENDEDOR (/vendedor/**): anúncios, propostas recebidas e vendas.
 * Os services conferem se o anúncio/proposta pertence mesmo ao vendedor logado,
 * então um vendedor não consegue mexer nos anúncios de outro nem trocando o id na URL.
 */
@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    private final PainelService painel;
    private final ApartamentoService apartamentos;
    private final ComodidadeService comodidades;
    private final PropostaService propostas;
    private final VendaService vendas;

    public VendedorController(PainelService painel, ApartamentoService apartamentos,
                              ComodidadeService comodidades, PropostaService propostas, VendaService vendas) {
        this.painel = painel;
        this.apartamentos = apartamentos;
        this.comodidades = comodidades;
        this.propostas = propostas;
        this.vendas = vendas;
    }

    @GetMapping
    public String resumo(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("painel", painel.vendedor(quem.getId()));
        return "vendedor/painel";
    }

    // ------------------------------------------------------------------ anúncios

    @GetMapping("/anuncios")
    public String anuncios(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("anuncios", apartamentos.listarDoVendedor(quem.getId()));
        return "vendedor/anuncios";
    }

    @GetMapping("/anuncios/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ApartamentoForm());
        prepararFormulario(model, null);
        return "vendedor/form";
    }

    @PostMapping("/anuncios")
    public String criar(@Valid @ModelAttribute("form") ApartamentoForm form, BindingResult binding,
                        @AuthenticationPrincipal UsuarioDetails quem, Model model,
                        RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepararFormulario(model, null);
            return "vendedor/form";
        }
        apartamentos.criar(form, quem.getId());
        redirect.addFlashAttribute("sucesso",
                "Anúncio enviado para análise. Assim que a administração aprovar, ele aparece na vitrine.");
        return "redirect:/vendedor/anuncios";
    }

    @GetMapping("/anuncios/{id}/editar")
    public String editar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem, Model model) {
        Apartamento a = apartamentos.buscarDoVendedor(id, quem.getId());
        model.addAttribute("form", ApartamentoForm.de(a));
        prepararFormulario(model, a);
        return "vendedor/form";
    }

    @PostMapping("/anuncios/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ApartamentoForm form,
                            BindingResult binding, @AuthenticationPrincipal UsuarioDetails quem,
                            Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepararFormulario(model, apartamentos.buscarDoVendedor(id, quem.getId()));
            return "vendedor/form";
        }
        apartamentos.atualizar(id, form, quem.getId());
        redirect.addFlashAttribute("sucesso", "Anúncio atualizado.");
        return "redirect:/vendedor/anuncios";
    }

    @PostMapping("/anuncios/{id}/pausar")
    public String pausar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                         RedirectAttributes redirect) {
        apartamentos.pausar(id, quem.getId());
        redirect.addFlashAttribute("sucesso", "Anúncio pausado. Ele saiu da vitrine.");
        return "redirect:/vendedor/anuncios";
    }

    @PostMapping("/anuncios/{id}/reativar")
    public String reativar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                           RedirectAttributes redirect) {
        apartamentos.reativar(id, quem.getId());
        redirect.addFlashAttribute("sucesso", "Anúncio reativado. Ele voltou para a vitrine.");
        return "redirect:/vendedor/anuncios";
    }

    @PostMapping("/anuncios/{id}/excluir")
    public String excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                          RedirectAttributes redirect) {
        apartamentos.excluir(id, quem.getId());
        redirect.addFlashAttribute("sucesso", "Anúncio excluído.");
        return "redirect:/vendedor/anuncios";
    }

    /** Fecha o negócio de um anúncio RESERVADO: vira VENDIDO e gera o registro da venda. */
    @PostMapping("/anuncios/{id}/concluir-venda")
    public String concluirVenda(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                                RedirectAttributes redirect) {
        propostas.concluirVenda(id, quem.getId());
        redirect.addFlashAttribute("sucesso", "Venda concluída. Parabéns!");
        return "redirect:/vendedor/vendas";
    }

    @PostMapping("/anuncios/{id}/cancelar-reserva")
    public String cancelarReserva(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                                  RedirectAttributes redirect) {
        propostas.cancelarReserva(id, quem.getId());
        redirect.addFlashAttribute("sucesso", "Reserva cancelada. O anúncio voltou para a vitrine.");
        return "redirect:/vendedor/anuncios";
    }

    // ----------------------------------------------------------------- propostas

    @GetMapping("/propostas")
    public String propostasRecebidas(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("propostas", propostas.listarRecebidas(quem.getId()));
        return "vendedor/propostas";
    }

    @PostMapping("/propostas/{id}/aceitar")
    public String aceitar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails quem,
                          RedirectAttributes redirect) {
        propostas.aceitar(id, quem.getId());
        redirect.addFlashAttribute("sucesso",
                "Proposta aceita. O anúncio ficou reservado; conclua a venda quando o negócio fechar.");
        return "redirect:/vendedor/propostas";
    }

    @PostMapping("/propostas/{id}/recusar")
    public String recusar(@PathVariable Long id, @RequestParam(required = false) String resposta,
                          @AuthenticationPrincipal UsuarioDetails quem, RedirectAttributes redirect) {
        propostas.recusar(id, quem.getId(), resposta);
        redirect.addFlashAttribute("sucesso", "Proposta recusada.");
        return "redirect:/vendedor/propostas";
    }

    // -------------------------------------------------------------------- vendas

    @GetMapping("/vendas")
    public String minhasVendas(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("vendas", vendas.listarDoVendedor(quem.getId()));
        return "vendedor/vendas";
    }

    // -------------------------------------------------------------------- apoio

    /** Dados que o formulário de anúncio precisa (lista de comodidades e título da página). */
    private void prepararFormulario(Model model, Apartamento existente) {
        model.addAttribute("comodidades", comodidades.listar());
        model.addAttribute("anuncio", existente);
        model.addAttribute("edicao", existente != null);
    }
}
