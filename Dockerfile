# ==========================================
# GIAI ĐOẠN 1: BUILD ỨNG DỤNG VỚI MAVEN & JDK 17
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Tải trước các thư viện phụ thuộc để tối ưu cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và thư mục static www/
COPY src ./src
COPY www ./www

# Build Uber-JAR đóng gói sẵn tất cả dependencies (FlatLaf, Gson)
RUN mvn clean package -DskipTests

# ==========================================
# GIAI ĐOẠN 2: CHẠY CONTAINER NHẸ VỚI JRE 17 ALPINE
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file JAR đã đóng gói và thư mục web
COPY --from=builder /app/target/LTUDM_App-1.0.0.jar ./app.jar
COPY --from=builder /app/www ./www

# Mở 2 cổng: 8888 (TCP Socket Chat) và 8080 (HTTP Server & Web Chat)
EXPOSE 8888 8080

# Thiết lập biến môi trường mặc định
ENV PORT_TCP=8888
ENV PORT_HTTP=8080

# Chạy Server ở chế độ Headless / Console Mode
CMD ["java", "-cp", "app.jar", "com.ltudm.ServerLauncher"]
