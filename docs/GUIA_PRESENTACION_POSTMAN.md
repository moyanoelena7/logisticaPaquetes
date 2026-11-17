# Guía para presentar Logística de Paquetes con Postman

Esta guía explica cómo preparar y demostrar el proyecto integrado. Las operaciones de negocio se realizan desde Postman. Docker y Keycloak se preparan antes de comenzar la exposición.

## 1. Qué vas a demostrar

El administrador registra datos, crea un envío de seis paquetes, reserva un vehículo y un repartidor y calcula la ruta. El repartidor consulta sus rutas, inicia el reparto, registra cinco entregas y una incidencia y confirma su retorno. Al cerrar el envío, los recursos quedan libres.

Responsabilidades:

| Componente | Función |
|---|---|
| API Gateway | Entrada pública y validación del token |
| ms-paquetes | Clientes, sucursales, paquetes, vehículos, repartidores y reservas |
| ms-logistica | Envíos, asignaciones, planificación, resultados y cierre |
| Keycloak | Usuarios, cuentas técnicas y emisión de JWT |
| OSRM | Cálculo del recorrido y orden de visita |

## 2. Preparación antes de presentar

### 2.1 Rama y contenedores

Usá la rama integrada o `main` después de integrar y aprobar el PR. No cambies de rama durante la demostración.

Desde la raíz del repositorio, en PowerShell:

```powershell
docker compose --profile osrm up -d --build
docker compose --profile osrm ps
```

Las bases deben estar `healthy`; los demás servicios, en ejecución. El mapa OSRM debe estar preparado. Si OSRM ya funciona, no hace falta volver a descargarlo. Para prepararlo por primera vez:

```powershell
.\ms-logistica\osrm\prepare.ps1
docker compose --profile osrm up -d osrm
```

Conservá los volúmenes actuales. No uses `docker compose down -v` para repetir la presentación.

### 2.2 Una instalación completamente nueva

Si ya tenés el sistema funcionando, omití este apartado.

1. Creá `.env` a partir de `.env.example` y completá las variables locales. `.env.example` es una plantilla; Compose usa `.env`.
2. Levantá primero las bases y Keycloak:

   ```powershell
   docker compose up -d paquetes-db logistica-db keycloak
   ```

3. Entrá a `http://localhost:8180`, realm `logistica`.
4. Verificá los usuarios `admin.logistica` y `repartidor.logistica`, con roles de realm `ADMINISTRADOR` y `REPARTIDOR`, respectivamente.
5. Si configurás una contraseña, dejá `Temporary` desactivado y completá las acciones pendientes del usuario.
6. En Clients → `ms-logistica-service`, verificá Client authentication y Service accounts roles habilitados.
7. En Credentials, copiá el secreto actual a `MS_PAQUETES_CLIENT_SECRET` en `.env`.
8. La cuenta de servicio debe tener asignado el rol de realm `MS-LOGISTICA`; el alcance del cliente debe permitirlo. La importación del realm integrado incluye la asignación y `scopeMappings`.
9. Levantá el resto con el comando de 2.1.

La importación al arrancar no reemplaza un realm ya existente. Los cambios posteriores de roles o alcance se aplican en la consola y requieren tokens nuevos.

### 2.3 Archivos correctos de Postman

Importá desde `postman/`:

- `coleccion-logistica-paquetes-actualizada.postman_collection.json`.
- `entorno-logistica-local-actualizado.postman_environment.json`.

La colección `coleccion-contrato-interno-ms-paquetes.postman_collection.json` es una prueba adicional, separada.

Usá un solo workspace para esta presentación. Otro workspace que apunta a las mismas URLs modifica las mismas bases. No ejecutes la colección externa que cambia `keycloakSub` por valores `liberado-...`.

## 3. Configurar el entorno de Postman

Seleccioná el entorno importado en el desplegable superior derecho. Debe aparecer su nombre en lugar de `No environment`.

| Variable | Valor o procedencia |
|---|---|
| gateway_url | `http://localhost:8080` |
| ms_paquetes_url | `http://localhost:8081` |
| ms_logistica_url | `http://localhost:8082` |
| keycloak_token_url | `http://localhost:8180/realms/logistica/protocol/openid-connect/token` |
| admin_username | `admin.logistica` |
| admin_password | Contraseña actual de ese usuario |
| repartidor_username | `repartidor.logistica` |
| repartidor_password | Contraseña actual de ese usuario |
| ms_paquetes_client_id | `ms-logistica-service` |
| ms_paquetes_client_secret | Secreto actual de Keycloak; necesario para la colección interna |

