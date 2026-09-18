# Cita Cloud

Cita Cloud es una aplicación SaaS multitenant para gestionar la operación clínica, administrativa y financiera de clínicas. La interfaz Vaadin y los servicios Spring Boot se despliegan como un monolito modular; PostgreSQL conserva los datos aislados por `empresa_id` y Flyway gobierna el esquema.

## Arquitectura resumida

```text
Navegador -> Vaadin / controladores HTTP -> servicios -> repositorios JPA -> PostgreSQL
                                      \-> outbox -> correo / WhatsApp
                                      \-> PDF y almacenamiento local
                                      \-> Micrometer / OpenTelemetry
```

Consulta [Arquitectura](docs/architecture/ARCHITECTURE.md) para el detalle y las limitaciones actuales.

## Tecnologías

- Java 21, Spring Boot 3.3, Spring Security y Spring Data JPA.
- Vaadin 24 para la interfaz.
- PostgreSQL y migraciones Flyway.
- Maven 3.9 o compatible para compilación y pruebas.
- Micrometer, Prometheus, OpenTelemetry Collector, Grafana y cAdvisor.
- Springdoc OpenAPI para el contrato HTTP generado.

## Requisitos de desarrollo

- JDK 21 y Maven 3.9 o compatible.
- PostgreSQL accesible en `localhost:5432`.
- Docker Desktop, opcional, para observabilidad y despliegues en contenedores.
- IntelliJ IDEA u otro IDE con soporte Maven.

## Configuración inicial

1. Copiar `.env.example` como referencia y definir las variables en el IDE o en el entorno. No agregar contraseñas reales al repositorio.
2. Crear la base local `cita_cloud`. Para una base de prueba diferente, cambiar `DB_URL`.
3. Usar `spring.jpa.hibernate.ddl-auto=validate`; Flyway ejecuta las migraciones de `src/main/resources/db/migration` al iniciar.

Variables locales mínimas:

```text
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:postgresql://localhost:5432/cita_cloud
DB_USERNAME=postgres
DB_PASSWORD=<definir fuera de Git>
```

El catálogo completo está en [Configuración](docs/operations/CONFIGURATION.md).

## Ejecutar localmente

Desde IntelliJ ejecute `com.citacloud.app.Application` con Java 21. Desde PowerShell:

```powershell
mvn spring-boot:run
```

La UI queda en `http://localhost:8080`. Los workers de notificaciones forman parte del mismo proceso Spring Boot; no se inicia un frontend separado porque Vaadin está integrado.

Para levantar solo observabilidad mientras la aplicación corre en el IDE:

```powershell
docker compose -f docker-compose.observability-ide.yml up -d
```

## Pruebas

```powershell
mvn test
```

Comprobaciones manuales útiles:

- Salud: `http://localhost:8080/health/ready`
- Métricas: `http://localhost:8080/metrics`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Documentación

El [índice documental](docs/README.md) es el punto de navegación central.

- [Funcional](docs/functional/README.md)
- [Arquitectura](docs/architecture/ARCHITECTURE.md)
- [API](docs/api/API-CONVENTIONS.md)
- [Base de datos](docs/database/DATABASE.md)
- [Seguridad](docs/security/SECURITY.md)
- [Operaciones](docs/operations/LOCAL-DEVELOPMENT.md)
- [Manual de usuario](docs/user-guide/getting-started.md)
- [Cambios](CHANGELOG.md)

## Definition of Done

Un cambio relevante termina cuando código, pruebas, migración, contrato HTTP, documentación afectada, permisos, observabilidad y changelog fueron revisados según corresponda, y no se introdujeron secretos ni datos clínicos reales.
