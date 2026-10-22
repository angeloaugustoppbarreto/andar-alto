# Como colocar o Andar Alto no ar, de graça

## 0. O desenho da solução

```
Seu computador ──git push──▶ GitHub ──deploy automático──▶ Render (roda o Java) ──▶ Neon (banco PostgreSQL)
                             (guarda o código)             (site com link público)    (guarda os dados)
```

**Por que não só o GitHub?** O GitHub guarda o código, mas o **GitHub Pages só serve sites estáticos** (HTML/CSS/JS prontos).
O Andar Alto é um programa Java que precisa ficar rodando num servidor e conversar com um banco de dados.
Por isso usamos três serviços gratuitos, cada um com um papel.

| Serviço | Papel | Plano |
|---|---|---|
| **GitHub** | Guarda o código e dispara o deploy a cada `git push` | Gratuito |
| **Render** | Executa o site (Docker + Java) e dá um link `https://....onrender.com` | Free web service |
| **Neon** | Banco PostgreSQL (o H2 em arquivo **não** serve: o disco gratuito é apagado quando o site dorme) | Free |

Os arquivos que preparam o projeto para isso já estão prontos:
`Dockerfile`, `.dockerignore`, `src/main/resources/application-prod.properties` e duas dependências novas no `pom.xml`
(driver do PostgreSQL e `flyway-database-postgresql`). **No seu computador nada muda**: sem o perfil `prod`,
o site continua usando o H2 como antes.

---

## 1. Subir o código para o GitHub

No terminal do VS Code, dentro da pasta `imoveis-ecommerce`:

```powershell
git init -b main
git add .
git commit -m "Andar Alto: e-commerce de apartamentos"
```

> Sua pasta `untitled` já é um repositório Git (com outros projetos). O `git init` acima cria um repositório
> **novo e separado**, só para o Andar Alto, e é isso que você quer.

1. Em github.com, clique em **New repository**, nome `andar-alto`. **Não** marque "Add a README" (já temos um).
2. Copie os comandos que o GitHub mostra em "push an existing repository" (troque `SEU-USUARIO`):

```powershell
git remote add origin https://github.com/SEU-USUARIO/andar-alto.git
git push -u origin main
```

O repositório pode ser público (bom para portfólio): **não há senha nem segredo no código**, tudo vai por variáveis de ambiente.

---

## 2. Criar o banco no Neon

1. Entre em neon.com (dá para entrar com sua conta do GitHub) e crie um projeto, por exemplo `andar-alto`.
   Escolha a região mais próxima de você (se houver São Paulo, melhor).
2. Em **Connection details**, **desligue "Connection pooling"** e use o endereço *direto*
   (o Flyway usa recursos que não funcionam bem com o pooler).
3. Anote: **host**, **nome do banco** (geralmente `neondb`), **usuário** e **senha**.
4. Monte a URL no formato do Java (JDBC), usando **só** `?sslmode=require` (apague outros parâmetros que o Neon incluir):

```
jdbc:postgresql://ep-exemplo-123456.sa-east-1.aws.neon.tech/neondb?sslmode=require
```

---

## 3. Publicar no Render

1. Em render.com, entre com o GitHub → **New + → Web Service** → escolha o repositório `andar-alto`.
2. **Language/Runtime: Docker** (ele encontra o `Dockerfile` sozinho). **Instance Type: Free**.
3. Em **Environment Variables**, cadastre:

| Variável | Valor |
|---|---|
| `DATABASE_URL` | a URL JDBC do passo 2 |
| `DATABASE_USERNAME` | usuário do Neon |
| `DATABASE_PASSWORD` | senha do Neon |
| `APP_ADMIN_SENHA` | **uma senha forte, só sua** (obrigatória: sem ela o site não sobe, de propósito) |
| `APP_ADMIN_EMAIL` | (opcional) e-mail do administrador; padrão `admin@andaralto.com` |
| `APP_DEMO_DADOS` | (opcional) `false` para o site começar vazio; padrão `true` (vitrine fictícia) |

