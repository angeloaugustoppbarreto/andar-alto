package com.andaralto.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.andaralto.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findAllByOrderByVendidoEmDesc();

    List<Venda> findByVendedorIdOrderByVendidoEmDesc(Long vendedorId);

    long countByVendedorId(Long vendedorId);

    // SUM de uma tabela vazia devolve NULL no SQL. Para os painéis mostrarem R$ 0
    // em vez de quebrar, as consultas abaixo são "embrulhadas" por métodos default
    // que trocam NULL por zero.
    @Query("select sum(v.valorFinal) from Venda v")
    BigDecimal somaBrutaValorFinal();

    @Query("select sum(v.comissaoValor) from Venda v")
    BigDecimal somaBrutaComissao();

    @Query("select sum(v.valorFinal) from Venda v where v.vendedor.id = :vendedorId")
    BigDecimal somaBrutaValorFinalDoVendedor(@Param("vendedorId") Long vendedorId);

    default BigDecimal somaValorFinal() {
        return semNulo(somaBrutaValorFinal());
    }

    default BigDecimal somaComissao() {
        return semNulo(somaBrutaComissao());
    }

    default BigDecimal somaValorFinalDoVendedor(Long vendedorId) {
        return semNulo(somaBrutaValorFinalDoVendedor(vendedorId));
    }

    private static BigDecimal semNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
