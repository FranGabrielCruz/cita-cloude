# AGENTS.md

# CitaCloud — Guía para Agentes de Desarrollo

## 1. Descripción del proyecto

**CitaCloud** es una aplicación SaaS multitenant para la gestión de citas médicas.

El sistema permitirá que múltiples clínicas o empresas utilicen la misma aplicación y la misma base de datos, manteniendo los datos de cada empresa completamente aislados mediante `empresa_id`.

La primera versión del sistema corresponde al **MVP — Fase 1**.

---

# 2. Stack tecnológico

## Backend

* Java
* Spring Boot
* Spring Security
* Flyway
* PostgreSQL

## Frontend / UI

* Vaadin
* Diseño responsive
* Componentes Vaadin
* Interfaz moderna tipo SaaS

Preferencias de diseño:
- Moderno
- Responsive
- Minimalista
- Claro

## Base de datos

* PostgreSQL
* Base de datos local:

```text
cita_cloud
```

La base de datos ya existe localmente y debe ser utilizada por la aplicación.

No crear otra base de datos automáticamente.

---

# 3. Configuración de PostgreSQL

La aplicación debe conectarse a la base de datos PostgreSQL local:

```text
Base de datos: cita_cloud
```

La configuración de conexión debe estar centralizada en la configuración de la aplicación.

Ejemplo:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cita_cloud
spring.datasource.username=postgres
spring.datasource.password=postgres
```

La contraseña real no debe almacenarse en el repositorio.

Utilizar variables de entorno para credenciales sensibles.

Ejemplo:

```properties
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}
```

---

# 4. Flyway

Flyway será el mecanismo oficial para administrar la estructura de la base de datos.

## Regla importante

NO crear tablas manualmente desde PostgreSQL.

NO utilizar Hibernate/JPA para generar automáticamente las tablas.

NO utilizar:

```properties
spring.jpa.hibernate.ddl-auto=create
```

ni:

```properties
spring.jpa.hibernate.ddl-auto=update
```

La estructura de la base de datos debe ser administrada exclusivamente mediante migraciones Flyway.

Utilizar:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

si JPA/Hibernate está siendo utilizado.

---

# 5. Estructura de migraciones

Crear las migraciones dentro de:

```text
src/main/resources/db/migration/
```

Utilizar nombres siguiendo el formato:

```text
V1__crear_empresas.sql
V2__crear_seguridad.sql
V3__crear_sucursales.sql
V4__crear_especialidades.sql
V5__crear_medicos.sql
V6__crear_pacientes.sql
V7__crear_consultorios.sql
V8__crear_horarios.sql
V9__crear_seguros.sql
V10__crear_citas.sql
```

No modificar una migración que ya haya sido ejecutada.

Si se necesita cambiar una tabla existente, crear una nueva migración.

Ejemplo:

```text
V11__agregar_campo_telefono_paciente.sql
```

---

# 6. Arquitectura Multitenant

CitaCloud utilizará:

```text
Shared Database
+
Shared Schema
+
empresa_id
```

Todas las empresas estarán dentro de la misma base de datos:

```text
PostgreSQL
└── cita_cloud
```

No crear una base de datos independiente para cada empresa.

Ejemplo:

```text
cita_cloud
│
├── empresas
├── usuarios
├── pacientes
├── medicos
├── citas
└── ...
```

Los registros estarán separados mediante:

```text
empresa_id
```

---

# 7. Regla fundamental de multitenancy

Todo dato perteneciente a una empresa debe estar asociado a:

```text
empresa_id
```

Ejemplo:

```sql
CREATE TABLE pacientes (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    ...
);
```

Nunca realizar consultas de datos de negocio sin aplicar el contexto de empresa.

Incorrecto:

```sql
SELECT * FROM pacientes;
```

Correcto:

```sql
SELECT *
FROM pacientes
WHERE empresa_id = :empresaId;
```

El `empresa_id` nunca debe ser proporcionado libremente por el usuario para acceder a información de otra empresa.

Debe derivarse del contexto de autenticación.

---

# 8. Login Multitenant

El login de CitaCloud debe utilizar exactamente:

```text
Empresa
Usuario
Contraseña
```

Ejemplo:

```text
Empresa: CLINICA01
Usuario: gabriel
Contraseña: ********
```

El sistema debe:

1. Buscar la empresa mediante el código.
2. Validar que la empresa esté activa.
3. Buscar el usuario dentro de esa empresa.
4. Validar la contraseña.
5. Validar el estado del usuario.
6. Crear el contexto de autenticación.
7. Mantener disponible el `empresa_id` durante toda la sesión.

Nunca buscar un usuario solamente por:

```text
usuario
```

Debe utilizarse:

```text
empresa_id + usuario
```

La combinación debe ser única:

```text
UNIQUE (empresa_id, usuario)
```

Esto permite que diferentes empresas puedan tener un usuario llamado:

```text
admin
```

sin conflicto.

---

# 9. Fase 1 — MVP

La primera fase de CitaCloud debe incluir exclusivamente los siguientes módulos:

1. Tenants / Empresas
2. Usuarios y Roles
3. Sucursales
4. Médicos
5. Especialidades
6. Pacientes
7. Consultorios
8. Horarios
9. Citas
10. Seguros

No implementar módulos fuera del alcance de esta fase salvo que sean necesarios técnicamente.

---

# 10. Modelo inicial de datos

Las tablas principales del MVP serán:

```text
empresas

