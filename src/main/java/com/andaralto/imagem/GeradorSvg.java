package com.andaralto.imagem;

import java.util.Locale;
import java.util.Random;

/**
 * Desenha as ilustrações FICTÍCIAS dos apartamentos em SVG (imagem vetorial: é só
 * um texto com formas geométricas, por isso não precisa de arquivo nem de foto real).
 *
 * <p>Tudo é determinístico: a mesma cena com a mesma semente sempre gera o mesmo desenho.
 * A semente escolhe a paleta de cores e o horário do dia (dia, fim de tarde ou noite).
 * É Java puro, sem Spring: dá para testar numa classe com {@code main}.
 */
public final class GeradorSvg {

    private static final int LARGURA = 960;
    private static final int ALTURA = 640;
    /** Linha do chão da fachada (onde o prédio encontra a calçada). */
    private static final int CHAO = 548;

    /** Conjunto de cores de um anúncio. */
    private record Paleta(String parede, String paredeSombra, String piso, String pisoSombra,
                          String madeira, String tecido, String destaque, String verde,
                          String fachada, String esquadria) {
    }

    /** Céu e luz do ambiente. */
    private record Ceu(String topo, String base, String astro, String silhueta, boolean noite) {
    }

    private static final Paleta[] PALETAS = {
        new Paleta("#ece4d6", "#ddd2bf", "#c49a6c", "#a97f52", "#8b5e3c", "#5c7f94", "#e0a93b", "#5d8a63", "#e4d9c8", "#2e3a46"),
        new Paleta("#dfe6db", "#cdd8c8", "#a98259", "#8d6a45", "#6f4a2d", "#3f6a5a", "#d98c5f", "#6c9a6f", "#d7e0d3", "#27352f"),
        new Paleta("#e4e6ea", "#d2d6dc", "#bfa58a", "#a38869", "#6d5340", "#3b4a63", "#e7b23c", "#7aa073", "#d8dbe0", "#1f2730"),
        new Paleta("#efe1dc", "#e2cfc8", "#c7a07a", "#a9835f", "#84563b", "#28576b", "#d1675c", "#5f8d7a", "#ecd9d2", "#26333b"),
        new Paleta("#f0efeb", "#e1dfd8", "#d1b184", "#b4936a", "#a9804f", "#8d98a5", "#3e7c7a", "#6f9b69", "#f0eee8", "#343a40"),
        new Paleta("#d5d9db", "#c2c8cb", "#8e7762", "#735e4c", "#5e4a3b", "#4c5560", "#c8793a", "#6d9470", "#c9cfd2", "#1c2227"),
    };

    /** Metade das imagens é de dia, um quarto de fim de tarde e um quarto de noite. */
    private static final Ceu DIA = new Ceu("#86c1e8", "#eaf5fb", "#fff4c2", "#b4cfe0", false);
    private static final Ceu TARDE = new Ceu("#f1a279", "#fbe4c9", "#fff0b8", "#dca88f", false);
    private static final Ceu NOITE = new Ceu("#1b2a4e", "#4b5b8c", "#f3f0d6", "#2a3a66", true);
    private static final Ceu[] CEUS = {DIA, DIA, TARDE, NOITE};

    private GeradorSvg() {
    }

    public static String gerar(CenaFicticia cena, long semente) {
        Paleta p = PALETAS[(int) Math.floorMod(semente, (long) PALETAS.length)];
        // "Embaralha" a semente para que números vizinhos (101, 102...) não tenham o mesmo horário.
        long mistura = semente * 0x9E3779B97F4A7C15L;
        mistura ^= mistura >>> 29;
        Ceu ceu = CEUS[(int) Math.floorMod(mistura, (long) CEUS.length)];
        Random r = new Random(semente * 7919L + cena.ordinal() * 104_729L + 17L);

        Desenho d = new Desenho();
        d.abrir("Ilustração fictícia: " + cena.getLegenda());
        switch (cena) {
            case FACHADA -> fachada(d, p, ceu, r, semente);
            case SALA -> sala(d, p, ceu, r);
            case QUARTO -> quarto(d, p, ceu, r);
            case COZINHA -> cozinha(d, p, ceu, r);
        }
        return d.fechar();
    }

