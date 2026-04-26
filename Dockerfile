FROM node:18-alpine

# Instalar Java para Gradle + bash
RUN apk add --no-cache openjdk21 bash

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Forzar uso del Node del sistema (evita que Gradle descargue el suyo)
ENV PATH="/usr/local/bin:$PATH"
ENV NODE_OPTIONS=--openssl-legacy-provider

# Build KMP web
RUN chmod +x ./gradlew && ./gradlew :composeApp:jsBrowserProductionWebpack --no-daemon

# Instalar servidor estático
RUN npm install -g serve

EXPOSE 8080

CMD ["sh", "-c", "serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]
