package com.andaralto.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** O anúncio de um apartamento à venda. */
@Entity
@Table(name = "apartamentos")
public class Apartamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** N:1 - vários anúncios para um vendedor. LAZY = só carrega quando precisar. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @Column(nullable = false, length = 140)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String descricao;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal preco;

    @Column(name = "valor_condominio", precision = 10, scale = 2)
    private BigDecimal valorCondominio;

    @Column(name = "area_m2", nullable = false)
    private Integer areaM2;

    @Column(nullable = false)
    private Integer quartos;

    @Column(nullable = false)
    private Integer suites = 0;

    @Column(nullable = false)
    private Integer banheiros;

    @Column(nullable = false)
    private Integer vagas = 0;

    private Integer andar;

    @Column(length = 9)
    private String cep;

    @Column(nullable = false, length = 160)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(nullable = false, length = 80)
    private String bairro;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAnuncio status = StatusAnuncio.EM_ANALISE;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;

    @Column(nullable = false)
    private boolean destaque;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    /** N:N - tabela de ligação "apartamento_comodidades". */
    @ManyToMany
    @JoinTable(name = "apartamento_comodidades",
            joinColumns = @JoinColumn(name = "apartamento_id"),
            inverseJoinColumns = @JoinColumn(name = "comodidade_id"))
    @OrderBy("nome")
    private Set<Comodidade> comodidades = new LinkedHashSet<>();

    /** 1:N - ao salvar/apagar o anúncio, as imagens vão junto (cascade). */
    @OneToMany(mappedBy = "apartamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ImagemApartamento> imagens = new ArrayList<>();

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public void adicionarImagem(ImagemApartamento imagem) {
        imagem.setApartamento(this);
        imagens.add(imagem);
    }

    // ----- Métodos de apoio usados pelas páginas -----

    public String getCapaUrl() {
        return imagens.isEmpty() ? "/imagens/ficticias/fachada/1.svg" : imagens.get(0).getUrl();
    }

    public String getLocalizacao() {
        return bairro + ", " + cidade + " - " + uf;
    }

    public String getEnderecoCompleto() {
        String num = (numero == null || numero.isBlank()) ? "" : ", " + numero;
        return logradouro + num + " - " + bairro + ", " + cidade + "/" + uf;
    }

    public BigDecimal getPrecoPorM2() {
        if (areaM2 == null || areaM2 == 0) {
            return BigDecimal.ZERO;
        }
        return preco.divide(BigDecimal.valueOf(areaM2), 0, RoundingMode.HALF_UP);
    }

    public boolean isPublicado() {
        return status == StatusAnuncio.PUBLICADO;
    }

    // ----- Getters e setters -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public BigDecimal getValorCondominio() { return valorCondominio; }
    public void setValorCondominio(BigDecimal valorCondominio) { this.valorCondominio = valorCondominio; }
    public Integer getAreaM2() { return areaM2; }
    public void setAreaM2(Integer areaM2) { this.areaM2 = areaM2; }
    public Integer getQuartos() { return quartos; }
    public void setQuartos(Integer quartos) { this.quartos = quartos; }
    public Integer getSuites() { return suites; }
    public void setSuites(Integer suites) { this.suites = suites; }
    public Integer getBanheiros() { return banheiros; }
    public void setBanheiros(Integer banheiros) { this.banheiros = banheiros; }
    public Integer getVagas() { return vagas; }
    public void setVagas(Integer vagas) { this.vagas = vagas; }
    public Integer getAndar() { return andar; }
    public void setAndar(Integer andar) { this.andar = andar; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public StatusAnuncio getStatus() { return status; }
    public void setStatus(StatusAnuncio status) { this.status = status; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    public boolean isDestaque() { return destaque; }
    public void setDestaque(boolean destaque) { this.destaque = destaque; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Set<Comodidade> getComodidades() { return comodidades; }
    public void setComodidades(Set<Comodidade> comodidades) { this.comodidades = comodidades; }
    public List<ImagemApartamento> getImagens() { return imagens; }
    public void setImagens(List<ImagemApartamento> imagens) { this.imagens = imagens; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Apartamento outro)) return false;
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
