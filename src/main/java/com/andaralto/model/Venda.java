package com.andaralto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Venda concluída. É o "recibo" que o administrador usa para ver o faturamento. */
@Entity
@Table(name = "vendas")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id")
    private Apartamento apartamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposta_id")
    private Proposta proposta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @Column(name = "valor_final", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "comissao_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal comissaoPercentual;

    @Column(name = "comissao_valor", nullable = false, precision = 14, scale = 2)
    private BigDecimal comissaoValor;

    @Column(name = "vendido_em", nullable = false, updatable = false)
    private LocalDateTime vendidoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Apartamento getApartamento() { return apartamento; }
    public void setApartamento(Apartamento apartamento) { this.apartamento = apartamento; }
    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }
    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }
    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }
    public BigDecimal getValorFinal() { return valorFinal; }
    public void setValorFinal(BigDecimal valorFinal) { this.valorFinal = valorFinal; }
    public BigDecimal getComissaoPercentual() { return comissaoPercentual; }
    public void setComissaoPercentual(BigDecimal comissaoPercentual) { this.comissaoPercentual = comissaoPercentual; }
    public BigDecimal getComissaoValor() { return comissaoValor; }
    public void setComissaoValor(BigDecimal comissaoValor) { this.comissaoValor = comissaoValor; }
    public LocalDateTime getVendidoEm() { return vendidoEm; }
    public void setVendidoEm(LocalDateTime vendidoEm) { this.vendidoEm = vendidoEm; }
}
