# Sistema de Logística de Paquetes 

___

## Problemática

¿Cómo distribuyo los paquetes en distintos camiones de la forma más eficiente y entregarlos siguiendo la ruta más corta? ¿A dónde entrego primero? ¿En qué orden?

El problema radica en la ineficiencia de la logística de distribución urbana, caracterizada por una planificación manual o basada en la experiencia de las rutas de entrega y de la carga de los vehículos:

- **Alta complejidad combinatoria (Vehicle Routing Problem - VRP)**: A medida que aumenta el volumen de paquetes y puntos de entrega, el cálculo del camino óptimo considerando restricciones simultáneas (volumen, peso, sectores y disponibilidad de choferes) supera la capacidad de resolución humana o de herramientas de oficina convencionales (planillas de cálculo).

- **Asignación empírica e informal**: La carga de camiones suele realizarse según el criterio del operador o repartidor, lo que genera sobrecarga de unos vehículos y subutilización de otros, así como trayectos redundantes (rutas que se solapan o regresan sobre el mismo sector).

- **Falta de visibilidad e integración de restricciones**: Inexistencia de un control sistematizado que valide si el peso y volumen total acumulado del lote respeta el límite permitido por el vehículo antes de iniciar la marcha.

- **Falta de un umbral de eficiencia por zona**: Salidas de vehículos con volúmenes de carga insuficientes por sector, encareciendo el costo unitario de cada entrega.

### ¿A quiénes afecta?

- A la Empresa de Logística: Genera sobrecostos operativos directos por consumo excesivo de combustible, desgaste prematuro de la flota vehicular y pago de horas extras no planificadas. Además, deteriora su margen de rentabilidad y competitividad en el mercado.
- A los Repartidores y Choferes: Provoca jornadas de trabajo ineficientes e impredecibles, aumento del estrés laboral debido a idas y vueltas innecesarias en el tránsito, e imposibilidad de planificar su jornada.
- A los Clientes Finales / Destinatarios: Genera incertidumbre, demoras en la recepción de sus compras y pérdida de confianza en la empresa debido al incumplimiento de la ventana horaria informada.

Por esto, se busca reducir el tiempo de entrega de paquetes de forma que resulte en un ahorro en recursos de la empresa de logística (tiempo, personal y combustible) y mejorar la satisfacción de los clientes mediante entregas rápidas y el cumplimiento del tiempo definido.

## Objetivo

> [!IMPORTANT]
>Sistema de logística de paquetes que resuelva el envío de paquetes entre varios puntos de entrega, optimizando la distancia y tiempo de entrega por el camino más conveniente. Destinada a una empresa de logística estilo OCA para mejorar sus procesos de envío de paquetes, ahorrando recursos y sirviendo de guía para los repartidores para lograr su trabajo. Abarca desde la recepción de paquetes y su registro, la clasificación de estos por sectores de entrega, la asignación de cargas a los vehículos según capacidad y disponibilidad, la optimización de las rutas y el seguimiento del paquete.

El administrador arma envíos por sucursal y sector, con al menos 6 paquetes por envío. El sistema valida los límites de volumen y peso del vehículo y la disponibilidad de los recursos elegidos. OSRM Trip ordena las entregas, con salida desde la sucursal y llegada al último destino que elige el motor.
Como resultado concreto, la plataforma proveerá a los administradores una herramienta centralizada para la gestión de flota y paquetes, y generará automáticamente hojas de ruta secuenciadas paso a paso para los repartidores. Esto permitirá reducir el tiempo total en tránsito y minimizar el consumo de combustible y desgaste de vehículos.

## Actores

- **Administrador / Operador de Sucursal**: Responsable del ingreso de paquetes al sistema, su clasificación por sectores de entrega y la gestión de la disponibilidad de la flota y choferes.
- **Repartidor**: Encargado de cargar el vehículo respetando la asignación previa, recorrer las paradas en la secuencia establecida e informar el resultado de la entrega (éxito o incidencia).

## Alcance funcional del MVP
- Gestión de flotas de vehículos y repartidores.
- Gestión de paquetes.
- Gestión del envío de paquetes por sectores.
- Asignación de paquetes a vehículos según capacidad (volumen y peso) y disponibilidad. 
- Gestión de seguimiento y registro de entrega. 
- Optimización del orden de entrega según el tiempo de viaje, con distancia estimada del recorrido.