Guardá los valores localmente. No incluyas secretos reales en los archivos que entregás al profesor.

Los IDs de la plantilla son ejemplos. Deben sustituirse por los IDs reales de tus respuestas; no se garantiza que comiencen en 1.

En Auth → Bearer Token usá variables, no tokens largos pegados manualmente:

| Operación | Token |
|---|---|
| CRUD y gestión de envíos | `{{admin_token}}` |
| Mis rutas, inicio, resultados y cierre | `{{repartidor_token}}` |
| `/interno/logistica/...` directo a 8081 | `{{service_token}}` |

Los campos numéricos usan variables sin comillas, por ejemplo `"vehiculoId": {{id_vehiculo_grande}}`. El UUID es texto: `"keycloakSub": "{{repartidor_sub}}"`.

## 4. Comenzar la demostración: carpeta 00

Ejecutá individualmente:

1. Health del gateway, ms-paquetes y ms-logistica: esperar 200 y `status: UP`.
2. Token - Administrador: esperar 200; se guarda `admin_token`.
3. Token - Repartidor: esperar 200; se guardan `repartidor_token` y `repartidor_sub`.

`repartidor_sub` es el identificador del usuario dentro del JWT. Es un UUID, no el access token. También se puede consultar como ID del usuario en Keycloak.

El token técnico no está en la carpeta 00 de la colección principal: está en la colección interna separada. El flujo principal obtiene sus propias credenciales técnicas desde `.env` dentro de ms-logistica.

Los tokens de prueba duran aproximadamente cinco minutos. Renovarlos no crea otro repartidor ni otro envío.

## 5. Elegir cómo cargar los datos

### Opción A: presentar con los datos actuales — recomendada

Reutilizá la central, un cliente y los recursos ya registrados. Cargá seis paquetes nuevos durante la exposición.

1. Con token de administrador, `GET {{gateway_url}}/sucursales`. Buscá la sucursal activa con `esCentral: true` y guardá su ID en `id_sucursal`.
2. `GET {{gateway_url}}/clientes`: elegí uno y guardá su ID en `id_cliente`; o ejecutá 01.3 para registrar uno nuevo.
3. `GET {{gateway_url}}/envios/disponibilidad?sucursalId={{id_sucursal}}`: elegí un vehículo grande y un repartidor disponibles.
4. Guardá sus IDs en `id_vehiculo_grande` e `id_repartidor`.
5. Consultá `GET {{gateway_url}}/repartidores/{{id_repartidor}}`. Su `keycloakSub` debe coincidir con `repartidor_sub` del usuario que usará la aplicación.
6. No ejecutes 01.1, 01.4 ni 01.6 si ya reutilizaste esos registros.

Referencia del último conjunto reparado: sucursal 2, vehículo 1 y repartidor 6. Estos números sirven solo si las consultas actuales confirman que siguen siendo compatibles y disponibles. La cuenta `repartidor.logistica` quedó vinculada al registro 6. El registro 1 con `liberado-...` no sirve para esta demostración.

La disponibilidad administrativa `disponible: true` no prueba que un recurso esté libre: la consulta operativa también descarta reservas activas.

### Opción B: registrar todo por primera vez

Solo si esos registros todavía no existen, ejecutá las solicitudes de 01 en este orden:

| Solicitud | Qué cargar y mostrar | Esperado |
|---|---|---|
| 01.1 Crear Sucursal Central | Nombre, dirección, coordenadas y `esCentral: true` | 201; guarda id_sucursal |
| 01.2 Listar Sucursales | Verificar la central creada | 200 |
| 01.3 Crear Cliente | Nombre, teléfono y email | 201; guarda id_cliente |
| 01.4 Crear Vehículo Grande | Patente única, 800 kg, 6 m³ y sucursal elegida | 201; guarda id_vehiculo_grande |
| 01.5 Crear Vehículo Chico | Opcional para demostrar rechazo por capacidad | 201; guarda id_vehiculo_chico |
| 01.6 Crear Repartidor | Usuario real, `{{repartidor_sub}}` y sucursal elegida | 201; guarda id_repartidor |

