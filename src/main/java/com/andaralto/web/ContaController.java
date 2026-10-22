package com.andaralto.web;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Usuario;
import com.andaralto.security.UsuarioDetails;
import com.andaralto.service.UsuarioService;
import com.andaralto.web.form.ContaForm;
import com.andaralto.web.form.SenhaForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/** "Minha conta": qualquer usuário logado (comprador, vendedor ou admin) edita os próprios dados. */
@Controller
@RequestMapping("/conta")
public class ContaController {

    private final UsuarioService usuarios;
    private final SecurityContextRepository repositorioDeContexto = new HttpSessionSecurityContextRepository();

    public ContaController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    public String conta(@AuthenticationPrincipal UsuarioDetails quem, Model model) {
        preparar(model, quem);
        return "conta";
    }

    @PostMapping
    public String salvarDados(@Valid @ModelAttribute("contaForm") ContaForm form, BindingResult binding,
                              @AuthenticationPrincipal UsuarioDetails quem, Model model,
                              HttpServletRequest request, HttpServletResponse response,
                              RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            preparar(model, quem);
            return "conta";
        }
        usuarios.atualizarConta(quem.getId(), form.getNome(), form.getTelefone());
        atualizarSessao(quem.getId(), request, response);
        redirect.addFlashAttribute("sucesso", "Dados atualizados.");
        return "redirect:/conta";
    }

    @PostMapping("/senha")
    public String alterarSenha(@Valid @ModelAttribute("senhaForm") SenhaForm form, BindingResult binding,
                               @AuthenticationPrincipal UsuarioDetails quem, Model model,
                               RedirectAttributes redirect) {
        if (form.getNovaSenha() != null && !form.getNovaSenha().equals(form.getConfirmacao())) {
            binding.rejectValue("confirmacao", "senha.diferente", "As senhas não são iguais.");
        }
        if (!binding.hasErrors()) {
            try {
                usuarios.alterarSenha(quem.getId(), form.getSenhaAtual(), form.getNovaSenha());
            } catch (RegraDeNegocioException e) {
                binding.rejectValue("senhaAtual", "senha.incorreta", e.getMessage());
            }
        }
        if (binding.hasErrors()) {
            preparar(model, quem);
            return "conta";
        }
        redirect.addFlashAttribute("sucesso", "Senha alterada.");
        return "redirect:/conta";
    }

    /** Coloca na tela os dados atuais e garante que os dois formulários existam no modelo. */
    private void preparar(Model model, UsuarioDetails quem) {
        Usuario u = usuarios.buscar(quem.getId());
        model.addAttribute("conta", u);
        if (!model.containsAttribute("contaForm")) {
            ContaForm form = new ContaForm();
            form.setNome(u.getNome());
            form.setTelefone(u.getTelefone());
            model.addAttribute("contaForm", form);
        }
        if (!model.containsAttribute("senhaForm")) {
            model.addAttribute("senhaForm", new SenhaForm());
        }
    }

    /**
     * O nome do usuário fica guardado na sessão de login. Depois de editar o
     * cadastro, trocamos esse objeto por um novo, para o menu mostrar o nome atualizado.
     */
    private void atualizarSessao(Long usuarioId, HttpServletRequest request, HttpServletResponse response) {
        UsuarioDetails atualizado = new UsuarioDetails(usuarios.buscar(usuarioId));
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
                atualizado, null, atualizado.getAuthorities());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(token);
        SecurityContextHolder.setContext(contexto);
        repositorioDeContexto.saveContext(contexto, request, response);
    }
}