No incluye:
- Seguimiento por parte del cliente del paquete. 
- Tracking en tiempo real: No se implementará seguimiento GPS en vivo de los vehículos de reparto. 
- Pagos y facturación: El MVP no incluirá cotización de envíos ni pasarelas de pago.

## Funcionalidades

| ID | Nombre | Actor | Comportamiento esperado | Resultado observable | Prioridad |
| --- | --- | --- | --- | --- | --- |
| F-01 | Gestionar Vehículos | Administrador | Registrar y actualizar vehículos, incluida su capacidad de peso y volumen y su disponibilidad | Los vehículos quedan disponibles para consultar y asignar | ALTA |
| F-02 | Gestionar repartidores | Administrador | Registrar los repartidores y su disponibilidad | Los repartidores podrán ser consultados para asignarles los envíos a los disponibles | ALTA |
| F-03 | Gestionar Sucursal | Administrador | Registrar, consultar, modificar y desactivar sucursales, incluyendo sus datos de identificación, dirección y sector asociado. | Las sucursales quedan disponibles para organizar paquetes y planificar envíos por sector. | MEDIA |
| F-04 | Gestionar paquetes | Administrador | Registrar y actualizar el estado de los paquetes | El paquete queda registrado y puede incluirse en la planificación de envíos | ALTA |
| F-05 | Asignar paquete a vehículo | Administrador | Gestionar los paquetes a los vehículos según capacidad y disponibilidad | Paquetes asignados a los vehículo para poder realizar el envío | ALTA |
| F-06 | Planificar rutas de entrega | Administrador | Calcular con OSRM Trip el orden de las entregas desde la sucursal. | Se muestran las paradas, la ruta abierta, su distancia y su duración estimada. | ALTA |
| F-07 | Gestionar el ordenamiento de paquetes por sector | Administrador | Gestionar cómo se ordenarán los paquetes según el sector de envio | Se muestra los paquetes ordenados por sector para realizar el envío | ALTA |
| F-08 | Consultar ruta | Repartidor | Consulta la ruta que debe realizar para entregar los paquetes | Visualizacion e indicaciones sobre la ruta especifica que se consulta | ALTA |
| F-09 | Gestionar el reparto | Repartidor | Iniciar el envío asignado y confirmar el retorno a la sucursal y la disponibilidad al terminar. | El envío pasa a `EN_CAMINO` al iniciar y a `CERRADO` al confirmar el retorno. | ALTA |
| F-10 | Registrar estado de la entrega | Repartidor | Registrar si el paquete fue entregado o si no pudo concretarse la entrega | Queda guardado el resultado y el momento del intento. | ALTA |

## Reglas de negocio

- Se deberá indicar el alto, ancho, largo y peso del paquete al registrarlo.
- El peso y el volumen totales de la carga deben respetar las capacidades del vehículo. El MVP controla el volumen agregado en m³. La comprobación de que un bulto cabe físicamente requeriría las dimensiones útiles del vehículo.
- El administrador elige un vehículo y un repartidor disponibles de la sucursal del envío. `ms-paquetes` vuelve a validar la disponibilidad y la carga al reservar los recursos.
- Se podrá consultar el estado del envío en cualquier momento que el repartidor desee.
- Cada paquete deberá tener indicado una dirección de envío (como coordenadas: latitud y longitud)
- Cada paquete deberá poder identificarse con un ID único.
- Cada paquete deberá contener el nombre del cliente.
- Cada paquete pertenece a una sucursal y un sector. Un envío agrupa entre 6 y 99 paquetes distintos de la misma sucursal y sector.
- Los paquetes deben estar `PENDIENTE` al crear el borrador y al reservar la carga. La reserva confirma su estado `ASIGNADO`. Un paquete solo puede tener una reserva activa.
- La ruta se calcula una vez confirmada la asignación. Sale de la sucursal e incluye todos los destinos del envío. Trip elige la última entrega.
- El envío sigue `CREADO → ASIGNADO → PLANIFICADO → EN_CAMINO → PENDIENTE_RETORNO → CERRADO`. El administrador puede cancelar antes del inicio.
- Los estados del paquete son `PENDIENTE`, `ASIGNADO`, `EN_CAMINO`, `ENTREGADO` e `INCIDENCIA`. Cada intento guarda el resultado y, si corresponde, el motivo de la incidencia.
- Después del último intento, el vehículo y el repartidor permanecen reservados hasta que el repartidor confirma el retorno a la sucursal y la disponibilidad. Tras el cierre, el administrador puede reprogramar un paquete con incidencia y devolverlo a `PENDIENTE` manteniendo el intento original.

