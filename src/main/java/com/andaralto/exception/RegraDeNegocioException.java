package com.andaralto.exception;

/**
 * Lançada quando o usuário tenta algo que as regras do site não permitem
 * (ex.: fazer proposta em anúncio já vendido). A mensagem é mostrada na tela.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
