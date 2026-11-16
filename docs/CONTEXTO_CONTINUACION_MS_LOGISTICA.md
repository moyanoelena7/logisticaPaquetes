# Contexto de integración para continuar ms-logistica

Este documento refleja el estado del contrato de integración implementado en ms-paquetes al 15/11/2026. ms-logistica todavía debe consumir estos endpoints y completar su propio flujo de asignación.

## Responsabilidad de cada servicio

- **ms-paquetes** es dueño de paquetes, vehículos, repartidores, sucursales y de las reservas operativas. Protege la reserva contra asignaciones simultáneas en una transacción local.
- **ms-logistica** es dueño de envíos, asignaciones y rutas. Mantiene su estado local y no accede a la base de ms-paquetes.
- El API Gateway expone las APIs de usuario. `/interno/logistica/**` no está publicado por el gateway; ms-logistica lo llama directamente dentro de Docker mediante `http://ms-paquetes:8081`.
- El repartidor consulta destinos y registra resultados a través de ms-logistica. ms-logistica debe verificar que el claim `sub` del token corresponda al repartidor asignado a ese envío antes de responder o cambiarlo.

## Contratos disponibles en ms-paquetes

Las rutas internas requieren un access token de `ms-logistica-service` que contenga el rol de realm `MS-LOGISTICA`.

| Método y ruta | Función | Respuesta/errores principales |
|---|---|---|
| `GET /interno/logistica/paquetes?ids=1,2,...` | Busca los IDs y devuelve `id`, `sucursalId`, `sector` y `estado`. Acepta de 1 a 99 IDs distintos. | `200`; `404` si falta algún ID; `422` para lista vacía, duplicada o fuera de rango. |
| `GET /interno/logistica/vehiculos/disponibles?sucursalId={id}` | Lista vehículos habilitados administrativamente y sin reserva activa en la sucursal central indicada. | `200`; `422` si la sucursal no existe, está inactiva o no está marcada central. |
| `GET /interno/logistica/repartidores/disponibles?sucursalId={id}` | Lista repartidores habilitados, sin reserva activa y con `keycloakSub` registrado. | `200`; mismas reglas de sucursal que vehículos. |
| `PUT /interno/logistica/envios/{envioId}/reserva` | Reserva el envío, paquetes, vehículo y repartidor de forma atómica en ms-paquetes. | `200` con la reserva; `400` por cuerpo inválido o cantidad fuera de 6–99; `404` por recurso inexistente; `409` por recurso ya reservado o reintento con datos distintos; `422` por IDs duplicados, sucursal/sector/estado incompatibles o capacidad. |
| `PATCH /interno/logistica/paquetes/{id}/estado` | Avanza el estado del paquete según el patrón State. JSON: `{ "estado": "EN_CAMINO" }`. | `200`; `404` si no existe; `422` si la transición no está permitida. |
| `DELETE /interno/logistica/envios/{envioId}/reserva` | Libera los recursos al cerrar el envío. Es idempotente si la reserva ya está liberada. | `204`; `404` si no existe la reserva. |

El PUT recibe:

```json
{
  "sucursalId": 1,
  "sector": "CENTRAL",
  "paqueteIds": [1, 2, 3, 4, 5, 6],
  "vehiculoId": 1,
  "repartidorId": 1
}
```

La reserva exige entre 6 y 99 IDs de paquete distintos. ms-paquetes bloquea las filas afectadas, valida que todos los paquetes estén `PENDIENTE` y coincidan en sucursal y sector, revalida que vehículo y repartidor estén habilitados y libres, y comprueba la capacidad del vehículo. Compara peso y volumen usando `BigDecimal`; el volumen de cada paquete es `altoCm × anchoCm × largoCm ÷ 1.000.000` m³. No se puede validar que cada bulto quepa físicamente porque el modelo del vehículo no tiene dimensiones útiles internas.