Cada usuario de Keycloak se vincula a un único registro de repartidor. Si el UUID ya existe, reutilizá ese registro: no inventes otro UUID y no cambies el de un registro anterior para liberar la unicidad.

Crear otra central desmarca la anterior. No repitas 01.1 en cada corrida. Para modificar una sucursal existente, usá PUT con sus datos actuales y `esCentral: true`, entendiendo que cambia la central de todo el sistema.

### Cargar seis paquetes nuevos — ambas opciones

Ejecutá 01.7 a 01.12, una por una. Cada alta debe responder 201 antes de continuar. Revisá que se hayan guardado los seis IDs en el entorno.

Los cuerpos ya contienen direcciones y coordenadas de Córdoba:

| Solicitud | Destino | Peso |
|---|---|---:|
| 01.7 | Nueva Córdoba | 2,5 kg |
| 01.8 | Alberdi | 3 kg |
| 01.9 | Güemes | 4 kg |
| 01.10 | Cerro de las Rosas | 3,5 kg |
| 01.11 | General Paz | 2 kg |
| 01.12 | Alta Córdoba | 5 kg |

Total: 20,5 kg y 0,1775 m³. El vehículo de 800 kg / 6 m³ alcanza; el chico de 10 kg / 0,1 m³ no.

Ejecutá 01.13 para mostrar los paquetes pendientes. La consulta filtra por estado y sector, no por sucursal: verificá que los seis IDs elegidos pertenezcan a la central indicada.

Antes del envío, confirmá:

- Los seis paquetes son distintos, están PENDIENTE y pertenecen a id_sucursal.
- El vehículo y el repartidor pertenecen a esa misma central y están libres.
- El usuario del token coincide por sub con el repartidor.
- Las solicitudes de 03 usan `{{id_envio}}`, sin un número fijo en la URL.

## 6. Flujo completo: carpeta 03

Ejecutá una solicitud por vez y detenete si falla. El orden siguiente coincide con el flujo, aunque la numeración 03.8 aparezca antes de 03.5.

### 6.1 Crear el borrador — 03.1

POST `{{gateway_url}}/envios`, token administrador:

```json
{
  "sucursalId": {{id_sucursal}},
  "sector": "CENTRAL",
  "paqueteIds": [{{id_paquete_1}}, {{id_paquete_2}}, {{id_paquete_3}}, {{id_paquete_4}}, {{id_paquete_5}}, {{id_paquete_6}}]
}
```

Esperado: 201, ID nuevo y estado CREADO. El script guarda el ID en id_envio. Si falla, no continúes con un ID viejo del entorno.

### 6.2 Asignar los recursos — 03.2

POST `{{gateway_url}}/envios/{{id_envio}}/asignaciones`, token administrador:

```json
{
  "vehiculoId": {{id_vehiculo_grande}},
  "repartidorId": {{id_repartidor}}
}
```

Esperado: 200. GET `/envios/{{id_envio}}` con administrador debe mostrar ASIGNADO. ms-paquetes reserva los recursos y pasa los paquetes a ASIGNADO; ms-logistica confirma su asignación local.

No ejecutes adicionalmente la reserva de la colección interna sobre este envío.

### 6.3 Calcular la ruta — 03.3

POST `{{gateway_url}}/envios/{{id_envio}}/ruta`, token administrador, sin body.

Esperado: 200 y estadoEnvio PLANIFICADO. Mostrar distanciaMetros, duracionSegundos y paradas con orden, paqueteId y destino. geometriaGeojson contiene la línea del recorrido como texto JSON.

OSRM Trip recibe primero la central y después los destinos, en formato longitud,latitud. El sistema conserva la central como origen y guarda el orden de visitas devuelto. La ruta es abierta: termina en un destino; este cálculo no incluye el retorno a la central. No afirmar que garantiza el mínimo global para cualquier cantidad de puntos.

### 6.4 Consultar como repartidor — 03.4 y 03.8

Renová Token - Repartidor si lo necesitás.

- 03.4: GET `/envios/{{id_envio}}/ruta`, token repartidor: 200 con seis paradas.
- 03.8: GET `/envios/mias/rutas`, token repartidor: 200 e inclusión del envío actual.

El script de 03.4 actualiza id_paquete_1 a id_paquete_6 según el orden de las paradas. Desde este momento, el paquete 2 de los resultados significa la segunda parada, no necesariamente el segundo paquete creado.

