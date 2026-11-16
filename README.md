# Sistema de Logística de Paquetes

Backend de una empresa de logística de última milla. Registra paquetes, administra la flota y los repartidores, asigna cargas a vehículos según su capacidad y planifica la ruta de entrega más conveniente.

**Trabajo Práctico — Backend de Aplicaciones, UTN FRC**

**Integrantes:** _(completar con nombre y apellido de los 4 integrantes)_

---

## Arquitectura

| Componente | Responsabilidad | Puerto interno |
|---|---|---|
| `api-gateway` | Punto de entrada único. Valida el token y enruta a los microservicios. | 8080 |
| `ms-paquetes` | Paquetes, clientes, vehículos, repartidores y sucursales. | 8081 |
| `ms-logistica` | Envíos, asignaciones, rutas (integración con OSRM) y resultados de entrega. | 8082 |
| `paquetes-db` | Instancia MariaDB exclusiva de `ms-paquetes` con `bd_paquetes`. | 3306 |
| `logistica-db` | Instancia MariaDB exclusiva de `ms-logistica` con `bd_logistica`. | 3306 |
| `keycloak` | Autenticación y autorización (OAuth2 / OIDC, tokens JWT), con almacenamiento local de desarrollo. | 8080 |
| `osrm` | Cálculo de rutas con OSRM (distancias, duraciones y geometría). Solo con `--profile osrm`. | 5000 |

Los diagramas (C4, ER y proceso principal) están en `docs/`.

## Estructura del repositorio

```
logistica-paquetes/
├── api-gateway/          Spring Cloud Gateway (reactivo)
├── ms-paquetes/          Microservicio de paquetes y flota
├── ms-logistica/         Microservicio de envíos y rutas
│   └── osrm/             preparación de datos (prepare.ps1) y datos de OSRM
├── infra/
│   └── keycloak/         realm-logistica.json (configuración inicial del realm)
├── docs/                 documentación y diagramas
├── postman/              colección y entorno de Postman (pendiente)
├── docker-compose.yml            configuración base
├── docker-compose.override.yml   solo desarrollo: publica puertos extra
├── .env.example                  plantilla de variables
└── README.md
```

---

## Requisitos

- **Git**
- **Docker Desktop** (en Windows con WSL 2 y la virtualización activada en la BIOS)
- **Java 21**: solo si vas a ejecutar un servicio fuera de Docker, desde el IDE
- No hace falta instalar Maven: cada proyecto trae su Maven Wrapper (`mvnw`)

Verificá la instalación:

```powershell
git --version
docker --version
docker compose version
java -version
```

---

## Cómo levantar el backend completo

```powershell
# 1. Clonar y entrar a la carpeta
git clone <URL_DEL_REPOSITORIO>
cd logistica-paquetes

# 2. Crear el archivo de variables a partir de la plantilla
if (!(Test-Path .env)) { Copy-Item .env.example .env }
# Si .env ya existe, no lo sobrescribas: conservá tus secretos y agregá solo variables faltantes.
# En un .env nuevo, reemplazá los valores de ejemplo por tus credenciales locales.

# 3. Construir y levantar todo
docker compose up --build -d

# 4. Verificar
docker compose ps
```

La primera vez tarda varios minutos: se descargan las imágenes y cada servicio descarga sus dependencias de Maven. Keycloak necesita un rato extra para crear sus tablas e importar el realm.

### Variables de entorno (`.env`)

| Variable | Descripción |
|---|---|
| `PAQUETES_DB_ROOT_PASSWORD` | Contraseña de root en `paquetes-db` |
| `PAQUETES_DB_USER` / `PAQUETES_DB_PASSWORD` | Credenciales de aplicación para `bd_paquetes` |
| `LOGISTICA_DB_ROOT_PASSWORD` | Contraseña de root en `logistica-db` |
| `LOGISTICA_DB_USER` / `LOGISTICA_DB_PASSWORD` | Credenciales de aplicación para `bd_logistica` |
| `KC_ADMIN_USER` | Usuario administrador de la consola de Keycloak |
| `KC_ADMIN_PASSWORD` | Contraseña de ese administrador |
| `MS_PAQUETES_CLIENT_ID` | ID del cliente técnico de ms-logistica en Keycloak (`ms-logistica-service`) |
| `MS_PAQUETES_CLIENT_SECRET` | Secret generado para ese cliente; se configura en `.env` por entorno y nunca se versiona |
| `MS_PAQUETES_URL` | URL interna compartida por ms-logistica y el gateway dentro de Docker (`http://ms-paquetes:8081`) |