usuarios
roles
permisos
usuario_roles
rol_permisos

sucursales

especialidades

medicos
medico_especialidades
horarios_medicos
ausencias_medicos

pacientes

consultorios

aseguradoras
planes_seguros
seguros_paciente

tipos_cita
estados_cita
citas
historial_cita
```

También pueden existir tablas técnicas necesarias para:

```text
sesiones
auditoria
configuracion_empresa
```

si son requeridas por la implementación.

---

# 11. Relaciones principales

## Empresa

Una empresa puede tener:

```text
1:N usuarios
1:N roles
1:N sucursales
1:N médicos
1:N pacientes
1:N consultorios
1:N citas
1:N aseguradoras
```

## Sucursal

Una sucursal pertenece a una empresa.

Una sucursal puede tener:

```text
1:N médicos
1:N consultorios
1:N citas
```

## Médico

Un médico pertenece a una empresa.

Un médico puede tener múltiples especialidades.

Un médico puede tener múltiples horarios.

Un médico puede tener múltiples citas.

## Paciente

Un paciente pertenece a una empresa.

Un paciente puede tener:

```text
1:N seguros
1:N citas
```

## Cita

Una cita pertenece a:

```text
Empresa
Paciente
Médico
Sucursal
Consultorio
Tipo de cita
Estado de cita
```

---

# 12. Reglas de integridad multitenant

Las relaciones entre entidades deben impedir que se mezclen datos de diferentes empresas.

Ejemplo:

Un médico de:

```text
empresa_id = A
```

no puede ser asociado a una cita de:

```text
empresa_id = B
```

La implementación debe utilizar restricciones de base de datos cuando sea necesario y validaciones en la capa de servicio.

No confiar únicamente en el frontend.

---

# 13. UUID

Utilizar UUID como identificadores principales para las entidades de negocio.

Ejemplo:

```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

No utilizar IDs incrementales como mecanismo principal para entidades multitenant.

---

# 14. Índices

Crear índices para los campos utilizados frecuentemente en búsquedas multitenant.

Ejemplos:

```sql
CREATE INDEX idx_usuarios_empresa
ON usuarios(empresa_id);
```

```sql
CREATE INDEX idx_pacientes_empresa
ON pacientes(empresa_id);
```

```sql
CREATE INDEX idx_medicos_empresa
ON medicos(empresa_id);
```

```sql
CREATE INDEX idx_citas_empresa_fecha
ON citas(empresa_id, fecha);
```

Para entidades donde aplique:

```text
UNIQUE (empresa_id, codigo)
```

o:

```text
UNIQUE (empresa_id, usuario)
```

---

# 15. Vaadin — Diseño de interfaz

La interfaz de CitaCloud debe desarrollarse utilizando **Vaadin**.

El diseño debe ser:

* Moderno
* Profesional
* Limpio
* Minimalista
* Responsive
* Orientado a SaaS
* Fácil de utilizar
* Consistente

Evitar diseños antiguos o excesivamente cargados.

---

# 16. Referencia visual

Crear y mantener una carpeta:

```text
desing/
```

Esta carpeta contendrá las referencias visuales del proyecto.

IMPORTANTE:

El diseño creado por Stitch y cualquier referencia visual proporcionada para CitaCloud debe quedar documentado dentro de:

```text
desing/
```

Ejemplo:

```text
desing/
├── README.md
├── login.png
├── dashboard.png
├── citas.png
├── pacientes.png
├── medicos.png
└── referencia-ui.png
```

