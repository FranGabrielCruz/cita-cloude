# Desarrollo local

## Requisitos

JDK 21, Maven 3.9 o compatible, PostgreSQL y Git. Docker Desktop es opcional para observabilidad. La base oficial es `cita_cloud`; una base de prueba puede indicarse mediante `DB_URL`.

## Preparación

1. Crear la base PostgreSQL, sin tablas manuales.
2. Definir `DB_USERNAME` y `DB_PASSWORD` fuera de Git.
3. Ejecutar `com.citacloud.app.Application` desde IntelliJ con perfil `local` o usar `mvn spring-boot:run`.
4. Flyway aplica migraciones y Hibernate valida el resultado.
5. Abrir `http://localhost:8080`.

No existe un frontend o worker separado: Vaadin y el worker programado viven en la aplicación Spring Boot.

## Observabilidad local

```powershell
docker compose -f docker-compose.observability-ide.yml up -d
```

Servicios: aplicación `8080`, Grafana `3000`, Prometheus `9090`, cAdvisor `8082`, OTLP HTTP `4318`. OpenTelemetry Collector no posee UI.

## Pruebas

```powershell
mvn test
```

Después verifique `/health/ready`, `/metrics` y `/v3/api-docs`. Use datos ficticios; nunca copie información real de pacientes.
