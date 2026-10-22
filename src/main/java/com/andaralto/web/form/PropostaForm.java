package com.andaralto.web.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PropostaForm {

    @NotNull(message = "Informe o valor da proposta.")
    @DecimalMin(value = "1.00", message = "O valor da proposta deve ser maior que zero.")
    private BigDecimal valor;

    @Size(max = 1000, message = "A mensagem pode ter até 1.000 caracteres.")
    private String mensagem;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
