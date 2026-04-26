FROM node:22-alpine

# Instalar Java para Gradle
RUN apk add --no-cache openjdk21

WORKDIR /app

# Copiar todo el proyecto
COPY . .

# Build KMP web
RUN chmod +x ./gradlew && ./gradlew :composeApp:jsBrowserProductionWebpack

# Instalar servidor estático
RUN npm install -g serve

EXPOSE 8080

CMD ["sh", "-c", "serve -s composeApp/build/kotlin-webpack/js/productionExecutable -l $PORT"]

