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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.security.UsuarioDetails;
import com.andaralto.service.FavoritoService;
import com.andaralto.service.PainelService;
import com.andaralto.service.PropostaService;
import com.andaralto.web.form.PropostaForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Área do COMPRADOR (/comprador/**): painel, favoritos e propostas. */
@Controller
@RequestMapping("/comprador")
public class CompradorController {

    private final PainelService painel;
    private final FavoritoService favoritos;
    private final PropostaService propostas;

    public CompradorController(PainelService painel, FavoritoService favoritos, PropostaService propostas) {
        this.painel = painel;
        this.favoritos = favoritos;
        this.propostas = propostas;
    }

    @GetMapping
    public String resumo(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("painel", painel.comprador(quem.getId()));
        return "comprador/painel";
    }

    // ---------------------------------------------------------------- favoritos

    @GetMapping("/favoritos")
    public String listaFavoritos(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("apartamentos", favoritos.listar(quem.getId()));
        model.addAttribute("favoritosIds", favoritos.idsDoComprador(quem));
        return "comprador/favoritos";
    }

    /** O coração das listas: favorita ou desfavorita e volta para a página em que a pessoa estava. */
    @PostMapping("/favoritos/{apartamentoId}")
    public String alternarFavorito(@PathVariable Long apartamentoId,
                                   @AuthenticationPrincipal UsuarioDetails quem,
                                   HttpServletRequest request) {
        favoritos.alternar(quem.getId(), apartamentoId);
        return Navegacao.voltar(request, "/comprador/favoritos");
    }

    // ---------------------------------------------------------------- propostas

    @GetMapping("/propostas")
    public String minhasPropostas(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        model.addAttribute("propostas", propostas.listarDoComprador(quem.getId()));
        return "comprador/propostas";
    }

    @PostMapping("/apartamentos/{apartamentoId}/proposta")
    public String enviarProposta(@PathVariable Long apartamentoId,
                                 @Valid @ModelAttribute("propostaForm") PropostaForm form,
                                 BindingResult binding,
                                 @AuthenticationPrincipal UsuarioDetails quem,
                                 RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            // Volta para a página do anúncio levando o formulário e os erros de validação.
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.propostaForm", binding);
            redirect.addFlashAttribute("propostaForm", form);
            return "redirect:/apartamentos/" + apartamentoId;
        }
        propostas.enviar(apartamentoId, quem.getId(), form.getValor(), form.getMensagem());
        redirect.addFlashAttribute("sucesso", "Proposta enviada. O vendedor vai responder por aqui.");
        return "redirect:/comprador/propostas";
    }

    @PostMapping("/propostas/{propostaId}/cancelar")
    public String cancelarProposta(@PathVariable Long propostaId,
                                   @AuthenticationPrincipal UsuarioDetails quem,
                                   RedirectAttributes redirect) {
        propostas.cancelar(propostaId, quem.getId());
        redirect.addFlashAttribute("sucesso", "Proposta cancelada.");
        return "redirect:/comprador/propostas";
    }
}
