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

El principal aporte del sistema radica en la automatización del proceso de planificación operativa: consolida paquetes por sectores de entrega y aplica un algoritmo de ruteo inteligente que resuelve el trayecto más eficiente, considerando las restricciones físicas de la flota (límites de volumen y peso por vehículo) y la disponibilidad del personal asignado. Asimismo, garantiza la viabilidad del envío exigiendo un umbral mínimo de 6 paquetes por lote planificado.
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
- Optimización de la ruta según tiempo y distancia.

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
| F-06 | Planificar rutas de entrega | Administrador | Planificar las rutas de entrega según el sector para minimizar el recorrido. | Se muestra una secuencia de puntos de entrega y la ruta propuesta. | ALTA |
| F-07 | Gestionar el ordenamiento de paquetes por sector | Administrador | Gestionar cómo se ordenarán los paquetes según el sector de envio | Se muestra los paquetes ordenados por sector para realizar el envío | ALTA |
| F-08 | Consultar ruta | Repartidor | Consulta la ruta que debe realizar para entregar los paquetes | Visualizacion e indicaciones sobre la ruta especifica que se consulta | ALTA |
| F-09 | Actualizar estado de envío | Repartidor | Actualizar el estado del envío desde el momento que salio hasta el momento de entrega | Estado de el envío actualizado por el repartidor (Posibles estados: Pendiente, En camino, Entregado) | ALTA |
| F-10 | Registrar estado de la entrega | Repartidor | Registrar si el paquete fue entregado o si no pudo concretarse la entrega | Queda guardado el resultado y el momento del intento. | ALTA |

## Reglas de negocio

- Se deberá indicar el alto, ancho, largo y peso del paquete al registrarlo.
- El peso y dimensión del paquete no deberá sobrepasar el límite establecido por vehículo.
- Se deberá asignar un peso máximo que podrá cargar el vehiculo
- Se podrá asignar a los envíos a repartidores que se encuentran disponibles.
- Se podrá asignar paquetes a vehículos si se encuentran disponibles.
- Se podrá consultar el estado del envío en cualquier momento que el repartidor desee.
- Cada paquete deberá tener indicado una dirección de envío (como coordenadas: latitud y longitud)
- Cada paquete deberá poder identificarse con un ID único.
- Cada paquete deberá contener el nombre del cliente.
- Cada paquete deberá pertenecer a un envío y a un sector de entrega.
- Un envío debe tener al menos 6 paquetes para poder planificarse.
- Solo se podrán planificar envíos con paquetes PENDIENTE, es decir que hayan llegado a la sucursal y hayan sido registrados por el administrador esperando a ser entregados.
- La ruta planificada deberá incluir los destinos de todos los paquetes asignados al envío.
- El paquete tendrá los siguientes estados válidos (PENDIENTE, ASIGNADO, EN_CAMINO, ENTREGADO, INCIDENCIA)

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
- Sistema de Logística de Paquetes → Keycloak: Valida tokens y permisos (OIDC / JWT).

# Diagrama de contenedor C4

### Componentes

- API Gateway: punto de entrada y filtro para las solicitudes consultando Keycloak si el usuario tiene autorización. Las enruta a ms-paquetes o ms-logistica.
- ms-paquetes: administra la información de paquetes, clientes, vehículos, repartidores y sucursales.
- ms-logistica: coordina la creación y planificación de envíos, asigna recursos, calcula rutas y registra el seguimiento operativo y los resultados de entrega.
- Base de datos de ms-paquetes: almacena los datos que administra ese microservicio, como paquetes, vehículos, repartidores y sucursales.
- Base de datos de ms-logistica: almacena los datos operativos de logística, por ejemplo envíos, asignaciones, rutas y resultados de entrega.
- OSRM: sistema externo que provee el cálculo de rutas y distancias.
- Keycloak: Gestiona autenticación y autorización mediante roles.

### Relaciones
- El Administrador envía solicitudes por HTTPS al API Gateway para gestionar información y planificar envíos.
- El Repartidor accede mediante HTTPS al gateway para consultar su recorrido y registrar resultados.
- El API Gateway deriva las solicitudes a los endpoints de ms-paquetes o ms-logistica, mediante APIs HTTP/REST.
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
| ms-paquetes | Sucursal | GET | `/sucursal` | Obtener la sucursal | Administrador |
| ms-paquetes | Sucursal | POST | `/sucursal` | Registrar sucursal | Administrador |
| ms-paquetes | Sucursal | PUT | `/sucursal` | actualiza datos de la sucursal | Administrador |
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
El sistema utilizará Key Cloak como proveedor de identidad para autenticar a los usuarios. Al iniciar sesión, el usuario obtendrá un token de acceso que deberá incluir en las solicitudes a los recursos protegidos.
Se definirán dos roles:
Administrador: podrá gestionar paquetes, sucursales, vehículos y repartidores, y organizar envíos, asignaciones y rutas.
Repartidor: podrá consultar los envíos y rutas que tiene asignados y registrar los resultados de la entrega
El API Gateway y los microservicios validarán el token y comprobarán que el usuario tenga el rol requerido para cada operación. Una solicitud sin un token válido recibirá 401 Unauthorized. Si el usuario está autenticado, pero no tiene el rol necesario, recibirá 403 Forbidden.

