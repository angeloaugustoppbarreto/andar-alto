package com.andaralto.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.exception.RegraDeNegocioException;
import com.andaralto.model.Perfil;
import com.andaralto.model.Usuario;
import com.andaralto.repository.UsuarioRepository;

/**
 * Service = onde ficam as REGRAS do negócio. O controller recebe o pedido,
 * o service decide se pode e como fazer, o repository grava no banco.
 *
 * @Transactional: tudo dentro do método acontece em uma transação. Se algo
 * falhar no meio, o banco volta ao estado anterior (nada fica pela metade).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    /**
     * Cadastro aberto ao público. Só permite COMPRADOR ou VENDEDOR: mesmo que
     * alguém altere o formulário no navegador para mandar "ADMIN", a regra
     * é conferida aqui, no servidor. Nunca confie só no que vem da tela.
     */
    @Transactional
    public Usuario cadastrarPublico(String nome, String email, String senha, String telefone, Perfil perfil) {
        if (perfil != Perfil.COMPRADOR && perfil != Perfil.VENDEDOR) {
            throw new RegraDeNegocioException("Escolha entre comprador e vendedor.");
        }
        return criar(nome, email, senha, telefone, perfil);
    }

    /** Criação sem restrição de perfil: uso interno (administrador inicial e dados de demonstração). */
    @Transactional
    public Usuario criar(String nome, String email, String senha, String telefone, Perfil perfil) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new RegraDeNegocioException("Já existe uma conta com este e-mail.");
        }
        Usuario u = new Usuario();
        u.setNome(nome.trim());
        u.setEmail(emailNormalizado);
        u.setSenhaHash(encoder.encode(senha)); // grava só o hash BCrypt
        u.setTelefone(telefone == null || telefone.isBlank() ? null : telefone.trim());
        u.setPerfil(perfil);
        return usuarios.save(u);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarios.findAllByOrderByCriadoEmDesc();
    }

    /**
     * Não há chamada a save(): como o objeto foi carregado dentro da transação,
     * o Hibernate percebe a mudança ("dirty checking") e faz o UPDATE sozinho.
     */
    @Transactional
    public void atualizarConta(Long id, String nome, String telefone) {
        Usuario u = buscar(id);
        u.setNome(nome.trim());
        u.setTelefone(telefone == null || telefone.isBlank() ? null : telefone.trim());
    }

    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario u = buscar(id);
        if (!encoder.matches(senhaAtual, u.getSenhaHash())) {
            throw new RegraDeNegocioException("A senha atual está incorreta.");
        }
        u.setSenhaHash(encoder.encode(novaSenha));
    }

    /** Ativar/desativar contas (administrador). Ninguém pode desativar a si mesmo. */
    @Transactional
    public Usuario alternarAtivo(Long adminId, Long usuarioId) {
        if (adminId.equals(usuarioId)) {
            throw new RegraDeNegocioException("Você não pode desativar a própria conta.");
        }
        Usuario u = buscar(usuarioId);
        u.setAtivo(!u.isAtivo());
        return u;
    }
}