La respuesta contiene el `envioId`, IDs de paquetes y recursos, `repartidorSub`, totales de peso y volumen, origen y destinos con coordenadas. El PUT es idempotente por `envioId`: una repetición con exactamente los mismos datos responde con la reserva persistida; con datos distintos responde `409`. Las llamadas concurrentes del mismo envío se serializan en ms-paquetes mediante una fila de sincronización; la reserva es el registro de idempotencia y no existe una tabla de operaciones pendientes. La reserva confirmada también cambia los paquetes a `ASIGNADO` dentro de la misma transacción.

Los estados válidos son `PENDIENTE → ASIGNADO → EN_CAMINO → ENTREGADO` o `INCIDENCIA`. ms-logistica debe usar el PATCH interno para avanzar estados con el token técnico; no debe reenviar el token del repartidor a ms-paquetes. Los datos físicos del paquete solo se pueden editar en `PENDIENTE`.

## Flujo que debe implementar ms-logistica

1. Al crear el borrador de envío, validar entre 6 y 99 IDs distintos. Consultar los paquetes por ID en ms-paquetes y comprobar que estén `PENDIENTE` y agrupados por la misma sucursal de origen y sector de destino.
2. Consultar vehículos y repartidores disponibles para esa sucursal. Esas consultas son informativas; no bloquean los recursos.
3. Al asignar, llamar al PUT de reserva con el ID persistido del envío, la sucursal, el sector, los paquetes y los dos recursos. Hacer la llamada HTTP fuera de la transacción local.
4. Si ms-paquetes responde `200`, abrir una transacción local breve en ms-logistica, volver a comprobar que el envío siga `CREADO`, guardar `Asignacion` con los IDs y el `repartidorSub` devuelto, y pasar el envío a `ASIGNADO`.
5. Si ms-paquetes confirma un rechazo, mantener el envío en `CREADO`. Si hay timeout, no asumir que la reserva falló: repetir con el mismo `envioId` y exactamente los mismos datos. Si ms-paquetes respondió `200` pero falló la escritura local, repetir la misma asignación para recuperar la reserva y completar la escritura local.
6. Al comenzar el reparto, avanzar los paquetes a `EN_CAMINO` mediante el PATCH interno. Registrar `ENTREGADO` o `INCIDENCIA` según el resultado de cada paquete. Las transiciones inválidas se rechazan con `422`.
7. Al cerrar el envío, llamar al DELETE de reserva para liberar vehículo y repartidor. La liberación no borra el historial ni retrocede los estados de los paquetes.

## Autenticación y autorización

- Cliente OAuth2 confidencial: `ms-logistica-service`.
- Flujo: `client_credentials`.
- Rol técnico: `MS-LOGISTICA`, asignado a la cuenta de servicio. El realm declara `Full Scope Allowed` desactivado.
- URL de token dentro de Docker: `http://keycloak:8080/realms/logistica/protocol/openid-connect/token`.
- Secret: valor local de `MS_PAQUETES_CLIENT_SECRET` en `.env`, excluido de Git. Nunca se copia a configuración Java ni a este documento.
- Todas las rutas públicas `/paquetes/**`, `/clientes/**`, `/vehiculos/**`, `/repartidores/**` y `/sucursales/**` exigen `ADMINISTRADOR` al llamar directamente a ms-paquetes.
- Las rutas `/interno/logistica/**` exigen `MS-LOGISTICA`. El gateway no tiene una ruta para ese prefijo.
- `GET /paquetes/{id}` es solo para administradores. No dar al repartidor acceso directo a los paquetes por ID.
- Para las rutas del repartidor, ms-logistica compara el `sub` autenticado con el `repartidorSub` guardado en la asignación del envío. El control de rol `REPARTIDOR` por sí solo no prueba propiedad del envío.

## Estado actual y tareas pendientes

### Ya implementado en ms-paquetes

