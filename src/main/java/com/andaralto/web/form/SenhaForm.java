package com.andaralto.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Troca de senha: exige a senha atual para confirmar que é mesmo a pessoa. */
public class SenhaForm {

    @NotBlank(message = "Informe a senha atual.")
    private String senhaAtual;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 6, max = 60, message = "A nova senha deve ter de 6 a 60 caracteres.")
    private String novaSenha;

    @NotBlank(message = "Repita a nova senha.")
    private String confirmacao;

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmacao() { return confirmacao; }
    public void setConfirmacao(String confirmacao) { this.confirmacao = confirmacao; }
}
