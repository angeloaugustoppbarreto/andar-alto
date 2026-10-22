package com.andaralto.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andaralto.model.Venda;
import com.andaralto.repository.VendaRepository;

@Service
public class VendaService {

    private final VendaRepository vendas;

    public VendaService(VendaRepository vendas) {
        this.vendas = vendas;
    }

    @Transactional(readOnly = true)
    public List<Venda> listarTodas() {
        return vendas.findAllByOrderByVendidoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<Venda> listarDoVendedor(Long vendedorId) {
        return vendas.findByVendedorIdOrderByVendidoEmDesc(vendedorId);
    }
}
