package com.andaralto.web.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.andaralto.model.Apartamento;
import com.andaralto.model.Comodidade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Campos do formulário de anúncio (usado para criar e para editar). */
public class ApartamentoForm {

    @NotBlank(message = "Dê um título ao anúncio.")
    @Size(max = 140, message = "O título pode ter até 140 caracteres.")
    private String titulo;

    @NotBlank(message = "Descreva o apartamento.")
    @Size(max = 4000, message = "A descrição pode ter até 4.000 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o preço.")
    @DecimalMin(value = "1.00", message = "O preço deve ser maior que zero.")
    private BigDecimal preco;

    @DecimalMin(value = "0.00", message = "O condomínio não pode ser negativo.")
    private BigDecimal valorCondominio;

    @NotNull(message = "Informe a área.")
    @Min(value = 10, message = "A área mínima é 10 m².")
    @Max(value = 2000, message = "A área máxima é 2.000 m².")
    private Integer areaM2;

    @NotNull(message = "Informe o número de quartos.")
    @Min(value = 0, message = "Use 0 para studio ou kitnet.")
    @Max(value = 20, message = "O máximo é 20 quartos.")
    private Integer quartos;

    @NotNull(message = "Informe as suítes (0 se não houver).")
    @Min(value = 0, message = "O mínimo é 0.")
    @Max(value = 20, message = "O máximo é 20 suítes.")
    private Integer suites = 0;

    @NotNull(message = "Informe os banheiros.")
    @Min(value = 1, message = "O apartamento precisa de ao menos 1 banheiro.")
    @Max(value = 20, message = "O máximo é 20 banheiros.")
    private Integer banheiros;

    @NotNull(message = "Informe as vagas (0 se não houver).")
    @Min(value = 0, message = "O mínimo é 0.")
    @Max(value = 20, message = "O máximo é 20 vagas.")
    private Integer vagas = 0;

    @Min(value = 0, message = "O andar não pode ser negativo.")
    @Max(value = 200, message = "O andar máximo é 200.")
    private Integer andar;

    @Pattern(regexp = "^$|^\\d{5}-?\\d{3}$", message = "Use o formato 00000-000.")
    private String cep;

    @NotBlank(message = "Informe a rua ou avenida.")
    @Size(max = 160, message = "O endereço pode ter até 160 caracteres.")
    private String logradouro;

    @Size(max = 10, message = "Até 10 caracteres.")
    private String numero;

    @NotBlank(message = "Informe o bairro.")
    @Size(max = 80, message = "O bairro pode ter até 80 caracteres.")
    private String bairro;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 80, message = "A cidade pode ter até 80 caracteres.")
    private String cidade;

    @NotBlank(message = "Informe o estado.")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Use a sigla com 2 letras, como PE ou SP.")
    private String uf;

    private List<Long> comodidadeIds = new ArrayList<>();

    /** Preenche o formulário com os dados de um anúncio existente (tela de edição). */
    public static ApartamentoForm de(Apartamento a) {
        ApartamentoForm f = new ApartamentoForm();
        f.titulo = a.getTitulo();
        f.descricao = a.getDescricao();
        f.preco = a.getPreco();
        f.valorCondominio = a.getValorCondominio();
        f.areaM2 = a.getAreaM2();
        f.quartos = a.getQuartos();
        f.suites = a.getSuites();
        f.banheiros = a.getBanheiros();
        f.vagas = a.getVagas();
        f.andar = a.getAndar();
        f.cep = a.getCep();
        f.logradouro = a.getLogradouro();
        f.numero = a.getNumero();
        f.bairro = a.getBairro();
        f.cidade = a.getCidade();
        f.uf = a.getUf();
        for (Comodidade c : a.getComodidades()) {
            f.comodidadeIds.add(c.getId());
        }
        return f;
    }

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
    public List<Long> getComodidadeIds() { return comodidadeIds; }
    public void setComodidadeIds(List<Long> comodidadeIds) { this.comodidadeIds = comodidadeIds; }
}