`.env.example` es una plantilla versionada con valores ilustrativos; no se usa directamente como almacén de secretos. `.env` es tu archivo local con credenciales reales y **nunca se sube al repositorio**. En esta configuración, `MS_PAQUETES_URL` tiene el mismo valor para ms-logistica y el gateway. No pongas `localhost` allí: dentro de Docker, `localhost` apuntaría al propio contenedor.

Si ya usabas el MariaDB compartido, agregá a tu `.env` las seis variables nuevas de la plantilla y detené el contenedor anterior con `docker stop mariadb-db` si está ocupando el puerto `3307`. Las dos bases nuevas se inicializan vacías y el volumen anterior queda disponible para recuperar datos si los necesitás.

### Modos de ejecución

```powershell
# Desarrollo: usa docker-compose.yml + docker-compose.override.yml
docker compose up --build -d

# Sin los puertos del override de desarrollo
docker compose --file docker-compose.yml up --build -d
```

| Servicio | URL en tu PC | Modo |
|---|---|---|
| API Gateway | http://localhost:8080 | solo desarrollo |
| ms-paquetes | http://localhost:8081 | solo desarrollo |
| ms-logistica | http://localhost:8082 | solo desarrollo |
| Keycloak | http://localhost:8180 | solo desarrollo |
| `paquetes-db` | `127.0.0.1:3307` | solo desarrollo |
| `logistica-db` | `127.0.0.1:3308` | solo desarrollo |
| OSRM | http://localhost:5000 | solo desarrollo (`--profile osrm`) |

### Comandos útiles

```powershell
docker compose ps                         # estado de los contenedores
docker compose logs -f ms-paquetes        # logs de un servicio
docker compose up --build -d ms-paquetes  # reconstruir solo un servicio
docker compose down                       # detener todo (conserva los datos)

# OSRM: preparar datos (una sola vez) y levantar el servicio
cd ms-logistica\osrm; .\prepare.ps1; cd ..\..
docker compose --profile osrm up -d
docker compose logs -f osrm
```

Cada instancia MariaDB crea su base y usuario al inicializar su volumen. Keycloak importa el JSON cuando el realm `logistica` todavía no existe y guarda los cambios posteriores en `keycloak-data`. Editar el JSON de arranque no modifica un realm ya importado.

### OSRM (cálculo de rutas)

OSRM corre como el servicio `osrm` con la imagen oficial `ghcr.io/project-osrm/osrm-backend:v6.0.0` y algoritmo MLD. **No se instala nada**: los comandos de preprocesamiento se ejecutan dentro de esa misma imagen.

| Paso | Comando | Cuándo |
|---|---|---|
| 1. Preparar datos | `ms-logistica\osrm\prepare.ps1` | una vez por equipo, o al actualizar el mapa |
| 2. Levantar OSRM | `docker compose --profile osrm up -d` | cada vez que se necesite ruteo |

`prepare.ps1` descarga el extracto de OSM de la provincia de Córdoba (~40 MB) y genera `cordoba.osrm*` en `ms-logistica/osrm/data/` (~200 MB). Esos archivos **no se versionan**.

El servicio está detrás del profile `osrm` a propósito: `docker compose up` sigue funcionando en máquinas donde todavía no se descargó el mapa. `ms-logistica` recibe `OSRM_URL=http://osrm:5000` y, si OSRM no está arriba, el cálculo de ruta falla de forma controlada.

Probarlo:

