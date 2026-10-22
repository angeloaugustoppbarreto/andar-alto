package com.andaralto.web;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.andaralto.exception.NaoEncontradoException;
import com.andaralto.imagem.CenaFicticia;
import com.andaralto.imagem.GeradorSvg;

/**
 * Entrega as ilustrações FICTÍCIAS dos apartamentos.
 * Exemplo de endereço: /imagens/ficticias/sala/4242.svg
 *
 * <p>A imagem é desenhada na hora pelo {@link GeradorSvg} e o navegador guarda
 * uma cópia (cache), então cada desenho é gerado uma vez só por visitante.
 */
@Controller
public class ImagemController {

    private static final MediaType SVG = MediaType.parseMediaType("image/svg+xml;charset=UTF-8");

    @GetMapping("/imagens/ficticias/{cena}/{semente}.svg")
    public ResponseEntity<byte[]> svg(@PathVariable String cena, @PathVariable long semente) {
        CenaFicticia c = CenaFicticia.porSlug(cena)
                .orElseThrow(() -> new NaoEncontradoException("Imagem não encontrada."));
        byte[] corpo = GeradorSvg.gerar(c, semente).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(SVG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(corpo);
    }
}
