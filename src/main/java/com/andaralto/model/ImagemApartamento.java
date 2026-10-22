package com.andaralto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Uma foto do anúncio. A primeira (menor "ordem") é a capa. */
@Entity
@Table(name = "apartamento_imagens")
public class ImagemApartamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartamento_id")
    private Apartamento apartamento;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 80)
    private String legenda;

    @Column(nullable = false)
    private int ordem;

    public ImagemApartamento() {
    }

    public ImagemApartamento(String url, String legenda, int ordem) {
        this.url = url;
        this.legenda = legenda;
        this.ordem = ordem;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Apartamento getApartamento() { return apartamento; }
    public void setApartamento(Apartamento apartamento) { this.apartamento = apartamento; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
}
