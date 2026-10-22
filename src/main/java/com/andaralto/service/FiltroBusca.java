package com.andaralto.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Filtros da busca de apartamentos. Cada campo é opcional: só entra na
 * consulta o que a pessoa preencheu.
 */
public class FiltroBusca {

    private String q;
    private String cidade;
    private BigDecimal precoMin;
    private BigDecimal precoMax;
    private Integer quartos;
    private Integer vagas;
    private String ordem;

    /**
     * Monta "q=sol&cidade=Recife&" para os links de paginação manterem os
     * filtros. Termina com "&" para a página ser anexada depois.
     */
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        adicionar(sb, "q", q);
        adicionar(sb, "cidade", cidade);
        adicionar(sb, "precoMin", precoMin);
        adicionar(sb, "precoMax", precoMax);
        adicionar(sb, "quartos", quartos);
        adicionar(sb, "vagas", vagas);
        adicionar(sb, "ordem", ordem);
        return sb.toString();
    }

    /** Há algum filtro ativo? (usado para mostrar o botão "Limpar filtros") */
    public boolean isAtivo() {
        return !getQueryString().isEmpty();
    }

    private static void adicionar(StringBuilder sb, String nome, Object valor) {
        if (valor == null || valor.toString().isBlank()) {
            return;
        }
        sb.append(nome).append('=')
          .append(URLEncoder.encode(valor.toString().trim(), StandardCharsets.UTF_8))
          .append('&');
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public BigDecimal getPrecoMin() { return precoMin; }
    public void setPrecoMin(BigDecimal precoMin) { this.precoMin = precoMin; }
    public BigDecimal getPrecoMax() { return precoMax; }
    public void setPrecoMax(BigDecimal precoMax) { this.precoMax = precoMax; }
    public Integer getQuartos() { return quartos; }
    public void setQuartos(Integer quartos) { this.quartos = quartos; }
    public Integer getVagas() { return vagas; }
    public void setVagas(Integer vagas) { this.vagas = vagas; }
    public String getOrdem() { return ordem; }
    public void setOrdem(String ordem) { this.ordem = ordem; }
}