```powershell
# las coordenadas son longitud,latitud (al revés que en los mapas)
curl.exe "http://localhost:5000/nearest/v1/driving/-64.1833,-31.4167"
curl.exe "http://localhost:5000/route/v1/driving/-64.1833,-31.4167;-64.1955,-31.4200?overview=full&geometries=geojson"
```

### Comprobar que todo funciona

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8081/actuator/health
curl.exe http://localhost:8082/actuator/health
```

Cada uno tiene que responder `{"status":"UP"}`.

---

## Seguridad

La autenticación la resuelve **Keycloak** (realm `logistica`, client `logistica-api`). Los servicios son Resource Servers que validan el token JWT y leen el rol desde `realm_access.roles`.

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin.logistica` | `admin123` | `ADMINISTRADOR` |
| `repartidor.logistica` | `repartidor123` | `REPARTIDOR` |

Son usuarios de prueba para desarrollo. La consola de Keycloak está en http://localhost:8180 (credenciales `KC_ADMIN_USER` / `KC_ADMIN_PASSWORD` del `.env`).

### Cuenta técnica ms-logistica → ms-paquetes

El realm `logistica` define el rol técnico `MS-LOGISTICA` y el cliente confidencial `ms-logistica-service`. El cliente usa el flujo `client_credentials` y su cuenta de servicio recibe el rol `MS-LOGISTICA`; ms-paquetes usa ese rol en sus reglas de autorización para los endpoints internos.

El cliente se declara sin un secret en `infra/keycloak/realm-logistica.json`. Keycloak genera el secret al crear el cliente. Para cada entorno, copiá el secret desde **Clients → ms-logistica-service → Credentials** y guardalo únicamente en el `.env` local:

```dotenv
MS_PAQUETES_CLIENT_ID=ms-logistica-service
MS_PAQUETES_CLIENT_SECRET=PEGAR_SECRET_GENERADO_POR_KEYCLOAK
```

No incluyas el secret en el archivo de realm, en `docker-compose.yml` ni en Git. Docker Compose pasa estas variables a ms-logistica. En Docker, el endpoint de tokens debe usar `http://keycloak:8080/realms/logistica/protocol/openid-connect/token`; desde la PC de desarrollo se usa `http://localhost:8180/realms/logistica/protocol/openid-connect/token`.

El contrato, flujo de reserva, autorización por `sub` y pendientes para el integrante de ms-logistica están en [docs/CONTEXTO_CONTINUACION_MS_LOGISTICA.md](docs/CONTEXTO_CONTINUACION_MS_LOGISTICA.md).

**Instalación nueva:** al iniciar Keycloak con `--import-realm`, se crea el realm, el rol, el cliente y la asignación del rol a la cuenta de servicio desde `realm-logistica.json`. Luego se copia el secret generado a `.env`.

**Realm existente:** reiniciar Keycloak no fusiona el archivo con un realm que ya existe; el importador omite ese realm para preservar su estado. En la consola de administración, seleccioná `logistica` y creá o actualizá el rol y el cliente, habilitá **Client authentication** y **Service accounts roles**, asigná el rol `MS-LOGISTICA` a la cuenta de servicio y guardá el secret del entorno en `.env`. Repetí la configuración del secret por separado en cada entorno.

El cliente de servicio está configurado con `Full Scope Allowed` desactivado. En un realm existente, asigná expresamente el rol de realm `MS-LOGISTICA` a la cuenta de servicio; los roles predeterminados de Keycloak no conceden acceso a la API.

### Sucursal central y paquetes existentes

El campo `sector` indica la zona de destino (`CENTRAL`); `sucursalId` indica desde qué sucursal sale el paquete. Para registrar paquetes y reservar envíos, la sucursal debe estar activa y marcada como central. En la base de desarrollo revisada, `Sucursal Centro` (id `1`) ya quedó marcada como central. En otra instalación, consultá `GET /sucursales` y enviá `PUT /sucursales/{id}` con los datos actuales de esa sucursal y `"esCentral": true`; al marcar otra, la anterior deja de ser central. Los paquetes históricos no se mueven solos y deben revisarse antes de asignarlos a envíos nuevos.

### Obtener un token (PowerShell)

