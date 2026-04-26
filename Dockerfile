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

# Copiar todos los recursos estáticos al directorio de producción
RUN cp -r composeApp/build/processedResources/js/main/* \
       composeApp/build/kotlin-webpack/js/productionExecutable/ || true
RUN cp -r composeApp/build/processedResources/web/main/* \
       composeApp/build/kotlin-webpack/js/productionExecutable/ || true

EXPOSE 8080

# Listar archivos y servir (para debug si algo falta)
CMD ["sh", "-c", "ls -la composeApp/build/kotlin-webpack/js/productionExecutable && serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]