## Diseño de la arquitectura
Se dividirán en los siguientes microservicios:
- **ms-paquetes** (vehículos, repartidores, sucursales, paquetes, clientes)
- **ms-logistica** (lógica de negocio de la logística e integración OSRM)
- **api-gateway**

### Componentes
- Administrador : Usuario encargado de gestionar la información de paquetes, vehículos, repartidores y de iniciar la planificación de los envíos.
- Repartidor : Usuario en terreno que accede a la plataforma para revisar su recorrido asignado y registrar los resultados de cada entrega.
- Sistema de Logística de Paquetes : Es el núcleo central de la solución. Coordina toda la operación logística, la información de envíos y el seguimiento (encapsula y oculta los detalles internos como el API Gateway, ms-paquetes, ms-logistica, sus bases de datos independientes y su comunicacion con la API externa OSRM).
- OSRM : Open Source Routing Machine, servicio de terceros utilizado para procesar y devolver el cálculo de rutas, distancias y tiempos de viaje resolviendo el problema.
- Keycloak: Gestiona autenticación y autorización mediante roles.

### Relaciones
- Administrador → Sistema de Logística de Paquetes: Gestiona la operación logística, administra recursos y planifica envíos [HTTPS].
- Repartidor → Sistema de Logística de Paquetes: Consulta los recorridos asignados y registra el estado o resultado de las entregas [HTTPS].
- Sistema de Logística de Paquetes → OSRM: Solicita el cálculo de rutas y distancias para optimizar los recorridos, recibiendo los tiempos estimados como respuesta [HTTP/JSON].
- API Gateway y microservicios → Keycloak: validan localmente los JWT con las claves públicas publicadas por JWKS; Keycloak emite los tokens y administra usuarios y roles.

# Diagrama de contenedor C4

### Componentes

- API Gateway: punto de entrada que valida los JWT recibidos y enruta las solicitudes a ms-paquetes o ms-logistica. Los microservicios vuelven a validar los tokens y aplican los permisos por operación.
- ms-paquetes: administra la información de paquetes, clientes, vehículos, repartidores y sucursales.
- ms-logistica: coordina la creación y planificación de envíos, asigna recursos, calcula rutas y registra el seguimiento operativo y los resultados de entrega.
- Base de datos de ms-paquetes: almacena los datos que administra ese microservicio, como paquetes, vehículos, repartidores y sucursales.
- Base de datos de ms-logistica: almacena los datos operativos de logística, por ejemplo envíos, asignaciones, rutas y resultados de entrega.
- OSRM: sistema externo que provee el cálculo de rutas y distancias.
- Keycloak: Gestiona autenticación y autorización mediante roles.

### Relaciones
- El Administrador envía solicitudes por HTTPS al API Gateway para gestionar información y planificar envíos.
- El Repartidor accede mediante HTTPS al gateway para consultar su recorrido y registrar resultados.
- El API Gateway deriva las solicitudes a los endpoints de ms-paquetes o ms-logistica mediante APIs HTTP/REST. En la configuración actual, autentica las solicitudes y cada microservicio aplica la autorización por roles.
- ms-logistica consulta a ms-paquetes los datos que necesita para planificar, como paquetes, vehículos y disponibilidad de repartidores.
- ms-logistica solicita a OSRM el cálculo de rutas y recibe los resultados.
- ms-paquetes lee y guarda su información en su base de datos.
- ms-logistica lee y guarda los datos operativos en su propia base de datos.

# Arquitectura APIs

