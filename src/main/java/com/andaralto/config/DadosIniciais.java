package com.andaralto.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.andaralto.model.Apartamento;
import com.andaralto.model.Comodidade;
import com.andaralto.model.Perfil;
import com.andaralto.model.Proposta;
import com.andaralto.model.Usuario;
import com.andaralto.repository.UsuarioRepository;
import com.andaralto.service.ApartamentoService;
import com.andaralto.service.ComodidadeService;
import com.andaralto.service.FavoritoService;
import com.andaralto.service.PropostaService;
import com.andaralto.service.UsuarioService;
import com.andaralto.web.form.ApartamentoForm;

/**
 * Roda uma vez, logo depois que a aplicação sobe (CommandLineRunner):
 * 1) cria o administrador, se ainda não existir;
 * 2) se app.demo.dados=true e o banco estiver sem usuários, cria uma vitrine de
 *    demonstração COMPLETA e fictícia: vendedores, compradores, 14 anúncios em todas
 *    as situações, propostas, favoritos e até uma venda concluída.
 *
 * <p>Tudo passa pelos mesmos services que as telas usam. Assim, os dados de
 * demonstração respeitam as regras do negócio (ex.: anúncio nasce "em análise").
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);
    private static final String SENHA_DEMO = "demo123";

    private final AppProperties props;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarios;
    private final ApartamentoService apartamentos;
    private final ComodidadeService comodidades;
    private final PropostaService propostas;
    private final FavoritoService favoritos;

    public DadosIniciais(AppProperties props, UsuarioRepository usuarioRepository, UsuarioService usuarios,
                         ApartamentoService apartamentos, ComodidadeService comodidades,
                         PropostaService propostas, FavoritoService favoritos) {
        this.props = props;
        this.usuarioRepository = usuarioRepository;
        this.usuarios = usuarios;
        this.apartamentos = apartamentos;
        this.comodidades = comodidades;
        this.propostas = propostas;
        this.favoritos = favoritos;
    }

    @Override
    public void run(String... args) {
        boolean bancoVazio = usuarioRepository.count() == 0;

        if (usuarioRepository.countByPerfil(Perfil.ADMIN) == 0) {
            usuarios.criar("Administração Andar Alto", props.admin().email(), props.admin().senha(),
                    null, Perfil.ADMIN);
            log.info("Administrador criado: {}", props.admin().email());
        }

        if (props.demo().dados() && bancoVazio) {
            try {
                criarDemonstracao();
                log.info("Dados de demonstração criados. Senha de todos os usuários de teste: {}", SENHA_DEMO);
            } catch (RuntimeException e) {
                // A demonstração é opcional: se algo der errado, o site sobe mesmo assim.
                log.error("Não foi possível criar os dados de demonstração. "
                        + "Apague a pasta ./data e tente de novo, ou use app.demo.dados=false.", e);
            }
        }
    }

    private void criarDemonstracao() {
        Usuario carla = usuarios.criar("Carla Menezes", "carla@andaralto.demo", SENHA_DEMO, "(81) 98888-1001", Perfil.VENDEDOR);
        Usuario roberto = usuarios.criar("Roberto Lima", "roberto@andaralto.demo", SENHA_DEMO, "(11) 97777-2002", Perfil.VENDEDOR);
        Usuario ana = usuarios.criar("Ana Souza", "ana@andaralto.demo", SENHA_DEMO, "(81) 96666-3003", Perfil.COMPRADOR);
        Usuario bruno = usuarios.criar("Bruno Teixeira", "bruno@andaralto.demo", SENHA_DEMO, "(81) 95555-4004", Perfil.COMPRADOR);
        Usuario marina = usuarios.criar("Marina Duarte", "marina@andaralto.demo", SENHA_DEMO, "(11) 94444-5005", Perfil.COMPRADOR);

        Map<String, Long> idPorNome = new HashMap<>();
        for (Comodidade c : comodidades.listar()) {
            idPorNome.put(c.getNome(), c.getId());
        }

        Long carlaId = carla.getId();
        Long robertoId = roberto.getId();

        // ---------------------------------------------------------- anúncios da Carla (Recife e região)
        Apartamento a1 = anuncio(carlaId, idPorNome,
                "Três quartos com vista para o mar em Boa Viagem",
                "Apartamento amplo e ventilado, de frente para o mar, com sala em dois ambientes e varanda gourmet. "
                        + "A suíte principal tem closet e o prédio conta com lazer completo. Pronto para morar.",
                890_000, 1_100, 98, 3, 1, 2, 2, 12, "51020-000", "Avenida das Jangadas", "1450",
                "Boa Viagem", "Recife", "PE",
                "Vista para o mar", "Varanda gourmet", "Piscina", "Academia", "Elevador", "Portaria 24 horas");

        Apartamento a2 = anuncio(carlaId, idPorNome,
                "Dois quartos reformados nas Graças",
                "Reforma recente, piso novo e cozinha integrada à sala. Rua arborizada, perto de padaria, escola e "
                        + "farmácia. O condomínio tem portaria 24 horas e salão de festas.",
                520_000, 780, 64, 2, 0, 1, 1, 6, "52050-000", "Rua dos Sabiás", "310",
                "Graças", "Recife", "PE",
                "Elevador", "Portaria 24 horas", "Salão de festas", "Armários embutidos");

        Apartamento a3 = anuncio(carlaId, idPorNome,
                "Apartamento arejado em Casa Caiada, perto da praia",
                "A poucos minutos da orla de Olinda, com varanda, sala clara e quartos com armários. "
                        + "Prédio tranquilo e bem cuidado, ideal para quem busca sossego.",
                395_000, 520, 70, 2, 0, 1, 1, 3, "53130-000", "Rua das Mangueiras", "88",
                "Casa Caiada", "Olinda", "PE",
                "Aceita pets", "Playground", "Churrasqueira");

        Apartamento a4 = anuncio(carlaId, idPorNome,
                "Studio moderno e mobiliado em Setúbal",
                "Studio compacto com mobília planejada, ar-condicionado e varanda. Ótimo para morar sozinho ou "
                        + "investir em locação de curta temporada. Prédio novo com coworking.",
                289_000, 430, 32, 0, 0, 1, 1, 9, "51130-000", "Rua dos Coqueirais", "205",
                "Setúbal", "Recife", "PE",
                "Mobiliado", "Ar-condicionado", "Coworking", "Elevador");

        Apartamento a5 = anuncio(carlaId, idPorNome,
                "Cobertura-garden com quatro quartos na Madalena",
                "Quatro quartos, sendo duas suítes, sala para três ambientes e terraço com churrasqueira. "
                        + "Acabamento de alto padrão, três vagas e depósito privativo.",
                1_450_000, 1_900, 160, 4, 2, 4, 3, 15, "50610-000", "Rua do Ipê Amarelo", "720",
                "Madalena", "Recife", "PE",
                "Churrasqueira", "Piscina", "Academia", "Elevador", "Portaria 24 horas", "Varanda gourmet");

        Apartamento a6 = anuncio(carlaId, idPorNome,
                "Três quartos com lazer completo em Piedade",
                "Apartamento de esquina, muito iluminado, a duas quadras da praia. O condomínio oferece quadra, "
                        + "piscina e salão de festas.",
                610_000, 850, 85, 3, 1, 2, 1, 8, "54400-000", "Avenida Litorânea", "2300",
                "Piedade", "Jaboatão", "PE",
                "Piscina", "Quadra esportiva", "Salão de festas", "Elevador");

        Apartamento a7 = anuncio(carlaId, idPorNome,
                "Dois quartos silenciosos em Casa Forte",
                "Em rua residencial e arborizada, com varanda para o verde. Apartamento conservado, "
                        + "com armários na cozinha e nos quartos.",
                640_000, 900, 72, 2, 1, 2, 1, 5, "52061-000", "Rua das Acácias", "157",
                "Casa Forte", "Recife", "PE",
                "Varanda gourmet", "Armários embutidos", "Elevador");

        // ---------------------------------------------------------- anúncios do Roberto (outras capitais)
        Apartamento a8 = anuncio(robertoId, idPorNome,
                "Dois quartos com varanda em Pinheiros",
                "Perto do metrô e de cafés, o apartamento tem varanda com vista aberta, piso de madeira e "
                        + "lavanderia. Condomínio com academia e coworking.",
                980_000, 1_350, 68, 2, 1, 2, 1, 10, "05422-000", "Rua dos Ipês Roxos", "540",
                "Pinheiros", "São Paulo", "SP",
                "Varanda gourmet", "Academia", "Coworking", "Lavanderia", "Elevador");

        Apartamento a9 = anuncio(robertoId, idPorNome,
                "Três quartos espaçosos na Vila Mariana",
                "Sala ampla com duas varandas, cozinha planejada e suíte com banheira. Duas vagas cobertas e "
                        + "portaria 24 horas. A pé de parque e mercado.",
                1_280_000, 1_700, 105, 3, 1, 3, 2, 7, "04117-000", "Rua das Paineiras", "99",
                "Vila Mariana", "São Paulo", "SP",
                "Portaria 24 horas", "Salão de festas", "Playground", "Elevador", "Aceita pets");

        Apartamento a10 = anuncio(robertoId, idPorNome,
                "Um quarto compacto e bem planejado em Moema",
                "Ideal para quem mora sozinho ou casal. Cozinha americana, banheiro reformado e andar alto, "
                        + "com vista livre.",
                560_000, 820, 45, 1, 0, 1, 1, 14, "04077-000", "Alameda dos Jacarandás", "1020",
                "Moema", "São Paulo", "SP",
                "Elevador", "Academia", "Mobiliado");

        Apartamento a11 = anuncio(robertoId, idPorNome,
                "Dois quartos com vista para a baía em Botafogo",
                "Sala com janelões e vista para a baía, dois quartos e cozinha ampla. Prédio clássico "
                        + "com portaria 24 horas, a poucos passos do metrô.",
                1_100_000, 1_500, 75, 2, 0, 2, 0, 5, "22250-000", "Rua das Gaivotas", "36",
                "Botafogo", "Rio de Janeiro", "RJ",
                "Vista para o mar", "Portaria 24 horas", "Elevador");

        Apartamento a12 = anuncio(robertoId, idPorNome,
                "Três quartos com suíte na Savassi",
                "Apartamento claro, com sala integrada à varanda, suíte master e dependência completa. "
                        + "Prédio com lazer no último andar.",
                870_000, 1_250, 110, 3, 1, 3, 2, 11, "30140-000", "Rua dos Ourives", "812",
                "Savassi", "Belo Horizonte", "MG",
                "Churrasqueira", "Piscina", "Academia", "Elevador", "Salão de festas");

        Apartamento a13 = anuncio(robertoId, idPorNome,
                "Dois quartos prontos para morar no Batel",
                "Acabamento moderno, cozinha equipada e varanda com churrasqueira. Região com restaurantes "
                        + "e parques. Uma vaga coberta.",
                720_000, 980, 70, 2, 1, 2, 1, 8, "80420-000", "Rua das Araucárias", "245",
                "Batel", "Curitiba", "PR",
                "Churrasqueira", "Elevador", "Aceita pets");

        Apartamento a14 = anuncio(robertoId, idPorNome,
                "Três quartos na Barra com vista para o farol",
                "Apartamento em andar baixo, com varanda e armários. Prédio com portaria e piscina.",
                950_000, null, 120, 3, 1, 2, 2, 4, "40140-000", "Avenida dos Faróis", "1500",
                "Barra", "Salvador", "BA",
                "Piscina", "Portaria 24 horas");

        // ------------------------------------------------ moderação: o admin aprova quase todos
        for (Apartamento a : List.of(a1, a2, a3, a5, a6, a7, a8, a9, a10, a11, a12)) {
            apartamentos.aprovar(a.getId());
        }
        // a4 e a13 continuam "em análise" (fila do administrador).
        apartamentos.rejeitar(a14.getId(),
                "Faltou informar o valor do condomínio e o estado de conservação do imóvel. "
                        + "Complete a descrição e envie novamente.");
        apartamentos.pausar(a3.getId(), carlaId); // a vendedora pausou este anúncio
        for (Apartamento a : List.of(a1, a5, a8)) {
            apartamentos.alternarDestaque(a.getId());
        }

        // ------------------------------------------------------------- propostas
        propostas.enviar(a1.getId(), ana.getId(), valor(860_000),
                "Tenho interesse e financiamento pré-aprovado. Aceita negociar o valor?");
        propostas.enviar(a2.getId(), bruno.getId(), valor(495_000), "Posso visitar neste fim de semana.");
        propostas.enviar(a8.getId(), marina.getId(), valor(940_000), null);
        propostas.enviar(a9.getId(), ana.getId(), valor(1_200_000), "Pagamento à vista, se o preço fechar.");

        // a6: duas propostas; a vendedora aceita a da Ana e a do Bruno é recusada automaticamente.
        propostas.enviar(a6.getId(), bruno.getId(), valor(570_000), null);
        Proposta aceitaA6 = propostas.enviar(a6.getId(), ana.getId(), valor(590_000),
                "Fechamos por este valor?");
        propostas.aceitar(aceitaA6.getId(), carlaId); // a6 vira RESERVADO

        // a7: negócio fechado, com comissão calculada pela plataforma.
        Proposta aceitaA7 = propostas.enviar(a7.getId(), bruno.getId(), valor(620_000),
                "Proposta final. Já tenho o financiamento aprovado.");
        propostas.aceitar(aceitaA7.getId(), carlaId);
        propostas.concluirVenda(a7.getId(), carlaId); // a7 vira VENDIDO

        // ------------------------------------------------------------- favoritos
        favoritos.alternar(ana.getId(), a1.getId());
        favoritos.alternar(ana.getId(), a5.getId());
        favoritos.alternar(ana.getId(), a8.getId());
        favoritos.alternar(bruno.getId(), a2.getId());
        favoritos.alternar(marina.getId(), a10.getId());
        favoritos.alternar(marina.getId(), a11.getId());
    }

    private static BigDecimal valor(long reais) {
        return BigDecimal.valueOf(reais);
    }

    /** Monta o formulário de um anúncio e o cadastra pelo service (nasce "em análise"). */
    private Apartamento anuncio(Long vendedorId, Map<String, Long> idPorNome, String titulo, String descricao,
                                long preco, Integer condominio, int areaM2, int quartos, int suites,
                                int banheiros, int vagas, int andar, String cep, String logradouro,
                                String numero, String bairro, String cidade, String uf,
                                String... nomesDasComodidades) {
        ApartamentoForm f = new ApartamentoForm();
        f.setTitulo(titulo);
        f.setDescricao(descricao);
        f.setPreco(valor(preco));
        f.setValorCondominio(condominio == null ? null : BigDecimal.valueOf(condominio));
        f.setAreaM2(areaM2);
        f.setQuartos(quartos);
        f.setSuites(suites);
        f.setBanheiros(banheiros);
        f.setVagas(vagas);
        f.setAndar(andar);
        f.setCep(cep);
        f.setLogradouro(logradouro);
        f.setNumero(numero);
        f.setBairro(bairro);
        f.setCidade(cidade);
        f.setUf(uf);
        for (String nome : nomesDasComodidades) {
            Long id = idPorNome.get(nome);
            if (id != null) {
                f.getComodidadeIds().add(id);
            }
        }
        return apartamentos.criar(f, vendedorId);
    }
}
