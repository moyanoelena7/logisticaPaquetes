# Guía de implementación de ms-logistica

## Propósito

Handoff para desarrollar ms-logistica sobre la base de seguridad existente. Resume el estado actual, responsabilidades, contratos, brechas y criterios para integrar el servicio. No reemplaza docs/DECISIONS.md ni docs/DIAGRAMS.md. Si cambia un contrato, coordinarlo con los responsables de ms-paquetes y OSRM y actualizar la documentación.

## Estado actual del repositorio

- La rama `feature/ms-logistica` incluye la seguridad JWT y el CRUD de `ms-paquetes` incorporado desde `main`.
- ms-logistica usa Java 21, Spring Boot 4.1.2, Spring MVC, Spring Data JPA, MariaDB y OAuth2 Resource Server.
- Existen MsLogisticaApplication y config/SecurityConfig.
- En `ms-logistica` aún faltan entidades, repositorios, servicios, controladores y clientes HTTP. `ms-paquetes` ya permite cambiar el estado de un paquete individual. Queda pendiente la reserva conjunta de la carga y la flota.
- El catálogo de sectores de `ms-paquetes` tiene por ahora un único valor, `CENTRAL`.
- application.yml define puerto 8082, conexión a bd_logistica, JWT, app.ms-paquetes.base-url y app.osrm.base-url.
- Docker Compose configura MS_PAQUETES_URL=http://ms-paquetes:8081 y las URLs de issuer/JWKS.
- OSRM se ejecuta como el servicio `osrm` de Docker Compose (imagen `ghcr.io/project-osrm/osrm-backend:v6.0.0`, algoritmo MLD) detrás del profile `osrm`. Los datos se generan una vez por equipo con `ms-logistica/osrm/prepare.ps1`. Dentro de Docker, `OSRM_URL` es `http://osrm:5000`. Todavía no hay cliente HTTP ni lógica de rutas en el código de `ms-logistica`.
- El gateway enruta /envios/** a ms-logistica. En desarrollo se publican los puertos de gateway y microservicios.
- MariaDB tiene bases lógicas separadas: bd_paquetes, bd_logistica y bd_keycloak.

## Rama y límites de responsabilidad

1. Trabajar sobre `feature/ms-logistica`, que ya incluye el merge de `main`.
2. Mantener entidades, repositorios, servicios y controladores de logística en ms-logistica.
3. Coordinar los contratos HTTP con el integrante de ms-paquetes. No añadir relaciones JPA hacia entidades de otro servicio ni leer/escribir directamente en su base.
4. Coordinar con el responsable de OSRM el contrato de entrada/salida y la URL. No duplicar su implementación.

## Decisiones de flujo y modelo

- Un envío agrupa entre 6 y 99 paquetes distintos de una sucursal y un sector. La composición existe desde `CREADO`. La asignación y la ruta se agregan en etapas posteriores.
- `EnvioPaquete` guarda el destino y el resultado del intento. `ParadaRuta` guarda el orden de visita y referencia a `EnvioPaquete`.
- El envío sigue `CREADO → ASIGNADO → PLANIFICADO → EN_CAMINO → PENDIENTE_RETORNO → CERRADO`. Puede cancelarse antes de iniciar. La flota queda reservada hasta que el repartidor confirma el retorno y la disponibilidad.
- `ms-paquetes` mantiene los estados de los paquetes y las reservas. Queda pendiente incorporar el contrato interno HTTP a partir del CRUD existente.

## Modelo y persistencia

El dominio operativo pertenece a ms-logistica. El DER actual propone:

- `Envio` guarda sucursal, sector, estado y fechas.
- `EnvioPaquete` guarda el ID externo del paquete, el destino y el resultado con fecha y motivo de incidencia.
- `Asignacion` guarda los IDs del vehículo y el repartidor, el `sub` del repartidor, los totales y las coordenadas de salida.
- `RutaPlanificada` guarda distancia en metros, duración en segundos, geometría GeoJSON y fecha de cálculo. Cada `ParadaRuta` guarda el orden y el vínculo con `EnvioPaquete`.

Paquete, Vehiculo y Repartidor pertenecen a ms-paquetes. En la base de logística guardar sus IDs externos; para autorización/auditoría, conservar solo los datos adicionales acordados. No crear @ManyToOne hacia clases de otro microservicio ni claves foráneas entre bases.

Usar tipos decimales adecuados, como BigDecimal con precisión/escala definidas. Calcular volumen como alto × ancho × largo y convertir cm³ a m³ consistentemente. Definir nulabilidad, unicidad e índices. No asumir que una asignación o ruta existe desde la creación del envío.

## Reglas de negocio que deben quedar en servicios

1. Crear envíos con paquetes pendientes de la misma sucursal y sector y respetar la cantidad admitida.
2. Asignar solo vehículo y repartidor disponibles.
3. Comprobar suma de peso y volumen de paquetes contra la capacidad del vehículo antes de confirmar.
4. Evitar paquetes duplicados o ya asignados a envíos incompatibles.
5. Asegurar que la ruta contenga todos los destinos y conserve el orden de visita calculado.
6. Iniciar reparto solo con asignación/ruta válidas y si el usuario autenticado es el repartidor asignado.
7. Registrar un resultado por paquete y envío. Pasar a `PENDIENTE_RETORNO` después del último intento.
8. Cerrar y liberar la flota al confirmar retorno y disponibilidad. Reprogramar una incidencia después del cierre por acción del administrador.

Para una escritura en ambos servicios, `ms-paquetes` confirma su transacción y responde `200 OK`. Después, `ms-logistica` guarda el cambio en una transacción local. Si se pierde la respuesta o falla el guardado local, el usuario repite la misma solicitud. La reserva por `envioId` y el resultado por paquete y envío permiten reconocer la repetición.

## Contrato API vigente

Mantener esta tabla alineada con docs/DECISIONS.md, los controladores y SecurityConfig.

| Método | Endpoint | Rol de ruta | Operación |
|---|---|---|---|
| GET | /envios?sector={sector} | ADMINISTRADOR | Buscar envíos por sector |
| GET | /envios?estado={estado} | ADMINISTRADOR | Buscar envíos por estado |
| GET | /envios/{id} | ADMINISTRADOR o REPARTIDOR | Consultar envío |
| POST | /envios | ADMINISTRADOR | Crear envío con paquetes de un sector |
| POST | /envios/{id}/asignaciones | ADMINISTRADOR | Asignar paquetes, vehículo y repartidor |
| GET | /envios/{id}/ruta | ADMINISTRADOR o REPARTIDOR | Consultar ruta |
| POST | /envios/{id}/ruta | ADMINISTRADOR | Calcular y guardar ruta |
| PATCH | /envios/{id}/inicio | REPARTIDOR | Iniciar reparto |
| PATCH | /envios/{id}/paquetes/{paqueteId}/resultado | REPARTIDOR | Registrar resultado de entrega |

Los query parameters no forman parte del path que evalúa Spring Security; GET /envios cubre las consultas por sector o estado.

## Seguridad: preservar y completar

La clase config/SecurityConfig actual es para Spring MVC y usa JWT stateless. No reemplazarla por la configuración reactiva del gateway.

Las reglas ya configuradas son:

- /actuator/health público.
- GET /envios: ADMINISTRADOR.
- GET /envios/{id} y GET /envios/{id}/ruta: ADMINISTRADOR o REPARTIDOR.
- POST de creación, asignación y cálculo de ruta: ADMINISTRADOR.
- PATCH de inicio y resultado: REPARTIDOR.
- Cualquier ruta no declarada queda denegada; cada endpoint nuevo necesita una regla explícita.
- realm_access.roles se convierte a ROLE_ADMINISTRADOR y ROLE_REPARTIDOR.

El control de rol no prueba que el recurso pertenece al usuario. Antes de habilitar los endpoints reales:

1. `ms-paquetes` registra el `username` del repartidor. Incorporar el vínculo por `sub` de Keycloak antes de habilitar las asignaciones.
2. Al asignar, guardar los IDs externos y el `sub` del repartidor en `Asignacion`.
3. Comparar el sub autenticado con el sub asignado antes de consultar/devolver o modificar un envío.
4. Aplicar esa comprobación a consulta de envío/ruta, inicio y resultado de entrega.
5. Revisar GET /paquetes/{id}: la regla actual admite cualquier REPARTIDOR y no prueba que el paquete esté en un envío suyo.
6. Si se usa @PreAuthorize, habilitar seguridad de métodos expresamente; alternativamente ejecutar la autorización en el servicio antes de acceder al recurso.
7. Nunca registrar JWT, encabezados Authorization ni contraseñas.

Configurar el `RestClient` de `ms-paquetes` con `app.ms-paquetes.base-url` y una cuenta de servicio con `client_credentials`. Autorizar ese cliente para el contrato interno. Crear el `RestClient` de OSRM sin credenciales.

## Integración con ms-paquetes

Antes de codificar el cliente, acordar DTOs y errores para:

- Consultar paquetes, coordenadas, dimensiones y pesos por uno o varios IDs.
- Consultar vehículos y repartidores disponibles.
- Reservar/liberar recursos y actualizar el estado de los paquetes.
- Manejar ID inexistente, estado no elegible, no disponibilidad y fallos del servicio.

Usar DTOs propios; nunca compartir entidades JPA. Traducir errores remotos a errores de aplicación claros. Configurar timeout. Limitar reintentos a operaciones idempotentes, principalmente GET; no repetir POST/PATCH sin idempotencia.

## Integración OSRM

El servicio ya está definido en Compose y la URL se configura por `OSRM_URL` / `app.osrm.base-url` (`http://osrm:5000` dentro de Docker; `http://localhost:5000` fuera). Datos: extracto de la provincia de Córdoba con el perfil `car.lua` y algoritmo MLD.

Contrato a respetar:

- Usar `/trip/v1/driving/{coords}` con `roundtrip=false&source=first&destination=any`. La sucursal es el primer punto de entrada y Trip elige el último destino. La respuesta incluye el orden, la geometría, la distancia y la duración.
- Enviar coordenadas como `longitud,latitud;longitud,latitud`. Asociar cada `waypoint` al punto en la misma posición de entrada y después ordenar por `waypoint_index`.
- Usar `overview=full&geometries=geojson` para obtener la geometría que se persiste en `RutaPlanificada`; `distance` sale en metros y `duration` en segundos.
- Cualquier punto fuera de la provincia de Córdoba no tiene red: validar los destinos antes de planificar.
- Timeout, respuesta inválida y caída externa sin dejar una ruta parcialmente válida.
- No enviar credenciales ni JWT de usuario a OSRM.

## Capas, errores y registros

Seguir las responsabilidades de la cátedra:

- Controller: HTTP, DTOs, validación y respuesta.
- Service: reglas de negocio, autorización sobre recursos y orquestación.
- Repository: persistencia local de logística.
- DTO: contratos HTTP; no exponer entidades JPA.
- Config/client: clientes externos e integración.

Validar entradas con Bean Validation y manejar errores globalmente con @RestControllerAdvice. Diferenciar validación, recurso inexistente, conflicto/regla de negocio, no autorización y dependencia remota. No devolver stack traces ni detalles internos.

Usar SLF4J con el ID de envío y operación cuando corresponda. No registrar JWT, contraseñas ni datos personales innecesarios. Propagar correlation/request ID cuando esté disponible.

## Configuración y ejecución

- Conservar puerto 8082 y nombre ms-logistica.
- Dentro de Docker, ms-paquetes debe resolverse como http://ms-paquetes:8081.
- Mantener KEYCLOAK_ISSUER_URI y KEYCLOAK_JWK_SET_URI de Compose.
- `OSRM_URL` ya está configurada en Compose como `http://osrm:5000`; fuera de Docker el `application.yml` usa `http://localhost:5000` (puerto publicado en el override). El servicio `osrm` requiere `docker compose --profile osrm up -d` y los datos de `ms-logistica/osrm/prepare.ps1`.
- Usar bd_logistica y credenciales inyectadas desde el .env local.
- ddl-auto:update es una facilidad de desarrollo; acordar migraciones/validación de esquema antes de producción.
- Actuator expone health e info, pero la seguridad actual solo permite health. Decidir si info queda restringido o se deja de exponer.
- Compose espera a MariaDB, no a que ms-logistica esté saludable. Esperar el inicio completo o definir healthcheck/readiness como tarea de infraestructura.

## Pruebas y criterios de aceptación

### Seguridad

- Sin JWT o con JWT inválido: 401 en gateway y acceso directo al microservicio.
- JWT válido sin rol: 403.
- ADMINISTRADOR crea/asigna/calcula ruta.
- REPARTIDOR no crea ni asigna envíos.
- Repartidor asignado consulta envío/ruta, inicia y registra resultado.
- Repartidor no asignado recibe 403 al consultar o cambiar ese envío.
- Health responde sin token.
- Probar también puertos directos mientras los microservicios estén publicados.

### Dominio e integración

- Envío bajo el mínimo acordado no se planifica.
- Exceso de peso/volumen se rechaza.
- Vehículo o repartidor no disponible se rechaza.
- Ruta incluye todos los destinos.
- No se inicia sin asignación/ruta válidas.
- Transiciones de estado respetan reglas.
- Fallo de ms-paquetes u OSRM da error controlado y no deja datos incoherentes.

Los tests existentes son de arranque de contexto. Agregar pruebas unitarias de servicios e integración/web para roles, pertenencia al envío, validación y errores remotos. La colección Postman debe usar el gateway y tokens de ambos roles.

## Orden sugerido de trabajo

1. Revisar el DER, el flujo de estados y las reglas de negocio documentadas.
2. Implementar entidades y repositorios locales.
3. Acordar DTOs e implementar cliente de ms-paquetes.
4. Implementar servicios de creación, asignación, planificación, inicio y resultado.
5. Integrar el contrato OSRM del responsable externo.
6. Implementar controladores y mantener SecurityConfig sincronizada.
7. Completar autorización por sub/asignación, errores y logging.
8. Agregar tests de seguridad, dominio e integraciones.
9. Verificar Compose y Postman end-to-end; retirar endpoints temporales de prueba.

## Listo para integrar cuando

- Todos los endpoints funcionan desde gateway y aplican 401/403 esperados.
- Un repartidor solo opera envíos asignados a su sub.
- Capacidad, disponibilidad, mínimo y estados se validan en servicios.
- Llamadas a ms-paquetes y OSRM tienen errores/tiempos de espera controlados.
- No hay relaciones JPA ni acceso directo a la base de otro microservicio.
- Tests y colección Postman cubren casos permitidos y rechazados.
