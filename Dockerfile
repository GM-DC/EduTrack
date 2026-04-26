FROM eclipse-temurin:21-jdk-jammy

# Instalar Node.js 22 y serve
RUN apt-get update && apt-get install -y curl ca-certificates \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g serve \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Build KMP web
RUN chmod +x ./gradlew && ./gradlew :composeApp:jsBrowserProductionWebpack --no-daemon --no-configuration-cache

EXPOSE 8080

CMD ["sh", "-c", "serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]