- Cliente/rol de servicio declarados para instalaciones nuevas en `infra/keycloak/realm-logistica.json`; el secret no está en el repositorio.
- Endpoints internos para consulta de paquetes, consulta de recursos, reserva, cambio de estado y liberación.
- Reserva persistida con `envioId` único, bloqueos pesimistas, revalidaciones, cálculo de capacidad, respuesta idempotente y liberación.
- Campo `keycloakSub` para relacionar un repartidor con el `sub` estable de Keycloak.
- Autorización separada entre rol administrador y rol técnico. El acceso de repartidores a paquetes está cerrado en ms-paquetes.
- `GET /clientes` ahora también está en el predicado del API Gateway.

### Requiere preparación o coordinación

- Para un realm ya creado, `--import-realm` con `IGNORE_EXISTING` no fusiona cambios. Crear o ajustar manualmente el cliente, el rol y la asignación de la cuenta de servicio, con `Full Scope Allowed` desactivado, y guardar el secret de ese entorno en `.env`.
- El `.env.example` contiene solo un marcador de posición; configurar `MS_PAQUETES_CLIENT_SECRET` con el secret real.
- En la base de desarrollo existente se agregó `es_central`; `Sucursal Centro` (id 1) quedó marcada `esCentral=true` tras confirmación. Las demás sucursales existentes siguen con `false`. En otras instalaciones se debe marcar la sucursal central real antes de crear paquetes o consultar recursos para asignar. Los paquetes históricos de otras sucursales no se mueven automáticamente.
- Los cuatro repartidores que ya existían reciben `keycloakSub=null`. Actualizar cada registro con el `sub` de su usuario Keycloak antes de que aparezca como disponible o pueda reservarse. El identificador no debe ser el username. La consulta interna de disponibilidad solo incluye repartidores con `sub` vinculado.
- Se creó y probó un fixture sintético independiente: cliente id `6`, vehículo id `5`/patente `ZQX523`, repartidor id `5` vinculado a la cuenta `repartidor.test.reserva.1115205249`, paquetes `9–14` y `envioId=9202611155249`. La cuenta de prueba lleva el rol `REPARTIDOR`; su contraseña es local de desarrollo y no está guardada en este documento ni en Git.
- La reserva del fixture respondió 200 con peso `12 kg` y volumen `0,144 m³`; el reintento idéntico respondió con la reserva existente, los datos distintos devolvieron `409`, el cambio inválido devolvió `422`, y liberar devolvió `204` incluso al repetirlo. Al finalizar, cuatro paquetes quedaron `ENTREGADO`, dos `INCIDENCIA`, y el vehículo y repartidor ficticios volvieron a estar disponibles.
- Esta prueba recorrió el flujo de ms-paquetes directamente, sin ms-logistica. La integración desde el servicio, la persistencia local del envío/asignación y la autorización de las rutas del repartidor siguen pendientes en ms-logistica.
- ms-logistica debe implementar su cliente HTTP con timeout, las reglas de propiedad por `sub`, el borrador, la llamada de reserva fuera de la transacción, el guardado local posterior y la liberación al cerrar.
- No hay dimensiones internas del vehículo; solo quedan validadas capacidad de peso y volumen.

## Configuración de ejecución

En `docker-compose.yml`, ms-paquetes usa `KEYCLOAK_ISSUER_URI` para la identidad del token y `KEYCLOAK_JWK_SET_URI` para obtener claves por la red de Docker. `MS_PAQUETES_URL=http://ms-paquetes:8081` se comparte entre ms-logistica y el gateway; la configuración actual de ms-logistica lo enlaza a `app.ms-paquetes.base-url`. No usar `localhost` desde un contenedor. Configurar timeout de conexión y lectura en el `RestClient` de ms-logistica. MariaDB debe estar saludable antes del arranque. El health endpoint público de ms-paquetes es `/actuator/health`.

La configuración y el ejemplo de alta de la cuenta técnica están en la sección **Cuenta técnica ms-logistica → ms-paquetes** de `README.md`; los contratos y decisiones están también en `docs/GUIA_IMPLEMENTACION_MS_LOGISTICA.md`.
