FROM node:18-alpine

# Instalar Java para Gradle + bash
RUN apk add --no-cache openjdk21 bash

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# 1️⃣ Primer build: deja que Kotlin descargue su Node interno (puede fallar, no importa)
RUN chmod +x ./gradlew && ./gradlew :composeApp:jsBrowserProductionWebpack --no-daemon || true

# 2️⃣ Eliminar el Node interno de Gradle para forzar uso del Node del sistema
RUN rm -rf /root/.gradle/nodejs

# 3️⃣ Build real: ahora sí usa el Node del sistema (node:18-alpine)
RUN ./gradlew :composeApp:jsBrowserProductionWebpack --no-daemon

# Instalar servidor estático
RUN npm install -g serve

EXPOSE 8080

CMD ["sh", "-c", "serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]