    // =====================================================================
    // FACHADA
    // =====================================================================
    private static void fachada(Desenho d, Paleta p, Ceu ceu, Random r, long semente) {
        // Céu, sol (ou lua) e nuvens
        d.gradienteVertical("ceu", ceu.topo(), ceu.base());
        d.ret(0, 0, LARGURA, ALTURA, "url(#ceu)");
        double ax = (semente % 2 == 0) ? 130 + r.nextInt(120) : 700 + r.nextInt(120);
        if (!ceu.noite()) {
            d.circ(ax, 118, 78, ceu.astro(), 0.28);
        }
        d.circ(ax, 118, 36, ceu.astro(), 1);
        if (ceu.noite()) {
            for (int i = 0; i < 30; i++) {
                d.circ(r.nextInt(LARGURA), r.nextInt(280), 1 + r.nextDouble() * 1.4, "#ffffff", 0.75);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                nuvem(d, 60 + r.nextInt(780), 60 + r.nextInt(120), 0.8 + r.nextDouble() * 0.7);
            }
        }

        // Prédios distantes (silhueta)
        double x = -20;
        while (x < LARGURA) {
            double w = 52 + r.nextInt(70);
            double h = 130 + r.nextInt(230);
            d.ret(x, CHAO - h, w, h, ceu.silhueta(), 0, 0.92);
            if (ceu.noite()) {
                for (double jy = CHAO - h + 14; jy < CHAO - 20; jy += 24) {
                    for (double jx = x + 8; jx < x + w - 10; jx += 16) {
                        if (r.nextInt(100) < 22) {
                            d.ret(jx, jy, 6, 9, "#f6d36e", 0, 0.9);
                        }
                    }
                }
            }
            x += w + r.nextInt(10);
        }

        // Calçada e rua
        d.ret(0, CHAO, LARGURA, 36, "#cfcfc7");
        d.ret(0, CHAO + 36, LARGURA, 6, "#b3b3ab");
        d.ret(0, CHAO + 42, LARGURA, ALTURA - CHAO - 42, "#4f565d");
        for (int dx = 20; dx < LARGURA; dx += 130) {
            d.ret(dx, CHAO + 76, 66, 6, "#e9e9e1", 2);
        }

        // Prédio vizinho (do lado oposto ao do astro, para equilibrar a imagem)
        boolean vizinhoEsquerda = ax > 480;
        double larguraViz = 150;
        double xViz = vizinhoEsquerda ? -10 : LARGURA - larguraViz + 10;
        double topoViz = 250 + r.nextInt(50);
        String corViz = ajustar(p.fachada(), 0.86);
        d.ret(xViz, topoViz, larguraViz, CHAO - topoViz, corViz);
        d.ret(xViz - 6, topoViz - 8, larguraViz + 12, 10, ajustar(corViz, 0.8));
        for (double jy = topoViz + 22; jy < CHAO - 60; jy += 38) {
            for (double jx = xViz + 22; jx < xViz + larguraViz - 30; jx += 40) {
                janela(d, p, ceu, r, jx, jy, 24, 22, false);
            }
        }

        // Prédio principal
        double larg = 380 + r.nextInt(60);
        double x0 = (LARGURA - larg) / 2 + r.nextInt(61) - 30;
        int andares = 7 + r.nextInt(4);
        double topo = 74;
        double alturaTerreo = 78;
        double baseAndares = CHAO - alturaTerreo;
        double hAndar = (baseAndares - topo) / andares;

        // lateral em sombra, dá volume
        d.poli((x0 + larg) + "," + topo + " " + (x0 + larg + 36) + "," + (topo + 18) + " "
                + (x0 + larg + 36) + "," + CHAO + " " + (x0 + larg) + "," + CHAO, ajustar(p.fachada(), 0.66), 1);
        d.ret(x0, topo, larg, CHAO - topo, p.fachada());
        // platibanda, caixa d'água e antena
        d.ret(x0 - 8, topo - 14, larg + 16, 16, ajustar(p.fachada(), 0.78));
        d.ret(x0 + larg * 0.62, topo - 46, 54, 32, ajustar(p.fachada(), 0.7), 4);
        d.ret(x0 + larg * 0.62 - 4, topo - 50, 62, 8, ajustar(p.fachada(), 0.6), 3);
        d.linha(x0 + larg * 0.2, topo - 14, x0 + larg * 0.2, topo - 58, p.esquadria(), 3);
        d.circ(x0 + larg * 0.2, topo - 60, 4, "#d94b4b", 1);
        // pilares laterais
        d.ret(x0, topo, 12, CHAO - topo, ajustar(p.fachada(), 0.84));
        d.ret(x0 + larg - 12, topo, 12, CHAO - topo, ajustar(p.fachada(), 0.84));

        int colunas = 4;
        double margem = 30;
        double slot = (larg - 2 * margem) / colunas;
        for (int a = 0; a < andares; a++) {
            double yAndar = topo + a * hAndar;
            // laje da varanda
            d.ret(x0 - 3, yAndar + hAndar - 6, larg + 6, 6, ajustar(p.fachada(), 0.74));
            for (int c = 0; c < colunas; c++) {
                double wx = x0 + margem + c * slot + slot * 0.18;
                double ww = slot * 0.64;
                double wy = yAndar + hAndar * 0.2;
                double wh = hAndar * 0.5;
                janela(d, p, ceu, r, wx, wy, ww, wh, true);
                // guarda-corpo da varanda
                if ((a + c) % 2 == 0) {
                    d.ret(wx - 3, yAndar + hAndar - 22, ww + 6, 16, p.esquadria(), 0, 0.16);
                    d.ret(wx - 3, yAndar + hAndar - 22, ww + 6, 2, p.esquadria(), 0, 0.8);
                }
            }
        }

        // Térreo: lojas de vidro e portaria
        double yT = baseAndares;
        d.ret(x0, yT, larg, alturaTerreo, ajustar(p.fachada(), 0.9));
        double cx = x0 + larg / 2;
        d.ret(cx - 74, yT + 10, 148, alturaTerreo - 10, p.esquadria(), 3);
        d.ret(cx - 68, yT + 16, 64, alturaTerreo - 16, "#a9d2e6", 0, ceu.noite() ? 0.55 : 0.95);
        d.ret(cx + 4, yT + 16, 64, alturaTerreo - 16, "#a9d2e6", 0, ceu.noite() ? 0.55 : 0.95);
        d.ret(cx - 4, yT + 16, 8, alturaTerreo - 16, p.esquadria());
        d.ret(cx - 96, yT + 4, 192, 9, p.destaque(), 2);
        d.ret(cx + 82, yT + 24, 14, 22, "#ffffff", 2, 0.85);
        for (int lado = -1; lado <= 1; lado += 2) {
            double lx = lado < 0 ? x0 + 22 : x0 + larg - 22 - 96;
            d.ret(lx, yT + 20, 96, alturaTerreo - 26, p.esquadria(), 3);
            d.ret(lx + 5, yT + 25, 86, alturaTerreo - 36, ceu.noite() ? "#f4cf75" : "#9bc7de", 0, ceu.noite() ? 0.9 : 0.85);
        }
        d.ret(cx - 90, CHAO - 8, 180, 8, "#bdbdb4");

        // Árvores na calçada
        arvore(d, p, x0 - 52, CHAO, 1.0, r);
        arvore(d, p, x0 + larg + 70, CHAO, 0.85, r);
        d.ret(0, CHAO - 2, LARGURA, 2, "#000000", 0, 0.08);
    }

