package com.andaralto.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados que a pessoa pode alterar no próprio cadastro. */
public class ContaForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120, message = "O nome pode ter até 120 caracteres.")
    private String nome;

    @Pattern(regexp = "^$|^[0-9 ()+-]{8,20}$", message = "Informe um telefone válido, com DDD.")
    private String telefone;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
