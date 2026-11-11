# Guía de implementación de ms-logistica

## Propósito

Handoff para desarrollar ms-logistica sobre la base de seguridad existente. Resume el estado actual, responsabilidades, contratos, brechas y criterios para integrar el servicio. No reemplaza docs/DECISIONS.md ni docs/DIAGRAMS.md. Si cambia un contrato, coordinarlo con los responsables de ms-paquetes y OSRM y actualizar la documentación.

## Estado actual del repositorio

- feature/seguridad ya contiene la base de JWT.
- ms-logistica usa Java 21, Spring Boot 4.1.2, Spring MVC, Spring Data JPA, MariaDB y OAuth2 Resource Server.
- Existen MsLogisticaApplication y config/SecurityConfig.
- Aún no hay entidades, repositorios, servicios, controladores ni cliente HTTP para ms-paquetes u OSRM.
- application.yml define puerto 8082, conexión a bd_logistica, JWT, app.ms-paquetes.base-url y app.osrm.base-url.
- Docker Compose configura MS_PAQUETES_URL=http://ms-paquetes:8081 y las URLs de issuer/JWKS.
- OSRM no está definido como contenedor en Compose; su implementación e infraestructura pertenecen al integrante asignado.
- El gateway enruta /envios/** a ms-logistica. En desarrollo se publican los puertos de gateway y microservicios.
- MariaDB tiene bases lógicas separadas: bd_paquetes, bd_logistica y bd_keycloak.

## Rama y límites de responsabilidad

1. Crear feature/ms-logistica desde una rama actualizada que ya incluya feature/seguridad, o desde la rama común una vez integrada seguridad. No partir de una base anterior a esa configuración.
2. Mantener entidades, repositorios, servicios y controladores de logística en ms-logistica.
3. Coordinar los contratos HTTP con el integrante de ms-paquetes. No añadir relaciones JPA hacia entidades de otro servicio ni leer/escribir directamente en su base.
4. Coordinar con el responsable de OSRM el contrato de entrada/salida y la URL. No duplicar su implementación.

## Decisiones que se deben cerrar antes de modelar

- El DER actual muestra ENVIO con asignación y ruta obligatorias, aunque el proceso las crea en etapas. Acordar cardinalidades opcionales para un envío recién creado, aún no asignado o planificado.
- El DER actual ubica estado_entrega, fecha_intento y observacion_incidencia en PARADA_RUTA. También se había considerado una entidad ResultadoEntrega. Definir una sola representación o una relación explícita, evitando duplicar el resultado.
- Unificar estados del envío, del paquete y del resultado. La documentación mezcla EN_CAMINO y EN_TRANSITO y menciona REPROGRAMADO/RETORNO sin incluirlos en todos los catálogos.
- Precisar si el mínimo de seis paquetes aplica por envío planificado o por lote/sector y mantener la regla igual en servicios y pruebas.
- Acordar con ms-paquetes cómo consultar y cambiar estados de paquetes y cómo reservar/liberar vehículos y repartidores. El contrato API actual contiene consultas de disponibilidad, pero no define todas las transiciones de paquetes necesarias para el proceso.

## Modelo y persistencia

El dominio operativo pertenece a ms-logistica. El DER actual propone:

- Envio: sector, estado y fechas de creación, inicio y cierre.
- Asignacion: envío, identificadores de vehículo/repartidor y totales de peso/volumen.
- RutaPlanificada: envío, distancia, duración estimada, geometría OSRM y fecha de cálculo.
- ParadaRuta: ruta, identificador externo del paquete, orden, coordenadas y resultado de visita según la decisión de modelado anterior.

Paquete, Vehiculo y Repartidor pertenecen a ms-paquetes. En la base de logística guardar sus IDs externos; para autorización/auditoría, conservar solo los datos adicionales acordados. No crear @ManyToOne hacia clases de otro microservicio ni claves foráneas entre bases.

Usar tipos decimales adecuados, como BigDecimal con precisión/escala definidas. Calcular volumen como alto × ancho × largo y convertir cm³ a m³ consistentemente. Definir nulabilidad, unicidad e índices. No asumir que una asignación o ruta existe desde la creación del envío.

## Reglas de negocio que deben quedar en servicios

1. Crear envíos con paquetes elegibles de un sector y cumplir el mínimo acordado.
2. Asignar solo vehículo y repartidor disponibles.
3. Comprobar suma de peso y volumen de paquetes contra la capacidad del vehículo antes de confirmar.
4. Evitar paquetes duplicados o ya asignados a envíos incompatibles.
5. Asegurar que la ruta contenga todos los destinos y conserve el orden de visita calculado.
6. Iniciar reparto solo con asignación/ruta válidas y si el usuario autenticado es el repartidor asignado.
7. Registrar resultado por paquete/parada y mantener coherente el estado agregado del envío.
8. Rechazar transiciones inválidas y definir entrega fallida, reintento, reprogramación y retorno.

Las operaciones sobre ms-logistica y ms-paquetes no comparten una transacción ACID. Definir el orden de llamadas y la recuperación/compensación ante fallos. No asumir una transacción única entre bases.

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

1. Coordinar con ms-paquetes cómo vincular el usuario Keycloak con Repartidor. Usar el claim estable sub, no username.
2. Al asignar, guardar los IDs externos acordados y, si el equipo lo decide, el sub del repartidor asignado en el registro de Asignacion.
3. Comparar el sub autenticado con el sub asignado antes de consultar/devolver o modificar un envío.
4. Aplicar esa comprobación a consulta de envío/ruta, inicio y resultado de entrega.
5. Revisar GET /paquetes/{id}: la regla actual admite cualquier REPARTIDOR y no prueba que el paquete esté en un envío suyo.
6. Si se usa @PreAuthorize, habilitar seguridad de métodos expresamente; alternativamente ejecutar la autorización en el servicio antes de acceder al recurso.
7. Nunca registrar JWT, encabezados Authorization ni contraseñas.

Cuando exista una llamada real a ms-paquetes, configurar un RestClient con app.ms-paquetes.base-url. Para una llamada síncrona originada por una solicitud de usuario, reenviar el JWT entrante para conservar identidad y rol. Para tareas sin usuario, acordar client credentials. No reenviar el token a OSRM.

## Integración con ms-paquetes

Antes de codificar el cliente, acordar DTOs y errores para:

- Consultar paquetes, coordenadas, dimensiones y pesos por uno o varios IDs.
- Consultar vehículos y repartidores disponibles.
- Reservar/liberar recursos y actualizar el estado de los paquetes.
- Manejar ID inexistente, estado no elegible, no disponibilidad y fallos del servicio.

Usar DTOs propios; nunca compartir entidades JPA. Traducir errores remotos a errores de aplicación claros. Configurar timeout. Limitar reintentos a operaciones idempotentes, principalmente GET; no repetir POST/PATCH sin idempotencia.

## Integración OSRM

La implementa el integrante responsable de OSRM. Coordinar con esa persona:

- La URL se configura por OSRM_URL/app.osrm.base-url.
- Dentro de Docker, localhost apunta al propio contenedor; OSRM_URL debe ser alcanzable desde la red Docker.
- Formato y orden de coordenadas, perfil de ruteo, distancia, duración, geometría y errores.
- Validación de todos los destinos y persistencia del orden de paradas.
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
- Configurar OSRM_URL cuando el servicio externo esté disponible desde Docker.
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

1. Cerrar DER, estados, regla de mínimo y contratos con el equipo.
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