| Microservicio | Recurso | Método HTTP | Endpoint | Operación | Seguridad |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ms-paquetes | Paquetes | POST | `/paquetes` | Registrar un paquete con destino,peso y dimensiones | Administrador |
| ms-paquetes | Paquetes | GET | `/paquetes/{id}` | Consultar un paquete específico | Administrador o repartidor asignado |
| ms-paquetes | Paquetes | GET | `/paquetes?estado={estado}` | Consultar estado de paquete específico | Administrador |
| ms-paquetes | Paquetes | GET | `/paquetes?sector={sector}` | Consultar sector de paquete específico | Administrador |
| ms-paquetes | Paquetes | GET | `/paquetes?estado={estado}&sector={sector}` | Consultar estado y sector de paquete específico | Administrador |
| ms-paquetes | Paquetes | GET | `/paquetes` | Consultar todos los paquetes | Administrador |
| ms-paquetes | Vehículos | GET | `/vehiculos` | Consultar todos los vehículos | Administrador |
| ms-paquetes | Vehículos | GET | `/vehiculos/{id}` | Consultar un vehículo específico | Administrador |
| ms-paquetes | Vehículos | GET | `/vehiculos?disponible=true` | Consultar vehículos disponibles | Administrador |
| ms-paquetes | Vehículos | POST | `/vehiculos` | Registrar vehículo con capacidad de carga | Administrador |
| ms-paquetes | Vehículos | PUT | `/vehiculos/{id}` | Actualizar datos o capacidad de un vehículo | Administrador |
| ms-paquetes | Repartidor | POST | `/repartidores` | Registrar repartidor | Administrador |
| ms-paquetes | Repartidor | GET | `/repartidores?disponible=true` | Consultar repartidores disponibles | Administrador |
| ms-paquetes | Repartidor | PUT | `/repartidores/{id}` | Actualizar datos de repartidor | Administrador |
| ms-paquetes | Sucursales | GET | `/sucursales` | Consultar sucursales | Administrador |
| ms-paquetes | Sucursales | POST | `/sucursales` | Registrar sucursal | Administrador |
| ms-paquetes | Sucursales | PUT | `/sucursales` | Actualizar datos de sucursales | Administrador |
| ms-logistica | Envío | GET | `/envios?sector={sector}` | Buscar envios por sector | Administrador |
| ms-logistica | Envío | GET | `/envios?estado={estado}` | Buscar envios por estado | Administrador |
| ms-logistica | Envío | GET | `/envios/{id}` | Consultar un envío específico | Administrador o repartidor |
| ms-logistica | Envío | POST | `/envios` | Crea un envío con paquetes en un sector | Administrador |
| ms-logistica | Asignación | POST | `/envios/{id}/asignaciones` | Se le asigna a un envío los paquetes, vehículo y repartidor | Administrador |
| ms-logistica | Rutas | GET | `/envios/{id}/ruta` | Se obtiene la ruta para un envío en particular | Administrador o Repartidor asignado |
| ms-logistica | Rutas | POST | `/envios/{id}/ruta` | Calcula y guarda la ruta calculada para ese envío | Administrador |
| ms-logistica | Reparto | PATCH | `/envios/{id}/inicio` | Inicia el reparto y actualiza el estado | Repartidor asignado |
| ms-logistica | Reparto | PATCH | `/envios/{id}/paquetes/{paqueteId}/resultado` | Registrar el estado de la entrega del paquete | Repartidor asignado |

Una solicitud PATCH en una API (siguiendo el estándar HTTP) se utiliza para realizar modificaciones parciales en un recurso existente
En lugar de enviar el objeto completo para actualizarlo, con PATCH solo envías los campos o atributos específicos que deseas cambiar, dejando el resto de los datos intactos.

## Arquitectura Librería Común

Para el MVP no se implementará una librería común. ms-paquetes y ms-logistica tienen responsabilidades diferenciadas y, por el momento, no se identificaron componentes de dominio que deban compartir. La comunicación entre ambos se realizará mediante sus APIs.
La gestión de paquetes, vehículos, repartidores y sucursales permanecerá en ms-paquetes. La lógica de asignación, planificación de rutas y gestión de entregas permanecerá en ms-logistica. Esta separación evita que un microservicio dependa del código interno del otro. Si en el futuro aparece una necesidad concreta de reutilización, se evaluará crear una librería versionada con componentes pequeños y estables.

## Implementación y Verificación

### Seguridad configurada

Keycloak es el proveedor de identidad del sistema. El realm `logistica` define los roles `ADMINISTRADOR` y `REPARTIDOR`, y emite tokens JWT.