    /** Uma janela: vidro azulado (ou luz acesa), caixilho, reflexo e, às vezes, cortina. */
    private static void janela(Desenho d, Paleta p, Ceu ceu, Random r, double x, double y, double w, double h, boolean detalhada) {
        int chanceAcesa = ceu.noite() ? 55 : 14;
        boolean acesa = r.nextInt(100) < chanceAcesa;
        d.ret(x - 2, y - 2, w + 4, h + 4, p.esquadria(), 2);
        if (acesa) {
            d.ret(x, y, w, h, "#f6c85c");
            if (detalhada) {
                d.ret(x, y, w * 0.28, h, "#f1e4c3", 0, 0.8);
                d.ret(x + w * 0.72, y, w * 0.28, h, "#f1e4c3", 0, 0.8);
            }
        } else {
            d.ret(x, y, w, h, ceu.noite() ? "#243a62" : (ceu.topo().equals("#f1a279") ? "#7ea6c0" : "#7fb4d6"));
            d.poli(n(x) + "," + n(y + h) + " " + n(x + w * 0.5) + "," + n(y) + " " + n(x + w * 0.78) + "," + n(y) + " " + n(x + w * 0.28) + "," + n(y + h),
                    "#ffffff", ceu.noite() ? 0.08 : 0.2);
        }
        if (detalhada) {
            d.ret(x + w / 2 - 1, y, 2, h, p.esquadria(), 0, 0.85);
        }
    }

    private static void arvore(Desenho d, Paleta p, double x, double chao, double escala, Random r) {
        d.ret(x - 5 * escala, chao - 70 * escala, 10 * escala, 70 * escala, "#6b4a32");
        String folha = ajustar(p.verde(), 0.85);
        d.circ(x, chao - 96 * escala, 46 * escala, folha, 1);
        d.circ(x - 30 * escala, chao - 74 * escala, 32 * escala, p.verde(), 1);
        d.circ(x + 30 * escala, chao - 78 * escala, 34 * escala, ajustar(p.verde(), 1.1), 1);
        d.circ(x - 8 * escala, chao - 120 * escala, 28 * escala, ajustar(p.verde(), 1.18), 1);
    }

    private static void nuvem(Desenho d, double x, double y, double e) {
        d.elip(x, y, 54 * e, 16 * e, "#ffffff", 0.85);
        d.elip(x + 28 * e, y - 12 * e, 34 * e, 16 * e, "#ffffff", 0.85);
        d.elip(x - 26 * e, y - 8 * e, 30 * e, 13 * e, "#ffffff", 0.85);
    }