Si Stitch genera imágenes, capturas, especificaciones visuales o referencias de diseño, utilizarlas como guía para implementar la interfaz en Vaadin.

No copiar ciegamente el diseño si algún componente no es apropiado para Vaadin.

La referencia visual define principalmente:

* Layout
* Espaciado
* Tipografía
* Jerarquía visual
* Colores
* Componentes
* Navegación
* Tablas
* Formularios
* Cards
* Estados
* Calendarios

---

# 17. Layout principal

La aplicación debe utilizar un layout similar a:

```text
┌──────────────────────────────────────────────────────────┐
│ CitaCloud              Empresa        Usuario           │
├──────────────┬───────────────────────────────────────────┤
│              │                                           │
│ Dashboard    │                                           │
│              │                                           │
│ Citas        │              CONTENIDO                    │
│ Pacientes    │                                           │
│ Médicos      │                                           │
│ Especialidad │                                           │
│ Horarios     │                                           │
│ Consultorios│                                           │
│ Sucursales   │                                           │
│ Seguros      │                                           │
│              │                                           │
│ Usuarios     │                                           │
│ Roles        │                                           │
│              │                                           │
│ Configuración│                                          │
└──────────────┴───────────────────────────────────────────┘
```

El sidebar debe ser responsive y poder colapsarse.

---

# 18. Dashboard

El dashboard debe mostrar información de la empresa autenticada.

Indicadores:

* Citas de hoy
* Citas pendientes
* Citas confirmadas
* Citas atendidas
* Pacientes
* Médicos activos

También incluir:

* Próximas citas
* Agenda del día
* Distribución de citas por estado

Nunca mostrar datos de otra empresa.

---

# 19. Módulo de citas

La agenda de citas es el módulo principal.

Debe permitir:

* Vista diaria
* Vista semanal
* Vista mensual
* Filtrar por médico
* Filtrar por sucursal
* Filtrar por consultorio
* Filtrar por estado

Crear cita mediante un flujo sencillo:

```text
Paciente
    ↓
Especialidad
    ↓
Médico
    ↓
Sucursal
    ↓
Consultorio
    ↓
Fecha
    ↓
Hora disponible
    ↓
Seguro
    ↓
Motivo
    ↓
Confirmar
```

No permitir seleccionar horarios ocupados.

No permitir citas fuera del horario del médico.

No permitir citas durante ausencias.

---

# 20. Estados de cita

Utilizar:

```text
PENDIENTE
CONFIRMADA
EN_ESPERA
EN_CONSULTA
ATENDIDA
CANCELADA
REPROGRAMADA
NO_ASISTIO
```

Mostrar los estados mediante badges/chips visuales.

---

# 21. Pantallas mínimas del MVP

Implementar:

### Autenticación

* Login
* Recuperación de contraseña

### Dashboard

* Dashboard principal

### Empresas

* Datos de empresa
* Configuración

### Usuarios

* Listado
* Crear
* Editar
* Activar/desactivar

### Roles

* Listado
* Crear
* Editar
* Permisos

### Sucursales

* Listado
* Crear
* Editar

### Especialidades

* Listado
* Crear
* Editar

### Médicos

* Listado
* Crear
* Editar
* Detalle
* Especialidades
* Horarios

### Pacientes

* Listado
* Crear
* Editar
* Detalle
* Seguros
* Historial de citas

### Consultorios

* Listado
* Crear
* Editar

### Seguros

* Aseguradoras
* Planes
* Seguros de pacientes

### Citas

* Agenda
* Crear cita
* Editar
* Reprogramar
* Cancelar
* Detalle
* Historial

---

# 22. Reglas para el desarrollo

## Backend

Utilizar arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Separar correctamente:

```text
DTO
Entity
Repository
Service
Controller
```

No colocar lógica de negocio compleja dentro de los componentes Vaadin.

---

# 23. Seguridad

Implementar:

* Spring Security
* Contraseñas almacenadas mediante hash seguro
* Control de acceso por roles
* Validación del tenant
* Protección contra acceso cruzado entre empresas
* Validación en backend
* Auditoría de operaciones importantes

Nunca almacenar contraseñas en texto plano.

Nunca confiar en `empresa_id` enviado desde el frontend.

---

# 24. Reglas para el código