```powershell
function Get-Token($user, $pass) {
  (curl.exe -s -X POST "http://localhost:8180/realms/logistica/protocol/openid-connect/token" `
    -H "Content-Type: application/x-www-form-urlencoded" `
    -d "grant_type=password" -d "client_id=logistica-api" `
    -d "username=$user" -d "password=$pass" | ConvertFrom-Json).access_token
}

$admin = Get-Token "admin.logistica" "admin123"
$rep   = Get-Token "repartidor.logistica" "repartidor123"

curl.exe -i -H "Authorization: Bearer $admin" http://localhost:8080/<endpoint>
```

Los tokens duran unos 5 minutos. Si aparece un 401 de golpe, pedí uno nuevo.

### Respuestas de seguridad

| Situación | Código |
|---|---|
| Sin token, o token inválido o vencido | `401 Unauthorized` |
| Token válido pero sin el rol requerido | `403 Forbidden` |

**Regla del equipo:** cada endpoint nuevo debe agregarse a la lista de reglas del `ResourceServerConfig` de su servicio con el rol que corresponde según la tabla de APIs. Lo que no esté listado queda **denegado** (`anyRequest().denyAll()`): un endpoint sin regla responde `403` aunque el usuario esté autenticado. Si un endpoint nuevo da `403` con el rol correcto, lo primero que hay que revisar es que esté en esa lista.

---

## Cómo trabajamos en equipo

### Ramas

- **`main`** es la rama estable. Siempre debe levantar con `docker compose up --build -d`. Está protegida: no se sube directo, solo por Pull Request.
- Cada integrante trabaja en su propia rama, creada desde `main` actualizado:

| Rama | Carpeta |
|---|---|---|
| `feature/ms-paquetes` | `ms-paquetes/` |
| `feature/ms-logistica` | `ms-logistica/` (y OSRM) |


### Empezar a trabajar

```powershell
git checkout main
git pull
git checkout -b feature/nombre-de-la-rama
```

Antes de escribir código, confirmá la rama en la que estás:

```powershell
git branch --show-current
```

### Flujo diario

```powershell
git add .
git status                       # revisá qué se va a subir
git commit -m ""
git push
```

Al empezar cada jornada, traé los cambios de `main` a tu rama:

```powershell
git fetch origin
git merge origin/main
```



### Pull Requests

1. Abrí el PR de tu rama hacia `main` desde GitHub.
2. Describí qué hace y cómo probarlo.
3. Otro integrante lo revisa y lo aprueba. No se aprueba el propio PR.
4. Se mergea con **Create a merge commit**.
5. Cada PR debe levantar con Docker antes de mergearse.

### Reglas para no pisarnos

- Cada uno modifica **solo la carpeta de su servicio**.
- Los archivos compartidos (`docker-compose*.yml`, `.env.example`, `README.md`, `docs/`) se cambian en PRs chicos y aparte, avisando al resto.
- **Nunca subas** `.env`, carpetas `target/`, contraseñas reales ni archivos de mapa de OSRM (`*.osm.pbf`).
- Si un merge a `main` rompe algo, se corrige con un PR nuevo o se revierte con `git revert -m 1 <hash>`. No se usa `git push --force` sobre ramas compartidas.

---

## Desarrollo de un servicio fuera de Docker

Para iterar rápido, dejá la infraestructura en Docker y corré solo tu servicio desde la terminal o el IDE:

```powershell
docker compose up -d paquetes-db keycloak
docker compose stop ms-paquetes          # liberar el puerto de la aplicación

cd ms-paquetes
$env:PAQUETES_DB_USER = "<valor de tu .env>"
$env:PAQUETES_DB_PASSWORD = "<valor de tu .env>"
.\mvnw.cmd spring-boot:run
```

Para `ms-logistica`, levantá `logistica-db` y `keycloak`, detené el contenedor `ms-logistica` si está en ejecución, y usá `LOGISTICA_DB_USER` y `LOGISTICA_DB_PASSWORD` en su módulo. En ambos casos, configurá esas variables también en el IDE si arrancás desde ahí.

Los valores por defecto son `127.0.0.1:3307` para `bd_paquetes`, `127.0.0.1:3308` para `bd_logistica` y `localhost:8180` para Keycloak. Probá directo contra la aplicación que levantaste en `http://localhost:8081` o `http://localhost:8082`, ya que el gateway de Docker enruta a los contenedores. Antes de abrir un PR verificá que también funcione dentro de Docker con `docker compose up --build -d <servicio>`.

