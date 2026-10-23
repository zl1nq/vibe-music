# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY . .
# Maven 仓库挂到 BuildKit 缓存上：改代码重新构建时不再重复下载依赖
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -s settings.xml clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /workspace/vibe-music-server/target/vibe-music-server-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080}"]