### Comprobaciones

Camino exitoso: el usuario con rol de administrador registra un paquete y el sistema devuelve “paquete registrado exitosamente”

Camino sin éxito: el usuario con rol de repartidor intenta registrar un vehículo y el sistema responde con 403 Forbidden.

## Tecnologías

1. Tecnologías y versiones principales El desarrollo del backend estará sustentado en el ecosistema Java y un motor relacional de alto rendimiento:
    - Lenguaje y Framework Principal: Java (versión 21+) utilizando Spring Boot. Se emplea para construir el API Gateway (con Spring Cloud Gateway) y los microservicios (ms-paquetes y ms-logistica).
    - Gestión de dependencias y construcción: Maven, utilizado también para empaquetar como un archivo .jar hacia los microservicios. 
    - Persistencia: MariaDB (12.3.3 LTS) como motor relacional, interactuando a través de Spring Data JPA (Hibernate) mediante el driver mariadb-java-client. Se administrarán dos bases de datos lógicas independientes (bd_paquetes y bd_logistica) para garantizar el aislamiento de dominios entre microservicios.
2. Dependencias de infraestructura y servicios externos Los componentes de infraestructura se ejecutarán en contenedores aislados:
    - Keycloak (Identity and Access Management): Servidor de autenticación externo para emisión y verificación de tokens JWT mediante OAuth2/OIDC y gestión de roles. 
    - Motor de Base de Datos: Instancia del servidor MariaDB hospedando de forma independiente las bases de datos bd_paquetes y bd_logistica. 
    - OSRM (Open Source Routing Machine): Servicio externo integrado mediante peticiones HTTP/JSON para el cálculo de rutas y tiempos de recorrido.
3. Configuración por entorno: la configuración del sistema se gestionará mediante perfiles de Spring Boot (application-dev.yml, application-prod.yml), externalizando parámetros sensibles mediante variables de entorno:
    - Variables clave inyectadas: Cadena de conexión JDBC de MariaDB (jdbc:mariadb://...), credenciales de acceso, URL del servidor de
    - Keycloak para los filtros de Spring Security, puerto de exposición y endpoint base de la API externa de OSRM.
4. Procedimiento de arranque de la solución: toda la solución estará orquestada con Docker y Docker Compose para permitir el despliegue del backend completo mediante un único comando. El archivo docker-compose.yml en la raíz definirá los servicios: api-gateway, ms-paquetes, ms-logistica, mariadb-db y keycloak.

### Instrucciones de ejecución:

1. Clonar el repositorio y ubicarse en la carpeta raíz. 
2. Configurar el archivo .env local tomando como referencia el archivo .env.example. 
3. Ejecutar en la terminal el comando: docker-compose up --build -d
4. Compose compilará los microservicios Java (usando construcciones multi-stage con Maven) e iniciará la instancia de MariaDB ejecutando los scripts de inicialización .sql para crear las bases de datos bd_paquetes y bd_logistica. 
5. Una vez levantados los servicios, el API Gateway estará listo en el puerto configurado (ej. http://localhost:8080) para recibir solicitudes y enrutarlas con los filtros de seguridad de Keycloak.

## Colección de ejecutables
1. Herramienta y Organización de la Colección La entrega se realizará mediante un archivo JSON exportado de Postman (Colección v2.1) acompañado de un archivo de entorno (entorno-logistica-local.json). La colección estará estructurada en carpetas numeradas para garantizar un flujo lógico de ejecución que refleje el proceso principal del negocio:
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
       Fallo de Autorización (Keycloak): Utilizar la variable {{repartidor_token}} para intentar consumir un endpoint administrativo, como registrar una nueva sucursal (POST /sucursal). El API Gateway o microservicio debe retornar HTTP 403 Forbidden. 
        - Asignación de Recursos no disponibles: Intentar asignar a un envío un repartidor o vehículo cuyo atributo disponible se encuentre en false. El sistema deberá rechazar la petición con un error 422 Unprocessable Entity, ya que viola una regla de negocio del dominio (“Solo se pueden asignar recursos disponibles”)

## Compromiso del equipo

1. Proceso central que quedará funcionando: el equipo se compromete a implementar un MVP del Sistema de Logística de Paquetes que permita organizar paquetes por sector y gestionar el ciclo completo del proceso de despacho y entrega de última milla.
2. Funcionalidades obligatorias
   - Gestión del dominio: Alta y registro de paquetes (con destino, peso y dimensiones), sucursales, vehículos y repartidores.
   - Asignación de recursos: Asignación de paquetes a recursos disponibles (vehículo y repartidor), validando estrictamente la compatibilidad de la carga (capacidad de peso y volumen) con cada vehículo y según el sector de entrega que cubrirá el vehículo.
   - Planificación: Trazado de rutas por tiempo y distancia, devolviendo la ruta más eficiente.
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