### 6.5 Iniciar — 03.5

PATCH `/envios/{{id_envio}}/inicio`, token repartidor, sin body.

Esperado: 204 sin cuerpo. El envío y los paquetes pasan a EN_CAMINO. Una respuesta vacía con 204 es correcta.

### 6.6 Registrar todos los resultados

Con token de repartidor, ejecutar 03.6, 03.7 y 03.9 a 03.12. Todas usan PATCH:

```text
{{gateway_url}}/envios/{{id_envio}}/paquetes/{{id_paquete_1}}/resultado
```

La variable del paquete cambia en cada solicitud. Entrega correcta:

```json
{"resultado":"ENTREGADO"}
```

Incidencia en 03.7:

```json
{
  "resultado": "INCIDENCIA",
  "motivoIncidencia": "Domicilio cerrado sin moradores"
}
```

Esperado: 204 por resultado. Al registrar los seis, GET del envío muestra PENDIENTE_RETORNO: terminar los intentos no confirma todavía el retorno.

### 6.7 Cerrar y comprobar — 03.13 a 03.15

03.13: PATCH `/envios/{{id_envio}}/cierre`, token repartidor, sin body: 204.

03.14: GET `/envios/{{id_envio}}`, token administrador: 200, CERRADO y fechaFin informada. Mostrar los resultados de los paquetes.

03.15: GET `/envios?sector=CENTRAL`, token administrador: 200 e inclusión del envío cerrado.

Finalmente, GET `/envios/disponibilidad?sucursalId={{id_sucursal}}` con administrador: el vehículo y el repartidor deben volver a aparecer libres. Consultá también los paquetes por ID: cinco ENTREGADO y uno INCIDENCIA.

## 7. Pruebas negativas para explicar las validaciones

No ejecutes toda la carpeta 04 ciegamente después del flujo principal: los estados de los paquetes afectan el motivo del rechazo. Un status esperado por sí solo no prueba que se validó la regla correcta; mostrale al profesor el mensaje.

| Prueba | Momento y condiciones | Esperado |
|---|---|---|
| GET sucursales sin token | Cualquier momento; Auth = No Auth | 401 |
| Repartidor intenta crear sucursal | Token repartidor válido | 403; no crea sucursal |
| Admin consulta contrato interno | Token admin en el puerto 8081 | 403 |
| Paquete inexistente | Token administrador, ID que no exista | 404 |
| Reserva con dos paquetes | Token técnico; body inválido de menos de seis | 400 |
| Vehículo chico | Seis paquetes PENDIENTE, misma central, repartidor libre, ID de reserva no usado | 422; mensaje de capacidad |
| PENDIENTE a ENTREGADO directo | Antes de asignar, paquete pendiente, token administrador | 422; transición inválida |

Para capacidad, los seis paquetes originales deben seguir pendientes. Ejecutá esta prueba antes de 03.2 o con un lote separado. Como debe rechazarse, no debe crear una reserva.

Después de completar un reparto, esos mismos paquetes ya no sirven para demostrar una transición desde PENDIENTE. Para pruebas adicionales creá paquetes separados.

## 8. Contrato interno — demostración opcional y separada

La colección interna obtiene un token con grant_type=client_credentials. El script guarda access_token en service_token. Su carpeta 02 usa ese Bearer Token.

Prepará seis paquetes distintos de los del flujo 03, en la central vigente, con vehículo y repartidor libres. Elegí un ID de reserva de prueba que no choque con envíos reales ni con otras reservas existentes. No reutilices el ID de un envío cerrado.

Orden: obtener token técnico → consultar paquetes → consultar disponibilidad → reservar → repetir exactamente la misma reserva → cambiar estados del paquete de prueba → liberar.

- Primera reserva: 200 y activa=true.
- Repetición idéntica: 200 sin duplicación.
- Mismo envioId con otro contenido: 409.
- Liberación: 204.

La prueba directa crea una reserva en paquetes; no crea un envío en logística. Liberarla no restablece automáticamente los paquetes a PENDIENTE. No mezcles este lote con la exposición del flujo principal.

## 9. Cómo usar el Runner

Para exponer, usar Send de a una solicitud ayuda a mostrar el cambio de estado. Después de ensayar, podés usar el Runner:

1. Elegí la carpeta y abrí Run.
2. Seleccioná el mismo entorno en el desplegable superior derecho.
3. Usá una sola iteración y ejecución local.
4. Para datos existentes, no incluyas las altas de sucursal, vehículo y repartidor. Ejecutá solo las altas necesarias y seis paquetes nuevos.
5. Obtené ambos tokens inmediatamente antes de correr 03. Si te detenés a explicar más de cinco minutos, renová el correspondiente y reanudá desde el paso pendiente.
6. No ejecutes simultáneamente dos runners contra la misma base.

Un HTTP rojo de una prueba negativa puede ser el resultado correcto. Revisá el test y el mensaje. Algunas solicitudes de la colección no tienen aserciones: verificá status y cuerpo aunque el Runner no marque un test fallido.

## 10. Errores que conviene tener preparados

| Síntoma | Qué revisar |
|---|---|
| 401 | Renovar el token correcto y usar la variable; revisar WWW-Authenticate en Headers de la respuesta inferior |
| invalid_grant | Username y contraseña del usuario en el realm correcto; cuenta habilitada y sin acciones pendientes |
| invalid_client / unauthorized_client | client_id, secreto y cuenta técnica habilitada |
| 403 interno | Token técnico con el Realm role MS-LOGISTICA incluido; imagen integrada de paquetes |
| 403 al consultar ruta | Token REPARTIDOR y sub idéntico al guardado al asignar ese envío |
| Subject ya vinculado | Reutilizar el registro que tiene el UUID; no reemplazar otros UUID por textos ficticios |
| Sucursal no central | Consultar la central actual; no volver a crear centrales para cada intento |
| Recurso de otra sucursal | Alinear central, vehículo, repartidor, paquetes y envío |
| Disponibilidad vacía | Revisar reservas activas además de disponible=true |
| 409 reserva con datos distintos | No reutilizar envioId con otro contenido; evitar mezclar contrato interno y flujo principal |
| 409 al calcular ruta | El envío debe estar ASIGNADO; usar el ID nuevo de 03.1 |
| 422 capacidad | Sumar los pesos y volúmenes reales y elegir el vehículo adecuado |
| 502 en POST envios | Revisar acceso técnico a paquetes, secreto en .env y rol; el token de Postman no configura ms-logistica |
| 504 al calcular ruta | Revisar estado y logs de OSRM; la lectura tiene un timeout de 15 segundos |

Si cambió el secreto de Keycloak, actualizalo tanto en el entorno de Postman como en `.env`. Para que el contenedor lo lea:

```powershell
docker compose up -d --no-deps --force-recreate ms-logistica
```

Cambiar un keycloakSub después de asignar no actualiza las copias históricas de reserva y asignación. Ante inconsistencias, detener la demostración y diagnosticar; no ejecutar scripts que cambien identidades para evitar duplicados.

## 11. Guion breve para hablar frente al profesor

1. Explicar los componentes y mostrar los tres health.
2. Obtener tokens de administrador y repartidor; explicar sus permisos.
3. Mostrar central, cliente, vehículo y repartidor; registrar seis paquetes nuevos en pantalla.
4. Mostrar el rechazo del vehículo chico con paquetes pendientes, si se preparó esa prueba.
5. Crear el envío y asignar el vehículo grande. Mostrar la reserva de recursos a través de la disponibilidad.
6. Calcular la ruta; explicar origen, orden, distancia y duración estimadas.
7. Cambiar al token del repartidor y mostrar Mis rutas.
8. Iniciar y registrar cinco entregas y una incidencia con motivo.
9. Mostrar PENDIENTE_RETORNO; confirmar cierre y mostrar CERRADO.
10. Mostrar los recursos libres y dos ejemplos de seguridad: 401 sin token y 403 con rol incorrecto.

## 12. Qué guardar para entregar

- Código integrado y commit utilizado en la presentación.
- Colección principal y entorno sin contraseñas, secretos ni tokens reales.
- Colección interna separada, si la solicitó el profesor.
- Esta guía y evidencias del Runner o capturas de las respuestas importantes.
- Explicación de que datos del mapa OSRM y `.env` se preparan localmente.

Para repetir la presentación con las mismas bases: reutilizá central y recursos, renová los tokens, cargá seis paquetes nuevos y creá un envío nuevo. Completá siempre el cierre para que los recursos queden disponibles.