- El API Gateway actúa como Resource Server: valida el JWT y exige autenticación para las solicitudes enrutadas. La comprobación de la firma usa el endpoint JWKS interno de Keycloak; el claim `iss` se valida contra el issuer publicado por el realm.
- `ms-paquetes` y `ms-logistica` también actúan como Resource Servers y validan los JWT de forma independiente. Los microservicios aplican los permisos por método HTTP y ruta.
- Los roles del realm se leen desde `realm_access.roles` y se convierten a autoridades `ROLE_ADMINISTRADOR` y `ROLE_REPARTIDOR`.
- En `ms-paquetes`, la administración de paquetes, vehículos, repartidores y sucursales requiere `ADMINISTRADOR`. La consulta de un paquete por ID permite ambos roles.
- En `ms-logistica`, la creación de envíos, las asignaciones y el cálculo de rutas requieren `ADMINISTRADOR`. El inicio del reparto y el registro del resultado de entrega requieren `REPARTIDOR`. Las consultas de un envío o su ruta permiten ambos roles.
- Una solicitud sin token válido debe recibir `401 Unauthorized`; un usuario autenticado sin el rol requerido debe recibir `403 Forbidden`. Las rutas no declaradas en las reglas de los microservicios se deniegan.

Las reglas actuales verifican roles. La comprobación de que el repartidor autenticado tiene asignado el envío se implementará en `ms-logistica`. `ms-paquetes` ya registra el `username` del repartidor. La asignación usará el `sub` de Keycloak para verificar la identidad.

### Estado de implementación

La configuración de seguridad, el enrutamiento del gateway, la conexión a MariaDB y la importación del realm de Keycloak están configurados. Se comprobó manualmente el acceso con un token de administrador a través del gateway. La matriz completa de casos `401`, `403` y accesos permitidos debe volver a comprobarse cuando estén implementados los endpoints reales.

`ms-paquetes` ya tiene entidades, repositorios, servicios y controladores para clientes, sucursales, vehículos, repartidores y paquetes. Ya permite cambiar el estado de un paquete individual. Quedan pendientes el contrato interno de reserva y los endpoints de negocio de `ms-logistica`.

### Comprobaciones comprometidas

- Camino permitido: un usuario con rol de administrador registra un paquete y recibe una respuesta de éxito.
- Camino rechazado: un usuario con rol de repartidor intenta registrar un vehículo y recibe `403 Forbidden`.
- También se comprobará que una solicitud sin token válido reciba `401 Unauthorized` y que un repartidor solo pueda consultar y operar los envíos que tenga asignados. Estas verificaciones requieren implementar los controladores y las reglas de negocio correspondientes.

## Tecnologías

- Lenguaje: Java 21.
- Framework: Spring Boot 4.1.2 en los tres módulos. El API Gateway utiliza Spring Cloud Gateway Server WebFlux con Spring Cloud BOM 2025.1.4.
- Construcción: Maven y Maven Wrapper; cada módulo genera una imagen Docker multi-stage.
- Microservicios MVC: Spring WebMVC, Spring Data JPA, Hibernate, validación, Actuator y OAuth2 Resource Server.
- Persistencia. `ms-paquetes` y `ms-logistica` usan instancias MariaDB 11.8 independientes. Cada una tiene su base (`bd_paquetes` o `bd_logistica`), usuario de aplicación, volumen y red con su microservicio.
- Identidad. Keycloak 26.4 importa el realm `logistica` desde `infra/keycloak/realm-logistica.json` y conserva su estado en el volumen `keycloak-data` mediante su almacenamiento de desarrollo `dev-file`.
- Ruteo: OSRM se ejecuta como el servicio `osrm` de Docker Compose con la imagen oficial `ghcr.io/project-osrm/osrm-backend:v6.0.0` y algoritmo MLD. Se levanta con el profile `osrm` (`docker compose --profile osrm up -d`) para que `docker compose up` siga funcionando en equipos que no descargaron el mapa. Los datos se generan una vez por equipo con `ms-logistica/osrm/prepare.ps1`, que descarga el extracto de OSM de la provincia de Córdoba (~40 MB) y ejecuta `osrm-extract`, `osrm-partition` y `osrm-customize` dentro de la misma imagen; el resultado (`cordoba.osrm*`, ~200 MB) queda en `ms-logistica/osrm/data/` y no se versiona. `ms-logistica` lee su URL desde `OSRM_URL`: en Docker se configura como `http://osrm:5000`, alcanzable por nombre de servicio; fuera de Docker el valor por defecto `http://localhost:5000` funciona porque el puerto se publica en el override. La etiqueta de la imagen y la del script deben coincidir, porque los archivos `.osrm.*` son binarios dependientes de la versión.

### Configuración por entorno

