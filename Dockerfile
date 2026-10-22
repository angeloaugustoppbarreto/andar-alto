# =====================================================================
# Dockerfile do Andar Alto (usado pela hospedagem para construir e rodar o site)
#
# Duas etapas ("multi-stage build"):
#   1) build:   usa uma imagem com Maven + JDK para gerar o .jar
#   2) runtime: usa uma imagem só com o JRE (menor) para rodar o .jar
# Assim a imagem final fica leve, o que importa em planos gratuitos (512 MB de RAM).
# =====================================================================

# ---- Etapa 1: compilar -------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar o pom antes do código permite ao Docker reaproveitar o "cache"
# das dependências quando só o código muda.
COPY pom.xml .
COPY src ./src

# Os testes rodam no seu computador (mvn test); aqui só geramos o .jar.
RUN mvn -B -q -DskipTests package

# ---- Etapa 2: rodar ----------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Não rodar como administrador dentro do contêiner (boa prática de segurança).
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/*.jar app.jar
USER app

# O perfil "prod" lê application-prod.properties (PostgreSQL, cookies seguros...).
ENV SPRING_PROFILES_ACTIVE=prod

# Limites de memória pensados para 512 MB: a JVM usa no máximo ~65% da RAM
# como heap, coletor de lixo simples e compilação mais leve (inicia mais rápido).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

# A hospedagem informa a porta pela variável PORT (veja application-prod.properties).
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