    // =====================================================================
    // SALA
    // =====================================================================
    private static void sala(Desenho d, Paleta p, Ceu ceu, Random r) {
        paredeEPiso(d, p, 436);

        // Janela com cortinas
        janelaDoAmbiente(d, p, ceu, 70, 68, 250, 270, true);
        // Quadros sobre o sofá
        quadro(d, p, 452, 92, 170, 120, 0);
        quadro(d, p, 640, 92, 104, 150, 1);
        quadro(d, p, 762, 112, 76, 76, 2);

        // Tapete e sombra do sofá
        d.elip(580, 560, 340, 58, p.destaque(), 0.38);
        d.elip(580, 560, 296, 44, p.parede(), 0.45);
        d.elip(570, 452, 250, 14, "#000000", 0.14);

        // Sofá
        String tec = p.tecido();
        String tecEscuro = ajustar(tec, 0.82);
        String tecClaro = ajustar(tec, 1.12);
        d.ret(356, 292, 428, 118, tecEscuro, 28);
        d.ret(340, 330, 50, 112, tecEscuro, 22);
        d.ret(750, 330, 50, 112, tecEscuro, 22);
        d.ret(388, 300, 182, 82, tecClaro, 18);
        d.ret(572, 300, 182, 82, tecClaro, 18);
        d.ret(380, 366, 190, 66, tec, 14);
        d.ret(572, 366, 190, 66, tec, 14);
        d.ret(380, 430, 382, 10, tecEscuro, 4);
        d.ret(356, 440, 10, 16, p.madeira(), 2);
        d.ret(774, 440, 10, 16, p.madeira(), 2);
        d.bruto("<g transform=\"rotate(-9 430 350)\">");
        d.ret(400, 322, 64, 64, p.destaque(), 14);
        d.bruto("</g><g transform=\"rotate(10 700 350)\">");
        d.ret(668, 326, 62, 62, ajustar(p.verde(), 1.0), 14);
        d.bruto("</g>");

        // Mesa de centro
        d.ret(478, 498, 210, 14, p.madeira(), 5);
        d.ret(492, 512, 10, 38, p.madeira(), 2);
        d.ret(664, 512, 10, 38, p.madeira(), 2);
        d.ret(512, 482, 64, 8, p.destaque(), 2);
        d.ret(516, 474, 56, 8, "#f4f1ea", 2);
        d.ret(620, 466, 26, 32, "#f4f1ea", 6);
        d.ret(630, 442, 4, 26, p.verde());
        d.elip(632, 440, 14, 8, p.verde(), 1);

        // Planta em frente à janela
        vaso(d, p, 270, 450, 1.1);
        // Luminária de piso
        luminaria(d, p, ceu, 872, 450, 250);

        luzDaJanela(d, ceu, 70, 340, 250);
        escurecerSeNoite(d, ceu);
        brilhoLampada(d, ceu, 872, 205, 140);
    }

    // =====================================================================
    // QUARTO
    // =====================================================================
    private static void quarto(Desenho d, Paleta p, Ceu ceu, Random r) {
        paredeEPiso(d, p, 440);

        // Janela com persiana
        double jx = 716, jy = 78, jw = 190, jh = 262;
        d.ret(jx - 8, jy - 8, jw + 16, jh + 16, "#f7f7f4", 4);
        d.gradienteVertical("ceuJ", ceu.topo(), ceu.base());
        d.bruto("<defs><clipPath id=\"jq\"><rect x=\"" + n(jx) + "\" y=\"" + n(jy) + "\" width=\"" + n(jw) + "\" height=\"" + n(jh) + "\"/></clipPath></defs><g clip-path=\"url(#jq)\">");
        d.ret(jx, jy, jw, jh, "url(#ceuJ)");
        d.circ(jx + 130, jy + 70, 24, ceu.astro(), 1);
        double sx = jx;
        while (sx < jx + jw) {
            double w = 30 + r.nextInt(36);
            double h = 60 + r.nextInt(120);
            d.ret(sx, jy + jh - h, w, h, ceu.silhueta(), 0, 0.9);
            sx += w + 2;
        }
        d.bruto("</g>");
        for (double sy = jy; sy < jy + jh * 0.55; sy += 16) {
            d.ret(jx - 2, sy, jw + 4, 11, "#f2efe8", 2, 0.97);
            d.ret(jx - 2, sy + 10, jw + 4, 2, "#000000", 0, 0.1);
        }
        d.ret(jx - 14, jy + jh + 6, jw + 28, 10, "#ffffff", 3);

        // Quadros na parede
        quadro(d, p, 330, 52, 90, 70, 1);
        quadro(d, p, 436, 40, 90, 90, 2);
        quadro(d, p, 542, 52, 90, 70, 0);

        // Tapete
        d.ret(150, 500, 660, 116, p.destaque(), 14, 0.34);
        d.ret(168, 512, 624, 92, p.parede(), 10, 0.4);
        d.elip(480, 506, 320, 18, "#000000", 0.14);

        // Cama: cabeceira, colchão, edredom, travesseiros
        d.ret(236, 140, 488, 230, p.madeira(), 20);
        d.ret(254, 158, 452, 196, ajustar(p.tecido(), 1.08), 14);
        for (double tx = 254 + 56; tx < 254 + 452; tx += 56) {
            d.ret(tx, 158, 2, 196, "#000000", 0, 0.1);
        }
        d.ret(226, 338, 508, 128, "#f5f2ea", 16);
        d.ret(226, 372, 508, 98, ajustar(p.tecido(), 0.9), 16);
        d.ret(226, 372, 508, 26, p.destaque(), 8);
        d.ret(226, 410, 508, 5, "#ffffff", 0, 0.14);
        d.ret(226, 432, 508, 5, "#ffffff", 0, 0.14);
        d.ret(268, 306, 132, 60, "#fbfaf6", 24);
        d.ret(414, 304, 132, 62, "#fbfaf6", 24);
        d.ret(560, 306, 132, 60, "#fbfaf6", 24);
        d.ret(300, 330, 70, 14, "#000000", 7, 0.05);
        d.ret(222, 464, 516, 44, p.madeira(), 8);
        d.ret(244, 506, 14, 22, ajustar(p.madeira(), 0.8), 2);
        d.ret(702, 506, 14, 22, ajustar(p.madeira(), 0.8), 2);

        // Criados-mudos com abajur
        criadoMudo(d, p, ceu, 104, 392);
        criadoMudo(d, p, ceu, 764, 392);

        vaso(d, p, 40, 470, 0.9);

        luzDaJanela(d, ceu, 716, 340, 190);
        escurecerSeNoite(d, ceu);
        brilhoLampada(d, ceu, 152, 352, 120);
        brilhoLampada(d, ceu, 812, 352, 120);
    }