Cada módulo tiene un `application.yml` y recibe la configuración de ejecución mediante variables de entorno. No existen actualmente archivos `application-dev.yml` ni `application-prod.yml`; la configuración local se orquesta con Docker Compose y `.env`.

- `.env.example` enumera las variables locales requeridas. Cada integrante crea su propio `.env`; `.env` está excluido de Git.
- Compose configura las conexiones JDBC y las credenciales por servicio con `PAQUETES_DB_*` y `LOGISTICA_DB_*`. También configura el issuer/JWKS de Keycloak y las URLs internas entre servicios.
- `MS_PAQUETES_URL` y `MS_LOGISTICA_URL` deben usar los nombres DNS de los servicios dentro de Docker (`ms-paquetes:8081` y `ms-logistica:8082`).
- `OSRM_URL` debe apuntar a `http://osrm:5000` dentro de Docker. Fuera de Docker, `application.yml` usa `http://localhost:5000`, que funciona porque `docker-compose.override.yml` publica el puerto 5000. El servicio `osrm` no arranca con `docker compose up`: depende del profile `osrm` y de tener los datos de mapa preparados.
- El issuer JWT configurado es `http://localhost:8180/realms/logistica`; los contenedores obtienen las claves por `http://keycloak:8080/realms/logistica/protocol/openid-connect/certs`.
- `spring.jpa.hibernate.ddl-auto` está configurado como `update` para desarrollo local; antes de un entorno productivo deberá definirse una estrategia de migraciones y validación de esquema.

### Procedimiento de arranque local

Desde la raíz del repositorio:

1. Crear `.env` tomando `.env.example` como referencia.
2. Ejecutar `docker compose up --build -d`.
3. Si se van a probar rutas, preparar los datos una vez (`ms-logistica\osrm\prepare.ps1`) y levantar `docker compose --profile osrm up -d`.
4. Esperar a que las aplicaciones terminen de iniciar y comprobar sus logs. Cada microservicio espera a que su instancia MariaDB esté saludable.
5. Usar el gateway en `http://localhost:8080`. Los demás puertos locales de desarrollo se definen en `docker-compose.override.yml`: `8081` (`ms-paquetes`), `8082` (`ms-logistica`), `8180` (Keycloak), `3307` (`paquetes-db`), `3308` (`logistica-db`) y `5000` (OSRM).

La imagen de MariaDB crea la base y el usuario de cada instancia la primera vez que inicializa su volumen. Hibernate crea las tablas del microservicio en esa instancia.

## Colección de ejecutables (planificada)
1. Herramienta y organización previstas: la entrega se realizará mediante un archivo JSON exportado de Postman (Colección v2.1) acompañado de un archivo de entorno (entorno-logistica-local.json). Estos artefactos todavía no están incluidos en el repositorio. La colección estará estructurada en carpetas numeradas para garantizar un flujo lógico de ejecución que refleje el proceso principal del negocio:
    - 00 - Autenticación (Keycloak)
    - 01 - Setup y Datos Iniciales (ms-paquetes)
    - 02 - Proceso Principal MVP (ms-logistica)
    - 03 - Escenarios Negativos y Excepciones
2. Variables de Entorno y Autenticación Se utilizarán variables de entorno para evitar valores "hardcodeados" y facilitar la prueba en distintos entornos (local, testing). 
    - Variables clave: {{gateway_url}}, {{admin_token}}, {{repartidor_token}}, {{id_envio}}, {{id_vehiculo}}. 
    - Autenticación: La carpeta 00 - Autenticación contendrá dos peticiones POST dirigidas al token endpoint de Keycloak (una con credenciales de Administrador y otra de Repartidor). Mediante la pestaña Tests de Postman, se ejecutará un script que extraerá el access_token de la respuesta JSON y lo guardará automáticamente en las variables de entorno {{admin_token}} y {{repartidor_token}} respectivamente, inyectándolos como Bearer Token en las cabeceras de las peticiones subsiguientes.
3. Datos Iniciales y Orden de Ejecución Para ejecutar la prueba completa, se deberá seguir el orden de las carpetas. 
    - Datos iniciales (Setup): Se ejecutarán secuencialmente las peticiones de la carpeta 01 para poblar las bases de datos partiendo de cero. Se crearán al menos: 1 sucursal, 2 vehículos (con capacidades específicas), 2 repartidores y 6 paquetes (con pesos y dimensiones concretas). Postman capturará los IDs generados y los guardará en variables de entorno. 
    - Ejecución del proceso: En la carpeta 02, el evaluador ejecutará la creación del envío, la asignación de los recursos creados, la solicitud de cálculo de ruta y, finalmente, cambiará el token al del repartidor para iniciar y finalizar el reparto.