4. Clique em **Create Web Service**. O primeiro deploy demora (Maven baixa as dependências dentro do Docker,
   e o plano gratuito tem pouca CPU): **de 5 a 15 minutos**. Acompanhe pelos logs.
5. Quando aparecer `Started ImoveisApplication` nos logs, abra o link `https://andar-alto-xxxx.onrender.com`.

**Para atualizar o site depois:** faça alterações, `git add .`, `git commit`, `git push`. O Render refaz o deploy sozinho.

---

## 4. Testar

- Entre como administrador com `APP_ADMIN_EMAIL` + a senha que você definiu em `APP_ADMIN_SENHA`.
- Se deixou `APP_DEMO_DADOS=true`: os usuários fictícios (`ana@andaralto.demo`, `carla@andaralto.demo`, ...)
  têm a senha pública `demo123`. Qualquer pessoa que ler o README consegue entrar com eles; tudo bem para
  uma demonstração com dados fictícios, mas **nunca** coloque dados reais de pessoas.

---

## 5. O que esperar do plano gratuito (limites reais)

| Ponto | Como é | Fonte |
|---|---|---|
| Site "dorme" | Após **15 min sem acessos** o Render desliga; o próximo visitante espera **cerca de 1 minuto** | [Render, limites do plano free](https://justinmckelvey.com/blog/is-render-free) |
| Memória | **512 MB** de RAM e CPU fracionada: Spring Boot roda, mas apertado | idem; [Spring Boot em hospedagens grátis](https://docs.bswen.com/blog/2026-02-28-springboot-free-hosting/) |
| Horas | 750 horas/mês por conta; acabou, os serviços gratuitos pausam até o mês seguinte | idem |
| Disco | Efêmero: arquivos locais somem ao dormir (por isso o banco é externo) | idem |
| Banco Neon | 1 GB por projeto; computação suspende após 5 min sem uso (a 1ª consulta acorda) | [Neon, limites do plano Free](https://neon.com/faqs/free-plan-limits-and-quotas) |
| Uso real | O próprio Render diz para não usar o plano free em produção | idem |

**"Tempo ilimitado"?** O plano gratuito do Render não tem prazo de validade, mas os planos gratuitos **mudam**:
em 2026 a Koyeb fechou o plano gratuito para novos usuários e a Fly.io acabou com o dela
([fonte](https://toolfreebie.com/koyeb-free-tier-alternatives/)). Por isso o projeto usa Docker:
o mesmo `Dockerfile` roda em qualquer hospedagem, e o seu código continua no GitHub.
Evite o PostgreSQL gratuito do próprio Render: ele **expira em 30 dias**.

**Plano B (não dorme):** a Oracle Cloud tem uma VM "Always Free" permanente, mas exige cartão de crédito
para o cadastro e dá bem mais trabalho de configurar (você instala Docker e cuida do servidor).

---

## 6. Problemas comuns

| Mensagem / sintoma | Causa e solução |
|---|---|
| `Could not resolve placeholder 'APP_ADMIN_SENHA'` | Falta cadastrar a variável no Render |
| `Could not resolve placeholder 'DATABASE_URL'` | Idem, para `DATABASE_URL`, `DATABASE_USERNAME` ou `DATABASE_PASSWORD` |
| `Unsupported Database: PostgreSQL` | Falta `flyway-database-postgresql` no `pom.xml` (já incluído nesta versão) |
| Erro de conexão/SSL com o banco | Confira o `?sslmode=require`, remova parâmetros extras e use o host **sem** pooler |
| Site cai com `Out of memory` | Reduza o heap: `JAVA_OPTS` = `-XX:MaxRAMPercentage=55 -XX:+UseSerialGC -Xss512k`; ou suba para um plano pago |
| Primeira visita demora ou dá erro | O site e/ou o banco estavam dormindo; aguarde um minuto e recarregue |
| Login não funciona após o deploy | Confira nos logs se o administrador foi criado; ele só é criado se o banco estiver vazio |

Em qualquer erro de build ou de subida, copie as últimas linhas do log do Render e cole no chat.
