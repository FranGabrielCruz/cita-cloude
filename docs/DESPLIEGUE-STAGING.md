# Despliegue de staging

El despliegue se ejecuta al enviar cambios a la rama `staging` o al iniciarlo manualmente desde la pestaña **Actions** de GitHub. El flujo primero ejecuta `mvn verify`, crea una imagen Docker, la publica en GitHub Container Registry (GHCR) y actualiza el servidor de staging por SSH.

## Preparación única en GitHub

En **Settings > Secrets and variables > Actions**, crear esta variable de repositorio:

| Tipo | Nombre | Valor |
| --- | --- | --- |
| Variable | `GHCR_IMAGE` | Nombre de la imagen completamente en minúsculas, por ejemplo `ghcr.io/organizacion/cita-cloud` |

Crear los siguientes secretos. No se deben guardar en archivos del repositorio:

| Secreto | Contenido |
| --- | --- |
| `STAGING_HOST` | IP o dominio del servidor de staging |
| `STAGING_USER` | Usuario SSH del servidor |
| `STAGING_SSH_KEY` | Clave privada SSH de ese usuario |
| `STAGING_KNOWN_HOSTS` | Línea del archivo `known_hosts` correspondiente al servidor |
| `STAGING_GHCR_USERNAME` | Usuario con acceso de lectura a la imagen GHCR |
| `STAGING_GHCR_TOKEN` | Token con permiso `read:packages` |
| `STAGING_DB_URL` | URL JDBC de la base de staging, por ejemplo `jdbc:postgresql://host:5432/cita_cloud_staging` |
| `STAGING_DB_USERNAME` | Usuario de la base de staging |
| `STAGING_DB_PASSWORD` | Contraseña de la base de staging |

La clave pública que corresponde a `STAGING_SSH_KEY` debe estar en `~/.ssh/authorized_keys` del usuario de staging. Para obtener el valor de `STAGING_KNOWN_HOSTS`, desde un equipo confiable se puede ejecutar `ssh-keyscan -H TU_SERVIDOR` y verificar la huella con el proveedor del servidor antes de guardarla como secreto.

También se recomienda crear el entorno protegido **staging** en GitHub, para poder exigir aprobación antes del trabajo de despliegue.

## Preparación única del servidor

Instalar Docker Engine y el complemento Docker Compose. El usuario configurado en `STAGING_USER` debe poder ejecutar `docker` sin privilegios adicionales. No hace falta copiar el código de CitaCloud al servidor: GitHub Actions sólo instala el archivo Compose y descarga la imagen publicada.

La base de datos de staging debe ser distinta de producción. Al iniciar el contenedor con el perfil `staging`, Flyway aplica las migraciones pendientes de forma automática.

## Uso diario

1. Integrar los cambios probados en la rama `staging`.
2. Enviar la rama a GitHub: `git push origin staging`.
3. Revisar el resultado en **Actions > Validar y desplegar en staging**.
4. Comprobar el inicio de sesión y los cambios desplegados en la URL de staging.

Si falla una migración, el trabajo falla y se debe corregir mediante una migración nueva; nunca se modifica una migración que ya se hubiera aplicado.