4. Escenarios de Verificación
    - Escenarios Positivos (Camino Feliz)
        - Proceso Principal Completo: Ejecución secuencial de la colección logrando llevar un paquete desde su registro (estado inicial) hasta su entrega exitosa por parte del repartidor. 
        - Integración con OSRM: Al disparar el endpoint POST /envios/{{id_envio}}/ruta, se verificará que ms-logistica se comunique con OSRM y devuelva un JSON HTTP 200/201 con la ruta optimizada, distancias y tiempos estimados guardados en la tabla RUTA.
    - Escenarios Negativos (Reglas de Negocio e Integraciones)
        - Validación de Capacidad (Regla de negocio): Intento de asignar al {{id_vehiculo}} una carga de paquetes que supere su capacidad_peso o capacidad_volumen. La API debe retornar un código de error de negocio (ej. HTTP 400 Bad Request) detallando la restricción. 
        - Validación de Mínimo de Paquetes (Regla de negocio): Intento de planificar un envío en un sector que posee solo 4 o 5 paquetes. La API debe retornar un error indicando que no se cumple la restricción mínima de 6 paquetes.
       Fallo de Autorización (Keycloak): Utilizar la variable {{repartidor_token}} para intentar consumir un endpoint administrativo, como registrar una nueva sucursal (POST /sucursales). `ms-paquetes` debe retornar HTTP 403 Forbidden. 
        - Asignación de Recursos no disponibles: Intentar asignar a un envío un repartidor o vehículo cuyo atributo disponible se encuentre en false. El sistema deberá rechazar la petición con un error 422 Unprocessable Entity, ya que viola una regla de negocio del dominio (“Solo se pueden asignar recursos disponibles”)

## Compromiso del equipo

1. Proceso central que quedará funcionando: el equipo se compromete a implementar un MVP del Sistema de Logística de Paquetes que permita organizar paquetes por sector y gestionar el ciclo completo del proceso de despacho y entrega de última milla.
2. Funcionalidades obligatorias
   - Gestión del dominio: Alta y registro de paquetes (con destino, peso y dimensiones), sucursales, vehículos y repartidores.
   - Asignación de recursos: Asignación de paquetes a recursos disponibles (vehículo y repartidor), validando estrictamente la compatibilidad de la carga (capacidad de peso y volumen) con cada vehículo y según el sector de entrega que cubrirá el vehículo.
   - Planificación: OSRM Trip propone el orden de las entregas según el tiempo de viaje y devuelve distancia, duración y recorrido.
   - Gestión operativa: Consulta del recorrido asignado y registro del resultado de entrega (éxito o motivo de fallo) por parte del repartidor.
3. Integraciones relevantes y Arquitectura
   - Arquitectura: La solución estará organizada en ms-paquetes, ms-logística y un api-gateway.
   - Seguridad: Autenticación y autorización mediante Keycloak, gestionando los roles de Administrador y Repartidor.
   - Servicios Externos: Integración con OSRM (Open Source Routing Machine) para la planificación y cálculo de rutas por tiempo y distancia.
4. Desafíos técnicos o funcionales
   - Validar correctamente la compatibilidad y los límites físicos (volumen/peso) de la carga al momento de la asignación.
   - Coordinar la consistencia de la información y la comunicación entre los distintos microservicios (ms-paquetes y ms-logística).
   - Gestionar de manera robusta los errores, transacciones distribuidas o resultados fallidos durante la planificación y la ejecución de la entrega.
5. Exclusiones expresas (Fuera de alcance)
   - Tracking en tiempo real: No se implementará seguimiento GPS en vivo de los vehículos de reparto.
   - Pagos y facturación: El MVP no incluirá cotización de envíos ni pasarelas de pago.
6. Riesgos o dependencias que pueden afectar el alcance
   - Dependencia externa (OSRM): Posibles limitaciones, latencia o caídas en la API de OSRM que puedan dificultar o retrasar el cálculo de la ruta durante el desarrollo y las pruebas.
   - Curva de integración: Demoras imprevistas en la correcta orquestación de seguridad (Keycloak) a través del API Gateway y los microservicios e incertidumbre acerca del uso de la API de OSRM.