* Código limpio.
* Nombres en español para entidades relacionadas con el dominio.
* Utilizar nombres descriptivos.
* Evitar duplicación.
* Crear componentes reutilizables.
* Crear servicios reutilizables.
* Validar datos tanto en frontend como backend.
* Mantener las responsabilidades separadas.
* No introducir dependencias innecesarias.

---

# 25. Base de datos

La base de datos oficial del proyecto durante desarrollo local es:

```text
cita_cloud
```

PostgreSQL:

```text
localhost:5432
```

La estructura debe ser creada y modificada únicamente mediante Flyway.

Nunca crear tablas manualmente en la base de datos de desarrollo.

---

# 26. Migraciones

Antes de modificar el modelo de datos:

1. Analizar las relaciones existentes.
2. Crear una nueva migración.
3. Ejecutar Flyway.
4. Verificar que la migración funcione.
5. Ejecutar las pruebas.
6. No modificar migraciones ya aplicadas.

Ejemplo:

```text
V1__crear_empresas.sql
V2__crear_seguridad.sql
V3__crear_sucursales.sql
V4__crear_especialidades.sql
V5__crear_medicos.sql
V6__crear_pacientes.sql
V7__crear_consultorios.sql
V8__crear_horarios.sql
V9__crear_seguros.sql
V10__crear_citas.sql
```

---

# 27. Prioridad de implementación

Implementar en este orden:

## Etapa 1

```text
PostgreSQL
Flyway
Empresas
Multitenancy
```

## Etapa 2

```text
Usuarios
Roles
Permisos
Login
Spring Security
```

## Etapa 3

```text
Sucursales
Especialidades
Consultorios
```

## Etapa 4

```text
Médicos
Especialidades de médicos
Horarios
```

## Etapa 5

```text
Pacientes
Seguros
```

## Etapa 6

```text
Citas
Agenda
Disponibilidad
Reprogramación
Cancelación
```

## Etapa 7

```text
Dashboard
Reportes básicos
Auditoría
```

---

# 28. Criterios de aceptación del MVP

El MVP se considera funcional cuando:

* El usuario puede iniciar sesión con Empresa + Usuario + Contraseña.
* El sistema identifica correctamente el tenant.
* Un usuario solamente puede acceder a los datos de su empresa.
* Se pueden crear y administrar usuarios.
* Se pueden asignar roles.
* Se pueden crear sucursales.
* Se pueden crear especialidades.
* Se pueden registrar médicos.
* Se pueden asignar especialidades a médicos.
* Se pueden configurar horarios médicos.
* Se pueden registrar pacientes.
* Se pueden registrar aseguradoras y planes.
* Se pueden asociar seguros a pacientes.
* Se pueden crear consultorios.
* Se pueden crear citas.
* El sistema valida la disponibilidad del médico.
* El sistema evita conflictos de horarios.
* Se pueden cancelar y reprogramar citas.
* Se puede visualizar la agenda.
* El dashboard muestra información de la empresa autenticada.
* PostgreSQL utiliza la base de datos `cita_cloud`.
* Flyway controla todas las modificaciones de la estructura de la base de datos.
* La interfaz está implementada en Vaadin.
* El diseño mantiene como referencia los archivos almacenados en `desing/`.

---

# 29. Regla principal para el agente

Antes de implementar cualquier funcionalidad, verificar:

1. ¿Pertenece al MVP?
2. ¿A qué tenant pertenece el dato?
3. ¿La operación respeta `empresa_id`?
4. ¿La base de datos necesita una migración Flyway?
5. ¿Existe una referencia visual en `desing/`?
6. ¿La interfaz puede implementarse utilizando Vaadin?
7. ¿La funcionalidad respeta los roles y permisos?

No implementar funcionalidades fuera del alcance sin justificar previamente su necesidad.

---

# 30. Objetivo final

Construir una primera versión funcional, moderna y escalable de **CitaCloud**, utilizando:

```text
Java
Spring Boot
Spring Security
Vaadin
PostgreSQL
Flyway
```

con una arquitectura:

```text
                CitaCloud
                    │
             ┌──────┴──────┐
             │             │
          Vaadin       Spring Boot
             │             │
             └──────┬──────┘
                    │
               PostgreSQL
                    │
              cita_cloud
                    │
             ┌──────┴──────┐
             │             │
        Empresa A      Empresa B
             │             │
        empresa_id      empresa_id
```

La prioridad es crear una base sólida para el MVP antes de agregar módulos avanzados como expediente clínico, facturación, laboratorio, farmacia o reportes avanzados.