    private static void criadoMudo(Desenho d, Paleta p, Ceu ceu, double x, double y) {
        d.ret(x, y, 96, 98, p.madeira(), 6);
        d.ret(x + 8, y + 10, 80, 36, ajustar(p.madeira(), 1.12), 3);
        d.ret(x + 8, y + 52, 80, 36, ajustar(p.madeira(), 1.12), 3);
        d.ret(x + 38, y + 24, 20, 4, "#e9dfc9", 2);
        d.ret(x + 38, y + 66, 20, 4, "#e9dfc9", 2);
        d.ret(x + 4, y + 98, 8, 14, ajustar(p.madeira(), 0.8));
        d.ret(x + 84, y + 98, 8, 14, ajustar(p.madeira(), 0.8));
        // abajur
        d.ret(x + 46, y - 34, 4, 34, "#6b6b6b");
        d.ret(x + 32, y - 6, 32, 6, "#6b6b6b", 2);
        d.poli(n(x + 22) + "," + n(y - 30) + " " + n(x + 74) + "," + n(y - 30) + " " + n(x + 64) + "," + n(y - 66) + " " + n(x + 32) + "," + n(y - 66),
                ceu.noite() ? "#ffe9a8" : "#f3ead6", 1);
    }

    // =====================================================================
    // COZINHA
    // =====================================================================
    private static void cozinha(Desenho d, Paleta p, Ceu ceu, Random r) {
        d.ret(0, 0, LARGURA, ALTURA, p.parede());
        // Piso xadrez
        d.ret(0, 570, LARGURA, 70, "#e8e6df");
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 2; j++) {
                if ((i + j) % 2 == 0) {
                    d.ret(i * 40, 570 + j * 35, 40, 35, ajustar(p.pisoSombra(), 0.9), 0, 0.45);
                }
            }
        }
        d.ret(0, 570, LARGURA, 6, "#000000", 0, 0.12);

        // Azulejos entre os armários
        d.ret(40, 244, 680, 124, "#f4f5f2");
        for (double gx = 40; gx <= 720; gx += 34) {
            d.ret(gx, 244, 1.6, 124, "#d6dad3");
        }
        for (double gy = 244; gy <= 368; gy += 31) {
            d.ret(40, gy, 680, 1.6, "#d6dad3");
        }

        String armario = p.tecido();
        String armarioClaro = ajustar(armario, 1.14);
        String puxador = "#e3dccd";

        // Armários altos (deixam um vão para o exaustor)
        double[][] altos = {{40, 142}, {186, 142}, {474, 122}, {600, 120}};
        for (double[] a : altos) {
            d.ret(a[0], 64, a[1] - 4, 172, armarioClaro, 5);
            d.ret(a[0] + a[1] - 22, 190, 4, 30, puxador, 2);
        }
        // Exaustor
        d.ret(372, 52, 58, 120, "#c3c9ce", 3);
        d.poli("330,230 472,230 436,168 366,168", "#aeb5bb", 1);
        d.ret(330, 228, 142, 6, "#8f979d", 2);

        // Bancada, armários baixos e rodapé
        d.ret(40, 384, 680, 184, armario, 5);
        for (double bx = 40; bx < 720; bx += 113.4) {
            d.ret(bx + 2, 394, 109, 164, ajustar(armario, 0.93), 4);
            d.ret(bx + 46, 402, 20, 4, puxador, 2);
        }
        d.ret(36, 366, 688, 20, "#ebe8e1", 3);
        d.ret(36, 384, 688, 4, "#000000", 0, 0.14);
        d.ret(40, 560, 680, 10, "#23272b");

        // Pia e torneira
        d.ret(126, 362, 110, 6, "#aab0b6", 2);
        d.bruto("<path d=\"M182 362 V326 Q182 312 196 312 H206\" fill=\"none\" stroke=\"#9aa2a9\" stroke-width=\"7\" stroke-linecap=\"round\"/>");

        // Fogão com panela e vapor
        d.ret(366, 358, 108, 9, "#2b2f33", 2);
        d.elip(396, 360, 16, 3, "#555b61", 1);
        d.elip(444, 360, 16, 3, "#555b61", 1);
        d.ret(402, 312, 46, 46, "#c7ccd1", 5);
        d.ret(398, 306, 54, 9, "#aeb4ba", 4);
        d.bruto("<path d=\"M418 296 q-8 -14 0 -26 q8 -12 0 -24 M436 298 q-8 -14 0 -26 q8 -12 0 -24\" fill=\"none\" stroke=\"#ffffff\" stroke-width=\"4\" stroke-linecap=\"round\" opacity=\"0.65\"/>");

        // Fruteira e planta na bancada
        d.ret(540, 350, 96, 14, p.madeira(), 7);
        d.circ(560, 342, 12, "#e0a93b", 1);
        d.circ(582, 340, 12, "#d1675c", 1);
        d.circ(604, 342, 12, p.verde(), 1);
        vaso(d, p, 668, 364, 0.45);

        // Geladeira
        d.ret(744, 88, 172, 482, "#dde2e5", 16);
        d.ret(744, 250, 172, 5, "#000000", 0, 0.18);
        d.ret(764, 130, 8, 90, "#9ba3aa", 4);
        d.ret(764, 280, 8, 150, "#9ba3aa", 4);
        d.ret(800, 160, 28, 24, p.destaque(), 3);
        d.ret(838, 186, 22, 28, "#ffffff", 3);
        d.ret(744, 560, 172, 10, "#23272b");

        escurecerSeNoite(d, ceu);
    }

    // =====================================================================
    // Peças reaproveitadas pelas cenas
    // =====================================================================

    private static void paredeEPiso(Desenho d, Paleta p, int alturaParede) {
        d.ret(0, 0, LARGURA, alturaParede, p.parede());
        d.gradienteVertical("sombraParede", "#000000", "#000000", 0.0, 0.07);
        d.ret(0, 0, LARGURA, alturaParede, "url(#sombraParede)");
        d.ret(0, alturaParede - 14, LARGURA, 18, p.paredeSombra());
        d.ret(0, alturaParede + 4, LARGURA, ALTURA - alturaParede - 4, p.piso());
        for (int y = alturaParede + 4, linha = 0; y < ALTURA; y += 24, linha++) {
            d.ret(0, y, LARGURA, 1.6, p.pisoSombra(), 0, 0.75);
            for (int x = (linha % 2) * 90 + 40; x < LARGURA; x += 180) {
                d.ret(x, y, 1.6, 24, p.pisoSombra(), 0, 0.55);
            }
        }
        d.gradienteVertical("sombraPiso", "#000000", "#000000", 0.0, 0.22);
        d.ret(0, alturaParede + 4, LARGURA, ALTURA - alturaParede - 4, "url(#sombraPiso)");
    }

    private static void janelaDoAmbiente(Desenho d, Paleta p, Ceu ceu, double x, double y, double w, double h, boolean cortinas) {
        d.ret(x - 8, y - 8, w + 16, h + 16, "#f7f7f4", 4);
        d.gradienteVertical("ceuJanela", ceu.topo(), ceu.base());
        d.bruto("<defs><clipPath id=\"jn\"><rect x=\"" + n(x) + "\" y=\"" + n(y) + "\" width=\"" + n(w) + "\" height=\"" + n(h) + "\"/></clipPath></defs><g clip-path=\"url(#jn)\">");
        d.ret(x, y, w, h, "url(#ceuJanela)");
        d.circ(x + w * 0.74, y + 72, 26, ceu.astro(), 1);
        double sx = x;
        int i = 0;
        while (sx < x + w) {
            double largura = 34 + (i * 17 % 30);
            double altura = 70 + (i * 53 % 110);
            d.ret(sx, y + h - altura, largura, altura, ceu.silhueta(), 0, 0.92);
            sx += largura + 3;
            i++;
        }
        d.bruto("</g>");
        d.ret(x + w / 2 - 2, y, 4, h, "#f7f7f4");
        d.ret(x, y + h * 0.55, w, 4, "#f7f7f4");
        d.ret(x - 16, y + h + 8, w + 32, 12, "#ffffff", 3);
        d.ret(x - 16, y + h + 18, w + 32, 3, "#000000", 0, 0.1);
        if (cortinas) {
            String tecido = ajustar(p.destaque(), 1.05);
            d.ret(x - 44, y - 24, w + 88, 7, p.madeira(), 3);
            for (int lado = 0; lado < 2; lado++) {
                double cx = lado == 0 ? x - 40 : x + w - 26;
                d.ret(cx, y - 18, 66, h + 70, tecido, 6, 0.96);
                for (int k = 1; k < 6; k++) {
                    d.ret(cx + k * 11, y - 18, 2, h + 70, "#000000", 0, 0.08);
                }
            }
        }
    }

    private static void quadro(Desenho d, Paleta p, double x, double y, double w, double h, int estilo) {
        d.ret(x, y, w, h, "#fbfaf6", 3);
        d.ret(x + 8, y + 8, w - 16, h - 16, ajustar(p.parede(), 0.96));
        switch (estilo) {
            case 0 -> {
                d.circ(x + w * 0.35, y + h * 0.42, Math.min(w, h) * 0.22, p.destaque(), 1);
                d.ret(x + 8, y + h * 0.66, w - 16, h * 0.34 - 8, p.tecido(), 0, 0.9);
            }
            case 1 -> {
                d.ret(x + w * 0.2, y + 16, w * 0.22, h - 32, p.verde(), 4, 0.95);
                d.ret(x + w * 0.5, y + 28, w * 0.26, h - 56, p.destaque(), 4, 0.95);
            }
            default -> {
                d.circ(x + w / 2, y + h / 2, Math.min(w, h) * 0.3, p.tecido(), 0.95);
                d.circ(x + w / 2, y + h / 2, Math.min(w, h) * 0.14, p.parede(), 1);
            }
        }
        d.ret(x, y, w, 3, "#000000", 0, 0.08);
    }

    private static void vaso(Desenho d, Paleta p, double x, double chao, double e) {
        d.poli(n(x - 26 * e) + "," + n(chao - 56 * e) + " " + n(x + 26 * e) + "," + n(chao - 56 * e) + " " + n(x + 19 * e) + "," + n(chao) + " " + n(x - 19 * e) + "," + n(chao),
                "#f1ede4", 1);
        d.ret(x - 28 * e, chao - 62 * e, 56 * e, 10 * e, "#e2ddd0", 3);
        double[][] folhas = {{-34, -118, -28}, {-8, -150, -6}, {26, -128, 24}, {40, -96, 52}, {-44, -90, -52}, {4, -104, 0}};
        for (double[] f : folhas) {
            d.bruto("<g transform=\"rotate(" + n(f[2]) + " " + n(x + f[0] * e * 0.4) + " " + n(chao - 60 * e) + ")\">");
            d.elip(x + f[0] * e * 0.7, chao + f[1] * e * 0.8, 15 * e, 38 * e, p.verde(), 0.96);
            d.bruto("</g>");
        }
    }

    private static void luminaria(Desenho d, Paleta p, Ceu ceu, double x, double chao, double topo) {
        d.ret(x - 22, chao - 6, 44, 6, "#4c4c4c", 3);
        d.ret(x - 2, topo + 30, 4, chao - topo - 30, "#4c4c4c");
        d.poli(n(x - 34) + "," + n(topo + 44) + " " + n(x + 34) + "," + n(topo + 44) + " " + n(x + 22) + "," + n(topo) + " " + n(x - 22) + "," + n(topo),
                ceu.noite() ? "#ffe9a8" : "#f5eddc", 1);
    }

    private static void luzDaJanela(Desenho d, Ceu ceu, double x, double yChao, double largura) {
        if (ceu.noite()) {
            return;
        }
        String cor = ceu.topo().equals("#f1a279") ? "#ffd9a6" : "#fff8dc";
        d.poli(n(x) + "," + n(yChao + 100) + " " + n(x + largura) + "," + n(yChao + 100) + " " + n(x + largura + 130) + "," + n(yChao + 250) + " " + n(x - 60) + "," + n(yChao + 250),
                cor, 0.2);
    }

    /** À noite, escurece o ambiente; as luminárias (desenhadas depois) brilham mais. */
    private static void escurecerSeNoite(Desenho d, Ceu ceu) {
        if (ceu.noite()) {
            d.ret(0, 0, LARGURA, ALTURA, "#101c3d", 0, 0.34);
        }
    }

    private static void brilhoLampada(Desenho d, Ceu ceu, double x, double y, double raio) {
        if (!ceu.noite()) {
            return;
        }
        String id = "brilho" + (int) x;
        d.gradienteRadial(id, "#ffe08a", 0.75, 0.0);
        d.circ(x, y, raio * 1.6, "url(#" + id + ")", 1);
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    /** Escurece (fator &lt; 1) ou clareia (fator &gt; 1) uma cor "#rrggbb". */
    static String ajustar(String hex, double fator) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        int[] canais = {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        StringBuilder sb = new StringBuilder("#");
        for (int c : canais) {
            double v = fator <= 1 ? c * fator : c + (255 - c) * (fator - 1);
            sb.append(String.format("%02x", Math.max(0, Math.min(255, (int) Math.round(v)))));
        }
        return sb.toString();
    }

    private static String n(double v) {
        return Desenho.n(v);
    }

    /** Pequeno "pincel" que monta o texto do SVG. */
    private static final class Desenho {
        private final StringBuilder sb = new StringBuilder(14_000);

        void abrir(String titulo) {
            sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(LARGURA).append(' ')
              .append(ALTURA).append("\" role=\"img\" aria-label=\"").append(titulo).append("\">")
              .append("<title>").append(titulo).append("</title>");
        }

        String fechar() {
            return sb.append("</svg>").toString();
        }

        void bruto(String svg) {
            sb.append(svg);
        }

        void ret(double x, double y, double w, double h, String cor) {
            ret(x, y, w, h, cor, 0, 1);
        }

        void ret(double x, double y, double w, double h, String cor, double raio) {
            ret(x, y, w, h, cor, raio, 1);
        }

        void ret(double x, double y, double w, double h, String cor, double raio, double opacidade) {
            sb.append("<rect x=\"").append(n(x)).append("\" y=\"").append(n(y))
              .append("\" width=\"").append(n(w)).append("\" height=\"").append(n(h)).append('"');
            if (raio > 0) {
                sb.append(" rx=\"").append(n(raio)).append('"');
            }
            preencher(cor, opacidade);
        }

        void circ(double cx, double cy, double r, String cor, double opacidade) {
            sb.append("<circle cx=\"").append(n(cx)).append("\" cy=\"").append(n(cy)).append("\" r=\"").append(n(r)).append('"');
            preencher(cor, opacidade);
        }

        void elip(double cx, double cy, double rx, double ry, String cor, double opacidade) {
            sb.append("<ellipse cx=\"").append(n(cx)).append("\" cy=\"").append(n(cy))
              .append("\" rx=\"").append(n(rx)).append("\" ry=\"").append(n(ry)).append('"');
            preencher(cor, opacidade);
        }

        void poli(String pontos, String cor, double opacidade) {
            sb.append("<polygon points=\"").append(pontos).append('"');
            preencher(cor, opacidade);
        }

        void linha(double x1, double y1, double x2, double y2, String cor, double largura) {
            sb.append("<line x1=\"").append(n(x1)).append("\" y1=\"").append(n(y1)).append("\" x2=\"").append(n(x2))
              .append("\" y2=\"").append(n(y2)).append("\" stroke=\"").append(cor).append("\" stroke-width=\"")
              .append(n(largura)).append("\"/>");
        }

        void gradienteVertical(String id, String topo, String base) {
            gradienteVertical(id, topo, base, 1, 1);
        }

        void gradienteVertical(String id, String topo, String base, double opTopo, double opBase) {
            sb.append("<defs><linearGradient id=\"").append(id).append("\" x1=\"0\" y1=\"0\" x2=\"0\" y2=\"1\">")
              .append("<stop offset=\"0\" stop-color=\"").append(topo).append("\" stop-opacity=\"").append(n(opTopo)).append("\"/>")
              .append("<stop offset=\"1\" stop-color=\"").append(base).append("\" stop-opacity=\"").append(n(opBase)).append("\"/>")
              .append("</linearGradient></defs>");
        }

        void gradienteRadial(String id, String cor, double opCentro, double opBorda) {
            sb.append("<defs><radialGradient id=\"").append(id).append("\">")
              .append("<stop offset=\"0\" stop-color=\"").append(cor).append("\" stop-opacity=\"").append(n(opCentro)).append("\"/>")
              .append("<stop offset=\"1\" stop-color=\"").append(cor).append("\" stop-opacity=\"").append(n(opBorda)).append("\"/>")
              .append("</radialGradient></defs>");
        }

        private void preencher(String cor, double opacidade) {
            sb.append(" fill=\"").append(cor).append('"');
            if (opacidade < 1) {
                sb.append(" fill-opacity=\"").append(n(opacidade)).append('"');
            }
            sb.append("/>");
        }

        /** Números sem casas inúteis (12 em vez de 12.0) e sempre com ponto decimal. */
        static String n(double v) {
            if (v == Math.rint(v)) {
                return Long.toString((long) v);
            }
            return String.format(Locale.ROOT, "%.1f", v);
        }
    }
}
