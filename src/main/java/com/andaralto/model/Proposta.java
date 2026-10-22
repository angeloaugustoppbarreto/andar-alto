package com.andaralto.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Oferta de compra feita por um comprador para um apartamento. */
@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id")
    private Apartamento apartamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @Column(name = "valor_ofertado", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorOfertado;

    @Column(length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProposta status = StatusProposta.PENDENTE;

    /** Texto do vendedor ao recusar (ou aviso automático do sistema). */
    @Column(length = 500)
    private String resposta;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    public boolean isPendente() {
        return status == StatusProposta.PENDENTE;
    }

    public boolean isAceita() {
        return status == StatusProposta.ACEITA;
    }

    /**
     * Quanto a oferta difere do preço anunciado, em porcentagem com uma casa decimal
     * (negativo = oferta abaixo do preço). O vendedor usa isso para avaliar a proposta.
     */
    public BigDecimal getDiferencaPercentual() {
        BigDecimal preco = apartamento.getPreco();
        return valorOfertado.subtract(preco)
                .multiply(BigDecimal.valueOf(100))
                .divide(preco, 1, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Apartamento getApartamento() { return apartamento; }
    public void setApartamento(Apartamento apartamento) { this.apartamento = apartamento; }
    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }
    public BigDecimal getValorOfertado() { return valorOfertado; }
    public void setValorOfertado(BigDecimal valorOfertado) { this.valorOfertado = valorOfertado; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public StatusProposta getStatus() { return status; }
    public void setStatus(StatusProposta status) { this.status = status; }
    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getRespondidoEm() { return respondidoEm; }
    public void setRespondidoEm(LocalDateTime respondidoEm) { this.respondidoEm = respondidoEm; }
}
