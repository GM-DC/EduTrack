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

# Verificar qué Node se está usando (sistema vs interno de Gradle)
RUN node -v && which node

# Build KMP web con logs detallados
RUN chmod +x ./gradlew && ./gradlew :composeApp:jsBrowserProductionWebpack \
    --no-daemon \
    --no-configuration-cache \
    --stacktrace \
    --info

# Copiar index.html al directorio de producción (necesario para que serve funcione)
RUN cp composeApp/build/processedResources/js/main/index.html \
       composeApp/build/kotlin-webpack/js/productionExecutable/index.html

EXPOSE 8080

CMD ["sh", "-c", "serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]
