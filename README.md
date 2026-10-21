Once the project y cloned

1. Change permissions:
chmod +x mvnw

2. Run the project
./mvnw spring-boot:run

3. If docker installed on local device change application.properties with:
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/comeycalla?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:comeycalla_user}
spring.datasource.password=${DB_PASSWORD:comeycalla_pass}

If not, run MySQL normally