---

## Convenciones del código

- **Estructura de paquetes** dentro de cada servicio: `config`, `controller`, `domain`, `dto`, `exception`, `repository`, `service`.
- Las **entidades JPA no se exponen en la API**: los controladores reciben y devuelven DTOs.
- **Endpoints en plural** (`/vehiculos`, `/sucursales`). Sin verbos en la URL; las acciones de negocio se modelan como sub-recursos (`/envios/{id}/asignaciones`).
- **Estados del paquete:** `PENDIENTE`, `ASIGNADO`, `EN_CAMINO`, `ENTREGADO`, `INCIDENCIA`.
- **Sector:** el MVP trabaja con un único sector, `CENTRAL`, que asigna el sistema.
- **Un solo formato de configuración:** `application.yml` (no `application.properties`).
- Las **reglas de negocio** van en la capa `service`, no en el controlador.

### Formato de errores (igual en todos los servicios)

```json
{
  "timestamp": "2026-11-10T15:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "mensaje": "Ya existe un vehículo con la patente AB123CD",
  "detalles": []
}
```

| Código | Cuándo |
|---|---|
| `400` | Datos/formato inválidos |
| `401` | Sin token o token inválido |
| `403` | Sin el rol necesario |
| `404` | Recurso inexistente |
| `422` | Regla de negocio incumplida (recurso no disponible, mínimo de paquetes, etc.) |

---

## Estado del proyecto

- [x] Estructura del monorepo
- [x] Una instancia MariaDB y un volumen por microservicio de aplicación
- [x] Keycloak con realm, roles y usuarios de prueba
- [x] Seguridad JWT por rol en gateway y microservicios
- [x] Microservicios y gateway en Docker Compose
- [x] `ms-paquetes`: entidades, API pública e integración interna de reserva implementadas; falta preparar los datos existentes de sucursal central y `sub` de repartidores
- [ ] `ms-logistica`: envíos, asignaciones y resultados de entrega
- [x] OSRM en Docker Compose (perfil `osrm`, datos de Córdoba preparados con `prepare.ps1`)
- [ ] Colección de Postman con escenarios positivos y negativos
- [ ] Documentación final (secciones 02 a 11 del enunciado)

---

## Problemas frecuentes

| Síntoma | Qué hacer |
|---|---|
| `docker` no se reconoce como comando | Abrir Docker Desktop, esperar a que inicie y abrir una terminal nueva |
| Keycloak no responde al principio | Esperar 1 a 3 minutos y revisar `docker compose logs keycloak` |
| Error de acceso a la base (`Access denied`) | Verificar las variables `PAQUETES_DB_*` o `LOGISTICA_DB_*` contra las credenciales con las que se inicializó el volumen. Editar `.env` no cambia los usuarios de una instancia ya creada. |
| Los cambios del JSON del realm no se aplican | Actualizar el realm existente desde la consola de Keycloak. El JSON se usa para importar un realm que todavía no existe. |
| 401 con un token que parecía válido | El token venció (5 minutos): pedir uno nuevo |
| Un servicio da 404 en todas las rutas del gateway | Revisar las rutas en `api-gateway/src/main/resources/application.yml` |
| Puerto ocupado al correr un servicio desde el IDE | Detener el contenedor correspondiente con `docker compose stop <servicio>` |
| `osrm` termina apenas arranca | Faltan los datos de mapa: correr `ms-logistica\osrm\prepare.ps1` y volver a levantarlo |
| OSRM responde `NoSegment` | Las coordenadas están invertidas: OSRM espera `longitud,latitud` |
| `osrm` no aparece en `docker compose ps` | Está detrás del profile: usar `docker compose --profile osrm up -d` |
