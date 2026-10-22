package com.andaralto.web.form;

import com.andaralto.model.Perfil;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Form = objeto que recebe os campos do formulário HTML. As anotações
 * (@NotBlank, @Email...) validam os dados antes de chegarem ao service.
 */
public class CadastroForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120, message = "O nome pode ter até 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160, message = "O e-mail pode ter até 160 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 6, max = 60, message = "A senha deve ter de 6 a 60 caracteres.")
    private String senha;

    @NotBlank(message = "Repita a senha.")
    private String confirmacaoSenha;

    @Pattern(regexp = "^$|^[0-9 ()+-]{8,20}$", message = "Informe um telefone válido, com DDD.")
    private String telefone;

    @NotNull(message = "Escolha como você vai usar o site.")
    private Perfil perfil = Perfil.COMPRADOR;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmacaoSenha() { return confirmacaoSenha; }
    public void setConfirmacaoSenha(String confirmacaoSenha) { this.confirmacaoSenha = confirmacaoSenha; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
}
